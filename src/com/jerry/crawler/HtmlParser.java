package com.jerry.crawler;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

public class HtmlParser {
    private String url;
    private Document doc;
    private LinkFilter filter;
	
    /**
     * 
     * @param url 绝对地址形式的 url，例如“http://...”
     */
    public HtmlParser(String url, LinkFilter filter) throws IOException {
        this.url = url;
        this.filter = filter;
        this.doc = Jsoup.connect(this.url).timeout(3000).get();
    }

    /**
     * 
     * @return 返回页面对应的实际内容
     */
    public String getText() {
    	return doc.text();
    }
    
    /**
     * 
     * @return 返回页面的title
     */
    public String getTitle() {
    	return this.doc.title();
    }
	
    /**
     * 获得 url 对应页面中 a 标签和 frame 标签中超链接的绝对地址
     * @return 绝对地址的集合
     */
    public List<String> getLinks() {
        ArrayList<String> result = new ArrayList<String>();
        Elements aLinks = doc.select("a[href]");
        Elements frameLinks = doc.select("frame[src]");
        for (Element aLink : aLinks)
            if (filter == null || filter.accept(aLink.attr("abs:href")))
                result.add(aLink.attr("abs:href"));
        for (Element frameLink : frameLinks)
            if (filter == null || filter.accept(frameLink.attr("abs:src")))
                result.add(frameLink.attr("abs:src"));
        return result;
    }
	

    /**
     * 测试方法
     * @param args
     */
    public static void main(String[] args) {
//        String url = " http://hhuedf.hhu.edu.cn/index.asp";
//        String url = "http://www.hhu.edu.cn";
    	String url = "http://www.facebook.com";
//    	String url = "http://www.baidu.com";
        HtmlParser parser = null;
        try {
            parser = new HtmlParser(url, null);
        } catch (IOException e) {
            e.printStackTrace();

        }
        System.out.println("hello");
        return;
//        System.out.println("链接数量: " + parser.getLinks().size());
//        for (int i = 0; i < parser.getLinks().size(); ++i) {
//        	System.out.println(i + ": " + parser.getLinks().get(i));
//        }
//        System.out.println("title: " + parser.getTitle());
//        System.out.println("text: " + parser.getText());
    }
}
