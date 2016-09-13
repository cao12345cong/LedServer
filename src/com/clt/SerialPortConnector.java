package com.clt;

import com.clt.commondata.SenderInfo;
import com.clt.operation.SenderOperation;

import android.content.Intent;

/**
 * 串口通信
 */
public interface SerialPortConnector extends LifeCycle
{
    
    /**
     * 设置tcp连接器
     */
    void setWrapper(Wrapper tcpConnector);
    
    void addOperation(SenderOperation senderOperation);
    
    SenderInfo getSenderInfo();
}
