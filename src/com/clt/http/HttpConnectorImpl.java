package com.clt.http;

import java.io.File;
import java.io.FileInputStream;

import android.content.Context;

import com.clt.HTTPConnector;
import com.clt.Wrapper;
import com.clt.http.NanoHTTPD2.Response.Status;
import com.clt.service.CommandExcutorImpl;

/**
 * An example of subclassing NanoHTTPD to make a custom HTTP server.
 */
public class HttpConnectorImpl extends NanoHTTPD2 implements HTTPConnector
{

   

    private static final int Context = 0;

    private CommandExcutorImpl mCommandExcutorThread;

    private Context context;

    /**
     * html页名称
     */
    public static final class Page
    {
        public static final String INDEX = "/index";

        public static final String SENDER_CARD = "/sendercard";
    }

    /**
     * 请求参数
     * @author Administrator
     *
     */
    public static final class Params
    {
        /**index页参数**/
        public static final String brightness = "brightness";// 亮度

        public static final String rBrightness = "rbrightness";// R

        public static final String gBrightness = "gbrightness";// G

        public static final String bBrightness = "bbrightness";// B

        public static final String colorTemp = "colortemp";// 色温

        public static final String onOff = "onoff";// 开关

        /**screenManager页参数**/
        public static final String detectSender = "detectCard";// 探卡

        // 网口面积
        public static final String portResolution = "portResolution";// 网口

        public static final String aStartX = "aPortX";// 列起点

        public static final String aStartY = "aPortY";// 行起点

        public static final String aWidth = "aPortWidth";// 宽度

        public static final String aHeight = "aPortHeight";// 高度

        public static final String bStartX = "bPortX";// 列起点

        public static final String bStartY = "bPortY";// 行起点

        public static final String bWidth = "bPortWidth";// 宽度

        public static final String bHeight = "bPortHeight";// 高度

        // 测试模式
        public static final String testMode = "testmode";// 测试模式索引

        // 自动亮度调节
        public static final String autoBrightTune = "autoBrightTune";//

        public static final String receiverCard = "receiverCard";//

        public static final String overSwitch = "overSwitch";//

        public static final String frameRate = "frameRate";//

        // 获得界面
        public static final String getProgramList = "getProgramList";

        public static final String setProgramIndex = "setProgramIndex";

        // 接收卡参数设置
        public static final String mode = "mode";

        public static final String row = "row";

        public static final String column = "column";

        // 语言切换
        public static final String language = "language";

        // 节目切换
        public static final String path = "path";

        public static final String fileName = "file_name";

        // 发送卡分辨率
        public static final String resolution = "resolution";//

        public static final String resolutionWidth = "resolutionWidth";//

        public static final String resolutionHeight = "resolutionHeight";//

        public static final String resolutionFrameRate = "resolutionFrameRate";//

        // 显示模式
        public static final String displayMode = "displayMode";

        // 视频来源
        public static final String videoSource = "videoSource";

    }
    public HttpConnectorImpl(String hostname, int port)
    {
        super(hostname, port);
    }

    public void setContext(Context context){
        this.context=context;
    }

    @Override
    public void serve(String uri, Method method, Request request,
            Response response)
    {
        try
        {

            if (uri.endsWith(".css") || uri.endsWith(".js"))// js或css
            {
                File file = new File(uri);
                if (file.exists() && file.canRead())
                {
                    response.setStatus(Status.OK);
                    String mimeType = uri.endsWith(".css") ? "text/css"
                            : "text/javascript";
                    response.setMimeType(mimeType);
                    response.setData(new FileInputStream(file));
                    response.send();
                }
            }
            else if (isImage(uri))
            {// 图片
                File file = new File(uri);
                if (file.exists() && file.canRead())
                {
                    response.setStatus(Status.OK);
                    String mimeType = getImgMimetype(uri);
                    response.setMimeType(mimeType);
                    response.setData(new FileInputStream(file));
                    response.send();
                }
            }
            else if (uri.endsWith("/") || uri.endsWith(Page.INDEX))// index
            {
                new HtmlIndexServlet(context).doRequest(method, request,
                        response);
            }
            else if (uri.endsWith(Page.SENDER_CARD))//
            {
                HtmlSenderCardServlet.getInstance(context).doRequest(method,
                        request, response);
            }

        }
        catch (Exception e)
        {
            e.printStackTrace();
        }

    }

    /**
     * 判断是否是图片
     * @param uri
     * @return
     */
    private boolean isImage(String uri)
    {
        if (uri.endsWith(".gif") || uri.endsWith(".png")
                || uri.endsWith(".jpg"))
        {
            return true;
        }
        return false;

    }

    private String getImgMimetype(String uri)
    {
        if (uri.endsWith(".gif"))
        {
            return "image/gif";
        }
        else if (uri.endsWith(".png"))
        {
            return "image/png";
        }
        else if (uri.endsWith(".jpg"))
        {
            return "image/jpeg";
        }
        return null;
    }



}
