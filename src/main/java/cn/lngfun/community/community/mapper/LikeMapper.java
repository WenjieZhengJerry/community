package cn.lngfun.community.community.mapper;

import cn.lngfun.community.community.model.Like;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface LikeMapper {
    //点赞
    @Insert("insert into `like` (parent_id,user_id,type,gmt_create) values (#{parentId},#{userId},#{type},#{gmtCreate})")
    void like(Like like);

    //取消点赞，仅删除一条点赞记录
    @Delete("delete from `like` where user_id = #{usersId} and parent_id = #{parentId}")
    void dislike(@Param(value = "usersId") Long usersId, @Param(value = "parentId") Long parentId);

    //通过父id查找点赞记录
    @Select("select * from `like` where parent_id = #{parentId}")
    List<Like> findByParentId(@Param(value = "parentId") Long parentId);

    //通过父id删除点赞记录，删除与该问题或评论有关的所有点赞记录
    @Delete("delete from `like` where parent_id = #{parentId}")
    void deleteByParentId(@Param(value = "parentId") Long parentId);
}
