package com.jerry;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;
import java.util.Set;

public class Crawler {
    /* 使用种子 url 初始化 URL 队列*/
    private void initCrawlerWithSeeds(String[] seeds) {
        for (int i=0; i<seeds.length; i++)
            LinkDB.addUnvisitedUrl(seeds[i]);
    }
	
    /* 爬取方法*/
    public void crawling(String[] seeds) {
        LinkFilter filter = new LinkFilter() {
                //提取以 http://www.twt.edu.cn 开头的链接
                public boolean accept(String url) {
                    if(url.startsWith("http://www.java2s.com/Code"))
                        return true;
                    else
                        return false;
                }
            };
        //初始化 URL 队列
        initCrawlerWithSeeds(seeds);
        
        //循环条件：待抓取的链接不空时
        while(!LinkDB.unVisitedUrlsEmpty()) {
            //队头 URL 出对
            String visitUrl = LinkDB.unVisitedUrlDeQueue();
            if(visitUrl == null) continue;
            HtmlParser parser;
			try {
				parser = new HtmlParser(visitUrl, filter);
			} catch (IOException e) {
//				e.printStackTrace();
				LinkDB.addUnvisitedUrl(visitUrl);
				System.out.println(visitUrl + "can't be reached. But I will try it again.");
				continue;
			}
            //该 url 放入到已访问的 URL 中
            LinkDB.addVisitedUrl(visitUrl);
           
            // 保存网页中的代码（如果有的话）
            saveCode(visitUrl, parser);

            //提取出网页中的 URL
            Set<String> links = parser.getLinks();
            
            //新的未访问的 URL 入队
            for(String link : links) {
                LinkDB.addUnvisitedUrl(link);
            }
            
            
        }
    }
    
    /**
     * 保存页面中的代码及其标题，目录结构与其对应的 url 相对应
     * 其中代码的标题保存在 title 文件中，代码保存在 code0.txt code1.txt ... 中
     * @param url 页面对应的绝对地址
     * @param parser 页面的解析器
     */
    public void saveCode(String url, HtmlParser parser) {
    	// 页面中没有代码，返回
    	System.out.println("正在访问的链接: " + url);
    	if (parser.getCodeTitle() == null) {
    		System.out.println("该链接没有代码。。。");
    		return;
    	}
    	
    	System.out.println("CodeTitle:" + parser.getCodeTitle());
//    	System.out.println(parser.getCodes().get(0));
    	
    	// 创建代码要保存的目录
    	String directory = "D:" + url.substring(6, url.lastIndexOf('.'));
    	File d = new File(directory);
    	
    	// 代码已经保存了
    	if (d.exists()) {
    		return;
    	}
    	d.mkdirs();
    	
    	// 保存代码的标题和代码
    	FileWriter fileWriter = null;
    	try {
			fileWriter = new FileWriter(directory + "/title.txt");
			fileWriter.write(parser.getCodeTitle());
			fileWriter.close();
			
			List<String> codes = parser.getCodes();
			for (int i = 0; i < codes.size(); ++i) {
				fileWriter = new FileWriter(directory + "/code" + i + ".txt");
				fileWriter.write(codes.get(i));
				fileWriter.close();
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
    }
    //main 方法入口
    public static void main(String[]args) {
        Crawler crawler = new Crawler();
        crawler.crawling(new String[]{"http://www.java2s.com"});
    }
}