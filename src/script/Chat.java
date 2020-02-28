package script;

import java.util.ArrayList;

import gui.MainGui;

/**
 * 弹幕的实体类
 */
public class Chat {
	private ArrayList<String> chats;
	private ArrayList<String> users;
	private ArrayList<Float> time;
	private ArrayList<Long> date;

	/**
	 * 创建弹幕
	 * 
	 * @param chats 弹幕
	 * @param users 用户
	 * @param time  发送时间（指位于视频的什么时候）
	 * @param date  发送时间（时间戳）
	 */
	public Chat(ArrayList<String> chats, ArrayList<String> users, ArrayList<Float> time, ArrayList<Long> date) {
		this.chats = chats;
		this.users = users;
		this.time = time;
		this.date = date;
	}

	/**
	 * 获取弹幕列表
	 * 
	 * @return 弹幕列表
	 */
	public ArrayList<String> getChats() {
		return chats;
	}

	/**
	 * 设置弹幕列表
	 * 
	 * @param chats 弹幕列表
	 */
	public void setChats(ArrayList<String> chats) {
		this.chats = chats;
	}

	/**
	 * 获取用户列表
	 * 
	 * @return 用户列表
	 */
	public ArrayList<String> getUsers() {
		return users;
	}

	/**
	 * 设置用户列表
	 * 
	 * @return 用户列表
	 */
	public void setUsers(ArrayList<String> users) {
		this.users = users;
	}

	/**
	 * 获取时间列表
	 * 
	 * @return 时间列表
	 */
	public ArrayList<Float> getTime() {
		return time;
	}

	/**
	 * 设置时间列表
	 * 
	 * @return 时间列表
	 */
	public void setTime(ArrayList<Float> time) {
		this.time = time;
	}

	/**
	 * 获取日期列表
	 * 
	 * @return 日期列表
	 */
	public ArrayList<Long> getDate() {
		return date;
	}

	/**
	 * 设置日期列表
	 * 
	 * @return 日期列表
	 */
	public void setDate(ArrayList<Long> date) {
		this.date = date;
	}

	/**
	 * 追加新的弹幕对象到原对象
	 * 
	 * @param new_chat 新弹幕
	 */
	public void append(Chat new_chat) {
		chats.addAll(new_chat.getChats());
		users.addAll(new_chat.getUsers());
		time.addAll(new_chat.getTime());
		date.addAll(new_chat.getDate());
	}

	/**
	 * 追加新的弹幕对象到原对象
	 * 
	 * @param chat  新弹幕
	 * @param user  新用户
	 * @param time_ 新时间
	 * @param date_ 新日期
	 */
	public void append(String chat, String user, Float time_, Long date_) {
		chats.add(chat);
		users.add(user);
		time.add(time_);
		date.add(date_);
	}

	/**
	 * 删除首尾字符
	 */
	public void trim() {
		for (int i = 0; i < chats.size(); i++) {
			MainGui.getInstance().refreshProgressBar(i);
			chats.set(i, chats.get(i).trim());
		}
	}

	/**
	 * 转换为全角字符
	 */
	public void to_sbc() {
		for (int i = 0; i < chats.size(); i++) {
			MainGui.getInstance().refreshProgressBar(i);
			char[] c = chats.get(i).toCharArray();
			for (int j = 0; j < c.length; j++) {
				if ((c[j] >= 33 && c[j] <= 47) || (c[j] >= 58 && c[j] <= 64) || (c[j] >= 91 && c[j] <= 96)
						|| (c[j] >= 123 && c[j] <= 126)) {
					c[j] = (char) (c[j] + 65248);
				}
			}
			chats.set(i, new String(c));
		}
	}

	/**
	 * 弹幕拆分
	 * 
	 * @param ignore_cases 是否忽略大小写
	 */
	public void split_chats(boolean ignore_cases) {
		outter: for (int i = 0; i < chats.size(); i++) {
			MainGui.getInstance().refreshProgressBar(i);
			// 试图删除空格
			String chat = chats.get(i).replaceAll(" ", "");
			if (ignore_cases) {
				chat.toLowerCase();
			}
			char[] chars = chat.toCharArray();
			tick: for (int j = 1; j < chat.length(); j++) {
				if (chars.length % j != 0) {
					continue tick;
				}
				for (int k = 0; k < chars.length / j - 1; k++) {
					for (int offset = 0; offset < j; offset++) {
						if (!(chars[offset] == chars[offset + j * (k + 1)])) {
							continue tick;
						}
					}
				}
				StringBuilder builder = new StringBuilder();
				for (int l = 0; l < j; l++) {
					builder.append(chars[l]);
				}
				chats.set(i, builder.toString());
				continue outter;
			}
		}
	}

	/**
	 * 高级弹幕合并
	 * 
	 * @param ignore_cases 是否忽略大小写
	 * @param set          合并规则库
	 */
	public void advanced_match(boolean ignore_cases, String[][] set) {
		for (int i = 0; i < set.length; i++) {
			MainGui.getInstance().refreshProgressBar(i);
			for (int j = 0; j < chats.size(); j++) {
				String text;
				if (ignore_cases) {
					text = chats.get(j).toLowerCase();
				} else {
					text = chats.get(j);
				}
				chats.set(j, text.replaceAll(set[i][0], set[i][1]));

			}
		}
	}

	/**
	 * 对于一个人发的多条相同弹幕只保留一条
	 * 
	 * @param ignore_cases 是否忽略大小写
	 */
	public void mark_once(boolean ignore_cases) {
		ArrayList<String> chats_temp = new ArrayList<>();
		ArrayList<String> users_temp = new ArrayList<>();
		ArrayList<Float> time_temp = new ArrayList<>();
		ArrayList<Long> date_temp = new ArrayList<>();
		for (int i = 0; i < chats.size(); i++) {
			chats_temp.add(chats.get(i));
			users_temp.add(users.get(i));
			time_temp.add(time.get(i));
			date_temp.add(date.get(i));
		}
		for (int i = 0; i < chats.size(); i++) {
			MainGui.getInstance().refreshProgressBar(i);
			String user = users.get(i);
			for (int j = i + 1; j < chats.size(); j++) {
				if (user.equals(users.get(j))) {
					if (ignore_cases) {
						if (chats.get(i).toLowerCase().equals(chats.get(j).toLowerCase())) {
							chats_temp.set(j, "");
						}
					} else {
						if (chats.get(i).equals(chats.get(j))) {
							chats_temp.set(j, "");
						}
					}
				}
			}
		}
		chats.clear();
		users.clear();
		time.clear();
		date.clear();
		for (int i = 0; i < chats_temp.size(); i++) {
			if (!chats_temp.get(i).equals("")) {
				chats.add(chats_temp.get(i));
				users.add(users_temp.get(i));
				time.add(time_temp.get(i));
				date.add(date_temp.get(i));
			}
		}
	}

	/**
	 * 获取弹幕数量
	 * 
	 * @return 数量
	 */
	public int getCount() {
		return chats.size();
	}
}
