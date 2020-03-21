package script;

import gui.MainGui;

/**
 * 处理弹幕的类
 */
public class ChatUtil {
	/**
	 * 处理弹幕
	 * 
	 * @param chat 弹幕实体类对象
	 * @throws InterruptedException
	 */
	public static void utilChat(Chat chat) throws InterruptedException {
		if (Config.public_config.TO_SBC) {
			MainGui.getInstance().initProgressBar(chat.getCount());
			chat.to_sbc();
			MainGui.getInstance().progressFinish();
		}
		if (Config.public_config.IGNORE_SPACES) {
			MainGui.getInstance().initProgressBar(chat.getCount());
			chat.trim();
			MainGui.getInstance().progressFinish();
		}
		if (Config.public_config.SPLIT_CHATS) {
			MainGui.getInstance().initProgressBar(chat.getCount());
			chat.split_chats(Config.public_config.IGNORE_CASES);
			MainGui.getInstance().progressFinish();
		}
		if (Config.public_config.ADVANCED_MATCH) {
			MainGui.getInstance().initProgressBar(
					Config.ADVANCED_MATCH_SET.length);
			chat.advanced_match(Config.public_config.IGNORE_CASES, Config.ADVANCED_MATCH_SET);
			MainGui.getInstance().progressFinish();
		}
		if (Config.public_config.MARK_ONCE) {
			MainGui.getInstance().initProgressBar(chat.getCount());
			chat.mark_once(Config.public_config.IGNORE_CASES);
			MainGui.getInstance().progressFinish();
		}
	}
}
