package gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import script.Config;

/**
 * 每一个选项卡的面板
 */
public class TabPanel extends JPanel {
	private static final long serialVersionUID = 1L;
	private JCheckBox check_ignore_cases;
	private JCheckBox check_to_sbc;
	private JCheckBox check_ignore_spaces;
	private JCheckBox check_split_chats;
	private JCheckBox check_advanced_match;
	private JCheckBox check_mark_once;
	private JTextField field_rank_limit;
	private JTextField field_length2;
	private JTextField field_start_time2;
	private JTextField field_end_time2;
	private JCheckBox check_crc32;
	private JTextField field_length6;
	private JTextField field_start_time6;
	private JTextField field_end_time6;
	private JLabel label_output;
	private JButton button_output;
	private JTextArea textArea;
	private JPanel panel;

	/**
	 * 创建面板
	 * 
	 * @param tab 选项卡的选项，从1开始
	 */
	public TabPanel(int tab) {
		GridBagLayout gridBagLayout = new GridBagLayout();
		gridBagLayout.columnWidths = new int[] { 512, 0 };
		gridBagLayout.rowHeights = new int[] { 24, 284, 0 };
		gridBagLayout.columnWeights = new double[] { 1.0, Double.MIN_VALUE };
		gridBagLayout.rowWeights = new double[] { 0.0, 1.0, Double.MIN_VALUE };
		setLayout(gridBagLayout);

		textArea = new JTextArea();
		textArea.setBackground(new Color(240, 240, 240));
		textArea.setLineWrap(true);
		GridBagConstraints gbc_textArea = new GridBagConstraints();
		gbc_textArea.fill = GridBagConstraints.HORIZONTAL;
		gbc_textArea.anchor = GridBagConstraints.NORTH;
		gbc_textArea.insets = new Insets(0, 0, 5, 0);
		gbc_textArea.gridx = 0;
		gbc_textArea.gridy = 0;
		add(textArea, gbc_textArea);

		GridLayout gl_panel = new GridLayout();
		gl_panel.setColumns(1);
		gl_panel.setRows(0);
		panel = new JPanel(gl_panel);
		GridBagConstraints gbc_panel = new GridBagConstraints();
		gbc_panel.fill = GridBagConstraints.BOTH;
		gbc_panel.gridx = 0;
		gbc_panel.gridy = 1;
		add(panel, gbc_panel);

		check_ignore_cases = new JCheckBox("忽略大小写");
		panel.add(check_ignore_cases);

		check_to_sbc = new JCheckBox("转换为全角字符");
		panel.add(check_to_sbc);

		check_ignore_spaces = new JCheckBox("删除首尾空格");
		panel.add(check_ignore_spaces);

		check_split_chats = new JCheckBox("尝试拆分弹幕，如“awsl awsl awsl”将记为“awsl”");
		panel.add(check_split_chats);

		check_advanced_match = new JCheckBox("高级弹幕匹配开关（匹配规则库见左边选项卡）");
		panel.add(check_advanced_match);

		check_mark_once = new JCheckBox("一个人发的多条相同弹幕只计一次（“相同弹幕”判定位于前面的选项处理完之后）");
		panel.add(check_mark_once);

		JPanel panel_output = new JPanel();
		panel_output.setLayout(new GridLayout(0, 2, 0, 0));

		label_output = new JLabel("输出方式，当前为【swing表格展示】");
		panel_output.add(label_output);

		button_output = new JButton("切换到【csv文件展示】");
		button_output.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				if (!Config.ALLOW_MODIFY) {
					return;
				}
				if (Config.public_config.OUTPUT_STYLE == 0) {
					Config.public_config.OUTPUT_STYLE = 1;
				} else {
					Config.public_config.OUTPUT_STYLE = 0;
				}
				if (Config.public_config.OUTPUT_STYLE == 0) {
					label_output.setText("输出方式，当前为【swing表格展示】");
					button_output.setText("切换到【csv文件展示】");
				} else {
					label_output.setText("输出方式，当前为【csv文件展示】");
					button_output.setText("切换到【swing表格展示】");
				}
			}
		});
		panel_output.add(button_output);
		check_mark_once.addChangeListener(new ChangeListener() {

			@Override
			public void stateChanged(ChangeEvent arg0) {
				if (!Config.ALLOW_MODIFY) {
					return;
				}
				Config.public_config.MARK_ONCE = check_mark_once.isSelected();
			}
		});
		check_advanced_match.addChangeListener(new ChangeListener() {

			@Override
			public void stateChanged(ChangeEvent arg0) {
				if (!Config.ALLOW_MODIFY) {
					return;
				}
				Config.public_config.ADVANCED_MATCH = check_advanced_match.isSelected();
			}
		});
		check_split_chats.addChangeListener(new ChangeListener() {

			@Override
			public void stateChanged(ChangeEvent arg0) {
				if (!Config.ALLOW_MODIFY) {
					return;
				}
				Config.public_config.SPLIT_CHATS = check_split_chats.isSelected();
			}
		});
		check_ignore_spaces.addChangeListener(new ChangeListener() {

			@Override
			public void stateChanged(ChangeEvent arg0) {
				if (!Config.ALLOW_MODIFY) {
					return;
				}
				Config.public_config.IGNORE_SPACES = check_ignore_spaces.isSelected();
			}
		});
		check_to_sbc.addChangeListener(new ChangeListener() {

			@Override
			public void stateChanged(ChangeEvent arg0) {
				if (!Config.ALLOW_MODIFY) {
					return;
				}
				Config.public_config.TO_SBC = check_to_sbc.isSelected();
			}
		});
		check_ignore_cases.addChangeListener(new ChangeListener() {

			@Override
			public void stateChanged(ChangeEvent arg0) {
				if (!Config.ALLOW_MODIFY) {
					return;
				}
				Config.public_config.IGNORE_CASES = check_ignore_cases.isSelected();
			}
		});

		JPanel panel_rank_limit = new JPanel();
		if (tab == 1) {
			panel.add(panel_rank_limit);
		}
		panel_rank_limit.setLayout(new BorderLayout(0, 0));

		JLabel label_rank_limit = new JLabel("参与排名的最大弹幕数量（即展示前多少名），设置为-1即全部排名");
		panel_rank_limit.add(label_rank_limit, BorderLayout.CENTER);

		field_rank_limit = new JTextField();
		panel_rank_limit.add(field_rank_limit, BorderLayout.EAST);
		field_rank_limit.getDocument().addDocumentListener(new DocumentListener() {

			@Override
			public void removeUpdate(DocumentEvent arg0) {
				if (!Config.ALLOW_MODIFY) {
					return;
				}
				try {
					Config.tab1.RANK_LIMIT = Integer.parseInt(field_rank_limit.getText());
				} catch (NumberFormatException e) {
				}
			}

			@Override
			public void insertUpdate(DocumentEvent arg0) {
				if (!Config.ALLOW_MODIFY) {
					return;
				}
				try {
					Config.tab1.RANK_LIMIT = Integer.parseInt(field_rank_limit.getText());
				} catch (NumberFormatException e) {
				}
			}

			@Override
			public void changedUpdate(DocumentEvent arg0) {
			}
		});
		field_rank_limit.setColumns(20);

		JPanel panel_length2 = new JPanel();

		panel_length2.setLayout(new BorderLayout(0, 0));

		JLabel label_length2 = new JLabel("采样长度，单位为秒");
		panel_length2.add(label_length2, BorderLayout.CENTER);

		field_length2 = new JTextField();
		panel_length2.add(field_length2, BorderLayout.EAST);
		field_length2.getDocument().addDocumentListener(new DocumentListener() {

			@Override
			public void removeUpdate(DocumentEvent arg0) {
				if (!Config.ALLOW_MODIFY) {
					return;
				}
				try {
					Config.tab2.LENGTH = Float.parseFloat(field_length2.getText());
				} catch (NumberFormatException e) {
				}
			}

			@Override
			public void insertUpdate(DocumentEvent arg0) {
				if (!Config.ALLOW_MODIFY) {
					return;
				}
				try {
					Config.tab2.LENGTH = Float.parseFloat(field_length2.getText());
				} catch (NumberFormatException e) {
				}
			}

			@Override
			public void changedUpdate(DocumentEvent arg0) {
			}
		});
		field_length2.setColumns(20);

		JPanel panel_start_time2 = new JPanel();

		panel_start_time2.setLayout(new BorderLayout(0, 0));

		JLabel label_start_time2 = new JLabel("起始时间（秒数），设置为0即从头开始");
		panel_start_time2.add(label_start_time2, BorderLayout.CENTER);

		field_start_time2 = new JTextField();
		panel_start_time2.add(field_start_time2, BorderLayout.EAST);
		field_start_time2.getDocument().addDocumentListener(new DocumentListener() {

			@Override
			public void removeUpdate(DocumentEvent arg0) {
				if (!Config.ALLOW_MODIFY) {
					return;
				}
				try {
					Config.tab2.START_TIME = Float.parseFloat(field_start_time2.getText());
				} catch (NumberFormatException e) {
				}
			}

			@Override
			public void insertUpdate(DocumentEvent arg0) {
				if (!Config.ALLOW_MODIFY) {
					return;
				}
				try {
					Config.tab2.START_TIME = Float.parseFloat(field_start_time2.getText());
				} catch (NumberFormatException e) {
				}
			}

			@Override
			public void changedUpdate(DocumentEvent arg0) {
			}
		});
		field_start_time2.setColumns(20);

		JPanel panel_end_time2 = new JPanel();

		if (tab == 2) {
			panel.add(panel_length2);
			panel.add(panel_start_time2);
			panel.add(panel_end_time2);
		}
		panel_end_time2.setLayout(new BorderLayout(0, 0));

		JLabel label_end_time2 = new JLabel("终止时间（秒数），设置为-1即到视频末尾");
		panel_end_time2.add(label_end_time2, BorderLayout.CENTER);

		field_end_time2 = new JTextField();
		panel_end_time2.add(field_end_time2, BorderLayout.EAST);
		field_end_time2.getDocument().addDocumentListener(new DocumentListener() {

			@Override
			public void removeUpdate(DocumentEvent arg0) {
				if (!Config.ALLOW_MODIFY) {
					return;
				}
				try {
					Config.tab2.END_TIME = Float.parseFloat(field_end_time2.getText());
				} catch (NumberFormatException e) {
				}
			}

			@Override
			public void insertUpdate(DocumentEvent arg0) {
				if (!Config.ALLOW_MODIFY) {
					return;
				}
				try {
					Config.tab2.END_TIME = Float.parseFloat(field_end_time2.getText());
				} catch (NumberFormatException e) {
				}
			}

			@Override
			public void changedUpdate(DocumentEvent arg0) {
			}
		});
		field_end_time2.setColumns(20);

		check_crc32 = new JCheckBox("进行CRC32反算（十分消耗时间，极不推荐）");
		check_crc32.addChangeListener(new ChangeListener() {

			@Override
			public void stateChanged(ChangeEvent arg0) {
				if (!Config.ALLOW_MODIFY) {
					return;
				}
				Config.tab3.CRC32 = check_crc32.isSelected();
			}
		});
		if (tab == 3) {
			panel.add(check_crc32);
			panel.add(panel_rank_limit);
		}

		JPanel panel_length6 = new JPanel();

		panel_length6.setLayout(new BorderLayout(0, 0));

		JLabel label_length6 = new JLabel("采样长度，单位为秒");
		panel_length6.add(label_length6, BorderLayout.CENTER);

		field_length6 = new JTextField();
		panel_length6.add(field_length6, BorderLayout.EAST);
		field_length6.getDocument().addDocumentListener(new DocumentListener() {

			@Override
			public void removeUpdate(DocumentEvent arg0) {
				if (!Config.ALLOW_MODIFY) {
					return;
				}
				try {
					Config.tab6.LENGTH = Integer.parseInt(field_length6.getText());
				} catch (NumberFormatException e) {
				}
			}

			@Override
			public void insertUpdate(DocumentEvent arg0) {
				if (!Config.ALLOW_MODIFY) {
					return;
				}
				try {
					Config.tab6.LENGTH = Integer.parseInt(field_length6.getText());
				} catch (NumberFormatException e) {
				}
			}

			@Override
			public void changedUpdate(DocumentEvent arg0) {
			}
		});
		field_length6.setColumns(20);

		JPanel panel_start_time6 = new JPanel();

		panel_start_time6.setLayout(new BorderLayout(0, 0));

		JLabel label_start_time6 = new JLabel("起始时间（秒数），设置为0即从00:00:00开始");
		panel_start_time6.add(label_start_time6);

		field_start_time6 = new JTextField();
		panel_start_time6.add(field_start_time6, BorderLayout.EAST);
		field_start_time6.getDocument().addDocumentListener(new DocumentListener() {

			@Override
			public void removeUpdate(DocumentEvent arg0) {
				if (!Config.ALLOW_MODIFY) {
					return;
				}
				try {
					Config.tab6.START_TIME = Integer.parseInt(field_start_time6.getText());
				} catch (NumberFormatException e) {
				}
			}

			@Override
			public void insertUpdate(DocumentEvent arg0) {
				if (!Config.ALLOW_MODIFY) {
					return;
				}
				try {
					Config.tab6.START_TIME = Integer.parseInt(field_start_time6.getText());
				} catch (NumberFormatException e) {
				}
			}

			@Override
			public void changedUpdate(DocumentEvent arg0) {
			}
		});
		field_start_time6.setColumns(20);

		JPanel panel_end_time6 = new JPanel();

		if (tab == 6) {
			panel.add(panel_length6);
			panel.add(panel_start_time6);
			panel.add(panel_end_time6);
		}
		panel_end_time6.setLayout(new BorderLayout(0, 0));

		JLabel label_end_time6 = new JLabel("终止时间（秒数），设置为-1即到一天结束");
		panel_end_time6.add(label_end_time6, BorderLayout.CENTER);

		field_end_time6 = new JTextField();
		panel_end_time6.add(field_end_time6, BorderLayout.EAST);
		field_end_time6.getDocument().addDocumentListener(new DocumentListener() {

			@Override
			public void removeUpdate(DocumentEvent arg0) {
				if (!Config.ALLOW_MODIFY) {
					return;
				}
				try {
					Config.tab6.END_TIME = Integer.parseInt(field_end_time6.getText());
				} catch (NumberFormatException e) {
				}
			}

			@Override
			public void insertUpdate(DocumentEvent arg0) {
				if (!Config.ALLOW_MODIFY) {
					return;
				}
				try {
					Config.tab6.END_TIME = Integer.parseInt(field_end_time6.getText());
				} catch (NumberFormatException e) {
				}
			}

			@Override
			public void changedUpdate(DocumentEvent arg0) {
			}
		});
		panel.add(panel_output);
		field_end_time6.setColumns(20);
		textArea.setEditable(false);
		switch (tab) {
		case 1:
			textArea.setText(
					"【重复弹幕统计】\n该功能可查看观众发送最多的弹幕是什么。\n1.2.0新增功能：在ChatStat.csv文件中“替换字符串”一栏可填写 <@ignore> 或 <@only> 标签，前者可以在统计时忽略匹配对应正则表达式的弹幕，后者可以在统计时只统计匹配对应正则表达式的弹幕。该功能对所有统计项均适用（需打开“高级弹幕匹配开关”），详见Github仓库。");
			break;
		case 2:
			textArea.setText(
					"【弹幕频数曲线统计】\n该功能可查看视频的弹幕密集区（类似于哔哩哔哩网页端播放器的高能进度条）。采样长度指每次采样的时间长短。如设置为15s，则程序将以15s为一组，统计每组内的弹幕数。\n查看曲线，请选择“csv文件展示”，然后使用电子表格创建图像。");
			break;
		case 3:
			textArea.setText(
					"【弹幕刷屏统计】\n该功能可查看发送弹幕最多的用户。\n如开启CRC32反算，程序将暴力破解CRC32反算出用户的UID（不推荐）。\n【重要】“直播弹幕抓取”输出的模拟xml文件直接包含用户的UID信息，如果您想处理该类xml文件，切勿勾选“CRC32反算”，否则进程将卡死。");
			break;
		case 4:
			textArea.setText(
					"【月弹幕数量曲线统计】\n该功能可查看弹幕数量随月份变化的情况。\n哔哩哔哩弹幕池总数是固定的，因此可能出现一段时间没有新增弹幕的情况。\n查看曲线，请选择“csv文件展示”，然后使用电子表格创建图像。");
			break;
		case 5:
			textArea.setText(
					"【日弹幕数量曲线统计】\n该功能可查看弹幕数量随日期变化的情况。\n哔哩哔哩弹幕池总数是固定的，因此可能出现一段时间没有新增弹幕的情况。\n查看曲线，请选择“csv文件展示”，然后使用电子表格创建图像。");
			break;
		case 6:
			textArea.setText(
					"【日均弹幕活跃时段统计】\n该功能可查看观众在一天内的哪个时间段发送弹幕最多。采样长度指每次采样的时间长短。如设置为1800s，则程序将以1800s为一组，统计每组内的弹幕数。\n查看曲线，请选择“csv文件展示”，然后使用电子表格创建图像。");
			break;
		}
	}

	/**
	 * 刷新面板
	 */
	public void refresh() {
		check_ignore_cases.setSelected(Config.public_config.IGNORE_CASES);
		check_to_sbc.setSelected(Config.public_config.TO_SBC);
		check_ignore_spaces.setSelected(Config.public_config.IGNORE_SPACES);
		check_split_chats.setSelected(Config.public_config.SPLIT_CHATS);
		check_advanced_match.setSelected(Config.public_config.ADVANCED_MATCH);
		check_mark_once.setSelected(Config.public_config.MARK_ONCE);
		field_rank_limit.setText(Integer.toString(Config.tab1.RANK_LIMIT));
		field_length2.setText(Float.toString(Config.tab2.LENGTH));
		field_start_time2.setText(Float.toString(Config.tab2.START_TIME));
		field_end_time2.setText(Float.toString(Config.tab2.END_TIME));
		check_crc32.setSelected(Config.tab3.CRC32);
		field_length6.setText(Integer.toString(Config.tab6.LENGTH));
		field_start_time6.setText(Integer.toString(Config.tab6.START_TIME));
		field_end_time6.setText(Integer.toString(Config.tab6.END_TIME));
		if (Config.public_config.OUTPUT_STYLE == 0) {
			label_output.setText("输出方式，当前为【swing表格展示】");
			button_output.setText("切换到【csv文件展示】");
		} else {
			label_output.setText("输出方式，当前为【csv文件展示】");
			button_output.setText("切换到【swing表格展示】");
		}
	}

	/**
	 * 设置面板是否可用
	 * 
	 * @param b 是否可用
	 */
	@Override
	public void setEnabled(boolean b) {
		super.setEnabled(b);
		check_ignore_cases.setEnabled(b);
		check_to_sbc.setEnabled(b);
		check_ignore_spaces.setEnabled(b);
		check_split_chats.setEnabled(b);
		check_advanced_match.setEnabled(b);
		check_mark_once.setEnabled(b);
		field_rank_limit.setEnabled(b);
		field_length2.setEnabled(b);
		field_start_time2.setEnabled(b);
		field_end_time2.setEnabled(b);
		check_crc32.setEnabled(b);
		field_length6.setEnabled(b);
		field_start_time6.setEnabled(b);
		field_end_time6.setEnabled(b);
		button_output.setEnabled(b);
	}
}
