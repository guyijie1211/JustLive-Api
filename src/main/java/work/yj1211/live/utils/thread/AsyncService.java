package work.yj1211.live.utils.thread;

import work.yj1211.live.vo.LiveRoomInfo;
import work.yj1211.live.vo.SimpleRoomInfo;

import java.util.List;
import java.util.concurrent.CountDownLatch;

public interface AsyncService {
    /**
     *  执行异步任务
     */
    void addRoomInfo(String uid, String platForm, String roomId, CountDownLatch countDownLatch, List<LiveRoomInfo> roomList);
}
