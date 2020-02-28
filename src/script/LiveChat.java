package script;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.HttpURLConnection;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import gui.LivePanel;

/**
 * 直播弹幕爬取
 */
public class LiveChat implements Runnable {

	File file;
	private static Chat chat;
	private static JSONArray total_json;
	private static String stringBuff = "";

	/**
	 * 实例化
	 * 
	 * @param file 输出的文件
	 */
	public LiveChat(File file) {
		this.file = file;
	}

	/**
	 * 运行
	 */
	@Override
	public void run() {
		chat = new Chat(new ArrayList<String>(), new ArrayList<String>(), new ArrayList<Float>(),
				new ArrayList<Long>());
		JSONArray jsonArray_old = new JSONArray();
		total_json = new JSONArray();
		boolean first_run = true;
		int[] stat = new int[10];
		int stat_index = 0;
		for (int i = 0; i < stat.length; i++) {
			stat[i] = 0;
		}
		LivePanel.getInstance().log("###  抓取已开始  ###");
		LivePanel.getInstance().refreshUi();
		while (Config.live_config.STATUS) {
			JSONObject jsonObject = null;
			int buffer = 0;
			int new_chat_count = 0;
			try {
				jsonObject = new JSONObject(request());
			} catch (JSONException | IOException e) {
				LivePanel.getInstance().log("###############");
				LivePanel.getInstance().log(e.getMessage());
				LivePanel.getInstance().log("【发生异常】" + e.getClass().getName());
				LivePanel.getInstance().log("###############");
			}
			JSONObject data = jsonObject.getJSONObject("data");
			JSONArray room = data.getJSONArray("room");
			LivePanel.getInstance().addTime();
			tick: for (int i = 0; i < room.length(); i++) {
				for (int j = 0; j < jsonArray_old.length(); j++) {
					if ((((JSONObject) room.get(i)).get("rnd"))
							.equals(((JSONObject) jsonArray_old.get(j)).get("rnd"))) {
						buffer++;
						continue tick;
					}
				}
				SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
				long time = 0;
				try {
					time = (format.parse(((JSONObject) room.get(i)).getString("timeline"))).getTime();
				} catch (JSONException | ParseException e) {
					LivePanel.getInstance().log("###############");
					LivePanel.getInstance().log(e.getMessage());
					LivePanel.getInstance().log("【发生异常】" + e.getClass().getName());
					LivePanel.getInstance().log("###############");
				}
				// 如果弹幕的发送时间早于设定的开始时间，则该弹幕是历史弹幕，不抓取
				if (time < Config.live_config.START_TIME) {
					LivePanel.getInstance().log("[历史弹幕，不记录]  " + ((JSONObject) room.get(i)).getString("text"));
					continue tick;
				}
				new_chat_count++;
				String text = ((JSONObject) room.get(i)).getString("text");
				text = text.replace("<", "&lt;");
				text = text.replace("&", "&amp;");
				chat.append(text, ((JSONObject) room.get(i)).getInt("uid") + "",
						(float) ((time - Config.live_config.START_TIME) / 1000), time / 1000);
				total_json.put(room.getJSONObject(i));
				if (first_run) {
					LivePanel.getInstance().log("[缓冲区填充中]  " + ((JSONObject) room.get(i)).getString("text"));
				} else if (buffer == 0) {
					LivePanel.getInstance()
							.log("【警告 - 缓冲区" + buffer + "】  " + ((JSONObject) room.get(i)).getString("text"));
				} else {
					LivePanel.getInstance().log("[缓冲区" + buffer + "]  " + ((JSONObject) room.get(i)).getString("text"));
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
						Config.live_config.DELAY -= 200;
						LivePanel.getInstance().log("###  延时已智能调低2000ms  ###");
						LivePanel.getInstance().refreshDelayField();
					} else {
						Config.live_config.DELAY = 0;
						LivePanel.getInstance().log("###  延时已智能设置为0  ###");
						LivePanel.getInstance().refreshDelayField();
					}
				}
			}

			jsonArray_old = room;
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
							Config.live_config.DELAY -= 100;
							LivePanel.getInstance().log("###  延时已智能调低100ms  ###");
							LivePanel.getInstance().refreshDelayField();
						} else {
							Config.live_config.DELAY = 0;
							LivePanel.getInstance().log("###  延时已智能设置为0  ###");
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
			try {
				Thread.sleep(Config.live_config.DELAY);
			} catch (InterruptedException e) {
				LivePanel.getInstance().log("###  抓取已结束  ###");
				LivePanel.getInstance().refreshUi();
			}
		}
	}

	/**
	 * 发送HTML请求
	 * 
	 * @return 服务器返回的字符串
	 * @throws IOException IO异常
	 */
	public static String request() throws IOException {
		try {
			URL url = new URL("https://api.live.bilibili.com/ajax/msg");
			byte[] postDataBytes = new String("roomid=" + Config.live_config.ROOM).getBytes("UTF-8");
			HttpURLConnection conn = (HttpURLConnection) url.openConnection();
			conn.setRequestMethod("POST");
			conn.setDoOutput(true);
			conn.setConnectTimeout(5000);
			conn.setReadTimeout(5000);
			conn.getOutputStream().write(postDataBytes);
			Reader in = new BufferedReader(new InputStreamReader(conn.getInputStream(), "UTF-8"));
			StringBuilder sb = new StringBuilder();
			for (int c; (c = in.read()) >= 0;) {
				sb.append((char) c);
			}
			stringBuff = sb.toString();
			return sb.toString();
		} catch (SocketTimeoutException e) {
			LivePanel.getInstance().log("【警告】HTTP连接超时");
			return stringBuff;
		}
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
	 * 获取存储的json
	 * 
	 * @return json
	 */
	public static JSONArray getJSONArray() {
		return total_json;
	}
}
