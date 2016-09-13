package com.clt.handler;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import android.util.Log;

import com.clt.entity.ConnectionParam;
import com.clt.parser.ReceiverSettingBinParser;
import com.clt.service.CommandExcutorImpl;
import com.clt.util.Config;
import com.clt.util.Constants;
import com.clt.util.FileLogger;

/**
 * 对接收卡连接关系的处理（经典模式下）
 * @author caocong
 *
 */
public class HandlerConnectionRelationClassic
{
    private CommandExcutorImpl soCommandExcutor;

    private ConnectionParam connectionParam;// 连接关系需要的参数

    private ArrayList<SCardAreaInfo> scardAreaInfos;

    private byte portIndex;// 网口序号,高四位表示发送卡序号，低四位为网口序号

    public HandlerConnectionRelationClassic(
            CommandExcutorImpl soCommandExcutor, ConnectionParam connectionParam)
    {
        this.soCommandExcutor = soCommandExcutor;

        this.connectionParam = connectionParam;

        this.scardAreaInfos = new ArrayList<SCardAreaInfo>();

        this.portIndex = (byte) ((connectionParam.getSender() << 4) + connectionParam
                .getPort());
    }

    /**
     * 固化到接收卡
     * @return
     * @throws IOException 
     */
    public boolean saveToReceiverCard() throws IOException
    {
        boolean isOk = false;
        // EC_SetReceiverCardMode();
        EC_GetControlAreasAndNum();
        isOk = detectReceiverCard();
        if (!isOk)
        {
            return false;
        }
        // 擦除
        isOk = doClearReceiverCard();
        if (!isOk)
        {
            return false;
        }

        File file = new File(Constants.SDCARD_PATH, "read.bin");
        if (!file.exists())
        {
            return false;
        }
        byte [] buffer = ReceiverSettingBinParser.getAllByteFromBin(file);
        // int time = buffer.length / 256;
        // 0x01-0x04 10-12 14-25 30-32 60-63 80-85 00(控制面积全0xff) 5A
        // 0x01-0x04 10-12 14-25 30-3e 60-63 80-8e 00(控制面积全0xff) I5AF
        // 基本参数
        for (int i = 0x01; i <= 0x04; i++)
        {
            isOk = doWriteByBroadcast(i, buffer);
            if (!isOk)
            {
                return false;
            }
        }
        // gamma表
        for (int i = 0x10; i <= 0x12; i++)
        {
            isOk = doWriteByBroadcast(i, buffer);
            if (!isOk)
            {
                return false;
            }
        }
        for (int i = 0x14; i <= 0x25; i++)
        {
            isOk = doWriteByBroadcast(i, buffer);
            if (!isOk)
            {
                return false;
            }
        }
        // Route表
        for (int i = 0x30; i <= 0x3e; i++)
        {
            isOk = doWriteByBroadcast(i, buffer);
            if (!isOk)
            {
                return false;
            }
        }

        // 扫描调度表
        for (int i = 0x60; i <= 0x63; i++)
        {
            isOk = doWriteByBroadcast(i, buffer);
            if (!isOk)
            {
                return false;
            }
        }
        // 参数备份??????
        for (int i = 0x80; i <= 0x85; i++)
        {

        }
        // 基本参数
        isOk = doWriteByBroadcast(0, buffer);
        if (!isOk)
        {
            return false;
        }
        // 写，step1.发送广播 step2.0-1023逐个发送基本参数
        for (int i = 0; i < 1023; i++)
        {
            isOk = doWriteOneByOneReceiverCard(i, buffer);
            if (!isOk)
            {
                return false;
            }
        }

        // 刷新
        isOk = doUpdateReceiverCard();
        if (!isOk)
        {
            return false;
        }
        // 0x01-0x08 11-09 12-1a
        return false;
    }

    /**
     * 发送到发送卡
     * @return
     * @throws IOException
     */
    public boolean sendToSenderCard() throws IOException
    {
        boolean isOk = false;
        isOk = doClearSenderCard();
        byte [] buffer = ReceiverSettingBinParser.getAllByteFromBin(new File(
                Constants.SDCARD_PATH, "read.bin"));
        int time = buffer.length / 256;
        // 广播
        // ABCD口 21 22 23 24
        for (int i = 0; i < time; i++)
        {
            isOk = doWriteSenderCard(i, buffer);
            if (!isOk)
            {
                return false;
            }
        }

        // 写，step1.发送广播 step2.0-1023逐个发送基本参数
        // 刷新

        return false;
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
            sendBuffer[1] = portIndex;// 网口序号
            sendBuffer[2] = (byte) (280 / 256);
            sendBuffer[3] = (byte) (280 % 256);
            sendBuffer[4] = (byte) 0x07;// flash操作
            sendBuffer[5] = (byte) 0x00;
            sendBuffer[6] = (byte) 0x00;
            sendBuffer[7] = (byte) 0x00;
            sendBuffer[8] = (byte) 0x00;
            sendBuffer[9] = (byte) 0x00;

            soCommandExcutor.setRcvBufLen(0);
            soCommandExcutor.setBatchRead(true);
            soCommandExcutor.setBatchCount(7);

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
     * 广播 写基本参数全0；
     */
    private boolean doWriteByBroadcast(int index, byte [] buffer)
    {
        try
        {
            byte [] sendBuffer = new byte [274];
            sendBuffer[0] = (byte) 0xcc;
            sendBuffer[1] = portIndex;// 网口序号
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
            soCommandExcutor.clearRcvBuffer();

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
     * 依次写基本参数到1024张接收卡
     * @param buffer 
     */
    private boolean doWriteOneByOneReceiverCard(int index, byte [] buffer)
    {
        try
        {
            int portNum = index;
            if (index <= (scardAreaInfos.size() - 1))
            {
                portNum = scardAreaInfos.get(index).senderNum;
            }
            byte [] sendBuffer = new byte [274];
            sendBuffer[0] = (byte) 0xcc;
            sendBuffer[1] = portIndex;// 网口序号
            sendBuffer[2] = (byte) (274 / 256);
            sendBuffer[3] = (byte) (274 % 256);
            sendBuffer[4] = (byte) 0x06;// flash操作
            sendBuffer[5] = (byte) 0x00;// 接收卡序号
            sendBuffer[6] = (byte) 0x00;
            sendBuffer[7] = (byte) (portNum / 256);// 目标卡号
            sendBuffer[8] = (byte) (portNum % 256);
            sendBuffer[9] = (byte) 0x85;// 写
            sendBuffer[10] = (byte) 0x00;// 是否需要返回值
            sendBuffer[11] = (byte) 0x07;// flash地址
            sendBuffer[12] = (byte) 0x00;
            sendBuffer[13] = (byte) 0x00;
            for (int j = 0; j < 256; j++)
            {
                sendBuffer[14 + j] = buffer[j];
            }
            /**
             * 修改控制区域
             */
            if (index <= (scardAreaInfos.size() - 1))
            {
                sendBuffer[14 + 240] = scardAreaInfos.get(index).x11;
                sendBuffer[14 + 241] = scardAreaInfos.get(index).x12;
                sendBuffer[14 + 242] = scardAreaInfos.get(index).y11;
                sendBuffer[14 + 243] = scardAreaInfos.get(index).y12;
                sendBuffer[14 + 244] = scardAreaInfos.get(index).x21;
                sendBuffer[14 + 245] = scardAreaInfos.get(index).x22;
                sendBuffer[14 + 246] = scardAreaInfos.get(index).y21;
                sendBuffer[14 + 247] = scardAreaInfos.get(index).y22;
                sendBuffer[14 + 248] = scardAreaInfos.get(index).shift1;
                sendBuffer[14 + 249] = scardAreaInfos.get(index).shift2;
            }
            else
            {
                sendBuffer[14 + 240] = scardAreaInfos.get(0).x11;
                sendBuffer[14 + 241] = scardAreaInfos.get(0).x12;
                sendBuffer[14 + 242] = scardAreaInfos.get(0).y11;
                sendBuffer[14 + 243] = scardAreaInfos.get(0).y12;
                sendBuffer[14 + 244] = scardAreaInfos.get(0).x21;
                sendBuffer[14 + 245] = scardAreaInfos.get(0).x22;
                sendBuffer[14 + 246] = scardAreaInfos.get(0).y21;
                sendBuffer[14 + 247] = scardAreaInfos.get(0).y22;
                sendBuffer[14 + 248] = scardAreaInfos.get(0).shift1;
                sendBuffer[14 + 249] = scardAreaInfos.get(0).shift2;
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
     * 广播 擦
     */
    private boolean doClearReceiverCard()
    {
        try
        {
            byte [] sendBuffer = new byte [136];
            sendBuffer[0] = (byte) 0xcc;
            sendBuffer[1] = portIndex;// 网口序号
            sendBuffer[2] = (byte) (136 / 256);
            sendBuffer[3] = (byte) (136 % 256);
            sendBuffer[4] = (byte) 0x06;// flash操作
            sendBuffer[5] = (byte) 0x00;
            sendBuffer[6] = (byte) 0x00;
            sendBuffer[7] = (byte) 0xff;// 广播
            sendBuffer[8] = (byte) 0xff;
            sendBuffer[9] = (byte) 0x23;// 擦除
            sendBuffer[10] = (byte) 0x00;
            sendBuffer[11] = (byte) 0x07;
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
            // FileLogger.getInstance().writeByteToFile(sendBuffer);

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

    /**
     * 获得接收卡的控制面积和编号
     */
    public void EC_GetControlAreasAndNum()
    {
        ConnectionParam ctd = connectionParam;

        int i = 0, j = 0;
        // int z = 0, w = 0;
        // int senderNum = ctd.getSender();// 发送卡号
        // int port = ctd.getPort();// 端口号
        int xoutset_pos, youtset_pos;
        int row = ctd.getRow();// 行
        int column = ctd.getColumn();// 列
        int width = ctd.getWidth();// 宽
        int height = ctd.getHeight();// 高

        SCardAreaInfo scCardAreaInfo = null;

        int rowIndex = 0;// 列索引
        int columnIndex = 0;// 行索引
        int index = 0;// 集合索引
        int cardNum = 0;// 卡编号
        // 计算每张接收卡的控制区域
        for (j = 0; j < row; j++)
        {
            for (i = 0; i < column; i++)
            {

                xoutset_pos = 0x0000 + i * width;
                youtset_pos = height * j;
                scCardAreaInfo = new SCardAreaInfo();
                scCardAreaInfo.x11 = (byte) (xoutset_pos / 256);
                scCardAreaInfo.x12 = (byte) (xoutset_pos % 256);
                scCardAreaInfo.x21 = (byte) ((xoutset_pos + width) / 256);
                scCardAreaInfo.x22 = (byte) ((xoutset_pos + width) % 256);
                scCardAreaInfo.y11 = (byte) (youtset_pos / 256);
                scCardAreaInfo.y12 = (byte) (youtset_pos % 256);
                scCardAreaInfo.y21 = (byte) ((youtset_pos + height) / 256);
                scCardAreaInfo.y22 = (byte) ((youtset_pos + height) % 256);
                scardAreaInfos.add(scCardAreaInfo);

            }

        }
        // 计算每张接收卡的编号
        switch (ctd.getMode())
        {
            case 1:
                for (j = 0; j < row; j++)
                {

                    if (columnIndex % 2 == 0)
                    {
                        for (i = 0; i < column; i++)
                        {
                            scardAreaInfos.get(j * column + i).senderNum = cardNum++;
                        }

                    }
                    else
                    {
                        for (i = column - 1; i >= 0; i--)
                        {
                            scardAreaInfos.get(j * column + i).senderNum = cardNum++;
                        }
                    }
                    columnIndex++;
                }
                break;
            case 2:
                for (j = 0; j < row; j++)
                {

                    if (columnIndex % 2 == 0)
                    {
                        for (i = column - 1; i >= 0; i--)
                        {
                            scardAreaInfos.get(j * column + i).senderNum = cardNum++;
                        }

                    }
                    else
                    {
                        for (i = 0; i < column; i++)
                        {
                            scardAreaInfos.get(j * column + i).senderNum = cardNum++;
                        }
                    }
                    columnIndex++;
                }
                break;
            case 3:
                for (j = row - 1; j >= 0; j--)
                {

                    if (columnIndex % 2 == 0)
                    {
                        for (i = 0; i < column; i++)
                        {
                            scardAreaInfos.get(j * column + i).senderNum = cardNum++;
                        }
                    }
                    else
                    {
                        for (i = column - 1; i >= 0; i--)
                        {
                            scardAreaInfos.get(j * column + i).senderNum = cardNum++;
                        }
                    }
                    columnIndex++;
                }
                break;
            case 4:
                for (j = row - 1; j >= 0; j--)
                {

                    if (columnIndex % 2 == 0)
                    {
                        for (i = column - 1; i >= 0; i--)
                        {
                            scardAreaInfos.get(j * column + i).senderNum = cardNum++;
                        }

                    }
                    else
                    {
                        for (i = 0; i < column; i++)
                        {
                            scardAreaInfos.get(j * column + i).senderNum = cardNum++;
                        }
                    }
                    columnIndex++;
                }
                break;
            case 5:
                for (j = 0; j < column; j++)
                {
                    if (rowIndex % 2 == 0)
                    {
                        for (i = 0; i < row; i++)
                        {
                            scardAreaInfos.get(i * column + j).senderNum = cardNum++;
                        }

                    }
                    else
                    {
                        for (i = row - 1; i >= 0; i--)
                        {
                            scardAreaInfos.get(i * column + j).senderNum = cardNum++;
                        }

                    }
                    rowIndex++;
                }
                break;
            case 6:
                for (j = 0; j < column; j++)
                {
                    if (rowIndex % 2 == 0)
                    {
                        for (i = row - 1; i >= 0; i--)
                        {
                            scardAreaInfos.get(i * column + j).senderNum = cardNum++;
                        }

                    }
                    else
                    {
                        for (i = 0; i < row; i++)
                        {
                            scardAreaInfos.get(i * column + j).senderNum = cardNum++;
                        }

                    }
                    rowIndex++;
                }
                break;
            case 7:
                for (j = column - 1; j >= 0; j--)
                {
                    if (rowIndex % 2 == 0)
                    {
                        for (i = 0; i < row; i++)
                        {
                            scardAreaInfos.get(i * column + j).senderNum = cardNum++;
                        }

                    }
                    else
                    {
                        for (i = row - 1; i >= 0; i--)
                        {
                            scardAreaInfos.get(i * column + j).senderNum = cardNum++;
                        }

                    }
                    rowIndex++;
                }
                break;
            case 8:
                for (j = column - 1; j >= 0; j--)
                {
                    if (rowIndex % 2 == 0)
                    {
                        for (i = row - 1; i >= 0; i--)
                        {
                            scardAreaInfos.get(i * column + j).senderNum = cardNum++;
                        }

                    }
                    else
                    {
                        for (i = 0; i < row; i++)
                        {
                            scardAreaInfos.get(i * column + j).senderNum = cardNum++;
                        }

                    }
                    rowIndex++;
                }
                break;
        }
        Log.i("", "");
    }

    // /**
    // * 设置每张卡的控制面积参数，保存到ArrayList中
    // * @param ctd
    // * @param len 128
    // * @param index 0
    // * @param total
    // */
    // public void EC_SetReceiverCardMode()
    // {
    // ConnectionParam ctd = connectionParam;
    //
    // int i = 0, j = 0;
    // // int z = 0, w = 0;
    // // int senderNum = ctd.getSender();// 发送卡号
    // // int port = ctd.getPort();// 端口号
    // int xoutset_pos, youtset_pos;
    // int row = ctd.getRow();// 行
    // int column = ctd.getColumn();// 列
    // int width = ctd.getWidth();// 宽
    // int height = ctd.getHeight();// 高
    //
    // SCardAreaInfo scCardAreaInfo = null;
    //
    // int num = 0;
    // int k = 0;
    // switch (ctd.getMode())
    // {
    // case 1:
    // for (j = 0; j < row; j++)
    // {
    // for (i = 0; i < column; i++)
    // {
    //
    // xoutset_pos = 0x0000 + i * width;
    // youtset_pos = height * j;
    // scCardAreaInfo = new SCardAreaInfo();
    // scCardAreaInfo.senderNum = num++;
    // scCardAreaInfo.x11 = (byte) (xoutset_pos / 256);
    // scCardAreaInfo.x12 = (byte) (xoutset_pos % 256);
    // scCardAreaInfo.x21 = (byte) ((xoutset_pos + width) / 256);
    // scCardAreaInfo.x22 = (byte) ((xoutset_pos + width) % 256);
    // scCardAreaInfo.y11 = (byte) (youtset_pos / 256);
    // scCardAreaInfo.y12 = (byte) (youtset_pos % 256);
    // scCardAreaInfo.y21 = (byte) ((youtset_pos + height) / 256);
    // scCardAreaInfo.y22 = (byte) ((youtset_pos + height) % 256);
    // scardAreaInfos.add(scCardAreaInfo);
    //
    // }
    //
    // }
    // // 更改顺序
    // for (j = 0; j < row; j++)
    // {
    //
    // if (j % 2 == 1)
    // {
    // for (i = 0; i < column; i++)
    // {
    // num = (j + 1) * column - 1;
    // scardAreaInfos.get(k++).senderNum = num - i;
    // }
    // }
    // else
    // {
    // for (i = 0; i < column; i++)
    // {
    // k++;
    // }
    // }
    // }
    // break;
    //
    // case 2:
    // for (j = 0; j < row; j++)
    // {
    // for (i = 0; i < column; i++)
    // {
    //
    // xoutset_pos = 0x0000 + i * width;
    // youtset_pos = height * j;
    // scCardAreaInfo = new SCardAreaInfo();
    // scCardAreaInfo.senderNum = num++;
    // scCardAreaInfo.x11 = (byte) (xoutset_pos / 256);
    // scCardAreaInfo.x12 = (byte) (xoutset_pos % 256);
    // scCardAreaInfo.x21 = (byte) ((xoutset_pos + width) / 256);
    // scCardAreaInfo.x22 = (byte) ((xoutset_pos + width) % 256);
    // scCardAreaInfo.y11 = (byte) (youtset_pos / 256);
    // scCardAreaInfo.y12 = (byte) (youtset_pos % 256);
    // scCardAreaInfo.y21 = (byte) ((youtset_pos + height) / 256);
    // scCardAreaInfo.y22 = (byte) ((youtset_pos + height) % 256);
    // scardAreaInfos.add(scCardAreaInfo);
    // }
    //
    // }
    //
    // // 更改顺序
    // for (j = 0; j < row; j++)
    // {
    //
    // if (j % 2 == 0)
    // {
    // for (i = 0; i < column; i++)
    // {
    // num = (j + 1) * column - 1;
    // scardAreaInfos.get(k++).senderNum = num - i;
    // }
    // }
    // else
    // {
    // for (i = 0; i < column; i++)
    // {
    // k++;
    // }
    // }
    //
    // }
    // break;
    // case 3:
    // for (j = row - 1; j >= 0; j--)
    // {
    // for (i = 0; i < column; i++)
    // {
    // xoutset_pos = 0x0000 + i * width;
    // youtset_pos = height * j;
    // scCardAreaInfo = new SCardAreaInfo();
    // scCardAreaInfo.senderNum = j * column + i;
    // scCardAreaInfo.x11 = (byte) (xoutset_pos / 256);
    // scCardAreaInfo.x12 = (byte) (xoutset_pos % 256);
    // scCardAreaInfo.x21 = (byte) ((xoutset_pos + width) / 256);
    // scCardAreaInfo.x22 = (byte) ((xoutset_pos + width) % 256);
    // scCardAreaInfo.y11 = (byte) (youtset_pos / 256);
    // scCardAreaInfo.y12 = (byte) (youtset_pos % 256);
    // scCardAreaInfo.y21 = (byte) ((youtset_pos + height) / 256);
    // scCardAreaInfo.y22 = (byte) ((youtset_pos + height) % 256);
    // scardAreaInfos.add(scCardAreaInfo);
    // }
    //
    // }
    //
    // // 更改顺序
    // for (j = row - 1; j >= 0; j--)
    // {
    //
    // if (j % 2 == 1)
    // {
    // for (i = 0; i < column; i++)
    // {
    // num = (j + 1) * column - 1;
    // scardAreaInfos.get(k++).senderNum = num - i;
    // }
    // }
    // else
    // {
    // for (i = 0; i < column; i++)
    // {
    // k++;
    // }
    // }
    //
    // }
    // // for (j = row-1; j >= 0; j--)
    // // {
    // // if (j % 2 == 0)
    // // {
    // // for (i = 0; i < column; i++)
    // // {
    // // num = j * column + i;
    // // scardAreaInfos.get(j * column + i).senderNum = num;
    // // }
    // // }
    // // else
    // // {
    // // for (i = 0; i < column; i++)
    // // {
    // // num = (j + 1) * column - 1;
    // // scardAreaInfos.get(j * column + i).senderNum = num
    // // - i;
    // // }
    // // }
    // //
    // // }
    // break;
    // case 4:
    // for (j = row; j > 0; j--)
    // {
    // for (i = column; i > 0; i--)
    // {
    // if ((row - j) % 2 == 1)
    // {
    // xoutset_pos = (byte) (0x0000 + (column - i) * width);
    // }
    // else
    // {
    // xoutset_pos = (byte) (0x0000 + (i - 1) * width);
    //
    // }
    // youtset_pos = (byte) (height * (j - 1));
    // scCardAreaInfo = new SCardAreaInfo();
    // scCardAreaInfo.x11 = (byte) (xoutset_pos / 256);
    // scCardAreaInfo.x12 = (byte) (xoutset_pos % 256);
    // scCardAreaInfo.x21 = (byte) ((xoutset_pos + width) / 256);
    // scCardAreaInfo.x22 = (byte) ((xoutset_pos + width) % 256);
    // scCardAreaInfo.y11 = (byte) (youtset_pos / 256);
    // scCardAreaInfo.y12 = (byte) (youtset_pos % 256);
    // scCardAreaInfo.y21 = (byte) ((youtset_pos + height) / 256);
    // scCardAreaInfo.y22 = (byte) ((youtset_pos + height) % 256);
    // scardAreaInfos.add(scCardAreaInfo);
    // }
    //
    // }
    // for (j = 0; j < row; j++)
    // {
    // if (j % 2 == 1)
    // {
    // for (i = 0; i < column; i++)
    // {
    // num = j * column + i;
    // scardAreaInfos.get(j * column + i).senderNum = num;
    // }
    // }
    // else
    // {
    // for (i = 0; i < column; i++)
    // {
    // num = (j + 1) * column - 1;
    // scardAreaInfos.get(j * column + i).senderNum = num
    // - i;
    // }
    // }
    //
    // }
    // break;
    // case 5:
    // for (i = 0; i < column; i++)
    // {
    // for (j = 0; j < row; j++)
    // {
    // if (i % 2 == 1)
    // {
    // youtset_pos = (row - j - 1) * height;
    // }
    // else
    // {
    // youtset_pos = j * height;
    //
    // }
    // xoutset_pos = i * width;
    // scCardAreaInfo = new SCardAreaInfo();
    // scCardAreaInfo.x11 = (byte) (xoutset_pos / 256);
    // scCardAreaInfo.x12 = (byte) (xoutset_pos % 256);
    // scCardAreaInfo.x21 = (byte) ((xoutset_pos + width) / 256);
    // scCardAreaInfo.x22 = (byte) ((xoutset_pos + width) % 256);
    // scCardAreaInfo.y11 = (byte) (youtset_pos / 256);
    // scCardAreaInfo.y12 = (byte) (youtset_pos % 256);
    // scCardAreaInfo.y21 = (byte) ((youtset_pos + height) / 256);
    // scCardAreaInfo.y22 = (byte) ((youtset_pos + height) % 256);
    // scardAreaInfos.add(scCardAreaInfo);
    // }
    //
    // }
    // break;
    // case 6:
    // for (i = 0; i < column; i++)
    // {
    // for (j = row; j > 0; j--)
    // {
    // if (i % 2 == 1)
    // {
    // youtset_pos = (byte) (0x0000 + (row - j) * height);
    // }
    // else
    // {
    // youtset_pos = (byte) (height * (j - 1));
    // }
    // xoutset_pos = (byte) (0x0000 + i * width);
    // scCardAreaInfo = new SCardAreaInfo();
    // scCardAreaInfo.x11 = (byte) (xoutset_pos / 256);
    // scCardAreaInfo.x12 = (byte) (xoutset_pos % 256);
    // scCardAreaInfo.x21 = (byte) ((xoutset_pos + width) / 256);
    // scCardAreaInfo.x22 = (byte) ((xoutset_pos + width) % 256);
    // scCardAreaInfo.y11 = (byte) (youtset_pos / 256);
    // scCardAreaInfo.y12 = (byte) (youtset_pos % 256);
    // scCardAreaInfo.y21 = (byte) ((youtset_pos + height) / 256);
    // scCardAreaInfo.y22 = (byte) ((youtset_pos + height) % 256);
    // scardAreaInfos.add(scCardAreaInfo);
    // }
    // }
    // break;
    // case 7:
    // for (i = column; i > 0; i--)
    // {
    // for (j = 0; j < row; j++)
    // {
    // if ((column - i) % 2 == 1)
    // {
    // youtset_pos = (byte) (0x0000 + (row - j - 1)
    // * height);
    // }
    // else
    // {
    // youtset_pos = (byte) (height * j);
    // }
    // xoutset_pos = (byte) (0x0000 + (i - 1) * width);
    // scCardAreaInfo = new SCardAreaInfo();
    // scCardAreaInfo.x11 = (byte) (xoutset_pos / 256);
    // scCardAreaInfo.x12 = (byte) (xoutset_pos % 256);
    // scCardAreaInfo.x21 = (byte) ((xoutset_pos + width) / 256);
    // scCardAreaInfo.x22 = (byte) ((xoutset_pos + width) % 256);
    // scCardAreaInfo.y11 = (byte) (youtset_pos / 256);
    // scCardAreaInfo.y12 = (byte) (youtset_pos % 256);
    // scCardAreaInfo.y21 = (byte) ((youtset_pos + height) / 256);
    // scCardAreaInfo.y22 = (byte) ((youtset_pos + height) % 256);
    // scardAreaInfos.add(scCardAreaInfo);
    // }
    // }
    // break;
    // case 8:
    // for (i = column; i > 0; i--)
    // {
    // for (j = row; j > 0; j--)
    // {
    // if ((column - i) % 2 == 1)
    // {
    // youtset_pos = (byte) (0x0000 + (row - j) * height);
    // }
    // else
    // {
    // youtset_pos = (byte) (height * (j - 1));
    // }
    // xoutset_pos = (byte) (0x0000 + (i - 1) * width);
    // scCardAreaInfo = new SCardAreaInfo();
    // scCardAreaInfo.x11 = (byte) (xoutset_pos / 256);
    // scCardAreaInfo.x12 = (byte) (xoutset_pos % 256);
    // scCardAreaInfo.x21 = (byte) ((xoutset_pos + width) / 256);
    // scCardAreaInfo.x22 = (byte) ((xoutset_pos + width) % 256);
    // scCardAreaInfo.y11 = (byte) (youtset_pos / 256);
    // scCardAreaInfo.y12 = (byte) (youtset_pos % 256);
    // scCardAreaInfo.y21 = (byte) ((youtset_pos + height) / 256);
    // scCardAreaInfo.y22 = (byte) ((youtset_pos + height) % 256);
    // scardAreaInfos.add(scCardAreaInfo);
    //
    // }
    // }
    // break;
    // }
    //
    // Log.i("aa", "asdf");
    //
    // }

    /**
     *  更新控制参数
     * @return
     */
    public boolean doUpdateReceiverCard()
    {
        byte [] sendBuffer = new byte [136];
        sendBuffer[0] = (byte) 0xcc;
        sendBuffer[1] = portIndex;// 网口序号
        sendBuffer[2] = (byte) 0x00;
        sendBuffer[3] = (byte) 0x88;
        sendBuffer[4] = (byte) 0x06;// flash操作
        sendBuffer[5] = (byte) 0x00;
        sendBuffer[6] = (byte) 0x00;
        sendBuffer[7] = (byte) 0xff;// 广播
        sendBuffer[8] = (byte) 0xff;
        sendBuffer[9] = (byte) 0x77;// 擦除
        sendBuffer[10] = (byte) 0x00;
        sendBuffer[11] = (byte) 0x00;
        sendBuffer[12] = (byte) 0x00;
        sendBuffer[13] = (byte) 0x00;
        for (int j = 0; j < 122; j++)
        {
            sendBuffer[14 + j] = 0x00;
        }

        try
        {
            soCommandExcutor.setRcvBufLen(0);
            soCommandExcutor.setBatchRead(true);
            soCommandExcutor.setBatchCount(6);

            soCommandExcutor.getmOutputStream().write(sendBuffer, 0, 136);
            soCommandExcutor.getmOutputStream().flush();
            FileLogger.getInstance().writeByteToFile(sendBuffer);
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
                // TODO Auto-generated catch block
                e.printStackTrace();
            }

            if (soCommandExcutor.getRcvBufLen() >= soCommandExcutor
                    .getBatchCount())
            {
                byte [] rcvBuffer = soCommandExcutor.getRcvBuffer();

                if (rcvBuffer[0] == (byte) 0xef)
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

    /****************************************************/
    /**
     * 广播 擦
     */
    private boolean doClearSenderCard()
    {

        byte [] sendBuffer = new byte [262];
        sendBuffer[0] = (byte) 0xaa;
        sendBuffer[1] = (byte) 0x67;
        sendBuffer[2] = (byte) 0x00;// 操作地址
        sendBuffer[3] = (byte) 0x21;
        sendBuffer[4] = (byte) 0x00;
        for (int j = 0; j < 256; j++)
        {
            sendBuffer[5 + j] = 0x00;
        }

        try
        {
            soCommandExcutor.setRcvBufLen(0);
            soCommandExcutor.setBatchRead(true);
            soCommandExcutor.setBatchCount(6);

            soCommandExcutor.getmOutputStream().write(sendBuffer, 0, 262);
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
                // TODO Auto-generated catch block
                e.printStackTrace();
            }

            if (soCommandExcutor.getRcvBufLen() >= soCommandExcutor
                    .getBatchCount())
            {
                byte [] rcvBuffer = soCommandExcutor.getRcvBuffer();

                int succeededFlag = rcvBuffer[0] & ((byte) 0xff);
                if (rcvBuffer[0] == (byte) 0xaa)
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

    /**
     *  写
     */
    private boolean doWriteSenderCard(int index, byte [] buffer)
    {

        byte [] sendBuffer = new byte [262];
        sendBuffer[0] = (byte) 0xaa;
        sendBuffer[1] = (byte) 0x77;// 网口序号
        sendBuffer[2] = (byte) 0x00;// 操作地址
        sendBuffer[3] = (byte) 0x21;
        sendBuffer[4] = (byte) index;
        for (int j = 0; j < 256; j++)
        {
            sendBuffer[5 + j] = buffer[index * 256 + j];
        }

        try
        {
            soCommandExcutor.setRcvBufLen(0);
            soCommandExcutor.setBatchRead(true);
            soCommandExcutor.setBatchCount(6);

            soCommandExcutor.getmOutputStream().write(sendBuffer, 0, 262);
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
                // TODO Auto-generated catch block
                e.printStackTrace();
            }

            if (soCommandExcutor.getRcvBufLen() >= soCommandExcutor
                    .getBatchCount())
            {
                byte [] rcvBuffer = soCommandExcutor.getRcvBuffer();

                int succeededFlag = rcvBuffer[0] & ((byte) 0xff);
                if (succeededFlag == 0xaa)
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

    class SCardAreaInfo
    {

        int senderNum;// 编号

        byte x11;

        byte x12;

        byte y11;

        byte y12;

        byte x21;

        byte x22;

        byte y21;

        byte y22;

        byte shift1;

        byte shift2;

        @Override
        public String toString()
        {
            int s1 = (x11 & 0xff) * 256 + (x12 & 0xff);
            int s2 = (y11 & 0xff) * 256 + (y12 & 0xff);
            int s3 = (x21 & 0xff) * 256 + (x22 & 0xff);
            int s4 = (y21 & 0xff) * 256 + (y22 & 0xff);
            int w = s3 - s1;
            int h = s4 - s2;
            String s = senderNum + ";" + s1 + ";" + s2 + ";" + w + ";" + h;
            return s;
        }

    }

}
