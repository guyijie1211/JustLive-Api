package work.yj1211.live.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import work.yj1211.live.mapper.RoomMapper;
import work.yj1211.live.utils.platForms.*;
import work.yj1211.live.utils.thread.AsyncService;
import work.yj1211.live.vo.LiveRoomInfo;

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
            if ("bilibili".equals(platForm)){
                roomInfo = bilibili.getRoomInfo(roomId);
            }
            if ("douyu".equals(platForm)){
                roomInfo = douyu.getRoomInfo(roomId);
            }
            if ("huya".equals(platForm)){
                roomInfo = huya.getRoomInfo(roomId);
            }
            if ("cc".equals(platForm)){
                roomInfo = cc.getRoomInfo(roomId);
            }
            roomList.add(roomInfo);
        } catch (Exception e) {

        } finally {
            countDownLatch.countDown();
        }
    }
}
