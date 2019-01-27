package com.tiddlywiki.atw.ui.main;

import android.content.ContentUris;
import android.content.Context;
import android.content.res.ColorStateList;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.support.design.widget.NavigationView;
import android.support.v4.content.ContextCompat;
import android.support.v4.graphics.drawable.DrawableCompat;
import android.support.v4.widget.DrawerLayout;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.webkit.WebView;
import android.widget.ImageView;
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
                    View decor = mWindow.getDecorView();
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
        final Context mContext = mWebView.getContext();
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
                    TextView titleText = (TextView) headerView.findViewById(R.id.siteTitle);
                    titleText.setTextColor(new ColorStateList(state, color));
                    ImageView navImage = (ImageView) headerView.findViewById(R.id.imageView);
                    Drawable currentImage = navImage.getDrawable();
                    if(!(currentImage instanceof BitmapDrawable)) {
                        DrawableCompat.setTint(navImage.getDrawable(), newColor);
                    }
                    navigationView.refreshDrawableState();
                } catch (Exception e) {
                    Log.e(TAG, e.getMessage());
                }
            }
        });
    }

    public static String getPath(final Context context, final Uri uri) {

        final boolean isKitKat = Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT;

        // DocumentProvider
        if (isKitKat && DocumentsContract.isDocumentUri(context, uri)) {
            // ExternalStorageProvider
            if (isExternalStorageDocument(uri)) {
                final String docId = DocumentsContract.getDocumentId(uri);
                final String[] split = docId.split(":");
                final String type = split[0];

                if ("primary".equalsIgnoreCase(type)) {
                    return Environment.getExternalStorageDirectory() + "/" + split[1];
                }

                // TODO handle non-primary volumes
            }
            // DownloadsProvider
            else if (isDownloadsDocument(uri)) {

                final String id = DocumentsContract.getDocumentId(uri);
                final Uri contentUri = ContentUris.withAppendedId(
                        Uri.parse("content://downloads/public_downloads"), Long.valueOf(id));

                return getDataColumn(context, contentUri, null, null);
            }
            // MediaProvider
            else if (isMediaDocument(uri)) {
                final String docId = DocumentsContract.getDocumentId(uri);
                final String[] split = docId.split(":");
                final String type = split[0];

                Uri contentUri = null;
                if ("image".equals(type)) {
                    contentUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
                } else if ("video".equals(type)) {
                    contentUri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
                } else if ("audio".equals(type)) {
                    contentUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
                }

                final String selection = "_id=?";
                final String[] selectionArgs = new String[] {
                        split[1]
                };

                return getDataColumn(context, contentUri, selection, selectionArgs);
            }
        }
        // MediaStore (and general)
        else if ("content".equalsIgnoreCase(uri.getScheme())) {
            return getDataColumn(context, uri, null, null);
        }
        // File
        else if ("file".equalsIgnoreCase(uri.getScheme())) {
            return uri.getPath();
        }

        return null;
    }

    /**
     * Get the value of the data column for this Uri. This is useful for
     * MediaStore Uris, and other file-based ContentProviders.
     *
     * @param context The context.
     * @param uri The Uri to query.
     * @param selection (Optional) Filter used in the query.
     * @param selectionArgs (Optional) Selection arguments used in the query.
     * @return The value of the _data column, which is typically a file path.
     */
    public static String getDataColumn(Context context, Uri uri, String selection,
                                       String[] selectionArgs) {

        Cursor cursor = null;
        final String column = "_data";
        final String[] projection = {
                column
        };

        try {
            cursor = context.getContentResolver().query(uri, projection, selection, selectionArgs,
                    null);
            if (cursor != null && cursor.moveToFirst()) {
                final int column_index = cursor.getColumnIndexOrThrow(column);
                return cursor.getString(column_index);
            }
        } finally {
            if (cursor != null)
                cursor.close();
        }
        return null;
    }


    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is ExternalStorageProvider.
     */
    public static boolean isExternalStorageDocument(Uri uri) {
        return "com.android.externalstorage.documents".equals(uri.getAuthority());
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is DownloadsProvider.
     */
    public static boolean isDownloadsDocument(Uri uri) {
        return "com.android.providers.downloads.documents".equals(uri.getAuthority());
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is MediaProvider.
     */
    public static boolean isMediaDocument(Uri uri) {
        return "com.android.providers.media.documents".equals(uri.getAuthority());
    }
}
