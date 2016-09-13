package com.clt.service;

import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;

import org.json.JSONObject;

import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.text.TextUtils;

import com.clt.ICommand;
import com.clt.SerialPortConnector;
import com.clt.TCPConnector;
import com.clt.Wrapper;
import com.clt.activity.MainActivity.PlayingVsn;
import com.clt.commondata.ColorTempToRGBCoef;
import com.clt.commondata.ColorTempToRGBCoef.ColorCoef;
import com.clt.commondata.SenderInfo;
import com.clt.commondata.SenderParameters;
import com.clt.commondata.SomeInfo;
import com.clt.entity.ConnectionParam;
import com.clt.entity.Program;
import com.clt.entity.ReceiverSettingInfo;
import com.clt.netmessage.NMChangeTermName;
import com.clt.netmessage.NMChangeTermNameAnswer;
import com.clt.netmessage.NMDeleteProgram;
import com.clt.netmessage.NMDeleteProgramAnswer;
import com.clt.netmessage.NMGetProgramsNamesAnswer;
import com.clt.netmessage.NMGetReceiverCardInfoAnswer;
import com.clt.netmessage.NMGetSomeInfoAnswer;
import com.clt.netmessage.NMSaveBrightAndColorTemp;
import com.clt.netmessage.NMSaveUidEncrpt;
import com.clt.netmessage.NMSetAutoBright;
import com.clt.netmessage.NMSetConnectionToReceiverCard;
import com.clt.netmessage.NMSetConnectionToSenderCard;
import com.clt.netmessage.NMSetDayPeriodBright;
import com.clt.netmessage.NMSetEDID;
import com.clt.netmessage.NMSetPlayProgram;
import com.clt.netmessage.NMSetPortAreaByXml;
import com.clt.netmessage.NMSetReceiverCardInfoSaveToReceiver;
import com.clt.netmessage.NMSetReceiverCardInfoSender;
import com.clt.netmessage.NMSetSenderBasicParameters;
import com.clt.netmessage.NMSetSenderBright;
import com.clt.netmessage.NMSetSenderColorTemp;
import com.clt.netmessage.NMSetSenderColorTempRGB;
import com.clt.netmessage.NMSetSenderShowOnOff;
import com.clt.netmessage.NMSetTestMode;
import com.clt.netmessage.NetMessageType;
import com.clt.operation.SenderOperation;
import com.clt.operation.SoDetectSender;
import com.clt.operation.SoGetRTC;
import com.clt.operation.SoSaveBrightAndClrT;
import com.clt.operation.SoSaveUidEncrpt;
import com.clt.operation.SoSetAutoBrightness;
import com.clt.operation.SoSetBasicParameters;
import com.clt.operation.SoSetBrightAndClrT;
import com.clt.operation.SoSetConnectionToReceicerCard;
import com.clt.operation.SoSetConnectionToSenderCard;
import com.clt.operation.SoSetDayPeriodBright;
import com.clt.operation.SoSetEDID;
import com.clt.operation.SoSetPortArea;
import com.clt.operation.SoSetReceiverCardInfoSender;
import com.clt.operation.SoSetTestMode;
import com.clt.operation.SoShowOnOff;
import com.clt.operation.SotReceiverCardInfoSaveToReceiver;
import com.clt.parser.AreaConfig;
import com.clt.parser.AreaConfigXmlParser;
import com.clt.parser.BrightConfig;
import com.clt.parser.BrightnessXmlParser;
import com.clt.parser.ReceiverSettingBinParser;
import com.clt.receiver.SettingReceiver;
import com.clt.util.CommonUtil;
import com.clt.util.Constants;
import com.clt.util.FileLogger;
import com.clt.util.ProgramUtil;
import com.clt.util.SharedPreferenceUtil;
import com.clt.util.SharedPreferenceUtil.ShareKey;
import com.clt.util.Tools;
import com.google.gson.Gson;

/**
 * 解析命令，交给串口处理
 * 
 */
public class WrapperImpl implements Wrapper, ICommand
{
	private SerialPortConnector serialPortConnector;
	
	private TCPConnector tcpConnector;
	
	private Context context;
	
	private SharedPreferenceUtil sharedPreferenceUtil;
	
	public WrapperImpl(Context context)
	{
		this.context = context;
		sharedPreferenceUtil = SharedPreferenceUtil.getInstance(context);
	}
	
	@Override
	public void inputOneMessage(String message)
	{
		dealNetMessage(message);
	}
	
	private void addOneOperation(SenderOperation senderOperation)
	{
		if (serialPortConnector != null)
		{
			serialPortConnector.addOperation(senderOperation);
		}
	}
	
	@Override
	public void outputOneMessage(String message)
	{
		if (tcpConnector != null)
		{
			tcpConnector.responseOneMessage(message);
		}
	}
	
	@Override
	public void setSerialPortConnector(SerialPortConnector serialPortConnector)
	{
		this.serialPortConnector = serialPortConnector;
	}
	
	@Override
	public void setTCPConnector(TCPConnector tcpConnector)
	{
		this.tcpConnector = tcpConnector;
	}
	
	/**
	 * 从服务端收到指令，并做相应的操作
	 * 
	 * @param intent2
	 */
	protected void dealNetMessage(String strNetMessage)
	{
		
		try
		{
			if (TextUtils.isEmpty(strNetMessage))
			{
				return;
			}
			JSONObject jsonObject = new JSONObject(strNetMessage);
			if (!jsonObject.has("mType"))
				return;
			
			int nmType = jsonObject.getInt("mType");
			// int currentClientType = jsonObject.getInt("netType");
			// if (mCommandExcutor != null)
			// {
			// mCommandExcutor.setCurrentClientType(currentClientType);
			// }
			
			switch (nmType)
			{
				case NetMessageType.SetSenderBright:// 设置亮度
				{
					Gson gson = new Gson();
					NMSetSenderBright nmSetSenderBright = gson.fromJson(
							strNetMessage, NMSetSenderBright.class);
					
					SenderInfo senderInfo = serialPortConnector.getSenderInfo();
					if (senderInfo == null)
					{
						detectSender();
					}
					else
					{
						
						setBrightness(nmSetSenderBright.getBright(),
								senderInfo.getRealTimeClrTemp());
						senderInfo.setRealTimeBright(nmSetSenderBright
								.getBright());
						
					}
				}
				break;
				case NetMessageType.SetColorTemperture:// 设置色温
				{
					Gson gson = new Gson();
					NMSetSenderColorTemp nmSetSenderColorTemp = gson.fromJson(
							strNetMessage, NMSetSenderColorTemp.class);
					
					SenderInfo senderInfo = serialPortConnector.getSenderInfo();
					if (senderInfo == null)
					{
						detectSender();
					}
					else
					{
						setBrightness(senderInfo.getRealTimeBright(),
								nmSetSenderColorTemp.getColorTemp());
						senderInfo.setRealTimeClrTemp(nmSetSenderColorTemp
								.getColorTemp());
					}
				}
				break;
				case NetMessageType.SetColorTempertureRGB:// 设置色温RGB
				{
					Gson gson = new Gson();
					NMSetSenderColorTempRGB nmSetSenderColorTempRGB = gson
							.fromJson(strNetMessage,
									NMSetSenderColorTempRGB.class);
					SenderInfo senderInfo = serialPortConnector.getSenderInfo();
					if (senderInfo == null)
					{
						detectSender();
					}
					else
					{
						int r = nmSetSenderColorTempRGB.getColorTempR();
						int g = nmSetSenderColorTempRGB.getColorTempG();
						int b = nmSetSenderColorTempRGB.getColorTempB();
						
						setBrightness(senderInfo.getRealTimeBright(), 6500);
						setBrightness(senderInfo.getRealTimeBright(), r, g, b,
								6500);
					}
					
				}
				break;
				case NetMessageType.SaveBrightAndColorTemp:// 同时保存亮度和色温
				{
					Gson gson = new Gson();
					NMSaveBrightAndColorTemp nmSaveBrightAndrColorTemp = gson
							.fromJson(strNetMessage,
									NMSaveBrightAndColorTemp.class);
					int bright = nmSaveBrightAndrColorTemp.getBright();
					int colorTemp = nmSaveBrightAndrColorTemp.getColorTemp();
					saveBrightness(bright, colorTemp);
				}
				break;
				case NetMessageType.DetectSender:// 探卡
				{
					detectSender();
				}
				break;
				case NetMessageType.SetPlayProgram:// 切换节目
				{
					Gson gson = new Gson();
					NMSetPlayProgram nmSetPlayProgram = gson.fromJson(
							strNetMessage, NMSetPlayProgram.class);
					playProgram(nmSetPlayProgram.getProgram());
				}
				break;
				case NetMessageType.deleteProgram:// 删掉节目
				{
					Gson gson = new Gson();
					NMDeleteProgram nmDeleteProgram = gson.fromJson(
							strNetMessage, NMDeleteProgram.class);
					deleteProgram(nmDeleteProgram.getProgram());
				}
				break;
				case NetMessageType.SetSenderShowOnOff:// 设置开关
				{
					Gson gson = new Gson();
					NMSetSenderShowOnOff nmSetSenderShowOnff = gson.fromJson(
							strNetMessage, NMSetSenderShowOnOff.class);
					screenShowOnOff(nmSetSenderShowOnff.isShowOn());
				}
				break;
				case NetMessageType.SetSenderBasicParameters:// 发送卡网口输出面积
				{
					Gson gson = new Gson();
					NMSetSenderBasicParameters senderParameters = gson
							.fromJson(strNetMessage,
									NMSetSenderBasicParameters.class);
					setBasicParam(senderParameters.getParams());
				}
				
				break;
				case NetMessageType.SetTestMode:// 设置测试模式
				{
					Gson gson = new Gson();
					NMSetTestMode setTestMode = gson.fromJson(strNetMessage,
							NMSetTestMode.class);
					setTestMode(setTestMode.getIndex());
				}
				break;
				case NetMessageType.SetEDID:// 设置分辨率
				{
					Gson gson = new Gson();
					NMSetEDID setEDID = gson.fromJson(strNetMessage,
							NMSetEDID.class);
					
					setEDID(setEDID.getWidth(), setEDID.getHeight(),
							setEDID.getFreq());
				}
				break;
				case NetMessageType.setDayPeriodBright:
				{
					Gson gson = new Gson();
					NMSetDayPeriodBright setDayPeriodBright = gson.fromJson(
							strNetMessage, NMSetDayPeriodBright.class);
					setDayPeriodBright(setDayPeriodBright.getMaps());
				}
				break;
				case NetMessageType.getProgramsNames:// 获取节目名单
				{
					getProgramsList();
				}
				break;
				case NetMessageType.SetAutoBrightness:// 设置亮度曲线
				{
					Gson gson = new Gson();
					NMSetAutoBright nmSetAutoBright = gson.fromJson(
							strNetMessage, NMSetAutoBright.class);
					setAutoBrightnessXML(nmSetAutoBright.getPath());
				}
				break;
				case NetMessageType.SetPortAreaByXml:// 设置网口面积
				{
					Gson gson = new Gson();
					NMSetPortAreaByXml nmSetPortAreaByXml = gson.fromJson(
							strNetMessage, NMSetPortAreaByXml.class);
					setPortAreaByXML(nmSetPortAreaByXml.getPath());
				}
				break;
				case NetMessageType.getReceiveCardInfo:// 获得接收卡参数
				{
					getReceiverSettingInfos();
				}
				break;
				case NetMessageType.setReceiveCardSettingInfoSend:// 发送接收卡参数
				{
					Gson gson = new Gson();
					NMSetReceiverCardInfoSender nm = gson.fromJson(
							strNetMessage, NMSetReceiverCardInfoSender.class);
					setReceiverSettingInfoSender(nm.getFileName(),
							nm.getBoxWidth(), nm.getBoxHeight());
				}
				break;
				case NetMessageType.setReceiveCardSettingInfoSaveToReceiver:// 固化接收卡参数
				{
					Gson gson = new Gson();
					NMSetReceiverCardInfoSaveToReceiver nm = gson.fromJson(
							strNetMessage,
							NMSetReceiverCardInfoSaveToReceiver.class);
					setReceiverSettingInfoSaveToReceiver(nm.getFileName(),
							nm.getBoxWidth(), nm.getBoxHeight());
				}
				break;
				case NetMessageType.getRTC:// 获得RTC时间
				{
					getRTC();
				}
				break;
				case NetMessageType.SaveUidEncrpt:// 保存uid加密码
				{
					Gson gson = new Gson();
					NMSaveUidEncrpt nmSaveUidEncrpt = gson.fromJson(
							strNetMessage, NMSaveUidEncrpt.class);
					saveUidEncrpt(nmSaveUidEncrpt.getPath());
				}
				
				break;
				case NetMessageType.setConnectionToReceiverCard:// 固化连接关系
				{
					Gson gson = new Gson();
					NMSetConnectionToReceiverCard nm_SetConnectionToReceiverCard = gson
							.fromJson(strNetMessage,
									NMSetConnectionToReceiverCard.class);
					setConnectionToReceiverCard(nm_SetConnectionToReceiverCard
							.getConnectionParam());
				}
				
				break;
				case NetMessageType.setConnectionToSenderCard:// 发送连接关系
				{
					Gson gson = new Gson();
					NMSetConnectionToSenderCard nm_SetConnectionToSenderCard = gson
							.fromJson(strNetMessage,
									NMSetConnectionToSenderCard.class);
					setConnectionToSenderCard(nm_SetConnectionToSenderCard
							.getConnectionParam());
				}
				
				break;
				case NetMessageType.ModifyTerminateName:
				{
					Gson gson = new Gson();
					NMChangeTermName nmChangeTermName = gson.fromJson(
							strNetMessage, NMChangeTermName.class);
					changeTermName(nmChangeTermName.getTermName());
				}
				break;
				case NetMessageType.GetSomeInfo:
				{// 获得一些消息
					getSomeInfo();
				}
				break;
			}
			
		}
		catch (Exception e)
		{
			e.printStackTrace();
			return;
		}
		
	}
	
	/**
	 * 探测发送卡
	 * 
	 * @return
	 */
	public void detectSender()
	{
		
		SoDetectSender soDetectSender = new SoDetectSender();
		addOneOperation(soDetectSender);
	}
	
	/**
	 * 屏幕显示和关闭
	 * 
	 * @param bShow
	 * @return
	 */
	public void screenShowOnOff(boolean bShow)
	{
		
		SoShowOnOff soShowOnOff = new SoShowOnOff();
		soShowOnOff.setbShowOnOff(bShow);
		addOneOperation(soShowOnOff);
	}
	
	/**
	 * 设置基本参数
	 * 
	 * @param param
	 * @return
	 */
	public void setBasicParam(SenderParameters param)
	{
		
		SoSetBasicParameters soSetBasicParam = new SoSetBasicParameters();
		soSetBasicParam.setSenderParameters(param);
		addOneOperation(soSetBasicParam);
	}
	
	/**
	 * 设置测试模式
	 * 
	 * @param index
	 * @return
	 */
	public void setTestMode(int index)
	{
		SoSetTestMode soSetTestMode = new SoSetTestMode();
		soSetTestMode.setIndex(index);
		addOneOperation(soSetTestMode);
	}
	
	/**
	 * 设置亮度和色温
	 * 
	 * @param bright
	 * @param colorTemp
	 * @return
	 */
	public void setBrightness(int bright, int colorTemp)
	{
		
		ColorTempToRGBCoef colorTempToRGBCoef = new ColorTempToRGBCoef();
		ColorCoef colorCoef = colorTempToRGBCoef.GetColorTemp(colorTemp);
		
		SoSetBrightAndClrT soSetBrightAndClrT = new SoSetBrightAndClrT();
		/**
		 * 把下面的代码写到SO_CommandExcutor文件中
		 */
		soSetBrightAndClrT.setBirght(bright);
		soSetBrightAndClrT.setrBirght((int) (colorCoef.r * 255));
		soSetBrightAndClrT.setgBright((int) (colorCoef.g * 255));
		soSetBrightAndClrT.setbBright((int) (colorCoef.b * 255));
		soSetBrightAndClrT.setColorTemperature(colorTemp);
		
		addOneOperation(soSetBrightAndClrT);
		
	}
	
	/**
	 * 设置亮度和色温
	 * 
	 * @param bright
	 * @param colorTemp
	 * @return
	 */
	public void setBrightness(int bright, int rBright, int gBright,
			int bBright, int colorTemp)
	{
		// ColorTempToRGBCoef colorTempToRGBCoef = new ColorTempToRGBCoef();
		// ColorCoef colorCoef = colorTempToRGBCoef.GetColorTemp(colorTemp);
		
		SoSetBrightAndClrT soSetBrightAndClrT = new SoSetBrightAndClrT();
		/**
		 * 把下面的代码写到SO_CommandExcutor文件中
		 */
		soSetBrightAndClrT.setBirght(bright);
		soSetBrightAndClrT.setrBirght(rBright);
		soSetBrightAndClrT.setgBright(gBright);
		soSetBrightAndClrT.setbBright(bBright);
		soSetBrightAndClrT.setColorTemperature(colorTemp);
		
		addOneOperation(soSetBrightAndClrT);
		
	}
	
	/**
	 * RGB转成色温
	 */
	public int rgb2ColorTemp(int r, int g, int b)
	{
		double total = 0.66697 * r + 1.13240 * g + 1.20063 * b;
		double x = (0.341427 * r + 0.188273 * g + 0.390202 * g) / total;
		double y = (0.138972 * r + 0.837182 * g + 0.073588 * b) / total;
		double z = (0.0375154 * g + 2.038878 * b) / total;
		double n = (x - 0.3320) / (y - 0.1858);
		double T = -437 * Math.pow(n, 3) + 3601 * Math.pow(n, 2) - 6861 * n
				+ 5514.31;
		
		if (r == g && g == b && r != 0)
			T = 6500;
		if (r == 0 && g == 0 && b == 0)
			T = 0;
		if (T < 0)
			T = 0;
		return CommonUtil.double2Round(T);
	}
	
	/**
	 * 色温转成RGB
	 * 
	 * @param colorTemp
	 */
	public void colorTemp2RGB(int colorTemp)
	{
		float r = 0.0f, g = 0.0f, b = 0.0f;
		
		int rr = CommonUtil.getRounding(r * 255);
		int gg = CommonUtil.getRounding(g * 255);
		int bb = CommonUtil.getRounding(b * 255);
		
	}
	
	/**
	 * 保存亮度和色温
	 * 
	 * @param bright
	 * @param colorTemp
	 * @return
	 */
	public void saveBrightness(int bright, int colorTemp)
	{
		
		ColorTempToRGBCoef colorTempToRGBCoef = new ColorTempToRGBCoef();
		ColorCoef colorCoef = colorTempToRGBCoef.GetColorTemp(colorTemp);
		
		SoSaveBrightAndClrT soSaveBrightAndClrT = new SoSaveBrightAndClrT();
		/**
		 * 把下面的代码写到SO_CommandExcutor文件中
		 */
		soSaveBrightAndClrT.setBirght(bright);
		soSaveBrightAndClrT.setrBirght((int) (colorCoef.r * 255));
		soSaveBrightAndClrT.setgBright((int) (colorCoef.g * 255));
		soSaveBrightAndClrT.setbBright((int) (colorCoef.b * 255));
		soSaveBrightAndClrT.setColorTemperature(colorTemp);
		
		addOneOperation(soSaveBrightAndClrT);
		
	}
	
	/**
	 * 写基本参数
	 * 
	 * @param parameters
	 * @return
	 */
	public void writeBaiscParam(SenderParameters parameters)
	{
		
		SoSetBasicParameters soSetBasicParameters = new SoSetBasicParameters();
		soSetBasicParameters.setSenderParameters(parameters);
		addOneOperation(soSetBasicParameters);
	}
	
	/**
	 * 设置EDID信息
	 * 
	 * @param width
	 * @param height
	 * @return
	 */
	public void setEDID(int width, int height, int freq)
	{
		SoSetEDID soSetEDID = new SoSetEDID();
		soSetEDID.setWidth(width);
		soSetEDID.setHeight(height);
		soSetEDID.setFreq(freq);
		addOneOperation(soSetEDID);
	}
	
	/**
	 * 分时段设置亮度
	 * 
	 * @param maps
	 * @return
	 */
	public void setDayPeriodBright(LinkedHashMap<String, Integer> maps)
	{
		SoSetDayPeriodBright soSetDayPeriodBright = new SoSetDayPeriodBright();
		soSetDayPeriodBright.setMaps(maps);
		addOneOperation(soSetDayPeriodBright);
	}
	
	/**
	 * 从本地获取节目单
	 */
	public void getProgramsList()
	{
		// NM_DetectSenderAnswer nmDetectSenderAnswer = new
		// NM_DetectSenderAnswer();
		// Gson gson = new Gson();
		// String nmString = gson.toJson(nmDetectSenderAnswer);
		// nmDetectSenderAnswer.setErrorCode(0);
		// SendNetMessage(nmString);
		// return false;
		
		ProgramUtil programUtil = new ProgramUtil();
		File usb1Dir = new File(Constants.USB_PATH_0);
		// File usb2Dir = new File(Constants.USB_PATH_1);
		File sdcardDir = new File(Constants.SDCARD_PATH);
		File downloadDir = new File(Constants.SDCARD_DOWNLOAD_PATH);
		File usbDir = new File(Constants.SDCARD_SD_PATH);
		File ftpDir = new File(Constants.SDCARD_SD_FTP);
		ArrayList<Program> programs = new ArrayList<Program>();
		if (usb1Dir.exists())
		{
			programs.addAll(programUtil.getPrograms(Constants.USB_PATH_0));
		}
		// if (usb2Dir.exists())
		// {
		// programs.addAll(programUtil.getPrograms(Constants.USB_PATH_1));
		// }
		if (sdcardDir.exists())
		{
			programs.addAll(programUtil.getPrograms(Constants.SDCARD_PATH));
		}
		if (downloadDir.exists())
		{
			programs.addAll(programUtil
					.getPrograms(Constants.SDCARD_DOWNLOAD_PATH));
		}
		if (usbDir.exists())
		{
			programs.addAll(programUtil.getPrograms(Constants.SDCARD_SD_PATH));
		}
		if (ftpDir.exists())
		{
			programs.addAll(programUtil.getPrograms(Constants.SDCARD_SD_FTP));
		}
		// ArrayList<Program> programsFromUSB =
		// programUtil.getPrograms(UploadConfig.USB_PATH_0);
		// ArrayList<Program> programs =
		// programUtil.getPrograms(UploadConfig.USB_PATH_0);
		//获得当前播放的节目
		Program playingProgram=getPlayingProgram(context);
		for (Program program : programs)
		{
			boolean flag=playingProgram.getFileName().equals(program.getFileName())
					&&playingProgram.getPath().equals(program.getPath());
			if(flag){
				program.setPlaying(true);
				break;
			}
		}
		
		NMGetProgramsNamesAnswer nmGetProgramsNames = new NMGetProgramsNamesAnswer();
		nmGetProgramsNames.setErrorCode(programs == null ? 0 : 1);
		nmGetProgramsNames.setProgramsNames(programs);
		
		//
		
		Gson gson = new Gson();
		String nmString = gson.toJson(nmGetProgramsNames);
		outputOneMessage(nmString);
		
	}
	
	/**
	 * 获得当前播放的节目
	 */
	public Program getPlayingProgram(Context context)
	{
		Intent intent = context.registerReceiver(null, new IntentFilter(
				"com.clt.intent.action.CURRENT_PROG_INDEX"));
		if (intent != null)
		{
			String folder = intent.getStringExtra("path");
			String vsnPlaying = intent.getStringExtra("file_name");
			Program p=new Program();
			p.setFileName(vsnPlaying);
			p.setPath(folder);
			return p;
		}
		return null;
	}
	
	/**
	 * 播放节目
	 */
	@Override
	public void playProgram(Program program)
	{
		Intent mIntent = new Intent();
		mIntent.setAction("com.clt.broadcast.playProgram");
		mIntent.putExtra("path", program.getPath());
		mIntent.putExtra("file_name", program.getFileName());
		context.sendBroadcast(mIntent);
	}
	
	/**
	 * 删除节目
	 * 
	 * @param program
	 * @return
	 */
	public void deleteProgram(Program program)
	{
		String path = program.getPath();
		String fileName = program.getFileName();
		File vsnFile = new File(path, fileName);
		File dir = new File(path, fileName.substring(0,
				fileName.lastIndexOf("."))
				+ ".files");
		boolean flag = false;
		// 删除文件夹
		if (dir.exists())
		{
			File[] files = dir.listFiles();
			for (File file : files)
			{
				flag = file.delete();
				if (flag == false)
				{
					break;
				}
			}
			flag = dir.delete();
		}
		// 删除文件
		if (vsnFile.exists())
		{
			
			flag = vsnFile.delete();
		}
		
		NMDeleteProgramAnswer nmDeleteProgramAnswer = new NMDeleteProgramAnswer();
		nmDeleteProgramAnswer.setErrorCode(flag ? 1 : 0);
		Gson gson = new Gson();
		String nmString = gson.toJson(nmDeleteProgramAnswer);
		
		outputOneMessage(nmString);
	}
	
	/**
	 * 设置亮度曲线
	 * 
	 * @param string
	 * @param brightConfig
	 * @return
	 * @throws Exception
	 */
	public void setAutoBrightnessXML(String path)
	{
		
		try
		{
			File file = new File(path, SettingReceiver.XML_BRIGHTNESS);
			if (!file.exists())
			{
				return;
			}
			FileInputStream fileInputStream = new FileInputStream(file);
			BrightConfig brightConfig = BrightnessXmlParser
					.readBrightnessXmlByPull(fileInputStream);
			SoSetAutoBrightness soSetAutoBrightness = new SoSetAutoBrightness();
			soSetAutoBrightness.setBuffer(CommonUtil.hexString2Bytes(256,
					brightConfig.getBrightTune()));
			soSetAutoBrightness.setIsAuto(brightConfig.getIsAuto());
			addOneOperation(soSetAutoBrightness);
		}
		catch (FileNotFoundException e)
		{
			e.printStackTrace();
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		
	}
	
	/**
	 * 设置网口面积
	 * 
	 * @param path
	 * @param areaConfig
	 * @return
	 */
	public void setPortAreaByXML(String dir)
	{
		
		try
		{
			File file = new File(dir, SettingReceiver.XML_PORTAREA);
			if (!file.exists())
			{
				return;
			}
			doSetPortAreaByXML(file);
			// File file1=new File(Config.USB_PATH_0,
			// SdcardInsertReceiver.XML_PORTAREA);
			// if(file1.exists()){
			// doSetPortAreaByXML(file1);
			// }
			//
			// File file2=new File(Config.USB_PATH_1,
			// SdcardInsertReceiver.XML_PORTAREA);
			// if(file2.exists()){
			// doSetPortAreaByXML(file2);
			// }
			
			FileLogger.getInstance().writeMessageToFile("设置网口面积结束");
			
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}
	
	/**
	 * 
	 * @param file
	 * @throws Exception
	 */
	public void doSetPortAreaByXML(File file)
	{
		
		try
		{
			FileInputStream fileInputStream = new FileInputStream(file);
			AreaConfig areaConfig = AreaConfigXmlParser
					.readXmlByPull(fileInputStream);
			SoSetPortArea soSetPortArea = new SoSetPortArea();
			soSetPortArea.setAreaConfig(areaConfig);
			addOneOperation(soSetPortArea);
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}
	
	/**
	 * 获取接收卡参数设置信息
	 * 
	 * @return
	 */
	public void getReceiverSettingInfos()
	{
		ArrayList<ReceiverSettingInfo> receiverSettingInfos = new ArrayList<ReceiverSettingInfo>();
		ReceiverSettingInfo receiverSettingInfo = null;
		try
		{
			File dir = new File(Constants.SDCARD_PATH);
			File[] binFiles = dir.listFiles(new FileFilter()
			{
				
				@Override
				public boolean accept(File pathname)
				{
					if (pathname.getName().endsWith(".bin")
							&& pathname.length() >= 32 * 1024)
					{
						return true;
					}
					return false;
				}
			});
			if (binFiles == null || binFiles.length == 0)
			{
				return;
			}
			for (File binFile : binFiles)
			{
				receiverSettingInfo = ReceiverSettingBinParser.parser(binFile);
				receiverSettingInfos.add(receiverSettingInfo);
				
			}
			
			NMGetReceiverCardInfoAnswer nmGetReceiverCardInfoAnswer = new NMGetReceiverCardInfoAnswer();
			nmGetReceiverCardInfoAnswer
					.setReceiverSettings(receiverSettingInfos);
			//
			Gson gson = new Gson();
			String nmString = gson.toJson(nmGetReceiverCardInfoAnswer);
			
			outputOneMessage(nmString);
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
		
	}
	
	/**
	 * 获得RTC时间
	 */
	public void getRTC()
	{
		ReceiverSettingInfo receiverSettingInfo = null;
		SoGetRTC sogetGetRTC = new SoGetRTC();
		addOneOperation(sogetGetRTC);
		
	}
	
	/**
	 * 设置接收卡参数,发送到发送卡
	 * 
	 * @param fileName
	 */
	public void setReceiverSettingInfoSender(String fileName, int width,
			int height)
	{
		SoSetReceiverCardInfoSender so_SetReceiverCardInfo = new SoSetReceiverCardInfoSender();
		so_SetReceiverCardInfo.setFileName(fileName);
		so_SetReceiverCardInfo.setWidth(width);
		so_SetReceiverCardInfo.setHeight(height);
		addOneOperation(so_SetReceiverCardInfo);
		
	}
	
	/**
	 * 设置接收卡参数，固化到接收卡
	 */
	public void setReceiverSettingInfoSaveToReceiver(String fileName,
			int width, int height)
	{
		SotReceiverCardInfoSaveToReceiver so_SetReceiverCardInfo = new SotReceiverCardInfoSaveToReceiver();
		so_SetReceiverCardInfo.setFileName(fileName);
		so_SetReceiverCardInfo.setWidth(width);
		so_SetReceiverCardInfo.setHeight(height);
		addOneOperation(so_SetReceiverCardInfo);
		
	}
	
	/**
	 * 保存uid加密码到stm
	 * 
	 * @param path
	 */
	public void saveUidEncrpt(String path)
	{
		SoSaveUidEncrpt so_SaveUidEncrpt = new SoSaveUidEncrpt();
		so_SaveUidEncrpt.setSdcardPath(path);
		addOneOperation(so_SaveUidEncrpt);
	}
	
	/**
	 * 发送连接关系
	 * 
	 * @param connectionParam
	 */
	public void setConnectionToSenderCard(ConnectionParam connectionParam)
	{
		SoSetConnectionToSenderCard soSetConnectionToSenderCard = new SoSetConnectionToSenderCard();
		soSetConnectionToSenderCard.setConnectionParam(connectionParam);
		addOneOperation(soSetConnectionToSenderCard);
	}
	
	/**
	 * 固化连接关系
	 * 
	 * @param connectionParam
	 */
	public void setConnectionToReceiverCard(ConnectionParam connectionParam)
	{
		SoSetConnectionToReceicerCard soSetConnectionToReceicerCard = new SoSetConnectionToReceicerCard();
		soSetConnectionToReceicerCard.setConnectionParam(connectionParam);
		addOneOperation(soSetConnectionToReceicerCard);
	}
	
	/**
	 * 改变终端名
	 * 
	 */
	public void changeTermName(String newName)
	{
		try
		{
			sharedPreferenceUtil.putString(ShareKey.TerminateName, newName);
			NMChangeTermNameAnswer answer = new NMChangeTermNameAnswer();
			answer.setErrorCode(1);
			//
			Gson gson = new Gson();
			String nmString = gson.toJson(answer);
			outputOneMessage(nmString);
		}
		catch (Exception e)
		{
			
		}
		
	}
	
	/**
	 * 获得一些信息,磁盘空间，版本信息，网络配置等
	 */
	public void getSomeInfo()
	{
		boolean isOk = true;
		SomeInfo someInfo = new SomeInfo();
		try
		{
			someInfo.setSdTotalSize(Tools.getSDTotalSize());
			someInfo.setSdAviableSize(Tools.getSDAvailableSize());
			
			someInfo.setLedServerVersion(Constants.VERSION);
			someInfo.setImgVersion(Tools.getImgVersion());
			
			PackageInfo packageInfo = context.getPackageManager()
					.getPackageInfo("com.color.home",
							PackageManager.GET_CONFIGURATIONS);
			if (packageInfo != null)
			{
				someInfo.setColorlightVersion(packageInfo.versionName);
			}
		}
		catch (Exception e)
		{
			isOk = false;
		}
		finally
		{
			NMGetSomeInfoAnswer answer = new NMGetSomeInfoAnswer();
			answer.setErrorCode(isOk ? 1 : 0);
			answer.setSomeInfo(someInfo);
			
			Gson gson = new Gson();
			String nmString = gson.toJson(answer);
			outputOneMessage(nmString);
		}
		
	}
	
	@Override
	public void start()
	{
	}
	
	@Override
	public void stop()
	{
		
	}
	
}
