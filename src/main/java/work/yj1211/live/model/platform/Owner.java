package work.yj1211.live.model.platform;

import lombok.Data;

@Data
public class Owner implements Comparable<Owner>{
    private String platform;
    private String nickName;
    private String roomId;
    private String headPic;
    private String cateName;
    private String isLive;

    private int followers;
    private int isFollowed; //是否关注此房间

    @Override
    public int compareTo(Owner owner) {
        return owner.followers - this.followers;
    }
}
