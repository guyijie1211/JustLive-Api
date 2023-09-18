package work.yj1211.live.service.platforms.impl;

import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import work.yj1211.live.enums.Platform;
import work.yj1211.live.model.LiveRoomInfo;
import work.yj1211.live.model.Owner;
import work.yj1211.live.model.platformArea.AreaInfo;
import work.yj1211.live.service.platforms.BasePlatform;
import work.yj1211.live.utils.Global;
import work.yj1211.live.utils.HttpUtil;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
public class Douyin implements BasePlatform {
    @Override
    public String getPlatformCode() {
        return Platform.DOUYIN.getCode();
    }

    @Override
    public void getRealUrl(Map<String, String> urls, String rid) {

    }

    @Override
    public LiveRoomInfo getRoomInfo(String roomId) {
        return null;
    }

    @Override
    public List<LiveRoomInfo> getRecommend(int page, int size) {
        return null;
    }

    @Override
    public List<AreaInfo> getAreaList() {
        return null;
    }

    @Override
    public List<LiveRoomInfo> getAreaRoom(String area, int page, int size) {
        List<LiveRoomInfo> list = new ArrayList<>();
        try {
            AreaInfo areaInfo = Global.getAreaInfo(getPlatformCode(), area);
            areaInfo = new AreaInfo();
            areaInfo.setAreaId("633");
            String url = "https://live.douyin.com/webcast/web/partition/detail/room/?" +
                    "aid=6383&" +
                    "app_name=douyin_web&" +
                    "live_id=1&" +
                    "device_platform=web&" +
                    "count=" + page +"&" +
                    "offset="+ size +"&" +
                    "partition="+ areaInfo.getAreaId() + "&" +
                    "partition_type=1&" +
                    "req_from=2";
            String result = HttpUtil.doGet(url);
            JSONObject resultJsonObj = JSONUtil.parseObj(result);
            if (resultJsonObj.getInt("status_code") == 0) {
                JSONArray data = resultJsonObj.getJSONObject("data").getJSONArray("data");
                Iterator<Object> it = data.iterator();
                while(it.hasNext()){
                    JSONObject roomInfo = (JSONObject) it.next();
                    JSONObject ownerInfo = roomInfo.getJSONObject("owner");
                    LiveRoomInfo liveRoomInfo = new LiveRoomInfo();
                    liveRoomInfo.setPlatForm(getPlatformCode());
                    liveRoomInfo.setRoomId(roomInfo.getStr("web_rid"));
                    liveRoomInfo.setCategoryId(areaInfo.getAreaId());
                    liveRoomInfo.setCategoryName(roomInfo.getStr("tag_name"));
                    liveRoomInfo.setRoomName(roomInfo.getStr("title"));
                    liveRoomInfo.setOwnerName(ownerInfo.getStr("nickname"));
                    liveRoomInfo.setRoomPic((String) roomInfo.getJSONObject("cover").getJSONArray("url_list").get(0));
                    liveRoomInfo.setOwnerHeadPic((String) ownerInfo.getJSONObject("avatar_thumb").getJSONArray("url_list").get(0));
                    liveRoomInfo.setOnline(roomInfo.getJSONObject("room_view_stats").getInt("display_value"));
                    liveRoomInfo.setIsLive(1);
                    list.add(liveRoomInfo);
                }
            }
        } catch (Exception e) {
            log.error("抖音---获取分区房间异常---area：" + area, e);
        }

        return list;
    }

    @Override
    public List<Owner> search(String keyWords) {
        return null;
    }
}
