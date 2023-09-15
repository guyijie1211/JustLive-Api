package work.yj1211.live.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import work.yj1211.live.enums.Platform;
import work.yj1211.live.model.LiveRoomInfo;
import work.yj1211.live.service.platforms.impl.Bilibili;
import work.yj1211.live.service.platforms.impl.CC;
import work.yj1211.live.service.platforms.impl.Douyu;
import work.yj1211.live.service.platforms.impl.Huya;
import work.yj1211.live.utils.thread.AsyncService;

import java.util.List;
import java.util.concurrent.CountDownLatch;

@Service
public class AsyncServiceImpl implements AsyncService {
    @Autowired
    private Bilibili bilibili;
    @Autowired
    private Douyu douyu;
    @Autowired
    private Huya huya;
    @Autowired
    private CC cc;

    @Async("asyncServiceExecutor")
    @Override
    public void addRoomInfo(String uid, String platForm, String roomId, CountDownLatch countDownLatch, List<LiveRoomInfo> roomList) {
        try {
            LiveRoomInfo roomInfo = null;
            if (Platform.BILIBILI.getCode().equals(platForm)) {
                roomInfo = bilibili.getRoomInfo(roomId);
            }
            if (Platform.DOUYU.getCode().equals(platForm)) {
                roomInfo = douyu.getRoomInfo(roomId);
            }
            if (Platform.HUYA.getCode().equals(platForm)) {
                roomInfo = huya.getRoomInfo(roomId);
            }
            if (Platform.CC.getCode().equals(platForm)) {
                roomInfo = cc.getRoomInfo(roomId);
            }
            roomList.add(roomInfo);
        } catch (Exception e) {

        } finally {
            countDownLatch.countDown();
        }
    }
}
