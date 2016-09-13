package com.clt.util;

import java.math.BigDecimal;

/**
 * BCD
 *
 */
public class CommonUtil
{
    /**
     * @功能: BCD码转为10进制串(阿拉伯数据)
     * @参数: BCD码
     * @结果: 10进制串
     */
    public static String bcd2Str(byte [] bytes)
    {
        StringBuffer temp = new StringBuffer(bytes.length * 2);
        for (int i = 0; i < bytes.length; i++)
        {
            temp.append((byte) ((bytes[i] & 0xf0) >>> 4));
            temp.append((byte) (bytes[i] & 0x0f));
        }
        return temp.toString().substring(0, 1).equalsIgnoreCase("0") ? temp
                .toString().substring(1) : temp.toString();
    }

    /**
     * @功能: BCD码转为10进制串(阿拉伯数据)
     * @参数: BCD码
     * @结果: 10进制串
     */
    public static String bcd2Str(byte value)
    {
        StringBuffer temp = new StringBuffer(2);
        temp.append((byte) ((value & 0xf0) >>> 4));
        temp.append((byte) (value & 0x0f));
        return temp.toString().substring(0, 1).equalsIgnoreCase("0") ? temp
                .toString().substring(1) : temp.toString();
    }

    /**
     * @功能: 10进制串转为BCD码
     * @参数: 10进制串
     * @结果: BCD码
     */
    public static byte [] str2Bcd(String asc)
    {
        int len = asc.length();
        int mod = len % 2;
        if (mod != 0)
        {
            asc = "0" + asc;
            len = asc.length();
        }
        byte abt[] = new byte [len];
        if (len >= 2)
        {
            len = len / 2;
        }
        byte bbt[] = new byte [len];
        abt = asc.getBytes();
        int j, k;
        for (int p = 0; p < asc.length() / 2; p++)
        {
            if ((abt[2 * p] >= '0') && (abt[2 * p] <= '9'))
            {
                j = abt[2 * p] - '0';
            }
            else if ((abt[2 * p] >= 'a') && (abt[2 * p] <= 'z'))
            {
                j = abt[2 * p] - 'a' + 0x0a;
            }
            else
            {
                j = abt[2 * p] - 'A' + 0x0a;
            }
            if ((abt[2 * p + 1] >= '0') && (abt[2 * p + 1] <= '9'))
            {
                k = abt[2 * p + 1] - '0';
            }
            else if ((abt[2 * p + 1] >= 'a') && (abt[2 * p + 1] <= 'z'))
            {
                k = abt[2 * p + 1] - 'a' + 0x0a;
            }
            else
            {
                k = abt[2 * p + 1] - 'A' + 0x0a;
            }
            int a = (j << 4) + k;
            byte b = (byte) a;
            bbt[p] = b;
        }
        return bbt;
    }

    /**
     * 四舍五入取整
     */
    public static int getRounding(float f)
    {
        BigDecimal a = new BigDecimal(f).setScale(0, BigDecimal.ROUND_HALF_UP);
        return a.intValue();
    }

    public static int double2Round(double d)
    {
        BigDecimal a = new BigDecimal(d).setScale(0, BigDecimal.ROUND_HALF_UP);
        return a.intValue();
    }

    /** 
    * 将指定字符串src，以每两个字符分割转换为16进制形式 如："2B44EFD9" --> byte[]{0x2B, 0x44, 0xEF, 0xD9} 
    * @param src String 
    * @return byte[] 
    */
    public static byte [] hexString2Bytes(int bufferLength, String src)
    {
        byte [] ret = new byte [bufferLength];
        byte [] tmp = src.getBytes();
        int len = src.length() / 2;
        for (int i = 0; i < len; i++)
        {
            ret[i] = uniteBytes(tmp[i * 2], tmp[i * 2 + 1]);
        }
        return ret;
    }

    public static byte uniteBytes(byte src0, byte src1)
    {
        byte _b0 = Byte.decode("0x" + new String(new byte []
            {
                src0
            })).byteValue();
        _b0 = (byte) (_b0 << 4);
        byte _b1 = Byte.decode("0x" + new String(new byte []
            {
                src1
            })).byteValue();
        byte ret = (byte) (_b0 ^ _b1);
        return ret;
    }

}
