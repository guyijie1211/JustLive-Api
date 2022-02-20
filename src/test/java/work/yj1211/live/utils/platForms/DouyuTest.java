package work.yj1211.live.utils.platForms;

import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.junit4.SpringRunner;
import work.yj1211.live.service.LiveRoomService;
import work.yj1211.live.vo.LiveRoomInfo;
import work.yj1211.live.vo.Owner;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class DouyuTest {
    @Test
    void get_real_url() {
        Map<String, String> map = new HashMap<>();
        Douyu.get_real_url(map, "92000");
        System.out.println(111);
    }

    @Test
    void getRealRoom(){
        System.out.println(Douyu.getRealRoomId("71415"));
    }

    @Test
    void search() {
        List<Owner> list = Douyu.search("92000", "1");
        System.out.println(list);
    }
}