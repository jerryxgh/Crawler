package com.jerry;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * 用来保存已经访问过url、正在访问的url、待访问的url
 */
public class LinkDB {
	private final String visitedUrlFileName = "visited.txt";
	private final String unVisitedUrlFileName = "un_visited.txt";
	private final String bloomFilterFileName = "bloom_filter.bin";
	private BloomFilter<String> bloomFilter = null; // 记录已经访问过的url
	private RandomAccessFile visitedUrlFile = null; // 已访问过的url文件
	private Queue<String> unVisitedUrl = null; // 待访问的url集合
	private List<String> visitingUrl = null; // 正在访问的url集合
	private long hasVisitedNum = 0;

	LinkDB() {
		// 成员变量初始化
		bloomFilter = new BloomFilter<String>();
		unVisitedUrl = new Queue<String>();
		visitingUrl = new ArrayList<String>();
		try {
			visitedUrlFile = new RandomAccessFile(visitedUrlFileName, "rw");
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		
		tryRecoverBloomFilter();
		tryRecoverUnVisitedUrl();
	}

	/**
	 * 将url从正在访问移动到已经访问
	 * @param url 已经访问过的url
	 */
	synchronized public void addVisitedUrl(String url) {
		visitingUrl.remove(url);
		bloomFilter.add(url);
		++hasVisitedNum;
		try {
			visitedUrlFile.seek(visitedUrlFile.length());
			visitedUrlFile.writeBytes(url);
			visitedUrlFile.writeBytes("\n");
		} catch (IOException e) {
			System.out.println("文件已经关闭！");
		}
	}

	/**
	 * 待访问url队列的队头出队
	 * @return 队头url
	 */
	synchronized public String unVisitedUrlDeQueue() {
		String result = null;
		if (!unVisitedUrl.isEmpty()) {
			while (bloomFilter.contains(result = unVisitedUrl.deQueue()))
				;
		}
		if (result != null) visitingUrl.add(result);
		return result;
	}

	/**
	 * 将未访问过的url放入待访问队列的队尾
	 * @param url 待入队的url
	 */
	synchronized public void addUnvisitedUrl(String url) {
		visitingUrl.remove(url);
		if (url != null && !url.trim().equals("") && !bloomFilter.contains(url)) {
			unVisitedUrl.enQueue(url);
		}
	}

	/**
	 * 已经访问过的url的个数
	 * @return
	 */
	synchronized public long getVisitedUrlNum() {
		return hasVisitedNum;
	}
	
	/**
	 * 将BloomFilter保存到磁盘上，并关闭visitedUrlFile。
	 */
	public void close() {
		RandomAccessFile bloomFilterFile = null;
		RandomAccessFile unVisitedUrlFile = null;
		try {
			bloomFilterFile = new RandomAccessFile(bloomFilterFileName, "rw");
			unVisitedUrlFile = new RandomAccessFile(unVisitedUrlFileName, "rw");
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
    	
    	try {
    		// 存储bloomFilter
    		bloomFilter.write(bloomFilterFile);
    		bloomFilterFile.close();
    		// 将待抓取的和正在抓取的url存储起来
    		String tmp;
    		while(!unVisitedUrl.isEmpty() && (tmp = unVisitedUrl.deQueue()) != null) {
    			unVisitedUrlFile.writeBytes(tmp);
    			unVisitedUrlFile.writeBytes("\n");
    		}
    		for (String s : visitingUrl) {
    			unVisitedUrlFile.writeBytes(s);
    			unVisitedUrlFile.writeBytes("\n");
    		}
    		unVisitedUrlFile.close();
			visitedUrlFile.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * 尝试载入上次的BloomFilter的状态
	 */
	protected void tryRecoverBloomFilter() {
		if (!new File(bloomFilterFileName).exists()) return;
		RandomAccessFile bloomFilterFile = null;
		try {
			bloomFilterFile = new RandomAccessFile(bloomFilterFileName, "r");
			bloomFilter.readFields(bloomFilterFile);
			bloomFilterFile.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * 尝试恢复上次的未访问url
	 */
	protected void tryRecoverUnVisitedUrl() {
		if (!new File(unVisitedUrlFileName).exists()) return;
		RandomAccessFile unVisitedUrlFile = null;
		try {
			unVisitedUrlFile = new RandomAccessFile(unVisitedUrlFileName, "r");
			String line = null;
			while ((line = unVisitedUrlFile.readLine()) != null)
				addUnvisitedUrl(line);
			unVisitedUrlFile.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}