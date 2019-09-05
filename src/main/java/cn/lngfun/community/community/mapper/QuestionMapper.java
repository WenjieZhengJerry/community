package cn.lngfun.community.community.mapper;

import cn.lngfun.community.community.model.Question;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface QuestionMapper {
    //添加问题
    @Insert("insert into question (title,description,gmt_create,gmt_modified,creator,tag,category_type) values (#{title},#{description},#{gmtCreate},#{gmtModified},#{creator},#{tag},#{categoryType})")
    void insert(Question question);

    //列出一页问题
    @Select("select * from question order by gmt_modified DESC limit #{offset},#{size}")
    List<Question> list(@Param(value = "offset") Integer offset, @Param(value = "size") Integer size);

    //计算所有问题的总数
    @Select("select count(1) from question")
    Integer count();

    //通过搜索条件列出一页问题
    @Select("select * from question where title regexp #{search} or  order by gmt_modified DESC limit #{offset},#{size}")
    List<Question> listBySearch(@Param(value = "offset") Integer offset, @Param(value = "size") Integer size, @Param(value = "search") String search);

    //计算所有符合搜索条件的问题总数
    @Select("select count(1) from question where title regexp #{search}")
    Integer countBySearch(@Param(value = "search") String search);

    //通过用户id列出一页问题
    @Select("select * from question where creator = #{userId} order by gmt_modified DESC limit #{offset},#{size}")
    List<Question> listByUserId(@Param(value = "userId") Long userId, @Param(value = "offset") Integer offset, @Param(value = "size") Integer size);

    //计算某用户提出的所有问题数
    @Select("select count(1) from question where creator = #{userId}")
    Integer countByUserId(@Param(value = "userId") Long userId);

    //通过问题id查找
    @Select("select * from question where id = #{id}")
    Question findById(@Param(value = "id") Long id);

    //更新问题
    @Update("update question set title = #{title}, description = #{description}, gmt_modified = #{gmtModified}, tag = #{tag}, category_type = #{categoryType} where id = #{id}")
    int update(Question question);

    //增加问题的浏览量
    @Update("update question set view_count = view_count + 1 where id = #{id}")
    void incView(@Param(value = "id") Long id);

    //增加问题的回复量
    @Update("update question set comment_count = comment_count + 1 where id = #{id}")
    void incCommentCount(@Param(value = "id") Long id);

    //减少问题的回复量
    @Update("update question set comment_count = comment_count - 1 where id = #{id}")
    void decCommentCount(@Param(value = "id") Long id);

    //通过标签查找相关问题，只列出10个
    @Select("select * from question where id != #{id} and tag regexp #{tag} order by view_count DESC limit 0, 10")
    List<Question> selectRelated(@Param(value = "id") Long id, @Param(value = "tag") String tag);

    //列出浏览量和回复量的前十个问题
    @Select("select * from question order by view_count DESC, comment_count DESC limit 0, 10")
    List<Question> selectHotIssue();

    //通过问题id删除问题
    @Delete("delete from question where id = #{questionId}")
    void deleteQuestion(@Param(value = "questionId") Long questionId);

    //计算所有符合标签的问题总数
    @Select("select count(1) from question where tag regexp #{tag}")
    Integer countByTag(@Param(value = "tag") String tag);

    //通过标签列出一页问题
    @Select("select * from question where tag regexp #{tag} order by gmt_modified DESC limit #{offset},#{size}")
    List<Question> listByTag(@Param(value = "offset") Integer offset, @Param(value = "size") Integer size, @Param(value = "tag") String tag);

    //给问题点赞
    @Update("update question set like_count = like_count + 1 where id = #{id}")
    void likeComment(@Param(value = "id") Long id);

    //给问题取消点赞
    @Update("update question set like_count = like_count - 1 where id = #{id}")
    void dislikeComment(@Param(value = "id") Long id);

    //计算所有符合该分类的问题总数
    @Select("select count(1) from question where category_type = #{categoryType}")
    Integer countByCategory(@Param(value = "categoryType") Integer categoryType);

    //通过分类列出一页问题
    @Select("select * from question where category_type = #{categoryType} order by gmt_modified DESC limit #{offset},#{size}")
    List<Question> listByCategory(@Param(value = "offset") Integer offset, @Param(value = "size") Integer size, @Param(value = "categoryType") Integer categoryType);
}
