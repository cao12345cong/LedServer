package com.clt.util;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.DecimalFormat;
import java.util.ArrayList;

import android.content.Context;
import android.os.Build;
import android.os.Environment;
import android.os.StatFs;
import android.text.TextUtils;

public class Tools
{
    /**
     * 
     * @param path
     * @param extension 后缀名
     * @param isIterative 是否进入子文件夹
     * @return
     */
    public static ArrayList<String> getFiles(String path, String extension,
            boolean isIterative) // 搜索目录，扩展名，是否进入子文件夹

    {
        if (TextUtils.isEmpty(path) || TextUtils.isEmpty(extension))
        {
            return null;
        }
        File file = new File(path);
        if (!file.exists())
        {
            return null;
        }
        ArrayList<String> lstFile = new ArrayList<String>();
        File [] files = new File(path).listFiles();

        for (int i = 0; i < files.length; i++)

        {
            File f = files[i];

            if (f.isFile())
            {
                // if (f.getPath()
                // .substring(f.getPath().length() - Extension.length())
                // .equals(Extension)) // 判断扩展名
                // lstFile.add(f.getPath());
                if (f.getName().toLowerCase().endsWith(extension.toLowerCase()))
                {
                    lstFile.add(f.getPath());
                }

                if (!isIterative)
                    break;
            }

            else if (f.isDirectory() && f.getPath().indexOf("/.") == -1)
            {
                getFiles(f.getPath(), extension, isIterative);
            }
            // 忽略点文件（隐藏文件/文件夹）
        }

        return lstFile;

    }

    /**
     * 复制文件到另外一个文件夹
     * @throws IOException 
     */
    public static boolean copyFileToOtherDir(File srcPath, String toPath)
    {
        InputStream in = null;
        OutputStream out = null;
        try
        {
            File toFile = new File(toPath);
            if (toFile.exists())
            {
                toFile.delete();
                toFile.createNewFile();
            }
            else
            {
                if (!toFile.getParentFile().exists())
                {
                    toFile.getParentFile().mkdirs();
                }
                toFile.createNewFile();
            }

            in = new FileInputStream(srcPath);
            out = new FileOutputStream(toFile);
            byte [] buffer = new byte [1024 * 5];
            int len = -1;
            while ((len = in.read(buffer)) != -1)
            {
                out.write(buffer, 0, len);
                out.flush();
            }

            return true;
        }
        catch (FileNotFoundException e)
        {
            e.printStackTrace();
            return false;
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
        finally
        {
            if (out != null)
            {
                try
                {
                    out.close();
                }
                catch (IOException e)
                {
                    e.printStackTrace();
                }
            }
            if (in != null)
            {
                try
                {
                    in.close();
                }
                catch (IOException e)
                {
                    e.printStackTrace();
                }
            }
        }
        return true;
    }

    /**
     * 改变系统时间
     * @param year
     * @param month
     * @param day
     * @param hour
     * @param minut
     * @param second
     */
    public static void changeSystemTime(byte year, byte month, byte day,
            byte hour, byte minut, byte second)
    {
        try
        {
            Process process = Runtime.getRuntime().exec("su");
            String datetime = "20" + year + bcd2StrFormat(month)
                    + bcd2StrFormat(day) + bcd2StrFormat(hour)
                    + bcd2StrFormat(minut) + bcd2StrFormat(second);
            // String datetime = "20131023.062800"; // 测试的设置的时间【时间格式
            // yyyyMMdd.HHmmss】
            DataOutputStream os = new DataOutputStream(
                    process.getOutputStream());
            os.writeBytes("setprop persist.sys.timezone GMT\n");
            os.writeBytes("/system/bin/date -s " + datetime + "\n");
            os.writeBytes("clock -w\n");
            os.writeBytes("exit\n");
            os.flush();
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
    }

    /**
     * 改变系统时间
     * @param year
     * @param month
     * @param day
     * @param hour
     * @param minut
     * @param second
     */
    public static void changeSystemTime(byte [] time)
    {
        try
        {
            Process process = Runtime.getRuntime().exec("su");
            String datetime = "20" + bcd2StrFormat(time[0])
                    + bcd2StrFormat(time[1]) + bcd2StrFormat(time[2]) + "."
                    + bcd2StrFormat(time[3]) + bcd2StrFormat(time[4])
                    + bcd2StrFormat(time[5]);
            // String datetime = "20131023.062800"; // 测试的设置的时间【时间格式
            // yyyyMMdd.HHmmss】
            DataOutputStream os = new DataOutputStream(
                    process.getOutputStream());
            os.writeBytes("setprop persist.sys.timezone GMT\n");
            os.writeBytes("/system/bin/date -s " + datetime + "\n");
            os.writeBytes("clock -w\n");
            os.writeBytes("exit\n");
            os.flush();
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
    }

    public static String format(int data)
    {
        DecimalFormat format = new DecimalFormat("00");
        return format.format(data);
    }
    

    /**
     * @功能: BCD码转为10进制串(阿拉伯数据)  0x06->"06"
     * @参数: BCD码
     * @结果: 10进制串
     */
    public static String bcd2StrFormat(byte value)
    {
        StringBuffer temp = new StringBuffer(2);
        temp.append((byte) ((value & 0xf0) >>> 4));
        temp.append((byte) (value & 0x0f));
        return temp.toString();
    }

    /**
     * 给UID加密
     * @param uid
     * @return
     */
    public static byte [] encryptUID(byte [] uid)
    {
        byte [] encryptCode = new byte [6];
        byte byte1 = 0x00;
        byte byte2 = 0x00;
        for (int i = 0; i < 6; i++)
        {
            byte1 = uid[2 * i];
            byte2 = uid[2 * i + 1];
            encryptCode[i] = (byte) ((byte1 & 0x55) | (byte2 & 0xAA));
        }
        return encryptCode;
    }
    
    /**
     * 判断手机是否存在SD卡
     * @return
     */
    public static boolean hasSDCard(){
        return Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState());
    }
    
    public static String formatHexString(String data)
    {
       if(data.length()==1){
           return "0"+data;
       }
       return data;
    }
    
    /**
     * 创建一个文件
     * @param dir
     * @param name
     */
    public static boolean createFile(String dir,String name){
        File file = new File(dir, name);
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
                e.printStackTrace();
                return false;
            }
        }
        return true;
        
    }
    
    /**
     * 获得SD卡总大小
     * 
     * @return
     */
    public static long getSDTotalSize()
    {
        File path = Environment.getExternalStorageDirectory();
        StatFs stat = new StatFs(path.getPath());
        long blockSize = stat.getBlockSize();
        long totalBlocks = stat.getBlockCount();
        return blockSize* totalBlocks;
    }

    /**
     * 获得sd卡剩余容量，即可用大小
     * 
     * @return
     */
    public static long getSDAvailableSize()
    {
        File path = Environment.getExternalStorageDirectory();
        StatFs stat = new StatFs(path.getPath());
        long blockSize = stat.getBlockSize();
        long availableBlocks = stat.getAvailableBlocks();
        return blockSize* availableBlocks;
    }

    /**
     * 获得文件大小
     * @param f
     * @return
     * @throws Exception
     */
    public static long getFileSize(File f) throws Exception{//取得文件大小
        long s=0;
        if (f.exists()) {
            FileInputStream fis = null;
            fis = new FileInputStream(f);
           s= fis.available();
        } else {
            f.createNewFile();
            System.out.println("文件不存在");
        }
        return s;
    }
    /**
     * 获得文件夹大小
     * @param f
     * @return
     * @throws Exception
     */
    public static long getDirSize(File dir)throws Exception//取得文件夹大小
    {
        long size = 0;
        File flist[] = dir.listFiles();
        for (int i = 0; i < flist.length; i++)
        {
            if (flist[i].isDirectory())
            {
                size = size + getFileSize(flist[i]);
            } else
            {
                size = size + flist[i].length();
            }
        }
        return size;
    }
    /**
     * 获得系统版本
     * @return
     */
    public static String getImgVersion(){  
        String str1 = "/proc/version";  
        String str2;  
        String[] arrayOfString;  
        try {  
            FileReader localFileReader = new FileReader(str1);  
            BufferedReader localBufferedReader = new BufferedReader(  
                    localFileReader, 8192);  
            str2 = localBufferedReader.readLine();  
            arrayOfString = str2.split("\\s+");  
            //version[0]=arrayOfString[2];//KernelVersion  
            localBufferedReader.close();  
        } catch (IOException e) {  
        }  
        return Build.VERSION.RELEASE;// firmware version  
//        version[2]=Build.MODEL;//model  
//        version[3]=Build.DISPLAY;//system version  
        //return version;  
    }  
    
    
}
