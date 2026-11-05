package real;

import com.htbb.pokemon.CheTaoLogic;
import com.htbb.pokemon.HePokemon;
import com.htbb.pokemon.LauDai;
import com.htbb.pokemon.NgocHe;
import data.DatabaseBoNgoc;
import io.Message;
import java.io.DataOutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Hashtable;
import java.util.List;

/**
 * CHE TAO o Machamp tho ren - phan GAME (thiet ke can bang 4.4; logic thuan com.htbb.pokemon.CheTaoLogic). Cong thuc tu
 * data_noi_dung_che_tao (+ cot loi_che_tao, bat), bac hiem thanh pham tu data_che_tao_bac (BoNgocGame.DL), pity Su thi
 * luu o tui ngoc (tob_ho_so_bo_ngoc "cp").
 *
 * <p>Luong: client mo man Che tao (goi -120 kieu 0) -&gt; {@link #guiDanhSach} (kieu 2: moi cong thuc + co / can) -&gt; nguoi
 * choi chon -&gt; -120 kieu 1 -&gt; {@link #cheTao}: kiem lai MOI dieu kien o may chu, tru xu + da + nguyen lieu trong MOT
 * khoi (Char.truNeuDu), quay bac, tao mon CHUA MAC (ban duoc), gui ket qua (kieu 3) + thong bao.
 */
public final class CheTaoGame {

    public static final String K_BAT = "che_tao.bat";
    public static final String K_PITY = "che_tao.pity";
    /** Ten mau vat pham "Loi che tao" (data_item type 28) - chua co thi cong thuc can loi bi khoa. */
    public static final String TEN_LOI = "Lõi chế tạo";

    private CheTaoGame() {
    }

    static boolean bat() {
        return BoNgocGame.bat() && LauDai.so(K_BAT, 1) != 0;
    }

    /** Doc cong thuc tu CSDL (null neu loi: giu ban cu). Cot loi_che_tao / bat co the chua co (CSDL cu). */
    static List<CheTaoLogic.CongThuc> napCongThuc() {
        List<Object[]> ds = DatabaseBoNgoc.docBang("SELECT thanh_pham, ten, bac, cap, da, nguyen_lieu_phu, so_nl_phu,"
                + " anh_xa_nl_phu, xu, loi_che_tao, bat FROM data_noi_dung_che_tao ORDER BY bac, thanh_pham", 11);
        if (ds == null) {
            ds = DatabaseBoNgoc.docBang("SELECT thanh_pham, ten, bac, cap, da, nguyen_lieu_phu, so_nl_phu, anh_xa_nl_phu, xu,"
                    + " 0, 1 FROM data_noi_dung_che_tao ORDER BY bac, thanh_pham", 11);
        }
        if (ds == null) {
            return null;
        }
        final List<CheTaoLogic.CongThuc> v = new ArrayList<CheTaoLogic.CongThuc>();
        for (final Object[] r : ds) {
            final int id = idTuAnhXa(DatabaseBoNgoc.chu(r[0]));
            if (id <= 0) {
                continue;
            }
            final ItemTemplates t = mau(id);
            final boolean phienDa = t != null && t.type == 12;
            v.add(new CheTaoLogic.CongThuc(id, DatabaseBoNgoc.chu(r[1]), DatabaseBoNgoc.so(r[2]), DatabaseBoNgoc.so(r[3]),
                    DatabaseBoNgoc.so(r[4]), DatabaseBoNgoc.chu(r[5]), idTuAnhXa(DatabaseBoNgoc.chu(r[7])), DatabaseBoNgoc.so(r[6]),
                    DatabaseBoNgoc.soDai(r[8]), DatabaseBoNgoc.so(r[9]), phienDa, DatabaseBoNgoc.so(r[10]) != 0 && t != null));
        }
        return Collections.unmodifiableList(v);
    }

    /** "item:126" -&gt; 126; khac -&gt; 0. */
    static int idTuAnhXa(final String s) {
        if (s == null || !s.startsWith("item:")) {
            return 0;
        }
        try {
            return Integer.parseInt(s.substring(5).trim());
        } catch (final NumberFormatException e) {
            return 0;
        }
    }

    static ItemTemplates mau(final int id) {
        try {
            return Map.itemTemplates.get(5).get(Integer.valueOf(id));
        } catch (final Exception e) {
            return null;
        }
    }

    /** Id mau "Loi che tao" (type 28), -1 neu may chu chua co. */
    static int idLoi() {
        try {
            for (final ItemTemplates t : Map.itemTemplates.get(5).values()) {
                if (t != null && t.type == 28 && TEN_LOI.equals(t.name == null ? "" : t.name.trim())) {
                    return t.id;
                }
            }
        } catch (final Exception ignored) {
        }
        return -1;
    }

    static CheTaoLogic.CongThuc congThuc(final int idTemplate) {
        for (final CheTaoLogic.CongThuc c : BoNgocGame.DL.congThuc) {
            if (c.idTemplate == idTemplate) {
                return c;
            }
        }
        return null;
    }

    static int pity() {
        return Math.max(0, Math.min(100, LauDai.so(K_PITY, CheTaoLogic.PITY_MAC_DINH)));
    }

    static int soDa(final Char p) {
        if (p.listGemitem == null || p.listGemitem.length <= BoNgocGame.ID_DA) {
            return 0;
        }
        final int sell = p.listGemitemSell != null && p.listGemitemSell.length > BoNgocGame.ID_DA ? p.listGemitemSell[BoNgocGame.ID_DA] : 0;
        return Math.max(0, p.listGemitem[BoNgocGame.ID_DA] - sell) + p.listGemitemLock[BoNgocGame.ID_DA];
    }

    // ------------------------------------------------------------------ goi tin

    static void xuLy(final Char p, final byte kieu, final Message msg) throws java.io.IOException {
        if (!BoNgocGame.oMachamp(p)) {
            return;
        }
        if (kieu == BoNgocGame.C_XIN_CHE_TAO) {
            guiDanhSach(p);
            return;
        }
        final short id = msg.dis.readShort();
        int he = -1;
        int kieuDa = 0;
        try {
            he = msg.dis.readByte();
            kieuDa = msg.dis.readByte();
        } catch (final Exception ignored) {
        }
        cheTao(p, id, he, kieuDa);
    }

    /** Kieu 2: tieu de, bac hiem (ten + phan nghin), pity, danh sach cong thuc (co / can). */
    static void guiDanhSach(final Char p) {
        if (!BoNgocGame.coClient(p)) {
            return;
        }
        if (!bat()) {
            BoNgocGame.ketQua(p, false, "Chế tạo đang tạm khoá.");
            return;
        }
        try {
            final BoNgocGame.DuLieu d = BoNgocGame.DL;
            final NgocHe.Tui tui = BoNgocGame.tui(p);
            final Message m = BoNgocGame.moi(BoNgocGame.S_CHE_TAO);
            final DataOutputStream o = m.dos;
            o.writeByte(d.bacHiem.size());
            for (final CheTaoLogic.BacHiem b : d.bacHiem) {
                o.writeByte(b.ma);
                o.writeUTF(b.ten);
                o.writeByte(b.colorName);
                o.writeShort(b.phanNghin);
                o.writeByte(b.soLo);
            }
            o.writeByte(tui == null ? 0 : Math.min(100, tui.cheTaoPity));
            o.writeByte(pity());
            final int capNv = p.lvDetail != null ? p.lvDetail.lv : 1;
            final int da = soDa(p);
            final int idLoi = idLoi();
            final int loi = idLoi > 0 ? VatPhamDungLauDai.demItem(p, idLoi) : 0;
            o.writeInt(da);
            final List<CheTaoLogic.CongThuc> ds = d.congThuc;
            final int n = Math.min(100, ds.size());
            o.writeByte(n);
            for (int i = 0; i < n; i++) {
                final CheTaoLogic.CongThuc c = ds.get(i);
                final ItemTemplates t = mau(c.idTemplate);
                final int nl = c.idNlPhu > 0 ? VatPhamDungLauDai.demItem(p, c.idNlPhu) : 0;
                final boolean khoaLoi = c.loi > 0 && idLoi <= 0;
                final String thieu = khoaLoi ? "Chưa có Lõi chế tạo trên máy chủ." : CheTaoLogic.thieu(c, capNv, p.getxu(), da, nl, loi);
                o.writeShort(c.idTemplate);
                o.writeShort(t != null ? t.idIcon : -1);
                o.writeUTF(c.ten);
                o.writeByte(c.bac);
                o.writeByte(c.cap);
                o.writeByte(c.da);
                o.writeUTF(c.nlPhu);
                o.writeByte(c.soNlPhu);
                o.writeShort(Math.min(Short.MAX_VALUE, nl));
                o.writeInt((int) Math.min(Integer.MAX_VALUE, c.xu));
                o.writeByte(c.loi);
                o.writeByte(c.phienDa ? 1 : 0);
                o.writeUTF(thieu == null ? "" : thieu);
            }
            p.sendMessage(m);
        } catch (final Exception e) {
            System.out.println("CheTaoGame.guiDanhSach: " + e);
        }
    }

    // ------------------------------------------------------------------ che tao

    /**
     * Che mot mon. {@code he} 0..17 + {@code kieuDa} 0 (ST he) / 1 (giam ST nhan) chi dung cho Phien da he; ngoai khoang
     * thi he cua chinh nguoi choi, ST he.
     */
    static void cheTao(final Char p, final int idTemplate, final int he, final int kieuDa) {
        if (!bat()) {
            BoNgocGame.ketQua(p, false, "Chế tạo đang tạm khoá.");
            return;
        }
        final CheTaoLogic.CongThuc c = congThuc(idTemplate);
        final ItemTemplates t = c == null ? null : mau(c.idTemplate);
        if (c == null || t == null) {
            BoNgocGame.ketQua(p, false, "Không có công thức này.");
            return;
        }
        if (p.isFullInventory()) {
            BoNgocGame.ketQua(p, false, "Hành trang đã đầy.");
            return;
        }
        final NgocHe.Tui tui = BoNgocGame.tui(p);
        if (tui == null) {
            BoNgocGame.ketQua(p, false, "Hồ sơ chế tạo tạm thời không đọc được, thử lại sau.");
            return;
        }
        final int idLoi = c.loi > 0 ? idLoi() : -1;
        if (c.loi > 0 && idLoi <= 0) {
            BoNgocGame.ketQua(p, false, "Công thức cần Lõi chế tạo (chưa mở).");
            return;
        }
        final int capNv = p.lvDetail != null ? p.lvDetail.lv : 1;
        final boolean[] khoa = {false};
        final String[] thieu = {null};
        synchronized (p) {
            // 05/11/2026 (bao loi "che tao xong vao kho khong thay ma xu cung tru roi"): NGUYEN TU - kiem tui + TAO MON
            // TRUOC, chi tru xu / da / nguyen lieu khi da cam mon trong tay; ban cu tru truoc roi moi taoMon, taoMon tra
            // null (mau loi / mau id 2 / ngoai le) -> "Lỗi tạo món" ma xu + da + nguyen lieu da mat. Kiem tui lai TRONG
            // khoa (hai goi che tao cung luc / nhat do giua chung khong lot qua o cuoi).
            if (p.isFullInventory()) {
                BoNgocGame.ketQua(p, false, "Hành trang đã đầy, hãy dọn 1 ô rồi chế lại (chưa trừ xu).");
                return;
            }
            final int truoc = tui.cheTaoPity;
            final CheTaoLogic.BacHiem bac = CheTaoLogic.quay(BoNgocGame.DL.bacHiem, Map.r.nextInt(1000), truoc, pity());
            final Item it;
            try {
                it = taoMon(p, t, bac, he, kieuDa);
            } catch (final RuntimeException e) {
                System.out.println("CheTaoGame.taoMon " + c.idTemplate + ": " + e);
                BoNgocGame.ketQua(p, false, "Lỗi tạo món (chưa trừ xu), hãy báo quản trị.");
                return;
            }
            if (it == null) {
                BoNgocGame.ketQua(p, false, "Lỗi tạo món (chưa trừ xu), hãy báo quản trị.");
                return;
            }
            final boolean du = p.truNeuDu(() -> {
                final int nl = c.idNlPhu > 0 ? VatPhamDungLauDai.demItem(p, c.idNlPhu) : 0;
                final int loi = idLoi > 0 ? VatPhamDungLauDai.demItem(p, idLoi) : 0;
                thieu[0] = CheTaoLogic.thieu(c, capNv, p.xu, soDa(p), nl, loi);
                return thieu[0] == null;
            }, () -> {
                p.subXu(c.xu, true, "che tao " + c.idTemplate);
                khoa[0] = truDa(p, c.da);
                if (c.idNlPhu > 0 && c.soNlPhu > 0) {
                    VatPhamDungLauDai.truItem(p, c.idNlPhu, c.soNlPhu);
                }
                if (idLoi > 0 && c.loi > 0) {
                    VatPhamDungLauDai.truItem(p, idLoi, c.loi);
                }
            });
            if (!du) {
                BoNgocGame.ketQua(p, false, thieu[0] == null ? "Không đủ nguyên liệu." : thieu[0]);
                guiDanhSach(p);
                return;
            }
            tui.cheTaoPity = CheTaoLogic.pitySau(truoc, bac);
            tui.cheTaoTong++;
            tui.ban = true;
            if (khoa[0]) {
                it.lock = 1; // dung da khoa thi mon khoa (giong nang cap)
            }
            it.id = p.getIDItem();
            it.owner = p.charDBID;
            p.iItems.add(it);
            final ItemTemplates tm = it.getTemplate() != null ? it.getTemplate() : t; // [do-moi] co the da doi sang mon ho
            final String ten = tm.name + ChiSoTrangBiGame.tenHePhienDa(it);
            final boolean hiem = bac != null && bac.ma >= CheTaoLogic.MA_SU_THI;
            com.htbb.thongbao.ThongBao.gui(p, hiem ? com.htbb.thongbao.ThongBao.HIEM : com.htbb.thongbao.ThongBao.KET_QUA,
                    "Chế tạo: " + ten + " (" + (bac != null ? bac.ten : "") + ")",
                    // bao loi 03/11/2026: noi ro mon nam o dau + ten that (do moi doi "Phien da he bach kim" -> "Phien da
                    // Violet" nen nguoi choi tim theo ten cong thuc khong thay trong hanh trang).
                    "Đã vào hành trang" + (tm.id != t.id ? " (công thức " + t.name + ")" : "")
                            + (hiem ? "" : " · Chưa ra Sử thi: " + tui.cheTaoPity + "/" + pity() + " lần"),
                    com.htbb.thongbao.ThongBao.HINH_ICON, tm.idIcon);
            try {
                final Message m = BoNgocGame.moi(BoNgocGame.S_CHE_TAO_KQ);
                m.dos.writeByte(1);
                m.dos.writeShort(tm.id);
                m.dos.writeShort(tm.idIcon);
                m.dos.writeUTF(ten);
                m.dos.writeByte(bac != null ? bac.ma : 0);
                m.dos.writeUTF(bac != null ? bac.ten : "");
                m.dos.writeByte(it.colorName);
                p.sendMessage(m);
                p.sendMessage(MessageCreator.createCharInventoryMessage(p, 0));
                p.sendMessage(MessageCreator.createCharGemItem(p));
            } catch (final Exception ignored) {
            }
            NhiemVuEngine.suKien(p, "che_tao", String.valueOf(t.id), 1);
            try {
                data.Database.instance.saveOrtherLog("", p.charname, "che tao " + t.id + " " + t.name + " bac " + (bac != null ? bac.ten : "?")
                        + " xu " + c.xu + " da " + c.da + " pity " + truoc + "->" + tui.cheTaoPity + " | " + it.getDBInfo(), "chetao");
            } catch (final Exception ignored) {
            }
            guiDanhSach(p);
        }
    }

    /** Tru {@code n} Da nang cap: da thuong truoc, thieu moi dung da khoa. @return true neu co dung da khoa. */
    static boolean truDa(final Char p, final int n) {
        final int sell = p.listGemitemSell != null && p.listGemitemSell.length > BoNgocGame.ID_DA ? p.listGemitemSell[BoNgocGame.ID_DA] : 0;
        final int thuong = Math.min(n, Math.max(0, p.listGemitem[BoNgocGame.ID_DA] - sell));
        if (thuong > 0) {
            p.delGem(BoNgocGame.ID_DA, thuong, false);
        }
        if (n - thuong > 0) {
            p.delGem(BoNgocGame.ID_DA, n - thuong, true);
            return true;
        }
        return false;
    }

    /** Tao mon tu mau {@code t}: dong cua nguoi choi, chi so x he so bac hiem, so lo them, phien da co he. */
    static Item taoMon(final Char p, final ItemTemplates t, final CheTaoLogic.BacHiem bac, final int he, final int kieuDa) {
        final Item it;
        try {
            it = Map.dropItemByIdTemplate(p.inCountry, t.id);
        } catch (final Exception e) {
            return null;
        }
        if (it == null) {
            return null;
        }
        if (t.type < 3 || t.type > 7) {
            final int cl = p.charClass;
            final Hashtable<Integer, ItemTemplates> h = cl >= 0 && cl < Map.itemTemplates.size() ? Map.itemTemplates.get(cl) : null;
            if (h != null && h.get(Integer.valueOf(t.id)) != null) {
                it.clazz = (byte) cl;
                it.classTemplate = cl;
            }
        }
        if (bac != null) {
            it.colorName = (byte) bac.colorName;
            for (int i = 0; i < 10 && i < it.atb.length; i++) {
                if (t.atb != null && t.atb.length > 9 && i >= 7) {
                    break; // atb7..9 la so hieu hinh / co thoi trang
                }
                it.atb[i] = (short) CheTaoLogic.nhanChiSo(it.atb[i], bac.heSoPhanTram);
            }
            final int loTheoMau = bac.colorName >= 0 && bac.colorName < BoNgocGame.DL.loTheoMau.length
                    ? BoNgocGame.DL.loTheoMau[bac.colorName] : 1;
            if (bac.soLo > loTheoMau) {
                final NgocHe.Kham k = new NgocHe.Kham();
                k.loThem = bac.soLo;
                it.ngocHe = NgocHe.maHoa(k);
            }
        }
        DoMoiGame.thayCheTao(it, t, he); // [do-moi] vong co -> bo he da chon, phien da -> bo vung (cung chi so)
        if (t.type == 12 && it.otherAtt != null && it.otherAtt.length > com.htbb.pokemon.ChiSoTrangBi.ID_KHANG_HE_PHIEN_DA - 61) {
            final int h = HePokemon.hopLe(he) ? he : Math.max(0, p.hePokemon1());
            final int id = kieuDa == 1 ? com.htbb.pokemon.ChiSoTrangBi.ID_KHANG_HE_PHIEN_DA : com.htbb.pokemon.ChiSoTrangBi.ID_ST_HE_PHIEN_DA;
            it.otherAtt[id - 61] = (short) com.htbb.pokemon.ChiSoTrangBi.maDongPhienDa(h, CheTaoLogic.PHIEN_DA_PHAN_NGHIN);
        }
        return it;
    }
}
