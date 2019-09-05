package cn.lngfun.community.community.provider;

import cn.lngfun.community.community.dto.ResultDTO;
import cn.lngfun.community.community.exception.CustomizeErrorCode;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.mail.SimpleEmail;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class EmailProvider {
    //被static修饰的变量不能直接用@Value赋值，要重载set方法
    private static String emailHost;

    @Value("${email.host}")
    public void setEmailHost(String host) {
        emailHost = host;
    }

    private static String emailUserEmail;

    @Value("${email.user.email}")
    public void setEmailUserEmail(String userEmail) {
        emailUserEmail = userEmail;
    }

    private static String emailUserName;

    @Value("${email.user.name}")
    public void setEmailUserName(String userName) {
        emailUserName = userName;
    }

    private static String emailPassword;

    @Value("${email.password}")
    public void setEmailPassword(String password) {
        emailPassword = password;
    }

    /**
     * 发送验证码到指定邮箱
     *
     * @param receiver 收件人
     * @param authCode 验证码
     * @return
     */
    public static Object getAuthCode(String receiver, String authCode) {
        SimpleEmail simpleEmail = new SimpleEmail();
        //设置字符编码方式
        simpleEmail.setCharset("UTF-8");
        try {
            //设置SMTP邮件服务器，比如:smtp.qq.com
            simpleEmail.setHostName(emailHost);
            //设置邮件服务器端口号，默认为25
            simpleEmail.setSmtpPort(587);
            //设置登入认证服务器的 用户名 和 授权密码 （发件人））
            simpleEmail.setAuthentication(emailUserName, emailPassword);
            //设置发送源邮箱
            simpleEmail.setFrom(emailUserEmail);
            //设置收件人可以是多个：simpleEmail.addTo("xxx@qq.com", "xxx@163.com");
            simpleEmail.addTo(receiver);
            //设置主题
            simpleEmail.setSubject("零分社区-验证码");
            //设置邮件内容
            simpleEmail.setMsg("欢迎使用零分社区，您的验证码是：" + authCode + "，该验证码10分钟内有效，过期请重新获取");
            //发送邮件
            simpleEmail.send();
        } catch (Exception e) {
            log.error("验证码发送失败：{}", e);
            return ResultDTO.errorOf(CustomizeErrorCode.EMAIL_SEND_FAIL);
        }

        return ResultDTO.okOf();
    }
}
