package real;

import com.htbb.pokemon.GiaiDoanBoss;
import com.htbb.pokemon.HoSoLauDai;
import com.htbb.pokemon.LauDai;
import com.htbb.pokemon.NhiemVuLauDai;
import data.Database;
import data.DatabaseLauDai;
import java.util.ArrayList;
import java.util.List;

/**
 * 16 thu linh Gym (GD2b, docs/thiet-ke-game.md; data_noi_dung_gym + data_noi_dung_roi nguon
 * gym / gym_tuan). Vao tu So tay huan luyen -> "Thách đấu Gym" (hoac muc tat dau menu Quầy đổi thưởng / hop 95 khi dang lam
 * buoc ha Gym - cung thachDauStt): trieu hoi boss tai cho, 1 dau 1 (chi
 * nguoi thach dau gay duoc sat thuong), no sau no_sau_phut phut (GiaiDoanBoss), het 10 phut chua ha
 * thi thua. Lan dau: huy hieu (bit trong ho so) + 1 vu khi Hoan my dung dong. Danh lai: 1 lan / tuan,
 * 3 Huy chuong Gym (item 1039, khoa).
 */
public final class GymLauDai {

    static final long THOI_GIAN_MS = 10L * 60L * 1000L;

    /** {stt, ma, huyHieu, cap, template, tenBoss, hp, tc, thu, noPhut, thuongDau, thuongTuan}. */
    static volatile List<Object[]> GYM = new ArrayList<Object[]>();

    private GymLauDai() {
    }

    static void nap() {
        GYM = DatabaseLauDai.napGym();
    }

    static int so(final Object o) {
        return o instanceof Number ? ((Number) o).intValue() : 0;
    }

    static Object[] gym(final int stt) {
        for (final Object[] g : GYM) {
            if (so(g[0]) == stt) {
                return g;
            }
        }
        return null;
    }

    public static boolean coHuyHieu(final HoSoLauDai h, final int stt) {
        return h != null && stt >= 1 && stt <= 30 && (h.gymHuyHieu & (1 << (stt - 1))) != 0;
    }

    /** charDBID -> stt cac dong menu "Thách đấu Gym" vua gui (Gym cua buoc dang lam len dau). */
    private static final java.util.concurrent.ConcurrentHashMap<Integer, int[]> THU_TU = new java.util.concurrent.ConcurrentHashMap<Integer, int[]>();

    static String tenBoss(final Object[] g) {
        return g == null || g[5] == null ? "" : String.valueOf(g[5]);
    }

    /** Thu tu stt menu Gym: Gym {@code dau} (buoc dang lam, 0 = khong) len dau, con lai theo stt. */
    static int[] thuTu(final List<Object[]> ds, final int dau) {
        final int[] kq = new int[ds.size()];
        int i = 0;
        for (final Object[] g : ds) {
            if (so(g[0]) == dau) {
                kq[i++] = dau;
            }
        }
        for (final Object[] g : ds) {
            if (so(g[0]) != dau) {
                kq[i++] = so(g[0]);
            }
        }
        return kq;
    }

    static void moMenu(final Char p) {
        final HoSoLauDai h = LauDaiNhanVat.lay(p);
        final List<Object[]> ds = GYM;
        if (h == null || ds.isEmpty()) {
            SoTayLauDai.bao(p, "Chưa có dữ liệu Gym.");
            return;
        }
        final Object[] dang = buocGym(p);
        final int[] tt = thuTu(ds, dang == null ? 0 : so(dang[0]));
        final String[] menu = new String[tt.length];
        for (int i = 0; i < tt.length; i++) {
            final Object[] g = gym(tt[i]);
            menu[i] = (dang != null && i == 0 ? "» " : "") + com.htbb.pokemon.DuongMenu.nhanGym(tenBoss(g), so(g[3]), coHuyHieu(h, tt[i]));
        }
        THU_TU.put(Integer.valueOf(p.charDBID), tt);
        p.sendMessage(MessageCreator.createMsgMenu(menu, SoTayLauDai.NPC_ID, SoTayLauDai.MENU_GYM));
    }

    /** So tay -> Thách đấu Gym, dong thu {@code i} (thu tu da gui; chua mo menu = theo stt). */
    static String chon(final Char p, final int i) {
        int[] tt = THU_TU.get(Integer.valueOf(p.charDBID));
        if (tt == null) {
            tt = thuTu(GYM, 0);
        }
        if (i < 0 || i >= tt.length) {
            return "Gym không hợp lệ.";
        }
        return thachDauStt(p, tt[i]);
    }

    /** Chon Gym thu {@code i} theo stt tang dan (giu cho goi cu). */
    static String thachDau(final Char p, final int i) {
        final List<Object[]> ds = GYM;
        return i < 0 || i >= ds.size() ? "Gym không hợp lệ." : thachDauStt(p, so(ds.get(i)[0]));
    }

    /**
     * Thach dau Gym {@code stt}: MOT cua cho moi duong (So tay -> Thách đấu Gym, muc tat o Quầy đổi thưởng, hop 95 cua
     * buoc nhiem vu) - cung kiem cong tac, cap, dang danh, gioi han tuan. Boss trieu hoi NGAY CANH nguoi choi o ban do dang
     * dung (khong dich chuyen), 1 dau 1, 10 phut.
     */
    static String thachDauStt(final Char p, final int stt) {
        if (!com.htbb.cauhinh.CongTac.gym()) {
            return "Gym đang tạm đóng. Vui lòng quay lại sau."; // cau hinh trung tam: gym.bat=0
        }
        final Object[] g = gym(stt);
        if (g == null) {
            return "Gym không hợp lệ.";
        }
        final HoSoLauDai h = LauDaiNhanVat.lay(p);
        if (h == null) {
            return "Tạm thời không thách đấu được.";
        }
        if (p.lvDetail.lv < so(g[3])) {
            return "Cần đạt cấp " + so(g[3]) + ".";
        }
        final String dang = dangDanh(p);
        if (dang != null) {
            return dang; // mot tran / nhan vat: bam lai (hai cua, bam dup) chi chi ve boss dang co
        }
        if (!banDoDuocTrieu(p.map)) {
            return "Không thể thách đấu Gym ở khu vực này. Hãy ra bản đồ thường hoặc về làng rồi thử lại.";
        }
        final int tuan = NhiemVuLauDai.tuan(com.htbb.pokemon.DongHo.bay());
        synchronized (h) {
            if (h.gymTuan != tuan) {
                h.gymTuan = tuan;
                h.gymTuanBit = 0;
            }
            if (coHuyHieu(h, stt) && (h.gymTuanBit & (1 << (stt - 1))) != 0) {
                return "Tuần này đã đánh lại Gym này rồi.";
            }
        }
        final MonsterTemplate t = Map.monsterTemplates.get(Integer.valueOf(so(g[4])));
        if (t == null || p.map == null) {
            return "Không triệu hồi được trưởng Gym.";
        }
        final boolean[] moi = {false};
        final int hp = com.htbb.pokemon.GiaiDoanBoss.hpGymTheoHe(so(g[6]), heTotNhat(p, t),
                com.htbb.pokemon.LauDai.so("gym.he_ra_toi_thieu", com.htbb.pokemon.GiaiDoanBoss.GYM_HE_RA_TOI_THIEU));
        final Monster b = BossLauDai.moGym(p, stt, t, so(g[3]), hp, so(g[7]), so(g[8]), THOI_GIAN_MS, moi);
        if (b == null) {
            return "Không triệu hồi được trưởng Gym.";
        }
        if (!moi[0]) {
            final String d = dangDanh(p); // luong khac vua mo tran cung luc (bam dup): khong trieu hoi them
            return d != null ? d : "Bạn đang thách đấu một Gym.";
        }
        try {
            Database.instance.saveOrtherLog("", p.charname, "thach dau gym " + stt, "laudai");
        } catch (final Exception ignored) {
        }
        return g[5] + " xuất hiện ngay cạnh bạn! Hãy đánh tay trong 10 phút (nổi giận sau " + so(g[9]) + " phút).";
    }

    /**
     * He so khac he (%) TOT NHAT cua cac chieu danh (o 0..3, da hoc) cua {@code p} vao he cua mau boss {@code t}; 0 = khong
     * biet. Dung de ha HP boss cho dong khong co chieu nao danh duoc no (GiaiDoanBoss.hpGymTheoHe).
     */
    static int heTotNhat(final Char p, final MonsterTemplate t) {
        int tot = 0;
        try {
            for (int k = 0; k < 4 && k < p.skill.length; k++) {
                if (p.skill[k] <= 0) {
                    continue;
                }
                final int hc = HocChieuGame.heChieu(p, k);
                if (hc < 0) {
                    continue;
                }
                tot = Math.max(tot, com.htbb.pokemon.HePokemon.heSoTrongGame(hc, t.he, t.he2));
            }
        } catch (final Exception ignored) {
            return 0;
        }
        return tot;
    }

    /**
     * Ban do trieu hoi duoc boss Gym: ban do thuong (lop Map) va Cao nguyen (MapTown) - khong trong dau truong, lien dau,
     * pho ban, ban do bang, nui chau bau... (chong lam dung, va cac ban do do co vong lap quai rieng).
     */
    static boolean banDoDuocTrieu(final Map m) {
        return m != null && (m.getClass() == Map.class || m instanceof MapTown) && !m.isMapLienDau();
    }

    /** Dang co tran Gym: cau chi ve boss dang co (ten, cach bao xa, giay con lai); null = khong co. */
    static String dangDanh(final Char p) {
        final Monster m = BossLauDai.gymCua(p);
        if (m == null) {
            return null;
        }
        final Object[] g = gym(BossLauDai.gymDangDanh(p));
        final long giay = BossLauDai.giayConGym(p);
        final String ten = g == null ? "Trưởng Gym" : tenBoss(g);
        final String o;
        if (m.map == p.map && m.region == p.region) {
            final int dx = (m.x - p.x) / 16;
            final int dy = (m.y - p.y) / 16;
            o = "ngay gần bạn (cách " + Math.max(Math.abs(dx), Math.abs(dy)) + " ô)";
        } else {
            o = "ở bản đồ khác";
        }
        return "Bạn đang thách đấu " + ten + " - " + o + ", còn " + (giay / 60) + " phút " + (giay % 60)
                + " giây. Hạ boss này trước, không triệu hồi thêm.";
    }

    /** Sau khi ha Gym: nhan thuong buoc nhiem vu o moi noi (bao loi 24/10/2026 "ve lang mat thoi gian"). */
    static final String SAU_THANG = "Trưởng Gym đã rời bản đồ. Nhận thưởng nhiệm vụ: "
            + com.htbb.pokemon.DuongMenu.NHAN_THUONG_MOI_NOI;

    /**
     * "Về làng miễn phí trong Sổ tay huấn luyện (còn N lượt, +1 nhờ thắng Gym). Hoặc Dây thoát hiểm (1000 xu, cửa hàng
     * thuốc) / Vé dịch chuyển (Kecleon ...)." - gia day doc data_potion 19; {@code ve} = {con lai, vua cong}
     * (VeLangMienPhiGame.khiThangGym).
     */
    static String veLangNhanh(final int[] ve) {
        int gia = 0;
        try {
            gia = Map.potionTemplates2.get(19).getPrice();
        } catch (final Exception ignored) {
        }
        return veLangMienPhi(ve) + " Hoặc Dây thoát hiểm" + (gia > 0 ? " (" + gia + " xu, cửa hàng thuốc)" : "") + " / "
                + com.htbb.pokemon.VeDichChuyen.TEN + " (Kecleon, về làng / thành phố đã mở).";
    }

    /** "Về làng miễn phí trong Sổ tay huấn luyện (còn N lượt, +1 nhờ thắng Gym)." */
    static String veLangMienPhi(final int[] ve) {
        return com.htbb.pokemon.VeLangMienPhi.TEN + " trong Sổ tay huấn luyện (còn " + ve[0] + " lượt"
                + (ve[1] > 0 ? ", +" + ve[1] + " nhờ thắng Gym" : "") + ").";
    }

    /** Tran Gym bi huy (BossLauDai.donGym / huyGymCua): bao chu (neu con online). Thua / huy deu duoc thach dau lai ngay. */
    static void baoHuy(final Char p, final int stt, final int ly) {
        if (p == null || p.exit) {
            return;
        }
        final Object[] g = gym(stt);
        final String ten = g == null ? "Trưởng Gym" : tenBoss(g);
        final String vi = ly == BossLauDai.HUY_HET_GIO ? "Hết 10 phút: " + ten + " đã rời đi."
                : ten + " đã rời đi vì bạn rời khỏi bản đồ thách đấu.";
        try {
            LauDaiNhanVat.bao(p, vi + " Bạn có thể thách đấu lại bất cứ lúc nào (Sổ tay huấn luyện → Thách đấu Gym).");
        } catch (final Exception ignored) {
        }
    }

    // ------------------------------------------------------------------ duong tat (buoc nhiem vu ha Gym)

    /** Dong Gym cua muc tieu ha_gym {@code id} (stt "13" hoac ma "C06.6"); null = khong co. */
    static Object[] gymCuaMucTieu(final String id) {
        if (id == null) {
            return null;
        }
        final String x = id.trim();
        for (final Object[] g : GYM) {
            if (String.valueOf(so(g[0])).equals(x) || x.equals(g[1])) {
                return g;
            }
        }
        return null;
    }

    /** Gym cua buoc ha_gym dang lam (da nhan, chua xong) dau tien theo ma; null = khong co / tat. */
    static Object[] buocGym(final Char p) {
        try {
            if (!bat() || p == null || GYM.isEmpty()) {
                return null;
            }
            final com.htbb.pokemon.NhiemVuMau m = NhiemVuEngine.buocGymDangLam(p);
            return m == null ? null : gymCuaMucTieu(m.mucTieuId);
        } catch (final Exception e) {
            System.out.println("GymLauDai.buocGym: " + e);
            return null;
        }
    }

    /** Ten boss Gym cua buoc dang lam ("Trưởng Gym Pewter: Golem"), null = khong (MenuDoiThuong: muc tat). */
    static String tenBossBuoc(final Char p) {
        final Object[] g = buocGym(p);
        return g == null ? null : tenBoss(g);
    }

    /** Hop 95 "Thách đấu Trưởng Gym … ngay?" (truoc: dong mo dau, co the rong). Đồng ý -> {@link #thachDauStt}. */
    static void hoi(final Char p, final Object[] g, final String truoc) {
        KhoaBanDoGame.hoiGym(p, so(g[0]), com.htbb.pokemon.DuongMenu.hoiGym(truoc, tenBoss(g)));
    }

    /**
     * Buoc {@code ma} la ha_gym dang lam: hien hop hoi thach dau (kem {@code truoc}) thay cho thong bao thuong, true. Khong
     * phai (hoac Gym khong co du lieu) -> false, nguoi goi bao nhu cu.
     */
    static boolean hoiNeuGym(final Char p, final com.htbb.pokemon.NhiemVuMau m, final long[] v, final String truoc) {
        if (m == null || v == null || v[1] != 0 || !"ha_gym".equals(m.kieu) || !bat()) {
            return false;
        }
        final Object[] g = gymCuaMucTieu(m.mucTieuId);
        if (g == null) {
            return false;
        }
        hoi(p, g, truoc);
        return true;
    }

    /** Roi game: bo thu tu menu da gui. */
    static void khiThoat(final Char p) {
        if (p != null) {
            THU_TU.remove(Integer.valueOf(p.charDBID));
        }
    }

    /** BossLauDai.khiChet: boss Gym {@code stt} cua {@code idChar} bi ha. */
    static void khiHa(final int stt, final int idChar) {
        final Char p = BossLauDai.timChar(idChar);
        final Object[] g = gym(stt);
        if (p == null || g == null) {
            return;
        }
        final HoSoLauDai h = LauDaiNhanVat.lay(p);
        if (h == null) {
            return;
        }
        final boolean lanDau;
        synchronized (h) {
            lanDau = !coHuyHieu(h, stt);
            if (lanDau) {
                h.gymHuyHieu |= 1 << (stt - 1);
            } else {
                if ((h.gymTuanBit & (1 << (stt - 1))) != 0) {
                    return;
                }
                h.gymTuanBit |= 1 << (stt - 1);
            }
            h.ban = true; // danh dau TRUOC, phat SAU
        }
        NhiemVuEngine.suKien(p, "ha_gym", (String) g[1], 1);
        NhiemVuEngine.suKien(p, "ha_gym", String.valueOf(stt), 1);
        final int[] ve = VeLangMienPhiGame.khiThangGym(p, h); // +luot Ve lang mien phi moi tran thang
        final String quaVe = VeLangMienPhiGame.quaHuyHieuDau(p, h); // huy hieu dau tien: +1 Ve dich chuyen (1 lan)
        if (lanDau) {
            vuKhiHoanMy(p, (String) g[1]);
            LauDaiNhanVat.bao(p, "Chiến thắng! Nhận " + g[2] + ". " + SAU_THANG + " " + veLangNhanh(ve));
        } else {
            for (final Object[] r : NoiDungLauDai.ROI) {
                if ("gym_tuan".equals(r[0]) && g[1].equals(r[1])) {
                    NoiDungLauDai.phat(p, (String) r[2], ((Integer) r[3]).intValue(), ((Integer) r[5]).intValue(), true);
                }
            }
            LauDaiNhanVat.bao(p, "Đánh lại Gym thành công: nhận Huy chương Gym. " + veLangMienPhi(ve));
        }
        if (quaVe != null) {
            LauDaiNhanVat.bao(p, quaVe);
        }
        try {
            Database.instance.saveOrtherLog("", p.charname, "ha gym " + stt + (lanDau ? " lan dau" : " tuan"), "laudai");
        } catch (final Exception ignored) {
        }
    }

    /** Vu khi Hoan my DUNG DONG nhan vat trong cac dong roi "gym" cua ma nhiem vu. */
    static void vuKhiHoanMy(final Char p, final String ma) {
        final List<Integer> ds = new ArrayList<Integer>();
        for (final Object[] r : NoiDungLauDai.ROI) {
            if ("gym".equals(r[0]) && ma.equals(r[1]) && "item".equals(r[2])) {
                ds.add((Integer) r[3]);
            }
        }
        if (ds.isEmpty()) {
            return;
        }
        // 5 dong theo thu tu Than lua / Hat giong / Khan lua / Nuoc than / Nam cham = class 0..4
        final int id = ds.get(Math.max(0, Math.min(ds.size() - 1, p.charClass))).intValue();
        try {
            final Item it = Map.dropItemByIdTemplate(p.inCountry, id);
            if (it == null || p.isFullInventory()) {
                LauDaiNhanVat.bao(p, "Hành trang đầy, không nhận được đồ cầm tay Hoàn mỹ.");
                return;
            }
            it.id = p.getIDItem();
            it.owner = p.charDBID;
            it.colorName = Item.COLOR_GREEN; // Hoan my (Item.java: ten mau 1 = "Hoàn mỹ")
            it.lock = 1;
            p.iItems.add(it);
            p.sendMessage(MessageCreator.createCharInventoryMessage(p, 0));
        } catch (final Exception e) {
            System.out.println("GymLauDai.vuKhiHoanMy: " + e);
        }
    }

    /** No sau (phut) cua Gym - dung GiaiDoanBoss.NO_GYM_MS mac dinh. */
    static long noMs() {
        return GiaiDoanBoss.NO_GYM_MS;
    }

    static boolean bat() {
        return LauDai.bat();
    }
}
