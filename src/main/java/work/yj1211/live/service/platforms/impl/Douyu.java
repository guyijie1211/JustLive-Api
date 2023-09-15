package work.yj1211.live.service.platforms.impl;

import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import work.yj1211.live.enums.Platform;

import work.yj1211.live.service.platforms.BasePlatform;
import work.yj1211.live.utils.DouYuOpenApi;
import work.yj1211.live.utils.http.HttpContentType;
import work.yj1211.live.utils.http.HttpRequest;
import work.yj1211.live.utils.http.HttpResponse;
import work.yj1211.live.model.LiveRoomInfo;
import work.yj1211.live.model.Owner;
import work.yj1211.live.model.platformArea.AreaInfo;

import javax.script.Invocable;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Slf4j
@Component
public class Douyu implements BasePlatform {


    //Douyu清晰度 1流畅；2高清；3超清；4蓝光4M；0蓝光8M或10M
    private List<String> qnList = new ArrayList<>();

    //存储获取的房间唯一标识(每次都去获取唯一标识太慢了，并且同一房间一段时间内标识是不变的，所以用缓存来保存)
    private Map<String, String> roomUrlMap = new HashMap<>();
    private Map<String, List<Integer>> roomRateMap = new HashMap<>();

    //    private final Pattern PATTERN = Pattern.compile("(function ub9.*)[\\s\\S](var.*)");
    private final Pattern PATTERN = Pattern.compile("(vdwdae325w_64we[\\s\\S]*function ub98484234[\\s\\S]*?)function");

    {
        qnList.add("OD");
        qnList.add("HD");
        qnList.add("SD");
        qnList.add("LD");
        qnList.add("FD");
    }

    @Override
    public String getPlatformName() {
        return Platform.DOUYU.getName();
    }

    @Override
    public void getRealUrl(Map<String, String> urls, String rid){
        List<Integer> rateList = roomRateMap.get(rid);
        if (null == rateList){
            get_simple_url(rid);
            rateList = roomRateMap.get(rid);
        }
        try{
            for (int i = 0; i < rateList.size(); i++){
                String qnString = qnList.get(i);
                String qn;
                if ("OD".equals(qnString)){
                    qn = "";
                }else {
                    qn = "_"+rateList.get(i).toString();
                }
                urls.put(qnString, get_single_url(rid, qn));
            }
        }catch (Exception e){
            e.printStackTrace();
        }finally {
            roomUrlMap.clear();
            roomRateMap.clear();
        }

    }

    /**
     * 根据房间号和清晰度获取直播地址
     * @param roomId
     * @param qn 清晰度
     * @return
     */
    private String get_single_url(String roomId, String qn){
        //获取房间唯一标识，第一次获取时去请求
        String roomUrl = roomUrlMap.computeIfAbsent(roomId, k -> get_simple_url(roomId));
        String result = "http://hw-tct.douyucdn.cn/live/" + roomUrl + qn + ".flv?uuid=";
        return result;
    }

    /**
     * 获取直播间标识地址
     * @param rid
     * @return
     */
    public String get_simple_url(String rid) {
        JSONObject tt = getTT();
        String tt1 = tt.getStr("tt1");
        String realUrl = null;
        try {
            JSONObject result = getHomeJs(rid);
            assert result != null;
            String real_rid = result.getStr("real_rid");
            String homejs = result.getStr("homejs");
            realUrl = getSignUrl("0", real_rid, tt1, homejs);
        } catch (NoSuchAlgorithmException | ScriptException | NoSuchMethodException e) {
            e.printStackTrace();
        }
        return realUrl;
    }

    /**
     * 获取唯一标识
     * @param rid
     * @param tt
     * @param ub9
     * @return
     * @throws ScriptException
     * @throws NoSuchAlgorithmException
     */
    private String getSign(String rid, String tt, String ub9) throws ScriptException, NoSuchAlgorithmException, NoSuchMethodException {
        ScriptEngine docjs = new ScriptEngineManager().getEngineByName("javascript");
        docjs.eval(ub9);
        Invocable invocable = (Invocable) docjs;
        String functionResult = (String)invocable.invokeFunction("ub98484234");
        Matcher matcher = Pattern.compile("v=(\\d+)").matcher(functionResult);
        if (!matcher.find()) {
            return null;
        }
        String v = matcher.group(1);
        String md5rb = md5String(rid + "10000000000000000000000000001501" + tt + v);
        String ub9_new = functionResult.replaceAll("return rt;}\\);?", "return rt;}");
        ub9_new = ub9_new.replace("(function (", "function sign(");
        ub9_new = ub9_new.replace("CryptoJS.MD5(cb).toString()", "\"" + md5rb + "\"");

        ScriptEngine docjs_new = new ScriptEngineManager().getEngineByName("javascript");
        String params = null;
        try {
            docjs_new.eval(ub9_new); //编译
            if (docjs_new instanceof Invocable) {
                params = (String) ((Invocable) docjs_new).invokeFunction("sign", rid, "10000000000000000000000000001501", tt); // 执行方法
            }
        } catch (Exception e) {
            log.error("表达式runtime错误:", e);
        }
        return params;
    }

    /**
     *
     * @param qn
     * @param rid
     * @param tt
     * @param ub9
     * @return
     * @throws ScriptException
     * @throws NoSuchAlgorithmException
     */
    private String getSignUrl(String qn, String rid, String tt, String ub9) throws ScriptException, NoSuchAlgorithmException, NoSuchMethodException {
        String params = getSign(rid, tt, ub9);
        params = params + "&cdn=ws-h5&rate=" + qn;
        Map<String, Object> paramsMap = handleParams(params);

        String requestUrl = "https://www.douyu.com/lapi/live/getH5Play/"+rid;
        JSONObject response = HttpRequest.create(requestUrl)
                .appendParameters(paramsMap)
                .post()
                .getBodyJson();

        if (response.getInt("code") != 0) {
            return null;
        }
        JSONObject data = response.getJSONObject("data");
        if (data == null){
            return null;
        }
        String url = data.getStr("rtmp_live");
        url = handleUrl(url);
        roomUrlMap.put(rid, url);
        List<Integer> rateList = handleRate(data.getJSONArray("multirates"));
        roomRateMap.put(rid, rateList);

        return url;
    }

    public String getRealRoomId(String rid) {
        String roomUrl = "https://www.douyu.com/" + rid;
        String response = HttpRequest.create(roomUrl).get().getBody();
        String realRid = response.substring(response.indexOf("$ROOM.room_id =") + "$ROOM.room_id =".length());
        return realRid.substring(0, realRid.indexOf(";")).trim();
    }

    /**
     *
     * @param rid
     * @return
     */
    private JSONObject getHomeJs(String rid) {
        String roomUrl = "https://www.douyu.com/" + rid;
        String response = HttpRequest.create(roomUrl).get().getBody();
        String realRid = response.substring(response.indexOf("$ROOM.room_id =") + "$ROOM.room_id =".length());
        realRid = realRid.substring(0, realRid.indexOf(";")).trim();
        if (!rid.equals(realRid)) {
            roomUrl = "https://www.douyu.com/" + realRid;
            response = HttpRequest.create(roomUrl).get().getBody();
        }

        Matcher matcher = PATTERN.matcher(response);
        if (!matcher.find()) {
            return null;
        }
        String result = matcher.group(1);
        String homejs = result.replaceAll("eval.*?;", "strc;");
        JSONObject jsonObject = new JSONObject();
        jsonObject.set("homejs", homejs);
        jsonObject.set("real_rid", realRid);
        return jsonObject;
    }

    /**
     * @return
     */
    private JSONObject getTT() {
        long nowTime = System.currentTimeMillis();
        String tt1 = String.valueOf(nowTime / 1000);
        String tt2 = String.valueOf(nowTime);
        String today = getTimeStr(nowTime, "yyyyMMdd");

        JSONObject jsonObject = new JSONObject();
        jsonObject.set("tt1", tt1);
        jsonObject.set("tt2", tt2);
        jsonObject.set("today", today);
        return jsonObject;
    }

    /**
     * 获取格式化日期
     * @param time
     * @param format
     * @return
     */
    public String getTimeStr(long time, String format) {
        Date date = new Date(time);
        SimpleDateFormat sdf = new SimpleDateFormat(format);
        return sdf.format(date);
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
            liveRoomInfo.setPlatForm(getPlatformName());
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
     * 处理url获取唯一标识
     * @param url
     * @return
     */
    private String handleUrl(String url){
        url = url.substring(0, url.indexOf("."));
        return url.split("_")[0];
    }

    /**
     * 处理直播间清晰度
     * @param jsonArray
     * @return
     */
    private List<Integer> handleRate(JSONArray jsonArray){
        List<Integer> list = new ArrayList<>();
        for (int i = 0; i < jsonArray.size(); i++){
            list.add(jsonArray.getJSONObject(i).getInt("bit"));
        }
        Collections.sort(list, new Comparator<Integer>() {
            @Override
            public int compare(Integer o1, Integer o2) {
                return o2-o1;
            }
        });
        return list;
    }

    /**
     * 处理params 把String转换成Map
     * @param params
     * @return
     */
    private Map<String, Object> handleParams(String params){
        Map<String, Object> paramsMap = new HashMap<>();
        String[] arr = params.split("&");
        String key;
        String value;
        String[] arr1;
        for (String param : arr){
            arr1 = param.split("=");
            key = arr1[0].trim();
            value = arr1[1].trim();
            paramsMap.put(key, value);
        }
        return paramsMap;
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
                douyuArea.setPlatform(getPlatformName());
                douyuArea.setId(cate2Obj.getInt("count")); // 该分区下的直播间数量
                areaInfoList.add(douyuArea);
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
//        String result = HttpUtil.doGet(url);
//        JSONObject resultJsonObj = JSONUtil.parseObj(result);
//        if (resultJsonObj.getInt("code") == 0) {
//            JSONArray roomList = resultJsonObj.getJSONObject("data").getJSONArray("list");
//            Iterator<Object> it = roomList.iterator();
//            while(it.hasNext()){
//                JSONObject roomInfo = (JSONObject) it.next();
//                LiveRoomInfo liveRoomInfo = new LiveRoomInfo();
//                liveRoomInfo.setPlatForm("douyu");
//                liveRoomInfo.setRoomId(roomInfo.getInt("rid").toString());
//                liveRoomInfo.setCategoryId(roomInfo.getStr("cate2Id"));
//                liveRoomInfo.setCategoryName(Global.DouyuCateMap.get(roomInfo.getStr("cate2Id")));
//                liveRoomInfo.setRoomName(roomInfo.getStr("roomName"));
//                liveRoomInfo.setOwnerName(roomInfo.getStr("nickname"));
//                liveRoomInfo.setRoomPic(roomInfo.getStr("roomSrc"));
//                liveRoomInfo.setOwnerHeadPic(roomInfo.getStr("avatar"));
//                liveRoomInfo.setOnline(DouyuNumStringToInt(roomInfo.getStr("hn")));
//                liveRoomInfo.setIsLive(roomInfo.getInt("isLive"));
//                list.add(liveRoomInfo);
//            }
//        }
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
//        AreaInfo areaInfo = Global.getAreaInfo("douyu", area);
//        int start = size*(page-1)/8 + 1;
//        start = (start == 0) ? 1 : start;
//        int startIndex = size*(page-1)%8;
//        int end = size*(page)/8 + ((size*(page)%8 == 0) ? 0 : 1);
//        int endIndex = size*(page)%8;
//        List<LiveRoomInfo> listTemp;
//        for(int i = start; i <= end; i++){
//            String url = "https://m.douyu.com/api/room/list?page="+ i +"&type="+areaInfo.getShortName();
//            listTemp = requestUrl(url, areaInfo.getAreaName());
//            list.addAll(listTemp);
//        }
//        if (list.size()>size){
//            list = list.subList(startIndex, list.size()-(8-endIndex));
//        }
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
                owner.setPlatform(getPlatformName());
                owner.setRoomId(responseOwner.getStr("roomId"));
                owner.setIsLive((responseOwner.getInt("isLive") == 1) ? "1" : "0");
                owner.setFollowers(DouyuNumStringToInt(responseOwner.getStr("hn")));
                list.add(owner);
                i++;
            }
        }
        return list;
    }

    private String md5String(String s) throws NoSuchAlgorithmException {
        byte[] bs = MessageDigest.getInstance("MD5").digest(s.getBytes());
        StringBuilder sb = new StringBuilder(40);
        for (byte x : bs) {
            if ((x & 0xff) >> 4 == 0) {
                sb.append("0").append(Integer.toHexString(x & 0xff));
            } else {
                sb.append(Integer.toHexString(x & 0xff));
            }
        }
        return sb.toString();
    }
}
