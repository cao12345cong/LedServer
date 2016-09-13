package com.clt.operation;

public class SoSetTestMode extends SenderOperation
{
    protected int index;

    public SoSetTestMode()
    {
        optertorType = OperatorType.setTestMode;
    }

    public int getIndex()
    {
        return index;
    }

    public void setIndex(int index)
    {
        this.index = index;
    }

}
