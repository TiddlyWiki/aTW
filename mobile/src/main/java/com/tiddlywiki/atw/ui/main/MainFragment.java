package com.tiddlywiki.atw.ui.main;

import android.Manifest;
import android.app.Activity;
import android.app.ActivityManager;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.NavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.webkit.JsResult;
import android.webkit.ValueCallback;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.ImageView;
import android.widget.TextView;

import com.tiddlywiki.atw.R;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import static android.content.ContentValues.TAG;

public class MainFragment extends Fragment {

    private MainViewModel mViewModel;
    private WebView mWebView;
    private View mContentView;
    private boolean mVisible;
    private View mView;
    private Window mWindow;

    private static final int INPUT_FILE_REQUEST_CODE = 1;
    private static final int PICK_FILE_REQUEST_CODE = 2;
    private ValueCallback<Uri[]> mFilePathCallback;
    private String mCameraPhotoPath;

    public static MainFragment newInstance() {
        return new MainFragment(); }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // keep the fragment and all its data across screen rotation
        setRetainInstance(true);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        mView = inflater.inflate(R.layout.main_fragment, container, false);

        Context mContext = mView.getContext();
        mContentView = getActivity().findViewById(R.id.main);
        mWindow = getActivity().getWindow();

        mWebView = mView.findViewById(R.id.webview_main);
        WebSettings mWebSettings = mWebView.getSettings();
        //Enable Javascript
        mWebSettings.setJavaScriptEnabled(true);
        // Zoom and MultiTouch
        mWebSettings.setSupportZoom(true);
        mWebSettings.setBuiltInZoomControls(true);
        mWebSettings.setDisplayZoomControls(false);
        mWebSettings.setAppCacheEnabled(true);
        mWebSettings.setDomStorageEnabled(true);
        mWebSettings.setSupportMultipleWindows(true);
        mWebSettings.setAppCachePath(mContext.getApplicationContext().getCacheDir().getAbsolutePath());
        mWebSettings.setCacheMode(WebSettings.LOAD_DEFAULT);
        mWebSettings.setDatabaseEnabled(true);
        mWebSettings.setDomStorageEnabled(true);
        mWebSettings.setUseWideViewPort(true);
        mWebSettings.setLoadWithOverviewMode(true);
        mWebSettings.setJavaScriptCanOpenWindowsAutomatically(true);
        mWebSettings.setAllowFileAccessFromFileURLs(true);
        mWebSettings.setAllowUniversalAccessFromFileURLs(true);

        mVisible = true;

        //Set a WebViewClient up
        mWebView.setWebViewClient(new AtwWebViewClient(mContext, mWebView, getActivity().getWindow()));
        //Set a WebChromeClient up
        mWebView.setWebChromeClient(new AtwWebChromeClient());//AtwWebChromeClient
        //Add a JavascriptInterface that is accessible within the WebView: window.twi
        mWebView.addJavascriptInterface(new AtwWebAppInterface(mContext, mWebView, getActivity().getWindow()), "twi");

        //Determine the url that should be loaded in the webview

        String urlToLoad = "file:///storage/emulated/0/aTW/LandingPage/landing_page.html";
        Bundle b = getArguments();

        if (b != null) {
            urlToLoad = b.getString("urlToLoad");
        }

        int fileAccessPermission = ContextCompat.checkSelfPermission(mContext,
                Manifest.permission.WRITE_EXTERNAL_STORAGE);

        if (fileAccessPermission == PackageManager.PERMISSION_GRANTED) {

            mWebView.loadUrl(urlToLoad);
            //setTaskDescription(new ActivityManager.TaskDescription(urlToLoad));
        } else {
            mWebView.destroy();
        }

        return mView;
    }

    private void saveLandingPageAsset(String url, String name, String value) {

    }

    public Bitmap StringToBitMap(String encodedString){
        try{
            byte [] encodeByte=Base64.decode(encodedString,Base64.DEFAULT);
            Bitmap bitmap=BitmapFactory.decodeByteArray(encodeByte, 0, encodeByte.length);
            return bitmap;
        }catch(Exception e){
            e.getMessage();
            return null;
        }
    }

    @Override
    public void onHiddenChanged(boolean hidden) {
        super.onHiddenChanged(hidden);
        if (!hidden) {

            mWebView.evaluateJavascript("javascript:$tw.wiki.getTiddlerText('favicon.ico');",
                    new ValueCallback<String>() {
                        @Override
                        public void onReceiveValue(String s) {
                            saveLandingPageAsset(mWebView.getUrl(), "favicon", s);
                            if(s != null && !s.equals("") && !s.equals("null") && !s.equals("undefined")) {
                                try {
                                    Bitmap faviconBitmap = StringToBitMap(s);
                                    NavigationView navigationView = (NavigationView) mWindow.findViewById(R.id.nav_view);
                                    View headerView = navigationView.getHeaderView(0);
                                    ImageView navImage = (ImageView) headerView.findViewById(R.id.imageView);
                                    navImage.setImageBitmap(faviconBitmap);
                                } catch (Exception e) {
                                    Log.e(TAG, e.getMessage());
                                }
                            } else {
                                NavigationView navigationView = (NavigationView) mWindow.findViewById(R.id.nav_view);
                                View headerView = navigationView.getHeaderView(0);
                                ImageView navImage = (ImageView) headerView.findViewById(R.id.imageView);
                                navImage.setImageResource(R.drawable.ic_launcher);
                            }
                        }
                    });
            mWebView.evaluateJavascript("javascript:$tw.wiki.getTiddlerText('$:/SiteTitle');",
                    new ValueCallback<String>() {
                        @Override
                        public void onReceiveValue(String s) {
                            saveLandingPageAsset(mWebView.getUrl(), "sitetitle", s);
                            Activity activity = (Activity) getContext();
                            activity.setTaskDescription(new ActivityManager.TaskDescription(s));
                        }
                    });
            //set system system ui colors to wiki background
            //TODO: when background changes, set colors again // done on wiki-side
            mWebView.evaluateJavascript("javascript:$tw.androidConnector.getWikiColor('page-background');",//javascript:$tw.wiki.extractTiddlerDataItem($tw.wiki.getTiddlerText('$:/palette'),'page-background');",
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
            mWebView.evaluateJavascript("javascript:$tw.androidConnector.getWikiColorContrast('foreground','#ffffff');",//$tw.wiki.extractTiddlerDataItem($tw.wiki.getTiddlerText('$:/palette'),'foreground');",
                    new ValueCallback<String>() {
                        @Override
                        public void onReceiveValue(final String colorString) {
                            //Convert color string if it's of the form #aaa
                            String newColor = colorString.replaceAll("\"", "");
                            char[] colorStringArray = newColor.toCharArray();
                            if (newColor.length() == 4 && colorStringArray[0] == '#') {
                                newColor = "#" + colorStringArray[1] + colorStringArray[1] + colorStringArray[2] + colorStringArray[2] + colorStringArray[3] + colorStringArray[3];
                            }

                            if(newColor.equals("#ffffff")) {
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
            mWebView.evaluateJavascript("javascript:$tw.wiki.getTiddlerText('$:/SiteTitle');",//$tw.wiki.extractTiddlerDataItem($tw.wiki.getTiddlerText('$:/palette'),'foreground');",
                    new ValueCallback<String>() {
                        @Override
                        public void onReceiveValue(String sitetitle) {
                            NavigationView navigationView = (NavigationView) mWindow.findViewById(R.id.nav_view);
                            View headerView = navigationView.getHeaderView(0);
                            TextView siteTitle = (TextView) headerView.findViewById(R.id.siteTitle);
                            siteTitle.setText(sitetitle.substring(1,sitetitle.length() -1));
                        }
            });
            mWebView.evaluateJavascript("javascript:$tw.wiki.getTiddlerText('$:/SiteSubtitle');",//$tw.wiki.extractTiddlerDataItem($tw.wiki.getTiddlerText('$:/palette'),'foreground');",
                    new ValueCallback<String>() {
                        @Override
                        public void onReceiveValue(String sitesubtitle) {
                            NavigationView navigationView = (NavigationView) mWindow.findViewById(R.id.nav_view);
                            View headerView = navigationView.getHeaderView(0);
                            TextView siteSubTitle = (TextView) headerView.findViewById(R.id.textView);
                            siteSubTitle.setText(sitesubtitle.substring(1,sitesubtitle.length() - 1));
                        }
                    });
        }
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mViewModel = ViewModelProviders.of(this).get(MainViewModel.class);
        // TODO: Use the ViewModel
    }

    final class AtwWebChromeClient extends WebChromeClient {

        @Override
        public boolean onJsConfirm(WebView view, String url, String message, final JsResult result) {
            new AlertDialog.Builder(view.getContext())
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

        public Fragment getVisibleFragment(FragmentManager m){
            List<Fragment> fragments = m.getFragments();
            for(Fragment fragment : fragments){
                if(fragment != null && fragment.isVisible())
                    return fragment;
            }
            return null;
        }

        public void hideAllFragments(FragmentManager m, FragmentTransaction ft, String tag, Fragment f) {
            List<Fragment> fragments = m.getFragments();
            for(Fragment fragment : fragments){
                if((fragment.getArguments() == null | (fragment.getArguments() != null && fragment.getArguments().getString("urlToLoad") != f.getArguments().getString("urlToLoad"))) && fragment.getTag() != null && !fragment.getTag().equals(tag)) {
                    ft.hide(fragment);
                }
            }
        }

        @Override
        public boolean onCreateWindow(WebView view, boolean dialog, boolean userGesture, android.os.Message resultMsg)
        {
            WebView.HitTestResult result = view.getHitTestResult();
            String urlToLoad = result.getExtra();
            Fragment fragment = null;
            Class fragmentClass = MainFragment.class;

            try {
                fragment = (Fragment) fragmentClass.newInstance();
                Bundle loadBundle = new Bundle();
                loadBundle.putString("urlToLoad",urlToLoad);
                fragment.setArguments(loadBundle);
            } catch (Exception e) {
                e.printStackTrace();
            }

            FragmentManager fragmentManager = getActivity().getSupportFragmentManager();
            FragmentTransaction ft = fragmentManager.beginTransaction();
            hideAllFragments(fragmentManager,ft,urlToLoad,fragment);

            if(fragment != null && fragmentManager.findFragmentByTag(urlToLoad) == null) {
                Fragment visibleFragment = getVisibleFragment(fragmentManager);
                ft.add(R.id.fullscreen_content, fragment, urlToLoad).addToBackStack(urlToLoad).show(visibleFragment).commit();
            } else {
                Fragment visibleFragment = getVisibleFragment(fragmentManager);
                Fragment bringTopFragment = fragmentManager.findFragmentByTag(urlToLoad);
                if(visibleFragment != null && bringTopFragment != null) {
                    ft.hide(visibleFragment).show(bringTopFragment).commit();
                }
            }
            return true;
        }

        public boolean onShowFileChooser(WebView view, ValueCallback<Uri[]> filePath, WebChromeClient.FileChooserParams fileChooserParams) {
            // Double check that we don't have any existing callbacks
            if (mFilePathCallback != null) {
                mFilePathCallback.onReceiveValue(null);
            }
            mFilePathCallback = filePath;

            Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            if (takePictureIntent.resolveActivity(view.getContext().getPackageManager()) != null) {
                // Create the File where the photo should go
                File photoFile = null;
                try {
                    photoFile = createImageFile();
                    takePictureIntent.putExtra("PhotoPath", mCameraPhotoPath);
                } catch (IOException ex) {
                    // Error occurred while creating the File
                    Log.e(TAG, "Unable to create Image File", ex);
                }

                // Continue only if the File was successfully created
                if (photoFile != null) {
                    mCameraPhotoPath = "file:" + photoFile.getAbsolutePath();
                    takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT,
                            Uri.fromFile(photoFile));
                } else {
                    takePictureIntent = null;
                }
            }

            Intent contentSelectionIntent = new Intent(Intent.ACTION_GET_CONTENT);
            contentSelectionIntent.addCategory(Intent.CATEGORY_OPENABLE);
            contentSelectionIntent.setType("*/*");

            Intent[] intentArray;
            if (takePictureIntent != null) {
                intentArray = new Intent[]{takePictureIntent};
            } else {
                intentArray = new Intent[0];
            }

            Intent chooserIntent = new Intent(Intent.ACTION_CHOOSER);
            chooserIntent.putExtra(Intent.EXTRA_INTENT, contentSelectionIntent);
            chooserIntent.putExtra(Intent.EXTRA_TITLE, "File Chooser");
            chooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS, intentArray);

            if(mWebView.getUrl().equals("file:///storage/emulated/0/TW/LandingPage/landing_page.html")) {
                startActivityForResult(contentSelectionIntent,PICK_FILE_REQUEST_CODE);
            } else {
                startActivityForResult(chooserIntent, INPUT_FILE_REQUEST_CODE);
            }
            return true;
        }

        private File createImageFile() throws IOException {
            // Create an image file name
            String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
            String imageFileName = "JPEG_" + timeStamp + "_";
            File storageDir = Environment.getExternalStoragePublicDirectory(
                    Environment.DIRECTORY_PICTURES);
            File imageFile = File.createTempFile(
                    imageFileName,  /* prefix */
                    ".jpg",         /* suffix */
                    storageDir      /* directory */
            );
            return imageFile;
        }

/*        @Override
        public void onReceivedIcon(WebView view, Bitmap icon) {
            //handle favicon, put into landing-page favicons subfolder
            Drawable wikiIcon = new BitmapDrawable(getResources(), icon);
            getSupportActionBar().setIcon(wikiIcon);
        }

        @Override
        public void onReceivedTitle(WebView view, String title) {
            //TODO:handle site-title
            getWindow().setTitle(title);
        }*/

/*        @Override
        public void onShowCustomView(View view, WebChromeClient.CustomViewCallback callback) {
            //TODO:handle entering fullscreen
            mWebView.setVisibility(View.GONE);
            mWebView.setSystemUiVisibility(
                    View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                            // Set the content to appear under the system bars so that the
                            // content doesn't resize when the system bars hide and show.
                            | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                            | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                            | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                            // Hide the nav bar and status bar
                            | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                            | View.SYSTEM_UI_FLAG_FULLSCREEN);
            mWebView.addView(view);
            mWebView.setVisibility(View.VISIBLE);

            //mCustomView = view;
            //mWebView.setVisibility(View.VISIBLE);
            //mTargetView.setVisibility(View.VISIBLE);
            //mWebView.bringToFront();
        }

        @Override
        public void onHideCustomView() {
            //TODO:handle leaving fullscreen
        }*/
    }
}