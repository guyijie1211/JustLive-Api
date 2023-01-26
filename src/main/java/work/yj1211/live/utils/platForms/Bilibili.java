package work.yj1211.live.utils.platForms;

import cn.hutool.core.collection.CollectionUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import work.yj1211.live.mapper.AllRoomsMapper;
import work.yj1211.live.utils.Global;
import work.yj1211.live.utils.HttpUtil;
import work.yj1211.live.utils.http.HttpRequest;
import work.yj1211.live.vo.LiveRoomInfo;
import work.yj1211.live.vo.Owner;
import work.yj1211.live.vo.platformArea.AreaInfo;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.*;
import java.util.concurrent.CountDownLatch;

@Slf4j
@Component
public class Bilibili extends BasePlatform{
    @Autowired
    private AllRoomsMapper allRoomsMapper;

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
        int code = response.getInteger("code");
        if(code == 0){
            JSONObject data = response.getJSONObject("data");
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("live_status", data.getBoolean("live_status"));
            jsonObject.put("room_id", data.getLongValue("room_id"));
            return jsonObject;
        }else {
            log.error("BILIBILI---获取直播间真实id异常---roomId：" + rid);
            return null;
        }
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
        if (!roomInfo.getBoolean("live_status")) {
            urls.put("state", "offline");
        }

        String fd = get_single_url(roomInfo.getLongValue("room_id"), bilibiliFD);
        if (bilibiliFD.equals(fd.split("&qn=")[1].split("&trid=")[0])){
            urls.put("FD",fd);
        }
        String ld = get_single_url(roomInfo.getLongValue("room_id"), bilibiliLD);
        if (bilibiliLD.equals(ld.split("&qn=")[1].split("&trid=")[0])){
            urls.put("LD",ld);
        }
        String sd = get_single_url(roomInfo.getLongValue("room_id"), bilibiliSD);
        if (bilibiliSD.equals(sd.split("&qn=")[1].split("&trid=")[0])){
            urls.put("SD",sd);
        }
        String hd = get_single_url(roomInfo.getLongValue("room_id"), bilibiliHD);
        if (bilibiliHD.equals(hd.split("&qn=")[1].split("&trid=")[0])){
            urls.put("HD",hd);
        }
        String od = get_single_url(roomInfo.getLongValue("room_id"), bilibiliOD);
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
            liveRoomInfo.setPlatForm("bilibili");
            liveRoomInfo.setRoomId(room_info.getString("room_id"));
            liveRoomInfo.setCategoryId(room_info.getInteger("area_id").toString());
            liveRoomInfo.setCategoryName(room_info.getString("area_name"));
            liveRoomInfo.setRoomName(room_info.getString("title"));
            liveRoomInfo.setOwnerName(owner_info.getString("uname"));
            liveRoomInfo.setRoomPic(room_info.getString("cover"));
            liveRoomInfo.setOwnerHeadPic(owner_info.getString("face"));
            liveRoomInfo.setOnline(room_info.getInteger("online"));
            liveRoomInfo.setIsLive((room_info.getInteger("live_status") == 1) ? 1 : 0);
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
        JSONObject resultJsonObj = JSON.parseObject(result);
        if (resultJsonObj.getInteger("code") == 0) {
            JSONArray data = resultJsonObj.getJSONObject("data").getJSONArray("list");
            Iterator<Object> it = data.iterator();
            while(it.hasNext()){
                JSONObject roomInfo = (JSONObject) it.next();
                LiveRoomInfo liveRoomInfo = new LiveRoomInfo();
                liveRoomInfo.setPlatForm("bilibili");
                liveRoomInfo.setRoomId(roomInfo.getString("roomid"));
                liveRoomInfo.setCategoryId(roomInfo.getString("area_id"));
                liveRoomInfo.setCategoryName(roomInfo.getString("area_name"));
                liveRoomInfo.setRoomName(roomInfo.getString("title"));
                liveRoomInfo.setOwnerName(roomInfo.getString("uname"));
                liveRoomInfo.setRoomPic(roomInfo.getString("cover"));
                liveRoomInfo.setOwnerHeadPic(roomInfo.getString("face"));
                liveRoomInfo.setOnline(roomInfo.getJSONObject("watched_show").getInteger("num"));
                liveRoomInfo.setIsLive(1);
                list.add(liveRoomInfo);
            }
        } else {
            log.error("BILIBILI---获取推荐直播间异常");
        }
        return list;
    }

    /**
     * 刷新分类缓存
     * @return
     */
    @Override
    public void refreshArea(){
        try {
            String url = "https://api.live.bilibili.com/xlive/web-interface/v1/index/getWebAreaList?source_id=2";//获取bilibili所有分类的请求地址
            List<List<AreaInfo>> areaMapTemp = new ArrayList<>();
            String result = HttpUtil.doGet(url);
            JSONObject resultJsonObj = JSON.parseObject(result);
            if (resultJsonObj.getInteger("code") == 0) {
                JSONArray data = resultJsonObj.getJSONObject("data").getJSONArray("data");
                Iterator<Object> it = data.iterator();
                while (it.hasNext()) {
                    JSONObject areaType = (JSONObject) it.next();
                    String typeName = areaType.getString("name");
                    List<AreaInfo> areaListTemp = new ArrayList<>();
                    JSONArray jsonArray = areaType.getJSONArray("list");
                    Iterator<Object> jsonArrayIt = jsonArray.iterator();
                    while (jsonArrayIt.hasNext()) {
                        JSONObject areaInfo = (JSONObject) jsonArrayIt.next();
                        AreaInfo bilibiliArea = new AreaInfo();
                        bilibiliArea.setAreaType(areaInfo.getString("parent_id"));
                        bilibiliArea.setTypeName(areaInfo.getString("parent_name"));
                        bilibiliArea.setAreaId(areaInfo.getString("id"));
                        bilibiliArea.setAreaName(areaInfo.getString("name"));
                        bilibiliArea.setAreaPic(areaInfo.getString("pic"));
                        bilibiliArea.setPlatform("bilibili");
                        Global.BilibiliCateMap.put(bilibiliArea.getAreaId(), bilibiliArea.getAreaName());
                        String areaTypeKey = bilibiliArea.getTypeName().substring(0, 2);
                        if (!Global.AllAreaMap.containsKey(areaTypeKey)) {
                            List<String> list = new ArrayList<>();
                            list.add(bilibiliArea.getAreaName());
                            Global.AreaTypeSortList.add(areaTypeKey);
                            Global.AreaInfoSortMap.put(areaTypeKey, list);
                        } else {
                            if (!Global.AllAreaMap.get(areaTypeKey).containsKey(bilibiliArea.getAreaName())) {
                                Global.AreaInfoSortMap.get(areaTypeKey).add(bilibiliArea.getAreaName());
                            }
                        }
                        Global.AllAreaMap.computeIfAbsent(areaTypeKey, k -> new HashMap<>())
                                .computeIfAbsent(bilibiliArea.getAreaName(), k -> new HashMap<>()).put("bilibili", bilibiliArea);
                        areaListTemp.add(bilibiliArea);
                    }
                    areaMapTemp.add(areaListTemp);
                }
            }
            Global.platformAreaMap.put("bilibili", areaMapTemp);
        } catch (Exception e) {
            log.error("BILIBILI---刷新分类缓存异常");
        }
    }

    /**
     * 获取b站分区房间
     * @param area 分类id
     * @param page 请求页数
     * @param size
     * @return
     */
    @Override
    public List<LiveRoomInfo> getAreaRoom(String area, int page, int size){
        List<LiveRoomInfo> list = new ArrayList<>();
        try {
            AreaInfo areaInfo = Global.getAreaInfo("bilibili", area);
            String url = "https://api.live.bilibili.com/xlive/web-interface/v1/second/getList?" +
                    "platform=web&parent_area_id="+areaInfo.getAreaType()+"&area_id="+
                    areaInfo.getAreaId()+"&sort_type=&page="+page;
            String result = HttpUtil.doGet(url);
            JSONObject resultJsonObj = JSON.parseObject(result);
            if (resultJsonObj.getInteger("code") == 0) {
                JSONArray data = resultJsonObj.getJSONObject("data").getJSONArray("list");
                Iterator<Object> it = data.iterator();
                while(it.hasNext()){
                    JSONObject roomInfo = (JSONObject) it.next();
                    LiveRoomInfo liveRoomInfo = new LiveRoomInfo();
                    liveRoomInfo.setPlatForm("bilibili");
                    liveRoomInfo.setRoomId(roomInfo.getInteger("roomid").toString());
                    liveRoomInfo.setCategoryId(roomInfo.getInteger("area_id").toString());
                    liveRoomInfo.setCategoryName(roomInfo.getString("area_name"));
                    liveRoomInfo.setRoomName(roomInfo.getString("title"));
                    liveRoomInfo.setOwnerName(roomInfo.getString("uname"));
                    liveRoomInfo.setRoomPic(roomInfo.getString("cover"));
                    liveRoomInfo.setOwnerHeadPic(roomInfo.getString("face"));
                    liveRoomInfo.setOnline(roomInfo.getInteger("online"));
                    liveRoomInfo.setIsLive(1);
                    list.add(liveRoomInfo);
                }
            }
        } catch (Exception e) {
            log.error("BILIBILI---获取分区房间异常---area：" + area);
        }

        return list;
    }

    /**
     * 搜索
     * @param keyWords  搜索关键字
     * @param isLive 是否搜索直播中的信息
     * @return
     */
    @Override
    public List<Owner> search(String keyWords, String isLive){
        int i = 0;
        List<Owner> list = new ArrayList<>();
        String cookieUrl = "https://bilibili.com";
        String cookie  = HttpRequest.create(cookieUrl).get().getCookieString();
        String url = "https://api.bilibili.com/x/web-interface/search/type?search_type=live_user&keyword=" + keyWords;
        String result = HttpRequest.create(url)
                .putHeader("Cookie",cookie)
                .get().getBody();
        JSONObject resultJsonObj = JSON.parseObject(result);
        if (resultJsonObj != null && resultJsonObj.getInteger("code") == 0) {
            JSONArray ownerList = resultJsonObj.getJSONObject("data").getJSONArray("result");
            Iterator<Object> it = ownerList.iterator();
            while(i < 5 && it.hasNext()){
                JSONObject responseOwner = (JSONObject) it.next();
                Owner owner = new Owner();
                owner.setNickName(getUserName(responseOwner.getString("uname")));
                owner.setCateName(responseOwner.getString("无"));
                owner.setHeadPic(responseOwner.getString("uface"));
                owner.setPlatform("bilibili");
                owner.setRoomId(responseOwner.getString("roomid"));
                owner.setIsLive(responseOwner.getBoolean("is_live") ? "1" : "0");
                owner.setFollowers(responseOwner.getInteger("attentions"));
                list.add(owner);
                i++;
            }
        }
        if ("1".equals(isLive)){
            List<Owner> resultList = new ArrayList<>();
            for (Owner owner : list){
                if ("1".equals(owner.getIsLive())){
                    resultList.add(owner);
                }
            }
            return resultList;
        }
        return list;
    }

    private String getUserName(String responseName){
        String str1 = responseName.replaceAll("<em class=\"keyword\">","");
        String result = str1.replaceAll("</em>","");
        return result;
    }

    /**
     * 拉取所有直播间
     */
    public void updateAllRooms() {
        for (int i = 0; i <= 7; i++) {
//            updateAllRoomByPage(i*100 + 1);
        }

    }

    @Async("asyncServiceExecutor")
    public void updateAllRoomByPage(int page, CountDownLatch countDownLatch) {
        int endPage = page + 99;
        while (true) {
            log.info("Bilibili全量拉取直播间===page:" + page);
            List<LiveRoomInfo> list = new ArrayList<>();
            String url = "https://api.live.bilibili.com/xlive/web-interface/v1/second/getUserRecommend?page=" + page + "&page_size=30&platform=web";
            String result = HttpUtil.doGet(url);
            JSONObject resultJsonObj = JSON.parseObject(result);
            if (resultJsonObj.getInteger("code") == 0) {
                JSONArray data = resultJsonObj.getJSONObject("data").getJSONArray("list");
                Iterator<Object> it = data.iterator();
                while(it.hasNext()){
                    JSONObject roomInfo = (JSONObject) it.next();
                    LiveRoomInfo liveRoomInfo = new LiveRoomInfo();
                    liveRoomInfo.setPlatForm("bilibili");
                    liveRoomInfo.setRoomId(roomInfo.getString("roomid"));
                    liveRoomInfo.setCategoryName(roomInfo.getString("area_name"));
                    liveRoomInfo.setRoomName(roomInfo.getString("title"));
                    liveRoomInfo.setOwnerName(roomInfo.getString("uname"));
                    liveRoomInfo.setOnline(roomInfo.getInteger("online"));
                    liveRoomInfo.setIsLive(1);
                    list.add(liveRoomInfo);
                }
                allRoomsMapper.updateRooms(list);
                if (page >= endPage || resultJsonObj.getJSONObject("data").getInteger("has_more") != 1 || list.get(0).getOnline() < 10) {
                    break;
                } else {
                    page++;
                }
            }
        }
        countDownLatch.countDown();
    }
}