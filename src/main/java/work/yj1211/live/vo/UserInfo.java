package work.yj1211.live.vo;

import lombok.Data;

import java.io.Serializable;

@Data
public class UserInfo implements Serializable {
    private String uid;
    private String userName;
    private String nickName;
    private String password;
    private String head;
}
