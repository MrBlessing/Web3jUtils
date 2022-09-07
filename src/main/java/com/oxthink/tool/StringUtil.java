package com.oxthink.tool;

import java.math.BigDecimal;
import java.math.RoundingMode;

public class StringUtil {
    /**
     * 判断字符串是否为空，排除空白字符的干扰
     *
     * @param string 字符串
     * @return 字符串为空返回true
     */
    public static boolean isEmpty(String string) {
        return StringUtil.isEmpty(string);
    }

    /**
     * 比较两个数字大小
     *
     * @param a a
     * @param b b
     * @return 0表示a=b 1表示a大于b -1表示a小于b
     */
    public static int compareTo(String a, String b) {
        return new BigDecimal(a).compareTo(new BigDecimal(b));
    }

    /**
     * 比较两个数字大小
     *
     * @param a a
     * @param b b
     * @return a是否大于b
     */
    public static boolean greatThan(String a, String b) {
        return compareTo(a, b) == 1;
    }

    /**
     * 比较两个数字大小
     *
     * @param a a
     * @param b b
     * @return a是否小于b
     */
    public static boolean lessThan(String a, String b) {
        return compareTo(a, b) == -1;
    }

    /**
     * 截取小数点
     *
     * @param decimal 小数
     * @param num     保留小数位数
     * @return 保留num位小数
     */
    public static String interceptDecimalPoint(String decimal, int num) {
        return new BigDecimal(decimal).setScale(num, RoundingMode.HALF_UP).toString();
    }
}
