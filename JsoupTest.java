 implementation group: 'org.jsoup', name: 'jsoup', version: '1.14.3'
 implementation group: 'cn.wanghaomiao', name: 'JsoupXpath', version: '2.5.1'
   
     public static void main(String[] args) throws Exception {
        File file = new File("/Users/sftc/Downloads/test.txt");
        FileWriter fileWriter = new FileWriter(file, true);
        for (int i = 129; i <= 1000; i++) {
            String contents = Jsoup.connect("http://www.aibj.pro/forum-137-" + i + ".html").get().html();
            JXDocument jxd = new JXDocument(contents);
            List<JXNode> list = jxd.selN("//*[contains(@id, 'normalthread_')]/tr/th/a[3]");
            if (CollectionUtils.isNotEmpty(list)) {
                for (JXNode node : list) {
                    String area = null;
                    String httpUrl = null;
                    String text = null;
                    try {
                        area = node.getElement().parentNode().childNode(5).childNode(1).childNode(0).toString();
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                    try {
                        httpUrl = "http://www.aibj.pro/" + node.getElement().attr("href");
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                    try {
                        text = node.getElement().text();
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
