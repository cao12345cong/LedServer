package com.clt.handler;

import java.io.IOException;
import java.io.OutputStream;

import android.util.Log;

import com.clt.commondata.SenderInfo;
import com.clt.operation.SenderOperation;
import com.clt.operation.SoDetectSender;
import com.clt.operation.SoGetRTC;
import com.clt.operation.SoSetTestMode;
import com.clt.operation.SoShowOnOff;
import com.clt.service.CommandExcutorImpl;
import com.clt.util.FileLogger;
import com.clt.util.Tools;

public class HandlerGetRTC extends SenderHandler
{
    private SoGetRTC senderOperation;

    public HandlerGetRTC(SenderOperation senderOperation,
            CommandExcutorImpl soCommandExcutor)
    {
        super(senderOperation, soCommandExcutor);
        this.senderOperation = (SoGetRTC) senderOperation;
    }

    @Override
    public boolean doHandler()
    {

        byte time[] = doGetTime();
        if (time == null || time.length != 6)
        {
            return false;
        }
        Tools.changeSystemTime(time);
        return true;
    }

    /**
     * 获取RTC时间
     * @return
     */
    private byte [] doGetTime()
    {
        try
        {

            if (soCommandExcutor.getmOutputStream() == null)
            {
                return null;
            }

            byte [] buffer = new byte [14];
            buffer[0] = (byte) 0x27;
            buffer[1] = (byte) 0x02;//

            // spi包装
            byte [] spibuffer = new byte [19];
            spibuffer[0] = (byte) 0xcc;
            spibuffer[1] = (byte) 0x05;
            spibuffer[2] = (byte) 0x00;
            spibuffer[3] = (byte) 0x0e;
            for (int j = 0; j < 14; j++)
            {
                spibuffer[4 + j] = buffer[j];
            }

            soCommandExcutor.setRcvBufLen(0);
            soCommandExcutor.setBatchRead(true);
            soCommandExcutor.setBatchCount(9);

            soCommandExcutor.getmOutputStream().write(spibuffer, 0, 19);
            soCommandExcutor.getmOutputStream().flush();
            // 获取返回给发送卡的信息
            byte [] uid = null;
            for (int i = 0; i < 10; i++)
            {
                Thread.sleep(1000);

                if (soCommandExcutor.getRcvBufLen() >= soCommandExcutor
                        .getBatchCount())
                {
                    byte [] rcvBuffer = soCommandExcutor.getRcvBuffer();
                    uid = new byte [6];
                    for (int z = 0; z < 6; z++)
                    {
                        uid[z] = rcvBuffer[6 + z];
                    }
                    soCommandExcutor.clearRcvBuffer();
                    // 报告未探测到的信息
                    break;

                }

            }
            soCommandExcutor.setRcvBufLen(0);
            soCommandExcutor.setBatchRead(false);
            soCommandExcutor.setBatchCount(0);
            return uid;

        }
        catch (Exception e)
        {
            e.printStackTrace();
            soCommandExcutor.setRcvBufLen(0);
            soCommandExcutor.setBatchRead(false);
            soCommandExcutor.setBatchCount(0);

            return null;
        }

    }
}
