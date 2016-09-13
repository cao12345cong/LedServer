package com.clt.receiver;

import java.io.File;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager.NameNotFoundException;
import android.net.Uri;

import com.clt.activity.MainActivity;
import com.clt.netmessage.NMGetRTC;
import com.clt.netmessage.NMSaveUidEncrpt;
import com.clt.netmessage.NMSetAutoBright;
import com.clt.netmessage.NMSetPortAreaByXml;
import com.clt.service.MainService;
import com.clt.util.Constants;
import com.clt.util.FileLogger;
import com.clt.util.Tools;
import com.google.gson.Gson;

/**
 * 设置广播接收器 
 * 目前支持:  1.U盘插入    2.pc的Ftp发送
 *
 */
public class SettingReceiver extends BroadcastReceiver
{
    private static final boolean DEBUG = false;

    public static final String APK_UPDATE_SERVERAPK = "LedServer.apk";

    public static final String XML_PORTAREA = "ControlArea.xml";// 设置网口面积的xml文件

    public static final String XML_BRIGHTNESS = "Brightness.xml";// 设置亮度曲线的xml文件

    public static final String BIN_RECEIVER = "receiver.bin";// 设置接收卡基本参数的bin文件

    public static final String DIR_ENCRYPT = "encrypt";// 保存uid的文件夹

    private Context context;

    @Override
    public void onReceive(Context context, Intent intent)
    {

        this.context = context;

        /*
         * 1.检测U盘中是否有apk 2.有apk,拷贝到sd卡中比较版本是否需要更新， 3.如果需要更新，则更新
         */
        String action = intent.getAction();
        if (action.equalsIgnoreCase(Intent.ACTION_MEDIA_MOUNTED))// U盘插上
        {
            FileLogger.getInstance().writeMessageToFile("接收到Ｕ盘广播");
            doSomethingByUdcard();

        }
        // else if (action.equalsIgnoreCase(Intent.ACTION_BOOT_COMPLETED))// 开机
        // {
        // doSthAfterBootCompleted(intent);
        // }
        else if (action.equalsIgnoreCase(Intent.ACTION_MEDIA_REMOVED))// U盘移除
        {

        }
        else if (action.equalsIgnoreCase(Intent.ACTION_MEDIA_CHECKING))
        {

            FileLogger.getInstance().writeMessageToFile("CHECKING接收到Ｕ盘广播");
        }
        else if (intent.getAction().equals("com.clt.ledserver.xmlsetting"))
        {// LedVison发送xml到ftp设置
            String mAction = intent.getStringExtra("action");
            String mPath = intent.getStringExtra("path");
            if (mAction.equalsIgnoreCase("ControlArea"))
            {
                setPortArea(new File(mPath));
            }
            else if (mAction.equalsIgnoreCase("Brightness"))
            {
                setAutoBright(new File(mPath));
            }

        }else if(intent.getAction().equals("com.clt.setportArea")){
        	
        	doSomethingByUdcard();
        }
        

    }

    /**
     * 开机后要做的事情
     * @param intent 
     */
    private void doSthAfterBootCompleted(Intent intent)
    {
        // 开启服务
        // startServices(intent);
        // 设置RTC时间
        // getRTC();
    }

    /**
     * 用U盘做的处理
     */
    private void doSomethingByUdcard()
    {
        try
        {

            String dir = getDir();
            // 设置网口面积
            setPortArea(new File(dir+XML_PORTAREA));
            // 更新Ledserver
            // updateAPK(dir+APK_UPDATE_SERVERAPK);
            // 设置亮度曲线
            setAutoBright(new File(dir + XML_BRIGHTNESS));
            // 设置uid加密码
            saveUidEncrpt(dir);
        }
        catch (Exception e)
        {
        }

    }

    // /**
    // * 开机启动各种服务
    // */
    // private void startServices(Intent intent)
    // {
    //
    // Log.e("LedServer.BootCompletedReceiver",
    // "startService ServerService.class-0");
    //
    // if (intent.getAction().equals(Intent.ACTION_BOOT_COMPLETED))
    // {
    //
    // Log.e("LedServer.BootCompletedReceiver",
    // "startService ServerService.class");
    // // Intent intentActivity=new Intent(contex, MainActivity.class);
    // // intentActivity.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
    // // contex.startActivity(intentActivity);
    //
    // Intent _intent = new Intent(context, UdpService.class);
    // context.startService(_intent);
    // // bindService(_intent, mConnection, Context.BIND_AUTO_CREATE);
    //
    // System.out.println("startService SenderOperationService.class");
    // Intent _intent1 = new Intent(context, SenderOperationService.class);
    // context.startService(_intent1);
    // // bindService(_intent1, sendOperatorSeviceConnection,
    // // Context.BIND_AUTO_CREATE);
    //
    // System.out.println("startService SenderOperationService.class");
    // Intent _intent2 = new Intent(context, TcpWifiServie.class);
    // context.startService(_intent2);
    // // bindService(_intent2, terminateNetServiceConnection,
    // // Context.BIND_AUTO_CREATE);
    //
    // Intent _intent3 = new Intent(context, UploadService.class);
    // context.startService(_intent3);
    // }
    //
    // }
    //

    /**
     * 判断sd是否存在，存在则返回U盘路径;如果存在2个U盘，获取存在相应文件的U盘路径，
     * 如果两个U盘都存在对应的文件，则默认获取第一个U盘路径
     * @return
     */
    public String getDir()
    {
        // if (this.DEBUG)
        // {
        // return Constants.SDCARD_PATH;
        // }
        return Constants.USB_PATH_0;
        //return Constants.SDCARD_PATH;
        //return "/mnt/usb_storage/USB_DISK0/udisk0";
    }

    /**
     * 升级apk
     */
    @Deprecated
    public void updateAPK(String path)
    {
        try
        {
            if (path == null)
            {
                return;
            }
            File file = new File(path);
            if (!file.exists())
            {
                return;
            }

            Tools.copyFileToOtherDir(file, "mnt/sdcard/" + APK_UPDATE_SERVERAPK);
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setDataAndType(
                    Uri.fromFile(new File("mnt/sdcard", APK_UPDATE_SERVERAPK)),
                    "application/vnd.android.package-archive");
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(intent);
        }
        catch (Exception e)
        {
        }

    }

    /**
     * 设置网口面积
     */

    private void setPortArea(File file)
    {
        FileLogger.getInstance().writeMessageToFile("开始U盘设置网口面积");
        try
        {
            // if (path == null)
            // {
            // return;
            // }
            // File file = new File(path);
            if (!file.exists())
            {
                FileLogger.getInstance().writeMessageToFile(
                        "U盘设置网口面积,文件不存在" + file.getAbsolutePath());
                return;
            }

            NMSetPortAreaByXml nmSetPortAreaByXml = new NMSetPortAreaByXml();
            nmSetPortAreaByXml.setPath(file.getParent());
            Gson gson = new Gson();
            String message = gson.toJson(nmSetPortAreaByXml);
            Intent intent = new Intent();
            intent.putExtra("netMessage", message);
            //intent.putExtra("path", file.getParent());
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            MainService.startService(context, intent);

            FileLogger.getInstance().writeMessageToFile("收到广播，设置网口面积");
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    /**
     * 获取RTC时间
     */
    public void getRTC()
    {
        NMGetRTC nm_GetRTC = new NMGetRTC();
        Gson gson = new Gson();
        String nmString = gson.toJson(nm_GetRTC);
        Intent intent = new Intent();
        intent.putExtra("netMessage", nmString);
        MainService.startService(context,intent);
        // SenderOperationService.startService(context, intent);
    }

    /**
     * 设置亮度曲线
     */
    public void setAutoBright(File file)
    {
        try
        {
            if (!file.exists())
            {
                return;
            }
            FileLogger.getInstance().writeMessageToFile("收到广播，自动亮度调节");
            NMSetAutoBright nm_SetAutoBright = new NMSetAutoBright();
            nm_SetAutoBright.setPath(file.getParent());
            Gson gson = new Gson();
            String nmString = gson.toJson(nm_SetAutoBright);
            Intent intent = new Intent();
            intent.putExtra("netMessage", nmString);
            //intent.putExtra("path",file.getParent());
            MainService.startService(context, intent);
        }
        catch (Exception e)
        {
        }

    }

    /**
     * 保存uid加密码
     */
    private void saveUidEncrpt(String path)
    {
        try
        {
            if (path == null)
            {
                return;
            }
            File file = new File(path, DIR_ENCRYPT);
            if (!file.exists())
            {
                return;
            }
            NMSaveUidEncrpt nm_SaveUidEncrpt = new NMSaveUidEncrpt();
            nm_SaveUidEncrpt.setPath(path);
            Gson gson = new Gson();
            String nmString = gson.toJson(nm_SaveUidEncrpt);
            Intent intent = new Intent();
            intent.putExtra("netMessage", nmString);
            MainService.startService(context, intent);
        }
        catch (Exception e)
        {
        }

    }

    /**
     * 获得版本号
     * @param context
     * @return
     */
    public int getVerCode(Context context)
    {
        int verCode = -1;
        try
        {
            verCode = context.getPackageManager().getPackageInfo(
                    "com.clt.ledserver", 0).versionCode;
        }
        catch (NameNotFoundException e)
        {
            e.printStackTrace();
        }

        return verCode;
    }

    /**
     * 获得版本名称
     */
    public String getVerName(Context context)
    {
        String verName = "";
        try
        {
            verName = context.getPackageManager().getPackageInfo(
                    "com.clt.ledserver", 0).versionName;
        }
        catch (NameNotFoundException e)
        {
        }
        return verName;
    }

}
