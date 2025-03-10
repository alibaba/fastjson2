package com.alibaba.fastjson2.issues_3300;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONReader;
import lombok.Getter;
import org.junit.jupiter.api.Test;

import java.beans.Transient;
import java.util.ArrayList;
import java.util.List;

public class Issue3367 {
    @Test
    public void test() {
        String strTmp = "{\"bornServerId\":123}";

        PlayerData playerData = JSON.parseObject(strTmp, PlayerData.class, JSONReader.Feature.FieldBased);

        System.out.println(playerData.getBornServerId());
    }

    public static class TDeviceInfo {
    }

    public static class BiDeviceInfo {
    }

    public static class PlayerData {
        public static final String dbName = "player_data";

        @Getter
        private long platformID;

        @Getter
        private String fakePlatformID = "null";

        @Getter
        private String accountName = "null";

        @Getter
        private String mediaChannel = "null";

        @Getter
        private String gameChannel = "null";

        @Getter
        private int platId = -1;

        @Getter
        private int bornServerId;

        @Getter
        private int loginServerId;

        @Getter
        private long openServerTime;

        @Getter
        private String clientVersion = "null";
        /**
         * 设备id
         */
        @Getter
        private String deviceUDID = "null";

        /**
         * sdkToken
         */
        @Getter
        private String sdkToken = "null";

        @Getter
        private String sdkVersion = "null";

        @Getter
        private String fcmToken = "null";

        @Getter
        private String channelOpenid = "null";

        @Getter
        private TDeviceInfo tDeviceInfo = new TDeviceInfo();

        @Getter
        private BiDeviceInfo biDeviceInfo = new BiDeviceInfo();

        @Getter
        private int regChannel;

        @Getter
        private int loginChannel;

        @Getter
        private String nickName;

        @Getter
        private String headIcon;

        /**
         * 性别
         */
        @Getter
        private int gender;

        @Getter
        private int level;

        @Getter
        private int exp;

        /**
         * 模型的id
         */
        @Getter
        private int modelId;

        @Getter
        private String sdkHeadUrl;

        @Getter
        private TDeviceInfo moneyData = new TDeviceInfo();

        @Getter
        private String sign;

        @Getter
        private TDeviceInfo headFrame = new TDeviceInfo();

        @Getter
        private TDeviceInfo playerTitle = new TDeviceInfo();

        /**
         * 当前显示的看板娘资源列表
         */
        @Getter
        private List<Integer> showMascotItemList = new ArrayList<>();

        /**
         * 角色创建时间
         * currentTimeSeconds
         */
        @Getter
        private long createTime;
        /**
         * 角色上次登录时间  只是记录从数据库加载的时间
         */
        @Getter
        private long lastLoginTime;

        /**
         * 最后一一次退出的时间
         */
        @Getter
        private long lastLogoutTime;
        /**
         * 角色上次存储时间
         */
        @Getter
        private long lastSaveTime;
        /**
         * 角色玩游戏总时长，秒
         */
        @Getter
        private long totalGameTime;

        /**
         * 玩家登陆的天数  在线夸天也算  统一按5点来算
         */
        @Getter
        private int loginDayNum;
        /**
         * 玩家登陆了几天  未登录的自然日不算 统一按5点来算
         */
        @Getter
        private int loginDayNumPlayer;
        /**
         * 是不是第一次创建角色
         */
        @Getter
        private boolean firstCreateState;

        @Getter
        private List<Integer> items = new ArrayList<>();

        @Getter
        private List<Integer> missionData = new ArrayList<>();

        /**
         * 头像框列表
         */
        @Getter
        private List<Integer> headFrameList = new ArrayList<>();

        /**
         * 称号列表
         */
        @Getter
        private List<Integer> playerTitleList = new ArrayList<>();

        @Getter
        private List<Integer> headIconList = new ArrayList<>();

        @Getter
        private List<Integer> selfDbKey;

        /**
         * 节点列表
         */

        @Getter
        private TDeviceInfo chapterData = new TDeviceInfo();

        @Transient
        public boolean hasName() {
            return !(nickName == null || nickName.isEmpty());
        }

        @Getter
        private int cancelType;

        @Getter
        private long calmEndTime;

        /**
         * 功能解锁
         */
        @Getter
        private List<TDeviceInfo> functionUnlock = new ArrayList<>();

        /**
         * 挂机数据
         */
        @Getter
        private TDeviceInfo hookData = new TDeviceInfo();

        /**
         * 玩家拥有技能背包信息
         */
        @Getter
        private List<TDeviceInfo> skills = new ArrayList<>();

        /**
         * 装配技能槽，技能ID数组，默认为0
         */
        @Getter
        private int[] skillSlots = new int[6];

        /**
         * 抽卡数据
         */
        @Getter
        private TDeviceInfo gachaData = new TDeviceInfo();

        /**
         * 玩家邮件列表
         */
        @Getter
        private List<TDeviceInfo> mailList = new ArrayList<>();

        /**
         * 个人全局的邮件信息
         */
        @Getter
        private List<TDeviceInfo> globalActivityMailData;

        /**
         * 已经处理过最大的 全局邮件的id
         */
        @Getter
        private long mailGlobalMaxId;

        /**
         * 系统邮件
         */
        @Getter
        private long mailGlobalSystemMaxId;

        /**
         * 配置文件固定的时间点的 事件
         */
        @Getter
        private List<TDeviceInfo> fixationEventDataList = new ArrayList<>();

        /**
         * 自定义到点时间 执行。 执行完结束 的事件
         */
        @Getter
        private List<TDeviceInfo> customEventDataList = new ArrayList<>();

        /**
         * 时装数据
         */
        @Getter
        private TDeviceInfo dressData = new TDeviceInfo();

        /**
         * 新手引导的存储信息
         */
        @Getter
        private TDeviceInfo newGuideData;

        /**
         * 签到
         */
        @Getter
        private TDeviceInfo signInToRewardData;

        /**
         * 七日任务的
         */
        @Getter
        private TDeviceInfo sevenDayTaskData;

        /**
         * 商城
         */
        @Getter
        private TDeviceInfo storeData = new TDeviceInfo();

        /**
         * 推送列表
         */
        @Getter
        private List<String> fcmPushKeyList = new ArrayList<>();

        /**
         * 设置相关数据
         */
        @Getter
        private TDeviceInfo settingData = new TDeviceInfo();

        /**
         * 首充数据
         */
        @Getter
        private List<TDeviceInfo> firstRechargeGiftDataList = new ArrayList<>();

        /**
         * 充值钻石数据
         */
        @Getter
        private List<TDeviceInfo> paymentDataList = new ArrayList<>();

        /**
         * 好友邀请数据
         */
        @Getter
        private TDeviceInfo inviteFriendData = new TDeviceInfo();

        /**
         * 主线任务id列表
         */
        @Getter
        private List<Integer> mainTaskTids = new ArrayList<>();

        /**
         * 日任务id列表
         */
        @Getter
        private List<Integer> dailyTaskTids = new ArrayList<>();

        /**
         * 日任务宝箱
         */
        @Getter
        private TDeviceInfo dailyBox;

        /**
         * 日任务id列表
         */
        @Getter
        private List<Integer> weekTaskTids = new ArrayList<>();

        /**
         * 周任务宝箱
         */
        @Getter
        private TDeviceInfo weekBox;

        /**
         * 战令
         */
        @Getter
        private TDeviceInfo battlePassData;

        /**
         * 月卡
         */
        @Getter
        private TDeviceInfo monthCardData;

        /**
         * 活动时间数据
         */
        @Getter
        private List<TDeviceInfo> activityTimeDataList = new ArrayList<>();

        /**
         * 召喚盛典活动
         */

        @Getter
        private TDeviceInfo activityLotteryData = new TDeviceInfo();

        /**
         * 排行榜 最后一次上传的分数 （同分不上传）
         * 和排行榜类型一一对应
         */
        @Getter
        private List<Double> lastRankScore = new ArrayList<>();

        /**
         * 活动列表
         */
        @Getter
        private TDeviceInfo activityListData = new TDeviceInfo();

        /**
         * 问卷
         */
        @Getter
        private List<TDeviceInfo> questDataList = new ArrayList<>();

        /**
         * 玩家身上的公会数据
         */
        @Getter
        private TDeviceInfo playerGuildData = new TDeviceInfo();

        /**
         * 好友的记录信息
         */
        @Getter
        private TDeviceInfo friendData;

        /**
         * 已经使用的CDKEY列表
         */
        @Getter
        private List<TDeviceInfo> useCDKeyList = new ArrayList<>();

        /**
         * 玩家可上线领取的体力
         */
        @Getter
        private List<TDeviceInfo> powerRewards = new ArrayList<>();
        /**
         * 玩家最后可上线领取的体力时间
         */
        @Getter
        private long lastPowerRewardTime;
        /**
         * 玩家最后可上线领取的体力位于每日刷新时间的索引
         */
        @Getter
        private int lastPowerRewardTimeIndex;

        /**
         * 体力购买次数
         */
        @Getter
        private TDeviceInfo playerPhysicalBuyData = new TDeviceInfo();

        /**
         * 玩家拥有装备背包信息
         */
        @Getter
        private List<TDeviceInfo> equips = new ArrayList<>();
        /**
         * 装备预设方案（6套）
         */
        @Getter
        private TDeviceInfo[] equipSchemeArr = new TDeviceInfo[6];

        /**
         * 玩家拥有宝石 背包
         */
        @Getter
        private List<TDeviceInfo> gemDatas = new ArrayList<>();
        /**
         * 宝石组
         */
        @Getter
        private List<TDeviceInfo> gemGroupData = new ArrayList<>();

        /**
         * 点击消失的红点
         */
        @Getter
        private List<TDeviceInfo> redDotDataList = new ArrayList<>();

        /**
         * 玩家身上的武器数据
         */
        @Getter
        private TDeviceInfo playerWeaponData = new TDeviceInfo();

        /**
         * 玩家拥有的战姬
         */
        @Getter
        private List<TDeviceInfo> heroData = new ArrayList<>();

        /**
         * 战姬亲密数据集
         */
        @Getter
        private List<TDeviceInfo> heroIntimacyDataList = new ArrayList<>();

        /**
         * 玩家拥有的战姬皮肤
         */
        @Getter
        private List<TDeviceInfo> heroSkinData = new ArrayList<>();

        /**
         * 出战的战姬
         */
        @Getter
        private int fightHeroTid;

        /**
         * 任务数据
         */
        @Getter
        private List<TDeviceInfo> taskData = new ArrayList<>();

        /**
         * 看板娘资源列表
         */
        @Getter
        private List<Integer> mascotItemList = new ArrayList<>();

        /**
         * 已完成的触发器id
         */
        @Getter
        private List<Integer> finishedTriggerTids = new ArrayList<>();

        /**
         * 已经触发 但是条件未完成的触发器表格id
         */
        @Getter
        private List<Integer> waitTriggerTids = new ArrayList<>();

        /**
         * 礼包购买次数
         */
        @Getter
        private List<TDeviceInfo> limitedGiftBuyCountList = new ArrayList<>();

        /**
         * 当前激活的礼包列表
         */
        @Getter
        private List<TDeviceInfo> limitedGiftDataList = new ArrayList<>();

        /**
         * 已完成的敲门砖礼包组
         */
        @Getter
        private List<Integer> finishDoorGiftGroupList = new ArrayList<>();

        /**
         * 当前激活的敲门砖礼包列表
         */
        @Getter
        private List<TDeviceInfo> doorGiftGroupDataList = new ArrayList<>();

        /**
         * 梦境记忆副本信息
         */
        @Getter
        private List<TDeviceInfo> heroDreamMemoryDataList = new ArrayList<>();

        /**
         * 商店数据
         */
        @Getter
        private List<TDeviceInfo> shopDataList = new ArrayList<>();

        /**
         * 剧情数据
         */
        @Getter
        private List<TDeviceInfo> storyDataList = new ArrayList<>();

        /**
         * 战姬诊疗室数据
         */
        @Getter
        private List<TDeviceInfo> heroClinicDataList = new ArrayList<>();

        /**
         * 战姬诊疗室数据
         */
        @Getter
        private List<TDeviceInfo> heroClinicDataList1 = new ArrayList<>();

        /**
         * 战姬诊疗室数据
         */
        @Getter
        private List<TDeviceInfo> heroClinicDataList2 = new ArrayList<>();

        /**
         * 战姬诊疗室数据
         */
        @Getter
        private List<TDeviceInfo> heroClinicDataList3 = new ArrayList<>();

        /**
         * 战姬诊疗室数据
         */
        @Getter
        private List<TDeviceInfo> heroClinicDataList4 = new ArrayList<>();

        /**
         * 战姬诊疗室数据
         */
        @Getter
        private List<TDeviceInfo> heroClinicDataList5 = new ArrayList<>();

        /**
         * 战姬诊疗室数据
         */
        @Getter
        private List<TDeviceInfo> heroClinicDataList6 = new ArrayList<>();

        /**
         * 战姬诊疗室数据
         */
        @Getter
        private List<TDeviceInfo> heroClinicDataList7 = new ArrayList<>();

        /**
         * 战姬诊疗室数据
         */
        @Getter
        private List<TDeviceInfo> heroClinicDataList8 = new ArrayList<>();

        /**
         * 战姬诊疗室数据
         */
        @Getter
        private List<TDeviceInfo> heroClinicDataList9 = new ArrayList<>();

        /**
         * 战姬诊疗室数据
         */
        @Getter
        private List<TDeviceInfo> heroClinicDataList10 = new ArrayList<>();
    }
}
