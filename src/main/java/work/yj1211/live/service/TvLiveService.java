package work.yj1211.live.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import work.yj1211.live.utils.Global;
import work.yj1211.live.vo.TV;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

@Service
public class TvLiveService {

    private static Logger logger = LoggerFactory.getLogger(TvLiveService.class);

    /**
     * 刷新M3U列表
     */
    public void refreshM3U() {
        List<TV> m3uReultTemp = readTxtFile(Global.getM3uPath());
        Global.m3uResult = m3uReultTemp;
    }

    private List<TV> readTxtFile(String filePath) {
        List<TV> result = new ArrayList<>();
        try {
            String encoding = "UTF-8";
            File file = new File(filePath);
            // 判断文件是否存在
            if (file.isFile() && file.exists()) {
                // 考虑到编码格式
                BufferedReader bufferedReader = null;
                try (InputStreamReader read = new InputStreamReader(
                        new FileInputStream(file), encoding)) {
                    bufferedReader = new BufferedReader(read);
                }
                String lineTxt = null;
                TV tv;
                while ((lineTxt = bufferedReader.readLine()) != null) {
                    tv = new TV();
                    tv.setName(lineTxt);
                    tv.setUrl(bufferedReader.readLine());
                    result.add(tv);
                }
            } else {
                logger.error("TvLiveService >>>>>>>> readTxtFile 找不到指定的文件");
            }
        } catch (Exception e) {
            logger.error("TvLiveService >>>>>>>> readTxtFile 读取文件内容出错,错误信息为： {}", e.getMessage());
        }
        return result;
    }
}
