package com.clt.service;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import android.app.Notification;
import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;

import com.clt.util.Config;

/**
 * 上传功能的服务
 *
 */
public class UploadService extends Service
{
    private String savePath = "/";// 文件保存路径

    private ServerSocket serverSocket;

    private ExecutorService executorService;

    private boolean quit = false;

    private final IBinder mBinder = new LocalBinder();

    public class LocalBinder extends Binder
    {
        public UploadService getService()
        {
            return UploadService.this;
        }
    }

    @Override
    public IBinder onBind(Intent intent)
    {
        return mBinder;
    }

    @Override
    public void onDestroy()
    {
        super.onDestroy();
    }

    @Override
    public void onCreate()
    {
        super.onCreate();
        executorService = Executors.newFixedThreadPool(Runtime.getRuntime()
                .availableProcessors() * 50);
        ListenerThread listenerThread = new ListenerThread();
        listenerThread.start();
        
        Notification notification = new Notification();
        startForeground(1, notification);
    }

    /**
     * 监听
     */
    private class ListenerThread extends Thread
    {
        @Override
        public void run()
        {
            super.run();
            try
            {
                serverSocket = new ServerSocket(Config.UPLOAD_PORT);
                while (!isInterrupted())
                {

//                    Socket socket = serverSocket.accept();
//                    UploaderServer task = new UploaderServer(socket);
//                    task.execute();
                }
            }
            catch (IOException e)
            {
                e.printStackTrace();
            }
        }
    }

}
