package cn.lngfun.community.community.service;

import cn.lngfun.community.community.dto.ResultDTO;
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
     *
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
            dbUser.setAvatarUrl(user.getAvatarUrl());
            dbUser.setName(user.getName());
            dbUser.setToken(user.getToken());
            dbUser.setEmail(user.getEmail());
            dbUser.setBio(user.getBio());
            dbUser.setBlog(user.getBlog());
            dbUser.setCompany(user.getCompany());
            dbUser.setLocation(user.getLocation());
            dbUser.setGmtModified(System.currentTimeMillis());
            userMapper.update(dbUser);
        }
    }

    /**
     * 邮箱登录，分四种情况：邮箱未注册、密码错误、token过期、成功登录
     *
     * @param email
     * @param password
     * @param response
     * @return
     */
    public Object loginByEmail(String email, String password, HttpServletResponse response) {
        if (userMapper.findByEmail(email) == null) {
            //邮箱未注册
            return ResultDTO.errorOf(CustomizeErrorCode.EMAIL_NOT_FOUND);
        }

        User user = userMapper.loginByEmail(email, password);
        if (user == null) {
            //密码错误
            return ResultDTO.errorOf(CustomizeErrorCode.PASSWORD_WRONG);
        } else {
            //登陆成功
            if (user.getToken() == null) {
                //token过期
                return ResultDTO.errorOf(CustomizeErrorCode.TOKEN_INVALID);
            }
            response.addCookie(new Cookie("token", user.getToken()));

            return ResultDTO.okOf();
        }
    }

    public void updateProfile(User user) {
        userMapper.update(user);
    }

    public User findUserById(Long id) {
        return userMapper.findById(id);
    }
}
