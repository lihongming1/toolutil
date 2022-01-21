package com.tblh.bms.util;

import org.apache.commons.collections.CollectionUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.seimicrawler.xpath.JXNode;

import org.seimicrawler.xpath.JXDocument;

import java.io.File;
import java.io.FileWriter;
import java.util.List;

/**
 * 测试爬虫
 * <p>
 * implementation group: 'org.jsoup', name: 'jsoup', version: '1.14.3'
 * implementation group: 'cn.wanghaomiao', name: 'JsoupXpath', version: '2.5.1'
 */
public class JsoupTest {

    public static void main(String[] args) throws Exception {
        File file = new File("/Users/sftc/Downloads/test2.txt");
        if (file.exists()) {
            file.delete();
        }
        file.createNewFile();
        FileWriter fileWriter = new FileWriter(file, true);
        for (int i = 1; i <= 2; i++) {
            Document document = Jsoup.connect("http://www.aibj.pro/forum-137-" + i + ".html").get();
            JXDocument jxd = JXDocument.create(document);
            List<JXNode> list = jxd.selN("//*[contains(@id, 'normalthread_')]/tr/th/a[3]");
            if (CollectionUtils.isNotEmpty(list)) {
                for (JXNode jxNode : list) {
                    String area = null;
                    String httpUrl = null;
                    String text = null;
                    try {
                        area = jxNode.asElement().parentNode().childNode(5).childNode(1).childNode(0).toString();
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                    try {
                        httpUrl = "http://www.aibj.pro/" + jxNode.asElement().attr("href");
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                    try {
                        text = jxNode.asElement().text();
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                    String context = "第" + i + "页 " + area + " , " + text + " , " + httpUrl;
                    System.out.println(context);
                    fileWriter.write(context + "\n");
                    fileWriter.flush();
                }
            }
        }
        fileWriter.close();
    }

}
