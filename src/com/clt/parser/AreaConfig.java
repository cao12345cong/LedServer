package com.clt.parser;

import com.clt.commondata.PortArea;

public class AreaConfig
{
    private String width;

    private String height;

    private PortArea [] PortAreas=new PortArea[4];

    public String getWidth()
    {
        return width;
    }

    public void setWidth(String width)
    {
        this.width = width;
    }

    public String getHeight()
    {
        return height;
    }

    public void setHeight(String height)
    {
        this.height = height;
    }

    public PortArea [] getPortAreas()
    {
        return PortAreas;
    }

    public void setPortAreas(PortArea [] portAreas)
    {
        PortAreas = portAreas;
    }

    
    
}
