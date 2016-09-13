package com.clt.handler;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.io.OutputStream;

import android.util.Log;

import com.clt.commondata.SenderInfo;
import com.clt.operation.SenderOperation;
import com.clt.operation.SoDetectSender;
import com.clt.operation.SoSaveUidEncrpt;
import com.clt.operation.SoSetPortArea;
import com.clt.operation.SoSetTestMode;
import com.clt.operation.SoShowOnOff;
import com.clt.parser.UidBinParser;
import com.clt.receiver.SettingReceiver;
import com.clt.service.CommandExcutor;
import com.clt.util.FileLogger;
import com.clt.util.Tools;

public class HandlerSaveCryptUid extends SenderHandler
{

    private SoSaveUidEncrpt senderOperation;

    public HandlerSaveCryptUid(SenderOperation senderOperation,
            CommandExcutor soCommandExcutor)
    {
        super(senderOperation, soCommandExcutor);
        this.senderOperation = (SoSaveUidEncrpt) senderOperation;
    }

    @Override
    public boolean doHandler()
    {
        String dir = senderOperation.getSdcardPath();
        /*
         * 1.encrypt文件夹是否存在，如果不存在，结束；如果存在，下一步 2.txt文件是否存在，如果不存在，创建，程序结束；如果存在，下一步
         * 3.判断uid是否正确，如果不正确，拒绝写入；如果正确，下一步 4.bin文件是否存在，如果存在，直接写；如果不存在
         */

        File dirPath = new File(dir + SettingReceiver.DIR_ENCRYPT);
        if (!dirPath.exists())
        {
            return false;
        }

        byte [] uid = EC_GetUID();
        if (uid == null || uid.length == 0)
        {

            return false;
        }
        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < uid.length; i++)
        {

            sb.append(Tools.formatHexString(Integer.toHexString(uid[i] & 0xff)));
        }
        String txtName = sb.toString();

        File [] txtFiles = dirPath.listFiles(new FileFilter()
        {

            @Override
            public boolean accept(File pathname)
            {

                return pathname.getName().endsWith(".txt");
            }
        });
        int len = txtFiles.length;

        if (len == 0)
        {
            Tools.createFile(dir + SettingReceiver.DIR_ENCRYPT, txtName + ".txt");
            return false;
        }
        else
        {
            int step = 0;
            for (File file : txtFiles)
            {
                if (file.getName().equalsIgnoreCase(txtName + ".txt"))
                {
                    step = 1;
                    break;
                }
            }
            if (step == 0)
            {// 有txt文件，但是uid不符合
                return false;
            }

        }
        File binFile = new File(dir + SettingReceiver.DIR_ENCRYPT, txtName + ".bin");
        if (!binFile.exists())
        {
            return false;
        }
        boolean isOk = doClearUidEncrypt();
        if (!isOk)
        {
            return false;
        }
        try
        {
            Thread.sleep(1000);
        }
        catch (InterruptedException e)
        {

            e.printStackTrace();
        }
        isOk = doWriteUidEncrypt(binFile);
        if (!isOk)
        {
            return false;
        }
        try
        {
            Thread.sleep(1000);
        }
        catch (InterruptedException e)
        {

            e.printStackTrace();
        }
        return doReadUidEncrypt();

    }

    /**
     *  获得 UID值
     * @return
     */
    private byte [] EC_GetUID()
    {
        try
        {
            if (soCommandExcutor.getmOutputStream() == null)
                return null;

            byte [] buffer = new byte [14];
            buffer[0] = (byte) 0x27;
            buffer[1] = (byte) 0x01;//
            for (int i = 2; i < 12; i++)
            {
                buffer[i] = 0x00;
            }

            // spi包装
            byte [] spibuffer = new byte [19];
            spibuffer[0] = (byte) 0xcc;
            spibuffer[1] = (byte) 0x05;
            spibuffer[2] = (byte) 0x00;
            spibuffer[3] = (byte) 0x0e;
            for (int j = 0; j < 14; j++)
            {
                spibuffer[4 + j] = buffer[j];
            }
            soCommandExcutor.setRcvBufLen(0);
            soCommandExcutor.setBatchRead(true);
            soCommandExcutor.setBatchCount(18);

            soCommandExcutor.getmOutputStream().write(spibuffer, 0, 19);
            soCommandExcutor.getmOutputStream().flush();
            // 获取返回给发送卡的信息
            byte [] uid = null;
            for (int i = 0; i < 100; i++)
            {
                Thread.sleep(100);

                if (soCommandExcutor.getRcvBufLen() >= soCommandExcutor
                        .getBatchCount())
                {
                    byte [] rcvBuffer = soCommandExcutor.getRcvBuffer();
                    uid = new byte [12];
                    for (int z = 0; z < 12; z++)
                    {
                        uid[z] = rcvBuffer[6 + z];
                    }
                    soCommandExcutor.clearRcvBuffer();
                    break;

                }

            }

            soCommandExcutor.setRcvBufLen(0);
            soCommandExcutor.setBatchRead(false);
            soCommandExcutor.setBatchCount(0);
            return uid;
        }
        catch (Exception e)
        {
            e.printStackTrace();
            soCommandExcutor.setRcvBufLen(0);
            soCommandExcutor.setBatchRead(false);
            soCommandExcutor.setBatchCount(0);
            return null;
        }

    }

    /**
     * 擦除取armflash数据
     * @return
     */
    public boolean doClearUidEncrypt()
    {
        try
        {
            byte [] buffer = new byte [262];
            buffer[0] = (byte) 0xaa;
            buffer[1] = (byte) 0x30;// 操作码
            buffer[2] = (byte) 0x00;// 操作地址
            buffer[3] = (byte) 0xfc;// 操作地址
            buffer[4] = (byte) 0x00;// 操作地址

            for (int i = 5; i < 262; i++)
            {
                buffer[i] = (byte) 0x00;
            }

            // 转发spi接口帧
            byte [] spiBuffer = new byte [267];
            spiBuffer[0] = (byte) 0xcc;
            spiBuffer[1] = (byte) 0x05;
            spiBuffer[2] = (byte) (262 / 256);// 帧长，高位在前
            spiBuffer[3] = (byte) (262 % 256);
            for (int i = 0; i < 262; i++)
            {
                spiBuffer[i + 4] = buffer[i];
            }
            soCommandExcutor.getmOutputStream().write(spiBuffer, 0, 267);
            soCommandExcutor.getmOutputStream().flush();
        }
        catch (IOException e)
        {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    /**
     * 写uid加密码
     * @param binFile
     * @return
     */
    public boolean doWriteUidEncrypt(File binFile)
    {
        try
        {
            byte [] cryptUid = UidBinParser.getByteFromBin(binFile);

            byte [] buffer = new byte [261];

            buffer[0] = (byte) 0xaa;
            buffer[1] = (byte) 0x32;
            buffer[2] = (byte) 0x00;
            buffer[3] = (byte) 0xfc;
            buffer[4] = (byte) 0x00;

            for (int i = 0; i < 256; i++)
            {
                buffer[i + 5] = cryptUid[i];// 亮度值
            }

            // 转发spi接口帧
            byte [] spiBuffer = new byte [265];
            spiBuffer[0] = (byte) 0xcc;
            spiBuffer[1] = (byte) 0x05;
            spiBuffer[2] = (byte) (265 / 256);// 帧长，高位在前
            spiBuffer[3] = (byte) (265 % 256);
            for (int i = 0; i < 261; i++)
            {
                spiBuffer[i + 4] = buffer[i];
            }
            soCommandExcutor.getmOutputStream().write(spiBuffer, 0, 265);
            soCommandExcutor.getmOutputStream().flush();
        }
        catch (IOException e)
        {
            e.printStackTrace();
            return false;
        }

        return true;
    }

    /**
     * 读取armflash数据
     * @return
     */
    public boolean doReadUidEncrypt()
    {
        try
        {
            // 操作ARM FLASH协议帧格式
            byte [] buffer = new byte [262];
            buffer[0] = (byte) 0xaa;
            buffer[1] = (byte) 0x31;// 操作码 读
            buffer[2] = (byte) 0x00;// 操作地址
            buffer[3] = (byte) 0xfc;// 操作地址
            buffer[4] = (byte) 0x00;// 操作地址

            for (int i = 5; i < 262; i++)
            {
                buffer[i] = (byte) 0x00;
            }
            // 转发spi接口帧
            byte [] spiBuffer = new byte [267];
            spiBuffer[0] = (byte) 0xcc;
            spiBuffer[1] = (byte) 0x05;
            spiBuffer[2] = (byte) (265 / 256);// 帧长，高位在前
            spiBuffer[3] = (byte) (265 % 256);
            for (int i = 0; i < 262; i++)
            {
                spiBuffer[i + 4] = buffer[i];
            }

            soCommandExcutor.setRcvBufLen(0);
            soCommandExcutor.setBatchRead(true);
            soCommandExcutor.setBatchCount(265);
            soCommandExcutor.getmOutputStream().write(spiBuffer, 0, 267);
            soCommandExcutor.getmOutputStream().flush();

            // 等待回复
            boolean bOk = false;
            for (int i = 0; i < 100; i++)
            {
                Thread.sleep(100);

                if (soCommandExcutor.getRcvBufLen() >= soCommandExcutor
                        .getBatchCount())
                {
                    byte [] rcvBuffer = soCommandExcutor.getRcvBuffer();
                    bOk=true;
                    soCommandExcutor.clearRcvBuffer();
                    break;

                }

            }

            soCommandExcutor.setRcvBufLen(0);
            soCommandExcutor.setBatchRead(false);
            soCommandExcutor.setBatchCount(0);
            return bOk;
        }
        catch (Exception e)
        {
            e.printStackTrace();
            soCommandExcutor.setRcvBufLen(0);
            soCommandExcutor.setBatchRead(false);
            soCommandExcutor.setBatchCount(0);
            return false;
        }
    }
}
