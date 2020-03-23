package script;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.HttpURLConnection;
import java.net.SocketTimeoutException;
import java.net.URL;

import org.json.JSONException;
import org.json.JSONObject;

import gui.LivePanel;

public class LiveRoomStatus implements Runnable {

	private static String stringBuff;
	private int expectedStatus;

	public LiveRoomStatus(int expectedStatus) {
		this.expectedStatus = expectedStatus;
	}

	@Override
	public void run() {
		if (expectedStatus == 1) {
			if(!LiveChat.isLiveRoomHandled()) {
				LivePanel.getInstance().log("###  正在处理房间号  ###");
				LivePanel.getInstance().refreshUi();
				if (!LiveChat.utilRoomNumber()) {
					LivePanel.getInstance().onUtilRoomNumberFailed(1);
					return;
				}
				LiveChat.setLiveRoomHandled(true);
				LivePanel.getInstance().log("###  处理完毕，配置已保存  ###");
				LivePanel.getInstance().refreshUi();
			}
			int i = 0;
			while (!Thread.interrupted()) {
				try {
					String str = request();
					JSONObject json = new JSONObject(str);
					int status = json.getJSONObject("data").getJSONObject("room_info").getInt("live_status");
					/*
					 * 0：未开播 1：直播中 2：轮播中
					 */
					if (status == 1) {
						LivePanel.getInstance().log("###  主播已开播，已自动启动爬虫  ###");
						LivePanel.getInstance().refreshUi();
						LivePanel.getInstance().onLiveStart();
						return;
					}
					i++;
					LivePanel.getInstance().log("###  已成功获取直播间状态" + i + "次  ###");
					LivePanel.getInstance().refreshUi();
				} catch (IOException | JSONException e) {
					LivePanel.getInstance().log("###############");
					LivePanel.getInstance().log(e.getMessage());
					LivePanel.getInstance().log("【发生异常】" + e.getClass().getName());
					LivePanel.getInstance().log("###############");
					LivePanel.getInstance().refreshUi();
				}
			}
		} else {
			int i = 0;
			while (!Thread.interrupted()) {
				try {
					String str = request();
					JSONObject json = new JSONObject(str);
					int status = json.getJSONObject("data").getJSONObject("room_info").getInt("live_status");
					/*
					 * 0：未开播 1：直播中 2：轮播中
					 */
					if (status != 1) {
						LivePanel.getInstance().log("###  主播已下播，已自动停止爬虫  ###");
						LivePanel.getInstance().refreshUi();
						LivePanel.getInstance().onLiveStop();
						return;
					}
					i++;
					LivePanel.getInstance().log("###  已成功获取直播间状态" + i + "次  ###");
					LivePanel.getInstance().refreshUi();
				} catch (IOException | JSONException e) {
					LivePanel.getInstance().log("###############");
					LivePanel.getInstance().log(e.getMessage());
					LivePanel.getInstance().log("【发生异常】" + e.getClass().getName());
					LivePanel.getInstance().log("###############");
					LivePanel.getInstance().refreshUi();
				}
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
			URL url = new URL("https://api.live.bilibili.com/xlive/web-room/v1/index/getInfoByRoom?room_id="
					+ +Config.live_config.ROOM);
			HttpURLConnection conn = (HttpURLConnection) url.openConnection();
			conn.setRequestMethod("GET");
			conn.setDoOutput(true);
			conn.setConnectTimeout(5000);
			conn.setReadTimeout(5000);
			Reader in = new BufferedReader(new InputStreamReader(conn.getInputStream(), "UTF-8"));
			StringBuilder sb = new StringBuilder();
			for (int c; (c = in.read()) >= 0;) {
				sb.append((char) c);
			}
			stringBuff = sb.toString();
			conn.disconnect();
			return sb.toString();
		} catch (SocketTimeoutException e) {
			LivePanel.getInstance().log("【警告】HTTP连接超时");
			LivePanel.getInstance().refreshUi();
			return stringBuff;
		}
	}
}
