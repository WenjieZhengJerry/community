package cn.lngfun.community.community.controller;

import cn.lngfun.community.community.dto.AccessTokenDTO;
import cn.lngfun.community.community.dto.GithubUser;
import cn.lngfun.community.community.provider.GithubProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

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
                           @RequestParam(name = "state") String state) {
        AccessTokenDTO accessTokenDTO = new AccessTokenDTO();
        accessTokenDTO.setClient_id(clientId);
        accessTokenDTO.setClient_secret(clientSecret);
        accessTokenDTO.setCode(code);
        accessTokenDTO.setRedirect_uri(redirectUri);
        accessTokenDTO.setState(state);

        String accessToken = githubProvider.getAccessToken(accessTokenDTO);
        GithubUser githubUser = githubProvider.getUser(accessToken);
        System.out.println(githubUser.getName());
        return "index";
    }
}
