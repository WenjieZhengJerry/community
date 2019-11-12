package cn.lngfun.community.community.controller;

import cn.lngfun.community.community.dto.*;
import cn.lngfun.community.community.exception.CustomizeErrorCode;
import cn.lngfun.community.community.model.User;
import cn.lngfun.community.community.provider.GithubProvider;
import cn.lngfun.community.community.provider.QQProvider;
import cn.lngfun.community.community.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import sun.misc.BASE64Encoder;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.UUID;

@Controller
@Slf4j
public class AuthorizeController {

    @Autowired
    private GithubProvider githubProvider;

    @Autowired
    private QQProvider qqProvider;

    //github登录所需参数
    @Value("${github.client.id}")
    private String gClientId;

    @Value("${github.client.secret}")
    private String gClientSecret;

    @Value("${github.redirect.uri}")
    private String gRedirectUri;

    //QQ登录所需参数
    @Value("${qq.client.id}")
    private String qqClientId;

    @Value("${qq.client.secret}")
    private String qqClientSecret;

    @Value("${qq.redirect.uri}")
    private String qqRedirectUri;

    @Value("${qq.grant.type}")
    private String qqGrantType;

    //默认头像
    @Value("${default.avatarurl}")
    private String defaultAvatarurl;

    @Autowired
    private UserService userService;

    /**
     * 调用Github登录的API，整个过程调用3个接口，包括获取code、获取access_token、获取user信息
     * 步骤：
     * 1.用户点击登录按钮
     * 2.调用获取code的接口接收返回的code
     * 3.利用接收到的code再调用获取access_token的接口接收access_token
     * 4.利用接收到的access_token再调用获取user的接口接收user信息
     * 5.最后通过接收到的user信息返回给用户
     *
     * @param code
     * @param state
     * @return
     */
    @GetMapping("/callback")
    public String callback(@RequestParam(name = "code") String code,//2.调用获取code的接口接收返回的code
                           @RequestParam(name = "state") String state,
                           HttpServletRequest request,
                           HttpServletResponse response) {
        GithubAccessTokenDTO githubAccessTokenDTO = new GithubAccessTokenDTO();
        githubAccessTokenDTO.setClient_id(gClientId);
        githubAccessTokenDTO.setClient_secret(gClientSecret);
        githubAccessTokenDTO.setCode(code);
        githubAccessTokenDTO.setRedirect_uri(gRedirectUri);
        githubAccessTokenDTO.setState(state);

        //3.利用接收到的code再调用获取access_token的接口接收access_token
        String accessToken = githubProvider.getAccessToken(githubAccessTokenDTO);
        //4.利用接收到的access_token再调用获取user的接口接收user信息
        GithubUser githubUser = githubProvider.getUser(accessToken);
        if (githubUser != null && githubUser.getId() != null) {
            if ("bind".equals(state)) {
                //绑定GitHub
                User user = (User) request.getSession().getAttribute("user");
                if (user == null) {
                    return ResultDTO.errorOf(CustomizeErrorCode.NO_LOGIN).getMessage();
                } else {
                    return userService.bindGithub(String.valueOf(githubUser.getId()), user);
                }
            }

            //登录成功，写入cookie
            User user = new User();
            String token = UUID.randomUUID().toString();
            user.setToken(token);
            user.setAccountId(String.valueOf(githubUser.getId()));
            //防止名称为空
            if (StringUtils.isBlank(githubUser.getName())) {
                githubUser.setName("未设置昵称");
            }
            user.setName(githubUser.getName());
            user.setBio(githubUser.getBio());
            user.setAvatarUrl(githubUser.getAvatarUrl());
            user.setEmail(githubUser.getEmail());
            user.setCompany(githubUser.getCompany());
            user.setBlog(githubUser.getBlog());
            user.setLocation(githubUser.getLocation());
            userService.createOrUpdate(user, "GitHub");

            Cookie cookie = new Cookie("token", token);
            cookie.setMaxAge(2592000);//30天有效期
            response.addCookie(cookie);
            return "redirect:/";
        } else {
            //登录失败，重新登陆
            log.error("GitHub登录失败,{}", githubUser);
            return "redirect:/";
        }
    }

    /**
     * QQ登录
     * 1、获取Authorization Code
     * 2、通过Authorization Code获取Access Token
     * 3、获取openId
     * 4、获取登录用户的信息
     *
     * @param code
     * @param response
     * @return
     */
    @GetMapping("/qqCallback")
    public String qqCallback(@RequestParam(name = "code") String code,//1、获取Authorization Code
                             @RequestParam(name = "state") String state,
                             HttpServletRequest request,
                             HttpServletResponse response) {
        QQAccessTokenDTO qqAccessTokenDTO = new QQAccessTokenDTO();
        qqAccessTokenDTO.setClient_id(qqClientId);
        qqAccessTokenDTO.setClient_secret(qqClientSecret);
        qqAccessTokenDTO.setCode(code);
        qqAccessTokenDTO.setRedirect_uri(qqRedirectUri);
        qqAccessTokenDTO.setGrantType(qqGrantType);

        //2、通过Authorization Code获取Access Token
        String accessToken = qqProvider.getAccessToken(qqAccessTokenDTO);
        //3、获取openId
        String openid = qqProvider.getOpenid(accessToken);

        if ("bind".equals(state)) {
            //绑定QQ
            User user = (User) request.getSession().getAttribute("user");
            if (user == null) {
                return ResultDTO.errorOf(CustomizeErrorCode.NO_LOGIN).getMessage();
            } else {
                return userService.bindQQ(openid, user);
            }
        }

        //4、获取登录用户的信息
        QQUserDTO qqUserDTO = qqProvider.getUser(accessToken, qqClientId, openid);

        if (qqUserDTO != null) {
            //登录成功，写入cookie
            User user = new User();
            String token = UUID.randomUUID().toString();
            user.setToken(token);
            user.setOpenid(openid);
            //防止名称为空
            if (StringUtils.isBlank(qqUserDTO.getNickname())) {
                qqUserDTO.setNickname("未设置昵称");
            }
            user.setName(qqUserDTO.getNickname());
            user.setAvatarUrl(qqUserDTO.getFigureurlQq());
            user.setLocation(qqUserDTO.getCity());
            userService.createOrUpdate(user, "QQ");

            Cookie cookie = new Cookie("token", token);
            cookie.setMaxAge(2592000);//30天有效期
            response.addCookie(cookie);
            return "redirect:/";
        } else {
            //登录失败，重新登陆
            log.error("QQ登录失败");
            return "redirect:/";
        }
    }

    /**
     * 通过邮箱登录
     * 1.判断邮箱是否存在
     * 2.判断密码是否正确
     * 3.判断token是否过期
     * 4.把从数据库中读取的token存入cookie中
     * 5.完成登录
     *
     * @param email
     * @param password
     * @param remember
     * @param response
     * @return
     */
    @PostMapping("/login")
    @ResponseBody
    public Object loginByEmail(@RequestParam(value = "email") String email,
                               @RequestParam(value = "password") String password,
                               @RequestParam(value = "remember", required = false) String remember,
                               HttpServletResponse response) throws NoSuchAlgorithmException, UnsupportedEncodingException {
        //前端校验
        if (!email.matches("^[a-zA-Z0-9_-]+@[a-zA-Z0-9_-]+(\\.[a-zA-Z0-9_-]+)+$") || email == null || "".equals(email)) {
            return ResultDTO.errorOf(CustomizeErrorCode.EMAIL_FORMAT_WRONG);
        }
        if (!password.matches("^(?=.*[A-Za-z])(?=.*\\d)[A-Za-z\\d]{6,18}$") || password == null || "".equals(password)) {
            return ResultDTO.errorOf(CustomizeErrorCode.PASSWORD_FORMAT_WRONG);
        }
        //记住邮箱
        if (remember != null && "1".equals(remember)) {
            //添加到cookie里
            Cookie cookie = new Cookie("tEmail", email);
            cookie.setMaxAge(2592000);//30天有效期
            response.addCookie(cookie);
        }
        //md5加密，确定计算方法
        MessageDigest md5 = MessageDigest.getInstance("MD5");
        BASE64Encoder base64en = new BASE64Encoder();
        //加密后的密码
        password = base64en.encode(md5.digest(password.getBytes("utf-8")));
        //登录
        return userService.loginByEmail(email, password, response);
    }

    /**
     * 邮箱注册
     *
     * @param email
     * @param password
     * @param authCode
     * @param request
     * @return
     * @throws NoSuchAlgorithmException
     * @throws UnsupportedEncodingException
     */
    @PostMapping("/register")
    @ResponseBody
    public Object register(@RequestParam(name = "email") String email,
                           @RequestParam(name = "password") String password,
                           @RequestParam(name = "authCode") String authCode,
                           HttpServletRequest request) throws NoSuchAlgorithmException, UnsupportedEncodingException {
        //前端校验
        if (!password.matches("^(?=.*[A-Za-z])(?=.*\\d)[A-Za-z\\d]{6,18}$") || password == null || "".equals(password)) {
            return ResultDTO.errorOf(CustomizeErrorCode.PASSWORD_FORMAT_WRONG);
        }

        //获取session里的验证码
        String sessionAuthCode = String.valueOf(request.getSession().getAttribute("authCode"));
        if (sessionAuthCode == null) {
            //验证码过期
            return ResultDTO.errorOf(CustomizeErrorCode.AUTH_CODE_INVALID);
        } else if (!sessionAuthCode.toLowerCase().equals(authCode.toLowerCase())) {
            //验证码错误
            return ResultDTO.errorOf(CustomizeErrorCode.AUTH_CODE_WRONG);
        } else {
            //验证码正确，注册成功,填装数据并录入数据库
            User user = new User();
            String token = UUID.randomUUID().toString();
            user.setToken(token);
            user.setEmail(email);
            user.setName("未设置昵称");
            //md5加密，确定计算方法
            MessageDigest md5 = MessageDigest.getInstance("MD5");
            BASE64Encoder base64en = new BASE64Encoder();
            //加密密码
            password = base64en.encode(md5.digest(password.getBytes("utf-8")));
            user.setPassword(password);
            user.setGmtCreate(System.currentTimeMillis());
            user.setGmtModified(user.getGmtCreate());
            user.setAvatarUrl(defaultAvatarurl);
            userService.register(user);

            return ResultDTO.okOf();
        }
    }


    /**
     * 退出登录
     *
     * @param request
     * @param response
     * @return
     */
    @PostMapping("/logout")
    @ResponseBody
    public Object logout(HttpServletRequest request, HttpServletResponse response,
                         @RequestParam(name = "redirect") String redirect) {
        request.getSession().removeAttribute("user");
        request.getSession().removeAttribute("unreadCount");
        Cookie cookie = new Cookie("token", null);
        cookie.setMaxAge(0);
        response.addCookie(cookie);

        if ("true".equals(redirect)) {
            //如果退出时的页面是需要登录才能访问的则退出后自动跳转到主页
            return ResultDTO.errorOf(CustomizeErrorCode.RETURN_TO_INDEX);
        } else {
            //否则保持当前页面
            return ResultDTO.okOf();
        }
    }
}
