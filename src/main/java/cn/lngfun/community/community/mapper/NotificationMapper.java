package cn.lngfun.community.community.mapper;

import cn.lngfun.community.community.model.Notification;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface NotificationMapper {

    @Select("select count(1) from notification where receiver = #{id} and status = #{status}")
    Integer countByUserId(@Param(value = "id") Long id, @Param(value = "status") Integer status);

    @Insert("insert into notification (notifier,receiver,outer_id,type,gmt_create,status,notifier_name,outer_title) values (#{notifier},#{receiver},#{outerId},#{type},#{gmtCreate},#{status},#{notifierName},#{outerTitle})")
    void insert(Notification notification);

    @Select("select count(1) from notification where receiver = #{receiverId}")
    Integer countByReceiverId(@Param(value = "receiverId") Long receiverId);

    @Select("select * from notification where receiver = #{receiverId} order by gmt_create DESC limit #{offset},#{size}")
    List<Notification> listByReceiverId(@Param(value = "receiverId") Long receiverId, @Param(value = "offset") Integer offset, @Param(value = "size") Integer size);

    @Select("select * from notification where id = #{id}")
    Notification findById(@Param(value = "id") Long id);

    @Update("update notification set status = #{status} where id = #{id}")
    void updateStatus(@Param(value = "id") Long id, @Param(value = "status") Integer status);
}
