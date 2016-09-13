package com.clt.operation;

import java.util.ArrayList;

import com.clt.entity.ReceiverSetting;

/**
 * 连接关系
 *
 */
public class SoReceiveCardSetting extends SenderOperation
{
    protected ReceiverSetting receiverSetting;

    public SoReceiveCardSetting()
    {
        optertorType = OperatorType.setReceiverCard;
    }

    public ReceiverSetting getReceiverSetting()
    {
        return receiverSetting;
    }

    public void setReceiverSetting(ReceiverSetting receiverSetting)
    {
        this.receiverSetting = receiverSetting;
    }
    
}
