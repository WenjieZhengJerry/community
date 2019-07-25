package cn.lngfun.community.community.enums;

public enum NotificationStatusEnum {
    UNREAD(0),
    READ(1);
    private Integer status;

    public Integer getStatus() {
        return status;
    }

    NotificationStatusEnum(Integer status) {
        this.status = status;
    }
}
