package cn.lngfun.community.community.dto;

import lombok.Data;
import org.springframework.stereotype.Component;

@Data
public class GithubUser {
    private Long id;
    private String name;
    private String bio;
    private String avatarUrl;
    private String email;
    private String company;
    private String blog;
    private String location;
}
