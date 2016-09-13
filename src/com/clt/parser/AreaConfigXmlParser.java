package com.clt.parser;

import java.io.InputStream;

import org.xmlpull.v1.XmlPullParser;

import com.clt.commondata.PortArea;

import android.util.Xml;

/**
 * 自动亮度调节曲线xml解析
 * @author caocong 2014.07.09
 *
 */
public class AreaConfigXmlParser
{
    
    public static AreaConfig readXmlByPull(InputStream inputStream)
            throws Exception
    {
        AreaConfig areaConfig=null;
        
        XmlPullParser xmlpull = Xml.newPullParser();
        xmlpull.setInput(inputStream, "utf-8");
        int eventCode = xmlpull.getEventType();
        
        PortArea portArea=null;
        int len=0;
       
        while (eventCode != XmlPullParser.END_DOCUMENT)
        {

            switch (eventCode)
            {
                case XmlPullParser.START_DOCUMENT:
                {
                    areaConfig=new AreaConfig();
                    break;
                }
                case XmlPullParser.START_TAG:
                {
                    if ("Width".equals(xmlpull.getName()))
                    {
                        areaConfig.setWidth(xmlpull.nextText());
                    }
                    else if ("Height".equals(xmlpull.getName()))
                    {
                        areaConfig.setHeight(xmlpull.nextText());
                    }else if("PortArea".equals(xmlpull.getName())){
                        portArea=new PortArea();
                    }else if("PortIndex".equals(xmlpull.getName())){
                        //portArea.setPortIndex(Integer.parseInt(xmlpull.nextText()));
                    }else if("StartX".equals(xmlpull.getName())){
                        portArea.setStartX(Integer.parseInt(xmlpull.nextText()));
                    }else if("StartY".equals(xmlpull.getName())){
                        portArea.setStarty(Integer.parseInt(xmlpull.nextText()));
                    }else if("ColWidth".equals(xmlpull.getName())){
                        portArea.setWidth(Integer.parseInt(xmlpull.nextText()));
                    }else if("RowHeight".equals(xmlpull.getName())){
                        portArea.setHeight(Integer.parseInt(xmlpull.nextText()));
                        areaConfig.getPortAreas()[len++]=portArea;
                    }
                    break;
                }

                case XmlPullParser.END_TAG:
                {
                    break;
                }
            }

            eventCode = xmlpull.next();

        }

        return areaConfig;
    }
    
    
    
    
    
}
