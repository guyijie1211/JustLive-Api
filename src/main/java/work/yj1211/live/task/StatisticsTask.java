package work.yj1211.live.task;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import work.yj1211.live.service.AreaService;
import work.yj1211.live.service.UserService;

/**
 * @author guyijie1211
 * @date 2023/3/22 17:27
 **/
@Component
@EnableScheduling
@EnableAsync
public class StatisticsTask {
    @Autowired
    private UserService userService;
    @Autowired
    private AreaService areaService;

    /**
     * 统计前一天活跃用户
     * 每天0点执行
     */
    @Async
    @Scheduled(cron = "0 0 0 * * ?")
    public void insertActiveUserNum() {
        userService.insertActiveUserNum();
    }

    /**
     * 刷新所有平台的分区映射
     * 每天凌晨5点执行
     */
    @Async
    @Scheduled(cron = "0 0 5 * * ?")
    public void refreshAllAreas() {
        areaService.refreshAreasAll();
    }
}
