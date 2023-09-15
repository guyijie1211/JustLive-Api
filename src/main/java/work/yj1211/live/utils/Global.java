package work.yj1211.live.utils;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import work.yj1211.live.model.BannerInfo;
import work.yj1211.live.model.TV;
import work.yj1211.live.model.UpdateInfo;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.List;

//全局变量
@Component
public class Global {
    private static String updateFilePath;
    private static String bannerInfoFilePath;
    public static List<TV> m3uResult;
    public static UpdateInfo updateInfo;
    public static List<BannerInfo> updateInfoList;

    @Value("${path.updateFilePath}")
    public void setUpdateFilePath(String updateFilePath) {
        Global.updateFilePath = updateFilePath;
    }

    @Value("${path.bannerInfoFilePath}")
    public void setBannerInfoFilePath(String bannerInfoFilePath) {
        Global.bannerInfoFilePath = bannerInfoFilePath;
    }


    public static String getUpdateFilePath() {
        return updateFilePath;
    }

    public static String getBannerInfoFilePath() {
        return bannerInfoFilePath;
    }

    public static String readTxtFile(String filePath){
        String readResult = "";
        try {
            String encoding="UTF-8";
            File file=new File(filePath);
            if(file.isFile() && file.exists()){ //判断文件是否存在
                InputStreamReader read = new InputStreamReader(
                        new FileInputStream(file),encoding);//考虑到编码格式
                BufferedReader bufferedReader = new BufferedReader(read);
                String lineTxt = null;
                while((lineTxt = bufferedReader.readLine()) != null){
                    readResult = readResult + lineTxt;
                }
                read.close();
            }else{
                readResult = "找不到指定的文件";
            }
        } catch (Exception e) {
            readResult = "读取文件内容出错";
        }

        return readResult;
    }
}
