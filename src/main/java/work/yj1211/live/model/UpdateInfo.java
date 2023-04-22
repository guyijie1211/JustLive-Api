package work.yj1211.live.model;

import lombok.Data;

import java.util.ArrayList;

@Data
public class UpdateInfo{
    private int versionNum;//版本号
    private String latestVersion;//最新版本号
    private String updateUrl;//apk下载地址
    private String apkSize;//apk大小
    private String apkMD5;//更新包md5
    private int importance;//更新重要性 0:小修复,1:新功能,2:大版本
    private ArrayList<String> description;//更新描述

    public UpdateInfo() {}

    public UpdateInfo (UpdateInfo updateInfo) {
        this.versionNum = updateInfo.versionNum;
        this.latestVersion = updateInfo.latestVersion;
        this.updateUrl = updateInfo.updateUrl;
        this.apkSize = updateInfo.apkSize;
        this.apkMD5 = updateInfo.apkMD5;
        this.importance = updateInfo.importance;
        this.description = updateInfo.description;
    }
}
