package com.clt.util;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import android.content.Context;
import android.content.res.AssetManager;
import android.os.Environment;

/**
 * asset文件夹的目录复制工具类
 * @author caocong
 *
 */
public class AssetFileCopyUtil
{

    private static final String ASSET_PATH = "ledserver";

    private static final String DIR_PATH = Environment
            .getExternalStorageDirectory() + "/" + ASSET_PATH;

    /**
     * 拷贝是否成功
     * @return
     */
    public static boolean assetsCopy(Context context)
    {

        try
        {
            AssetManager manager = context.getAssets();
            assetsCopy(ASSET_PATH, DIR_PATH, manager); // assets内.wfs文件找不到==
        }
        catch (IOException e)
        {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    /**
     * 拷贝
     * @param assetsPath asset目录
     * @param dirPath    复制到的目录
     * @throws IOException
     */
    public static void assetsCopy(String assetsPath, String dirPath,
            AssetManager manager) throws IOException
    {
        String [] list = manager.list(assetsPath);
        if (list.length == 0)
        { // 文件
            InputStream in = manager.open(assetsPath);
            File file = new File(dirPath);
            if (!file.getParentFile().exists())
            {
                file.getParentFile().mkdirs();
            }
            file.createNewFile();
            FileOutputStream fout = new FileOutputStream(file);
            /* 复制 */
            byte [] buf = new byte [1024];
            int count;
            while ((count = in.read(buf)) != -1)
            {
                fout.write(buf, 0, count);
                fout.flush();
            }
            /* 关闭 */
            in.close();
            fout.close();
        }
        else
        { // 目录
            for (String path : list)
            {
                assetsCopy(assetsPath + "/" + path, dirPath + "/" + path,
                        manager);
            }
        }
    }

    public static File copyAssetFileToSdcard(Context context, String fileName)
    {
        // File file = new File(SD_PATH);
        // // 不存在则创建，存在就返回
        // if (!file.exists())
        // file.mkdirs();
        InputStream in = null;
        OutputStream out = null;
        try
        {
            File copyFile = new File(Constants.SDCARD_PATH, "fonts/" + fileName);
            if (!copyFile.getParentFile().exists())
            {
                copyFile.getParentFile().mkdirs();
            }
            if (!copyFile.exists())
            {
                copyFile.createNewFile();
            }

            // 获取图片，将图片copy到sdcard
            in = context.getAssets().open(fileName);
            out = new FileOutputStream(copyFile);
            byte [] buff = new byte [1024];
            int len;
            while ((len = in.read(buff)) > 0)
            {
                out.write(buff, 0, len);
            }
            return copyFile;
        }
        catch (IOException e)
        {
            e.printStackTrace();
            return null;
        }
        finally
        {
            try
            {
                if (out != null)
                {
                    out.close();
                }
                if (in != null)
                {
                    in.close();
                }

            }
            catch (IOException e)
            {
                e.printStackTrace();
            }
        }
    }
}
