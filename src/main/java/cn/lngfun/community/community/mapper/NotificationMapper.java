package cn.lngfun.community.community.mapper;

import cn.lngfun.community.community.model.Notification;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface NotificationMapper {
    //通过用户id计算通知个数
    @Select("select count(1) from notification where receiver = #{id} and status = #{status}")
    Integer countByUserId(@Param(value = "id") Long id, @Param(value = "status") Integer status);

    //添加通知
    @Insert("insert into notification (notifier,receiver,outer_id,type,gmt_create,status,notifier_name,outer_title) values (#{notifier},#{receiver},#{outerId},#{type},#{gmtCreate},#{status},#{notifierName},#{outerTitle})")
    void insert(Notification notification);

    //通过被通知用户的id计算通知个数
    @Select("select count(1) from notification where receiver = #{receiverId}")
    Integer countByReceiverId(@Param(value = "receiverId") Long receiverId);

    //通过被通知用户的id列出该用户的所有通知
    @Select("select * from notification where receiver = #{receiverId} order by status ASC, gmt_create DESC limit #{offset},#{size}")
    List<Notification> listByReceiverId(@Param(value = "receiverId") Long receiverId, @Param(value = "offset") Integer offset, @Param(value = "size") Integer size);

    //通过用户id查找一条通知
    @Select("select * from notification where id = #{id}")
    Notification findById(@Param(value = "id") Long id);

    //更新通知的状态
    @Update("update notification set status = #{status} where id = #{id}")
    void updateStatus(@Param(value = "id") Long id, @Param(value = "status") Integer status);

    //设为全部已读
    @Update("update notification set status = 1 where receiver = #{receiverId}")
    void readAll(@Param(value = "receiverId") Long receiverId);

    //删除全部已读
    @Delete("delete from notification where receiver = #{receiverId} and status = 1")
    void deleteRead(@Param(value = "receiverId") Long receiverId);
}
