package com.tiddlywiki.atw;

import android.content.Context;
import android.content.DialogInterface;
import android.support.v7.app.AlertDialog;
import android.webkit.JsResult;
import android.webkit.WebChromeClient;
import android.webkit.WebView;

class AtwWebChromeClient extends WebChromeClient {

    private Context mContext;

    /** Instantiate the interface and set the context */
    AtwWebChromeClient (Context c) {
        mContext = c;
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
}
