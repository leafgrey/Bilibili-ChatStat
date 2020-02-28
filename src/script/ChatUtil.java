package script;

import gui.MainGui;

public class ChatUtil {
	public static void utilChat(Chat chat) {
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
			MainGui.getInstance().initProgressBar(Config.ADVANCED_MATCH_SET.length);
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
