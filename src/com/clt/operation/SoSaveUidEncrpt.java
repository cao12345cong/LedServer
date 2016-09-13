package com.clt.operation;

public class SoSaveUidEncrpt extends SenderOperation
{
    String sdcardPath;
    
    public SoSaveUidEncrpt()
    {
        optertorType = OperatorType.saveUidEncrpt;
    }

    public String getSdcardPath()
    {
        return sdcardPath;
    }

    public void setSdcardPath(String sdcardPath)
    {
        this.sdcardPath = sdcardPath;
    }

    
}
