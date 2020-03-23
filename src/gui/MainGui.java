package gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.EventQueue;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.ListModel;
import javax.swing.ScrollPaneConstants;
import javax.swing.TransferHandler;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import script.Config;
import script.CsvReader;
import script.OutputManager;
import script.RunScript;
import script.Spider;

/**
 * 主窗口
 */
public class MainGui implements Runnable {

	private JFrame frame;
	private static MainGui instance;
	FileManager fileManager;
	private JLabel label_progress_1;
	private JLabel label_progress_2;
	private JProgressBar progressBar_1;
	private JProgressBar progressBar_2;
	private JButton button_start;
	private JTabbedPane tabbedPane;
	private TabPanel tab1;
	private TabPanel tab2;
	private TabPanel tab3;
	private TabPanel tab4;
	private TabPanel tab5;
	private TabPanel tab6;
	private JPanel tab7;
	private JPanel tab8;
	private JList<String> list;
	private JButton button_add_files;
	private JButton button_delete_files;
	private Thread thread;
	private Thread spiderThread;
	// 选项卡文字
	private String[] titles = { "重复弹幕统计", "弹幕频数曲线统计", "弹幕刷屏统计", "月弹幕数量曲线统计", "日弹幕数量曲线统计", "日均弹幕活跃时段统计" };
	// 第一进度条的步骤文字
	private String[][] texts1 = { { "正在读取xml文件", "正在进行重复弹幕统计", "正在进行排名", "正在输出xml" },
			{ "正在读取xml文件", "正在进行弹幕排序", "正在进行弹幕分组", "正在输出xml" }, { "正在读取xml文件", "正在统计相同用户", "正在进行排名", "正在输出xml" },
			{ "正在读取xml文件", "正在进行弹幕排序", "正在进行弹幕分组", "正在输出xml" }, { "正在读取xml文件", "正在进行弹幕排序", "正在进行弹幕分组", "正在输出xml" },
			{ "正在读取xml文件", "正在进行弹幕排序", "正在进行弹幕分组", "正在输出xml" } };
	// 第一进度条的步骤文字（可选）
	private String[] text2 = { "正在转换为全角符号", "正在删除首尾空格", "正在进行弹幕分割", "正在进行高级弹幕匹配", "正在合并同一用户发的相同弹幕" };
	private ArrayList<String> processes = new ArrayList<>();
	private int process = -1;
	private JTabbedPane tabbedPane_up;
	private JPanel spider_panel;
	private JScrollPane scroll_info;
	private JList<String> list_info;
	private JPanel panel_info;
	private JPanel panel_av;
	private JLabel label_av;
	private JTextField field_av;
	private JButton button_av;
	private JPanel panel_import;
	private JLabel label_import;
	private JButton button_import;
	private JButton button_import_2;
	private JPanel panel_uid;
	private JLabel label_uid;
	private JTextField field_uid;
	private JButton button_uid;
	private JButton button_confirm;
	private JPanel panel_confirm;
	private JPanel panel_add_files;
	private JPanel panel_delete_files;
	private boolean selected = false;
	private JTextArea about;
	private JScrollPane scroll_set;
	private JTable table;
	private int status = 0;
	private JPanel panel;
	private JButton button_1;
	private JButton button_2;
	private File txtFile = null;
	private ArrayList<String> logs;
	private ImageIcon icon;
	private JButton button_clear;
	private boolean long_clicked = false;

	/**
	 * 主方法
	 * 
	 * @param args 参数列表
	 */
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					MainGui window = new MainGui();
					window.frame.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	/**
	 * 创建主窗口
	 */
	public MainGui() {
		initialize();
		instance = this;
	}

	/**
	 * 初始化
	 */
	@SuppressWarnings("unchecked")
	private void initialize() {
		Thread jsonThread = new Thread(this);
		logs = new ArrayList<>();
		icon = new ImageIcon(MainGui.class.getResource("/img/icon.png"));
		frame = new JFrame();
		frame.setIconImage(icon.getImage());
		fileManager = new FileManager();
		frame.setTitle("ChatStat - 哔哩哔哩弹幕统计工具 " + Config.VERSION + " by JellyBlack");
		frame.setBounds(100, 100, 900, 700);
		frame.setLocationRelativeTo(null);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		button_start = new JButton();

		JPanel north_panel = new JPanel();
		frame.getContentPane().add(north_panel, BorderLayout.NORTH);
		north_panel.setLayout(new BorderLayout(0, 0));

		JTextArea textArea = new JTextArea();
		textArea.setBackground(new Color(240, 240, 240));
		textArea.setText(
				" 欢迎使用 ChatStat " + Config.VERSION + "！\n 使用教程请前往源码仓库：https://github.com/JellyBlack/Bilibili-ChatStat");
		textArea.setEditable(false);
		textArea.setLineWrap(true);
		north_panel.add(textArea, BorderLayout.NORTH);

		tabbedPane_up = new JTabbedPane(JTabbedPane.LEFT);
		north_panel.add(tabbedPane_up, BorderLayout.SOUTH);

		JPanel scroll_panel = new JPanel();
		tabbedPane_up.addTab("导入xml文件", null, scroll_panel, null);

		spider_panel = new JPanel();
		tabbedPane_up.addTab("视频弹幕爬取", null, spider_panel, null);
		spider_panel.setLayout(new BorderLayout(0, 0));

		list_info = new JList<String>();
		scroll_info = new JScrollPane(list_info);
		scroll_info.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
		spider_panel.add(scroll_info, BorderLayout.CENTER);

		panel_info = new JPanel();
		spider_panel.add(panel_info, BorderLayout.EAST);
		panel_info.setLayout(new GridLayout(0, 1, 0, 0));

		panel_av = new JPanel();
		FlowLayout flowLayout_2 = (FlowLayout) panel_av.getLayout();
		flowLayout_2.setAlignment(FlowLayout.LEFT);
		panel_info.add(panel_av);

		label_av = new JLabel("爬取单视频 - 键入av号：av");
		panel_av.add(label_av);

		field_av = new JTextField();
		panel_av.add(field_av);
		field_av.setColumns(10);

		button_av = new JButton("选定");
		button_av.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				if (!selected) {
					try {
						Integer.parseInt(field_av.getText());
					} catch (NumberFormatException err) {
						new Dialog("输入错误", "请检查输入").setVisible(true);
						return;
					}
					selected = true;
					Config.spider_config.avs = new int[] { Integer.parseInt(field_av.getText()) };
					Config.spider_config.mode = 0;
					log("已选定单视频爬取");
					button_av.setText("撤销");
					button_import_2.setEnabled(false);
					button_uid.setEnabled(false);
					field_av.setEnabled(false);
					button_import.setEnabled(false);
					field_uid.setEnabled(false);
				} else {
					reset();
				}
			}
		});
		panel_av.add(button_av);

		panel_import = new JPanel();
		FlowLayout flowLayout_3 = (FlowLayout) panel_import.getLayout();
		flowLayout_3.setAlignment(FlowLayout.LEFT);
		panel_info.add(panel_import);

		label_import = new JLabel("导入txt列表");
		panel_import.add(label_import);

		button_import = new JButton("选择txt");
		button_import.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent arg0) {
				txtFile = null;
				fileManager.showFileOpenDialogForTxt(frame);
				if (txtFile == null) {
					return;
				}
				try {
					ArrayList<Integer> list = new ArrayList<>();
					BufferedReader bufferedReader = new BufferedReader(
							new InputStreamReader(new FileInputStream(txtFile)));
					String string;
					while ((string = bufferedReader.readLine()) != null) {
						String string2 = string.toLowerCase().replace("av", "");
						if (string2.isEmpty()) {
							continue;
						}
						try {
							int av = Integer.parseInt(string2);
							if (av <= 0) {
								log(string + " 导入失败，av号无法识别");
								continue;
							}
							list.add(av);
						} catch (NumberFormatException e) {
							log(string + " 导入失败，av号无法识别");
						}
					}
					int[] avs = new int[list.size()];
					for (int i = 0; i < list.size(); i++) {
						avs[i] = list.get(i);
					}
					Config.spider_config.avs = avs;
					log("导入完毕，共发现 " + avs.length + " 个av号");
					button_import.setText("重选txt");
					Config.spider_config.mode = 2;
					bufferedReader.close();
				} catch (IOException e) {
					e.printStackTrace();
					new Dialog("导入失败", "糟糕，发生了以下异常。\n\n" + e.getClass().getName() + "\n" + e.getMessage())
							.setVisible(true);
					;
				}
			}
		});
		panel_import.add(button_import);

		button_import_2 = new JButton("选定");
		button_import_2.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				if (!selected) {
					if (Config.spider_config.mode != 2) {
						new Dialog("未导入文件", "请先导入txt文件。").setVisible(true);
						return;
					}
					selected = true;
					Config.spider_config.mode = 1;
					log("已选定多视频爬取");
					button_import_2.setText("撤销");
					button_av.setEnabled(false);
					button_uid.setEnabled(false);
					field_av.setEnabled(false);
					button_import.setEnabled(false);
					field_uid.setEnabled(false);
				} else {
					reset();
				}
			}
		});
		panel_import.add(button_import_2);

		panel_uid = new JPanel();
		FlowLayout flowLayout_4 = (FlowLayout) panel_uid.getLayout();
		flowLayout_4.setAlignment(FlowLayout.LEFT);
		panel_info.add(panel_uid);

		label_uid = new JLabel("爬取UP主所有视频 - 键入uid：");
		panel_uid.add(label_uid);

		field_uid = new JTextField();
		panel_uid.add(field_uid);
		field_uid.setText("");
		field_uid.setColumns(10);

		button_uid = new JButton("选定");
		button_uid.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				if (!selected) {
					try {
						Integer.parseInt(field_uid.getText());
					} catch (NumberFormatException err) {
						new Dialog("输入错误", "请检查输入").setVisible(true);
						return;
					}
					selected = true;
					log("已选定爬取UP主的所有视频");
					Config.spider_config.uid = Integer.parseInt(field_uid.getText());
					Config.spider_config.mode = 2;
					button_uid.setText("撤销");
					button_av.setEnabled(false);
					button_import_2.setEnabled(false);
					field_av.setEnabled(false);
					button_import.setEnabled(false);
					field_uid.setEnabled(false);
				} else {
					reset();
				}
			}
		});
		panel_uid.add(button_uid);

		panel_confirm = new JPanel();
		FlowLayout flowLayout_5 = (FlowLayout) panel_confirm.getLayout();
		flowLayout_5.setVgap(12);
		panel_info.add(panel_confirm);

		button_confirm = new JButton("确定配置，下一步");
		button_confirm.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				if (!selected) {
					new Dialog("未选择模式", "请选择弹幕爬取模式（点击“选定”按钮）。").setVisible(true);
					return;
				}
				button_av.setEnabled(false);
				button_import_2.setEnabled(false);
				button_uid.setEnabled(false);
				button_confirm.setEnabled(false);
				spiderThread = new Thread(new Spider());
				spiderThread.start();
			}
		});

		button_clear = new JButton("清空日志");
		button_clear.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				if (long_clicked) {
					return;
				}
				logs.clear();
				list_info.setListData(logs.toArray(new String[0]));
			}
		});
		button_clear.addMouseListener(new MouseAdapter() {
			boolean thread_started = false;
			Thread thread2;
			Runnable runnable = new Runnable() {
				@Override
				public void run() {
					try {
						Thread.sleep(500);
					} catch (InterruptedException e) {
						return;
					}
					long_clicked = true;
					log("已强制刷新日志显示区");
				}
			};

			@Override
			public void mousePressed(MouseEvent e) {
				if (!thread_started) {
					thread2 = new Thread(runnable);
					thread2.start();
					thread_started = true;
				}
			}

			@Override
			public void mouseReleased(MouseEvent e) {
				thread2.interrupt();
				try {
					thread2.join();
				} catch (InterruptedException e1) {
					e1.printStackTrace();
				}
				thread_started = false;
				long_clicked = false;
			}
		});
		panel_confirm.add(button_clear);
		panel_confirm.add(button_confirm);

		LivePanel livePanel = new LivePanel();
		tabbedPane_up.addTab("直播弹幕抓取", null, livePanel, null);
		scroll_panel.setLayout(new BorderLayout(0, 0));

		list = new JList<>();
		list.setVisibleRowCount(5);
		// 实现文件拖拽
		list.setTransferHandler(new TransferHandler() {
			private static final long serialVersionUID = 1L;

			@Override
			public boolean importData(JComponent comp, Transferable t) {
				try {
					if (!list.isEnabled()) {
						return false;
					}
					Object o = t.getTransferData(DataFlavor.javaFileListFlavor);
					List<File> files = (List<File>) o;
					ArrayList<String> arrayList = new ArrayList<>();
					ListModel<String> listModel = list.getModel();
					for (int i = 0; i < listModel.getSize(); i++) {
						arrayList.add(listModel.getElementAt(i));
					}
					tick: for (int i = 0; i < files.size(); i++) {
						String name = files.get(i).getName();
						String[] strArray = name.split("\\.");
						int suffixIndex = strArray.length - 1;// 后缀名
						if (!strArray[suffixIndex].matches("[Xx][Mm][Ll]")) {
							new Dialog("导入xml文件错误", files.get(i).getPath() + " 不是xml文件。\n已终止本次导入，请选择xml格式哔哩哔哩弹幕文件。")
									.setVisible(true);
							return false;
						}
						// 判断添加的文件是否已在列表中存在
						for (int j = 0; j < listModel.getSize(); j++) {
							if (files.get(i).getPath().equals(listModel.getElementAt(j))) {
								continue tick;
							}
						}
						// 添加新文件
						arrayList.add(files.get(i).getPath());
					}
					list.setListData(arrayList.toArray(new String[0]));
					return true;
				} catch (Exception e) {
					e.printStackTrace();
				}
				return false;
			}

			@Override
			public boolean canImport(JComponent comp, DataFlavor[] flavors) {
				for (int i = 0; i < flavors.length; i++) {
					if (DataFlavor.javaFileListFlavor.equals(flavors[i])) {
						return true;
					}
				}
				return false;
			}
		});

		JScrollPane scrollPane = new JScrollPane(list);
		scroll_panel.add(scrollPane, BorderLayout.CENTER);
		scrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);

		JPanel control_panel = new JPanel();
		scroll_panel.add(control_panel, BorderLayout.EAST);
		control_panel.setLayout(new GridLayout(0, 1, 0, 0));

		panel_add_files = new JPanel();
		FlowLayout flowLayout = (FlowLayout) panel_add_files.getLayout();
		flowLayout.setVgap(45);
		control_panel.add(panel_add_files);

		button_add_files = new JButton("添加xml文件");
		panel_add_files.add(button_add_files);
		button_add_files.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				showFileOpenDialog(frame, list);
			}

			private void showFileOpenDialog(JFrame frame, JList<String> list) {
				fileManager.showFileOpenDialog(frame, list);
			}
		});

		panel_delete_files = new JPanel();
		FlowLayout flowLayout_1 = (FlowLayout) panel_delete_files.getLayout();
		flowLayout_1.setVgap(45);
		control_panel.add(panel_delete_files);

		button_delete_files = new JButton("删除选中的xml文件");
		panel_delete_files.add(button_delete_files);
		button_delete_files.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				ArrayList<String> arrayList = new ArrayList<>();
				ArrayList<String> arrayList_new = new ArrayList<>();
				int[] indices = list.getSelectedIndices();
				if (indices.length == 0) {
					new Dialog("删除xml文件出错", "您未选择任何xml文件。").setVisible(true);
					;
					return;
				}
				for (int i = 0; i < list.getModel().getSize(); i++) {
					arrayList.add(list.getModel().getElementAt(i));
				}
				tick: for (int i = 0; i < arrayList.size(); i++) {
					for (int j = 0; j < indices.length; j++) {
						if (i == indices[j]) {
							continue tick;
						}
					}
					arrayList_new.add(arrayList.get(i));
				}
				list.setListData(arrayList_new.toArray(new String[0]));
			}
		});

		tabbedPane = new JTabbedPane(JTabbedPane.LEFT);
		tabbedPane.addChangeListener(new ChangeListener() {
			@Override
			public void stateChanged(ChangeEvent e) {
				if (tabbedPane.getSelectedComponent() instanceof TabPanel) {
					((TabPanel) tabbedPane.getSelectedComponent()).refresh();
					button_start.setText("开始 「" + titles[tabbedPane.getSelectedIndex()] + "」");
					button_start.setEnabled(true);
				} else {
					button_start.setText("请选择功能");
					button_start.setEnabled(false);
				}
			}
		});
		frame.getContentPane().add(tabbedPane, BorderLayout.CENTER);

		tab1 = new TabPanel(1);
		tab1.refresh();
		tabbedPane.addTab("重复弹幕统计", null, tab1, null);

		tab2 = new TabPanel(2);
		tab2.refresh();
		tabbedPane.addTab("弹幕频数曲线统计", null, tab2, null);

		tab3 = new TabPanel(3);
		tab3.refresh();
		tabbedPane.addTab("弹幕刷屏统计", null, tab3, null);

		tab4 = new TabPanel(4);
		tab4.refresh();
		tabbedPane.addTab("月弹幕数量曲线统计", null, tab4, null);

		tab5 = new TabPanel(5);
		tab5.refresh();
		tabbedPane.addTab("日弹幕数量曲线统计", null, tab5, null);

		tab6 = new TabPanel(6);
		tab6.refresh();
		tabbedPane.addTab("日均弹幕活跃时段统计", null, tab6, null);

		tab7 = new JPanel();
		tabbedPane.addTab("查看高级弹幕合并规则", null, tab7, null);
		tab7.setLayout(new BorderLayout(0, 0));

		tab8 = new JPanel();
		tabbedPane.addTab("关于 ChatStat", null, tab8, null);
		tab8.setLayout(new BorderLayout(0, 0));

		about = new JTextArea();
		about.setBackground(new Color(240, 240, 240));
		about.setEditable(false);
		about.setLineWrap(true);
		about.setText(
				"（我才不会告诉你下面是从README.md里摘录的呢）\n该项目是用swing开发的GUI程序，支持各项弹幕统计、单个或批量视频弹幕爬取、直播间弹幕爬取，详见下文。\n开发者：Jelly Black\n哔哩哔哩 (゜-゜)つロ 干杯~\n\n本项目仅用于统计分析（例如你可以统计阿伟在某个UP主下死了多少次），不可用于非法用途。\n开发者不能确定滥用爬虫是否会导致 你 号 没 了 ，所以进行爬取时您可以在网页端退出哔哩哔哩账号，为了保险可以清浏览器缓存，甚至还可以使用IP代理。（如果你不做大量爬取，或者你是 封 号 斗 罗 ，请忽略）\n\n开发者的联系方式：\nQQ：1574854804\nEmail：l45531@126.com\n哔哩哔哩 / Github：JellyBlack");
		tab8.add(about);

		panel = new JPanel();
		tab8.add(panel, BorderLayout.SOUTH);
		panel.setLayout(new GridLayout(1, 0, 1, 0));

		button_1 = new JButton("点此访问Github源代码仓库");
		button_1.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				try {
					Runtime.getRuntime().exec("rundll32 url.dll,FileProtocolHandler "
							+ "https://github.com/JellyBlack/Bilibili-ChatStat");
				} catch (IOException e1) {
					e1.printStackTrace();
				}
			}
		});
		panel.add(button_1);

		button_2 = new JButton("点此访问开发者的哔哩哔哩空间");
		button_2.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				try {
					Runtime.getRuntime()
							.exec("rundll32 url.dll,FileProtocolHandler " + "https://space.bilibili.com/368205203");
				} catch (IOException e1) {
					e1.printStackTrace();
				}
			}
		});
		panel.add(button_2);

		button_start.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				if (list.getModel().getSize() == 0) {
					new Dialog("未选择xml文件", "请将需要统计的xml文件添加到上方。").setVisible(true);
					return;
				}
				if (Config.public_config.OUTPUT_STYLE == 1) {
					FileManager.showFileSaveDialog(frame, titles[tabbedPane.getSelectedIndex()], 0);
					if (OutputManager.getFile() == null) {
						return;
					}
				}
				Config.ALLOW_MODIFY = false;
				button_start.setText("统计正在进行");
				tabbedPane_up.setSelectedIndex(0);
				setEnabled(false);
				if (tabbedPane.getSelectedIndex() + 1 != 3) {
					processes.add(texts1[tabbedPane.getSelectedIndex()][0]);
					if (Config.public_config.TO_SBC) {
						processes.add(text2[0]);
					}
					if (Config.public_config.IGNORE_SPACES) {
						processes.add(text2[1]);
					}
					if (Config.public_config.SPLIT_CHATS) {
						processes.add(text2[2]);
					}
					if (Config.public_config.ADVANCED_MATCH) {
						processes.add(text2[3]);
					}
					if (Config.public_config.MARK_ONCE) {
						processes.add(text2[4]);
					}
					processes.add(texts1[tabbedPane.getSelectedIndex()][1]);
					processes.add(texts1[tabbedPane.getSelectedIndex()][2]);
					processes.add(texts1[tabbedPane.getSelectedIndex()][3]);
				} else {
					processes.add(texts1[tabbedPane.getSelectedIndex()][0]);
					if (Config.public_config.TO_SBC) {
						processes.add(text2[0]);
					}
					if (Config.public_config.IGNORE_SPACES) {
						processes.add(text2[1]);
					}
					if (Config.public_config.SPLIT_CHATS) {
						processes.add(text2[2]);
					}
					if (Config.public_config.ADVANCED_MATCH) {
						processes.add(text2[3]);
					}
					if (Config.public_config.MARK_ONCE) {
						processes.add(text2[4]);
					}
					processes.add(texts1[tabbedPane.getSelectedIndex()][1]);
					processes.add(texts1[tabbedPane.getSelectedIndex()][2]);
					if (Config.tab3.CRC32) {
						processes.add("正在进行CRC32反算");
					}
					processes.add(texts1[tabbedPane.getSelectedIndex()][3]);
				}
				File[] files = new File[list.getModel().getSize()];
				for (int i = 0; i < list.getModel().getSize(); i++) {
					files[i] = new File(list.getModel().getElementAt(i));
				}
				thread = new Thread(new RunScript(tabbedPane.getSelectedIndex() + 1, files));
				thread.start();
			}
		});

		JPanel south_panel = new JPanel();
		frame.getContentPane().add(south_panel, BorderLayout.SOUTH);
		south_panel.setLayout(new BorderLayout(0, 0));

		JPanel panel_progress = new JPanel();
		south_panel.add(panel_progress, BorderLayout.CENTER);
		panel_progress.setLayout(new GridLayout(0, 1, 0, 0));

		JPanel panel_progress_1 = new JPanel();
		panel_progress.add(panel_progress_1);
		panel_progress_1.setLayout(new FlowLayout(FlowLayout.CENTER, 5, 5));

		label_progress_1 = new JLabel("第一进度条");
		panel_progress_1.add(label_progress_1);

		progressBar_1 = new JProgressBar();
		panel_progress_1.add(progressBar_1);

		JPanel panel_progress_2 = new JPanel();
		panel_progress.add(panel_progress_2);
		panel_progress_2.setLayout(new FlowLayout(FlowLayout.CENTER, 5, 5));

		label_progress_2 = new JLabel("第二进度条");
		panel_progress_2.add(label_progress_2);

		progressBar_2 = new JProgressBar();
		panel_progress_2.add(progressBar_2);

		south_panel.add(button_start, BorderLayout.EAST);

		File file = new File(new File("").getAbsolutePath() + "\\" + "ChatStat.csv");
		try {
			if (file.exists()) {
				CsvReader csvReader = new CsvReader(file);
				String[][] set = csvReader.getStringArray();
				ArrayList<String> ignore_chats = new ArrayList<>();
				ArrayList<String> only_chats = new ArrayList<>();
				ArrayList<String[]> set2 = new ArrayList<>();
				ArrayList<String> failures = new ArrayList<>();
				for (int i = 0; i < set.length; i++) {
					try {
						new String("").matches(set[i][0]);
					} catch (Exception e) {
						failures.add(set[i][0]);
					}
				}
				if (failures.size() > 0) {
					StringBuilder sb = new StringBuilder("ChatStat.csv中以下正则表达式不合法。请修正后重启ChatStat。\n");
					for (int i = 0; i < failures.size(); i++) {
						sb.append("\n" + failures.get(i));
						if (i == 9 && failures.size() > 10) {
							sb.append("\n等更多" + (failures.size() - i - 1) + "处");
							break;
						}
					}
					sb.append(
							"\n\n点击“确定”终止运行，点击“取消”继续运行。\n（如果继续运行，进行正则匹配时会出问题。）\n\n提示：检查你的正则表达式是否有未转义的以下字符：\n$      (      )      *      +      .      [      ?      \\      ^      {      |");
					int result = JOptionPane.showConfirmDialog(frame, sb.toString(), "正则表达式读取失败",
							JOptionPane.OK_CANCEL_OPTION, JOptionPane.ERROR_MESSAGE);
					if (result != JOptionPane.CANCEL_OPTION) {
						System.exit(0);
					}
				}
				// 读取特殊标记
				for (int i = 0; i < set.length; i++) {
					if (set[i][1].replace(" ", "").toLowerCase().equals(Config.CHAT_TAG_IGNORE)) {
						ignore_chats.add(set[i][0]);
						continue;
					}
					if (set[i][1].replace(" ", "").toLowerCase().equals(Config.CHAT_TAG_ONLY)) {
						only_chats.add(set[i][0]);
						continue;
					}
					set2.add(set[i]);
				}
				Config.ADVANCED_MATCH_SET = set2.toArray(new String[0][]);
				Config.IGNORE_CHAT_SET = ignore_chats.toArray(new String[0]);
				Config.ONLY_CHAT_SET = only_chats.toArray(new String[0]);
				status = 1;
			} else {
				ArrayList<String> list1 = new ArrayList<>();
				ArrayList<String> list2 = new ArrayList<>();
				for (int i = 0; i < Config.ADVANCED_MATCH_SET.length; i++) {
					list1.add(Config.ADVANCED_MATCH_SET[i][0]);
					list2.add(Config.ADVANCED_MATCH_SET[i][1]);
				}
				OutputManager.setFile(new File(new File("").getAbsolutePath() + "\\" + "ChatStat.csv"));
				OutputManager.saveToCsv(null, new ArrayList[] { list1, list2 }, false);
				OutputManager.setFile(null);
				status = 2;
			}
		} catch (Exception e) {
			if (file.exists()) {
				file.delete();
			}
			ArrayList<String> list1 = new ArrayList<>();
			ArrayList<String> list2 = new ArrayList<>();
			for (int i = 0; i < Config.ADVANCED_MATCH_SET.length; i++) {
				list1.add(Config.ADVANCED_MATCH_SET[i][0]);
				list2.add(Config.ADVANCED_MATCH_SET[i][1]);
			}
			OutputManager.setFile(new File(new File("").getAbsolutePath() + "\\" + "ChatStat.csv"));
			try {
				OutputManager.saveToCsv(null, new ArrayList[] { list1, list2 }, false);
			} catch (InterruptedException e1) {
				e1.printStackTrace();
			}
			OutputManager.setFile(null);
			status = 2;
		}
		jsonThread.start();
		String[][] set = new String[Config.ADVANCED_MATCH_SET.length + Config.IGNORE_CHAT_SET.length
				+ Config.ONLY_CHAT_SET.length][2];
		for (int i = 0; i < Config.ADVANCED_MATCH_SET.length; i++) {
			set[i] = Config.ADVANCED_MATCH_SET[i];
		}
		for (int i = 0; i < Config.IGNORE_CHAT_SET.length; i++) {
			set[Config.ADVANCED_MATCH_SET.length + i] = new String[] { Config.IGNORE_CHAT_SET[i],
					"<html><strong>&lt;@ignore&gt;&nbsp;&nbsp;&nbsp;&nbsp;（识别到特殊标签）</strong>" };
		}
		for (int i = 0; i < Config.ONLY_CHAT_SET.length; i++) {
			set[Config.ADVANCED_MATCH_SET.length + Config.IGNORE_CHAT_SET.length + i] = new String[] {
					Config.ONLY_CHAT_SET[i], "<html><strong>&lt;@only&gt;&nbsp;&nbsp;&nbsp;&nbsp;（识别到特殊标签）</strong>" };
		}
		table = new JTable(set, new String[] { "正则表达式", "替换字符串" });
		table.setEnabled(false);
		table.getTableHeader().setReorderingAllowed(false);
		scroll_set = new JScrollPane(table);
		tab7.add(scroll_set, BorderLayout.CENTER);
	}

	/**
	 * 获取该类的实例
	 * 
	 * @return 实例
	 */
	public static MainGui getInstance() {
		return instance;
	}

	/**
	 * 进度条初始化，声明最大进度，每个步骤开始前需要调用该方法
	 * 
	 * @param maxProgress 最大进度
	 */
	public void initProgressBar(int maxProgress) {
		process++;
		label_progress_1.setText((process + 1) + " / " + processes.size() + "    " + processes.get(process));
		progressBar_1.setMaximum(processes.size());
		progressBar_1.setValue(process);
		label_progress_2.setText("0 / " + maxProgress);
		progressBar_2.setMaximum(maxProgress);
		progressBar_2.setValue(0);
	}

	/**
	 * 刷新进度条
	 * 
	 * @param progress 进度
	 */
	public void refreshProgressBar(int progress) {
		label_progress_2.setText(progress + " / " + progressBar_2.getMaximum());
		progressBar_2.setValue(progress);
	}

	/**
	 * 完成当前步骤，每个步骤完成后需要调用该方法
	 */
	public void progressFinish() {
		if (process == processes.size() - 1) {
			// 处理完毕
			setEnabled(true);
			label_progress_1.setText("处理完毕");
			progressBar_1.setMaximum(1);
			progressBar_1.setValue(1);
			label_progress_2.setText("处理完毕");
			progressBar_2.setMaximum(1);
			progressBar_2.setValue(1);
			process = -1;
			processes.clear();
			OutputManager.setFile(null);
			Config.ALLOW_MODIFY = true;
			if (tabbedPane.getSelectedComponent() instanceof TabPanel) {
				((TabPanel) tabbedPane.getSelectedComponent()).refresh();
				button_start.setText("开始 「" + titles[tabbedPane.getSelectedIndex()] + "」");
				button_start.setEnabled(true);
			} else {
				button_start.setText("请选择功能");
				button_start.setEnabled(false);
			}
		} else {
			progressBar_2.setValue(progressBar_2.getMaximum());
		}
	}

	/**
	 * 设置控件能否交互
	 * 
	 * @param b 能否交互
	 */
	public void setEnabled(boolean b) {
		tabbedPane_up.setEnabled(b);
		list.setEnabled(b);
		button_add_files.setEnabled(b);
		button_delete_files.setEnabled(b);
		button_start.setEnabled(b);
		tabbedPane.setEnabled(b);
		tab1.setEnabled(b);
		tab2.setEnabled(b);
		tab3.setEnabled(b);
		tab4.setEnabled(b);
		tab5.setEnabled(b);
		tab6.setEnabled(b);
		tab7.setEnabled(b);
		tab8.setEnabled(b);
	}

	/**
	 * 响应出现的异常，并结束xml处理线程。用于出现了严重的、不可修复的异常。可处理的异常不要调用此方法
	 * 
	 * @param e 异常对象
	 */
	public void notifyXmlHandlingException(Exception e) {
		thread.interrupt();
		new Dialog("发生异常", "发生了如下异常，导致线程被迫中止。\n\n" + e.getClass().getName() + "\n" + e.getMessage()).setVisible(true);
		;
		setEnabled(true);
		label_progress_1.setText("很抱歉，处理失败");
		progressBar_1.setMaximum(1);
		progressBar_1.setValue(0);
		label_progress_2.setText("很抱歉，处理失败");
		progressBar_2.setMaximum(1);
		progressBar_2.setValue(0);
		process = -1;
		processes.clear();
		OutputManager.setFile(null);
		Config.ALLOW_MODIFY = true;
		if (tabbedPane.getSelectedComponent() instanceof TabPanel) {
			((TabPanel) tabbedPane.getSelectedComponent()).refresh();
			button_start.setText("开始 「" + titles[tabbedPane.getSelectedIndex()] + "」");
			button_start.setEnabled(true);
		} else {
			button_start.setText("请选择功能");
			button_start.setEnabled(false);
		}
	}

	/**
	 * 闪烁文字
	 */
	@Override
	public void run() {
		try {
			if (status == 1) {
				Thread.sleep(500);
				tabbedPane.setTitleAt(6, "已导入合并规则库");
				Thread.sleep(1000);
				tabbedPane.setTitleAt(6, "查看高级弹幕合并规则");
				Thread.sleep(1000);
				tabbedPane.setTitleAt(6, "已导入合并规则库");
				Thread.sleep(1000);
				tabbedPane.setTitleAt(6, "查看高级弹幕合并规则");
			} else if (status == 2) {
				Thread.sleep(500);
				tabbedPane.setTitleAt(6, "已创建新合并规则库");
				Thread.sleep(1000);
				tabbedPane.setTitleAt(6, "查看高级弹幕合并规则");
				Thread.sleep(1000);
				tabbedPane.setTitleAt(6, "已创建新合并规则库");
				Thread.sleep(1000);
				tabbedPane.setTitleAt(6, "查看高级弹幕合并规则");
			} else if (status == 3) {
				Thread.sleep(500);
				tabbedPane.setTitleAt(6, "已重载合并规则库");
				Thread.sleep(1000);
				tabbedPane.setTitleAt(6, "查看高级弹幕合并规则");
				Thread.sleep(1000);
				tabbedPane.setTitleAt(6, "已重载合并规则库");
				Thread.sleep(1000);
				tabbedPane.setTitleAt(6, "查看高级弹幕合并规则");
			}
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	public void log(String log) {
		SimpleDateFormat format = new SimpleDateFormat("HH:mm:ss");
		logs.add(0, "[" + format.format(new Date()) + "] " + log);
		new Thread(new Runnable() {

			@Override
			public void run() {
				try {
					Thread.sleep(1);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				list_info.setListData(logs.toArray(new String[0]));
			}
		}).start();
	}

	/**
	 * 设置视频弹幕抓取的“确定”按钮的文字
	 * 
	 * @param text 显示文字
	 */
	public void setButtonText(String text) {
		button_confirm.setText(text);
	}

	/**
	 * 重置视频弹幕抓取的状态
	 */
	public void reset() {
		selected = false;
		Config.spider_config.avs = null;
		Config.spider_config.mode = 0;
		Config.spider_config.uid = 0;
		button_av.setText("选定");
		button_import.setText("选择txt");
		button_import_2.setText("选定");
		button_uid.setText("选定");
		button_av.setEnabled(true);
		button_import_2.setEnabled(true);
		field_av.setEnabled(true);
		button_import.setEnabled(true);
		field_uid.setEnabled(true);
		button_uid.setEnabled(true);
		txtFile = null;
		button_confirm.setEnabled(true);
		button_confirm.setText("确定配置，下一步");
	}

	/**
	 * 设置txt文件，用于“批量弹幕爬取”
	 * 
	 * @param file txt文件
	 */
	public void setTxtFile(File file) {
		txtFile = file;
	}
}
