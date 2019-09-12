package cn.lngfun.community.community.exception;

public enum CustomizeErrorCode implements ICustomizeErrorCode {
    SUCCESS(2000, "请求成功"),
    QUESTION_NOT_FOUND(2001, "你找的问题不存在了，请换一个试试！！！"),
    TARGET_PARAM_NOT_FOUND(2002, "未选中任何问题或评论进行回复"),
    NO_LOGIN(2003, "当前操作需要登录，请登录后再重试"),
    SYS_ERROR(2004, "服务器冒烟了，请稍后再尝试一下！！！"),
    TYPE_PARAM_WRONG(2005, "评论类型错误或不存在"),
    COMMENT_NOT_FOUND(2006, "该评论不存在了，要不要换个试试？"),
    CONTENT_IS_EMPTY(2007, "评论内容不能为空"),
    READ_NOTIFICATION_FAIL(2008, "兄弟你读到别人的信息了"),
    NOTIFICATION_NOT_FOUND(2009, "这条消息离家出走了"),
    INVALID_QUESTION_ID(2010, "兄弟，你不能编辑别人的问题！！！"),
    NETWORK_CONNECT_FAIL(2011,"网络连接超时，请稍后再试一下"),
    DELETE_QUESTION_FAIL(2012, "大哥你怎么可以删别人的问题"),
    EMAIL_NOT_FOUND(2013, "这个邮箱不存在哦"),
    EMAIL_FORMAT_WRONG(2014, "兄弟，你的邮箱格式不正确，再输入一次试试？"),
    PASSWORD_FORMAT_WRONG(2015, "兄弟，你的密码格式不正确，正确格式：6-18位，同时含有数字和字母"),
    PASSWORD_WRONG(2016, "密码错误，再想想？"),
    TOKEN_INVALID(2017, "token过期，请用第三方登录"),
    FILE_UPLOAD_FAIL(2018, "文件上传失败"),
    USER_NOT_FOUND(2019, "这个用户不存在哦"),
    EMAIL_SEND_FAIL(2020, "验证码发送失败，请稍后再试"),
    AUTH_CODE_INVALID(2021, "验证码过期，请重新获取"),
    AUTH_CODE_WRONG(2022, "验证码错误，请重新输入"),
    EMAIL_IS_EXIST(2023, "这个邮箱已经被别的兄弟绑定了，再选一个吧"),
    FOLLOW_IS_EXIST(2024, "你已经关注过Ta了"),
    INVALID_FOLLOW(2025, "不能关注自己哦"),
    DELETE_COMMENT_FAIL(2026, "大哥你怎么可以删别人的评论"),
    CHANGE_AVATAR_FAIL(2027, "更改头像失败"),
    RESOURCE_NOT_FOUND(2028, "这个网页走丢了，试试其他的吧"),
    RETURN_TO_INDEX(2029, "返回主页"),
    COLLECTION_IS_EXIST(2030, "你已经收藏了这个问题了");

    private String message;
    private Integer code;

    @Override
    public String getMessage() {
        return message;
    }

    @Override
    public Integer getCode() {
        return code;
    }


    CustomizeErrorCode(Integer code, String message) {
        this.message = message;
        this.code = code;
    }

}
