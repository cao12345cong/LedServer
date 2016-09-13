package com.clt.operation;
/**
 * 设置接收卡参数
 * @author Administrator
 *
 */
public class SoSetReceiverCardInfoSender extends SenderOperation
{

    private String fileName;
    
    private int width;

    private int height;
    
    public SoSetReceiverCardInfoSender()
    {
        optertorType = OperatorType.setReceiverCardInfoSender;
    }
    public String getFileName()
    {
        return fileName;
    }
    public void setFileName(String fileName)
    {
        this.fileName = fileName;
    }
    public int getWidth()
    {
        return width;
    }
    public void setWidth(int width)
    {
        this.width = width;
    }
    public int getHeight()
    {
        return height;
    }
    public void setHeight(int height)
    {
        this.height = height;
    }
    

}
