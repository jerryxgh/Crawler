package com.jerry.crawler;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.apache.log4j.Logger;

public class Crawler implements Runnable {
	Logger log = Logger.getLogger(Crawler.class);
	
	private static int count = 0;
	private final int threadId = count++;
	private LinkDB linkDB;
	private ContentDB contentDB;
	Crawler(LinkDB linkDB, ContentDB contentDB) {
		this.linkDB = linkDB;
		this.contentDB = contentDB;
	}
	private static LinkFilter filter = new LinkFilter() {
		public boolean accept(String url) {
			if(!url.endsWith(".ppt") && !url.endsWith(".xls")
					&& !url.endsWith(".doc") && !url.endsWith(".pdf")
					&& !url.endsWith(".zip") && !url.endsWith(".docx")
					&& !url.startsWith("mailto") && !url.endsWith(".jpg")
					&& !url.endsWith(".png"))
				return true;
			else
				return false;
		}
	};

	/* 爬取线程 */
	@Override
	public void run() {
		String visitUrl;
		while (!Thread.interrupted()) { // 始终循环
			log.info("Thread:" + threadId + ":" +  1);
			visitUrl = linkDB.unVisitedUrlDeQueue();
			log.info("Thread:" + threadId + ":" +  2);
			linkDB.addVisitedUrl(visitUrl); // 该 url 放入到已访问的 URL 中
			if (visitUrl == null) continue;	
			HtmlParser parser = null;
			try {
				log.info("Thread:" + threadId + ":" +  3);
				parser = new HtmlParser(visitUrl, filter);
			} catch (IOException e) {
				log.error(e.getMessage());
				continue;
			}
			log.info("Thread:" + threadId + ":" +  4);
			contentDB.save(linkDB.getVisitedUrlNum(), parser.getText());
			log.info("Thread:" + threadId + ":" +  5);
			List<String> links = parser.getLinks(); // 提取出网页中的 URL
			log.info("Thread:" + threadId + ":" +  6);
			for(String link : links) { // 新的未访问的 URL 入队
//				long startTime = System.nanoTime();
				linkDB.addBufferedUrl(link);
//				System.out.println("线程 " + threadId + " linkDB.addUnvisitedUrl(link)耗时：" + (System.nanoTime() - startTime));
			}
		}
		--count;
	}

	/**
	 * 主程序入口
	 * @param args
	 */
	public static void main(String[] args) {
		// 初始化开始抓取集合
		LinkDB linkDB = new LinkDB();
		if (args.length == 1) {
			System.out.println("Using default init urls：[http://www.hhu.edu.cn]");
			linkDB.addUnvisitedUrl("http://www.hhu.edu.cn");
		}
		if (args.length > 2 || args.length < 1) {
			System.out.println("Usage: Crawler threadNumber [init file]");
			return;
		}
		if (args.length == 2) {
			try {
				BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(args[1])));
				String tmp;
				while((tmp = br.readLine()) != null)
					linkDB.addUnvisitedUrl(tmp);
			} catch (IOException e) {
				System.out.println("File " + args[1] + " doesn't exists, using default init urls：[http://www.hhu.edu.cn]");
				linkDB.addUnvisitedUrl("http://www.hhu.edu.cn");
			}
		}
		
		int threadNum = 2;
		if (args.length >= 1) threadNum = Integer.parseInt(args[0]);
		ContentDB contentDB = new ContentDB("/user/jerry/crawler/", "yyyyMMddHH");		// TODO 实验
		ExecutorService exec = Executors.newFixedThreadPool(threadNum);
		for (int i = 0; i < threadNum; ++i) {
			exec.submit(new Crawler(linkDB, contentDB));
		}
		System.out.println("您好，正在努力为您抓取网页！输入 exit 退出：");
		BufferedReader stdin = new BufferedReader(new InputStreamReader(System.in));
		String signal;
		try {
			while((signal = stdin.readLine()) != null && !signal.equals("exit")) {
				System.out.println(signal);
				System.out.println("正在运行的线程数：" + count);
			}
			System.out.println("您好，正在退出，请稍后...！");
			exec.shutdownNow();
			while (count > 0) {
				try {
					Thread.sleep(500);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
//				System.out.println("count=" + count);
			}
			System.out.println("再见！");
			linkDB.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}