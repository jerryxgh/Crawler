package com.jerry;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.Node;
import org.jsoup.select.Elements;

public class HtmlParser {
	private String url;
	Document doc;
	LinkFilter filter;
	
	/**
	 * 
	 * @param url 需要处理的绝对地址形式的 url
	 */
	public HtmlParser(String url, LinkFilter filter) throws IOException {
		this.url = url;
		this.filter = filter;
		this.doc = Jsoup.connect(this.url).timeout(3000).get();
	}
	/**
	 * 获得 url 对应的页面中的所有代码
	 * @return
	 */
	public List<String> getCodes() {
		List<String> result = new ArrayList<String>();
		Elements codes = doc.getElementsByTag("code");
		for (Element code : codes)
			result.add(getCode(code));
		
		return result;
	}
	
	/**
	 * 获得节点 node 中的代码，与默认的 node.text() 方法的唯一区别是把 br 标签
	 * 换成了 Java 字符串中的换行符 \n
	 * @param node
	 * @return
	 */
	public String getCode(Node node) {
		StringBuffer result = new StringBuffer();
		if (node.nodeName().equals("#text"))
			result.append(Jsoup.parse(node.toString()).text());
		else if (node.nodeName().equals("br"))
			result.append("\n");
		else {
			List<Node> nodes = node.childNodes();
			for (Node subNode : nodes) {
				result.append(getCode(subNode));
			}
		}
		return result.toString();
	}
	
	/**
	 * 获得 url 对应页面的代码标题
	 * @return
	 */
	public String getCodeTitle() {
		Element e = doc.getElementsByClass("codeTitle").first();
		String result = null;
		if (e != null)
			result = e.text();
		return result;
	}
	
	/**
	 * 获得 url 对应页面中 a 标签和 frame 标签中超链接的绝对地址
	 * @return 绝对地址的集合
	 */
	public Set<String> getLinks() {
		Set<String> result = new HashSet<String>();
		Elements aLinks = doc.select("a[href]");
		Elements frameLinks = doc.select("frame[src]");
		for (Element aLink : aLinks)
			if (filter != null && filter.accept(aLink.attr("abs:href")))
				result.add(aLink.attr("abs:href"));
		for (Element frameLink : frameLinks)
			if (filter != null && filter.accept(frameLink.attr("abs:src")))
			result.add(frameLink.attr("abs:src"));
		return result;
	}
	

	/**
	 * 测试方法
	 * @param args
	 */
	public static void main(String[] args) {
		String url = "http://www.java2s.com/Code/Java/Spring/SpringTracingAspect.htm";
		HtmlParser parser = null;
		try {
			parser = new HtmlParser(url, null);
		} catch (IOException e) {
			e.printStackTrace();
		}
		System.out.println(parser.getCodeTitle());
//		System.out.println(parser.getCodes());
		System.out.println(parser.getLinks());
	}
}
