package com.clt.util;

import android.os.Environment;

public class Constants
{
    public static final String VERSION="0.2.7";
  // public static final String SAVE_PATH =
  // "/mnt/sdcard/Android/data/com.color.home/files/Download/";
  public static final String SAVE_PATH = "/mnt/usb_storage/USB_DISK0/udisk0/";

  public static final String USB_PATH_0 = "/mnt/usb_storage/USB_DISK0/udisk0/";

  public static final String USB_PATH_1 = "/mnt/usb_storage/USB_DISK1/udisk1/";
  public static final String SDCARD_PATH = Environment
          .getExternalStorageDirectory().getAbsolutePath() + "/";

  public static final String SDCARD_DOWNLOAD_PATH = "/mnt/sdcard/Android/data/com.color.home/files/Download/";
  public static final String SDCARD_SD_PATH = "/mnt/sdcard/Android/data/com.color.home/files/Usb/";
  public static final String SDCARD_SD_FTP = "/mnt/sdcard/Android/data/com.color.home/files/Ftp/program";

  // public static final String SAVE_PATH = Environment
  // .getExternalStorageDirectory().getAbsolutePath() + "/";

  public static final String PROGRAM_EXTENSION = ".vsn";
}
