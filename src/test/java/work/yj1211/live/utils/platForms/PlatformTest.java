package work.yj1211.live.utils.platForms;

import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import work.yj1211.live.LiveApplication;
import work.yj1211.live.service.platforms.impl.Bilibili;
import work.yj1211.live.service.platforms.impl.CC;
import work.yj1211.live.service.platforms.impl.Douyu;
import work.yj1211.live.service.platforms.impl.Huya;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest(classes = LiveApplication.class)
class PlatformTest {
    @Autowired
    private Bilibili bilibili;
    @Autowired
    private Douyu douyu;
    @Autowired
    private Huya huya;
    @Autowired
    private CC cc;

}