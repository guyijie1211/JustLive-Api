package work.yj1211.live.utils.platForms;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import work.yj1211.live.utils.Global;
import work.yj1211.live.utils.HttpUtil;
import work.yj1211.live.utils.http.HttpRequest;
import work.yj1211.live.vo.LiveRoomInfo;
import work.yj1211.live.vo.Owner;
import work.yj1211.live.vo.platformArea.AreaInfo;

import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Egame {

    /**
     * 获取企鹅电竞分区房间
     * @param area
     * @param page
     * @param size
     * @return
     */
    public static List<LiveRoomInfo> getAreaRoom(String area, int page, int size){
        List<LiveRoomInfo> list = new ArrayList<>();
        String urlFront = "https://share.egame.qq.com/cgi-bin/pgg_live_async_fcgi?param=";
        String urlAfter = "{\"key\":{\"module\":\"pgg_live_read_ifc_mt_svr\",\"method\":\"get_pc_live_list\",\"param\":{\"appid\":\"" + area + "\",\"page_num\":" + page + ",\"page_size\":" + size + ",\"tag_id\":0,\"tag_id_str\":\"\"}}}";
        String reqUrl = null;

        try {
            reqUrl = URLEncoder.encode(urlAfter, "UTF-8");
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }


        String result = HttpUtil.doGet(urlFront + reqUrl);
        JSONObject resultJsonObj = JSON.parseObject(result);
        if ("成功".equals(resultJsonObj.getJSONObject("data").getJSONObject("key").getString("retMsg"))) {
            JSONArray data = resultJsonObj.getJSONObject("data").getJSONObject("key").getJSONObject("retBody")
                    .getJSONObject("data").getJSONObject("live_data").getJSONArray("live_list");
            for (int i = 0; i < data.size(); i++) {
                JSONObject roomInfo = data.getJSONObject(i);
                LiveRoomInfo liveRoomInfo = new LiveRoomInfo();
                liveRoomInfo.setPlatForm("egame");
                liveRoomInfo.setRoomId(roomInfo.getInteger("anchor_id").toString());
                liveRoomInfo.setCategoryId(roomInfo.getString("appid"));
                liveRoomInfo.setCategoryName(roomInfo.getString("appname"));
                liveRoomInfo.setRoomName(roomInfo.getString("title"));
                liveRoomInfo.setOwnerName(roomInfo.getString("anchor_name"));
                liveRoomInfo.setRoomPic(roomInfo.getJSONObject("video_info").getString("url_high_reslution"));
                liveRoomInfo.setOwnerHeadPic(roomInfo.getString("anchor_face_url"));
                liveRoomInfo.setOnline(roomInfo.getInteger("online"));
                liveRoomInfo.setIsLive(1);
                list.add(liveRoomInfo);
            }
        }
        return  list;
    }

    /**
     * 刷新分类缓存
     * @return
     */
    public static void refreshArea() {
        List<List<AreaInfo>> areaMapTemp = new ArrayList<>();
        List<AreaInfo> areaListTemp = new ArrayList<>();
        try {
            String room_url = "https://egame.qq.com/gamelist";
            String response = HttpRequest.create(room_url)
                    .get().getBody();
            Pattern pattern = Pattern.compile("/livelist\\?layoutid=(.+?)\".+?title=\"(.+?)\".+?>");//"/livelist\?layoutid=(.+?)\".+?title=\"(.+?)\".+?>");
            Matcher matcher = pattern.matcher(response);

            while (matcher.find()) {
                AreaInfo egameArea = new AreaInfo();
                egameArea.setAreaType("allArea");
                egameArea.setTypeName("所有分类");
                egameArea.setAreaId(matcher.group(1));
                egameArea.setAreaName(matcher.group(2));
                egameArea.setAreaPic("");
                egameArea.setPlatform("egame");
                Global.EgameCateMap.put(egameArea.getAreaId(), egameArea.getAreaName());
                Global.EgameCateMapVer.put(egameArea.getAreaName(), egameArea.getAreaId());
                areaListTemp.add(egameArea);
            }
            areaMapTemp.add(areaListTemp);
            Global.platformAreaMap.put("egame", areaMapTemp);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 根据分页获取推荐直播间
     * @param page 页数
     * @param size 每页大小
     * @return
     */
    public static List<LiveRoomInfo> getRecommend(int page, int size){
        List<LiveRoomInfo> list = new ArrayList<>();
        String urlFront = "https://share.egame.qq.com/cgi-bin/pgg_async_fcgi?param=";
        String urlAfter = "{\"key\":{\"module\":\"pgg_live_read_ifc_mt_svr\",\"method\":\"get_pc_live_list\",\"param\":{\"appid\":\"hot\",\"page_num\":" + page + ",\"page_size\":" + size + ",\"tag_id\":0,\"tag_id_str\":\"\"}}}";
        String reqUrl = null;

        try {
            reqUrl = URLEncoder.encode(urlAfter, "UTF-8");
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }


        String result = HttpUtil.doGet(urlFront + reqUrl);
        JSONObject resultJsonObj = JSON.parseObject(result);
        if ("成功".equals(resultJsonObj.getJSONObject("data").getJSONObject("key").getString("retMsg"))) {
            JSONArray data = resultJsonObj.getJSONObject("data").getJSONObject("key").getJSONObject("retBody")
                    .getJSONObject("data").getJSONObject("live_data").getJSONArray("live_list");
            for (int i = 0; i < data.size(); i++) {
                JSONObject roomInfo = data.getJSONObject(i);
                LiveRoomInfo liveRoomInfo = new LiveRoomInfo();
                liveRoomInfo.setPlatForm("egame");
                liveRoomInfo.setRoomId(roomInfo.getInteger("anchor_id").toString());
                liveRoomInfo.setCategoryId(roomInfo.getString("appid"));
                liveRoomInfo.setCategoryName(roomInfo.getString("appname"));
                liveRoomInfo.setRoomName(roomInfo.getString("title"));
                liveRoomInfo.setOwnerName(roomInfo.getString("anchor_name"));
                liveRoomInfo.setRoomPic(roomInfo.getJSONObject("video_info").getString("url_high_reslution"));
                liveRoomInfo.setOwnerHeadPic(roomInfo.getString("anchor_face_url"));
                liveRoomInfo.setOnline(roomInfo.getInteger("online"));
                liveRoomInfo.setIsLive(1);
                list.add(liveRoomInfo);
            }
        }
        return list;
    }

    /**
     * 获取单个直播间信息
     * @param roomId 房间号
     * @return
     */
    public static LiveRoomInfo getRoomInfo(String roomId){
        LiveRoomInfo liveRoomInfo = new LiveRoomInfo();
        String live_addr = null;
        Integer online = null;
        String roomName = null;

        //获取房间信息
        String urlFront = "https://share.egame.qq.com/cgi-bin/pgg_async_fcgi?param=";
        String urlAfter = "{\"0\":{\"module\":\"pgg_live_read_svr\",\"method\":\"get_live_and_profile_info\",\"param\":{\"anchor_id\":" + roomId + "}}}";
        String reqUrl = null;
        try {
            reqUrl = URLEncoder.encode(urlAfter, "UTF-8");
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
        String result = HttpUtil.doGet(urlFront + reqUrl);
        JSONObject resultJsonObj = JSON.parseObject(result);
        if ("成功".equals(resultJsonObj.getJSONObject("data").getJSONObject("0").getString("retMsg"))) {
            JSONObject data = resultJsonObj.getJSONObject("data").getJSONObject("0")
                    .getJSONObject("retBody").getJSONObject("data");
            JSONObject room_info = data.getJSONObject("video_info");
            JSONObject owner_info = data.getJSONObject("profile_info");
            liveRoomInfo.setPlatForm("egame");
            liveRoomInfo.setRoomId(roomId);
            liveRoomInfo.setCategoryId(room_info.getString("appid"));
            liveRoomInfo.setCategoryName(room_info.getString("appname"));
            liveRoomInfo.setRoomName(room_info.getString("title"));
            liveRoomInfo.setOwnerName(owner_info.getString("nick_name"));
            liveRoomInfo.setOwnerHeadPic(owner_info.getString("face_url"));
            liveRoomInfo.setIsLive((owner_info.getInteger("is_live") == 1) ? 1 : 0);
            live_addr = room_info.getString("live_addr");
        }

        //获取观看人数
        String urlFrontCount = "https://share.egame.qq.com/cgi-bin/pgg_async_fcgi?param=";
        String urlAfterCount = "{\"key\":{\"module\":\"pgg_live_video_svr\",\"method\":\"get_online\",\"param\":{\"pid\":\"" + live_addr + "\"}}}";
        String reqUrlCount = null;
        try {
            reqUrlCount = URLEncoder.encode(urlAfterCount, "UTF-8");
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
        String resultCount = HttpUtil.doGet(urlFrontCount + reqUrlCount);
        JSONObject resultJsonObjCount = JSON.parseObject(resultCount);
        if ("成功".equals(resultJsonObjCount.getJSONObject("data").getJSONObject("key").getString("retMsg"))) {
            JSONObject room_info = resultJsonObjCount.getJSONObject("data").getJSONObject("key")
                    .getJSONObject("retBody").getJSONObject("data");
            online = room_info.getInteger("online");
        }

        liveRoomInfo.setOnline(online);
        return liveRoomInfo;
    }

    /**
     * 获取直播间所有清晰度的url
     * @param urls
     * @param rid
     */
    public static void get_real_url(Map<String, String> urls, String rid) {
        String urlFrontToken = "https://share.egame.qq.com/cgi-bin/pgg_async_fcgi?param=";
        String urlAfterToken = "{\"0\": {\"module\": \"pgg.ws_token_go_svr.DefObj\", \"method\": \"get_token\", \"param\": {\"scene_flag\": 16, \"subinfo\": {\"page\": {\"scene\": 1, " +
                "\"page_id\": " + rid + ", \"msg_type_list\": [1, 2]}}, \"version\": 1, " +
                "\"message_seq\": -1, \"dc_param\": {\"params\": {\"info\": {\"aid\": \"" + rid + "\"}}, \"position\": {\"page_id\": \"QG_HEARTBEAT_PAGE_LIVE_ROOM\"}, \"refer\": {}}, \"other_uid\": 0}}}";
        String reqUrlToken = null;
        try {
            reqUrlToken = URLEncoder.encode(urlAfterToken, "UTF-8");
        } catch (Exception e) {
            e.printStackTrace();
        }
        String resultToken = HttpUtil.doGet(urlFrontToken + reqUrlToken);
        JSONObject resultJsonObjToken = JSON.parseObject(resultToken);
        if ("ok".equals(resultJsonObjToken.getJSONObject("data").getJSONObject("0").getString("retMsg"))) {
            JSONObject data = resultJsonObjToken.getJSONObject("data").getJSONObject("0")
                    .getJSONObject("retBody").getJSONObject("data");
            urls.put("token", data.getString("token"));
        }


        //获取房间信息
        String urlFront = "https://share.egame.qq.com/cgi-bin/pgg_async_fcgi?param=";
        String urlAfter = "{\"0\":{\"module\":\"pgg_live_read_svr\",\"method\":\"get_live_and_profile_info\",\"param\":{\"anchor_id\":" + rid + "}}}";
        String reqUrl = null;
        try {
            reqUrl = URLEncoder.encode(urlAfter, "UTF-8");
        } catch (Exception e) {
            e.printStackTrace();
        }
        String result = HttpUtil.doGet(urlFront + reqUrl);
        JSONObject resultJsonObj = JSON.parseObject(result);
        if ("成功".equals(resultJsonObj.getJSONObject("data").getJSONObject("0").getString("retMsg"))) {
            JSONObject data = resultJsonObj.getJSONObject("data").getJSONObject("0")
                    .getJSONObject("retBody").getJSONObject("data");
            JSONObject room_info = data.getJSONObject("video_info");
            JSONArray urlArray = room_info.getJSONArray("stream_infos");
            for (int i = 0; i < urlArray.size(); i++) {
                JSONObject urlObj = urlArray.getJSONObject(i);
                urls.put(getBit(urlObj.getInteger("bitrate")), urlObj.getString("play_url").replace(".flv", ".m3u8"));
            }
        }
    }

    /**
     * bitrate映射
     * @param bit 获取到的bit值
     * @return
     */
    private static String getBit(int bit) {
        if (bit == 0) {
            return "OD";
        }
        if (bit == 4000) {
            return "HD";
        }
        if (bit == 2000) {
            return "SD";
        }
        if (bit == 1200) {
            return "LD";
        }
        if (bit == 500) {
            return "FD";
        }
        return null;
    }
}
