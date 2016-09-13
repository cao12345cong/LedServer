package com.clt.operation;

import java.util.LinkedHashMap;

import com.clt.commondata.SenderParameters;

/**
 * 分时段亮度调节
 *
 */
public class SoSetDayPeriodBright extends SenderOperation
{

    protected int senderIndex = 0;

    protected LinkedHashMap<String, Integer> maps = new LinkedHashMap<String, Integer>();

    public int getSenderIndex()
    {
        return senderIndex;
    }

    public void setSenderIndex(int senderIndex)
    {
        this.senderIndex = senderIndex;
    }

    public SoSetDayPeriodBright()
    {
        optertorType = OperatorType.setDayPeriodBright;
    }

    public LinkedHashMap<String, Integer> getMaps()
    {
        return maps;
    }

    public void setMaps(LinkedHashMap<String, Integer> maps)
    {
        this.maps = maps;
    }

}
