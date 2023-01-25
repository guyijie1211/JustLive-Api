package work.yj1211.live.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import work.yj1211.live.utils.Global;
import work.yj1211.live.vo.TV;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
public class TvLiveService {

    /**
     * 刷新M3U列表
     */
    public void refreshM3U() {
        List<TV> m3uReultTemp = readTxtFile(Global.getM3uPath());
        Global.m3uResult = m3uReultTemp;
    }

    private List<TV> readTxtFile(String filePath){
        List<TV> result = new ArrayList<>();
        try {
            String encoding="UTF-8";
            File file=new File(filePath);
            if(file.isFile() && file.exists()){ //判断文件是否存在
                InputStreamReader read = new InputStreamReader(
                        new FileInputStream(file),encoding);//考虑到编码格式
                BufferedReader bufferedReader = new BufferedReader(read);
                String lineTxt = null;
                String txt[];
                TV tv;
                while((lineTxt = bufferedReader.readLine()) != null){
                    tv = new TV();
                    tv.setName(lineTxt);
                    tv.setUrl(bufferedReader.readLine());
                    result.add(tv);
                }
                read.close();
            }else{
                log.error("找不到指定的文件");
            }
        } catch (Exception e) {
            log.error("读取文件内容出错");
            e.printStackTrace();
        }
        return result;
    }
}
