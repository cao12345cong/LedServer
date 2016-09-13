package com.clt.service;

import java.util.Timer;
import java.util.TimerTask;

/**
 * 心跳接收
 *
 */
public class HeartBreakReceiver
{
    private static final boolean useHeartBreak=false;//是否用心跳
    
    private static final int HEART_BREAK_SPAN = 10000;//心跳发送一次的时间间隔

    private static final int HEART_TIME_OUT = 60000;// 心跳的超时时间 
    
    private long lastReceiverMessageTime;//最近一次收到心跳的时间
    
    private OnHeartBreakListener listener;
    
    private Timer timer;
    
    public void setOnHeartBreakListener(OnHeartBreakListener listener)
    {
        this.listener = listener;
    }
    /**
     * 接收到一个心跳包，清空
     */
    public void recevierOneHeartBreak(){
        if(!useHeartBreak){
            return;
        }
        lastReceiverMessageTime=System.currentTimeMillis();
    }
    /**
     * 开始接收
     */
    public void startReceiver(){
        if(!useHeartBreak){
            return;
        }
        lastReceiverMessageTime=System.currentTimeMillis();
        timer=new Timer();
        timer.schedule(myTimerTask,0,1000);
    }
    /**
     * 停止接收
     */
    public void stop(){
        if(!useHeartBreak){
            return;
        }
        timer.cancel();
        timer=null;
    }
    /**
     * 计时器
     */
    TimerTask myTimerTask=new TimerTask()
    {
        
        @Override
        public void run()
        {
            long currentTime = System.currentTimeMillis();
            //System.out.println("myTimerTask");
            if(currentTime-lastReceiverMessageTime>=HEART_TIME_OUT){
                if(listener!=null){
                    listener.onReceiverHeartBreakFail();
                }
            }
        }
    };
    
    public interface OnHeartBreakListener{
        public void onReceiverHeartBreakFail();
    }
}
