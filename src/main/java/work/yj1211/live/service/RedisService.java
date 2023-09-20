package work.yj1211.live.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import work.yj1211.live.model.platform.LiveRoomInfo;
import work.yj1211.live.utils.RedisUtils;

@Service
public class RedisService {
    @Autowired
    private RedisUtils redisUtils;

    /**
     * 保存房间信息
     * @param roomInfo
     */
    public void saveRoom(LiveRoomInfo roomInfo) {
        String key = roomInfo.getPlatForm() + roomInfo.getRoomId();
        redisUtils.set(key, roomInfo);
    }

    /**
     * 获取房间
     * @param platform
     * @param roomId
     * @return 如果没有，返回空
     */
    public LiveRoomInfo getRoom(String platform, String roomId) {
        String key = platform + roomId;
        return (LiveRoomInfo) redisUtils.get(key);
    }
}
