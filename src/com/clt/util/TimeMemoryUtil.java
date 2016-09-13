package com.clt.util;

/**
 * 计算一段程序使用了多少时间和内存的工具类
 * @author caocong
 * 2013.10.28
 *
 */
public class TimeMemoryUtil
{
    static long startTime = 0;// 起始时间戳

    static long startFreeMemory = 0;
    
    /**
     * 在要计算的某段程序开始位置调用
     */
    public static void getStartTimeMemory()
    {
        startTime = System.currentTimeMillis();
        startFreeMemory = Runtime.getRuntime().freeMemory();
    }

    /**
     *  在要计算的某段程序结束位置调用
     *  @param methodName 方法名
     */
    public static void showTimeMemory(String methodName)
    {
        long endTime = System.currentTimeMillis();
        long endFreeMemory = Runtime.getRuntime().freeMemory();
        LogUtil.i(methodName, "time=" + (endTime - startTime));
        //LogUtil.i(methodName, "memory=" + (startFreeMemory - endFreeMemory));
    }
    
    

}
