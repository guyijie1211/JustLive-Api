package work.yj1211.live.service;

import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import work.yj1211.live.LiveApplication;
import work.yj1211.live.mapper.AllRoomsMapper;
import work.yj1211.live.vo.LiveRoomInfo;

import java.util.ArrayList;
import java.util.List;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest(classes = LiveApplication.class)
class AllRoomsServiceTest {
    @Autowired
    private AllRoomsMapper allRoomsMapper;

    @Test
    void updateAllRooms() {
        List<LiveRoomInfo> roomsList = new ArrayList<>();
        for (int i=0; i<100; i++) {
            LiveRoomInfo roomInfo = new LiveRoomInfo();
            roomInfo.setIsFollowed(1);
            roomInfo.setRoomId(String.valueOf(i));
            roomInfo.setPlatForm("douyu");
            roomInfo.setCategoryName("英雄联盟");
            roomInfo.setOnline(999);
            roomInfo.setOwnerName("YJ1211");
            roomInfo.setRoomName("dnwijefneowenmdoiwmd");
            roomsList.add(roomInfo);
        }
        allRoomsMapper.updateRooms(roomsList);
    }

}