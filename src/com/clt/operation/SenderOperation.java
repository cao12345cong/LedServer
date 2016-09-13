package com.clt.operation;

/**
 * 发送卡操作类
 */
public abstract class SenderOperation 
{
    // 探测发送卡，设置亮度和色温，显示或关闭，设置基本参数
    public enum OperatorType
    {
        invalidType, //
        detectSender, // 探测发送卡
        setBrightAndClrT, // 设置亮度色温
        saveBrightAndClrT, // 保存亮度色温
        showOnOff, // 开关屏
        setBasicParameters, // 设置基本参数
        setTestMode, // 设置测试模式
        setEDID, // 设置EDID
        setDayPeriodBright, // 分时段亮度调节
        setReceiverCard, // 设置接收卡
        setAutoBrightness, // 设置亮度调节曲线
        setAreaPortByXML, // 设置网口面积
        setReceiverCardInfoSender, // 设置接收卡基本参数,发送到发送卡
        setReceiverCardInfoSaveToReceiver, // 设置接收卡基本参数,固化到接收卡
        setConnectionToSenderCard, // 发送连接关系到发送卡
        setConnectionToReceicerCard, // 固化连接关系到接收卡
        getRTC, // 获得实时时间
        saveUidEncrpt, // 保存uid加密码
        setDotArray, // 点阵字体
        closeDotArray// 关闭点阵字
    }

    protected OperatorType optertorType = OperatorType.invalidType;//命令标识

    protected int operationStep = 0;//处理进度

    protected boolean bFinished = false;//处理完毕，进行下一个任务
    
    protected int clientType=ClientType.WIFI;
    
    public static class ClientType{
        public static final int WIFI=1;
        
        public static final int Ethernet=2;
    }
   


    public OperatorType getOptertorType()
    {
        return optertorType;
    }

    public void setOptertorType(OperatorType optertorType)
    {
        this.optertorType = optertorType;
    }

    public int getOperationStep()
    {
        return operationStep;
    }

    public void setOperationStep(int operationStep)
    {
        this.operationStep = operationStep;
    }

    public boolean isbFinished()
    {
        return bFinished;
    }

    public void setbFinished(boolean bFinished)
    {
        this.bFinished = bFinished;
    }

    public int getClientType()
    {
        return clientType;
    }

    public void setClientType(int clientType)
    {
        this.clientType = clientType;
    }
    
    

}
