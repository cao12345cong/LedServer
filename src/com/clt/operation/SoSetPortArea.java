package com.clt.operation;

import com.clt.parser.AreaConfig;

public class SoSetPortArea extends SenderOperation
{

    private AreaConfig areaConfig;
    public SoSetPortArea()
    {
        optertorType = OperatorType.setAreaPortByXML;
    }
    public AreaConfig getAreaConfig()
    {
        return areaConfig;
    }
    public void setAreaConfig(AreaConfig areaConfig)
    {
        this.areaConfig = areaConfig;
    }


}
