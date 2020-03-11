package script;

/**
 * 程序配置
 */
public class Config {

	public static String VERSION = "1.2.0";// 版本
	public static boolean ALLOW_MODIFY = true;// 是否允许修改配置

	// 高级弹幕合并，格式为{"正则表达式","替换文字"}，如{"^OH{2,}$","OHHHH"}；也可以在“替换文字”后添加注释。
	// 这是预设的规则库，使用者可以自定义。
	public static String[][] ADVANCED_MATCH_SET = { { "^[Oo][Hh]{2,}$", "OHHHH" }, { "^23{2,}$", "23333" },
			{ "啊+", "啊" }, { "！+", "！" }, { "\\!+", "!" }, { "哇+", "哇" }, { ".*[Aa].*[Ww].*[Ss].*[Ll].*", "awsl" },
			{ ".*啊我死了.*", "awsl" }, { ".*阿伟死了.*", "awsl" }, { ".*阿伟睡了.*", "awsl" }, { ".*啊我是鹿.*", "awsl" },
			{ ".*啊我是驴.*", "awsl" }, { ".*啊我酸了.*", "awsl" }, { ".*啊我生了.*", "awsl" }, { ".*爱我苏联.*", "awsl" },
			{ ".*啊我睡了.*", "awsl" }, { ".*阿伟是鹿.*", "awsl" }, { ".*阿伟是驴.*", "awsl" }, { ".*阿伟酸了.*", "awsl" },
			{ ".*阿伟生了.*", "awsl" }, { ".*爱我苏联.*", "awsl" } };

	public static String CHAT_TAG_IGNORE = "<@ignore>";// 忽略弹幕的标记
	public static String CHAT_TAG_ONLY = "<@only>";// 只统计含该标记的弹幕
	public static String[] IGNORE_CHAT_SET = new String[0];// 该数组中的正则匹配规则将用于去掉弹幕
	public static String[] ONLY_CHAT_SET = new String[0];// 该数组中的正则匹配规则将用于将弹幕加入特殊统计池，只统计该统计池的弹幕

	// 公共配置
	public static class public_config {
		public static boolean IGNORE_CASES = true;// 忽略大小写
		public static boolean TO_SBC = false;// 转换为全角字符（为避免csv格式错误，不管是否开启此选项，半角逗号都会被转换为全角逗号）
		public static boolean IGNORE_SPACES = true;// 删除首尾空格
		public static boolean SPLIT_CHATS = true;// 试图将弹幕拆分为单个片段，如“awsl awsl awsl”将记为“awsl”，“？ ？ ？ ？ ？”将记为“？”
		public static boolean ADVANCED_MATCH = true;// 高级弹幕匹配开关
		public static boolean MARK_ONCE = true;// 一个人发的多条相同弹幕只记一次（“相同弹幕”支持前三个选项，即前三个选项处理完毕后，再判断是否是“相同弹幕”）
		public static int OUTPUT_STYLE = 1;// 输出方式，0为swing表格展示，1为csv文件展示
	}

	// 爬取视频弹幕配置
	public static class spider_config {
		public static int mode = 0;// 模式，0为爬取单视频，1为爬取多视频，2为爬取指定up主的全部视频
		public static int[] avs;// av号列表
		public static int uid = 0;// UP主的UID
	}

	// 直播间抓取弹幕配置
	public static class live_config {
		public static int ROOM = -1;// 直播间
		public static int DELAY = 1000;// 两次爬取弹幕的延时
		public static boolean STATUS = false;// 是否正在执行
		public static boolean AUTO_DELAY = true;// 自动调整延时
		public static long START_TIME = 0;// 开始爬取的时间戳
	}

	// 第一个选项卡的配置
	public static class tab1 {
		public static int RANK_LIMIT = -1;// 参与排名的最大弹幕数量（即展示前多少名），设置为-1即全部排名
	}

	// 第二个选项卡的配置
	public static class tab2 {
		public static float LENGTH = 15; // 采样长度，单位为秒
		public static float START_TIME = 0; // 起始时间，设置为0即从头开始
		public static float END_TIME = -1; // 终止时间，设置为-1即到视频末尾
	}

	// 第三个选项卡的配置
	public static class tab3 {
		public static boolean CRC32 = false; // 是否进行CRC32反算（十分消耗时间，极不推荐）
	}

	// 第六个选项卡的配置
	public static class tab6 {
		public static int LENGTH = 1800; // 采样长度，单位为秒
		public static int START_TIME = 0; // 起始时间，设置为0即从00:00:00开始
		public static int END_TIME = -1; // 终止时间，设置为-1即到一天结束
	}
}
