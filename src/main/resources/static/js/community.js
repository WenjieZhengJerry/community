$(document).ready(function () {
    $("#email").val($.cookie('tEmail'));
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
                    }).append($("<img/>", {
                        "class": "media-object img-rounded",
                        "src": comment.user.avatarUrl
                    }));

                    var mediaBodyElement = $("<div/>", {
                        "class": "media-body"
                    }).append($("<h5/>", {
                        "class": "media-heading",
                        "html": comment.user.name
                    })).append($("<span/>", {
                        "class": "pull-right",
                        "html": "<a href='javascript:void(0)'>回复</a>"
                    })).append($("<div/>", {
                        "html": comment.content
                    })).append($("<div/>", {
                        "class": "menu"
                    }).append($("<span/>", {
                        "class": "pull-right",
                        "html": moment(comment.gmtCreate).format('YYYY-MM-DD HH:mm')
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
            type: "POST",
            url: "/deleteQuestion?id=" + id,
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
function login() {
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
    var id = e.getAttribute("data-id");
    if (e.classList.contains("active")) {
        e.classList.remove("active");
        e.children[1].innerHTML = String(Number(e.children[1].innerHTML) - 1);

        $.ajax({
            type: "post",
            url: "/likeOrDislike?commentId=" + id + "&option=0",
            dataType: "json",
            success: function (response) {
                if (response.code != 200) {
                    alert(response.message);
                    e.classList.add("active");
                    e.children[1].innerHTML = String(Number(e.children[1].innerHTML) + 1);
                }
            }
        });
    } else {
        e.classList.add("active");
        e.children[1].innerHTML = String(Number(e.children[1].innerHTML) + 1);

        $.ajax({
            type: "post",
            url: "/likeOrDislike?commentId=" + id + "&option=1",
            dataType: "json",
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
