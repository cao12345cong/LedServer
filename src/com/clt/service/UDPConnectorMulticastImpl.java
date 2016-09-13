package com.clt.service;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.net.SocketException;

import android.content.Context;
import android.net.wifi.WifiManager;

import com.clt.UDPConnector;
import com.clt.ledservers.R;
import com.clt.netmessage.NMFindTerminateAnswer;
import com.clt.util.Config;
import com.clt.util.SharedPreferenceUtil;
import com.clt.util.SharedPreferenceUtil.ShareKey;
import com.google.gson.Gson;

/**
 *UDP连接器，多播
 */
public class UDPConnectorMulticastImpl implements UDPConnector
{

    
    public String mTerminateName, mTerminatePassword;

    private MulticastSocket socket;

    private Context context;

    private SharedPreferenceUtil sharedPreferenceUtil;
    
    private final static int RECEIVE_LENGTH = 1024*3;//3Kb

    private static String multicastHost = "224.0.0.255";

    private static int localPort = 9998;
    
    private InetAddress group;
    
    private WifiManager.MulticastLock lock;
    
    private ReceiverThread receiverThread;
    
    public UDPConnectorMulticastImpl(Context context)
    {
        try
        {
            this.context = context;
            this.sharedPreferenceUtil = SharedPreferenceUtil.getInstance(context, null);
            group=InetAddress.getByName(multicastHost);
            if (!group.isMulticastAddress())
            {// 测试是否为多播地址

                throw new Exception("请使用多播地址");

            }
            WifiManager manager = (WifiManager) context
                    .getSystemService(Context.WIFI_SERVICE);
            this.lock= manager.createMulticastLock("UDPwifi"); 
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
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
     */
    class ReceiverThread extends Thread{
        private boolean runnning = true;
        public void run()
        {
            socket = null;
            try
            {
                socket = new MulticastSocket(localPort);
                //setSocketOption();
               
                socket.joinGroup(group);
                //socket.joinGroup(new InetSocketAddress("224.0.0.1", 2048),NetworkInterface.getByName("wlan0"));
                byte [] getBuf = new byte [RECEIVE_LENGTH];
                
                
                while (runnning)
                {
                    DatagramPacket getPacket = new DatagramPacket(getBuf, getBuf.length);
                    lock.acquire();
                    socket.receive(getPacket);
                    // 整理要发送的消息
                    mTerminateName = sharedPreferenceUtil.getString(ShareKey.TerminateName,
                            context.getString(R.string.screen_name_default_val));
                    mTerminatePassword = sharedPreferenceUtil.getString(ShareKey.TerminatePassword,
                            context.getString(R.string.screen_password_default_val));
                    String rcvMsg = new String(getBuf, 0, getPacket.getLength());
                    InetAddress ip = getPacket.getAddress();
                    int port = getPacket.getPort();

                    NMFindTerminateAnswer findTerminateAnswer = new NMFindTerminateAnswer();
                    findTerminateAnswer.setTerminateName(mTerminateName);
                    findTerminateAnswer.setPassword(mTerminatePassword);

                    Gson gson = new Gson();
                    String strFindTerminateAnswer = gson
                            .toJson(findTerminateAnswer);
                    byte [] sendBuf = strFindTerminateAnswer.getBytes();
                    DatagramPacket sendPacket = new DatagramPacket(sendBuf,
                            sendBuf.length, ip, port);
                    // 发送消息
                    socket.send(sendPacket);
                    lock.release();
                }
            }
            catch (SocketException e)
            {
                e.printStackTrace();
            }
            catch (IOException e)
            {
                e.printStackTrace();
            }
            finally
            {
                
                if (socket != null)
                {
                    try
                    {
                        socket.leaveGroup(group);
                        socket.close();
                    }
                    catch (IOException e)
                    {
                        e.printStackTrace();
                    }
                }
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
        
        public void cancel(){
            runnning=false;
            
            if (socket != null)
            {
                socket.close();
            }
            
        }
        
    }

   
    

   
}
