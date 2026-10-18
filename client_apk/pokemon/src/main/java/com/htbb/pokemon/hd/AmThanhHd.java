package com.htbb.pokemon.hd;

import android.content.Context;
import android.content.res.AssetFileDescriptor;
import android.media.AudioAttributes;
import android.media.MediaPlayer;
import android.media.SoundPool;
import android.util.Log;
import java.util.HashMap;
import javax.microedition.lcdui.NenAndroid;

/**
 * Âm thanh chất lượng cao của APK: thay lớp MMAPI của jar (game.model.SoundMm - WAV 8 bit 11 kHz + MIDI)
 * bằng assets/am_hd (OGG 44,1 kHz do client_apk/pokemon/tools/lam_am_thanh_hd.py làm từ cùng nguồn Kenney CC0
 * và cùng các bài chiptune gốc). port-j2me.py chèn "if (AmThanhHd.xxx(...)) return;" vào đầu các hàm SoundMm:
 * cùng sự kiện, cùng ưu tiên, cùng cài đặt bật / tắt / âm lượng của jar (game.model.Sound gọi vào).
 * - Hiệu ứng: SoundPool (8 luồng, nạp sẵn).
 * - Nhạc nền: MediaPlayer lặp, CHUYỂN BÀI CÓ CROSSFADE (~1,2 s) khi đổi vùng / vào trận / boss.
 * - Hai thanh âm lượng riêng (Nhạc / Hiệu ứng, bảng Chỉnh nút của HUD) nhân với âm lượng chung của jar.
 * Thiếu tệp HD -> trả false: jar tự phát bản cũ.
 */
public final class AmThanhHd {
    private AmThanhHd() {
    }

    private static Context ctx;
    private static SoundPool pool;
    private static final HashMap<String, Integer> SFX = new HashMap<String, Integer>();
    private static final HashMap<Integer, Boolean> DA_NAP = new HashMap<Integer, Boolean>();
    private static boolean coHd;
    /** 0..1 (bảng Chỉnh nút) */
    public static float amNhac = 0.75f;
    public static float amHieuUng = 1f;
    private static int amChung = 80;

    private static MediaPlayer nhac;
    private static String tenNhac;
    private static float vNhac;          // âm lượng hiện tại của bài đang phát (0..1, chưa nhân)
    private static MediaPlayer nhacCu;
    private static float vCu;
    private static Thread luongMo;
    /** số lần phát thành công (kiểm thử) */
    public static volatile int soLanPhat, soLanNhac;

    public static void khoiTao(Context c) {
        ctx = c.getApplicationContext();
        try {
            android.content.SharedPreferences p = ctx.getSharedPreferences("htbb_hud", 0);
            amNhac = p.getFloat("amNhac", 0.75f);
            amHieuUng = p.getFloat("amHieuUng", 1f);
        } catch (Throwable e) {
            // mặc định
        }
    }

    public static void luu() {
        try {
            ctx.getSharedPreferences("htbb_hud", 0).edit().putFloat("amNhac", amNhac).putFloat("amHieuUng", amHieuUng).apply();
        } catch (Throwable e) {
            // bỏ qua
        }
        datAmNhac();
    }

    private static boolean co(String tep) {
        try {
            AssetFileDescriptor fd = ctx.getAssets().openFd(tep);
            fd.close();
            return true;
        } catch (Throwable e) {
            return false;
        }
    }

    /** SoundMm.napTruoc: nạp sẵn mọi tiếng HD vào SoundPool. */
    public static synchronized boolean napTruoc() {
        if (ctx == null) return false;
        if (pool != null) return coHd;
        coHd = co("am_hd/s_click.ogg");
        if (!coHd) return false;
        AudioAttributes aa = new AudioAttributes.Builder().setUsage(AudioAttributes.USAGE_GAME)
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION).build();
        pool = new SoundPool.Builder().setMaxStreams(8).setAudioAttributes(aa).build();
        pool.setOnLoadCompleteListener(new SoundPool.OnLoadCompleteListener() {
            public void onLoadComplete(SoundPool sp, int id, int st) {
                synchronized (DA_NAP) {
                    DA_NAP.put(id, st == 0);
                }
            }
        });
        try {
            String[] ds = ctx.getAssets().list("am_hd");
            for (int i = 0; ds != null && i < ds.length; i++) {
                String t = ds[i];
                if (t.startsWith("s_") && t.endsWith(".ogg")) {
                    AssetFileDescriptor fd = ctx.getAssets().openFd("am_hd/" + t);
                    int id = pool.load(fd, 1);
                    fd.close();
                    SFX.put(t.substring(2, t.length() - 4), id);
                }
            }
            Log.i(NenAndroid.TAG, "am thanh HD: " + SFX.size() + " tieng");
        } catch (Throwable e) {
            Log.w(NenAndroid.TAG, "khong nap duoc am thanh HD", e);
        }
        return true;
    }

    /** SoundMm.choi(id, ten, uuTien, amLuong) */
    public static boolean choi(int id, String ten, int uuTien, int amLuong) {
        if (!napTruoc()) return false;
        Integer s = SFX.get(ten);
        if (s == null) return false;
        Boolean xong;
        synchronized (DA_NAP) {
            xong = DA_NAP.get(s);
        }
        if (xong != null && !xong) return false;
        amChung = amLuong;
        float v = Math.max(0f, Math.min(1f, amLuong / 100f * amHieuUng));
        if (v <= 0) return true;
        int r = pool.play(s, v, v, uuTien, 0, 1f);
        if (r != 0) soLanPhat++;
        return true;
    }

    /** SoundMm.choiNhac(ten, amLuong): đổi bài có crossfade. */
    public static synchronized boolean choiNhac(String ten, int amLuong) {
        if (ctx == null) return false;
        String tep = "am_hd/n_" + ten + ".ogg";
        if (!co(tep)) return false;
        amChung = amLuong;
        if (ten.equals(tenNhac) && nhac != null) {
            datAmNhac();
            return true;
        }
        MediaPlayer m = null;
        try {
            AssetFileDescriptor fd = ctx.getAssets().openFd(tep);
            m = new MediaPlayer();
            m.setAudioAttributes(new AudioAttributes.Builder().setUsage(AudioAttributes.USAGE_GAME)
                    .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC).build());
            m.setDataSource(fd.getFileDescriptor(), fd.getStartOffset(), fd.getLength());
            fd.close();
            m.setLooping(true);
            m.prepare();
            m.setVolume(0f, 0f);
            m.start();
            soLanNhac++;
        } catch (Throwable e) {
            Log.w(NenAndroid.TAG, "khong phat duoc nhac HD " + ten, e);
            if (m != null) m.release();
            return false;
        }
        if (nhacCu != null) {
            try {
                nhacCu.release();
            } catch (Throwable e) {
                // bỏ qua
            }
        }
        nhacCu = nhac;
        vCu = vNhac;
        nhac = m;
        tenNhac = ten;
        vNhac = 0f;
        moDan();
        return true;
    }

    /** SoundMm.dungNhac: tắt dần. */
    public static synchronized boolean dungNhac() {
        if (nhac == null && tenNhac == null) return co("am_hd/n_tieude.ogg");
        if (nhacCu != null) {
            try {
                nhacCu.release();
            } catch (Throwable e) {
                // bỏ qua
            }
        }
        nhacCu = nhac;
        vCu = vNhac;
        nhac = null;
        tenNhac = null;
        moDan();
        return true;
    }

    /** SoundMm.amLuong(a) */
    public static synchronized boolean amLuong(int a) {
        if (ctx == null || !co("am_hd/n_tieude.ogg")) return false;
        amChung = a;
        datAmNhac();
        return true;
    }

    private static void datAmNhac() {
        synchronized (AmThanhHd.class) {
            float k = amChung / 100f * amNhac;
            try {
                if (nhac != null) nhac.setVolume(vNhac * k, vNhac * k);
            } catch (Throwable e) {
                // bỏ qua
            }
        }
    }

    /** Luồng crossfade: bài mới lên dần, bài cũ xuống dần (~1,2 s). */
    private static void moDan() {
        if (luongMo != null && luongMo.isAlive()) return;
        luongMo = new Thread("HTBB-nhac") {
            public void run() {
                while (true) {
                    synchronized (AmThanhHd.class) {
                        float k = amChung / 100f * amNhac;
                        boolean xong = true;
                        if (nhac != null && vNhac < 1f) {
                            vNhac = Math.min(1f, vNhac + 0.035f);
                            xong = false;
                        }
                        if (nhacCu != null) {
                            vCu -= 0.035f;
                            if (vCu <= 0f) {
                                try {
                                    nhacCu.release();
                                } catch (Throwable e) {
                                    // bỏ qua
                                }
                                nhacCu = null;
                            } else {
                                xong = false;
                                try {
                                    nhacCu.setVolume(vCu * k, vCu * k);
                                } catch (Throwable e) {
                                    // bỏ qua
                                }
                            }
                        }
                        try {
                            if (nhac != null) nhac.setVolume(vNhac * k, vNhac * k);
                        } catch (Throwable e) {
                            // bỏ qua
                        }
                        if (xong) return;
                    }
                    try {
                        Thread.sleep(40);
                    } catch (InterruptedException e) {
                        return;
                    }
                }
            }
        };
        luongMo.setDaemon(true);
        luongMo.start();
    }
}
