package com.clt.operation;

public class SoSetBrightAndClrT extends SenderOperation
{

    protected int birght = 255;

    protected int rBirght = 255;

    protected int gBright = 255;

    protected int bBright = 255;

    protected int colorTemperature = 6500;

    public SoSetBrightAndClrT()
    {
        optertorType = OperatorType.setBrightAndClrT;
    }

    public int getBirght()
    {
        if (birght < 0)
            return 0;
        if (birght > 255)
            return 255;

        return birght;
    }

    public void setBirght(int birght)
    {
        this.birght = birght;
    }

    public int getrBirght()
    {
        if (rBirght < 0)
            return 0;
        if (rBirght > 255)
            return 255;

        return rBirght;
    }

    public void setrBirght(int rBirght)
    {
        this.rBirght = rBirght;
    }

    public int getgBright()
    {
        if (gBright < 0)
            return 0;
        if (gBright > 255)
            return 255;

        return gBright;
    }

    public void setgBright(int gBright)
    {
        this.gBright = gBright;
    }

    public int getbBright()
    {
        if (bBright < 0)
            return 0;
        if (bBright > 255)
            return 255;

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
