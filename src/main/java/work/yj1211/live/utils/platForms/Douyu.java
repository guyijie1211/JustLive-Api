package work.yj1211.live.utils.platForms;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import work.yj1211.live.utils.Global;
import work.yj1211.live.utils.HttpUtil;
import work.yj1211.live.utils.MD5Util;
import work.yj1211.live.utils.http.HttpContentType;
import work.yj1211.live.utils.http.HttpRequest;
import work.yj1211.live.utils.http.HttpResponse;
import work.yj1211.live.vo.LiveRoomInfo;
import work.yj1211.live.vo.Owner;
import work.yj1211.live.vo.platformArea.AreaInfo;

import javax.script.Invocable;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import java.security.NoSuchAlgorithmException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Douyu {
    //Douyu清晰度 1流畅；2高清；3超清；4蓝光4M；0蓝光8M或10M
    private static List<String> qnList = new ArrayList<>();

    //存储获取的房间唯一标识(每次都去获取唯一标识太慢了，并且同一房间一段时间内标识是不变的，所以用缓存来保存)
    private static Map<String, String> roomUrlMap = new ConcurrentHashMap<>();
    private static Map<String, List<Integer>> roomRateMap = new ConcurrentHashMap<>();

    //    private static final Pattern PATTERN = Pattern.compile("(function ub9.*)[\\s\\S](var.*)");
    private static final Pattern PATTERN = Pattern.compile("(vdwdae325w_64we[\\s\\S]*function ub98484234[\\s\\S]*?)function");

    static {
        qnList.add("OD");
        qnList.add("HD");
        qnList.add("SD");
        qnList.add("LD");
        qnList.add("FD");
    }

    public static void get_real_url(Map<String, String> urls, String rid){
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
            return;
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
    private static String get_single_url(String roomId, String qn){
        //获取房间唯一标识，第一次获取时去请求
        String roomUrl = roomUrlMap.computeIfAbsent(roomId, k -> get_simple_url(roomId));
        String result = "http://dyscdnali1.douyucdn.cn/live/" + roomUrl + qn + ".flv?uuid=";
        return result;
    }

    /**
     * 获取直播间标识地址
     * @param rid
     * @return
     */
    public static String get_simple_url(String rid) {
        JSONObject tt = getTT();
        String tt1 = tt.getString("tt1");
        String realUrl = null;
        try {
            JSONObject result = getHomeJs(rid);
            assert result != null;
            String real_rid = result.getString("real_rid");
            String homejs = result.getString("homejs");
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
    private static String getSign(String rid, String tt, String ub9) throws ScriptException, NoSuchAlgorithmException, NoSuchMethodException {
        ScriptEngine docjs = new ScriptEngineManager().getEngineByName("javascript");
        docjs.eval(ub9);
        Invocable invocable = (Invocable) docjs;
        String functionResult = (String)invocable.invokeFunction("ub98484234");
        Matcher matcher = Pattern.compile("v=(\\d+)").matcher(functionResult);
        if (!matcher.find()) {
            return null;
        }
        String v = matcher.group(1);
        String md5rb = MD5Util.md5String(rid + "10000000000000000000000000001501" + tt + v);
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
            System.out.println("表达式runtime错误:" + e.getMessage());
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
    private static String getSignUrl(String qn, String rid, String tt, String ub9) throws ScriptException, NoSuchAlgorithmException, NoSuchMethodException {
        String params = getSign(rid, tt, ub9);
        params = params + "&cdn=ws-h5&rate=" + qn;
        Map<String, Object> paramsMap = handleParams(params);

        String requestUrl = "https://www.douyu.com/lapi/live/getH5Play/"+rid;
        JSONObject response = HttpRequest.create(requestUrl)
                .appendParameters(paramsMap)
                .post()
                .getBodyJson();

        if (response.getIntValue("code") != 0) {
            return null;
        }
        JSONObject data = response.getJSONObject("data");
        if (data == null){
            return null;
        }
        String url = data.getString("rtmp_live");
        url = handleUrl(url);
        roomUrlMap.put(rid, url);
        List<Integer> rateList = handleRate(data.getJSONArray("multirates"));
        roomRateMap.put(rid, rateList);

        return url;
    }

    /**
     *
     * @param rid
     * @return
     */
    private static JSONObject getHomeJs(String rid) {
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
        jsonObject.put("homejs", homejs);
        jsonObject.put("real_rid", realRid);
        return jsonObject;
    }

    /**
     * @return
     */
    private static JSONObject getTT() {
        long nowTime = System.currentTimeMillis();
        String tt1 = String.valueOf(nowTime / 1000);
        String tt2 = String.valueOf(nowTime);
        String today = getTimeStr(nowTime, "yyyyMMdd");

        JSONObject jsonObject = new JSONObject();
        jsonObject.put("tt1", tt1);
        jsonObject.put("tt2", tt2);
        jsonObject.put("today", today);
        return jsonObject;
    }

    /**
     * 获取格式化日期
     * @param time
     * @param format
     * @return
     */
    public static String getTimeStr(long time, String format) {
        Date date = new Date(time);
        SimpleDateFormat sdf = new SimpleDateFormat(format);
        return sdf.format(date);
    }

    /**
     * 获取斗鱼房间信息
     * @param roomId
     * @return
     */
    public static LiveRoomInfo getRoomInfo(String roomId) {
        String url = DouYuOpenApi.ROOM_INFO + roomId;
        HttpResponse response = HttpRequest.create(url)
                .setContentType(HttpContentType.FORM).get();
        if (404 == response.getCode()){
            return null;
        }
        JSONObject room_info = response.getBodyJson().getJSONObject("data");
        LiveRoomInfo liveRoomInfo = new LiveRoomInfo();
        liveRoomInfo.setPlatForm("douyu");
        liveRoomInfo.setRoomId(room_info.getString("room_id"));
        liveRoomInfo.setCategoryId(room_info.getString("cate_id"));//分类id不对
        liveRoomInfo.setCategoryName(room_info.getString("cate_name"));
        liveRoomInfo.setRoomName(room_info.getString("room_name"));
        liveRoomInfo.setOwnerName(room_info.getString("owner_name"));
        liveRoomInfo.setRoomPic(room_info.getString("room_thumb"));
        liveRoomInfo.setOwnerHeadPic(room_info.getString("avatar"));
        liveRoomInfo.setOnline(room_info.getInteger("online"));
        liveRoomInfo.setIsLive((room_info.getInteger("room_status") == 1) ? 1 : 0);

        return liveRoomInfo;
    }

    /**
     * 处理url获取唯一标识
     * @param url
     * @return
     */
    private static String handleUrl(String url){
        url = url.substring(0, url.indexOf("."));
        return url.split("_")[0];
    }

    /**
     * 处理直播间清晰度
     * @param jsonArray
     * @return
     */
    private static List<Integer> handleRate(JSONArray jsonArray){
        List<Integer> list = new ArrayList<>();
        for (int i = 0; i < jsonArray.size(); i++){
            list.add(jsonArray.getJSONObject(i).getIntValue("bit"));
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
    private static Map<String, Object> handleParams(String params){
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
    public static List<LiveRoomInfo> getRecommend(int page, int size){
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

    /**
     * 获取url请求的所有房间信息
     * @param url
     * @return
     */
    private static List<LiveRoomInfo> requestUrl(String url, String categoryName){
        List<LiveRoomInfo> list = new ArrayList<>();
        String result = HttpUtil.doGet(url);
        JSONObject resultJsonObj = JSON.parseObject(result);
        if (resultJsonObj.getInteger("code") == 0) {
            JSONArray roomList = resultJsonObj.getJSONObject("data").getJSONArray("list");
            Iterator<Object> it = roomList.iterator();
            while(it.hasNext()){
                JSONObject roomInfo = (JSONObject) it.next();
                LiveRoomInfo liveRoomInfo = new LiveRoomInfo();
                liveRoomInfo.setPlatForm("douyu");
                liveRoomInfo.setRoomId(roomInfo.getInteger("rid").toString());
                liveRoomInfo.setCategoryId(roomInfo.getString("cate2Id"));
                liveRoomInfo.setCategoryName(Global.DouyuCateMap.get(roomInfo.getString("cate2Id")));
                liveRoomInfo.setRoomName(roomInfo.getString("roomName"));
                liveRoomInfo.setOwnerName(roomInfo.getString("nickname"));
                liveRoomInfo.setRoomPic(roomInfo.getString("roomSrc"));
                liveRoomInfo.setOwnerHeadPic(roomInfo.getString("avatar"));
                liveRoomInfo.setOnline(DouyuNumStringToInt(roomInfo.getString("hn")));
                liveRoomInfo.setIsLive(roomInfo.getInteger("isLive"));
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
    public static Integer DouyuNumStringToInt(String number){
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
     * 刷新分类缓存
     * @return
     */
    public static void refreshArea(){
        String url = "https://m.douyu.com/api/cate/list";//获取bilibili所有分类的请求地址
        List<List<AreaInfo>> resultList = new ArrayList<>();
        Map<String, List<AreaInfo>> areaMapTemp = new HashMap<>();
        String result = HttpUtil.doGet(url);
        JSONObject resultJsonObj = JSON.parseObject(result);
        if (resultJsonObj.getInteger("code") == 0) {
            JSONArray cate1Info = resultJsonObj.getJSONObject("data").getJSONArray("cate1Info");
            Iterator<Object> it1 = cate1Info.iterator();
            Map<String, String> typeNameMap = new HashMap<>();
            List<String> typeSortList = new ArrayList<>();
            while(it1.hasNext()) {
                JSONObject areaInfo = (JSONObject) it1.next();
                String cate1Name = areaInfo.getString("cate1Name");
                String cate1Id = areaInfo.getInteger("cate1Id").toString();
                if("21".equals(cate1Id)){
                    continue;
                }
                areaMapTemp.put(cate1Name, new ArrayList<>());
                typeNameMap.put(cate1Id, cate1Name);
                typeSortList.add(cate1Name);
            }

            JSONArray cate2Info = resultJsonObj.getJSONObject("data").getJSONArray("cate2Info");
            Iterator<Object> it2 = cate2Info.iterator();
            while(it2.hasNext()){
                JSONObject areaInfo = (JSONObject) it2.next();
                AreaInfo douyuArea = new AreaInfo();
                String cate1Id = areaInfo.getInteger("cate1Id").toString();
                if("21".equals(cate1Id)){
                    continue;
                }
                douyuArea.setAreaType(cate1Id);
                douyuArea.setTypeName(typeNameMap.get(cate1Id));
                douyuArea.setAreaId(areaInfo.getInteger("cate2Id").toString());
                douyuArea.setAreaName(areaInfo.getString("cate2Name"));
                douyuArea.setAreaPic(areaInfo.getString("pic"));
                douyuArea.setShortName(areaInfo.getString("shortName"));
                douyuArea.setPlatform("douyu");
                Global.DouyuCateMap.put(douyuArea.getAreaId(), douyuArea.getAreaName());
                String areaTypeKey = douyuArea.getTypeName().substring(0,2);
                if (!Global.AllAreaMap.containsKey(areaTypeKey)){
                    List<String> list = new ArrayList<>();
                    list.add(douyuArea.getAreaName());
                    Global.AreaTypeSortList.add(areaTypeKey);
                    Global.AreaInfoSortMap.put(areaTypeKey, list);
                }else {
                    if(!Global.AllAreaMap.get(areaTypeKey).containsKey(douyuArea.getAreaName())){
                        Global.AreaInfoSortMap.get(areaTypeKey).add(douyuArea.getAreaName());
                    }
                }
                Global.AllAreaMap.computeIfAbsent(areaTypeKey, k -> new HashMap<>())
                        .computeIfAbsent(douyuArea.getAreaName(), k -> new HashMap<>()).put("douyu", douyuArea);
                areaMapTemp.get(douyuArea.getTypeName()).add(douyuArea);
            }

            Iterator<String> itList = typeSortList.iterator();
            while(itList.hasNext()){
                String type = itList.next();
                resultList.add(areaMapTemp.get(type));
            }
        }
        Global.platformAreaMap.put("douyu",resultList);
    }

    /**
     * 获取斗鱼分区房间
     * @param area
     * @param page
     * @param size
     * @return
     */
    public static List<LiveRoomInfo> getAreaRoom(String area, int page, int size){
        List<LiveRoomInfo> list = new ArrayList<>();
        AreaInfo areaInfo = Global.getAreaInfo("douyu", area);
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

    public static List<LiveRoomInfo> getRoomTest(String area){
        AreaInfo areaInfo = Global.getAreaInfo("douyu", area);
        String url = "https://www.douyu.com/gapi/rkc/directory/mixList/2_"+areaInfo.getAreaId()+"/1";
        List<LiveRoomInfo> list = new ArrayList<>();
        String result = HttpUtil.doGet(url);
        JSONObject resultJsonObj = JSON.parseObject(result);
        if (resultJsonObj.getInteger("code") == 0) {
            JSONArray roomList = resultJsonObj.getJSONObject("data").getJSONArray("rl");
            Iterator<Object> it = roomList.iterator();
            while(it.hasNext()){
                JSONObject roomInfo = (JSONObject) it.next();
                LiveRoomInfo liveRoomInfo = new LiveRoomInfo();
                liveRoomInfo.setPlatForm("douyu");
                liveRoomInfo.setRoomId(roomInfo.getInteger("rid").toString());
                liveRoomInfo.setCategoryId(roomInfo.getString("cate2Id"));
                liveRoomInfo.setCategoryName(roomInfo.getString("c2name"));
                liveRoomInfo.setRoomName(roomInfo.getString("rn"));
                liveRoomInfo.setOwnerName(roomInfo.getString("nn"));
                liveRoomInfo.setRoomPic(roomInfo.getString("rs16"));
                liveRoomInfo.setOwnerHeadPic("http://apic.douyucdn.cn/upload/"+roomInfo.getString("av"));
                liveRoomInfo.setOnline(roomInfo.getInteger("ol"));
                liveRoomInfo.setIsLive(1);
                list.add(liveRoomInfo);
            }
        }
        return list;
    }

    /**
     * 搜索
     * @param keyWords  搜索关键字
     * @param isLive 是否搜索直播中的信息
     * @return
     */
    public static List<Owner> search(String keyWords, String isLive){
        List<Owner> list = new ArrayList<>();
        String url = "https://www.douyu.com/japi/search/api/searchAnchor?kw="+ keyWords + "&page=1&pageSize=5&filterType=" + isLive;
        String result = HttpUtil.doGet(url);
        JSONObject resultJsonObj = JSON.parseObject(result);
        if (resultJsonObj.getInteger("error") == 0) {
            JSONArray ownerList = resultJsonObj.getJSONObject("data").getJSONArray("relateAnchor");
            Iterator<Object> it = ownerList.iterator();
            while(it.hasNext()){
                JSONObject responseOwner = (JSONObject) it.next();
                Owner owner = new Owner();
                owner.setNickName(responseOwner.getString("nickName"));
                owner.setCateName(responseOwner.getString("cateName"));
                owner.setHeadPic(responseOwner.getString("avatar"));
                owner.setPlatform("douyu");
                owner.setRoomId(responseOwner.getString("rid"));
                owner.setIsLive((responseOwner.getInteger("isLive") == 1) ? "1" : "0");
                owner.setFollowers(DouyuNumStringToInt(responseOwner.getString("followerCount")));
                list.add(owner);
            }
        }
        return list;
    }
}
