package script;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import org.apache.hc.client5.http.classic.methods.HttpGet;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.CloseableHttpResponse;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.core5.http.HttpEntity;
import org.apache.hc.core5.http.ParseException;
import org.apache.hc.core5.http.io.entity.EntityUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import gui.FileManager;
import gui.MainGui;
import gui.Reminding;
import gui.UpInfo;

/**
 * 哔哩哔哩小黑屋看守员提醒您： 视频千万条， 版权第一条。 爬虫不规范， 游客两行泪。
 */
public class Spider implements Runnable {
	private static Spider instance;
	private Thread currentThread;
	private boolean confirmed = false;
	private String[] pages;

	@Override
	public void run() {
		instance = this;
		currentThread = Thread.currentThread();
		MainGui.getInstance().setButtonText("正在准备，请稍候");
		MainGui.getInstance().log("准备工作正在进行，请稍候......");
		if (Config.spider_config.mode == 0) {
			confirmed = false;
			try {
				JSONObject data = getVideoInfoJson(Config.spider_config.avs[0] + "");
				MainGui.getInstance().log("获取视频JSON成功");
				String title = getVideoTitle(data);
				MainGui.getInstance().log("获取视频标题成功： " + title);
				pages = getVideoPages(data);
				MainGui.getInstance().log("获取视频分P信息成功，总P数： " + pages.length);
				if (pages.length == 0) {
					MainGui.getInstance().log("【警告】弹幕爬取失败，视频分P数为0。");
					MainGui.getInstance().reset();
					return;
				}
				if (pages.length == 1) {
					FileManager.showFileSaveDialog(null, getUpName(data) + " - " + title, 2);
					if (OutputManager.getFile() == null) {
						MainGui.getInstance().log("您未选择输出文件，爬取已终止");
						MainGui.getInstance().reset();
						return;
					}
					MainGui.getInstance().log("您已选择输出文件：" + OutputManager.getFile().getPath());
					new Reminding().setVisible(true);
					try {
						Thread.sleep(Long.MAX_VALUE);
					} catch (InterruptedException e) {
						// 继续执行操作
						if (!confirmed) {
							MainGui.getInstance().log("您已取消爬取");
							MainGui.getInstance().reset();
							return;
						}
						MainGui.getInstance().log("视频弹幕爬取开始");
						confirmed = false;
						String chatStr = null;
						try {
							chatStr = getChatByCid(pages[0]);
							MainGui.getInstance().log("弹幕文件获取完毕");
						} catch (ParseException e1) {
							MainGui.getInstance().log("【警告】弹幕爬取失败： " + e1.toString());
							MainGui.getInstance().reset();
							e1.printStackTrace();
							return;
						}
						OutputManager.saveToXml(chatStr);
						MainGui.getInstance().log("爬取成功！");
						MainGui.getInstance().reset();
						OutputManager.setFile(null);
					}
				} else {
					FileManager.showFileSaveDialog(null, "", 3);
					if (OutputManager.getFile() == null) {
						MainGui.getInstance().log("您未选择输出目录，爬取已终止");
						MainGui.getInstance().reset();
						return;
					}
					MainGui.getInstance().log("您已选择输出目录：" + OutputManager.getFile().getPath());
					new Reminding().setVisible(true);
					try {
						Thread.sleep(Long.MAX_VALUE);
					} catch (InterruptedException e) {
						// 继续执行操作
						if (!confirmed) {
							MainGui.getInstance().log("您已取消爬取");
							MainGui.getInstance().reset();
							return;
						}
						MainGui.getInstance().log("视频弹幕爬取开始");
						MainGui.getInstance().setButtonText("正在爬取中...");
						confirmed = false;
						String name = getUpName(data);
						for (int i = 0; i < pages.length; i++) {
							String chatStr = null;
							try {
								chatStr = getChatByCid(pages[i]);
								MainGui.getInstance().log("分P： " + (i + 1) + "P弹幕爬取完毕");
							} catch (ParseException e1) {
								MainGui.getInstance().log("【警告】弹幕爬取失败： " + e1.toString());
								e1.printStackTrace();
							}
							OutputManager.saveToXml(chatStr,
									OutputManager.getFile().getPath() + "\\" + OutputManager.replaceFileName(name
											+ " - " + title + " （" + (i + 1) + "P：" + getPageName(data, i) + "）.xml"));
							randomSleep();
						}
						OutputManager.setFile(null);
						MainGui.getInstance().log("爬取成功！");
						MainGui.getInstance().reset();
					}
				}
			} catch (IOException e) {
				MainGui.getInstance().log("【警告】弹幕爬取失败： " + e.toString());
				MainGui.getInstance().reset();
				e.printStackTrace();
				return;
			} catch (JSONException e) {
				MainGui.getInstance().log("【警告】在获取av" + Config.spider_config.avs[0] + "的弹幕时发生错误，视频可能不存在");
				MainGui.getInstance().reset();
				e.printStackTrace();
				return;
			}
		} else if (Config.spider_config.mode == 1) {
			confirmed = false;
			try {
				FileManager.showFileSaveDialog(null, "", 3);
				if (OutputManager.getFile() == null) {
					MainGui.getInstance().log("您未选择输出目录，爬取已终止");
					MainGui.getInstance().reset();
					return;
				}
				MainGui.getInstance().log("您已选择输出目录：" + OutputManager.getFile().getPath());
				new Reminding().setVisible(true);
				try {
					Thread.sleep(Long.MAX_VALUE);
				} catch (InterruptedException err) {
					// 继续执行操作
					if (!confirmed) {
						FileManager.deleteTempDir();
						MainGui.getInstance().log("您已取消爬取");
						MainGui.getInstance().reset();
						return;
					}
					int[] avs = Config.spider_config.avs;
					MainGui.getInstance().log("视频弹幕爬取开始，总计视频数为 " + avs.length);
					MainGui.getInstance().setButtonText("正在爬取中...");
					confirmed = false;
					for (int i = 0; i < avs.length; i++) {
						JSONObject data = null;
						try {
							data = getVideoInfoJson(avs[i] + "");
						} catch (JSONException e) {
							MainGui.getInstance().log("【警告】在获取av" + Config.spider_config.avs[i] + "的弹幕时发生错误，视频可能不存在");
							continue;
						}
						String name = getUpName(data);
						String title = getVideoTitle(data);
						pages = getVideoPages(data);
						for (int j = 0; j < pages.length; j++) {
							String chatStr = null;
							try {
								chatStr = getChatByCid(pages[j]);
							} catch (ParseException e1) {
								MainGui.getInstance().log("【警告】弹幕爬取失败： " + e1.toString());
								e1.printStackTrace();
							}
							if (pages.length == 1) {
								OutputManager.saveToXml(chatStr, OutputManager.getFile().getPath() + "\\"
										+ OutputManager.replaceFileName(name + " - " + title + ".xml"));
								MainGui.getInstance().log("第 " + (i + 1) + " 个视频爬取完毕");
							} else {
								OutputManager.saveToXml(chatStr,
										OutputManager.getFile().getPath() + "\\"
												+ OutputManager.replaceFileName(name + " - " + title + " （" + (j + 1)
														+ "P：" + getPageName(data, j) + "）.xml"));
								MainGui.getInstance().log("第 " + (i + 1) + " 个视频（" + (j + 1) + "P）爬取完毕");
							}
							randomSleep();
						}
					}
					OutputManager.setFile(null);
					MainGui.getInstance().log("爬取成功！");
					MainGui.getInstance().reset();
				}
			} catch (IOException e) {
				MainGui.getInstance().log("【警告】弹幕爬取失败： " + e.toString());
				MainGui.getInstance().reset();
				e.printStackTrace();
				return;
			} catch (JSONException e) {
				MainGui.getInstance().log("【警告】在获取视频弹幕时发生错误：" + e.toString());
				MainGui.getInstance().reset();
				e.printStackTrace();
				return;
			}
		} else if (Config.spider_config.mode == 2) {
			confirmed = false;
			try {
				Up up = null;
				try {
					up = getUpInfo(Config.spider_config.uid + "");
				} catch (JSONException e) {
					MainGui.getInstance().log("【警告】无法获取UP主信息，UID可能不存在");
					MainGui.getInstance().reset();
					e.printStackTrace();
					return;
				}
				MainGui.getInstance().log("获取UP主信息完毕");
				new UpInfo(up).setVisible(true);
				try {
					Thread.sleep(Long.MAX_VALUE);
				} catch (InterruptedException e) {
					// 继续执行操作
					if (!confirmed) {
						MainGui.getInstance().log("您已取消爬取");
						MainGui.getInstance().reset();
						return;
					}
					MainGui.getInstance().log("您已确认UP主信息");
					confirmed = false;
					SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
					String date = format.format(new Date());
					FileManager.showFileSaveDialog(null, up.name + " - " + date + "全部视频弹幕", 3);
					if (OutputManager.getFile() == null) {
						FileManager.deleteTempDir();
						MainGui.getInstance().log("您未选择输出目录，爬取已终止");
						MainGui.getInstance().reset();
						return;
					}
					MainGui.getInstance().log("您已选择输出目录：" + OutputManager.getFile().getPath());
					new Reminding().setVisible(true);
					try {
						Thread.sleep(Long.MAX_VALUE);
					} catch (InterruptedException err) {
						// 继续执行操作
						if (!confirmed) {
							FileManager.deleteTempDir();
							MainGui.getInstance().log("您已取消爬取");
							MainGui.getInstance().reset();
							return;
						}
						MainGui.getInstance().log("视频弹幕爬取开始");
						MainGui.getInstance().setButtonText("正在爬取中...");
						String[] avs = getAvsOfUp(up.uid);
						MainGui.getInstance().log("已获取UP主的所有视频，总数为 " + avs.length);
						confirmed = false;
						for (int i = 0; i < up.videos; i++) {
							JSONObject data = null;
							try {
								data = getVideoInfoJson(avs[i] + "");
							} catch (JSONException error) {
								MainGui.getInstance().log("【警告】在获取av" + avs[i] + "的弹幕时发生错误，视频可能不存在");
								continue;
							}
							String title = getVideoTitle(data);
							pages = getVideoPages(data);
							for (int j = 0; j < pages.length; j++) {
								String chatStr = null;
								try {
									chatStr = getChatByCid(pages[j]);
								} catch (ParseException e1) {
									MainGui.getInstance().log("【警告】弹幕爬取失败： " + e1.toString());
									MainGui.getInstance().reset();
									e1.printStackTrace();
									return;
								}
								if (pages.length == 1) {
									OutputManager.saveToXml(chatStr, OutputManager.getFile().getPath() + "\\"
											+ OutputManager.replaceFileName(up.name + " - " + title + ".xml"));
									MainGui.getInstance().log("第 " + (i + 1) + " 个视频爬取完毕");
								} else {
									OutputManager.saveToXml(chatStr,
											OutputManager.getFile().getPath() + "\\"
													+ OutputManager.replaceFileName(up.name + " - " + title + " （"
															+ (j + 1) + "P：" + getPageName(data, j) + "）.xml"));
									MainGui.getInstance().log("第 " + (i + 1) + " 个视频（" + (j + 1) + "P）爬取完毕");
								}
								randomSleep();
							}
						}
						OutputManager.setFile(null);
						MainGui.getInstance().log("爬取成功！");
						MainGui.getInstance().reset();
					}
				}
			} catch (IOException e) {
				MainGui.getInstance().log("【警告】弹幕爬取失败： " + e.toString());
				MainGui.getInstance().reset();
				e.printStackTrace();
				return;
			}
		}
	}

	private String[] getAvsOfUp(String uid) throws IOException, JSONException {
		int count;
		ArrayList<String> avs = new ArrayList<>();
		String first = getDataFromServer(
				"https://api.bilibili.com/x/space/arc/search?mid=" + uid + "&pn=1&ps=1&jsonp=jsonp");
		JSONObject first_root = new JSONObject(first);
		JSONObject first_data = first_root.getJSONObject("data");
		JSONObject first_page = first_data.getJSONObject("page");
		count = first_page.getInt("count");
		int currentPage = 1;
		while (avs.size() != count) {
			String json = getDataFromServer("https://api.bilibili.com/x/space/arc/search?mid=" + uid + "&pn="
					+ currentPage + "&ps=100&jsonp=jsonp");
			JSONArray array = new JSONObject(json).getJSONObject("data").getJSONObject("list").getJSONArray("vlist");
			for (int i = 0; i < array.length(); i++) {
				JSONObject video = array.getJSONObject(i);
				avs.add(video.getInt("aid") + "");
			}
			currentPage++;
			randomSleep();
		}
		return avs.toArray(new String[0]);
	}

	private String getDataFromServer(String url_) throws IOException {
		URL url = new URL(url_);
		HttpURLConnection conn = (HttpURLConnection) url.openConnection();
		conn.setRequestMethod("GET");
		conn.setDoOutput(true);
		conn.setConnectTimeout(20000);
		conn.setReadTimeout(20000);
		Reader in = new BufferedReader(new InputStreamReader(conn.getInputStream(), "UTF-8"));
		StringBuilder sb = new StringBuilder();
		for (int c; (c = in.read()) >= 0;) {
			sb.append((char) c);
		}
		return sb.toString();
	}

	private int getUpVideosCount(String uid) throws IOException {
		int count;
		String first = getDataFromServer(
				"https://api.bilibili.com/x/space/arc/search?mid=" + uid + "&pn=1&ps=1&jsonp=jsonp");
		JSONObject first_root = new JSONObject(first);
		JSONObject first_data = first_root.getJSONObject("data");
		JSONObject first_page = first_data.getJSONObject("page");
		count = first_page.getInt("count");
		return count;
	}

	private Up getUpInfo(String uid) throws IOException, JSONException {
		Up up = new Up();
		String info = getDataFromServer("https://api.bilibili.com/x/space/acc/info?mid=" + uid + "&jsonp=jsonp");
		JSONObject data = new JSONObject(info).getJSONObject("data");
		up.uid = uid;
		up.name = data.getString("name");
		up.level = data.getInt("level");
		up.sex = data.getString("sex");
		up.face_url = new URL(data.getString("face"));
		up.sign = data.getString("sign");
		up.role = data.getJSONObject("official").getInt("role");
		up.videos = getUpVideosCount(uid);
		return up;
	}

	private JSONObject getVideoInfoJson(String av) throws IOException, JSONException {
		String text = getDataFromServer("https://api.bilibili.com/x/web-interface/view?aid=" + av);
		return new JSONObject(text).getJSONObject("data");
	}

	private String getVideoTitle(JSONObject json) {
		return json.getString("title");
	}

	private String getPageName(JSONObject json, int page) {
		return json.getJSONArray("pages").getJSONObject(page).getString("part");
	}

	private String getUpName(JSONObject json) {
		return json.getJSONObject("owner").getString("name");
	}

	private String[] getVideoPages(JSONObject json) throws IOException {
		ArrayList<String> str = new ArrayList<>();
		JSONArray pages = json.getJSONArray("pages");
		for (int i = 0; i < pages.length(); i++) {
			str.add(pages.getJSONObject(i).getInt("cid") + "");
		}
		return str.toArray(new String[0]);
	}

	private void randomSleep() {
		try {
			Thread.sleep(2000 + (int) Math.random() * 2000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
		}
	}

	/*
	 * 开发者诉苦： 哔哩哔哩弹幕xml是压缩过了的，浏览器访问xml文件会自动解压从而获得正确的文件；而跑程序则不会。
	 * TMD这个问题困扰我整整两天，敲代码到半夜，我甚至想过直接搞解压算法，直到最后一刻才想起用第三方库，成功爬取的时候（爷）真是老泪纵横23333
	 */
	private String getChatByCid(String cid) throws ParseException, IOException {
		CloseableHttpClient client = HttpClients.createDefault();
		HttpGet httpGet = new HttpGet("https://comment.bilibili.com/" + cid + ".xml");
		CloseableHttpResponse response = client.execute(httpGet);
		HttpEntity entity = response.getEntity();
		String string = EntityUtils.toString(entity);
		response.close();
		return string;
	}

	public static class Up {
		public String uid;// uid
		public String name;// UP主的名字
		public int level;// 等级
		public String sex;// 性别
		public URL face_url;// 头像的url
		public String sign;// 介绍
		public int role;// 认证信息，0为无认证，1为黄色闪电，2为蓝色闪电
		public int videos;// UP主的视频数
	}

	public static Spider getInstance() {
		return instance;
	}

	public Thread getThread() {
		return currentThread;
	}

	public void confirm() {
		confirmed = true;
	}

}
