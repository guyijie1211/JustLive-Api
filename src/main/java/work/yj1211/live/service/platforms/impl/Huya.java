package work.yj1211.live.service.platforms.impl;

import cn.hutool.core.codec.Base64;
import cn.hutool.core.net.NetUtil;
import cn.hutool.core.net.URLDecoder;
import cn.hutool.core.util.RandomUtil;
import cn.hutool.core.util.ReUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.crypto.SecureUtil;
import cn.hutool.http.Header;
import cn.hutool.http.HttpRequest;
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

import java.io.ByteArrayInputStream;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
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
    public LinkedHashMap<String, List<UrlQuality>> getRealUrl(String roomId) {
        LinkedHashMap<String, List<UrlQuality>> resultMap = new LinkedHashMap<>();
        List<UrlQuality> qualityResultList = new ArrayList<>();
        try {
            String resultText = HttpRequest.get("https://m.huya.com/" + roomId)
                    .header(Header.USER_AGENT, "Mozilla/5.0 (Linux; Android 11; Pixel 5) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/90.0.4430.91 Mobile Safari/537.36 Edg/117.0.0.0")
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
                    streamUrl += "?" + processAnticode(line.getStr("sFlvAntiCode"), getUid(13, 10), streamName);
                    if (bitRate > 0) {
                        streamUrl += "&ratio=" + bitRate;
                    }
                    UrlQuality urlQuality = new UrlQuality();
                    urlQuality.setQualityName(qualityName);
                    urlQuality.setUrlType(PlayUrlType.FLV.getTypeName());
                    urlQuality.setPlayUrl(streamUrl);
                    urlQuality.setSourceName("线路" + (i + 1));
                    resultMap.put(urlQuality.getSourceName(), null);
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
        Map<String, List<UrlQuality>> dataMap = qualityResultList.stream().collect(
                Collectors.groupingBy(UrlQuality::getSourceName)
        );

        resultMap.forEach((sourceName, valueList) -> {
            resultMap.put(sourceName, dataMap.get(sourceName));
        });
        return resultMap;
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
        LiveRoomInfo liveRoomInfo = new LiveRoomInfo();
        try {
            String room_url = "https://m.huya.com/" + roomId;
            String response = HttpRequest.get(room_url)
                    .contentType("application/x-www-form-urlencoded")
                    .header("User-Agent", "Mozilla/5.0 (Linux; Android 5.0; SM-G900P Build/LRX21T) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/75.0.3770.100 Mobile Safari/537.36")
                    .execute().body();
            if (response.contains("找不到此页面")) {
                log.info("虎牙获取房间信息异常,找不到此页面,roomId:[{}]", roomId);
                return liveRoomInfo;
            }
            Matcher matcherOwnerName = OwnerName.matcher(response);
            Matcher matcherRoomName = RoomName.matcher(response);
            Matcher matcherRoomPic = RoomPic.matcher(response);
            Matcher matcherOwnerPic = OwnerPic.matcher(response);
            Matcher matcherAREA = AREA.matcher(response);
            Matcher matcherNum = Num.matcher(response);
            Matcher matcherISLIVE = ISLIVE.matcher(response);
            if (!(matcherOwnerName.find() && matcherRoomName.find() && matcherRoomPic.find()
                    && matcherOwnerPic.find() && matcherAREA.find() && matcherNum.find()
                    && matcherISLIVE.find())) {
                log.info("虎牙获取房间信息异常,roomId:[{}]", roomId);
                return liveRoomInfo;
            }
            String resultOwnerName = matcherOwnerName.group();
            String resultRoomName = matcherRoomName.group();
            String resultRoomPic = matcherRoomPic.group();
            String resultOwnerPic = matcherOwnerPic.group();
            String resultAREA = matcherAREA.group();
            String resultNum = matcherNum.group();
            String resultISLIVE = matcherISLIVE.group();


            liveRoomInfo.setRoomId(roomId);
            liveRoomInfo.setPlatForm(getPlatformCode());
            liveRoomInfo.setOwnerName(getMatchResult(resultOwnerName, "\":\"", "\""));
            liveRoomInfo.setRoomName(getMatchResult(resultRoomName, "\":\"", "\""));
            liveRoomInfo.setRoomPic(getMatchResult(resultRoomPic, "\":\"", "\""));
            liveRoomInfo.setOwnerHeadPic(getMatchResult(resultOwnerPic, "\":\"", "\""));
            liveRoomInfo.setCategoryName(getMatchResult(resultAREA, "\":\"", "\""));
            if (!getMatchResult(resultNum, "\":", ",").equals("")) {
                liveRoomInfo.setOnline(Integer.valueOf(getMatchResult(resultNum, "\":", ",")));
            } else {
                liveRoomInfo.setOnline(0);
            }
            liveRoomInfo.setIsLive(getMatchResult(resultISLIVE, "\":", ",").equals("2") ? 1 : 0);
        } catch (Exception e) {
            e.printStackTrace();
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

    private String processAnticode(String anticode, String uid, String streamname) throws UnsupportedEncodingException {
        Map<String, String> q = new HashMap<>();
        try {
            for (String param : anticode.split("&")) {
                String[] pair = param.split("=");
                String key = URLDecoder.decode(pair[0], StandardCharsets.UTF_8);
                String value = "";
                if (pair.length > 1) {
                    value = URLDecoder.decode(pair[1], StandardCharsets.UTF_8);
                }
                q.put(key, value);
            }
        } catch (Exception e) {

        }
        q.put("t", "100");
        q.put("ctype", "huya_live");

        long seqid = System.currentTimeMillis() + Long.parseLong(uid);

        // wsTime
        String wsTime = Long.toHexString(Instant.now().toEpochMilli() / 1000 + 21600);

        // wsSecret
        String fm = new String(Base64.decode(URLDecoder.decode(q.get("fm"), StandardCharsets.UTF_8)), StandardCharsets.UTF_8);
        String wsSecretPrefix = fm.split("_")[0];

        byte[] temp = String.format("%s|%s|%s", seqid, q.get("ctype"), q.get("t")).getBytes(StandardCharsets.UTF_8);
        String wsSecretHash = SecureUtil.md5(new ByteArrayInputStream(temp));
        String wsSecret = SecureUtil.md5(new ByteArrayInputStream(String.format("%s_%s_%s_%s_%s", wsSecretPrefix, uid, streamname, wsSecretHash, wsTime).getBytes(StandardCharsets.UTF_8)));

        LinkedHashMap<String, String> resultParamMap = new LinkedHashMap<>();
        resultParamMap.put("wsSecret", wsSecret);
        resultParamMap.put("wsTime", wsTime);
        resultParamMap.put("seqid", String.valueOf(seqid));
        resultParamMap.put("ctype", "huya_live");
        resultParamMap.put("ver", "1");
        resultParamMap.put("fs", q.get("fs"));
        resultParamMap.put("sphdcdn", q.putIfAbsent("sphdcdn", ""));
        resultParamMap.put("sphdDC", q.putIfAbsent("sphdDC", ""));
        resultParamMap.put("sphd", Objects.requireNonNull(q.putIfAbsent("sphd", "")).replace("*", "%2A"));
        resultParamMap.put("exsphd", Objects.requireNonNull(q.putIfAbsent("exsphd", "")).replace(",", "%2C"));
        resultParamMap.put("uid", uid);
        resultParamMap.put("uuid", getUUid());
        resultParamMap.put("t", "100");
        resultParamMap.put("sv", "2110211124");
        return buildQueryString(resultParamMap);
    }

    public static String getUUid() {
        long currentTime = System.currentTimeMillis();
        SecureRandom random = new SecureRandom();
        int randomValue = random.nextInt(Integer.MAX_VALUE);
        long result = (currentTime % 10000000000L * 1000L + randomValue) % 4294967295L;
        return Long.toString(result);
    }

    private String buildQueryString(Map<String, String> params) {
        StringBuilder sb = new StringBuilder();
        params.forEach((key, value) -> {
            sb.append(key).append("=").append(value).append("&");
        });
        if (sb.length() > 0) {
            sb.setLength(sb.length() - 1); // 移除最后一个"&"字符
        }
        return sb.toString();
    }

    public String getUid(Integer length, Integer bound) {
        Random random = new Random();
        char[] characters = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz" .toCharArray();
        StringBuilder uid = new StringBuilder();

        if (length != null) {
            for (int i = 0; i < length; i++) {
                uid.append(characters[random.nextInt(bound != null ? bound : characters.length)]);
            }
        }

        return uid.toString();
    }
}
