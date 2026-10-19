package com.htbb.pokemon;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.net.Uri;
import android.os.ParcelFileDescriptor;
import android.provider.OpenableColumns;
import java.io.File;
import java.io.FileNotFoundException;

/**
 * FileProvider TỐI GIẢN (0.9.7, không kéo thư viện androidx): phát đúng MỘT loại tệp - APK bản mới đã tải + kiểm SHA-256
 * trong cache/cap_nhat/ - cho trình cài Android qua content://&lt;gói&gt;.capnhat/apk/&lt;tên&gt; (Android 7+ cấm file://).
 * Chỉ đọc, không xuất (exported=false), quyền đọc cấp theo từng Intent (grantUriPermissions). Tên tệp lạ / thoát thư mục
 * -&gt; FileNotFoundException.
 */
public class TepCapNhat extends ContentProvider {
    static final String THU_MUC = "cap_nhat";

    static String quyen(Context c) {
        return c.getPackageName() + ".capnhat";
    }

    static Uri uri(Context c, File tep) {
        return new Uri.Builder().scheme("content").authority(quyen(c)).appendPath("apk").appendPath(tep.getName()).build();
    }

    File tep(Uri uri) throws FileNotFoundException {
        Context c = getContext();
        java.util.List<String> p = uri.getPathSegments();
        if (c == null || p.size() != 2 || !"apk".equals(p.get(0))) {
            throw new FileNotFoundException(String.valueOf(uri));
        }
        String ten = p.get(1);
        if (!ten.matches("[A-Za-z0-9._-]+\\.apk") || ten.startsWith(".")) {
            throw new FileNotFoundException(ten);
        }
        File f = new File(new File(c.getCacheDir(), THU_MUC), ten);
        if (!f.isFile()) {
            throw new FileNotFoundException(ten);
        }
        return f;
    }

    @Override
    public boolean onCreate() {
        return true;
    }

    @Override
    public ParcelFileDescriptor openFile(Uri uri, String mode) throws FileNotFoundException {
        if (mode == null || !mode.startsWith("r") || mode.contains("w")) {
            throw new FileNotFoundException("chi doc");
        }
        return ParcelFileDescriptor.open(tep(uri), ParcelFileDescriptor.MODE_READ_ONLY);
    }

    @Override
    public String getType(Uri uri) {
        return "application/vnd.android.package-archive";
    }

    @Override
    public Cursor query(Uri uri, String[] cot, String sel, String[] selArgs, String sort) {
        File f;
        try {
            f = tep(uri);
        } catch (FileNotFoundException e) {
            return null;
        }
        MatrixCursor m = new MatrixCursor(new String[]{OpenableColumns.DISPLAY_NAME, OpenableColumns.SIZE});
        m.addRow(new Object[]{f.getName(), f.length()});
        return m;
    }

    @Override
    public Uri insert(Uri uri, ContentValues v) {
        throw new UnsupportedOperationException();
    }

    @Override
    public int delete(Uri uri, String sel, String[] a) {
        throw new UnsupportedOperationException();
    }

    @Override
    public int update(Uri uri, ContentValues v, String sel, String[] a) {
        throw new UnsupportedOperationException();
    }
}
