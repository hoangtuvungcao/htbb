package real;

import com.htbb.pokemon.CapDoLauDai;
import com.htbb.pokemon.CuonHut;
import com.htbb.pokemon.HoSoLauDai;
import com.htbb.pokemon.KinhTeLauDai;
import com.htbb.pokemon.LauDai;
import com.htbb.pokemon.NangLuong;
import com.htbb.pokemon.ThangPvp;
import com.htbb.pokemon.ThongSoLauDai;
import data.Database;
import data.DatabaseGiftCode;
import data.DatabaseLauDai;
import java.util.List;

/**
 * "So tay huan luyen": muc noi vao CUOI menu NPC doi thuong (-90) khi bat co sv.lau_dai - cua
 * vao cac he thong dai han khong can doi client (chi dung menu + thong bao co san):
 *   0 Nang luong & tinh thong (chon cong tan cong / HP)
 *   1 Nhiem vu tuan (2 / tuan)
 *   2 Ham nguc bi an (hat giong theo ngay, 1 luot thuong / ngay)
 *   3 Thang PvP mua
 *   4 Su pham (nhan tro trong doi)
 *   5 Lich dang nhap 7 ngay / ve chuong
 *   9 Cua hang & doi vat pham (CuaHangLauDai, menu 20/21)
 * NPC ao {@link #NPC_ID} (-527, da grep src/: trong). Moi dieu kien kiem lai o day (muc 11).
 *
 * Text hien cho nguoi choi o day la BAN NHAP - can duyet (CODING_CONVENTION muc 10).
 */
public final class SoTayLauDai {

    public static final int NPC_ID = -527;
    public static final String TEN_MUC = "Sổ tay huấn luyện";

    static final int MENU_GOC = 0;
    static final int MENU_TINH_THONG = 1;
    static final int MENU_TUAN = 2;
    static final int MENU_HAM_NGUC = 3;
    static final int MENU_QUA_HOAT_DONG = 4;
    static final int MENU_GYM = 5;
    /** So muc co dinh cua menu goc; muc tu chi so nay la muc huong dan tan thu (HuongDanTanThuGame.themMucSoTay). */
    static final int SO_MUC_GOC = 11; // 0..9 + "Cúp PvP tuần" (10, pho-ban-cup)

    private SoTayLauDai() {
    }

    /** Vi tri muc trong menu NPC -90: sau "Xem doi thuong" (0) va "Pokédex" (1, neu co). */
    public static int viTri() {
        return 2;
    }

    public static void moMenu(final Char p) {
        if (!LauDai.bat() || p == null) {
            return;
        }
        // huong dan tan thu: muc Bo qua / Huong dan tan thu noi vao CUOI (chi so >= SO_MUC_GOC, chu tu CSDL)
        p.sendMessage(MessageCreator.createMsgMenu(HuongDanTanThuGame.themMucSoTay(p, new String[]{"Năng lượng và tinh thông", "Nhiệm vụ tuần",
            "Hầm ngục bí ẩn", "Thang PvP mùa", "Sư phạm (nhận trò)", "Lịch đăng nhập, vé chương", "Quà hoạt động hôm nay", "Thách đấu Gym", "Nhiệm vụ của tôi", "Cửa hàng và đổi vật phẩm", "Cúp PvP tuần"}), NPC_ID, MENU_GOC));
    }

    static void bao(final Char p, final String s) {
        p.sendMessage(MessageCreator.createServerAlertMessage(s, ""));
    }

    public static void doMenu(final Char p, final int idMenu, final int idOption) {
        if (!LauDai.bat() || p == null) {
            return;
        }
        final HoSoLauDai h = LauDaiNhanVat.lay(p);
        if (h == null) {
            bao(p, "Sổ tay tạm thời không đọc được, thử lại sau.");
            return;
        }
        try {
            if (idMenu == MENU_GOC) {
                switch (idOption) {
                    case 0:
                        xemNangLuong(p, h);
                        break;
                    case 1:
                        NhiemVuEngine.dsCoTheNhan(p, 3); // bang tuan chung T-/TV-/TD- (engine GD2b)
                        break;
                    case 2:
                        moHamNguc(p, h);
                        break;
                    case 3:
                        xemPvp(p, h);
                        break;
                    case 4:
                        suPham(p);
                        break;
                    case 5:
                        xemDangNhap(p, h);
                        break;
                    case 6:
                        QuaHoatDong.moMenu(p);
                        break;
                    case 7:
                        GymLauDai.moMenu(p);
                        break;
                    case 8:
                        NhiemVuEngine.moMenu(p);
                        break;
                    case 9:
                        CuaHangLauDai.moMenu(p); // luat "khong item mo coi" (kiem_item_mo_coi.py)
                        break;
                    case 10:
                        CupGame.moMenu(p); // 0.9.7 Cup PvP tuan xoay vong (real.CupGame)
                        break;
                    default:
                        HuongDanTanThuGame.chonMucSoTay(p, idOption - SO_MUC_GOC);
                        break;
                }
            } else if (idMenu == MENU_TINH_THONG && (idOption == 0 || idOption == 1)) {
                synchronized (h) {
                    h.ttLoai = idOption;
                    h.ban = true;
                }
                p.calculateAttrib();
                bao(p, "Tinh thông nay cộng " + (idOption == 0 ? "tấn công" : "HP") + ".");
            } else if (idMenu == MENU_TUAN) {
                chonTuan(p, idOption);
            } else if (idMenu == MENU_GYM) {
                bao(p, GymLauDai.thachDau(p, idOption));
            } else if (idMenu >= NhiemVuEngine.MENU_DAU && idMenu <= NhiemVuEngine.MENU_CUOI) {
                NhiemVuEngine.doMenu(p, idMenu, idOption);
            } else if (idMenu == CuaHangLauDai.MENU_NHOM || idMenu == CuaHangLauDai.MENU_DONG) {
                CuaHangLauDai.doMenu(p, idMenu, idOption);
            } else if (idMenu == MENU_QUA_HOAT_DONG) {
                bao(p, QuaHoatDong.nhan(p, idOption));
            } else if (idMenu == MENU_HAM_NGUC) {
                hamNguc(p, h, idOption);
            } else if (PhoBanGame.doMenu(p, idMenu, idOption) || CupGame.doMenu(p, idMenu, idOption)) {
                return; // 0.9.7 Pho ban Ham nguc (60..61), Cup PvP tuan (70..72)
            } else if (idMenu == com.htbb.pokemon.HuongDanTanThu.MENU_MOI || idMenu == com.htbb.pokemon.HuongDanTanThu.MENU_NPC) {
                HuongDanTanThuGame.doMenu(p, idMenu, idOption);
            }
        } catch (final Exception e) {
            System.out.println("SoTayLauDai.doMenu: " + e);
        }
    }

    static void xemNangLuong(final Char p, final HoSoLauDai h) {
        final long bay = com.htbb.pokemon.DongHo.bay();
        final int hs = NangLuong.heSoHienTai(h, bay);
        final long phut = h.nlNgay == NangLuong.ngay(bay) ? h.nlMsDung / 60000L : 0L;
        final int tran = CapDoLauDai.tranCap(LauDai.ngayHienTai(), LevelDetail.expMain.length);
        final String s = "Năng lượng huấn luyện\nĐã đánh quái hôm nay: " + phut + " phút\nGiờ 100% hôm nay: "
                + h.nlPhut100 + " phút\nTốc độ EXP hiện tại: " + hs + "%, xu/đồ rơi: " + NangLuong.heSoXuHienTai(h, bay) + "%\n"
                + com.htbb.pokemon.TangToc.trangThai(h, NangLuong.ngay(bay), com.htbb.pokemon.NhiemVuLauDai.tuan(bay))
                + (LauDai.tranTheoMua() ? "\n\nTrần cấp mùa này: " + tran : "\n\nCấp tối đa: " + tran)
                + "\nTinh thông: " + h.ttDiem + " điểm (+" + (CapDoLauDai.tinhThongPhanNghin(h.ttDiem) / 10.0) + "% "
                + (h.ttLoai == 0 ? "tấn công" : "HP") + ")";
        bao(p, s);
        p.sendMessage(MessageCreator.createMsgMenu(new String[]{"Tinh thông: cộng tấn công", "Tinh thông: cộng HP"}, NPC_ID, MENU_TINH_THONG));
    }

    // ------------------------------------------------------------------ nhiem vu tuan

    static List<Object[]> danhSachNhan;

    static void moTuan(final Char p) {
        final List<String[]> dang = NhiemVuLauDaiGame.trangThai(p);
        final List<Object[]> nhan = NhiemVuLauDaiGame.coTheNhan(p);
        final String[] menu = new String[dang.size() + Math.min(10, nhan.size())];
        int i = 0;
        for (final String[] d : dang) {
            menu[i++] = d[1];
        }
        for (int j = 0; j < Math.min(10, nhan.size()); j++) {
            menu[i++] = "Nhận: " + nhan.get(j)[1] + " (" + nhan.get(j)[3] + " quái)";
        }
        if (menu.length == 0) {
            bao(p, "Chưa có nhiệm vụ tuần cho cấp của bạn (từ cấp 20).");
            return;
        }
        p.sendMessage(MessageCreator.createMsgMenu(menu, NPC_ID, MENU_TUAN));
    }

    static void chonTuan(final Char p, final int idOption) {
        final List<String[]> dang = NhiemVuLauDaiGame.trangThai(p);
        if (idOption < 0) {
            return;
        }
        if (idOption < dang.size()) {
            bao(p, NhiemVuLauDaiGame.nhanThuong(p, dang.get(idOption)[0]));
            return;
        }
        final List<Object[]> nhan = NhiemVuLauDaiGame.coTheNhan(p);
        final int j = idOption - dang.size();
        if (j < nhan.size() && j < 10) {
            bao(p, NhiemVuLauDaiGame.nhanTuan(p, (String) nhan.get(j)[0]));
        }
    }

    // ------------------------------------------------------------------ ham nguc bi an

    /**
     * Ban toi thieu (GD2 7.4): chua co ban do rieng (viec doi map). Moi tang = ha so quai +-5
     * cap theo hat giong ngay (CuonHut.tangHamNguc) o bat ky dau; tang 5/10/15.. cho thuong
     * (luong khoa + da), 1 luot thuong / ngay; bang xep hang tang o tob_ham_nguc.
     */
    static void moHamNguc(final Char p, final HoSoLauDai h) {
        final int hom = NangLuong.ngay(com.htbb.pokemon.DongHo.bay());
        synchronized (h) {
            if (h.hnNgay != hom) {
                h.hnNgay = hom;
                h.hnTangCao = 0;
                h.hnDaThuong = 0;
                h.ban = true;
            }
        }
        final int tang = h.hnTangCao + 1;
        final int[] t = CuonHut.tangHamNguc(hom, tang, new int[]{0});
        bao(p, "Hầm ngục bí ẩn hôm nay\nĐã qua tầng: " + h.hnTangCao + "\nTầng " + tang
                + ": hạ " + t[1] + " quái chênh không quá 5 cấp" + (t[2] == 1 ? " (tầng thưởng)" : "")
                + "\nTiến độ: " + HamNgucGame.tienDo(p) + "/" + t[1]);
        p.sendMessage(MessageCreator.createMsgMenu(new String[]{"Vào tầng " + tang, "Nhận thưởng hôm nay", "Phó bản tổ đội (5 tầng)"}, NPC_ID, MENU_HAM_NGUC));
    }

    static void hamNguc(final Char p, final HoSoLauDai h, final int idOption) {
        if (idOption == 0) {
            if (!com.htbb.cauhinh.CongTac.hamNguc()) {
                bao(p, "Hầm ngục đang tạm đóng. Phần thưởng đã đạt vẫn nhận được."); // ham_nguc.bat=0
                return;
            }
            HamNgucGame.batDau(p);
            bao(p, "Đã vào tầng " + (h.hnTangCao + 1) + ". Hạ quái chênh không quá 5 cấp để qua tầng.");
        } else if (idOption == 1) {
            bao(p, HamNgucGame.nhanThuong(p, h));
        } else if (idOption == 2) {
            PhoBanGame.moMenu(p); // 0.9.7: phong rieng 5 tang cho to doi (real.PhoBanGame)
        }
    }

    // ------------------------------------------------------------------ PvP / su pham / dang nhap

    static void xemPvp(final Char p, final HoSoLauDai h) {
        final int mua = ThangPvp.mua(LauDai.ngayHienTai());
        bao(p, "Thang PvP mùa " + (mua + 1) + "\nElo: " + h.pvpElo + " - bậc " + ThangPvp.TEN_BAC[ThangPvp.bac(h.pvpElo)]
                + "\nThắng/thua: " + h.pvpThang + "/" + h.pvpThua + "\nChỉ tính trận cùng dải 10 cấp.");
    }

    static void suPham(final Char p) {
        if (p.party == null || p.party.userParty == null) {
            bao(p, "Hãy lập nhóm với trò trước.");
            return;
        }
        int n = 0;
        for (int i = 0; i < p.party.userParty.size(); i++) {
            final Char t = p.party.userParty.get(i);
            if (t == null || t.id == p.id || !CuonHut.ghepSuPham(p.lvDetail.lv, t.lvDetail.lv)) {
                continue;
            }
            final HoSoLauDai ht = LauDaiNhanVat.lay(t);
            if (ht == null || ht.thayId != 0) {
                continue;
            }
            synchronized (ht) {
                ht.thayId = p.charDBID;
                ht.moSuPham = 0;
                ht.ban = true;
            }
            DatabaseLauDai.ghiSuPham(t.charDBID, p.charDBID, "");
            NhiemVuEngine.suKien(p, "kem_cap", "", 1);
            LauDaiNhanVat.bao(t, p.charname + " đã nhận bạn làm trò (+20% EXP khi cùng nhóm).");
            n++;
        }
        bao(p, n > 0 ? "Đã nhận " + n + " trò." : "Cần từ cấp 30 trở lên, trò thấp hơn ít nhất 20 cấp, chưa có thầy và ở cùng nhóm.");
    }

    static void xemDangNhap(final Char p, final HoSoLauDai h) {
        NhiemVuLauDaiGame.capNhatVe(h);
        final int[] d = QuaHoatDong.lay(p);
        final int buoc = d == null ? 0 : d[3];
        bao(p, "Lịch đăng nhập (theo tài khoản): ngày " + buoc + "/7 (ngày tới: " + KinhTeLauDai.luongKhoaDangNhap(buoc)
                + " lượng khoá). Nghỉ 1 ngày vẫn giữ chuỗi.\nLịch tháng: " + (d == null ? 0 : d[6]) + "/20 ngày.\nVé chương: " + h.veChuong + "/" + ThongSoLauDai.VE_CHUONG_TOI_DA
                + " (+2 mỗi thứ Hai 05:00).");
    }

    // ------------------------------------------------------------------ len cap (su pham)

    /** Goi tu MapXuLyLenh khi len cap: thuong moc su pham cho tro + thay (lan dau dat moc). */
    static void khiLenCap(final Char p, final int capCu, final int capMoi) {
        if (!LauDai.bat()) {
            return;
        }
        NhiemVuEngine.datMuc(p, "thanh_tuu_cap", capMoi);
        final HoSoLauDai h = LauDaiNhanVat.lay(p);
        if (h == null || h.thayId == 0) {
            return;
        }
        final int moc = CuonHut.mocSuPham(capCu, capMoi);
        if (moc < 0) {
            return;
        }
        final int bit = moc == 20 ? 1 : 2;
        synchronized (h) {
            if ((h.moSuPham & bit) != 0) {
                return;
            }
            h.moSuPham |= bit;
            h.ban = true;
        }
        final String thuong = "LUONGKHOA:10,GEM:" + RoiLauDai.ID_DA + ":2:-1";
        try {
            DatabaseGiftCode.applyItemString(p, thuong);
            LauDaiNhanVat.bao(p, "Sư phạm: đạt cấp " + moc + ", nhận 10 lượng khoá + 2 đá.");
            for (final Char t : CharManager.instance.vChars) {
                if (t != null && t.charDBID == h.thayId) {
                    DatabaseGiftCode.applyItemString(t, thuong);
                    LauDaiNhanVat.bao(t, "Trò " + p.charname + " đạt cấp " + moc + ": nhận 10 lượng khoá + 2 đá.");
                    break;
                }
            }
            Database.instance.saveOrtherLog("", p.charname, "su pham moc " + moc + " thay " + h.thayId, "laudai");
        } catch (final Exception e) {
            System.out.println("SoTayLauDai.khiLenCap: " + e);
        }
    }
}
