package com.tiddlywiki.atw.ui.main;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.support.design.widget.NavigationView;
import android.view.View;
import android.view.Window;
import android.webkit.ValueCallback;
import android.webkit.WebResourceError;
import android.webkit.WebResourceRequest;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.TextView;

import com.tiddlywiki.atw.R;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

public class AtwWebViewClient extends WebViewClient {

    /* Downloads
    Intent downloadIntent = new Intent(this, DownloadService.class);
downloadIntent.setData(Uri.parse(fileUrl));
startService(downloadIntent);
     */

    private Context mContext;
    private WebView mWebView;
    private Window mWindow;

    /** Instantiate the interface and set the context */
    AtwWebViewClient (Context c, WebView v,Window w) {
        mContext = c;
        mWebView = v;
        mWindow = w;
    }

    @Override
    public void onPageStarted(WebView view, String url, Bitmap favicon) {
        // TODO Auto-generated method stub
        super.onPageStarted(view, url, favicon);
    }

    //if for whatever reason the specified file cannot load, show something
    //Got this from https://stackoverflow.com/questions/32769505/webviewclient-onreceivederror-deprecated-new-version-does-not-detect-all-errors/33419123#33419123
    @SuppressWarnings("deprecation")
    @Override
    public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
        // Handle the error
        if(description.equals("net::ERR_FILE_NOT_FOUND") && failingUrl.equals(view.getUrl())) {
            mWebView.loadUrl("file:///android_res/raw/page_not_found.html");
        }
    }

    @TargetApi(Build.VERSION_CODES.M)
    @Override
    public void onReceivedError(WebView view, WebResourceRequest req, WebResourceError rerr) {
        super.onReceivedError(view, req, rerr);
        onReceivedError(view, rerr.getErrorCode(), rerr.getDescription().toString(), req.getUrl().toString());
    }

    @Override
    public boolean shouldOverrideUrlLoading(WebView view, String url) {
        if(Uri.parse(url).getHost().length() == 0) {
            return false;
        }

        if (url != null && url.startsWith("tel:")) {
            Intent intent = new Intent(Intent.ACTION_DIAL, Uri.parse(url));
            view.getContext().startActivity(intent);
            return true;
        }

        if (url != null && url.startsWith("mailto:")) {
            Intent intent = new Intent(Intent.ACTION_SENDTO, Uri.parse(url));
            view.getContext().startActivity(intent);
            return true;
        }
        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
        view.getContext().startActivity(intent);
        return true;
    }

    private void saveLandingPageAsset(String url, String name, String value) {

    }

    @Override
    public void onPageFinished(final WebView view, String url) {
        //Get the text file
        InputStream in = mContext.getResources().openRawResource(
                mContext.getResources().getIdentifier("raw/android_connector",
                        "raw", mContext.getPackageName()));

        StringBuilder text = new StringBuilder();

        try {
            BufferedReader br = new BufferedReader(new InputStreamReader(in));
            String line;

            while ((line = br.readLine()) != null) {
                text.append(line);
                text.append('\n');
            }
            br.close();
        }
        catch (IOException e) {
            e.printStackTrace();
        }

        //IMPORTANT: this loads the wiki-side android-communication logic!
        mWebView.evaluateJavascript("javascript:var AndroidConnector = " + text.toString() + "; $tw.androidConnector = new AndroidConnector;",null);

        mWebView.evaluateJavascript("javascript:$tw.wiki.getTiddlerText('$:/SiteTitle');",//$tw.wiki.extractTiddlerDataItem($tw.wiki.getTiddlerText('$:/palette'),'foreground');",
                new ValueCallback<String>() {
                    @Override
                    public void onReceiveValue(String sitetitle) {
                        NavigationView navigationView = (NavigationView) mWindow.findViewById(R.id.nav_view);
                        View headerView = navigationView.getHeaderView(0);
                        TextView siteTitle = (TextView) headerView.findViewById(R.id.siteTitle);
                        if(sitetitle != null && !sitetitle.equals("null")) {
                            siteTitle.setText(sitetitle.substring(1, sitetitle.length() - 1));
                        } else {
                            siteTitle.setText("aTW");
                        }
                        saveLandingPageAsset(mWebView.getUrl(),"sitetitle",sitetitle);
                        Activity activity = (Activity) mContext;
                        activity.setTaskDescription(new ActivityManager.TaskDescription(sitetitle.substring(1, sitetitle.length() - 1)));
                    }
                });
        mWebView.evaluateJavascript("javascript:$tw.wiki.getTiddlerText('$:/SiteSubtitle');",//$tw.wiki.extractTiddlerDataItem($tw.wiki.getTiddlerText('$:/palette'),'foreground');",
                new ValueCallback<String>() {
                    @Override
                    public void onReceiveValue(String sitesubtitle) {
                        NavigationView navigationView = (NavigationView) mWindow.findViewById(R.id.nav_view);
                        View headerView = navigationView.getHeaderView(0);
                        TextView siteSubTitle = (TextView) headerView.findViewById(R.id.textView);
                        if(sitesubtitle != null && !sitesubtitle.equals("null")) {
                            siteSubTitle.setText(sitesubtitle.substring(1,sitesubtitle.length() - 1));
                        } else {
                            siteSubTitle.setText("A simple Android app for TiddlyWiki");
                        }
                    }
                });
        //extract favicon and siteTitle from wiki, for use in landing page
        mWebView.evaluateJavascript("javascript:$tw.wiki.getTiddlerText('favicon.ico');",
                new ValueCallback<String>() {
                    @Override
                    public void onReceiveValue(String s) {
                        saveLandingPageAsset(mWebView.getUrl(),"favicon",s);
                    }
                });
        //set system system ui colors to wiki background
        //TODO: when background changes, set colors again // done on wiki-side
        mWebView.evaluateJavascript("javascript:$tw.androidConnector.getWikiColor('page-background');",
                new ValueCallback<String>() {
                    @Override
                    public void onReceiveValue(final String colorString) {
                        //Convert color string if it's of the form #aaa
                        String newColor = colorString.replaceAll("\"", "");
                        char[] colorStringArray = newColor.toCharArray();
                        if (newColor.length() == 4 && colorStringArray[0] == '#') {
                            newColor = "#" + colorStringArray[1] + colorStringArray[1] + colorStringArray[2] + colorStringArray[2] + colorStringArray[3] + colorStringArray[3];
                        }

                        UtilMethods.setBackgroundColors(mWebView,mWindow,colorString.replaceAll("\"", ""));
                    }
                });
        mWebView.evaluateJavascript("javascript:$tw.androidConnector.getWikiColorContrast('foreground','#ffffff');",
                new ValueCallback<String>() {
                    @Override
                    public void onReceiveValue(final String colorString) {
                        //Convert color string if it's of the form #aaa
                        String newColor = colorString.replaceAll("\"", "");
                        char[] colorStringArray = newColor.toCharArray();
                        if (newColor.length() == 4 && colorStringArray[0] == '#') {
                            newColor = "#" + colorStringArray[1] + colorStringArray[1] + colorStringArray[2] + colorStringArray[2] + colorStringArray[3] + colorStringArray[3];
                        }

                        if (colorString.replaceAll("\"", "").equals("#ffffff")) {
                            View decor = mWindow.getDecorView();
                            decor.setSystemUiVisibility(0);
                        } else {
                            View decor = mWindow.getDecorView();
                            if (Build.VERSION.SDK_INT >= 26) {
                                decor.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR | View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR);
                            } else {
                                decor.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
                            }
                        }

                        UtilMethods.setForegroundColors(mWebView,mWindow,colorString.replaceAll("\"", ""));
                    }
                });
    }
}
