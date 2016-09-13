package com.clt;
/**
 * 串口处理的包装
 */
public interface Wrapper extends LifeCycle
{
    /**
     * 接收一条信息
     * @param jsonStr
     */
    void inputOneMessage(String message);
    
    void outputOneMessage(String message);
    
    void setSerialPortConnector(SerialPortConnector serialPortConnector);
    
    void setTCPConnector(TCPConnector tcpConnector);
    
    
}
