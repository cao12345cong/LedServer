package com.clt.handler;

import java.io.IOException;
import java.io.OutputStream;

import android.util.Log;

import com.clt.commondata.PortArea;
import com.clt.commondata.SenderInfo;
import com.clt.commondata.SenderParameters;
import com.clt.entity.EdidInfo;
import com.clt.operation.SenderOperation;
import com.clt.operation.SoDetectSender;
import com.clt.operation.SoSetEDID;
import com.clt.operation.SoSetTestMode;
import com.clt.operation.SoShowOnOff;
import com.clt.service.CommandExcutorImpl;
import com.clt.util.FileLogger;

public class HandlerSetEDID extends SenderHandler
{
    private SoSetEDID senderOperation;

    public HandlerSetEDID(SenderOperation senderOperation,
            CommandExcutorImpl soCommandExcutor)
    {
        super(senderOperation, soCommandExcutor);
        this.senderOperation = (SoSetEDID) senderOperation;
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
            // 第一步，擦除
            boolean bOk = do_sender_flash_clear(0x07,
                    senderOperation.getSenderIndex());
            if (!bOk)
            {

                return false;
            }

            byte [] buffer1 = new byte [256];

            for (int i = 0; i < 256; i++)
            {
                buffer1[i] = (byte) 0x00;
            }
            eDIDToFlashBuffer(senderOperation, buffer1);
            // 第二步，发送卡网口一输出面积写入
            bOk = do_sender_flash_write_EDID(0x07, 0,
                    senderOperation.getSenderIndex(), buffer1);
            if (!bOk)
            {

                return false;
            }

            // 第四步，加载
            bOk = do_sender_flash_load(senderOperation.getSenderIndex());
            if (!bOk)
            {

                return false;
            }

            return true;

        }
        catch (Exception e)
        {

            return false;
        }

    }

    /**
     * 擦除
     * 
     * @param sectorIndex
     * @param scIndex
     *            area 0x030000~ 0x03FFFF 64KB 亮度、色温、对比度定义 0x040000~ 0x04FFFF
     *            64KB 0x050000~ 0x05FFFF 64KB 基本参数及网口控制区域 0x060000~ 0x06FFFF
     *            64KB 0x070000~ 0x07FFFF 64KB EDID信息存储
     * 
     * @return
     */
    public boolean do_sender_flash_clear(int sectorIndex, int scIndex)
    {
        try
        {
            byte [] buffer = new byte [262];
            buffer[0] = (byte) 0xaa;
            buffer[1] = (byte) 0x23;// 操作码
            buffer[2] = (byte) (sectorIndex + ((scIndex << 4) & 0xF0));// 操作地址

            // for (int i = 3; i < 262; i++)
            // {
            // buffer[i] = (byte) 0x00;
            // }

            soCommandExcutor.setRcvBufLen(0);
            soCommandExcutor.setBatchRead(true);
            soCommandExcutor.setBatchCount(5);

            soCommandExcutor.getmOutputStream().write(buffer, 0, 262);
            soCommandExcutor.getmOutputStream().flush();

            boolean bOk = false;
            for (int i = 0; i < 100; i++)
            {

                Thread.sleep(100);

                if (soCommandExcutor.getRcvBufLen() >= soCommandExcutor
                        .getBatchCount())
                {
                    byte [] rcvBuffer = soCommandExcutor.getRcvBuffer();

                    int succeededFlag = rcvBuffer[1] & ((byte) 0x0f);
                    int senderIndex = (rcvBuffer[1] & ((byte) 0xf0)) >> 4;

                    if (succeededFlag == 0x03 && senderIndex == scIndex)
                    {
                        bOk = true;
                        soCommandExcutor.clearRcvBuffer();
                        break;
                    }
                }

            }

            soCommandExcutor.setRcvBufLen(0);
            soCommandExcutor.setBatchRead(false);
            soCommandExcutor.setBatchCount(0);

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

    /**
     * 写入
     * 
     * @param sectorIndex
     * @param startPos
     * @param scIndex
     * @param wirteBuf
     * @return
     */
    public boolean do_sender_flash_write(int sectorIndex, int startPos,
            int scIndex, byte [] wirteBuf)
    {
        try
        {
            byte [] buffer = new byte [262];

            // for (int i = 1; i < 262; i++)
            // {
            // buffer[i] = (byte) 0x00;
            // }

            buffer[0] = (byte) 0xaa;
            buffer[1] = (byte) 0x85;
            buffer[2] = (byte) (sectorIndex + ((scIndex << 4) & 0xF0));
            buffer[3] = (byte) (startPos / 256);
            buffer[4] = (byte) 0x00;

            for (int i = 0; i < 256; i++)
            {
                buffer[i + 5] = wirteBuf[i];
            }

            soCommandExcutor.setRcvBufLen(0);
            soCommandExcutor.setBatchRead(true);
            soCommandExcutor.setBatchCount(5);

            soCommandExcutor.getmOutputStream().write(buffer, 0, 262);
            soCommandExcutor.getmOutputStream().flush();

            boolean bOk = false;
            for (int i = 0; i < 100; i++)
            {
                Thread.sleep(100);
                if (soCommandExcutor.getRcvBufLen() >= soCommandExcutor
                        .getBatchCount())
                {
                    byte [] rcvBuffer = soCommandExcutor.getRcvBuffer();

                    int succeededFlag = rcvBuffer[1] & ((byte) 0x0f);
                    int senderIndex = (rcvBuffer[1] & ((byte) 0xf0)) >> 4;

                    if (succeededFlag == 0x03 && senderIndex == scIndex)
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
        catch (Exception e)
        {

            soCommandExcutor.setRcvBufLen(0);
            soCommandExcutor.setBatchRead(false);
            soCommandExcutor.setBatchCount(0);

            return false;
        }
    }

    /**
     * 保存EDID信息到256个字节数组
     * 
     * @param soSetEDID
     * @param buffer1
     */
    public void eDIDToFlashBuffer(SoSetEDID soSetEDID, byte [] buffer1)
    {
        int width = soSetEDID.getWidth();
        int height = soSetEDID.getHeight();
        buffer1[0] = 1;
        buffer1[1] = (byte) (width / 256);
        buffer1[2] = (byte) (width % 256);
        buffer1[3] = (byte) (height / 256);
        buffer1[4] = (byte) (height % 256);
        buffer1[5] = 0;

        EdidInfo edidInfo = new EdidInfo();
        edidInfo.changeWH(width, height, soSetEDID.getFreq());
        byte [] edidInfoArr = edidInfo.getByteArr();

        for (int i = 0; i < 128; i++)
        {
            buffer1[128 + i] = edidInfoArr[i];
        }
    }

    /**
     * 加载
     * 
     * @param scIndex
     * @return
     */
    public boolean do_sender_flash_load(int scIndex)
    {
        try
        {
            byte [] buffer = new byte [262];

            // for (int i = 1; i < 262; i++)
            // {
            // buffer[i] = (byte) 0x00;
            // }

            buffer[0] = (byte) 0xaa;
            buffer[1] = (byte) 0x44;
            buffer[2] = (byte) (0x06 + ((scIndex << 4) & 0xF0));
            buffer[3] = (byte) (0x00);
            buffer[4] = (byte) 0x00;

            soCommandExcutor.setRcvBufLen(0);
            soCommandExcutor.setBatchRead(true);
            soCommandExcutor.setBatchCount(5);

            soCommandExcutor.getmOutputStream().write(buffer, 0, 262);
            soCommandExcutor.getmOutputStream().flush();

            boolean bOk = false;
            for (int i = 0; i < 100; i++)
            {

                Thread.sleep(100);

                if (soCommandExcutor.getRcvBufLen() >= soCommandExcutor
                        .getBatchCount())
                {
                    byte [] rcvBuffer = soCommandExcutor.getRcvBuffer();

                    int succeededFlag = rcvBuffer[1] & ((byte) 0x0f);
                    int senderIndex = (rcvBuffer[1] & ((byte) 0xf0)) >> 4;

                    if (succeededFlag == 0x03 && senderIndex == scIndex)
                    {
                        bOk = true;
                        soCommandExcutor.clearRcvBuffer();
                        break;
                    }
                }

            }

            soCommandExcutor.setRcvBufLen(0);
            soCommandExcutor.setBatchRead(false);
            soCommandExcutor.setBatchCount(0);

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

    public boolean do_sender_flash_write_EDID(int sectorIndex, int startPos,
            int scIndex, byte [] wirteBuf)
    {

        try
        {
            byte [] buffer = new byte [262];

            // for (int i = 1; i < 262; i++)
            // {
            // buffer[i] = (byte) 0x00;
            // }

            buffer[0] = (byte) 0xaa;
            buffer[1] = (byte) 0x85;
            buffer[2] = (byte) (sectorIndex + ((scIndex << 4) & 0xF0));
            buffer[3] = (byte) (startPos / 256);
            buffer[4] = (byte) 0x00;

            for (int i = 0; i < 256; i++)
            {
                buffer[i + 5] = wirteBuf[i];
            }

            soCommandExcutor.setRcvBufLen(0);
            soCommandExcutor.setBatchRead(true);
            soCommandExcutor.setBatchCount(5);

            soCommandExcutor.getmOutputStream().write(buffer, 0, 262);
            soCommandExcutor.getmOutputStream().flush();

            boolean bOk = false;
            for (int i = 0; i < 100; i++)
            {

                Thread.sleep(100);

                if (soCommandExcutor.getRcvBufLen() >= soCommandExcutor
                        .getBatchCount())
                {
                    byte [] rcvBuffer = soCommandExcutor.getRcvBuffer();

                    int succeededFlag = rcvBuffer[1] & ((byte) 0x0f);
                    int senderIndex = (rcvBuffer[1] & ((byte) 0xf0)) >> 4;

                    if (succeededFlag == 0x03 && senderIndex == scIndex)
                    {
                        bOk = true;
                        soCommandExcutor.clearRcvBuffer();
                        break;
                    }
                }

            }

            soCommandExcutor.setRcvBufLen(0);
            soCommandExcutor.setBatchRead(false);
            soCommandExcutor.setBatchCount(0);

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
