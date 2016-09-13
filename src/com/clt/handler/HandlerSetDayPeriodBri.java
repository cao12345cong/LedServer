package com.clt.handler;

import java.io.IOException;
import java.io.OutputStream;
import java.util.LinkedHashMap;
import java.util.Map;

import android.util.Log;

import com.clt.commondata.SenderInfo;
import com.clt.operation.SenderOperation;
import com.clt.operation.SoDetectSender;
import com.clt.operation.SoSetDayPeriodBright;
import com.clt.operation.SoSetTestMode;
import com.clt.operation.SoShowOnOff;
import com.clt.service.CommandExcutorImpl;
import com.clt.util.CommonUtil;
import com.clt.util.FileLogger;

public class HandlerSetDayPeriodBri extends SenderHandler
{
    SoSetDayPeriodBright senderOperation;
    public HandlerSetDayPeriodBri(SenderOperation senderOperation,
            CommandExcutorImpl soCommandExcutor)
    {
        super(senderOperation, soCommandExcutor);
        this.senderOperation=(SoSetDayPeriodBright) senderOperation;
    }

    @Override
    public boolean doHandler()
    {
        senderOperation.setOperationStep(1);
        

        if (soCommandExcutor.getmOutputStream() == null)
            return false;
        // 第一步，擦除
        boolean bOk = do_sender_flash_clear_Day_priod_bright();
        if (!bOk)
            return false;

        byte [] buffer1 = new byte [256];

        for (int i = 0; i < 256; i++)
        {
            buffer1[i] = (byte) 0x00;
        }
        eDayPeriodBrightFlashBuffer(senderOperation, buffer1);
        // 第二步，发送卡网口一输出面积写入
        bOk = do_sender_flash_write_DayPeriodBright(
                senderOperation.getSenderIndex(), buffer1);
        if (!bOk)
            return false;
        // 第四步，加载
        // bOk = do_sender_flash_load(senderOperation.getSenderIndex());
        // if (!bOk)
        // return false;

        return true;
    
    }
    
    
    public void eDayPeriodBrightFlashBuffer(SoSetDayPeriodBright soSetdpb,
            byte [] buffer1)
    {
        LinkedHashMap<String, Integer> maps = soSetdpb.getMaps();
        buffer1[0] = 0;
        buffer1[1] = (byte) (maps.size());
        int i = 2;
        int value = 0;
        String key = null;
        for (Map.Entry<String, Integer> entry : maps.entrySet())
        {
            value = entry.getValue();// 亮度
            key = entry.getKey();
            buffer1[i++] = (byte) value;// 亮度
            String [] time = key.split(":");
            buffer1[i++] = CommonUtil.str2Bcd(time[0])[0];
            buffer1[i++] = CommonUtil.str2Bcd(time[1])[0];
        }

    }
    @Deprecated
    private boolean do_sender_flash_write_DayPeriodBright(int senderIndex,
            byte [] wirteBuf)
    {
        byte [] buffer = new byte [262];

        for (int i = 1; i < 262; i++)
        {
            buffer[i] = (byte) 0x00;
        }

        buffer[0] = (byte) 0xaa;
        buffer[1] = (byte) 0x77;
        buffer[2] = (byte) 0x00;
        buffer[3] = (byte) 0x26;
        buffer[4] = (byte) 0x00;

        for (int i = 0; i < 256; i++)
        {
            buffer[i + 5] = wirteBuf[i];
        }

        try
        {
            soCommandExcutor.setRcvBufLen(0);
            soCommandExcutor.setBatchRead(true);
            soCommandExcutor.setBatchCount(6);

            soCommandExcutor.getmOutputStream().write(buffer, 0, 262);
            soCommandExcutor.getmOutputStream().flush();
        }
        catch (IOException e)
        {

            e.printStackTrace();

            soCommandExcutor.setRcvBufLen(0);
            soCommandExcutor.setBatchRead(false);
            soCommandExcutor.setBatchCount(0);

            return false;
        }

        boolean bOk = false;
        for (int i = 0; i < 10; i++)
        {

            try
            {
                Thread.sleep(20);
            }
            catch (InterruptedException e)
            {

                e.printStackTrace();
            }

            if (soCommandExcutor.getRcvBufLen() >= soCommandExcutor.getBatchCount())
            {
                byte [] rcvBuffer = soCommandExcutor.getRcvBuffer();

                int succeededFlag = rcvBuffer[1] & ((byte) 0x0f);
                // int senderIndex = (rcvBuffer[1] & ((byte) 0xf0)) >> 4;

                if (succeededFlag == 0x03)
                {
                    bOk = true;
                    break;
                }
            }

        }

        soCommandExcutor.setRcvBufLen(0);
        soCommandExcutor.setBatchRead(false);
        soCommandExcutor.setBatchCount(0);

        return bOk;
    }
    @Deprecated
    public boolean do_sender_flash_clear_Day_priod_bright()
    {
        byte [] buffer = new byte [262];
        buffer[0] = (byte) 0xaa;
        buffer[1] = (byte) 0x67;// 操作码
        buffer[2] = 0;// 操作地址
        buffer[3] = 0x26;
        for (int i = 4; i < 262; i++)
        {
            buffer[i] = (byte) 0x00;
        }

        try
        {
            soCommandExcutor.setRcvBufLen(0);
            soCommandExcutor.setBatchRead(true);
            soCommandExcutor.setBatchCount(6);

            soCommandExcutor.getmOutputStream().write(buffer, 0, 262);
            soCommandExcutor.getmOutputStream().flush();
        }
        catch (IOException e)
        {
            e.printStackTrace();

            soCommandExcutor.setRcvBufLen(0);
            soCommandExcutor.setBatchRead(false);
            soCommandExcutor.setBatchCount(0);

            return false;
        }

        boolean bOk = false;
        for (int i = 0; i < 100; i++)
        {

            try
            {
                Thread.sleep(100);
            }
            catch (InterruptedException e)
            {
                e.printStackTrace();
            }

            if (soCommandExcutor.getRcvBufLen() >= soCommandExcutor.getBatchCount())
            {
                byte [] rcvBuffer = soCommandExcutor.getRcvBuffer();

                int succeededFlag = rcvBuffer[1] & ((byte) 0x0f);
                int senderIndex = (rcvBuffer[1] & ((byte) 0xf0)) >> 4;

                if (succeededFlag == 0x03)
                {
                    bOk = true;
                    break;
                }
            }

        }

        soCommandExcutor.setRcvBufLen(0);
        soCommandExcutor.setBatchRead(false);
        soCommandExcutor.setBatchCount(0);

        return bOk;
    }

}
