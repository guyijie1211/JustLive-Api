package work.yj1211.live.utils.platForms;

import work.yj1211.live.vo.LiveRoomInfo;
import work.yj1211.live.vo.Owner;

import java.util.List;
import java.util.Map;

public abstract class BasePlatform {
    abstract void getRealUrl(Map<String, String> urls, String rid);

    abstract LiveRoomInfo getRoomInfo(String roomId);

    abstract List<LiveRoomInfo> getRecommend(int page, int size);

    public void refreshArea() {}

    abstract List<LiveRoomInfo> getAreaRoom(String area, int page, int size);

    abstract List<Owner> search(String keyWords, String isLive);
}
