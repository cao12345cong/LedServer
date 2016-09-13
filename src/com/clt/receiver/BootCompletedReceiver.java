package com.clt.receiver;

import java.io.File;

import com.clt.service.MainService;
import com.clt.util.Constants;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

/**
 * 开机广播接收器
 *
 */
public class BootCompletedReceiver extends BroadcastReceiver
{

    @Override
    public void onReceive(Context context, Intent intent)
    {

        if (intent.getAction().equals(Intent.ACTION_BOOT_COMPLETED))
        {
            Intent intent1=new Intent(context,MainService.class);
            context.startService(intent1);
                       
            
        }
    }
    /**
     * 创建FTP的上传目录program
     */
    public void createFTPProgramDir(){
        File dir=new File(Constants.SDCARD_SD_FTP);
        if(!dir.exists()){
            dir.mkdirs();
        }
    }

}
