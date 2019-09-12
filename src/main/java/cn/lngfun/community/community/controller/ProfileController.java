package cn.lngfun.community.community.controller;

import cn.lngfun.community.community.dto.FollowDTO;
import cn.lngfun.community.community.dto.PagingDTO;
import cn.lngfun.community.community.dto.ResultDTO;
import cn.lngfun.community.community.exception.CustomizeErrorCode;
import cn.lngfun.community.community.exception.CustomizeException;
import cn.lngfun.community.community.model.Notification;
import cn.lngfun.community.community.model.User;
import cn.lngfun.community.community.provider.EmailProvider;
import cn.lngfun.community.community.service.*;
import com.sun.org.apache.xpath.internal.objects.XNull;
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
import java.util.List;
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

    @Autowired
    private FollowService followService;

    @Autowired
    private CollectionService collectionService;

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
        } else if ("follows".equals(action)) {
            model.addAttribute("section", "follows");
            model.addAttribute("sectionName", "我的关注");

            List<FollowDTO> follows = followService.list(user.getId());
            model.addAttribute("follows", follows);
        } else if ("collection".equals(action)) {
            model.addAttribute("section", "collection");
            model.addAttribute("sectionName", "我的收藏");

            PagingDTO pagingDTO = collectionService.list(user.getId(), page, size);
            model.addAttribute("pagingDTO", pagingDTO);
        } else if ("setting".equals(action)) {
            model.addAttribute("section", "setting");
            model.addAttribute("sectionName", "设置");
        } else {
            throw new CustomizeException(CustomizeErrorCode.RESOURCE_NOT_FOUND);
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
        //判断邮箱是否被绑定
        if (userService.hasEmail(email) != null) {
            return ResultDTO.errorOf(CustomizeErrorCode.EMAIL_IS_EXIST);
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
     * 查看个人资料
     *
     * @param page
     * @param size
     * @param id
     * @param model
     * @param request
     * @return
     */
    @GetMapping("/people/{id}")
    public String people(@PathVariable(name = "id") Long id,
                         @RequestParam(name = "section", defaultValue = "questions") String section,
                         @RequestParam(name = "page", defaultValue = "1") Integer page,
                         @RequestParam(name = "size", defaultValue = "10") Integer size,
                         Model model,
                         HttpServletRequest request) {

        User user = userService.findUserById(id);
        //找不到的用户
        if (user == null) {
            throw new CustomizeException(CustomizeErrorCode.USER_NOT_FOUND);
        }
        //把密码去掉
        user.setPassword(null);
        //判断点击的人是否为登录后的自己
        User currentUser = (User) request.getSession().getAttribute("user");
        if (currentUser != null && followService.isFollowed(id, currentUser.getId()) != null) {
            //判断是否关注
            model.addAttribute("isFollowed", true);
        }
        model.addAttribute("user", user);
        model.addAttribute("userName", user.getName());
        //获取关注数
        Integer followCount = followService.countFollowById(id);
        //获取粉丝数
        Integer followerCount = followService.countFollowerById(id);
        //获取收藏数
        Integer collectionCount = collectionService.countByUserId(id);
        //获取问题数
        Integer questionCount = 0;
        PagingDTO questions = questionService.list(id, page, size);
        questionCount = questions.getData().size();

        model.addAttribute("followCount", followCount);
        model.addAttribute("followerCount", followerCount);
        model.addAttribute("questionCount", questionCount);
        model.addAttribute("collectionCount", collectionCount);

        if ("questions".equals(section)) {
            model.addAttribute("pagingDTO", questions);
            model.addAttribute("sectionName", "提问");
            model.addAttribute("section", section);
        } else if ("follow".equals(section)) {
            List<FollowDTO> followDTOS = followService.list(id);
            model.addAttribute("followDTOS", followDTOS);
            model.addAttribute("sectionName", "关注");
            model.addAttribute("section", section);
        } else if ("collection".equals(section)) {
            PagingDTO pagingDTO = collectionService.list(id, page, size);
            model.addAttribute("pagingDTO", pagingDTO);
            model.addAttribute("sectionName", "收藏");
            model.addAttribute("section", section);
        } else {
            throw new CustomizeException(CustomizeErrorCode.RESOURCE_NOT_FOUND);
        }

        return "people";
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
        //如果数据库的密码不为null，则判断旧密码是否正确
        if (dbPassword != null) {
            //从前端传来的旧密码，加密后的旧密码
            oldPassword = base64en.encode(md5.digest(oldPassword.getBytes("utf-8")));
            //判断旧密码是否输入正确
            if (!oldPassword.equals(dbPassword)) {
                return ResultDTO.errorOf(CustomizeErrorCode.PASSWORD_WRONG);
            }
        }
        //加密新密码
        newPassword = base64en.encode(md5.digest(newPassword.getBytes("utf-8")));
        //修改密码
        user.setPassword(newPassword);
        userService.updatePassword(user);

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
