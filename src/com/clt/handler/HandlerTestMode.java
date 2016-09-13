package com.clt.handler;

import java.io.IOException;
import java.io.OutputStream;

import android.util.Log;

import com.clt.commondata.SenderInfo;
import com.clt.operation.SenderOperation;
import com.clt.operation.SoDetectSender;
import com.clt.operation.SoSetTestMode;
import com.clt.operation.SoShowOnOff;
import com.clt.service.CommandExcutorImpl;
import com.clt.util.FileLogger;

public class HandlerTestMode extends SenderHandler
{
    SoSetTestMode senderOperation;

    public HandlerTestMode(SenderOperation senderOperation,
            CommandExcutorImpl soCommandExcutor)
    {
        super(senderOperation, soCommandExcutor);
        this.senderOperation = (SoSetTestMode) senderOperation;
    }

    @Override
    public boolean doHandler()
    {
        try
        {
            senderOperation.setOperationStep(1);
            

            if (soCommandExcutor.getmOutputStream() == null)
                return false;

            int bufLen = 1;
            byte [] buffer = new byte [9];
            buffer[0] = (byte) 0x99;
            buffer[1] = 0;
            buffer[2] = (byte) ((7 + bufLen + 1) >> 8);
            buffer[3] = (byte) ((7 + bufLen + 1) & 0xFF);
            buffer[4] = (byte) (0x03);
            buffer[5] = 0;
            buffer[6] = (byte) 0xff;
            buffer[7] = (byte) senderOperation.getIndex();
            int sum = 0;
            for (int i = 0; i < buffer.length - 1; i++)
            {
                sum += buffer[i];
            }
            buffer[8] = (byte) sum;

            soCommandExcutor.getmOutputStream().write(buffer, 0, 9);
            soCommandExcutor.getmOutputStream().flush();
            
            return true;
        }
        catch (Exception e)
        {
            
            return false;
        }

        

    }

}
