package game.render.screen;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import com.teamobi.kpah.GameMidlet;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.MessageDigest;
import org.json.JSONObject;

/**
 * Tự cập nhật: lúc mở game đọc https://nqsh-h2.top/tai/phienban.json (do phat-hanh.sh ghi),
 * mục "android": {"ban": versionCode, "link": ..., "sha256": ...}. Bản mới hơn versionCode đang
 * cài thì hỏi người chơi, tải APK (kiểm sha256) rồi mở màn hình cài đặt của Android.
 * Không có mạng / file hỏng thì bỏ qua, game chạy bình thường.
 */
public final class NqshCapNhat {
    private static final String LINK = "https://nqsh-h2.top/tai/phienban.json";
    private static final String AUTHORITY_DUOI = ".capnhat";
    private static boolean daChay;

    private NqshCapNhat() {
    }

    public static synchronized void batDau() {
        if (daChay) {
            return;
        }
        daChay = true;
        new Thread() {
            public void run() {
                try {
                    Thread.sleep(1500L);
                    kiemTra();
                } catch (Throwable t) {
                    System.out.println("NQSH capnhat: " + t);
                }
            }
        }.start();
    }

    private static void kiemTra() throws Exception {
        final Activity a = GameMidlet.instance;
        if (a == null) {
            return;
        }
        String s = GameMidlet.connectHTTP(LINK + "?t=" + System.currentTimeMillis());
        if (s == null) {
            return;
        }
        JSONObject j = new JSONObject(s).optJSONObject("android");
        if (j == null) {
            return;
        }
        final int ban = j.optInt("ban", 0);
        final String link = j.optString("link", "");
        final String sha = j.optString("sha256", "").toLowerCase();
        final String ghiChu = j.optString("ghiChu", "");
        int dangCai = a.getPackageManager().getPackageInfo(a.getPackageName(), 0).versionCode;
        System.out.println("NQSH capnhat: dang cai " + dangCai + ", moi nhat " + ban);
        if (ban <= dangCai || link.length() == 0) {
            return;
        }
        a.runOnUiThread(new Runnable() {
            public void run() {
                new AlertDialog.Builder(a)
                        .setTitle("Có phiên bản mới")
                        .setMessage(ghiChu.length() > 0 ? ghiChu : "Đã có bản NQSH-H2 mới. Cập nhật ngay để chơi tiếp.")
                        .setCancelable(false)
                        .setPositiveButton("Cập nhật", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface d, int w) {
                                taiVe(a, link, sha, ban);
                            }
                        })
                        .setNegativeButton("Để sau", null)
                        .show();
            }
        });
    }

    private static void taiVe(final Activity a, final String link, final String sha, final int ban) {
        final ProgressDialog pd = new ProgressDialog(a);
        pd.setTitle("Đang tải bản mới");
        pd.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        pd.setCancelable(false);
        pd.setMax(100);
        pd.show();
        new Thread() {
            public void run() {
                File f = null;
                String loi = null;
                try {
                    File dir = new File(a.getCacheDir(), "capnhat");
                    dir.mkdirs();
                    File[] cu = dir.listFiles();
                    if (cu != null) {
                        for (int i = 0; i < cu.length; i++) {
                            cu[i].delete();
                        }
                    }
                    f = new File(dir, "NQSH-H2-" + ban + ".apk");
                    HttpURLConnection c = (HttpURLConnection) new URL(link).openConnection();
                    c.setConnectTimeout(15000);
                    c.setReadTimeout(30000);
                    int tong = c.getContentLength();
                    MessageDigest md = MessageDigest.getInstance("SHA-256");
                    InputStream in = c.getInputStream();
                    FileOutputStream out = new FileOutputStream(f);
                    byte[] b = new byte[16384];
                    int n;
                    long da = 0;
                    while ((n = in.read(b)) > 0) {
                        out.write(b, 0, n);
                        md.update(b, 0, n);
                        da += n;
                        if (tong > 0) {
                            final int pt = (int) (da * 100 / tong);
                            a.runOnUiThread(new Runnable() {
                                public void run() {
                                    pd.setProgress(pt);
                                }
                            });
                        }
                    }
                    out.close();
                    in.close();
                    c.disconnect();
                    if (sha.length() > 0 && !sha.equals(hex(md.digest()))) {
                        loi = "File tải về bị lỗi, vui lòng thử lại.";
                    }
                } catch (Throwable t) {
                    loi = "Không tải được bản mới, kiểm tra mạng rồi thử lại.";
                    System.out.println("NQSH capnhat: " + t);
                }
                final String loiCuoi = loi;
                final File apk = f;
                a.runOnUiThread(new Runnable() {
                    public void run() {
                        pd.dismiss();
                        if (loiCuoi != null) {
                            new AlertDialog.Builder(a).setMessage(loiCuoi).setPositiveButton("OK", null).show();
                            return;
                        }
                        caiDat(a, apk);
                    }
                });
            }
        }.start();
    }

    private static void caiDat(Activity a, File apk) {
        try {
            Uri u;
            if (Build.VERSION.SDK_INT >= 24) {
                u = androidx.core.content.FileProvider.getUriForFile(a, a.getPackageName() + AUTHORITY_DUOI, apk);
            } else {
                u = Uri.fromFile(apk);
            }
            Intent i = new Intent(Intent.ACTION_VIEW);
            i.setDataAndType(u, "application/vnd.android.package-archive");
            i.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_ACTIVITY_NEW_TASK);
            a.startActivity(i);
        } catch (Throwable t) {
            System.out.println("NQSH capnhat: " + t);
            new AlertDialog.Builder(a).setMessage("Không mở được trình cài đặt: " + t).setPositiveButton("OK", null).show();
        }
    }

    private static String hex(byte[] b) {
        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < b.length; i++) {
            sb.append(Integer.toHexString((b[i] & 0xFF) | 0x100).substring(1));
        }
        return sb.toString();
    }
}
