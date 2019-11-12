package cn.lngfun.community.community.dto;

import lombok.Data;

@Data
public class QQAccessTokenDTO {
    private String grantType;
    private String client_id;
    private String client_secret;
    private String code;
    private String redirect_uri;
}
