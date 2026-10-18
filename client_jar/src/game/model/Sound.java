package game.model;

import game.pokemon.AmThanh;
import javax.microedition.rms.RecordStore;

/**
 * Am thanh cua ban jar (Huyen Thoai Bao Boi): tieng hieu ung (WAV) + nhac nen (MIDI) trong /am/.
 * Danh sach tieng/nhac va cach chon theo su kien/ban do: game.pokemon.AmThanh.
 *
 * An toan tren moi may:
 *  - KHONG tham chieu javax.microedition.media o lop nay: phan MMAPI nam rieng trong SoundMm, chi
 *    tao khi may bao co MMAPI (microedition.media.version) va moi loi (ke ca NoClassDefFoundError)
 *    deu bi bat -> may khong co MMAPI van choi binh thuong, chi im lang.
 *  - Phat tren MOT luong nen (khong lam giat vong game): game chi dat yeu cau vao hang doi co dinh
 *    (mang int, khong cap phat moi su kien), luong nen tao/nap/phat Player.
 *  - Cai dat (nhac bat/tat, hieu ung bat/tat, am luong) luu RecordStore "htbbAm".
 * Lenh -46 cua server cu (gui file am thanh) bi bo qua nhu ban goc.
 */
public class Sound implements Runnable {
    public int volume;
    public static Sound me = new Sound();
    /** Tuong thich ma cu (Nen/PauseMenu): 1 = co tieng, 0 = tat het. */
    public int isPlaying = -1;

    // ------------------------------------------------------------------ cai dat
    public boolean batNhac = true;
    public boolean batHieuUng = true;
    /** 0..100 */
    public int amLuong = 80;
    private boolean daNapCaiDat;

    // ------------------------------------------------------------------ luong nen
    private SoundMm mm;
    private boolean thuMm;
    private boolean khongCoMm;
    private Thread luong;
    private final Object khoa = new Object();
    private final int[] hang = new int[8];
    private int dau, cuoi;
    /** Nhac can phat (-1 = tat), nhac dang phat. */
    private int nhacMuon = -1;
    private int nhacDang = -1;
    private boolean doiAmLuong;
    private boolean tamDung;

    public Sound() {
    }

    // ================================================================== cai dat

    public void napCaiDat() {
        if (daNapCaiDat) {
            return;
        }
        daNapCaiDat = true;
        RecordStore rs = null;
        try {
            rs = RecordStore.openRecordStore("htbbAm", false);
            byte[] b = rs.getRecord(1);
            if (b != null && b.length >= 3) {
                batNhac = b[0] != 0;
                batHieuUng = b[1] != 0;
                amLuong = b[2] < 0 ? 0 : (b[2] > 100 ? 100 : b[2]);
            }
        } catch (Throwable e) {
            // chua co -> mac dinh
        } finally {
            dong(rs);
        }
        isPlaying = (batNhac || batHieuUng) ? 1 : 0;
        volume = amLuong;
    }

    public void luuCaiDat() {
        RecordStore rs = null;
        try {
            byte[] b = {(byte) (batNhac ? 1 : 0), (byte) (batHieuUng ? 1 : 0), (byte) amLuong};
            rs = RecordStore.openRecordStore("htbbAm", true);
            if (rs.getNumRecords() == 0) {
                rs.addRecord(b, 0, b.length);
            } else {
                rs.setRecord(1, b, 0, b.length);
            }
        } catch (Throwable e) {
            // khong luu duoc: chi giu trong phien
        } finally {
            dong(rs);
        }
    }

    private static void dong(RecordStore rs) {
        try {
            if (rs != null) {
                rs.closeRecordStore();
            }
        } catch (Throwable e) {
            // bo qua
        }
    }

    public void datNhac(boolean bat) {
        napCaiDat();
        batNhac = bat;
        isPlaying = (batNhac || batHieuUng) ? 1 : 0;
        luuCaiDat();
        danhThuc();
    }

    public void datHieuUng(boolean bat) {
        napCaiDat();
        batHieuUng = bat;
        isPlaying = (batNhac || batHieuUng) ? 1 : 0;
        luuCaiDat();
    }

    public void datAmLuong(int a) {
        napCaiDat();
        amLuong = a < 0 ? 0 : (a > 100 ? 100 : a);
        volume = amLuong;
        luuCaiDat();
        synchronized (khoa) {
            doiAmLuong = true;
        }
        danhThuc();
    }

    // ================================================================== API cu (giu ten)

    public void onRequestOpenSound(String des, byte id) {
    }

    public int setSound(String na) {
        return -1;
    }

    public void onSoundData(byte[] dataS, byte id, byte loopS) {
    }

    /** Tam dung nhac (vd man hinh bi che - Canvas.hideNotify). */
    public void pauseMusic() {
        synchronized (khoa) {
            tamDung = true;
        }
        danhThuc();
    }

    public void resumeMusic() {
        synchronized (khoa) {
            tamDung = false;
        }
        danhThuc();
    }

    public void stopMusic() {
        phatNhac(-1);
    }

    public void play(byte[] data, int index) {
    }

    /** Ma cu (Nen.datAmThanh): 0 = tat het, &gt; 0 = bat het. */
    public void setVolume(int a) {
        napCaiDat();
        boolean bat = a > 0;
        batNhac = bat;
        batHieuUng = bat;
        isPlaying = bat ? 1 : 0;
        luuCaiDat();
        danhThuc();
    }

    // ================================================================== phat

    /** Tieng hieu ung so id (AmThanh.*). Goi tu vong game: chi xep hang, khong cho. */
    public void phat(int id) {
        napCaiDat();
        if (!batHieuUng || amLuong <= 0 || khongCoMm || id < 0) {
            return;
        }
        if (!batDau()) {
            return;
        }
        synchronized (khoa) {
            int sau = (cuoi + 1) % hang.length;
            if (sau == dau) {
                return;   // hang day: bo tieng nay
            }
            hang[cuoi] = id;
            cuoi = sau;
            khoa.notify();
        }
    }

    /** Doi nhac nen (AmThanh.N_*), -1 = tat. Cung bai dang phat thi khong lam gi. */
    public void phatNhac(int id) {
        napCaiDat();
        synchronized (khoa) {
            nhacMuon = id;
        }
        if (id >= 0 && !batDau()) {
            return;
        }
        danhThuc();
    }

    public int nhacDangMuon() {
        return nhacMuon;
    }

    private void danhThuc() {
        synchronized (khoa) {
            khoa.notify();
        }
    }

    /** Tao luong nen lan dau; false neu may khong co MMAPI. */
    private boolean batDau() {
        if (khongCoMm) {
            return false;
        }
        if (luong != null) {
            return true;
        }
        try {
            if (System.getProperty("microedition.media.version") == null) {
                khongCoMm = true;
                return false;
            }
            luong = new Thread(this);
            luong.start();
            return true;
        } catch (Throwable e) {
            khongCoMm = true;
            return false;
        }
    }

    private SoundMm layMm() {
        if (mm == null && !thuMm) {
            thuMm = true;
            try {
                mm = new SoundMm();
                mm.napTruoc();
            } catch (Throwable e) {
                mm = null;
                khongCoMm = true;
            }
        }
        return mm;
    }

    public void run() {
        try {
            Thread.sleep(300);
        } catch (Throwable e) {
            // bo qua
        }
        while (true) {
            int id = -1;
            int nhac;
            boolean amLuongMoi;
            synchronized (khoa) {
                if (dau != cuoi) {
                    id = hang[dau];
                    dau = (dau + 1) % hang.length;
                }
                // tam dung / tat nhac / am luong 0 -> khong phat bai nao
                nhac = (batNhac && amLuong > 0 && !tamDung) ? nhacMuon : -1;
                amLuongMoi = doiAmLuong;
                doiAmLuong = false;
                if (id < 0 && nhac == nhacDang && !amLuongMoi) {
                    try {
                        khoa.wait(2000);
                    } catch (Throwable e) {
                        // bo qua
                    }
                    continue;
                }
            }
            SoundMm m = layMm();
            if (m == null) {
                return;
            }
            try {
                if (amLuongMoi) {
                    m.amLuong(amLuong);
                }
                if (nhac != nhacDang) {
                    if (nhac < 0) {
                        m.dungNhac();
                    } else {
                        m.choiNhac(AmThanh.TEN_NHAC[nhac], amLuong);
                    }
                    nhacDang = nhac;
                }
                if (id >= 0 && batHieuUng) {
                    m.choi(id, AmThanh.TEN[id], AmThanh.UU_TIEN[id], amLuong);
                }
            } catch (Throwable e) {
                // mot tieng loi khong lam chet luong
            }
        }
    }
}
