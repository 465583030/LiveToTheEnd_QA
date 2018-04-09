package com.bonult.money;

import com.bonult.money.ocr.OCR;
import com.bonult.money.screenshot.GetScreenshot;
import com.bonult.money.search.Search;
import com.bonult.money.search.SearchResultItem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.*;
import java.lang.reflect.Method;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author bonult
 */
public class MakingMoneyWithQA {

	private static final Logger LOGGER = LoggerFactory.getLogger(MakingMoneyWithQA.class);

	private GetScreenshot getScreenshot;
	private OCR ocr;
	private Search search;
	private ExecutorService threadPool;

	public static MakingMoneyWithQA instance;

	public MakingMoneyWithQA(GetScreenshot getScreenshot, OCR ocr, Search search){
		this.getScreenshot = getScreenshot;
		this.ocr = ocr;
		this.search = search;
		threadPool = Executors.newFixedThreadPool(10);
		instance = this;
	}

	public void addTask(Runnable r){
		threadPool.submit(r);
	}

	public void shutdown(){
		getScreenshot.close();
		threadPool.shutdown();
	}

	public void run(){
		if(!ConfigHolder.CONFIG.isOk())
			return;
		long start = System.currentTimeMillis();
		Main.clearMessage();

		byte[] imgFileContent = getScreenshot.getImg();
		if(imgFileContent == null){
			Main.insertText("=======获取截图失败=======\n", Color.RED, 14);
			return;
		}else if(imgFileContent.length == 1){
			Main.insertText("=======配置信息已发送=======\n", Color.BLUE, 14);
			return;
		}

		List<String> words = ocr.getWords(imgFileContent);
		if(words.size() == 0){
			Main.insertText("=======没有识别到文字=======\n", Color.RED, 14);
			return;
		}

		List<Option> options = new ArrayList<>(4);

		String questionSeg = separateQuestionAndAnswers(options, words);

		Main.insertText("###### 正在计算中...\n" + questionSeg + "\n", Color.BLUE, 14);

		final String question = getQuestion(questionSeg);

		threadPool.submit(() -> openURL(question));

		search(options, question);

		Main.insertText("\n=========用时 " + (System.currentTimeMillis() - start) + " ms=========\n", Color.BLUE, 14);
	}

	private void search(List<Option> options, String question){
		AtomicInteger aiQA = new AtomicInteger(0);
		Callable<Long> callableQA = () -> {
			int index = aiQA.getAndIncrement();
			String A = options.get(index).option;
			return search.getSearchResultCount(question + " " + A);
		};

		AtomicInteger aiA = new AtomicInteger(0);
		Callable<Long> callableA = () -> {
			int index = aiA.getAndIncrement();
			String A = options.get(index).option;
			return search.getSearchResultCount(A);
		};

		List<Future<Long>> futuresQA = new ArrayList<>(options.size());
		List<Future<Long>> futuresA = new ArrayList<>(options.size());

		for(int i = 0; i < options.size(); i++){
			Future<Long> futureQA = threadPool.submit(callableQA);
			futuresQA.add(futureQA);
			Future<Long> futureA = threadPool.submit(callableA);
			futuresA.add(futureA);
		}

		Future<List<SearchResultItem>> futureQ = threadPool.submit(() -> search.getSearchResult(question));

		for(int j = 0; j < options.size(); j++){
			try{
				options.get(j).quesAndOptCount = futuresQA.get(j).get();
			}catch(Exception e){
				e.printStackTrace();
				Main.insertText("并行任务出错\n", Color.RED, 14);
			}
			try{
				options.get(j).optCount = futuresA.get(j).get();
			}catch(Exception e){
				e.printStackTrace();
				Main.insertText("并行任务出错\n", Color.RED, 14);
			}
		}

		if(options.size() > 0){
			Main.insertText("===========================\n不精确搜索结果：\n", Color.BLUE, 14);
			int maxIndex = 0;
			double max = -1;
			for(int i = 0; i < options.size(); i++){
				if(options.get(i).getWeight() > max){
					max = options.get(i).getWeight();
					maxIndex = i;
				}
			}
			Main.insertBoldMessage(options.get(maxIndex).option + "\n", Color.BLUE, 14);
		}

		double sum = 0;
		long countSum = 0;
		for(int j = options.size() - 1; j >= 0; j--){
			sum += options.get(j).getWeight();
			countSum += options.get(j).quesAndOptCount;
		}

		for(int j = options.size() - 1; j >= 0; j--){
			Option ops = options.get(j);
			Main.insertText(String.format("| %2d%% | %2d%% | %s\n", (int)(ops.getWeight() / sum * 100), (int)(ops.quesAndOptCount * 1.0 / countSum * 100), ops.option), Color.BLUE, 14);
		}

		try{
			if(options.size() > 0){
				List<SearchResultItem> matches = futureQ.get(7, TimeUnit.SECONDS);
				for(SearchResultItem match : matches){
					for(Option option : options){
						option.computeMatchNum(match);
					}
				}
				Main.insertText("===========================\n精确搜索结果：\n", Color.BLUE, 14);
				for(int j = options.size() - 1; j >= 0; j--){
					Option ops = options.get(j);
					String str = String.format("| %2d | %s\n", ops.matchNum/*, ops.subStrMatchNum*/, ops.option);
					if(ops.matchNum > 0 /*|| ops.subStrMatchNum > 0*/)
						Main.insertBoldMessage(str, Color.BLUE, 14);
					else
						Main.insertText(str, Color.BLUE, 14);
				}
			}else{
				futureQ.cancel(true);
			}
		}catch(Exception e){
			LOGGER.error("未匹配到答案", e);
		}
	}

	private String separateQuestionAndAnswers(List<Option> options, List<String> words){
		int lineCount = words.size();
		String questionSeg = "";

		int quesLineNum;
		for(quesLineNum = 0; quesLineNum < lineCount; quesLineNum++){
			int qIndex = words.get(quesLineNum).indexOf('?');
			if(qIndex > -1){
				break;
			}
		}

		if(lineCount >= 4){
			int i = lineCount - 1 - quesLineNum > 3 && ConfigHolder.CONFIG.getMaxOptionNum() == 4 ? 4 : 3; // 判断选项个数
			for(; i > 0; i--){
				lineCount--;
				options.add(new Option(removeABC(words.get(lineCount))));
			}
		}
		for(int i = 0; i < lineCount; i++){
			String q = words.get(i);
			int qIndex = q.indexOf('?');
			if(qIndex > -1){
				questionSeg += q.substring(0, qIndex);
				break;
			}else
				questionSeg += q;
		}
		return questionSeg;
	}

	private String getQuestion(String questionSeg){
		if(!ConfigHolder.CONFIG.isRmvQuesNum())
			return questionSeg;
		int quesStart = 0;
		for(; quesStart < questionSeg.length(); quesStart++){
			char p = questionSeg.charAt(quesStart);
			if((p >= '0' && p <= '9')){
				if(quesStart == 1){
					quesStart++;
					if(questionSeg.charAt(quesStart) == '.'){
						quesStart++;
					}
					break;
				}
			}else if(p == 'B'){ // 有时候OCR会把 8 识别为 B
				quesStart++;
				if(questionSeg.charAt(quesStart) == '.'){
					quesStart++;
				}
				break;
			}else if(p == '.'){
				quesStart++;
				break;
			}else{
				break;
			}
		}
		return questionSeg.substring(quesStart, questionSeg.length()).replaceAll("[,，;；'\"!`~:：《》]", "");
	}

	private String removeABC(String s){
		if(s.length() > 2 && (s.charAt(1) == '.' || s.charAt(1) == ':')){
			return s.substring(2);
		}else if(ConfigHolder.CONFIG.isRmvOptNum() && s.length() > 1 && s.matches("^[ABCD].*")){
			return s.substring(1);
		}
		return s;
	}

	/**
	 * 打开浏览器，自动百度搜索问题
	 *
	 * @param word 问题
	 */
	private void openURL(String word){
		// 见http://yuncode.net/code/c_516d4130f11f245
		try{
			String url = "http://www.baidu.com/s?ie=utf-8&tn=ichuner&lm=0&word=" + URLEncoder.encode(word, "UTF-8") + "&rn=20";
			String os = System.getProperty("os.name", "");
			if(os.startsWith("Windows")){
				Runtime.getRuntime().exec("rundll32 url.dll,FileProtocolHandler " + url);
			}else if(os.startsWith("Mac OS")){
				Class fileMgr = Class.forName("com.apple.eio.FileManager");
				Method openURL = fileMgr.getDeclaredMethod("openURL", new Class[]{String.class});
				openURL.invoke(null, new Object[]{url});
			}else{
				// Unix or Linux
				String[] browsers = {"firefox", "opera", "konqueror", "epiphany", "mozilla", "netscape"};
				String browser = null;
				for(int count = 0; count < browsers.length && browser == null; count++)
					if(Runtime.getRuntime().exec(new String[]{"which", browsers[count]}).waitFor() == 0)
						browser = browsers[count];
				if(browser == null)
					throw new Exception("Could not find web browser");
				else
					Runtime.getRuntime().exec(new String[]{browser, url});
			}
		}catch(Exception e){
			LOGGER.error("打开浏览器失败", e);
		}
	}
}

class Option {
	String option;
	String abc;
	long quesAndOptCount;
	long optCount;
	int matchNum;
	//	int subStrMatchNum;
	private double weight;

	Option(String option){
		this.option = option;
		weight = -1;
		matchNum = 0;
//		subStrMatchNum=0;
	}

	public double getWeight(){
		if(weight < 0){
			weight = quesAndOptCount * 1.0 / optCount;
		}
		return weight;
	}

	void computeMatchNum(SearchResultItem item){
		if(item.title.contains(option))
			matchNum++;
		if(item.description.contains(option))
			matchNum++;
	}
}
