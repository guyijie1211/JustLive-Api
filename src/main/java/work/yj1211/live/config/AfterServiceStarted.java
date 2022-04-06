package work.yj1211.live.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;
import work.yj1211.live.service.LiveRoomService;
import work.yj1211.live.service.TvLiveService;

@Component
public class AfterServiceStarted implements ApplicationRunner {

    @Autowired
    private LiveRoomService liveRoomService;
    @Autowired
    private TvLiveService tvLiveService;

    /**
     * 会在服务启动完成后立即执行
     */
    @Override
    public void run(ApplicationArguments args){
        liveRoomService.refreshArea();
        liveRoomService.refreshUpdate();
        //tvLiveService.refreshM3U();
    }
}
