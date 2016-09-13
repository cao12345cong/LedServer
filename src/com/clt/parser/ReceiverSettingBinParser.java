package com.clt.parser;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

import com.clt.entity.ReceiverSettingInfo;

/**
 * 解析bin文件
 *
 */
public class ReceiverSettingBinParser
{

    public static byte [] getAllByteFromBin(File file) throws IOException
    {

        FileInputStream fis = null;
        ByteArrayOutputStream baos = null;
        byte [] buffer = null;
        try
        {

            fis = new FileInputStream(file);
            baos = new ByteArrayOutputStream();
            int i = 0;
            while ((i = fis.read()) != -1)
            {
                baos.write(i);
            }
            buffer = baos.toByteArray();

        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        finally
        {
            if (fis != null)
            {
                fis.close();
            }
            if (baos != null)
            {
                baos.close();
            }
        }
        return buffer;

    }

    /**
     * 从bin文件解析前256个字节
     * @param fileName
     * @return
     * @throws IOException 
     */
    public static byte [] getByteFromBin(File file) throws IOException
    {

        FileInputStream fis = null;
        ByteArrayOutputStream baos = null;
        byte [] buffer = null;
        try
        {

            fis = new FileInputStream(file);
            baos = new ByteArrayOutputStream();
            int i = 0;
            while ((i = fis.read()) != -1)
            {
                baos.write(i);
                if (baos.size() >= 256)
                {
                    break;
                }
            }
            buffer = baos.toByteArray();

        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        finally
        {
            if (fis != null)
            {
                fis.close();
            }
            if (baos != null)
            {
                baos.close();
            }
        }
        return buffer;

    }

    /**
     * 解析
     * @param fileName
     * @return
     * @throws IOException 
     */
    public static byte [] getByteFromBin(File file, int num) throws IOException
    {

        FileInputStream fis = null;
        ByteArrayOutputStream baos = null;
        byte [] buffer = null;
        try
        {

            fis = new FileInputStream(file);
            baos = new ByteArrayOutputStream();
            int i = 0;
            int byteNum = 0;
            while ((i = fis.read()) != -1)
            {
                baos.write(i);
                byteNum = byteNum + i;
                if (byteNum >= num)
                {
                    break;
                }
            }
            buffer = baos.toByteArray();

        }
        catch (Exception e)
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        finally
        {
            if (fis != null)
            {
                fis.close();
            }
            if (baos != null)
            {
                baos.close();
            }
        }
        return buffer;

    }

    /**
     * 解析文件
     * @param file
     * @return
     * @throws IOException
     */
    public static ReceiverSettingInfo parser(File file) throws IOException
    {
        ReceiverSettingInfo receiverSettingInfo = new ReceiverSettingInfo();
        receiverSettingInfo.setFileName(file.getName());
        byte [] buffer = getByteFromBin(file);
        initParams(buffer, receiverSettingInfo);
        return receiverSettingInfo;
    }

    /**
     * 解析文件256个字节
     * @param file
     * @return
     * @throws IOException
     */
    public static ReceiverSettingInfo parser256byte(File file)
            throws IOException
    {
        ReceiverSettingInfo receiverSettingInfo = new ReceiverSettingInfo();
        receiverSettingInfo.setFileName(file.getName());
        byte [] buffer = getByteFromBin(file, 256);
        initParams(buffer, receiverSettingInfo);
        return receiverSettingInfo;
    }

    /**
     * 为ReceiverSettingInfo的参数赋值
     * @param buffer
     * @param receiverSettingInfo
     */
    private static void initParams(byte [] buffer,
            ReceiverSettingInfo receiverSettingInfo)
    {
        // 刷新率
        receiverSettingInfo.setRefreshRate(buffer[7] & 0xff);
        // 灰度等级
        receiverSettingInfo.setGrayLevel(buffer[8] & 0xff);
        // 串钟
        receiverSettingInfo.setGrayLevel(buffer[9] & 0xff);
        // gamma值
        String gamma = (buffer[1] >> 4) + "." + (buffer[1] & 0x0f);
        receiverSettingInfo.setGammaValue(gamma);
        // 亮度有效率

        // 刷新倍数

        // 灰度模式

        // 消隐时间
        receiverSettingInfo.setBlankingValue(String
                .valueOf((buffer[16] & 0xff) << 8 + buffer[17] & 0xff));
        // 亮度等级
        receiverSettingInfo.setBrightnessLevel(String
                .valueOf((buffer[14] & 0xff) << 8 + buffer[15] & 0xff));
        // 最小OE
    }
}
