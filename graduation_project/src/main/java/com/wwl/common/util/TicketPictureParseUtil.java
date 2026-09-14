package com.wwl.common.util;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 电影票图片OCR文本解析工具类
 * 提取影院、影片名、场次时间、座位、价格等信息
 */
public class TicketPictureParseUtil {

    /**
     * 提取影院名称
     */
    public static String getCinema(String text) {
        String regex = ".*影院|.*影城|.*购物中心";
        Matcher matcher = Pattern.compile(regex).matcher(text);
        if (matcher.find()) {
            return matcher.group().trim();
        }
        return "";
    }

    /**
     * 提取电影名称
     */
    public static String getMovieName(String text) {
        String[] lines = text.split("\n");
        for (String line : lines) {
            if (line.length() > 2 && !line.contains("号") && !line.contains("厅")
                    && !line.contains("座") && !line.contains("¥")
                    && !line.contains("时间") && !line.contains("票价")) {
                return line.trim();
            }
        }
        return "";
    }

    /**
     * 提取观影日期
     */
    public static String getStartDate(String text) {
        String regex = "\\d{4}[-/]\\d{1,2}[-/]\\d{1,2}";
        Matcher matcher = Pattern.compile(regex).matcher(text);
        if (matcher.find()) {
            return matcher.group();
        }
        return "";
    }

    /**
     * 提取开场时间
     */
    public static String getStartTime(String text) {
        String regex = "\\d{1,2}:\\d{2}";
        Matcher matcher = Pattern.compile(regex).matcher(text);
        List<String> timeList = new ArrayList<>();
        while (matcher.find()) {
            timeList.add(matcher.group());
        }
        if (timeList.size() >= 1) {
            return timeList.get(0);
        }
        return "";
    }

    /**
     * 提取散场时间
     */
    public static String getEndTime(String text) {
        String regex = "\\d{1,2}:\\d{2}";
        Matcher matcher = Pattern.compile(regex).matcher(text);
        List<String> timeList = new ArrayList<>();
        while (matcher.find()) {
            timeList.add(matcher.group());
        }
        if (timeList.size() >= 2) {
            return timeList.get(1);
        }
        return "";
    }

    /**
     * 提取座位集合
     */
    public static List<String> getSeatList(String text) {
        List<String> seatList = new ArrayList<>();
        String regex = "\\d+排\\d+号|\\d+排\\d+座";
        Matcher matcher = Pattern.compile(regex).matcher(text);
        while (matcher.find()) {
            seatList.add(matcher.group());
        }
        return seatList;
    }

    /**
     * 提取单张票价
     */
    public static String getSeatPrice(String text) {
        String regex = "¥\\d+(\\.\\d+)?";
        Matcher matcher = Pattern.compile(regex).matcher(text);
        List<String> priceList = new ArrayList<>();
        while (matcher.find()) {
            priceList.add(matcher.group());
        }
        if (!priceList.isEmpty()) {
            return priceList.get(0);
        }
        return "0";
    }

    /**
     * 提取订单总价
     */
    public static String getTotalPrice(String text) {
        String regex = "合计[:：]?¥?\\d+(\\.\\d+)?";
        Matcher matcher = Pattern.compile(regex).matcher(text);
        if (matcher.find()) {
            String res = matcher.group().replaceAll("合计[:：]?", "");
            return res;
        }
        return getSeatPrice(text);
    }
}
