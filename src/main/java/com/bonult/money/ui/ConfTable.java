package com.bonult.money.ui;

import com.bonult.money.ConfigHolder;
import com.bonult.money.tools.OrderedProperties;
import com.bonult.money.tools.PropsTool;

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

public class ConfTable extends JFrame {

	public ConfTable(){
		String columns[] = {"Name", "X", "Y", "Width", "Height"};
		List<Object[]> confList = new ArrayList<>();
		File confDir = new File(ConfigHolder.USER_DIR + "conf");
		Properties props;
		if(confDir.exists()){
			for(File file : confDir.listFiles()){
				String name = file.getName();
				if(name.endsWith(".properties")){
					Object[] os = new Object[5];
					os[0] = name.substring(0, name.length() - 11);
					try{
						props = PropsTool.loadProps(file);
					}catch(IOException e){
						continue;
					}
					os[1] = PropsTool.getInt(props, "PROBLEM_AREA_X");
					os[2] = PropsTool.getInt(props, "PROBLEM_AREA_Y");
					os[3] = PropsTool.getInt(props, "PROBLEM_AREA__WIDTH");
					os[4] = PropsTool.getInt(props, "PROBLEM_AREA_HEIGHT");
					confList.add(os);
				}
			}
		}
		int height;
		int width;

		setLocation(150, 150);
		Container pane = getContentPane();
		pane.setLayout(null);
		Insets insets = pane.getInsets();

		Object[][] rows = new Object[confList.size()][5];
		for(int i = 0; i < confList.size(); i++){
			Object[] ooo = confList.get(i);
			Object[] ne = new Object[5];
			ne[0] = ooo[0];
			ne[1] = ooo[1];
			ne[2] = ooo[2];
			ne[3] = ooo[3];
			ne[4] = ooo[4];
			rows[i] = ne;
		}
		JTable table = new JTable(rows, columns);
		table.setPreferredScrollableViewportSize(new Dimension(500, 200));
		table.setFillsViewportHeight(true);
		table.addPropertyChangeListener(event -> {
			int row = table.getSelectedRow();
			int col = table.getSelectedColumn();

			if(row < 0 || col < 1)
				return;
			int x = -1;
			try{
				x = Integer.parseInt(table.getValueAt(row, col).toString());
			}catch(Exception e){
			}
			if(x >= 0){
				rows[row][col] = x;
				table.setValueAt(x, row, col);
			}else{
				table.setValueAt(confList.get(row)[col], row, col);
			}
		});
		JScrollPane scrollpane = new JScrollPane(table);

		Dimension dim = scrollpane.getPreferredSize();
		int x = insets.left + 20;
		int y = insets.top + 20;
		scrollpane.setBounds(x, y, dim.width, dim.height);
		pane.add(scrollpane);

		JTextField textField = new JTextField(10);

		JButton button = new JButton("添加");
		button.addActionListener(e -> {
			System.out.println(textField.getText());
		});

		pane.add(textField);
		pane.add(button);

		width = x + dim.width + 20 + insets.left;
		height = y + dim.height + 40 + insets.bottom;

		setSize(width, height);
		addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent e){
				super.windowClosing(e);
				int i = 0;
				for(Object[] o : confList){
					if(o[1].equals(rows[i][1]) && o[2].equals(rows[i][2]) && o[3].equals(rows[i][3]) && o[4].equals(rows[i][4])){
					}else{
						Properties props = new OrderedProperties();
						props.put("SCREEN_WIDTH", "1080");
						props.put("SCREEN_HEIGHT", "1920");
						props.put("PROBLEM_AREA_X", rows[i][1].toString());
						props.put("PROBLEM_AREA_Y", rows[i][2].toString());
						props.put("PROBLEM_AREA__WIDTH", rows[i][3].toString());
						props.put("PROBLEM_AREA_HEIGHT", rows[i][4].toString());
						ConfigHolder.writeConfig(ConfigHolder.USER_DIR + "conf" + File.separator, props, o[0] + ".properties");
					}
					i++;
				}
			}

		});
		setVisible(true);
	}


}