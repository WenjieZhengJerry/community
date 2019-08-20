package cn.lngfun.community.community.service;

import cn.lngfun.community.community.exception.CustomizeErrorCode;
import cn.lngfun.community.community.mapper.UserMapper;
import cn.lngfun.community.community.model.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;

@Service
public class UserService {

    @Autowired
    private UserMapper userMapper;

    /**
     * 第三方登录
     * @param user
     */
    public void createOrUpdate(User user) {
        User dbUser = userMapper.findByAccountId(user.getAccountId());

        if (dbUser == null) {
            //插入
            user.setGmtCreate(System.currentTimeMillis());
            user.setGmtModified(user.getGmtCreate());
            userMapper.insert(user);
        } else {
            //更新
            dbUser.setGmtModified(System.currentTimeMillis());
            dbUser.setAvatarUrl(user.getAvatarUrl());
            dbUser.setName(user.getName());
            dbUser.setToken(user.getToken());
            dbUser.setEmail(user.getEmail());
            userMapper.update(dbUser);
        }
    }

    /**
     * 邮箱登录
     * @param email
     * @param password
     * @param response
     * @return
     */
    public Integer loginByEmail(String email, String password, HttpServletResponse response) {
        if (userMapper.findByEmail(email) == null) {
            //邮箱未注册
            return CustomizeErrorCode.EMAIL_NOT_FOUND.getCode();
        }

        User user = userMapper.loginByEmail(email, password);
        if (user == null) {
            //密码错误
            return CustomizeErrorCode.PASSWORD_WRONG.getCode();
        } else {
            //登陆成功
            if (user.getToken() == null) {
                //token过期
                return CustomizeErrorCode.TOKEN_INVALID.getCode();
            }
            response.addCookie(new Cookie("token", user.getToken()));

            return CustomizeErrorCode.SUCCESS.getCode();
        }
    }
}
