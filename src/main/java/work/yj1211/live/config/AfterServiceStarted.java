package work.yj1211.live.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;
import work.yj1211.live.service.LiveRoomService;

@Component
public class AfterServiceStarted implements ApplicationRunner {

    @Autowired
    private LiveRoomService liveRoomService;

    /**
     * 会在服务启动完成后立即执行
     */
    @Override
    public void run(ApplicationArguments args) throws Exception {
        liveRoomService.refreshArea();
    }
}
