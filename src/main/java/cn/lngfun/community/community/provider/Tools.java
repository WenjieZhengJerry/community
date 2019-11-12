package cn.lngfun.community.community.provider;

import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.util.Timer;
import java.util.TimerTask;
import java.util.UUID;

/**
 * 工具组件
 */
@Component
public class Tools {
    /**
     * 获取ip地址
     *
     * @param request
     * @return
     */
    public static String getIpAddress(HttpServletRequest request) {
        String ip = request.getHeader("x-forwarded-for");

        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("Proxy-Client-IP");
        }
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("WL-Proxy-Client-IP");
        }
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        if (ip.equals("0:0:0:0:0:0:0:1")) {
            ip = "127.0.0.1";
        }

        return ip;
    }

    /**
     * 指定时间内移除session的某个元素
     *
     * @param session
     * @param attrName
     * @param minute
     */
    public static void removeAttrbute(final HttpSession session, final String attrName, final int minute) {
        final Timer timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                // 删除session中存的验证码
                session.removeAttribute(attrName);
                timer.cancel();
            }
        }, minute * 60 * 1000);
    }

    /**
     * 发送验证码
     *
     * @param email
     * @param request
     * @return
     */
    public static Object getAuthCode(String email, HttpServletRequest request) {
        //获取随机6位数的验证码
        String authCode = UUID.randomUUID().toString().substring(0, 6);
        //保存验证码到session中
        request.getSession().setAttribute("authCode", authCode);
        //10分钟后移除session中的验证码
        Tools.removeAttrbute(request.getSession(), "authCode", 10);
        //发送验证码到指定邮箱并返回结果
        return EmailProvider.getAuthCode(email, authCode);
    }
}
