import com.htbb.pokemon.HeSinhThai;
import com.htbb.pokemon.HoSoSinhThai;
import com.htbb.pokemon.NhiemVuMau;
import com.htbb.pokemon.ThongSoLauDai;
import com.htbb.pokemon.VatLieuSinhThai;
import com.htbb.pokemon.VuonBerry;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * BAT LAI VUON BERRY (chu game 29/10/2026). Truoc: o dat (Char.seedsItem) chi Database.getFarm nap - khong ai goi - nen 26
 * buoc So tay "Trồng và thu hoạch ..." khong bao gio xong. Kiem: luat VuonBerry (so o / cap / mua o, gieo, lon theo dong
 * ho may chu, thu hoach nguyen tu, tran ngay, tuoi them), luu - nap ho so (khoa vu / vd, chuoi cu doc duoc, khong nhan doi),
 * khop buoc So tay theo loai berry, NGAN SACH (trong co loi, thuoc tu vuon re hon NPC co tran, voi xu NPC bi tran chung; so xu / ngay), khoi SQL
 * bat_lai_vuon.sql (moi buoc lam_vuon co chu dung loai + duong menu that), ma nguon (khong con chu "chưa mở trong bản thử
 * nghiệm", duong o dat cu khong nap lai). Duong menu that + So tay: real.KiemVuonBerryGame.
 */
public final class KiemVuonBerry {

    private KiemVuonBerry() {
    }

    static final long PHUT = 60000L;

    public static void chay() {
        Kiem.nhom("Vuon berry - luat o dat / gieo / lon / thu hoach / tran (VuonBerry)");
        luat();
        Kiem.nhom("Vuon berry - luu / nap ho so, khong nhan doi khi dang nhap lai");
        luuNap();
        Kiem.nhom("Vuon berry - buoc So tay 'Trồng và thu hoạch' khop dung loai berry");
        khopSoTay();
        Kiem.nhom("Vuon berry - ngan sach (trong co loi, thuoc tu vuon re hon NPC, voi xu bi tran chung NPC)");
        nganSach();
        Kiem.nhom("Vuon berry - khoi SQL bat_lai_vuon.sql + ma nguon");
        sqlVaMa();
        Kiem.nhom("Vuon berry - duong menu that + So tay (real.KiemVuonBerryGame)");
        real.KiemVuonBerryGame.BAO = new real.KiemKetTau.Bao() {
            @Override
            public void bang(final String moTa, final Object mongDoi, final Object thucTe) {
                Kiem.bang(moTa, mongDoi, thucTe);
            }
        };
        try {
            real.KiemVuonBerryGame.chay();
        } catch (final Exception e) {
            e.printStackTrace(System.out);
            Kiem.bang("KiemVuonBerryGame khong nem loi", "", String.valueOf(e));
        }
    }

    static void luat() {
        Kiem.bang("so o cap 1 / 14 / 15 / 30 / 100", "2/2/3/4/4", VuonBerry.soO(1, 0) + "/" + VuonBerry.soO(14, 0) + "/"
                + VuonBerry.soO(15, 0) + "/" + VuonBerry.soO(30, 0) + "/" + VuonBerry.soO(100, 0));
        Kiem.bang("toi da 6 o (4 mien phi + 2 mua)", Integer.valueOf(6), Integer.valueOf(VuonBerry.soO(100, 99)));
        Kiem.bang("O_TOI_DA = co ban + moc cap + mua", Integer.valueOf(VuonBerry.O_TOI_DA),
                Integer.valueOf(VuonBerry.O_CO_BAN + VuonBerry.CAP_THEM_O.length + VuonBerry.O_MUA_TOI_DA));
        Kiem.dung("mua o: 1 gio roi 3 gio thu nhap, het thi -1", VuonBerry.giaMoO(27, 0) == ThongSoLauDai.xuMoiGio(27)
                && VuonBerry.giaMoO(27, 1) == ThongSoLauDai.xuMoiGio(27) * 3 && VuonBerry.giaMoO(27, 2) == -1L);
        Kiem.bang("8 berry gieo duoc = VatLieuSinhThai 30..37 loai Berry", Integer.valueOf(8), Integer.valueOf(VuonBerry.MA.length));
        for (final int ma : VuonBerry.MA) {
            Kiem.bang("ma " + ma + " la Berry", Integer.valueOf(VatLieuSinhThai.L_BERRY), Integer.valueOf(VatLieuSinhThai.loai(ma)));
        }
        final HoSoSinhThai h = new HoSoSinhThai();
        final long t0 = 1_800_000_000_000L;
        Kiem.bang("o 1 trong", Integer.valueOf(VuonBerry.TRONG), Integer.valueOf(VuonBerry.trangThai(h, 0, 1, t0)));
        Kiem.bang("o 3 khoa o cap 1", Integer.valueOf(VuonBerry.KHOA), Integer.valueOf(VuonBerry.trangThai(h, 2, 1, t0)));
        Kiem.sai("khong gieo o khoa", VuonBerry.datGieo(h, 2, 30, 1, t0));
        Kiem.sai("khong gieo Lum o cap 1 (can 40)", VuonBerry.datGieo(h, 0, 37, 1, t0));
        Kiem.sai("khong gieo ma khong phai berry", VuonBerry.datGieo(h, 0, 6, 1, t0));
        Kiem.dung("gieo Oran o 1", VuonBerry.datGieo(h, 0, 30, 1, t0));
        Kiem.sai("khong gieo chong len o dang co cay", VuonBerry.datGieo(h, 0, 31, 1, t0));
        Kiem.bang("dang lon, con 20 phut", "1/20", VuonBerry.trangThai(h, 0, 1, t0) + "/" + VuonBerry.conPhut(h, 0, t0));
        Kiem.bang("19 phut 1 giay: con 1 phut", Integer.valueOf(1), Integer.valueOf(VuonBerry.conPhut(h, 0, t0 + 19 * PHUT + 1000L)));
        Kiem.bang("thu truoc khi chin: CHUA_CHIN", Integer.valueOf(VuonBerry.CHUA_CHIN),
                Integer.valueOf(VuonBerry.thu(h, 0, t0 + 20 * PHUT - 1L, null)));
        Kiem.bang("chin dung 20 phut (dong ho may chu)", Integer.valueOf(VuonBerry.CHIN), Integer.valueOf(VuonBerry.trangThai(h, 0, 1, t0 + 20 * PHUT)));
        Kiem.bang("chin roi khong heo (30 ngay sau)", Integer.valueOf(VuonBerry.CHIN),
                Integer.valueOf(VuonBerry.trangThai(h, 0, 1, t0 + 30L * 24 * 60 * PHUT)));
        final int[] ma = new int[1];
        Kiem.bang("thu hoach: 3 berry", Integer.valueOf(3), Integer.valueOf(VuonBerry.thu(h, 0, t0 + 20 * PHUT, ma)));
        Kiem.bang("thu hoach: ma Oran", Integer.valueOf(30), Integer.valueOf(ma[0]));
        Kiem.bang("tui +3 Oran bac 1", Integer.valueOf(3), Integer.valueOf(h.so(VatLieuSinhThai.khoa(30, 1))));
        Kiem.bang("thu lai cung o: KHONG_CO_CAY (khong nhan doi)", Integer.valueOf(VuonBerry.KHONG_CO_CAY),
                Integer.valueOf(VuonBerry.thu(h, 0, t0 + 21 * PHUT, null)));
        Kiem.bang("tui van 3", Integer.valueOf(3), Integer.valueOf(h.tongMa(30)));
        // bac theo cap luc gieo (len cap giua chung khong doi bac)
        Kiem.dung("gieo Lum cap 60", VuonBerry.datGieo(h, 1, 37, 60, t0));
        Kiem.bang("bac luc gieo = 4", Integer.valueOf(4), Integer.valueOf(h.vuonBac[1]));
        Kiem.bang("Lum lon 120 phut", Integer.valueOf(VuonBerry.DANG_LON), Integer.valueOf(VuonBerry.trangThai(h, 1, 60, t0 + 119 * PHUT)));
        // ban be tuoi
        h.tuoiNhanHomNay = 1;
        Kiem.bang("tuoi 1 lan: van 3", Integer.valueOf(3), Integer.valueOf(VuonBerry.sanLuong(h.tuoiNhanHomNay)));
        h.tuoiNhanHomNay = 2;
        Kiem.bang("tuoi 2 lan: 4", Integer.valueOf(4), Integer.valueOf(VuonBerry.thu(h, 1, t0 + 120 * PHUT, null)));
        Kiem.bang("Lum bac 4 vao tui", Integer.valueOf(4), Integer.valueOf(h.so(VatLieuSinhThai.khoa(37, 4))));
        Kiem.bang("tuoi 5 lan: van 4 (tran)", Integer.valueOf(4), Integer.valueOf(VuonBerry.sanLuong(5)));
        // tran ngay
        h.tuoiNhanHomNay = 0;
        int lan = h.thuVuonHomNay;
        long bay = t0;
        while (lan < VuonBerry.THU_TOI_DA_NGAY) {
            VuonBerry.datGieo(h, 0, 31, 1, bay);
            bay += 20 * PHUT;
            if (VuonBerry.thu(h, 0, bay, null) > 0) {
                lan++;
            } else {
                break;
            }
        }
        Kiem.bang("thu duoc dung " + VuonBerry.THU_TOI_DA_NGAY + " lan / ngay", Integer.valueOf(VuonBerry.THU_TOI_DA_NGAY), Integer.valueOf(h.thuVuonHomNay));
        VuonBerry.datGieo(h, 0, 31, 1, bay);
        bay += 20 * PHUT;
        final int pecha = h.tongMa(31);
        Kiem.bang("lan 13: HET_LUOT", Integer.valueOf(VuonBerry.HET_LUOT), Integer.valueOf(VuonBerry.thu(h, 0, bay, null)));
        Kiem.dung("het luot: cay chin van con, tui khong doi", h.vuonMa[0] == 31 && h.tongMa(31) == pecha);
        h.sangNgay(h.ngay + 1);
        Kiem.bang("sang ngay: dem thu ve 0", Integer.valueOf(0), Integer.valueOf(h.thuVuonHomNay));
        Kiem.bang("sang ngay: thu tiep duoc", Integer.valueOf(3), Integer.valueOf(VuonBerry.thu(h, 0, bay, null)));
        // mua o
        final HoSoSinhThai m = new HoSoSinhThai();
        Kiem.dung("mua o 1", VuonBerry.datMuaO(m, 0));
        Kiem.sai("menu cu (da mua 0) bam lai: khong mua chong", VuonBerry.datMuaO(m, 0));
        Kiem.dung("mua o 2", VuonBerry.datMuaO(m, 1));
        Kiem.sai("mua o 3: khong", VuonBerry.datMuaO(m, 2));
        Kiem.bang("cap 1 + 2 o mua = 4 o", Integer.valueOf(4), Integer.valueOf(VuonBerry.soO(1, m.vuonDaMua)));
        Kiem.dung("huong dan ghi duong menu that + moi berry", VuonBerry.huongDan().contains("Hệ sinh thái → Vườn nhà và bạn bè → Ô đất của tôi")
                && VuonBerry.huongDan().contains("Berry Lum: cấp 40, 120 phút"));
    }

    static void luuNap() {
        final HoSoSinhThai h = new HoSoSinhThai();
        final long t0 = 1_800_000_000_000L;
        VuonBerry.datGieo(h, 0, 30, 1, t0);
        VuonBerry.datGieo(h, 1, 34, 25, t0 + 5);
        h.vuonDaMua = 1;
        h.thuVuonHomNay = 7;
        h.them(VatLieuSinhThai.khoa(30, 1), 5);
        final String s = h.chuoi();
        Kiem.dung("chuoi co khoa vu / vd", s.contains("|vu=30.1." + t0 + ",34.2." + (t0 + 5) + ",0.0.0") && s.contains("|vd=1,7,0"));
        final HoSoSinhThai d = HoSoSinhThai.doc(s);
        Kiem.dung("nap lai: o dat, bac, moc gieo, da mua, dem ngay", d.vuonMa[0] == 30 && d.vuonMa[1] == 34 && d.vuonBac[1] == 2
                && d.vuonGieo[0] == t0 && d.vuonGieo[1] == t0 + 5 && d.vuonDaMua == 1 && d.thuVuonHomNay == 7 && d.tongMa(30) == 5);
        Kiem.bang("nap lai roi luu lai: giong het", s, d.chuoi());
        final HoSoSinhThai cu = HoSoSinhThai.doc("v1|ng=20261029|tl=100|tui=301:4");
        Kiem.dung("chuoi cu (truoc ban vuon): vuon trong, tui giu", cu.vuonMa[0] == 0 && cu.vuonDaMua == 0 && cu.tongMa(30) == 4);
        final HoSoSinhThai hong = HoSoSinhThai.doc("v1|vu=6.1.5,30.9.x,999.1.1,31.1.7,32.3.8,33.1.9,34.1.1,35.1.1,36.1.1|vd=9,-3");
        Kiem.dung("chuoi hong: bo o sai ma / sai so, bac kep, toi da 6 o, da mua kep 0..2", hong.vuonMa[0] == 0 && hong.vuonMa[1] == 0
                && hong.vuonMa[2] == 0 && hong.vuonMa[3] == 31 && hong.vuonGieo[3] == 7 && hong.vuonBac[4] == 3 && hong.vuonMa[5] == 33
                && hong.vuonDaMua == 2 && hong.thuVuonHomNay == 0);
        // khong nhan doi: thu hoach -> luu -> nap -> thu lai = khong co cay; berry chi mot lan
        final HoSoSinhThai a = HoSoSinhThai.doc(s);
        final int truoc = a.tongMa(30);
        Kiem.bang("thu o 1", Integer.valueOf(3), Integer.valueOf(VuonBerry.thu(a, 0, t0 + 20 * PHUT, null)));
        final HoSoSinhThai b = HoSoSinhThai.doc(a.chuoi());
        Kiem.bang("dang nhap lai sau thu: o 1 trong", Integer.valueOf(VuonBerry.KHONG_CO_CAY), Integer.valueOf(VuonBerry.thu(b, 0, t0 + 40 * PHUT, null)));
        Kiem.bang("dang nhap lai sau thu: berry +3 dung mot lan", Integer.valueOf(truoc + 3), Integer.valueOf(b.tongMa(30)));
        // cay lon khi offline: nap lai sau 1 ngay van chin (moc gieo tuyet doi, khong dem nhip online)
        final HoSoSinhThai off = HoSoSinhThai.doc(s);
        Kiem.bang("offline 1 ngay: o 2 Chesto chin", Integer.valueOf(VuonBerry.CHIN), Integer.valueOf(VuonBerry.trangThai(off, 1, 25, t0 + 24 * 60 * PHUT)));
    }

    static void khopSoTay() {
        final NhiemVuMau m = new NhiemVuMau();
        m.kieu = "lam_vuon";
        m.dieuKien = NhiemVuMau.docDieuKien("vat_sinh_thai=Berry Chesto");
        Kiem.dung("Berry Chesto khop buoc Hoa sương", m.khop("lam_vuon", "Berry Chesto"));
        Kiem.sai("Berry Oran KHONG khop buoc Chesto", m.khop("lam_vuon", "Berry Oran"));
        Kiem.sai("so id cu (5) khong khop buoc co loai", m.khop("lam_vuon", "5"));
        Kiem.sai("kieu khac khong khop", m.khop("dao_dat", "Berry Chesto"));
        final NhiemVuMau r = new NhiemVuMau();
        r.kieu = "lam_vuon";
        Kiem.dung("buoc lam_vuon khong ghi loai: moi lan thu hoach deu dem", r.khop("lam_vuon", "Berry Lum"));
        Kiem.bang("diem moc gui ten berry", "Berry Lum", VatLieuSinhThai.ten(37));
    }

    static void nganSach() {
        // 1. trong la LOI: mot lan thu (3 berry, bac luc gieo) dang gia hon tien hat o moi cap, moi berry
        boolean loi = true;
        String loiSai = "";
        for (int cap = 1; cap <= ThongSoLauDai.CAP_TOI_DA; cap++) {
            final int bac = VatLieuSinhThai.bacTheoCap(cap);
            for (final int ma : VuonBerry.MA) {
                final long hat = VuonBerry.giaHat(ma, cap);
                if (VuonBerry.SAN_LUONG * VatLieuSinhThai.giaXu(ma, bac) < 2 * hat || hat < 1) {
                    loi = false;
                    loiSai = "cap " + cap + " ma " + ma;
                }
            }
        }
        Kiem.dung("moi cap, moi berry: 1 lan thu (3 berry) >= 2 x tien hat (trong la loi)" + (loi ? "" : " (" + loiSai + ")"), loi);
        Kiem.dung("gia hat trong [0,5; 1] x gia tham chieu 1 berry", VuonBerry.GIA_HAT_PHAN_TRAM >= 50 && VuonBerry.GIA_HAT_PHAN_TRAM <= 100);
        Kiem.bang("hat Oran cap 1 = min(50% gia tham chieu bac 1, tran tu gia thuoc NPC)", Long.valueOf(Math.max(1L,
                Math.min(VatLieuSinhThai.giaXu(30, 1) / 2, VuonBerry.hatToiDa(30)))), Long.valueOf(VuonBerry.giaHat(30, 1)));
        // 2. voi xu: moi duong doi vat lieu sinh thai ra xu = HeSinhThaiMenu.banNpc (ChoSinhThai.npcMua) -> tran chung / ngay
        final String menu = doc("src/com/htbb/pokemon/HeSinhThaiMenu.java");
        Kiem.bang("chi MOT cho NPC mua vat lieu sinh thai (ChoSinhThai.npcMua)", Integer.valueOf(1),
                Integer.valueOf(menu.split(Pattern.quote("ChoSinhThai.npcMua("), -1).length - 1));
        Kiem.dung("banNpc cong h.banNpcHomNay (tran chung moi vat lieu, ke ca berry)", menu.contains("h.banNpcHomNay += kq[1];"));
        Kiem.dung("NPC mua trong tran ngay con lai", com.htbb.pokemon.ChoSinhThai.npcMua(30, 2, 1000, 0, 27)[1]
                <= com.htbb.pokemon.ChoSinhThai.tranBanNpcNgay(27) && com.htbb.pokemon.ChoSinhThai.npcMua(30, 2, 1000,
                com.htbb.pokemon.ChoSinhThai.tranBanNpcNgay(27), 27)[0] == 0);
        Kiem.dung("tran NPC <= 0,1 gio thu nhap / ngay", HeSinhThai.TRAN_BAN_NPC_NGAY_PHAN_NGHIN <= 100);
        Kiem.dung("binh thuoc that khong ban NPC (doSellItem chi duyet iItems)", doc("src/real/OfflineMap.java")
                .contains("for (int i = 0; i < player.iItems.size(); i++) {"));
        Kiem.dung("khong gieo berry tu tui (chi mua hat)", !menu.contains("layTheoMa(ma, 1)") && VuonBerry.GIA_HAT_PHAN_TRAM > 0);
        // 3. thuoc tu vuon re hon NPC (loi ich chinh) - va dau ra <= nguyen lieu (‰ gio)
        int soMe = 0;
        for (final com.htbb.pokemon.KyNangSong.CongThuc ct : com.htbb.pokemon.KyNangSong.CONG_THUC) {
            if (ct.id < VuonBerry.CT_DAU || ct.id > VuonBerry.CT_CUOI) {
                continue;
            }
            soMe++;
            int vao = 0;
            boolean chiBerry = true;
            for (final int[] nl : ct.nguyenLieu) {
                vao += VatLieuSinhThai.giaPhanNghin(nl[0]) * nl[1];
                chiBerry &= VuonBerry.chiSo(nl[0]) >= 0;
            }
            Kiem.dung(ct.ten + ": chi berry trong duoc, dau ra thuoc 90..94", chiBerry && ct.ra >= 90 && ct.ra <= 94
                    && ct.kyNang == com.htbb.pokemon.KyNangSong.CHE_THUOC);
            Kiem.dung(ct.ten + ": gia tri dau ra (" + VatLieuSinhThai.giaPhanNghin(ct.ra) + "‰) <= nguyen lieu (" + vao + "‰)",
                    VatLieuSinhThai.giaPhanNghin(ct.ra) <= vao);
        }
        Kiem.bang("5 cong thuc thuoc tu vuon", Integer.valueOf(5), Integer.valueOf(soMe));
        Kiem.bang("ME_THUOC_TOI_DA_NGAY giu 5 (chung ca thuoc vuon)", Integer.valueOf(5), Integer.valueOf(com.htbb.pokemon.KyNangSong.ME_THUOC_TOI_DA_NGAY));
        final StringBuilder bang = new StringBuilder();
        for (final int cap : new int[]{10, 27, 50, 80}) {
            final long gio = ThongSoLauDai.xuMoiGio(cap);
            final long tk = VuonBerry.tietKiemNgay(cap);
            final long thuocThietKe = 3L * gio * ThongSoLauDai.TIEU_HAO_PHAN_TRAM / 100L; // nguoi choi VUA 3 gio / ngay
            final long xuNpc = VuonBerry.xuNpcNgayToiDa(cap);
            bang.append(String.format("cap %d: thu nhap %d xu/gio; voi xu NPC toi da %d xu/ngay (%d‰ gio, tran chung %d); tiet kiem thuoc"
                    + " toi da %d xu/ngay (%d‰ gio, ho hut thuoc thiet ke 3 gio %d)%n", cap, gio, xuNpc, xuNpc * 1000 / gio,
                    com.htbb.pokemon.ChoSinhThai.tranBanNpcNgay(cap), tk, tk * 1000 / gio, thuocThietKe));
            Kiem.dung("cap " + cap + ": thuoc tu vuon re hon NPC (tiet kiem " + tk + " xu/ngay > 0)", tk > 0);
            Kiem.dung("cap " + cap + ": voi xu NPC tu vuon <= tran chung 0,1 gio", xuNpc <= com.htbb.pokemon.ChoSinhThai.tranBanNpcNgay(cap));
            Kiem.dung("cap " + cap + ": tiet kiem thuoc <= ngan sach thuoc thiet ke 3 gio (" + VuonBerry.tranThuocNgay(cap) + ")",
                    tk <= VuonBerry.tranThuocNgay(cap) && VuonBerry.tranThuocNgay(cap) == thuocThietKe);
        }
        System.out.print(bang.toString().replaceAll("(?m)^", "   [vuon] "));
        for (final com.htbb.pokemon.KyNangSong.CongThuc ct : com.htbb.pokemon.KyNangSong.CONG_THUC) {
            if (ct.id >= VuonBerry.CT_DAU && ct.id <= VuonBerry.CT_CUOI && VuonBerry.meVuonDuoc(ct, 27)) {
                Kiem.dung("cap 27: " + ct.ten + " ton " + VuonBerry.chiPhiMe(ct, 27) + " < NPC " + VuonBerry.giaNpcMe(ct),
                        VuonBerry.chiPhiMe(ct, 27) < VuonBerry.giaNpcMe(ct));
            }
        }
        // 01/11/2026 (gia thuoc NPC ha 2-7 xu): 1 me thuoc vuon ton ~60-70% gia NPC 10 binh o MOI cap, tran ngay giu so binh
        boolean meKhung = true;
        String meSai = "";
        final StringBuilder meBang = new StringBuilder();
        for (final com.htbb.pokemon.KyNangSong.CongThuc ct : com.htbb.pokemon.KyNangSong.CONG_THUC) {
            if (!VuonBerry.laThuocVuon(ct)) {
                continue;
            }
            meBang.append(ct.ten).append(": NPC ").append(VuonBerry.giaNpcMe(ct)).append(", vuon ").append(VuonBerry.meMucTieu(ct))
                    .append(" (hat cap 27 ").append(VuonBerry.hatMe(ct, 27)).append(" + bep ").append(VuonBerry.phiBepThuocVuon(ct, 27))
                    .append("; cap 100 ").append(VuonBerry.hatMe(ct, 100)).append(" + ").append(VuonBerry.phiBepThuocVuon(ct, 100)).append(")\n");
            for (int cap = 1; cap <= ThongSoLauDai.CAP_TOI_DA; cap++) {
                if (!VuonBerry.meVuonDuoc(ct, cap)) {
                    continue;
                }
                final long cp = VuonBerry.chiPhiMe(ct, cap);
                final long npc = VuonBerry.giaNpcMe(ct);
                if (cp * 100 < npc * 60 || cp * 100 > npc * 70 || VuonBerry.phiBepThuocVuon(ct, cap) <= 0) {
                    meKhung = false;
                    meSai = ct.ten + " cap " + cap + ": " + cp + " / " + npc;
                }
            }
        }
        System.out.print(meBang.toString().replaceAll("(?m)^", "   [vuon] "));
        Kiem.dung("moi cap, moi thuoc vuon: tien 1 me (hat + bep) 60-70% gia NPC 10 binh, bep > 0" + (meKhung ? "" : " (" + meSai + ")"), meKhung);
        Kiem.bang("tran ngay tinh theo trong so gia NPC cu (giu so binh / ngay)", "[100, 500, 800, 50, 150]",
                java.util.Arrays.toString(VuonBerry.TRONG_SO_TRAN));
        final String menuHst = doc("src/com/htbb/pokemon/HeSinhThaiMenu.java");
        Kiem.dung("HeSinhThaiMenu: tran / cong thuocVuonHomNay bang trongSoMe, bep thuoc vuon = phiBepThuocVuon",
                menuHst.contains("h.thuocVuonHomNay + VuonBerry.trongSoMe(ct) > tranVuon") && menuHst.contains("h.thuocVuonHomNay += VuonBerry.trongSoMe(ct)")
                && menuHst.contains("VuonBerry.phiBepThuocVuon(ct, HeSinhThaiGame.cap(p))"));
        // gia binh NPC trong VuonBerry = phan A data_potion (id 1..5)
        final String toan = DayDu.toanVan();
        final String[] ten = {"Thuốc hồi máu", "Siêu thuốc hồi máu", "Thuốc hồi máu cực mạnh", "Ether", "Ether lớn"};
        for (int i = 0; i < 5; i++) {
            Kiem.dung("gia NPC binh " + ten[i] + " = " + VuonBerry.GIA_BINH_NPC[i] + " (data_potion " + (i + 1) + ")",
                    Pattern.compile("\\(" + (i + 1) + ",\\d+,\\d+,\\d+,'" + ten[i] + "','[a-z]+'," + VuonBerry.GIA_BINH_NPC[i] + ",").matcher(toan).find());
        }
        Kiem.dung("thuoc 90..94 doi binh 1..5", "PT:1:10".equals(com.htbb.pokemon.KyNangSong.thuongDungThuoc(90))
                && "PT:5:10".equals(com.htbb.pokemon.KyNangSong.thuongDungThuoc(94)));
        Kiem.dung("menu hat ghi cong dung", menu.contains("VuonBerry.congDung(ma, 2)") && VuonBerry.congDung(30, 2).startsWith("Thuốc xịt từ vườn"));
        Kiem.dung("huong dan ghi cong dung + gia NPC", VuonBerry.moTaCongDung(27).contains("Thuốc xịt từ vườn (Chế thuốc 1): 3 Berry Oran → 10 bình HP nhỏ (NPC bán " + VuonBerry.giaNpcMe(ct30()) + " xu"));
    }

    static com.htbb.pokemon.KyNangSong.CongThuc ct30() {
        for (final com.htbb.pokemon.KyNangSong.CongThuc c : com.htbb.pokemon.KyNangSong.CONG_THUC) {
            if (c.id == 30) {
                return c;
            }
        }
        return null;
    }

    static String doc(final String tep) {
        try {
            return new String(Files.readAllBytes(Paths.get(tep)), StandardCharsets.UTF_8);
        } catch (final Exception e) {
            return "";
        }
    }

    static void sqlVaMa() {
        final String khoi = DayDu.phanNangCap("bat_lai_vuon.sql");
        Kiem.dung("co khoi -- >>> bat_lai_vuon.sql", !khoi.isEmpty());
        final String toan = DayDu.toanVan();
        // moi buoc kieu lam_vuon o phan A
        final Matcher q = Pattern.compile("\\('([A-Z0-9.-]+)',(\\d+),'[a-z_]+',\\d+," + rep(Q + ",", 8) + "\\d+,\\d+," + Q + ",'lam_vuon',")
                .matcher(toan);
        int so = 0;
        final StringBuilder thieu = new StringBuilder();
        while (q.find()) {
            final String ma = q.group(1);
            so++;
            if ("C20.2".equals(ma) || "C34.5".equals(ma)) {
                continue; // chuoi chinh cu (data_quest_moi) - chu da la "Thu ... (rơi từ ...)"
            }
            if (!khoi.contains("WHERE `ma` = '" + ma + "' AND `muc_tieu_text` = 'Trồng và thu hoạch ")) {
                thieu.append(ma).append(' ');
            }
        }
        Kiem.bang("phan A: 28 buoc lam_vuon", Integer.valueOf(28), Integer.valueOf(so));
        Kiem.bang("moi buoc lam_vuon (tru C20.2 / C34.5) co chu moi", "", thieu.toString().trim());
        Kiem.dung("chu moi ghi loai berry + vuon nha", khoi.contains("'Trồng và thu hoạch 3 cây Berry Chesto (Hoa sương) ở vườn nhà' WHERE `ma` = 'TV-05.2'")
                && khoi.contains("'Trồng và thu hoạch 2 cây Berry Oran (Quả Oran) ở vườn nhà' WHERE `ma` = 'N-1.12'")
                && khoi.contains("'Trồng và thu hoạch 3 cây Berry Pecha (Hạt giống Berry) ở vườn nhà' WHERE `ma` = 'TV-08.1'")
                && khoi.contains("Berry Lum (Nấm phát quang)") && khoi.contains("Berry Leppa (Cành hành thơm)"));
        Kiem.dung("goi y: duong menu that", khoi.contains("Menu → Hệ sinh thái → Vườn nhà và bạn bè → Ô đất của tôi → chọn ô trống"));
        Kiem.sai("khong con goi y 'bản đồ cá nhân' moi (chi o WHERE gia cu)", khoi.replaceAll("AND `[a-z_]+` = '[^']*'", "").contains("bản đồ cá nhân"));
        Kiem.sai("khoi chi UPDATE bang thiet ke (khong dung bang nguoi choi)", khoi.replaceAll("(?m)^--.*$", "").contains("tob_"));
        // ten trong truyen -> berry: phu hop dieu_kien vat_sinh_thai cua phan A
        final String[][] anhXa = {{"Quả Oran", "Berry Oran"}, {"Hạt giống Berry", "Berry Pecha"}, {"Hoa sương", "Berry Chesto"},
            {"Nấm phát quang", "Berry Lum"}, {"Cành hành thơm", "Berry Leppa"}};
        for (final String[] a : anhXa) {
            Kiem.dung("anh xa " + a[0] + " -> " + a[1] + " khop dieu_kien phan A", Pattern.compile("'Trồng và thu hoạch \\d " + a[0]
                    + "'").matcher(toan).find() && toan.contains("'vat_sinh_thai=" + a[1] + "'"));
        }
        // cap gieo <= cap mo buoc (khong buoc nao doi berry chua gieo duoc)
        final Matcher c = Pattern.compile("\\('([A-Z0-9.-]+)',\\d+,'[a-z_]+',\\d+," + rep(Q + ",", 8) + "(\\d+),\\d+," + Q
                + ",'lam_vuon'," + Q + ",'([^']*)'").matcher(toan);
        final StringBuilder capSai = new StringBuilder();
        while (c.find()) {
            final int cap = Integer.parseInt(c.group(2));
            for (final String[] a : anhXa) {
                if (a[0].equals(c.group(3))) {
                    final int i = VuonBerry.chiSo(maTen(a[1]));
                    if (VuonBerry.CAP[i] > Math.max(1, cap)) {
                        capSai.append(c.group(1)).append(' ');
                    }
                }
            }
        }
        Kiem.bang("cap gieo berry <= cap mo buoc", "", capSai.toString().trim());
        Kiem.dung("huong dan tan thu npc.20_vuon: chi cach trong that", toan.contains("('npc.20_vuon', 49, 'Vườn và berry', 'Vườn không phải một bản đồ hay NPC")
                && toan.contains("Trồng: Ô đất của tôi → chọn ô trống → chọn berry"));
        final String menu = doc("src/com/htbb/pokemon/HeSinhThaiMenu.java");
        Kiem.sai("he sinh thai khong con 'chưa mở trong bản thử nghiệm'", menu.contains("chưa mở trong bản thử nghiệm")
                || toan.contains("Ô đất gieo hạt chưa mở"));
        Kiem.dung("menu Vườn co 'Ô đất của tôi'", menu.contains("\"Ô đất của tôi (\""));
        Kiem.dung("thu hoach ban diem moc lam_vuon theo ten berry", menu.contains("real.DiemMocNhiemVu.khiThuHoachBerry(p, ma[0]);"));
        // duong o dat cu (ban do vuon 29 = Rung Ilex, mua o / hat mien phi) khong duoc nap lai
        int goi = 0;
        for (final String tep : new String[]{"src/real/CharKhoiTao.java", "src/real/Char.java", "src/server/SelectCharHandler.java",
            "src/real/cmd/SelectCharHandler.java", "src/data/DatabaseNhanVat.java"}) {
            goi += doc(tep).split(Pattern.quote(".getFarm("), -1).length - 1;
        }
        Kiem.bang("Database.getFarm (o dat cu) van khong ai goi", Integer.valueOf(0), Integer.valueOf(goi));
    }

    /** Mot chuoi SQL '...' (co the co \\' ben trong). */
    static final String Q = "'(?:[^'\\\\]|\\\\.)*'";

    static String rep(final String s, final int n) {
        final StringBuilder b = new StringBuilder();
        for (int i = 0; i < n; i++) {
            b.append(s);
        }
        return b.toString();
    }

    static int maTen(final String ten) {
        for (final int ma : VuonBerry.MA) {
            if (VatLieuSinhThai.ten(ma).equals(ten)) {
                return ma;
            }
        }
        return -1;
    }
}
