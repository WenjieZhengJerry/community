package cn.lngfun.community.community.provider;

import cn.lngfun.community.community.dto.QQAccessTokenDTO;
import cn.lngfun.community.community.dto.QQUserDTO;
import cn.lngfun.community.community.exception.CustomizeErrorCode;
import cn.lngfun.community.community.exception.CustomizeException;
import com.alibaba.fastjson.JSON;
import lombok.extern.slf4j.Slf4j;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class QQProvider {

    /**
     * 获取access token
     * @param qqAccessTokenDTO
     * @return
     */
    public String getAccessToken(QQAccessTokenDTO qqAccessTokenDTO) {
        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder()
                .url("https://graph.qq.com/oauth2.0/token?grant_type=" +
                    qqAccessTokenDTO.getGrantType() + "&client_id=" +
                    qqAccessTokenDTO.getClient_id() + "&client_secret=" +
                    qqAccessTokenDTO.getClient_secret() + "&code=" +
                    qqAccessTokenDTO.getCode() + "&redirect_uri=" +
                    qqAccessTokenDTO.getRedirect_uri())
                .build();

        try (Response response = client.newCall(request).execute()) {
            String str = response.body().string();
            String token = str.split("&")[0].split("=")[1];

            return token;
        } catch (Exception e) {
            e.printStackTrace();
            log.error("获取access token失败，网络连接异常", e);
            throw new CustomizeException(CustomizeErrorCode.NETWORK_CONNECT_FAIL);
        }
    }

    /**
     * 获取openid
     * @param accessToken
     * @return
     */
    public String getOpenid(String accessToken) {
        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder()
                .url("https://graph.qq.com/oauth2.0/me?access_token=" + accessToken)
                .build();

        try (Response response = client.newCall(request).execute()) {
            String string = response.body().string();
            String openid = string.split(":")[2].substring(1, 33);

            return openid;
        } catch (Exception e) {
            e.printStackTrace();
            log.error("获取openid失败，网络连接异常", e);
            throw new CustomizeException(CustomizeErrorCode.NETWORK_CONNECT_FAIL);
        }
    }

    /**
     * 获取用户信息
     * @param accessToken
     * @param qqClientId
     * @param openid
     * @return
     */
    public QQUserDTO getUser(String accessToken, String qqClientId, String openid) {
        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder()
                .url("https://graph.qq.com/user/get_user_info?access_token=" +
                        accessToken + "&oauth_consumer_key=" +
                        qqClientId + "&openid=" + openid)
                .build();

        try (Response response = client.newCall(request).execute()) {
            String string = response.body().string();
            QQUserDTO qqUserDTO = JSON.parseObject(string, QQUserDTO.class);

            return qqUserDTO;
        } catch (Exception e) {
            e.printStackTrace();
            log.error("获取用户信息失败，网络连接异常", e);
            throw new CustomizeException(CustomizeErrorCode.NETWORK_CONNECT_FAIL);
        }
    }
}
