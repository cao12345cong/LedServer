package com.clt.handler;

import java.io.IOException;
import java.io.OutputStream;

import android.util.Log;

import com.clt.commondata.SenderInfo;
import com.clt.operation.SenderOperation;
import com.clt.operation.SoDetectSender;
import com.clt.service.CommandExcutor;
import com.clt.util.FileLogger;

public class HandlerDetectReceiver extends SenderHandler
{
    SenderOperation senderOperation;

    public HandlerDetectReceiver(SenderOperation senderOperation,
            CommandExcutor soCommandExcutor)
    {
        super(senderOperation, soCommandExcutor);
        this.senderOperation = senderOperation;
    }

    @Override
    public boolean doHandler()
    {
        try
        {
            if (soCommandExcutor.getmOutputStream() == null)
            {
                return false;
            }

            byte [] buffer = new byte [262];
            buffer[0] = (byte) 0xaa;
            buffer[1] = (byte) 0x44;

            // 上位机发送262个字节给发送卡
            soCommandExcutor.setRcvBufLen(0);
            soCommandExcutor.setBatchRead(true);
            soCommandExcutor.setBatchCount(160);

            soCommandExcutor.getmOutputStream().write(buffer, 0, 262);
            soCommandExcutor.getmOutputStream().flush();
            
            boolean bOk = false;
            // 获取返回给发送卡的信息
            for (int i = 0; i < 100; i++)
            {

                Thread.sleep(100);

                if (soCommandExcutor.getRcvBufLen() >= soCommandExcutor
                        .getBatchCount())
                {
                    bOk=true;
                    byte [] sss = soCommandExcutor.getRcvBuffer();
                    soCommandExcutor.senderInfo = new SenderInfo();
                    // 从一堆字节数据中获取有用的信息
                    soCommandExcutor.senderInfo.LoadFromBuffer(
                            soCommandExcutor.getRcvBuffer(),
                            soCommandExcutor.getRcvBufLen());
                    soCommandExcutor.clearRcvBuffer();
                    break;
                }

            }
            soCommandExcutor.setRcvBufLen(0);
            soCommandExcutor.setBatchRead(false);
            soCommandExcutor.setBatchCount(0);
            // 报告未探测到的信息
            return bOk;
        }
        catch (Exception e)
        {

            soCommandExcutor.setRcvBufLen(0);
            soCommandExcutor.setBatchRead(false);
            soCommandExcutor.setBatchCount(0);
            return false;
        }

    }

}
