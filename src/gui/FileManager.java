package gui;

import java.awt.Component;
import java.io.File;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.JFileChooser;
import javax.swing.JList;
import javax.swing.ListModel;
import javax.swing.filechooser.FileNameExtensionFilter;

import script.OutputManager;

/**
 * 用于控制文件选择器的类
 */
public class FileManager {
	ListModel<String> listModel;
	ArrayList<String> arrayList;
	private static File defaultDir;
	private static boolean dirExists = true;// 用于选择文件夹，指示文件夹原来是否存在，如果原来不存在，在取消选择后需删除文件夹

	/**
	 * 展示打开文件列表的对话框
	 * 
	 * @param parent 父控件
	 * @param list   JList对象
	 */
	public void showFileOpenDialog(Component parent, JList<String> list) {
		JFileChooser fileChooser = new JFileChooser();
		fileChooser.setCurrentDirectory(new File("."));
		fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
		fileChooser.setMultiSelectionEnabled(true);
		fileChooser.setFileFilter(new FileNameExtensionFilter("xml（哔哩哔哩弹幕文件）", "xml"));
		int result = fileChooser.showOpenDialog(parent);
		listModel = list.getModel();
		arrayList = new ArrayList<>();
		for (int i = 0; i < listModel.getSize(); i++) {
			arrayList.add(listModel.getElementAt(i));
		}
		if (result == JFileChooser.APPROVE_OPTION) {
			File[] files = fileChooser.getSelectedFiles();
			tick: for (int i = 0; i < files.length; i++) {
				String name = files[i].getName();
				String[] strArray = name.split("\\.");
				int suffixIndex = strArray.length - 1;// 后缀名
				if (!strArray[suffixIndex].matches("[Xx][Mm][Ll]")) {
					new Dialog("导入xml文件错误", files[i].getPath() + " 不是xml文件。\n已终止本次导入，请选择xml格式哔哩哔哩弹幕文件。")
							.setVisible(true);
					return;
				}
				// 判断添加的文件是否已在列表中存在
				for (int j = 0; j < listModel.getSize(); j++) {
					if (files[i].getPath().equals(listModel.getElementAt(j))) {
						continue tick;
					}
				}
				// 添加新文件
				arrayList.add(files[i].getPath());
			}
			list.setListData(arrayList.toArray(new String[0]));
		}
	}

	/**
	 * 打开文件选择器，用于读取txt文件
	 * 
	 * @param parent 父控件
	 */
	public void showFileOpenDialogForTxt(Component parent) {
		JFileChooser fileChooser = new JFileChooser();
		fileChooser.setCurrentDirectory(new File("."));
		fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
		fileChooser.setMultiSelectionEnabled(false);
		fileChooser.setFileFilter(new FileNameExtensionFilter(".txt", "txt"));
		int result = fileChooser.showOpenDialog(parent);
		if (result == JFileChooser.APPROVE_OPTION) {
			File file = fileChooser.getSelectedFile();
			MainGui.getInstance().setTxtFile(file);
			MainGui.getInstance().log("已选定txt文件： " + file.getPath());
		}
	}

	/**
	 * 展示保存直播弹幕的对话框
	 * 
	 * @param parent   父控件
	 * @param fileName 文件名（不包含后缀）
	 * @param mode     模式，0：输出为csv文件；1：直播弹幕输出；2：单视频弹幕输出（单P）；3：输出至目录
	 */
	public static void showFileSaveDialog(Component parent, String fileName, int mode) {
		JFileChooser fileChooser = new JFileChooser();
		if (mode == 0) {
			fileChooser.setSelectedFile(new File(replaceFileName(fileName) + ".csv"));
			fileChooser.setFileFilter(new FileNameExtensionFilter("*.csv", "csv"));
		} else if (mode == 1) {
			fileChooser.setSelectedFile(new File(replaceFileName(fileName)));
			fileChooser.setFileFilter(new FileNameExtensionFilter("*.*（输出时自动补充后缀名）", ".*"));
		} else if (mode == 2) {
			fileChooser.setSelectedFile(new File(replaceFileName(fileName) + ".xml"));
			fileChooser.setFileFilter(new FileNameExtensionFilter("*.xml", "xml"));
		} else if (mode == 3) {
			fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
			File dir = new File(fileChooser.getCurrentDirectory() + "\\" + replaceFileName(fileName) + "\\");
			defaultDir = dir;
			dirExists = dir.exists();
			dir.mkdir();
			fileChooser.setCurrentDirectory(dir);
		}
		int result = fileChooser.showSaveDialog(parent);

		if (result == JFileChooser.APPROVE_OPTION) {
			File file = fileChooser.getSelectedFile();
			OutputManager.setFile(file);
			// 删除临时创建的文件夹
			if (OutputManager.getFile() == null) {
				if (!dirExists) {
					deleteTempDir();
				}
			}
		}
	}

	/**
	 * 替换不合法的字符
	 * 
	 * @param fileName 文件名
	 * @return 合法的文件名
	 */
	public static String replaceFileName(String fileName) {
		Pattern pattern = Pattern.compile("[\\\\/:\\*\\?\\\"<>\\|]");
		Matcher matcher = pattern.matcher(fileName);
		return matcher.replaceAll("");
	}

	/**
	 * 删除临时创建的文件夹
	 */
	public static void deleteTempDir() {
		if (defaultDir != null && !dirExists) {
			defaultDir.delete();
			dirExists = true;
			defaultDir = null;
		}
	}

}
