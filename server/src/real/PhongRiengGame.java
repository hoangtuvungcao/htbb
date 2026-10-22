package real;

import com.htbb.pokemon.PhongRieng;
import io.Message;

/**
 * Phan GAME dung chung cua phong rieng (Pho ban Ham nguc + Cup PvP tuan, 0.9.7): tao ban do, dua nguoi choi vao /
 * ra mot khu, goi -112 (gate client BAN &gt;= 908), luat ban do (dang nhap lai, doi khu). Logic thuan:
 * com.htbb.pokemon.PhongRieng.
 */
public final class PhongRiengGame {

    /** Ban do pho ban: nen "Ngục tối" (117: cap 0, khong Pokemon hoang da, khoa-map da dat khong tu dong). */
    public static final int MAP_PHO_BAN = 4381, NAP_PHO_BAN = 117, KHU_PHO_BAN = 41;
    /** San cup: nen "Sàn đấu" (227: cap 0, dau truong). */
    public static final int MAP_CUP = 4382, NAP_CUP = 227, KHU_CUP = 127;

    static volatile MapPhongRieng phoBan;
    static volatile MapPhongRieng cup;
    static final PhongRieng.Kho KHO_PHO_BAN = new PhongRieng.Kho(KHU_PHO_BAN);
    static final PhongRieng.Kho KHO_CUP = new PhongRieng.Kho(KHU_CUP);

    private PhongRiengGame() {
    }

    /** RealController.initMapHTBB: tao hai ban do phong rieng. Loi thi tinh nang tu tat (ban do null). */
    static void taoBanDo(final java.util.Hashtable<Integer, Map> mapList) {
        try {
            phoBan = new MapPhongRieng(MAP_PHO_BAN, NAP_PHO_BAN, KHU_PHO_BAN);
            mapList.put(Integer.valueOf(MAP_PHO_BAN), phoBan);
            PhongRieng.dangKyMap(MAP_PHO_BAN);
        } catch (final Throwable e) {
            System.out.println("[PHONG RIENG] khong tao duoc ban do pho ban: " + e);
        }
        try {
            cup = new MapPhongRieng(MAP_CUP, NAP_CUP, KHU_CUP);
            mapList.put(Integer.valueOf(MAP_CUP), cup);
            PhongRieng.dangKyMap(MAP_CUP);
        } catch (final Throwable e) {
            System.out.println("[PHONG RIENG] khong tao duoc san cup: " + e);
        }
    }

    // ------------------------------------------------------------------ vi tri

    /** Noi nguoi choi dung truoc khi vao phong (de dua ve dung cho). */
    static final class ViTri {
        final int mapId, x, y, khu, phe;

        ViTri(final Char p) {
            this.mapId = p.mapID;
            this.x = p.x;
            this.y = p.y;
            this.khu = p.region;
            this.phe = p.inCountry;
        }

        ViTri(final int mapId, final int x, final int y, final int khu, final int phe) {
            this.mapId = mapId;
            this.x = x;
            this.y = y;
            this.khu = khu;
            this.phe = phe;
        }
    }

    /** Diem di duoc gan (x, y) px tren ban do {@code m} (xoan oc toi 12 o), khong co thi tra lai (x, y). */
    static int[] diemDiDuoc(final Map m, final int x, final int y) {
        if (m == null) {
            return new int[]{x, y};
        }
        for (int r = 0; r <= 12; r++) {
            for (int dx = -r; dx <= r; dx++) {
                for (int dy = -r; dy <= r; dy++) {
                    if (Math.max(Math.abs(dx), Math.abs(dy)) != r) {
                        continue;
                    }
                    final int px = x + dx * 16;
                    final int py = y + dy * 16;
                    if (px > 16 && py > 16 && px < m.w * 16 - 16 && py < m.h * 16 - 16 && m.canMove(px, py)) {
                        return new int[]{px, py};
                    }
                }
            }
        }
        return new int[]{x, y};
    }

    /** Dua {@code p} vao khu {@code khu} cua ban do {@code den} tai (x, y) px, phe {@code phe}. */
    static boolean dua(final Char p, final Map den, final int khu, final int x, final int y, final int phe) {
        if (p == null || den == null) {
            return false;
        }
        try {
            final Map cu = p.map != null ? p.map : den;
            if (p.map != null) {
                cu.playerExit(p);
            }
            p.region = khu;
            cu.move2Map(p, x / 16, y / 16, den.mapId, phe);
            return p.map == den;
        } catch (final Exception e) {
            System.out.println("PhongRiengGame.dua: " + e);
            return false;
        }
    }

    /** Dua ve noi cu (ban do con ton tai), khong thi ve lang khoi dau theo vung. */
    static void veCho(final Char p, final ViTri v) {
        if (p == null) {
            return;
        }
        Map den = v == null ? null : RealController.mapList.get(Integer.valueOf(v.mapId));
        if (den == null || PhongRieng.laMapRieng(den.mapId)) {
            veLang(p);
            return;
        }
        if (p.hp <= 0) {
            p.hp = Math.max(1, p.maxhp / 2);
        }
        dua(p, den, den.nRegion > 0 ? Math.max(0, Math.min(v.khu, den.nRegion - 1)) : 0, v.x, v.y, Math.max(0, v.phe));
    }

    /** Lang ve cua nhan vat (real.LangKhoiDau: lang khoi dau theo vung, nhan vat cu giu lang cu). */
    static int langVe(final Char p) {
        final int vung = Math.max(0, Math.min(2, p.myCountry));
        return LangKhoiDau.bang(p)[vung][0];
    }

    /** O (tile) diem xuat hien o lang {@code lang} (LangKhoiDau.viTri; thieu -> giua lang). */
    static int[] oLang(final int lang) {
        final int[] vt = LangKhoiDau.viTri(lang);
        return vt != null && vt.length >= 2 ? new int[]{vt[0], vt[1]} : new int[]{10, 23};
    }

    static void veLang(final Char p) {
        final int lang = langVe(p);
        final Map den = RealController.mapList.get(Integer.valueOf(lang));
        if (p.hp <= 0) {
            p.hp = Math.max(1, p.maxhp / 2);
        }
        final int[] o = oLang(lang);
        dua(p, den, 0, o[0] * 16 + 8, o[1] * 16 + 8, Math.max(0, p.myCountry));
    }

    /**
     * CharNapDuLieu (dang nhap): nhan vat luu o ban do phong rieng. Pho ban con song + con trong han mat ket noi thi
     * dat lai vao dung khu (dang nhap lai ve phong); con lai ve lang (khong lot vao khu cua phong khac).
     */
    static void khiNapViTri(final Char self) {
        try {
            if (!PhongRieng.laMapRieng(self.mapID)) {
                return;
            }
            final int[] vt = PhoBanGame.viTriKhiDangNhap(self);
            if (vt != null) {
                self.mapID = (short) vt[0];
                self.region = vt[1];
                self.x = vt[2];
                self.y = vt[3];
                self.inCountry = 0;
                return;
            }
            final int lang = langVe(self);
            final int[] o = oLang(lang);
            self.mapID = (short) lang;
            self.region = 0;
            self.x = o[0] * 16 + 8;
            self.y = o[1] * 16 + 8;
        } catch (final Exception e) {
            System.out.println("PhongRiengGame.khiNapViTri: " + e);
        }
    }

    // ------------------------------------------------------------------ nhip + lenh

    private static volatile boolean dangChay;

    /** TeamServer: luong nhip 1 giay cho pho ban + cup (tach khoi vong bao tri). */
    /** Ngu canh PvP cua tran cup (BanHuongGame): o san cup VA dang la nguoi thi dau cua mot tran chua ket thuc. */
    public static final java.util.function.Predicate<Char> NGU_CANH_CUP = new java.util.function.Predicate<Char>() {
        public boolean test(final Char p) {
            return p != null && p.mapID == MAP_CUP && CupGame.dangThiDau(p);
        }
    };

    public static void khoiDong() {
        if (dangChay) {
            return;
        }
        dangChay = true;
        // 0.9.8 (pvp-keo): tran Cup PvP tuan (san 4382, dang thi dau) la ngu canh PvP -> ngam tay, khong tu dong / tu nham.
        // Sat thuong van chuan hoa theo loai cup (10..80 phan nghin HP) vi dan ngam tay di qua Map.doAttackPlayer ->
        // Region.doAttackPlayer (nhanh cupTran -> CupGame.satThuong).
        BanHuongGame.dangKyNguCanh(NGU_CANH_CUP);
        final Thread t = new Thread(new Runnable() {
            public void run() {
                while (true) {
                    final long bay = com.htbb.pokemon.DongHo.bay();
                    try {
                        PhoBanGame.nhip(bay);
                    } catch (final Throwable e) {
                        System.out.println("[PHONG RIENG] pho ban: " + e);
                    }
                    try {
                        CupGame.nhip(bay);
                    } catch (final Throwable e) {
                        System.out.println("[PHONG RIENG] cup: " + e);
                    }
                    try {
                        Thread.sleep(1000L);
                    } catch (final InterruptedException e) {
                        return;
                    }
                }
            }
        }, "PhongRieng");
        t.setDaemon(true);
        t.start();
        System.out.println("PHONG RIENG -> pho ban " + (phoBan != null ? MAP_PHO_BAN : -1) + ", san cup " + (cup != null ? MAP_CUP : -1));
    }

    /** Lenh C-&gt;S -112 (client 908): byte 0 = "Rời" (pho ban) / "Bỏ cuộc" (tran cup). */
    static void xuLy(final Char p, final Message m) {
        try {
            final int lam = m.dis.readByte();
            if (lam == 0) {
                moMenuTrongPhong(p);
            }
        } catch (final Exception ignored) {
        }
    }

    /** Lenh "khu" / cham the trong phong rieng: hoi lai truoc khi roi (menu So tay, khong roi ngay khi cham nham). */
    static void moMenuTrongPhong(final Char p) {
        if (p == null) {
            return;
        }
        if (p.mapID == MAP_PHO_BAN && PhoBanGame.phongCua(p.charDBID) != null) {
            p.sendMessage(MessageCreator.createMsgMenu(new String[]{"Rời phó bản (lượt hôm nay đã dùng)"}, SoTayLauDai.NPC_ID, PhoBanGame.MENU_TRONG));
        } else if (p.mapID == MAP_CUP && CupGame.dangThiDau(p.charDBID)) {
            p.sendMessage(MessageCreator.createMsgMenu(new String[]{"Bỏ cuộc trận này (tính thua)"}, SoTayLauDai.NPC_ID, CupGame.MENU_BO));
        } else if (laMapRiengCua(p)) {
            veLang(p); // ket o phong rieng ma khong con phong / tran (vd sau loi): ve lang
        }
    }

    static boolean laMapRiengCua(final Char p) {
        return PhongRieng.laMapRieng(p.mapID);
    }

    // ------------------------------------------------------------------ goi -112

    static boolean coThe(final Char p) {
        return p != null && p.getSession() != null && p.getSession().banClient >= PhongRieng.BAN_CLIENT;
    }

    /** Gui the trang thai phong (client &gt;= 908). Client cu: bo qua (da co thong bao chu). */
    static void guiThe(final Char p, final byte loai, final String tieuDe, final String dong1, final String dong2,
            final int giay, final int phanTram) {
        if (!coThe(p)) {
            return;
        }
        try {
            final Message m = new Message(PhongRieng.LENH);
            PhongRieng.viet(m.dos, loai, com.htbb.pokemon.ChuPokemon.doi(tieuDe), com.htbb.pokemon.ChuPokemon.doi(dong1),
                    com.htbb.pokemon.ChuPokemon.doi(dong2), giay, phanTram);
            p.sendMessage(m);
            m.cleanup();
        } catch (final Exception ignored) {
        }
    }

    static void anThe(final Char p) {
        guiThe(p, PhongRieng.AN, "", "", "", -1, -1);
    }

    /** Thong bao chu (the nhanh client &gt;= 902, hop thoai cho client cu). */
    static void bao(final Char p, final String chinh, final String phu) {
        com.htbb.thongbao.ThongBao.gui(p, com.htbb.thongbao.ThongBao.TIN, chinh, phu,
                com.htbb.thongbao.ThongBao.HINH_BIEU_TUONG, com.htbb.thongbao.ThongBao.BT_SU_KIEN);
    }

    static void loi(final Char p, final String chinh) {
        com.htbb.thongbao.ThongBao.loi(p, chinh, "");
    }

    static void hiem(final Char p, final String chinh, final String phu) {
        com.htbb.thongbao.ThongBao.gui(p, com.htbb.thongbao.ThongBao.HIEM, chinh, phu,
                com.htbb.thongbao.ThongBao.HINH_BIEU_TUONG, com.htbb.thongbao.ThongBao.BT_SU_KIEN);
    }

    static Char online(final int charDBID) {
        try {
            return CharManager.instance.getCharByCharDbID(charDBID);
        } catch (final Exception e) {
            return null;
        }
    }

    /** Dang ban viec khac khong vao phong duoc: dang o phong rieng, dang giao dich. */
    static boolean dangBan(final Char p) {
        try {
            return PhongRieng.laMapRieng(p.mapID) || (p.userTrade != null && p.userTrade.size() > 0);
        } catch (final Exception e) {
            return true;
        }
    }

    /**
     * Trao danh hieu co han vao kho danh hieu cua nhan vat (Char.allDanhHieu, tob_danh_hieu - chung he danh hieu
     * 0.9.7) MA KHONG tu deo (khong doi nhan dang deo cua nguoi choi). Da co thi gia han.
     */
    static void traoDanhHieu(final Char p, final int id, final long phut) {
        if (p == null || id <= 0 || p.allDanhHieu == null) {
            return;
        }
        try {
            synchronized (p.allDanhHieu) {
                for (int i = 0; i < p.allDanhHieu.size(); i++) {
                    final data.DanhHieu d = p.allDanhHieu.get(i);
                    if (d.ideff == id) {
                        final long moi = phut > 0 ? System.currentTimeMillis() + phut * 60000L : 0L;
                        if (d.timeExpire != 0L && (moi == 0L || moi > d.timeExpire)) {
                            d.timeExpire = moi;
                        }
                        data.Database.instance.addCharDanhHieu(p);
                        return;
                    }
                }
                final data.DanhHieu d = new data.DanhHieu();
                d.ideff = id;
                d.timeExpire = phut > 0 ? System.currentTimeMillis() + phut * 60000L : 0L;
                p.allDanhHieu.add(d);
            }
            data.Database.instance.addCharDanhHieu(p);
        } catch (final Exception e) {
            System.out.println("PhongRiengGame.traoDanhHieu: " + e);
        }
    }
}
