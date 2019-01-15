package com.tiddlywiki.atw.ui.main;

import android.content.Context;
import android.graphics.Color;
import android.support.design.widget.NavigationView;
import android.support.v4.widget.DrawerLayout;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.webkit.JavascriptInterface;
import android.webkit.WebView;
import android.widget.Toast;

import com.tiddlywiki.atw.R;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;

import static android.content.ContentValues.TAG;

public class AtwWebAppInterface {
    private Context mContext;
    private WebView mWebView;
    private Window mWindow;

    /** Instantiate the interface and set the context */
    AtwWebAppInterface(Context c, WebView v, Window w) {
        mContext = c;
        mWebView = v;
        mWindow = w;
    }

    /** Show a toast from the web page */
    @JavascriptInterface
    public void showToast(String toast) {
        Toast.makeText(mContext, toast, Toast.LENGTH_SHORT).show();
    }

    @JavascriptInterface
    public void setLightStatusbarFg() {

        mWebView.post(new Runnable() {
            @Override
            public void run() {
                View decor = mWindow.getDecorView();
                decor.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR | View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR);
            }
        });
    }

    @JavascriptInterface
    public void setDarkStatusbarFg() {
        mWebView.post(new Runnable() {
            @Override
            public void run() {
                View decor = mWindow.getDecorView();
                decor.setSystemUiVisibility(0);
            }
        });
    }

    @JavascriptInterface
    public void  saveFile(final String filename, final String data) {

        mWebView.post(new Runnable() {
            @Override
            public void run() {

                String filePath = mWebView.getUrl().replaceFirst("file://", "");
                String filePathSuffix = filePath.substring(filePath.lastIndexOf('/') + 1).trim();
                String folderPath = filePath.replaceFirst(filePathSuffix, "");
                String baseName = filePathSuffix.replaceFirst(".html", "");
                String timeStamp = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss-SSS").format(new Date());

                InputStream in;
                OutputStream out;

                try {

                    String outputPath = String.valueOf(folderPath) + "/" + baseName + "-backup";
                    String inputFile = String.valueOf(filePath);
                    String outputFile = String.valueOf(folderPath) + "/" + baseName + "-backup/" + baseName;
                    //create output directory if it doesn't exist
                    File dir = new File(outputPath);
                    if (!dir.exists()) {
                        dir.mkdirs();
                    }

                    Long tsLong = System.currentTimeMillis() / 1000;
                    String ts = tsLong.toString();

                    in = new FileInputStream(inputFile);
                    out = new FileOutputStream(outputFile + "-backup-" + timeStamp + ".html");

                    byte[] buffer = new byte[1024];
                    int read;
                    while ((read = in.read(buffer)) != -1) {
                        out.write(buffer, 0, read);
                    }
                    in.close();
                    // write the output file
                    out.flush();
                    out.close();

                } catch (Exception e) {
                    Log.e(TAG, e.getMessage());
                }

                File f = new File(String.valueOf(filePath));

                if (!f.getParentFile().exists()) {
                    f.getParentFile().mkdirs();
                }
                try {
                    int one = (int) (((double) data.length()) * 0.01d);
                    int s = 0;
                    BufferedWriter w = new BufferedWriter(new FileWriter(f));
                    do {
                        int e = data.indexOf(10, s);
                        if (e == -1) {
                            e = data.length();
                        }
                        w.write(data, s, e - s);
                        w.newLine();
                        s = e + 1;
                        if (s > one) {
                            one += one;
                        }
                    } while (s < data.length());
                    w.flush();
                    w.close();
                } catch (IOException e2) {
                    e2.printStackTrace();
                }
            }
        });
    }

    //window.twi.updateSystemColors($tw.wiki.extractTiddlerDataItem($tw.wiki.getTiddlerText('$:/palette'),'page-background'));
    @JavascriptInterface
    public void updateSystemColors(final String colorString) {
        //Convert color string if it's of the form #aaa
        String newColor = colorString.replaceAll("\"", "");
        char[] colorStringArray = newColor.toCharArray();
        if (newColor.length() == 4 && colorStringArray[0] == '#') {
            newColor = "#" + colorStringArray[1] + colorStringArray[1] + colorStringArray[2] + colorStringArray[2] + colorStringArray[3] + colorStringArray[3];
        }
        UtilMethods.setBackgroundColors(mWebView,mWindow,colorString.replaceAll("\"", ""));
    }
}
