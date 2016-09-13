package com.clt.service;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.ArrayList;

import android.content.Context;

import com.clt.TCPConnector;
import com.clt.Wrapper;
import com.clt.util.Config;

/**
 * TCP连接器代理
 */
public class TCPConnectorProxyImpl implements TCPConnector
{
    private ArrayList<TCPConnectorImpl> socketProcessorList;// socket处理器集合

    private TCPConnectorImpl socketProcessor;

    protected ServerSocket mServerSocket;

    private Socket socket;

    private Context context;

    private AccpetThread mAccpetThread;

    private Wrapper wrapper;

    public TCPConnectorProxyImpl(Context context)
    {
    	this(context,Config.TCP_PORT);
    }
    
    public TCPConnectorProxyImpl(Context context,int port)
    {
        try
        {
            this.context = context;
            mServerSocket = new ServerSocket(port);
            socketProcessorList = new ArrayList<TCPConnectorImpl>();
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
    }

    public void start()
    {
        mAccpetThread = new AccpetThread();
        mAccpetThread.setName("ListeningThread");
        mAccpetThread.start();
    }

    public void stop()
    {
        if (mServerSocket != null)
        {

            try
            {
                mServerSocket.close();
            }
            catch (IOException e)
            {
                e.printStackTrace();
            }
        }
        if (mAccpetThread != null)
        {
            mAccpetThread.interrupt();
            mAccpetThread = null;
        }
        if (socketProcessor != null)
        {
            socketProcessor.stop();
        }

    }

    /**
     * 接收socket，交给socketProceesor处理
     * 
     */
    private class AccpetThread extends Thread
    {

        @Override
        public void run()
        {
            while (!isInterrupted())
            {
                try
                {

                    socket = mServerSocket.accept();
                    setSocketOptions();
                    socketProcessor = new TCPConnectorImpl(context, socket);
                    socketProcessor.setWrapper(wrapper);
                    socketProcessor.start();
                    handlerOldSocket();
                    socketProcessorList.add(socketProcessor);

                }
                catch (Exception e)
                {
                    e.printStackTrace();
                }

            }

        }

        /**
         * 处理已有的连接
         */
        public void handlerOldSocket()
        {
            if (socketProcessorList.isEmpty())
            {
                return;
            }
            int size = socketProcessorList.size();
            for (int i = size - 1; i >= 0; i--)
            {
                TCPConnector socketProcessor = socketProcessorList.remove(i);
                socketProcessor.stop();
            }
        }

    }

    /**
     * 设置socket选项
     */
    public void setSocketOptions()
    {
        try
        {
            if (!socket.getTcpNoDelay())
            {
                socket.setTcpNoDelay(true);
            }

            socket.setSoLinger(true, 3);
            socket.setReceiveBufferSize(8*1024);
            socket.setSendBufferSize(8*1024);
            socket.setKeepAlive(true);
            socket.setReuseAddress(true);
        }
        catch (SocketException e)
        {
            e.printStackTrace();
        }

    }

    @Override
    public void responseOneMessage(String message)
    {
        if(socketProcessor!=null){
            socketProcessor.responseOneMessage(message);
        }
        
    }

    @Override
    public void setWrapper(Wrapper wrapper)
    {
        this.wrapper=wrapper;
    }

}
