package com.clt.handler;

import java.io.IOException;

import com.clt.commondata.PortArea;
import com.clt.commondata.SenderParameters;
import com.clt.operation.SenderOperation;
import com.clt.operation.SoSetBasicParameters;
import com.clt.service.CommandExcutor;

/**
 * 发送卡基本参数
 *
 */
public class HandlerSetBasicParameters extends SenderHandler
{
    private SoSetBasicParameters senderOperation;

    public HandlerSetBasicParameters(SenderOperation senderOperation,
            CommandExcutor soCommandExcutor)
    {
        super(senderOperation, soCommandExcutor);
        this.senderOperation = (SoSetBasicParameters) senderOperation;
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
            boolean bOk = do_sender_flash_clear(0x05,
                    senderOperation.getSenderIndex());
            if (!bOk)
            {

                return false;
            }
            byte [] buffer1 = new byte [256];
            byte [] buffer2 = new byte [256];
            byte [] buffer3 = new byte [256];
            byte [] buffer4 = new byte [256];

            BasicParamToFlashBuffer(senderOperation.getSenderParameters(),
                    buffer1, buffer2, buffer3, buffer4);
            // 第二步，发送卡网口一输出面积写入
            bOk = do_sender_flash_write(0x05, 0,
                    senderOperation.getSenderIndex(), buffer1);
            if (!bOk)
            {

                return false;
            }

            // 第三步，发送卡网口二输出面积写入
            bOk = do_sender_flash_write(0x05, 4 * 256,
                    senderOperation.getSenderIndex(), buffer2);
            if (!bOk)
            {

                return false;
            }

            if (soCommandExcutor.senderInfo != null
                    && soCommandExcutor.senderInfo.getPortCount() > 2)
            {
                bOk = do_sender_flash_write(0x05, 5 * 256,
                        senderOperation.getSenderIndex(), buffer3);
                if (!bOk)
                {

                    return false;
                }

                bOk = do_sender_flash_write(0x05, 6 * 256,
                        senderOperation.getSenderIndex(), buffer4);
                if (!bOk)
                {

                    return false;
                }

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
            int bufferLen = 262;
            byte [] buffer = new byte [bufferLen];
            buffer[0] = (byte) 0xaa;
            buffer[1] = (byte) 0x23;// 操作码
            buffer[2] = (byte) (sectorIndex + ((scIndex << 4) & 0xF0));// 操作地址
            buffer[3] = (byte) 0x00;
            buffer[4] = (byte) 0x00;

            soCommandExcutor.setRcvBufLen(0);
            soCommandExcutor.setBatchRead(true);
            soCommandExcutor.setBatchCount(4);

            soCommandExcutor.getmOutputStream().write(buffer, 0, bufferLen);
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
     * 保存基本参数到发送卡flash
     * 
     * @param senderParam
     * @param buffer1
     * @param buffer2
     * @param buffer3
     * @param buffer4
     */
    private void BasicParamToFlashBuffer(SenderParameters senderParam,
            byte [] buffer1, byte [] buffer2, byte [] buffer3, byte [] buffer4)
    {
        buffer1[0] = (byte) 0x06;
        buffer1[1] = (byte) 0x33; // 5A卡
        buffer1[2] = (byte) 0x00; // 5A卡
        buffer1[3] = (byte) 0x00; // 5A卡
        buffer1[4] = (byte) 0x00; // 5A卡

        buffer1[5] = senderParam.isbBigPack() ? (byte) 0x01 : 0x00;
        buffer1[6] = senderParam.isbAutoBright() ? (byte) 0x01 : 0x00;
        buffer1[7] = (byte) senderParam.getM_frameRate();
        buffer1[8] = senderParam.isRealParamFlag() ? (byte) 0x01 : 0x00;
        buffer1[9] = senderParam.isRealParamFlag() ? (byte) 0x01 : 0x00;
        buffer1[10] = senderParam.isbZeroDelay() ? (byte) 0x01 : 0x00;// 0延迟
        int tenBitFlag = senderParam.getRgbBitsFlag();
        if (tenBitFlag == 1)
        {// 10bit
            buffer1[11] = (byte) 0x01;// 10bit
        }
        else if (tenBitFlag == 2)
        {
            buffer1[11] = (byte) 0x02;// 12bit
        }
        else
        {
            buffer1[11] = (byte) 0x00;// 8bit
        }

        buffer1[12] = senderParam.isbHDCP() ? (byte) 0x01 : 0x00;// dhcp
        int inputType = senderParam.getInputType();
        buffer1[13] = (byte) inputType;

        PortArea [] ports = senderParam.getPorts();
        PortArea port1 = ports[0];

        int temp = port1.getStarty();
        temp = (temp % (256 * 256));
        buffer1[16] = (byte) (temp % 256);
        buffer1[17] = (byte) (temp / 256);

        temp = port1.getHeight();
        temp = (temp % (256 * 256));
        buffer1[18] = (byte) (temp % 256);
        buffer1[19] = (byte) (temp / 256);

        temp = port1.getStartX();
        temp = (temp % (256 * 256));
        buffer1[20] = (byte) (temp % 256);
        buffer1[21] = (byte) (temp / 256);

        temp = port1.getWidth();
        temp = (temp % (256 * 256));
        buffer1[22] = (byte) (temp % 256);
        buffer1[23] = (byte) (temp / 256);

        PortArea port2 = ports[1];

        temp = port2.getStarty();
        temp = (temp % (256 * 256));
        buffer2[0] = (byte) (temp % 256);
        buffer2[1] = (byte) (temp / 256);

        temp = port2.getHeight();
        temp = (temp % (256 * 256));
        buffer2[2] = (byte) (temp % 256);
        buffer2[3] = (byte) (temp / 256);

        temp = port2.getStartX();
        temp = (temp % (256 * 256));
        buffer2[4] = (byte) (temp % 256);
        buffer2[5] = (byte) (temp / 256);

        temp = port2.getWidth();
        temp = (temp % (256 * 256));
        buffer2[6] = (byte) (temp % 256);
        buffer2[7] = (byte) (temp / 256);

        PortArea port3 = ports[2];
        if (port3 != null)
        {
            temp = port3.getStarty();
            temp = (temp % (256 * 256));
            buffer3[0] = (byte) (temp % 256);
            buffer3[1] = (byte) (temp / 256);

            temp = port3.getHeight();
            temp = (temp % (256 * 256));
            buffer3[2] = (byte) (temp % 256);
            buffer3[3] = (byte) (temp / 256);

            temp = port3.getStartX();
            temp = (temp % (256 * 256));
            buffer3[4] = (byte) (temp % 256);
            buffer3[5] = (byte) (temp / 256);

            temp = port3.getWidth();
            temp = (temp % (256 * 256));
            buffer3[6] = (byte) (temp % 256);
            buffer3[7] = (byte) (temp / 256);
        }

        PortArea port4 = ports[3];
        if (port4 != null)
        {

            temp = port4.getStarty();
            temp = (temp % (256 * 256));
            buffer4[0] = (byte) (temp % 256);
            buffer4[1] = (byte) (temp / 256);

            temp = port4.getHeight();
            temp = (temp % (256 * 256));
            buffer4[2] = (byte) (temp % 256);
            buffer4[3] = (byte) (temp / 256);

            temp = port4.getStartX();
            temp = (temp % (256 * 256));
            buffer4[4] = (byte) (temp % 256);
            buffer4[5] = (byte) (temp / 256);

            temp = port4.getWidth();
            temp = (temp % (256 * 256));
            buffer4[6] = (byte) (temp % 256);
            buffer4[7] = (byte) (temp / 256);
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
//            for (int i = 1; i < 262; i++)
//            {
//                buffer[i] = (byte) 0x00;
//            }
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
}
