package work.yj1211.live.utils.platForms;
import org.junit.jupiter.api.Test;
import java.util.HashMap;
import java.util.Map;

class HuyaTest {
    @Test
    void getRealUrl() {
        Map<String, String> map = new HashMap<>();
        Huya.getRealUrl(map, "667812");
        System.out.println(map);
    }
}