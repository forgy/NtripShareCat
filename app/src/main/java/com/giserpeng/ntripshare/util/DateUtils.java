package com.giserpeng.ntripshare.util;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class DateUtils {
    public static String getFirstDayOfMonth(int year, int month) {
        Calendar cal = Calendar.getInstance();
        // 设置年份
        cal.set(Calendar.YEAR, year);
        // 设置月份
        cal.set(Calendar.MONTH, month - 1);
        // 设置日历中月份的第1天
        cal.set(Calendar.DAY_OF_MONTH, 1);
        // 格式化日期
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        String firstDayOfMonth = sdf.format(cal.getTime());
        return firstDayOfMonth;
    }

    public static String getLastDayOfMonth(int year, int month) {
        Calendar cal = Calendar.getInstance();
        // 设置年份
        cal.set(Calendar.YEAR, year);
        // 设置月份
        cal.set(Calendar.MONTH, month);
        // 设置日历中月份的最后1天
        cal.set(Calendar.DATE, 0);
        // 格式化日期
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        String lastDayOfMonth = sdf.format(cal.getTime());
        return lastDayOfMonth;
    }

    public static String getFirstDayOfYear(int year) {
        Calendar cal = Calendar.getInstance();
        // 设置年份
        cal.set(Calendar.YEAR, year);
        // 设置月份
        cal.set(Calendar.MONTH, 0);
        // 设置日历中月份的第1天
        cal.set(Calendar.DAY_OF_MONTH, 1);
        // 格式化日期
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        String firstDayOfYear = sdf.format(cal.getTime());
        return firstDayOfYear;
    }

    public static String getLastDayOfYear(int year) {
        Calendar cal = Calendar.getInstance();
        // 设置年份
        cal.set(Calendar.YEAR, year);
        // 设置月份
        cal.set(Calendar.MONTH, 11);
        // 设置日历中月份的最后1天
        cal.set(Calendar.DATE, 0);
        // 格式化日期
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        String lastDayOfYear = sdf.format(cal.getTime());
        return lastDayOfYear;
    }

    /**
     * 获取当前月第一天
     * @return
     */
    public static String firstDayOfCurrentMonth(){
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        Calendar cal=Calendar.getInstance();//获取当前日期
        cal.add(Calendar.MONTH, 0);
        cal.set(Calendar.DAY_OF_MONTH,1);//设置为1号,当前日期既为本月第一天
        return sdf.format(cal.getTime());
    }

    /**
     * 获取当前月最后一天
     * @return
     */
    public static String lastDayOfCurrentMonth(){
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        Calendar cal = Calendar.getInstance();//获取当前日期
        cal.set(Calendar.DAY_OF_MONTH, cal.getActualMaximum(Calendar.DAY_OF_MONTH));
        return sdf.format(cal.getTime());
    }



    public static Date getNextDay(Date date) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        calendar.add(Calendar.DAY_OF_MONTH, +1);//+1今天的时间加一天
        date = calendar.getTime();
        return date;
    }

    /**
     * 获取日期的月份
     * @param date
     * @return
     */
    public static String getMonth(Date date){
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        int month = cal.get(Calendar.MONTH) + 1;
        if(month < 10){
            return "0"+month;
        } else {
            return String.valueOf(month);
        }
    }

    /**
     * 使用用户格式格式化日期
     *
     * @param date 日期
     * @param pattern 日期格式
     * @return
     */
    public static String format(Date date, String pattern) {
        String returnValue = "";
        if (date != null) {
            SimpleDateFormat df = new SimpleDateFormat(pattern);
            returnValue = df.format(date);
        }
        return (returnValue);
    }

    /**
     * 比较两个日期大小
     * @param DATE1
     * @param DATE2
     * @param format 格式 yyyy-MM-dd,yyyy-MM-dd hh:mm:ss
     * @return
     */
    public static int compareDate(String DATE1, String DATE2, String format) {
        DateFormat df = new SimpleDateFormat(format);
        try {
            Date dt1 = df.parse(DATE1);
            Date dt2 = df.parse(DATE2);
            if (dt1.getTime() > dt2.getTime()) {
                return 1;
            } else if (dt1.getTime() < dt2.getTime()) {
                return -1;
            } else {
                return 0;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return 0;
    }

    public static void main(String[] args) {
        System.out.println(firstDayOfCurrentMonth());
        System.out.println(lastDayOfCurrentMonth());
        String camStartDate = "2016-11-11";
        String camEndDate = "2019-11-11";
        List<String> years = new ArrayList<>();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        try {
            Date d1 = sdf.parse(camStartDate);
            Date d2 = sdf.parse(camEndDate);
            Calendar c = Calendar.getInstance();
            c.setTime(d1);
            int year1 = c.get(Calendar.YEAR);
            c.setTime(d2);
            int year2 = c.get(Calendar.YEAR);
            do {
                if(year1 >= 2017){
                    years.add(year1 + "");
                }
                year1++;
            } while (year2 >= year1);

        } catch (ParseException e) {
            e.printStackTrace();
        }
        System.out.println(years);
    }

    /**
     * 传入月份，将period转换为MM的格式
     * @param period
     * @return
     */
    public static String getMonthTwoPlace(String period){
        if(period.length() == 2){
            return period;
        }

        if(period.length() == 1){
            return "0" + period;
        }
        return null;
    }

    /**
     * 将日期字符串转化为Date类型
     * @param dateStr
     * @param pattern
     * @return
     */
    public static Date StringToDate(String dateStr, String pattern) {
        try {
            DateFormat sdf = new SimpleDateFormat(pattern);
            Date date = sdf.parse(dateStr);
            return date;
        } catch (Exception ex) {
            return null;
        }
    }

    /**
     * 将日期转化为字符串类型
     * @param date
     * @param pattern
     * @return
     */
    public static String dateToString(Date date, String pattern) {
        DateFormat sdf = new SimpleDateFormat(pattern);
        String dateStr = sdf.format(date);
        return dateStr;
    }
}