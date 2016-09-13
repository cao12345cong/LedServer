package com.clt.operation;

import com.clt.commondata.SenderParameters;

public class SoSetBasicParameters extends SenderOperation
{

    protected int senderIndex = 0;

    protected SenderParameters senderParameters = new SenderParameters();

    public int getSenderIndex()
    {
        return senderIndex;
    }

    public void setSenderIndex(int senderIndex)
    {
        this.senderIndex = senderIndex;
    }

    public SoSetBasicParameters()
    {
        optertorType = OperatorType.setBasicParameters;
    }

    public SenderParameters getSenderParameters()
    {
        return senderParameters;
    }

    public void setSenderParameters(SenderParameters senderParameters)
    {
        this.senderParameters = senderParameters;
    }

}
