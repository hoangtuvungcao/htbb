package network;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;

/**
 * Kho goi client (0.9.15, network.KhoGoi) tren RMS that cua ban PC (tep trong thu muc du lieu tam):
 * luu goi nguyen / goi manh, khai dung bam, dung kho tra DUNG byte, kho hong -> khong khai + doc hong -> ngat, bam sai
 * khong luu, goi vuot cho trong kho khong luu.
 *
 *   java -Djava.awt.headless=true -cp client_pc/out/build/cls:client_pc/out/build/res:$O network.KiemKhoGoi
 */
public final class KiemKhoGoi {
    static int dat, hong;

    static void kiem(boolean ok, String ten) {
        if (ok) {
            dat++;
        } else {
            hong++;
            System.out.println("HONG: " + ten);
        }
    }

    static byte[] ngauNhien(int n, int hat) {
        byte[] b = new byte[n];
        new java.util.Random(hat).nextBytes(b);
        return b;
    }

    static long bam(byte[] b) {
        return KhoGoi.bam(KhoGoi.BAM_DAU, b, 0, b.length);
    }

    static Message goi117(int kieu, int lenh, long h, int dai) throws Exception {
        ByteArrayOutputStream bo = new ByteArrayOutputStream();
        DataOutputStream o = new DataOutputStream(bo);
        o.writeByte(kieu);
        o.writeByte(lenh);
        o.writeLong(h);
        o.writeInt(dai);
        return new Message(KhoGoi.LENH, bo.toByteArray());
    }

    /** {lenh, bam, dai} cac muc khai */
    static long[][] khai() throws Exception {
        ByteArrayOutputStream bo = new ByteArrayOutputStream();
        KhoGoi.khai(new DataOutputStream(bo));
        DataInputStream d = new DataInputStream(new ByteArrayInputStream(bo.toByteArray()));
        int n = d.readUnsignedByte();
        long[][] r = new long[n][];
        for (int i = 0; i < n; i++) {
            r[i] = new long[]{d.readByte(), d.readLong(), d.readInt()};
        }
        return r;
    }

    static boolean coKhai(long[][] k, int lenh, long h, int dai) {
        for (long[] x : k) {
            if (x[0] == (byte) lenh && x[1] == h && x[2] == dai) {
                return true;
            }
        }
        return false;
    }

    static byte[] docHet(Message m) throws Exception {
        ByteArrayOutputStream bo = new ByteArrayOutputStream();
        byte[] t = new byte[4096];
        int n;
        while ((n = m.reader().read(t)) > 0) {
            bo.write(t, 0, n);
        }
        return bo.toByteArray();
    }

    public static void main(String[] a) throws Exception {
        // canh gio: kiem khong bao gio duoc treo (lan thu J2ME heap nho 10/2026 treo 5 gio) - qua 120 s la HONG
        Thread canh = new Thread(new Runnable() {
            public void run() {
                try {
                    Thread.sleep(120000L);
                } catch (InterruptedException e) {
                    return;
                }
                System.out.println("HONG: KiemKhoGoi qua 120 s (treo)");
                System.exit(2);
            }
        });
        canh.setDaemon(true);
        canh.start();
        java.io.File du = java.nio.file.Files.createTempDirectory("kiem_kho_goi").toFile();
        System.setProperty("htbb.duLieu", du.getAbsolutePath());
        System.setProperty("java.awt.headless", "true");

        kiem(KhoGoi.khaNang() == KhoGoi.KHA_NANG_KHO, "co RMS -> bao bit 4");
        kiem(khai().length == 0, "kho trong -> khai 0 muc");

        // goi nguyen (ban PC/APK nhan qua goi nen): lenh 26
        byte[] d26 = ngauNhien(150000, 1);
        long h26 = bam(d26);
        kiem(KhoGoi.nhan(goi117(0, 26, h26, d26.length)) == KhoGoi.BO_QUA, "-117 kieu 0 -> bo qua (cho luu)");
        Message m26 = new Message((byte) 26, d26);
        KhoGoi.sauXuLy(m26);
        kiem(KhoGoi.soLuu == 1, "luu goi 26 (" + KhoGoi.soLuu + ")");

        // goi manh (J2ME): lenh 25, ba manh co dau rieng
        byte[] d25 = ngauNhien(40000, 2);
        byte[][] ds = new byte[3][];
        int[] dau = {6, 1, 1};
        ds[0] = new byte[6 + 15000];
        System.arraycopy(d25, 0, ds[0], 6, 15000);
        ds[1] = new byte[1 + 15000];
        System.arraycopy(d25, 15000, ds[1], 1, 15000);
        ds[2] = new byte[1 + 10000];
        System.arraycopy(d25, 30000, ds[2], 1, 10000);
        long h25 = bam(d25);
        KhoGoi.nhan(goi117(0, 25, h25, d25.length));
        KhoGoi.sauXuLy(new Message((byte) 25, new LuongManh(ds, dau)));
        kiem(KhoGoi.soLuu == 2, "luu goi 25 tu manh");

        // bam sai -> khong luu
        byte[] d105 = ngauNhien(20000, 3);
        KhoGoi.nhan(goi117(0, 105, 12345L, d105.length));
        KhoGoi.sauXuLy(new Message((byte) 105, d105));
        kiem(KhoGoi.soLuu == 2, "bam khong khop goi -> khong luu");
        // goi khac lenh dang cho -> khong luu goi nay
        KhoGoi.nhan(goi117(0, 105, bam(d105), d105.length));
        KhoGoi.sauXuLy(new Message((byte) 27, d105));
        kiem(KhoGoi.soLuu == 2, "goi khac lenh khong bi luu nham");
        KhoGoi.sauXuLy(new Message((byte) 105, d105));
        kiem(KhoGoi.soLuu == 3, "goi 105 dung lenh -> luu");

        // lenh 1: hai noi dung khac nhau -> hai o (PC)
        byte[] d1a = ngauNhien(17000, 4), d1b = ngauNhien(24000, 5);
        KhoGoi.nhan(goi117(0, 1, bam(d1a), d1a.length));
        KhoGoi.sauXuLy(new Message((byte) 1, d1a));
        KhoGoi.nhan(goi117(0, 1, bam(d1b), d1b.length));
        KhoGoi.sauXuLy(new Message((byte) 1, d1b));

        long[][] k = khai();
        kiem(k.length == 5, "khai 5 muc (" + k.length + ")");
        kiem(coKhai(k, 26, h26, d26.length) && coKhai(k, 25, h25, d25.length) && coKhai(k, 105, bam(d105), d105.length), "khai dung (lenh, bam, dai)");
        kiem(coKhai(k, 1, bam(d1a), d1a.length) && coKhai(k, 1, bam(d1b), d1b.length), "lenh 1: hai noi dung hai o");

        // dung kho -> dung byte
        Message g = KhoGoi.nhan(goi117(1, 26, h26, d26.length));
        kiem(g != null && g.command == 26 && java.util.Arrays.equals(docHet(g), d26), "dung kho 26 -> dung tung byte");
        Message g25 = KhoGoi.nhan(goi117(1, 25, h25, d25.length));
        kiem(g25 != null && java.util.Arrays.equals(docHet(g25), d25), "dung kho 25 (luu tu manh) -> dung tung byte");
        kiem(g25 != null && g25.tuDau() && java.util.Arrays.equals(docHet(g25), d25), "goi tu kho doc lai tu dau duoc (thieu bo nho -> xu ly lai)");
        kiem(KhoGoi.soDung == 2, "dem dung kho");

        // goi 26 doi -> luu de len o cu (moi lenh 2 o: o con lai)
        byte[] d26b = ngauNhien(150000, 9);
        KhoGoi.nhan(goi117(0, 26, bam(d26b), d26b.length));
        KhoGoi.sauXuLy(new Message((byte) 26, d26b));
        k = khai();
        kiem(coKhai(k, 26, bam(d26b), d26b.length), "goi 26 moi duoc khai");

        // kho hong: sua mot khuc -> khong khai muc do; dung kho muc khong khai -> null (ngat) + tat kho
        javax.microedition.rms.RecordStore r = javax.microedition.rms.RecordStore.openRecordStore(KhoGoi.ten(KhoGoi.viTri(105), 0), false);
        byte[] x = r.getRecord(2);
        x[5] ^= 1;
        r.setRecord(2, x, 0, x.length);
        r.closeRecordStore();
        k = khai();
        kiem(!coKhai(k, 105, bam(d105), d105.length), "khuc hong -> muc 105 khong khai (" + k.length + " muc)");
        kiem(KhoGoi.nhan(goi117(1, 105, bam(d105), d105.length)) == null && KhoGoi.tat, "dung kho muc hong -> null (ngat) + tat kho");
        kiem(KhoGoi.khaNang() == 0 && khai().length == 0, "kho da tat -> khong bao bit, khai 0 (lan noi sau nhan du)");
        KhoGoi.tat = false;
        kiem(khai().length == 0, "kho hong da bi xoa het");

        // vuot cho trong kho (PC bao 4 MB) -> khong luu
        int truoc = KhoGoi.soLuu;
        byte[] to = ngauNhien(4300000, 7);
        KhoGoi.nhan(goi117(0, 26, bam(to), to.length));
        KhoGoi.sauXuLy(new Message((byte) 26, to));
        kiem(KhoGoi.soLuu == truoc && khai().length == 0, "goi vuot cho trong kho -> khong luu");

        // KHONG co RMS (may khong mo duoc kho / J2ME): khong bao bit, khai 0, goi -117 kieu 0 bo qua va KHONG luu, kieu 1
        // (khong the xay ra vi khong khai) -> null = ngat; lan noi sau van khong bao bit -> may chu gui du. Khong vong lap.
        KhoGoi.coRms = 0;
        KhoGoi.tat = false;
        int luu0 = KhoGoi.soLuu;
        kiem(KhoGoi.khaNang() == 0, "khong RMS: khong bao bit kho");
        kiem(khai().length == 0, "khong RMS: khai 0 muc");
        byte[] dk = ngauNhien(30000, 11);
        kiem(KhoGoi.nhan(goi117(0, 105, bam(dk), dk.length)) == KhoGoi.BO_QUA, "khong RMS: -117 kieu 0 bo qua");
        KhoGoi.sauXuLy(new Message((byte) 105, dk));
        kiem(KhoGoi.soLuu == luu0, "khong RMS: goi du van xu ly, khong luu");
        long t0 = System.currentTimeMillis();
        for (int i = 0; i < 1000; i++) {
            KhoGoi.nhan(goi117(1, 105, bam(dk), dk.length));
        }
        kiem(KhoGoi.khaNang() == 0 && System.currentTimeMillis() - t0 < 5000, "khong RMS: dung kho that bai lap lai van tra ngay, khong bat lai kho");

        System.out.println("KiemKhoGoi: " + dat + " dat, " + hong + " hong");
        System.exit(hong == 0 ? 0 : 1);
    }
}
