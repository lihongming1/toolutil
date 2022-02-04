package com.tblh.bms.util;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import lombok.Data;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.io.FileUtils;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.math.BigDecimal;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * 一夜持股法
 * <p>
 * implementation group: 'org.jsoup', name: 'jsoup', version: '1.14.3'
 * implementation group: 'cn.wanghaomiao', name: 'JsoupXpath', version: '2.5.1'
 * implementation group: 'net.sourceforge.htmlunit', name: 'htmlunit', version: '2.33'
 */
public class JsoupGuPiaoTest {

    public static final String[] UA = {"Mozilla/5.0 (Windows NT 6.1; WOW64; rv:46.0) Gecko/20100101 Firefox/46.0",
            "Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/50.0.2661.87 Safari/537.36 OPR/37.0.2178.32",
            "Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/534.57.2 (KHTML, like Gecko) Version/5.1.7 Safari/534.57.2",
            "Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/45.0.2454.101 Safari/537.36",
            "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/46.0.2486.0 Safari/537.36 Edge/13.10586",
            "Mozilla/5.0 (Windows NT 10.0; WOW64; Trident/7.0; rv:11.0) like Gecko",
            "Mozilla/5.0 (compatible; MSIE 10.0; Windows NT 6.1; WOW64; Trident/6.0)",
            "Mozilla/5.0 (compatible; MSIE 9.0; Windows NT 6.1; WOW64; Trident/5.0)",
            "Mozilla/4.0 (compatible; MSIE 8.0; Windows NT 6.1; WOW64; Trident/4.0)",
            "Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/47.0.2526.106 BIDUBrowser/8.3 Safari/537.36",
            "Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/47.0.2526.80 Safari/537.36 Core/1.47.277.400 QQBrowser/9.4.7658.400",
            "Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/48.0.2564.116 UBrowser/5.6.12150.8 Safari/537.36",
            "Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/38.0.2125.122 Safari/537.36 SE 2.X MetaSr 1.0",
            "Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/48.0.2564.116 Safari/537.36 TheWorld 7",
            "Mozilla/5.0 (Windows NT 6.1; W…) Gecko/20100101 Firefox/60.0"};

    public static void main(String[] args) throws Exception {
        // 线程池
        ExecutorService executorService = Executors.newFixedThreadPool(1);
        // 获取雪球cookie
        Map<String, String> cookieMap = getXueQiuCookie();
        // 命中一夜持股法的股票编码
        List<String> hitList = new ArrayList<>();

        String filePath = "/Users/sftc/work/test999/settle-order-adapter/src/main/java/com/tblh/bms/util/gupiao.json";
        JSONArray jsonArray = getAllGuPiaoJSON(filePath, false);
        for (int i = 0; i < jsonArray.size(); i++) {
            JSONObject item = jsonArray.getJSONObject(i);
            // 股票代码
            String gupiaoCode = item.getString("stock_id");
            gupiaoCode = gupiaoCode.toUpperCase();
            if (gupiaoCode.contains("HK")) {
                continue;
            }
            // 股票名称
            String gupiaoName = item.getString("stock_name");
            String finalGupiaoCode = gupiaoCode;
            int finali = i;
            executorService.execute(new Runnable() {
                @Override
                public void run() {
                    try {
                        System.out.println(finali + "\t" + Thread.currentThread().getId() + "\t" + finalGupiaoCode + "\t" + gupiaoName + "\t" + "执行中...");
                        boolean hit = yiyechigu(finalGupiaoCode, cookieMap);
                        if (hit) {
                            hitList.add(finalGupiaoCode);
                        }
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                }
            });
        }

        // 所有任务执行完成且等待队列中也无任务关闭线程池
        executorService.shutdown();
        // 阻塞主线程, 直至线程池关闭
        executorService.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS);

        System.out.println("命中结果: " + JSON.toJSONString(hitList));

    }

    /**
     * 一夜持股法
     * <p>
     * 1.选60开头的股票，涨幅在3%-5%。
     * <p>
     * 2.量比小于1的全部剔除。
     * <p>
     * 3.换手续在5%-10%。
     * <p>
     * 4.流通市值50亿-200亿。
     * <p>
     * 5.看K线，留下成交量持续温和放大的股票，剔除成交量不规律的股票。
     * <p>
     * 6.关注K线，高位长上影线，或者均线空头压制，短期支撑不明显等全部剔除，只关注K线上方没有任何压力的个股，确保次日冲高时大概率事件。
     * <p>
     * K线，在10天线，30天线，60天线，上方压力较小的股票。
     * <p>
     * 7.看分时图，全天股价必须在分时均价上方，且股价必须强于当天的大盘分时图，逆势上涨的股票动力更足。
     * <p>
     * 8.股价在14:30左右创出当天的新高后，回踩均线不破，就是尾盘的最佳进场点。
     *
     * @param gupiaoCode
     * @param cookieMap
     * @return
     * @throws Exception
     */
    public static boolean yiyechigu(String gupiaoCode, Map<String, String> cookieMap) throws Exception {
        XueQiuGuPiaoDetail xueQiuGuPiaoDetail = null;
        try {
            xueQiuGuPiaoDetail = getXueQiuGuPiaoDetail(gupiaoCode, cookieMap);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        if (xueQiuGuPiaoDetail == null) {
            return false;
        }
        boolean result = false;
        // 涨跌幅
        BigDecimal percent = xueQiuGuPiaoDetail.getPercent();
        // 涨幅在3%-5%
        if (percent != null && percent.compareTo(new BigDecimal("3")) >= 0 && percent.compareTo(new BigDecimal("5")) <= 0) {
            // 量比
            BigDecimal volumeRatio = xueQiuGuPiaoDetail.getVolumeRatio();
            // 量比大于等于1
            if (volumeRatio != null && volumeRatio.compareTo(new BigDecimal("1")) >= 0) {
                // 换手率
                BigDecimal turnoverRate = xueQiuGuPiaoDetail.getTurnoverRate();
                // 换手续在5%-10%
                if (turnoverRate != null && turnoverRate.compareTo(new BigDecimal("5")) >= 0 && turnoverRate.compareTo(new BigDecimal("10")) <= 0) {
                    // 流通市值
                    BigDecimal floatMarketCapital = xueQiuGuPiaoDetail.getFloatMarketCapital();
                    // 流通市值50亿-200亿
                    if (floatMarketCapital != null && floatMarketCapital.compareTo(new BigDecimal("5000000000")) >= 0 && floatMarketCapital.compareTo(new BigDecimal("20000000000")) <= 0) {
                        // 分时数据
                        JSONObject fenshiPrice = getXueQiuFenShiPrice(gupiaoCode, cookieMap);
                        JSONArray items = fenshiPrice.getJSONObject("data").getJSONArray("items");
                        // 是否全部价格都在分时均线上方
                        boolean isPrice = true;
                        for (int j = 0; j < items.size(); j++) {
                            JSONObject jsonObject = items.getJSONObject(j);
                            // Long timestamp = jsonObject.getLong("timestamp");
                            // String datetime = convertTimeToString(timestamp);
                            // 当前价格
                            BigDecimal current = jsonObject.getBigDecimal("current");
                            // 分时均价
                            BigDecimal avgPrice = jsonObject.getBigDecimal("avg_price");
                            // 看分时图，全天股价必须在分时均价上方，且股价必须强于当天的大盘分时图，逆势上涨的股票动力更足。
                            if (current.compareTo(avgPrice) < 0) {
                                isPrice = false;
                            }
                        }
                        if (isPrice) {
                            // K线，在10天线，30天线，60天线，上方压力较小的股票。
                            BigDecimal current = xueQiuGuPiaoDetail.getCurrent();
                            XueQiuGuPiaoMa xueQiuGuPiaoMa = getXueQiuGuPiaoMA(gupiaoCode, cookieMap);
                            if (current != null && xueQiuGuPiaoMa != null) {
                                BigDecimal ma5 = xueQiuGuPiaoMa.getMa5();
                                BigDecimal ma10 = xueQiuGuPiaoMa.getMa10();
                                BigDecimal ma20 = xueQiuGuPiaoMa.getMa20();
                                BigDecimal ma30 = xueQiuGuPiaoMa.getMa30();
                                if (current.compareTo(ma5) > 0 && current.compareTo(ma10) > 0 & current.compareTo(ma20) > 0 & current.compareTo(ma30) > 0) {
                                    result = true;
                                }
                            }
                        }
                    }
                }
            }
        }
        return result;
    }

    /**
     * 获取雪球股票均线信息
     *
     * @param gupiaoCode
     * @param cookieMap
     * @return
     * @throws Exception
     */
    public static XueQiuGuPiaoMa getXueQiuGuPiaoMA(String gupiaoCode, Map<String, String> cookieMap) throws Exception {
        LocalDate date = LocalDate.now();
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        String dateStr = dtf.format(date);
        // todo 测试
        dateStr = "2022-01-28";
        LocalDateTime ldt = LocalDate.parse(dateStr, DateTimeFormatter.ISO_DATE).atStartOfDay();
        //当前时间的毫秒数
        long times = ldt.toInstant(ZoneOffset.of("+8")).toEpochMilli();
        String url = "https://stock.xueqiu.com/v5/stock/chart/kline.json?symbol=" + gupiaoCode + "&begin=" + times + "&period=day&indicator=ma";
        Document document = sendHttp(url, cookieMap);
        String body = document.body().text();
        JSONObject detail = JSONObject.parseObject(body);
        JSONArray itemArray = detail.getJSONObject("data").getJSONArray("item");
        if (itemArray != null && itemArray.size() > 0) {
            JSONArray maArray = itemArray.getJSONArray(0);
            if (maArray != null && maArray.size() > 0) {
                BigDecimal ma5 = maArray.getBigDecimal(1);
                BigDecimal ma10 = maArray.getBigDecimal(2);
                BigDecimal ma20 = maArray.getBigDecimal(3);
                BigDecimal ma30 = maArray.getBigDecimal(4);
                XueQiuGuPiaoMa xueQiuGuPiaoMa = new XueQiuGuPiaoMa();
                xueQiuGuPiaoMa.setMa5(ma5);
                xueQiuGuPiaoMa.setMa10(ma10);
                xueQiuGuPiaoMa.setMa20(ma20);
                xueQiuGuPiaoMa.setMa30(ma30);
                return xueQiuGuPiaoMa;
            }
        }
        return null;
    }

    /**
     * 获取雪球股票详情信息
     *
     * @param gupiaoCode
     * @param cookieMap
     * @return
     * @throws Exception
     */
    public static XueQiuGuPiaoDetail getXueQiuGuPiaoDetail(String gupiaoCode, Map<String, String> cookieMap) throws Exception {
        String url = "https://stock.xueqiu.com/v5/stock/quote.json?symbol=" + gupiaoCode + "&extend=detail";
        Document document = sendHttp(url, cookieMap);

        String body = document.body().text();
        JSONObject detail = JSONObject.parseObject(body);
        if (detail == null) {
            return null;
        }
        // 获取股票详情
        JSONObject quote = detail.getJSONObject("data").getJSONObject("quote");
        if (quote == null) {
            return null;
        }
        // 当前股价
        BigDecimal current = quote.getBigDecimal("current");
        // 涨跌幅
        BigDecimal percent = quote.getBigDecimal("percent");
        // 换手率
        BigDecimal turnoverRate = quote.getBigDecimal("turnover_rate");
        // 量比
        BigDecimal volumeRatio = quote.getBigDecimal("volume_ratio");
        // 流通市值
        BigDecimal floatMarketCapital = quote.getBigDecimal("float_market_capital");
        // 科学计算法转普通计算法
        floatMarketCapital = new BigDecimal(floatMarketCapital.toPlainString());
        // 股票详情
        XueQiuGuPiaoDetail xueQiuGuPiaoDetail = new XueQiuGuPiaoDetail();
        xueQiuGuPiaoDetail.setCurrent(current);
        xueQiuGuPiaoDetail.setPercent(percent);
        xueQiuGuPiaoDetail.setTurnoverRate(turnoverRate);
        xueQiuGuPiaoDetail.setVolumeRatio(volumeRatio);
        xueQiuGuPiaoDetail.setFloatMarketCapital(floatMarketCapital);
        return xueQiuGuPiaoDetail;
    }

    /**
     * 获取雪球分时数据
     *
     * @param gupiaoCode
     * @param cookieMap
     * @return
     * @throws Exception
     */
    public static JSONObject getXueQiuFenShiPrice(String gupiaoCode, Map<String, String> cookieMap) throws Exception {
        String url = "https://stock.xueqiu.com/v5/stock/chart/minute.json?symbol=" + gupiaoCode + "&period=1d";
        Document document = sendHttp(url, cookieMap);
        String body = document.body().text();
        JSONObject bodyJsonObject = JSONObject.parseObject(body);
        return bodyJsonObject;
    }

    /**
     * 获取雪球cookie
     *
     * @return
     */
    public static Map<String, String> getXueQiuCookie() throws Exception {
        String url = "https://xueqiu.com/";
        Map<String, String> cookieMap = new HashMap<>();
        Connection connection = Jsoup.connect(url);
        Connection.Response response = connection.method(Connection.Method.GET).ignoreContentType(true).timeout(Integer.MAX_VALUE).execute();
        List<String> cookies = response.headers("Set-Cookie");
        if (CollectionUtils.isNotEmpty(cookies)) {
            for (String cookie : cookies) {
                String[] cookieArray = cookie.split(";");
                String cookieItem = cookieArray[0];
                String[] cookieKeyValue = cookieItem.split("=");
                String cookieKey = cookieKeyValue[0];
                String cookieValue = "";
                if (cookieKeyValue.length > 1) {
                    cookieValue = cookieKeyValue[1];
                }
                cookieMap.put(cookieKey, cookieValue);
            }
        }
        return cookieMap;
    }

    /**
     * 获取全部股票代码JSON
     *
     * @param localFilePath
     * @param isUseCache    是否使用缓存文件
     * @return
     * @throws Exception
     */
    public static JSONArray getAllGuPiaoJSON(String localFilePath, boolean isUseCache) throws Exception {
        File file = new File(localFilePath);
        if (file.exists() && isUseCache) {
            String input = FileUtils.readFileToString(file, "UTF-8");
            JSONArray jsonArray = JSONArray.parseArray(input);
            return jsonArray;
        }
        if (!isUseCache) {
            getAllGuPiaoCode(localFilePath);
            String input = FileUtils.readFileToString(file, "UTF-8");
            JSONArray jsonArray = JSONArray.parseArray(input);
            return jsonArray;
        }
        return null;
    }

    /**
     * 获取全部股票代码
     *
     * @param localFilePath
     * @throws Exception
     */
    public static void getAllGuPiaoCode(String localFilePath) throws Exception {
        System.out.println("获取全部股票代码，开始。。。");
        File file = new File(localFilePath);
        if (file.exists()) {
            file.delete();
        }
        file.createNewFile();
        // 获取全部股票代码
        String url = "https://eniu.com/static/data/stock_list.json";
        Connection connection = Jsoup.connect(url);
        Connection.Response response = connection.method(Connection.Method.GET).ignoreContentType(true).timeout(Integer.MAX_VALUE).execute();
        BufferedInputStream bufferedInputStream = response.bodyStream();
        //一次最多读取1k
        byte[] buffer = new byte[1024];
        //实际读取的长度
        int readLenghth;
        //根据文件保存地址，创建文件输出流
        FileOutputStream fileOutputStream = new FileOutputStream(file);
        //创建的一个写出的缓冲流
        BufferedOutputStream bufferedOutputStream = new BufferedOutputStream(fileOutputStream);
        //文件逐步写入本地
        while ((readLenghth = bufferedInputStream.read(buffer, 0, 1024)) != -1) {//先读出来，保存在buffer数组中
            bufferedOutputStream.write(buffer, 0, readLenghth);//再从buffer中取出来保存到本地
        }
        //关闭缓冲流
        bufferedOutputStream.close();
        fileOutputStream.close();
        bufferedInputStream.close();
        System.out.println("获取全部股票代码，结束。。。");
    }

    /**
     * 将Long类型的时间戳转换成String 类型的时间格式，时间格式为：yyyy-MM-dd HH:mm:ss
     */
    public static String convertTimeToString(Long time) {
        DateTimeFormatter ftf = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        return ftf.format(LocalDateTime.ofInstant(Instant.ofEpochMilli(time), ZoneId.systemDefault()));
    }

    /**
     * 发送http请求
     *
     * @param url
     * @param cookieMap
     * @return
     * @throws Exception
     */
    public static Document sendHttp(String url, Map<String, String> cookieMap) throws Exception {
        Random r = new Random();
        int i = r.nextInt(14);
        String ua = new String(UA[i]);
        Document document = Jsoup.connect(url).
                cookies(cookieMap).
                ignoreContentType(true)
                .ignoreHttpErrors(true)
                .userAgent(ua)
                .get();
        return document;
    }

    /**
     * 雪球股票详情
     */
    @Data
    public static class XueQiuGuPiaoDetail {
        // 当前股价
        private BigDecimal current;
        // 涨跌幅
        private BigDecimal percent;
        // 换手率
        private BigDecimal turnoverRate;
        // 量比
        private BigDecimal volumeRatio;
        // 流通市值
        private BigDecimal floatMarketCapital;
    }

    /**
     * 雪球股票均线
     */
    @Data
    public static class XueQiuGuPiaoMa {
        // 5日均线
        private BigDecimal ma5;
        // 10日均线
        private BigDecimal ma10;
        // 20日均线
        private BigDecimal ma20;
        // 30日均线
        private BigDecimal ma30;
    }

}
