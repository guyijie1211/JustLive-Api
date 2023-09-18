package work.yj1211.live.service.platforms.impl;

import cn.hutool.core.util.ReUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.http.Header;
import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.client.utils.URIBuilder;
import org.springframework.stereotype.Service;
import work.yj1211.live.enums.Platform;
import work.yj1211.live.model.LiveRoomInfo;
import work.yj1211.live.model.Owner;
import work.yj1211.live.model.platformArea.AreaInfo;
import work.yj1211.live.service.platforms.BasePlatform;
import work.yj1211.live.utils.Global;

import java.security.SecureRandom;
import java.util.*;

@Slf4j
@Service
public class Douyin implements BasePlatform {
    private String COOKIE = "";

    @Override
    public String getPlatformCode() {
        return Platform.DOUYIN.getCode();
    }

    @Override
    public void getRealUrl(Map<String, String> urls, String rid) {

    }

    @Override
    public LiveRoomInfo getRoomInfo(String roomId) {
        LiveRoomInfo roomInfo = new LiveRoomInfo();
        try {
            HttpResponse response = HttpRequest.get("https://live.douyin.com/webcast/room/web/enter/")
                    .addHeaders(getHeader())
                    .form(getRoomInfoParam(roomId))
                    .execute();
            updateCOOKIE(response.header(Header.SET_COOKIE));
            String result = response.body();
            JSONObject resultJsonObj = JSONUtil.parseObj(result);
            if (resultJsonObj.getInt("status_code") == 0) {
                JSONObject roomObj = (JSONObject) resultJsonObj.getJSONObject("data").getJSONArray("data").get(0);
                JSONObject ownerObj = resultJsonObj.getJSONObject("data").getJSONObject("user");

                roomInfo.setPlatForm(getPlatformCode());
                roomInfo.setRoomId(roomId);
                roomInfo.setRoomName(roomObj.getStr("title"));
                roomInfo.setIsLive(roomObj.getInt("status") == 2 ? 1 : 0);
                if (roomInfo.getIsLive() == 1) {
                    roomInfo.setRoomPic((String) roomObj.getJSONObject("cover").getJSONArray("url_list").get(0));
                } else {
                    roomInfo.setRoomPic("");
                }
                roomInfo.setCategoryName(resultJsonObj.getJSONObject("data").getJSONObject("partition_road_map").getJSONObject("sub_partition").getJSONObject("partition").getStr("title"));
                roomInfo.setOnline(roomObj.getJSONObject("room_view_stats").getInt("display_value"));
                roomInfo.setOwnerName(ownerObj.getStr("nickname"));
                roomInfo.setOwnerHeadPic((String) ownerObj.getJSONObject("avatar_thumb").getJSONArray("url_list").get(0));
            }
        } catch (Exception e) {
            log.error("抖音---获取房间信息异常", e);
        }
        return roomInfo;
    }

    @Override
    public List<LiveRoomInfo> getRecommend(int page, int size) {
        List<LiveRoomInfo> list = new ArrayList<>();
        try {
            HttpResponse response = HttpRequest.get("https://live.douyin.com/webcast/web/partition/detail/room/")
                    .addHeaders(getHeader())
                    .form(getRecommendRoomsParam(page))
                    .execute();
            updateCOOKIE(response.header(Header.SET_COOKIE));
            String result = response.body();
            JSONObject resultJsonObj = JSONUtil.parseObj(result);
            if (resultJsonObj.getInt("status_code") == 0) {
                JSONArray data = resultJsonObj.getJSONObject("data").getJSONArray("data");
                data.forEach(room -> {
                    JSONObject roomObj = ((JSONObject) room).getJSONObject("room");
                    JSONObject ownerObj = roomObj.getJSONObject("owner");

                    LiveRoomInfo roomInfo = new LiveRoomInfo();
                    roomInfo.setPlatForm(getPlatformCode());
                    roomInfo.setRoomId(((JSONObject) room).getStr("web_rid"));
                    roomInfo.setRoomName(roomObj.getStr("title"));
                    roomInfo.setRoomPic((String) roomObj.getJSONObject("cover").getJSONArray("url_list").get(0));
                    roomInfo.setIsLive(1);
                    roomInfo.setCategoryName(((JSONObject) room).getStr("tag_name"));
                    roomInfo.setOnline(roomObj.getJSONObject("room_view_stats").getInt("display_value"));
                    roomInfo.setOwnerName(ownerObj.getStr("nickname"));
                    roomInfo.setOwnerHeadPic((String) ownerObj.getJSONObject("avatar_thumb").getJSONArray("url_list").get(0));
                    list.add(roomInfo);
                });
            }
        } catch (Exception e) {
            log.error("抖音---获取推荐房间列表信息异常", e);
        }
        return list;
    }

    @Override
    public List<AreaInfo> getAreaList() {
        List<AreaInfo> list = new ArrayList<>();
        try {
            HttpResponse response = HttpRequest.get("https://live.douyin.com/hot_live")
                    .addHeaders(getHeader())
                    .execute();
            updateCOOKIE(response.header(Header.SET_COOKIE));
            String result = response.body();
            String regex = "8:\\[\\\\\"\\$\\\\\",\\\\\"\\$L11\\\\\",null,(.*?)\\]\\\\n";
            String renderData = ReUtil.get(regex, result, 1);
            String renderJsonString = renderData.replaceAll("\\\\", "");
            JSONObject resultJsonObj = JSONUtil.parseObj(renderJsonString);
            JSONArray data = resultJsonObj.getJSONArray("categoryData");
            data.forEach(categoryData -> {
                JSONObject partition = ((JSONObject) categoryData).getJSONObject("partition");
                String areaTypeId = partition.getStr("id_str");
                String areaTypeName = partition.getStr("title");

                JSONArray subPartitionArray = ((JSONObject) categoryData).getJSONArray("sub_partition");
                subPartitionArray.forEach(subObj -> {
                    JSONObject subPartition = ((JSONObject) subObj).getJSONObject("partition");
                    AreaInfo areaInfo = new AreaInfo();
                    areaInfo.setAreaType(areaTypeId);
                    areaInfo.setTypeName(areaTypeName);
                    areaInfo.setPlatform(getPlatformCode());
                    areaInfo.setAreaPic("");
                    areaInfo.setAreaId(subPartition.getStr("id_str"));
                    areaInfo.setAreaName(subPartition.getStr("title"));
                    list.add(areaInfo);
                });
            });
        } catch (Exception e) {
            log.error("抖音---获取分区信息异常", e);
        }
        return list;
    }

    @Override
    public List<LiveRoomInfo> getAreaRoom(String area, int page, int size) {
        List<LiveRoomInfo> list = new ArrayList<>();
        try {
            AreaInfo areaInfo = Global.getAreaInfo(getPlatformCode(), area);
            HttpResponse response = HttpRequest.get("https://live.douyin.com/webcast/web/partition/detail/room/")
                    .addHeaders(getHeader())
                    .form(getAreaRoomParam(areaInfo.getAreaId(), page))
                    .execute();
            updateCOOKIE(response.header(Header.SET_COOKIE));
            String result = response.body();
            JSONObject resultJsonObj = JSONUtil.parseObj(result);
            if (resultJsonObj.getInt("status_code") == 0) {
                JSONArray data = resultJsonObj.getJSONObject("data").getJSONArray("data");
                Iterator<Object> it = data.iterator();
                while(it.hasNext()){
                    JSONObject totalInfo = (JSONObject) it.next();
                    JSONObject roomInfo = totalInfo.getJSONObject("room");
                    JSONObject ownerInfo = roomInfo.getJSONObject("owner");
                    LiveRoomInfo liveRoomInfo = new LiveRoomInfo();
                    liveRoomInfo.setPlatForm(getPlatformCode());
                    liveRoomInfo.setRoomId(totalInfo.getStr("web_rid"));
                    liveRoomInfo.setCategoryId(areaInfo.getAreaId());
                    liveRoomInfo.setCategoryName(totalInfo.getStr("tag_name"));
                    liveRoomInfo.setRoomName(roomInfo.getStr("title"));
                    liveRoomInfo.setOwnerName(ownerInfo.getStr("nickname"));
                    liveRoomInfo.setRoomPic((String) roomInfo.getJSONObject("cover").getJSONArray("url_list").get(0));
                    liveRoomInfo.setOwnerHeadPic((String) ownerInfo.getJSONObject("avatar_thumb").getJSONArray("url_list").get(0));
                    liveRoomInfo.setOnline(roomInfo.getJSONObject("room_view_stats").getInt("display_value"));
                    liveRoomInfo.setIsLive(1);
                    list.add(liveRoomInfo);
                }
            }
        } catch (Exception e) {
            log.error("抖音---获取分区房间异常---area：" + area, e);
        }

        return list;
    }

    @Override
    public List<Owner> search(String keyWords) {
        List<Owner> list = new ArrayList<>();
        try {
            URIBuilder uriBuilder = new URIBuilder("https://www.douyin.com/aweme/v1/web/live/search/")
                    .setScheme("https")
                    .setPort(443)
                    .addParameter("device_platform", "webapp")
                    .addParameter("device_platform", "webapp")
                    .addParameter("aid", "6383")
                    .addParameter("channel", "channel_pc_web")
                    .addParameter("search_channel", "aweme_live")
                    .addParameter("keyword", keyWords)
                    .addParameter("search_source", "switch_tab")
                    .addParameter("query_correct_type", "1")
                    .addParameter("is_filter_search", "0")
                    .addParameter("from_group_id", "")
                    .addParameter("offset", "0")
                    .addParameter("count", "10")
                    .addParameter("pc_client_type", "1")
                    .addParameter("version_code", "170400")
                    .addParameter("version_name", "17.4.0")
                    .addParameter("cookie_enabled", "true")
                    .addParameter("screen_width", "1980")
                    .addParameter("screen_height", "1080")
                    .addParameter("browser_language", "zh-CN")
                    .addParameter("browser_platform", "Win32")
                    .addParameter("browser_name", "Edge")
                    .addParameter("browser_version", "114.0.1823.58")
                    .addParameter("browser_online", "true")
                    .addParameter("engine_name", "Blink")
                    .addParameter("engine_version", "114.0.0.0")
                    .addParameter("os_name", "Windows")
                    .addParameter("os_version", "10")
                    .addParameter("cpu_core_num", "12")
                    .addParameter("device_memory", "8")
                    .addParameter("platform", "PC")
                    .addParameter("downlink", "4.7")
                    .addParameter("effective_type", "4g")
                    .addParameter("round_trip_time", "100")
                    .addParameter("webid", "7247041636524377637");

            String requestUrl = signUrl(uriBuilder.build().toString());
            HttpResponse response = HttpRequest.get(requestUrl)
                    .addHeaders(getRealRmooIdHead())
                    .execute();
            String result = response.body();
            JSONObject resultJsonObj = JSONUtil.parseObj(result);
            if (resultJsonObj.getInt("status_code") == 0) {
                JSONArray data = resultJsonObj.getJSONObject("data").getJSONArray("data");
                data.forEach(room -> {
                    JSONObject roomObj = ((JSONObject) room).getJSONObject("room");
                    JSONObject ownerObj = roomObj.getJSONObject("owner");

                    LiveRoomInfo roomInfo = new LiveRoomInfo();
                    roomInfo.setPlatForm(getPlatformCode());
                    roomInfo.setRoomId(((JSONObject) room).getStr("web_rid"));
                    roomInfo.setRoomName(roomObj.getStr("title"));
                    roomInfo.setRoomPic((String) roomObj.getJSONObject("cover").getJSONArray("url_list").get(0));
                    roomInfo.setIsLive(1);
                    roomInfo.setCategoryName(((JSONObject) room).getStr("tag_name"));
                    roomInfo.setOnline(roomObj.getJSONObject("room_view_stats").getInt("display_value"));
                    roomInfo.setOwnerName(ownerObj.getStr("nickname"));
                    roomInfo.setOwnerHeadPic((String) ownerObj.getJSONObject("avatar_thumb").getJSONArray("url_list").get(0));

                });
            }
        } catch (Exception e) {
            log.error("抖音---获取推荐房间列表信息异常", e);
        }
        return null;
    }

    /**
     * 获取抖音接口header，主要是cookie
     *
     * @return
     */
    private Map<String, String> getHeader() {
        Map<String, String> headerMap = new HashMap<>();
        if (StrUtil.isEmpty(COOKIE)) {
            String[] cookieArray = HttpRequest.get("https://live.douyin.com").execute().header(Header.SET_COOKIE).split(";");
            COOKIE = cookieArray[0];
        }
        headerMap.put(Header.COOKIE.getValue(), COOKIE);
        return headerMap;
    }

    /**
     * 更新cookie
     *
     * @param cookie
     */
    private void updateCOOKIE(String cookie) {
        String[] cookieArray = cookie.split(";");
        COOKIE = cookieArray[0];
    }

    /**
     * 获取分类房间的请求param
     *
     * @param areaId   分类id
     * @param page  页数
     * @return
     */
    private Map<String, Object> getAreaRoomParam(String areaId, int page) {
        Map<String, Object> paramMap = new HashMap<>();
        paramMap.put("aid", 6383);
        paramMap.put("app_name", "douyin_web");
        paramMap.put("live_id", 1);
        paramMap.put("device_platform", "web");
        paramMap.put("count", 15);
        paramMap.put("offset", (page - 1) * 15);
        paramMap.put("partition", areaId);
        paramMap.put("partition_type", 1);
        paramMap.put("req_from", 2);
        return paramMap;
    }

    /**
     * 获取推荐房间的请求param
     *
     * @return
     */
    private Map<String, Object> getRecommendRoomsParam(int page) {
        Map<String, Object> paramMap = new HashMap<>();
        paramMap.put("aid", 6383);
        paramMap.put("app_name", "douyin_web");
        paramMap.put("live_id", 1);
        paramMap.put("device_platform", "web");
        paramMap.put("count", 15);
        paramMap.put("offset", (page - 1) * 15);
        paramMap.put("partition", 720);
        paramMap.put("partition_type", 1);
        return paramMap;
    }

    /**
     * 获取房间信息的请求param
     *
     * @return
     */
    private Map<String, Object> getRoomInfoParam(String webRoomId) {
        Map<String, Object> paramMap = new HashMap<>();
        paramMap.put("aid", 6383);
        paramMap.put("app_name", "douyin_web");
        paramMap.put("live_id", 1);
        paramMap.put("device_platform", "web");
        paramMap.put("enter_from", "web_live");
        paramMap.put("web_rid", webRoomId);
        paramMap.put("room_id_str", webRoomId);
//        paramMap.put("room_id_str", getRealRoomId(webRoomId));
        paramMap.put("enter_source", "");
        paramMap.put("Room-Enter-User-Login-Ab", 0);
        paramMap.put("is_need_double_stream", false);
        paramMap.put("cookie_enabled", true);
        paramMap.put("screen_width", 1980);
        paramMap.put("screen_height", 1080);
        paramMap.put("browser_language", "zh-CN");
        paramMap.put("browser_platform", "Win32");
        paramMap.put("browser_name", "Edge");
        paramMap.put("browser_version", "114.0.1823.51");
        return paramMap;
    }

    /**
     * 获取realRoomId的请求头
     *
     * @return
     */
    private Map<String, String> getRealRmooIdHead() {
        Map<String, String> headerMap = new HashMap<>();
        headerMap.put("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0.7");
        headerMap.put("Authority", "live.douyin.com");
        headerMap.put("Referer", "https://live.douyin.com");
        headerMap.put("Cookie", "__ac_nonce=" + generateRandomString(21));
        headerMap.put("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/114.0.0.0 Safari/537.36 Edg/114.0.1823.51");
        return headerMap;
    }

    /**
     * 获取真实roomId
     *
     * @param webRoomId 网页roomId
     * @return realRoomId
     */
    private String getRealRoomId(String webRoomId) {
        try {
            HttpResponse response = HttpRequest.get("https://live.douyin.com/" + webRoomId)
                    .addHeaders(getRealRmooIdHead())
                    .execute();
            String result = response.body();
            String regex = "a:\\[\\\\\"\\$\\\\\",\\\\\"\\$L11\\\\\",null,(.*?)\\]\\\\n";
            String renderData = ReUtil.get(regex, result, 1);
            String renderJsonString = renderData.replaceAll("\\\\", "");
            JSONObject resultJsonObj = JSONUtil.parseObj(renderJsonString);
            return resultJsonObj.getJSONObject("state").getJSONObject("roomStore").getJSONObject("roomInfo").getStr("roomId");
        } catch (Exception e) {
            log.error("抖音---获取realRoomId异常", e);
        }
        return webRoomId;
    }

    /**
     * 抖音url请求价签(搜索接口用)
     *
     * @param url 请求url
     * @return
     */
    public String signUrl(String url) {
        JSONObject obj = new JSONObject();
        obj.set("url", url);
        obj.set("userAgent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/114.0.0.0 Safari/537.36 Edg/114.0.1823.51");
        String result = HttpRequest.post("https://tk.nsapps.cn/")
                .header(Header.CONTENT_TYPE, "application/json")
                .body(JSONUtil.toJsonStr(obj))
                .execute().body();
        JSONObject resultObj = JSONUtil.parseObj(result);
        return resultObj.getJSONObject("data").getStr("url");
    }


    private String generateRandomString(int length) {
        SecureRandom random = new SecureRandom();
        StringBuilder stringBuilder = new StringBuilder();
        for (int i = 0; i < length; i++) {
            int value = random.nextInt(16);
            stringBuilder.append(Integer.toHexString(value));
        }
        return stringBuilder.toString();
    }
}
