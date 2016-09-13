package com.clt.service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.ArrayList;

import android.content.Context;

import com.clt.ledservers.R;
import com.clt.netmessage.NMBase;
import com.clt.netmessage.NMFindTerminateAnswer;
import com.clt.util.SharedPreferenceUtil;
import com.clt.util.SharedPreferenceUtil.ShareKey;
import com.google.gson.Gson;

/**
 * 负责socket的IO的读写，以及socket的关闭
 *
 */
public class SocketProcessor
{

    private volatile ArrayList<String> list = new ArrayList<String>();

    private InputHandler inputHandler;

    private OutputHandler outputHandler;

    private volatile Socket socket;

    private static String CLOSE = "close";

    private SharedPreferenceUtil sharedPreferenceUtil;
    
    private Context context;

    public SocketProcessor(Socket socket,Context context)
    {
        this.socket = socket;
        this.context=context;
        sharedPreferenceUtil=SharedPreferenceUtil.getInstance(context);
    }

    /**
     * 开始处理
     */
    public void start()
    {
        try
        {
            inputHandler = new InputHandler(socket.getInputStream());
            outputHandler = new OutputHandler(socket.getOutputStream());
            inputHandler.start();
            outputHandler.start();

        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
    }

    /**
     * 停止处理
     */
    public void stop()
    {
        try
        {
            inputHandler.cancel();
            outputHandler.cancel();
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

        InputStream in;

        InputStreamReader isr;

        String readMessage;

        boolean running = true;

        public InputHandler(InputStream in)
        {
            input = new BufferedReader(new InputStreamReader(in));
            this.in = in;
            this.isr = new InputStreamReader(in);
        }

        @Override
        public void run()
        {

            try
            {
                while (running)
                {
                    String messge = input.readLine();
                    Gson gson = new Gson();
                    NMBase base = gson.fromJson(messge, NMBase.class);

                    int nmType = base.getmType();
                    String rvsMessage = null;
                    switch (base.getmType())
                    {
                        case 71:{
                        	// 整理要发送的消息
                            String mTerminateName = sharedPreferenceUtil.getString(ShareKey.TerminateName,
                                    context.getString(R.string.screen_name_default_val));
                            String mTerminatePassword = sharedPreferenceUtil.getString(ShareKey.TerminatePassword,
                                    context.getString(R.string.screen_password_default_val));

                            NMFindTerminateAnswer findTerminateAnswer = new NMFindTerminateAnswer();
                            findTerminateAnswer.setTerminateName(mTerminateName);
                            findTerminateAnswer.setPassword(mTerminatePassword);

                            Gson gson2 = new Gson();
                            rvsMessage = gson2.toJson(findTerminateAnswer);
                        }
                        break;
                    }

                    outputHandler.pushString(rvsMessage);

                }
            }
            catch (Exception e)
            {
            	e.printStackTrace();
            }

        }

        public void cancel() throws IOException
        {
            socket.shutdownInput();
            running = false;
        }
    }

    /**
     * 处理写IO
     * @author Administrator
     *
     */
    class OutputHandler extends Thread
    {
        OutputStream os;

        PrintWriter pw;

        private boolean running = true;

        public OutputHandler(OutputStream os)
        {
            this.os = os;
            pw = new PrintWriter(new OutputStreamWriter(os));

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
                        message = list.remove(0);
                    }
                    if(message!=null){
                        
                        this.pw.println(message);
                        this.pw.flush();
                    }
                   
                }
            }
            catch (Exception e)
            {

            }
            finally
            {
                if (os != null)
                {
                    try
                    {
                        os.close();
                    }
                    catch (IOException e)
                    {
                        e.printStackTrace();
                    }
                }
                if (socket != null)
                {
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

        public void cancel() throws IOException
        {
            list.clear();
            pushString(CLOSE);
        }
    }

    /**
     * 向消息队列中添加一条消息
     * @param message
     */
    public void addOneMessage(String message)
    {
        outputHandler.pushString(message);
    }


}
