package cn.lngfun.community.community.service;

import cn.lngfun.community.community.dto.FollowDTO;
import cn.lngfun.community.community.dto.ResultDTO;
import cn.lngfun.community.community.exception.CustomizeErrorCode;
import cn.lngfun.community.community.mapper.FollowMapper;
import cn.lngfun.community.community.mapper.QuestionMapper;
import cn.lngfun.community.community.mapper.UserMapper;
import cn.lngfun.community.community.model.User;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
public class UserService {

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private QuestionMapper questionMapper;

    @Autowired
    private FollowMapper followMapper;

    /**
     * 注册
     *
     * @param user
     */
    public void register(User user) {
        userMapper.register(user);
    }

    /**
     * Github、QQ第三方登录
     *
     * @param user
     */
    public void createOrUpdate(User user, String type) {
        User dbUser = new User();
        if ("GitHub".equals(type))
            dbUser = userMapper.findByAccountId(user.getAccountId());
        else if ("QQ".equals(type))
            dbUser = userMapper.findByOpenid(user.getOpenid());

        if (dbUser == null) {
            //插入
            user.setGmtCreate(System.currentTimeMillis());
            user.setGmtModified(user.getGmtCreate());
            if ("GitHub".equals(type))
                userMapper.insertGithub(user);
            else if ("QQ".equals(type))
                userMapper.insertQQ(user);
        } else {
            //更新:只有数据库内字段值为空的属性需要更新
            if (StringUtils.isBlank(dbUser.getAvatarUrl())) {
                dbUser.setAvatarUrl(user.getAvatarUrl());
            }
            if (StringUtils.isBlank(dbUser.getName())) {
                dbUser.setName(user.getName());
            }
            if (StringUtils.isBlank(dbUser.getEmail())) {
                dbUser.setEmail(user.getEmail());
            }
            if (StringUtils.isBlank(dbUser.getBio())) {
                dbUser.setBio(user.getBio());
            }
            if (StringUtils.isBlank(dbUser.getBlog())) {
                dbUser.setBlog(user.getBlog());
            }
            if (StringUtils.isBlank(dbUser.getCompany())) {
                dbUser.setCompany(user.getCompany());
            }
            if (StringUtils.isBlank(dbUser.getLocation())) {
                dbUser.setLocation(user.getLocation());
            }

            dbUser.setToken(user.getToken());
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
            //更新token
            String token = UUID.randomUUID().toString();
            user.setToken(token);
            userMapper.update(user);
            Cookie cookie = new Cookie("token", user.getToken());
            cookie.setMaxAge(2592000);//30天有效期
            response.addCookie(cookie);

            return ResultDTO.okOf();
        }
    }

    /**
     * 更新个人资料
     *
     * @param user
     */
    public void updateProfile(User user) {
        userMapper.update(user);
    }

    /**
     * 通过id查找用户
     *
     * @param id
     * @return
     */
    public User findUserById(Long id) {
        return userMapper.findById(id);
    }

    /**
     * 查询邮箱是否存在
     *
     * @param email
     * @return
     */
    public User hasEmail(String email) {
        return userMapper.hasEmail(email);
    }

    /**
     * 更新用户密码
     *
     * @param user
     */
    public void updatePassword(User user) {
        userMapper.updatePassword(user);
    }

    /**
     * 获取5个最新注册的用户
     *
     * @return
     */
    public List<FollowDTO> selectNewUser() {
        List<FollowDTO> newUsersDTO = new ArrayList<>();
        int offset = 0;
        int size = 5;
        List<User> newUsers = userMapper.findNewUsers(offset, size);

        for (User newUser : newUsers) {
            FollowDTO followDTO = new FollowDTO();
            Integer questionCount = questionMapper.countByUserId(newUser.getId());
            Integer followerCount = followMapper.countFollowerById(newUser.getId());
            newUser.setPassword(null);
            followDTO.setUser(newUser);
            followDTO.setQuestionCount(questionCount);
            followDTO.setFollowerCount(followerCount);
            newUsersDTO.add(followDTO);
        }

        return newUsersDTO;
    }

    /**
     * 绑定GitHub
     * @param accoutId
     * @param user
     * @return
     */
    public String bindGithub(String accoutId, User user) {
        if (userMapper.findByAccountId(accoutId) != null) {
            //account id已存在
            return ResultDTO.errorOf(CustomizeErrorCode.ACCOUNT_ID_IS_EXIST).getMessage();
        } else {
            user.setAccountId(accoutId);
            userMapper.update(user);
            return ResultDTO.okOf().getMessage();
        }
    }

    /**
     * 绑定QQ
     * @param openid
     * @param user
     * @return
     */
    public String bindQQ(String openid, User user) {
        if (userMapper.findByOpenid(openid) != null) {
            //openid已存在
            return ResultDTO.errorOf(CustomizeErrorCode.OPENID_IS_EXIST).getMessage();
        } else {
            user.setOpenid(openid);
            userMapper.update(user);
            return ResultDTO.okOf().getMessage();
        }

    }
}
