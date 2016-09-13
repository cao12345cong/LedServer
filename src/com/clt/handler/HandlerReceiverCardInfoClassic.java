package com.clt.handler;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;

import com.clt.operation.SenderOperation;
import com.clt.operation.SoSetReceiverCardInfoSender;
import com.clt.operation.SotReceiverCardInfoSaveToReceiver;
import com.clt.parser.ReceiverSettingBinParser;
import com.clt.service.CommandExcutor;
import com.clt.service.CommandExcutorImpl;
import com.clt.util.Config;
import com.clt.util.Constants;

/**
 * 对接收卡参数的处理(经典模式下)
 * 
 * @author caocong
 * 
 */
public class HandlerReceiverCardInfoClassic extends SenderHandler
{
	private SenderOperation senderOperation;
	
	private File binFile;
	
	private byte[] buffer;
	
	private int width;// 箱体宽
	
	private int height;// 箱体高
	
	public HandlerReceiverCardInfoClassic(SenderOperation senderOperation,
			CommandExcutor soCommandExcutor)
	{
		super(senderOperation, soCommandExcutor);
		this.senderOperation = senderOperation;
	}
	
	/**
	 * 固化到接收卡
	 * 
	 * @return
	 * @throws IOException
	 */
	public boolean saveToReceiverCard() throws IOException
	{
		
		boolean isOk = false;
		isOk = detectReceiverCard();
		if (!isOk)
		{
			return false;
		}
		// 擦除
		isOk = doClearReceiverCard();
		if (!isOk)
		{
			return false;
		}
		// byte [] buffer = ReceiverSettingBinParser.getAllByteFromBin(new File(
		// Constants.SDCARD_PATH, "read.bin"));
		// 基本参数
		for (int i = 0x01; i <= 0x04; i++)
		{
			isOk = doWriteByBroadcast(i, buffer);
			if (!isOk)
			{
				return false;
			}
		}
		// gamma表
		for (int i = 0x10; i <= 0x12; i++)
		{
			isOk = doWriteByBroadcast(i, buffer);
			if (!isOk)
			{
				return false;
			}
		}
		for (int i = 0x14; i <= 0x25; i++)
		{
			isOk = doWriteByBroadcast(i, buffer);
			if (!isOk)
			{
				return false;
			}
		}
		// Route表
		for (int i = 0x30; i <= 0x3e; i++)
		{
			isOk = doWriteByBroadcast(i, buffer);
			if (!isOk)
			{
				return false;
			}
		}
		
		// 扫描调度表
		for (int i = 0x60; i <= 0x63; i++)
		{
			isOk = doWriteByBroadcast(i, buffer);
			if (!isOk)
			{
				return false;
			}
		}
		// 参数备份??????
		for (int i = 0x80; i <= 0x85; i++)
		{
			
		}
		// 基本参数
		isOk = doWriteByBroadcast(0, buffer);
		if (!isOk)
		{
			return false;
		}
		// // 广播,
		// for (int i = 1; i < time; i++)
		// {
		// isOk = doWriteByBroadcastReceiverCard(i, buffer);
		// if (!isOk)
		// {
		// return false;
		// }
		// }
		// 写，step1.发送广播 step2.0-1023逐个发送基本参数
		for (int i = 0; i < 27; i++)
		{
			isOk = doWriteOneByOneReceiverCard(i, buffer);
			if (!isOk)
			{
				return false;
			}
		}
		// 刷新基本参数
		isOk = doUpdateReceiverCard();
		if (!isOk)
		{
			return false;
		}
		return true;
	}
	
	/**
	 * 发送到发送卡
	 * 
	 * @return
	 * @throws IOException
	 */
	public boolean sendToSenderCard() throws IOException
	{
		boolean isOk = false;
		// 探测发送卡
		// 擦除
		isOk = doClearSenderCard();
		if (!isOk)
		{
			return false;
		}
////		File file = new File(Constants.SDCARD_PATH, "read.bin");
//		if (!binFile.exists())
//		{
//			return false;
//		}
//		// 写
//		byte[] buffer = ReceiverSettingBinParser.getByteFromBin(file);
		int time = buffer.length / 256;
		// 广播
		for (int i = 0; i < time; i++)
		{
			isOk = doWriteSenderCard(i, buffer);
			if (!isOk)
			{
				return false;
			}
		}
		return true;
	}
	
	/**
	 * 探测接收卡
	 * 
	 * @return
	 */
	public boolean detectReceiverCard()
	{
		byte[] sendBuffer = new byte[280];
		sendBuffer[0] = (byte) 0xcc;
		sendBuffer[1] = (byte) 0x00;// 网口序号
		sendBuffer[2] = (byte) (280 / 256);
		sendBuffer[3] = (byte) (280 % 256);
		sendBuffer[4] = (byte) 0x07;// flash操作
		sendBuffer[5] = (byte) 0x00;
		sendBuffer[6] = (byte) 0x00;
		sendBuffer[7] = (byte) 0x00;
		sendBuffer[8] = (byte) 0x00;
		sendBuffer[9] = (byte) 0x00;
		for (int j = 0; j < 270; j++)
		{
			sendBuffer[10 + j] = 0x00;
		}
		
		try
		{
			soCommandExcutor.setRcvBufLen(0);
			soCommandExcutor.setBatchRead(true);
			soCommandExcutor.setBatchCount(6);
			
			soCommandExcutor.getmOutputStream().write(sendBuffer, 0, 280);
			soCommandExcutor.getmOutputStream().flush();
		}
		catch (IOException e)
		{
			e.printStackTrace();
			
			soCommandExcutor.setRcvBufLen(0);
			soCommandExcutor.setBatchRead(false);
			soCommandExcutor.setBatchCount(0);
			
			return false;
		}
		
		boolean bOk = false;
		for (int i = 0; i < 100; i++)
		{
			
			try
			{
				Thread.sleep(100);
			}
			catch (InterruptedException e)
			{
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			if (soCommandExcutor.getRcvBufLen() >= soCommandExcutor
					.getBatchCount())
			{
				byte[] rcvBuffer = soCommandExcutor.getRcvBuffer();
				
				int succeededFlag = rcvBuffer[0] & ((byte) 0xff);
				if (rcvBuffer[0] == (byte) 0xef)
				{
					bOk = true;
					break;
				}
			}
			
		}
		soCommandExcutor.setRcvBufLen(0);
		soCommandExcutor.setBatchRead(false);
		soCommandExcutor.setBatchCount(0);
		return bOk;
	}
	
	/*********************** 固化 ***************************************/
	/**
	 * 广播 擦
	 */
	private boolean doClearReceiverCard()
	{
		byte[] sendBuffer = new byte[136];
		sendBuffer[0] = (byte) 0xcc;
		sendBuffer[1] = (byte) 0x00;// 网口序号
		sendBuffer[2] = (byte) (136 / 256);
		sendBuffer[3] = (byte) (136 % 256);
		sendBuffer[4] = (byte) 0x06;// flash操作
		sendBuffer[5] = (byte) 0x00;
		sendBuffer[6] = (byte) 0x00;
		sendBuffer[7] = (byte) 0xff;// 广播
		sendBuffer[8] = (byte) 0xff;
		sendBuffer[9] = (byte) 0x23;// 擦除
		sendBuffer[10] = (byte) 0x00;// 是否需要返回值
		sendBuffer[11] = (byte) 0x07;
		sendBuffer[12] = (byte) 0x00;
		sendBuffer[13] = (byte) 0x00;
		for (int j = 0; j < 122; j++)
		{
			sendBuffer[14 + j] = 0x00;
		}
		try
		{
			soCommandExcutor.clearRcvBuffer();
			soCommandExcutor.setRcvBufLen(0);
			soCommandExcutor.setBatchRead(true);
			soCommandExcutor.setBatchCount(6);
			
			soCommandExcutor.getmOutputStream().write(sendBuffer, 0, 136);
			soCommandExcutor.getmOutputStream().flush();
		}
		catch (IOException e)
		{
			e.printStackTrace();
			soCommandExcutor.setRcvBufLen(0);
			soCommandExcutor.setBatchRead(false);
			soCommandExcutor.setBatchCount(0);
			return false;
		}
		
		boolean bOk = false;
		for (int i = 0; i < 100; i++)
		{
			
			try
			{
				Thread.sleep(100);
			}
			catch (InterruptedException e)
			{
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			if (soCommandExcutor.getRcvBufLen() >= soCommandExcutor
					.getBatchCount())
			{
				byte[] rcvBuffer = soCommandExcutor.getRcvBuffer();
				
				if (rcvBuffer[0] == (byte) 0xef)
				{
					bOk = true;
					break;
				}
			}
			
		}
		soCommandExcutor.setRcvBufLen(0);
		soCommandExcutor.setBatchRead(false);
		soCommandExcutor.setBatchCount(0);
		return bOk;
	}
	
	/**
	 * 广播 写
	 * 
	 * @param buffer
	 */
	private boolean doWriteByBroadcast(int index, byte[] buffer)
	{
		byte[] sendBuffer = new byte[274];
		sendBuffer[0] = (byte) 0xcc;
		sendBuffer[1] = (byte) 0x00;// 网口序号
		sendBuffer[2] = (byte) (274 / 256);
		sendBuffer[3] = (byte) (274 % 256);
		sendBuffer[4] = (byte) 0x06;// flash操作
		sendBuffer[5] = (byte) 0x00;// 接收卡序号
		sendBuffer[6] = (byte) 0x00;
		sendBuffer[7] = (byte) 0xff;// 目标卡号
		sendBuffer[8] = (byte) 0xff;
		sendBuffer[9] = (byte) 0x85;// 写
		sendBuffer[10] = (byte) 0x00;// 是否需要返回值
		sendBuffer[11] = (byte) 0x07;// flash地址
		sendBuffer[12] = (byte) index;
		sendBuffer[13] = (byte) 0x00;
		for (int j = 0; j < 256; j++)
		{
			sendBuffer[14 + j] = buffer[index * 256 + j];
		}
		
		try
		{
			soCommandExcutor.setRcvBufLen(0);
			soCommandExcutor.setBatchRead(true);
			soCommandExcutor.setBatchCount(6);
			
			soCommandExcutor.getmOutputStream().write(sendBuffer, 0, 274);
			soCommandExcutor.getmOutputStream().flush();
		}
		catch (IOException e)
		{
			e.printStackTrace();
			
			soCommandExcutor.setRcvBufLen(0);
			soCommandExcutor.setBatchRead(false);
			soCommandExcutor.setBatchCount(0);
			
			return false;
		}
		
		boolean bOk = false;
		for (int i = 0; i < 100; i++)
		{
			
			try
			{
				Thread.sleep(100);
			}
			catch (InterruptedException e)
			{
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			if (soCommandExcutor.getRcvBufLen() >= soCommandExcutor
					.getBatchCount())
			{
				byte[] rcvBuffer = soCommandExcutor.getRcvBuffer();
				
				int succeededFlag = rcvBuffer[0] & ((byte) 0xff);
				if (rcvBuffer[0] == (byte) 0xef)
				{
					bOk = true;
					break;
				}
			}
			
		}
		
		soCommandExcutor.setRcvBufLen(0);
		soCommandExcutor.setBatchRead(false);
		soCommandExcutor.setBatchCount(0);
		
		return bOk;
	}
	
	/**
	 * 广播 依次写基本参数到1024张接收卡
	 * 
	 * @param buffer
	 */
	private boolean doWriteOneByOneReceiverCard(int index, byte[] buffer)
	{
		byte[] sendBuffer = new byte[274];
		sendBuffer[0] = (byte) 0xcc;
		sendBuffer[1] = (byte) 0x00;// 网口序号
		sendBuffer[2] = (byte) (274 / 256);
		sendBuffer[3] = (byte) (274 % 256);
		sendBuffer[4] = (byte) 0x06;// flash操作
		sendBuffer[5] = (byte) 0x00;// 接收卡序号
		sendBuffer[6] = (byte) 0x00;
		sendBuffer[7] = (byte) (index / 256);// 目标卡号
		sendBuffer[8] = (byte) (index % 256);
		sendBuffer[9] = (byte) 0x85;// 写
		sendBuffer[10] = (byte) 0x00;// 是否需要返回值
		sendBuffer[11] = (byte) 0x07;// flash地址
		sendBuffer[12] = (byte) 0x00;
		sendBuffer[13] = (byte) 0x00;
		for (int j = 0; j < 256; j++)
		{
			sendBuffer[14 + j] = buffer[j];
		}
		
		try
		{
			soCommandExcutor.setRcvBufLen(0);
			soCommandExcutor.setBatchRead(true);
			soCommandExcutor.setBatchCount(6);
			
			soCommandExcutor.getmOutputStream().write(sendBuffer, 0, 274);
			soCommandExcutor.getmOutputStream().flush();
		}
		catch (IOException e)
		{
			e.printStackTrace();
			
			soCommandExcutor.setRcvBufLen(0);
			soCommandExcutor.setBatchRead(false);
			soCommandExcutor.setBatchCount(0);
			
			return false;
		}
		
		boolean bOk = false;
		for (int i = 0; i < 100; i++)
		{
			
			try
			{
				Thread.sleep(100);
			}
			catch (InterruptedException e)
			{
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			if (soCommandExcutor.getRcvBufLen() >= soCommandExcutor
					.getBatchCount())
			{
				byte[] rcvBuffer = soCommandExcutor.getRcvBuffer();
				
				int succeededFlag = rcvBuffer[0] & ((byte) 0xff);
				if (succeededFlag == (byte) 0xef)
				{
					bOk = true;
					break;
				}
			}
			
		}
		
		soCommandExcutor.setRcvBufLen(0);
		soCommandExcutor.setBatchRead(false);
		soCommandExcutor.setBatchCount(0);
		
		return bOk;
	}
	
	// 更新控制参数
	public boolean doUpdateReceiverCard()
	{
		byte[] sendBuffer = new byte[136];
		sendBuffer[0] = (byte) 0xcc;
		sendBuffer[1] = (byte) 0x00;// 网口序号
		sendBuffer[2] = (byte) 0x00;
		sendBuffer[3] = (byte) 0x88;
		sendBuffer[4] = (byte) 0x06;// flash操作
		sendBuffer[5] = (byte) 0x00;
		sendBuffer[6] = (byte) 0x00;
		sendBuffer[7] = (byte) 0xff;// 广播
		sendBuffer[8] = (byte) 0xff;
		sendBuffer[9] = (byte) 0x77;// 擦除
		sendBuffer[10] = (byte) 0x00;
		sendBuffer[11] = (byte) 0x07;
		sendBuffer[12] = (byte) 0x00;
		sendBuffer[13] = (byte) 0x00;
		for (int j = 0; j < 122; j++)
		{
			sendBuffer[14 + j] = 0x00;
		}
		
		try
		{
			soCommandExcutor.setRcvBufLen(0);
			soCommandExcutor.setBatchRead(true);
			soCommandExcutor.setBatchCount(6);
			
			soCommandExcutor.getmOutputStream().write(sendBuffer, 0, 136);
			soCommandExcutor.getmOutputStream().flush();
		}
		catch (IOException e)
		{
			e.printStackTrace();
			
			soCommandExcutor.setRcvBufLen(0);
			soCommandExcutor.setBatchRead(false);
			soCommandExcutor.setBatchCount(0);
			
			return false;
		}
		
		boolean bOk = false;
		for (int i = 0; i < 100; i++)
		{
			
			try
			{
				Thread.sleep(100);
			}
			catch (InterruptedException e)
			{
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			if (soCommandExcutor.getRcvBufLen() >= soCommandExcutor
					.getBatchCount())
			{
				byte[] rcvBuffer = soCommandExcutor.getRcvBuffer();
				
				if (rcvBuffer[0] == (byte) 0xef)
				{
					bOk = true;
					break;
				}
			}
			
		}
		
		soCommandExcutor.setRcvBufLen(0);
		soCommandExcutor.setBatchRead(false);
		soCommandExcutor.setBatchCount(0);
		
		return bOk;
	}
	
	/*********************** 发送 ***************************************/
	/**
	 * 广播 擦
	 */
	private boolean doClearSenderCard()
	{
		
		byte[] sendBuffer = new byte[262];
		sendBuffer[0] = (byte) 0xaa;
		sendBuffer[1] = (byte) 0x67;// 网口序号
		sendBuffer[2] = (byte) 0x00;// 操作地址
		sendBuffer[3] = (byte) 0x21;
		sendBuffer[4] = (byte) 0x00;
		for (int j = 0; j < 256; j++)
		{
			sendBuffer[5 + j] = 0x00;
		}
		
		try
		{
			soCommandExcutor.setRcvBufLen(0);
			soCommandExcutor.setBatchRead(true);
			soCommandExcutor.setBatchCount(6);
			
			soCommandExcutor.getmOutputStream().write(sendBuffer, 0, 262);
			soCommandExcutor.getmOutputStream().flush();
		}
		catch (IOException e)
		{
			e.printStackTrace();
			
			soCommandExcutor.setRcvBufLen(0);
			soCommandExcutor.setBatchRead(false);
			soCommandExcutor.setBatchCount(0);
			
			return false;
		}
		
		boolean bOk = false;
		for (int i = 0; i < 100; i++)
		{
			
			try
			{
				Thread.sleep(100);
			}
			catch (InterruptedException e)
			{
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			if (soCommandExcutor.getRcvBufLen() >= soCommandExcutor
					.getBatchCount())
			{
				byte[] rcvBuffer = soCommandExcutor.getRcvBuffer();
				
				int succeededFlag = rcvBuffer[0] & ((byte) 0xff);
				if (rcvBuffer[0] == (byte) 0xaa)
				{
					bOk = true;
					break;
				}
			}
			
		}
		soCommandExcutor.setRcvBufLen(0);
		soCommandExcutor.setBatchRead(false);
		soCommandExcutor.setBatchCount(0);
		
		return bOk;
	}
	
	/**
	 * 写
	 */
	private boolean doWriteSenderCard(int index, byte[] buffer)
	{
		
		byte[] sendBuffer = new byte[262];
		sendBuffer[0] = (byte) 0xaa;
		sendBuffer[1] = (byte) 0x77;// 网口序号
		sendBuffer[2] = (byte) 0x00;// 操作地址
		sendBuffer[3] = (byte) 0x21;
		sendBuffer[4] = (byte) index;
		for (int j = 0; j < 256; j++)
		{
			sendBuffer[5 + j] = buffer[index * 256 + j];
		}
		
		try
		{
			soCommandExcutor.setRcvBufLen(0);
			soCommandExcutor.setBatchRead(true);
			soCommandExcutor.setBatchCount(6);
			
			soCommandExcutor.getmOutputStream().write(sendBuffer, 0, 262);
			soCommandExcutor.getmOutputStream().flush();
		}
		catch (IOException e)
		{
			e.printStackTrace();
			
			soCommandExcutor.setRcvBufLen(0);
			soCommandExcutor.setBatchRead(false);
			soCommandExcutor.setBatchCount(0);
			
			return false;
		}
		
		boolean bOk = false;
		for (int i = 0; i < 100; i++)
		{
			
			try
			{
				Thread.sleep(100);
			}
			catch (InterruptedException e)
			{
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			if (soCommandExcutor.getRcvBufLen() >= soCommandExcutor
					.getBatchCount())
			{
				byte[] rcvBuffer = soCommandExcutor.getRcvBuffer();
				
				int succeededFlag = rcvBuffer[0] & ((byte) 0xff);
				if (succeededFlag == (byte) 0xaa)
				{
					bOk = true;
					break;
				}
			}
			
		}
		
		soCommandExcutor.setRcvBufLen(0);
		soCommandExcutor.setBatchRead(false);
		soCommandExcutor.setBatchCount(0);
		
		return bOk;
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
			if (senderOperation instanceof SotReceiverCardInfoSaveToReceiver)
			{// 固化
				String dir = Constants.SDCARD_PATH;
				this.binFile = new File(dir,
						((SotReceiverCardInfoSaveToReceiver) senderOperation)
								.getFileName());
				if (!this.binFile.exists())
				{
					return false;
				}
				this.buffer = ReceiverSettingBinParser
						.getAllByteFromBin(binFile);
				if(buffer==null){
		    		return false;
		    	}
				// this.width = width;
				// this.height = height;
				return saveToReceiverCard();
			}
			else if (senderOperation instanceof SoSetReceiverCardInfoSender)
			{// 发送
				String dir = Constants.SDCARD_PATH;
				this.binFile = new File(dir,
						((SoSetReceiverCardInfoSender) senderOperation)
								.getFileName());
				if (!this.binFile.exists())
				{
					return false;
				}
				
				if (binFile.exists())
				{
					this.buffer = ReceiverSettingBinParser
							.getAllByteFromBin(binFile);
				}
				if(buffer==null){
		    		return false;
		    	}
				this.width = ((SoSetReceiverCardInfoSender) senderOperation)
						.getWidth();
				this.height = ((SoSetReceiverCardInfoSender) senderOperation)
						.getHeight();
				return sendToSenderCard();
			}
		}
		catch (IOException e)
		{
			return false;
		}
		return false;
	}
}
