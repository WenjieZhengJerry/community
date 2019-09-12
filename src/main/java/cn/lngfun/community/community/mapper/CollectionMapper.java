package cn.lngfun.community.community.mapper;

import cn.lngfun.community.community.model.Collection;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface CollectionMapper {
    //收藏问题
    @Insert("insert into collection (user_id,question_id,gmt_create) values (#{userId},#{questionId},${gmtCreate})")
    void collectQuestion(Collection collection);

    //取消收藏
    @Delete("delete from collection where question_id = #{questionId} and user_id = #{userId}")
    void unCollectQuestion(@Param("questionId") Long questionId, @Param("userId") Long userId);

    //查询是否已经收藏了该问题
    @Select("select * from collection where question_id = #{questionId} and user_id = #{userId}")
    Collection isCollected(@Param("questionId") Long questionId, @Param("userId") Long userId);

    //通过userId计算出该用户收藏了多少个问题
    @Select("select count(1) from collection where user_id = #{userId}")
    Integer countByUserId(@Param("userId") Long userId);

    //获取一页收藏的问题
    @Select("select * from collection where user_id = #{userId} order by gmt_create DESC limit #{offset}, #{size}")
    List<Collection> list(@Param("userId") Long userId, @Param("offset") Integer offset, @Param("size") Integer size);

    //通过问题id获取收藏
    @Select("select * from collection where question_id = #{questionId}")
    List<Collection> findByQuestionId(@Param("questionId") Long questionId);
}
