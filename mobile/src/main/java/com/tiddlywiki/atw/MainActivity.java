package com.tiddlywiki.atw;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.support.design.widget.NavigationView;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;

import com.tiddlywiki.atw.ui.main.MainFragment;


public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    final Context mContext = this;

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
                    //mWebView.loadUrl("file:///storage/emulated/0/aTW/LandingPage/landing_page.html");
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

    protected void makeBackstagePage() {
        File path = new File("/storage/emulated/0" + File.separator + "aTW" + File.separator + "LandingPage" + File.separator + "Backstage");
        copyRawFile(String.valueOf(path), "page_not_found.html", "page_not_found");
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

        path = String.valueOf(extStoragePath) + File.separator + "aTW" + File.separator + "LandingPage" + File.separator + "Backstage";
        testFile = new File(path);
        if(!testFile.getParentFile().exists()) {
            testFile.getParentFile().mkdirs();
        }

        if (!testFile.exists()) {
            testFile.mkdirs();
            makeBackstagePage();
        }

        //A folder used to store favicons from wikies
        path = String.valueOf(path) + File.separator + "favicons";
        File favTestfile = new File(path);
        if(!favTestfile.exists()) {
            favTestfile.mkdirs();
        }
    }























    /**
     * Whether or not the system UI should be auto-hidden after
     * {@link #AUTO_HIDE_DELAY_MILLIS} milliseconds.
     */
    private static final boolean AUTO_HIDE = false;

    /**
     * If {@link #AUTO_HIDE} is set, the number of milliseconds to wait after
     * user interaction before hiding the system UI.
     */
    private static final int AUTO_HIDE_DELAY_MILLIS = 3000;

    /**
     * Some older devices needs a small delay between UI widget updates
     * and a change of the status and navigation bar.
     */
    private static final int UI_ANIMATION_DELAY = 300;
    private final Handler mHideHandler = new Handler();
    private View mContentView;

    private final Runnable mHidePart2Runnable = new Runnable() {
        @SuppressLint("InlinedApi")
        @Override
        public void run() {
            // Delayed removal of status and navigation bar

            // Note that some of these constants are new as of API 16 (Jelly Bean)
            // and API 19 (KitKat). It is safe to use them, as they are inlined
            // at compile-time and do nothing on earlier devices.
            mContentView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LOW_PROFILE
                    | View.SYSTEM_UI_FLAG_FULLSCREEN
                    | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                    | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                    | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                    | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION);
        }
    };

    //private View mControlsView;
    private final Runnable mShowPart2Runnable = new Runnable() {
        @Override
        public void run() {
            // Delayed display of UI elements
            ActionBar actionBar = getSupportActionBar();
            if (actionBar != null) {
                actionBar.show();
            }
            //mControlsView.setVisibility(View.VISIBLE);
        }
    };

    private boolean mVisible;
    private final Runnable mHideRunnable = new Runnable() {
        @Override
        public void run() {
            hide();
        }
    };
    /**
     * Touch listener to use for in-layout UI controls to delay hiding the
     * system UI. This is to prevent the jarring behavior of controls going away
     * while interacting with activity UI.
     */
    private final View.OnTouchListener mDelayHideTouchListener = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View view, MotionEvent motionEvent) {
            if (AUTO_HIDE) {
                delayedHide(AUTO_HIDE_DELAY_MILLIS);
            }
            return false;
        }
    };

    private void toggle() {
        if (mVisible) {
            hide();
        } else {
            show();
        }
    }

    private void hide() {
        // Hide UI first
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.hide();
        }
        //mControlsView.setVisibility(View.GONE);
        mVisible = false;

        // Schedule a runnable to remove the status and navigation bar after a delay
        mHideHandler.removeCallbacks(mShowPart2Runnable);
        mHideHandler.postDelayed(mHidePart2Runnable, UI_ANIMATION_DELAY);
    }

    @SuppressLint("InlinedApi")
    private void show() {
        // Show the system bar
        mContentView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION);
        mVisible = true;

        // Schedule a runnable to display UI elements after a delay
        mHideHandler.removeCallbacks(mHidePart2Runnable);
        mHideHandler.postDelayed(mShowPart2Runnable, UI_ANIMATION_DELAY);
    }

    /**
     * Schedules a call to hide() in delay milliseconds, canceling any
     * previously scheduled calls.
     */
    private void delayedHide(int delayMillis) {
        mHideHandler.removeCallbacks(mHideRunnable);
        mHideHandler.postDelayed(mHideRunnable, delayMillis);
    }

















    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //Check for permissions and ask if necessary
        checkPermissions();

        //Check if App-Folder in home directory exists, if not, set it up
        checkLandingPageSetup();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.getMenu().getItem(0).setChecked(true);
        navigationView.setNavigationItemSelectedListener(this);

        int fileAccessPermission = ContextCompat.checkSelfPermission(mContext,
                Manifest.permission.WRITE_EXTERNAL_STORAGE);

        if (fileAccessPermission == PackageManager.PERMISSION_GRANTED) {

            if (savedInstanceState == null) {
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.fullscreen_content, MainFragment.newInstance(),"file:///storage/emulated/0/aTW/LandingPage/landing_page.html")
                        .commitNow();
            }

        }
    }

    int leave = 0;

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
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

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        Fragment fragment = null;
        Class fragmentClass = MainFragment.class;
        String urlToLoad = "";

        if (id == R.id.landing_page_fragment) {
            // Handle the camera action
            urlToLoad = "file:///storage/emulated/0/aTW/LandingPage/landing_page.html";
        } else if (id == R.id.backstage_fragment) {
            urlToLoad = "file:///storage/emulated/0/aTW/LandingPage/BackStage/backstage.html";
        }

        try {
            fragment = (Fragment) fragmentClass.newInstance();
            Bundle loadBundle = new Bundle();
            loadBundle.putString("urlToLoad",urlToLoad);
            fragment.setArguments(loadBundle);
        } catch (Exception e) {
            e.printStackTrace();
        }

        FragmentManager fragmentManager = getSupportFragmentManager();
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

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }
}

