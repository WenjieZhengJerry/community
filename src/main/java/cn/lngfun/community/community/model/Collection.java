package cn.lngfun.community.community.model;

import lombok.Data;

@Data
public class Collection {
    private Long id;
    private Long userId;
    private Long questionId;
    private Long gmtCreate;
}
