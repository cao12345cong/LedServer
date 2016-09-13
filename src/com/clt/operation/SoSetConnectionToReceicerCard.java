package com.clt.operation;

import com.clt.entity.ConnectionParam;

/**
 * 设置接收卡参数
 * @author Administrator
 *
 */
public class SoSetConnectionToReceicerCard extends SenderOperation
{
    protected ConnectionParam connectionParam;
    public SoSetConnectionToReceicerCard()
    {
        optertorType = OperatorType.setConnectionToReceicerCard;
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
