package com.htbb.pokemon;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.provider.Settings;
import htbb.capnhat.CapNhatChung;
import java.io.File;
import java.io.IOException;
import javax.microedition.lcdui.NenAndroid;

/**
 * Bộ cập nhật bản APK (0.9.7, game.pokemon.CapNhat tìm theo tên lớp): tải APK bản mới NGAY TRONG GAME (thanh tiến độ,
 * kiểm SHA-256 - htbb.capnhat.TaiVe) vào cache riêng của app (cache/cap_nhat/), rồi mở trình cài của Android
 * (ACTION_VIEW + content:// của {@link TepCapNhat}, quyền REQUEST_INSTALL_PACKAGES). Android kiểm chữ ký: APK ký cùng
 * khoá với bản đang cài thì cài đè, GIỮ dữ liệu (cài đặt, tài khoản đã nhớ).
 *
 * Chưa cho phép "Cài ứng dụng không rõ nguồn" cho HTBB: mở trang cài đặt đó; người chơi bật rồi quay lại bấm "Cài lại"
 * (tệp đã tải, đã kiểm - không tải lại). Không có hoạt động / không mở được trình cài -> lỗi, hộp CapNhat cho "Mở trang
 * tải" (trình duyệt).
 */
public class CapNhatApk extends CapNhatChung {

    static Context ctx() {
        Context c = NenAndroid.ungDung;
        if (c == null && NenAndroid.hoatDong != null) {
            c = NenAndroid.hoatDong.getApplicationContext();
        }
        return c;
    }

    protected boolean coTheCai(String url) {
        String u = url == null ? "" : url.toLowerCase();
        int q = u.indexOf('?');
        if (q >= 0) {
            u = u.substring(0, q);
        }
        return u.endsWith(".apk") && ctx() != null;
    }

    protected File noiTai(String url, String phienBan) throws IOException {
        Context c = ctx();
        if (c == null) {
            throw new IOException("Ứng dụng chưa sẵn sàng");
        }
        File thu = new File(c.getCacheDir(), TepCapNhat.THU_MUC);
        // bản tải cũ (bản trước) không cần nữa
        File[] cu = thu.listFiles();
        if (cu != null) {
            for (File f : cu) {
                f.delete();
            }
        }
        String ten = tenTep(url, "HTBB.apk");
        if (!ten.toLowerCase().endsWith(".apk")) {
            ten = ten + ".apk";
        }
        return new File(thu, ten);
    }

    protected String cai(File tep) throws Exception {
        final Activity a = NenAndroid.hoatDong;
        if (a == null) {
            throw new IOException("Ứng dụng chưa sẵn sàng");
        }
        if (Build.VERSION.SDK_INT >= 26 && !a.getPackageManager().canRequestPackageInstalls()) {
            a.runOnUiThread(new Runnable() {
                public void run() {
                    try {
                        Intent i = new Intent(Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES, Uri.parse("package:" + a.getPackageName()));
                        a.startActivity(i);
                    } catch (Throwable e) {
                        // máy không có trang đó: người chơi dùng "Mở trang tải"
                    }
                }
            });
            return "Đã tải xong. Bật \"Cho phép từ nguồn này\" cho Huyền Thoại Bảo Bối, quay lại game rồi bấm Cài lại.";
        }
        final Uri uri = TepCapNhat.uri(a, tep);
        final Throwable[] loi = new Throwable[1];
        final java.util.concurrent.CountDownLatch xong = new java.util.concurrent.CountDownLatch(1);
        a.runOnUiThread(new Runnable() {
            public void run() {
                try {
                    Intent i = new Intent(Intent.ACTION_VIEW);
                    i.setDataAndType(uri, "application/vnd.android.package-archive");
                    i.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_ACTIVITY_NEW_TASK);
                    i.putExtra(Intent.EXTRA_NOT_UNKNOWN_SOURCE, true);
                    a.startActivity(i);
                } catch (Throwable e) {
                    loi[0] = e;
                } finally {
                    xong.countDown();
                }
            }
        });
        xong.await(5, java.util.concurrent.TimeUnit.SECONDS);
        if (loi[0] != null) {
            throw new IOException("Không mở được trình cài của Android");
        }
        return "Đã tải xong bản " + (phienBan.length() > 0 ? phienBan : "mới")
                + ". Bấm Cài đặt / Cập nhật trong hộp của Android (dữ liệu được giữ nguyên).";
    }
}
