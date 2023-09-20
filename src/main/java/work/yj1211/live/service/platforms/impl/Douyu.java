package work.yj1211.live.service.platforms.impl;

import cn.hutool.crypto.digest.DigestUtil;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.digest.DigestUtils;
import org.springframework.stereotype.Component;
import work.yj1211.live.enums.Platform;
import work.yj1211.live.model.platform.LiveRoomInfo;
import work.yj1211.live.model.platform.Owner;
import work.yj1211.live.model.platform.UrlQuality;
import work.yj1211.live.model.platformArea.AreaInfo;
import work.yj1211.live.service.platforms.BasePlatform;
import work.yj1211.live.utils.DouYuOpenApi;
import work.yj1211.live.utils.Global;
import work.yj1211.live.utils.HttpUtil;
import work.yj1211.live.utils.http.HttpContentType;
import work.yj1211.live.utils.http.HttpRequest;
import work.yj1211.live.utils.http.HttpResponse;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Slf4j
@Component
public class Douyu implements BasePlatform {
    //Douyu清晰度 1流畅；2高清；3超清；4蓝光4M；0蓝光8M或10M
    private List<String> qnList = new ArrayList<>();

    {
        qnList.add("OD");
        qnList.add("HD");
        qnList.add("SD");
        qnList.add("LD");
        qnList.add("FD");
    }

    @Override
    public String getPlatformCode() {
        return Platform.DOUYU.getCode();
    }

    @Override
    public void getRealUrl(Map<String, String> urls, String roomId){
        String url = "https://playweb.douyucdn.cn/lapi/live/hlsH5Preview/" + roomId;
        String t13 =  String.valueOf(System.currentTimeMillis());
        String auth = DigestUtils.md5Hex(roomId + t13);

        Map<String, Object> map = new HashMap<>();
        map.put("rid", Integer.valueOf(roomId));
        map.put("did", "10000000000000000000000000001501");
        String body = JSONUtil.toJsonStr(map);
        String response = HttpRequest.create(url)
                .setContentType(HttpContentType.JSON)
                .putHeader("rid", roomId)
                .putHeader("time", t13)
                .putHeader("auth", auth)
                .setBody(body)
                .post()
                .getBody();
        JSONObject res = JSONUtil.parseObj(response);
        if (res.getStr("error").equalsIgnoreCase("0")) {
            JSONObject data = res.getJSONObject("data");
            String rtmp_live = data.getStr("rtmp_live");
            url = data.getStr("rtmp_url") + "/" + rtmp_live;
//            System.out.println(url);
            urls.put("HD", url);
            String result1 = HttpRequest.create("https://m.douyu.com/" + roomId)
                    .get()
                    .getBody();
            String pattern = "(function ub98484234.*)\\s(var.*)";
            Pattern p = Pattern.compile(pattern);
            Matcher m = p.matcher(result1);
            String result = null;
            if (m.find()) {
                result = m.group();
            }

            String func_ub9 = result.replaceAll("eval.*;}", "strc;}");
            ScriptEngineManager manager = new ScriptEngineManager();
            ScriptEngine engine = manager.getEngineByName("javascript");
            String jsRes = "";
            try {
                engine.eval(func_ub9);
                jsRes = (String) engine.eval("ub98484234()");
            } catch (Exception e) {
                e.printStackTrace();
            }

            Pattern pattern2 = Pattern.compile("v=(\\d+)");
            Matcher matcher = pattern2.matcher(jsRes);
            String v = "";
            if (matcher.find()) {
                v = matcher.group(1);
            }
            String t10 = Long.toString(System.currentTimeMillis() / 1000);
            String rb = DigestUtil.md5Hex(roomId + "10000000000000000000000000001501" + t10 + v);

            String func_sign = jsRes.replaceAll("return rt;}\\);?", "return rt;}")
                    .replace("(function (", "function sign(")
                    .replace("CryptoJS.MD5(cb).toString()", "\"" + rb + "\"");

            String params = "";
            try {
                engine.eval(func_sign);
                params = (String) engine.eval("sign('" + roomId + "', '10000000000000000000000000001501', '" + t10 + "')");
            } catch (Exception e) {
                e.printStackTrace();
            }
            params += "&ver=219032101&rid=" + roomId + "&rate=-1";
//            String response2 = HttpRequest.create("https://m.douyu.com/api/room/ratestream?" + params)
//                    .setContentType(HttpContentType.JSON)
//                    .post()
//                    .getBody();
            String response2 = cn.hutool.http.HttpRequest.post("https://m.douyu.com/api/room/ratestream")
                    .body(params)
                    .execute().body();
            res = JSONUtil.parseObj(response2).getJSONObject("data");

            Pattern pattern3 = Pattern.compile("(\\d{1,8}[0-9a-zA-Z]+)_?\\d{0,4}p?(.m3u8|/playlist)");
            Matcher matcher3 = pattern3.matcher(res.getStr("url"));
            String key = null;
            if (matcher3.find()) {
                key = matcher.group(1);
            }
            urls.put("OD", res.getStr("url"));

        }
    }

    @Override
    public Map<String, List<UrlQuality>> getRealUrl(String roomId) {
        // TODO
        return null;
    }

    public String getRealRoomId(String rid) {
        String roomUrl = "https://www.douyu.com/" + rid;
        String response = HttpRequest.create(roomUrl).get().getBody();
        String realRid = response.substring(response.indexOf("$ROOM.room_id =") + "$ROOM.room_id =".length());
        return realRid.substring(0, realRid.indexOf(";")).trim();
    }

    /**
     * 获取斗鱼房间信息
     * @param roomId
     * @return
     */
    @Override
    public LiveRoomInfo getRoomInfo(String roomId) {
        String url = DouYuOpenApi.ROOM_INFO + roomId;
        HttpResponse response = HttpRequest.create(url)
                .setContentType(HttpContentType.FORM).get();
        if (404 == response.getCode()){
            return null;
        }

        if (response.getBodyJson().getInt("error") == 0) {
            JSONObject room_info = response.getBodyJson().getJSONObject("data");
            LiveRoomInfo liveRoomInfo = new LiveRoomInfo();
            liveRoomInfo.setPlatForm(getPlatformCode());
            liveRoomInfo.setRoomId(room_info.getStr("room_id"));
            liveRoomInfo.setCategoryId(room_info.getStr("cate_id"));//分类id不对
            liveRoomInfo.setCategoryName(room_info.getStr("cate_name"));
            liveRoomInfo.setRoomName(room_info.getStr("room_name"));
            liveRoomInfo.setOwnerName(room_info.getStr("owner_name"));
            liveRoomInfo.setRoomPic(room_info.getStr("room_thumb"));
            liveRoomInfo.setOwnerHeadPic(room_info.getStr("avatar"));
            liveRoomInfo.setOnline(room_info.getInt("online"));
            liveRoomInfo.setIsLive((room_info.getInt("room_status") == 1) ? 1 : 0);
            return liveRoomInfo;
        }
        return null;
    }

    /**
     * 根据分页获取推荐直播间（这个斗鱼api一次请求8个房间）
     * @param page 页数
     * @param size 每页大小
     * @return
     */
    @Override
    public List<LiveRoomInfo> getRecommend(int page, int size){
        List<LiveRoomInfo> list = new ArrayList<>();
        int start = size*(page-1)/8 + ((size*(page-1)%8 == 0) ? 0 : 1);
        start = (start == 0) ? 1 : start;
        int startIndex = size*(page-1)%8;
        int end = size*(page)/8 + ((size*(page)%8 == 0) ? 0 : 1);
        int endIndex = size*(page)%8;
        List<LiveRoomInfo> listTemp;
        for(int i = start; i <= end; i++){
            String url = "https://m.douyu.com/api/room/list?page="+ i + "&type=";
            listTemp = requestUrl(url, null);
            list.addAll(listTemp);
        }
        list = list.subList(startIndex, list.size()-endIndex);
        return list;
    }

    @Override
    public List<AreaInfo> getAreaList() {
        List<AreaInfo> areaInfoList = new ArrayList<>();
        String url = "https://m.douyu.com/api/cate/list";
        String result = HttpRequest.create(url)
                .setContentType(HttpContentType.FORM)
                .putHeader("User-Agent", "Mozilla/5.0 (Linux; Android 5.0; SM-G900P Build/LRX21T) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/75.0.3770.100 Mobile Safari/537.36")
                .get().getBody();
        JSONObject resultJsonObj = JSONUtil.parseObj(result);
        if (resultJsonObj.getInt("code") == 0) {
            // 获取新的分区信息
            // cate1Info
            JSONArray cate1Array = resultJsonObj.getJSONObject("data").getJSONArray("cate1Info");
            Map<String, String> cate1Map = new HashMap<>();
            cate1Array.forEach(cate1Item->{
                JSONObject cate1Obj = (JSONObject) cate1Item;
                cate1Map.put(cate1Obj.getInt("cate1Id").toString(), cate1Obj.getStr("cate1Name"));
            });

            // cate2Info
            JSONArray cate2Array = resultJsonObj.getJSONObject("data").getJSONArray("cate2Info");
            cate2Array.forEach(cate2Item->{
                JSONObject cate2Obj = (JSONObject) cate2Item;
                AreaInfo douyuArea = new AreaInfo();
                String cate1Id = cate2Obj.getInt("cate1Id").toString();
                douyuArea.setAreaType(cate1Id);
                douyuArea.setTypeName(cate1Map.get(cate1Id));
                douyuArea.setAreaId(cate2Obj.getInt("cate2Id").toString());
                douyuArea.setAreaName(cate2Obj.getStr("cate2Name"));
                douyuArea.setAreaPic(cate2Obj.getStr("pic"));
                douyuArea.setShortName(cate2Obj.getStr("shortName"));
                douyuArea.setPlatform(getPlatformCode());
                douyuArea.setId(cate2Obj.getInt("count")); // 该分区下的直播间数量
                areaInfoList.add(douyuArea);
                Global.DouyuCateMap.put(douyuArea.getAreaId(), douyuArea.getAreaName());
            });
        }
        return areaInfoList
                .stream()
                .sorted(Comparator.comparing(AreaInfo::getId).reversed())
                .collect(Collectors.toList());
    }

    /**
     * 获取url请求的所有房间信息
     * @param url
     * @return
     */
    private List<LiveRoomInfo> requestUrl(String url, String categoryName){
        List<LiveRoomInfo> list = new ArrayList<>();
        String result = HttpUtil.doGet(url);
        JSONObject resultJsonObj = JSONUtil.parseObj(result);
        if (resultJsonObj.getInt("code") == 0) {
            JSONArray roomList = resultJsonObj.getJSONObject("data").getJSONArray("list");
            Iterator<Object> it = roomList.iterator();
            while(it.hasNext()){
                JSONObject roomInfo = (JSONObject) it.next();
                LiveRoomInfo liveRoomInfo = new LiveRoomInfo();
                liveRoomInfo.setPlatForm("douyu");
                liveRoomInfo.setRoomId(roomInfo.getInt("rid").toString());
                liveRoomInfo.setCategoryId(roomInfo.getStr("cate2Id"));
                liveRoomInfo.setCategoryName(Global.DouyuCateMap.get(roomInfo.getStr("cate2Id")));
                liveRoomInfo.setRoomName(roomInfo.getStr("roomName"));
                liveRoomInfo.setOwnerName(roomInfo.getStr("nickname"));
                liveRoomInfo.setRoomPic(roomInfo.getStr("roomSrc"));
                liveRoomInfo.setOwnerHeadPic(roomInfo.getStr("avatar"));
                liveRoomInfo.setOnline(DouyuNumStringToInt(roomInfo.getStr("hn")));
                liveRoomInfo.setIsLive(roomInfo.getInt("isLive"));
                list.add(liveRoomInfo);
            }
        }
        return list;
    }

    /**
     * 把形如 "12.3万" 的字符串转为 123XXX 的int
     * @param number
     * @return
     */
    public Integer DouyuNumStringToInt(String number){
        int num = 0;
        if(number.contains("万")){
            int index = number.indexOf(".");
            String temp = number.substring(0, index)+number.substring(index+1, number.length()-1);
            temp = temp + (int)((Math.random()*9+1)*100);
            num = Integer.valueOf(temp);
        }else if (number.contains("亿")){
            int index = number.indexOf(".");
            String temp = number.substring(0, index)+number.substring(index+1, number.length()-1);
            temp = temp + (int)((Math.random()*9+1)*1000000);
            num = Integer.valueOf(temp);
        }else {
            return Integer.valueOf(number);
        }
        return num;
    }

    /**
     * 获取斗鱼分区房间
     * @param area
     * @param page
     * @param size
     * @return
     */
    @Override
    public List<LiveRoomInfo> getAreaRoom(String area, int page, int size){
        List<LiveRoomInfo> list = new ArrayList<>();
        AreaInfo areaInfo = Global.getAreaInfo(getPlatformCode(), area);
        int start = size*(page-1)/8 + 1;
        start = (start == 0) ? 1 : start;
        int startIndex = size*(page-1)%8;
        int end = size*(page)/8 + ((size*(page)%8 == 0) ? 0 : 1);
        int endIndex = size*(page)%8;
        List<LiveRoomInfo> listTemp;
        for(int i = start; i <= end; i++){
            String url = "https://m.douyu.com/api/room/list?page="+ i +"&type="+areaInfo.getShortName();
            listTemp = requestUrl(url, areaInfo.getAreaName());
            list.addAll(listTemp);
        }
        if (list.size()>size){
            list = list.subList(startIndex, list.size()-(8-endIndex));
        }
        return list;
    }

    /**
     * 搜索
     *
     * @param keyWords 搜索关键字
     * @return
     */
    @Override
    public List<Owner> search(String keyWords) {
        List<Owner> list = new ArrayList<>();
        HashMap<String, Object> paramMap = new HashMap<>();
        paramMap.put("sk", keyWords);
        paramMap.put("offset", 0);
        paramMap.put("limit", 20);

        String result= cn.hutool.http.HttpUtil.post("https://m.douyu.com/api/search/anchor", paramMap);
        JSONObject resultJsonObj = JSONUtil.parseObj(result);
        if (resultJsonObj.getInt("error") == 0) {
            JSONArray ownerList = resultJsonObj.getJSONObject("data").getJSONArray("list");
            Iterator<Object> it = ownerList.iterator();
            int i = 0;
            while(i < 5 && it.hasNext()) {
                JSONObject responseOwner = (JSONObject) it.next();
                Owner owner = new Owner();
                owner.setNickName(responseOwner.getStr("nickname"));
                owner.setCateName(responseOwner.getStr("cateName"));
                owner.setHeadPic(responseOwner.getStr("avatar"));
                owner.setPlatform(getPlatformCode());
                owner.setRoomId(responseOwner.getStr("roomId"));
                owner.setIsLive((responseOwner.getInt("isLive") == 1) ? "1" : "0");
                owner.setFollowers(DouyuNumStringToInt(responseOwner.getStr("hn")));
                list.add(owner);
                i++;
            }
        }
        return list;
    }
}
