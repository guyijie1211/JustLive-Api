package work.yj1211.live.service;

import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import work.yj1211.live.LiveApplication;

import static org.junit.jupiter.api.Assertions.*;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest(classes = LiveApplication.class)
class LiveRoomServiceTest {

    @Autowired
    private LiveRoomService liveRoomService;

    @Test
    void getRoomsByUid() {
        Long start = System.currentTimeMillis();
        liveRoomService.getRoomsByUid("4ee7d7afa8844db2997bf446c16a1359");
        System.out.println(System.currentTimeMillis() - start);
    }
}