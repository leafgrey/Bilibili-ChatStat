package script;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.DocumentHelper;
import org.dom4j.io.SAXReader;

import gui.MainGui;

public class XmlLoader {

	public static Chat loadChatFromXml(File[] files) {
		Chat chat = new Chat(new ArrayList<String>(), new ArrayList<String>(), new ArrayList<Float>(),
				new ArrayList<Long>());
		for (int i = 0; i < files.length; i++) {
			MainGui.getInstance().refreshProgressBar(i);
			Document document;
			try {
				document = loadFromFile(files[i].getPath());
			} catch (InterruptedException e) {
				return null;
			}
			org.dom4j.Element root = document.getRootElement();
			ArrayList<org.dom4j.Element> list = (ArrayList<org.dom4j.Element>) root.elements("d");
			ArrayList<String> chats = new ArrayList<>();
			ArrayList<String> users = new ArrayList<>();
			ArrayList<Float> time = new ArrayList<>();
			ArrayList<Long> date = new ArrayList<>();
			for (int j = 0; j < list.size(); j++) {
				chats.add(list.get(j).getText());
				String[] p = list.get(j).attributeValue("p").split(",");
				users.add(p[6]);
				time.add(Float.valueOf(p[0]));
				date.add(Long.parseLong(p[4]));
			}
			chat.append(new Chat(chats, users, time, date));
		}
		return chat;
	}

	public static Chat loadChatFromXml(String[] xmls) {
		Chat chat = new Chat(new ArrayList<String>(), new ArrayList<String>(), new ArrayList<Float>(),
				new ArrayList<Long>());
		for (int i = 0; i < xmls.length; i++) {
			Document document;
			try {
				document = loadFromString(xmls[i]);
			} catch (InterruptedException e) {
				return null;
			}
			org.dom4j.Element root = document.getRootElement();
			ArrayList<org.dom4j.Element> list = (ArrayList<org.dom4j.Element>) root.elements("d");
			ArrayList<String> chats = new ArrayList<>();
			ArrayList<String> users = new ArrayList<>();
			ArrayList<Float> time = new ArrayList<>();
			ArrayList<Long> date = new ArrayList<>();
			for (int j = 0; j < list.size(); j++) {
				chats.add(list.get(j).getText());
				String[] p = list.get(j).attributeValue("p").split(",");
				users.add(p[6]);
				time.add(Float.valueOf(p[0]));
				date.add(Long.parseLong(p[4]));
			}
			chat.append(new Chat(chats, users, time, date));
		}
		return chat;
	}

	public static Chat loadChatFromXml(String xml) {
		Chat chat = new Chat(new ArrayList<String>(), new ArrayList<String>(), new ArrayList<Float>(),
				new ArrayList<Long>());
		Document document;
		try {
			document = loadFromString(xml);
		} catch (InterruptedException e) {
			return null;
		}
		org.dom4j.Element root = document.getRootElement();
		ArrayList<org.dom4j.Element> list = (ArrayList<org.dom4j.Element>) root.elements("d");
		ArrayList<String> chats = new ArrayList<>();
		ArrayList<String> users = new ArrayList<>();
		ArrayList<Float> time = new ArrayList<>();
		ArrayList<Long> date = new ArrayList<>();
		for (int j = 0; j < list.size(); j++) {
			chats.add(list.get(j).getText());
			String[] p = list.get(j).attributeValue("p").split(",");
			users.add(p[6]);
			time.add(Float.valueOf(p[0]));
			date.add(Long.parseLong(p[4]));
		}
		chat.append(new Chat(chats, users, time, date));
		return chat;
	}

	private static Document loadFromFile(String path) throws InterruptedException {
		Document document = null;
		try {
			SAXReader saxReader = new SAXReader();
			document = saxReader.read(new File(path));
		} catch (DocumentException e) {
			try {
				byte[] bytes = null;
				InputStream is = new BufferedInputStream(new FileInputStream(new File(path)));
				ByteArrayOutputStream baos = new ByteArrayOutputStream();
				byte[] flush = new byte[1024];
				int len = -1;
				while ((len = is.read(flush)) != -1) {
					baos.write(flush, 0, len);
				}
				is.close();
				baos.flush();
				bytes = baos.toByteArray();
				// 删除3字节的BOM
				byte[] newBytes = new byte[bytes.length - 3];
				for (int i = 3; i < bytes.length; i++) {
					newBytes[i - 3] = bytes[i];
				}
				return loadFromString(replaceChar(new String(newBytes, "UTF-8")));
			} catch (IOException e1) {
				MainGui.getInstance().notifyXmlHandlingException(e1);
			}

		} catch (Exception ex) {
			MainGui.getInstance().notifyXmlHandlingException(ex);
		}
		return document;
	}

	private static Document loadFromString(String str) throws InterruptedException {
		Document document = null;
		try {
			document = DocumentHelper.parseText(str);
		} catch (DocumentException e) {
			try {
				return DocumentHelper.parseText(replaceChar(str));
			} catch (DocumentException e1) {
				MainGui.getInstance().notifyXmlHandlingException(e1);
			}
		} catch (Exception ex) {
			MainGui.getInstance().notifyXmlHandlingException(ex);
		}
		return document;
	}

	/*
	 * 在爬取鹿乃的日文版勾指起誓的弹幕时遇到了问题，有人发送了0xffff这个Unicode字符，API网页提示错误，导致xml解析出错，
	 * 解决方案是替换掉无效字符。 （所以我是不是还得感谢鹿乃和那个发送弹幕的人帮我找到了程序的bug23333）
	 */
	private static String replaceChar(String str) {
		char[] chars = str.toCharArray();
		for (int i = 0; i < chars.length; i++) {
			if (chars[i] == 0x9 || chars[i] == 0xA || chars[i] == 0xD || (chars[i] >= 0x20 && chars[i] <= 0xD7FF)
					|| (chars[i] >= 0xE000 && chars[i] <= 0xFFFD) || (chars[i] >= 0x10000 && chars[i] <= 0x10FFFF)) {
				continue;
			}
			chars[i] = ' ';
		}
		return new String(chars);
	}

}
