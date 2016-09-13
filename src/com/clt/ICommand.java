package com.clt;

import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;

import android.content.Context;

import com.clt.commondata.ColorTempToRGBCoef;
import com.clt.commondata.SenderParameters;
import com.clt.commondata.ColorTempToRGBCoef.ColorCoef;
import com.clt.entity.ConnectionParam;
import com.clt.entity.Program;
import com.clt.entity.ReceiverSettingInfo;
import com.clt.netmessage.NMChangeTermNameAnswer;
import com.clt.netmessage.NMDeleteProgramAnswer;
import com.clt.netmessage.NMGetProgramsNamesAnswer;
import com.clt.netmessage.NMGetReceiverCardInfoAnswer;
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
import com.clt.util.SharedPreferenceUtil.ShareKey;
import com.google.gson.Gson;

public interface ICommand
{
    /**
     * 探测发送卡
     * @return
     */
    void detectSender();

    /**
     * 屏幕显示和关闭
     * @param bShow
     * @return
     */
    void screenShowOnOff(boolean bShow);

    /**
     * 设置基本参数
     * @param param
     * @return
     */
    void setBasicParam(SenderParameters param);

    /**
     * 设置测试模式
     * @param index
     * @return
     */
    void setTestMode(int index);

    /**
     * 设置亮度和色温
     * @param bright
     * @param colorTemp
     * @return
     */
    void setBrightness(int bright, int colorTemp);

    /**
     * 设置亮度和色温
     * @param bright
     * @param colorTemp
     * @return
     */
    void setBrightness(int bright, int rBright, int gBright, int bBright,
            int colorTemp);

    /**
     * 保存亮度和色温
     * @param bright
     * @param colorTemp
     * @return
     */
    void saveBrightness(int bright, int colorTemp);

    /**
     * 写基本参数
     * @param parameters
     * @return
     */
    void writeBaiscParam(SenderParameters parameters);

    /**
     * 设置EDID信息
     * @param width
     * @param height
     * @return
     */
    void setEDID(int width, int height, int freq);

    /**
     * 分时段设置亮度
     * @param maps
     * @return
     */
    void setDayPeriodBright(LinkedHashMap<String, Integer> maps);
    
    /**
     * 播放某个节目
     * @param program
     */
    void playProgram(Program program);
    /**
     * 从本地获取节目单
     * @return
     */
    void getProgramsList();

    Program getPlayingProgram(Context context);
    /**
     * 删除节目
     * @param program
     * @return
     */
    void deleteProgram(Program program);

    /**
     * 设置亮度曲线
     * @param string 
     * @param brightConfig
     * @return
     * @throws Exception 
     */
    void setAutoBrightnessXML(String path);

    /**
     * 设置网口面积
     * @param path 
     * @param areaConfig
     * @return
     */
    void setPortAreaByXML(String dir);

    /**
     * 
     * @param file
     * @throws Exception
     */
    void doSetPortAreaByXML(File file);

    /**
     * 获取接收卡参数设置信息
     * @return
     */
    void getReceiverSettingInfos();

    /**
     * 获得RTC时间
     */
    void getRTC();

    /**
     * 设置接收卡参数,发送到发送卡
     * @param fileName 
     */
    void setReceiverSettingInfoSender(String fileName, int width, int height);

    /**
     * 设置接收卡参数，固化到接收卡
     */
    void setReceiverSettingInfoSaveToReceiver(String fileName, int width,
            int height);

    /**
     * 保存uid加密码到stm
     * @param path 
     */
    void saveUidEncrpt(String path);

    /**
     * 发送连接关系
     * @param connectionParam
     */
    void setConnectionToSenderCard(ConnectionParam connectionParam);

    /**
     * 固化连接关系
     * @param connectionParam
     */
    void setConnectionToReceiverCard(ConnectionParam connectionParam);

    /**
     * 改变终端名
     * 
     */
    void changeTermName(String newName);

    /**
     * 获得一些信息,磁盘空间，版本信息，网络配置等
     */
    void getSomeInfo();
}
