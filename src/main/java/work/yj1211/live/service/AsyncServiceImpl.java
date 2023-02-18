package work.yj1211.live.service;

import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import work.yj1211.live.mapper.RoomMapper;
import work.yj1211.live.utils.platForms.Bilibili;
import work.yj1211.live.utils.platForms.CC;
import work.yj1211.live.utils.platForms.Douyu;
import work.yj1211.live.utils.platForms.Huya;
import work.yj1211.live.utils.thread.AsyncService;
import work.yj1211.live.vo.LiveRoomInfo;

import java.util.List;
import java.util.concurrent.CountDownLatch;

import static work.yj1211.live.utils.Constant.*;

@Slf4j
@Service
public class AsyncServiceImpl implements AsyncService {

    private static final Logger logger = LoggerFactory.getLogger(AsyncServiceImpl.class);

    @Autowired
    private RoomMapper roomMapper;

    @Autowired
    private Bilibili bilibili;

    @Async("asyncServiceExecutor")
    @Override
    public void addRoomInfo(String uid, String platForm, String roomId, CountDownLatch countDownLatch, List<LiveRoomInfo> roomList) {
        try {
            LiveRoomInfo roomInfo = null;
            switch (platForm) {
                case BILIBILI:
                    roomInfo = bilibili.getSingleRoomInfo(roomId);
                    break;
                case DOU_YU:
                    roomInfo = Douyu.getRoomInfo(roomId);
                    break;
                case HU_YA:
                    roomInfo = Huya.getRoomInfo(roomId);
                    break;
                case WANGYI_CC:
                    roomInfo = CC.getRoomInfo(roomId);
                    break;
                default:
                    roomInfo = null;
            }
            roomList.add(roomInfo);
        } catch (Exception e) {
            logger.error("AsyncServiceImpl.addRoomInfo>>>>>>>>{}", e.getMessage());
        } finally {
            countDownLatch.countDown();
        }
    }
}
