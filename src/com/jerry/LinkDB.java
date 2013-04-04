package com.jerry;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.List;

/**
 * 用来保存已经访问过url、正在访问的url、待访问的url
 */
public class LinkDB {
	private final String visitedNumFileName = "visitedNum.txt";
	private final String unVisitedUrlFileName = "un_visited.txt";
	private final String bloomFilterFileName = "bloom_filter.bin";
	private BloomFilter<String> bloomFilter = null; // 记录已经访问过的url
	private Queue<String> unVisitedUrl = null; // 待访问的url集合
	private List<String> bufferedUrl = null; // 缓存的url集合
//	private List<String> visitingUrl = null; // 正在访问的url集合
	private int bufferSize = 10000;
	private long visitedNum = 0;

	LinkDB() {
		// 成员变量初始化
		bloomFilter = new BloomFilter<String>();
		unVisitedUrl = new Queue<String>();
		bufferedUrl = new ArrayList<String>();
//		visitingUrl = new ArrayList<String>();
//		try {
//			visitedUrlFile = new RandomAccessFile(visitedUrlFileName, "rw");
//		} catch (FileNotFoundException e) {
//			e.printStackTrace();
//		}
		
		tryRecoverBloomFilter();
		tryRecoverUnVisitedUrl();
		tryRecoverVisitedNum();
	}

	/**
	 * 将url从正在访问移动到已经访问
	 * @param url 已经访问过的url
	 */
	synchronized public void addVisitedUrl(String url) {
//		visitingUrl.remove(url);
		bloomFilter.add(url);
		++visitedNum;
//		try {
//			visitedUrlFile.seek(visitedUrlFile.length());
//			visitedUrlFile.writeBytes(visitedNum + ":" + url);
//			visitedUrlFile.writeBytes("\n");
//		} catch (IOException e) {
//			System.out.println("文件已经关闭，无法写入！");
//		}
	}

	/**
	 * 待访问url队列的队头出队
	 * @return 队头url
	 */
	synchronized public String unVisitedUrlDeQueue() {
		String result = null;
		if (unVisitedUrl.isEmpty()) {
			JdbcUtils.getbufferedUrl(unVisitedUrl, bufferSize);
		}
		if (!unVisitedUrl.isEmpty()) {
			while (bloomFilter.contains(result = unVisitedUrl.deQueue()))
				;
		}
//		if (result != null) visitingUrl.add(result);
		return result;
	}

	/**
	 * 将未访问过的url放入待访问队列的队尾
	 * @param url 待入队的url
	 */
	synchronized public void addUnvisitedUrl(String url) {
		if (url != null && !url.trim().equals("") && !bloomFilter.contains(url)) {
			bufferedUrl.add(url);
		}
		if (bufferedUrl.size() > bufferSize)
			JdbcUtils.savebufferedUrl(bufferedUrl);
	}

	/**
	 * 已经访问过的url的个数
	 * @return
	 */
	synchronized public long getVisitedUrlNum() {
		return visitedNum;
	}
	
	/**
	 * 将BloomFilter保存到磁盘上，并关闭visitedUrlFile。
	 */
	synchronized public void close() {
    	saveBloomFilter();
    	saveUnVisitedUrl();
    	saveVisitedNum();
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
	
	/**
	 * 尝试恢复上次已经访问的链接数目
	 */
	protected void tryRecoverVisitedNum() {
		if (!new File(visitedNumFileName).exists()) return;
		RandomAccessFile visitedNumFile = null;
		try {
			visitedNumFile = new RandomAccessFile(visitedNumFileName, "r");
			String visitedNumS = visitedNumFile.readLine();
			this.visitedNum = Long.parseLong(visitedNumS);
			visitedNumFile.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * 保存BloomFilter到磁盘
	 */
	protected void saveBloomFilter() {
		RandomAccessFile bloomFilterFile = null;
		try {
			bloomFilterFile = new RandomAccessFile(bloomFilterFileName, "rw");
    		bloomFilter.write(bloomFilterFile); // 存储bloomFilter
    		bloomFilterFile.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * 保存访问队列和正在访问的链接到磁盘
	 */
	protected void saveUnVisitedUrl() {
		RandomAccessFile unVisitedUrlFile = null;
		try {
			unVisitedUrlFile = new RandomAccessFile(unVisitedUrlFileName, "rw");
    		// 将待抓取的和正在抓取的url存储起来
			String tmp;
    		while(!unVisitedUrl.isEmpty() && (tmp = unVisitedUrl.deQueue()) != null) {
    			unVisitedUrlFile.writeBytes(tmp);
    			unVisitedUrlFile.writeBytes("\n");
    		}
//    		for (String s : visitingUrl) {
//    			unVisitedUrlFile.writeBytes(s);
//    			unVisitedUrlFile.writeBytes("\n");
//    		}
    		unVisitedUrlFile.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * 保存已访问的链接数量到磁盘
	 */
	protected void saveVisitedNum() {
		RandomAccessFile visitedNumFile = null;
		try {
			visitedNumFile = new RandomAccessFile(visitedNumFileName, "rw");    		
    		visitedNumFile.writeBytes(String.valueOf(visitedNum)); // 存储visitedNum
    		visitedNumFile.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public static void main(String[] args) {
		LinkDB test = new LinkDB();
		test.saveVisitedNum();
	}
}