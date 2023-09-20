package work.yj1211.live.service.platforms;

import org.springframework.stereotype.Component;
import work.yj1211.live.model.platform.LiveRoomInfo;
import work.yj1211.live.model.platform.Owner;
import work.yj1211.live.model.platformArea.AreaInfo;

import java.util.List;
import java.util.Map;

/**
 * @author guyijie1211
 * @date 2023/3/27 15:10
 **/
@Component
public interface BasePlatform {
    /**
     * 平台code
     *
     * @return Platform.getCode()
     */
    String getPlatformCode();

    /**
     * 获取直播地址
     * @param urls
     * @param rid 房间号
     */
    void getRealUrl(Map<String, String> urls, String rid);

    /**
     * 获取房间信息
     * @param roomId 房间号
     * @return
     */
    LiveRoomInfo getRoomInfo(String roomId);

    /**
     * 获取推荐
     * @param page 页数
     * @param size 分页大小
     * @return
     */
    List<LiveRoomInfo> getRecommend(int page, int size);

    /**
     * 获取平台的分区列表, 最好按热度降序排序
     * @return
     */
    List<AreaInfo> getAreaList();

    /**
     * 获取分区房间列表
     * @param area 分区名
     * @param page 页数
     * @param size 分页大小
     * @return
     */
    List<LiveRoomInfo> getAreaRoom(String area, int page, int size);

    /**
     * 搜索
     *
     * @param keyWords 关键词
     * @return
     */
    List<Owner> search(String keyWords);
}
