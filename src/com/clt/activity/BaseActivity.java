package com.clt.activity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

/**
 * 基类
 *
 */
public abstract class BaseActivity extends Activity
{
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
    }

    /**
     * Toast显示信息
     * @param msg
     * @param time
     */
    public void toast(String msg, int time)
    {
        Toast.makeText(this, msg, time).show();
    }

    /**
     * Activity之间的跳转
     * @param activityClass
     */
    protected void skip(Class<?> activityClass)
    {
        Intent intent = new Intent();
        intent.setClass(this, activityClass);
        startActivity(intent);
    }
    
    public String getResString(int resId){
        return getResources().getString(resId);
    }
}
