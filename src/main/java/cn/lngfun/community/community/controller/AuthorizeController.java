package cn.lngfun.community.community.controller;

import cn.lngfun.community.community.dto.AccessTokenDTO;
import cn.lngfun.community.community.dto.GithubUser;
import cn.lngfun.community.community.mapper.UserMapper;
import cn.lngfun.community.community.model.User;
import cn.lngfun.community.community.provider.GithubProvider;
import cn.lngfun.community.community.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.UUID;

@Controller
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
        if(githubUser != null && githubUser.getId() != null) {
            //登录成功，写入cookie
            User user = new User();
            String token = UUID.randomUUID().toString();
            user.setToken(token);
            user.setAccountId(String.valueOf(githubUser.getId()));
            user.setName(githubUser.getName());
            user.setBio(githubUser.getBio());
            user.setAvatarUrl(githubUser.getAvatarUrl());
            userService.createOrUpdate(user);

            response.addCookie(new Cookie("token", token));
            return "redirect:/";
        }else {
            //登录失败，重新登陆
            return "redirect:/";
        }
    }

    @GetMapping("/logout")
    public String logout(HttpServletRequest request, HttpServletResponse response) {
        request.getSession().removeAttribute("user");
        Cookie cookie = new Cookie("token", null);
        cookie.setMaxAge(0);
        response.addCookie(cookie);

        return "redirect:/";
    }
}
