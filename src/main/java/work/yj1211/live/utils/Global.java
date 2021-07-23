package work.yj1211.live.utils;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import work.yj1211.live.vo.TV;
import work.yj1211.live.vo.platformArea.AreaInfo;

import java.util.*;

//全局变量
@Component
public class Global {
    public static Map<String, List<List<AreaInfo>>> platformAreaMap= new HashMap<>();
    public static Map<String, Map<String, Map<String, AreaInfo>>> AllAreaMap= new HashMap<>(); //<分区类型，<分区名,<平台，分区信息>>>
    public static Map<String, String> DouyuCateMap = new HashMap<>();
    public static Map<String, String> BilibiliCateMap = new HashMap<>();
    public static Map<String, String> HuyaCateMap = new HashMap<>();
    public static Map<String, String> CCCateMap = new HashMap<>();
    public static Map<String, String> EgameCateMap = new HashMap<>();
    public static Map<String, String> EgameCateMapVer = new HashMap<>();
    public static List<String> AreaTypeSortList = new ArrayList<>();
    public static Map<String, List<String>> AreaInfoSortMap = new HashMap<>();
    private static String m3uPath;
    public static List<TV> m3uResult;

    @Value("${path.m3uPath}")
    public void setM3uPath(String m3uPath) {
        Global.m3uPath = m3uPath;
    }

    public static String getM3uPath() {
        return m3uPath;
    }

    public static AreaInfo getAreaInfo(String platform, String areaName){
        List<List<AreaInfo>> platformList = platformAreaMap.get(platform);
        Iterator<List<AreaInfo>> it = platformList.iterator();
        while(it.hasNext()){
            List<AreaInfo> areaInfoList = it.next();
            Iterator<AreaInfo> it2 = areaInfoList.iterator();
            while(it2.hasNext()){
                AreaInfo areaInfo = it2.next();
                if (areaName.equals(areaInfo.getAreaName())){
                    return areaInfo;
                }
            }
        }
        return null;
    };
}
