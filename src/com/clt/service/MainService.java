package com.clt.service;

import java.io.IOException;

import com.clt.HTTPConnector;
import com.clt.SerialPortConnector;
import com.clt.TCPConnector;
import com.clt.UDPConnector;
import com.clt.Wrapper;
import com.clt.http.HttpConnectorImpl;
import com.clt.util.AssetFileCopyUtil;
import com.clt.util.Config;
import com.clt.util.FileLogger;
import com.clt.util.NetUtil;

import android.app.Notification;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.IBinder;

public class MainService extends BaseService
{
	
	private TCPConnector tcpConnector;
	
	private TCPFindTerminalConnector tcpLoopConnector;//TCP轮询查找盒子
	
	private UDPConnector udpConnector;
	
	private HttpConnectorImpl httpConnector;
	
	private Wrapper wrapper;
	
	private SerialPortConnector serialPortConnector;
	
	
	
	@Override
	public IBinder onBind(Intent intent)
	{
		return mBinder;
	}
	
	@Override
	public void onCreate()
	{
		try
		{
			super.onCreate();
			/**
			 * 创建前台服务，防止进程被系统杀死
			 */
			Notification notification = new Notification();
			startForeground(1, notification);
			init();
			start();
			//FileLogger.getInstance().writeMessageToFile("开机启动服务成功");
		}
		catch (Exception e)
		{
			
		}
		
		
	}
	
	public static void startService(Context context, Intent intent)
	{
		try
		{
			intent.setComponent(new ComponentName(context, MainService.class));
			context.startService(intent);
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		
	}
	
	private void init()
	{
		// 将asset中的文件拷贝到sd卡中
		boolean flag = AssetFileCopyUtil.assetsCopy(this);
		// http
		httpConnector = new HttpConnectorImpl(NetUtil.getIpAddress(this), 8080);
		httpConnector.setContext(this);
		
		// udp广播查找
		udpConnector = new UDPConnectorImpl(this,Config.UDP_TARGET_PORT);
		
//		//tcp轮询查找
		tcpLoopConnector=new TCPFindTerminalConnector(this);
		
		// tcp
		tcpConnector = new TCPConnectorProxyImpl(this,Config.TCP_PORT);
		// 消息处理拦截
		wrapper = new WrapperImpl(this);
		// 串口通信
		serialPortConnector = new CommandExcutorImpl(this);
		
		tcpConnector.setWrapper(wrapper);
		serialPortConnector.setWrapper(wrapper);
		wrapper.setTCPConnector(tcpConnector);
		wrapper.setSerialPortConnector(serialPortConnector);
	}
	
	private void start()
	{
		try
		{
			tcpConnector.start();
			tcpLoopConnector.start();
			udpConnector.start();
			serialPortConnector.start();
			httpConnector.start();
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}
	
	// 发送消息
	@Override
	public int onStartCommand(Intent intent, int flags, int startId)
	{
		try
		{
//			Bundle bundle = intent.getExtras();
//			if (bundle != null)
//			{
//				String strNetMessage = intent.getExtras().getString("netMessage");
//				if (strNetMessage != null)
//				{
//					wrapper.inputOneMessage(strNetMessage);
//				}
//			}
//			flags = START_STICKY;
			
			if(intent!=null){
				String strNetMessage = intent.getStringExtra("netMessage");
				if(strNetMessage!=null&&strNetMessage.length()>0){
					wrapper.inputOneMessage(strNetMessage);
				}
			}
			
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	
		return super.onStartCommand(intent, flags, startId);
		
	}
	
	@Override
	public void onDestroy()
	{
		super.onDestroy();
		tcpConnector.stop();
		udpConnector.stop();
		serialPortConnector.stop();
		
	}
	
}
