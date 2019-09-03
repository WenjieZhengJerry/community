package cn.lngfun.community.community.enums;

public enum LikeTypeAndOptionEnum {
    QUESTION(1),
    COMMENT(2),
    LIKE(3),
    DISLIKE(4);

    private Integer type;

    public Integer getType() {
        return type;
    }

    LikeTypeAndOptionEnum(Integer type) {
        this.type = type;
    }
}
