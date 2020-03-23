package script;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.HttpURLConnection;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Date;

import gui.LivePanel;

public class MultiThreadUtil {

	private Thread thread;

	public int getMaxDelay() {
		int success = 0;
		int max_delay = 0;
		if (!LiveChat.isLiveRoomHandled()) {
			LivePanel.getInstance().log("###  正在处理房间号  ###");
			LivePanel.getInstance().refreshUi();
			if (!LiveChat.utilRoomNumber()) {
				LivePanel.getInstance().onUtilRoomNumberFailed(1);
				return 0;
			}
			if (Thread.interrupted()) {
				return 0;
			}
			LiveChat.setLiveRoomHandled(true);
			LivePanel.getInstance().log("###  处理完毕，配置已保存  ###");
			LivePanel.getInstance().refreshUi();
		}
		while (true) {
			try {
				Date date = new Date();
				URL url = new URL("https://api.live.bilibili.com/ajax/msg");
				byte[] postDataBytes = new String("roomid=" + Config.live_config.ROOM).getBytes("UTF-8");
				HttpURLConnection conn = (HttpURLConnection) url.openConnection();
				conn.setRequestMethod("POST");
				conn.setDoOutput(true);
				conn.setConnectTimeout(1000);
				conn.setReadTimeout(1000);
				conn.getOutputStream().write(postDataBytes);
				Reader in = new BufferedReader(new InputStreamReader(conn.getInputStream(), "UTF-8"));
				StringBuilder sb = new StringBuilder();
				for (int c; (c = in.read()) >= 0;) {
					sb.append((char) c);
				}
				success++;
				int delay = (int) (new Date().getTime() - date.getTime());// 如果延迟超过21亿秒，我当场把这个电脑屏幕吃掉
				LivePanel.getInstance().log("第" + success + "次请求成功，延迟为" + delay + "ms");
				LivePanel.getInstance().refreshUi();
				if (delay > max_delay) {
					max_delay = delay;
				}
				if (success == 50) {
					break;
				}
				Thread.sleep(100);
			} catch (SocketTimeoutException e) {
				LivePanel.getInstance().log("【警告】HTTP连接超时");
				LivePanel.getInstance().refreshUi();
			} catch (IOException e) {
				LivePanel.getInstance().log("【警告】HTTP连接失败");
				LivePanel.getInstance().refreshUi();
			} catch (InterruptedException e) {
				return 0;
			}
		}
		return max_delay;
	}

	public void init(MultiThreadOperation utilClass, int timeout) {
		thread = new Thread(new Runnable() {
			@Override
			public void run() {
				try {
					URL url = new URL("https://api.live.bilibili.com/ajax/msg");
					byte[] postDataBytes = new String("roomid=" + Config.live_config.ROOM).getBytes("UTF-8");
					HttpURLConnection conn = (HttpURLConnection) url.openConnection();
					conn.setRequestMethod("POST");
					conn.setDoOutput(true);
					conn.setConnectTimeout(timeout);
					conn.setReadTimeout(timeout);
					conn.getOutputStream().write(postDataBytes);
					Reader in = new BufferedReader(new InputStreamReader(conn.getInputStream(), "UTF-8"));
					StringBuilder sb = new StringBuilder();
					for (int c; (c = in.read()) >= 0;) {
						sb.append((char) c);
					}
					conn.disconnect();
					utilClass.callback(sb.toString());
				} catch (SocketTimeoutException e) {
					LivePanel.getInstance().log("【警告】HTTP连接超时");
					LivePanel.getInstance().refreshUi();
				} catch (IOException e) {
					LivePanel.getInstance().log("【警告】HTTP连接失败");
					LivePanel.getInstance().refreshUi();
				}
			}
		});
	}

	public void start() {
		while (Config.live_config.STATUS) {
			new Thread(thread).start();
			try {
				Thread.sleep(Config.live_config.DELAY);
			} catch (InterruptedException e) {
			}
		}
	}

	private ArrayList<String> demo_list = new ArrayList<>();

	/**
	 * 模拟返回json，用于调试代码
	 * 
	 * @param min
	 * @param max
	 * @return
	 */
	@SuppressWarnings("unused")
	private String demo(int min, int max) {
		if (demo_list.size() == 0) {
			for (int i = 0; i < 10; i++) {
				addToDemoList(i);
			}
			return cast();
		}
		int j = min + (int) (Math.random() * (max - min));
		for (int i = 0; i < j; i++) {
			addToDemoList(demo_list.size() - 1);
		}
		return cast();
	}

	private void addToDemoList(int ct) {
		demo_list.add("{\"text\":\"模拟弹幕 - " + (ct + 1)
				+ "\",\"nickname\":\"JellyBlack\",\"uname_color\":\"\",\"uid\":368205203,\"timeline\":\"2020-01-01 00:00:00\",\"isadmin\":0,\"vip\":0,\"svip\":0,\"medal\":[20,\"封号斗罗\",\"JellyBlack\",368205203,0,\"\",0],\"title\":[\"title-0-0\",\"title-0-0\"],\"user_level\":[60,0,0,\"null\"],\"rank\":10000,\"teamid\":0,\"rnd\":\"0\",\"user_title\":\"title-0-0\",\"guard_level\":0,\"bubble\":0,\"check_info\":{\"ts\":2147483647,\"ct\":\""
				+ ct + "\"},\"lpl\":0}");
		if (demo_list.size() > 10) {
			demo_list.set(demo_list.size() - 11, null);
		}
	}

	private String cast() {
		StringBuilder sb = new StringBuilder("{\"code\":0,\"data\":{\"room\":[");
		for (int i = demo_list.size() - 10; i < demo_list.size() - 1; i++) {
			sb.append(demo_list.get(i));
			sb.append(",");
		}
		sb.append(demo_list.get(demo_list.size() - 1));
		sb.append("]},\"message\":\"\",\"msg\":\"\"}");
		return sb.toString();
	}
}
