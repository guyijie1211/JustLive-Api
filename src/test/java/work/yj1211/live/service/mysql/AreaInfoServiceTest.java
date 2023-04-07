package work.yj1211.live.service.mysql;

import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import work.yj1211.live.LiveApplication;
import work.yj1211.live.model.platformArea.AreaInfo;
import work.yj1211.live.service.AreaService;

/**
 * @author guyijie
 * @date 2023/4/7 12:15
 **/
@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest(classes = LiveApplication.class)
class AreaInfoServiceTest {
    @Autowired
    private AreaInfoService areaInfoService;

    @Test
    void saveOrUpdate() {
        AreaInfo areaInfo = new AreaInfo();
        areaInfo.setAreaId("86");
        areaInfo.setAreaType("2");
        areaInfo.setPlatform("bilibili");
        areaInfo.setTypeName("网游");
        areaInfo.setAreaName("英雄联盟Test");
//        areaInfoService.save()
    }
}