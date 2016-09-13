package com.clt.entity;

import java.util.Formatter;

/**
 * EDID方面的信息
 * 
 * @author Administrator
 * 
 */
public class EdidInfo
{
    byte [] m_edidRaw = new byte [128];

    public byte [] getByteArr()
    {
        return m_edidRaw;
    }

    public EdidInfo()
    {
        init();
    }

    enum TimingID
    {
        timing_id_800_600_60, 
        timing_id_1024_768_60, 
        timing_id_1280_960_60, 
        timing_id_1280_1024_60,
        timing_id_1366_768_60, 
        timing_id_1440_900_60, 
        timing_id_1440_1050_60, 
        timing_id_1600_900_60, 
        timing_id_1680_1050_60, 
        timing_id_1920_1080_60, 
        timing_id_1920_1200_60, 
        timing_id_2048_1152_60, 
        timing_id_800_600_120, 
        timing_id_count
    };

    enum TimingVarName
    {
        timing_var_name_h_width, 
        timing_var_name_h_blank, 
        timing_var_name_h_sync_offset, 
        timing_var_name_h_sync, 
        timing_var_name_h_border, 
        timing_var_name_v_width, 
        timing_var_name_v_blank, 
        timing_var_name_v_sync_offset, 
        timing_var_name_v_sync, 
        timing_var_name_v_border, 
        timing_var_count
    };

    enum HblankE
    {
        hBlank_800, 
        hBlank_1024, 
        hBlank_1280, 
        hBlank_1366, 
        hBlank_1440, 
        hBlank_1600, 
        hBlank_1680, 
        hBlank_1920, 
        hBlank_2048, 
        hBlank_Counts
    };

    enum VblankE
    {
        vBlank_600, 
        vBlank_768, 
        vBlank_900, 
        vBlank_960, 
        vBlank_1024,
        vBlank_1050, 
        vBlank_1080, 
        vBlank_1152, 
        vBlank_1200, 
        vBlank_Counts
    };

    short timingData[][] =
        {
                    // HWidth HBlank HSync.offset HSync HBorder VWidth VBlank
                    // VSync.offset VSync VBorder
                {
                        800, 256, 40, 128, 0, 600, 28, 1, 4, 0
                },// 800*600@60
                {
                        1024, 320, 24, 136, 0, 768, 38, 3, 6, 0
                },// 1024*768@60
                    {
                            1280, 520, 96, 112, 0, 960, 40, 1, 3, 0
                    },// 1280*960@60
                    {
                            1280, 408, 48, 112, 0, 1024, 42, 1, 3, 0
                    },// 1280*1024@60
                    {
                            1366, 426, 70, 143, 0, 768, 30, 3, 3, 0
                    },// 1366*768@60
                    {
                            1440, 160, 80, 152, 0, 900, 26, 3, 6, 0
                    },// 1440*900@60
                    {
                            1440, 464, 88, 144, 0, 1050, 39, 3, 4, 0
                    },// 1440*1050@60
                    {
                            1600, 160, 48, 32, 0, 900, 26, 3, 5, 0
                    },// 1600*900@60
                    {
                            1680, 160, 104, 176, 0, 1050, 30, 3, 6, 0
                    },// 1680*1050@60
                    {
                            1920, 280, 88, 44, 0, 1080, 45, 4, 5, 0
                    },// 1920*1080@60
                    {
                            1920, 160, 48, 32, 0, 1200, 35, 3, 6, 0
                    },// 1920*1200@60
                    {
                            2048, 160, 48, 32, 0, 1152, 33, 3, 5, 0
                    },// 2048*1152@60
                    {
                            800, 160, 48, 32, 0, 600, 36, 3, 4, 0
                    },// 800*600@120
        };

    short hBlankList[][] =
        {
                    {
                            800, 256
                    },
                    {
                            1024, 320
                    },
                    {
                            1280, 408
                    },
                    {
                            1366, 426
                    },
                    {
                            1440, 160
                    },
                    {
                            1600, 160
                    },
                    {
                            1680, 160
                    },
                    {
                            1920, 280
                    },
                    {
                            2048, 160
                    }
        };

    short vBlankList[][] =
        {
                    {
                            600, 28
                    },
                    {
                            768, 38
                    },
                    {
                            900, 26
                    },
                    {
                            960, 40
                    },
                    {
                            1024, 42
                    },
                    {
                            1050, 39
                    },
                    {
                            1080, 45
                    },
                    {
                            1152, 33
                    },
                    {
                            1200, 35
                    }
        };

    private void init()
    {
        // Header
        m_edidRaw[0] = (byte) 0x00;
        m_edidRaw[1] = (byte) 0xff;
        m_edidRaw[2] = (byte) 0xff;
        m_edidRaw[3] = (byte) 0xff;
        m_edidRaw[4] = (byte) 0xff;
        m_edidRaw[5] = (byte) 0xff;
        m_edidRaw[6] = (byte) 0xff;
        m_edidRaw[7] = (byte) 0x00;
        // 制造商名称
        m_edidRaw[8] = (byte) 0x0d;
        m_edidRaw[9] = (byte) 0x94;
        // 产品代码
        m_edidRaw[10] = (byte) 0x02;
        m_edidRaw[11] = (byte) 0x07;
        // 产品序列号
        m_edidRaw[12] = (byte) 0x00;
        m_edidRaw[13] = (byte) 0x07;
        m_edidRaw[14] = (byte) 0x00;
        m_edidRaw[15] = (byte) 0x02;

        // 制造时间：周
        m_edidRaw[16] = (byte) 0x32;
        // 制造时间：年
        m_edidRaw[17] = (byte) 0x11;
        // 版本号
        m_edidRaw[18] = (byte) 0x01;
        // 修订版本号
        m_edidRaw[19] = (byte) 0x03;
        // 输入视频信号定义
        m_edidRaw[20] = (byte) 0x81;
        // 最大水平图像大小
        m_edidRaw[21] = (byte) 0x1e;
        // 最大垂直图像大小
        m_edidRaw[22] = (byte) 0x17;
        // gamma值
        m_edidRaw[23] = (byte) 0xaa;
        // 支持的功能
        m_edidRaw[24] = (byte) 0xea;
        // 颜色特征
        m_edidRaw[25] = (byte) 0xc1;
        m_edidRaw[26] = (byte) 0xe5;
        m_edidRaw[27] = (byte) 0xa3;
        m_edidRaw[28] = (byte) 0x57;
        m_edidRaw[29] = (byte) 0x4e;
        m_edidRaw[30] = (byte) 0x9c;
        m_edidRaw[31] = (byte) 0x23;
        m_edidRaw[32] = (byte) 0x1D;
        m_edidRaw[33] = (byte) 0x50;
        m_edidRaw[34] = (byte) 0x54;
        // 内建时序
        m_edidRaw[35] = (byte) 0xff; // 01
        m_edidRaw[36] = (byte) 0xff; // 08
        m_edidRaw[37] = (byte) 0x80;
        // 标准时序开始，每个占2BYTE，总共16BYTE
        // 1280*960@60
        m_edidRaw[38] = (byte) 0x81;
        m_edidRaw[39] = (byte) 0x40;
        // 1280*1024@60
        m_edidRaw[40] = (byte) 0x81;
        m_edidRaw[41] = (byte) 0x80;
        // 1440*900@60
        m_edidRaw[42] = (byte) 0x95;
        m_edidRaw[43] = (byte) 0x00;
        // 1600*900@60
        m_edidRaw[44] = (byte) 0xA9;
        m_edidRaw[45] = (byte) 0xC0;
        // 1680*1050@60
        m_edidRaw[46] = (byte) 0xB3;
        m_edidRaw[47] = (byte) 0x00;
        // 1920*1080@60
        m_edidRaw[48] = (byte) 0xD1;
        m_edidRaw[49] = (byte) 0xC0;
        // 1366*768@60
        m_edidRaw[50] = (byte) 0x8B;
        m_edidRaw[51] = (byte) 0xC0;
        // 1440*1050@60
        m_edidRaw[52] = (byte) 0x95;
        m_edidRaw[53] = (byte) 0x40;
        // 第一个详细时序开始,四个，每个占18个字节,第一个用来选择具体时序时候填写
        m_edidRaw[54] = (byte) 0x64;
        m_edidRaw[55] =(byte)  0x2a;
        m_edidRaw[56] =(byte) 0x00;
        m_edidRaw[57] = (byte) 0x98;
        m_edidRaw[58] = (byte) 0x51;
        m_edidRaw[59] = (byte) 0x00;
        m_edidRaw[60] = (byte) 0x2a;
        m_edidRaw[61] = (byte) 0x40;
        m_edidRaw[62] = (byte) 0x30;
        m_edidRaw[63] = (byte) 0x70;
        m_edidRaw[64] =(byte)  0x13;
        m_edidRaw[65] = (byte) 0x00;

        m_edidRaw[66] = (byte) 0x00;
        m_edidRaw[67] = (byte) 0x00;
        m_edidRaw[68] = (byte) 0x00;
        m_edidRaw[69] = (byte) 0x00;
        m_edidRaw[70] = (byte) 0x00;
        m_edidRaw[71] = (byte) 0x1e;
        // m_edidRaw[66] = 0x54;
        // m_edidRaw[67] = 0x0e;
        // m_edidRaw[68] = 0x11;
        // m_edidRaw[69] = 0x00;
        // m_edidRaw[70] = 0x00;
        // m_edidRaw[71] = 0x1e;
        // 第二个详细时序开始,填写固定的，1024*768-60
        m_edidRaw[72] = (byte) 0x00;
        m_edidRaw[73] = (byte) 0x00;
        m_edidRaw[74] = (byte) 0x00;
        m_edidRaw[75] = (byte) 0xff;
        m_edidRaw[76] = (byte) 0x00;
        m_edidRaw[77] = (byte) 0x20;
        m_edidRaw[78] = (byte) 0x42;
        m_edidRaw[79] = (byte) 0x5a;
        m_edidRaw[80] = (byte) 0x20;
        m_edidRaw[81] = (byte) 0x20;
        m_edidRaw[82] = (byte) 0x31;
        m_edidRaw[83] = (byte) 0x36;
        m_edidRaw[84] = (byte) 0x32;
        m_edidRaw[85] = (byte) 0x30;
        m_edidRaw[86] = (byte) 0x37;
        m_edidRaw[87] = (byte) 0x30;
        m_edidRaw[88] = (byte) 0x0a;
        m_edidRaw[89] = (byte) 0x20;
        // 第三个时序开始,填写固定的，1280*1024-60
        m_edidRaw[90] = (byte) 0x00;
        m_edidRaw[91] = (byte) 0x00;
        m_edidRaw[92] = (byte) 0x00;
        m_edidRaw[93] = (byte) 0xfc;
        m_edidRaw[94] = (byte) 0x00;
        m_edidRaw[95] = (byte) 0x4c;
        m_edidRaw[96] = (byte) 0x45;
        m_edidRaw[97] = (byte) 0x44;
        m_edidRaw[98] = (byte) 0x20;

        m_edidRaw[99] = (byte) 0x31;
        m_edidRaw[100] =(byte)  0x32;
        m_edidRaw[101] =(byte)  0x38;
        m_edidRaw[102] =(byte)  0x30;
        m_edidRaw[103] = (byte) 0x58;
        m_edidRaw[104] = (byte) 0x31;
        m_edidRaw[105] =(byte)  0x30;
        m_edidRaw[106] = (byte) 0x32;
        m_edidRaw[107] =(byte)  0x34;
        // 第四个时序开始,填写固定的，1920*1080-60
        m_edidRaw[108] = (byte) 0x00;
        m_edidRaw[109] = (byte) 0x00;
        m_edidRaw[110] = (byte) 0x00;
        m_edidRaw[111] = (byte) 0xfd;
        m_edidRaw[112] = (byte) 0x00;
        m_edidRaw[113] = (byte) 0x38;
        m_edidRaw[114] = (byte) 0x56;
        m_edidRaw[115] = (byte) 0x1e;
        m_edidRaw[116] = (byte) 0x45;
        m_edidRaw[117] = (byte) 0x0a;
        m_edidRaw[118] = (byte) 0x00;
        m_edidRaw[119] = (byte) 0x0a;
        m_edidRaw[120] = (byte) 0x20;
        m_edidRaw[121] = (byte) 0x20;
        m_edidRaw[122] = (byte) 0x20;
        m_edidRaw[123] = (byte) 0x20;
        m_edidRaw[124] = (byte) 0x20;
        m_edidRaw[125] = (byte) 0x20;
        m_edidRaw[126] = (byte) 0x00;
        m_edidRaw[127] = (byte) 0x80;
    }

    /**
     * 
     * @param width
     * @param height
     * @param freq
     *            帧率
     */
    public void changeWH(int width, int height, int freq)
    {
        changeWH2(width, height, Math.min(freq, getMaxFreahFraq(width, height)));
    }

    public int [] getPixelFreq(int width, int height, int freq, int timingID,
            Short pvBlanking, Short phBlanking)
    {
        if (((width == 800) && (height == 600) && (freq == 60)))
        {
            timingID = TimingID.timing_id_800_600_60.ordinal();
        }
        else if (((width == 800) && (height == 600) && (freq == 120)))
        {
            timingID = TimingID.timing_id_800_600_120.ordinal();
        }
        else if (((width == 1024) && (height == 768) && (freq == 60))
                || ((width == 1024) && (height == 768) && (freq == 50)))
        {
            timingID = TimingID.timing_id_1024_768_60.ordinal();
        }
        else if (((width == 1280) && (height == 960) && (freq == 60))
                || ((width == 1280) && (height == 960) && (freq == 50)))
        {
            timingID = TimingID.timing_id_1280_960_60.ordinal();
        }
        else if (((width == 1280) && (height == 1024) && (freq == 60))
                || ((width == 1280) && (height == 1024) && (freq == 50)))
        {
            timingID = TimingID.timing_id_1280_1024_60.ordinal();
        }
        else if (((width == 1366) && (height == 768) && (freq == 60))
                || ((width == 1366) && (height == 768) && (freq == 50)))
        {
            timingID = TimingID.timing_id_1366_768_60.ordinal();
        }
        else if (((width == 1440) && (height == 900) && (freq == 60))
                || ((width == 1440) && (height == 900) && (freq == 50)))
        {
            timingID = TimingID.timing_id_1440_900_60.ordinal();
        }
        else if (((width == 1440) && (height == 1050) && (freq == 60))
                || ((width == 1440) && (height == 1050) && (freq == 50)))
        {
            timingID = TimingID.timing_id_1440_1050_60.ordinal();
        }
        else if (((width == 1600) && (height == 900) && (freq == 60))
                || ((width == 1600) && (height == 900) && (freq == 50)))
        {
            timingID = TimingID.timing_id_1600_900_60.ordinal();
        }
        else if (((width == 1680) && (height == 1050) && (freq == 60))
                || ((width == 1680) && (height == 1050) && (freq == 50)))
        {
            timingID = TimingID.timing_id_1680_1050_60.ordinal();
        }
        else if (((width == 1920) && (height == 1080) && (freq == 60))
                || ((width == 1920) && (height == 1080) && (freq == 50)))
        {
            timingID = TimingID.timing_id_1920_1080_60.ordinal();
        }
        else if (((width == 1920) && (height == 1200) && (freq == 60))
                || ((width == 1920) && (height == 1200) && (freq == 50)))
        {
            timingID = TimingID.timing_id_1920_1200_60.ordinal();
        }
        else if (((width == 2048) && (height == 1152) && (freq == 60))
                || ((width == 2048) && (height == 1152) && (freq == 50)))
        {
            timingID = TimingID.timing_id_2048_1152_60.ordinal();
        }
        if (timingID != -1)
        {
            short tempHblank = timingData[timingID][TimingVarName.timing_var_name_h_blank
                    .ordinal()];
            short tempVblank = timingData[timingID][TimingVarName.timing_var_name_v_blank
                    .ordinal()];
            if (null == pvBlanking || null == phBlanking)
                return new int []
                    {
                        (width + tempHblank) * (height + tempVblank) * freq
                    };
            pvBlanking = tempVblank;
            phBlanking = tempHblank;
            int result = (width + phBlanking) * (height + pvBlanking) * freq;
            return new int []
                {
                        result, pvBlanking, phBlanking, timingID
                };
        }
        if (!(null == pvBlanking || null == phBlanking))
        {
            boolean bCalcSucc = CalcHblankVblank(width, height, pvBlanking,
                    phBlanking);
            if (bCalcSucc)
                return new int []
                    {
                            (width + phBlanking) * (height + pvBlanking) * freq,
                            pvBlanking, phBlanking, timingID
                    };
        }

        short vBlanking = 42;
        short hBlanking = 8;

        int minVBlanking = 0;
        int k = (height + vBlanking) * freq;
        double _pow10_6 = Math.pow(10.0f, 6.0f);
        minVBlanking = (int) (width * k / (_pow10_6 - k));

        hBlanking = (short) (Math.round(minVBlanking) + 48);
        hBlanking = (short) (hBlanking / 16 * 16);

        if (hBlanking + width < 1688)
            hBlanking = (short) (1688 - width);

        if (hBlanking > 208)
        {
            hBlanking = 208;
        }

        int pixelFreq = (width + hBlanking) * (height + vBlanking) * freq;

        if (pvBlanking != null)
            pvBlanking = vBlanking;

        if (phBlanking != null)
            phBlanking = hBlanking;

        if (pvBlanking != null && phBlanking != null)
        {
            return new int []
                {
                        (int) pixelFreq, pvBlanking, phBlanking, timingID
                };
        }
        return new int []
            {
                    (int) pixelFreq, timingID
            };
    }

    private boolean CalcHblankVblank(int width, int height, Short pVblank,
            Short pHblank)
    {

        boolean bSucc = true;
        boolean bEqual = false;
        int hIndex = -1;
        int vIndex = -1;
        double hWeight = 0;
        double vWeight = 0;
        // hblank
        for (int i = 0; i < HblankE.hBlank_Counts.ordinal(); ++i)
        {
            if (hBlankList[i][0] > width)
            {
                hIndex = i;
                break;
            }
            else if (hBlankList[i][0] == width)
            {
                pHblank = hBlankList[i][1];
                hIndex = i;
                bEqual = true;
                break;
            }

        }
        if (-1 == hIndex)
            bSucc = false;
        else
        {
            if (!bEqual)
            {
                if (hIndex > 0)
                {
                    hWeight = (hBlankList[hIndex][0] - width)
                            * 1.0
                            / (hBlankList[hIndex][0] - hBlankList[hIndex - 1][0]);
                    pHblank = (short) Math.round(hBlankList[hIndex][1]
                            * hWeight + hBlankList[hIndex - 1][1]
                            * (1 - hWeight));
                }
                else
                {
                    pHblank = hBlankList[0][1];
                }
            }
        }

        // vblank
        bEqual = false;
        for (int i = 0; i < VblankE.vBlank_Counts.ordinal(); ++i)
        {
            if (vBlankList[i][0] > height)
            {
                vIndex = i;
                break;
            }
            else if (vBlankList[i][0] == height)
            {
                pVblank = vBlankList[i][1];
                vIndex = i;
                bEqual = true;
                break;
            }
        }
        if (-1 == vIndex)
            bSucc = false;
        else
        {
            if (!bEqual)
            {
                if (vIndex > 0)
                {
                    vWeight = (vBlankList[vIndex][0] - height)
                            * 1.0
                            / (vBlankList[vIndex][0] - vBlankList[vIndex - 1][0]);
                    pVblank = (short) Math.round(vBlankList[vIndex][1]
                            * vWeight + vBlankList[vIndex - 1][1]
                            * (1 - vWeight));
                }
                else
                {
                    pVblank = vBlankList[0][1];
                }
            }
        }
        return bSucc;
    }

    public int getMaxFreahFraq(int width, int height)
    {
        int timingID = -1;
        if (getPixelFreq(width, height, 120, timingID, null, null)[0] <= 160 * 1000 * 1000)
            return 120;
        else if (getPixelFreq(width, height, 60, timingID, null, null)[0] <= 160 * 1000 * 1000)
            return 60;
        else if (getPixelFreq(width, height, 50, timingID, null, null)[0] <= 160 * 1000 * 1000)
            return 50;
        else
            return 30;
    }

    public void changeWH2(int width, int height, int freq)
    {
        Short vBlanking = 42;
        Short hBlanking = 8;

        int timingID = -1;
        int [] result = getPixelFreq(width, height, freq, timingID, vBlanking,
                hBlanking);
        int pixelFreq = result[0];
        if (result.length > 1)
        {
            vBlanking = (short) result[1];
            hBlanking = (short) result[2];
            timingID = result[3];
        }

        pixelFreq /= 10000;
        short wPixelFreq = (short) pixelFreq;

        m_edidRaw[54] = LOBYTE(wPixelFreq);
        m_edidRaw[55] = HIBYTE(wPixelFreq);

        byte hWidth = (byte) (width / 256);
        byte lWidth = (byte) (width % 256);

        byte hHeight = (byte) (height / 256);
        byte lHeight = (byte) (height % 256);

        m_edidRaw[56] = lWidth;
        m_edidRaw[57] = LOBYTE(hBlanking);
        m_edidRaw[58] &= 0x0f;
        m_edidRaw[58] |= (hWidth << 4);

        m_edidRaw[58] &= 0xf0;
        m_edidRaw[58] |= (HIBYTE(hBlanking) & 0x0f);

        m_edidRaw[59] = lHeight;
        m_edidRaw[60] = LOBYTE(vBlanking);

        m_edidRaw[61] &= 0x0f;
        m_edidRaw[61] |= (hHeight << 4);

        m_edidRaw[61] &= 0xf0;
        m_edidRaw[61] |= (HIBYTE(vBlanking) & 0x0f);

        if (timingID != -1)
        {
            m_edidRaw[62] = LOBYTE(timingData[timingID][TimingVarName.timing_var_name_h_sync_offset
                    .ordinal()]);
            m_edidRaw[63] = LOBYTE(timingData[timingID][TimingVarName.timing_var_name_h_sync
                    .ordinal()]);
            byte tempVal = 0;
            tempVal = SetBitsInByte(
                    tempVal,
                    4,
                    4,
                    LOBYTE(timingData[timingID][TimingVarName.timing_var_name_v_sync_offset
                            .ordinal()]) & 0x0f);
            tempVal = SetBitsInByte(
                    tempVal,
                    0,
                    4,
                    LOBYTE(timingData[timingID][TimingVarName.timing_var_name_v_sync
                            .ordinal()]) & 0x0f);
            m_edidRaw[64] = tempVal;
            tempVal = 0;
            SetBitsInByte(
                    tempVal,
                    6,
                    2,
                    HIBYTE(timingData[timingID][TimingVarName.timing_var_name_h_sync_offset
                            .ordinal()]) & 0x03);
            SetBitsInByte(
                    tempVal,
                    4,
                    2,
                    HIBYTE(timingData[timingID][TimingVarName.timing_var_name_h_sync
                            .ordinal()]) & 0x03);
            SetBitsInByte(
                    tempVal,
                    2,
                    2,
                    HIBYTE(timingData[timingID][TimingVarName.timing_var_name_v_sync_offset
                            .ordinal()]) & 0x03);
            SetBitsInByte(
                    tempVal,
                    0,
                    2,
                    HIBYTE(timingData[timingID][TimingVarName.timing_var_name_v_sync
                            .ordinal()]) & 0x03);
            m_edidRaw[65] = tempVal;
            tempVal = 0;
            // m_edidRaw[66] = lWidth;
            // m_edidRaw[67] = lHeight;
            //
            // m_edidRaw[68] &= 0x0f;
            // m_edidRaw[68] |= (hWidth<<4);
            // m_edidRaw[68] &= 0xf0;
            // m_edidRaw[68] |= (HIBYTE(hHeight)&0x0f);

        }

        // char[] resString = new char[16];
        String resString = sprintf(width, height);

        // CStringA strDesp;
        // strDesp.Format("LED%5dX%d", width, height);

        for (int i = 0; i < 14; i++)
            m_edidRaw[95 + i] = 0x00;

        int length = resString.length();// strDesp.GetLength();
        for (int i = 0; i < length; i++)
            m_edidRaw[95 + i] = (byte) resString.charAt(i);

        calSumCheck();

        // CFile file;
        // if(file.Open(_T("c:\\i.bir"),
        // CFile::modeCreate|CFile::modeReadWrite))
        // {
        // file.Write(m_edidRaw, 128);
        // }
    }

    private byte SetBitsInByte(byte desByte, int startBitPos, int bitCount,
            int val)
    {
        boolean flag = (((startBitPos >= 0) && (startBitPos < 8)) && ((bitCount
                + startBitPos <= 8) && (bitCount > 0)));
        if (!flag)
        {
            return 0;
        }
        byte i = 0;
        byte mask = 0;
        // 求掩码
        while (i < bitCount)
        {
            mask <<= 1;
            mask++;
            i++;
        }
        mask <<= startBitPos;
        desByte &= ~mask; // 给从startBitPos位开始的bitCount个位清零
        desByte |= mask & (val << startBitPos);
        return desByte;
    }
    /**
     * 取高8位
     * @param wPixelFreq
     * @return
     */
    private byte HIBYTE(Short wPixelFreq)
    {
        return (byte) (wPixelFreq&0xff >> 8);
    }
    /**
     * 取低8位
     * @param wPixelFreq
     * @return
     */
    private byte LOBYTE(short wPixelFreq)
    {
        // TODO Auto-generated method stub
        return (byte) (wPixelFreq&0xff);
    }

    public int getWidth()
    {
        int width = m_edidRaw[56];
        width += (m_edidRaw[58] >> 4) * 256;
        return width;
    }

    public int getHeight()
    {
        int height = m_edidRaw[59];
        height += (m_edidRaw[61] >> 4) * 256;

        return height;
    }

    public void calSumCheck()
    {
        byte sum = 0;
        for (int i = 0; i < 127; i++)
            sum += m_edidRaw[i];

        m_edidRaw[127] = (byte) (256 - sum);
    }

    public String sprintf(int w, int h)
    {
        return String.format("LED%5dX%d", w, h);
    }

}
