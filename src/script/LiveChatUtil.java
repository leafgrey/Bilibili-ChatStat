package script;

import java.util.ArrayList;
import java.util.Date;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import gui.LivePanel;

public class LiveChatUtil implements Runnable {
	private ArrayList<String> list = new ArrayList<>();
	private static Chat chat;
	private static JSONArray total_json;
	private static ArrayList<Integer> color = null;
	private String[] ct_old;
	private boolean first_run;
	private int[] stat;
	private int stat_index;
	private int cursor = 0;
	private int size;
	private int index = 0;
	private boolean prevent = true;// 为防止各种玄学bug，进入while循环一段时间后将其设置为false

	public LiveChatUtil(int size) {
		this.size = size;
		ct_old = new String[size];
	}

	@Override
	public void run() {
		chat = new Chat(new ArrayList<String>(), new ArrayList<String>(), new ArrayList<Float>(),
				new ArrayList<Long>());
		color = new ArrayList<>();
		total_json = new JSONArray();
		first_run = true;
		stat = new int[10];
		stat_index = 0;
		for (int i = 0; i < stat.length; i++) {
			stat[i] = 0;
		}
		Config.live_config.STATUS = true;
		try {
			Thread.sleep(100);
		} catch (InterruptedException e1) {
			e1.printStackTrace();
		}
		long time1 = new Date().getTime();
		while (Config.live_config.STATUS || prevent) {
			if (prevent) {
				if (new Date().getTime() - time1 > 2000) {
					prevent = false;
				}
			}
			if (index >= list.size() - 1) {
				continue;
			}
			try {
				JSONObject jsonObject = null;
				int buffer = 0;
				int new_chat_count = 0;
				jsonObject = new JSONObject(list.get(index));
				JSONObject data = jsonObject.getJSONObject("data");
				JSONArray room = data.getJSONArray("room");
				LivePanel.getInstance().addTime();
				tick: for (int i = 0; i < room.length(); i++) {
					JSONObject chat_json = room.getJSONObject(i);
					JSONObject check_info = chat_json.getJSONObject("check_info");
					String ct = check_info.getString("ct");
					String text = chat_json.getString("text");
					for (int j = 0; j < ct_old.length; j++) {
						// 判断弹幕是否相等
						if (ct_old[j] == null) {
							continue;
						}
						if (ct.equals(ct_old[j])) {
							buffer++;
							continue tick;
						}
					}
					addCheckInfo(ct);
					Long time = check_info.getLong("ts");
					// 如果弹幕的发送时间早于设定的开始时间，则该弹幕是历史弹幕，不抓取
					if (time * 1000 < Config.live_config.START_TIME) {
						LivePanel.getInstance().log("[历史弹幕，不记录]  " + text);
						continue tick;
					}
					new_chat_count++;
					String text2 = text.replace("<", "&lt;").replace("&", "&amp;");
					// 字体颜色，弹幕转换工具Github上一搜一大堆
					// 暂时只支持这几种，因为开发者翻遍F12也找不到控制弹幕颜色的字段在哪里（泪）
					// 优先级：船员 > 年费老爷 > 月费老爷 > 普通
					color_block: {
						if (chat_json.getInt("guard_level") > 0) {
							color.add(0xe33fff);// 紫色
							break color_block;
						}
						if (chat_json.getInt("svip") == 1) {
							color.add(0x66ccff);// 蓝色（天依色哦，刻在DNA里的RGB号）
							break color_block;
						}
						if (chat_json.getInt("vip") == 1) {
							color.add(0xff6868);// 红色
							break color_block;
						}
						color.add(0xffffff);// 白色
					}
					chat.append(text2, chat_json.getInt("uid") + "",
							(float) (time - Config.live_config.START_TIME / 1000), time);
					total_json.put(chat_json);
					if (first_run) {
						LivePanel.getInstance().log("[缓冲区填充中]  " + text);
					} else if (buffer == 0) {
						LivePanel.getInstance().log("【警告 - 缓冲区" + buffer + "】  " + text);
					} else {
						LivePanel.getInstance().log("[缓冲区" + buffer + "]  " + text);
					}
				}
				if (!first_run && buffer == 0) {
					if (Config.live_config.AUTO_DELAY && !first_run) {
						if (Config.live_config.DELAY >= 4000) {
							Config.live_config.DELAY -= 2000;
							LivePanel.getInstance().log("###  延时已智能调低2000ms  ###");
							LivePanel.getInstance().refreshDelayField();
						} else if (Config.live_config.DELAY >= 2000) {
							Config.live_config.DELAY -= 1000;
							LivePanel.getInstance().log("###  延时已智能调低1000ms  ###");
							LivePanel.getInstance().refreshDelayField();
						} else if (Config.live_config.DELAY >= 1000) {
							Config.live_config.DELAY -= 600;
							LivePanel.getInstance().log("###  延时已智能调低600ms  ###");
							LivePanel.getInstance().refreshDelayField();
						} else if (Config.live_config.DELAY >= 200) {
							if ((!Config.live_config.MULTI_THREAD) || Config.live_config.DELAY >= 50
									|| Config.live_config.I_DO_NOT_FEAR_BANNING) {
								Config.live_config.DELAY -= 200;
								LivePanel.getInstance().log("###  延时已智能调低200ms  ###");
							} else {
								Config.live_config.DELAY = 50;
								LivePanel.getInstance().log("###  延时已智能设置为50ms  ###");
							}
							LivePanel.getInstance().refreshDelayField();
						} else {
							if ((!Config.live_config.MULTI_THREAD) || Config.live_config.DELAY >= 50
									|| Config.live_config.I_DO_NOT_FEAR_BANNING) {
								Config.live_config.DELAY = 0;
								LivePanel.getInstance().log("###  延时已智能设置为0  ###");
							} else if (Config.live_config.DELAY != 50) {
								Config.live_config.DELAY = 50;
								LivePanel.getInstance().log("###  延时已智能设置为50ms  ###");
							}
							LivePanel.getInstance().refreshDelayField();
						}
					}
				}
				if (stat_index != stat.length - 1) {
					stat[stat_index] = buffer;
					stat_index++;
				} else {
					stat[stat_index] = buffer;
					stat_index = 0;
					if (Config.live_config.AUTO_DELAY && !first_run) {
						int sum = 0;
						for (int k = 0; k < stat.length; k++) {
							sum += stat[k];
						}
						double average = (double) sum / stat.length;
						if (average < 4.0) {
							if (Config.live_config.DELAY >= 4000) {
								Config.live_config.DELAY -= 1000;
								LivePanel.getInstance().log("###  延时已智能调低1000ms  ###");
								LivePanel.getInstance().refreshDelayField();
							} else if (Config.live_config.DELAY >= 2000) {
								Config.live_config.DELAY -= 500;
								LivePanel.getInstance().log("###  延时已智能调低500ms  ###");
								LivePanel.getInstance().refreshDelayField();
							} else if (Config.live_config.DELAY >= 1000) {
								Config.live_config.DELAY -= 300;
								LivePanel.getInstance().log("###  延时已智能调低300ms  ###");
								LivePanel.getInstance().refreshDelayField();
							} else if (Config.live_config.DELAY >= 200) {
								if ((!Config.live_config.MULTI_THREAD) || Config.live_config.DELAY >= 50
										|| Config.live_config.I_DO_NOT_FEAR_BANNING) {
									Config.live_config.DELAY -= 200;
									LivePanel.getInstance().log("###  延时已智能调低200ms  ###");
								} else {
									Config.live_config.DELAY = 50;
									LivePanel.getInstance().log("###  延时已智能设置为50ms  ###");
								}
								LivePanel.getInstance().refreshDelayField();
							} else {
								if ((!Config.live_config.MULTI_THREAD) || Config.live_config.DELAY >= 50
										|| Config.live_config.I_DO_NOT_FEAR_BANNING) {
									Config.live_config.DELAY = 0;
									LivePanel.getInstance().log("###  延时已智能设置为0  ###");
								} else if (Config.live_config.DELAY != 50) {
									Config.live_config.DELAY = 50;
									LivePanel.getInstance().log("###  延时已智能设置为50ms  ###");
								}
								LivePanel.getInstance().refreshDelayField();
							}
						} else if (average > 7.5) {
							Config.live_config.DELAY += (int) (Config.live_config.DELAY * 0.25 + 50);
							LivePanel.getInstance().log("###  延时已智能调高（50ms + 25%）  ###");
							LivePanel.getInstance().refreshDelayField();
						}
					}
				}
				LivePanel.getInstance().refreshLabel(new_chat_count, buffer, first_run);
				buffer = 0;
				if (chat.getCount() > 9) {
					first_run = false;
				}
				LivePanel.getInstance().refreshUi();
			} catch (JSONException e) {
				LivePanel.getInstance().log("###############");
				LivePanel.getInstance().log(e.getMessage());
				LivePanel.getInstance().log("【发生异常】" + e.getClass().getName());
				LivePanel.getInstance().log("###############");
				LivePanel.getInstance().refreshUi();
				continue;
			} finally {
				if (index != 0) {
					list.set(index - 1, null);// 设为空指针以释放内存
				}
				index++;
			}
		}
	}

	public void push(String json) {
		list.add(json);
	}

	private void addCheckInfo(String checkinfo) {
		if (cursor == size) {
			cursor = 0;
		}
		ct_old[cursor] = checkinfo;
		cursor++;
	}

	/**
	 * 获取弹幕实体类对象
	 * 
	 * @return 弹幕
	 */
	public static Chat getChat() {
		return chat;
	}

	/**
	 * 获取弹幕颜色数组
	 * 
	 * @return 颜色
	 */
	public static int[] getChatColor() {
		int[] c = new int[color.size()];
		for (int i = 0; i < c.length; i++) {
			c[i] = color.get(i);
		}
		return c;
	}

	/**
	 * 获取存储的json
	 * 
	 * @return json
	 */
	public static JSONArray getJSONArray() {
		return total_json;
	}

}
