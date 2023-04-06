package work.yj1211.live.service.platforms.impl;

import cn.hutool.core.net.NetUtil;
import cn.hutool.core.util.RandomUtil;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import work.yj1211.live.enums.Platform;
import work.yj1211.live.service.mysql.AreaInfoService;
import work.yj1211.live.service.platforms.BasePlatform;
import work.yj1211.live.utils.FixHuya;
import work.yj1211.live.utils.HttpUtil;
import work.yj1211.live.utils.http.HttpContentType;
import work.yj1211.live.utils.http.HttpRequest;
import work.yj1211.live.model.LiveRoomInfo;
import work.yj1211.live.model.Owner;
import work.yj1211.live.model.platformArea.AreaInfo;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
@Component
public class Huya implements BasePlatform {
    @Autowired
    private AreaInfoService areaInfoService;

    private static final Pattern OwnerName = Pattern.compile("\"sNick\":\"([\\s\\S]*?)\",");
    private static final Pattern RoomName = Pattern.compile("\"sIntroduction\":\"([\\s\\S]*?)\",");
    private static final Pattern RoomPic = Pattern.compile("\"sScreenshot\":\"([\\s\\S]*?)\",");
    private static final Pattern OwnerPic = Pattern.compile("\"sAvatar180\":\"([\\s\\S]*?)\",");
    private static final Pattern AREA = Pattern.compile("\"sGameFullName\":\"([\\s\\S]*?)\",");
    private static final Pattern Num = Pattern.compile("\"lActivityCount\":([\\s\\S]*?),");
    private static final Pattern ISLIVE = Pattern.compile("\"eLiveStatus\":([\\s\\S]*?),");

    @Override
    public String getPlatformName() {
        return Platform.HUYA.getName();
    }

    /**
     * 搜索
     *
     * @param keyWords 搜索关键字
     * @return
     */
    @Override
    public List<Owner> search(String keyWords){
        List<Owner> list = new ArrayList<>();
        String ip = NetUtil.longToIpv4(RandomUtil.randomLong());
        String url = "https://search.cdn.huya.com/?m=Search&do=getSearchContent&q=" + keyWords + "&uid=0&v=4&typ=-5&livestate=0&rows=5&start=0";
        Map<String, String> headers = new HashMap<>(1);
        headers.put("X-Forwarded-For", ip);
        String result = HttpUtil.doGetWithHeaders(url, headers);
        JSONObject resultJsonObj = JSONUtil.parseObj(result);
        if (result != null) {
            JSONArray ownerList = resultJsonObj.getJSONObject("response").getJSONObject("1").getJSONArray("docs");
            Iterator<Object> it = ownerList.iterator();
            while(it.hasNext()){
                JSONObject responseOwner = (JSONObject) it.next();
                Owner owner = new Owner();
                owner.setNickName(responseOwner.getStr("game_nick"));
                owner.setCateName(responseOwner.getStr("game_name"));
                owner.setHeadPic(responseOwner.getStr("game_avatarUrl52"));
                owner.setPlatform(getPlatformName());
                owner.setRoomId(responseOwner.getStr("room_id"));
                owner.setIsLive(responseOwner.getBool("gameLiveOn") ? "1" : "0");
                owner.setFollowers(responseOwner.getInt("game_activityCount"));
                list.add(owner);
            }
        }
        return list;
    }

    /**
     * 获取真实地址
     * @param urls
     * @param roomId
     */
    @Override
    public void getRealUrl(Map<String, String> urls, String roomId) {
        FixHuya.getRealUrl(urls, roomId);
    }

    /**
     * 获取虎牙单个类型下的所有分区
     * @param areaCode
     * @return
     */
    private List<AreaInfo> refreshSingleArea(String areaCode, String typeName){
        List<AreaInfo> areaInfoList = new ArrayList<>();
        String url = "https://m.huya.com/cache.php?m=Game&do=ajaxGameList&bussType=" + areaCode;
        String result = HttpRequest.create(url)
                .setContentType(HttpContentType.FORM)
                .putHeader("User-Agent", "Mozilla/5.0 (Linux; Android 5.0; SM-G900P Build/LRX21T) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/75.0.3770.100 Mobile Safari/537.36")
                .get().getBody();
        JSONObject responseObj = JSONUtil.parseObj(result);
        if (result != null && responseObj.containsKey("gameList")) {
            // 获取新的分区信息
            JSONArray gameList = responseObj.getJSONArray("gameList");

            gameList.forEach(item->{
                JSONObject areaInfo = (JSONObject) item;
                AreaInfo huyaArea = new AreaInfo();
                huyaArea.setAreaType(areaCode);
                huyaArea.setTypeName(typeName);
                huyaArea.setAreaId(areaInfo.getStr("gid"));
                huyaArea.setAreaName(areaInfo.getStr("gameFullName"));
                huyaArea.setAreaPic("https://huyaimg.msstatic.com/cdnimage/game/" + huyaArea.getAreaId() + "-MS.jpg");
                huyaArea.setPlatform(getPlatformName());
                areaInfoList.add(huyaArea);
            });
        }
        return areaInfoList;
    }

    /**
     * 通过移动端请求获取虎牙房间信息
     * @param roomId
     * @return
     */
    @Override
    public LiveRoomInfo getRoomInfo(String roomId) {
        String room_url = "https://m.huya.com/" + roomId;
        String response = HttpRequest.create(room_url)
                .setContentType(HttpContentType.FORM)
                .putHeader("User-Agent", "Mozilla/5.0 (Linux; Android 5.0; SM-G900P Build/LRX21T) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/75.0.3770.100 Mobile Safari/537.36")
                .get().getBody();
        Matcher matcherOwnerName = OwnerName.matcher(response);
        Matcher matcherRoomName = RoomName.matcher(response);
        Matcher matcherRoomPic = RoomPic.matcher(response);
        Matcher matcherOwnerPic = OwnerPic.matcher(response);
        Matcher matcherAREA = AREA.matcher(response);
        Matcher matcherNum = Num.matcher(response);
        Matcher matcherISLIVE = ISLIVE.matcher(response);
        if (!(matcherOwnerName.find() && matcherRoomName.find() && matcherRoomPic.find()
                && matcherOwnerPic.find() && matcherAREA.find() && matcherNum.find()
                && matcherISLIVE.find())){
            log.info("虎牙获取房间信息异常,roomId:[{}]",roomId);
        }
        String resultOwnerName = matcherOwnerName.group();
        String resultRoomName = matcherRoomName.group();
        String resultRoomPic = matcherRoomPic.group();
        String resultOwnerPic = matcherOwnerPic.group();
        String resultAREA = matcherAREA.group();
        String resultNum = matcherNum.group();
        String resultISLIVE = matcherISLIVE.group();
        LiveRoomInfo liveRoomInfo = new LiveRoomInfo();

        liveRoomInfo.setRoomId(roomId);
        liveRoomInfo.setPlatForm(getPlatformName());
        liveRoomInfo.setOwnerName(getMatchResult(resultOwnerName, "\":\"", "\""));
        liveRoomInfo.setRoomName(getMatchResult(resultRoomName,"\":\"", "\""));
        liveRoomInfo.setRoomPic(getMatchResult(resultRoomPic, "\":\"", "\""));
        liveRoomInfo.setOwnerHeadPic(getMatchResult(resultOwnerPic, "\":\"", "\""));
        liveRoomInfo.setCategoryName(getMatchResult(resultAREA, "\":\"", "\""));
        if (!getMatchResult(resultNum, "\":", ",").equals("") ) {
            liveRoomInfo.setOnline(Integer.valueOf(getMatchResult(resultNum, "\":", ",")));
        } else {
            liveRoomInfo.setOnline(0);
        }
        liveRoomInfo.setIsLive(getMatchResult(resultISLIVE, "\":", ",").equals("2") ? 1 : 0);

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
        int realPage = page/6 + 1;
        int start = (page-1)*size%120;
        if (size == 10){
            realPage = page/12 + 1;
            start = (page-1)*size%120;
        }
        String url = "https://www.huya.com/cache.php?m=LiveList&do=getLiveListByPage&tagAll=0&page="+realPage;
        String result = HttpUtil.doGet(url);
        JSONObject resultJsonObj = JSONUtil.parseObj(result);
        if (resultJsonObj.getInt("status") == 200) {
            JSONArray data = resultJsonObj.getJSONObject("data").getJSONArray("datas");
            for (int i = start; i < start+size; i++){
                JSONObject roomInfo = data.getJSONObject(i);
                LiveRoomInfo liveRoomInfo = new LiveRoomInfo();
                liveRoomInfo.setPlatForm(getPlatformName());
                liveRoomInfo.setRoomId(roomInfo.getStr("profileRoom"));
                liveRoomInfo.setCategoryId(roomInfo.getStr("gid"));
                liveRoomInfo.setCategoryName(roomInfo.getStr("gameFullName"));
                liveRoomInfo.setRoomName(roomInfo.getStr("introduction"));
                liveRoomInfo.setOwnerName(roomInfo.getStr("nick"));
                liveRoomInfo.setRoomPic(roomInfo.getStr("screenshot"));
                liveRoomInfo.setOwnerHeadPic(roomInfo.getStr("avatar180"));
                liveRoomInfo.setOnline(Integer.valueOf(roomInfo.getStr("totalCount")));
                liveRoomInfo.setIsLive(1);
                list.add(liveRoomInfo);
            }
        }
        return list;
    }

    @Override
    public List<AreaInfo> getAreaList() {
        List<AreaInfo> areaInfoList = new ArrayList<>();
        areaInfoList.addAll(refreshSingleArea("1", "网游"));
        areaInfoList.addAll(refreshSingleArea("2", "单机"));
        areaInfoList.addAll(refreshSingleArea("3", "手游"));
        areaInfoList.addAll(refreshSingleArea("8", "娱乐"));
        return areaInfoList;
    }

    /**
     * 获取虎牙分区房间
     * @param area
     * @param page
     * @param size
     * @return
     */
    @Override
    public List<LiveRoomInfo> getAreaRoom(String area, int page, int size){
        List<LiveRoomInfo> list = new ArrayList<>();
//        int realPage = page/6 + 1;
//        int start = (page-1)*size%120;
//        if (size == 10){
//            realPage = page/12 + 1;
//            start = (page-1)*size%120;
//        }
//        AreaInfo areaInfo = Global.getAreaInfo(getPlatformName(), area);
//        String url = "https://www.huya.com/cache.php?m=LiveList&do=getLiveListByPage&gameId=" + areaInfo.getAreaId() + "&tagAll=0&page="+realPage;
//        String result = HttpUtil.doGet(url);
//        JSONObject resultJsonObj = JSONUtil.parseObj(result);
//        if (resultJsonObj.getInt("status") == 200) {
//            JSONArray data = resultJsonObj.getJSONObject("data").getJSONArray("datas");
//            for (int i = start; i < start+size; i++){
//                JSONObject roomInfo = data.getJSONObject(i);
//                LiveRoomInfo liveRoomInfo = new LiveRoomInfo();
//                liveRoomInfo.setPlatForm(getPlatformName());
//                liveRoomInfo.setRoomId(roomInfo.getStr("profileRoom"));
//                liveRoomInfo.setCategoryId(roomInfo.getStr("gid"));
//                liveRoomInfo.setCategoryName(roomInfo.getStr("gameFullName"));
//                liveRoomInfo.setRoomName(roomInfo.getStr("introduction"));
//                liveRoomInfo.setOwnerName(roomInfo.getStr("nick"));
//                liveRoomInfo.setRoomPic(roomInfo.getStr("screenshot"));
//                liveRoomInfo.setOwnerHeadPic(roomInfo.getStr("avatar180"));
//                liveRoomInfo.setOnline(Integer.valueOf(roomInfo.getStr("totalCount")));
//                liveRoomInfo.setIsLive(1);
//                list.add(liveRoomInfo);
//            }
//        }
        return list;
    }

    /**
     * 分割搜索结果
     * @param str
     * @return
     */
    private String getMatchResult(String str, String indexStartStr, String indexEndStr) {
        String result;
        result = str.substring(str.indexOf(indexStartStr)+indexStartStr.length(),str.lastIndexOf(indexEndStr));
        return result;
    }
}
