package com.tiddlywiki.atw;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.webkit.WebView;
import android.webkit.WebViewClient;

public class AtwWebViewClient extends WebViewClient {

    private Context mContext;

    /** Instantiate the interface and set the context */
    AtwWebViewClient (Context c) {
        mContext = c;
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

    @Override
    public void onPageFinished(final WebView view, String url) {
        // do your stuff here
    }
}
