package gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.border.EmptyBorder;

import script.Config;

public class RequestCookie extends JDialog {

	private static final long serialVersionUID = 1L;
	private final JPanel contentPanel = new JPanel();
	private JTextField textField;

	public RequestCookie() {
		setTitle("键入Cookie");
		setModal(true);
		setBounds(100, 100, 500, 350);
		setLocationRelativeTo(null);
		getContentPane().setLayout(new BorderLayout());

		contentPanel.setBorder(new EmptyBorder(5, 5, 5, 5));
		getContentPane().add(contentPanel, BorderLayout.CENTER);
		contentPanel.setLayout(new BorderLayout(0, 0));

		JTextArea textArea = new JTextArea();
		textArea.setWrapStyleWord(true);
		contentPanel.add(textArea, BorderLayout.CENTER);
		textArea.setEditable(false);
		textArea.setLineWrap(true);
		textArea.setBackground(new Color(240, 240, 240));
		textArea.setText("受哔哩哔哩安全策略的影响，爬取历史弹幕需要传入用户的登录信息。\n" + "请按照以下方法获取Cookie：\n" + "1. 浏览器打开哔哩哔哩，确保已登录账号，若没登录请登录。\n"
				+ "2. 浏览器新建一个标签页，按F12打开开发者工具（如果你的浏览器按F12打不开开发者工具，请百度搜索进入开发者工具的方式）。\n"
				+ "3. 在该标签页访问http://comment.bilibili.com/279786.xml\n"
				+ "4. 在开发者工具中依次点击“Network/网络”、“279786.xml”，右边显示“Headers/头部”，拉到下面，在“Request Headers/请求头”中可以看到“Cookie”字段（以“_uuid=”开头），把“Cookie”的内容复制下来，粘贴到下面。\n"
				+ "Cookie仅用于请求历史弹幕，开发者不收集任何信息（代码开源，有没有收集完全可以看得到）。\n" + "【警告】最好不要用大号！用大号的话账号没了不要找我！");

		textField = new JTextField();
		contentPanel.add(textField, BorderLayout.SOUTH);
		textField.setText(Config.spider_config.COOKIE);
		textField.setColumns(10);

		JPanel buttonPane = new JPanel();
		buttonPane.setLayout(new FlowLayout(FlowLayout.RIGHT));
		getContentPane().add(buttonPane, BorderLayout.SOUTH);

		JButton okButton = new JButton("确定");
		okButton.setActionCommand("OK");
		okButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				String str = textField.getText();
				if (str.isEmpty()) {
					new Dialog("请输入Cookie", "请输入Cookie。").setVisible(true);
					return;
				}
				Config.spider_config.COOKIE = str.replaceFirst("^Cookie:", "");
				Config.spider_config.HISTORICAL = true;
				MainGui.getInstance().setCheckSelected(Config.spider_config.HISTORICAL);
				dispose();
			}
		});
		buttonPane.add(okButton);
		JButton cancelButton = new JButton("取消");
		cancelButton.setActionCommand("Cancel");
		cancelButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				MainGui.getInstance().setCheckSelected(Config.spider_config.HISTORICAL);
				dispose();
			}
		});
		buttonPane.add(cancelButton);
		setVisible(true);
	}
}
