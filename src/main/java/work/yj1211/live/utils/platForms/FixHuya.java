package work.yj1211.live.utils.platForms;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.net.URLDecoder;
import cn.hutool.crypto.digest.DigestAlgorithm;
import cn.hutool.crypto.digest.Digester;
import com.alibaba.fastjson.JSON;
import com.fasterxml.jackson.annotation.JsonProperty;
import kotlin.text.Charsets;
import lombok.Data;
import lombok.NoArgsConstructor;
import work.yj1211.live.utils.http.HttpRequest;

import java.io.Serializable;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 修复虎牙解析
 *
 * @author Hangsman
 */
public class FixHuya {

    private static final String URL_FORMAT_TEMPLATE = "%s/%s.flv?wsSecret=%s&wsTime=%s&seqid=%s&ctype=%s&ver=1&txyp=%s&fs=%s&u=%s&t=%s&sv=2107230539";
    private static final List<String> QN_LIST = CollectionUtil.toList("HD","SD","LD","FD");
    private static final Digester MD5 = new Digester(DigestAlgorithm.MD5);
    private static final Long UID = 1464636405087L;

    public static void getRealUrl(Map<String, String> urls, String roomId) {
        HuyaDTO liveInfo = getHuyaLiveInfo(roomId);
        HuyaDTO.DataDTO.StreamDTO streamDTO = liveInfo.getData().getStream();
        HuyaDTO.DataDTO.StreamDTO.BaseSteamInfoListDTO steamInfoListDTO = streamDTO.getBaseSteamInfoList().get(0);
        String liveUrl = decodeLiveUrl(steamInfoListDTO.sFlvUrl, steamInfoListDTO.sStreamName, steamInfoListDTO.sFlvAntiCode);
        List<HuyaDTO.DataDTO.StreamDTO.HlsDTO.RateArrayDTO> rateArray = streamDTO.getHls().rateArray;
        List<Integer> ratioList = rateArray
                .stream()
                .map(HuyaDTO.DataDTO.StreamDTO.HlsDTO.RateArrayDTO::getIBitRate)
                .filter(item -> item != 0)
                .sorted()
                .collect(Collectors.toList());
        Collections.reverse(ratioList);
        for (int i = 0; i < ratioList.size(); i++) {
            urls.put(QN_LIST.get(i), liveUrl + "&ratio=" + ratioList.get(i));
        }
        urls.put("OD", liveUrl);
        urls.put("ayyuid", steamInfoListDTO.lChannelId.toString());
    }

    private static HuyaDTO getHuyaLiveInfo(String roomId){
        String json = HttpRequest.create("https://mp.huya.com/cache.php?m=Live&do=profileRoom&roomid="+ roomId)
                .get()
                .getBody();
        return JSON.parseObject(json, HuyaDTO.class);
    }


    private static String decodeLiveUrl(String cdnUrl,String streamName,String flvAntiCode) {
        String[] params = flvAntiCode.split("&");
        HashMap<String, String> paramMap = new HashMap<>();
        Arrays.stream(params).forEach(param->{
            String[] split = param.split("=");
            paramMap.put(split[0], split[1]);
        });
        String deFm = new String(Base64.getDecoder().decode(URLDecoder.decode(paramMap.get("fm"), Charsets.UTF_8)));
        String hashPrefix = deFm.split("_")[0];
        long currentTimeMillis = System.currentTimeMillis();
        String seqid = String.valueOf((currentTimeMillis + UID));
        String wsTime = Long.toString((currentTimeMillis / 1000 + 3600), 16).replace("0x","");
        String ctype = paramMap.get("ctype");
        String fs = paramMap.get("fs");
        String t = paramMap.get("t");
        String hash0 = MD5.digestHex(seqid + "|" + ctype + "|" + t);
        String wsSecret = MD5.digestHex(hashPrefix + "_" + UID + "_" + streamName + "_" + hash0 + "_" + wsTime);
        return String.format(URL_FORMAT_TEMPLATE, cdnUrl, streamName, wsSecret, wsTime, seqid, ctype, "", fs, UID.toString(), t);
    }
    @NoArgsConstructor
    @Data
    public static class HuyaDTO implements Serializable {
        @JsonProperty("status")
        private Integer status;
        @JsonProperty("data")
        private DataDTO data;

        @NoArgsConstructor
        @Data
        public static class DataDTO {
            @JsonProperty("stream")
            private StreamDTO stream;
            @JsonProperty("liveStatus")
            private String liveStatus;

            @NoArgsConstructor
            @Data
            public static class StreamDTO {
                @JsonProperty("baseSteamInfoList")
                private List<BaseSteamInfoListDTO> baseSteamInfoList;
                @JsonProperty("hls")
                private HlsDTO hls;

                @NoArgsConstructor
                @Data
                public static class HlsDTO {
                    @JsonProperty("rateArray")
                    private List<RateArrayDTO> rateArray;

                    @NoArgsConstructor
                    @Data
                    public static class RateArrayDTO {
                        @JsonProperty("sDisplayName")
                        private String sDisplayName;
                        @JsonProperty("iBitRate")
                        private Integer iBitRate;
                    }
                }

                @NoArgsConstructor
                @Data
                public static class BaseSteamInfoListDTO {
                    @JsonProperty("lChannelId")
                    private Long lChannelId;
                    @JsonProperty("sStreamName")
                    private String sStreamName;
                    @JsonProperty("sFlvUrl")
                    private String sFlvUrl;
                    @JsonProperty("sFlvAntiCode")
                    private String sFlvAntiCode;
                }
            }
        }
    }

}
