package cn.lngfun.community.community.service;

import cn.lngfun.community.community.dto.NotificationDTO;
import cn.lngfun.community.community.dto.PagingDTO;
import cn.lngfun.community.community.enums.NotificationStatusEnum;
import cn.lngfun.community.community.enums.NotificationTypeEnum;
import cn.lngfun.community.community.exception.CustomizeErrorCode;
import cn.lngfun.community.community.exception.CustomizeException;
import cn.lngfun.community.community.mapper.NotificationMapper;
import cn.lngfun.community.community.model.Comment;
import cn.lngfun.community.community.model.Notification;
import cn.lngfun.community.community.model.User;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Service
public class NotificationService {

    @Autowired
    private NotificationMapper notificationMapper;

    /**
     * 创建通知
     *
     * @param comment
     * @param receiver
     * @param notifierName
     * @param outerTitle
     * @param notificationType
     * @param outerId
     */
    public void createNotify(Comment comment, Long receiver, String notifierName, String outerTitle, NotificationTypeEnum notificationType, Long outerId) {
        //对自己的问题操作时不需要通知
        if (receiver.equals(comment.getCommentator())) {
            return;
        }

        Notification notification = new Notification();
        notification.setGmtCreate(System.currentTimeMillis());
        notification.setNotifier(comment.getCommentator());
        notification.setOuterId(outerId);
        notification.setReceiver(receiver);
        notification.setType(notificationType.getType());
        notification.setStatus(NotificationStatusEnum.UNREAD.getStatus());
        notification.setNotifierName(notifierName);
        notification.setOuterTitle(outerTitle);
        notificationMapper.insert(notification);
    }

    /**
     * 列出一页通知
     * @param userId
     * @param page
     * @param size
     * @return
     */
    public PagingDTO list(Long userId, Integer page, Integer size) {
        PagingDTO<NotificationDTO> pagingDTO = new PagingDTO<>();
        pagingDTO.setTotalCount(notificationMapper.countByReceiverId(userId));
        //计算从第几页开始
        Integer offset = pagingDTO.calculateOffset(page, size);
        //获取这一页的所有通知
        List<Notification> notifications = notificationMapper.listByReceiverId(userId, offset, size);
        if (notifications.size() == 0) {
            return pagingDTO;
        }

        List<NotificationDTO> notificationDTOList = new ArrayList<>();
        for (Notification notification : notifications) {
            NotificationDTO notificationDTO = new NotificationDTO();
            BeanUtils.copyProperties(notification, notificationDTO);
            notificationDTO.setTypeName(NotificationTypeEnum.nameOfType(notification.getType()));
            notificationDTOList.add(notificationDTO);
        }

        pagingDTO.setData(notificationDTOList);

        return pagingDTO;
    }

    /**
     * 获取未读通知的数量
     * @param id
     * @return
     */
    public Integer getUnreadCount(Long id) {
        return notificationMapper.countByUserId(id, NotificationStatusEnum.UNREAD.getStatus());
    }

    /**
     * 设为已读
     * @param id
     * @param user
     * @return
     */
    public NotificationDTO read(Long id, User user) {
        Notification notification = notificationMapper.findById(id);

        if (notification == null) {
            throw new CustomizeException(CustomizeErrorCode.NOTIFICATION_NOT_FOUND);
        }
        if (!Objects.equals(notification.getReceiver(), user.getId())) {
            throw new CustomizeException(CustomizeErrorCode.READ_NOTIFICATION_FAIL);
        }
        //设置为已读
        notificationMapper.updateStatus(id, NotificationStatusEnum.READ.getStatus());

        NotificationDTO notificationDTO = new NotificationDTO();
        BeanUtils.copyProperties(notification, notificationDTO);
        notificationDTO.setTypeName(NotificationTypeEnum.nameOfType(notification.getType()));

        return notificationDTO;
    }

    /**
     * 设为全部已读
     * @param receiverId
     */
    public void readAll(Long receiverId) {
        notificationMapper.readAll(receiverId);
    }

    /**
     * 删除全部已读
     * @param receiverId
     */
    public void deleteRead(Long receiverId) {
        notificationMapper.deleteRead(receiverId);
    }
}
