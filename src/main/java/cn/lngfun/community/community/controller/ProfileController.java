package cn.lngfun.community.community.controller;

import cn.lngfun.community.community.dto.PagingDTO;
import cn.lngfun.community.community.dto.ResultDTO;
import cn.lngfun.community.community.exception.CustomizeErrorCode;
import cn.lngfun.community.community.exception.CustomizeException;
import cn.lngfun.community.community.model.Notification;
import cn.lngfun.community.community.model.User;
import cn.lngfun.community.community.provider.EmailProvider;
import cn.lngfun.community.community.service.NotificationService;
import cn.lngfun.community.community.service.QuestionService;
import cn.lngfun.community.community.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import sun.misc.BASE64Encoder;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Timer;
import java.util.TimerTask;
import java.util.UUID;

@Controller
public class ProfileController {

    @Autowired
    private QuestionService questionService;

    @Autowired
    private NotificationService notificationService;

    @Autowired
    private UserService userService;

    /**
     * 跳转到个人空间
     *
     * @param action
     * @param model
     * @param request
     * @param page
     * @param size
     * @return
     */
    @GetMapping("/profile/{action}")
    public String profile(@PathVariable(name = "action") String action, Model model, HttpServletRequest request,
                          @RequestParam(name = "page", defaultValue = "1") Integer page,
                          @RequestParam(name = "size", defaultValue = "10") Integer size) {

        //判断登录状态
        User user = (User) request.getSession().getAttribute("user");
        if (user == null) {
            throw new CustomizeException(CustomizeErrorCode.NO_LOGIN);
        }

        if ("questions".equals(action)) {
            model.addAttribute("section", "questions");
            model.addAttribute("sectionName", "我的提问");

            PagingDTO pagingDTO = questionService.list(user.getId(), page, size);
            model.addAttribute("pagingDTO", pagingDTO);
        } else if ("replies".equals(action)) {
            model.addAttribute("section", "replies");
            model.addAttribute("sectionName", "最新回复");

            PagingDTO pagingDTO = notificationService.list(user.getId(), page, size);
            model.addAttribute("pagingDTO", pagingDTO);
        } else if ("information".equals(action)) {
            model.addAttribute("section", "information");
            model.addAttribute("sectionName", "我的资料");
        } else if ("setting".equals(action)) {
            model.addAttribute("section", "setting");
            model.addAttribute("sectionName", "设置");
        }

        return "profile";
    }

    /**
     * 编辑个人资料
     *
     * @param model
     * @param request
     * @param name
     * @param bio
     * @param company
     * @param blog
     * @param location
     * @return
     */
    @PostMapping("/editProfile")
    public String eidtProfile(Model model, HttpServletRequest request,
                              @RequestParam(name = "name") String name,
                              @RequestParam(name = "bio") String bio,
                              @RequestParam(name = "company") String company,
                              @RequestParam(name = "blog") String blog,
                              @RequestParam(name = "location") String location) {
        //判断登录状态
        User user = (User) request.getSession().getAttribute("user");
        if (user == null) {
            throw new CustomizeException(CustomizeErrorCode.NO_LOGIN);
        }

        user.setName(name);
        user.setBio(bio);
        user.setCompany(company);
        user.setBlog(blog);
        user.setLocation(location);
        user.setGmtModified(System.currentTimeMillis());
        userService.updateProfile(user);

        model.addAttribute("section", "information");
        model.addAttribute("sectionName", "我的资料");

        return "profile";
    }

    /**
     * 查看个人资料
     *
     * @param page
     * @param size
     * @param id
     * @param model
     * @param request
     * @return
     */
    @GetMapping("/people")
    public String people(@RequestParam(name = "page", defaultValue = "1") Integer page,
                         @RequestParam(name = "size", defaultValue = "10") Integer size,
                         @RequestParam(name = "id") Long id,
                         Model model,
                         HttpServletRequest request) {

        User user = userService.findUserById(id);
        //找不到的用户
        if (user == null) {
            throw new CustomizeException(CustomizeErrorCode.USER_NOT_FOUND);
        }
        //判断点击的人是否为登录后的自己
        if (user.equals(request.getSession().getAttribute("user"))) {
            model.addAttribute("section", "questions");
            model.addAttribute("sectionName", "我的提问");

            PagingDTO pagingDTO = questionService.list(user.getId(), page, size);
            model.addAttribute("pagingDTO", pagingDTO);

            return "profile";
        }

        PagingDTO questions = questionService.list(id, page, size);
        model.addAttribute("user", user);
        model.addAttribute("userName", user.getName());
        model.addAttribute("pagingDTO", questions);

        return "people";
    }

    /**
     * 获取验证码
     *
     * @param email
     * @param request
     * @return
     */
    @PostMapping("/getAuthCode")
    @ResponseBody
    public Object getAuthCode(@RequestParam(name = "email") String email,
                              HttpServletRequest request) {
        //前端校验
        if (!email.matches("^[a-zA-Z0-9_-]+@[a-zA-Z0-9_-]+(\\.[a-zA-Z0-9_-]+)+$") || email == null || "".equals(email)) {
            return ResultDTO.errorOf(CustomizeErrorCode.EMAIL_FORMAT_WRONG);
        }
        //判断登录状态
        User user = (User) request.getSession().getAttribute("user");
        if (user == null) {
            return ResultDTO.errorOf(CustomizeErrorCode.NO_LOGIN);
        }
        //获取随机6位数的验证码
        String authCode = UUID.randomUUID().toString().substring(0, 6);
        //保存验证码到session中
        request.getSession().setAttribute("authCode", authCode);
        //10分钟后移除session中的验证码
        this.removeAttrbute(request.getSession(), "authCode", 10);
        //发送验证码到指定邮箱并返回结果
        return EmailProvider.getAuthCode(email, authCode);
    }

    /**
     * 绑定或修改邮箱
     *
     * @param email
     * @param authCode 前端传来的验证码
     * @param request
     * @return
     */
    @PostMapping("/editEmail")
    @ResponseBody
    public Object editEmail(@RequestParam(name = "setting_email") String email,
                            @RequestParam(name = "authCode") String authCode,
                            HttpServletRequest request) {
        //获取session里的验证码
        String sessionAuthCode = String.valueOf(request.getSession().getAttribute("authCode"));
        if (sessionAuthCode == null) {
            //验证码过期
            return ResultDTO.errorOf(CustomizeErrorCode.AUTH_CODE_INVALID);
        } else if (!sessionAuthCode.toLowerCase().equals(authCode.toLowerCase())) {
            //验证码错误
            return ResultDTO.errorOf(CustomizeErrorCode.AUTH_CODE_WRONG);
        } else {
            //验证码正确，保存邮箱到数据库
            User user = (User) request.getSession().getAttribute("user");
            user.setEmail(email);
            userService.updateProfile(user);

            return ResultDTO.okOf();
        }
    }

    /**
     * 修改密码
     *
     * @param oldPassword
     * @param newPassword
     * @param request
     * @return
     * @throws NoSuchAlgorithmException
     * @throws UnsupportedEncodingException
     */
    @PostMapping("/changePassword")
    @ResponseBody
    public Object changePassword(@RequestParam(name = "old_password") String oldPassword,
                                 @RequestParam(name = "new_password") String newPassword,
                                 HttpServletRequest request) throws NoSuchAlgorithmException, UnsupportedEncodingException {
        //前端校验
        if (!oldPassword.matches("^(?=.*[A-Za-z])(?=.*\\d)[A-Za-z\\d]{6,18}$") || oldPassword == null || "".equals(oldPassword)) {
            return ResultDTO.errorOf(CustomizeErrorCode.PASSWORD_FORMAT_WRONG);
        }
        if (!newPassword.matches("^(?=.*[A-Za-z])(?=.*\\d)[A-Za-z\\d]{6,18}$") || newPassword == null || "".equals(newPassword)) {
            return ResultDTO.errorOf(CustomizeErrorCode.PASSWORD_FORMAT_WRONG);
        }
        //判断登录状态
        User user = (User) request.getSession().getAttribute("user");
        if (user == null) {
            return ResultDTO.errorOf(CustomizeErrorCode.NO_LOGIN);
        }
        //数据库中获取的密码
        String dbPassword = userService.findUserById(user.getId()).getPassword();
        //md5加密，确定计算方法
        MessageDigest md5 = MessageDigest.getInstance("MD5");
        BASE64Encoder base64en = new BASE64Encoder();
        //从前端传来的旧密码，加密后的旧密码
        oldPassword = base64en.encode(md5.digest(oldPassword.getBytes("utf-8")));
        //判断旧密码是否输入正确
        if (!oldPassword.equals(dbPassword)) {
            return ResultDTO.errorOf(CustomizeErrorCode.PASSWORD_WRONG);
        }
        //加密新密码
        newPassword = base64en.encode(md5.digest(newPassword.getBytes("utf-8")));
        //修改密码
        user.setPassword(newPassword);
        userService.updateProfile(user);

        return ResultDTO.okOf();
    }

    /**
     * 指定时间内移除session的某个元素
     *
     * @param session
     * @param attrName
     * @param minute
     */
    private void removeAttrbute(final HttpSession session, final String attrName, final int minute) {
        final Timer timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                // 删除session中存的验证码
                session.removeAttribute(attrName);
                timer.cancel();
            }
        }, minute * 60 * 1000);
    }

}
