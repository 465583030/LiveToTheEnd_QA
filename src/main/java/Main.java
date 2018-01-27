import com.bonult.money.MakingMoneyWithQA;
import com.bonult.money.config.AutoConfig;
import com.bonult.money.config.ConfigHolder;
import com.bonult.money.ocr.BaiduOCR;
import com.bonult.money.screenshot.GetDesktopScreenshot;
import com.bonult.money.screenshot.GetPhoneScreenshot;
import com.bonult.money.screenshot.GetRemoteScreenshot;
import com.bonult.money.search.BaiduSearch;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;

/**
 * @author bonult
 */
public class Main {

	private static Map<String,String> params = new HashMap<>();

	public static void main(String[] args) throws Exception{
		MakingMoneyWithQA makingMoneyWithQA;
		boolean canUserInput = true;
		if(checkArgs(args)){
			if("desktop".equals(params.get("source")) || params.containsKey("D")){
				makingMoneyWithQA = new MakingMoneyWithQA(new GetDesktopScreenshot(), new BaiduOCR(), new BaiduSearch());
			}else if("remote".equals(params.get("source")) || params.containsKey("R")){
				makingMoneyWithQA = new MakingMoneyWithQA(new GetRemoteScreenshot(), new BaiduOCR(), new BaiduSearch());
				canUserInput=false;
			}else{
				makingMoneyWithQA = new MakingMoneyWithQA(new GetPhoneScreenshot(), new BaiduOCR(), new BaiduSearch());
			}
			BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
			String line;
			while(true){
				line = in.readLine();
				if(line.length() == 0)
					continue;
				if(line.equals("exit"))
					break;
				try{
					if(canUserInput)
						makingMoneyWithQA.run();
				}catch(Exception e){
					System.out.println("运行出错 " + e.getMessage());
				}
			}
			in.close();
			makingMoneyWithQA.shutdown();
		}
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

		if(params.containsKey("auto-config") && !"desktop".equals(params.get("source"))){
			new AutoConfig().config(params.get("auto-config"), new GetPhoneScreenshot(), params.get("config-file"));
			return false;
		}

		if(params.containsKey("config-file") || params.containsKey("C")){
			ConfigHolder.loadConfigs(params.get("config-file") == null ? params.get("C") : params.get("config-file"));
		}else{
			ConfigHolder.loadConfigs("config.properties");
		}

		if("".equals(ConfigHolder.CONFIG.getBdOcrApiID()) || "".equals(ConfigHolder.CONFIG.getBdOcrApiKey()) || "".equals(ConfigHolder.CONFIG.getBdOcrApiToken())){
			System.out.println("请填写完整OCR配置");
			return false;
		}

		if(params.containsKey("max-option-num"))
			ConfigHolder.CONFIG.setMaxOptionNum(Integer.parseInt(params.get("max-option-num")));
		else if(params.containsKey("M3"))
			ConfigHolder.CONFIG.setMaxOptionNum(3);
		else if(params.containsKey("M4"))
			ConfigHolder.CONFIG.setMaxOptionNum(4);

		if(params.containsKey("delete-ques-num") || params.containsKey("DEL"))
			ConfigHolder.CONFIG.setRmvQuesNum(true);

		return true;
	}

}
