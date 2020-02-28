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

public class OutputManager {
	private static File file;

	public static File getFile() {
		return file;
	}

	public static void setFile(File file) {
		OutputManager.file = file;
	}

	public static void saveToCsv(String[] titles, ArrayList<?>[] lists, boolean autoOpen) {
		FileOutputStream out = null;
		OutputStreamWriter osw = null;
		BufferedWriter bw = null;
		if (!file.exists()) {
			try {
				file.createNewFile();
			} catch (IOException e) {
				e.printStackTrace();
				MainGui.getInstance().notifyXmlHandlingException(e);
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
						bw.append(lists[j].get(i).toString());
					} else if (lists[j].get(i).toString().contains(",") || lists[j].get(i).toString().contains(",")) {
						String string = lists[j].get(i).toString();
						string = string.replace("\"", "\"\"");
						bw.append("\"" + string + "\",");
					} else {
						bw.append("\"" + lists[j].get(i) + "\",");
					}
				}
				if (!(lists[lists.length - 1].get(0) instanceof String)) {
					bw.append(lists[lists.length - 1].get(i).toString() + "\n");
				} else if (lists[lists.length - 1].get(i).toString().contains(",")
						|| lists[lists.length - 1].get(i).toString().contains(",")) {
					String string = lists[lists.length - 1].get(i).toString();
					string = string.replace("\"", "\"\"");
					bw.append("\"" + string + "\",\n");
				} else {
					bw.append("\"" + lists[lists.length - 1].get(i) + "\",\n");
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
			MainGui.getInstance().notifyXmlHandlingException(e);
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

	public static void saveToJson(JSONArray jsonArray) {
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
		} catch (IOException e) {
			e.printStackTrace();
			new Dialog("Êä³öÊ§°Ü", "ºÜ±§Ç¸£¬Êä³öÎªjsonÊ§°Ü¡£\n\n" + e.getClass().getName() + "\n" + e.getMessage()).setVisible(true);
		}
	}

	public static void saveToXml(Chat chat) {
		saveToXml(chat, file.getPath() + ".xml");
	}

	public static void saveToXml(Chat chat, String filePath) {
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
				writer.write(",1,25,0,");
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
		} catch (IOException e) {
			e.printStackTrace();
			new Dialog("Êä³öÊ§°Ü", "ºÜ±§Ç¸£¬Êä³öÎªxmlÊ§°Ü¡£\n\n" + e.getClass().getName() + "\n" + e.getMessage()).setVisible(true);
		}
	}

	public static void saveToXml(String chatStr) {
		saveToXml(chatStr, file.getPath());
	}

	public static void saveToXml(String chatStr, String filePath) {
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
		} catch (IOException e) {
			e.printStackTrace();
			new Dialog("Êä³öÊ§°Ü", "ºÜ±§Ç¸£¬Êä³öÎªxmlÊ§°Ü¡£\n\n" + e.getClass().getName() + "\n" + e.getMessage()).setVisible(true);
		}
	}

	@Deprecated
	public static void saveToXmls(String[] data, String[] fileNames) {
		file.mkdirs();
		for (int i = 0; i < data.length; i++) {
			try {
				File subFile = new File(file.getPath() + "\\" + fileNames[i]);
				FileOutputStream fos = new FileOutputStream(subFile);
				fos.write(new byte[] { (byte) 0xEF, (byte) 0xBB, (byte) 0xBF });
				FileWriter fileWriter = new FileWriter(subFile.getAbsoluteFile());
				BufferedWriter bw = new BufferedWriter(fileWriter);
				bw.write(data[i]);
				fos.close();
				bw.close();
			} catch (IOException e) {
				e.printStackTrace();
				new Dialog("Êä³öÊ§°Ü", "ºÜ±§Ç¸£¬Êä³öÎªxmlÊ§°Ü¡£\n\n" + e.getClass().getName() + "\n" + e.getMessage()).setVisible(true);
			}
		}
	}

	public static String replaceFileName(String fileName) {
		Pattern pattern = Pattern.compile("[\\\\/:\\*\\?\\\"<>\\|]");
		Matcher matcher = pattern.matcher(fileName);
		return matcher.replaceAll("");
	}
}
