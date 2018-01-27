package com.bonult.money.ocr;

import java.util.List;

/**
 * 识别图片上的文字
 *
 * @author bonult
 */
public interface OCR {

	/**
	 * 调用OCR，识别问题和选项
	 *
	 * @param bytes 图片文件内容
	 * @return 识别结果
	 */
	List<String> getWords(byte[] bytes);

}
