package script;

import java.awt.Desktop;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.json.JSONArray;

import gui.Dialog;
import gui.MainGui;

/**
 * 文件输出处理类
 */
public class OutputManager {
	private static File file;

	/**
	 * 获取配置好的文件
	 * 
	 * @return 文件
	 */
	public static File getFile() {
		return file;
	}

	/**
	 * 设置文件
	 * 
	 * @param file 文件
	 */
	public static void setFile(File file) {
		OutputManager.file = file;
	}

	/**
	 * 保存到csv文件
	 * 
	 * @param titles   表头，可为null
	 * @param lists    数据列表
	 * @param autoOpen 是否自动打开保存的文件
	 * @throws InterruptedException
	 */
	public static void saveToCsv(String[] titles, ArrayList<?>[] lists, boolean autoOpen) throws InterruptedException {
		FileOutputStream out = null;
		OutputStreamWriter osw = null;
		BufferedWriter bw = null;
		if (!file.exists()) {
			try {
				file.createNewFile();
			} catch (IOException e) {
				e.printStackTrace();
				MainGui.getInstance().notifyXmlHandlingException(e);
				throw new InterruptedException();
			}
		}
		try {
			out = new FileOutputStream(file);
			osw = new OutputStreamWriter(out);
			bw = new BufferedWriter(osw);
			if (titles != null) {
				for (int i = 0; i < titles.length - 1; i++) {
					bw.append(titles[i] + ",");
				}
				bw.append(titles[titles.length - 1]).append("\n");
			}
			for (int i = 0; i < lists[0].size(); i++) {
				if (autoOpen) {
					MainGui.getInstance().refreshProgressBar(i);
				}
				for (int j = 0; j < lists.length - 1; j++) {
					if (!(lists[j].get(0) instanceof String)) {
						bw.append(lists[j].get(i).toString() + ",");
					} else if (lists[j].get(i).toString().contains(",") || lists[j].get(i).toString().contains(",")) {
						String string = lists[j].get(i).toString();
						string = string.replace("\"", "\"\"");
						bw.append("\"" + string + "\",");
					} else {
						bw.append(lists[j].get(i) + ",");
					}
				}
				if (!(lists[lists.length - 1].get(0) instanceof String)) {
					bw.append(lists[lists.length - 1].get(i).toString() + "\n");
				} else if (lists[lists.length - 1].get(i).toString().contains(",")
						|| lists[lists.length - 1].get(i).toString().contains(",")) {
					String string = lists[lists.length - 1].get(i).toString();
					string = string.replace("\"", "\"\"");
					bw.append("\"" + string + "\"\n");
				} else {
					bw.append(lists[lists.length - 1].get(i) + "\n");
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
			MainGui.getInstance().notifyXmlHandlingException(e);
			throw new InterruptedException();
		} finally {
			if (bw != null) {
				try {
					bw.close();
					bw = null;
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			if (osw != null) {
				try {
					osw.close();
					osw = null;
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			if (out != null) {
				try {
					out.close();
					out = null;
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		if (autoOpen) {
			try {
				Desktop.getDesktop().open(file);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	/**
	 * 保存到json文件
	 * 
	 * @param jsonArray 要保存的json数组
	 */
	public static boolean saveToJson(JSONArray jsonArray) {
		try {
			file = new File(OutputManager.getFile().getPath() + ".json");
			if (file.exists()) {
				file.delete();
			}
			file.createNewFile();
			Writer writer = new OutputStreamWriter(new FileOutputStream(file), "UTF-8");
			writer.write(jsonArray.toString());
			writer.flush();
			writer.close();
			return true;
		} catch (IOException e) {
			e.printStackTrace();
			new Dialog("输出失败", "很抱歉，输出为json失败。\n\n" + e.getClass().getName() + "\n" + e.getMessage()).setVisible(true);
			return false;
		}
	}

	/**
	 * 保存到xml文件
	 * 
	 * @param chat  弹幕实体类对象
	 * @param color 颜色数组
	 */
	public static boolean saveToXml(Chat chat, int[] color) {
		return saveToXml(chat, color, file.getPath() + ".xml");
	}

	/**
	 * 保存到xml文件
	 * 
	 * @param chat     弹幕实体类对象
	 * @param color    颜色数组
	 * @param filePath 指定文件路径
	 */
	public static boolean saveToXml(Chat chat, int[] color, String filePath) {
		try {
			file = new File(filePath);
			file.mkdirs();
			if (file.exists()) {
				file.delete();
			}
			file.createNewFile();
			DecimalFormat df = new DecimalFormat("#.00000");
			Writer writer = new OutputStreamWriter(new FileOutputStream(file), "UTF-8");
			writer.write(
					"<?xml version=\"1.0\" encoding=\"UTF-8\"?><i><chatserver>chat.bilibili.com</chatserver><chatid>0</chatid><mission>0</mission><maxlimit>2147483647</maxlimit><state>0</state><real_name>0</real_name><source>n-a</source>");
			for (int i = 0; i < chat.getCount(); i++) {
				writer.write("<d p=\"");
				if (df.format(chat.getTime().get(i)).toString().charAt(0) == '.') {
					writer.write("0");
				}
				writer.write(df.format(chat.getTime().get(i)));
				writer.write(",1,25,");
				writer.write(color[i] + "");
				writer.write(",");
				writer.write(chat.getDate().get(i).toString());
				writer.write(",0,");
				writer.write(chat.getUsers().get(i));
				writer.write(",0\">");
				writer.write(chat.getChats().get(i));
				writer.write("</d>");
			}
			writer.write("</i>");
			writer.flush();
			writer.close();
			return true;
		} catch (IOException e) {
			e.printStackTrace();
			new Dialog("输出失败", "很抱歉，输出为xml失败。\n\n" + e.getClass().getName() + "\n" + e.getMessage()).setVisible(true);
			return false;
		}
	}

	/**
	 * 保存到xml文件
	 * 
	 * @param chatStr 字符串形式的弹幕
	 */
	public static boolean saveToXml(String chatStr) {
		return saveToXml(chatStr, file.getPath());
	}

	/**
	 * 保存到xml文件
	 * 
	 * @param chatStr  字符串形式的弹幕
	 * @param filePath 文件路径
	 */
	public static boolean saveToXml(String chatStr, String filePath) {
		File file = new File(filePath);
		try {
			file.mkdirs();
			if (file.exists()) {
				file.delete();
			}
			file.createNewFile();
			FileOutputStream fos = new FileOutputStream(filePath);
			fos.write(new byte[] { (byte) 0xEF, (byte) 0xBB, (byte) 0xBF });
			OutputStreamWriter osw = new OutputStreamWriter(fos, "UTF-8");
			BufferedWriter bw = new BufferedWriter(osw);
			bw.write(chatStr);
			bw.close();
			return true;
		} catch (IOException e) {
			e.printStackTrace();
			new Dialog("输出失败", "很抱歉，输出为xml失败。\n\n" + e.getClass().getName() + "\n" + e.getMessage()).setVisible(true);
			return false;
		}
	}

	/**
	 * 保存到xml文件(批量保存)
	 * 
	 * @param data      弹幕数组
	 * @param fileNames 文件路径数组
	 */
	@Deprecated
	public static void saveToXmls(String[] data, String[] fileNames) {
		file.mkdirs();
		for (int i = 0; i < data.length; i++) {
			try {
				File subFile = new File(file.getPath() + File.separator + fileNames[i]);
				FileOutputStream fos = new FileOutputStream(subFile);
				fos.write(new byte[] { (byte) 0xEF, (byte) 0xBB, (byte) 0xBF });
				FileWriter fileWriter = new FileWriter(subFile.getAbsoluteFile());
				BufferedWriter bw = new BufferedWriter(fileWriter);
				bw.write(data[i]);
				fos.close();
				bw.close();
			} catch (IOException e) {
				e.printStackTrace();
				new Dialog("输出失败", "很抱歉，输出为xml失败。\n\n" + e.getClass().getName() + "\n" + e.getMessage())
						.setVisible(true);
			}
		}
	}

	/**
	 * 替换非法的文件字符
	 * 
	 * @param fileName 文件名
	 * @return 合法的文件名
	 */
	public static String replaceFileName(String fileName) {
		Pattern pattern = Pattern.compile("[\\\\/:\\*\\?\\\"<>\\|]");
		Matcher matcher = pattern.matcher(fileName);
		return matcher.replaceAll("");
	}
}
