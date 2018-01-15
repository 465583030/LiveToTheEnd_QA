import com.bonult.money.MakingMoneyWithQA;
import com.bonult.money.impl.DefaultGetScreenshot;
import com.bonult.money.impl.DefaultOCR;
import com.bonult.money.impl.DefaultSearch;

import java.util.Scanner;

/**
 * @author bonult
 */
public class Main {

	public static void main(String[] args){
		MakingMoneyWithQA makingMoneyWithQA = new MakingMoneyWithQA(new DefaultGetScreenshot(), new DefaultOCR(), new DefaultSearch());
		if(makingMoneyWithQA.checkArgs(args)){
			Scanner in = new Scanner(System.in);
			String line;
			while((line = in.next()) != null){
				if(line.equals("exit"))
					break;
				try{
					makingMoneyWithQA.run();
				}catch(Exception e){
					System.out.println("运行出错 " + e.getMessage());
				}
			}
			in.close();
			makingMoneyWithQA.shutdown();
		}
	}

}
