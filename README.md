# JustLive

直播聚合（后端项目）

Android项目 [JustLive-Android](https://github.com/guyijie1211/JustLive-Android)

前端Web项目    [JustLive-Web](https://github.com/guyijie1211/JustLive-Web)

网站页面    [live.yj1211.work](live.yj1211.work)

数据库结构    [mixlive.sql](https://github.com/guyijie1211/MixLive/blob/master/mixlive.sql)

## 部署方式
- 修改 配置文件(src/main/resources/application-github.properties) 中的数据库信息
- 运行 src/main/java/work/yj1211/live/LiveApplication.java 启动

## 直播支持

虎牙、斗鱼、BILIBILI直播、网易cc（cc暂无清晰度切换）、企鹅电竞

直播源获取参考	[wbt5/real-url](https://github.com/wbt5/real-url)

## 接口说明
（仅供参考，接口已经更新过多次，实际代码可能和以下说明不符）

直播相关接口都采用 HTTP GET 方法请求

|          请求地址           |                       参数                       |                           接口说明                           |
| :-------------------------: | :----------------------------------------------: | :----------------------------------------------------------: |
|        /getRecommend        |                int page, int size                | 根据分页信息获取**所有支持直播平台**的推荐直播间（根据观看人数降序排序） |
|   /getRecommendByPlatform   |       String platform, int page, int size        | 根据分页信息获取**指定直播平台**的推荐直播间（根据观看人数降序排序） |
| /getRecommendByPlatformArea | String platform, String area, int page, int size |      获取指定直播平台下特定分区（area）的推荐直播间信息      |
|   /getRecommendByAreaAll    |      String areaType, String area, int page      |   获取特定分区下所有直播平台的直播间信息（一页10个直播间）   |
|         /getRealUrl         |          String platform, String roomId          | 获取指定平台直播间的真实直播推流地址 |
|        /getRoomInfo         |    String uid, String platform, String roomId    | 获取指定平台直播间的房间信息（uid用来确定改用户是否已关注次直播间） |
|         /getRoomsOn         |                    String uid                    |                 获取用户所有关注的直播间信息                 |
|        /refreshArea         |                        无                        |                 更新缓存中所有平台的分区信息                 |
|          /getAreas          |                 String platform                  |                   获取指定平台下的分区信息                   |
|        /getAllAreas         |                        无                        |                    获取所有平台的分区信息                    |
|           /search           | String platform, String keyWords, String isLive  | 搜索指定平台的主播（keyWords：搜索关键词，isLive：搜索正在直播的主播） |

## 调用方式

支持通过Http和Https请求调用，Http请求请发送至8013端口，Https请求请发送至8014端口。

调用时请在uri中加上/api/live

Https请求调用示例：

![image-20210524170329627](https://typora-pic-yj.oss-cn-shanghai.aliyuncs.com/img/image-20210524170329627.png)

调用成功时，返回信息中code为200。

调用失败时，返回code为400。
