package com.clt.handler;

import java.io.IOException;
import java.io.OutputStream;

import android.util.Log;

import com.clt.commondata.SenderInfo;
import com.clt.operation.SenderOperation;
import com.clt.operation.SoDetectSender;
import com.clt.operation.SoShowOnOff;
import com.clt.service.CommandExcutorImpl;
import com.clt.util.FileLogger;

public class HandlerSetOnOff extends SenderHandler
{
    SoShowOnOff senderOperation;

    public HandlerSetOnOff(SenderOperation senderOperation,
            CommandExcutorImpl soCommandExcutor)
    {
        super(senderOperation, soCommandExcutor);
        this.senderOperation = (SoShowOnOff) senderOperation;
    }

    @Override
    public boolean doHandler()
    {
        try
        {
            if (soCommandExcutor.getmOutputStream() == null)
                return false;

            byte [] buffer = new byte [32];
            buffer[0] = (byte) 0x81;
            buffer[1] = (byte) 0x11;
            buffer[2] = (byte) 0x22;
            buffer[3] = (byte) 0x33;
            buffer[4] = (byte) 0x44;

            if (senderOperation.isbShowOnOff())
                buffer[5] = (byte) 0x01;
            else
                buffer[5] = (byte) 0x00;

            buffer[6] = (byte) 0xff;
            buffer[7] = (byte) 0x00;

            soCommandExcutor.getmOutputStream().write(buffer, 0, 8);
            soCommandExcutor.getmOutputStream().flush();
            
            return true;
        }
        catch (IOException e)
        {
            
            return false;
        }

    }

}
