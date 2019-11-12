$(document).ready(function () {
    $("#email").val($.cookie('tEmail'));

    $("#logout").click(function () {
        var url = window.location.href;
        var str = "profile";
        var redirect = url.indexOf(str) >= 0 ? true : false;

        $.ajax({
            type: "POST",
            url: "/logout?redirect=" + redirect,
            contentType: "application/json",
            dataType: "json",
            success: function (response) {
                if (response.code == 200) {
                    window.location.reload();
                } else if (response.code == 2029) {
                    window.location.href = "/";
                } else {
                    alert("退出登录失败，请稍后再试")
                }
            }
        });
    });
});

/**
 * 这里是评论问题
 */
function post() {
    var questionId = $("#question_id").val();
    var content = $("#comment_content").val();

    comment2target(questionId, 1, content);
}

/**
 * 这里是评论回复
 */
function comment(e) {
    var commentId = e.getAttribute("data-id");
    var content = $("#input-" + commentId).val();
    comment2target(commentId, 2, content);
}

/**
 * 封装 提交评论 的方法
 * @param parentId
 * @param type
 * @param content
 */
function comment2target(parentId, type, content) {
    if (content == null || content == "") {
        alert("评论内容不能为空");
        return;
    }

    $.ajax({
        type: "POST",
        url: "/comment",
        contentType: "application/json",
        dataType: "json",
        data: JSON.stringify({
            "parentId": parentId,
            "content": content,
            "type": type
        }),
        success: function (response) {
            if (response.code == 200) {
                window.location.reload();
            } else {
                if (response.code == 2003) {
                    if (confirm(response.message)) {
                        window.open("https://github.com/login/oauth/authorize?client_id=1f883171aeed4ab10a2d&redirect_uri=" + document.location.origin + "/callback&scope=user&state=1");
                        window.localStorage.setItem("closable", "true");
                    }
                } else {
                    alert(response.message);
                }
            }
        }
    });
}

/**
 * 删除评论
 * @param e
 */
function deleteComment(e) {
    if (confirm("删除评论无法复原，是否删除")) {
        var id = e.getAttribute("data-id");
        $.ajax({
            // type: "post",
            // url: "/comment/deleteComment?id=" + id,
            type: "delete",
            url: "/comment/" + id,
            dataType: "json",
            success: function (response) {
                if (response.code == 200) {
                    $("#comment-media-" + id).attr("class", "hidden");
                    $("#question-comment-count").html($("#question-comment-count").html() - 1);
                } else {
                    alert(response.message);
                }
            }
        });
    }
}

/**
 * 展示二级评论内容
 */
function collapseComments(e) {
    var id = e.getAttribute("data-id");
    var comments = $("#comment-" + id);
    //获取二级评论的展开状态
    var collapse = e.getAttribute("data-collapse");
    if (collapse) {
        //折叠二级评论,移除标记状态和样式
        comments.removeClass("in");
        e.removeAttribute("data-collapse");
        e.classList.remove("active");
    } else {
        var subCommentContainer = $("#comment-" + id);
        if (subCommentContainer.children().length != 1) {
            //展开二级评论
            comments.addClass("in");
            // 标记二级评论展开状态
            e.setAttribute("data-collapse", "in");
            e.classList.add("active");
        } else {
            $.getJSON("/comment/" + id, function (data) {
                $.each(data.data.reverse(), function (index, comment) {
                    var mediaLeftElement = $("<div/>", {
                        "class": "media-left"
                    }).append($("<a/>", {
                        "href": "/people/" + comment.user.id
                    }).append($("<img/>", {
                        "class": "media-object img-rounded",
                        "src": comment.user.avatarUrl
                    })));

                    var mediaBodyElement = $("<div/>", {
                        "class": "media-body"
                    }).append($("<h5/>", {
                        "class": "media-heading",
                        "html": comment.user.name
                    }))/*.append($("<span/>", {
                        "class": "pull-right",
                        "html": "<a href='javascript:void(0)'>回复</a>"
                    }))*/.append($("<div/>", {
                        "html": comment.content
                    })).append($("<div/>", {
                        "class": "menu"
                    }).append($("<span/>", {
                        "class": "pull-right",
                        "html": moment(comment.gmtCreate).format('YYYY-MM-DD HH:mm:ss')
                    })));

                    var mediaElement = $("<div/>", {
                        "class": "media"
                    }).append(mediaLeftElement).append(mediaBodyElement);

                    var commentElement = $("<div/>", {
                        "class": "col-lg-12 col-md-12 col-sm-12 col-xs-12 comments"
                    }).append(mediaElement);

                    subCommentContainer.prepend(commentElement);
                });
                //展开二级评论
                comments.addClass("in");
                // 标记二级评论展开状态
                e.setAttribute("data-collapse", "in");
                e.classList.add("active");
            });
        }
    }
}

/**
 * 选中标签
 * @param e
 */
function selectTag(e) {
    var previous = $("#tag").val();
    var value = e.getAttribute("data-tag");

    if (previous.indexOf(value) == -1) {
        if (previous) {
            $("#tag").val(previous + ',' + value);
        } else {
            $("#tag").val(value);
        }
    }
}

/**
 * 展示标签栏
 */
function showSelectTag() {
    $("#select-tag").show();
}

/**
 * 删除问题
 * @param id
 */
function deleteQuestion(id) {
    if (confirm("删除问题无法复原，是否删除？")) {
        $.ajax({
            type: "delete",
            url: "/question/" + id,
            contentType: "application/json",
            dataType: "json",
            success: function (response) {
                if (response.code == 200) {
                    window.location.reload();
                } else {
                    response.code == 2012 ? alert(response.message) : alert("删除失败");
                }
            }
        });
    }
}

/**
 * 邮箱登录
 */
function loginByEmail() {
    var email = $("#email").val();
    var password = $("#password").val();

    if (email == null || email == "") {
        alert("邮箱不能为空");
        return;
    }
    if (password == null || password == "") {
        alert("密码不能为空");
        return;
    }

    $.ajax({
        type: "post",
        url: "/login",
        data: $("#login-form").serialize(),
        dataType: "json",
        success: function (response) {
            if (response.code == 200) {
                window.location.reload();
            } else {
                alert(response.message);
            }
        }
    });
}

/**
 * 点赞、取消点赞
 * @param e
 */
function likeOrDislike(e) {
    var parentId = e.getAttribute("data-id");
    var type = e.getAttribute("data-type");
    if (type == "question") {
        type = 1;
    } else {
        type = 2;
    }

    if (e.classList.contains("active")) {
        //取消点赞
        e.classList.remove("active");
        e.children[1].innerHTML = String(Number(e.children[1].innerHTML) - 1);

        $.ajax({
            type: "post",
            url: "/likeOrDislike",
            dataType: "json",
            contentType: "application/json",
            data: JSON.stringify({
                "parentId": parentId,
                "option": 4,
                "type": type
            }),
            success: function (response) {
                if (response.code != 200) {
                    alert(response.message);
                    e.classList.add("active");
                    e.children[1].innerHTML = String(Number(e.children[1].innerHTML) + 1);
                }
            }
        });
    } else {
        //点赞
        e.classList.add("active");
        e.children[1].innerHTML = String(Number(e.children[1].innerHTML) + 1);

        $.ajax({
            type: "post",
            url: "/likeOrDislike",
            dataType: "json",
            contentType: "application/json",
            data: JSON.stringify({
                "parentId": parentId,
                "option": 3,
                "type": type
            }),
            success: function (response) {
                if (response.code != 200) {
                    alert(response.message);
                    e.classList.remove("active");
                    e.children[1].innerHTML = String(Number(e.children[1].innerHTML) - 1);
                }
            }
        });
    }
}



/**
 * 编辑个人资料
 * @param action
 */
function editInformation(action) {
    if (action == 1) {
        $("#info-div").attr("class", "hidden");
        $("#info-form").removeClass("hidden");
    } else {
        $("#info-form").attr("class", "hidden");
        $("#info-div").removeClass("hidden");
    }
}

/**
 * 修改密码
 */
function changePassword() {
    if ($("#old_password").val() == $("#new_password").val()) {
        alert("新密码和旧密码不能一样");
        return;
    }
    if ($("#new_password").val() != $("#confirm_password").val()) {
        alert("新密码和确认密码不一致");
        return;
    }

    $.ajax({
        type: "post",
        url: "/changePassword",
        data: $("#change-password-form").serialize(),
        dataType: "json",
        success: function (response) {
            if (response.code == 200) {
                alert("密码修改成功");
                window.location.reload();
            } else {
                alert(response.message);
            }
        }
    });
}

/**
 * 绑定或修改邮箱
 * @param action
 */
function bindOrEditEmail(action) {
    var form;
    var result = "";
    if ("bind" == action) {
        form = $("#bind-email-form");
        result = "绑定成功";
    }
    if ("edit" == action) {
        form = $("#edit-email-form");
        result = "修改成功";
    }

    $.ajax({
        type: "post",
        url: "/editEmail",
        data: form.serialize(),
        dataType: "json",
        success: function (response) {
            if (response.code == 200) {
                alert(result);
                window.location.reload();
            } else {
                alert(response.message);
            }
        }
    });
}

/**
 * 定时器
 * @param obj
 * @param countdown
 */
function settime(obj, countdown) {
    if (countdown == 0) {
        obj.removeAttribute("disabled");
        obj.value = "获取验证码";
        return;
    } else {
        obj.setAttribute("disabled", "disabled");
        obj.value = "重新发送(" + countdown + ")";
        countdown--;
    }
    setTimeout(function () {
            settime(obj, countdown)
        }
        , 1000)
}

/**
 * 绑定邮箱时获取验证码
 * @param action
 * @param obj
 */
function sendAuthCode(action, obj) {
    var email;
    var countdown = 30;
    if ("bind" == action) {
        email = $("#bind_email").val();
        settime(obj, countdown);
    }
    if ("edit" == action) {
        if ($("#iniEmail").val() != null && $("#iniEmail").val() == $("#setting_email").val()) {
            alert("修改的邮箱不能和之前的邮箱一样");
            return;
        }
        email = $("#setting_email").val();
        settime(obj, countdown);
    }

    $.ajax({
        type: "post",
        url: "/getAuthCode/bindEmail?email=" + email,
        dataType: "json",
        success: function (response) {
            if (response.code == 200) {
                alert("验证码发送成功，请登录邮箱获取验证码");
            } else {
                alert(response.message);
                window.location.reload();
            }
        }
    });
}

/**
 * 关注或取消关注
 *
 * @param e
 */
function follow(e) {
    var userId = e.getAttribute("data-id");
    var type;
    if ($("#follow-btn-" + userId).html() == "关注Ta") {
        type = "follow";
    } else {
        type = "unfollow";
    }

    $.ajax({
        type: "post",
        url: "/follow?id=" + userId + "&type=" + type,
        dataType: "json",
        success: function (response) {
            if (response.code == 200) {
                if (type == "follow") {
                    $("#follow-btn-" + userId).attr("class", "btn btn-danger people-follow-btn");
                    $("#follow-btn-" + userId).html("取消关注");
                    $("#follower-count").html(Number($("#follower-count").html()) + 1);
                } else {
                    $("#follow-btn-" + userId).attr("class", "btn btn-primary people-follow-btn");
                    $("#follow-btn-" + userId).html("关注Ta");
                    $("#follower-count").html($("#follower-count").html() - 1);
                }
            } else {
                alert(response.message);
            }
        }
    });
}

/**
 * 收藏、取消收藏
 * @param e
 */
function collectOrUnCollect(e) {
    var questionId = e.getAttribute("data-id");
    var className = e.getAttribute("class");
    var from = e.getAttribute("data-from");
    var type = className.indexOf("active") >= 0 ? "uncollect" : "collect";

    if (type == "collect") {
        e.classList.add("active");
    } else {
        e.classList.remove("active");
    }

    $.ajax({
        type: "post",
        url: "/collectQuestion?id=" + questionId + "&type=" + type,
        dataType: "json",
        success: function (response) {
            if (response.code == 200) {
                //收藏成功
            } else if (response.code == 2000) {
                //取消收藏成功
                if (from == "profile") {
                    window.location.reload();
                }
            } else {
                //操作失败
                if (type == "collect") {
                    e.classList.remove("active");
                } else {
                    e.classList.add("active");
                }
                alert(response.message);
            }
        }
    });
}

/**
 * 设为全部已读
 */
function readAll() {
    if ($(".unread").length == 0) {
        alert("没有未读通知哦");
        return;
    } else {
        $.ajax({
            type: "post",
            url: "/notification",
            dataType: "json",
            success: function (response) {
                if (response.code == 200) {
                    window.location.reload();
                } else {
                    alert(response.message);
                }
            }
        });
    }
}

/**
 * 删除全部已读
 */
function deleteRead() {
    if ($(".notification").length - $(".unread").length == 0) {
        alert("没有已读通知哦");
        return;
    } else {
        $.ajax({
            type: "delete",
            url: "/notification",
            dataType: "json",
            success: function (response) {
                if (response.code == 200) {
                    window.location.reload();
                } else {
                    alert(response.message);
                }
            }
        });
    }
}

/**
 * 预览头像
 * @param obj
 */
function changepic(obj) {
    //console.log(obj.files[0]);//这里可以获取上传文件的name
    var newsrc = getObjectURL(obj.files[0]);
    document.getElementById('show').src = newsrc;
}

/**
 * 建立一個可存取到該file的url
 * @param file
 * @returns {*}
 */
function getObjectURL(file) {
    var url = null;
    // 下面函数执行的效果是一样的，只是需要针对不同的浏览器执行不同的 js 函数而已
    if (window.createObjectURL != undefined) { // basic
        url = window.createObjectURL(file);
    } else if (window.URL != undefined) { // mozilla(firefox)
        url = window.URL.createObjectURL(file);
    } else if (window.webkitURL != undefined) { // webkit or chrome
        url = window.webkitURL.createObjectURL(file);
    }
    return url;
}

/**
 * 修改头像
 */
function changeAvatar() {
    $("#change-avatar-btn").html("上传中...");
    $("#change-avatar-btn").attr("disabled", "disabled");
    var data = new FormData($("#change-avatar-form")[0]);
    $.ajax({
        type: "post",
        url: "/file/changeAvatar",
        data: data,
        dataType: "json",
        processData: false,
        contentType: false,
        success: function (response) {
            if (response.code == 200) {
                window.location.reload();
            } else {
                alert(response.message);
            }
        }
    });
}

