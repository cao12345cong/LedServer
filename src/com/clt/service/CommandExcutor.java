package com.clt.service;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import android.content.Context;
import android.content.Intent;
import android_serialport_api.SerialPort;

import com.clt.SerialPortConnector;
import com.clt.Wrapper;
import com.clt.commondata.SenderInfo;
import com.clt.netmessage.NMBase;
import com.clt.operation.SenderOperation;
import com.clt.util.FileLogger;
import com.google.gson.Gson;

/**
 * 1.消息的轮询，2.获得串口返回消息
 *
 */
public abstract class CommandExcutor implements SerialPortConnector
{

    protected SerialPort mSerialPort;
    
    protected SenderOperation senderOperation; // 当前的操作

    public SenderInfo senderInfo;

    protected Context context;

    protected OutputStream mOutputStream = null;

    protected InputStream mInputStream = null;

    protected ArrayList<SenderOperation> operaionList = new ArrayList<SenderOperation>();// 操作指令队列

    protected byte [] rcvBuffer = new byte [2560];

    protected int rcvBufLen = 0;// 收到的字符串长度

    protected int batchCount = 0;

    protected boolean batchRead = false;

    protected int batchWaitTime = 0;

    private ReadThread readThread;// 读取串口返回信息

    private AcceptCommandThread acceptCommandThread;// 接收操作指令此线程

    private ExecutorService executors;// 线程池，异步处理命令

    private int clientType=NMBase.WIFI;//新加的命令的类型
    
    private int currentClientType=NMBase.WIFI;//当前处理的命令的类型
    
    private Wrapper wrapper;

    
    public CommandExcutor(Context context)
    {
        try
        {
            this.context = context;
            this.executors = Executors.newFixedThreadPool(3);
            
            // 初始化串口
            mSerialPort = new SerialPort(new File("/dev/ttyS1"), 115200);
            mOutputStream = mSerialPort.getOutputStream();
            mInputStream = mSerialPort.getInputStream();
        }
        catch (Exception e)
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    /**
     * 开始
     */
    public void start()
    {
        readThread = new ReadThread();
        readThread.start();

        acceptCommandThread = new AcceptCommandThread();
        acceptCommandThread.start();

    }

    /**
     * 结束
     */
    public void stop()
    {
        readThread.cancel();
        acceptCommandThread.cancel();
        if (mOutputStream != null)
        {
            try
            {
                mOutputStream.close();
            }
            catch (IOException e)
            {
                e.printStackTrace();
            }
        }
        if (mInputStream != null)
        {
            try
            {
                mInputStream.close();
            }
            catch (IOException e)
            {
                e.printStackTrace();
            }
        }
    }

    /**
     * 当前是否有程序在运行
     * @return
     */
    public boolean canExcuteRightNow(){
        return senderOperation==null&&operaionList.isEmpty();
    }
    /**
     * 添加一个操作
     * @param so_op
     */
    public void addOperation(SenderOperation so)
    {
        synchronized (operaionList)
        {
            so.setClientType(clientType);
            operaionList.add(so);
            operaionList.notifyAll();
        }
    }

    public byte [] getRcvBuffer()
    {
        return rcvBuffer;
    }

    public void setRcvBuffer(byte [] rcvBuffer)
    {
        this.rcvBuffer = rcvBuffer;
    }

    public void clearRcvBuffer()
    {
        this.rcvBuffer = null;
        this.rcvBuffer = new byte [2560];
    }

    public int getRcvBufLen()
    {
        return rcvBufLen;
    }

    public void setRcvBufLen(int rcvBufLen)
    {
        this.rcvBufLen = rcvBufLen;
    }

    public int getBatchCount()
    {
        return batchCount;
    }

    public void setBatchCount(int batchCount)
    {
        this.batchCount = batchCount;
    }

    public boolean isBatchRead()
    {
        return batchRead;
    }

    public void setBatchRead(boolean batchRead)
    {
        this.batchRead = batchRead;
    }

    public SenderInfo getSenderInfo()
    {
        return senderInfo;
    }

    public OutputStream getmOutputStream()
    {
        return mOutputStream;
    }

    public void setmOutputStream(OutputStream mOutputStream)
    {
        this.mOutputStream = mOutputStream;
    }

    public InputStream getmInputStream()
    {
        return mInputStream;
    }

    public void setmInputStream(InputStream mInputStream)
    {
        this.mInputStream = mInputStream;
    }

    public int getCurrentClientType()
    {
        return clientType;
    }

    public void setCurrentClientType(int clientType)
    {
        this.clientType = clientType;
    }
    
    @Override
    public void setWrapper(Wrapper wrapper)
    {
        this.wrapper=wrapper;
    }
//    /**
//     * 发送处理结果给客户端
//     * @param strNetMessage
//     */
//    protected void sendNetMessage(String strNetMessage)
//    {
//        Intent intent=null;
//        if(currentClientType==NMBase.WIFI){
//            intent = new Intent(context,TcpWifiServie.class);
//            intent.putExtra("netMessage", strNetMessage);
//            context.startService(intent);
//          //TcpWifiServie.startService(context, intent);
//        }
////        else if(currentClientType==NMBase.Ethernet){
////            intent = new Intent(context,TcpEthernetServie.class);
////            intent.putExtra("netMessage", strNetMessage);
////            context.startService(intent);
////            //TcpEthernetServie.startService(context, intent);
////            
////        }
//        
//        
//    }

    /**
     * 响应给客户端
     * @param nmBase
     */
    protected void reponseToClient(NMBase nmBase)
    {
        Gson gson = new Gson();
        String nmString = gson.toJson(nmBase);
        //sendNetMessage(nmString);
        wrapper.outputOneMessage(nmString);
        
    }

    /**
     * 清空数据
     */
    protected void clearData(){
        setBatchCount(0);
        setBatchRead(false);
        setRcvBufLen(0);
        clearRcvBuffer();
        
    }
    /****************************************内部类********************************************/
    /**
     * 获取串口返回的信息
     * @author Administrator
     *
     */
    private class ReadThread extends Thread
    {

        @Override
        public void run()
        {
            super.run();
            byte [] tempBuffer = new byte [2048];
            int size = 0;
            try
            {
                while (!isInterrupted())
                {
                    size = mInputStream.available();
                    if (size <= 0)
                    {
                        Thread.sleep(100);
                        continue;
                    }

                    if (batchRead)
                    {
                        size = mInputStream.read(rcvBuffer, rcvBufLen,
                                2560 - rcvBufLen);
                        rcvBufLen += size;
                        if (rcvBufLen >= batchCount)
                        {
                            batchRead = false;
                        }

                    }
                    else
                    {
                        mInputStream.read(tempBuffer);
                    }

                }
            }
            catch (IOException e)
            {
                e.printStackTrace();
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }

        }// run结束

        public void cancel()
        {
            interrupt();
        }
    }

    /**
     * 接收处理命令线程
     * @author Administrator
     *
     */
    class AcceptCommandThread extends Thread
    {
        @Override
        public void run()
        {
            super.run();

            try
            {
                while (!isInterrupted())
                {
                    if (operaionList.isEmpty())
                    {
                        synchronized (operaionList)
                        {
                            try
                            {
                                operaionList.wait();
                            }
                            catch (InterruptedException e)
                            {
                                e.printStackTrace();
                            }
                        }
                    }

                    if (senderOperation == null)
                    {
                        synchronized (operaionList)
                        {
                            int size = operaionList.size();
                            if (size > 0)
                            {
                                senderOperation = operaionList.remove(0);
                            }
                        }
                        executors.execute(new Runnable()
                        {

                            @Override
                            public void run()
                            {
                                try
                                {
                                    FileLogger
                                            .getInstance()
                                            .writeMessageToFile(
                                                    "处理命令"
                                                            + senderOperation
                                                                    .getOptertorType());
                                    //当前处理的命令类型
                                    currentClientType=senderOperation.getClientType();
                                    excuteCommandWrapper(senderOperation);

                                }
                                catch (Exception e)
                                {
                                    FileLogger.getInstance()
                                            .writeMessageToFile(
                                                    "ExcuteCommand退出");
                                }

                            }
                        });

                    }

                    if (senderOperation.isbFinished())
                    {
                        senderOperation = null;
                    }

                }// while结束
            }
            catch (Exception e)
            {
                FileLogger.getInstance().writeMessageToFile(
                        "AcceptCommandThread异常" + e.getMessage());
                e.printStackTrace();
            }
            finally
            {
                FileLogger.getInstance().writeMessageToFile(
                        "AcceptCommandThread退出");
            }
        }// run结束

        public void cancel()
        {
            interrupt();
        }
    }

   private boolean excuteCommandWrapper(SenderOperation senderOperation){
       senderOperation.setbFinished(false);
       boolean bOK = excuteCommand(senderOperation);
       senderOperation.setbFinished(true);
       return bOK;
   }
    
    /********************************************************************/

    
    public abstract boolean excuteCommand(SenderOperation senderOperation);

}
