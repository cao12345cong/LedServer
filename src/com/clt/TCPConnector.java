package com.clt;

/**
 * TCP连接器
 */
public interface TCPConnector extends LifeCycle
{
    /**
     * 发送一条消息
     * @param message
     */
    void responseOneMessage(String message);
    
    void setWrapper(Wrapper wrapper); 
}
