package com.clt.http;

import java.util.ArrayList;
import java.util.Locale;

import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.text.TextUtils;
import android.util.DisplayMetrics;

import com.clt.entity.Program;
import com.clt.http.HttpConnectorImpl.Params;
import com.clt.http.NanoHTTPD2.Request;
import com.clt.http.NanoHTTPD2.Response;
import com.clt.ledservers.R;
import com.clt.netmessage.NMSetSenderBright;
import com.clt.netmessage.NMSetSenderColorTemp;
import com.clt.netmessage.NMSetSenderColorTempRGB;
import com.clt.netmessage.NMSetSenderShowOnOff;
import com.clt.netmessage.NMSetTestMode;
import com.clt.service.MainService;
import com.clt.util.Constants;
import com.clt.util.LogUtil;
import com.clt.util.ProgramUtil;
import com.google.gson.Gson;

/**
 * 首页html
 * @author Administrator
 *
 */
public class HtmlIndexServlet extends HtmlBasePageServlet
{
    private static HtmlIndexServlet instance;

    private static String action = "com.clt.language";

    private ArrayList<Program> programs;

    // public static HtmlIndexServlet getInstance(Context context){
    // if(instance==null){
    // instance=new HtmlIndexServlet(context);
    // }
    // return instance;
    // }

    public HtmlIndexServlet(Context context)
    {
        super(context);
    }

    @Override
    public void doGet(Request request, Response response)
    {
        try
        {
            getProgramList(response);
           
        }
        catch (Exception e)
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    /**
     * 获取节目列表
     */
    private void getProgramList(Response response)
    {
        ProgramUtil programUtil = new ProgramUtil();
        this.programs = programUtil.getPrograms(Constants.SAVE_PATH);
        updataProgramsView(response);

    }

   
    /**
     * 切换节目
     * @param path
     * @param fileName
     */
    private void setProgram(String path,String fileName){
        Intent intent = new Intent();
        intent.setAction("com.clt.broadcast.playProgram");
        intent.putExtra("path", path);
        if(!fileName.endsWith(".vsn")){
            fileName=fileName+".vsn";
        }
        intent.putExtra("file_name", fileName);
        context.sendBroadcast(intent);
    }
    private String getProgramName(String name){
        if(!TextUtils.isEmpty(name)){
            if(name.endsWith(".vsn")||name.endsWith(".VSN")){
                return name.substring(0,name.lastIndexOf("."));
            }
        }
        return null;
    }
    private void updataProgramsView(Response response)
    {
       
        StringBuffer sb = new StringBuffer();
        String [] scripts =
            {
                    "jquery.js", "index.js"
            };
        doHeader(sb, scripts);
        doMenuBar(sb);
        
        sb.append(body1);
        //节目
        sb.append("<div class='slideItem'>\n" 
                + "<div id='program'>\n"
                + "<p>"+getRString(R.string.program_manager)+"</p>\n" + "<div>\n" 
                + " <select id='programMan'>\n");
        if(programs!=null&&!programs.isEmpty()){
            
            for (Program program : programs)
            {
                sb.append("<option value='"+program.getPath()+"'>"+getProgramName(program.getFileName())+"</option>\n");
            }
        }else{
            sb.append("<option>"+getRString(R.string.none)+"</option>\n");
        }
       
        sb.append("</select>\n" 
                + "</div>\n" 
                + "</div>\n"
                + "</div>\n");
        
        sb.append(body2);
        //语言
//        String language=
//                "<div class='slideItem'>\n" + "<div id='language'>\n" + "<p>"
//                + getRString(R.string.change_language) + "</p>\n" + "<div>\n"
//                + "<select id='languageChange'>\n"
//                + "<option value='cn' selected='selected'>"
//                + getRString(R.string.language_cn) + "</option>\n"
//                + "<option value='en'>" + getRString(R.string.language_en)
//                + "</option>\n" 
//                + "</select>\n" + "</div>\n" + "</div>\n"
//                + "</div>";
        sb.append("<div class='slideItem'>\n" + "<div id='language'>\n" + "<p>"
                + getRString(R.string.change_language) + "</p>\n" + "<div>\n"
                + "<select id='languageChange'>\n");
        String language=getLanguage();
        if("cn".equalsIgnoreCase(language)){
            language="<option value='cn' selected='selected'>简体中文</option>\n"
                    + "<option value='en'>English</option>\n";
        }else{
            language="<option value='cn' >简体中文</option>\n"
                    + "<option value='en' selected='selected'>English</option>\n";
        }
        sb.append(language);
        sb.append("</select>\n" + "</div>\n" + "</div>\n"
                + "</div>");
        doFooter(sb);
        response.send(sb.toString());
    }
    @Override
    public void doPost(Request request, Response response)
    {
        if (request.containKey(Params.brightness))// 设置亮度
        {
            int bright = Integer.parseInt(request
                    .getParmeter(Params.brightness));
            setBright(bright);
        }
        else if (request.containKey(Params.colorTemp))
        {// 设置色温
            int colorTemp = Integer.parseInt(request
                    .getParmeter(Params.colorTemp));
            setColorTemp(colorTemp);
        }
        else if (request.containKey(Params.onOff))// 设置开关
        {
            String onOff = request.getParmeter(Params.onOff);
            boolean bShowOn = false;
            if (onOff.equalsIgnoreCase("on"))
            {
                bShowOn = true;
            }
            setOnOff(bShowOn);
            // 输出
            // StringBuffer sb = new StringBuffer();
            // doHeader(sb);
            // doMenuBar(sb);
            // sb.append(bodyNew);
            // doFooter(sb);
            // response.send(sb.toString());
        }
        else if (request.containKey(Params.testMode))
        {
            String value = request.getParmeter(Params.testMode);
            int i = Integer.parseInt(value);
            setTestMode(i);
        }
        else if (request.containKey(Params.language))
        {
            try
            {
                Intent intent = new Intent(action);
                String language = request.getParmeter(Params.language);
                if ("cn".equalsIgnoreCase(language))
                {
                    switchLanguage(Locale.CHINESE);
                }
                else if ("en".equalsIgnoreCase(language))
                {
                    switchLanguage(Locale.ENGLISH);
                }
                context.sendBroadcast(intent);
                updataProgramsView(response);
            }
            catch (Exception e)
            {
                // TODO: handle exception
            }
           
            
        }
        else if (request.containKey(Params.path))
        {
            String path = request.getParmeter(Params.path);
            String fileName = request.getParmeter(Params.fileName);
            setProgram(path, fileName);
        }

    }

    /**
     * 设置亮度
     * @param bright
     * @param colorTemp
     */
    private void setBright(int bright)
    {
        NMSetSenderBright nmSetSenderBright = new NMSetSenderBright();
        nmSetSenderBright.setBright(bright);
        Gson gson = new Gson();
        String nmString = gson.toJson(nmSetSenderBright);
        Intent intent = new Intent();
        intent.putExtra("netMessage", nmString);
        MainService.startService(context, intent);
    }

    /**
     * 设置色温
     * @param bright
     * @param colorTemp
     */
    private void setColorTemp(int colorTemp)
    {
        NMSetSenderColorTemp nmSenderColorTemp = new NMSetSenderColorTemp();
        nmSenderColorTemp.setColorTemp(colorTemp);
        Gson gson = new Gson();
        String nmString = gson.toJson(nmSenderColorTemp);
        Intent intent = new Intent();
        intent.putExtra("netMessage", nmString);
        MainService.startService(context, intent);
    }

    private void setRGB(int r, int g, int b)
    {
        NMSetSenderColorTempRGB nmSetSenderColorTempRGB = new NMSetSenderColorTempRGB();
        nmSetSenderColorTempRGB.setColorTempR(r);
        nmSetSenderColorTempRGB.setColorTempG(g);
        nmSetSenderColorTempRGB.setColorTempB(b);

        Gson gson = new Gson();
        String nmString = gson.toJson(nmSetSenderColorTempRGB);
        Intent intent = new Intent();
        intent.putExtra("netMessage", nmString);
        MainService.startService(context, intent);
    }

    /**
     * 开关显示屏
     * @param onOff
     */
    private void setOnOff(boolean bShowOn)
    {
        LogUtil.i("onOff", bShowOn);

        NMSetSenderShowOnOff nmSetSenderShowOnOff = new NMSetSenderShowOnOff();
        nmSetSenderShowOnOff.setShowOn(bShowOn);

        Gson gson = new Gson();
        String nmString = gson.toJson(nmSetSenderShowOnOff);

        Intent intent = new Intent();
        intent.putExtra("netMessage", nmString);

        MainService.startService(context, intent);

    }

    /**
     * 设置测试模式
     * @param index
     */
    public void setTestMode(int index)
    {
        try
        {
            NMSetTestMode nmSetTestMode = new NMSetTestMode();
            nmSetTestMode.setIndex(index);
            Gson gson = new Gson();
            String nmString = gson.toJson(nmSetTestMode);

            Intent intent = new Intent();
            intent.putExtra("netMessage", nmString);
            MainService.startService(context, intent);
        }
        catch (Exception e)
        {
            // TODO: handle exception
        }

    }

    /**
     * 切换语言
     * @param locale
     */
    public void switchLanguage(Locale locale)
    {
        Resources resources = context.getResources();// 获得res资源对象
        Configuration config = resources.getConfiguration();// 获得设置对象
        DisplayMetrics dm = resources.getDisplayMetrics();// 获得屏幕参数：主要是分辨率，像素等。
        config.locale = locale; // 简体中文
        resources.updateConfiguration(config, dm);
    }

    /**
     * 获得当前语言
     * @return
     */
    public String getLanguage()
    {
        Resources resources = context.getResources();// 获得res资源对象
        Configuration config = resources.getConfiguration();// 获得设置对象
        // DisplayMetrics dm = resources.getDisplayMetrics();//
        // 获得屏幕参数：主要是分辨率，像素等。
        // config.locale = locale; // 简体中文
        if (config.locale == Locale.CHINESE)
        {
            return "cn";
        }
        else if (config.locale == Locale.ENGLISH)
        {
            return "en";
        }
        return "cn";
    }

    // String body = "<form action='/index' method='post'>\n"
    // +
    // "<p>"+getRString(R.string.bright)+":<input type='text' name='brightness' value='255'/><input type='submit' name='submit' value='submit'/></p>\n"
    // + "</form>\n"
    // + "<form action='/index' method='post'>\n"
    // + "<p>R:<input type='text' name='rbrightness' value='255'/></p>\n"
    // + "<p>G:<input type='text' name='gbrightness' value='255'/></p>\n"
    // + "<p>B:<input type='text' name='bbrightness' value='255'/></p>\n"
    // + "<p><input type='submit' value='submit'/></p>\n"
    // + "</form>\n"
    // + "<form action='/index' method='post'>\n"
    // +
    // "<p>"+getRString(R.string.color_temp)+":<input type='text' name='colortemp' value='6500'/><input type='submit' name='submit' value='submit'/></p>\n"
    // + "</form>\n"
    // + "<form action='/index' method='post'>\n"
    // +
    // "Switch<p>On:<input name='onoff' type='radio' value='on'  checked='true'/>\n"
    // + "Off:<input name='onoff' type='radio' value='off'/></p>\n"
    // + "<p><input type='submit' value='submit'/></p>\n" + "</form>\n";

    String bodyNew = "<div class='slideItem'>\n" + "<div class='slideName'>"
            + getRString(R.string.bright) + "</div>\n"
            + "<div class='slideBg' id='brightBg'>\n"
            + "<div class='slider' id='brightBar'></div>\n" + "</div>\n"
            + "<div class='slideInfo' id='brightInfo'>0</div>\n" + "</div>\n"
            + "<div class='select'>\n" + "<select id='colorTempAndRgb'>\n"
            + "<option value='1' selected='selected'>"
            + getRString(R.string.color_temp) + "</option>\n"
            + "<option value='2'>RGB</option>\n" + "</select>\n" + "</div>\n"
            + "<div class='slideItem' id='colorTempBox'>\n"
            + "<div class='slideName'>" + getRString(R.string.color_temp)
            + "</div>\n" + "<div class='slideBg' id='colorTempBg'>\n"
            + "<div class='slider' id='colorTempBar'></div>\n" + "</div>\n"
            + "<div class='slideInfo' id='colorTempInfo'>2000K</div>\n"
            + "</div>\n" + "<div class='rgbBox' id='rgbBox'>\n"
            + "<div class='slideItem'>\n" + "<div class='slideName'>R</div>\n"
            + "<div class='slideBg' id='rBg'>\n"
            + "<div class='slider' id='rBar'></div>\n" + "</div>\n"
            + "<div class='slideInfo' id='rInfo'>0</div>\n" + "</div>\n"
            + "<div class='slideItem'>\n" + "<div class='slideName'>G</div>\n"
            + "<div class='slideBg' id='gBg'>\n"
            + "<div class='slider' id='gBar'></div>\n" + "</div>\n"
            + "<div class='slideInfo' id='gInfo'>0</div>\n" + "</div>\n"
            + "<div class='slideItem'>\n" + "<div class='slideName'>B</div>\n"
            + "<div class='slideBg' id='bBg'>\n"
            + "<div class='slider' id='bBar'></div>\n" + "</div>\n"
            + "<div class='slideInfo' id='bInfo'>0</div>\n" + "</div>\n"
            + "</div>\n" + "<div id='switch'>\n" + "<p>"
            + getRString(R.string.onoff) + "</p>\n" + "<div>\n"
            + "<select id='onOff'>\n"
            + "<option value='on' selected='selected'>"
            + getRString(R.string.on) + "</option>\n" + "<option value='off'>"
            + getRString(R.string.off) + "</option>\n" + "</select>\n"
            + "</div>\n";

    String body1 = "<div class='slideItem'>\n" + "<div class='slideName'>"
            + getRString(R.string.bright) + "</div>\n"
            + "<div class='slideBg' id='brightBg'>\n"
            + "<div class='slider' id='brightBar'></div>\n" + "</div>\n"
            + "<div class='slideInfo' id='brightInfo'>0</div>\n" + " </div>\n"
            + "<div class='slideItem' id='colorTempBox'>\n"
            + "<div class='slideName'>" + getRString(R.string.color_temp)
            + "</div>\n" + "<div class='slideBg' id='colorTempBg'>\n"
            + "<div class='slider' id='colorTempBar'></div>\n" + "</div>\n"
            + "<div class='slideInfo' id='colorTempInfo'>2000K</div>\n"
            + "</div>\n" + "<div class='slideItem'>\n"
            + " <div id='testmode'>\n" + "<p>" + getRString(R.string.test_mode)
            + "</p>\n" + "<div>\n"
            + "<select name='testmode' id='testMode'>\n"
            +"<option value='0' selected='selected'>"+getRArrString(R.array.test_mode, 0)+"</option>\n"
            + "<option value='1'>"+getRArrString(R.array.test_mode, 1)+"</option>\n"
            + "<option value='2'>"+getRArrString(R.array.test_mode, 2)+"</option>\n"
            + "<option value='3'>"+getRArrString(R.array.test_mode, 3)+"</option>\n"
            + "<option value='4'>"+getRArrString(R.array.test_mode, 4)+"</option>\n"
            + "<option value='5'>"+getRArrString(R.array.test_mode, 5)+"</option>\n"
            + "<option value='6'>"+getRArrString(R.array.test_mode, 6)+"</option>\n"
            + "<option value='7'>"+getRArrString(R.array.test_mode, 7)+"</option>\n"
            + "<option value='8'>"+getRArrString(R.array.test_mode, 8)+"</option>\n"
            + "<option value='9'>"+getRArrString(R.array.test_mode, 9)+"</option>\n"
            + "<option value='10'>"+getRArrString(R.array.test_mode, 10)+"</option>\n"
            + "<option value='11'>"+getRArrString(R.array.test_mode, 11)+"</option>\n"
            + "<option value='12'>"+getRArrString(R.array.test_mode, 12)+"</option>\n"
            + "<option value='13'>"+getRArrString(R.array.test_mode, 13)+"</option>\n"
            + "<option value='14'>"+getRArrString(R.array.test_mode, 14)+"</option>\n"
            + "<option value='15'>"+getRArrString(R.array.test_mode, 15)+"</option>\n"
            + "<option value='16'>"+getRArrString(R.array.test_mode, 16)+"</option>\n"
            + "<option value='17'>"+getRArrString(R.array.test_mode, 17)+"</option>\n"
            + "<option value='18'>"+getRArrString(R.array.test_mode, 18)+"</option>\n"
            + "<option value='19'>"+getRArrString(R.array.test_mode, 19)+"</option>\n"
            + "<option value='20'>"+getRArrString(R.array.test_mode, 20)+"</option>\n"
            + "<option value='21'>"+getRArrString(R.array.test_mode, 21)+"</option>\n"
            + "<option value='22'>"+getRArrString(R.array.test_mode, 22)+"</option>\n"
            + "</select>\n" + "</div>\n" + "</div>\n" + "</div>\n";
     String programStr=       
            "<div class='slideItem'>\n" 
            + "<div id='program'>\n"
            + "<p>"+getRString(R.string.program_manager)+"</p>\n" + "<div>\n" 
            + " <select id='programMan'>\n"
            + "<option value='0' selected='selected'>Off</option>\n"
            + "</select>\n" 
            + "</div>\n" 
            + "</div>\n"
            + "</div>\n";
     String body2=
            "<div class='slideItem'>\n" + "<div id='switch'>\n"
            + "<p>"+getRString(R.string.onoff) +"</p>\n" + "<div>\n" + "<select id='onOff'>\n"
            + "<option value='on' selected='selected'>On</option>\n"
            + "<option value='off'>Off</option>\n" + "</select>\n"
            + " </div>\n" + "</div>\n" + "</div>\n";
     String language=
            "<div class='slideItem'>\n" + "<div id='language'>\n" + "<p>"
            + getRString(R.string.change_language) + "</p>\n" + "<div>\n"
            + "<select id='languageChange'>\n"
            + "<option value='cn' selected='selected'>简体中文</option>\n"
            + "<option value='en'>English</option>\n" + "</select>\n" + "</div>\n" + "</div>\n"
            + "</div>";
}
