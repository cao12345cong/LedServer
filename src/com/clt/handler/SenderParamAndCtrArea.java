package com.clt.handler;

import com.clt.commondata.PortArea;
import com.clt.commondata.SenderInfo;

/**
 * 发送卡基本参数及面积控制区域
 *
 */
public class SenderParamAndCtrArea
{
    private SenderInfo senderInfo;

    public SenderParamAndCtrArea(SenderInfo senderInfo)
    {
        this.senderInfo = senderInfo;
    }

    public byte [] getBuffer()
    {
        byte [] buffer = new byte [1544];
        int k=0x00;
        buffer[k++] = (byte) 0x06;// 公共参数
        buffer[k++] = (byte) 0x33;// 5A标志
        buffer[k++] = (byte) 0x00;// LED方向
        buffer[k++] = (byte) 0x00;// 虚拟像素
        buffer[k++] = (byte) 0x00;// 网络包间隙
        buffer[k++] = (byte) 0x01;// 大包标志
        buffer[k++] = (byte) 0x00;// 亮度自动调节
        buffer[k++] = (byte) senderInfo.getFrameRate();// 帧率
        buffer[k++] = (byte) 0x00;// 是否采用发送卡参数
        buffer[k++] = (byte) 0x00;// reserved
        buffer[k++] = (byte) 0x00;// normal mode
        buffer[k++] = (byte) senderInfo.getTenBitFlag();// 10bit标志
        buffer[k++] = (byte) (senderInfo.isBHDCP()?0x01:0x00);// 启用HDCP
        buffer[k++] = (byte) 0x10;// DVI和HDMI
        //第一个网口控制面积
        k=0x10;
        PortArea pa = senderInfo.getPorts()[0];
        buffer[k++] = (byte) (pa.getStartX() % 256);
        buffer[k++] = (byte) (pa.getStartX() / 256);
        buffer[k++] = (byte) (pa.getHeight() % 256);
        buffer[k++] = (byte) (pa.getHeight() / 256);
        buffer[k++] = (byte) (pa.getStarty() % 256);
        buffer[k++] = (byte) (pa.getStarty() / 256);
        buffer[k++] = (byte) (pa.getWidth() % 256);
        buffer[k++] = (byte) (pa.getWidth() / 256);
        
        buffer[k++] = (byte) 0x00;//开启禁止抽点
        buffer[k++] = (byte) 0x00;//水平方向抽点步长
        buffer[k++] = (byte) 0x00;//水平方向每步长内抽第几个点
        buffer[k++] = (byte) 0x00;//垂直方向抽点步长
        buffer[k++] = (byte) 0x00;//垂直方向每步长内抽第几个点
        //第二个网口控制面积
        k=0x400;
        pa = senderInfo.getPorts()[1];
        buffer[k++] = (byte) (pa.getStartX() % 256);
        buffer[k++] = (byte) (pa.getStartX() / 256);
        buffer[k++] = (byte) (pa.getHeight() % 256);
        buffer[k++] = (byte) (pa.getHeight() / 256);
        buffer[k++] = (byte) (pa.getStarty() % 256);
        buffer[k++] = (byte) (pa.getStarty() / 256);
        buffer[k++] = (byte) (pa.getWidth() % 256);
        buffer[k++] = (byte) (pa.getWidth() / 256);
        
        if(senderInfo.getPorts().length>2){
          //第三个网口控制面积
            k=0x500;
            pa = senderInfo.getPorts()[2];
            buffer[k++] = (byte) (pa.getStartX() % 256);
            buffer[k++] = (byte) (pa.getStartX() / 256);
            buffer[k++] = (byte) (pa.getHeight() % 256);
            buffer[k++] = (byte) (pa.getHeight() / 256);
            buffer[k++] = (byte) (pa.getStarty() % 256);
            buffer[k++] = (byte) (pa.getStarty() / 256);
            buffer[k++] = (byte) (pa.getWidth() % 256);
            buffer[k++] = (byte) (pa.getWidth() / 256);
            
            //第四个网口控制面积
            k=0x600;
            pa = senderInfo.getPorts()[3];
            buffer[k++] = (byte) (pa.getStartX() % 256);
            buffer[k++] = (byte) (pa.getStartX() / 256);
            buffer[k++] = (byte) (pa.getHeight() % 256);
            buffer[k++] = (byte) (pa.getHeight() / 256);
            buffer[k++] = (byte) (pa.getStarty() % 256);
            buffer[k++] = (byte) (pa.getStarty() / 256);
            buffer[k++] = (byte) (pa.getWidth() % 256);
            buffer[k++] = (byte) (pa.getWidth() / 256);
        }
        return buffer;
    }
}
