package cn.lngfun.community.community.mapper;

import cn.lngfun.community.community.model.User;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface UserMapper {
    //添加用户
    @Insert("insert into user (name,account_id,token,gmt_create,gmt_modified,avatar_url) values (#{name},#{accountId},#{token},#{gmtCreate},#{gmtModified},#{avatarUrl})")
    void insert(User user);
    //通过token查找用户
    @Select("select * from user where token = #{token}")
    User findByToken(@Param("token") String token);
    //通过id查找用户
    @Select("select * from user where id = #{id}")
    User findById(@Param("id") Long id);
    //通过account_id查找用户
    @Select("select * from user where account_id = #{accountId}")
    User findByAccountId(@Param("accountId") String accountId);
    //更新用户信息
    @Update("update user set name = #{name}, token = #{token}, avatar_url = #{avatarUrl}, gmt_modified = #{gmtModified} where id = #{id}")
    void update(User user);
    //通过id查找一组用户
    @Select("select * from user where id in (#{usersIdStr})")
    List<User> findByIds(@Param("usersIdStr") String usersIdStr);
}
