package com.clt.util;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Enumeration;

import org.apache.http.conn.util.InetAddressUtils;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.DhcpInfo;
import android.net.NetworkInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.util.Log;

/**
 * netutil.
 * @author Abelart.
 */
public class NetUtil
{
    private static final String TAG = "NetUtil";

    /**
     * 网络连接是否可用
     */
    public static boolean isConnnected(Context context)
    {
        ConnectivityManager connectivityManager = (ConnectivityManager) context
                .getSystemService(Context.CONNECTIVITY_SERVICE);
        if (null != connectivityManager)
        {
            NetworkInfo networkInfo[] = connectivityManager.getAllNetworkInfo();

            if (null != networkInfo)
            {
                for (NetworkInfo info : networkInfo)
                {
                    if (info.getState() == NetworkInfo.State.CONNECTED)
                    {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    /**
     * 是否是wifi连接
     */
    public static boolean isWifiConnect(Context context)
    {

        ConnectivityManager connManager = (ConnectivityManager) context
                .getSystemService(Context.CONNECTIVITY_SERVICE);

        NetworkInfo mWifi = connManager
                .getNetworkInfo(ConnectivityManager.TYPE_WIFI);

        return mWifi.isConnected();

    }

    /**
     * 获得本机Ip地址
     * @return
     */
    public static String getWifiIpAddress(Context context)
    {
        WifiManager wifiManager = (WifiManager) context
                .getSystemService(Context.WIFI_SERVICE);
        if (!wifiManager.isWifiEnabled())
        {
            wifiManager.setWifiEnabled(true);
        }

        WifiInfo wifiInfo = wifiManager.getConnectionInfo();
        int ipAddress = wifiInfo.getIpAddress();

        // 格式化IP address，例如：格式化前：1828825280，格式化后：192.168.1.109
        String ip = String.format("%d.%d.%d.%d", (ipAddress & 0xff),
                (ipAddress >> 8 & 0xff), (ipAddress >> 16 & 0xff),
                (ipAddress >> 24 & 0xff));
        return ip;

    }

    public static String getIpAddress(Context context)
    {
        try
        {
            for (Enumeration<NetworkInterface> en = NetworkInterface
                    .getNetworkInterfaces(); en.hasMoreElements();)
            {
                NetworkInterface intf = en.nextElement();
                for (Enumeration<InetAddress> enumIpAddr = intf
                        .getInetAddresses(); enumIpAddr.hasMoreElements();)
                {
                    InetAddress inetAddress = enumIpAddr.nextElement();
                    if (!inetAddress.isLoopbackAddress()
                            && InetAddressUtils.isIPv4Address(inetAddress
                                    .getHostAddress()))
                    {
                        return inetAddress.getHostAddress().toString();
                    }
                }
            }
        }
        catch (SocketException ex)
        {
            Log.e("WifiPreference IpAddress", ex.toString());
        }
        return null;
    }

    /**
     * 获取子网掩码
     * @return
     */
    public static String getLocalMacAddress(Context context)
    {
        WifiManager wifi = (WifiManager) context
                .getSystemService(Context.WIFI_SERVICE);
        WifiInfo info = wifi.getConnectionInfo();
        String mac = info.getMacAddress();
        
        WifiManager wm = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
        DhcpInfo d = wm.getDhcpInfo();
//        FormatString(d.ipAddress).toString();//ip
//        FormatString(d.gateway).toString();//默认网关
        return FormatString(d.netmask).toString();
    }

    /**
     * 获取子网掩码
     * @return
     */
    public static String getGateWay(Context context)
    {
        WifiManager wifi = (WifiManager) context
                .getSystemService(Context.WIFI_SERVICE);
        WifiInfo info = wifi.getConnectionInfo();
        String mac = info.getMacAddress();
        
        WifiManager wm = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
        DhcpInfo d = wm.getDhcpInfo();
//        FormatString(d.ipAddress).toString();//ip
//        FormatString(d.gateway).toString();//默认网关
        return FormatString(d.gateway).toString();
    }
    public static String FormatString(int value)
    {
        String strValue = "";
        byte [] ary = intToByteArray(value);
        for (int i = ary.length - 1; i >= 0; i--)
        {
            strValue += (ary[i] & 0xFF);
            if (i > 0)
            {
                strValue += ".";
            }
        }
        return strValue;
    }

    public static byte [] intToByteArray(int value)
    {
        byte [] b = new byte [4];
        for (int i = 0; i < 4; i++)
        {
            int offset = (b.length - 1 - i) * 8;
            b[i] = (byte) ((value >>> offset) & 0xFF);
        }
        return b;
    }
}