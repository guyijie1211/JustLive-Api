package work.yj1211.live.vo;

import lombok.Data;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

@Data
public class UserInfo implements Serializable {
    private String uid;
    private String userName;
    private String nickName;
    private String password;
    private String head;
    private String isActived;
    private String allContent;
    private String selectedContent;
    private String douyuLevel;
    private String bilibiliLevel;
    private String huyaLevel;
    private String ccLevel;
    private String egameLevel;
    private Date created;
    private Date modified;
    private Date lastLogin;
}
