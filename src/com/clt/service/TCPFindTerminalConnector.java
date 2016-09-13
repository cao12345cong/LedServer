package com.clt.service;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;

import android.content.Context;

public class TCPFindTerminalConnector
{
    private ServerSocket mServerSocket;
    private ArrayList<SocketProcessor> socketProcessorList;
    private Context context;
    
    public TCPFindTerminalConnector(Context context)
	{
    	this.context=context;
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

                    Socket socket = mServerSocket.accept();
                    // 如果集合中已经有了该连接，不再向下面执行
                    SocketProcessor socketProcessor = new SocketProcessor(socket,context);
                    socketProcessor.start();
                }
                catch (IOException e)
                {
                    e.printStackTrace();
                }

            }

        }

    }
    
    public void start()
    {
        try
        {
            mServerSocket = new ServerSocket(9043);
            AccpetThread mAccpetThread = new AccpetThread();
            mAccpetThread.setName("ListeningThread");
            mAccpetThread.start();
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
        
    }
}
