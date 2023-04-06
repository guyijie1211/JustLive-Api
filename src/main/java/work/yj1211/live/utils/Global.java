package work.yj1211.live.utils;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import work.yj1211.live.model.TV;
import work.yj1211.live.model.UpdateInfo;
import work.yj1211.live.model.platformArea.AreaInfo;

import java.util.*;

//全局变量
@Component
public class Global {
    private static String updateFilePath;
    public static UpdateInfo updateInfo;

    @Value("${path.updateFilePath}")
    public void setUpdateFilePath(String updateFilePath) {
        Global.updateFilePath = updateFilePath;
    }

    public static String getUpdateFilePath() {
        return updateFilePath;
    }
}
