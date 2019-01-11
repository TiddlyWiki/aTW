package com.tiddlywiki.atw;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Window;
import android.webkit.JsResult;
import android.webkit.WebChromeClient;
import android.webkit.WebView;

import static android.content.ContentValues.TAG;

class AtwWebChromeClient extends WebChromeClient {

    private Context mContext;
    private Window mWindow;

    /** Instantiate the interface and set the context */
    AtwWebChromeClient (Context c, Window w) {
        mContext = c;
        mWindow = w;
    }

    @Override
    public boolean onJsConfirm(WebView view, String url, String message, final JsResult result) {
        new AlertDialog.Builder(mContext)
                .setTitle(R.string.tiddly_wiki)
                .setIcon(R.drawable.ic_launcher)
                .setMessage(message)
                .setPositiveButton(android.R.string.ok,
                        new DialogInterface.OnClickListener()
                        {
                            public void onClick(DialogInterface dialog, int which)
                            {
                                result.confirm();
                            }
                        })
                .setNegativeButton(android.R.string.cancel,
                        new DialogInterface.OnClickListener()
                        {
                            public void onClick(DialogInterface dialog, int which)
                            {
                                result.cancel();
                            }
                        })
                .create()
                .show();
        return true;
    }

    @Override
    public boolean onCreateWindow(WebView view, boolean dialog, boolean userGesture, android.os.Message resultMsg)
    {
        WebView.HitTestResult result = view.getHitTestResult();
        String data = result.getExtra();
        Intent newActivity = new Intent(mContext, MainActivity.class);
        Bundle newBundle = new Bundle();
        newBundle.putString("urlToLoad",data);
        newActivity.putExtras(newBundle);
        mContext.startActivity(newActivity);
        return true;
    }
}
