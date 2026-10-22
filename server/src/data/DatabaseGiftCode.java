package data;


import real.Map;
import real.*;
import server.TeamServer;
import java.io.*;
import java.sql.*;

import static real.Char.ID_MAT_NA;
import static real.Char.doDropModelClothe;

import real.plugins.GiftCode;

/**
 * Than cac method giftcode, su kien va migrate cau truc bang: doi giftcode,
 * nap cau hinh su kien, kiem tra qua da nhan, them cot moi cho bang thong tin.
 *
 * Tach tu data/Database.java. Cac method VAN CON trong Database, giu NGUYEN
 * chu ky cu, chi than chuyen sang day.
 * Tham so "Database db" thay cho "this" truoc day.
 */
public class DatabaseGiftCode {

    /**
     * Them cot `userId` vao giftcode_log neu chua co, roi doi chieu userId cho cac dong cu dua
     * theo ten nhan vat con ton tai trong tob_char.
     *
     * VI SAO CAN: truoc day chan nhan lai mot ma tinh theo TEN NHAN VAT (cot `player`), nen mot
     * tai khoan nhieu nhan vat nhan duoc nhieu lan cung mot ma giftcode - xem newDoGetGiftCode()
     * duoi day. Sua lai chan theo `userId` (tai khoan), nhung bang cu chua co cot nay.
     *
     * Doi chieu (UPDATE ... WHERE userId = 0) la IDEMPOTENT nen goi lai nhieu lan khong hai gi;
     * nhan vat da bi xoa thi dong log cu giu nguyen userId = 0 - khong con hai vi nhan vat do
     * khong con ton tai de nhan lai. Goi 1 lan luc khoi dong server (xem TeamServer), TRUOC khi
     * nhan nguoi choi vao, cung nhom voi Database.themCotCongTacHienThi().
     */
    static void chuanBiCotUserId(Database db) {
        Connection conn = null;
        Statement stmt = null;
        ResultSet rs = null;
        try {
            conn = db.getConnection();
            stmt = conn.createStatement();
            rs = stmt.executeQuery("SHOW COLUMNS FROM giftcode_log LIKE 'userId'");
            final boolean daCo = rs.next();
            rs.close();
            rs = null;
            if (!daCo) {
                stmt.execute("ALTER TABLE giftcode_log ADD COLUMN userId INT(11) NOT NULL DEFAULT 0 AFTER player");
                System.out.println("[GIFTCODE] Da them cot giftcode_log.userId.");
            }
            final int soDong = stmt.executeUpdate(
                    // Subquery tuong quan thay cho "UPDATE .. JOIN .." (NovaDB khong ho tro
                    // UPDATE nhieu bang); EXISTS giu dung nghia INNER JOIN: dong khong co
                    // nhan vat tuong ung thi khong dong toi.
                    "UPDATE giftcode_log SET userId = (SELECT tc.userId FROM tob_char tc"
                    + " WHERE tc.charname = giftcode_log.player LIMIT 1)"
                    + " WHERE userId = 0 AND EXISTS (SELECT 1 FROM tob_char tc WHERE tc.charname = giftcode_log.player)");
            if (soDong > 0) {
                System.out.println("[GIFTCODE] Da doi chieu userId cho " + soDong + " dong giftcode_log cu.");
            }
        } catch (Exception e) {
            System.out.println("[GIFTCODE] loi chuan bi cot userId cho giftcode_log: " + e);
        } finally {
            try {
                if (rs != null) {
                    rs.close();
                }
            } catch (Exception ignored) {
            }
            try {
                if (stmt != null) {
                    stmt.close();
                }
            } catch (Exception ignored) {
            }
            try {
                Database.connPool.free(conn);
            } catch (Exception ignored) {
            }
        }
    }

    /**
     * Them UNIQUE KEY(giftcode,userId) vao giftcode_log neu chua co - lam luoi an toan thu
     * hai o tang DB cho cung dieu kien "da nhan chua" ma newDoGetGiftCode() da kiem bang
     * SELECT-roi-INSERT (khong nguyen tu). Rui ro ly thuyet: hai phien REDEEM cung luc cho
     * cung mot userId deu SELECT thay "chua dung" truoc khi ben nao kip INSERT. Thuc te hien
     * bi chan o tang dang nhap (CharManager.put() tu choi mot userId online 2 lan), nhung
     * khoa nay giu an toan ke ca sau nay logic do doi khac.
     *
     * Neu du lieu cu con dong trung (giftcode,userId) - vi du nhieu nhan vat DA BI XOA cung
     * nhan mot ma, chuanBiCotUserId() doi chieu tat ca ve userId=0 - ALTER TABLE se bao loi
     * duplicate entry va bi bat lai o day (khong lam sap server, chi log). Xem
     * tools/luu_tru_sinh_du_lieu/sql_nguon/them_khoa_duy_nhat_giftcode_log.sql de don du lieu trung roi them khoa bang tay.
     */
    static void chuanBiKhoaDuyNhatGiftCodeLog(Database db) {
        Connection conn = null;
        Statement stmt = null;
        ResultSet rs = null;
        try {
            conn = db.getConnection();
            stmt = conn.createStatement();
            rs = stmt.executeQuery("SHOW INDEX FROM giftcode_log WHERE Key_name = 'uk_giftcode_userid'");
            final boolean daCo = rs.next();
            rs.close();
            rs = null;
            if (!daCo) {
                stmt.execute("ALTER TABLE giftcode_log ADD UNIQUE KEY uk_giftcode_userid (giftcode(191), userId)");
                System.out.println("[GIFTCODE] Da them UNIQUE KEY(giftcode,userId) cho giftcode_log.");
            }
        } catch (Exception e) {
            System.out.println("[GIFTCODE] Chua them duoc UNIQUE KEY(giftcode,userId) cho giftcode_log"
                    + " (co the do du lieu cu con dong trung - xem tools/luu_tru_sinh_du_lieu/sql_nguon/them_khoa_duy_nhat_giftcode_log.sql): " + e);
        } finally {
            try {
                if (rs != null) {
                    rs.close();
                }
            } catch (Exception ignored) {
            }
            try {
                if (stmt != null) {
                    stmt.close();
                }
            } catch (Exception ignored) {
            }
            try {
                Database.connPool.free(conn);
            } catch (Exception ignored) {
            }
        }
    }

    static void newDoGetGiftCode(Database db, String text, Char player) {
        if (com.htbb.pokemon.ThanhVien.chanVaBao(player, "giftcode")) { // thanh vien: nhap giftcode
            return;
        }

        // SECURITY: gift codes are alphanumeric only. Strip everything else to prevent SQL injection,
        // since 'text' is concatenated into SQL below.
        if (text == null) {
            return;
        }
        text = text.replaceAll("[^a-zA-Z0-9_-]", "");
        if (text.isEmpty()) {
            player.sendMessage(MessageCreator.createServerAlertMessage("Mã quà tặng không hợp lệ.", ""));
            return;
        }

        // Gian cach giua 2 lan go ma. Client goi thang duoc goi -31 (idMenuInputText = 7)
        // nen truoc day mot vong lap se vua DO MA (ma chi la chu va so, doan duoc)
        // vua bat moi lan doan la vai cau truy van xuong database — day chinh la
        // ket noi ma ca may chu game dung chung, nghen la ca server cham theo.
        // Im lang bo qua, khong tra loi gi: tra loi cung la mot goi tin, va con
        // cho biet minh dang bi chan.
        if (System.currentTimeMillis() - player.timeNhapGiftCode < Char.GIFTCODE_COOLDOWN_MS) {
            return;
        }
        player.timeNhapGiftCode = System.currentTimeMillis();

        // Ma test "sv.testCode" (100 trieu xu + 10.000 luong) da XOA (GD2, 17/10/2026): mot dong cau
        // hinh dat nham tren VPS la mo cua in tien. Kiem thu dung bang tob_giftcode + lenh GM.

        // GD2 / yeu cau 17/10/2026: bat co sv.lau_dai -> he giftcode AN TOAN moi (bang tob_giftcode,
        // xem GiftcodeAnToan). Bang giftcode cu (cho xu/luong) KHONG con duoc doc.
        if (com.htbb.pokemon.LauDai.bat()) {
            real.GiftcodeAnToan.nhap(player, text);
            return;
        }

        int userId = player.userID;
        if (!Database.check_active_putcode(userId)) {
            player.sendMessage(MessageCreator.createServerAlertMessage("Vui lòng kích hoạt tài khoản để sử dụng." + userId, ""));
            return;
        }

        Connection conn = null;
        PreparedStatement pre1 = null;
        ResultSet rs = null;
        try {
            conn = db.getConnection();

            // Chan theo TAI KHOAN (userId), khong phai theo tung nhan vat: truoc day dieu kien
            // la "player = charname" nen mot tai khoan nhieu nhan vat nhan duoc nhieu lan cung
            // mot ma. Bang giftcode_log duoc tu them cot userId luc khoi dong, xem chuanBiCotUserId().
            pre1 = conn.prepareStatement("select * from giftcode_log where giftcode = ? AND userId = ?");
            pre1.setString(1, text);
            pre1.setInt(2, userId);
            rs = pre1.executeQuery();
            if (rs.next()) {
                player.sendMessage(MessageCreator.createServerAlertMessage("Tài khoản của bạn đã nhập mã quà tặng này rồi.", ""));
                return;
            }
            // Ma "cua sau" (petmax, xuluong, alldanhhieu, ...) da XOA HAN (yeu cau 17/10/2026, GD2):
            // truoc day nam trong mot khoi if (false) - xoa de khong ai bat lai duoc.

            // Tham so bind thay cho noi chuoi (CODING_CONVENTION muc 13).
            pre1.close();
            pre1 = conn.prepareStatement("select * from giftcode where giftcode = ? and type = 0 limit 1");
            pre1.setString(1, text);
            rs = pre1.executeQuery();

            if (rs.next()) {
                int xu = rs.getInt("xu");
                int luong = rs.getInt("luong");
                int luongkhoa = rs.getInt("luongLock");
                String item = rs.getString("item");
                String code = rs.getString("giftcode");
                int limit = rs.getInt("limit_use");

                if (limit == 0) {
                    player.sendMessage(MessageCreator.createServerAlertMessage("Mã Quà Tặng đã hết lượt sử dụng.", ""));
                    return;
                }
                // GD2 5.4: bat co sv.lau_dai -> toi da 2 ma/thang, chi vat pham (bo xu/luong).
                final String chanLauDai = real.GiftcodeLauDai.choPhep(player);
                if (chanLauDai != null) {
                    player.sendMessage(MessageCreator.createServerAlertMessage(chanLauDai, ""));
                    return;
                }
                if (com.htbb.pokemon.LauDai.bat()) {
                    xu = 0;
                    luong = 0;
                    luongkhoa = 0;
                }
                // 19/10/2026: kiem o trong TRUOC khi tru luot (truoc day ma bi tieu roi moi cap, tui day thi mat qua
                // hoac tran tui). Uoc luong tren: moi muc 1 o, muc DB: so luong; danh hieu / pet khong vao tui.
                final int canO = uocLuongO(item);
                if (canO > 0 && player.countSlotInventory() < canO) {
                    player.sendMessage(MessageCreator.createServerAlertMessage("Hành trang cần " + canO + " ô trống để nhận quà.", ""));
                    return;
                }

                // GHI NHAN DA DUNG + TRU LUOT TRUOC, cap thuong SAU. Truoc day thu tu nguoc
                // lai: neu bat ky buoc cap thuong nao o duoi nem ngoai le (id vat pham khong
                // ton tai trong Map.itemTemplates -> NPE, chuoi cau hinh thieu truong ->
                // ArrayIndexOutOfBounds, id thuoc >= 162 -> AIOOBE tren potions[]...) thi khoi
                // catch cuoi ham nuot loi, dong giftcode_log KHONG duoc ghi va limit_use KHONG
                // giam - nguoi choi da nhan xu/luong/vat phamma van nhap lai duoc VO HAN LAN.
                // Cau UPDATE co "AND limit_use > 0" va kiem so dong sua duoc: hai nguoi cung
                // nhap mot ma chi con 1 luot trong cung khoanh khac (hai session khac nhau,
                // -31 la lenh rieng nen chay tren luong phien cua tung nguoi) khong the ca hai
                // cung tru thanh cong.
                if (limit >= 1) {
                    String sql_updatelimit = "UPDATE giftcode SET limit_use = limit_use - 1 WHERE giftcode = ? AND limit_use > 0";
                    PreparedStatement preLimit = conn.prepareStatement(sql_updatelimit);
                    preLimit.setString(1, code);
                    int soDongSua = preLimit.executeUpdate();
                    preLimit.close();
                    if (soDongSua != 1) {
                        player.sendMessage(MessageCreator.createServerAlertMessage("Mã Quà Tặng đã hết lượt sử dụng.", ""));
                        return;
                    }
                }
                real.GiftcodeLauDai.ghiNhan(player); // GD2 5.4 (khong lam gi khi tat co)
                PreparedStatement preLog = conn.prepareStatement(
                        "insert into giftcode_log set player = ?, userId = ?, giftcode = ?, item = ?, xu = ?, luong = ?, luongK = ?");
                preLog.setString(1, player.charname);
                preLog.setInt(2, userId);
                preLog.setString(3, text);
                preLog.setString(4, item);
                preLog.setInt(5, xu);
                preLog.setInt(6, luong);
                preLog.setInt(7, luongkhoa);
                preLog.execute();
                preLog.close();

                if (xu != 0) {
                    player.addXu(xu, "nhan tu giftcode" + code);
                }
                if (luong != 0) {
                    player.addLuong(luong);
                }

                if (luongkhoa != 0) {
                    player.addLuongLock(luongkhoa);
                }

                applyItemString(player, item);

                player.sendMessage(MessageCreator.createCharInventoryMessage(player, 0));
                player.sendMessage(MessageCreator.createServerAlertMessage("Chúc mừng bạn đã nhận được phần quà từ: " + code + ".", ""));
                Database.instance.saveOrtherLog("", player.charname, "CODE: " + code, "putGiftCode");
                // (giftcode_log da ghi va limit_use da giam - neu co - NGAY TRUOC khi cap thuong o tren)
            } else {
                player.sendMessage(MessageCreator.createServerAlertMessage("Mã quà tặng không tồn tại.", ""));
            }
            // System.out.println("\n" + rs.toString() + "\n");
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                Database.connPool.free(conn);
            } catch (Exception exception) {
            }
            try {
                pre1.close();
            } catch (Exception exception) {
            }
        }
    }

    /**
     * Ap dung mot chuoi phan thuong (cu phap ITEM:id:soLuong, GEM:id:soLuong:khoa, ...
     * xem HTBB-Web shared/utils/thuong.ts loaiThuong()) cho mot nhan vat.
     *
     * TACH RA tu newDoGetGiftCode() (chinh xac cung ~60 nhanh if/else, khong sua logic
     * nao) de dung lai cho cac nguon phat thuong KHAC ngoai giftcode - vd thuong dua
     * top tu trang quan tri (xem real/SuKienDuaTop.java). Ham nay KHONG dung gi den
     * giftcode/giftcode_log/limit_use - noi goi tu lo ghi log/tru luot rieng.
     */
    /** So o hanh trang (uoc luong tren) chuoi qua {@code item} can - xem applyItemString. */
    public static int uocLuongO(final String item) {
        if (item == null || item.equals("NULL") || item.trim().isEmpty()) {
            return 0;
        }
        int can = 0;
        for (final String muc : item.trim().split(",")) {
            final String[] t = muc.trim().split(":");
            if (t.length == 0 || t[0].isEmpty()) {
                continue;
            }
            if (t[0].equals("DANHHIEU2") || t[0].equals("DH") || t[0].equals("PET") || t[0].equals("PETV")) {
                continue;
            }
            int so = 1;
            if (t[0].equals("DB") && t.length > 2) {
                try {
                    so = Math.max(1, Math.min(1000, Integer.parseInt(t[2].trim())));
                } catch (final NumberFormatException ignored) {
                }
            }
            can += so;
        }
        return can;
    }

    public static void applyItemString(final Char player, final String item) {
        if (item == null || item.equals("NULL") || item.isEmpty()) {
            return;
        }
        String[] tachItem = item.trim().split(",");
        for (int i = 0; i < tachItem.length; i++) {
            String[] tachTime = tachItem[i].trim().split(":");
            if (tachTime[0].equals("DB")) {
                            for (int j = 0; j < Integer.parseInt(tachTime[2]); j++) {
                                GiftCode.AddItem(player, Integer.parseInt(tachTime[1]), 0, 0, Integer.parseInt(tachTime[3]));
                            }
                        } else if (tachTime[0].equals("EGG")) {
                            for (int j = 0; j < Integer.parseInt(tachTime[2]); j++) {
                                player.createTrungPhuongHoang(1L, Integer.parseInt(tachTime[1]));
                            }

                        } else if (tachTime[0].equals("DANHHIEU2")) {
                            for (int j = 0; j < Integer.parseInt(tachTime[2]); j++) {
                                player.addDanhHieu(Short.parseShort(tachTime[1]), Long.parseLong(tachTime[3]));
                            }
                            try {
                                player.sendMessage(MessageCreator.createMainCharInfoMessage(player));
                                player.sendToNearPlayer(MessageCreator.createCharInfo(player));
                            } catch (IOException ex) {
                            }
                        } else if (tachTime[0].equals("PET")) {
                            for (int j = 0; j < Integer.parseInt(tachTime[2]); j++) {
                                player.createPet(Integer.parseInt(tachTime[1]), Integer.parseInt(tachTime[3]));
                            }
                        } else if (tachTime[0].equals("PETV")) {
                           
                                player.createPetVinhVien(Integer.parseInt(tachTime[1]));
                        
                        } else if (tachTime[0].equals("DH")) {
                            // Danh sách ID danh hiệu có sẵn
                            int[] availableTitles = {
                                8767, 8768, 8769, 8770, 8771, 8772, 8773, 8774, 8775, 8776, 8777,
                                8778, 8779, 8785, 8786, 8787, 8788, 8900, 8901, 8902, 8903, 8904,
                                8905, 8906, 8907, 8908, 8909, 8910, 8911, 8924, 8925, 8935, 8956, 8957
                            };
                            
                            // Nếu ID = 0, chọn ngẫu nhiên một danh hiệu
                            int titleId = Integer.parseInt(tachTime[1]);
                            if (titleId == 0) {
                                titleId = availableTitles[Map.r.nextInt(availableTitles.length)];
                            }
                            
                            for (int j = 0; j < Integer.parseInt(tachTime[2]); j++) {
                                player.addDanhHieu(titleId, Long.parseLong(tachTime[3]));
                            }
                            try {
                                player.sendMessage(MessageCreator.createMainCharInfoMessage(player));
                                player.sendToNearPlayer(MessageCreator.createCharInfo(player));
                            } catch (IOException ex) {
                            }

                        } else if (tachTime[0].equals("HLT")) {
                            for (int j = 0; j < Integer.parseInt(tachTime[2]); j++) {
                                Map.doCreateHuyetLinhThao(player, Integer.parseInt(tachTime[1]));
                            }
                         
                        } else if (tachTime[0].equals("PPR")) {
                            for (int j = 0; j < Integer.parseInt(tachTime[2]); j++) {
                                player.createtemCoat(Integer.parseInt(tachTime[1]), player, Integer.parseInt(tachTime[4]), false, Integer.parseInt(tachTime[3]));
                            }
                        } else if (tachTime[0].equals("PPRK")) {
                            for (int j = 0; j < Integer.parseInt(tachTime[2]); j++) {
                                player.createtemCoat_chonkhang(Integer.parseInt(tachTime[1]), player, Integer.parseInt(tachTime[4]), false, Integer.parseInt(tachTime[3]), Integer.parseInt(tachTime[5]));
                            }
                        } else if (tachTime[0].equals("PPV")) {
                            for (int j = 0; j < Integer.parseInt(tachTime[2]); j++) {
                                player.createtemCoat(Integer.parseInt(tachTime[1]), player, Integer.parseInt(tachTime[4]), true, Integer.parseInt(tachTime[3]));
                            }
                        } else if (tachTime[0].equals("PPVK")) {
                            for (int j = 0; j < Integer.parseInt(tachTime[2]); j++) {
                                player.createtemCoat_chonkhang(Integer.parseInt(tachTime[1]), player, Integer.parseInt(tachTime[4]), true, Integer.parseInt(tachTime[3]), Integer.parseInt(tachTime[5]));
                            }
                        } else if (tachTime[0].equals("PPLUCK")) {
                            player.createtemCoat(Integer.parseInt(tachTime[1]), player, Integer.parseInt(tachTime[4]), true, 0);
                        } else if (tachTime[0].equals("PT")) {
                            int idGem = Integer.parseInt(tachTime[1]);
                            int soluong = Integer.parseInt((tachTime[2]));
                            player.potions[idGem] = player.potions[idGem] + soluong;
                            player.calculateAttrib();
                            try {
                                player.sendMessage(MessageCreator.createMainCharInfoMessage(player));
                                player.sendMessage(MessageCreator.createCharInventoryMessage(player, 0));
                            } catch (IOException ex) {
                            }
                        } else if (tachTime[0].equals("GEM")) {
                            boolean lockitemgem = true;
                            try {
                                lockitemgem = (Integer.parseInt(tachTime[3]) < 0 ? true : false);
                            } catch (Exception e) {
                                lockitemgem = true;
                                e.printStackTrace();
                            }
                            // GEM:ID:SL
                            player.doAddGemItem(Integer.parseInt(tachTime[1]), Integer.parseInt(tachTime[2]), lockitemgem);
                            player.sendMessage(MessageCreator.createCharGemItem(player));
                        } else if (tachTime[0].equals("DN")) {
                            player.diemNapVip += Integer.parseInt(tachTime[2]);
                            // Bat buoc luu xuong tob_nap_xai_luong: luc dang nhap
                            // getDiemNapXaiLuong() se ghi de diemNapVip bang gia tri
                            // trong bang do, khong luu la mat ngay khi thoat ra vao lai.
                            Database.instance.addCharXaiNapLuong(player, 0);
                        } else if (tachTime[0].equals("MN")) {
                            player.luongNap += Integer.parseInt(tachTime[2]);
                        } else if (tachTime[0].equals("DIEMDT")) {
                            // Diem doi thuong — cung kho diem ma NPC "doi thuong" tieu
                            // (Char.diemDoiThuong), va cung kho ma van tieu / cuop tieu /
                            // giet quai cong vao.
                            //
                            // KHONG dung cham diemQuaiHomNay: tran 1000 diem/ngay o
                            // Map.capNhatDiemGietQuai chi chan nguon GIET QUAI, khong phai
                            // chan tong diem. Giftcode la nguon khac, admin phat bao nhieu
                            // la co y bay nhieu.
                            //
                            // Khong phai ghi DB rieng nhu DN: diemDoiThuong nam trong chuoi
                            // pInfo (CharVongLap:913, doc lai o CharNapDuLieu:224) nen no di
                            // theo duong luu nhan vat binh thuong.
                            final int themDiem = Integer.parseInt(tachTime[1]);
                            player.diemDoiThuong += themDiem;
                            player.sendMessage(MessageCreator.createServerAlertMessage(
                                    "Bạn nhận được " + themDiem + " điểm đổi thưởng (tổng: "
                                            + player.diemDoiThuong + " điểm).", ""));
                        } else if (tachTime[0].equals("PP")) {
                            Char.createPhiPhongMaxBaoKich(player, 0, Integer.parseInt(tachTime[3]), (Item) null, false, false, 0, true);
                            Char.createPhiPhongMaxBaoKich(player, 1, Integer.parseInt(tachTime[3]), (Item) null, false, false, 0, true);
                        } else if (tachTime[0].equals("PPLUCK")) {
                            player.createtemCoat(Integer.parseInt(tachTime[1]), player, Integer.parseInt(tachTime[4]), true, 0);
                        } else if (tachTime[0].equals("PP8X")) {
                            player.createPhiPhongMaxBaoKich(player, 0, Integer.parseInt(tachTime[3]), (Item) null, true, true, 5, true);
                            player.createPhiPhongMaxBaoKich(player, 1, Integer.parseInt(tachTime[3]), (Item) null, true, true, 5, true);
                        } else if (tachTime[0].equals("VKTT")) {
                            player.createVuKhiThoiTrang(player, 0, Integer.parseInt(tachTime[3]), (Item) null, false, true, 1, false);
                        } else if (tachTime[0].equals("VKID")) {

                        player.createVuKhiThoiTrangID(player, Integer.parseInt(tachTime[1]), 0, Integer.parseInt(tachTime[2]), (Item) null, false, true, 1, true);

                        } else if (tachTime[0].equals("VKMAX")) {
                            player.createVuKhiThoiTrang(player, 0, Integer.parseInt(tachTime[3]), (Item) null, true, true, 1, false);
                        } else if (tachTime[0].equals("VKTB")) {
                            player.createVuKhiThoiTrangTHANBINH_NEW(player, 0, Integer.parseInt(tachTime[3]), (Item) null, false, true, 1, false);
                        } else if (tachTime[0].equals("VKTBMAX")) {
                            player.createVuKhiThoiTrangTHANBINH_NEW(player, 0, Integer.parseInt(tachTime[3]), (Item) null, true, true, 1, false);
                        } else if (tachTime[0].equals("VKTBLV")) {
                            int thoihan = Integer.parseInt(tachTime[3]) * 24 * 60;
                            player.createVuKhiThoiTrangTHANBINH_LV(player, 0, thoihan, null, true, Integer.parseInt(tachTime[1]), Integer.parseInt(tachTime[2]), false);
                        } else if (tachTime[0].equals("BT")) {
                            player.doCreatePetBachThu(Integer.parseInt(tachTime[3]), false);
                        } else if (tachTime[0].equals("AOTT")) {
                            Map.doBuyModelClothe(player, Integer.parseInt(tachTime[1]), Integer.parseInt(tachTime[3]), 1);
                        } else if (tachTime[0].equals("AOSHOP")) {
                            doDropModelClothe(player, Integer.parseInt(tachTime[1]), Integer.parseInt(tachTime[3]));
                        } else if (tachTime[0].equals("CHOI")) {
                            player.createChoi(Integer.parseInt(tachTime[1]), player, Integer.parseInt(tachTime[3]), Integer.parseInt(tachTime[4]), true);
                        } else if (tachTime[0].equals("HOVELAN")) {
                            player.createHoVeLenhLan(Integer.parseInt(tachTime[1]), player, Integer.parseInt(tachTime[3]), 1);
                        } else if (tachTime[0].equals("TRANGPHUC")) {
                            // Format: TRANGPHUC:itemID:quantity:days
                            for (int j = 0; j < Integer.parseInt(tachTime[2]); j++) {
                                player.doDropModelClothe(player, Integer.parseInt(tachTime[1]), Integer.parseInt(tachTime[3]));
                            }
                        } else if (tachTime[0].equals("LUONG")) {
                            // GD2 2.12: luong tu xep hang tuan (tob_top_thuong, THIET_KE 2.1: top 1 50,
                            // top 10 20, top 100 8, top 1000 3 - KinhTeLauDai.luongXepHangTuan).
                            player.addLuong(Math.max(0, Integer.parseInt(tachTime[1])));
                        } else if (tachTime[0].equals("LUONGKHOA")) {
                            player.addLuongLock(Math.max(0, Integer.parseInt(tachTime[1])));
                        } else if (tachTime[0].equals("ITEM")) {
                            player.createItemSoLuong(Integer.parseInt(tachTime[1]), Integer.parseInt(tachTime[2]), player);
                        } else if (tachTime[0].equals("MATNA")) {
                            int[] valuemn = {5, 5, 10, 30, 50};
                            int randomMN = Map.r.nextInt(valuemn.length);
                            if (Integer.parseInt(tachTime[1]) > -1) {
                                randomMN = Integer.parseInt(tachTime[1]);
                            }
                            Item newitem = Map.doCreateMatNa(player, Integer.parseInt(tachTime[3]), ID_MAT_NA[randomMN], randomMN, valuemn[randomMN]);
                            player.sendMessage(MessageCreator.createServerAlertMessage("Nhận " + newitem.getName() + " 7 ngày", ""));
                        // ... existing code ...
                    } else if (tachTime[0].equals("MATNA2")) {
                        // Tạo mặt nạ với option và value random
                        int[] optionValues = {5, 5, 10, 30, 50}; // Các giá trị value có thể
                        int randomIndex = Map.r.nextInt(optionValues.length);
                        int matnaID = Integer.parseInt(tachTime[1]); // ID mặt nạ
                        int option = randomIndex; // Option mặt nạ (0-4)
                        int value = optionValues[randomIndex];  // Giá trị option tương ứng
                        Item newitem = Map.doCreateMatNa2(player, Integer.parseInt(tachTime[3]), matnaID, option, value);
                        player.sendMessage(MessageCreator.createServerAlertMessage("Nhận " + newitem.getName() + (Integer.parseInt(tachTime[3]) > 0 ? " " + Integer.parseInt(tachTime[3])/1440 + " ngày" : " vĩnh viễn"), ""));
                    } else if (tachTime[0].equals("MATNAID")) {
                        // MATNAID:idtemplate:option:thoiHan:khoa - khac MATNA2: "option" (0..4,
                        // dung muc %5/5/10/30/50 co san) duoc CHON THAT thay vi luon random,
                        // cho ghep RIENG mau mat na (idtemplate) voi RIENG hieu ung (option) -
                        // vd "Mat na bi ngo" + option 4 "Thuoc tinh Lan" (Item.createOptionMatNa).
                        // "khoa" (them 19/09/2026): 1 = khoa (khong giao dich duoc, nhu MATNA2
                        // luon lam), 0 = khong khoa - MATNA/MATNA2 KHONG co tham so nay, van
                        // luon khoa cung nhu truoc.
                        int[] giaTriTheoOptionMatNa = {5, 5, 10, 30, 50};
                        int idTemplateMatNaId = Integer.parseInt(tachTime[1]);
                        int optionMatNaId = Integer.parseInt(tachTime[2]);
                        int valueMatNaId = (optionMatNaId >= 0 && optionMatNaId < giaTriTheoOptionMatNa.length)
                                ? giaTriTheoOptionMatNa[optionMatNaId] : 0;
                        boolean khoaMatNaId = tachTime.length <= 4 || !"0".equals(tachTime[4]);
                        Item matNaId = Map.doCreateMatNa2(player, Integer.parseInt(tachTime[3]),
                                idTemplateMatNaId, optionMatNaId, valueMatNaId, khoaMatNaId);
                        // Item(...) (Item.java ~712) TU DONG dinh san 1 option rieng cho dung
                        // idtemplate 745-748 (745->0, 746->1, 747->2, 748->3), doc lap voi
                        // option nguoi choi vua chon o tren. MATNA/MATNA2 (id va option luon
                        // random CUNG mot chi so) khong lo vi hai gia tri luon TRUNG nhau;
                        // MATNAID cho chon RIENG id/option nen phai XOA option mac dinh nay
                        // di neu no KHAC option da chon - neu khong se bi dinh them stat cu
                        // (vd chon "Mat na bi ngo" + "Thuoc tinh Lan" (option 4) bi dinh them
                        // ca "Tang cong" cua option 0 - dung id 745 tu dong gan san).
                        if (matNaId != null && idTemplateMatNaId >= 745 && idTemplateMatNaId <= 748) {
                            final int optionMacDinh = idTemplateMatNaId - 745;
                            if (optionMacDinh != optionMatNaId) {
                                matNaId.createOptionMatNa(optionMacDinh, 0);
                                // QUAN TRONG: doCreateMatNa2() da GUI createCharInventoryMessage
                                // cho client TU BEN TRONG no, TRUOC KHI dong xoa o tren kip chay -
                                // client da nhan ban CON DINH stat cu roi. Phai gui lai lan nua
                                // SAU KHI xoa xong thi client moi cap nhat dung; thieu dong nay la
                                // client hien sai (van thay "Tang cong") du server/DB da dung.
                                player.sendMessage(MessageCreator.createCharInventoryMessage(player, 1));
                            }
                        }
                        player.sendMessage(MessageCreator.createServerAlertMessage("Nhận " + matNaId.getName()
                                + (Integer.parseInt(tachTime[3]) > 0 ? " " + Integer.parseInt(tachTime[3]) / 1440 + " ngày" : " vĩnh viễn"), ""));
                    } else if (tachTime[0].equals("RONGBANG")) {
                            player.createRongLon(Integer.parseInt(tachTime[1]), true);
                        } else if (tachTime[0].equals("SUTU")) {
                            player.createSuTu(Integer.parseInt(tachTime[1]), true);
                        }  else if (tachTime[0].equals("SUTUBANG")) {
                            player.createSuTuBang(Integer.parseInt(tachTime[1]), true);  
                        } else if (tachTime[0].equals("DK")) {
                            player.createHeo(0, false);
                        } else if (tachTime[0].equals("DKB")) {
                            player.createHeoBang(Integer.parseInt(tachTime[1]), false);
                        } else if (tachTime[0].equals("TL")) {
                            player.createTuanLoc(Integer.parseInt(tachTime[1]));
                            
                        } else if (tachTime[0].equals("DBT")) {
                            player.createPoro(Integer.parseInt(tachTime[1]), false);

                            ///  todo REGEX quà tích lũy 
                        } else if (tachTime[0].equals("LANT")) {
                            player.createLan(Integer.parseInt(tachTime[1]), false, 95);

                        } else if (tachTime[0].equals("DKT")) {
                            player.createHeo(Integer.parseInt(tachTime[1]), true);
                            
                            
                        } else if (tachTime[0].equals("PHDS")) {
                            
                            Animal anima = null;
                            anima = new Animal();
                            anima.name = "Ho-Oh";
                            anima.idImg = Animal.PHUONG_HOANG_7MAU;
                            anima.createAtt();
                            anima.level = 1;
                            anima.place = 0;
                            anima.ownerId = player.charDBID;
                            anima.id = player.getIDItem();
                            player.animal.add(anima);
                            player.sendMessage(MessageCreator.createCharInventoryMessage(player, 0));
                            player.sendMessage(MessageCreator.createCharInventoryMessage(player, 2));
                            Database.instance.saveAnimal(anima);

                        } else if (tachTime[0].equals("PHT")) {
                            player.createPhuongHoang(Integer.parseInt(tachTime[1]), Animal.PHUONG_HOANG, true);
                        } else if (tachTime[0].equals("PHDST")) {
                            player.createPhuongHoang(Integer.parseInt(tachTime[1]), Animal.PHUONG_HOANG_7MAU, true);
                        } else if (tachTime[0].equals("PHMT")) {
                            player.createPhuongHoang(Integer.parseInt(tachTime[1]), Animal.PHUONG_HOANG_MOC, true);
                         } else if (tachTime[0].equals("PHKT")) {
                            player.createPhuongHoang(Integer.parseInt(tachTime[1]), Animal.PHUONG_HOANG_KIM, true);
                         } else if (tachTime[0].equals("PHTT")) {
                            player.createPhuongHoang(Integer.parseInt(tachTime[1]), Animal.PHUONG_HOANG_THO, true);
                        } else if (tachTime[0].equals("PHBT")) {
                            player.createPhuongHoang(Integer.parseInt(tachTime[1]), Animal.PHUONG_HOANG_BANG, true);
                        } else if (tachTime[0].equals("MNT")) {
                            Item newitem = Map.doCreateMatNa3(player, Integer.parseInt(tachTime[1]), 837, 10, 5);
                        } else if (tachTime[0].equals("MNC")) {
                            Item newitem2 = Map.doCreateMatNa3(player, Integer.parseInt(tachTime[1]), 838, 11, 5);
                            
                        } else if (tachTime[0].equals("NLT")) {
                            player.createNguyetLinhTruong(player, 0, Integer.parseInt(tachTime[3]), (Item) null, false, true, 1, false);
                        } else if (tachTime[0].equals("TKMAX")) {
                            player.createThanKhi(player, Integer.parseInt(tachTime[1]), 0, Integer.parseInt(tachTime[3]), null, true, true, 1, false);
                        } else if (tachTime[0].equals("TK")) {
                            player.createThanKhi(player, Integer.parseInt(tachTime[1]), 0, Integer.parseInt(tachTime[3]), null, false, true, 1, false);                       
                        } else if (tachTime[0].equals("NLTMAX")) {
                            player.createNguyetLinhTruong(player, 0, Integer.parseInt(tachTime[3]), (Item) null, true, true, 1, true);
                        } else if (tachTime[0].equals("HKL")) {
                            doUsePotion.doCreateHoaKyLanTime(player, Integer.parseInt(tachTime[1]));
                        } else if (tachTime[0].equals("TKMAXF")) {
                            // TKMAXF:<id 890/891/892>:<so luong>:<so dong an 1-29>:<he -1..4>:<lock>
                            for (int j = 0; j < Integer.parseInt(tachTime[2]); j++) {
                                Char.createThanKhiMax(player, Integer.parseInt(tachTime[1]),
                                        Integer.parseInt(tachTime[3]), Integer.parseInt(tachTime[4]),
                                        Integer.parseInt(tachTime[5]));
                            }
                        } else if (tachTime[0].equals("TKF")) {
                            // TKF:<id 890/891/892>:<so luong>:<mau 0,3,2,1,4>:<lock>
                            for (int j = 0; j < Integer.parseInt(tachTime[2]); j++) {
                                Char.createThanKhiFull(player, Integer.parseInt(tachTime[1]),
                                        Integer.parseInt(tachTime[3]), Integer.parseInt(tachTime[4]));
                            }
                        } else if (tachTime[0].equals("TT")) {
                            // TT:<idtemplate>:<so luong>:<so ngay, 0=vinh vien>:<lock>
                            for (int j = 0; j < Integer.parseInt(tachTime[2]); j++) {
                                Char.createThoiTrang(player, Integer.parseInt(tachTime[1]),
                                        Integer.parseInt(tachTime[3]), Integer.parseInt(tachTime[4]));
                            }
                        } else if (tachTime[0].equals("CAITRANG")) {
                            // CAITRANG:<idtemplate>:<so luong>:<plus>   903 Bat Gioi | 904 Sa Tang
                            for (int j = 0; j < Integer.parseInt(tachTime[2]); j++) {
                                Char.createItemCaiTrang(Integer.parseInt(tachTime[1]), player, Integer.parseInt(tachTime[3]));
                            }
                        } else if (tachTime[0].equals("TBT")) {
                            // TBT:<level>:<so bo>:<phut>   level PHAI dung 30/35/…/85 (khac thi
                            // lang le khong tao gi). Moi bo du 5 mon Giap/Ho uyen/Non/Ban dap/Yen
                            // cuong cung cap; he ngu hanh (heItem) tu gan theo dung chuoi
                            // checkKick5AttAnimal() doc (groupKickAnimal) nen CA 5 MON deu kich
                            // duoc ngu hanh cua mon truoc, khong mon nao bi bo sot.
                            for (int j = 0; j < Integer.parseInt(tachTime[2]); j++) {
                                Char.createTrangBiThuFullHe(player, Integer.parseInt(tachTime[1]), Integer.parseInt(tachTime[3]));
                            }
            }
        }
    }

    static String getCheckQua(Database db, Char player) {
        String respond = "0| KHONG CO QUA";
        long daNap = (player.luongNap * 2);
        if (player.tichluy >= daNap) {
            return respond;
        }
        try {
            if (daNap >= 10000 && player.tichluy < 10000) {
                Connection conn = Database.connGiftCode.getConnection();
                Statement stm = conn.createStatement();
                respond = "1|0|0|" + 10000 + "|DB:686:1:-1|1|mốc nạp 10K lượng";
                player.tichluy = 10000;
                stm.execute("update `tob_char` set tichluy = '10000' where charname = '" + player.charname + "'");
                stm.close();
                Database.connGiftCode.free(conn);
                return respond;
            }
            if (daNap >= 20000 && player.tichluy < 20000) {
                Connection conn = Database.connGiftCode.getConnection();
                Statement stm = conn.createStatement();
                respond = "1|0|0|" + 20000 + "|DB:687:1:0,PP:0:1:43200|1|mốc nạp 20K lượng";
                player.tichluy = 20000;
                stm.execute("update `tob_char` set tichluy = '20000' where charname = '" + player.charname + "'");
                stm.close();
                Database.connGiftCode.free(conn);
                return respond;
            }
            if (daNap >= 40000 && player.tichluy < 40000) {
                Connection conn = Database.connGiftCode.getConnection();
                Statement stm = conn.createStatement();
                respond = "1|0|0|" + 40000 + "|PT:74:1:0,PP:0:1:129600|1|mốc nạp 40k lượng";
                player.tichluy = 40000;
                stm.execute("update `tob_char` set tichluy = '40000' where charname = '" + player.charname + "'");
                stm.close();
                Database.connGiftCode.free(conn);
                return respond;
            }
            if (daNap >= 80000 && player.tichluy < 80000) {
                Connection conn = Database.connGiftCode.getConnection();
                Statement stm = conn.createStatement();
                respond = "1|0|0|" + 80000 + "|DB:684:1:-1,PP:0:1:-1,PP:0:1:0,HLT:1:1|1|mốc nạp 80k lượng";
                player.tichluy = 80000;
                stm.execute("update `tob_char` set tichluy = '80000' where charname = '" + player.charname + "'");
                stm.close();
                Database.connGiftCode.free(conn);
                return respond;
            }

            if (daNap >= 120000 && player.tichluy < 120000) {
                Connection conn = Database.connGiftCode.getConnection();
                Statement stm = conn.createStatement();
                respond = "1|0|0|" + 120000 + "|DB:684:1:-1,AOSHOP:723:1:0,AOSHOP:724:1:0,GEM:249:300:-1,PET:5:1:0|1|mốc nạp 120K lượng";
                player.tichluy = 120000;
                stm.execute("update `tob_char` set tichluy = '120000' where charname = '" + player.charname + "'");
                stm.close();
                Database.connGiftCode.free(conn);
                return respond;

            }
            if (daNap >= 200000 && player.tichluy < 200000) {
                Connection conn = Database.connGiftCode.getConnection();
                Statement stm = conn.createStatement();
                respond = "1|0|0|" + 200000 + "|DB:688:1:-1,HLT:0:1,CHOI:617:1:43200:-1,BT:0:1:43200|1|mốc nạp 200k Lượng";
                player.tichluy = 200000;
                stm.execute("update `tob_char` set tichluy = '200000' where charname = '" + player.charname + "'");
                stm.close();
                Database.connGiftCode.free(conn);
                return respond;

            }
            
            if (daNap >= 400000 && player.tichluy < 400000) {
                Connection conn = Database.connGiftCode.getConnection();
                Statement stm = conn.createStatement();
                respond = "1|0|0|400000|DB:688:1:-1,HLT:0:1,BT:0:1:0,GEM:249:1000:-1,AOSHOP:725:1:15,AOSHOP:726:1:15|1|mốc nạp 400k Lượng";
                player.tichluy = 400000;
                stm.execute("update `tob_char` set tichluy = '400000' where charname = '" + player.charname + "'");
                stm.close();
                Database.connGiftCode.free(conn);
                return respond;

            }
            
            if (daNap >= 600000 && player.tichluy < 600000) {
                Connection conn = Database.connGiftCode.getConnection();
                Statement stm = conn.createStatement();

                respond = "1|0|0|" + 600000 + "|DB:685:1:-1,DB:688:1:-1,PPV:750:1:21600:1,GEM:249:500:-1,HLT:0:1,TRANGPHUC:725:1:30,TRANGPHUC:726:1:30|1|mốc nạp 600k Lượng";
                player.tichluy = 600000;
                stm.execute("update `tob_char` set tichluy = '600000' where charname = '" + player.charname + "'");
                stm.close();
                Database.connGiftCode.free(conn);
                return respond;
            }


            if (daNap >= 1000000 && player.tichluy < 1000000) {
                Connection conn = Database.connGiftCode.getConnection();
                Statement stm = conn.createStatement();
                respond = "1|0|0|" + 1000000 + "|DB:685:1:-1,DB:688:1:-1,PPV:742:1:0:1,PPV:742:1:0:0,MATNA:3:1:-1,DH:8770:1:0|1|mốc nạp 1 Triệu Lượng.";
                player.tichluy = 1000000;
                stm.execute("update `tob_char` set tichluy = '1000000' where charname = '" + player.charname + "'");
                stm.close();
                Database.connGiftCode.free(conn);
                return respond;
            }
            if (daNap >= 1200000 && player.tichluy < 1200000) {
                Connection conn = Database.connGiftCode.getConnection();
                Statement stm = conn.createStatement();
                respond = "1|0|0|" + 1200000 + "|PT:70:1:0,CHOI:617:1:0,MATNA:2:1:-1,TRANGPHUC:725:1:0,TRANGPHUC:726:1:0|1|mốc nạp 1 Triệu 2 Lượng.";
                player.tichluy = 1200000;
                stm.execute("update `tob_char` set tichluy = '1200000' where charname = '" + player.charname + "'");
                stm.close();
                Database.connGiftCode.free(conn);
                return respond;
            }
            if (daNap >= 1400000 && player.tichluy < 1400000) {
                Connection conn = Database.connGiftCode.getConnection();
                Statement stm = conn.createStatement();
                respond = "1|0|0|1400000|PT:115:1:0,CHOI:729:1:0:0,DB:899:1:0|1|mốc nạp 1 Triệu 4 Lượng.";
                player.tichluy = 1400000;
                stm.execute("update `tob_char` set tichluy = '1400000' where charname = '" + player.charname + "'");
                stm.close();
                Database.connGiftCode.free(conn);
                return respond;
            } // từ đây trở xuống là lỗi à ? ừm

            if (daNap >= 1600000 && player.tichluy < 1600000) {
                Connection conn = Database.connGiftCode.getConnection();
                Statement stm = conn.createStatement();
                respond = "1|0|0|1600000|PT:72:1:-1,DB:898:1:0,HOVELAN:882:1:43200,CHOI:618:1:0:0|1|mốc nạp 1 trieu 6 luong.";
                player.tichluy = 1600000;
                stm.execute("update `tob_char` set tichluy = '1600000' where charname = '" + player.charname + "'");
                stm.close();
                Database.connGiftCode.free(conn);
                return respond;
            }
            if (daNap >= 1800000 && player.tichluy < 1800000) {

                Connection conn = Database.connGiftCode.getConnection();
                Statement stm = conn.createStatement();

                respond = "1|0|0|1800000|MATNA:4:1:-1,VKMAX:0:1:0,VKTBMAX:0:1:0,HOVELAN:882:1:86400,PPV:750:1:0:0,DB:901:1:0|1|mốc nạp 1 Triệu 8 Lượng.";
                player.tichluy = 1800000;
                stm.execute("update `tob_char` set tichluy = '1800000' where charname = '" + player.charname + "'");
                stm.close();
                Database.connGiftCode.free(conn);
                return respond;
            }

            if (daNap >= 2000000 && player.tichluy < 2000000) {
                Connection conn = Database.connGiftCode.getConnection();
                Statement stm = conn.createStatement();
                respond = "1|0|0|2000000|MNT:14400:837:10:5,MNC:14400:837:10:5,DB:638:1:-1,DB:902:1:0,TRANGPHUC:839:1:0,VKTBMAX:0:1:0|1|mốc nạp 2tr Lượng.";
                player.tichluy = 2000000;
                stm.execute("update `tob_char` set tichluy = '2000000' where charname = '" + player.charname + "'");
                stm.close();
                Database.connGiftCode.free(conn);
                return respond;
            }

            if (daNap >= 2200000 && player.tichluy < 2200000) {
                Connection conn = Database.connGiftCode.getConnection();
                Statement stm = conn.createStatement();
                respond = "1|0|0|2200000|MNT:0:837:10:5,MNC:0:837:10:5,GEM:249:5000:-1,HOVELAN:882:1:108000,NLT:0:1:0|1|mốc nạp 2tr2 Lượng.";
                player.tichluy = 2200000;
                stm.execute("update `tob_char` set tichluy = '2200000' where charname = '" + player.charname + "'");
                stm.close();
                Database.connGiftCode.free(conn);
                return respond;
            }
            

            if (daNap >= 2600000 && player.tichluy < 2600000) {
                Connection conn = Database.connGiftCode.getConnection();
                Statement stm = conn.createStatement();
                respond = "1|0|0|2600000|PT:71:1:-1,PT:86:1:-1,GEM:249:5000:-1,DB:685:1:-1,DB:688:1:-1,HOVELAN:882:1:129600,|1|mốc nạp 2tr6 Lượng.";
                player.tichluy = 2600000;
                stm.execute("update `tob_char` set tichluy = '2600000' where charname = '" + player.charname + "'");
                stm.close();
                Database.connGiftCode.free(conn);
                return respond;
            }
//            if (daNap >= 2800000 && player.tichluy < 2800000) {
//                Connection conn = Database.connGiftCode.getConnection();
//                Statement stm = conn.createStatement();
//                respond = "1|0|0|2800000|SUTU:-1,DB:581:1:-1|1|mốc nạp 2tr8 Lượng.";
//                player.tichluy = 2800000;
//                stm.execute("update `tob_char` set tichluy = '2800000' where charname = '" + player.charname + "'");
//                stm.close();
//                Database.connGiftCode.free(conn);
//                return respond;
//            }
            
            if (daNap >= 3000000 && player.tichluy < 3000000) {
                Connection conn = Database.connGiftCode.getConnection();
                Statement stm = conn.createStatement();
                respond = "1|0|0|3000000|VKTBLV:80:5:0,DB:577:1:-1,DB:578:1:-1|1|mốc nạp 3tr Lượng.";
                player.tichluy = 3000000;
                stm.execute("update `tob_char` set tichluy = '3000000' where charname = '" + player.charname + "'");
                stm.close();
                Database.connGiftCode.free(conn);
                return respond;
            }

            return respond;
        } catch (SQLException e) {
            return "0|LOI MYSQL: " + e.getMessage();
        }
    }

    static void doAddNewColumInfoPart(Database db) {
        Connection conn = null;
        PreparedStatement pre = null;
        try {
            try {
                final String sql = "alter table tob_qua_ct add `tlog` datetime  NULL";
                conn = db.getConnection();
                pre = conn.prepareStatement(sql);
                pre.execute();
            } catch (Exception ignored) {
            } finally {
                try {
                    pre.close();
                } catch (Exception ignored) {
                }
                try {
                    Database.connPool.free(conn);
                } catch (Exception ignored) {
                }
            }
            try {
                pre.close();
            } catch (Exception ignored) {
            }
            try {
                Database.connPool.free(conn);
            } catch (Exception ignored) {
            }
            try {
                final String sql = "alter table tob_qua_ct add `tset` datetime  NULL";
                conn = db.getConnection();
                pre = conn.prepareStatement(sql);
                pre.execute();
            } catch (Exception ignored) {
            } finally {
                try {
                    pre.close();
                } catch (Exception ignored) {
                }
                try {
                    Database.connPool.free(conn);
                } catch (Exception ignored) {
                }
            }
            try {
                pre.close();
            } catch (Exception ignored) {
            }
            try {
                Database.connPool.free(conn);
            } catch (Exception ignored) {
            }
            try {
                final String sql = "alter table tob_qua_ct_top add `tlog` datetime  NULL";
                conn = db.getConnection();
                pre = conn.prepareStatement(sql);
                pre.execute();
            } catch (Exception ignored) {
            } finally {
                try {
                    pre.close();
                } catch (Exception ignored) {
                }
                try {
                    Database.connPool.free(conn);
                } catch (Exception ignored) {
                }
            }
            try {
                pre.close();
            } catch (Exception ignored) {
            }
            try {
                Database.connPool.free(conn);
            } catch (Exception ignored) {
            }
            try {
                final String sql = "alter table tob_qua_ct_top add `tset` datetime  NULL";
                conn = db.getConnection();
                pre = conn.prepareStatement(sql);
                pre.execute();
            } catch (Exception ignored) {
            } finally {
                try {
                    pre.close();
                } catch (Exception ignored) {
                }
                try {
                    Database.connPool.free(conn);
                } catch (Exception ignored) {
                }
            }
            try {
                pre.close();
            } catch (Exception ignored) {
            }
            try {
                Database.connPool.free(conn);
            } catch (Exception ignored) {
            }
            try {
                final String sql = "alter table tob_char add `tuido` text";
                conn = db.getConnection();
                pre = conn.prepareStatement(sql);
                pre.execute();
            } catch (Exception ignored) {
            } finally {
                try {
                    pre.close();
                } catch (Exception ignored) {
                }
                try {
                    Database.connPool.free(conn);
                } catch (Exception ignored) {
                }
            }
            try {
                pre.close();
            } catch (Exception ignored) {
            }
            try {
                Database.connPool.free(conn);
            } catch (Exception ignored) {
            }
            try {
                final String sql = "alter table tob_event add `lixi` varchar(100) DEFAULT '100,18,30'";
                conn = db.getConnection();
                pre = conn.prepareStatement(sql);
                pre.execute();
            } catch (Exception ignored) {
            } finally {
                try {
                    pre.close();
                } catch (Exception ignored) {
                }
                try {
                    Database.connPool.free(conn);
                } catch (Exception ignored) {
                }
            }
            try {
                pre.close();
            } catch (Exception ignored) {
            }
            try {
                Database.connPool.free(conn);
            } catch (Exception ignored) {
            }
            try {
                final String sql = "alter table tob_event add `vip` varchar(100) DEFAULT '500,100,5'";
                conn = db.getConnection();
                pre = conn.prepareStatement(sql);
                pre.execute();
            } catch (Exception ignored) {
            } finally {
                try {
                    pre.close();
                } catch (Exception ignored) {
                }
                try {
                    Database.connPool.free(conn);
                } catch (Exception ignored) {
                }
            }
            try {
                pre.close();
            } catch (Exception ignored) {
            }
            try {
                Database.connPool.free(conn);
            } catch (Exception ignored) {
            }
            try {
                String[] name = {"Kháng Lửa", "Kháng Nước", "Kháng Cỏ", "Kháng Thép", "Kháng Đất", "Giá lượng", "Số lượng"};
                if (TeamServer.isServerLienDau()) {
                    name = new String[]{"Kháng Lửa", "Kháng Nước", "Kháng Cỏ", "Kháng Thép", "Kháng Đất"};
                }
                int[] id = {120, 121, 122, 123, 124, 125, 126};
                int[] idcolor = {1, 1, 1, 1, 1, 2, 2, 2};
                int[] ispercent = {2, 2, 2, 2, 2, 0, 0, 0};
                final String sql2 = "insert into data_attribute(id,name,idcolor,ispercent,decript,namein) values(@,'#',^,&,'#','')";
                for (int i = 0; i < name.length; ++i) {
                    try {
                        String sql3 = sql2.replace("@", String.valueOf(id[i])).replace("#", name[i]).replace("^", String.valueOf(idcolor[i])).replace("&", String.valueOf(ispercent[i]));
                        conn = db.getConnection();
                        pre = conn.prepareStatement(sql3);
                        pre.execute();
                        try {
                            pre.close();
                        } catch (Exception ignored) {
                        }
                        try {
                            Database.connPool.free(conn);
                        } catch (Exception ignored) {
                        }
                    } catch (Exception ignored) {
                    }
                }
            } catch (Exception ignored) {
            } finally {
                try {
                    pre.close();
                } catch (Exception ignored) {
                }
                try {
                    Database.connPool.free(conn);
                } catch (Exception ignored) {
                }
            }
            try {
                pre.close();
            } catch (Exception ignored) {
            }
            try {
                Database.connPool.free(conn);
            } catch (Exception ignored) {
            }
            try {
                String[] data = {"750, 'Ruy Băng Hoenn', 19, 15, 0, 0, 1, 480, 0, 170, 0, 0, 0, 0, 0, 0, 0, 0, 0, -1, 626, 0, -1, 10080, -1, '', 8806,0", "751, 'Vảy trái tim kết đôi', 28, 0, -1, 0, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, -1, 627, 0, -1, 0, -1, '', -1,0", "752, 'Rương nguyên liệu cao cấp', 28, 0, -1, 0, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, -1, 596, 0, -1, 0, -1, '', -1,0", "753, 'Rương nguyên liệu sơ cấp', 28, 0, -1, 0, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, -1, 596, 0, -1, 0, -1, '', -1,0", "754, 'Áo dài nam \nTăng 100% băng ,sét ,độc ,lửa lan', 0, 0, 0, 1, 1, 480, 10, 10, 5000, 5000, 0, 0, 0, 0, 0, 1, 0, -1, 630, 0, -1, 10080, -1, '', -1, 0", "755, 'Áo dài nữ \nTăng 100% băng ,sét ,độc ,lửa lan', 0, 0, 0, 2, 1, 480, 10, 10, 5000, 5000, 0, 0, 0, 0, 0, 1, 0, -1, 631, 0, -1, 10080, -1, '', -1, 0"};
                for (String datum : data) {
                    try {
                        String sql4 = "insert into data_item values(" + datum + " )";
                        conn = db.getConnection();
                        pre = conn.prepareStatement(sql4);
                        pre.execute();
                    } catch (Exception ignored) {
                    } finally {
                        try {
                            pre.close();
                        } catch (Exception ignored) {
                        }
                        try {
                            Database.connPool.free(conn);
                        } catch (Exception ignored) {
                        }
                    }
                    try {
                        pre.close();
                    } catch (Exception ignored) {
                    }
                    try {
                        Database.connPool.free(conn);
                    } catch (Exception ignored) {
                    }
                }
            } catch (Exception ignored) {
            } finally {
                try {
                    pre.close();
                } catch (Exception ignored) {
                }
                try {
                    Database.connPool.free(conn);
                } catch (Exception ignored) {
                }
            }
            try {
                pre.close();
            } catch (Exception ignored) {
            }
            try {
                Database.connPool.free(conn);
            } catch (Exception ignored) {
            }
            try {
                final String sql = "CREATE TABLE  `tob_dau_gia2` (`id` int(10) unsigned NOT NULL AUTO_INCREMENT,`charname` varchar(100) NOT NULL,`luongbid` int(10) unsigned NOT NULL DEFAULT '0',`luongcoc` int(10) unsigned NOT NULL DEFAULT '0',`timebid` datetime DEFAULT NULL,`hang` int(10) unsigned NOT NULL DEFAULT '0' COMMENT '0 la chua xep hang, 1 la dc quyen mua, 2 la nhan lai tien',PRIMARY KEY (`id`),UNIQUE KEY `Index_2` (`charname`)) ENGINE=MyISAM AUTO_INCREMENT=4 DEFAULT CHARSET=utf8";
                conn = db.getConnection();
                pre = conn.prepareStatement(sql);
                pre.execute();
            } catch (Exception ignored) {
            } finally {
                try {
                    pre.close();
                } catch (Exception ignored) {
                }
                try {
                    Database.connPool.free(conn);
                } catch (Exception ignored) {
                }
            }
            try {
                pre.close();
            } catch (Exception ignored) {
            }
            try {
                Database.connPool.free(conn);
            } catch (Exception ignored) {
            }
            try {
                final String sql = "CREATE TABLE  `tob_qua_loi_dai` (`id` int(10) unsigned NOT NULL AUTO_INCREMENT,`charname` varchar(100) NOT NULL,`qua` int(10) unsigned NOT NULL DEFAULT '0',`typeQua` int(10) unsigned NOT NULL DEFAULT '0',`nhom` int(10) unsigned NOT NULL DEFAULT '0',`diem` int(10) unsigned NOT NULL DEFAULT '0',PRIMARY KEY (`id`)) ENGINE=MyISAM AUTO_INCREMENT=2 DEFAULT CHARSET=utf8";
                conn = db.getConnection();
                pre = conn.prepareStatement(sql);
                pre.execute();
            } catch (Exception ignored) {
            } finally {
                try {
                    pre.close();
                } catch (Exception ignored) {
                }
                try {
                    Database.connPool.free(conn);
                } catch (Exception ignored) {
                }
            }
            try {
                pre.close();
            } catch (Exception ignored) {
            }
            try {
                Database.connPool.free(conn);
            } catch (Exception ignored) {
            }
            try {
                final String sql = "CREATE TABLE  `tob_char_vip` (`id` int(10) unsigned NOT NULL AUTO_INCREMENT,`charname` varchar(100) NOT NULL,`vip` int(10) unsigned DEFAULT '0',`timeRcv` datetime NOT NULL,`reset` int(10) unsigned DEFAULT '0',PRIMARY KEY (`id`)) ENGINE=MyISAM DEFAULT CHARSET=utf8";
                conn = db.getConnection();
                pre = conn.prepareStatement(sql);
                pre.execute();
            } catch (Exception ignored) {
            } finally {
                try {
                    pre.close();
                } catch (Exception ignored) {
                }
                try {
                    Database.connPool.free(conn);
                } catch (Exception ignored) {
                }
            }
            try {
                pre.close();
            } catch (Exception ignored) {
            }
            try {
                Database.connPool.free(conn);
            } catch (Exception ignored) {
            }
            try {
                final String sql = "CREATE TABLE  `tob_buy_ruong` (`id` int(10) unsigned NOT NULL AUTO_INCREMENT,`charname` varchar(100) NOT NULL,`dateLog` varchar(100) DEFAULT NULL,`timeBuy` datetime DEFAULT NULL,`soluong` int(10) unsigned NOT NULL DEFAULT '0',PRIMARY KEY (`id`)) ENGINE=MyISAM AUTO_INCREMENT=2 DEFAULT CHARSET=utf8;";
                conn = db.getConnection();
                pre = conn.prepareStatement(sql);
                pre.execute();
            } catch (Exception ignored) {
            } finally {
                try {
                    pre.close();
                } catch (Exception ignored) {
                }
                try {
                    Database.connPool.free(conn);
                } catch (Exception ignored) {
                }
            }
            try {
                pre.close();
            } catch (Exception ignored) {
            }
            try {
                Database.connPool.free(conn);
            } catch (Exception ignored) {
            }
            try {
                final String sql = "CREATE TABLE  `tob_qua_ct_bk` (`id` int(10) unsigned NOT NULL AUTO_INCREMENT,`charname` varchar(45) NOT NULL,`soluong` int(10) unsigned NOT NULL DEFAULT '1',`nhan` int(10) unsigned NOT NULL DEFAULT '0',`tlog` datetime DEFAULT NULL,`tset` datetime DEFAULT NULL,PRIMARY KEY (`id`)) ENGINE=MyISAM AUTO_INCREMENT=250 DEFAULT CHARSET=utf8";
                conn = db.getConnection();
                pre = conn.prepareStatement(sql);
                pre.execute();
            } catch (Exception ignored) {
            } finally {
                try {
                    pre.close();
                } catch (Exception ignored) {
                }
                try {
                    Database.connPool.free(conn);
                } catch (Exception ignored) {
                }
            }
            try {
                pre.close();
            } catch (Exception ignored) {
            }
            try {
                Database.connPool.free(conn);
            } catch (Exception ignored) {
            }
            try {
                final String sql = "CREATE TABLE  `tob_qua_ct_top_bk` (`id` int(10) unsigned NOT NULL AUTO_INCREMENT,`charname` varchar(45) NOT NULL,`soluong` int(10) unsigned NOT NULL DEFAULT '1',`nhan` int(10) unsigned NOT NULL DEFAULT '0',`tlog` datetime DEFAULT NULL,`tset` datetime DEFAULT NULL,PRIMARY KEY (`id`)) ENGINE=MyISAM AUTO_INCREMENT=250 DEFAULT CHARSET=utf8";
                conn = db.getConnection();
                pre = conn.prepareStatement(sql);
                pre.execute();
            } catch (Exception ignored) {
            } finally {
                try {
                    pre.close();
                } catch (Exception ignored) {
                }
                try {
                    Database.connPool.free(conn);
                } catch (Exception ignored) {
                }
            }
            try {
                pre.close();
            } catch (Exception ignored) {
            }
            try {
                Database.connPool.free(conn);
            } catch (Exception ignored) {
            }
            try {
                final String sql = "CREATE TABLE `tob_nap_xai_luong` (`int` int(10) unsigned NOT NULL AUTO_INCREMENT,`charname` varchar(100) NOT NULL,`luongxai` int(10) unsigned NOT NULL DEFAULT '0',`luongnap` int(10) unsigned NOT NULL DEFAULT '0',`tnap` datetime DEFAULT NULL,`txai` datetime DEFAULT NULL,PRIMARY KEY (`int`),UNIQUE KEY `Index_2` (`charname`),KEY `Index_3` (`luongxai`),KEY `Index_4` (`luongnap`)) ENGINE=MyISAM DEFAULT CHARSET=utf8";
                conn = db.getConnection();
                pre = conn.prepareStatement(sql);
                pre.execute();
            } catch (Exception ignored) {
            } finally {
                try {
                    pre.close();
                } catch (Exception ignored) {
                }
                try {
                    Database.connPool.free(conn);
                } catch (Exception ignored) {
                }
            }
            try {
                pre.close();
            } catch (Exception ignored) {
            }
            try {
                Database.connPool.free(conn);
            } catch (Exception ignored) {
            }
            try {
                String sql = "CREATE TABLE  `tob_code_lixi` (`id` int(10) unsigned NOT NULL AUTO_INCREMENT,`code` varchar(100) NOT NULL,`nhan` int(10) unsigned DEFAULT '0',`timeNhan` datetime DEFAULT NULL,`charnhan` varchar(100) DEFAULT NULL,PRIMARY KEY (`id`),UNIQUE KEY `Index_2` (`code`) USING BTREE ,KEY `Index_3` (`charnhan`),KEY `Index_4` (`nhan`)) ENGINE=MyISAM AUTO_INCREMENT=1 DEFAULT CHARSET=utf8";
                conn = db.getConnection();
                pre = conn.prepareStatement(sql);
                pre.execute();
                try {
                    pre.close();
                } catch (Exception ignored) {
                }
                try {
                    Database.connPool.free(conn);
                } catch (Exception ignored) {
                }
                if (TeamServer.isServerLienDau() || TeamServer.isServerLocal()) {
                    int j = 0;
                    while (j < 1000) {
                        try {
                            sql = "insert into tob_code_lixi(code) values('" + Database.genCodeLixi() + "')";
                            conn = db.getConnection();
                            pre = conn.prepareStatement(sql);
                            pre.execute();
                            try {
                                pre.close();
                            } catch (Exception ignored) {
                            }
                            try {
                                Database.connPool.free(conn);
                            } catch (Exception ignored) {
                            }
                            ++j;
                        } catch (Exception ignored) {
                        }
                    }
                }
            } catch (Exception ignored) {
            } finally {
                try {
                    pre.close();
                } catch (Exception ignored) {
                }
                try {
                    Database.connPool.free(conn);
                } catch (Exception ignored) {
                }
            }
            try {
                pre.close();
            } catch (Exception ignored) {
            }
            try {
                Database.connPool.free(conn);
            } catch (Exception ignored) {
            }
            try {
                String sql = "CREATE TABLE  `tob_code_lixi` (`id` int(10) unsigned NOT NULL AUTO_INCREMENT,`code` varchar(100) NOT NULL,`nhan` int(10) unsigned DEFAULT '0',`timeNhan` datetime DEFAULT NULL,`charnhan` varchar(100) DEFAULT NULL,PRIMARY KEY (`id`),UNIQUE KEY `Index_2` (`code`) USING BTREE ,KEY `Index_3` (`charnhan`),KEY `Index_4` (`nhan`)) ENGINE=MyISAM AUTO_INCREMENT=1 DEFAULT CHARSET=utf8";
                conn = db.getConnection();
                pre = conn.prepareStatement(sql);
                pre.execute();
                try {
                    pre.close();
                } catch (Exception ignored) {
                }
                try {
                    Database.connPool.free(conn);
                } catch (Exception ignored) {
                }
                if (TeamServer.isServerLienDau() || TeamServer.isServerLocal()) {
                    int j = 0;
                    while (j < 1000) {
                        try {
                            sql = "insert into tob_code_lixi(code) values('" + Database.genCodeLixi() + "')";
                            conn = db.getConnection();
                            pre = conn.prepareStatement(sql);
                            pre.execute();
                            try {
                                pre.close();
                            } catch (Exception ignored) {
                            }
                            try {
                                Database.connPool.free(conn);
                            } catch (Exception ignored) {
                            }
                            ++j;
                        } catch (Exception ignored) {
                        }
                    }
                }
            } catch (Exception ignored) {
            } finally {
                try {
                    pre.close();
                } catch (Exception ignored) {
                }
                try {
                    Database.connPool.free(conn);
                } catch (Exception ignored) {
                }
            }
        } catch (Exception ignored) {
        } finally {
            try {
                pre.close();
            } catch (Exception ignored) {
            }
            try {
                Database.connPool.free(conn);
            } catch (Exception ignored) {
            }
        }
        try {
            pre.close();
        } catch (Exception ignored) {
        }
        try {
            Database.connPool.free(conn);
        } catch (Exception ignored) {
        }
    }

    static void loadEvent(Database db) {
        Connection conn = null;
        Statement pre1 = null;
        try {
            conn = db.getConnection();
            final String sql = "select * from tob_event where id=1";
            pre1 = conn.createStatement();
            ResultSet rs = pre1.executeQuery(sql);
            if (rs.next()) {
                Char.initTongKim(rs.getString("topluong"));
                Char.initTopNapLuong(rs.getString("topNapLuong"));
                String[] data = Char.split(rs.getString("part1"), ",");
                try {
                    if (data.length > 0) {
                        for (int i = 0; i < Map.event.value.length; ++i) {
                            Map.event.value[i] = Byte.parseByte(data[i]);
                        }
                        Map.event.timeBossAppear[0] = Long.parseLong(data[3]);
                        Map.event.timeBossAppear[1] = Long.parseLong(data[4]);
                        Map.event.timeBossAppear[2] = Long.parseLong(data[5]);
                        Map.event.timeBossAppear[3] = Long.parseLong(data[6]);
                        Map.event.timeBossAppear[4] = Long.parseLong(data[7]);
                        Map.event.timeBossAppear[5] = Long.parseLong(data[8]);
                        Map.event.timeBossAppear[6] = Long.parseLong(data[9]);
                    }
                } catch (Exception ignored) {
                }
                try {
                    Map.setInfoShopRuongMayMan(rs.getString("ruongmm"));
                } catch (Exception e) {
                    e.printStackTrace();
                }
                try {
                    data = Char.split(rs.getString("part2"), ",");
                    if (data.length > 0) {
                        for (int i = 0; i < Map.allUser.length; ++i) {
                            Map.allUser[i] = Byte.parseByte(data[i]);
                            Map.allLV[i + 3] = Byte.parseByte(data[i + 3]);
                        }
                        Map.allXuLucky = Long.parseLong(data[6]);
                        Map.allLuongLucky = Long.parseLong(data[7]);
                    }
                } catch (Exception ignored) {
                }
                try {
                    data = Char.split(rs.getString("lixi"), ",");
                    if (data.length > 0) {
                        Map.MAX_LI_XI = Integer.parseInt(data[0]);
                        Map.hourGiveLixi = Integer.parseInt(data[1]);
                        Map.minuteGiveLixi = Integer.parseInt(data[2]);
                        if (UtilHTBB.getHour() == 5 || UtilHTBB.getHour() == 6) {
                            Map.MAX_LI_XI = 100;
                            Map.hourGiveLixi = 18;
                            Map.minuteGiveLixi = 30;
                        }
                    }
                } catch (Exception ignored) {
                }
                try {
                    data = Char.split(rs.getString("vip"), ",");
                    if (data.length > 0) {
                        Map.MAX_VIP1 = Short.parseShort(data[0]);
                        Map.MAX_VIP2 = Short.parseShort(data[1]);
                        Map.MAX_VIP3 = Short.parseShort(data[2]);
                    }
                } catch (Exception ignored) {
                }
                try {
                    data = Char.split(rs.getString("part3"), ",");
                    if (data.length > 0) {
                        for (int i = 0; i < 3; ++i) {
                            RegisterAttack reg = Map.nationWar.get(i);
                            reg.dayAttack = data[i * 3];
                            reg.idMyAttack = Byte.parseByte(data[i * 3 + 1]);
                            reg.idNationAttackMe = Byte.parseByte(data[i * 3 + 2]);
                            if (reg.dayAttack.compareTo(Char.getDayOpen(0L)) < 0 || reg.dayAttack.equals("")) {
                                reg.reset();
                            }
                            if (reg.idMyAttack > -1 && Map.idClanTown[reg.idMyAttack] == -1) {
                                reg.idMyAttack = -1;
                            }
                        }
                        Map.nationBeAttack[0] = Byte.parseByte(data[9]);
                        Map.nationBeAttack[1] = Byte.parseByte(data[10]);
                        Map.nationBeAttack[2] = Byte.parseByte(data[11]);
                        if (Map.idClanTown[0] == -1) {
                            Map.nationBeAttack[0] = -1;
                        }
                        if (Map.idClanTown[1] == -1) {
                            Map.nationBeAttack[1] = -1;
                        }
                        if (Map.idClanTown[2] == -1) {
                            Map.nationBeAttack[2] = -1;
                        }
                        Map.minuteX2Country[0] = Long.parseLong(data[12]);
                        Map.minuteX2Country[1] = Long.parseLong(data[13]);
                        Map.minuteX2Country[2] = Long.parseLong(data[14]);
                        Map.x2Country[0] = Byte.parseByte(data[15]);
                        Map.x2Country[1] = Byte.parseByte(data[16]);
                        Map.x2Country[2] = Byte.parseByte(data[17]);
                        RegisterAttack reg2 = Map.nationWar.get(0);
                        reg2.win = Byte.parseByte(data[18]);
                        reg2.lose = Byte.parseByte(data[19]);
                        reg2.timeX2 = Byte.parseByte(data[20]);
                        RegisterAttack reg3 = Map.nationWar.get(1);
                        reg3.win = Byte.parseByte(data[21]);
                        reg3.lose = Byte.parseByte(data[22]);
                        reg3.timeX2 = Byte.parseByte(data[23]);
                        RegisterAttack reg4 = Map.nationWar.get(2);
                        reg4.win = Byte.parseByte(data[24]);
                        reg4.lose = Byte.parseByte(data[25]);
                        reg4.timeX2 = Byte.parseByte(data[26]);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
                try {
                    data = Char.split(rs.getString("part4"), ",");
                    if (data.length > 0) {
                        Map.event.maxT = Byte.parseByte(data[0]);
                        Map.event.nTDay = Byte.parseByte(data[1]);
                        Map.event.countT = Byte.parseByte(data[2]);
                        Map.winNumber = Byte.parseByte(data[3]);
                    }
                } catch (Exception ignored) {
                }
                try {
                    data = Char.split(rs.getString("shield"), ",");
                    if (data.length > 0) {
                        for (int i = 0; i < 4; ++i) {
                            Map.SHIELD[0][i] = Byte.parseByte(data[i]);
                            Map.SHIELD[1][i] = Byte.parseByte(data[i + 4]);
                            Map.SHIELD[2][i] = Byte.parseByte(data[i + 8]);
                        }
                        NewClan.timeDelayOpenShield[0] = Byte.parseByte(data[12]);
                        NewClan.timeDelayOpenShield[1] = Byte.parseByte(data[13]);
                        NewClan.timeDelayOpenShield[2] = Byte.parseByte(data[14]);
                        Map.newDay = data[15];
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
                try {
                    if (Map.idClanTown[0] > -1) {
                        data = Char.split(rs.getString("gov1"), ",");
                        if (data.length > 0) {
                            Char.gov[0].addMember(0, data[0]);
                            Char.gov[0].addMember(1, data[1]);
                            Char.gov[0].addMember(1, data[2]);
                            Char.gov[0].addMember(2, data[3]);
                            Char.gov[0].addMember(2, data[4]);
                            Char.gov[0].addMember(3, data[5]);
                            Char.gov[0].addMember(3, data[6]);
                        }
                    }
                } catch (Exception ignored) {
                }
                try {
                    if (Map.idClanTown[1] > -1) {
                        data = Char.split(rs.getString("gov2"), ",");
                        if (data.length > 0) {
                            Char.gov[1].addMember(0, data[0]);
                            Char.gov[1].addMember(1, data[1]);
                            Char.gov[1].addMember(1, data[2]);
                            Char.gov[1].addMember(2, data[3]);
                            Char.gov[1].addMember(2, data[4]);
                            Char.gov[1].addMember(3, data[5]);
                            Char.gov[1].addMember(3, data[6]);
                        }
                    }
                } catch (Exception ignored) {
                }
                try {
                    if (Map.idClanTown[2] > -1) {
                        data = Char.split(rs.getString("gov3"), ",");
                        if (data.length > 0) {
                            Char.gov[2].addMember(0, data[0]);
                            Char.gov[2].addMember(1, data[1]);
                            Char.gov[2].addMember(1, data[2]);
                            Char.gov[2].addMember(2, data[3]);
                            Char.gov[2].addMember(2, data[4]);
                            Char.gov[2].addMember(3, data[5]);
                            Char.gov[2].addMember(3, data[6]);
                        }
                    }
                } catch (Exception ignored) {
                }
                try {
                    data = Char.split(rs.getString("quayso"), ",");
                    if (data.length > 0) {
                        Map.dayQuaySo = data[0];
                        Map.nPhuongHoang = 0;
                        Map.max_phuong_hoang = 0;
                        Map.countQuaySo = 0;
                        Map.MAX_QUAY = 5000;
                    } else {
                        Map.dayQuaySo = Char.getDayOpen(0L);
                    }
                } catch (Exception ignored) {
                }
                try {
                    data = Char.split(rs.getString("phaohoa"), ",");
                    if (data.length > 0) {
                        Map.totalDotPhao = Integer.parseInt(data[0]);
                        Map.x3ExpDotPhao = Byte.parseByte(data[1]);
                    } else {
                        Map.dayQuaySo = Char.getDayOpen(0L);
                    }
                } catch (Exception ignored) {
                }
                try {
                    data = Char.split(rs.getString("openphuonghoang"), ",");
                    Map.minOpenHopqua = Integer.parseInt(data[0]);
                    Map.totalPhuongHoang = 0;
                    Map.winPhuongHoang = 0;
                    Map.countOpenHopqua = 0;
                } catch (Exception ignored) {
                }
                try {
                    data = Char.split(rs.getString("trung"), "|");
                    Char.total_open = Integer.parseInt(data[0]);
                    Char.numberLucky = Integer.parseInt(data[1]);
                    Char.numberRandom = Integer.parseInt(data[2]);
                } catch (Exception ignored) {
                }
                ItemSell.ID_ITEM_SELL_AUTO_INCREMENT = rs.getInt("iditemsell");
                System.out.println("IDITEM SELL HIEN TAI: " + ItemSell.ID_ITEM_SELL_AUTO_INCREMENT);
            }
            try {
                rs.close();
            } catch (Exception ignored) {
            }
        } catch (Exception e2) {
            e2.printStackTrace();
        } finally {
            try {
                Database.connPool.free(conn);
            } catch (Exception ignored) {
            }
            try {
                pre1.close();
            } catch (Exception ignored) {
            }
        }
        try {
            Database.connPool.free(conn);
        } catch (Exception ignored) {
        }
        try {
            pre1.close();
        } catch (Exception ignored) {
        }
    }

}
