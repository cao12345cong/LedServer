package com.clt.service;

import com.clt.LocalBinder;
import com.clt.Server;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
/**
 * service基类
 */
public abstract class BaseService extends Service implements Server
{

    protected IBinder mBinder = new LocalBinder(this);
    

}
