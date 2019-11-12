package cn.lngfun.community.community.mapper;

import cn.lngfun.community.community.model.Follow;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface FollowMapper {
    //通过userId查询对应的关注列表
    @Select("select * from follow where follower_id = #{userId} order by gmt_create DESC")
    List<Follow> findById(@Param(value = "userId") Long userId);

    //通过userId查询对应的粉丝列表
    @Select("select * from follow where user_id = #{userId} order by gmt_create DESC")
    List<Follow> findFollowersById(@Param(value = "userId") Long userId);

    //通过userId查询其被多少个人关注，即粉丝数
    @Select("select count(1) from follow where user_id = #{userId}")
    Integer countFollowerById(@Param(value = "userId") Long userId);

    //通过userId查询其关注了多少个人，即关注数
    @Select("select count(1) from follow where follower_id = #{followerId}")
    Integer countFollowById(@Param(value = "followerId") Long followerId);

    //插入一条关注记录
    @Insert("insert into follow (user_id, follower_id, gmt_create) values (#{userId}, #{followerId}, #{gmtCreate})")
    void insert(Follow follow);

    //删除一条关注记录
    @Delete("delete from follow where user_id = #{userId} and follower_id = #{followerId}")
    void delete(@Param(value = "userId") Long userId, @Param(value = "followerId") Long followerId);

    //查询关注记录是否存在，避免重复关注
    @Select("select * from follow where user_id = #{userId} and follower_id = #{followerId}")
    Follow isFollowed(@Param(value = "userId") Long userId, @Param(value = "followerId") Long followerId);


}
