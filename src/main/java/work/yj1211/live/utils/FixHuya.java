package work.yj1211.live.utils;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.net.URLDecoder;
import cn.hutool.core.util.ReUtil;
import cn.hutool.crypto.digest.DigestAlgorithm;
import cn.hutool.crypto.digest.Digester;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import work.yj1211.live.utils.http.HttpContentType;
import work.yj1211.live.utils.http.HttpRequest;

import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 修复虎牙解析
 *
 * @author Hangsman
 */
@Slf4j
public class FixHuya {

    private static final String URL_FORMAT_TEMPLATE = "%s/%s.flv?wsSecret=%s&wsTime=%s&seqid=%s&ctype=%s&ver=1&txyp=%s&fs=%s&u=%s&t=%s&sv=2107230539";
    private static final List<String> QN_LIST = CollectionUtil.toList("HD", "SD", "LD", "FD");
    private static final Pattern FLV_URL_PATTERN = Pattern.compile("\"sFlvUrl\":\"([\\s\\S]*?)\",");
    private static final Pattern ANTI_CODE_PATTERN = Pattern.compile("\"sFlvAntiCode\":\"([\\s\\S]*?)\",");
    private static final Pattern STREAM_NAME_PATTERN = Pattern.compile("\"sStreamName\":\"([\\s\\S]*?)\",");
    private static final Pattern LUID_PATTERN = Pattern.compile("},\"lUid\":([\\s\\S]*?),");
    private static final Pattern RATE_INFO_PATTERN = Pattern.compile("\"vBitRateInfo\":([\\s\\S]*?)\"},\"");
    private static final Long UID = 1464636405087L;

    public static void getRealUrl(Map<String, String> urls, String roomId) {
        LiveStreamInfo liveStreamInfo = getLiveStreamInfo(roomId);
        if (liveStreamInfo == null) {
            return;
        }
        String liveUrl;
        try {
            liveUrl = decodeLiveUrl(liveStreamInfo.getFlvUrl(), liveStreamInfo.getStreamName(), liveStreamInfo.getAntiCode());
        } catch (Exception e) {
            log.error("虎牙获取异常", e);
            return;
        }
        List<Integer> qnList = liveStreamInfo.getQnList();
        for (int i = 0; i < qnList.size(); i++) {
            urls.put(QN_LIST.get(i), liveUrl + "&ratio=" + qnList.get(i));
        }
        urls.put("OD", liveUrl);
        urls.put("ayyuid", liveStreamInfo.getLuid());
    }


    public static LiveStreamInfo getLiveStreamInfo(String roomId) {
        String room_url = "https://m.huya.com/" + roomId;
        String response = HttpRequest.create(room_url)
                .setContentType(HttpContentType.FORM)
                .putHeader("User-Agent", "Mozilla/5.0 (Linux; Android 5.0; SM-G900P Build/LRX21T) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/75.0.3770.100 Mobile Safari/537.36")
                .get().getBody();
        Matcher urlMatcher = FLV_URL_PATTERN.matcher(response);
        Matcher antiCodeMatcher = ANTI_CODE_PATTERN.matcher(response);
        Matcher streamNameMatcher = STREAM_NAME_PATTERN.matcher(response);
        Matcher RateInfoMatcher = RATE_INFO_PATTERN.matcher(response);
        Matcher LuidMatcher = LUID_PATTERN.matcher(response);
        if (!urlMatcher.find() || !antiCodeMatcher.find() || !streamNameMatcher.find() || !RateInfoMatcher.find()) {
            return null;
        }
        String luid = LuidMatcher.find() ? LuidMatcher.group(1) : "";
        String flvUrl = urlMatcher.group(1);
        String antiCode = antiCodeMatcher.group(1);
        String streamName = streamNameMatcher.group(1);
        String rateInfo = RateInfoMatcher.group(1) + "\"}";
        JSONObject jsonObject = JSONUtil.parseObj(ReUtil.replaceAll(rateInfo, "function([\\s\\S]*?)},", "0,"));
        JSONArray jsonArray = jsonObject.getJSONArray("value");
        List<Integer> qnList = new ArrayList<>();
        for (int i = 0; i < jsonArray.size(); i++) {
            int qn = jsonArray.getJSONObject(i).getInt("iBitRate");
            if (qn != 0) {
                qnList.add(qn);
            }
        }
        Collections.sort(qnList);
        Collections.reverse(qnList);
        flvUrl = URLDecoder.decode(flvUrl.replace("\\u002F", "/"), StandardCharsets.UTF_8);
        return new LiveStreamInfo(flvUrl, streamName, antiCode, qnList, luid);
    }


    private static String decodeLiveUrl(String cdnUrl, String streamName, String flvAntiCode) {
        String[] params = flvAntiCode.split("&");
        HashMap<String, String> paramMap = new HashMap<>();
        Arrays.stream(params).forEach(param -> {
            String[] split = param.split("=");
            paramMap.put(split[0], split[1]);
        });
        String deFm = new String(Base64.getDecoder().decode(URLDecoder.decode(paramMap.get("fm"), StandardCharsets.UTF_8)));
        String hashPrefix = deFm.split("_")[0];
        long currentTimeMillis = System.currentTimeMillis();
        String seqid = String.valueOf((currentTimeMillis + UID));
        String wsTime = Long.toString((currentTimeMillis / 1000 + 3600), 16).replace("0x", "");
        String ctype = paramMap.get("ctype");
        String fs = paramMap.get("fs");
        String t = paramMap.get("t");
        Digester MD5 = new Digester(DigestAlgorithm.MD5);
        String hash0 = MD5.digestHex(seqid + "|" + ctype + "|" + t);
        String wsSecret = MD5.digestHex(hashPrefix + "_" + UID + "_" + streamName + "_" + hash0 + "_" + wsTime);
        return String.format(URL_FORMAT_TEMPLATE, cdnUrl, streamName, wsSecret, wsTime, seqid, ctype, "", fs, UID.toString(), t);
    }

    @NoArgsConstructor
    @AllArgsConstructor
    @Data
    public static class LiveStreamInfo {

        private String flvUrl;
        private String streamName;
        private String antiCode;
        private List<Integer> qnList;
        private String luid;

    }

}
