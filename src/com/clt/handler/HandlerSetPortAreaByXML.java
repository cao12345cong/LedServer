package com.clt.handler;

import java.io.IOException;

import com.clt.commondata.PortArea;
import com.clt.commondata.SenderInfo;
import com.clt.commondata.SenderParameters;
import com.clt.operation.SenderOperation;
import com.clt.operation.SoSetAutoBrightness;
import com.clt.operation.SoSetBasicParameters;
import com.clt.operation.SoSetPortArea;
import com.clt.service.CommandExcutorImpl;
import com.clt.util.FileLogger;

/**
 * 发送卡基本参数
 *
 */
public class HandlerSetPortAreaByXML extends SenderHandler
{
    private SoSetPortArea senderOperation;

    public HandlerSetPortAreaByXML(SenderOperation senderOperation,
            CommandExcutorImpl soCommandExcutor)
    {
        super(senderOperation, soCommandExcutor);
        this.senderOperation = (SoSetPortArea) senderOperation;
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
            sendBuffer[1] = (byte) 0x00;// 发送卡序号
            sendBuffer[2] = (byte) (bufferLen / 256);// 帧长
            sendBuffer[3] = (byte) (bufferLen % 256);
            sendBuffer[4] = (byte) 0xa7;// 帧编号
            sendBuffer[5] = (byte) 0x00;// 子帧编号
            sendBuffer[6] = (byte) 0xff;// 网口序号
            int k = 7;
            // 控制面积
            for (int i = 0; i < 4; i++)
            {
                PortArea pa = senderOperation.getAreaConfig().getPortAreas()[i];
                if(pa==null){
                    break;
                }
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
    @Override
    public boolean doHandler()
    {
        
        try
        {
            senderOperation.setOperationStep(1);
            if (soCommandExcutor.getmOutputStream() == null)
                return false;
            boolean isOk = false;
            if (soCommandExcutor.senderInfo == null)
            {
//                new HandlerDetectReceiver(senderOperation,
//                        this.soCommandExcutor).doHandler();
               isOk=detectCard();
               FileLogger.getInstance().writeMessageToFile("U盘设置网口面积，开始探卡");
               if(!isOk){
                   FileLogger.getInstance().writeMessageToFile("U盘设置网口面积，探卡失败");
                   return false;
               }
            }
            
            SenderInfo senderInfo = soCommandExcutor.senderInfo;
            if(senderInfo==null){
                return false;
            }
            // 构建一个SenderParameters数据封装类
            SenderParameters params = new SenderParameters();
            params.setbBigPack(senderInfo.isBigPacket());
            params.setbAutoBright(senderInfo.isAutoBright());
            params.setM_frameRate(senderInfo.getFrameRate());
            params.setRealParamFlag(senderInfo.isRealParamFlags());
            params.setbZeroDelay(senderInfo.isBZeroDelay());
            params.setRgbBitsFlag(senderInfo.getTenBitFlag());
            params.setbHDCP(senderInfo.isBHDCP());
            params.setInputType(senderInfo.getInputType());
            // 设置网口面积
            params.setPorts(senderOperation.getAreaConfig().getPortAreas());
            SoSetBasicParameters soSetBasicParameters = new SoSetBasicParameters();
            soSetBasicParameters.setSenderParameters(params);
            // isOk = EC_SetBasicParameters(soSetBasicParameters);
            FileLogger.getInstance().writeMessageToFile("U盘设置网口面积，开始设置");
            isOk = new HandlerSetBasicParameters(soSetBasicParameters,
                    soCommandExcutor).doHandler();
            //FileLogger.getInstance().writeMessageToFile("设置网口面积是否成功" + isOk);
            if(!isOk){
                
                FileLogger.getInstance().writeMessageToFile("U盘设置网口面积，设置失败");
            }else{
                
                FileLogger.getInstance().writeMessageToFile("U盘设置网口面积，设置成功");
            }
            return isOk;

        }
        catch (Exception e)
        {
            
            return false;
        }
        
    }
    
    private boolean detectCard(){
        
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
