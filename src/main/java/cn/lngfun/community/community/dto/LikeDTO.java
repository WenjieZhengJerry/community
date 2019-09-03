package cn.lngfun.community.community.dto;

import lombok.Data;

@Data
public class LikeDTO {
    private Long parentId;
    private Integer type;
    private Integer option;
}
