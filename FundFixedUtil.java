package com.tblh.bms.sos.web.settle;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

/**
 * 基金定投，计算扣款金额
 */
public class FundFixedUtil {

    // 日期格式化
    public static SimpleDateFormat SDF = new SimpleDateFormat("yyyy-MM-dd");

    // 当前日期
    public static Date CURRENT_DATE = new Date();

    // 定投基准金额
    public static int AMOUNT = 500;

    // 定投最高金额倍数
    public static int NUM = 10;

    // 通货膨胀率
    public static int RATE = 10;

    // 首次定投实际日期
    public static String FIRST_DAY = "2021-06-01";

    // 已定投基金份额最新市值
    public static double LATEST_AMOUNT = 500 + 555.55 + 875.55;

    static {
        try {
            // 当前日期
            CURRENT_DATE = SDF.parse("2021-06-17");
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public static void main(String[] args) {
        FundFixedUtil test = new FundFixedUtil();
        test.fund(AMOUNT, NUM, RATE, FIRST_DAY, LATEST_AMOUNT);
    }

    /**
     * @param amount   定投基准金额
     * @param num      定投最高金额倍数
     * @param rate     通货膨胀率
     * @param firstDay 首次定投实际日期
     * @param latest   已定投基金份额最新市值
     */
    public void fund(int amount, int num, int rate, String firstDay, double latest) {
        try {
            if (firstDay.equals(SDF.format(CURRENT_DATE))) {
                System.out.println("首次扣款金额：" + amount);
                return;
            }
            // 定投基准金额
            BigDecimal amountBig = new BigDecimal(amount);
            // 通货膨胀率
            BigDecimal rateBig = new BigDecimal(rate);
            // 已定投基金份额最新市值
            BigDecimal latestBig = new BigDecimal(latest);
            // 今天是否是周四
            String week = getWeek();
            if (!"周四".equals(week)) {
                System.out.println("今天非扣款日");
                return;
            }
            // 扣款金额
            BigDecimal deduct = deduct(amountBig, num, rateBig, firstDay, latestBig);
            System.out.println("扣款金额：" + deduct);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    /**
     * 扣款金额 - T日实际扣款金额=定投基准金额 * POW((1+通货膨胀率/365), (当前日期-首次定投实际日期)) * 定投期数 - 已定投基金份额最新市值；
     *
     * @param amount   定投基准金额
     * @param num      定投最高金额倍数
     * @param rate     通货膨胀率
     * @param firstDay 首次定投实际日期
     * @param latest   已定投基金份额最新市值
     * @return
     * @throws Exception
     */
    public BigDecimal deduct(BigDecimal amount, int num, BigDecimal rate, String firstDay, BigDecimal latest) throws Exception {
        BigDecimal a = new BigDecimal("1");
        BigDecimal b = rate.divide(new BigDecimal("365"), 10, BigDecimal.ROUND_HALF_UP);
        BigDecimal c = a.add(b);
        // 当前日期 - 首次定投实际日期
        int diff = diffDay(firstDay);
        BigDecimal d = c.pow(diff);
        // 日期 转 期数
        int nper = nper(firstDay);
        BigDecimal value = amount.multiply(d).multiply(new BigDecimal(nper)).subtract(latest);
        BigDecimal max = amount.multiply(new BigDecimal(num));
        if (value.compareTo(max) < 0) {
            BigDecimal result = value.setScale(2, BigDecimal.ROUND_HALF_UP);
            return result;
        } else {
            return max;
        }
    }

    /**
     * 日期转期数
     *
     * @param firstDay
     * @return
     * @throws Exception
     */
    public int nper(String firstDay) throws Exception {
        int diff = diffDay(firstDay);
        int tmp = (diff / 7) == 0 ? 1 : (diff / 7) + 1;
        int value = tmp + 1;
        return value;
    }

    /**
     * 日期差
     *
     * @param firstDay
     * @return
     * @throws Exception
     */
    public int diffDay(String firstDay) throws Exception {
        String currentDay = SDF.format(CURRENT_DATE);
        Date firstDate = SDF.parse(firstDay);
        Date currentDate = SDF.parse(currentDay);
        long diff = currentDate.getTime() - firstDate.getTime();
        long days = diff / (1000 * 60 * 60 * 24);
        Long days2 = new Long(days);
        return days2.intValue();
    }

    /**
     * 今天是周几
     *
     * @return
     */
    private String getWeek() {
        String week = "";
        Date today = CURRENT_DATE;
        Calendar c = Calendar.getInstance();
        c.setTime(today);
        int weekday = c.get(Calendar.DAY_OF_WEEK);
        if (weekday == 1) {
            week = "周日";
        } else if (weekday == 2) {
            week = "周一";
        } else if (weekday == 3) {
            week = "周二";
        } else if (weekday == 4) {
            week = "周三";
        } else if (weekday == 5) {
            week = "周四";
        } else if (weekday == 6) {
            week = "周五";
        } else if (weekday == 7) {
            week = "周六";
        }
        return week;
    }

}
