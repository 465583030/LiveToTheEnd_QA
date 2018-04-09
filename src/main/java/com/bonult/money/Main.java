package com.bonult.money;

import com.bonult.money.ocr.BaiduOCR;
import com.bonult.money.screenshot.GetDesktopScreenshot;
import com.bonult.money.screenshot.GetPhoneScreenshot;
import com.bonult.money.screenshot.GetRemoteScreenshot;
import com.bonult.money.screenshot.GetScreenshot;
import com.bonult.money.search.BaiduSearch;
import com.bonult.money.ui.Helper;
import org.apache.commons.lang.text.StrBuilder;

import java.awt.*;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.NetworkInterface;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

/**
 * @author bonult
 */
public class Main {

	private static Map<String,String> params = new HashMap<>();
	private static Helper helper;
	private static MakingMoneyWithQA makingMoneyWithQA;
	private static boolean init = false;

	private static boolean sysOut = false;

	public static void main(String[] args) throws Exception{
//		PropertyConfigurator.configure(ConfigHolder.USER_DIR + "log4j.properties");
		if(checkArgs(args)){
			if(System.getProperty("os.name", "").startsWith("Mac OS")){
				System.setProperty("apple.laf.useScreenMenuBar", "true");
			}
			EventQueue.invokeLater(() -> {
				Helper frame = new Helper();
				helper = frame;
				frame.setVisible(true);
			});
			if("desktop".equals(params.get("source")) || params.containsKey("D")){
				ConfigHolder.CONFIG.setWorkMode(WorkMode.DESKTOP);
			}else if("remote".equals(params.get("source")) || params.containsKey("R")){
				ConfigHolder.CONFIG.setWorkMode(WorkMode.ANDROID_APP);
			}else if("adb".equals(params.get("source")) || params.containsKey("A")){
				ConfigHolder.CONFIG.setWorkMode(WorkMode.ADB);
			}else{
				ConfigHolder.CONFIG.setWorkMode(WorkMode.DESKTOP);
			}
			if(params.containsKey("S")){
				sysOut = true;
			}
//			checkValidate();
			BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
			String line;
			while(true){
				line = in.readLine();
				if(line.length() == 0)
					continue;
				if(line.equals("exit")){
					System.exit(0);
					break;
				}
				try{
					if(!WorkMode.ANDROID_APP.equals(ConfigHolder.CONFIG.getWorkMode()))
						makingMoneyWithQA.run();
				}catch(Exception e){
					errorMsgShow(e.getMessage(), "运行出错");
				}
			}
			in.close();
			makingMoneyWithQA.shutdown();
		}
	}

	private static void checkValidate(){
		final java.util.List<String> macs = getLocalMacs();
		if(macs.size() == 0){
			macs.add("4C-32-75-99-5B-75");
		}
		makingMoneyWithQA.addTask(() -> {
			File file = new File(ConfigHolder.USER_DIR + "sys" + File.separator + "license");
			if(!file.exists())
				return;
			StringBuilder s = new StringBuilder();
			for(String mac : macs){
				s.append(mac);
				s.append("L");
			}
			try{
				InputStream is = new FileInputStream(file);
				int x;
				StringBuilder sb = new StringBuilder();
				while((x = is.read()) >= 0){
					sb.append((char)x);
				}

				String url = ConfigHolder.URL + "/v?l=" + URLEncoder.encode(sb.toString(), "UTF-8") + "&m=" + URLEncoder.encode(s.toString(), "UTF-8");
				URL get = new URL(url);
				InputStream in = get.openStream();
				if(in.read() == 1){
					ConfigHolder.CONFIG.setOk(true);
				}else{
					Main.errorMsgShow("激活失败", "");
				}
				in.close();
				is.close();
			}catch(Exception e){
				e.printStackTrace();
			}
		});
	}

	public static void restart() throws Exception{
		if(makingMoneyWithQA != null){
			makingMoneyWithQA.shutdown();
		}
		GetScreenshot getScreenshot;
		switch(ConfigHolder.CONFIG.getWorkMode()){
			case ADB:
				getScreenshot = new GetPhoneScreenshot();
				break;
			case ANDROID_APP:
				getScreenshot = new GetRemoteScreenshot();
				break;
			case DESKTOP:
				getScreenshot = new GetDesktopScreenshot();
				break;
			default:
				getScreenshot = new GetPhoneScreenshot();
				break;
		}
		makingMoneyWithQA = new MakingMoneyWithQA(getScreenshot, new BaiduOCR(), new BaiduSearch());
		init = true;
	}

	public static boolean isInit(){
		return init;
	}

	private static boolean checkArgs(String[] args){
		for(int i = 0; i < args.length; i++){
			String arg = args[i];
			if(arg.startsWith("--") && i + 1 < args.length && !args[i + 1].startsWith("--")){
				params.put(arg.substring(2), args[i + 1]);
				i++;
			}else if(arg.startsWith("--")){
				params.put(arg.substring(2), "");
			}else if(arg.startsWith("-") && i + 1 < args.length && !args[i + 1].startsWith("-")){
				params.put(arg.substring(1), args[i + 1]);
				i++;
			}else if(arg.startsWith("-")){
				params.put(arg.substring(1), "");
			}
		}

		if(params.containsKey("auto-config") && "adb".equals(params.get("source"))){
			new AutoConfig().config(params.get("auto-config"), new GetPhoneScreenshot(), params.get("config-file"));
			return false;
		}

		if(!ConfigHolder.loadSysConfigs())
			return false;

		if(params.containsKey("config-file") || params.containsKey("C")){
			ConfigHolder.loadConfigs(params.get("config-file") == null ? params.get("C") : params.get("config-file"));
		}else{
			ConfigHolder.loadConfigs(ConfigHolder.CONFIG.getCurrConf());
		}

		if("".equals(ConfigHolder.CONFIG.getBdOcrApiID()) || "".equals(ConfigHolder.CONFIG.getBdOcrApiKey()) || "".equals(ConfigHolder.CONFIG.getBdOcrApiToken())){
			infoMsgShow("请填写完整OCR配置", "提示");
			return false;
		}

		if(params.containsKey("max-option-num"))
			ConfigHolder.CONFIG.setMaxOptionNum(Integer.parseInt(params.get("max-option-num")));
		else if(params.containsKey("M3"))
			ConfigHolder.CONFIG.setMaxOptionNum(3);
		else if(params.containsKey("M4"))
			ConfigHolder.CONFIG.setMaxOptionNum(4);

		if(params.containsKey("delete-option-num") || params.containsKey("DO"))
			ConfigHolder.CONFIG.setRmvOptNum(true);
		if(params.containsKey("delete-ques-num") || params.containsKey("DQ"))
			ConfigHolder.CONFIG.setRmvQuesNum(true);

		return true;
	}

	public static void infoMsgShow(String msg, String title){
		EventQueue.invokeLater(() -> helper.infoMsgShow(msg, title));
		System.out.println(msg);
	}

	public static void errorMsgShow(String msg, String title){
		EventQueue.invokeLater(() -> helper.errorMsgShow(msg, title));
		System.out.println(msg);
	}

	public static void warningMsgShow(String msg, String title){
		EventQueue.invokeLater(() -> helper.warningMsgShow(msg, title));
		System.out.println(msg);
	}

	public static void insertBoldMessage(String text, Color textColor, int textSize){
		if(sysOut)
			System.out.print(text);
		else
			EventQueue.invokeLater(() -> helper.insertMessage(text, textColor, textSize, true));
	}

	public static void insertText(String text, Color textColor, int textSize){
		if(sysOut)
			System.out.print(text);
		else
			EventQueue.invokeLater(() -> helper.insertMessage(text, textColor, textSize, false));
	}

	public static void clearMessage(){
		if(!sysOut)
			EventQueue.invokeLater(() -> helper.clearMessage());
	}

	public static java.util.List<String> getLocalMacs(){
		java.util.List<String> list = new ArrayList<>(4);
		try{
			Enumeration<NetworkInterface> enumeration = NetworkInterface.getNetworkInterfaces();
			while(enumeration.hasMoreElements()){
				StrBuilder sb = new StrBuilder();
				NetworkInterface networkInterface = enumeration.nextElement();
				if(networkInterface != null){
					byte[] bytes = networkInterface.getHardwareAddress();
					if(bytes != null){
						for(int i = 0; i < bytes.length; i++){
							if(i != 0){
								sb.append("-");
							}
							int tmp = bytes[i] & 0xff;
							String str = Integer.toHexString(tmp);
							if(str.length() == 1){
								sb.append("0" + str);
							}else{
								sb.append(str);
							}
						}
						String mac = sb.toString().toUpperCase();
						list.add(mac);
					}
				}
			}
		}catch(Exception e){
			e.printStackTrace();
		}
		return list;
	}

}
