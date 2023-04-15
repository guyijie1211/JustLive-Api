package work.yj1211.live.service.platforms.impl;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import work.yj1211.live.enums.Platform;
import work.yj1211.live.service.platforms.BasePlatform;
import work.yj1211.live.utils.HttpUtil;
import work.yj1211.live.utils.http.HttpRequest;
import work.yj1211.live.model.LiveRoomInfo;
import work.yj1211.live.model.Owner;
import work.yj1211.live.model.platformArea.AreaInfo;

import java.util.*;

@Slf4j
@Component
public class Bilibili implements BasePlatform {
    //Bilibili清晰度
    private String bilibiliFD = "80";
    private String bilibiliLD = "150";
    private String bilibiliSD = "250";
    private String bilibiliHD = "400";
    private String bilibiliOD = "10000";

    /**
     * 获取真实直播间id
     * @param rid
     * @return
     */
    public JSONObject get_real_rid(String rid) {
        String room_url = "https://api.live.bilibili.com/room/v1/Room/room_init?id=" + rid;
        JSONObject response = HttpRequest.create(room_url).get().getBodyJson();
        int code = response.getInt("code");
        if(code == 0){
            JSONObject data = response.getJSONObject("data");
            JSONObject jsonObject = new JSONObject();
            jsonObject.set("live_status", data.getBool("live_status"));
            jsonObject.set("room_id", data.getLong("room_id"));
            return jsonObject;
        }else {
            log.error("BILIBILI---获取直播间真实id异常---roomId：" + rid);
            return null;
        }
    }

    @Override
    public String getPlatformName() {
        return Platform.BILIBILI.getName();
    }

    /**
     * 获取直播间所有清晰度的url
     * @param urls
     * @param rid
     */
    @Override
    public void getRealUrl(Map<String, String> urls, String rid) {
        JSONObject roomInfo = get_real_rid(rid);

        if (roomInfo == null) {
            urls.put("state", "notExist");
        }
        if (!roomInfo.getBool("live_status")) {
            urls.put("state", "offline");
        }

        String fd = get_single_url(roomInfo.getLong("room_id"), bilibiliFD);
        if (bilibiliFD.equals(fd.split("&qn=")[1].split("&trid=")[0])){
            urls.put("FD",fd);
        }
        String ld = get_single_url(roomInfo.getLong("room_id"), bilibiliLD);
        if (bilibiliLD.equals(ld.split("&qn=")[1].split("&trid=")[0])){
            urls.put("LD",ld);
        }
        String sd = get_single_url(roomInfo.getLong("room_id"), bilibiliSD);
        if (bilibiliSD.equals(sd.split("&qn=")[1].split("&trid=")[0])){
            urls.put("SD",sd);
        }
        String hd = get_single_url(roomInfo.getLong("room_id"), bilibiliHD);
        if (bilibiliHD.equals(hd.split("&qn=")[1].split("&trid=")[0])){
            urls.put("HD",hd);
        }
        String od = get_single_url(roomInfo.getLong("room_id"), bilibiliOD);
        if (bilibiliOD.equals(od.split("&qn=")[1].split("&trid=")[0])){
            urls.put("OD",od);
        }
    }

    /**
     * 获取直播间单个清晰度的url
     * @param roomId
     * @param qn
     * @return
     */
    private String get_single_url(Long roomId, String qn){
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
            String formatName = ((JSONObject) formatArray.get(0)).getStr("format_name");
            if (formatName.equals(suffix)) {
                JSONArray codec = format.getJSONArray("codec");
                JSONObject jsonObject = (JSONObject) codec.get(0);
                String base_url = jsonObject.getStr("base_url");
                JSONArray url_info = jsonObject.getJSONArray("url_info");
                url_info.forEach(q -> {
                    JSONObject object = (JSONObject) q;
                    String host = object.getStr("host");
                    String extra = object.getStr("extra");
                    list.add(host + base_url + extra);
                });

            }
        });
        if (CollectionUtil.isNotEmpty(list)) {
            return list.get(0);
        } else {
            log.error("BILIBILI---获取真实地址异常---roomId：" + roomId);
            return "获取失败";
        }
    }

    /**
     * 获取单个直播间信息
     * @param roomId 房间号
     * @return
     */
    @Override
    public LiveRoomInfo getRoomInfo(String roomId){
        LiveRoomInfo liveRoomInfo = new LiveRoomInfo();
        try{
            String req_url = "https://api.live.bilibili.com/xlive/web-room/v1/index/" +
                    "getH5InfoByRoom?room_id="+roomId;
            JSONObject response = HttpRequest.create(req_url).get().getBodyJson();
            JSONObject data = response.getJSONObject("data");
            JSONObject room_info = data.getJSONObject("room_info");
            JSONObject owner_info = data.getJSONObject("anchor_info").getJSONObject("base_info");
            liveRoomInfo.setPlatForm(getPlatformName());
            liveRoomInfo.setRoomId(room_info.getStr("room_id"));
            liveRoomInfo.setCategoryId(room_info.getInt("area_id").toString());
            liveRoomInfo.setCategoryName(room_info.getStr("area_name"));
            liveRoomInfo.setRoomName(room_info.getStr("title"));
            liveRoomInfo.setOwnerName(owner_info.getStr("uname"));
            liveRoomInfo.setRoomPic(room_info.getStr("cover"));
            liveRoomInfo.setOwnerHeadPic(owner_info.getStr("face"));
            liveRoomInfo.setOnline(room_info.getInt("online"));
            liveRoomInfo.setIsLive((room_info.getInt("live_status") == 1) ? 1 : 0);
        } catch (Exception e) {
            log.error("BILIBILI---获取直播间信息异常---roomId：" + roomId + "\n" + e);
        }
        return liveRoomInfo;
    }

    /**
     * 根据分页获取推荐直播间
     * @param page 页数
     * @param size 每页大小
     * @return
     */
    @Override
    public List<LiveRoomInfo> getRecommend(int page, int size){
        List<LiveRoomInfo> list = new ArrayList<>();
        String url = "https://api.live.bilibili.com/xlive/web-interface/v1/second/getListByArea?sort=online&platform=web&page=" + page + "&page_size=" + size;
        String result = HttpUtil.doGet(url);
        JSONObject resultJsonObj = JSONUtil.parseObj(result);
        if (resultJsonObj.getInt("code") == 0) {
            JSONArray data = resultJsonObj.getJSONObject("data").getJSONArray("list");
            Iterator<Object> it = data.iterator();
            while(it.hasNext()){
                JSONObject roomInfo = (JSONObject) it.next();
                LiveRoomInfo liveRoomInfo = new LiveRoomInfo();
                liveRoomInfo.setPlatForm(getPlatformName());
                liveRoomInfo.setRoomId(roomInfo.getStr("roomid"));
                liveRoomInfo.setCategoryId(roomInfo.getStr("area_id"));
                liveRoomInfo.setCategoryName(roomInfo.getStr("area_name"));
                liveRoomInfo.setRoomName(roomInfo.getStr("title"));
                liveRoomInfo.setOwnerName(roomInfo.getStr("uname"));
                liveRoomInfo.setRoomPic(roomInfo.getStr("cover"));
                liveRoomInfo.setOwnerHeadPic(roomInfo.getStr("face"));
                liveRoomInfo.setOnline(roomInfo.getJSONObject("watched_show").getInt("num"));
                liveRoomInfo.setIsLive(1);
                list.add(liveRoomInfo);
            }
        } else {
            log.error("BILIBILI---获取推荐直播间异常");
        }
        return list;
    }

    @Override
    public List<AreaInfo> getAreaList() {
        List<AreaInfo> areaInfoList = new ArrayList<>();
        String url = "https://api.live.bilibili.com/xlive/web-interface/v1/index/getWebAreaList?source_id=2";//获取bilibili所有分类的请求地址
        String result = HttpUtil.doGet(url);
        JSONObject resultJsonObj = JSONUtil.parseObj(result);
        if (resultJsonObj.getInt("code") == 0) {
            // 获取新的分区信息
            JSONArray dataArray = resultJsonObj.getJSONObject("data").getJSONArray("data");
            dataArray.forEach(item ->{
                JSONObject areaTypeObject = (JSONObject) item;
                areaTypeObject.getJSONArray("list").forEach(areaItem->{
                    JSONObject areaItemObject = (JSONObject) areaItem;
                    AreaInfo bilibiliArea = new AreaInfo();
                    bilibiliArea.setAreaType(areaItemObject.getStr("parent_id"));
                    bilibiliArea.setTypeName(areaItemObject.getStr("parent_name"));
                    bilibiliArea.setAreaId(areaItemObject.getStr("id"));
                    bilibiliArea.setAreaName(areaItemObject.getStr("name"));
                    bilibiliArea.setAreaPic(areaItemObject.getStr("pic"));
                    bilibiliArea.setPlatform(getPlatformName());
                    areaInfoList.add(bilibiliArea);
                });
            });
        }
        return areaInfoList;
    }

    /**
     * 获取b站分区房间
     *
     * @param areaInfo
     * @param page     请求页数
     * @param size
     * @return
     */
    @Override
    public List<LiveRoomInfo> getAreaRoom(AreaInfo areaInfo, int page, int size){
        List<LiveRoomInfo> list = new ArrayList<>();
        String url = "https://api.live.bilibili.com/xlive/web-interface/v1/second/getList?" +
                "platform=web&parent_area_id="+areaInfo.getAreaType()+"&area_id="+
                areaInfo.getAreaId()+"&sort_type=&page="+page;
        String result = HttpUtil.doGet(url);
        JSONObject resultJsonObj = JSONUtil.parseObj(result);
        if (resultJsonObj.getInt("code") == 0) {
            JSONArray data = resultJsonObj.getJSONObject("data").getJSONArray("list");
            Iterator<Object> it = data.iterator();
            while(it.hasNext()){
                JSONObject roomInfo = (JSONObject) it.next();
                LiveRoomInfo liveRoomInfo = new LiveRoomInfo();
                liveRoomInfo.setPlatForm(getPlatformName());
                liveRoomInfo.setRoomId(roomInfo.getInt("roomid").toString());
                liveRoomInfo.setCategoryId(roomInfo.getInt("area_id").toString());
                liveRoomInfo.setCategoryName(roomInfo.getStr("area_name"));
                liveRoomInfo.setRoomName(roomInfo.getStr("title"));
                liveRoomInfo.setOwnerName(roomInfo.getStr("uname"));
                liveRoomInfo.setRoomPic(roomInfo.getStr("cover"));
                liveRoomInfo.setOwnerHeadPic(roomInfo.getStr("face"));
                liveRoomInfo.setOnline(roomInfo.getInt("online"));
                liveRoomInfo.setIsLive(1);
                list.add(liveRoomInfo);
            }
        }
        return list;
    }

    /**
     * 搜索
     * @param keyWords 搜索关键字
     * @return
     */
    @Override
    public List<Owner> search(String keyWords){
        int i = 0;
        List<Owner> list = new ArrayList<>();
        String cookieUrl = "https://bilibili.com";
        String cookie  = HttpRequest.create(cookieUrl).get().getCookieString();
        String url = "https://api.bilibili.com/x/web-interface/search/type?search_type=live_user&keyword=" + keyWords;
        String result = HttpRequest.create(url)
                .putHeader("Cookie",cookie)
                .get().getBody();
        JSONObject resultJsonObj = JSONUtil.parseObj(result);
        if (resultJsonObj != null && resultJsonObj.getInt("code") == 0) {
            JSONArray ownerList = resultJsonObj.getJSONObject("data").getJSONArray("result");
            Iterator<Object> it = ownerList.iterator();
            while(i < 5 && it.hasNext()){
                JSONObject responseOwner = (JSONObject) it.next();
                Owner owner = new Owner();
                owner.setNickName(getUserName(responseOwner.getStr("uname")));
                owner.setCateName(responseOwner.getStr("无"));
                owner.setHeadPic(responseOwner.getStr("uface"));
                owner.setPlatform(getPlatformName());
                owner.setRoomId(responseOwner.getStr("roomid"));
                owner.setIsLive(responseOwner.getBool("is_live") ? "1" : "0");
                owner.setFollowers(responseOwner.getInt("attentions"));
                list.add(owner);
                i++;
            }
        }
        return list;
    }

    private String getUserName(String responseName){
        String str1 = responseName.replaceAll("<em class=\"keyword\">","");
        String result = str1.replaceAll("</em>","");
        return result;
    }
}