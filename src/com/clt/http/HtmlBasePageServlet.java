package com.clt.http;

import java.util.Map;

import android.content.Context;

import com.clt.commondata.SenderInfo;
import com.clt.http.NanoHTTPD2.Method;
import com.clt.http.NanoHTTPD2.Request;
import com.clt.http.NanoHTTPD2.Response;

/**
 * Html的父类
 * @author Administrator
 *
 */
public abstract class HtmlBasePageServlet
{
    static Context context;
    
    private SenderInfo senderInfo;
    
    public HtmlBasePageServlet(Context context)
    {
        this.context=context;
    }
    
    public Context getContext(){
        return this.context;
    }
    /**
     * html header字符串
     * @param sb
     */
    public void doHeader(StringBuffer sb){
        
        HTMLHelper.doHeader(sb,null);
    }
    /**
     * html header字符串
     * @param sb
     */
    public void doHeader(StringBuffer sb,String[] scripts){
        
        HTMLHelper.doHeader(sb,scripts);
    }
    
    /**
     * Html 菜单栏
     * @param sb
     */
    public void doMenuBar(StringBuffer sb){
        HTMLHelper.doMenuBar(sb,context);
    }
    /**
     * html footer字符串
     * @param sb
     */
    public void doFooter(StringBuffer sb){
        HTMLHelper.doFooter(sb);
    }
    
    /**
     * 处理http请求
     * @param method
     * @param request
     * @param response 
     */
    public void doRequest(Method method,Request request, Response response){
        if(method==Method.GET){
            doGet(request,response);
        }else if(method==Method.POST){
            doPost(request,response);
        }
    }
    
    public abstract void doGet(Request request, Response response);
    
    public abstract void doPost(Request request, Response response);
    /**
     * 获取资源的字符串
     * @param resId
     * @return
     */
    public static String getRString(int resId){
        return context.getResources().getString(resId);
    }
    
    public static String getRArrString(int resId,int index){
        return context.getResources().getStringArray(resId)[index];
    }

    public SenderInfo getSenderInfo()
    {
        return senderInfo;
    }

    public void setSenderInfo(SenderInfo senderInfo)
    {
        this.senderInfo = senderInfo;
    }
    
    
}
