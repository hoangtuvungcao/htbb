package game.model;

import game.pokemon.AmThanh;
import java.io.InputStream;
import javax.microedition.media.Control;
import javax.microedition.media.Manager;
import javax.microedition.media.Player;
import javax.microedition.media.control.VolumeControl;

/**
 * Phan MMAPI (JSR-135) cua Sound - CHI duoc tao tren luong nen cua Sound, trong try/catch: may
 * khong co javax.microedition.media thi `new SoundMm()` nem NoClassDefFoundError va Sound im lang.
 *
 * - Tieng hieu ung: moi tieng mot Player WAV, nap truoc (prefetch) khi dung lan dau; giu toi da
 *   TOI_DA_GIU Player san sang (LRU, bo cai lau khong dung) vi nhieu may gioi han so Player.
 * - Toi da TOI_DA_CHOI tieng cung luc; day thi tieng moi chi thay tieng dang choi co uu tien
 *   thap hon hoac bang (vd tieng danh lien tuc thay nhau, khong che tieng len cap).
 * - May khong tron duoc nhieu tieng (supports.mixing=false): chi mot tieng, va tieng hieu ung uu
 *   tien thap khong phat khi dang co nhac (tranh cat nhac lien tuc).
 * - Nhac nen: mot Player MIDI lap vo han, am luong = 70% am luong chung.
 */
final class SoundMm {
    /**
     * 0.9.22 (Nokia E72 / S60): mỗi Player đã prefetch giữ một kênh âm thanh gốc của máy (S60: DevSound) - máy J2ME thật
     * chỉ giữ 3 tiếng sẵn sàng (PC / APK 8).
     */
    private static final int TOI_DA_GIU = game.pokemon.BoNho.laJ2me ? 3 : 8;
    private static final int TOI_DA_CHOI = 2;

    private final Player[] p = new Player[AmThanh.TEN.length];
    private final long[] dungLuc = new long[AmThanh.TEN.length];
    private final boolean[] hong = new boolean[AmThanh.TEN.length];
    private final int[] dangChoi = new int[TOI_DA_CHOI];
    private final int[] uuTienChoi = new int[TOI_DA_CHOI];
    private final boolean tron;
    private Player nhac;

    SoundMm() {
        // tham chieu Manager ngay: may thieu MMAPI nem loi o day (Sound bat)
        Manager.getSupportedContentTypes(null);
        String m = System.getProperty("supports.mixing");
        tron = m == null || !"false".equals(m.toLowerCase());
        for (int i = 0; i < TOI_DA_CHOI; i++) {
            dangChoi[i] = -1;
        }
    }

    /** Nap truoc vai tieng hay dung nhat (tieng dau tien khong bi tre). */
    void napTruoc() {
        int[] ds = {AmThanh.DANH, AmThanh.BI_DANH, AmThanh.CLICK};
        for (int i = 0; i < ds.length; i++) {
            lay(ds[i]);
        }
    }

    private InputStream mo(String ten) {
        return getClass().getResourceAsStream(ten);
    }

    private int soDangGiu() {
        int n = 0;
        for (int i = 0; i < p.length; i++) {
            if (p[i] != null) {
                n++;
            }
        }
        return n;
    }

    private void boBot() {
        while (soDangGiu() >= TOI_DA_GIU) {
            int cu = -1;
            for (int i = 0; i < p.length; i++) {
                if (p[i] != null && !dangPhat(i) && (cu < 0 || dungLuc[i] < dungLuc[cu])) {
                    cu = i;
                }
            }
            if (cu < 0) {
                return;
            }
            dong(p[cu]);
            p[cu] = null;
        }
    }

    private boolean dangPhat(int id) {
        for (int i = 0; i < TOI_DA_CHOI; i++) {
            if (dangChoi[i] == id) {
                Player x = p[id];
                return x != null && x.getState() == Player.STARTED;
            }
        }
        return false;
    }

    private Player lay(int id) {
        if (p[id] != null || hong[id]) {
            return p[id];
        }
        boBot();
        InputStream in = null;
        try {
            in = mo("/am/s_" + AmThanh.TEN[id] + ".wav");
            if (in == null) {
                hong[id] = true;   // ban nhe khong co tieng nay
                return null;
            }
            Player x = Manager.createPlayer(in, "audio/x-wav");
            x.realize();
            x.prefetch();
            p[id] = x;
        } catch (Throwable e) {
            hong[id] = true;
        } finally {
            try {
                if (in != null) {
                    in.close();
                }
            } catch (Throwable e) {
                // bo qua
            }
        }
        return p[id];
    }

    private static void datAm(Player x, int amLuong) {
        try {
            Control c = x.getControl("VolumeControl");
            if (c instanceof VolumeControl) {
                ((VolumeControl) c).setLevel(amLuong);
            }
        } catch (Throwable e) {
            // may khong co VolumeControl
        }
    }

    private static void dong(Player x) {
        try {
            x.close();
        } catch (Throwable e) {
            // bo qua
        }
    }

    void choi(int id, String ten, int uuTien, int amLuong) {
        int toiDa = tron ? TOI_DA_CHOI : 1;
        if (!tron && nhac != null && uuTien < 2) {
            return;
        }
        // tim cho: o trong / o da choi xong / o cung tieng / o uu tien thap nhat
        int o = -1;
        for (int i = 0; i < toiDa; i++) {
            int d = dangChoi[i];
            if (d < 0 || d == id || p[d] == null || p[d].getState() != Player.STARTED) {
                o = i;
                break;
            }
        }
        if (o < 0) {
            int thap = 0;
            for (int i = 1; i < toiDa; i++) {
                if (uuTienChoi[i] < uuTienChoi[thap]) {
                    thap = i;
                }
            }
            if (uuTienChoi[thap] > uuTien) {
                return;
            }
            o = thap;
            try {
                p[dangChoi[o]].stop();
            } catch (Throwable e) {
                // bo qua
            }
        }
        Player x = lay(id);
        if (x == null) {
            return;
        }
        try {
            if (x.getState() == Player.STARTED) {
                x.stop();
            }
            x.setMediaTime(0);
            datAm(x, amLuong);
            x.start();
            dangChoi[o] = id;
            uuTienChoi[o] = uuTien;
            dungLuc[id] = System.currentTimeMillis();
        } catch (Throwable e) {
            // Player hong (vd thiet bi am thanh ban): dong, lan sau tao lai
            dong(x);
            p[id] = null;
        }
    }

    void choiNhac(String ten, int amLuong) {
        dungNhac();
        InputStream in = null;
        try {
            in = mo("/am/n_" + ten + ".mid");
            if (in == null) {
                return;
            }
            Player x = Manager.createPlayer(in, "audio/midi");
            x.realize();
            x.setLoopCount(-1);
            x.prefetch();
            datAm(x, amLuong * 70 / 100);
            x.start();
            nhac = x;
        } catch (Throwable e) {
            nhac = null;
        } finally {
            try {
                if (in != null) {
                    in.close();
                }
            } catch (Throwable e) {
                // bo qua
            }
        }
    }

    void dungNhac() {
        if (nhac != null) {
            try {
                nhac.stop();
            } catch (Throwable e) {
                // bo qua
            }
            dong(nhac);
            nhac = null;
        }
    }

    void amLuong(int a) {
        if (nhac != null) {
            datAm(nhac, a * 70 / 100);
        }
    }
}
