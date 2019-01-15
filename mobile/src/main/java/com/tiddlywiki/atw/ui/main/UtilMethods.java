package com.tiddlywiki.atw.ui.main;

import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Build;
import android.support.design.widget.NavigationView;
import android.support.v4.widget.DrawerLayout;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.webkit.WebView;
import android.widget.TextView;

import com.tiddlywiki.atw.R;

import static android.content.ContentValues.TAG;

public class UtilMethods {

    public static void setBackgroundColors (WebView mWebView, Window w, final String color) {
        final Window mWindow = w;
        mWebView.post(new Runnable() {
            @Override
            public void run() {
                try {
                    int newColor = Color.parseColor(color);
                    mWindow.setStatusBarColor(newColor);
                    if (Build.VERSION.SDK_INT >= 26) {
                        mWindow.setNavigationBarColor(newColor);
                    }
                    DrawerLayout drawer = (DrawerLayout) mWindow.findViewById(R.id.drawer_layout);
                    NavigationView navigationView = (NavigationView) mWindow.findViewById(R.id.nav_view);
                    navigationView.setBackgroundColor(newColor);
                    drawer.setBackgroundColor(newColor);
                } catch (Exception e) {
                    Log.e(TAG, e.getMessage());
                }
            }
        });
    }

    public static void setForegroundColors (WebView mWebView, Window w, final String color) {
        final Window mWindow = w;
        mWebView.post(new Runnable() {
            @Override
            public void run() {
                try {
                    int newColor = Color.parseColor(color);
                    DrawerLayout drawer = (DrawerLayout) mWindow.findViewById(R.id.drawer_layout);
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
                    View headerView = navigationView.getHeaderView(0);
                    TextView navText = (TextView) headerView.findViewById(R.id.textView);
                    navText.setTextColor(new ColorStateList(state, color));
                } catch (Exception e) {
                    Log.e(TAG, e.getMessage());
                }
            }
        });
    }
}
