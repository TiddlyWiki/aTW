package com.tiddlywiki.atw;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.webkit.JsResult;
import android.webkit.ValueCallback;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;

import static android.content.ContentValues.TAG;

public class MainActivity extends AppCompatActivity {

    final Context mContext = this;
    private WebView mWebView;

    private static final int WRITE_EXTERNAL_STORAGE_REQUEST_CODE = 101;
    private static final int ACCESS_CAMERA_REQUEST_CODE = 102;
    private static final int INPUT_FILE_REQUEST_CODE = 1;
    private static final int PICK_FILE_REQUEST_CODE = 2;
    private ValueCallback<Uri[]> mFilePathCallback;
    private String mCameraPhotoPath;

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
                    mWebView.loadUrl("file:///storage/emulated/0/aTW/LandingPage/landing_page.html");
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

            String outputFile = path + File.separator + name;

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
        File path = new File("/storage/emulated/0" + File.separator + "aTW" + File.separator + "LandingPage");
        copyRawFile(String.valueOf(path), "landing_page.html", "landing_page");
        copyRawFile(String.valueOf(path), "missing_favicon.png", "missing_favicon");
    }

    private void checkLandingPageSetup() {
        File extStoragePath = Environment.getExternalStorageDirectory();
        String path = String.valueOf(extStoragePath) + File.separator + "aTW" + File.separator + "LandingPage";
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
        if(!favTestfile.exists()) {
            favTestfile.mkdirs();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //Check for permissions and ask if necessary
        checkPermissions();

        //Check if App-Folder in home directory exists, if not, set it up
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
        mWebSettings.setJavaScriptCanOpenWindowsAutomatically(true);
        mWebSettings.setAllowFileAccessFromFileURLs(true);
        mWebSettings.setAllowUniversalAccessFromFileURLs(true);

        //Set a WebViewClient up
        mWebView.setWebViewClient(new AtwWebViewClient(this, mWebView, getWindow()));
        //Set a WebChromeClient up
        mWebView.setWebChromeClient(new AtwWebChromeClient());
        //Add a JavascriptInterface that is accessible within the WebView: window.twi
        mWebView.addJavascriptInterface(new AtwWebAppInterface(this, mWebView, getWindow()), "twi");

        //Determine the url that should be loaded in the webview
        Bundle b = getIntent().getExtras();
        String urlToLoad = null; // or other values
        if (b != null) {
            urlToLoad = b.getString("urlToLoad");
        }
        if(urlToLoad == null) {
            urlToLoad = "file:///storage/emulated/0/aTW/LandingPage/landing_page.html";
        }

        int fileAccessPermission = ContextCompat.checkSelfPermission(mContext,
                Manifest.permission.WRITE_EXTERNAL_STORAGE);

        if (fileAccessPermission == PackageManager.PERMISSION_GRANTED) {
            mWebView.loadUrl(urlToLoad);
        } else {
            mWebView.destroy();
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {

    }

    final class AtwWebChromeClient extends WebChromeClient {

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

        public boolean onShowFileChooser(WebView view, ValueCallback<Uri[]> filePath, WebChromeClient.FileChooserParams fileChooserParams) {
            // Double check that we don't have any existing callbacks
            if (mFilePathCallback != null) {
                mFilePathCallback.onReceiveValue(null);
            }
            mFilePathCallback = filePath;

            Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
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
    }
}
