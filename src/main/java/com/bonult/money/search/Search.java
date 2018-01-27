package com.bonult.money.search;

import java.util.List;

/**
 * 调用百度搜索，返回特定结果
 *
 * @author bonult
 */
public interface Search {

	/**
	 * 返回结果总数
	 *
	 * @param word 查询关键字
	 * @return 总数（查询失败返回0）
	 */
	long getSearchResultCount(String word);

	/**
	 * 从百度返回的HTML中提取每条记录的标题和描述
	 *
	 * @param word 查询关键字
	 * @return （标题+描述）的列表
	 */
	List<SearchResultItem> getSearchResult(String word);

}
