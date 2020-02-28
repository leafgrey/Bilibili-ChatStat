package script;

import java.io.File;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.zip.CRC32;

import gui.MainGui;
import gui.Table;

/**
 * 对导入的弹幕进行统计的类
 */
public class RunScript implements Runnable {
	int tab;
	File[] files;

	/**
	 * 实例化
	 * 
	 * @param tab      选项
	 * @param xmlFiles xml文件
	 */
	public RunScript(int tab, File[] xmlFiles) {
		this.tab = tab;
		this.files = xmlFiles;
	}

	/**
	 * 开始运行
	 */
	@Override
	public void run() {
		try {
			if (tab == 1) {
				MainGui.getInstance().initProgressBar(files.length);
				Chat chat = XmlLoader.loadChatFromXml(files);
				MainGui.getInstance().progressFinish();
				ChatUtil.utilChat(chat);
				// 进行排名
				MainGui.getInstance().initProgressBar(chat.getCount());
				ArrayList<String> rank = new ArrayList<>();
				ArrayList<Integer> count = new ArrayList<>();
				ArrayList<String> rank2 = new ArrayList<>();
				ArrayList<Integer> count2 = new ArrayList<>();
				tick: for (int i = 0; i < chat.getCount(); i++) {
					MainGui.getInstance().refreshProgressBar(i);
					for (int j = 0; j < rank.size(); j++) {
						if (Config.public_config.IGNORE_CASES) {
							if (chat.getChats().get(i).toLowerCase().equals(rank.get(j).toLowerCase())) {
								count.set(j, count.get(j) + 1);
								continue tick;
							}
						} else {
							if (chat.getChats().get(i).equals(rank.get(j))) {
								count.set(j, count.get(j) + 1);
								continue tick;
							}
						}
					}
					rank.add(chat.getChats().get(i));
					count.add(1);
				}
				MainGui.getInstance().progressFinish();
				if (Config.tab1.RANK_LIMIT == -1) {
					MainGui.getInstance().initProgressBar(rank.size());
				} else {
					MainGui.getInstance().initProgressBar(Config.tab1.RANK_LIMIT);
				}
				int max = 0;
				int max_index = 0;
				int progress = 0;
				tick: while (true) {
					for (int i = 0; i < rank.size(); i++) {
						if (count.get(i) > max) {
							max = count.get(i);
							max_index = i;
						}
					}
					if (max == 0) {
						break tick;
					}
					int limit = Config.tab1.RANK_LIMIT;
					if (limit != -1) {
						if (rank2.size() == limit) {
							break tick;
						}
					}
					rank2.add(rank.get(max_index));
					count2.add(count.get(max_index));
					MainGui.getInstance().refreshProgressBar(progress);
					progress++;
					count.set(max_index, 0);
					max = 0;
				}
				MainGui.getInstance().progressFinish();
				MainGui.getInstance().initProgressBar(rank2.size());
				if (Config.public_config.OUTPUT_STYLE == 1) {
					OutputManager.saveToCsv(new String[] { "弹幕", "重复次数" }, new ArrayList<?>[] { rank2, count2 }, true);
				} else {
					String[][] strings = new String[count2.size()][2];
					for (int i = 0; i < count2.size(); i++) {
						MainGui.getInstance().refreshProgressBar(i);
						strings[i][0] = rank2.get(i);
						strings[i][1] = count2.get(i).toString();
					}
					new Table(new String[] { "弹幕", "重复次数" }, strings).setVisible(true);
				}
				MainGui.getInstance().progressFinish();
			} else if (tab == 2) {
				MainGui.getInstance().initProgressBar(files.length);
				Chat chat = XmlLoader.loadChatFromXml(files);
				MainGui.getInstance().progressFinish();
				ChatUtil.utilChat(chat);
				// 根据时间先后进行排序
				MainGui.getInstance().initProgressBar(chat.getCount());
				ArrayList<Float> time = chat.getTime();
				ArrayList<Float> time2 = new ArrayList<>();
				int i;
				int progress = 0;
				float min = 2147483647;
				int min_index = 0;
				while (true) {
					for (i = 0; i < time.size(); i++) {
						if (time.get(i) < min) {
							min = time.get(i);
							min_index = i;
						}
					}
					if (min == 2147483647) {
						break;
					}
					MainGui.getInstance().refreshProgressBar(progress);
					if (min < Config.tab2.START_TIME) {
						min = 2147483647;
						time.remove(min_index);
						progress++;
						continue;
					}
					if (Config.tab2.END_TIME != -1) {
						if (min > Config.tab2.END_TIME) {
							break;
						}
					}
					progress++;
					time2.add(time.get(min_index));
					time.set(min_index, 2147483647F);
					min = 2147483647;
				}

				MainGui.getInstance().progressFinish();
				// 分组统计
				MainGui.getInstance().initProgressBar(time2.size());
				int[] group_value = null;
				// 为避免用户作死，把采样长度设置过大、设置为0或者负数等等，此处需要处理异常
				if (Config.tab2.LENGTH > 2147483647.0) {
					MainGui.getInstance()
							.notifyXmlHandlingException(new IllegalArgumentException("采样长度过大（超过2147483647），请调低采样长度。"));
					throw new InterruptedException();
				}
				if (Config.tab2.START_TIME < 0.0) {
					MainGui.getInstance().notifyXmlHandlingException(new IllegalArgumentException("起始时间不能小于0。"));
					throw new InterruptedException();
				}
				if (Config.tab2.END_TIME <= 0.0 && Config.tab2.END_TIME != -1) {
					MainGui.getInstance()
							.notifyXmlHandlingException(new IllegalArgumentException("终止时间不能小于或等于0（设置为-1除外）。"));
					throw new InterruptedException();
				}

				if (Config.tab2.END_TIME != -1 && Config.tab2.START_TIME >= Config.tab2.END_TIME) {
					MainGui.getInstance()
							.notifyXmlHandlingException(new IllegalArgumentException("起始时间还没有终止时间大，你是想时  间  倒  流吗？"));
					throw new InterruptedException();
				}

				if (time2.size() == 0) {
					MainGui.getInstance().notifyXmlHandlingException(
							new IllegalArgumentException("组数为0。可能由以下原因导致：\n1）xml弹幕文件无弹幕\n2）起始时间和终止时间过近\n3）起始时间过大"));
					throw new InterruptedException();
				}

				if ((int) (Math
						.ceil((time2.get(time2.size() - 1) - Config.tab2.START_TIME) / Config.tab2.LENGTH)) > 65535) {
					MainGui.getInstance()
							.notifyXmlHandlingException(new IllegalArgumentException("组数过大（超过65535），请调高采样长度。"));
					throw new InterruptedException();
				}
				try {
					group_value = new int[(int) (Math
							.ceil((time2.get(time2.size() - 1) - Config.tab2.START_TIME) / Config.tab2.LENGTH))];
				}

				catch (NegativeArraySizeException e) {
					e = new NegativeArraySizeException("采样长度设置异常，请检查您的设置。");
					MainGui.getInstance().notifyXmlHandlingException(e);
					throw new InterruptedException();
				}
				ArrayList<String> text = new ArrayList<>();
				for (int j = 0; j < group_value.length; j++) {
					group_value[j] = 0;
					DecimalFormat df = new DecimalFormat("#.0");
					boolean hour;
					if (time2.get(time2.size() - 1) >= 3600) {
						hour = true;
					} else {
						hour = false;
					}
					if (j != group_value.length - 1) {
						float time_1 = Config.tab2.START_TIME + Config.tab2.LENGTH * j;
						float time_2 = Config.tab2.START_TIME + Config.tab2.LENGTH * (j + 1);
						String out = "";
						if (time_1 >= 3600) {
							if ((int) time_1 / 3600 < 10) {
								out += 0;
							}
							out += (int) time_1 / 3600;
							out += ":";
							time_1 = time_1 % 3600;
						} else if (hour) {
							out += "00:";
						}
						if (time_1 >= 60) {
							if ((int) time_1 / 60 < 10) {
								out += 0;
							}
							out += (int) time_1 / 60;
							out += ":";
							time_1 = time_1 % 60;
						} else {
							out += "00:";
						}
						time_1 = Float.parseFloat(df.format(time_1));
						if (time_1 < 10) {
							out += 0;
						}
						out += time_1;
						out += "  -  ";
						if (time_2 >= 3600) {
							if ((int) time_2 / 3600 < 10) {
								out += 0;
							}
							out += (int) time_2 / 3600;
							out += ":";
							time_2 = time_2 % 3600;
						} else if (hour) {
							out += "00:";
						}
						if (time_2 >= 60) {
							if ((int) time_2 / 60 < 10) {
								out += 0;
							}
							out += (int) time_2 / 60;
							out += ":";
							time_2 = time_2 % 60;
						} else {
							out += "00:";
						}
						time_2 = Float.parseFloat(df.format(time_2));
						if (time_2 < 10) {
							out += 0;
						}
						out += time_2;
						text.add(out);
					} else {
						float time_1 = Config.tab2.START_TIME + Config.tab2.LENGTH * j;
						float time_2 = time2.get(time2.size() - 1);
						String out = "";
						if (time_1 >= 3600) {
							if ((int) time_1 / 3600 < 10) {
								out += 0;
							}
							out += (int) time_1 / 3600;
							out += ":";
							time_1 = time_1 % 3600;
						} else if (hour) {
							out += "00:";
						}
						if (time_1 >= 60) {
							if ((int) time_1 / 60 < 10) {
								out += 0;
							}
							out += (int) time_1 / 60;
							out += ":";
							time_1 = time_1 % 60;
						} else {
							out += "00:";
						}
						time_1 = Float.parseFloat(df.format(time_1));
						if (time_1 < 10) {
							out += 0;
						}
						out += time_1;
						out += "  -  ";
						if (time_2 >= 3600) {
							if ((int) time_2 / 3600 < 10) {
								out += 0;
							}
							out += (int) time_2 / 3600;
							out += ":";
							time_2 = time_2 % 3600;
						} else if (hour) {
							out += "00:";
						}
						if (time_2 >= 60) {
							if ((int) time_2 / 60 < 10) {
								out += 0;
							}
							out += (int) time_2 / 60;
							out += ":";
							time_2 = time_2 % 60;
						} else {
							out += "00:";
						}
						time_2 = Float.parseFloat(df.format(time_2));
						if (time_2 < 10) {
							out += 0;
						}
						out += time_2;
						text.add(out);
					}
				}
				for (int j = 0; j < time2.size(); j++) {
					MainGui.getInstance().refreshProgressBar(j);
					// 最后一条弹幕的时间如果十分凑巧，会抛数组越界异常
					try {
						group_value[(int) ((Math.floor(time2.get(j) - Config.tab2.START_TIME) / Config.tab2.LENGTH))]++;
					} catch (ArrayIndexOutOfBoundsException e) {
						group_value[(int) ((Math.floor(time2.get(j) - Config.tab2.START_TIME) / Config.tab2.LENGTH))
								- 1]++;
					}
				}
				MainGui.getInstance().progressFinish();
				MainGui.getInstance().initProgressBar(group_value.length);
				ArrayList<Integer> group = new ArrayList<>();
				for (int j = 0; j < group_value.length; j++) {
					group.add(group_value[j]);
				}
				if (Config.public_config.OUTPUT_STYLE == 1) {
					OutputManager.saveToCsv(new String[] { "时间段", "弹幕数量" }, new ArrayList<?>[] { text, group }, true);
				} else {
					String[][] strings = new String[group_value.length][2];
					for (int j = 0; j < group_value.length; j++) {
						MainGui.getInstance().refreshProgressBar(j);
						strings[j][0] = text.get(j);
						strings[j][1] = group.get(j).toString();
					}
					new Table(new String[] { "时间段", "弹幕数量" }, strings).setVisible(true);
				}
				MainGui.getInstance().progressFinish();
			} else if (tab == 3) {
				MainGui.getInstance().initProgressBar(files.length);
				Chat chat = XmlLoader.loadChatFromXml(files);
				MainGui.getInstance().progressFinish();
				ChatUtil.utilChat(chat);
				// 进行排名
				MainGui.getInstance().initProgressBar(chat.getCount());
				ArrayList<String> rank = new ArrayList<>();
				ArrayList<Integer> count = new ArrayList<>();
				ArrayList<String> rank2 = new ArrayList<>();
				ArrayList<Integer> count2 = new ArrayList<>();
				ArrayList<Long> uids = new ArrayList<>();
				tick: for (int i = 0; i < chat.getCount(); i++) {
					MainGui.getInstance().refreshProgressBar(i);
					for (int j = 0; j < rank.size(); j++) {
						if (chat.getUsers().get(i).equals(rank.get(j))) {
							count.set(j, count.get(j) + 1);
							continue tick;
						}
					}
					rank.add(chat.getUsers().get(i));
					count.add(1);
				}
				MainGui.getInstance().progressFinish();
				int max = 0;
				int max_index = 0;
				int processed = 0;
				int progress = 0;
				if (Config.tab1.RANK_LIMIT == -1) {
					MainGui.getInstance().initProgressBar(rank.size());
				} else {
					MainGui.getInstance().initProgressBar(Config.tab1.RANK_LIMIT);
				}
				tick: while (true) {
					for (int i = 0; i < rank.size(); i++) {
						if (count.get(i) > max) {
							max = count.get(i);
							max_index = i;
						}
					}
					if (max == 0) {
						break tick;
					}
					int limit = Config.tab1.RANK_LIMIT;
					if (limit != -1) {
						if (rank2.size() == limit) {
							break tick;
						}
					}
					rank2.add(rank.get(max_index));
					count2.add(count.get(max_index));
					MainGui.getInstance().refreshProgressBar(progress);
					progress++;
					count.set(max_index, 0);
					max = 0;
				}
				MainGui.getInstance().progressFinish();
				// CRC32反算
				for (int i = 0; i < rank2.size(); i++) {
					uids.add(0L);
				}
				if (Config.tab3.CRC32) {
					MainGui.getInstance().initProgressBar(rank2.size());
					CRC32 crc32 = new CRC32();
					long uid = 0;
					tick: while (processed != rank2.size()) {
						MainGui.getInstance().refreshProgressBar(processed);
						crc32.update((uid + "").getBytes());
						String value = Long.toHexString(crc32.getValue());
						for (int i = 0; i < rank2.size(); i++) {
							if (value.equals(rank2.get(i))) {
								uids.set(i, uid);
								processed++;
								continue tick;
							}
						}
						uid++;
						crc32.reset();
					}
					MainGui.getInstance().progressFinish();
				}
				MainGui.getInstance().initProgressBar(rank2.size());
				if (Config.public_config.OUTPUT_STYLE == 1) {
					if (Config.tab3.CRC32) {
						OutputManager.saveToCsv(new String[] { "用户", "用户（UID）", "发送弹幕总数" },
								new ArrayList<?>[] { rank2, uids, count2 }, true);
					} else {
						OutputManager.saveToCsv(new String[] { "用户", "发送弹幕总数" }, new ArrayList<?>[] { rank2, count2 },
								true);
					}
				} else {
					if (Config.tab3.CRC32) {
						String[][] strings = new String[rank2.size()][3];
						for (int i = 0; i < rank2.size(); i++) {
							MainGui.getInstance().refreshProgressBar(i);
							strings[i][0] = rank2.get(i);
							strings[i][1] = uids.get(i).toString();
							strings[i][2] = count2.get(i).toString();
						}
						new Table(new String[] { "用户", "用户（UID）", "发送弹幕总数" }, strings).setVisible(true);
					} else {
						String[][] strings = new String[rank2.size()][2];
						for (int i = 0; i < rank2.size(); i++) {
							MainGui.getInstance().refreshProgressBar(i);
							strings[i][0] = rank2.get(i);
							strings[i][1] = count2.get(i).toString();
						}
						new Table(new String[] { "用户", "发送弹幕总数" }, strings).setVisible(true);
					}
				}
				MainGui.getInstance().progressFinish();
			} else if (tab == 4) {
				MainGui.getInstance().initProgressBar(files.length);
				Chat chat = XmlLoader.loadChatFromXml(files);
				MainGui.getInstance().progressFinish();
				ChatUtil.utilChat(chat);
				MainGui.getInstance().initProgressBar(chat.getCount());
				ArrayList<Long> date = chat.getDate();
				ArrayList<Long> date2 = new ArrayList<>();
				int i;
				long min = Long.MAX_VALUE;
				int min_index = 0;
				while (true) {
					for (i = 0; i < date.size(); i++) {
						if (date.get(i) < min) {
							min = date.get(i);
							min_index = i;
						}
					}
					if (min == Long.MAX_VALUE) {
						break;
					}
					MainGui.getInstance().refreshProgressBar(date2.size());
					date2.add(date.get(min_index));
					date.set(min_index, Long.MAX_VALUE);
					min = Long.MAX_VALUE;
				}
				MainGui.getInstance().progressFinish();
				// 时间戳转换
				MainGui.getInstance().initProgressBar(date2.size());
				ArrayList<String> chat_date = new ArrayList<>();
				for (int j = 0; j < date2.size(); j++) {
					chat_date.add(new SimpleDateFormat("yyyy-MM").format(new Date(date2.get(j) * 1000)));
				}
				ArrayList<String> out_date = new ArrayList<>();
				ArrayList<Integer> count = new ArrayList<>();
				tick: for (int j = 0; j < chat_date.size(); j++) {
					MainGui.getInstance().refreshProgressBar(j);
					for (int k = 0; k < out_date.size(); k++) {
						if (chat_date.get(j).equals(out_date.get(k))) {
							count.set(out_date.indexOf(chat_date.get(j)),
									count.get(out_date.indexOf(chat_date.get(j))) + 1);
							continue tick;
						}
					}
					try {
						int months = 1;
						while (!out_date.get(out_date.size() - 1).equals(chat_date.get(j))) {
							Calendar calendar = new GregorianCalendar();
							calendar.setTime(new Date(date2.get(j - 1) * 1000));
							calendar.add(Calendar.MONTH, months);
							out_date.add(new SimpleDateFormat("yyyy-MM").format(calendar.getTime()));
							count.add(0);
							months++;
						}
						count.set(count.size() - 1, 1);
					} catch (ArrayIndexOutOfBoundsException e) {
						out_date.add(chat_date.get(j));
						count.add(1);
					}
				}
				ArrayList<Integer> sum = new ArrayList<>();
				for (int j = 0; j < count.size(); j++) {
					sum.add(0);
					for (int k = 0; k < j + 1; k++) {
						sum.set(j, sum.get(j) + count.get(k));
					}
				}
				MainGui.getInstance().progressFinish();
				MainGui.getInstance().initProgressBar(out_date.size());
				if (Config.public_config.OUTPUT_STYLE == 1) {
					OutputManager.saveToCsv(new String[] { "月份", "该月弹幕总数", "累计弹幕总数" },
							new ArrayList<?>[] { out_date, count, sum }, true);
				} else {

					String[][] strings = new String[out_date.size()][3];
					for (int j = 0; j < out_date.size(); j++) {
						MainGui.getInstance().refreshProgressBar(j);
						strings[j][0] = out_date.get(j);
						strings[j][1] = count.get(j).toString();
						strings[j][2] = sum.get(j).toString();
					}
					new Table(new String[] { "月份", "该月弹幕总数", "累计弹幕总数" }, strings).setVisible(true);
				}
				MainGui.getInstance().progressFinish();
			} else if (tab == 5) {
				MainGui.getInstance().initProgressBar(files.length);
				Chat chat = XmlLoader.loadChatFromXml(files);
				MainGui.getInstance().progressFinish();
				ChatUtil.utilChat(chat);
				MainGui.getInstance().initProgressBar(chat.getCount());
				ArrayList<Long> date = chat.getDate();
				ArrayList<Long> date2 = new ArrayList<>();
				int i;
				long min = Long.MAX_VALUE;
				int min_index = 0;
				while (true) {
					for (i = 0; i < date.size(); i++) {
						if (date.get(i) < min) {
							min = date.get(i);
							min_index = i;
						}
					}
					if (min == Long.MAX_VALUE) {
						break;
					}
					MainGui.getInstance().refreshProgressBar(date2.size());
					date2.add(date.get(min_index));
					date.set(min_index, Long.MAX_VALUE);
					min = Long.MAX_VALUE;
				}
				MainGui.getInstance().progressFinish();
				// 时间戳转换
				MainGui.getInstance().initProgressBar(date2.size());
				ArrayList<String> chat_date = new ArrayList<>();
				for (int j = 0; j < date2.size(); j++) {
					chat_date.add(new SimpleDateFormat("yyyy-MM-dd").format(new Date(date2.get(j) * 1000)));
				}
				ArrayList<String> out_date = new ArrayList<>();
				ArrayList<Integer> count = new ArrayList<>();
				tick: for (int j = 0; j < chat_date.size(); j++) {
					MainGui.getInstance().refreshProgressBar(j);
					for (int k = 0; k < out_date.size(); k++) {
						if (chat_date.get(j).equals(out_date.get(k))) {
							count.set(out_date.indexOf(chat_date.get(j)),
									count.get(out_date.indexOf(chat_date.get(j))) + 1);
							continue tick;
						}
					}
					try {
						int days = 1;
						while (!out_date.get(out_date.size() - 1).equals(chat_date.get(j))) {
							Calendar calendar = new GregorianCalendar();
							calendar.setTime(new Date(date2.get(j - 1) * 1000));
							calendar.add(Calendar.DATE, days);
							out_date.add(new SimpleDateFormat("yyyy-MM-dd").format(calendar.getTime()));
							count.add(0);
							days++;
						}
						count.set(count.size() - 1, 1);
					} catch (ArrayIndexOutOfBoundsException e) {
						out_date.add(chat_date.get(j));
						count.add(1);
					}
				}
				ArrayList<Integer> sum = new ArrayList<>();
				for (int j = 0; j < count.size(); j++) {
					sum.add(0);
					for (int k = 0; k < j + 1; k++) {
						sum.set(j, sum.get(j) + count.get(k));
					}
				}
				MainGui.getInstance().progressFinish();
				MainGui.getInstance().initProgressBar(out_date.size());
				if (Config.public_config.OUTPUT_STYLE == 1) {
					OutputManager.saveToCsv(new String[] { "日期", "该日弹幕总数", "累计弹幕总数" },
							new ArrayList<?>[] { out_date, count, sum }, true);
				} else {

					String[][] strings = new String[out_date.size()][3];
					for (int j = 0; j < out_date.size(); j++) {
						MainGui.getInstance().refreshProgressBar(j);
						strings[j][0] = out_date.get(j);
						strings[j][1] = count.get(j).toString();
						strings[j][2] = sum.get(j).toString();
					}
					new Table(new String[] { "日期", "该日弹幕总数", "累计弹幕总数" }, strings).setVisible(true);
				}
				MainGui.getInstance().progressFinish();
			} else if (tab == 6) {
				MainGui.getInstance().initProgressBar(files.length);
				Chat chat = XmlLoader.loadChatFromXml(files);
				MainGui.getInstance().progressFinish();
				ChatUtil.utilChat(chat);
				MainGui.getInstance().initProgressBar(chat.getCount());
				ArrayList<Long> date = chat.getDate();
				ArrayList<Integer> date_seconds = new ArrayList<>();
				ArrayList<Integer> date_seconds_2 = new ArrayList<>();
				for (int i = 0; i < date.size(); i++) {
					String temp = new SimpleDateFormat("HH:mm:ss").format(new Date(date.get(i) * 1000));
					String parts[] = temp.split(":");
					date_seconds.add(Integer.parseInt(parts[0]) * 3600 + Integer.parseInt(parts[1]) * 60
							+ Integer.parseInt(parts[2]));
				}
				int i;
				int progress = 0;
				int min = 2147483647;
				int min_index = 0;
				while (true) {
					for (i = 0; i < date_seconds.size(); i++) {
						if (date_seconds.get(i) < min) {
							min = date_seconds.get(i);
							min_index = i;
						}
					}
					if (min == 2147483647) {
						break;
					}
					MainGui.getInstance().refreshProgressBar(progress);
					if (min < Config.tab6.START_TIME) {
						min = 2147483647;
						date_seconds.remove(min_index);
						progress++;
						continue;
					}
					if (Config.tab6.END_TIME != -1) {
						if (min > Config.tab6.END_TIME) {
							break;
						}
					}
					progress++;
					date_seconds_2.add(date_seconds.get(min_index));
					date_seconds.set(min_index, 2147483647);
					min = 2147483647;
				}
				MainGui.getInstance().progressFinish();
				// 分组统计
				// 为避免用户作死，把采样长度设置过大、设置为0或者负数等等，此处需要处理异常
				if (Config.tab6.LENGTH <= 0) {
					MainGui.getInstance().notifyXmlHandlingException(new IllegalArgumentException("采样长度不能小于或等于0。"));
					throw new InterruptedException();
				}
				if (Config.tab6.START_TIME < 0) {
					MainGui.getInstance().notifyXmlHandlingException(new IllegalArgumentException("起始时间不能小于0。"));
					throw new InterruptedException();
				}
				if (Config.tab6.END_TIME <= 0 && Config.tab6.END_TIME != -1) {
					MainGui.getInstance()
							.notifyXmlHandlingException(new IllegalArgumentException("终止时间不能小于或等于0（设置为-1除外）。"));
					throw new InterruptedException();
				}

				if (Config.tab6.END_TIME != -1 && Config.tab6.START_TIME >= Config.tab6.END_TIME) {
					MainGui.getInstance()
							.notifyXmlHandlingException(new IllegalArgumentException("起始时间还没有终止时间大，你是想时  间  倒  流吗？"));
					throw new InterruptedException();
				}

				if (date_seconds_2.size() == 0) {
					MainGui.getInstance().notifyXmlHandlingException(
							new IllegalArgumentException("组数为0。可能由以下原因导致：\n1）xml弹幕文件无弹幕\n2）起始时间和终止时间过近\n3）起始时间过大"));
					throw new InterruptedException();
				}

				if ((int) (Math.ceil(((double) (date_seconds_2.get(date_seconds_2.size() - 1) - Config.tab6.START_TIME))
						/ Config.tab6.LENGTH)) > 65535) {
					MainGui.getInstance()
							.notifyXmlHandlingException(new IllegalArgumentException("组数过大（超过65535），请调高采样长度。"));
					throw new InterruptedException();
				}
				MainGui.getInstance().initProgressBar(date_seconds_2.size());
				int[] group_value = new int[(int) (Math
						.ceil(((double) (date_seconds_2.get(date_seconds_2.size() - 1) - Config.tab6.START_TIME))
								/ Config.tab6.LENGTH))];
				ArrayList<String> text = new ArrayList<>();
				for (int j = 0; j < group_value.length; j++) {
					group_value[j] = 0;
					if (j != group_value.length - 1) {
						int time_1 = Config.tab6.START_TIME + Config.tab6.LENGTH * j;
						int time_2 = Config.tab6.START_TIME + Config.tab6.LENGTH * (j + 1);
						String out = "";
						if (time_1 >= 3600) {
							if ((int) time_1 / 3600 < 10) {
								out += 0;
							}
							out += (int) time_1 / 3600;
							out += ":";
							time_1 = time_1 % 3600;
						} else {
							out += "00:";
						}
						if (time_1 >= 60) {
							if ((int) time_1 / 60 < 10) {
								out += 0;
							}
							out += (int) time_1 / 60;
							out += ":";
							time_1 = time_1 % 60;
						} else {
							out += "00:";
						}
						if (time_1 < 10) {
							out += 0;
						}
						out += time_1;
						out += "  -  ";
						if (time_2 >= 3600) {
							if ((int) time_2 / 3600 < 10) {
								out += 0;
							}
							out += (int) time_2 / 3600;
							out += ":";
							time_2 = time_2 % 3600;
						} else {
							out += "00:";
						}
						if (time_2 >= 60) {
							if ((int) time_2 / 60 < 10) {
								out += 0;
							}
							out += (int) time_2 / 60;
							out += ":";
							time_2 = time_2 % 60;
						} else {
							out += "00:";
						}
						if (time_2 < 10) {
							out += 0;
						}
						out += time_2;
						text.add(out);
					} else if (Config.tab6.END_TIME != -1) {
						int time_1 = Config.tab6.START_TIME + Config.tab6.LENGTH * j;
						int time_2 = date_seconds_2.get(date_seconds_2.size() - 1);
						String out = "";
						if (time_1 >= 3600) {
							if ((int) time_1 / 3600 < 10) {
								out += 0;
							}
							out += (int) time_1 / 3600;
							out += ":";
							time_1 = time_1 % 3600;
						} else {
							out += "00:";
						}
						if (time_1 >= 60) {
							if ((int) time_1 / 60 < 10) {
								out += 0;
							}
							out += (int) time_1 / 60;
							out += ":";
							time_1 = time_1 % 60;
						} else {
							out += "00:";
						}
						if (time_1 < 10) {
							out += 0;
						}
						out += time_1;
						out += "  -  ";
						if (time_2 >= 3600) {
							if ((int) time_2 / 3600 < 10) {
								out += 0;
							}
							out += (int) time_2 / 3600;
							out += ":";
							time_2 = time_2 % 3600;
						} else {
							out += "00:";
						}
						if (time_2 >= 60) {
							if ((int) time_2 / 60 < 10) {
								out += 0;
							}
							out += (int) time_2 / 60;
							out += ":";
							time_2 = time_2 % 60;
						} else {
							out += "00:";
						}
						if (time_2 < 10) {
							out += 0;
						}
						out += time_2;
						text.add(out);
					} else {
						int time_1 = Config.tab6.START_TIME + Config.tab6.LENGTH * j;
						String out = "";
						if (time_1 >= 3600) {
							if ((int) time_1 / 3600 < 10) {
								out += 0;
							}
							out += (int) time_1 / 3600;
							out += ":";
							time_1 = time_1 % 3600;
						} else {
							out += "00:";
						}
						if (time_1 >= 60) {
							if ((int) time_1 / 60 < 10) {
								out += 0;
							}
							out += (int) time_1 / 60;
							out += ":";
							time_1 = time_1 % 60;
						} else {
							out += "00:";
						}
						if (time_1 < 10) {
							out += 0;
						}
						out += time_1;
						out += "  -  00:00:00";
						text.add(out);
					}
				}
				for (int j = 0; j < date_seconds_2.size(); j++) {
					MainGui.getInstance().refreshProgressBar(j);
					try {
						group_value[(int) (((double) (date_seconds_2.get(j) - Config.tab6.START_TIME)
								/ Config.tab6.LENGTH))]++;
					} catch (ArrayIndexOutOfBoundsException e) {
						System.err.println("error");
						group_value[(int) (((double) (date_seconds_2.get(j) - Config.tab6.START_TIME)
								/ Config.tab6.LENGTH)) - 1]++;
					}
				}
				MainGui.getInstance().progressFinish();
				MainGui.getInstance().initProgressBar(group_value.length);
				ArrayList<Integer> group = new ArrayList<>();
				for (int j = 0; j < group_value.length; j++) {
					group.add(group_value[j]);
				}
				if (Config.public_config.OUTPUT_STYLE == 1) {
					OutputManager.saveToCsv(new String[] { "时间段", "弹幕数量" }, new ArrayList<?>[] { text, group }, true);
				} else {
					String[][] strings = new String[group_value.length][2];
					for (int j = 0; j < group_value.length; j++) {
						MainGui.getInstance().refreshProgressBar(j);
						strings[j][0] = text.get(j);
						strings[j][1] = group.get(j).toString();
					}
					new Table(new String[] { "时间段", "弹幕数量" }, strings).setVisible(true);
				}
				MainGui.getInstance().progressFinish();
			} else {
				throw new IllegalArgumentException();
			}
		} catch (InterruptedException e) {
			return;
		}
	}
}
