package gui;

import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Date;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.ScrollPaneConstants;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import script.Config;
import script.LiveChat;
import script.OutputManager;
import javax.swing.JCheckBox;

/**
 * 直播弹幕爬取的控件类
 */
public class LivePanel extends JPanel {
	private static final long serialVersionUID = 1L;
	private JTextField field_delay;
	private JTextField field_room;
	private JLabel label_times;
	private JLabel label_total;
	private JButton button_choose;
	private JButton button_status;
	private JCheckBox check;
	private int times = 0;
	private int count = 0;
	private int failure = 0;
	private JScrollPane scrollPane_live;
	private JList<String> list;
	private static LivePanel instance;
	private ArrayList<String> logs;
	private Thread thread;

	/**
	 * 创建控件
	 */
	public LivePanel() {
		LivePanel.instance = this;
		logs = new ArrayList<>();
		list = new JList<>();
		list.setListData(logs.toArray(new String[0]));
		setLayout(new BorderLayout(0, 0));
		scrollPane_live = new JScrollPane(list);
		scrollPane_live.setMaximumSize(getSize());
		scrollPane_live.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
		scrollPane_live.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		add(scrollPane_live, BorderLayout.CENTER);
		JPanel panel_right = new JPanel();
		add(panel_right, BorderLayout.EAST);
		panel_right.setLayout(new GridLayout(0, 1, 0, 0));

		JPanel panel_room = new JPanel();
		panel_right.add(panel_room);

		JButton button = new JButton("清空日志");
		button.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				logs.clear();
				list.setListData(logs.toArray(new String[0]));
			}
		});
		panel_room.add(button);

		JLabel label_room = new JLabel("直播间");
		panel_room.add(label_room);

		field_room = new JTextField();
		panel_room.add(field_room);
		field_room.setColumns(10);

		label_times = new JLabel("爬取成功的次数：未开始");
		panel_right.add(label_times);

		label_total = new JLabel("已爬取弹幕数：未开始");
		panel_right.add(label_total);

		check = new JCheckBox("智能调整爬取间隔");
		check.setSelected(Config.live_config.AUTO_DELAY);
		check.addChangeListener(new ChangeListener() {

			@Override
			public void stateChanged(ChangeEvent e) {
				if (!Config.ALLOW_MODIFY) {
					return;
				}
				Config.live_config.AUTO_DELAY = check.isSelected();
			}
		});
		panel_right.add(check);

		JPanel panel_delay = new JPanel();
		panel_right.add(panel_delay);

		JLabel label_delay = new JLabel("爬取间隔（ms）");
		panel_delay.add(label_delay);

		field_delay = new JTextField();
		field_delay.setText(Config.live_config.DELAY + "");
		panel_delay.add(field_delay);
		field_delay.setColumns(10);

		JButton button_delay = new JButton("注册");
		button_delay.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				Config.live_config.DELAY = Integer.parseInt(field_delay.getText());
			}
		});
		panel_delay.add(button_delay);

		JPanel panel_control = new JPanel();
		panel_right.add(panel_control);

		button_choose = new JButton("选择输出目录");
		button_status = new JButton("开始爬取");
		button_choose.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				if (!Config.live_config.STATUS) {
					FileManager.showFileSaveDialog(instance, "直播弹幕", 1);
					if (OutputManager.getFile() != null) {
						log("###  已选择输出目录  ###");
						refreshUi();
					}
				} else {
					button_choose.setText("等待抓取结束");
					button_choose.setEnabled(false);
					button_status.setEnabled(false);
					Config.live_config.STATUS = false;
					thread.interrupt();
					button_choose.setText("正在输出");
					OutputManager.saveToJson(LiveChat.getJSONArray());
					new Dialog("输出成功", "已输出到json文件。").setVisible(true);
					;
					reset();
					button_choose.setEnabled(true);
					button_status.setEnabled(true);
					button_choose.setText("选择输出目录");
					button_status.setText("开始爬取");
					label_times.setText("爬取成功的次数：未开始");
					label_total.setText("已爬取弹幕数：未开始");
					Config.ALLOW_MODIFY = false;
					setEnabled(true);
					MainGui.getInstance().setEnabled(true);
					field_room.setEnabled(true);
					Config.ALLOW_MODIFY = true;
					OutputManager.setFile(null);
				}
			}
		});
		panel_control.add(button_choose);

		button_status.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				if (!Config.live_config.STATUS) {
					if (OutputManager.getFile() == null) {
						new Dialog("配置不完整", "请先选择输出目录。").setVisible(true);
						return;
					}
					if (field_room.getText().isEmpty()) {
						new Dialog("配置不完整", "请输入直播间。").setVisible(true);
						return;
					}
					try {
						Config.live_config.ROOM = Integer.parseInt(field_room.getText());
					} catch (NumberFormatException err) {
						new Dialog("直播间配置错误", "无法读取房间号。请检查直播间房间号。").setVisible(true);
						return;
					}
					if (Config.live_config.ROOM <= 0) {
						new Dialog("直播间配置错误", "直播间房间号必须是大于0的整数。").setVisible(true);
						return;
					}
					Config.live_config.START_TIME = new Date().getTime();
					Config.live_config.STATUS = true;
					setEnabled(false);
					Config.ALLOW_MODIFY = false;
					MainGui.getInstance().setEnabled(false);
					Config.ALLOW_MODIFY = true;
					button_choose.setText("结束并输出为json");
					button_status.setText("结束并模拟输出xml");
					thread = new Thread(new LiveChat(OutputManager.getFile()));
					thread.start();
				} else {
					button_status.setText("等待抓取结束");
					button_choose.setEnabled(false);
					button_status.setEnabled(false);
					Config.live_config.STATUS = false;
					thread.interrupt();
					button_status.setText("正在输出");
					OutputManager.saveToXml(LiveChat.getChat());
					new Dialog("输出成功", "已输出到xml文件。\n【注意】导出的xml文件仅供ChatStat统计使用，并不是真正的哔哩哔哩xml弹幕文件。").setVisible(true);
					;
					reset();
					button_choose.setEnabled(true);
					button_status.setEnabled(true);
					button_choose.setText("选择输出目录");
					button_status.setText("开始爬取");
					label_times.setText("爬取成功的次数：未开始");
					label_total.setText("已爬取弹幕数：未开始");
					Config.ALLOW_MODIFY = false;
					setEnabled(true);
					MainGui.getInstance().setEnabled(true);
					field_room.setEnabled(true);
					Config.ALLOW_MODIFY = true;
					OutputManager.setFile(null);
				}
			}
		});
		panel_control.add(button_status);

	}

	/**
	 * 获取示例
	 * 
	 * @return 示例
	 */
	public static LivePanel getInstance() {
		return instance;
	}

	/**
	 * 输出日志
	 * 
	 * @param log 日志内容
	 */
	public void log(String log) {
		logs.add(0, log);
	}

	/**
	 * 更新日志展示的UI
	 */
	public void refreshUi() {
		list.setListData(logs.toArray(new String[0]));
	}

	/**
	 * 添加一次爬取成功的次数
	 */
	public void addTime() {
		times++;
		label_times.setText("爬取成功的次数：" + times + "     零缓冲次数：" + failure);
	}

	/**
	 * 刷新状态展示区
	 * 
	 * @param new_chat_count 新弹幕的数量，即与上一次爬取的结果不重复的弹幕数量
	 * @param buffer         缓冲区
	 * @param first_run      是否是第一次爬取
	 */
	public void refreshLabel(int new_chat_count, int buffer, boolean first_run) {
		count += new_chat_count;
		if (first_run) {
			label_total.setText("已爬取弹幕数：" + count + "     [缓冲区填充中]");
		} else if (buffer == 0) {
			failure++;
			label_total.setText("已爬取弹幕数：" + count + "     【警告 - 缓冲区：" + buffer + "】");
		} else {
			StringBuilder sb = new StringBuilder();
			sb.append("已爬取弹幕数：" + count + "     缓冲区：" + buffer + " ");
			for (int i = 0; i < buffer; i++) {
				sb.append("+");
			}
			label_total.setText(sb.toString());
		}
	}

	/**
	 * 设置控件是否可用
	 * 
	 * @param b 是否可用
	 */
	@Override
	public void setEnabled(boolean b) {
		super.setEnabled(b);
		if (!Config.ALLOW_MODIFY) {
			return;
		}
		field_room.setEnabled(b);
	}

	/**
	 * 重置状态展示区
	 */
	public void reset() {
		times = 0;
		count = 0;
		failure = 0;
	}

	/**
	 * 更新爬取延时
	 */
	public void refreshDelayField() {
		field_delay.setText(Config.live_config.DELAY + "");
	}

}
