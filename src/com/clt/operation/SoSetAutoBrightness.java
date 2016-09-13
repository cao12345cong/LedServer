package com.clt.operation;

public class SoSetAutoBrightness extends SenderOperation
{

    protected int senderIndex = 0;
    
    protected byte[] buffer=new byte[256];
    
    protected int isAuto;

    public int getSenderIndex()
    {
        return senderIndex;
    }

    public void setSenderIndex(int senderIndex)
    {
        this.senderIndex = senderIndex;
    }

    public SoSetAutoBrightness()
    {
        optertorType = OperatorType.setAutoBrightness;
    }

    public byte [] getBuffer()
    {
        return buffer;
    }

    public void setBuffer(byte [] buffer)
    {
        this.buffer = buffer;
    }

    public int getIsAuto()
    {
        return isAuto;
    }

    public void setIsAuto(int isAuto)
    {
        this.isAuto = isAuto;
    }
    
    
    
}
