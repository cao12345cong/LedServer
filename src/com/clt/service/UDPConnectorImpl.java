package com.clt.service;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;

import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.text.TextUtils;

import com.clt.UDPConnector;
import com.clt.ledservers.R;
import com.clt.netmessage.NMFindTerminateAnswer;
import com.clt.netmessage.NetMessageType;
import com.clt.util.Config;
import com.clt.util.SharedPreferenceUtil;
import com.clt.util.SharedPreferenceUtil.ShareKey;
import com.google.gson.Gson;

/**
 * UDP连接器，广播
 *
 */
public class UDPConnectorImpl implements UDPConnector
{

    public String mTerminateName, mTerminatePassword;

    private Context context;

    private SharedPreferenceUtil sharedPreferenceUtil;
    
    private ReceiverThread receiverThread;
    
    private int port;
    
    public UDPConnectorImpl(Context context,int port)
    {
        this.context = context;
        this.port=port;
        this.sharedPreferenceUtil = SharedPreferenceUtil.getInstance(context, null);
    }

    @Override
    public void start()
    {
        receiverThread=new ReceiverThread();
        receiverThread.start();
    }


    @Override
    public void stop()
    {
        if(receiverThread!=null){
            receiverThread.cancel();
        }
        
    }
    
    
    /**
     * 接收线程
     *
     */
    class ReceiverThread extends Thread{
        
        private boolean running = true;
        
        private DatagramSocket socket;
        
        public void run()
        {
            socket = null;
            try
            {
                socket = new DatagramSocket(port);
                setSocketOption();
                byte [] getBuf = new byte [1024];
                DatagramPacket getPacket = new DatagramPacket(getBuf, getBuf.length);
                while (running)
                {
                   
                    socket.receive(getPacket);
                   
                    String receiveMessage = new String(getBuf, 0, getPacket.getLength());
                    //获得客户端的ip和端口
                    InetAddress remoteIp = getPacket.getAddress();
                    int remotePort = getPacket.getPort();
                    //如果不是json字符串会报错
                    if(!isStringJson(receiveMessage)){
                    	continue;
                    }
                    JSONObject jsonObject=new JSONObject(receiveMessage);
                    if (!jsonObject.has("mType")){
                    	continue;
                    }
                    int nmType = jsonObject.getInt("mType");
                    if(nmType==NetMessageType.FindTerminate){
                    	 // 整理要发送的消息
                    	mTerminateName = sharedPreferenceUtil.getString(ShareKey.TerminateName,
                                context.getString(R.string.screen_name_default_val));
                        mTerminatePassword = sharedPreferenceUtil.getString(ShareKey.TerminatePassword,
                                context.getString(R.string.screen_password_default_val));
                    	 NMFindTerminateAnswer findTerminateAnswer = new NMFindTerminateAnswer();
                         findTerminateAnswer.setTerminateName(mTerminateName);
                         findTerminateAnswer.setPassword(mTerminatePassword);

                         Gson gson = new Gson();
                         String strFindTerminateAnswer = gson
                                 .toJson(findTerminateAnswer)+"\n";
                         byte [] sendBuf = strFindTerminateAnswer.getBytes();
                         DatagramPacket sendPacket = new DatagramPacket(sendBuf,
                                 sendBuf.length, remoteIp, remotePort);
                         // 发送消息
                         socket.send(sendPacket);
                    }
                    
                   

                }
            }
            catch (SocketException e)
            {
                e.printStackTrace();
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }
            finally
            {
                if (socket != null)
                {
                    socket.close();
                }
            }

        }
        
        public void cancel(){
            this.running=false;
            
            if (socket != null)
            {
                socket.close();
            }
            
        }
        
        private void setSocketOption()
        {
            try
            {
                socket.setReceiveBufferSize(Config.RECEVICE_BUF_SIZE);
                socket.setSendBufferSize(Config.SEND_BUF_SIZE);
            }
            catch (SocketException e)
            {
                e.printStackTrace();
            }
        }
    }
    

    
    private boolean isStringJson(String string){
    	if(TextUtils.isEmpty(string)){
    		return false;
    	}
    	try
		{
			new JSONObject(string);
			return true;
		}
		catch (JSONException e)
		{
			return false;
		}
    }


   

}
