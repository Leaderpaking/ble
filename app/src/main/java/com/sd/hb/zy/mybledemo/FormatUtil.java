package com.sd.hb.zy.mybledemo;

import android.text.TextUtils;

import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;

public class FormatUtil {

    /**
     * 格式化Double数据
     *
     * @param inputNumber
     * @param num
     */
    public static String formatDoubleNumber(double inputNumber, int num) {
        try {
            BigDecimal bd = new BigDecimal(inputNumber);
            bd = bd.setScale(num, BigDecimal.ROUND_HALF_UP);
            return bd.toString();
        } catch (Exception e) {
            e.printStackTrace();
            return "0";
        }
    }

    /**
     * 去掉字符串特殊字符
     *
     * @param str
     * @return
     */
    public static String replaceSpecialCharacter(String str) {
        if (TextUtils.isEmpty(str)) {
            return "";
        }
        str = str.replace("&lt;", "<");
        str = str.replace("    ", "\t");
        str = str.replace("&nbsp;", " ");
        str = str.replace("&acute;", "'");
        str = str.replace("&quot;", "\"");
        str = str.replace("&amp;", "&");
        str = str.replace("<br>", "\r\n");
        str = str.replace("&#40;", "(");
        str = str.replace("&#41;", ")");
        str = str.replace("&#42;", "*");
        str = str.replace("&#43;", "+");
        str = str.replace("&#44;", ",");
        str = str.replace("&#45;", "-");
        str = str.replace("&#46;", ".");
        str = str.replace("&#47;", "/");
        str = str.replace("&#48;", "0");
        return str;
    }

    /**
     * byte[]转字符串
     *
     * @param bArray
     * @return
     */
    public static String bytesToString(byte[] bArray, String charsetName) {
        String hexStr = bytesToHexString(bArray);
        return hexStrToString(hexStr, charsetName);
    }

    /**
     * 十六进制字符串转化字符串
     *
     * @param hexStr
     * @return
     */
    public static String hexStrToString(String hexStr, String charsetName) {
        String returnStr = null;
        try {
            String str = "0123456789ABCDEF";
            char[] hexs = hexStr.toCharArray();
            byte[] bytes = new byte[hexStr.length() / 2];
            int n;
            for (int i = 0; i < bytes.length; i++) {
                n = str.indexOf(hexs[2 * i]) * 16;
                n += str.indexOf(hexs[2 * i + 1]);
                bytes[i] = (byte) (n & 0xff);
            }
            returnStr = new String(bytes, charsetName);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return returnStr;
    }

    /**
     * 字符串转换成十六进制字符串
     *
     * @param str
     */
    public static String strToHexStr(String str) {
        char[] chars = "0123456789ABCDEF".toCharArray();
        StringBuilder sb = new StringBuilder("");
        byte[] bs = str.getBytes();
        int bit;
        for (int i = 0; i < bs.length; i++) {
            bit = (bs[i] & 0x0f0) >> 4;
            sb.append(chars[bit]);
            bit = bs[i] & 0x0f;
            sb.append(chars[bit]);
        }
        return sb.toString().trim();
    }

    /**
     * 字符串转换成十六进制字符串
     *
     * @param str
     * @param charsetName
     * @return
     */
    public static String strToHexStr(String str, String charsetName) {
        char[] chars = "0123456789ABCDEF".toCharArray();
        StringBuilder sb = new StringBuilder("");
        byte[] bs = new byte[0];
        try {
            bs = str.getBytes(charsetName);
            int bit;
            for (int i = 0; i < bs.length; i++) {
                bit = (bs[i] & 0x0f0) >> 4;
                sb.append(chars[bit]);
                bit = bs[i] & 0x0f;
                sb.append(chars[bit]);
            }
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return sb.toString().trim();
    }

    /**
     * byte[]转换为16进制字符串
     *
     * @param bArray
     * @return
     */
    public static String bytesToHexString(byte[] bArray) {
        StringBuffer sb = new StringBuffer(bArray.length);
        String sTemp;
        for (int i = 0; i < bArray.length; i++) {
            sTemp = Integer.toHexString(0xFF & bArray[i]);
            if (sTemp.length() < 2)
                sb.append(0);
            sb.append(sTemp.toUpperCase());
        }
        return sb.toString();
    }

    /**
     * Hex转换为Bytes
     *
     * @param hex
     * @return
     */
    public static byte[] hexStringToBytes(String hex) {
        byte[] b = new byte[hex.length() / 2];
        for (int i = 0; i < b.length; i++) {
            int index = i * 2;
            int v = Integer.parseInt(hex.substring(index, index + 2), 16);
            b[i] = (byte) v;
        }
        return b;
    }

    /**
     * 字符串补齐
     *
     * @param source     源字符串
     * @param fillLength 补齐长度
     * @param fillChar   补齐的字符
     * @param isLeftFill true为左补齐，false为右补齐
     * @return
     */
    public static String stringFill(String source, int fillLength, char fillChar, boolean isLeftFill) {
        try {
            int sourceLen = source.getBytes("GBK").length;
            if (source == null || sourceLen >= fillLength) return source;
            StringBuilder result = new StringBuilder(fillLength);
            int len = fillLength - sourceLen;
            if (isLeftFill) {
                for (; len > 0; len--) {
                    result.append(fillChar);
                }
                result.append(source);
            } else {

                result.append(source);
                for (; len > 0; len--) {
                    result.append(fillChar);
                }
            }
            return result.toString();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }


}