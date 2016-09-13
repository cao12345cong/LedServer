package com.clt.http;

import android.content.Context;
import android.database.Cursor;

import com.clt.ledservers.R;

public class HTMLHelper
{
    
    private static String [] _navBarLabels =
        {
                "Index", "Sender Card"
        };

    private static String [] _navBarItems =
        {
                "/index", "/sendercard"
                
        };
    
    private static String[] ids={
        
        "index", "sendercard"
    };

    private static String [] _phrases =
        {
                "Now with 100% more awesome.",
                "Better than cake before dinner!", "Chuck Norris approves.",
                "werkin teh intarwebz sinse 1841", "It's lemon-y fresh!",
                "More amazing than a potato.",
                "All the cool kids are doing it!", "Open sauce, eh?",
                "<code>Nothing happens.</code>"
        };

    public static void doFooter(StringBuffer sb) 
    {
        sb.append("    </div>\n");
        sb.append("    <div class='wrap'>\n");
        sb.append("    <div id='footer'>\n");
        sb.append(" ");
        sb.append("    </div>\n</div>\n");
        sb.append("</body>\n");
        sb.append("</html>\n");

    }

    public static void doHeader(StringBuffer sb)
    {
        doHeader(sb,new String []
            {
                    "/console/jquery.js", "/console/jquery.tablesorter.min.js"
            });
    }

    public static void doHeader(StringBuffer sb,String [] scripts)
           
    {
        sb.append("<html xmlns='http://www.w3.org/1999/xhtml'>\n");
        sb.append("<head>\n");
        sb.append("<meta http-equiv='Content-Type' content='text/html; charset=UTF-8'>\n");
        sb.append("    <title>LedServer</title>\n");
        sb.append("    <link rel='stylesheet' type='text/css' href='/mnt/sdcard/ledserver/css/common.css' />\n");

        if (scripts != null)
        {
            for (String script : scripts)
            {
                script="/mnt/sdcard/ledserver/js/"+script;
                sb.append("    <script src='"+script+"' type='text/javascript'></script>\n");
            }

            //sb.append("    <script>$(document).ready(function() { $('table').tablesorter(); });</script>");
        }

        sb.append("</head>\n");
        sb.append("<body>\n");
    }

    public static void doMenuBar(StringBuffer sb,Context context)
    {
        sb.append("<div class='wrap'>\n");
        sb.append("    <div id='navigation'>\n");
        sb.append("    <ul>\n");
        //String path = request.getServletPath();
        String [] _navBarLabels =
                {
                        context.getResources().getString(R.string.home_page), 
                        context.getResources().getString(R.string.send_card), 
                };
        for (int i = 0; i < _navBarItems.length; i++)
        {
            sb.append("<li id='"+ids[i]+"'>");

            String [] splitPath = _navBarItems[i].split("/");
                sb.append("<a href='" + _navBarItems[i] + "'>"
                        + _navBarLabels[i] + "</a>");

            sb.append("</li>\n");
        }
        sb.append("    </ul>\n");
        sb.append("    </div>\n");
        sb.append("    </div>\n");
        sb.append("    <div class='wrap main'>\n");
    }

    public static void formatTable(String [] colNames, Cursor cursor,
            StringBuffer sb)
    {
        if ((colNames != null) && (cursor != null) && (sb != null))
        {
            sb.append("<table>");
            sb.append("<thead>");
            sb.append("<tr>");
            for (int i = 0; i < colNames.length; i++)
            {
                sb.append("    <th>" + colNames[i] + "</th>");
            }
            sb.append("</tr>");
            sb.append("</thead>");
            sb.append("<tbody>");
            int row = 0;
            while (cursor.moveToNext())
            {
                String classExtra = getRowStyle(row);
                sb.append("<tr>");
                for (int i = 0; i < colNames.length; i++)
                {
                    String val = cursor.getString(i);
                    sb.append("<td" + classExtra + ">"
                            + (val == null ? "&nbsp;" : val) + "</td>");
                }
                sb.append("</tr>");
                row++;
            }
            sb.append("</tbody>");
            sb.append("</table>");
        }
    }

    public static String getRowStyle(int row)
    {
        if (row % 2 == 0)
        {
            return "";
        }

        return " class='odd'";
    }
}