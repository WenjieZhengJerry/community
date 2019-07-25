package cn.lngfun.community.community.model;

import lombok.Data;

@Data
public class Notification {
    private Long id;
    private Long notifier;
    private Long receiver;
    private String notifierName;
    private Long outerId;
    private String outerTitle;
    private Integer type;
    private Long gmtCreate;
    private Integer status;
}
