package com.clt.operation;

import com.clt.entity.ConnectionParam;

/**
 * 设置接收卡参数
 * @author Administrator
 *
 */
public class SoSetConnectionToSenderCard extends SenderOperation
{
    private ConnectionParam connectionParam;
    public SoSetConnectionToSenderCard()
    {
        optertorType = OperatorType.setConnectionToSenderCard;
    }
    public ConnectionParam getConnectionParam()
    {
        return connectionParam;
    }
    public void setConnectionParam(ConnectionParam connectionParam)
    {
        this.connectionParam = connectionParam;
    }


}
