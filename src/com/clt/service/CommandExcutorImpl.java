package com.clt.service;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import android.content.Context;
import android.util.Log;

import com.clt.Wrapper;
import com.clt.entity.ConnectionParam;
import com.clt.handler.HandlerConnectionRelationIType;
import com.clt.handler.HandlerDetectReceiver;
import com.clt.handler.HandlerGetRTC;
import com.clt.handler.HandlerReceiverCardInfoClassic;
import com.clt.handler.HandlerReceiverCardInfoIType;
import com.clt.handler.HandlerSaveBriAndClrT;
import com.clt.handler.HandlerSaveCryptUid;
import com.clt.handler.HandlerSetAutoBrightness;
import com.clt.handler.HandlerSetBasicParameters;
import com.clt.handler.HandlerSetBriAndClT;
import com.clt.handler.HandlerSetDayPeriodBri;
import com.clt.handler.HandlerSetEDID;
import com.clt.handler.HandlerSetOnOff;
import com.clt.handler.HandlerSetPortAreaByXML;
import com.clt.handler.HandlerTestMode;
import com.clt.netmessage.NMDetectSenderAnswer;
import com.clt.netmessage.NMSaveBrightAndColorTempAnswer;
import com.clt.netmessage.NMSetConnectionToReceiverCardAnswer;
import com.clt.netmessage.NMSetConnectionToSenderCardAnswer;
import com.clt.netmessage.NMSetEDIDAnswer;
import com.clt.netmessage.NMSetSenderBasicParametersAnswer;
import com.clt.netmessage.NMSetTestModeAnswer;
import com.clt.operation.SenderOperation;
import com.clt.operation.SoDetectSender;
import com.clt.operation.SoGetRTC;
import com.clt.operation.SoSaveBrightAndClrT;
import com.clt.operation.SoSaveUidEncrpt;
import com.clt.operation.SoSetAutoBrightness;
import com.clt.operation.SoSetBasicParameters;
import com.clt.operation.SoSetBrightAndClrT;
import com.clt.operation.SoSetConnectionToReceicerCard;
import com.clt.operation.SoSetConnectionToSenderCard;
import com.clt.operation.SoSetDayPeriodBright;
import com.clt.operation.SoSetEDID;
import com.clt.operation.SoSetPortArea;
import com.clt.operation.SoSetReceiverCardInfoSender;
import com.clt.operation.SoSetTestMode;
import com.clt.operation.SoShowOnOff;
import com.clt.operation.SotReceiverCardInfoSaveToReceiver;
import com.clt.util.Config;
import com.clt.util.Constants;
import com.google.gson.Gson;

/**
 * 通过串口与发送卡进行IO读写操作
 *
 */
public class CommandExcutorImpl extends CommandExcutor
{

    private static final boolean DEBUG = false;
    
    
    public CommandExcutorImpl(Context context)
    {
        super(context);
    }

    

    /**
     * 固化连接关系
     * @param connectionParam
     * @return
     */
    private boolean excuteSetConnectionToReceicerCard(
            SenderOperation senderOperation)
    {
        try
        {
            if (mOutputStream == null)
                return false;
            boolean bOk = new HandlerConnectionRelationIType(senderOperation,
                    this).doHandler();

            
            NMSetConnectionToReceiverCardAnswer answer = new NMSetConnectionToReceiverCardAnswer();
            answer.setErrorCode(bOk ? 1 : 0);
            reponseToClient(answer);
            return bOk;
        }
        catch (Exception e)
        {
            
            return false;
        }

    }

    /**
     * 发送连接关系
     * @param connectionParam
     * @return
     */
    private boolean excuteSetConnectionToSenderCard(
            SenderOperation senderOperation)
    {
        try
        {
            if (mOutputStream == null)
                return false;
            boolean bOk = new HandlerConnectionRelationIType(senderOperation,
                    this).doHandler();
            
            NMSetConnectionToSenderCardAnswer answer = new NMSetConnectionToSenderCardAnswer();
            answer.setErrorCode(bOk ? 1 : 0);
            reponseToClient(answer);
            return bOk;
        }
        catch (Exception e)
        {
            
            return false;
        }

    }

    /**
     * 设置接收卡参数,固化到接收卡
     * @param so 
     * @return
     */
    private boolean excuteSetReceiverCardInfoSaveToReceiver(SenderOperation so)
    {
        try
        {
            if (mOutputStream == null)
                return false;

            HandlerReceiverCardInfoClassic rc = new HandlerReceiverCardInfoClassic(so,
                    this);

            boolean isOk = rc.doHandler();
            
            return isOk;
        }
        catch (Exception e)
        {
            
            return false;
        }

    }
    /**
     * 设置接收卡参数,发送到发送卡
     * @param so 
     * @return
     */
    private boolean excuteSetReceiverCardInfoSend(SenderOperation so)
    {
        try
        {
            if (mOutputStream == null)
                return false;
            // if (DEBUG)
            // {
            // dir = Config.SDCARD_PATH;
            // }
            // else
            // {
            // dir = Config.USB_PATH_0;
            // }
            // File file = new File(dir, so.getFileName());
            // if (!file.exists())
            // {
            // return false;
            // }
            HandlerReceiverCardInfoClassic rc = new HandlerReceiverCardInfoClassic(so,this);

            boolean isOk = rc.doHandler();
            
            return isOk;
        }
        catch (Exception e)
        {
            
            return false;
        }

    }

    /**
     * 发送卡信息
     * 
     * @param senderOperation
     * @return
     */
    public boolean excuteDetectSender()
    {
        try
        {
            boolean bOk = new HandlerDetectReceiver(senderOperation, this)
                    .doHandler();
            
            
            NMDetectSenderAnswer nmDetectSenderAnswer = new NMDetectSenderAnswer();
            nmDetectSenderAnswer.setErrorCode(bOk ? 1 : 0);
            nmDetectSenderAnswer.setSenderInfo(senderInfo);
            
            reponseToClient(nmDetectSenderAnswer);

            return bOk;
        }
        catch (Exception e)
        {
            
            return false;
        }

    }

    /**
     * 发送卡临时亮度调节，发送六个字节
     * 
     * @param senderOperation
     * @return
     */
    public boolean excuteSetBrightAndClrT(SoSetBrightAndClrT senderOperation)
    {
        return new HandlerSetBriAndClT(senderOperation, this).doHandler();
    }

    /**
     * 保存亮度和色温
     */
    public boolean excuteSaveBrightAndClrt(SoSaveBrightAndClrT senderOperation)
    {
        boolean bOk = new HandlerSaveBriAndClrT(senderOperation, this)
                .doHandler();
        NMSaveBrightAndColorTempAnswer answer = new NMSaveBrightAndColorTempAnswer();
        answer.setErrorCode(bOk ? 1 : 0);
        reponseToClient(answer);
        return bOk;
    }

    /**
     * 发送卡临时开关屏，发送六个字节
     * 
     * @param senderOperation
     * @return
     */
    public boolean excuteShowOnOff(SoShowOnOff senderOperation)
    {
        return new HandlerSetOnOff(senderOperation, this).doHandler();
    }

    /**
     * 设置基本参数
     * 
     * @param senderOperation
     * @return
     */
    public boolean excuteSetBasicParameters(
            SoSetBasicParameters so_SetBasicParameters)
    {
        boolean bOk = new HandlerSetBasicParameters(so_SetBasicParameters, this)
                .doHandler();

        // 修改后，返回修改是否成功
        NMSetSenderBasicParametersAnswer bpa = new NMSetSenderBasicParametersAnswer();
        bpa.setErrorCode(bOk ? 1 : 0);

        reponseToClient(bpa);
        // Gson gson = new Gson();
        // String nmString = gson.toJson(bpa);
        // sendNetMessage(nmString);
        return bOk;
    }

    /**
     * 改变测试模式
     * 
     * @author caocong
     */
    public boolean excuteSetTestMode(SoSetTestMode senderOperation)
    {
        boolean bOK = new HandlerTestMode(senderOperation, this).doHandler();
        NMSetTestModeAnswer answer = new NMSetTestModeAnswer();
        answer.setErrorCode(bOK ? 1 : 0);
        reponseToClient(answer);
        // Gson gson = new Gson();
        // String nmString = gson.toJson(answer);
        // sendNetMessage(nmString);
        return bOK;
    }

    /**
     * 设置EDID信息
     * 
     * @author caocong
     * @param senderOperation
     * @return
     */
    public boolean excuteSetEDID(SoSetEDID senderOperation)
    {

        boolean bOk = new HandlerSetEDID(this.senderOperation, this)
                .doHandler();
        NMSetEDIDAnswer answer = new NMSetEDIDAnswer();
        answer.setErrorCode(bOk ? 1 : 0);
        reponseToClient(answer);
        return bOk;
    }

    /**
     * 设置分时亮度调节
     * 
     * @param senderOperation
     * @return
     */
    public boolean excuteSetDayPeriodBright(SoSetDayPeriodBright senderOperation)
    {
        return new HandlerSetDayPeriodBri(senderOperation, this).doHandler();
    }

    /**
     * 设置亮度曲线
     * @param soSetAutoBrightness
     * @return
     */
    public boolean excuteSetAutoBrightness(
            SoSetAutoBrightness soSetAutoBrightness)
    {
        return new HandlerSetAutoBrightness(senderOperation, this).doHandler();
    }

    /**
     * 用Ｕ盘的xml文件设置网口面积
     * @param senderOperation2
     * @return
     */
    private boolean excuteSetPortAreaByXML(SoSetPortArea soSetPortArea)
    {
        return new HandlerSetPortAreaByXML(soSetPortArea, this).doHandler();
    }

    /**
     * 获得RTC时间
     */
    public boolean excuteGetRTC()
    {
        return new HandlerGetRTC(senderOperation, this).doHandler();
    }

    /**
     * 发送uid的加密码
     * @param string 
     * @param uid
     */
    private boolean excuteSaveCryptUid(SoSaveUidEncrpt senderOperation)
    {
        return new HandlerSaveCryptUid(senderOperation, this).doHandler();
    }

    // HandlerDotArray dotArrayOperation = null;

    // /**
    // * 点阵字体
    // * @param so
    // * @return
    // */
    // public boolean excuteSetDotArray(SoSetDotArray so)
    // {
    // senderOperation.setOperationStep(1);
    // senderOperation.setbFinished(true);
    // // context.getAssets().openFd("file:///android_asset/simsun_U16.bin");
    //
    // // File file = new File(new
    // // URI("file:///android_asset/simsun_U16.bin"));
    // File file = AssetFileCopyUtil.copyAssetFileToSdcard(context,
    // "simsun_U16.bin");
    // boolean b = file.exists();
    // dotArrayOperation = new HandlerDotArray(this, so.getTextProgram(), file);
    // return dotArrayOperation.sendMessage();
    //
    // }

    // public boolean excuteCloseDotArray(SoCloseDotArray so)
    // {
    // senderOperation.setOperationStep(1);
    // senderOperation.setbFinished(true);
    // if (dotArrayOperation != null)
    // {
    // return dotArrayOperation.showSwitch(0);
    // }
    // return false;
    // }

    /**
     * 执行命令
     * @param senderOperation
     * @return
     */
    public boolean excuteCommand(SenderOperation senderOperation)
    {
        switch (senderOperation.getOptertorType())
        {
            case detectSender:// 探测发送卡
            {
                boolean bClass = senderOperation instanceof SoDetectSender;
                if (!bClass)
                    return false;

                return excuteDetectSender();

            }
            case setBrightAndClrT:// //
            {
                boolean bClass = senderOperation instanceof SoSetBrightAndClrT;
                if (!bClass)
                    return false;

                return excuteSetBrightAndClrT((SoSetBrightAndClrT) senderOperation);
            }
            case showOnOff:
            {
                boolean bClass = senderOperation instanceof SoShowOnOff;
                if (!bClass)
                    return false;

                return excuteShowOnOff((SoShowOnOff) senderOperation);
            }
            case setBasicParameters:
            {
                boolean bClass = senderOperation instanceof SoSetBasicParameters;
                if (!bClass)
                    return false;

                return excuteSetBasicParameters((SoSetBasicParameters) senderOperation);

            }
            case setTestMode:// 测试模式
            {
                boolean bClass = senderOperation instanceof SoSetTestMode;
                if (!bClass)
                    return false;

                boolean bOk = excuteSetTestMode((SoSetTestMode) senderOperation);

                return bOk;
            }
            case setEDID:// EDID信息
            {
                boolean bClass = senderOperation instanceof SoSetEDID;
                if (!bClass)
                    return false;

                return excuteSetEDID((SoSetEDID) senderOperation);

            }
            case saveBrightAndClrT:// 保存亮度和色温
            {
                boolean bClass = senderOperation instanceof SoSaveBrightAndClrT;
                if (!bClass)
                    return false;

                return excuteSaveBrightAndClrt((SoSaveBrightAndClrT) senderOperation);

            }
            // case setReceiverCard:// 连接关系
            // {
            // boolean bClass = senderOperation instanceof
            // SO_ReceiveCardSetting;
            // if (!bClass)
            // return false;
            // boolean bOk = EC_SetReceiverConnection((SO_ReceiveCardSetting)
            // senderOperation);
            // // boolean bOk =EC_SetReceiverCardMode((SO_ReceiveCardSetting)
            // // senderOperation);
            // NM_ReceiveCardSettingAnswer answer = new
            // NM_ReceiveCardSettingAnswer();
            // answer.setErrorCode(bOk ? 1 : 0);
            // Gson gson = new Gson();
            // String nmString = gson.toJson(answer);
            // SendNetMessage(nmString);
            // return true;
            // }
            case setAutoBrightness:
            {
                boolean bClass = senderOperation instanceof SoSetAutoBrightness;
                if (!bClass)
                    return false;
                return excuteSetAutoBrightness((SoSetAutoBrightness) senderOperation);
            }
            case setAreaPortByXML:
            {
                boolean bClass = senderOperation instanceof SoSetPortArea;
                if (!bClass)
                    return false;
                return excuteSetPortAreaByXML((SoSetPortArea) senderOperation);
            }
            case setReceiverCardInfoSender:// 设置接收卡参数,发送到发送卡
            {
                boolean bClass = senderOperation instanceof SoSetReceiverCardInfoSender;
                if (!bClass)
                    return false;
                return excuteSetReceiverCardInfoSend((SoSetReceiverCardInfoSender) senderOperation);
            }
            case setReceiverCardInfoSaveToReceiver:// 设置接收卡参数,固化到接收卡
            {
                boolean bClass = senderOperation instanceof SotReceiverCardInfoSaveToReceiver;
                if (!bClass)
                    return false;
                return excuteSetReceiverCardInfoSaveToReceiver((SotReceiverCardInfoSaveToReceiver) senderOperation);
            }
            case getRTC:// 获取RTC时间
            {
                boolean bClass = senderOperation instanceof SoGetRTC;// 获取RTC时间
                if (!bClass)
                    return false;
                return excuteGetRTC();
            }
            case saveUidEncrpt:// 保存uid加密码
            {
                boolean bClass = senderOperation instanceof SoSaveUidEncrpt;// 保存加密码
                if (!bClass)
                    return false;
                return excuteSaveCryptUid(((SoSaveUidEncrpt) senderOperation));
            }
            case setConnectionToSenderCard:// 发送连接关系
            {
                boolean bClass = senderOperation instanceof SoSetConnectionToSenderCard;// 保存加密码
                if (!bClass)
                    return false;
                return excuteSetConnectionToSenderCard(senderOperation);

            }
            case setConnectionToReceicerCard:// 固化连接关系
            {
                boolean bClass = senderOperation instanceof SoSetConnectionToReceicerCard;// 保存加密码
                if (!bClass)
                    return false;
                return excuteSetConnectionToReceicerCard(senderOperation);

            }
            // case setDotArray:// 点阵字体
            // {
            // boolean bClass = senderOperation instanceof SoSetDotArray;//
            // 保存加密码
            // if (!bClass)
            // return false;
            // boolean bOk = excuteSetDotArray((SoSetDotArray) senderOperation);
            // return true;
            // }
            // case closeDotArray:// 关闭点阵字体
            // {
            // boolean bClass = senderOperation instanceof SoCloseDotArray;//
            // 保存加密码
            // if (!bClass)
            // return false;
            // boolean bOk = excuteCloseDotArray((SoCloseDotArray)
            // senderOperation);
            // // EC_setDotArray((SO_SetDotArray) senderOperation);
            // return true;
            // }
            default:
                return false;
        }

    }

   
}
