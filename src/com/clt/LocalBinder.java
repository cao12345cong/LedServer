package com.clt;

import android.os.Binder;

import com.clt.service.BaseService;

/**
 * 
 */
public class LocalBinder extends Binder
{
    private BaseService baseService;
    public LocalBinder(BaseService baseService)
    {
        this.baseService=baseService;
    }
    public BaseService getService()
    {
        return baseService;
    }
    
    
}

