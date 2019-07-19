package cn.lngfun.community.community.mapper;

import cn.lngfun.community.community.model.Question;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface QuestionMapper {

    @Insert("insert into question (title,description,gmt_create,gmt_modified,creator,tag) values (#{title},#{description},#{gmtCreate},#{gmtModified},#{creator},#{tag})")
    void insert(Question question);

    @Select("select * from question order by gmt_modified DESC limit #{offset},#{size}")
    List<Question> list(@Param(value = "offset") Integer offset, @Param(value = "size") Integer size);

    @Select("select count(1) from question")
    Integer count();

    @Select("select * from question where creator = #{userId} order by gmt_modified DESC limit #{offset},#{size}")
    List<Question> listByUserId(@Param(value = "userId") Long userId, @Param(value = "offset") Integer offset, @Param(value = "size") Integer size);

    @Select("select count(1) from question where creator = #{userId}")
    Integer countByUserId(@Param(value = "userId") Long userId);

    @Select("select * from question where id = #{id}")
    Question findById(@Param(value = "id") Long id);

    @Update("update question set title = #{title}, description = #{description}, gmt_modified = #{gmtModified}, tag = #{tag} where id = #{id}")
    int update(Question question);

    @Update("update question set view_count = view_count + 1 where id = #{id}")
    void incView(@Param(value = "id") Long id);

    @Update("update question set comment_count = comment_count + 1 where id = #{id}")
    void incCommentCount(Long id);
}
