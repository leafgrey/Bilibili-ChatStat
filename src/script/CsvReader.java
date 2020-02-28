package script;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;

/**
 * 读取csv文件的类
 */
public class CsvReader {
	File file;

	/**
	 * 创建对象
	 * 
	 * @param csvFile csv文件
	 */
	public CsvReader(File csvFile) {
		file = csvFile;
	}

	/*
	 * 不要问我为什么没有注释，我想补注释的时候结果看不懂了233
	 */
	/**
	 * 从csv文件中读取String二维数组
	 * 
	 * @return 二维数组
	 * @throws Exception 任意形式的异常，只要出现了异常就重新创建csv文件
	 */
	public String[][] getStringArray() throws Exception {
		String[] lines = readLines();
		ArrayList<String[]> arrayList = new ArrayList<>();
		for (int i = 0; i < lines.length; i++) {
			String line = lines[i];
			String[] split = line.split(",");
			if (split.length == 0) {
				split = new String[] { line };
			}
			ArrayList<String> processed = new ArrayList<>();
			int index = 0;
			while (index != split.length) {
				String temp = split[index];
				StringBuilder builder = new StringBuilder(temp);
				while (true) {
					int count = 0;
					char[] chars = builder.toString().toCharArray();
					for (int j = 0; j < chars.length; j++) {
						if (chars[j] == '"') {
							count++;
						}
					}
					// 引号成对出现
					if (count % 2 == 0) {
						index++;
						break;
					}
					builder.append(",");
					builder.append(split[index + 1]);
					index++;
				}
				String string = builder.toString();
				if (string.contains("\"")) {
					char[] chars = string.toCharArray();
					char[] newChars = new char[chars.length - 2];
					for (int j = 1; j < chars.length - 1; j++) {
						newChars[j - 1] = chars[j];
					}
					string = new String(newChars);
					string = string.replace("\"\"", "\"");
				}
				processed.add(string);
			}
			if (processed.size() == 1) {
				processed.add("");
			}
			arrayList.add(processed.toArray(new String[0]));
		}
		return arrayList.toArray(new String[0][]);
	}

	/**
	 * 读取行信息
	 * 
	 * @return 行
	 * @throws Exception 异常
	 */
	private String[] readLines() throws Exception {
		ArrayList<String> list = new ArrayList<>();
		BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(new FileInputStream(file)));
		String string;
		while ((string = bufferedReader.readLine()) != null) {
			list.add(string);
		}
		bufferedReader.close();
		return list.toArray(new String[0]);
	}
}
