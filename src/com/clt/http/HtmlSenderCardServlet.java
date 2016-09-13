package com.clt.http;

import java.util.ArrayList;

import android.content.Context;
import android.content.Intent;

import com.clt.commondata.SenderInfo;
import com.clt.commondata.SenderParameters;
import com.clt.entity.Program;
import com.clt.http.HttpConnectorImpl.Params;
import com.clt.http.NanoHTTPD2.Request;
import com.clt.http.NanoHTTPD2.Response;
import com.clt.ledservers.R;
import com.clt.netmessage.NMDetectSender;
import com.clt.netmessage.NMDetectSenderAnswer;
import com.clt.netmessage.NMSetEDID;
import com.clt.netmessage.NMSetSenderBasicParameters;
import com.clt.service.MainService;
import com.google.gson.Gson;

/**
 * 首页html
 * @author Administrator
 *
 */
public class HtmlSenderCardServlet extends HtmlBasePageServlet
{
    private static HtmlSenderCardServlet instance;

    private static String action = "com.clt.language";

    private ArrayList<Program> programs;

    private SenderInfo senderInfo;

    private Response response;

    // public static HtmlIndexServlet getInstance(Context context){
    // if(instance==null){
    // instance=new HtmlIndexServlet(context);
    // }
    // return instance;
    // }

    public HtmlSenderCardServlet(Context context)
    {
        super(context);
    }

    public static HtmlSenderCardServlet getInstance(
            android.content.Context context)
    {
        if (instance == null)
        {
            instance = new HtmlSenderCardServlet(context);
        }
        return instance;
    }

    @Override
    public void doGet(Request request, Response response)
    {
        this.response = response;
        try
        {
            initHtml();
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    private void initHtml()
    {
        StringBuffer sb = new StringBuffer();
        String [] scripts =
            {
                    "jquery.js", "sendcard.js"
            };
        doHeader(sb, scripts);
        doMenuBar(sb);

        sb.append(initDetectCard());
        sb.append(initResolution());
        sb.append(initDisplayMode());
        sb.append(initVideoSource());
        sb.append(initDviInfo());
        sb.append(initTemperture());

        doFooter(sb);
        this.response.send(sb.toString());
    }

    @Override
    public void doPost(Request request, Response response)
    {
        this.response = response;
        if (request.containKey(Params.detectSender))// 设置亮度
        {
            detectCard();
        }
        else if (request.containKey(Params.resolution))
        {
            String str = request.getParmeter(Params.resolution);
            String [] values = str.split("\\*");
            int width = Integer.parseInt(values[0]);
            int height = Integer.parseInt(values[1]);
            setSenderResolution(width, height, 60);

        }
        else if (request.containKey(Params.resolutionWidth))
        {
            try
            {
                int width = Integer.parseInt(request
                        .getParmeter(Params.resolutionWidth));
                int height = Integer.parseInt(request
                        .getParmeter(Params.resolutionHeight));
                int frameRate = Integer.parseInt(request
                        .getParmeter(Params.resolutionFrameRate));
                setSenderResolution(width, height, frameRate);
            }
            catch (Exception e)
            {
            }
           
        }
        else if (request.containKey(Params.videoSource))
        {
            int index = Integer.parseInt(request
                    .getParmeter(Params.videoSource));
            setVideoSource(index);
        }
    }

    /**
     * 设置视频来源
     * @param index
     */
    private void setVideoSource(int index)
    {
        if (senderInfo == null)
        {
            return;
        }
        NMSetSenderBasicParameters basicParams = new NMSetSenderBasicParameters();
        // 构建一个SenderParameters数据封装类
        SenderParameters params = new SenderParameters();
        params.setbBigPack(senderInfo.isBigPacket());
        params.setbAutoBright(senderInfo.isAutoBright());
        params.setM_frameRate(senderInfo.getFrameRate());
        params.setRealParamFlag(senderInfo.isRealParamFlags());
        params.setbZeroDelay(senderInfo.isBZeroDelay());
        params.setRgbBitsFlag(senderInfo.getTenBitFlag());
        params.setbHDCP(senderInfo.isBHDCP());
        params.setPorts(senderInfo.getPorts());
        // 信号输入类型 0为hdmi 1为DVI 3C1S
        if (index == 2)
        {
            index = 3;
        }
        int inputType = senderInfo.getInputType();
        int myvalue = ((inputType & 0xf0) | (index & 0x0f));
        params.setInputType(myvalue);
        basicParams.setParams(params);

        Gson gson = new Gson();
        String nmString = gson.toJson(basicParams);
        Intent intent = new Intent();
        intent.putExtra("netMessage", nmString);
        MainService.startService(context, intent);
    }

    /**
     * 探测发送卡
     */
    private void detectCard()
    {
        NMDetectSender nmDetectSender = new NMDetectSender();
        Gson gson = new Gson();
        String nmString = gson.toJson(nmDetectSender);
        Intent intent = new Intent();
        intent.putExtra("netMessage", nmString);
        MainService.startService(context, intent);
    }

    public void onDetectCardAnswer(NMDetectSenderAnswer nmDetectSenderAnswer)
    {
        if (nmDetectSenderAnswer.getErrorCode() == 1)
        {
            this.senderInfo = nmDetectSenderAnswer.getSenderInfo();
        }
        initHtml();
    }

    /**
     * 设置发送卡分辨率
     * @param width
     * @param height
     */
    public void setSenderResolution(int width, int height, int freq)
    {
        NMSetEDID nmSetEdid = new NMSetEDID();
        nmSetEdid.setWidth(width);
        nmSetEdid.setHeight(height);
        nmSetEdid.setFreq(freq);
        Gson gson = new Gson();
        String nmString = gson.toJson(nmSetEdid);

        Intent intent = new Intent();
        intent.putExtra("netMessage", nmString);
        MainService.startService(context, intent);
    }

    public String initDetectCard()
    {
        String str = null;
        if (senderInfo != null)
        {
            str = "<div class='slideItem'>\n" + "<div id='sender_card'>\n"
                    + "<p>Sender Card:</p>\n" + "<div>"
                    + getRString(R.string.sender_type) + " "
                    + senderInfo.getSenderType() + " "
                    + senderInfo.getMajorVersion() + "."
                    + senderInfo.getMinorVersion() + "</div>\n"
                    + "<a id='btnDetect'>Detect</a> </div>\n" + "</div>\n";
        }
        else
        {
            str = "<div class='slideItem'>\n" + "<div id='sender_card'>\n"
                    + "<p>"+getRString(R.string.sender_type)+"</p>\n" + "<div></div>\n"
                    + "<a id='btnDetect'>Detect</a> </div>\n" + "</div>\n";
        }
        return str;
    }

    public String initResolution()
    {
        StringBuffer sb = new StringBuffer();
        sb.append("<div class='slideItem'>\n" + "<div id='resolution'>\n"
                + "<p>"+getRString(R.string.resolution)+"</p>\n" + "<div>\n"
                + "<select name='resolution' id='resolutions'>\n");
        // 添加option
        String [] resolutions = context.getResources().getStringArray(
                R.array.resolution);
        int selectIndex = 1;
        if (senderInfo != null)
        {
            int width = senderInfo.getResolutionWidth();
            int height = senderInfo.getResolutionHeight();
            String sss = width + "*" + height;
            int indexs = getIndexFromArray(resolutions, sss);// 非自定义
        }
        for (int i = 0; i < resolutions.length; i++)
        {
            if (i == selectIndex)
            {
                sb.append("<option selected='selected'>" + resolutions[i]
                        + "</option>\n");
            }
            else
            {
                sb.append("<option>" + resolutions[i] + "</option>\n");
            }

        }
        sb.append("</select>\n" + "</div>\n"
                + "<a id='btn_res_set'>Set</a> </div>\n" + "</div>\n"
                + "<div class='slideItem' style='display:none' id='res_w_h'>\n"
                + "<div id='resolution_add'>\n" + "<p></p>\n"
                + "<div id='res_add_w'>\n"
                + "<input type='text' id='input_res_w' />\n" + "</div>\n"
                + "<div id='res_add_x'> X </div>\n" + "<div id='res_add_h'>\n"
                + "<input  type='text' id='input_res_h'/>\n" + "</div>\n"
                + "<div id='res_frame_rate'>\n" + "<a>Frame</a>\n"
                + "<p><select id='frame_rate'>");
        String [] frameRates = context.getResources().getStringArray(
                R.array.frame_rate);
        for (int i = 0; i < frameRates.length; i++)
        {
            sb.append("<option>" + frameRates[i] + "</option>\n");
        }
        sb.append("</select>\n" + "</p>\n" + "</div>\n" + "</div>\n" + "</div>");
        return sb.toString();
    }

    public String initDisplayMode()
    {
        StringBuffer sb = new StringBuffer();
        sb.append("<div class='slideItem'>\n" + "<div id='showmode'>\n"
                + "<p>"+getRString(R.string.display_mode)+"</p>\n" + "<div>\n"
                + "<select name='showmode' id='showMode'>\n");
        String [] displaymodes = context.getResources().getStringArray(
                R.array.display_mode);
        for (int i = 0; i < displaymodes.length; i++)
        {
            sb.append("<option value='" + i + "'>" + displaymodes[i]
                    + "</option>\n");
        }
        sb.append("</select>\n" + "</div>\n" + "</div>\n" + "</div>");
        return sb.toString();
    }

    public String initVideoSource()
    {
        StringBuffer sb = new StringBuffer();
        sb.append("<div class='slideItem'>\n" + "<div id='videosource'>\n"
                + "<p>"+getRString(R.string.video_source)+"</p>\n" + "<div>\n"
                + "<select name='showmode' id='videoSource'>\n");
        String [] videoSources = context.getResources().getStringArray(
                R.array.video_source);
        int index = 0;
        if (senderInfo != null)
        {
            index = senderInfo.getInputType();
        }
        if (index == 3)
        {
            index = 2;
        }
        for (int i = 0; i < videoSources.length; i++)
        {
            if (i == index)
            {
                sb.append("<option value='" + i + "' selected='selected'>"
                        + videoSources[i] + "</option>\n");
            }
            else
            {
                sb.append("<option value='" + i + "'>" + videoSources[i]
                        + "</option>\n");
            }
        }
        sb.append("</select>\n" + "</div>\n" + "</div>\n" + "</div>\n");
        return sb.toString();

    }

    public String initDviInfo()
    {
        StringBuffer sb = new StringBuffer();
        sb.append("<div class='slideItem'>\n" + "<div id='framerate'>\n"
                + "<p>"+getRString(R.string.frame_rate)+"</p>\n" + "<div>\n");
        if (senderInfo != null)
        {
            sb.append("<input type='text' id='frameRate' value='"
                    + senderInfo.getFrameRate() + "' readonly='true'/>\n");
        }
        else
        {
            sb.append("<input type='text' id='frameRate' readonly='true'/>\n");
        }
        sb.append("</div>\n" + "</div>\n" + "</div>\n"
                + "<div class='slideItem'>\n" + "<div id='dvi_width'>\n"
                + "<p>"+getRString(R.string.width)+"</p>\n" + "<div>\n");
        if (senderInfo != null)
        {
            sb.append("<input type='text' id='dviWidth' value='"
                    + senderInfo.getResolutionWidth() + "' readonly='true'/>\n");
        }
        else
        {
            sb.append("<input type='text' id='dviWidth' readonly='true'/>\n");
        }
        sb.append("</div>\n" + "</div>\n" + "</div>\n"
                + "<div class='slideItem'>\n" + "<div id='dvi_height'>\n"
                + "<p>"+getRString(R.string.height)+"</p>\n" + "<div>\n");
        if (senderInfo != null)
        {
            sb.append("<input type='text' id='dviHeight' value='"
                    + senderInfo.getResolutionHeight()
                    + "' readonly='true'/>\n");
        }
        else
        {
            sb.append("<input type='text' id='dviHeight' readonly='true'/>\n");
        }
        sb.append("</div>\n" + "</div>\n" + "</div>");
        return sb.toString();
    }

    public String initTemperture()
    {
        StringBuffer sb = new StringBuffer();
        String temp = "";
        if (senderInfo != null)
        {
            temp = senderInfo.getTemperature() + "℃";
        }
        sb.append("<div class='slideItem'>\n" + "<div id='temperture'>\n"
                + "<p>"+getRString(R.string.tempeture)+"</p>\n" + "<div id='ssTemperture'>" + temp
                + "</div>\n" + "</div>\n" + "</div>");
        return sb.toString();
    }

    /**
     * 从数组中获取当前值的索引，没有则返回1
     */
    private int getIndexFromArray(String [] arr, String res)
    {
        for (int i = 0; i < arr.length; i++)
        {
            if (arr[i].trim().equalsIgnoreCase(res))
            {
                return i;
            }
        }
        return 1;
    }

    String detectcard = "<div class='slideItem'>\n"
            + "<div id='sender_card'>\n" + "<p>Sender Card:</p>\n"
            + "<div> </div>\n" + "<a id='btnDetect'>Detect</a> </div>\n"
            + "</div>\n";

    String resolution = "<div class='slideItem'>\n" + "<div id='resolution'>\n"
            + "<p>Resolution:</p>\n" + "<div>\n"
            + "<select name='resolution' id='resolutions'>\n"
            + "<option value='0' selected='selected'>800*600</option>\n"
            + "<option value='0' selected='selected'>900*700</option>\n"
            + "</select>\n" + "</div>\n"
            + "<a id='btn_res_set'>Set</a> </div>\n" + "</div>\n"
            + "<div class='slideItem' style='clear:both' id='res_w_h'>\n"
            + "<div id='resolution_add'>\n" + "<p></p>\n"
            + "<div id='res_add_w'>\n"
            + "<input type='text' id='input_res_w' />\n" + "</div>\n"
            + "<div id='res_add_x'> X </div>\n" + "<div id='res_add_h'>\n"
            + "<input  type='text' id='input_res_h'/>\n" + "</div>\n"
            + "<div id='res_frame_rate'>\n" + "<a>Frame</a>\n"
            + "<p><select id='frame_rate'>\n"
            + "<option value='0' selected='selected'>Off</option>\n"
            + "<option value='1' selected='selected'>Off</option>\n"
            + "</select>\n" + "</p>\n" + "</div>\n" + "</div>\n" + "</div>";

    String displayMode = "<div class='slideItem'>\n" + "<div id='showmode'>\n"
            + "<p>Display Mode:</p>\n" + "<div>\n"
            + "<select name='showmode' id='showMode'>\n"
            + "<option value='0' selected='selected'>Off</option>\n"
            + "<option value='1'>Red</option>\n"
            + "<option value='2'>Green</option>\n" + "</select>\n" + "</div>\n"
            + "</div>\n" + "</div>";

    String videoSource = "<div class='slideItem'>\n"
            + "<div id='videosource'>\n" + "<p>Video Source:</p>\n" + "<div>\n"
            + "<select name='showmode' id='videoSource'>\n"
            + "<option value='0' selected='selected'>HDMI</option>\n"
            + "<option value='1'>DVI</option>\n"
            + "<option value='2'>C1S</option>\n" + "</select>\n" + "</div>\n"
            + "</div>\n" + "</div>\n";

    String dviInfo = "<div class='slideItem'>\n" + "<div id='framerate'>\n"
            + "<p>Frame Rate:</p>\n" + "<div>\n"
            + " <input type='text' id='frameRate' readonly='true'/>\n"
            + "</div>\n" + "</div>\n" + "</div>\n"
            + "<div class='slideItem'>\n" + "<div id='dvi_width'>\n"
            + "<p>width:</p>\n" + "<div>\n"
            + "<input type='text' id='dviWidth' readonly='true'/>\n"
            + "</div>\n" + "</div>\n" + "</div>\n"
            + "<div class='slideItem'>\n" + "<div id='dvi_height'>\n"
            + "<p>Height:</p>\n" + "<div>\n"
            + "<input type='text' id='dviHeight' readonly='true'/>\n"
            + "</div>\n" + "</div>\n" + "</div>";

    String temperture = "<div class='slideItem'>\n" + "<div id='temperture'>\n"
            + "<p>Temperture:</p>\n" + "<div id='ssTemperture'> </div>\n"
            + "</div>\n" + "</div>";
}
