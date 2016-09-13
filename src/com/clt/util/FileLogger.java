package com.clt.util;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.Date;

import android.os.Environment;

/**
 * 错误日志
 * @author Administrator
 */
public class FileLogger
{
    private static String filePath = Environment.getExternalStorageDirectory()
            .getAbsolutePath() + "/";

    private static String fileName = "filelog.txt";

    private static String byteFileName = "byte.txt";

    private boolean DEBUG =false;

    private static FileLogger instance;

    private File file,bytefile;

    private FileLogger()
    {
        file = new File(filePath, fileName);
        // 如果父目录不存，则创建该目录
        if (!file.getParentFile().exists())
        {
            file.getParentFile().mkdirs();
        }
        if (!file.exists())
        {
            try
            {
                file.createNewFile();
            }
            catch (IOException e)
            {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }

        bytefile = new File(filePath, byteFileName);
        // 如果父目录不存，则创建该目录
        if (!bytefile.getParentFile().exists())
        {
            bytefile.getParentFile().mkdirs();
        }
        if (!bytefile.exists())
        {
            try
            {
                bytefile.createNewFile();
            }
            catch (IOException e)
            {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
    }

    public static FileLogger getInstance()
    {
        if (instance == null)
        {
            instance = new FileLogger();
        }
        return instance;
    }

    public void writeMessageToFile(String message)
    {
        if (!DEBUG)
        {
            return;
        }
        try
        {
            FileOutputStream fos = new FileOutputStream(file, true);
            PrintWriter pw = new PrintWriter(fos);
            pw.println("LedServer " + getTime());
            pw.println(message);
            pw.flush();
        }
        catch (FileNotFoundException e)
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    public String getTime()
    {
        Date now = new Date();
        SimpleDateFormat dateFormat = new SimpleDateFormat(
                "yyyy-MM-dd HH:mm:ss");// 可以方便地修改日期格式
        return dateFormat.format(now);
    }

    /**
     * 清空txt的内容
     * @throws FileNotFoundException 
     */
    public void clearTxtContent() throws FileNotFoundException
    {
        if (!DEBUG)
        {
            return;
        }
        FileOutputStream fos = new FileOutputStream(file, false);
        PrintWriter pw = new PrintWriter(fos);
        pw.println("");
    }

    // public static void main(String [] args) throws FileNotFoundException
    // {
    // FileLogger.getInstance().clearTxtContent();
    // FileLogger.getInstance().writeMessage("我的");
    // FileLogger.getInstance().writeMessage("error2");
    // }

    public void writeByteToFile(byte [] buffer)
    {
        try
        {
            FileOutputStream fos = new FileOutputStream(bytefile, true);
            PrintWriter pw = new PrintWriter(fos);
            pw.println("");
            for (int i = 0; i < buffer.length; i++)
            {

                if (i > 0 && i % 16 == 0)
                {
                    pw.println("");

                }
                pw.print(Tools.formatHexString(Integer
                        .toHexString(buffer[i] & 0xff)) + " ");
                pw.flush();
            }
        }
        catch (FileNotFoundException e)
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
}
