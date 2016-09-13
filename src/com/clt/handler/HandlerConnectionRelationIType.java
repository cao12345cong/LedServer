package com.clt.handler;

import java.io.IOException;
import java.util.ArrayList;

import android.util.Log;

import com.clt.commondata.PortArea;
import com.clt.entity.ConnectionParam;
import com.clt.operation.SenderOperation;
import com.clt.operation.SoDetectSender;
import com.clt.operation.SoSetConnectionToReceicerCard;
import com.clt.operation.SoSetConnectionToSenderCard;
import com.clt.service.CommandExcutor;
import com.clt.util.FileLogger;

/**
 * 对接收卡连接关系的处理（i系列模式）
 * @author caocong
 *
 */
public class HandlerConnectionRelationIType extends SenderHandler
{
    private SenderOperation senderOperation;

    private ConnectionParam connectionParam;// 连接关系需要的参数

    private ArrayList<SCardAreaInfo> scardAreaInfos;

    private byte portIndex;// 网口序号,高四位表示发送卡序号，低四位为网口序号

    public HandlerConnectionRelationIType(SenderOperation senderOperation,
            CommandExcutor soCommandExcutor)
    {
        super(senderOperation, soCommandExcutor);
        this.senderOperation = senderOperation;

        this.scardAreaInfos = new ArrayList<SCardAreaInfo>();

    }

    @Override
    public boolean doHandler()
    {
        if (senderOperation instanceof SoSetConnectionToReceicerCard)
        {// 固化
            this.connectionParam = ((SoSetConnectionToReceicerCard) senderOperation)
                    .getConnectionParam();
            this.portIndex = (byte) ((connectionParam.getSender() << 4) + connectionParam
                    .getPort());
            return saveToReceiverCard();
        }
        else if (senderOperation instanceof SoSetConnectionToSenderCard)
        {// 发送
            this.connectionParam = ((SoSetConnectionToSenderCard) senderOperation)
                    .getConnectionParam();
            this.portIndex = (byte) ((connectionParam.getSender() << 4) + connectionParam
                    .getPort());
            return sendToSenderCard();
        }
        return false;

    }

    /************************************************************************************/
    // public HandlerConnectionRelationIType(CommandExcutor soCommandExcutor,
    // ConnectionParam connectionParam)
    // {
    // this.soCommandExcutor = soCommandExcutor;
    //
    // this.connectionParam = connectionParam;
    //
    // this.scardAreaInfos = new ArrayList<SCardAreaInfo>();
    //
    // this.portIndex = (byte) ((connectionParam.getSender() << 4) +
    // connectionParam
    // .getPort());
    // }

    /**
     * 固化到接收卡
     * @return
     * @throws IOException 
     */
    public boolean saveToReceiverCard()
    {

        boolean isOk = false;

        // 1.发送到发送卡
        isOk = sendToSenderCard();
        if (!isOk)
        {
            return false;
        }
        // 2.探测接收卡
        isOk = detectReceiverCard();
        if (!isOk)
        {
            return false;
        }
        try
        {
            Thread.sleep(100);
        }
        catch (InterruptedException e)
        {

        }
        // 3.写控制区域和偏移位置
        int len = scardAreaInfos.size();
        for (int i = 0; i < len; i++)
        {
            isOk = doWriteControlArea(i);
            if (!isOk)
            {
                return false;
            }
            isOk = doWriteOffset(i);
            if (!isOk)
            {
                return false;
            }
        }

        // 4.备份
        /****************备份暂时未做***********************/

        // 5.探测发送卡
        isOk = new HandlerDetectReceiver(null, soCommandExcutor).doHandler();
        if (!isOk)
        {
            return false;
        }

        // 6.基本参数及面积控制区域
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
        return isOk;

    }

    /**
     * 发送到发送卡
     * @return
     * @throws IOException
     */
    public boolean sendToSenderCard()
    {
        // EC_GetControlAreasAndNum();
        EC_SetReceiverCardMode();
        boolean isOk = false;
        // 1.探测发送卡
        isOk = new HandlerDetectReceiver(null, soCommandExcutor).doHandler();
        if (!isOk)
        {
            return false;
        }
        // 2.向接收卡02帧发送数据
        isOk = doWriteToCtrlArea();
        if (!isOk)
        {
            return false;
        }
        // 3.?????
        int len = scardAreaInfos.size();
        for (int i = 0; i < len; i++)
        {
            isOk = doWrite_CC_00(scardAreaInfos.get(i).senderNum);
            if (!isOk)
            {
                return false;
            }
            isOk = doWrite_CC_01(scardAreaInfos.get(i).senderNum);
            if (!isOk)
            {
                return false;
            }
        }
        // 4.探测发送卡
        isOk = new HandlerDetectReceiver(null, soCommandExcutor).doHandler();
        if (!isOk)
        {
            return false;
        }
        // // 5.?????
        // // 构建一个SenderParameters数据封装类
        // SenderInfo senderInfo = soCommandExcutor.senderInfo;
        // SenderParameters params = new SenderParameters();
        // params.setbBigPack(senderInfo.isBigPacket());
        // params.setbAutoBright(senderInfo.isAutoBright());
        // params.setM_frameRate(senderInfo.getFrameRate());
        // params.setRealParamFlag(senderInfo.isRealParamFlags());
        // params.setbZeroDelay(senderInfo.isBZeroDelay());
        // params.setRgbBitsFlag(senderInfo.getTenBitFlag());
        // params.setbHDCP(senderInfo.isBHDCP());
        // params.setInputType(senderInfo.getInputType());
        // // 设置网口面积
        // params.setPorts(senderInfo.getPorts());
        // SO_SetBasicParameters soSetBasicParameters = new
        // SO_SetBasicParameters();
        // soSetBasicParameters.setSenderParameters(params);
        // isOk = soCommandExcutor.EC_SetBasicParameters(soSetBasicParameters);
        // // isOk =doWrite_99_00();
        if (!isOk)
        {
            return false;
        }
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
            sendBuffer[1] = portIndex;// 网口序号
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

    /*********固化到接收卡***********************************************/
    /**
     * 写控制区域
     * @param i
     * @return
     */
    private boolean doWriteControlArea(int index)
    {
        try
        {
            int bufferLen = 136;
            byte [] sendBuffer = new byte [bufferLen];
            sendBuffer[0] = (byte) 0xcc;
            sendBuffer[1] = (byte) portIndex;// 网口序号
            sendBuffer[2] = (byte) (bufferLen / 256);
            sendBuffer[3] = (byte) (bufferLen % 256);
            sendBuffer[4] = (byte) 0x19;
            sendBuffer[5] = (byte) 0x00;
            sendBuffer[6] = (byte) 0x00;
            sendBuffer[7] = (byte) (index / 256);
            sendBuffer[8] = (byte) (index % 256);
            sendBuffer[9] = (byte) 0x85;
            sendBuffer[10] = (byte) 0x00;
            sendBuffer[11] = (byte) 0x00;
            sendBuffer[12] = (byte) 0x00;
            sendBuffer[13] = (byte) 0x02;
            sendBuffer[14] = (byte) 0x00;
            sendBuffer[15] = (byte) 0x00;
            sendBuffer[16] = (byte) 0x00;
            sendBuffer[17] = (byte) 0x2a;
            // 控制区域
            sendBuffer[18] = scardAreaInfos.get(index).x11;
            sendBuffer[19] = scardAreaInfos.get(index).x12;
            sendBuffer[20] = scardAreaInfos.get(index).y11;
            sendBuffer[21] = scardAreaInfos.get(index).y12;
            sendBuffer[22] = scardAreaInfos.get(index).x21;
            sendBuffer[23] = scardAreaInfos.get(index).x22;
            sendBuffer[24] = scardAreaInfos.get(index).y21;
            sendBuffer[25] = scardAreaInfos.get(index).y22;
            sendBuffer[26] = scardAreaInfos.get(index).shift1;
            sendBuffer[27] = scardAreaInfos.get(index).shift2;
            // for (int j = 28; j < 136; j++)
            // {
            // sendBuffer[j] = 0x00;
            // }

            soCommandExcutor.setRcvBufLen(0);
            soCommandExcutor.setBatchRead(true);
            soCommandExcutor.setBatchCount(6);

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
     * 写偏移
     * @param i
     * @return
     */
    private boolean doWriteOffset(int index)
    {
        try
        {
            int bufferLen = 136;
            byte [] sendBuffer = new byte [bufferLen];
            sendBuffer[0] = (byte) 0xcc;
            sendBuffer[1] = (byte) portIndex;// 网口序号
            sendBuffer[2] = (byte) (bufferLen / 256);
            sendBuffer[3] = (byte) (bufferLen % 256);
            sendBuffer[4] = (byte) 0x19;
            sendBuffer[5] = (byte) 0x00;
            sendBuffer[6] = (byte) 0x00;
            sendBuffer[7] = (byte) (index / 256);
            sendBuffer[8] = (byte) (index % 256);
            sendBuffer[9] = (byte) 0x85;
            sendBuffer[10] = (byte) 0x00;
            sendBuffer[11] = (byte) 0x00;
            sendBuffer[12] = (byte) 0x00;
            sendBuffer[13] = (byte) 0x92;
            sendBuffer[14] = (byte) 0x00;
            sendBuffer[15] = (byte) 0x00;
            sendBuffer[16] = (byte) 0x00;
            sendBuffer[17] = (byte) 0x20;
            // 控制区域
            sendBuffer[18] = scardAreaInfos.get(index).x11;
            sendBuffer[19] = scardAreaInfos.get(index).x12;
            sendBuffer[20] = scardAreaInfos.get(index).y11;
            sendBuffer[21] = scardAreaInfos.get(index).y12;
            sendBuffer[22] = scardAreaInfos.get(index).x21;
            sendBuffer[23] = scardAreaInfos.get(index).x22;
            sendBuffer[24] = scardAreaInfos.get(index).y21;
            sendBuffer[25] = scardAreaInfos.get(index).y22;
            sendBuffer[26] = scardAreaInfos.get(index).shift1;
            sendBuffer[27] = scardAreaInfos.get(index).shift2;
            // for (int j = 28; j < 136; j++)
            // {
            // sendBuffer[j] = 0x00;
            // }

            soCommandExcutor.setRcvBufLen(0);
            soCommandExcutor.setBatchRead(true);
            soCommandExcutor.setBatchCount(6);

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

    /**
       * 设置每张卡的控制面积参数，保存到ArrayList中
       * @param ctd
       * @param len 128
       * @param index 0
       * @param total
       */
    public void EC_SetReceiverCardMode()
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

        int num = 0;
        int rowIndex = 0;// 列索引
        int columnIndex = 0;// 行索引
        int index = 0;// 集合索引
        int cardNum = 0;// 卡编号
        switch (ctd.getMode())
        {
            case 1:
                for (j = 0; j < row; j++)
                {
                    if (columnIndex % 2 == 0)
                    {
                        for (i = 0; i < column; i++)
                        {
                            xoutset_pos = 0x0000 + i * width;
                            youtset_pos = height * j;
                            addOneScCardAreaInfo(xoutset_pos, youtset_pos,
                                    num++);
                        }

                    }
                    else
                    {
                        for (i = column - 1; i >= 0; i--)
                        {
                            xoutset_pos = 0x0000 + i * width;
                            youtset_pos = height * j;
                            addOneScCardAreaInfo(xoutset_pos, youtset_pos,
                                    num++);
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
                            xoutset_pos = 0x0000 + i * width;
                            youtset_pos = height * j;
                            addOneScCardAreaInfo(xoutset_pos, youtset_pos,
                                    num++);
                        }

                    }
                    else
                    {
                        for (i = 0; i < column; i++)
                        {
                            xoutset_pos = 0x0000 + i * width;
                            youtset_pos = height * j;
                            addOneScCardAreaInfo(xoutset_pos, youtset_pos,
                                    num++);
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
                            xoutset_pos = 0x0000 + i * width;
                            youtset_pos = height * j;
                            addOneScCardAreaInfo(xoutset_pos, youtset_pos,
                                    num++);
                        }
                    }
                    else
                    {
                        for (i = column - 1; i >= 0; i--)
                        {
                            xoutset_pos = 0x0000 + i * width;
                            youtset_pos = height * j;
                            addOneScCardAreaInfo(xoutset_pos, youtset_pos,
                                    num++);
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
                            xoutset_pos = 0x0000 + i * width;
                            youtset_pos = height * j;
                            addOneScCardAreaInfo(xoutset_pos, youtset_pos,
                                    num++);
                        }

                    }
                    else
                    {
                        for (i = 0; i < column; i++)
                        {
                            xoutset_pos = 0x0000 + i * width;
                            youtset_pos = height * j;
                            addOneScCardAreaInfo(xoutset_pos, youtset_pos,
                                    num++);
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
                            xoutset_pos = 0x0000 + j * width;
                            youtset_pos = height * i;
                            addOneScCardAreaInfo(xoutset_pos, youtset_pos,
                                    num++);
                        }

                    }
                    else
                    {
                        for (i = row - 1; i >= 0; i--)
                        {
                            xoutset_pos = 0x0000 + j * width;
                            youtset_pos = height * i;
                            addOneScCardAreaInfo(xoutset_pos, youtset_pos,
                                    num++);
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
                            xoutset_pos = 0x0000 + j * width;
                            youtset_pos = height * i;
                            addOneScCardAreaInfo(xoutset_pos, youtset_pos,
                                    num++);
                        }

                    }
                    else
                    {
                        for (i = 0; i < row; i++)
                        {
                            xoutset_pos = 0x0000 + j * width;
                            youtset_pos = height * i;
                            addOneScCardAreaInfo(xoutset_pos, youtset_pos,
                                    num++);
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
                            xoutset_pos = 0x0000 + j * width;
                            youtset_pos = height * i;
                            addOneScCardAreaInfo(xoutset_pos, youtset_pos,
                                    num++);
                        }

                    }
                    else
                    {
                        for (i = row - 1; i >= 0; i--)
                        {
                            xoutset_pos = 0x0000 + j * width;
                            youtset_pos = height * i;
                            addOneScCardAreaInfo(xoutset_pos, youtset_pos,
                                    num++);
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
                            xoutset_pos = 0x0000 + j * width;
                            youtset_pos = height * i;
                            addOneScCardAreaInfo(xoutset_pos, youtset_pos,
                                    num++);
                        }

                    }
                    else
                    {
                        for (i = 0; i < row; i++)
                        {
                            xoutset_pos = 0x0000 + j * width;
                            youtset_pos = height * i;
                            addOneScCardAreaInfo(xoutset_pos, youtset_pos,
                                    num++);
                        }

                    }
                    rowIndex++;
                }
                break;
        }

        Log.i("aa", "asdf");

    }

    private void addOneScCardAreaInfo(int xoutset_pos, int youtset_pos, int num)
    {
        int width = connectionParam.getWidth();
        int height = connectionParam.getHeight();
        SCardAreaInfo scCardAreaInfo = new SCardAreaInfo();
        scCardAreaInfo.senderNum = num;
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

    /*********************************发送到发送卡*****************************************/
    // 写到02帧控制区域
    public boolean doWriteToCtrlArea()
    {
        try
        {
            int bufferLen = 1292;
            byte [] sendBuffer = new byte [bufferLen];
            sendBuffer[0] = (byte) 0xcc;
            sendBuffer[1] = (byte) portIndex;// 网口序号
            sendBuffer[2] = (byte) (bufferLen / 256);// 数组长度
            sendBuffer[3] = (byte) (bufferLen % 256);
            sendBuffer[4] = (byte) 0x02;// 02帧
            sendBuffer[5] = (byte) 0x00;
            sendBuffer[6] = (byte) 0x00;
            sendBuffer[7] = (byte) 0x00;
            // 128张卡的实施区域，每张卡占用十个字节
            int k = 8;
            int size = scardAreaInfos.size();
            for (SCardAreaInfo scardAreaInfo : scardAreaInfos)
            {
                sendBuffer[k++] = scardAreaInfo.x11;
                sendBuffer[k++] = scardAreaInfo.x12;
                sendBuffer[k++] = scardAreaInfo.y11;
                sendBuffer[k++] = scardAreaInfo.y12;
                sendBuffer[k++] = scardAreaInfo.x21;
                sendBuffer[k++] = scardAreaInfo.x22;
                sendBuffer[k++] = scardAreaInfo.y21;
                sendBuffer[k++] = scardAreaInfo.y22;
                sendBuffer[k++] = scardAreaInfo.shift1;
                sendBuffer[k++] = scardAreaInfo.shift2;
            }

            soCommandExcutor.setRcvBufLen(0);
            soCommandExcutor.setBatchRead(true);
            soCommandExcutor.setBatchCount(6);

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

    private boolean doWrite_CC_00(int senderNum)
    {
        try
        {
            int bufferLen = 77;
            byte [] sendBuffer = new byte [bufferLen];
            sendBuffer[0] = (byte) 0xcc;
            sendBuffer[1] = (byte) portIndex;// 网口序号
            sendBuffer[2] = (byte) (bufferLen / 256);// 数组长度
            sendBuffer[3] = (byte) (bufferLen % 256);
            sendBuffer[4] = (byte) 0x11;// 11帧
            sendBuffer[5] = (byte) 0x00;
            sendBuffer[6] = (byte) 0x00;
            sendBuffer[7] = (byte) (senderNum / 256);
            sendBuffer[8] = (byte) (senderNum % 256);

            soCommandExcutor.setRcvBufLen(0);
            soCommandExcutor.setBatchRead(true);
            soCommandExcutor.setBatchCount(6);

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

    private boolean doWrite_CC_01(int senderNum)
    {
        try
        {
            int bufferLen = 77;
            byte [] sendBuffer = new byte [bufferLen];
            sendBuffer[0] = (byte) 0xcc;
            //sendBuffer[1] = (byte) 0x01;// 网口序号
            sendBuffer[1] = (byte) portIndex;// 网口序号
            sendBuffer[2] = (byte) (bufferLen / 256);// 数组长度
            sendBuffer[3] = (byte) (bufferLen % 256);
            sendBuffer[4] = (byte) 0x11;// 11帧
            sendBuffer[5] = (byte) 0x00;
            sendBuffer[6] = (byte) 0x00;
            sendBuffer[7] = (byte) (senderNum / 256);
            sendBuffer[8] = (byte) (senderNum % 256);

            soCommandExcutor.setRcvBufLen(0);
            soCommandExcutor.setBatchRead(true);
            soCommandExcutor.setBatchCount(6);

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
     * 快速改变发送卡面积
     * @return
     */
    private boolean doWrite_99_00()
    {
        try
        {
            int bufferLen = 40;
            byte [] sendBuffer = new byte [bufferLen];
            sendBuffer[0] = (byte) 0x99;
            sendBuffer[1] = (byte) portIndex;// 发送卡序号
            sendBuffer[2] = (byte) (bufferLen / 256);// 帧长
            sendBuffer[3] = (byte) (bufferLen % 256);
            sendBuffer[4] = (byte) 0xa7;// 帧编号
            sendBuffer[5] = (byte) 0x00;// 子帧编号
            sendBuffer[6] = (byte) 0xff;// 网口序号
            int k = 7;
            // 控制面积
            for (int i = 0; i < 4; i++)
            {
                PortArea pa = soCommandExcutor.senderInfo.getPorts()[i];
                sendBuffer[k++] = (byte) (pa.getStartX() % 256);
                sendBuffer[k++] = (byte) (pa.getStartX() / 256);
                sendBuffer[k++] = (byte) (pa.getHeight() % 256);
                sendBuffer[k++] = (byte) (pa.getHeight() / 256);
                sendBuffer[k++] = (byte) (pa.getStarty() % 256);
                sendBuffer[k++] = (byte) (pa.getStarty() / 256);
                sendBuffer[k++] = (byte) (pa.getWidth() % 256);
                sendBuffer[k++] = (byte) (pa.getWidth() / 256);
            }

            soCommandExcutor.setRcvBufLen(0);
            soCommandExcutor.setBatchRead(true);
            soCommandExcutor.setBatchCount(6);

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
                    if (rcvBuffer[0] == (byte) 0x99)
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
