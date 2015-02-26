/*
 * Copyright 2014.
 * Distributed under the terms of the GPLv3 License.
 *
 * Authors:
 *      Clemens Zeidler <czei002@aucklanduni.ac.nz>
 */
package nz.ac.auckland.lablet.misc;


import android.app.Activity;
import android.content.*;
import android.database.Cursor;
import android.net.Uri;
import android.os.ParcelFileDescriptor;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;


public class LabletDataProvider extends ContentProvider {
    private UriMatcher uriMatcher;
    public static final String AUTHORITY = "nz.ac.auckland.lablet.provider";

    static public Intent makeMailIntent(String subject, String text, List<String> attachments, String packageName,
                                        String className) {
        ArrayList<Uri> uriList = new ArrayList<>();
        for (String attachment : attachments)
            uriList.add(Uri.parse("content://" + LabletDataProvider.AUTHORITY + "/" + attachment));

        // start mail intent
        final Intent intent = new Intent(Intent.ACTION_SEND);
        if (packageName != null && className != null)
            intent.setClassName(packageName, className);
        intent.setType("text/html");
        intent.putExtra(Intent.EXTRA_SUBJECT, subject);
        intent.putExtra(Intent.EXTRA_TEXT, text);
        intent.putParcelableArrayListExtra(Intent.EXTRA_STREAM, uriList);
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);

        return intent;
    }

    static public void mailData(Activity activity, String subject, String text,
                                List<String> attachments) {
        final Intent intent = makeMailIntent(subject, text, attachments, null, null);

        activity.startActivity(Intent.createChooser(intent, "Send Email"));
    }

    @Override
    public boolean onCreate() {
        uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
        uriMatcher.addURI(AUTHORITY, "*", 1);
        return true;
    }

    static public File getProviderDir(Context context) {
        File dir = new File(context.getCacheDir(), "provider");
        if (!dir.exists())
            dir.mkdir();
        return dir;
    }

    @Override
    public ParcelFileDescriptor openFile(Uri uri, String mode) throws FileNotFoundException {
        switch (uriMatcher.match(uri)) {
            case 1:
                File file = new File(getProviderDir(getContext()), uri.getLastPathSegment());
                ParcelFileDescriptor pfd = ParcelFileDescriptor.open(file, ParcelFileDescriptor.MODE_READ_ONLY);
                return pfd;

            default:
                throw new FileNotFoundException("Unsupported uri: " + uri.toString());
        }
    }

    @Override
    public Cursor query(Uri uri, String[] strings, String s, String[] strings2, String s2) {
        return null;
    }

    @Override
    public String getType(Uri uri) {
        return null;
    }

    @Override
    public Uri insert(Uri uri, ContentValues contentValues) {
        return null;
    }

    @Override
    public int delete(Uri uri, String s, String[] strings) {
        return 0;
    }

    @Override
    public int update(Uri uri, ContentValues contentValues, String s, String[] strings) {
        return 0;
    }
}
