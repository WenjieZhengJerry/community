package cn.lngfun.community.community.mapper;

import cn.lngfun.community.community.model.User;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface UserMapper {
    //通过GitHub登录添加用户
    @Insert("insert into user (name,account_id,token,gmt_create,gmt_modified,bio,avatar_url,email,company,blog,location) values (#{name},#{accountId},#{token},#{gmtCreate},#{gmtModified},#{bio},#{avatarUrl},#{email},#{company},#{blog},#{location})")
    void insertGithub(User user);

    //通过QQ登录添加用户
    @Insert("insert into user (name,openid,token,gmt_create,gmt_modified,avatar_url,location) values (#{name},#{openid},#{token},#{gmtCreate},#{gmtModified},#{avatarUrl},#{location})")
    void insertQQ(User user);

    //通过邮箱注册添加用户
    @Insert("insert into user (name,token,email,password,avatar_url,gmt_create,gmt_modified) values (#{name},#{token},#{email},#{password},#{avatarUrl},#{gmtCreate},#{gmtModified})")
    void register(User user);

    //通过token查找用户
    @Select("select * from user where token = #{token}")
    User findByToken(@Param("token") String token);

    //通过id查找用户
    @Select("select * from user where id = #{id}")
    User findById(@Param("id") Long id);

    //通过account_id查找用户
    @Select("select * from user where account_id = #{accountId}")
    User findByAccountId(@Param("accountId") String accountId);

    //通过openid查找用户
    @Select("select * from user where openid = #{openid}")
    User findByOpenid(@Param("openid") String openid);

    //更新用户信息
    @Update("update user set openid = #{openid}, account_id = #{accountId}, name = #{name}, token = #{token}, avatar_url = #{avatarUrl}, gmt_modified = #{gmtModified}, bio = #{bio}, email = #{email}, company = #{company}, blog = #{blog}, location = #{location} where id = #{id}")
    void update(User user);

    //更新用户密码
    @Update("update user set password = #{password} where id = #{id}")
    void updatePassword(User user);

    //通过邮箱查找用户
    @Select("select * from user where email = #{email}")
    User findByEmail(@Param("email") String email);

    //通过邮箱登录
    @Select("select * from user where email = #{email} and password = #{password}")
    User loginByEmail(@Param("email") String email, @Param("password") String password);

    //判断邮箱是否存在
    @Select("select * from user where email = #{email}")
    User hasEmail(@Param("email") String email);

    //查找最新用户
    @Select("select * from user order by gmt_create DESC limit #{offset}, #{size}")
    List<User> findNewUsers(@Param("offset") int offset, @Param("size") int size);

}
