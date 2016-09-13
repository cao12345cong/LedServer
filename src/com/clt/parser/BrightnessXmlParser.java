package com.clt.parser;

import java.io.InputStream;

import org.xmlpull.v1.XmlPullParser;

import android.util.Xml;

/**
 * 自动亮度调节曲线xml解析
 * @author caocong 2014.07.09
 *
 */
public class BrightnessXmlParser
{
    
    /**
     * pull解析
     * @param inputStream
     * @return
     * @throws Exception
     */
    public static BrightConfig readBrightnessXmlByPull(InputStream inputStream)
            throws Exception
    {
        BrightConfig brightConfig = null;

        XmlPullParser xmlpull = Xml.newPullParser();
        xmlpull.setInput(inputStream, "utf-8");
        int eventCode = xmlpull.getEventType();

        while (eventCode != XmlPullParser.END_DOCUMENT)
        {

            switch (eventCode)
            {
                case XmlPullParser.START_DOCUMENT:
                {
                    break;
                }
                case XmlPullParser.START_TAG:
                {
                    if ("IsAuto".equals(xmlpull.getName()))
                    {
                        brightConfig = new BrightConfig();
                        brightConfig.setIsAuto(Integer.parseInt(xmlpull
                                .nextText()));
                    }
                    else if ("BrightTune".equals(xmlpull.getName()))
                    {
                        brightConfig.setBrightTune(xmlpull.nextText());
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

        return brightConfig;
    }
}
