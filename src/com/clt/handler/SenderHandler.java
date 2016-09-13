package com.clt.handler;

import java.io.OutputStream;

import com.clt.operation.SenderOperation;
import com.clt.service.CommandExcutor;
/**
 * 发送卡操作
 *
 */
public abstract class SenderHandler implements IHandler
{
    public CommandExcutor soCommandExcutor;
    
    
    public SenderHandler(SenderOperation senderOperation,CommandExcutor soCommandExcutor)
    {
        this.soCommandExcutor=soCommandExcutor;
    }
    
    
}
