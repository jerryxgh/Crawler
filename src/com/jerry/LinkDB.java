package com.jerry;

import java.util.HashSet;
import java.util.Set;

/**
 * 用来保存已经访问过 Url 和待访问的 Url 的类
 */
public class LinkDB {

	// 已访问的 url 集合
	private static Set<String> visitedUrl = new HashSet<String>();
	// 待访问的 url 集合
	private static Queue<String> unVisitedUrl = new Queue<String>();
	// 正在访问的 url 集合
	private static Set<String> visitingUrl = new HashSet<String>();
	
	synchronized public static Queue<String> getUnVisitedUrl() {
		return unVisitedUrl;
	}

	synchronized public static void addVisitedUrl(String url) {
		visitingUrl.remove(url);
		visitedUrl.add(url);
	}

	synchronized public static void removeVisitedUrl(String url) {
		visitedUrl.remove(url);
	}

	synchronized public static String unVisitedUrlDeQueue() {
		String result = unVisitedUrl.isEmpty() ? null : unVisitedUrl.deQueue();
		visitingUrl.add(result);
		return result;
	}

	// 保证每个 url 只被访问一次
	synchronized public static void addUnvisitedUrl(String url) {
		visitingUrl.remove(url);
		if (url != null && !url.trim().equals("") && !visitedUrl.contains(url)
				&& !unVisitedUrl.contians(url))
			unVisitedUrl.enQueue(url);
	}

	synchronized public static int getVisitedUrlNum() {
		return visitedUrl.size();
	}
}