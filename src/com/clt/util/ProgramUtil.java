package com.clt.util;

import java.io.File;
import java.io.FileFilter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;

import com.clt.entity.Program;

public class ProgramUtil
{
    /**
     * 从指定目录中获取节目单
     * @param path 目录名
     * @return
     */
    public ArrayList<Program> getPrograms(String dirPath){
        File dir=new File(dirPath);
        if(!dir.exists()||!dir.isDirectory()){
            return null;
        }
        ArrayList<Program> programs=new ArrayList<Program>();
        //programs
        
        //ArrayList<String> programs=new ArrayList<String>();
        //FileFilter 
        File [] files = dir.listFiles(vsnfileFilter);
        Program program=null;
        for (File file : files)
        {
            program=new Program();
            if(dirPath.equalsIgnoreCase(Constants.USB_PATH_0)||dirPath.equalsIgnoreCase(Constants.USB_PATH_1)){
                program.setPathType(Program.UDISK);
            }else if(dirPath.equalsIgnoreCase(Constants.SDCARD_PATH)||dirPath.equalsIgnoreCase(Constants.SDCARD_DOWNLOAD_PATH)
                    ||dirPath.equalsIgnoreCase(Constants.SDCARD_SD_PATH)||dirPath.equalsIgnoreCase(Constants.SDCARD_SD_FTP)){
                program.setPathType(Program.SDCARD);
            }else{
                program.setPathType(Program.INTERNAL_STORAGE);
            }
            program.setFileName(file.getName());
            program.setPath(file.getParent());
            String fileName=file.getName();
            File programDir=new File(file.getParent(),fileName.substring(0,fileName.lastIndexOf("."))+".files");
            try
            {
                program.setSize(Tools.getDirSize(programDir)+Tools.getFileSize(file));
            }
            catch (Exception e)
            {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            long lastTime=file.lastModified();
            SimpleDateFormat format=new SimpleDateFormat("yyyy-MM-dd");
            program.setCreateTime(format.format(lastTime));
            programs.add(program);
        }
        return programs;
    }
    /**
     * 获取文件名，去掉后缀名
     * @param fileName
     * @return
     */
    private String getFileName(String fileName){
        if(fileName.indexOf(".")>0){
            return fileName.substring(0,fileName.lastIndexOf("."));
        }
        return null;
    }
    /**
     * 文件过滤，只获取符合节目格式的文件
     */
    FileFilter vsnfileFilter=new FileFilter()
    {
        
        @Override
        public boolean accept(File file)
        {
            if(file.getName().toLowerCase().endsWith(Constants.PROGRAM_EXTENSION)){
                return true;
            }
            return false;
        }
    };
}
