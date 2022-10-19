package work.yj1211.live.utils.platForms;

import cn.hutool.core.collection.CollectionUtil;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import work.yj1211.live.utils.http.HttpRequest;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * FixBilili.class
 *
 * @author dr5hx
 * @date 2022/10/19 21:36
 */
@Component
@Slf4j
public class FixBilili {
    private String bilibiliFD = "0";
    private String bilibiliLD = "250";
    private String bilibiliSD = "400";
    private String bilibiliHD = "10000";
    private String bilibiliOD = "20000";
    @Resource
    private Bilibili bilibili;
    public void get_real_url(Map<String, String> urls, String rid) {
        JSONObject roomInfo = bilibili.get_real_rid(rid);

        if (roomInfo == null) {
            urls.put("state", "notExist");
        }
        if (!roomInfo.getBoolean("live_status")) {
            urls.put("state", "offline");
        }

        String fd = get_single_url(roomInfo.getLongValue("room_id"), bilibiliFD);
        if (bilibiliFD.equals(fd.split("&qn=")[1].split("&trid=")[0])) {
            urls.put("FD", fd);
        }
        String ld = get_single_url(roomInfo.getLongValue("room_id"), bilibiliLD);
        if (bilibiliLD.equals(ld.split("&qn=")[1].split("&trid=")[0])) {
            urls.put("LD", ld);
        }
        String sd = get_single_url(roomInfo.getLongValue("room_id"), bilibiliSD);
        if (bilibiliSD.equals(sd.split("&qn=")[1].split("&trid=")[0])) {
            urls.put("SD", sd);
        }
        String hd = get_single_url(roomInfo.getLongValue("room_id"), bilibiliHD);
        if (bilibiliHD.equals(hd.split("&qn=")[1].split("&trid=")[0])) {
            urls.put("HD", hd);
        }
        String od = get_single_url(roomInfo.getLongValue("room_id"), bilibiliOD);
        if (bilibiliOD.equals(od.split("&qn=")[1].split("&trid=")[0])) {
            urls.put("OD", od);
        }
    }

    private static final Logger logger = LoggerFactory.getLogger(FixBilili.class);
    private String get_single_url(Long roomId, String qn) {
        String room_url = "https://api.live.bilibili.com/xlive/web-room/v2/index/getRoomPlayInfo?"
                + "room_id=" + roomId
                + "&protocol=0,1"
                + "&format=0,1,2"
                + "&codec=0,1"
                + "&qn=" + qn
                + "&platform=h5"
                + "&ptype=8";
        JSONObject response = HttpRequest.create(room_url).putHeader("User-Agent", "Mozilla/5.0 (iPod; CPU iPhone OS 14_5 like Mac OS X) AppleWebKit/605.1.15 (KHTML, like Gecko) CriOS/87.0.4280.163 Mobile/15E148 Safari/604.1").get().getBodyJson();
        JSONArray jsonArray = response.getJSONObject("data").getJSONObject("playurl_info").getJSONObject("playurl").getJSONArray("stream");
        final String suffix = "ts";
        List<String> list = new ArrayList<>();
        jsonArray.forEach(e -> {
            JSONObject data = (JSONObject) e;
            JSONArray formatArray = data.getJSONArray("format");
            JSONObject format = (JSONObject) formatArray.get(formatArray.size() - 1);
            String formatName = ((JSONObject) formatArray.get(0)).getString("format_name");
            if (formatName.equals(suffix)) {
                JSONArray codec = format.getJSONArray("codec");
                JSONObject jsonObject = (JSONObject) codec.get(0);
                String base_url = jsonObject.getString("base_url");
                JSONArray url_info = jsonObject.getJSONArray("url_info");
                url_info.forEach(q -> {
                    JSONObject object = (JSONObject) q;
                    String host = object.getString("host");
                    String extra = object.getString("extra");
                    list.add(host + base_url + extra);
                });

            }
        });
        if (CollectionUtil.isNotEmpty(list)) {
            return list.get(0);
        } else {
            logger.error("BILIBILI---获取真实地址异常---roomId：" + roomId);
            return "获取失败";
        }
    }

}
