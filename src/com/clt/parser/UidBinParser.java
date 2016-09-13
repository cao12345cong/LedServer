package com.clt.parser;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

/**
 * 解析Stm的uid加密bin文件
 *
 */
public class UidBinParser
{
    /**
     * 解析
     * @param fileName
     * @return
     * @throws IOException 
     */
    public static byte[] getByteFromBin(File file) throws IOException{
        
        FileInputStream fis=null;
        ByteArrayOutputStream baos=null;
        byte[] buffer=null;
        try
        {
            
            fis=new FileInputStream(file);
            baos=new ByteArrayOutputStream();
            int i=0;
            while((i=fis.read())!=-1){
                baos.write(i);
                if(baos.size()>=256){
                    break;
                }
            }
            buffer=baos.toByteArray();
            
            
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }finally{
            if(fis!=null){
                fis.close();
            }
            if(baos!=null){
                baos.close();
            }
        }
        return buffer;
        
    }
}
