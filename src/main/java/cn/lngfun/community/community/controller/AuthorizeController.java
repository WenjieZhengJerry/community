package cn.lngfun.community.community.controller;

import cn.lngfun.community.community.dto.AccessTokenDTO;
import cn.lngfun.community.community.dto.GithubUser;
import cn.lngfun.community.community.dto.ResultDTO;
import cn.lngfun.community.community.exception.CustomizeErrorCode;
import cn.lngfun.community.community.mapper.UserMapper;
import cn.lngfun.community.community.model.User;
import cn.lngfun.community.community.provider.GithubProvider;
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

    @Value("${github.client.id}")
    private String clientId;

    @Value("${github.client.secret}")
    private String clientSecret;

    @Value("${github.redirect.uri}")
    private String redirectUri;

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
    public String callback(@RequestParam(name = "code") String code,
                           @RequestParam(name = "state") String state,
                           HttpServletResponse response) {
        AccessTokenDTO accessTokenDTO = new AccessTokenDTO();
        accessTokenDTO.setClient_id(clientId);
        accessTokenDTO.setClient_secret(clientSecret);
        accessTokenDTO.setCode(code);
        accessTokenDTO.setRedirect_uri(redirectUri);
        accessTokenDTO.setState(state);

        String accessToken = githubProvider.getAccessToken(accessTokenDTO);
        GithubUser githubUser = githubProvider.getUser(accessToken);
        if (githubUser != null && githubUser.getId() != null) {
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
            userService.createOrUpdate(user);

            response.addCookie(new Cookie("token", token));
            return "redirect:/";
        } else {
            //登录失败，重新登陆
            log.error("GitHub登录失败,{}", githubUser);
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
            response.addCookie(new Cookie("tEmail", email));
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
     * 退出登录
     *
     * @param request
     * @param response
     * @return
     */
    @GetMapping("/logout")
    public String logout(HttpServletRequest request, HttpServletResponse response) {
        request.getSession().removeAttribute("user");
        request.getSession().removeAttribute("unreadCount");
        Cookie cookie = new Cookie("token", null);
        cookie.setMaxAge(0);
        response.addCookie(cookie);

        return "redirect:/";
    }
}
