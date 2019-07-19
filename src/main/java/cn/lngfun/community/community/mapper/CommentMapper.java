package cn.lngfun.community.community.mapper;

import cn.lngfun.community.community.model.Comment;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface CommentMapper {

    @Insert("insert into comment (parent_id,type,content,commentator,gmt_create,gmt_modified,like_count) values (#{parentId},#{type},#{content},#{commentator},#{gmtCreate},#{gmtModified},#{likeCount})")
    void insert(Comment comment);

    @Select("select * from comment where id = #{id}")
    Comment findById(@Param(value = "id") Long id);
}