package script;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;

public class CsvReader {
	File file;

	public CsvReader(File csvFile) {
		file = csvFile;
	}

	// 此处代码必须在二十分钟内行云流水地敲出，否则时间久了就看不懂了23333
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
