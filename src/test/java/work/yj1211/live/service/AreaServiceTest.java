package work.yj1211.live.service;

import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import work.yj1211.live.LiveApplication;
import work.yj1211.live.model.platformArea.AreaInfo;
import work.yj1211.live.model.platformArea.AreaInfoIndex;
import work.yj1211.live.service.platforms.impl.Douyu;
import work.yj1211.live.service.platforms.impl.Huya;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @Author: guyijie1211
 * @Data:2023/4/7 21:46
 */
@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest(classes = LiveApplication.class)
class AreaServiceTest {
    @Autowired
    private AreaService areaService;
    @Autowired
    private Douyu douyu;
    @Autowired
    private Huya huya;

    @Test
    void saveOrUpdateBatchByPlatform() {
        areaService.saveOrUpdateBatchByPlatform(douyu.getAreaList(), douyu.getPlatformName());
    }

    @Test
    void refreshAreasAll() {
        long start = System.currentTimeMillis();
        areaService.refreshAreasAll();
        long end = System.currentTimeMillis();
        System.out.println("===========" + (end-start));
    }

    @Test
    void getAreasAll() {
        long start = System.currentTimeMillis();
        List<List<AreaInfoIndex>> result = areaService.getAllAreas();
        long end = System.currentTimeMillis();
        System.out.println("===========" + (end-start));
    }
}