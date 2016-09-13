package com.clt.operation;

public class SoShowOnOff extends SenderOperation
{

    public SoShowOnOff()
    {
        optertorType = OperatorType.showOnOff;
    }

    public boolean isbShowOnOff()
    {
        return bShowOnOff;
    }

    public void setbShowOnOff(boolean bShowOnOff)
    {
        this.bShowOnOff = bShowOnOff;
    }

    protected boolean bShowOnOff;

}
