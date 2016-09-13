package com.clt.handler;

import java.io.IOException;

import com.clt.operation.SenderOperation;
import com.clt.operation.SoSetBrightAndClrT;
import com.clt.service.CommandExcutor;

public class HandlerSetBriAndClT extends SenderHandler
{

    private SoSetBrightAndClrT senderOperation;

    public HandlerSetBriAndClT(SenderOperation senderOperation,
            CommandExcutor soCommandExcutor)
    {
        super(senderOperation, soCommandExcutor);
        this.senderOperation = (SoSetBrightAndClrT) senderOperation;
    }

    @Override
    public boolean doHandler()
    {
        try
        {
            if (soCommandExcutor.getmOutputStream() == null)
            {
                
                return false;
            }

            byte [] buffer = new byte [32];
            buffer[0] = (byte) 0x80;
            buffer[1] = (byte) 0x11;
            buffer[2] = (byte) 0x22;
            buffer[3] = (byte) 0x33;
            buffer[4] = (byte) 0x44;

            buffer[5] = (byte) senderOperation.getBirght();// 亮度值
            buffer[6] = (byte) senderOperation.getrBirght();
            buffer[7] = (byte) senderOperation.getgBright();
            buffer[8] = (byte) senderOperation.getbBright();
            buffer[9] = (byte) 0x00;// 虚红

            int colorTemp = senderOperation.getColorTemperature();

            buffer[10] = (byte) (colorTemp / 256);
            buffer[11] = (byte) (colorTemp % 256);

            buffer[12] = (byte) 0xff;
            buffer[13] = (byte) 0x00;

            soCommandExcutor.getmOutputStream().write(buffer, 0, 14);
            soCommandExcutor.getmOutputStream().flush();
            
            return true;
        }
        catch (IOException e)
        {
            
            return false;
        }
       
    }

}
