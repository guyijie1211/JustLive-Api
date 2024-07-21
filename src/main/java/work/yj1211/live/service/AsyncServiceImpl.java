package work.yj1211.live.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import work.yj1211.live.enums.Platform;
import work.yj1211.live.model.platform.LiveRoomInfo;
import work.yj1211.live.service.platforms.BasePlatform;
import work.yj1211.live.service.platforms.impl.Bilibili;
import work.yj1211.live.service.platforms.impl.CC;
import work.yj1211.live.service.platforms.impl.Douyu;
import work.yj1211.live.service.platforms.impl.Huya;
import work.yj1211.live.utils.thread.AsyncService;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class AsyncServiceImpl implements AsyncService {
    private final Map<String, BasePlatform> platformMap;
    @Autowired
    public AsyncServiceImpl(List<BasePlatform> platforms){
        platformMap = platforms.stream().collect(Collectors.toMap(BasePlatform::getPlatformCode, Function.identity(), (oldV, newV)-> newV));
    }

    @Async("asyncServiceExecutor")
    @Override
    public void addRoomInfo(String uid, String platForm, String roomId, CountDownLatch countDownLatch, List<LiveRoomInfo> roomList) {
        try {
            roomList.add(platformMap.get(platForm).getRoomInfo(roomId));
        } catch (Exception e) {

        } finally {
            countDownLatch.countDown();
        }
    }
}
