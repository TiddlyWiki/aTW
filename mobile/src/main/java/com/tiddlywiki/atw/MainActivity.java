package com.tiddlywiki.atw;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.webkit.WebSettings;
import android.webkit.WebView;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;

public class MainActivity extends AppCompatActivity {

    final Context mContext = this;
    private WebView mWebView;

    private static final int WRITE_EXTERNAL_STORAGE_REQUEST_CODE = 101;
    private static final int ACCESS_CAMERA_REQUEST_CODE = 102;

    protected void makeWritePermissionRequest() {
        ActivityCompat.requestPermissions(this,
                new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                WRITE_EXTERNAL_STORAGE_REQUEST_CODE);
    }

    protected void makeCameraPermissionRequest() {
        ActivityCompat.requestPermissions(this,
                new String[]{Manifest.permission.CAMERA},
                ACCESS_CAMERA_REQUEST_CODE);
    }

    private void checkPermissions() {
        int fileAccessPermission = ContextCompat.checkSelfPermission(mContext,
                Manifest.permission.WRITE_EXTERNAL_STORAGE);

        int cameraAccessPermission = ContextCompat.checkSelfPermission(mContext,
                Manifest.permission.CAMERA);

        if (fileAccessPermission != PackageManager.PERMISSION_GRANTED) {
            makeWritePermissionRequest();
        }

        if (cameraAccessPermission != PackageManager.PERMISSION_GRANTED) {
            makeCameraPermissionRequest();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case WRITE_EXTERNAL_STORAGE_REQUEST_CODE: {

                if (grantResults.length == 0
                        || grantResults[0] !=
                        PackageManager.PERMISSION_GRANTED) {

                } else if (ContextCompat.checkSelfPermission(mContext,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED){
                    mWebView.loadUrl("file:///storage/emulated/0/TW/LandingPage/landing_page.html");
                    int cameraAccessPermission = ContextCompat.checkSelfPermission(mContext,
                            Manifest.permission.CAMERA);
                    if (cameraAccessPermission != PackageManager.PERMISSION_GRANTED) {
                        makeCameraPermissionRequest();
                    }
                }
            }
        }
    }

    protected void copyRawFile(String path, String name, String identifier) {
        InputStream in;
        OutputStream out;
        try {

            String outputFile = String.valueOf(path) + File.separator + name;

            in = getResources().openRawResource(
                    getResources().getIdentifier("raw/" + identifier,
                            "raw", getPackageName()));

            out = new FileOutputStream(outputFile);

            byte[] buffer = new byte[1024];
            int read;
            while ((read = in.read(buffer)) != -1) {
                out.write(buffer, 0, read);
            }
            in.close();

            // write the output file (You have now copied the file)
            out.flush();
            out.close();

        } catch (Exception e) {
            Log.e("tag", e.getMessage());
        }
    }

    protected void makeLandingPage() {
        File path = new File("/storage/emulated/0" + File.separator + R.string.app_name + File.separator + "LandingPage");
        copyRawFile(String.valueOf(path), "landing_page.html", "landing_page");
        copyRawFile(String.valueOf(path), "missing_favicon.png", "missing_favicon");
    }

    private void checkLandingPageSetup() {
        File extStoragePath = Environment.getExternalStorageDirectory();
        String path = String.valueOf(extStoragePath) + File.separator + R.string.app_name + File.separator + "LandingPage";
        File testFile = new File(path);
        if(!testFile.getParentFile().exists()) {
            testFile.getParentFile().mkdirs();
        }

        if (!testFile.exists()) {
            testFile.mkdirs();
            makeLandingPage();
        }

        //A folder used to store favicons from wikies
        path = String.valueOf(path) + File.separator + "favicons";
        File favTestfile = new File(path);
        if(!favTestfile.getParentFile().exists()) {
            favTestfile.getParentFile().mkdirs();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //Check for permissions and ask if necessary
        checkPermissions();

        //TODO:Check if App-Folder in home directory exists, if not, set it up
        checkLandingPageSetup();

        //Set the WebView up
        mWebView = (WebView) findViewById(R.id.atw_main_layout);
        //Documentation at https://developer.android.com/reference/android/webkit/WebSettings
        //Worth having a look what makes sense to enable/disable
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
        mWebSettings.setAppCachePath(getApplicationContext().getCacheDir().getAbsolutePath());
        mWebSettings.setCacheMode(WebSettings.LOAD_DEFAULT);
        mWebSettings.setDatabaseEnabled(true);
        mWebSettings.setDomStorageEnabled(true);
        mWebSettings.setUseWideViewPort(true);
        mWebSettings.setLoadWithOverviewMode(true);

        //Set a WebViewClient up
        mWebView.setWebViewClient(new AtwWebViewClient(this));
        //Set a WebChromeClient up
        mWebView.setWebChromeClient(new AtwWebChromeClient(this));
        //Add a JavascriptInterface that is accessible within the WebView: window.twi
        mWebView.addJavascriptInterface(new AtwWebAppInterface(this,mWebView), "twi");

        int fileAccessPermission = ContextCompat.checkSelfPermission(mContext,
                Manifest.permission.WRITE_EXTERNAL_STORAGE);

        if (fileAccessPermission == PackageManager.PERMISSION_GRANTED) {
            mWebView.loadUrl("file:///storage/emulated/0/" + R.string.app_name + "/LandingPage/landing_page.html");
        } else {
            mWebView.destroy();
        }
    }
}
