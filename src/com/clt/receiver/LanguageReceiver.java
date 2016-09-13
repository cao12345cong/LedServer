package com.clt.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.clt.activity.MainActivity;

/**
 *  通过BroadcastReceiver重启当前页面，实现语言切换

 */
public class LanguageReceiver extends BroadcastReceiver
{

    @Override
    public void onReceive(Context context, Intent intent)
    {

        Intent it = new Intent(context, MainActivity.class);
        it.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);// 这个必须加
        context.startActivity(it);
    }

}
