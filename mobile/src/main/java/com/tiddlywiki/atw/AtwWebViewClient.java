package com.tiddlywiki.atw;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.util.Log;
import android.view.Window;
import android.webkit.ValueCallback;
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
