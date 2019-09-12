package cn.lngfun.community.community.dto;

import cn.lngfun.community.community.model.User;
import lombok.Data;

import java.util.List;

@Data
public class QuestionDTO {
    private Long id;
    private String title;
    private String description;
    private String tag;
    private Long gmtCreate;
    private Long gmtModified;
    private Long creator;
    private Integer commentCount;
    private Integer viewCount;
    private Integer likeCount;
    private Integer collectionCount;
    private Integer categoryType;
    private String categoryName;
    private String categoryColor;
    private User user;
    private List<User> collectors;
    private boolean liked = false;
    private boolean collected = false;
}
