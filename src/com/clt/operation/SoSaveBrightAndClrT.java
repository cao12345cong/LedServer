package com.clt.operation;

public class SoSaveBrightAndClrT extends SenderOperation
{

    protected int senderIndex = 0;

    protected int birght = 255;

    protected int rBirght = 255;

    protected int gBright = 255;

    protected int bBright = 255;

    protected int colorTemperature = 6500;

    public SoSaveBrightAndClrT()
    {
        optertorType = OperatorType.saveBrightAndClrT;
    }

    public int getSenderIndex()
    {
        return senderIndex;
    }

    public void setSenderIndex(int senderIndex)
    {
        this.senderIndex = senderIndex;
    }

    public int getBirght()
    {
        return birght;
    }

    public void setBirght(int birght)
    {
        this.birght = birght;
    }

    public int getrBirght()
    {
        return rBirght;
    }

    public void setrBirght(int rBirght)
    {
        this.rBirght = rBirght;
    }

    public int getgBright()
    {
        return gBright;
    }

    public void setgBright(int gBright)
    {
        this.gBright = gBright;
    }

    public int getbBright()
    {
        return bBright;
    }

    public void setbBright(int bBright)
    {
        this.bBright = bBright;
    }

    public int getColorTemperature()
    {
        return colorTemperature;
    }

    public void setColorTemperature(int colorTemperature)
    {
        this.colorTemperature = colorTemperature;
    }

}
