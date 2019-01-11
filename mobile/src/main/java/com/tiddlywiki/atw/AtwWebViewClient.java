package com.tiddlywiki.atw;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.util.Log;
import android.view.Window;
import android.webkit.ValueCallback;
import android.webkit.WebResourceError;
import android.webkit.WebResourceRequest;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import static android.content.ContentValues.TAG;

public class AtwWebViewClient extends WebViewClient {

    private Context mContext;
    private WebView mWebView;
    private Window mWindow;

    /** Instantiate the interface and set the context */
    AtwWebViewClient (Context c, WebView v,Window w) {
        mContext = c;
        mWebView = v;
        mWindow = w;
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

        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
        view.getContext().startActivity(intent);
        return true;
    }

    private void saveLandingPageAsset(String url, String name, String value) {

    }

    @Override
    public void onPageFinished(final WebView view, String url) {
        //extract favicon and siteTitle from wiki, for use in landing page
        mWebView.evaluateJavascript("javascript:$tw.wiki.getTiddlerText('favicon.ico');",
                new ValueCallback<String>() {
                    @Override
                    public void onReceiveValue(String s) {
                        saveLandingPageAsset(mWebView.getUrl(),"favicon",s);
                    }
                });
        mWebView.evaluateJavascript("javascript:$tw.wiki.getTiddlerText('$:/SiteTitle');",
                new ValueCallback<String>() {
                    @Override
                    public void onReceiveValue(String s) {
                        saveLandingPageAsset(mWebView.getUrl(),"sitetitle",s);
                    }
                });
        //set system system ui colors to wiki background
        //TODO: when background changes, set colors again
        mWebView.evaluateJavascript("javascript:$tw.wiki.extractTiddlerDataItem($tw.wiki.getTiddlerText('$:/palette'),'page-background');",
                new ValueCallback<String>() {
                    @Override
                    public void onReceiveValue(String colorString) {
                        //Convert color string if it's of the form #aaa
                        char[] colorStringArray = colorString.toCharArray();
                        if(colorString.length() == 4 && colorStringArray[0] == '#') {
                            colorString = "#" + colorStringArray[1] + colorStringArray[1] + colorStringArray[2] + colorStringArray[2] + colorStringArray[3] + colorStringArray[3];
                        }
                        try {
                            mWindow.setStatusBarColor(Color.parseColor(colorString.replaceAll("\"","")));
                            mWindow.setNavigationBarColor(Color.parseColor(colorString.replaceAll("\"","")));
                        } catch (Exception e) {
                            Log.e(TAG, e.getMessage());
                        }
                    }
                });
    }
}
