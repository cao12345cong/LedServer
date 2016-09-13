package com.clt.operation;

public class SoSetEDID extends SenderOperation
{
    protected int width;

    protected int height;

    protected int freq;

    protected int senderIndex = 0;

    public int getSenderIndex()
    {
        return senderIndex;
    }

    public void setSenderIndex(int senderIndex)
    {
        this.senderIndex = senderIndex;
    }

    public SoSetEDID()
    {
        optertorType = OperatorType.setEDID;
    }

    public int getWidth()
    {
        return width;
    }

    public void setWidth(int width)
    {
        this.width = width;
    }

    public int getHeight()
    {
        return height;
    }

    public void setHeight(int height)
    {
        this.height = height;
    }

    public int getFreq()
    {
        return freq;
    }

    public void setFreq(int freq)
    {
        this.freq = freq;
    }

}
