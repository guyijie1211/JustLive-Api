package work.yj1211.live.utils.platForms;

import org.springframework.stereotype.Component;
import work.yj1211.live.model.LiveRoomInfo;
import work.yj1211.live.model.Owner;

import java.util.List;
import java.util.Map;

/**
 * @author guyijie
 * @date 2023/3/27 15:10
 **/
@Component
public interface BasePlatform {

    /**
     * 平台名称
     *
     * @return
     */
    String getType();

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
     * 刷新分区信息
     */
    void refreshArea();

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
