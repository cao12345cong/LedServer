package com.clt.handler;

import java.io.IOException;

import com.clt.commondata.PortArea;
import com.clt.commondata.SenderParameters;
import com.clt.operation.SenderOperation;
import com.clt.operation.SoSetAutoBrightness;
import com.clt.operation.SoSetBasicParameters;
import com.clt.service.CommandExcutorImpl;

/**
 * 发送卡基本参数
 *
 */
public class HandlerSetAutoBrightness extends SenderHandler
{
    private SoSetAutoBrightness senderOperation;

    public HandlerSetAutoBrightness(SenderOperation senderOperation,
            CommandExcutorImpl soCommandExcutor)
    {
        super(senderOperation, soCommandExcutor);
        this.senderOperation = (SoSetAutoBrightness) senderOperation;
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
            // 1.擦除，亮度调节映射表
            boolean bOk = doSendeFlashClearAutoBrightness(0xf4);
            if (!bOk)
            {

                return false;
            }
            Thread.sleep(1000);

            // 2.写，亮度调节映射表
            bOk = doSenderFlashWriteAutoBrightness(0xf4,
                    senderOperation.getBuffer(), senderOperation.getIsAuto());
            if (!bOk)
            {

                return false;
            }
            Thread.sleep(100);
            // 加载1次

            bOk = doSenderFlashLoadAutoBrightness();
            if (!bOk)
            {

                return false;
            }
            Thread.sleep(100);
            // 1.读
            bOk = doSendeFlashReadAutoBrightness();
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
     * 擦除取armflash数据
     * @return
     */
    public boolean doSendeFlashClearAutoBrightness(int address)
    {
        try
        {
            byte [] buffer = new byte [262];
            buffer[0] = (byte) 0xaa;
            buffer[1] = (byte) 0x30;// 操作码
            buffer[2] = (byte) 0x00;// 操作地址
            buffer[3] = (byte) address;// 操作地址
            buffer[4] = (byte) 0x00;// 操作地址

            for (int i = 5; i < 262; i++)
            {
                buffer[i] = (byte) 0x00;
            }

            // 转发spi接口帧
            byte [] spiBuffer = new byte [267];
            spiBuffer[0] = (byte) 0xcc;
            spiBuffer[1] = (byte) 0x05;
            spiBuffer[2] = (byte) (262 / 256);// 帧长，高位在前
            spiBuffer[3] = (byte) (262 % 256);
            for (int i = 0; i < 262; i++)
            {
                spiBuffer[i + 4] = buffer[i];
            }
            soCommandExcutor.getmOutputStream().write(spiBuffer, 0, 267);
            soCommandExcutor.getmOutputStream().flush();
        }
        catch (IOException e)
        {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    /**
     * 写入armflash数据
     * 
     * @param sectorIndex
     * @param startPos
     * @param scIndex
     * @param wirteBuf
     * @param isAuto 使能标志位
     * @return
     */
    public boolean doSenderFlashWriteAutoBrightness(int address,
            byte [] brightBuffer, int isAuto)
    {
        try
        {
            byte [] buffer = new byte [262];

            for (int i = 1; i < 262; i++)
            {
                buffer[i] = (byte) 0x00;
            }

            buffer[0] = (byte) 0xaa;
            buffer[1] = (byte) 0x32;
            buffer[2] = (byte) 0x00;
            buffer[3] = (byte) address;
            buffer[4] = (byte) 0x00;

            for (int i = 0; i < 128; i++)
            {
                buffer[i + 5] = brightBuffer[i];// 亮度值
            }
            buffer[5 + 128] = (byte) isAuto;// 使能标志位

            // 转发spi接口帧
            byte [] spiBuffer = new byte [267];
            spiBuffer[0] = (byte) 0xcc;
            spiBuffer[1] = (byte) 0x05;
            spiBuffer[2] = (byte) (262 / 256);// 帧长，高位在前
            spiBuffer[3] = (byte) (262 % 256);
            for (int i = 0; i < 262; i++)
            {
                spiBuffer[i + 4] = buffer[i];
            }
            soCommandExcutor.getmOutputStream().write(spiBuffer, 0, 267);
            soCommandExcutor.getmOutputStream().flush();
        }
        catch (IOException e)
        {

            e.printStackTrace();
            return false;
        }

        return true;
    }

    /**
     * 加载armflash数据
     * 
     * @param sectorIndex
     * @param startPos
     * @param scIndex
     * @param wirteBuf
     * @return
     */
    public boolean doSenderFlashLoadAutoBrightness()
    {
        try
        {
            byte [] buffer = new byte [262];
            for (int i = 1; i < 262; i++)
            {
                buffer[i] = (byte) 0x00;
            }
            buffer[0] = (byte) 0xaa;
            buffer[1] = (byte) 0x77;
            buffer[2] = (byte) 0x00;
            buffer[3] = (byte) 0xf4;
            buffer[4] = (byte) 0x00;
            buffer[5] = (byte) 0x03;

            for (int i = 6; i < 262; i++)
            {
                buffer[i] = (byte) 0x00;
            }

            // 转发spi接口帧
            byte [] spiBuffer = new byte [267];
            spiBuffer[0] = (byte) 0xcc;
            spiBuffer[1] = (byte) 0x05;
            spiBuffer[2] = (byte) (262 / 256);// 帧长，高位在前
            spiBuffer[3] = (byte) (262 % 256);
            for (int i = 0; i < 262; i++)
            {
                spiBuffer[i + 4] = buffer[i];
            }
            soCommandExcutor.getmOutputStream().write(spiBuffer, 0, 267);
            soCommandExcutor.getmOutputStream().flush();
        }
        catch (IOException e)
        {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    /**
     * 读取armflash数据
     * @return
     */
    public boolean doSendeFlashReadAutoBrightness()
    {
        try
        {
            // 操作ARM FLASH协议帧格式
            byte [] buffer = new byte [262];
            buffer[0] = (byte) 0xaa;
            buffer[1] = (byte) 0x31;// 操作码 读
            buffer[2] = (byte) 0x00;// 操作地址
            buffer[3] = (byte) 0xf4;// 操作地址
            buffer[4] = (byte) 0x00;// 操作地址

            // for (int i = 5; i < 262; i++)
            // {
            // buffer[i] = (byte) 0x00;
            // }
            // 转发spi接口帧
            byte [] spiBuffer = new byte [267];
            spiBuffer[0] = (byte) 0xcc;
            spiBuffer[1] = (byte) 0x05;
            spiBuffer[2] = (byte) (262 / 256);// 帧长，高位在前
            spiBuffer[3] = (byte) (262 % 256);
            for (int i = 0; i < 262; i++)
            {
                spiBuffer[i + 4] = buffer[i];
            }

            soCommandExcutor.setRcvBufLen(0);
            soCommandExcutor.setBatchRead(true);
            soCommandExcutor.setBatchCount(262);

            soCommandExcutor.getmOutputStream().write(spiBuffer, 0, 267);
            soCommandExcutor.getmOutputStream().flush();

            // 等待回复
            boolean bOk = false;
            for (int i = 0; i < 100; i++)
            {

                Thread.sleep(100);

                if (soCommandExcutor.getRcvBufLen() >= soCommandExcutor
                        .getBatchCount())
                {
                    byte [] rcvBuffer = soCommandExcutor.getRcvBuffer();
                    bOk = true;
                    soCommandExcutor.clearRcvBuffer();
                    break;

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
