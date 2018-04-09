package com.bonult.money.ui;

import com.bonult.money.Config;
import com.bonult.money.ConfigHolder;
import com.bonult.money.Main;
import com.bonult.money.WorkMode;

import javax.swing.*;
import javax.swing.plaf.FontUIResource;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import java.awt.*;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLEncoder;
import java.util.Enumeration;

/**
 * Created by bonult on 2018/1/30.
 */
public class Helper extends JFrame {
	private JTextPane textPane;

	private int width;
	private int height;

	public Helper(){
		initGlobalFont(new Font("微软雅黑", Font.PLAIN, 14));
//		Toolkit toolkit = Toolkit.getDefaultToolkit();
//		Dimension dimension = toolkit.getScreenSize();
		width = 315;//dimension.width / 3;
		height = 355;//dimension.height / 3;
		readConfigs();
//		setIconImage(new ImageIcon("miku.jpg").getImage());
		initMenuBar();
		createAppComponents();
		initFrame();
	}

	private void readConfigs(){

	}

	public void infoMsgShow(String msg, String title){
		JOptionPane.showMessageDialog(null, msg, title, JOptionPane.INFORMATION_MESSAGE);
	}

	public void errorMsgShow(String msg, String title){
		JOptionPane.showMessageDialog(null, msg, title, JOptionPane.ERROR_MESSAGE);
	}

	public void warningMsgShow(String msg, String title){
		JOptionPane.showMessageDialog(null, msg, title, JOptionPane.WARNING_MESSAGE);
	}

	private void initFrame(){
		setLocationRelativeTo(null);
		setPreferredSize(new Dimension(width, height));
		setResizable(false);
		setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
		pack();
	}

	private void initMenuBar(){
		Config config = ConfigHolder.CONFIG;
		JMenuBar bar = new JMenuBar();
		JMenu menu = new JMenu("设置");

		JMenu themeMenu = new JMenu("主题");
		if(config.getLookAndFeel() != null){
			setLookAndFeel(config.getLookAndFeel());
		}
		UIManager.LookAndFeelInfo[] themes = UIManager.getInstalledLookAndFeels();
		ButtonGroup buttonGroup = new ButtonGroup();
		for(UIManager.LookAndFeelInfo theme : themes){
			JMenuItem item = new JRadioButtonMenuItem(theme.getName());
			themeMenu.add(item);
			buttonGroup.add(item);
			if(theme.getClassName().equals(config.getLookAndFeel()))
				item.setSelected(true);
			item.addActionListener(event -> setLookAndFeel(theme.getClassName()));
		}

		JMenu runningTypeMenu = new JMenu("工作模式");
		buttonGroup = new ButtonGroup();
		for(WorkMode workMode : WorkMode.values()){
			JMenuItem item = new JRadioButtonMenuItem(workMode.getName());
			runningTypeMenu.add(item);
			buttonGroup.add(item);
			item.addActionListener(event -> ConfigHolder.CONFIG.setWorkMode(workMode));
			if(ConfigHolder.CONFIG.getWorkMode() == workMode)
				item.setSelected(true);
		}

		JMenu confMenu = new JMenu("配置文件");
		buttonGroup = new ButtonGroup();
		File confDir = new File(ConfigHolder.USER_DIR + "conf");
		if(confDir.exists())
			for(File file : confDir.listFiles()){
				String name = file.getName();
				if(name.endsWith(".properties")){
					JMenuItem item = new JRadioButtonMenuItem(name.substring(0, name.length() - 11));
					confMenu.add(item);
					buttonGroup.add(item);
					item.addActionListener(event -> ConfigHolder.CONFIG.setCurrConf(name));
					if(ConfigHolder.CONFIG.getCurrConf().equals(name))
						item.setSelected(true);
				}
			}

		JMenu optionNumMenu = new JMenu("选项个数");
		buttonGroup = new ButtonGroup();
		JMenuItem item3 = new JRadioButtonMenuItem("3");
		JMenuItem item4 = new JRadioButtonMenuItem("4");
		optionNumMenu.add(item3);
		optionNumMenu.add(item4);
		item3.addActionListener(event -> ConfigHolder.CONFIG.setMaxOptionNum(3));
		item4.addActionListener(event -> ConfigHolder.CONFIG.setMaxOptionNum(4));
		buttonGroup.add(item3);
		buttonGroup.add(item4);
		if(ConfigHolder.CONFIG.getMaxOptionNum() == 3)
			item3.setSelected(true);
		else
			item4.setSelected(true);

		JMenuItem portMenuItem = new JMenuItem("监听端口");
		portMenuItem.addActionListener(ev -> {
			String inputValue = JOptionPane.showInputDialog("请输入系统监听的端口");
			int port;
			if(inputValue == null || !inputValue.matches("\\d{1,5}") || (port = Integer.parseInt(inputValue)) < 0 || port > 65535){
				warningMsgShow("输入格式错误", "格式错误");
			}else
				ConfigHolder.CONFIG.setPort(port);
		});

		JMenuItem delQuesNum = new JRadioButtonMenuItem("删除问题序号");
		delQuesNum.addActionListener(event -> ConfigHolder.CONFIG.setRmvQuesNum(!ConfigHolder.CONFIG.isRmvQuesNum()));
		JMenuItem delOptNum = new JRadioButtonMenuItem("删除选项序号");
		delOptNum.addActionListener(event -> ConfigHolder.CONFIG.setRmvOptNum(!ConfigHolder.CONFIG.isRmvOptNum()));
		if(ConfigHolder.CONFIG.isRmvQuesNum())
			delQuesNum.setSelected(true);
		else
			delQuesNum.setSelected(false);
		if(ConfigHolder.CONFIG.isRmvOptNum())
			delOptNum.setSelected(true);
		else
			delOptNum.setSelected(false);

		JMenuItem inputOCR = new JMenuItem("填写OCR配置");
		JMenuItem inputConf = new JMenuItem("修改截图配置");
		inputConf.addActionListener(e -> new ConfTable());

		menu.add(themeMenu);
		menu.addSeparator();
		menu.add(runningTypeMenu);
		menu.add(optionNumMenu);
		menu.addSeparator();
		menu.add(portMenuItem);
		menu.addSeparator();
		menu.add(confMenu);
		menu.addSeparator();
		menu.add(delQuesNum);
		menu.add(delOptNum);
		menu.addSeparator();
		menu.add(inputOCR);
		menu.add(inputConf);

		File file = new File(ConfigHolder.USER_DIR + "sys" + File.separator + "license");
		if(!file.exists()){
			JMenuItem license = new JMenuItem("填写激活码");
			license.addActionListener(e -> {
				final String inputValue = JOptionPane.showInputDialog("请输入激活码");
				if(inputValue == null){
					return;
				}
				if(inputValue.equals("")){
					Main.errorMsgShow("激活码无效！！", "");
					return;
				}
				new Thread(() -> {
					final java.util.List<String> macs = Main.getLocalMacs();
					if(macs.size() == 0){
						macs.add("4C-32-75-99-5B-75");
					}
					StringBuilder s = new StringBuilder();
					for(String mac : macs){
						s.append(mac);
						s.append("L");
					}
					try{
						String url = ConfigHolder.URL + "/a?m=" + URLEncoder.encode(s.toString(), "UTF-8") + "&c=" + URLEncoder.encode(inputValue, "UTF-8");
						URL get = new URL(url);
						InputStream in = get.openStream();
						int i;
						if((i = in.read()) == 1){
							OutputStream o = new FileOutputStream(file);
							o.write(inputValue.getBytes());
							o.close();
							Main.infoMsgShow("激活成功~","");
						}else{
							Main.errorMsgShow("激活码无效！！", "");
						}
						in.close();
					}catch(Exception e1){
						e1.printStackTrace();
					}
				}).start();
			});
			menu.add(license);
		}

		bar.add(menu);
		setJMenuBar(bar);
	}

	private void setLookAndFeel(String className){
		if(!UIManager.getLookAndFeel().getClass().getName().equals(className)){
			try{
				ConfigHolder.CONFIG.setLookAndFeel(className);
				UIManager.setLookAndFeel(className);
			}catch(Exception e){
				errorMsgShow("主题切换出错", "错误！");
			}
		}
	}

	private void createAppComponents(){
		SpringLayout layout = new SpringLayout();
		JPanel mainPanel = new JPanel(layout);
		mainPanel.setBackground(Color.WHITE);
		setContentPane(mainPanel);
		JTextArea welcomeText = new JTextArea(1, 25);
		welcomeText.setFont(new Font(null, Font.BOLD, 14));
		welcomeText.setEditable(false);
		welcomeText.setText("欢迎使用bobo答题助手~");
		mainPanel.add(welcomeText);

		textPane = new JTextPane();
		textPane.setEditable(false);
		JScrollPane scrollPane = new JScrollPane(textPane);
		scrollPane.setBorder(BorderFactory.createEmptyBorder());
		mainPanel.add(scrollPane);
		insertMessage("等待题目中。。。\n", Color.BLUE, 14, false);

		SpringLayout.Constraints welTxtCons = layout.getConstraints(welcomeText);
		welTxtCons.setX(Spring.constant(5));
		welTxtCons.setY(Spring.constant(5));
		welTxtCons.setConstraint(SpringLayout.SOUTH, Spring.constant(30));

		SpringLayout.Constraints txtPanelCons = layout.getConstraints(scrollPane);
		txtPanelCons.setX(Spring.constant(5));
		txtPanelCons.setY(welTxtCons.getConstraint(SpringLayout.SOUTH));
		txtPanelCons.setConstraint(SpringLayout.EAST, welTxtCons.getConstraint(SpringLayout.EAST));

		SpringLayout.Constraints panelCons = layout.getConstraints(mainPanel);
		panelCons.setConstraint(SpringLayout.EAST, Spring.sum(welTxtCons.getConstraint(SpringLayout.EAST), Spring.constant(5)));
		panelCons.setConstraint(SpringLayout.SOUTH, Spring.sum(txtPanelCons.getConstraint(SpringLayout.SOUTH), Spring.constant(5)));

	}

	public void insertMessage(String text, Color textColor, int textSize, boolean bold){
		SimpleAttributeSet set = new SimpleAttributeSet();
		StyleConstants.setForeground(set, textColor);
		StyleConstants.setFontSize(set, textSize);
		StyleConstants.setBold(set, bold);
		Document doc = textPane.getStyledDocument();
		try{
			doc.insertString(doc.getLength(), text, set);
		}catch(BadLocationException e){
			errorMsgShow(e.getMessage(), "运行出错！");
		}
	}

	public void clearMessage(){
		textPane.setText("");
	}

	private void initGlobalFont(Font font){
		FontUIResource fontRes = new FontUIResource(font);
		for(Enumeration<Object> keys = UIManager.getDefaults().keys(); keys.hasMoreElements(); ){
			Object key = keys.nextElement();
			Object value = UIManager.get(key);
			if(value instanceof FontUIResource){
				UIManager.put(key, fontRes);
			}
		}
	}
}
//			int n = JOptionPane.showConfirmDialog(null, "你高兴吗?", "标题",JOptionPane.YES_NO_OPTION);
