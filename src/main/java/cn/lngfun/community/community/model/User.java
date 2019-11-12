package cn.lngfun.community.community.model;

import lombok.Data;

@Data
public class User {
    private Long id;
    private String name;
    private String accountId;
    private String openid;
    private String token;
    private Long gmtCreate;
    private Long gmtModified;
    private String bio;
    private String avatarUrl;
    private String email;
    private String password;
    private String company;
    private String blog;
    private String location;
}
