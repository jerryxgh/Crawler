package com.jerry;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Crawler implements Runnable {
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
			visitUrl = linkDB.unVisitedUrlDeQueue();
			if (visitUrl == null) continue;	
			HtmlParser parser = null;
			try {
				parser = new HtmlParser(visitUrl, filter);
			} catch (IOException e) {
//				linkDB.addUnvisitedUrl(visitUrl);
//				System.out.println(visitUrl + "无法到达!");
				continue;
			}
//			System.out.println("您好，我是线程：" + this.threadId);
//			System.out.println(visitUrl);
//			System.out.println(parser.getTitle());
//			System.out.println(parser.getText());
			contentDB.save(linkDB.getVisitedUrlNum(), parser.getText());
			linkDB.addVisitedUrl(visitUrl); // 该 url 放入到已访问的 URL 中	
			List<String> links = parser.getLinks(); // 提取出网页中的 URL
			for(String link : links) { // 新的未访问的 URL 入队
				linkDB.addUnvisitedUrl(link);
			}
		}
		--count;
		System.out.println("线程 " + threadId + " 已经退出，还有" + count + "个线程正在运行...");
	}

	/**
	 * 主程序入口
	 * @param args
	 */
	public static void main(String[] args) {
		// 初始化开始抓取集合
		if (args.length == 1) System.out.println("Using default init urls：[http://www.hhu.edu.cn]");
		if (args.length > 2 || args.length < 1) System.out.println("Usage: Crawler threadNumber [init file]");
		LinkDB linkDB = new LinkDB();
		if (args.length == 2) {
			try {
				BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(args[1])));
				String tmp;
				while((tmp = br.readLine()) != null)
					linkDB.addUnvisitedUrl(tmp);
			} catch (IOException e) {
				System.out.println("File " + args[1] + "doesn't exists, using default init urls：[http://www.hhu.edu.cn]");
			}
		} else {
			linkDB.addUnvisitedUrl("http://www.hhu.edu.cn");
		}
		
		int theradsNum = Integer.parseInt(args[0]);
		ContentDB contentDB = new ContentDB("/user/jerry/crawler/", "yyyyMMddHH");
		ExecutorService exec = Executors.newFixedThreadPool(theradsNum);
		for (int i = 0; i < theradsNum; ++i) {
			exec.submit(new Crawler(linkDB, contentDB));
		}
		System.out.println("您好，正在努力为您抓取网页！输入 exit 退出：");
		BufferedReader stdin = new BufferedReader(new InputStreamReader(System.in));
		String signal;
		try {
			while((signal = stdin.readLine()) != null && !signal.equals("exit")) {
				System.out.println(signal);
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