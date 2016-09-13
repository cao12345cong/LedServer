package com.clt.service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.TimerTask;

import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.content.Intent;

import com.clt.SerialPortConnector;
import com.clt.TCPConnector;
import com.clt.Wrapper;
import com.clt.netmessage.NMHeartBreak;
import com.clt.netmessage.NMHeartBreakAnswer;
import com.clt.netmessage.NMKickOutOf;
import com.clt.netmessage.NetMessageType;
import com.clt.service.HeartBreakReceiver.OnHeartBreakListener;
import com.clt.util.Config;
import com.clt.util.FileLogger;
import com.google.gson.Gson;
/**
 * TCP连接器
 */
public class TCPConnectorImpl implements TCPConnector,OnHeartBreakListener
{
    private Context context;
    
    private volatile ArrayList<String> list = new ArrayList<String>();
    
    private InputHandler inputHandler;

    private OutputHandler outputHandler;
    
    private volatile Socket socket;
    
    private static String CLOSE="close";
    
    private boolean isUseHeartBreak=true;
    
    private static final int HEART_BREAK_SPAN=10000;
    
    private HeartBreakReceiver heartBreakReceiver;
    
    private int flag=Source.WIFI;
    
    private Wrapper wrapper;
            
    private static final class Source{
        private static final int WIFI=1;
        private static final int PC=2;
        
    }
    
    public TCPConnectorImpl(Context context,Socket socket)
    {
        this.context=context;
        this.socket=socket;
        heartBreakReceiver=new HeartBreakReceiver();
        heartBreakReceiver.setOnHeartBreakListener(this);
    }
    /**
     * 开始处理
     */
    public void start(){
        try
        {
            inputHandler = new InputHandler(socket.getInputStream());
            outputHandler = new OutputHandler(socket.getOutputStream());
            inputHandler.start();
            outputHandler.start();
            //启动心跳接收器
            heartBreakReceiver.startReceiver();
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
    }
    /**
     * 停止处理
     */
    public void stop(){
        try
        {
            inputHandler.cancel();
            outputHandler.cancel();
            if(heartBreakReceiver!=null){
                heartBreakReceiver.stop();
            }
            
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
    }
    /**
     * 处理读IO
     * @author Administrator
     *
     */
    class InputHandler extends Thread
    {
        BufferedReader input;

        String readMessage;
        
        boolean running=true;

        public InputHandler(InputStream in)
        {
            input = new BufferedReader(new InputStreamReader(in));
        }

        @Override
        public void run()
        {

            try
            {
                while (running)
                {
                    readMessage= input.readLine();
                    if(readMessage==null){
                        break;
                    }
                    if (readMessage != null)
                    {
                        
                        try
                        {
                            JSONObject jsonObject = new JSONObject(readMessage); 
                            if (jsonObject.has("mType")){
                                
                                int nmType = jsonObject.getInt("mType");
                                //心跳
                                heartBreakReceiver.recevierOneHeartBreak();
                                if(nmType==NetMessageType.HeartBreak){
                                   NMHeartBreakAnswer nmHeartBreakAnswer=new NMHeartBreakAnswer();
                                   Gson gson=new Gson();
                                   String message=gson.toJson(nmHeartBreakAnswer);
                                   responseOneMessage(message);
                                   FileLogger.getInstance().writeMessageToFile("收到心跳");
                                }
                            }
                            if(wrapper!=null){
                                wrapper.inputOneMessage(readMessage);
                            }
                           
                            //addOneMessage(readMessage);
                            FileLogger.getInstance().writeMessageToFile("收到消息"+readMessage);
                        }
                        catch (JSONException e)
                        {
                            e.printStackTrace();
                        }
                    }
                }
            }
            catch (IOException e)
            {
                FileLogger.getInstance().writeMessageToFile("inputHandlerThread退出"+e.getMessage());
            }
            

        }
        public void cancel() throws IOException{
            socket.shutdownInput();
            running=false;
        }
    }
    /**
     * 处理写IO
     * @author Administrator
     *
     */
    class OutputHandler extends Thread
    {
        PrintWriter output;
        private boolean running=true;

        public OutputHandler(OutputStream os)
        {
            output = new PrintWriter(new OutputStreamWriter(os));

        }

        @Override
        public void run()
        {
            try
            {
                while (running)
                {
                    if (list.isEmpty())
                    {
                        synchronized (list)
                        {
                            try
                            {
                                list.wait();
                            }
                            catch (InterruptedException e)
                            {
                                e.printStackTrace();
                            }
                        }

                    }
                    
                    String message = null;
                    synchronized (list)
                    {
                        message =list.remove(0);
                    }
                    if(message.equalsIgnoreCase(CLOSE)){
                        NMKickOutOf nm_KickOutOf=new NMKickOutOf();
                        Gson gson = new Gson();
                        message= gson.toJson(nm_KickOutOf);
                    }
                   
                    output.println(message);
                    output.flush();
                    FileLogger.getInstance().writeMessageToFile("发送消息"+message);
                    if(message.equalsIgnoreCase(CLOSE)){
                        running=false;
                    }
                }
            }
            catch (Exception e)
            {
                
            }finally{
                FileLogger.getInstance().writeMessageToFile("OutputHandler退出");
                if(output!=null){
                    try
                    {
                        output.close();
                    }
                    catch (Exception e)
                    {
                        e.printStackTrace();
                    }
                }
                if(socket!=null){
                    try
                    {
                        socket.close();
                    }
                    catch (IOException e)
                    {
                        e.printStackTrace();
                    }
                }
                
            }
            

        }
        /**
         * 
         * @param msg
         */
        public void pushString(String msg)
        {
            synchronized (list)
            {
                list.add(msg);
                list.notifyAll();
            }
        }
        public void cancel() throws IOException{
            list.clear();
            pushString(CLOSE);
            //running=false;
        }
    }
    
    
    
    /**
     * 心跳
     */
    TimerTask myTimeTask = new TimerTask()
    {

        @Override
        public void run()
        {
            if (isUseHeartBreak)
            {
                Gson gson = new Gson();
                String nmString = gson.toJson(new NMHeartBreak());
                FileLogger.getInstance().writeMessageToFile(
                        "发送心跳");
                responseOneMessage(nmString);
            }
        }
    };

    @Override
    public void onReceiverHeartBreakFail()
    {
        stop();
        
    }
    @Override
    public void responseOneMessage(String message)
    {
        outputHandler.pushString(message); 
    }
    @Override
    public void setWrapper(Wrapper wrapper)
    {
        this.wrapper=wrapper;
    }

}

