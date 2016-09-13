package com.clt.handler;

import java.io.File;
import java.io.IOException;

import com.clt.operation.SenderOperation;
import com.clt.operation.SoSetConnectionToReceicerCard;
import com.clt.operation.SoSetConnectionToSenderCard;
import com.clt.operation.SoSetReceiverCardInfoSender;
import com.clt.operation.SotReceiverCardInfoSaveToReceiver;
import com.clt.parser.ReceiverSettingBinParser;
import com.clt.service.CommandExcutor;
import com.clt.service.CommandExcutorImpl;
import com.clt.util.Constants;
import com.clt.util.FileLogger;

/**
 * 对接收卡参数的处理(i系列模式下)
 * @author caocong
 *
 */
public class HandlerReceiverCardInfoIType extends SenderHandler
{

    private SenderOperation senderOperation;

    private int portIndex = 0xff;

    private File binFile;

    private byte [] buffer;

    private int width;// 箱体宽

    private int height;// 箱体高

    public HandlerReceiverCardInfoIType(SenderOperation senderOperation,
    		CommandExcutor soCommandExcutor)
    {
        super(senderOperation, soCommandExcutor);
        this.senderOperation = senderOperation;

    }

    // public HandlerReceiverCardInfoIType(CommandExcutorImpl soCommandExcutor,
    // File binFile,int width,int height)
    // {
    // try
    // {
    // this.soCommandExcutor = soCommandExcutor;
    //
    // this.binFile=binFile;
    //
    // if(binFile.exists()){
    // this.buffer=ReceiverSettingBinParser.getAllByteFromBin(binFile);
    // }
    //
    // this.width=width;
    //
    // this.height=height;
    // }
    // catch (IOException e)
    // {
    // e.printStackTrace();
    // }
    //
    // }

    @Override
    public boolean doHandler()
    {
        try
        {
        	if (soCommandExcutor.getmOutputStream() == null)
            {
                return false;
            }
            if (senderOperation instanceof SotReceiverCardInfoSaveToReceiver)
            {// 固化
                String dir = Constants.SDCARD_PATH;
                this.binFile = new File(dir,
                        ((SotReceiverCardInfoSaveToReceiver) senderOperation)
                                .getFileName());
                if (!this.binFile.exists())
                {
                    return false;
                }
                    this.buffer = ReceiverSettingBinParser
                            .getAllByteFromBin(binFile);
//                this.width = width;
//                this.height = height;
                return saveToReceiverCard();
            }
            else if (senderOperation instanceof SoSetReceiverCardInfoSender)
            {// 发送
                String dir = Constants.SDCARD_PATH;
                this.binFile = new File(dir,
                        ((SoSetReceiverCardInfoSender) senderOperation)
                                .getFileName());
                if (!this.binFile.exists())
                {
                    return false;
                }

                if (binFile.exists())
                {
                    this.buffer = ReceiverSettingBinParser
                            .getAllByteFromBin(binFile);
                }

                this.width = ((SoSetReceiverCardInfoSender) senderOperation).getWidth();
                this.height = ((SoSetReceiverCardInfoSender) senderOperation).getHeight();
                return sendToSenderCard();
            }
        }
        catch (IOException e)
        {
            return false;
        }
        return false;
    }

    /**********************************************************************************/

    /**
     * 固化到接收卡
     * @return
     * @throws IOException 
     */
    public boolean saveToReceiverCard() throws IOException
    {
        sendToSenderCard();
        boolean isOk = false;
        // // 探测发送卡
        // isOk=soCommandExcutor.EC_DetectSender();
        // if(!isOk){
        // return false;
        // }
        //
        if (this.buffer == null)
        {
            return false;
        }
        // // 快速改变控制区域
        // File file = new File(Config.SDCARD_PATH, "read.bin");
        // if (!file.exists())
        // {
        // return false;
        // }
        // 0x01-0x04 10-12 14-25 30-32 60-63 80-85 00(控制面积全0xff) 5A
        // 0x01-0x04 10-12 14-25 30-3e 60-63 80-8e 00(控制面积全0xff) I5AF
        // byte [] buffer = ReceiverSettingBinParser.getAllByteFromBin(binFile);
        /**
         * gamma表
         */
        // 擦除
        isOk = doClear_cc(0x26, 0x09);
        // 写
        for (int i = 0x00; i <= 0x04; i++)
        {
            isOk = doWrite_cc_26_09(i, buffer, 0x26, 0x09);
            if (!isOk)
            {
                return false;
            }
        }
        /**
         * 基本参数
         */
        // 擦除
        isOk = doClear_cc(0x06, 0x07);
        // 写
        for (int i = 0x00; i <= 0x04; i++)
        {
            isOk = doWrite_cc_06(i, buffer);
            if (!isOk)
            {
                return false;
            }
        }

        // // gamma表
        // for (int i = 0x10; i <= 0x12; i++)
        // {
        // isOk = doWrite_cc_26(i, buffer,0x06,0x07);
        // if (!isOk)
        // {
        // return false;
        // }
        // }
        // for (int i = 0x14; i <= 0x25; i++)
        // {
        // isOk = doWrite_cc_26(i, buffer);
        // if (!isOk)
        // {
        // return false;
        // }
        // }
        // Route表
        for (int i = 0x30; i <= 0x32; i++)
        {
            isOk = doWrite_cc_06(i, buffer);
            if (!isOk)
            {
                return false;
            }
        }

        // 扫描调度表
        for (int i = 0x60; i <= 0x63; i++)
        {
            isOk = doWrite_cc_06(i, buffer);
            if (!isOk)
            {
                return false;
            }
        }
        // 参数备份??????
        for (int i = 0x80; i <= 0x85; i++)
        {

        }
        /**
         * EEPROM存储空间定义
         */
        // 19帧 写 85
        byte [] buffer_40 = new byte []
            {
                    0x00, 0x01, 0x00, (byte) 0xff, (byte) 0xff, (byte) 0xff
            };
        isOk = doWrite_cc_19(buffer_40, 0x40, 0x06);
        if (!isOk)
        {
            return false;
        }
        // 06帧 加载 77
        isOk = doLoad_cc_06();
        if (!isOk)
        {
            return false;
        }
        // 19帧 写 控制区域
        byte [] buffer_02 = new byte []
            {
                    0x00, 0x00, 0x00, 0x00, 0x00, (byte) 0x80, 0x00,
                    (byte) 0x80
            };
        isOk = doWrite_cc_19(buffer_02, 0x02, 0x2a);
        if (!isOk)
        {
            return false;
        }
        // 19帧 写 数据偏移
        byte [] buffer_92 = new byte []
            {
                0x00
            };
        isOk = doWrite_cc_19(buffer_92, 0x92, 0x20);
        if (!isOk)
        {
            return false;
        }
        // 06帧 加载 77
        isOk = doLoad_cc_06();
        if (!isOk)
        {
            return false;
        }
        // 探测发送卡
        isOk = new HandlerDetectReceiver(null, soCommandExcutor).doHandler();
        if (!isOk)
        {
            return false;
        }
        // 探测接收卡 cc 00-03 各三次
        for (int i = 0; i < 3; i++)
        {
            isOk = detectReceiverCard(0x00);
            if (!isOk)
            {
                return false;
            }
        }
        for (int i = 0; i < 3; i++)
        {
            isOk = detectReceiverCard(0x01);
            if (!isOk)
            {
                return false;
            }
        }
        for (int i = 0; i < 3; i++)
        {
            isOk = detectReceiverCard(0x02);
            if (!isOk)
            {
                return false;
            }
        }
        for (int i = 0; i < 3; i++)
        {
            isOk = detectReceiverCard(0x03);
            if (!isOk)
            {
                return false;
            }
        }
        /**
         * 基本参数及面积控制区域
         */
        isOk = doClear_aa_23_05();
        if (!isOk)
        {
            return false;
        }
        SenderParamAndCtrArea sp = new SenderParamAndCtrArea(
                soCommandExcutor.senderInfo);
        isOk = doWrite_aa_85_05(sp.getBuffer(), 0x00);
        if (!isOk)
        {
            return false;
        }
        isOk = doWrite_aa_85_05(sp.getBuffer(), 0x04);
        if (!isOk)
        {
            return false;
        }
        isOk = doWrite_aa_85_05(sp.getBuffer(), 0x05);
        if (!isOk)
        {
            return false;
        }
        isOk = doWrite_aa_85_05(sp.getBuffer(), 0x06);
        if (!isOk)
        {
            return false;
        }

        // 7.
        isOk = doDetect_44_06();
        if (!isOk)
        {
            return false;
        }
        // 探测发送卡
        return true;
    }

    /**
     * 发送到发送卡
     * @return
     * @throws IOException 
     */
    public boolean sendToSenderCard() throws IOException
    {
        boolean isOk = false;
////         探测发送卡
////         擦除
//         isOk = doClearSenderCard();
//         if (!isOk)
//         {
//         return false;
//         }
//         //写
//         File file = new File(Config.SDCARD_PATH, "read.bin");
//         if (!file.exists())
//         {
//         return false;
//         }
//         byte [] buffer = ReceiverSettingBinParser.getAllByteFromBin(binFile);
//         int time = buffer.length / 256;
//         // 广播
//         for (int i = 0; i < time; i++)
//         {
//         isOk = doWriteSenderCard(i, buffer);
//         if (!isOk)
//         {
//         return false;
//         }
//         }
        if (this.buffer == null)
        {
            return false;
        }
        // 基本参数帧
        isOk = doWriteByBroadcast(0x05, 0, 268, buffer, 0x00);
        if (!isOk)
        {
            return false;
        }
        // 抽点表
        isOk = doWriteByBroadcast(0x10, 0, 1036, buffer, 0x100);
        if (!isOk)
        {
            return false;
        }
        // 走线表
        isOk = doWriteByBroadcast(0x03, 0, 780, buffer, 0x3000);
        if (!isOk)
        {
            return false;
        }

        // // 走线表
        // isOk = doWriteByBroadcast(0x03, 0x01, 780, buffer, 0x3400);
        // if (!isOk)
        // {
        // return false;
        // }
        // gamma表

        isOk = doWriteByBroadcast(0x76, 0, 1167, buffer, 0x1000);
        if (!isOk)
        {
            return false;
        }
        // 扫描调度表
        isOk = doWriteByBroadcast(0x18, 0, 1036, buffer, 0x6000);
        if (!isOk)
        {
            return false;
        }
        byte [] bf = new byte [1280];
        for (int i = 0; i < 128; i++)
        {
            bf[i * 10 + 0] = 0x00;
            bf[i * 10 + 1] = 0x00;
            bf[i * 10 + 2] = 0x00;
            bf[i * 10 + 3] = 0x00;
            bf[i * 10 + 4] = (byte) (this.width / 256);
            bf[i * 10 + 5] = (byte) (this.width % 256);
            bf[i * 10 + 6] = (byte) (this.height / 256);
            bf[i * 10 + 7] = (byte) (this.height % 256);
            bf[i * 10 + 8] = 0x00;
            bf[i * 10 + 9] = 0x00;
        }

        // 控制区域
        // isOk = doWriteByBroadcast(0x02, 0, 1292, buffer, 0x7000);
        isOk = doWriteByBroadcast(0x02, 0, 1292, bf, 0x00);
        if (!isOk)
        {
            return false;
        }
        // 探测发送卡
        new HandlerDetectReceiver(null, soCommandExcutor).doHandler();
        // 快速改变控制区域

        return true;
    }

    /**
     * 探测接收卡
     * @return
     */
    public boolean detectReceiverCard()
    {
        try
        {
            byte [] sendBuffer = new byte [280];
            sendBuffer[0] = (byte) 0xcc;
            sendBuffer[1] = (byte) 0x00;// 网口序号
            sendBuffer[2] = (byte) (280 / 256);
            sendBuffer[3] = (byte) (280 % 256);
            sendBuffer[4] = (byte) 0x07;// flash操作
            sendBuffer[5] = (byte) 0x00;
            sendBuffer[6] = (byte) 0x00;
            sendBuffer[7] = (byte) 0x00;
            sendBuffer[8] = (byte) 0x00;
            sendBuffer[9] = (byte) 0x00;
            // for (int j = 0; j < 270; j++)
            // {
            // sendBuffer[10 + j] = 0x00;
            // }

            soCommandExcutor.setRcvBufLen(0);
            soCommandExcutor.setBatchRead(true);
            soCommandExcutor.setBatchCount(6);

            soCommandExcutor.getmOutputStream().write(sendBuffer, 0, 280);
            soCommandExcutor.getmOutputStream().flush();

            boolean bOk = false;
            for (int i = 0; i < 100; i++)
            {

                Thread.sleep(100);

                if (soCommandExcutor.getRcvBufLen() >= soCommandExcutor
                        .getBatchCount())
                {
                    byte [] rcvBuffer = soCommandExcutor.getRcvBuffer();

                    int succeededFlag = rcvBuffer[0] & ((byte) 0xff);
                    if (rcvBuffer[0] == (byte) 0xef)
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
     * 探测接收卡
     * @return
     */
    public boolean detectReceiverCard(int portIndex)
    {
        try
        {
            byte [] sendBuffer = new byte [280];
            sendBuffer[0] = (byte) 0xcc;
            sendBuffer[1] = (byte) portIndex;// 网口序号
            sendBuffer[2] = (byte) (280 / 256);
            sendBuffer[3] = (byte) (280 % 256);
            sendBuffer[4] = (byte) 0x07;// flash操作
            sendBuffer[5] = (byte) 0x00;
            sendBuffer[6] = (byte) 0x00;
            sendBuffer[7] = (byte) 0x00;
            sendBuffer[8] = (byte) 0x00;
            sendBuffer[9] = (byte) 0x00;
            // for (int j = 0; j < 270; j++)
            // {
            // sendBuffer[10 + j] = 0x00;
            // }

            soCommandExcutor.setRcvBufLen(0);
            soCommandExcutor.setBatchRead(true);
            soCommandExcutor.setBatchCount(6);

            soCommandExcutor.getmOutputStream().write(sendBuffer, 0, 280);
            soCommandExcutor.getmOutputStream().flush();

            boolean bOk = false;
            for (int i = 0; i < 100; i++)
            {

                Thread.sleep(100);

                if (soCommandExcutor.getRcvBufLen() >= soCommandExcutor
                        .getBatchCount())
                {
                    byte [] rcvBuffer = soCommandExcutor.getRcvBuffer();

                    int succeededFlag = rcvBuffer[0] & ((byte) 0xff);
                    if (rcvBuffer[0] == (byte) 0xef)
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
            e.printStackTrace();

            soCommandExcutor.setRcvBufLen(0);
            soCommandExcutor.setBatchRead(false);
            soCommandExcutor.setBatchCount(0);

            return false;
        }
    }

    /*************************固化*******************************/
    /**
     * 
     * @param frameNum 帧号
     * @param address  flash地址
     * @return
     */
    private boolean doClear_cc(int frameNum, int address)
    {
        try
        {
            byte [] sendBuffer = new byte [136];
            sendBuffer[0] = (byte) 0xcc;
            sendBuffer[1] = (byte) portIndex;// 网口序号
            sendBuffer[2] = (byte) (136 / 256);
            sendBuffer[3] = (byte) (136 % 256);
            sendBuffer[4] = (byte) frameNum;// 帧号
            sendBuffer[5] = (byte) 0x00;// 接收卡序号
            sendBuffer[6] = (byte) 0x00;
            sendBuffer[7] = (byte) 0xff;// 目标卡号
            sendBuffer[8] = (byte) 0xff;
            sendBuffer[9] = (byte) 0x23;// 操作子类型
            sendBuffer[10] = (byte) 0x00;// 是否需要返回值
            sendBuffer[11] = (byte) address;// 欲操作Flash地址
            sendBuffer[12] = (byte) 0x00;
            sendBuffer[13] = (byte) 0x00;
            // for (int j = 0; j < 122; j++)
            // {
            // sendBuffer[14 + j] = 0x00;
            // }

            soCommandExcutor.setRcvBufLen(0);
            soCommandExcutor.setBatchRead(true);
            soCommandExcutor.setBatchCount(6);

            soCommandExcutor.getmOutputStream().write(sendBuffer, 0, 136);
            soCommandExcutor.getmOutputStream().flush();

            boolean bOk = false;
            for (int i = 0; i < 100; i++)
            {

                Thread.sleep(100);

                if (soCommandExcutor.getRcvBufLen() >= soCommandExcutor
                        .getBatchCount())
                {
                    byte [] rcvBuffer = soCommandExcutor.getRcvBuffer();
                    // int succeededFlag = rcvBuffer[0] & ((byte) 0xff);
                    if (rcvBuffer[0] == (byte) 0xef)
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

    private boolean doWrite_cc_06(int index, byte [] buffer)
    {
        try
        {
            byte [] sendBuffer = new byte [274];
            sendBuffer[0] = (byte) 0xcc;
            sendBuffer[1] = (byte) portIndex;// 网口序号
            sendBuffer[2] = (byte) (274 / 256);
            sendBuffer[3] = (byte) (274 % 256);
            sendBuffer[4] = (byte) 0x06;// 帧号
            sendBuffer[5] = (byte) 0x00;// 接收卡序号
            sendBuffer[6] = (byte) 0x00;
            sendBuffer[7] = (byte) 0xff;// 目标卡号
            sendBuffer[8] = (byte) 0xff;
            sendBuffer[9] = (byte) 0x85;// 写
            sendBuffer[10] = (byte) 0x00;// 是否需要返回值
            sendBuffer[11] = (byte) 0x07;// flash地址
            // 0x01-0x04 10-12 14-25 30-32 60-63 80-85 00(控制面积全0xff)
            sendBuffer[12] = (byte) index;
            sendBuffer[13] = (byte) 0x00;
            for (int j = 0; j < 260; j++)
            {
                sendBuffer[14 + j] = buffer[index * 256 + j];

            }
            if (index == 0)
            {
                sendBuffer[14 + 240] = (byte) 0xff;
                sendBuffer[14 + 241] = (byte) 0xff;
                sendBuffer[14 + 242] = (byte) 0xff;
                sendBuffer[14 + 243] = (byte) 0xff;
                sendBuffer[14 + 244] = (byte) 0xff;
                sendBuffer[14 + 245] = (byte) 0xff;
                sendBuffer[14 + 246] = (byte) 0xff;
                sendBuffer[14 + 247] = (byte) 0xff;
                sendBuffer[14 + 248] = (byte) 0xff;
                sendBuffer[14 + 249] = (byte) 0xff;
            }

            soCommandExcutor.setRcvBufLen(0);
            soCommandExcutor.setBatchRead(true);
            soCommandExcutor.setBatchCount(6);

            soCommandExcutor.getmOutputStream().write(sendBuffer, 0, 274);
            soCommandExcutor.getmOutputStream().flush();

            boolean bOk = false;
            for (int i = 0; i < 100; i++)
            {

                Thread.sleep(100);

                if (soCommandExcutor.getRcvBufLen() >= soCommandExcutor
                        .getBatchCount())
                {
                    byte [] rcvBuffer = soCommandExcutor.getRcvBuffer();

                    int succeededFlag = rcvBuffer[0] & ((byte) 0xff);
                    if (succeededFlag == (byte) 0xef)
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
     * 
     * @param index    
     * @param buffer   要发送的字节数组
     * @param frameNum 帧号
     * @param address  flash地址首位
     * @return
     */
    private boolean doWrite_cc_26_09(int index, byte [] buffer, int frameNum,
            int address)
    {
        try
        {
            byte [] sendBuffer = new byte [274];
            sendBuffer[0] = (byte) 0xcc;
            sendBuffer[1] = (byte) portIndex;// 网口序号
            sendBuffer[2] = (byte) (274 / 256);
            sendBuffer[3] = (byte) (274 % 256);
            sendBuffer[4] = (byte) frameNum;// 帧号
            sendBuffer[5] = (byte) 0x00;// 接收卡序号
            sendBuffer[6] = (byte) 0x00;
            sendBuffer[7] = (byte) 0xff;// 目标卡号
            sendBuffer[8] = (byte) 0xff;
            sendBuffer[9] = (byte) 0x85;// 写
            sendBuffer[10] = (byte) 0x00;// 是否需要返回值
            sendBuffer[11] = (byte) address;// flash地址
            // 0x01-0x04 10-12 14-25 30-32 60-63 80-85 00(控制面积全0xff)
            sendBuffer[12] = (byte) index;
            sendBuffer[13] = (byte) 0x00;
            for (int j = 0; j < 260; j++)
            {
                sendBuffer[14 + j] = buffer[index * 256 + j + 0x1000];

            }
            if (index == 0)
            {
                sendBuffer[14 + 240] = (byte) 0xff;
                sendBuffer[14 + 241] = (byte) 0xff;
                sendBuffer[14 + 242] = (byte) 0xff;
                sendBuffer[14 + 243] = (byte) 0xff;
                sendBuffer[14 + 244] = (byte) 0xff;
                sendBuffer[14 + 245] = (byte) 0xff;
                sendBuffer[14 + 246] = (byte) 0xff;
                sendBuffer[14 + 247] = (byte) 0xff;
                sendBuffer[14 + 248] = (byte) 0xff;
                sendBuffer[14 + 249] = (byte) 0xff;
            }

            soCommandExcutor.setRcvBufLen(0);
            soCommandExcutor.setBatchRead(true);
            soCommandExcutor.setBatchCount(6);

            soCommandExcutor.getmOutputStream().write(sendBuffer, 0, 274);
            soCommandExcutor.getmOutputStream().flush();

            boolean bOk = false;
            for (int i = 0; i < 100; i++)
            {

                Thread.sleep(100);

                if (soCommandExcutor.getRcvBufLen() >= soCommandExcutor
                        .getBatchCount())
                {
                    byte [] rcvBuffer = soCommandExcutor.getRcvBuffer();

                    int succeededFlag = rcvBuffer[0] & ((byte) 0xff);
                    if (succeededFlag == (byte) 0xef)
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
     * 
     * @param buffer  字节数组
     * @param address 操作地址
     * @param len     操作数据长度   
     * @return
     */
    private boolean doWrite_cc_19(byte [] buffer, int address, int len)
    {
        try
        {
            int bufLen = 136;
            byte [] sendBuffer = new byte [bufLen];
            sendBuffer[0] = (byte) 0xcc;
            sendBuffer[1] = (byte) portIndex;// 网口序号
            sendBuffer[2] = (byte) (bufLen / 256);
            sendBuffer[3] = (byte) (bufLen % 256);
            sendBuffer[4] = (byte) 0x19;// 帧号
            sendBuffer[5] = (byte) 0x00;// 接收卡序号
            sendBuffer[6] = (byte) 0x00;
            sendBuffer[7] = (byte) 0xff;// 目标卡号
            sendBuffer[8] = (byte) 0xff;
            sendBuffer[9] = (byte) 0x85;// 写
            sendBuffer[10] = (byte) 0x00;// 操作起始地址 4个字节
            sendBuffer[11] = (byte) 0x00;
            sendBuffer[12] = (byte) 0x00;
            sendBuffer[13] = (byte) address;// flash地址
            sendBuffer[14] = (byte) 0x00;// 操作数据长度 4个字节
            sendBuffer[15] = (byte) 0x00;
            sendBuffer[16] = (byte) 0x00;
            sendBuffer[17] = (byte) len;
            int size = buffer.length;
            for (int j = 0; j < size; j++)
            {
                sendBuffer[18 + j] = buffer[j];

            }

            soCommandExcutor.setRcvBufLen(0);
            soCommandExcutor.setBatchRead(true);
            soCommandExcutor.setBatchCount(6);

            soCommandExcutor.getmOutputStream().write(sendBuffer, 0, bufLen);
            soCommandExcutor.getmOutputStream().flush();

            boolean bOk = false;
            for (int i = 0; i < 100; i++)
            {

                Thread.sleep(100);

                if (soCommandExcutor.getRcvBufLen() >= soCommandExcutor
                        .getBatchCount())
                {
                    byte [] rcvBuffer = soCommandExcutor.getRcvBuffer();

                    int succeededFlag = rcvBuffer[0] & ((byte) 0xff);
                    if (succeededFlag == (byte) 0xef)
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
     * 加载06帧
     * @return
     */
    private boolean doLoad_cc_06()
    {
        try
        {
            int bufLen = 136;
            byte [] sendBuffer = new byte [bufLen];
            sendBuffer[0] = (byte) 0xcc;
            sendBuffer[1] = (byte) portIndex;// 网口序号
            sendBuffer[2] = (byte) (bufLen / 256);
            sendBuffer[3] = (byte) (bufLen % 256);
            sendBuffer[4] = (byte) 0x06;// 帧号
            sendBuffer[5] = (byte) 0x00;// 接收卡序号
            sendBuffer[6] = (byte) 0x00;
            sendBuffer[7] = (byte) 0xff;// 目标卡号
            sendBuffer[8] = (byte) 0xff;
            sendBuffer[9] = (byte) 0x77;// 写

            soCommandExcutor.setRcvBufLen(0);
            soCommandExcutor.setBatchRead(true);
            soCommandExcutor.setBatchCount(6);

            soCommandExcutor.getmOutputStream().write(sendBuffer, 0, bufLen);
            soCommandExcutor.getmOutputStream().flush();

            boolean bOk = false;
            for (int i = 0; i < 100; i++)
            {

                Thread.sleep(100);

                if (soCommandExcutor.getRcvBufLen() >= soCommandExcutor
                        .getBatchCount())
                {
                    byte [] rcvBuffer = soCommandExcutor.getRcvBuffer();

                    int succeededFlag = rcvBuffer[0] & ((byte) 0xff);
                    if (succeededFlag == (byte) 0xef)
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

    private boolean doClear_aa_23_05()
    {

        try
        {
            int bufferLen = 262;
            byte [] sendBuffer = new byte [bufferLen];
            sendBuffer[0] = (byte) 0xaa;
            sendBuffer[1] = (byte) 0x23;// 操作码
            sendBuffer[2] = (byte) 0x05;// 操作地址
            sendBuffer[3] = (byte) 0x00;
            sendBuffer[4] = (byte) 0x00;

            soCommandExcutor.setRcvBufLen(0);
            soCommandExcutor.setBatchRead(true);
            soCommandExcutor.setBatchCount(4);

            soCommandExcutor.getmOutputStream().write(sendBuffer, 0, bufferLen);
            soCommandExcutor.getmOutputStream().flush();

            boolean bOk = false;
            for (int i = 0; i < 100; i++)
            {
                Thread.sleep(100);
                if (soCommandExcutor.getRcvBufLen() >= soCommandExcutor
                        .getBatchCount())
                {
                    byte [] rcvBuffer = soCommandExcutor.getRcvBuffer();
                    if (rcvBuffer[0] == (byte) 0xaa)
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
     * 基本参数及面积控制区域
     * @param buffer 
     * @return
     */
    private boolean doWrite_aa_85_05(byte [] buffer, int index)
    {
        try
        {
            int bufferLen = 262;
            byte [] sendBuffer = new byte [bufferLen];
            sendBuffer[0] = (byte) 0xaa;
            sendBuffer[1] = (byte) 0x85;// 操作码
            sendBuffer[2] = (byte) 0x05;// 操作地址
            sendBuffer[3] = (byte) index;
            sendBuffer[4] = (byte) 0x00;//

            for (int i = 0; i < 256; i++)
            {
                sendBuffer[5 + i] = buffer[index + i];
            }

            soCommandExcutor.setRcvBufLen(0);
            soCommandExcutor.setBatchRead(true);
            soCommandExcutor.setBatchCount(4);

            soCommandExcutor.getmOutputStream().write(sendBuffer, 0, bufferLen);
            soCommandExcutor.getmOutputStream().flush();

            boolean bOk = false;
            for (int i = 0; i < 100; i++)
            {
                Thread.sleep(100);

                if (soCommandExcutor.getRcvBufLen() >= soCommandExcutor
                        .getBatchCount())
                {
                    byte [] rcvBuffer = soCommandExcutor.getRcvBuffer();
                    if (rcvBuffer[0] == (byte) 0xaa)
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

    private boolean doDetect_44_06()
    {
        try
        {
            int bufferLen = 262;
            byte [] sendBuffer = new byte [bufferLen];
            sendBuffer[0] = (byte) 0xaa;
            sendBuffer[1] = (byte) 0x44;// 操作码
            sendBuffer[2] = (byte) 0x06;// 操作地址
            sendBuffer[3] = (byte) 0x00;
            sendBuffer[4] = (byte) 0x00;//

            soCommandExcutor.setRcvBufLen(0);
            soCommandExcutor.setBatchRead(true);
            soCommandExcutor.setBatchCount(4);

            soCommandExcutor.getmOutputStream().write(sendBuffer, 0, bufferLen);
            soCommandExcutor.getmOutputStream().flush();

            boolean bOk = false;
            for (int i = 0; i < 100; i++)
            {

                Thread.sleep(100);

                if (soCommandExcutor.getRcvBufLen() >= soCommandExcutor
                        .getBatchCount())
                {
                    byte [] rcvBuffer = soCommandExcutor.getRcvBuffer();
                    if (rcvBuffer[0] == (byte) 0xaa)
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

    /*************************发送*******************************/

    /**
     * 广播写
     * @param frameNum 帧号
     * @param subframeNum 子帧编号
     * @param bufLen   字节数组长度
     * @param buffer   字节数组
     * @param startIndex 数组开始索引值
     * @return
     */
    private boolean doWriteByBroadcast(int frameNum, int subframeNum,
            int bufLen, byte [] buffer, int startIndex)
    {
        try
        {
            byte [] sendBuffer = new byte [bufLen];
            sendBuffer[0] = (byte) 0xcc;
            sendBuffer[1] = (byte) this.portIndex;
            sendBuffer[2] = (byte) (bufLen / 256);
            sendBuffer[3] = (byte) (bufLen % 256);
            sendBuffer[4] = (byte) frameNum;// 帧号
            sendBuffer[5] = (byte) 0x00;// 流水号
            sendBuffer[6] = (byte) 0x00;
            sendBuffer[7] = (byte) subframeNum;// 子帧编号
            int len = bufLen - 8 - 4;
            for (int j = 0; j < len; j++)
            {
                sendBuffer[8 + j] = buffer[startIndex + j];

            }

            soCommandExcutor.setRcvBufLen(0);
            soCommandExcutor.setBatchRead(true);
            soCommandExcutor.setBatchCount(6);

            soCommandExcutor.getmOutputStream().write(sendBuffer, 0, bufLen);
            soCommandExcutor.getmOutputStream().flush();

            boolean bOk = false;
            for (int i = 0; i < 100; i++)
            {

                Thread.sleep(100);

                if (soCommandExcutor.getRcvBufLen() >= soCommandExcutor
                        .getBatchCount())
                {
                    byte [] rcvBuffer = soCommandExcutor.getRcvBuffer();

                    int succeededFlag = rcvBuffer[0] & ((byte) 0xff);
                    if (succeededFlag == (byte) 0xef)
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
     * 广播 写基本参数全0；
     */
    private boolean doWriteByBroadcast(int index, byte [] buffer)
    {
        try
        {
            byte [] sendBuffer = new byte [274];
            sendBuffer[0] = (byte) 0xcc;
            sendBuffer[1] = (byte) 0xff;// 网口序号
            sendBuffer[2] = (byte) (274 / 256);
            sendBuffer[3] = (byte) (274 % 256);
            sendBuffer[4] = (byte) 0x06;// flash操作
            sendBuffer[5] = (byte) 0x00;// 接收卡序号
            sendBuffer[6] = (byte) 0x00;
            sendBuffer[7] = (byte) 0xff;// 目标卡号
            sendBuffer[8] = (byte) 0xff;
            sendBuffer[9] = (byte) 0x85;// 写
            sendBuffer[10] = (byte) 0x00;// flash地址
            sendBuffer[11] = (byte) 0x07;
            // 0x01-0x04 10-12 14-25 30-32 60-63 80-85 00(控制面积全0xff)
            sendBuffer[12] = (byte) index;
            sendBuffer[13] = (byte) 0x00;
            for (int j = 0; j < 260; j++)
            {
                sendBuffer[14 + j] = buffer[index * 256 + j];

            }
            if (index == 0)
            {
                sendBuffer[14 + 240] = (byte) 0xff;
                sendBuffer[14 + 241] = (byte) 0xff;
                sendBuffer[14 + 242] = (byte) 0xff;
                sendBuffer[14 + 243] = (byte) 0xff;
                sendBuffer[14 + 244] = (byte) 0xff;
                sendBuffer[14 + 245] = (byte) 0xff;
                sendBuffer[14 + 246] = (byte) 0xff;
                sendBuffer[14 + 247] = (byte) 0xff;
                sendBuffer[14 + 248] = (byte) 0xff;
                sendBuffer[14 + 249] = (byte) 0xff;
            }

            soCommandExcutor.setRcvBufLen(0);
            soCommandExcutor.setBatchRead(true);
            soCommandExcutor.setBatchCount(6);

            soCommandExcutor.getmOutputStream().write(sendBuffer, 0, 274);
            soCommandExcutor.getmOutputStream().flush();

            boolean bOk = false;
            for (int i = 0; i < 100; i++)
            {

                Thread.sleep(100);

                if (soCommandExcutor.getRcvBufLen() >= soCommandExcutor
                        .getBatchCount())
                {
                    byte [] rcvBuffer = soCommandExcutor.getRcvBuffer();

                    int succeededFlag = rcvBuffer[0] & ((byte) 0xff);
                    if (succeededFlag == (byte) 0xef)
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
