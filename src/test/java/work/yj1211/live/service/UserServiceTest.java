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
class UserServiceTest {

    @Autowired
    private UserService userService;

    @Test
    void followRoom() {
        userService.followRoom("douyu", "71415", "0eb26a33e68d4582858a74abf5a645d5");
    }
}