package com.tiddlywiki.atw.ui.main;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.support.design.widget.NavigationView;
import android.support.v4.widget.DrawerLayout;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.webkit.ValueCallback;
import android.webkit.WebResourceError;
import android.webkit.WebResourceRequest;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.tiddlywiki.atw.R;

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

        if (url.startsWith("tel:")) {
            Intent intent = new Intent(Intent.ACTION_DIAL, Uri.parse(url));
            view.getContext().startActivity(intent);
            return true;
        }

        if (url.startsWith("mailto:")) {
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
                        Activity activity = (Activity) mContext;
                        activity.setTaskDescription(new ActivityManager.TaskDescription(s));
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
                        mWebView.post(new Runnable() {
                            @Override
                            public void run() {
                                try {
                                    int newColor = Color.parseColor(colorString.replaceAll("\"", ""));
                                    mWindow.setStatusBarColor(newColor);
                                    mWindow.setNavigationBarColor(newColor);
                                    DrawerLayout drawer = (DrawerLayout) mWindow.findViewById(R.id.drawer_layout);
                                    NavigationView navigationView = (NavigationView) mWindow.findViewById(R.id.nav_view);
                                    navigationView.setBackgroundColor(newColor);
                                    //drawer.setScrimColor(newColor);
                                    //drawer.setBackgroundColor(newColor);
                                } catch (Exception e) {
                                    Log.e(TAG, e.getMessage());
                                }
                            }
                        });
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
                        mWebView.post(new Runnable() {
                            @Override
                            public void run() {

                                if (colorString.replaceAll("\"", "").equals("#ffffff")) {
                                    View decor = mWindow.getDecorView();
                                    decor.setSystemUiVisibility(0);
                                } else {
                                    View decor = mWindow.getDecorView();
                                    decor.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR | View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR);
                                }

                                try {
                                    int newColor = Color.parseColor(colorString.replaceAll("\"", ""));
                                    DrawerLayout drawer = (DrawerLayout) mWindow.findViewById(R.id.drawer_layout);
                                    //drawer.setOutlineAmbientShadowColor(newColor);
                                    //drawer.setOutlineSpotShadowColor(newColor);
                                    //drawer.setScrimColor(newColor);
                                    NavigationView navigationView = (NavigationView) mWindow.findViewById(R.id.nav_view);
                                    // FOR NAVIGATION VIEW ITEM TEXT COLOR
                                    int[][] state = new int[][]{
                                            new int[]{-android.R.attr.state_enabled}, // disabled
                                            new int[]{android.R.attr.state_enabled}, // enabled
                                            new int[]{-android.R.attr.state_checked}, // unchecked
                                            new int[]{android.R.attr.state_pressed}  // pressed

                                    };

                                    // FOR NAVIGATION VIEW ITEM ICON COLOR
                                    int[][] states = new int[][]{
                                            new int[]{-android.R.attr.state_enabled}, // disabled
                                            new int[]{android.R.attr.state_enabled}, // enabled
                                            new int[]{-android.R.attr.state_checked}, // unchecked
                                            new int[]{android.R.attr.state_pressed}  // pressed

                                    };

                                    int[] colors = new int[]{
                                            newColor,
                                            newColor,
                                            newColor,
                                            newColor
                                    };

                                    int[] color = new int[]{
                                            newColor,
                                            newColor,
                                            newColor,
                                            newColor
                                    };
                                    navigationView.setItemTextColor(new ColorStateList(state, color));
                                    navigationView.setItemIconTintList(new ColorStateList(states, colors));

                                    //drawer.setScrimColor(newColor);
                                    //drawer.setBackgroundColor(newColor);
                                } catch (Exception e) {
                                    Log.e(TAG, e.getMessage());
                                }
                            }
                        });
                    }
                });
    }
}
