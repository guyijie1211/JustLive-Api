package work.yj1211.live.service.platforms.impl;

import cn.hutool.core.codec.Base64;
import cn.hutool.core.net.NetUtil;
import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.RandomUtil;
import cn.hutool.core.util.ReUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.crypto.digest.DigestUtil;
import cn.hutool.http.Header;
import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import work.yj1211.live.enums.Platform;
import work.yj1211.live.enums.PlayUrlType;
import work.yj1211.live.model.platform.LiveRoomInfo;
import work.yj1211.live.model.platform.Owner;
import work.yj1211.live.model.platform.UrlQuality;
import work.yj1211.live.model.platformArea.AreaInfo;
import work.yj1211.live.service.platforms.BasePlatform;
import work.yj1211.live.utils.FixHuya;
import work.yj1211.live.utils.Global;
import work.yj1211.live.utils.HttpUtil;

import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Slf4j
@Component
public class Huya implements BasePlatform {
    private static final Pattern OwnerName = Pattern.compile("\"sNick\":\"([\\s\\S]*?)\",");
    private static final Pattern RoomName = Pattern.compile("\"sIntroduction\":\"([\\s\\S]*?)\",");
    private static final Pattern RoomPic = Pattern.compile("\"sScreenshot\":\"([\\s\\S]*?)\",");
    private static final Pattern OwnerPic = Pattern.compile("\"sAvatar180\":\"([\\s\\S]*?)\",");
    private static final Pattern AREA = Pattern.compile("\"sGameFullName\":\"([\\s\\S]*?)\",");
    private static final Pattern Num = Pattern.compile("\"lActivityCount\":([\\s\\S]*?),");
    private static final Pattern ISLIVE = Pattern.compile("\"eLiveStatus\":([\\s\\S]*?),");

    // 清晰度
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
        return Platform.HUYA.getCode();
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
                owner.setPlatform(getPlatformCode());
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

    @Override
    public Map<String, List<UrlQuality>> getRealUrl(String roomId) {
        List<UrlQuality> qualityResultList = new ArrayList<>();
        try {
            String resultText = HttpRequest.get("https://m.huya.com/" + roomId)
                    .header(Header.USER_AGENT, "Mozilla/5.0 (iPhone; CPU iPhone OS 13_2_3 like Mac OS X) AppleWebKit/605.1.15 (KHTML, like Gecko) Version/13.0.3 Mobile/15E148 Safari/604.1 Edg/91.0.4472.69")
                    .execute().body();
            String pattern = "window\\.HNF_GLOBAL_INIT.=.\\{(.*?)\\}.</script>";
            String text = ReUtil.get(pattern, resultText, 1);
            JSONObject jsonObj = JSONUtil.parseObj("{" + text + "}");

            JSONArray biterates = jsonObj.getJSONObject("roomInfo").getJSONObject("tLiveInfo").getJSONObject("tLiveStreamInfo").getJSONObject("vBitRateInfo").getJSONArray("value");
            JSONArray lines = jsonObj.getJSONObject("roomInfo").getJSONObject("tLiveInfo").getJSONObject("tLiveStreamInfo").getJSONObject("vStreamInfo").getJSONArray("value");

            for (int j = 0; j < biterates.size(); j++) {
                JSONObject biterate = (JSONObject) biterates.get(j);
                String qualityName = biterate.getStr("sDisplayName");
                int bitRate = biterate.getInt("iBitRate");
                if (StrUtil.containsIgnoreCase(qualityName, "HDR")) {
                    continue;
                }
                for (int i = 0; i < lines.size(); i++) {
                    JSONObject line = (JSONObject) lines.get(i);
                    String streamName = line.getStr("sStreamName");
                    String streamUrl = line.getStr("sFlvUrl") + "/" + streamName + ".flv";
                    streamUrl += "?" + processAnticode(line.getStr("sFlvAntiCode"), getAnonymousUid(), streamName);
                    if (bitRate > 0) {
                        streamUrl += "&ratio=" + bitRate;
                    }
                    UrlQuality urlQuality = new UrlQuality();
                    urlQuality.setQualityName(qualityName);
                    urlQuality.setUrlType(PlayUrlType.FLV.getTypeName());
                    urlQuality.setPlayUrl(streamUrl);
                    urlQuality.setSourceName("线路" + (i + 1));
                    urlQuality.setPriority(10 - j);
                    qualityResultList.add(urlQuality);
                }
            }
            biterates.forEach(biterate -> {

            });
        } catch (Exception e) {
            log.error("虎牙---获取直播源异常", e);
        }
        Collections.sort(qualityResultList);
        return qualityResultList.stream().collect(
                Collectors.groupingBy(UrlQuality::getSourceName)
        );
    }

    /**
     * 获取虎牙单个类型下的所有分区
     * @param areaCode
     * @return
     */
    private List<AreaInfo> refreshSingleArea(String areaCode, String typeName){
        List<AreaInfo> areaInfoList = new ArrayList<>();
        String url = "https://m.huya.com/cache.php?m=Game&do=ajaxGameList&bussType=" + areaCode;
        String result = HttpRequest.get(url)
                .contentType("application/x-www-form-urlencoded")
                .header("User-Agent", "Mozilla/5.0 (Linux; Android 5.0; SM-G900P Build/LRX21T) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/75.0.3770.100 Mobile Safari/537.36")
                .execute().body();
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
                huyaArea.setPlatform(getPlatformCode());
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
        String response = HttpRequest.get(room_url)
                .contentType("application/x-www-form-urlencoded")
                .header("User-Agent", "Mozilla/5.0 (Linux; Android 5.0; SM-G900P Build/LRX21T) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/75.0.3770.100 Mobile Safari/537.36")
                .execute().body();
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
        liveRoomInfo.setPlatForm(getPlatformCode());
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
                liveRoomInfo.setPlatForm(getPlatformCode());
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
        int realPage = page/6 + 1;
        int start = (page-1)*size%120;
        if (size == 10){
            realPage = page/12 + 1;
            start = (page-1)*size%120;
        }
        AreaInfo areaInfo = Global.getAreaInfo(getPlatformCode(), area);
        String url = "https://www.huya.com/cache.php?m=LiveList&do=getLiveListByPage&gameId=" + areaInfo.getAreaId() + "&tagAll=0&page="+realPage;
        String result = HttpUtil.doGet(url);
        JSONObject resultJsonObj = JSONUtil.parseObj(result);
        if (resultJsonObj.getInt("status") == 200) {
            JSONArray data = resultJsonObj.getJSONObject("data").getJSONArray("datas");
            for (int i = start; i < start+size; i++){
                JSONObject roomInfo = data.getJSONObject(i);
                LiveRoomInfo liveRoomInfo = new LiveRoomInfo();
                liveRoomInfo.setPlatForm(getPlatformCode());
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

    private String processAnticode(String anticode, String uid, String streamname) {
        Map<String, String> q = new HashMap<>();
        try {
            for (String param : anticode.split("&")) {
                String[] pair = param.split("=");
                String key = URLDecoder.decode(pair[0], "UTF-8");
                String value = "";
                if (pair.length > 1) {
                    value = URLDecoder.decode(pair[1], "UTF-8");
                }
                q.put(key, value);
            }
        } catch (Exception e) {

        }


        q.put("ver", "1");
        q.put("sv", "2110211124");

        long seqid = Long.parseLong(uid) + (long) (Instant.now().getEpochSecond() * 1000);
        q.put("seqid", String.valueOf(seqid));
        q.put("uid", uid);
        q.put("uuid", IdUtil.randomUUID());

        String ss = DigestUtil.md5Hex(String.format("%s|%s|%s", q.get("seqid"), q.get("ctype"), q.get("t")), StandardCharsets.UTF_8);
        q.put("fm", new String(Base64.decode(q.get("fm")), StandardCharsets.UTF_8)
                .replace("$0", q.get("uid"))
                .replace("$1", streamname)
                .replace("$2", ss)
                .replace("$3", q.get("wsTime")));

        String wsSecret = DigestUtil.md5Hex(q.get("fm"), StandardCharsets.UTF_8);
        q.put("wsSecret", wsSecret);
        q.remove("fm");

        if (q.containsKey("txyp")) {
            q.remove("txyp");
        }

        String result = buildQueryString(q);
        return result;
    }

    private String buildQueryString(Map<String, String> params) {
        StringBuilder sb = new StringBuilder();
        for (Map.Entry<String, String> entry : params.entrySet()) {
            sb.append(entry.getKey()).append("=").append(entry.getValue()).append("&");
        }
        if (sb.length() > 0) {
            sb.setLength(sb.length() - 1); // 移除最后一个"&"字符
        }
        return sb.toString();
    }

    private String getAnonymousUid() {
        String url = "https://udblgn.huya.com/web/anonymousLogin";
        String requestBody = "{\"appId\": 5002, \"byPass\": 3, \"context\": \"\", \"version\": \"2.4\", \"data\": {}}";

        HttpRequest request = HttpRequest.post(url)
                .contentType("application/json")
                .body(requestBody);

        HttpResponse response = request.execute();

        if (response.isOk()) {
            JSONObject jsonObject = new JSONObject(response.body());
            return jsonObject.getJSONObject("data").getStr("uid");
        }

        return null;
    }
}
