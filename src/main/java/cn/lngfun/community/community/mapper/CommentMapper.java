package cn.lngfun.community.community.mapper;

import cn.lngfun.community.community.model.Comment;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface CommentMapper {
    //添加回复
    @Insert("insert into comment (parent_id,type,content,commentator,gmt_create,gmt_modified,like_count) values (#{parentId},#{type},#{content},#{commentator},#{gmtCreate},#{gmtModified},#{likeCount})")
    void insert(Comment comment);
    //通过id查找回复
    @Select("select * from comment where id = #{id}")
    Comment findById(@Param(value = "id") Long id);
    //通过父id查找回复
    @Select("select * from comment where parent_id = #{parentId} and type = #{type} order by gmt_create DESC")
    List<Comment> findByParentId(@Param(value = "parentId") Long parentId, @Param(value = "type") Integer type);
    //增加回复量
    @Update("update comment set comment_count = comment_count + 1 where id = #{id}")
    void incCommentCount(@Param(value = "id") Long id);
}
