package real;


import com.htbb.ManagerGame;
import util.Logger;
import server.TeamServer;
import data.Database;
import real.cmd.LoginHandler;
import io.Message;
import java.io.IOException;

import data.ItemLuckyDraw;
import java.util.Hashtable;
import data.Animal;
import real.plugins.*;

/**
 * Than cac method qua tang / su kien cua Char: qua tich luy, vong quay may
 * man, qua offline, chuc mung 8/3, nap danh sach vat pham lucky, doi tieu.
 *
 * Tach tu real/Char.java. Cac method VAN CON trong Char, giu NGUYEN chu ky
 * cu, chi than chuyen sang day. Tham so "Char self" thay cho "this".
 */
class CharSuKien {

    static void doResponseTieu(Char self, final Char pReceive, final boolean isThief, final int type, int lvOpen) {
        try {
            synchronized (Map.FINISH_TIEU) {
                if (lvOpen > 78) {
                    lvOpen = 78;
                }
                final String[] name = {"Trắng", "Xanh", "Đỏ", "Hoàn mỹ", "Vàng", "Tím"};
                final int div = isThief ? 2 : 1;
                final int typeTieu = type / 6;
                final int colortieu = type % 6;
                int indexExp = (lvOpen - 20) / 10;
                if (indexExp < 0) {
                    indexExp = 0;
                }
                final int[] idexp = {0, 0, 1, 2, 2, 2};
                // Them Hoan My (idx3) vao nhanh cap luong/luong khoa - truoc day chi
                // Vang(4)/Tim(5) (yeu cau nguoi dung 13/09/2026).
                final int[] luongadd = {0, 0, 0, 100 + Map.r.nextInt(11), 100 + Map.r.nextInt(11), 100 + Map.r.nextInt(11), 0};
                final int random = Map.r.nextInt(100);
                if (random >= 80) {
                    if (random < 89) {
                        luongadd[4] = 110 + Map.r.nextInt(11);
                        luongadd[5] = 110 + Map.r.nextInt(11);
                    } else if (random < 93) {
                        luongadd[4] = 120 + Map.r.nextInt(31);
                        luongadd[5] = 120 + Map.r.nextInt(31);
                    } else if (random < 97) {
                        luongadd[4] = 150 + Map.r.nextInt(21);
                        luongadd[5] = 150 + Map.r.nextInt(21);
                    } else {
                        luongadd[4] = 170 + Map.r.nextInt(31);
                        luongadd[5] = 170 + Map.r.nextInt(31);
                    }
                }
                // Ti le mau tieu + he so EXP tra tieu quan ly qua SQL (server.CauHinh) -
                // xem Char.TIEU_HE_SO_EXP / TIEU_PC_EXP_THEO_MAU / TIEU_PERMILLE_CAO_CAP.
                final int[] pcExpAdd = Char.TIEU_PC_EXP_THEO_MAU;
                if (lvOpen > pReceive.lvDetail.lv) {
                    lvOpen = pReceive.lvDetail.lv;
                }
                // Tu lv50, RIENG 3 mau Hoan My(3)/Vang(4)/Tim(5): exp la % CUA exp can de
                // len cap hien tai (LevelDetail.expRequire[lv]) thay vi bang tra
                // EXP_NV_HANG_NGAY cu - giam dan theo moc cap (tru THEO MOC, khong tru
                // luy tien tu goc). Don vi phan nghin (permille) de giu duoc so le 0.5%.
                // Trang/Xanh/Do và duoi lv50 GIU NGUYEN cong thuc cu, khong doi.
                // TIEU_PERMILLE_CAO_CAP gop phang 3 khung cap (50-59/60-69/70+) x 3 mau
                // (Hoan My/Vang/Tim) thanh 9 phan tu - chi so = khung*3 + (mau-3).
                final int[] permilleCaoCap = Char.TIEU_PERMILLE_CAO_CAP;
                if (typeTieu == Char.INDIVITUAL) {
                    long exp;
                    if (lvOpen < 50 || colortieu < 3) {
                        exp = Char.EXP_NV_HANG_NGAY[indexExp][idexp[colortieu]] * Char.TIEU_HE_SO_EXP / 100L; // TangExpVanTieu
                        exp = exp * pcExpAdd[colortieu] / 100L;
                    } else {
                        final int bracket = (lvOpen >= 70) ? 2 : ((lvOpen >= 60) ? 1 : 0);
                        final int permille = permilleCaoCap[bracket * 3 + (colortieu - 3)];
                        final int lvExp = Math.min(lvOpen, LevelDetail.expRequire.length - 1);
                        exp = LevelDetail.expRequire[lvExp] * permille / 1000L;
                    }
                    exp /= div;
                    if (TeamServer.isServerHoaLu() && Char.isSuKienDuaTopHoaLu()) {
                        exp = 0L;
                    }
                    if (TeamServer.ExpVantieu) {
                    Map.addXpCharEvent(pReceive, exp, false, "char doResponseTieu");
                    }
                    final Item it = self.doAddItemVanTieu(pReceive, colortieu, lvOpen);
                    if (it != null) {
                        it.magic_physic = (byte) Map.r.nextInt(2);
                        if (it.magic_physic == 0) {
                            it.atb[6] = it.atb[1];
                            final short[] atb = it.atb;
                            final int n = 1;
                            atb[n] /= 10;
                        } else if (it.magic_physic == 1) {
                            it.atb[6] = (short) (it.atb[1] / 10);
                        }
                    }
                    
                    String info = "nhận được " + exp + "exp";
                    if (it != null) {
                        self.sendMessage(MessageCreator.createCharInventoryMessage(pReceive, 1));
                        info = String.valueOf(info) + "," + it.getName();
                    }
                    // Tu lv65 (ha tu lv70 - yeu cau nguoi dung 16/09/2026): bo luong/luong
                    // khoa, thay bang Bot xanh la (GemTemplate.BOT_XANH_LA, id 249 - doi tu
                    // BOT_XANH/"Bot xanh" id 247 theo yeu cau nguoi dung 17/09/2026, ten cu
                    // "bot xanh lam" chi la cach goi tat, khong phai ten vat pham thuc su).
                    // Tbt/exp/diem doi thuong KHONG doi - van cho DEU o ca hai phia nguong
                    // nay (yeu cau nguoi dung 13/09/2026).
                    if (luongadd[colortieu] > 0 && lvOpen < 65 && com.htbb.pokemon.LauDai.bat()) {
                        // Bo sung kiem toan A2: bat co sv.lau_dai -> chi luong KHOA, trong ngan
                        // sach (toi da 10 / lan, nhu nhiem vu ngay - THIET_KE 5.1), khong ra luong mo.
                        luongadd[colortieu] = Math.min(10 * div, luongadd[colortieu]);
                    }
                    if (luongadd[colortieu] > 0 && lvOpen < 65) {
                        final boolean notlock = !com.htbb.pokemon.LauDai.bat() && Map.r.nextInt(1000) < 5;
                        if (!notlock) {
                            self.addLuongLock(luongadd[colortieu] / div);
                        } else {
                            self.addLuong(luongadd[colortieu] / div);
                        }
                        info = String.valueOf(info) + " và " + luongadd[colortieu] / div + " lượng" + (notlock ? "" : " khoá");
                        self.sendMessage(MessageCreator.createCharInventoryMessage(pReceive, 0));
                        if (notlock) {
                            try {
                                com.htbb.chongspam.ThongBaoGop.dua("nhan_thuong", "Chúc mừng " + self.charname + " đã nhận được " + luongadd[colortieu] / div + " lượng");
                            } catch (final Exception e) {
                                e.printStackTrace();
                            }
                        }
                    } else if (lvOpen >= 65) {
                        // Hoan My/Vang/Tim/Do: muc CO DINH rieng tung mau (yeu cau nguoi
                        // dung 13/09/2026 - truoc do ca 3 mau deu random 1-3 giong
                        // Trang/Xanh/Do; them Do vao nhanh co dinh + Tim giam 35 -> 30 theo
                        // yeu cau nguoi dung 17/09/2026).
                        // Trang/Xanh van giu nguyen random 1-3 cu, khong doi.
                        // CUOP TIEU: gap 1,5 lan (khong con giam mot nua qua "div" nhu truoc -
                        // yeu cau nguoi dung 16/09/2026, giong cach doi voi diem doi thuong).
                        final int slBotXanh;
                        if (colortieu >= 2) {
                            final int[] BOT_XANH_THEO_MAU = {5, 10, 20, 30}; // [Do, Hoan My, Vang, Tim]
                            final int slGoc = BOT_XANH_THEO_MAU[colortieu - 2];
                            slBotXanh = Math.max(1, isThief ? slGoc * 3 / 2 : slGoc);
                        } else {
                            final int slGoc = Map.r.nextInt(3) + 1;
                            slBotXanh = Math.max(1, isThief ? slGoc * 3 / 2 : slGoc);
                        }
                        self.doAddGemItem(GemTemplate.BOT_XANH_LA, slBotXanh, true);
                        info = String.valueOf(info) + " và " + slBotXanh + " " + Map.gemTemplate[GemTemplate.BOT_XANH_LA].name;
                        self.sendMessage(MessageCreator.createCharGemItem(self));
                    }
                    final int[] pcAdd = {0, isThief ? 5 : 10, isThief ? 25 : 50, isThief ? 35 : 70, isThief ? 35 : 70, isThief ? 50 : 100};
                    final int pc = Map.r.nextInt(100);
                    final int[] sl = {15, 15, 15, 6, 3, 1};
                    final int[] slmin = {3, 3, 3, 2, 1, 1};
                    if (pc < pcAdd[colortieu]) {
                        final int index = Map.r.nextInt(6);
                        int numberran = sl[index] - slmin[index];
                        if (numberran <= 0) {
                            numberran = 1;
                        }
                        final int soluong = Map.r.nextInt(numberran) + slmin[index];
                        self.doAddGemItem(GemTemplate.ID_NGOC_HUYEN_MINH[index], soluong, true);
                        info = String.valueOf(info) + " và " + soluong + " " + Map.gemTemplate[GemTemplate.ID_NGOC_HUYEN_MINH[index]].name;
                        pReceive.sendMessage(MessageCreator.createCharGemItem(self));
                    }
                    self.sendMessage(MessageCreator.createServerAlertMessage("Chúc mừng bạn " + info, ""));
                    Database.instance.saveOrtherLog("", pReceive.charname, String.valueOf(info) + ((it != null) ? ("_" + it.getInfoSave()) : "") + " " + isThief + " mau tieu: " + name[colortieu], isThief ? "motieu" : "tratieu");
                    if (!isThief) {
                        final long[] finish_TIEU = Map.FINISH_TIEU;
                        final int n2 = colortieu;
                        ++finish_TIEU[n2];
                    }
                    // Diem doi thuong: tu Xanh tro len (colortieu>=1, Trang khong duoc) -
                    // mo rong them Do/Xanh, moi mau MOT muc rieng - Tim 40, Vang 25, Hoan My
                    // 15, Do 10, Xanh 5 (yeu cau nguoi dung 17/09/2026 - truoc day chi
                    // Hoan My/Vang/Tim duoc, Tim 50/Vang 30). Diem nay cap DEU o ca hai phia
                    // moc lv65 (song song voi bot xanh lam tu lv65). CUOP TIEU (isThief)
                    // duoc GAP 1,5 LAN diem cua van tieu thanh cong - RIENG voi bien "div"
                    // dung chung o tren cho exp/luong (nhung do van con giam mot nua khi bi
                    // cuop, khong doi).
                    if (colortieu >= 1) {
                        final int[] DIEM_DOI_THUONG = {5, 10, 15, 25, 40}; // [Xanh, Do, Hoan My, Vang, Tim]
                        final int diemGoc = DIEM_DOI_THUONG[colortieu - 1];
                        final int diem = isThief ? diemGoc * 3 / 2 : diemGoc;
                        pReceive.diemDoiThuong += diem;
                        pReceive.sendMessage(MessageCreator.createServerAlertMessage(
                                "Bạn nhận được " + diem + " điểm đổi thưởng (tổng: " + pReceive.diemDoiThuong + " điểm).", ""));
                    }
                } else if (typeTieu != Char.CLAN) {
                }
                Char.isSuKienHe2017();
                if (Map.r.nextInt(1000) < 5) {
                    Map.doCreateBookSkillPet(self, 0);
                }
                //monitorexit(Map.FINISH_TIEU);
            }
        } catch (final Exception e2) {
            e2.printStackTrace();
        }
    }

    static void doAddQuaTichLuy(Char self, int QuaTuan) {

        if (Char.TYPE_QUA_TICH_LUY == false) {
            return;
        }

        String result = "0|NO DATA";
        if (QuaTuan == 1) {
            result = Database.instance.getCheckQua_Tuan(self);
        } else if (QuaTuan == 2) {
            result = Database.instance.getCheckQua(self);
        } else if (QuaTuan == 3) {
            result = Database.instance.getCheckQua_Bonus(self);
        }
        // System.out.println(result);
        String[] tach_string = result.trim().split("\\|");
        int id = Integer.parseInt(tach_string[0]);
        if (id == 1) {
            if (self.getLevel() < Integer.parseInt(tach_string[5])) {
                self.sendMessage(MessageCreator.createServerAlertMessage("Bạn phải đạt cấp " + tach_string[5] + " thì mới nhận được giftcode", ""));
                return;
            }
            String code = tach_string[6];
            //System.out.println(self.charname + " nhap CODE: " + code + " KQ= " + result + " ID= " + id);
            int xu = Integer.parseInt(tach_string[1]);
            int luong = Integer.parseInt(tach_string[2]);
            int luongkhoa = Integer.parseInt(tach_string[3]);
            String listItem = tach_string[4];
            //System.out.println("XU: " + xu + " - luong: " + luong + " - luongkhoa: " + luongkhoa + " - listItem: " + listItem + " - ");
            if (xu > 0) {
                self.addXu(xu, "nhan tu giftcode" + code);
            }
            if (luong > 0) {
                self.addLuong(luong);
            }
            if (luongkhoa > 0) {
                self.addLuongLock(luongkhoa);
            }
            if (!listItem.equals("NULL")) {
                String[] tachItem = listItem.trim().split(",");
                for (int i = 0; i < tachItem.length; i++) {
                    String[] tachTime = tachItem[i].trim().split(":");
                    if (tachTime[0].equals("DB")) {
                        for (int j = 0; j < Integer.parseInt(tachTime[2]); j++) {
                            GiftCode.AddItem(self, Integer.parseInt(tachTime[1]), 0, 0, Integer.parseInt(tachTime[3]));
                        }
                    } else if (tachTime[0].equals("EGG")) {
                        for (int j = 0; j < Integer.parseInt(tachTime[2]); j++) {
                            self.createTrungPhuongHoang(1L, Integer.parseInt(tachTime[1]));
                        }
                    } else if (tachTime[0].equals("PET")) {
                        for (int j = 0; j < Integer.parseInt(tachTime[2]); j++) {
                            self.createPet(Integer.parseInt(tachTime[1]), Integer.parseInt(tachTime[3]));
                        }
                    } else if (tachTime[0].equals("TK")) {
                        self.createThanKhi(self, Integer.parseInt(tachTime[1]), 0, Integer.parseInt(tachTime[3]), null, false, true, 1, false);    
                    } else if (tachTime[0].equals("PETV")) {
                           
                        self.createPetVinhVien(Integer.parseInt(tachTime[1]));    
                    } else if (tachTime[0].equals("HLT")) {
                        for (int j = 0; j < Integer.parseInt(tachTime[2]); j++) {
                            Map.doCreateHuyetLinhThao(self, Integer.parseInt(tachTime[1]));
                        }
                    } else if (tachTime[0].equals("HOVELAN")) {
                        self.createHoVeLenhLan(Integer.parseInt(tachTime[1]), self, Integer.parseInt(tachTime[3]), 1);
                    } else if (tachTime[0].equals("TRANGPHUC")) {
                        // Format: TRANGPHUC:itemID:quantity:days
                        for (int j = 0; j < Integer.parseInt(tachTime[2]); j++) {
                            Char.doDropModelClothe(self, Integer.parseInt(tachTime[1]), Integer.parseInt(tachTime[3]));
                        } 
                        
                    } else if (tachTime[0].equals("DANHHIEU2")) {
                        for (int j = 0; j < Integer.parseInt(tachTime[2]); j++) {
                            self.addDanhHieu(Short.parseShort(tachTime[1]), Long.parseLong(tachTime[3]));
                        }
                        try {
                            self.sendMessage(MessageCreator.createMainCharInfoMessage(self));
                            self.sendToNearPlayer(MessageCreator.createCharInfo(self));
                        } catch (IOException ex) {
                        } 
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
                        self.addDanhHieu(titleId, Long.parseLong(tachTime[3]));
                    }
                    try {
                        self.sendMessage(MessageCreator.createMainCharInfoMessage(self));
                        self.sendToNearPlayer(MessageCreator.createCharInfo(self));
                    } catch (IOException ex) {
    }


                    } else if (tachTime[0].equals("PPR")) {
                        for (int j = 0; j < Integer.parseInt(tachTime[2]); j++) {
                            self.createtemCoat(Integer.parseInt(tachTime[1]), self, Integer.parseInt(tachTime[4]), false, Integer.parseInt(tachTime[3]));
                        }
                    } else if (tachTime[0].equals("PPRK")) {
                        for (int j = 0; j < Integer.parseInt(tachTime[2]); j++) {
                            self.createtemCoat_chonkhang(Integer.parseInt(tachTime[1]), self, Integer.parseInt(tachTime[4]), false, Integer.parseInt(tachTime[3]), Integer.parseInt(tachTime[5]));
                        }
                    } else if (tachTime[0].equals("PPV")) {
                        for (int j = 0; j < Integer.parseInt(tachTime[2]); j++) {
                            self.createtemCoat(Integer.parseInt(tachTime[1]), self, Integer.parseInt(tachTime[4]), true, Integer.parseInt(tachTime[3]));
                        }
                    } else if (tachTime[0].equals("PPVK")) {
                        for (int j = 0; j < Integer.parseInt(tachTime[2]); j++) {
                            self.createtemCoat_chonkhang(Integer.parseInt(tachTime[1]), self, Integer.parseInt(tachTime[4]), true, Integer.parseInt(tachTime[3]), Integer.parseInt(tachTime[5]));
                        }

                    } else if (tachTime[0].equals("DH")) {
                        for (int j = 0; j < Integer.parseInt(tachTime[2]); j++) {
                            self.addDanhHieu(Short.parseShort(tachTime[1]), Long.parseLong(tachTime[3]));
                        }
                        try {
                            self.sendMessage(MessageCreator.createMainCharInfoMessage(self));
                            self.sendToNearPlayer(MessageCreator.createCharInfo(self));
                        } catch (IOException ex) {
                        }
                    } else if (tachTime[0].equals("DANHHIEU2")) {
                        for (int j = 0; j < Integer.parseInt(tachTime[2]); j++) {
                            self.addDanhHieu(Short.parseShort(tachTime[1]), Long.parseLong(tachTime[3]));
                        }
                        try {
                            self.sendMessage(MessageCreator.createMainCharInfoMessage(self));
                            self.sendToNearPlayer(MessageCreator.createCharInfo(self));
                        } catch (IOException ex) {
                        }    
                    } else if (tachTime[0].equals("PT")) {
                        int idGem = Integer.parseInt(tachTime[1]);
                        int soluong = Integer.parseInt((tachTime[2]));
                        self.potions[idGem] = self.potions[idGem] + soluong;
                        self.calculateAttrib();
                        try {
                            self.sendMessage(MessageCreator.createMainCharInfoMessage(self));
                            self.sendMessage(MessageCreator.createCharInventoryMessage(self, 0));
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
                        self.doAddGemItem(Integer.parseInt(tachTime[1]), Integer.parseInt(tachTime[2]), lockitemgem);
                        self.sendMessage(MessageCreator.createCharGemItem(self));
                    } else if (tachTime[0].equals("PP")) {
                        Char.createPhiPhongMaxBaoKich(self, 0, Integer.parseInt(tachTime[3]), (Item) null, false, false, 0, true);
                        Char.createPhiPhongMaxBaoKich(self, 1, Integer.parseInt(tachTime[3]), (Item) null, false, false, 0, true);
                    } else if (tachTime[0].equals("PPLUCK")) {
                        self.createtemCoat(Integer.parseInt(tachTime[1]), self, Integer.parseInt(tachTime[4]), true, 0);
                    } else if (tachTime[0].equals("PP8X")) {
                        Char.createPhiPhongMaxBaoKich(self, 0, Integer.parseInt(tachTime[3]), (Item) null, true, true, 5, true);
                        Char.createPhiPhongMaxBaoKich(self, 1, Integer.parseInt(tachTime[3]), (Item) null, true, true, 5, true);
                    } else if (tachTime[0].equals("VKTT")) {
                        Char.createVuKhiThoiTrang(self, 0, Integer.parseInt(tachTime[3]), (Item) null, false, true, 1, false);
                    } else if (tachTime[0].equals("VKMAX")) {
                        Char.createVuKhiThoiTrang(self, 0, Integer.parseInt(tachTime[3]), (Item) null, true, true, 1, false);
                    } else if (tachTime[0].equals("VKTB")) {
                        Char.createVuKhiThoiTrangTHANBINH_NEW(self, 0, Integer.parseInt(tachTime[3]), (Item) null, false, true, 1, false);
                    } else if (tachTime[0].equals("VKTBMAX")) {
                        Char.createVuKhiThoiTrangTHANBINH_NEW(self, 0, Integer.parseInt(tachTime[3]), (Item) null, true, true, 1, false);
                    } else if (tachTime[0].equals("BT")) {
                        self.doCreatePetBachThu(Integer.parseInt(tachTime[3]), false);
                    } else if (tachTime[0].equals("AOTT")) {
                        if (Integer.parseInt(tachTime[1]) <= 755) {
                            Map.doBuyModelClothe(self, Integer.parseInt(tachTime[1]), Integer.parseInt(tachTime[3]), 1);
                        } else {
                            ManagerGame.doBuyModelClothe(self, Integer.parseInt(tachTime[1]), Integer.parseInt(tachTime[3]), 1);
                        }
                    } else if (tachTime[0].equals("AOSHOP")) {
                        Char.doDropModelClothe(self, Integer.parseInt(tachTime[1]), Integer.parseInt(tachTime[3]));
                    } else if (tachTime[0].equals("CHOI")) {
                            self.createChoi(Integer.parseInt(tachTime[1]), self, Integer.parseInt(tachTime[3]), Integer.parseInt(tachTime[4]), true);
                    } else if (tachTime[0].equals("MATNA")) {
                        int[] valuemn = {5, 5, 10, 30, 50};
                        int randomMN = Map.r.nextInt(valuemn.length);
                        if (Integer.parseInt(tachTime[1]) > -1) {
                            randomMN = Integer.parseInt(tachTime[1]);
                        }
                        Item newitem = Map.doCreateMatNa3(self, Integer.parseInt(tachTime[3]), Char.ID_MAT_NA[randomMN], randomMN, valuemn[randomMN]);
                        self.sendMessage(MessageCreator.createServerAlertMessage("Nhận " + newitem.getName(), ""));
                    } else if (tachTime[0].equals("MATNA2")) {
                        // Tạo mặt nạ với option và value random
                        int[] optionValues = {5, 5, 10, 30, 50}; // Các giá trị value có thể
                        int randomIndex = Map.r.nextInt(optionValues.length);
                        int matnaID = Integer.parseInt(tachTime[1]); // ID mặt nạ
                        int option = randomIndex; // Option mặt nạ (0-4)
                        int value = optionValues[randomIndex];  // Giá trị option tương ứng
                        Item newitem = Map.doCreateMatNa2(self, Integer.parseInt(tachTime[3]), matnaID, option, value);
                        self.sendMessage(MessageCreator.createServerAlertMessage("Nhận " + newitem.getName() + (Integer.parseInt(tachTime[3]) > 0 ? " " + Integer.parseInt(tachTime[3])/1440 + " ngày" : " vĩnh viễn"), ""));    
// ... existing code ...    
                    } else if (tachTime[0].equals("DK")) {
                        self.createHeo(0, false);
                    } else if (tachTime[0].equals("TL")) {
                        self.createTuanLoc(Integer.parseInt(tachTime[1]));
                    } else if (tachTime[0].equals("DBT")) {
                        self.createPoro(Integer.parseInt(tachTime[1]), false);

                        ///  todo REGEX quà tích lũy 
                    } else if (tachTime[0].equals("SUTU")) {
                        self.createSuTu(Integer.parseInt(tachTime[1]), true);
                      
                    }  else if (tachTime[0].equals("SUTUBANG")) {
                        self.createSuTuBang(Integer.parseInt(tachTime[1]), true);  
                    
                    }  else if (tachTime[0].equals("LANT")) {
                        self.createLan(Integer.parseInt(tachTime[1]), false, 95);

                    } else if (tachTime[0].equals("DKT")) {
                        self.createHeo(Integer.parseInt(tachTime[1]), true);

                    } else if (tachTime[0].equals("PHT")) {
                        self.createPhuongHoang(Integer.parseInt(tachTime[1]), Animal.PHUONG_HOANG, true);
                    } else if (tachTime[0].equals("PHBT")) {
                        self.createPhuongHoang(Integer.parseInt(tachTime[1]), Animal.PHUONG_HOANG_BANG, true);
                    } else if (tachTime[0].equals("PHDST")) {
                        self.createPhuongHoang(Integer.parseInt(tachTime[1]), Animal.PHUONG_HOANG_7MAU, true);
                    } else if (tachTime[0].equals("PHMT")) {
                        self.createPhuongHoang(Integer.parseInt(tachTime[1]), Animal.PHUONG_HOANG_MOC, true);
                    } else if (tachTime[0].equals("PHKT")) {
                        self.createPhuongHoang(Integer.parseInt(tachTime[1]), Animal.PHUONG_HOANG_KIM, true);
                    } else if (tachTime[0].equals("PHTT")) {
                        self.createPhuongHoang(Integer.parseInt(tachTime[1]), Animal.PHUONG_HOANG_THO, true);

                    } else if (tachTime[0].equals("MNT")) {
                        Item newitem = Map.doCreateMatNa3(self, Integer.parseInt(tachTime[1]), 837, 10, 5);
                    } else if (tachTime[0].equals("MNC")) {
                        Item newitem2 = Map.doCreateMatNa3(self, Integer.parseInt(tachTime[1]), 838, 11, 5);

                    }
                }
            }
            self.sendMessage(MessageCreator.createCharInventoryMessage(self, 0));
            self.sendMessage(MessageCreator.createServerAlertMessage("Chúc mừng bạn đã nhận được phần quà từ " + code + ".", ""));
            Database.instance.saveOrtherLog("", self.charname, "MOC: " + code, "mocNap");
            return;
        }
    }

    static void doAddGiftEventOffline(Char self) {
        if (!Char.NhanGiftCode) {
            return;
        }
        String result = Database.instance.getGiftCode_DB(self);
        // System.out.println(result);
        String[] tach_string = result.trim().split("\\|");
        int id = Integer.parseInt(tach_string[0]);
        if (id == 1) {
            if (self.getLevel() < Integer.parseInt(tach_string[5])) {
                self.sendMessage(MessageCreator.createServerAlertMessage("Bạn phải đạt cấp " + tach_string[5] + " thì mới nhận được giftcode", ""));
                return;
            }
            String code = tach_string[6];
            //System.out.println(self.charname + " nhap CODE: " + code + " KQ= " + result + " ID= " + id);
            int xu = Integer.parseInt(tach_string[1]);
            int luong = Integer.parseInt(tach_string[2]);
            int luongkhoa = Integer.parseInt(tach_string[3]);
            String listItem = tach_string[4];
            //System.out.println("XU: " + xu + " - luong: " + luong + " - luongkhoa: " + luongkhoa + " - listItem: " + listItem + " - ");
            if (xu != 0) {
                self.addXu(xu, "nhan tu giftcode" + code);
            }
            if (luong != 0) {
                self.addLuong(luong);
            }
            if (luongkhoa != 0) {
                self.addLuongLock(luongkhoa);
            }
            if (!listItem.equals("NULL")) {
                String[] tachItem = listItem.trim().split(",");
                for (int i = 0; i < tachItem.length; i++) {
                    String[] tachTime = tachItem[i].trim().split(":");
                    if (tachTime[0].equals("DB")) {
                        for (int j = 0; j < Integer.parseInt(tachTime[2]); j++) {
                            GiftCode.AddItem(self, Integer.parseInt(tachTime[1]), 0, 0, Integer.parseInt(tachTime[3]));
                        }
                    } else if (tachTime[0].equals("EGG")) {
                        for (int j = 0; j < Integer.parseInt(tachTime[2]); j++) {
                            self.createTrungPhuongHoang(1L, Integer.parseInt(tachTime[1]));
                        }

                    } else if (tachTime[0].equals("DH")) {
                        for (int j = 0; j < Integer.parseInt(tachTime[2]); j++) {
                            self.addDanhHieu(Short.parseShort(tachTime[1]), Long.parseLong(tachTime[3]));
                        }
                        try {
                            self.sendMessage(MessageCreator.createMainCharInfoMessage(self));
                            self.sendToNearPlayer(MessageCreator.createCharInfo(self));
                        } catch (IOException ex) {
                        }
                    } else if (tachTime[0].equals("PET")) {
                        for (int j = 0; j < Integer.parseInt(tachTime[2]); j++) {
                            self.createPet(Integer.parseInt(tachTime[1]), Integer.parseInt(tachTime[3]));
                        }
                    } else if (tachTime[0].equals("TK")) {
                        self.createThanKhi(self, Integer.parseInt(tachTime[1]), 0, Integer.parseInt(tachTime[3]), null, false, true, 1, false);    
                    } else if (tachTime[0].equals("PETV")) {
                      
                            self.createPetVinhVien(Integer.parseInt(tachTime[1]));
                      

                    } else if (tachTime[0].equals("PPR")) {
                        for (int j = 0; j < Integer.parseInt(tachTime[2]); j++) {
                            self.createtemCoat(Integer.parseInt(tachTime[1]), self, Integer.parseInt(tachTime[4]), false, Integer.parseInt(tachTime[3]));
                        }
                    } else if (tachTime[0].equals("PPRK")) {
                        for (int j = 0; j < Integer.parseInt(tachTime[2]); j++) {
                            self.createtemCoat_chonkhang(Integer.parseInt(tachTime[1]), self, Integer.parseInt(tachTime[4]), false, Integer.parseInt(tachTime[3]), Integer.parseInt(tachTime[5]));
                        }
                    } else if (tachTime[0].equals("PPV")) {
                        for (int j = 0; j < Integer.parseInt(tachTime[2]); j++) {
                            self.createtemCoat(Integer.parseInt(tachTime[1]), self, Integer.parseInt(tachTime[4]), true, Integer.parseInt(tachTime[3]));
                        }
                    } else if (tachTime[0].equals("PPVK")) {
                        for (int j = 0; j < Integer.parseInt(tachTime[2]); j++) {
                            self.createtemCoat_chonkhang(Integer.parseInt(tachTime[1]), self, Integer.parseInt(tachTime[4]), true, Integer.parseInt(tachTime[3]), Integer.parseInt(tachTime[5]));
                        }

                    } else if (tachTime[0].equals("PPLUCK")) {
                        self.createtemCoat(Integer.parseInt(tachTime[1]), self, Integer.parseInt(tachTime[4]), true, 0);
                    } else if (tachTime[0].equals("PT")) {
                        int idGem = Integer.parseInt(tachTime[1]);
                        int soluong = Integer.parseInt((tachTime[2]));
                        self.potions[idGem] = self.potions[idGem] + soluong;
                        self.calculateAttrib();
                        try {
                            self.sendMessage(MessageCreator.createMainCharInfoMessage(self));
                            self.sendMessage(MessageCreator.createCharInventoryMessage(self, 0));
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
                        self.doAddGemItem(Integer.parseInt(tachTime[1]), Integer.parseInt(tachTime[2]), lockitemgem);
                        self.sendMessage(MessageCreator.createCharGemItem(self));
                    } else if (tachTime[0].equals("DN")) {
                        self.diemNapVip += Integer.parseInt(tachTime[2]);
                    } else if (tachTime[0].equals("MN")) {
                        self.luongNap += Integer.parseInt(tachTime[2]);
                    } else if (tachTime[0].equals("PP")) {
                        Char.createPhiPhongMaxBaoKich(self, 0, Integer.parseInt(tachTime[3]), (Item) null, false, false, 0, true);
                        Char.createPhiPhongMaxBaoKich(self, 1, Integer.parseInt(tachTime[3]), (Item) null, false, false, 0, true);
                    } else if (tachTime[0].equals("PPLUCK")) {
                        self.createtemCoat(Integer.parseInt(tachTime[1]), self, Integer.parseInt(tachTime[4]), true, 0);
                    } else if (tachTime[0].equals("PP8X")) {
                        self.createPhiPhongMaxBaoKich(self, 0, Integer.parseInt(tachTime[3]), (Item) null, true, true, 5, true);
                        self.createPhiPhongMaxBaoKich(self, 1, Integer.parseInt(tachTime[3]), (Item) null, true, true, 5, true);
                    } else if (tachTime[0].equals("VKTT")) {
                        self.createVuKhiThoiTrang(self, 0, Integer.parseInt(tachTime[3]), (Item) null, false, true, 1, false);
                    } else if (tachTime[0].equals("VKMAX")) {
                        self.createVuKhiThoiTrang(self, 0, Integer.parseInt(tachTime[3]), (Item) null, true, true, 1, false);
                    } else if (tachTime[0].equals("VKTB")) {
                        self.createVuKhiThoiTrangTHANBINH_NEW(self, 0, Integer.parseInt(tachTime[3]), (Item) null, false, true, 1, false);
                    } else if (tachTime[0].equals("VKTBMAX")) {
                        self.createVuKhiThoiTrangTHANBINH_NEW(self, 0, Integer.parseInt(tachTime[3]), (Item) null, true, true, 1, false);
                        
                    } else if (tachTime[0].equals("BT")) {
                        self.doCreatePetBachThu(Integer.parseInt(tachTime[3]), false);
                    } else if (tachTime[0].equals("AOTT")) {
                        Map.doBuyModelClothe(self, Integer.parseInt(tachTime[1]), Integer.parseInt(tachTime[3]), 1);
                    } else if (tachTime[0].equals("AOSHOP")) {
                        Char.doDropModelClothe(self, Integer.parseInt(tachTime[1]), Integer.parseInt(tachTime[3]));
                    } else if (tachTime[0].equals("CHOI")) {
                        self.createChoi(Integer.parseInt(tachTime[1]), self, Integer.parseInt(tachTime[3]), Integer.parseInt(tachTime[4]), true);
                    } else if (tachTime[0].equals("MATNA")) {
                        int[] valuemn = {5, 5, 10, 30, 50};
                        int randomMN = Map.r.nextInt(valuemn.length);
                        if (Integer.parseInt(tachTime[1]) > -1) {
                            randomMN = Integer.parseInt(tachTime[1]);
                        }
                        Item newitem = Map.doCreateMatNa(self, Integer.parseInt(tachTime[3]), Char.ID_MAT_NA[randomMN], randomMN, valuemn[randomMN]);
                        self.sendMessage(MessageCreator.createServerAlertMessage("Nhận " + newitem.getName() + " 7 ngày", ""));
                    } else if (tachTime[0].equals("MATNA2")) {
                        // Tạo mặt nạ với option và value random
                        int[] optionValues = {5, 5, 10, 30, 50}; // Các giá trị value có thể
                        int randomIndex = Map.r.nextInt(optionValues.length);
                        int matnaID = Integer.parseInt(tachTime[1]); // ID mặt nạ
                        int option = randomIndex; // Option mặt nạ (0-4)
                        int value = optionValues[randomIndex];  // Giá trị option tương ứng
                        Item newitem = Map.doCreateMatNa2(self, Integer.parseInt(tachTime[3]), matnaID, option, value);
                        self.sendMessage(MessageCreator.createServerAlertMessage("Nhận " + newitem.getName() + (Integer.parseInt(tachTime[3]) > 0 ? " " + Integer.parseInt(tachTime[3])/1440 + " ngày" : " vĩnh viễn"), ""));    
                    } else if (tachTime[0].equals("DK")) {
                        self.createHeo(0, false);
                    } else if (tachTime[0].equals("TL")) {
                        self.createTuanLoc(Integer.parseInt(tachTime[1]));
                    } else if (tachTime[0].equals("DBT")) {
                        self.createPoro(Integer.parseInt(tachTime[1]), false);

                        ///  todo REGEX quà tích lũy 
                    } else if (tachTime[0].equals("LANT")) {
                        self.createLan(Integer.parseInt(tachTime[1]), false, 95);

                    } else if (tachTime[0].equals("DKT")) {
                        self.createHeo(Integer.parseInt(tachTime[1]), true);

                    } else if (tachTime[0].equals("PHT")) {
                        self.createPhuongHoang(Integer.parseInt(tachTime[1]), Animal.PHUONG_HOANG, true);
                    } else if (tachTime[0].equals("PHBT")) {
                        self.createPhuongHoang(Integer.parseInt(tachTime[1]), Animal.PHUONG_HOANG_BANG, true);
                    } else if (tachTime[0].equals("PHDST")) {
                        self.createPhuongHoang(Integer.parseInt(tachTime[1]), Animal.PHUONG_HOANG_7MAU, true);
                    } else if (tachTime[0].equals("PHMT")) {
                        self.createPhuongHoang(Integer.parseInt(tachTime[1]), Animal.PHUONG_HOANG_MOC, true);
                    } else if (tachTime[0].equals("PHKT")) {
                        self.createPhuongHoang(Integer.parseInt(tachTime[1]), Animal.PHUONG_HOANG_KIM, true);
                    } else if (tachTime[0].equals("PHTT")) {
                        self.createPhuongHoang(Integer.parseInt(tachTime[1]), Animal.PHUONG_HOANG_THO, true);
                    } else if (tachTime[0].equals("MNT")) {
                        Item newitem = Map.doCreateMatNa3   (self, Integer.parseInt(tachTime[1]), 837, 10, 5);
                    } else if (tachTime[0].equals("MNC")) {
                        Item newitem2 = Map.doCreateMatNa3(self, Integer.parseInt(tachTime[1]), 838, 11, 5);

                    }
                }
            }
            self.sendMessage(MessageCreator.createCharInventoryMessage(self, 0));
            self.sendMessage(MessageCreator.createServerAlertMessage("Chúc mừng bạn đã nhận được phần quà từ hệ thống.", ""));
            Database.instance.saveOrtherLog("", self.charname, "CODE: " + code, "putGiftCode");
            return;
        }
    }

    static void doChuc83(Char self, final String friendName) {
        if (!Char.isSuKien83()) {
            return;
        }
        final int[] per = {5000, 5000, 5000, 5000, 5000, 5000, 5000, 5000, 10, 10, 2000, 200, 500, 5000, 5000, 5000, 5000, 5000, 5000, 5000, 5000, 5000, 5000, 5000, 5000, 5000, 5000, 200, 200, 200, 200, 200, 200, 10, 1000, 1000, 1000, 1000, 1000, 10, 10};
        final int[] idgift = {84, 8, 113, 112, 114, 94, 96, 29, GemTemplate.LKD_40, GemTemplate.LKD_35, GemTemplate.DA_TIEN_GIAI, GemTemplate.BOT_XANH_LA, GemTemplate.BOT_XANH, GemTemplate.BOT_TRANG, GemTemplate.DUC_TUONG_KHAM_1, GemTemplate.DUC_TUONG_KHAM_2, GemTemplate.DUC_TUONG_KHAM_3, GemTemplate.DUC_TUONG_KHAM_4, GemTemplate.DUC_TUONG_KHAM_5, GemTemplate.DUC_TUONG_KHAM_6, GemTemplate.DA_MAY_MAN_C4, GemTemplate.DA_MAY_MAN_C5, GemTemplate.VAI_C1, GemTemplate.SAT_C1, GemTemplate.NGOC_C1, GemTemplate.GO_THUONG_C1, GemTemplate.DA_MEM_C1, 587, 588, 589, 590, 591, 592, 83, 715, 716, 717, 718, 719, 29, Potion.TYPE_SACH_SKILL_PET};
        final int[] typegift = {4, 4, 4, 4, 4, 4, 4, 4, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, -1, -1, -1, -1, -1, -1, 4, 3, 3, 3, 3, 3, -4, -5};
        final int[] soluongmax = {5, 3, 5, 5, 5, 100, 100, 10, 1, 1, 3, 50, 100, 100, 20, 20, 20, 20, 20, 20, 100, 100, 100, 100, 100, 100, 100, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1};
        final int[] soluongmin = {3, 1, 1, 1, 1, 10, 10, 5, 1, 1, 1, 10, 50, 50, 10, 10, 10, 10, 10, 10, 50, 50, 50, 50, 50, 50, 50, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1};
        String info = "Chúc mừng bạn nhận được ";
        final int index = Char.getIndexRandom(per);
        Label_2933:
        {
            if (typegift[index] == -5) {
                Map.doCreateBookSkillPet(self, (Map.r.nextInt(100) >= 30) ? 1 : 0);
                info = "sach skill pet";
            } else if (typegift[index] == -4) {
                final int[] time = {1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 3, 3, 5};
                self.doCreatePetBachThu(time[Map.r.nextInt(time.length)] * 24 * 60, false);
                info = "pet bach thu";
            } else if (typegift[index] == -1) {
                final int idtemp = idgift[index];
                Item newItem = null;
                if (!ItemLuckyDraw.isChoi(idtemp)) {
                    newItem = ItemLuckyDraw.createtemCoat(idtemp, self, Map.r.nextInt(2), false, (Map.r.nextInt(7) + 1) * 24 * 60);
                    newItem.timeLoan = System.currentTimeMillis();
                    newItem.minuteExist = (Map.r.nextInt(7) + 1) * 24 * 60;
                    self.sendMessage(MessageCreator.createServerAlertMessage("Nhận được " + newItem.getName() + " thời hạn " + newItem.minuteExist + " phút", ""));
                    self.sendMessage(MessageCreator.createCharInventoryMessage(self, 1));
                    info = String.valueOf(newItem.getName()) + "_" + newItem.getDBInfo() + "|" + newItem.getAttribute() + "_" + newItem.level + " thoi han " + newItem.minuteExist;
                    try {
                        com.htbb.chongspam.ThongBaoGop.dua("nhan_thuong", String.valueOf(self.charname) + " đã nhận được " + newItem.getName() + " thời hạn " + newItem.minuteExist + " phút khi gửi lời chúc 8/3 đến bạn " + friendName);
                    } catch (final Exception ex) {
                    }
                } else {
                    newItem = new Item((ItemTemplates) Map.itemTemplates.get(5).get(idtemp), false, 0, 0, idtemp);
                    newItem.id = self.getIDItem();
                    newItem.owner = self.charDBID;
                    newItem.level = newItem.getTemplate().level;
                    newItem.identify = self.charDBID;
                    newItem.clazz = self.charClass;
                    newItem.createAttChoi();
                    newItem.dateCreate = Char.getDayOpen(0L);
                    newItem.timeLoan = System.currentTimeMillis();
                    newItem.minuteExist = (Map.r.nextInt(7) + 1) * 24 * 60;
                    self.iItems.add(newItem);
                    self.sendMessage(MessageCreator.createServerAlertMessage("Nhận được " + newItem.getName() + " thời hạn " + newItem.minuteExist + " phút", ""));
                    self.sendMessage(MessageCreator.createCharInventoryMessage(self, 1));
                    info = String.valueOf(newItem.getName()) + "_" + newItem.getDBInfo() + "|" + newItem.getAttribute() + "_" + newItem.level + " thoi han " + newItem.minuteExist;
                    try {
                        com.htbb.chongspam.ThongBaoGop.dua("nhan_thuong", String.valueOf(self.charname) + " đã nhận được " + newItem.getName() + " thời hạn " + newItem.minuteExist + " phút khi gửi lời chúc 8/3 đến bạn " + friendName);
                    } catch (final Exception ex2) {
                    }
                }
            } else if (typegift[index] == -2) {
                self.addXu(soluongmax[index], "char 2");
                info = String.valueOf(info) + soluongmax[index] + " xu";
                self.sendMessage(MessageCreator.createServerAlertMessage(info, ""));
            } else if (typegift[index] == -3) {
                Map.addXpCharEvent(self, soluongmax[index], false, "char doChuc83");
                info = String.valueOf(info) + soluongmax[index] + " exp";
                self.sendMessage(MessageCreator.createServerAlertMessage(info, ""));
            } else if (typegift[index] == 4) {
                int soluong = soluongmin[index];
                if (soluongmin[index] < soluongmax[index]) {
                    soluong += Map.r.nextInt(soluongmax[index] - soluongmin[index] + 1);
                }
                final int[] potions = self.potions;
                final int n = idgift[index];
                potions[n] += soluong;
                info = String.valueOf(info) + soluong + " " + LoginHandler.PORTION_NAME[idgift[index]];
                self.sendMessage(MessageCreator.createServerAlertMessage(info, ""));
            } else if (typegift[index] == 6) {
                int soluong = soluongmin[index];
                if (soluongmin[index] < soluongmax[index]) {
                    soluong += Map.r.nextInt(soluongmax[index] - soluongmin[index] + 1);
                }
                final boolean lock = Map.r.nextInt(2) == 1;
                self.doAddGemItem(idgift[index], soluong, lock);
                info = String.valueOf(info) + soluong + " " + Map.gemTemplate[idgift[index]].name + (lock ? " khoá" : "");
                self.sendMessage(MessageCreator.createCharGemItem(self));
                self.sendMessage(MessageCreator.createServerAlertMessage(info, ""));
                if (idgift[index] != GemTemplate.LKD_35) {
                    if (idgift[index] != GemTemplate.LKD_40) {
                        break Label_2933;
                    }
                }
                try {
                    com.htbb.chongspam.ThongBaoGop.dua("nhan_thuong", String.valueOf(self.charname) + " đã nhận được " + Map.gemTemplate[idgift[index]].name + " khi gửi lời chúc 8/3 đến bạn " + friendName);
                } catch (final Exception ex3) {
                }
            } else if (typegift[index] == 3) {
                if (self.isFullInventory()) {
                    final int xu = 5000 + Map.r.nextInt(100000);
                    self.addXu(xu, "char 3");
                    info = String.valueOf(info) + xu + " xu";
                    self.sendMessage(MessageCreator.createCharInventoryMessage(self, 0));
                } else {
                    final ItemTemplates template = (ItemTemplates) Map.itemTemplates.get(5).get(idgift[index]);
                    Item newItem = null;
                    newItem = new Item(template, false, template.clazz, template.clazz, template.id);
                    newItem.id = self.getIDItem();
                    newItem.owner = self.charDBID;
                    newItem.level = newItem.getTemplate().level;
                    newItem.identify = self.charDBID;
                    int time2 = Map.r.nextInt(48) + 1;
                    final int pc = Map.r.nextInt(100);
                    if (pc < 1) {
                        time2 = 360;
                    } else if (pc < 10) {
                        time2 = 240;
                    } else if (pc < 50) {
                        time2 = 72 + Map.r.nextInt(49);
                    }
                    newItem.timeLoan = System.currentTimeMillis();
                    newItem.minuteExist = time2 * 60;
                    if (newItem.isVukhiThoiTrang()) {
                        newItem.createAttributeVuKhiThoiTrang();
                    }
                    newItem.dateCreate = Char.getDayTime(0L);
                    self.iItems.add(newItem);
                    self.sendMessage(MessageCreator.createCharInventoryMessage(self, 1));
                    info = String.valueOf(info) + newItem.getName();
                    try {
                        com.htbb.chongspam.ThongBaoGop.dua("nhan_thuong", String.valueOf(self.charname) + " đã nhận được " + newItem.getName() + " khi tặng hoa");
                    } catch (final Exception ex4) {
                    }
                }
                self.sendMessage(MessageCreator.createServerAlertMessage(info, ""));
            }
        }
        Database.instance.saveOrtherLog("", self.charname, info, "chuc83");
        if (Char.isSuKien83()) {
            Database.instance.addCharEvent83(self);
        }
    }

    static void loadAllItemLucky() {
        if (TeamServer.isServerIndo()) {
            return;
        }
        for (int i = 584; i <= 592; ++i) {
            final ItemLuckyDraw item = new ItemLuckyDraw();
            item.cat = 3;
            item.id = i;
            final ItemTemplates it = (ItemTemplates) Map.itemTemplates.get(5).get(i);
            item.name = String.valueOf(it.name) + " lv " + it.level;
            Char.allItemLuckyDraw.add(item);
        }
        ItemLuckyDraw item2 = new ItemLuckyDraw();
        item2.cat = 3;
        item2.id = 616;
        ItemTemplates it2 = (ItemTemplates) Map.itemTemplates.get(5).get(616);
        item2.name = String.valueOf(it2.name) + " lv " + it2.level;
        Char.allItemLuckyDraw.add(item2);
        item2 = new ItemLuckyDraw();
        item2.cat = 3;
        item2.id = 617;
        it2 = (ItemTemplates) Map.itemTemplates.get(5).get(617);
        item2.name = String.valueOf(it2.name) + " lv " + it2.level;
        Char.allItemLuckyDraw.add(item2);
        item2 = new ItemLuckyDraw();
        item2.cat = 3;
        item2.id = 618;
        it2 = (ItemTemplates) Map.itemTemplates.get(5).get(618);
        item2.name = String.valueOf(it2.name) + " lv " + it2.level;
        Char.allItemLuckyDraw.add(item2);
        item2 = new ItemLuckyDraw();
        item2.cat = 3;
        item2.id = 619;
        it2 = (ItemTemplates) Map.itemTemplates.get(5).get(619);
        item2.name = String.valueOf(it2.name) + " lv " + it2.level;
        Char.allItemLuckyDraw.add(item2);
        // Canh (898-902) - them 5 phan tu NGAY TAI DAY. Char.addMore da duoc tang
        // tu 2 len 7 (xem Char.java) de bu dung 5 vi tri nay - groupLucky[] van
        // tro dung vao vong lap idgem/ps o duoi. Doi vi tri chen (hoac so luong
        // phan tu) ma khong doi addMore theo se lam phan thuong phu (idgem/ps)
        // trung sang nham vat pham khac - xem KiemVongQuayThanTai.
        item2 = new ItemLuckyDraw();
        item2.cat = 3;
        item2.id = 898;
        it2 = (ItemTemplates) Map.itemTemplates.get(5).get(898);
        item2.name = String.valueOf(it2.name) + " lv " + it2.level;
        Char.allItemLuckyDraw.add(item2);

        item2 = new ItemLuckyDraw();
        item2.cat = 3;
        item2.id = 899;
        it2 = (ItemTemplates) Map.itemTemplates.get(5).get(899);
        item2.name = String.valueOf(it2.name) + " lv " + it2.level;
        Char.allItemLuckyDraw.add(item2);

        item2 = new ItemLuckyDraw();
        item2.cat = 3;
        item2.id = 900;
        it2 = (ItemTemplates) Map.itemTemplates.get(5).get(900);
        item2.name = String.valueOf(it2.name) + " lv " + it2.level;
        Char.allItemLuckyDraw.add(item2);

        item2 = new ItemLuckyDraw();
        item2.cat = 3;
        item2.id = 901;
        it2 = (ItemTemplates) Map.itemTemplates.get(5).get(901);
        item2.name = String.valueOf(it2.name) + " lv " + it2.level;
        Char.allItemLuckyDraw.add(item2);

        item2 = new ItemLuckyDraw();
        item2.cat = 3;
        item2.id = 902;
        it2 = (ItemTemplates) Map.itemTemplates.get(5).get(902);
        item2.name = String.valueOf(it2.name) + " lv " + it2.level;
        Char.allItemLuckyDraw.add(item2);


        item2 = new ItemLuckyDraw();
        item2.cat = 6;
        item2.id = 11;
        item2.name = Map.gemTemplate[11].name;
        Char.allItemLuckyDraw.add(item2);
        item2 = new ItemLuckyDraw();
        item2.cat = 12;
        item2.id = 5;
        item2.name = Map.petTemplate[5].name;
        item2.idIconPet = Map.petTemplate[5].idImgPaint;
        Char.allItemLuckyDraw.add(item2);
        item2 = new ItemLuckyDraw();
        item2.cat = 12;
        item2.id = 16;
        item2.name = Map.petTemplate[16].name;
        item2.idIconPet = Map.petTemplate[16].idImgPaint;
        Char.allItemLuckyDraw.add(item2);
        item2 = new ItemLuckyDraw();
        item2.cat = 12;
        item2.id = 17;
        item2.name = Map.petTemplate[17].name;
        item2.idIconPet = Map.petTemplate[17].idImgPaint;
        Char.allItemLuckyDraw.add(item2);
        final short[] idgem = {67, 74, 81, 88, 95, 68, 75, 82, 89, 96, 102, 109, 116, 123, 130, 103, 110, 117, 124, 131, 137, 60, 61, 62, 63, 64, 65};
        for (int j = 0; j < idgem.length; ++j) {
            item2 = new ItemLuckyDraw();
            item2.cat = 6;
            item2.id = idgem[j];
            item2.isSelect = 0;
            item2.name = Map.gemTemplate[idgem[j]].name;
            Char.allItemLuckyDraw.add(item2);
        }
        final byte[] ps = {3, 21, 22, 94, 6, 23, 24, 96, 100, 0, 84};
        final int[] value = {3, 3, 3, 3, 3, 3, 3, 3, 1, 1000, 1};
        for (int k = 0; k < ps.length; ++k) {
            item2 = new ItemLuckyDraw();
            item2.cat = 4;
            item2.id = ps[k];
            item2.value = value[k];
            item2.name = String.valueOf(value[k]) + " " + LoginHandler.PORTION_NAME[ps[k]];
            item2.isSelect = 0;
            Char.allItemLuckyDraw.add(item2);
        }
    }

    static void doFinishLuckyDraw(Char self) {
        try {
            if (self.indexItemLuckyDraw == -1) {
                return;
            }
            
            int pc = Map.r.nextInt(100); // Set tỉ lệ min
            int soluongtrung = 3;
            final byte[] index = new byte[soluongtrung];
            byte n = 0;
            String info = "";
            int pcwin = Char.pcWinLuckyDraw;
            
            // Lưu index gốc và kiểm tra phượng hoàng
            final int originalIndex = self.indexItemLuckyDraw;
            boolean isPhuongHoang = Char.allItemLuckyDraw.get(originalIndex).isPhuongHoang();
            int lv = 35;
    
            // Xử lý đặc biệt cho phượng hoàng
            if (isPhuongHoang) {
                pc = Map.r.nextInt(100_000_000); // set tỉ lệ max
                pcwin = 1;
                lv = 1;
                ++Map.countQuaySo;
                System.out.println("");
               
            }
    
            // Kiểm tra người chơi đặc biệt
            try {
                if (Char.except_quay_so != null) {
                    for (int i = 0; i < Char.except_quay_so.length; ++i) {
                        if (self.charname.toLowerCase().equals(Char.except_quay_so[i].toLowerCase().trim())) {
                            self.winPH = 1;
                            break;
                        }
                    }
                }
            } catch (Exception ex) {}
    
            // Xử lý điều kiện trúng phượng hoàng
            if (isPhuongHoang) {
                if (Char.isFinishAllSuKienHaloween2016() || self.winPH == 1) {
                    pcwin = -1;
                } else {
                    synchronized (Char.allItemLuckyDraw) {
                        if (Map.countQuaySo >= Map.MAX_QUAY) {
                            Map.MAX_QUAY *= 2;
                            Map.countQuaySo = 0;
                            if (Map.max_phuong_hoang > 0) {
                                pcwin = 10000;
                                System.out.println(self.charname + " QUAY SO TRUNG PHUONG HOANG NE");
                            } else {
                                pcwin = -1;
                            }
                            Char.timeGetPhuongHoang = System.currentTimeMillis() + (Map.r.nextInt(5) + 1) * 60 * 60000;
                        } else {
                            pcwin = -1;
                        }
                    }
                }
            }
    
            // Xử lý admin
            if (self.isAdmin) {
                pcwin = 100_000_000;
                isPhuongHoang = true;
                Map.max_phuong_hoang = 1;
                Map.nPhuongHoang = 1;
            }
    
            // Xử lý trúng thưởng
            Label_0613: {
                if (pc < pcwin && self.lvDetail.lv >= lv) {
                    if (isPhuongHoang) {
                        synchronized (Char.allItemLuckyDraw) {
                            if (Map.max_phuong_hoang > 0 && Map.nPhuongHoang > 0) {
                                --soluongtrung;
                                index[0] = (byte)originalIndex;
                                ++n;
                                self.winPH = 1;
                                --Map.max_phuong_hoang;
                                --Map.nPhuongHoang;
                                if (Map.nPhuongHoang < 0) {
                                    Map.nPhuongHoang = 0;
                                }
                                Char.allItemLuckyDraw.get(originalIndex).addGif(self, 30);
                                Database.instance.saveEvent(Map.event.getInfo());
                                break Label_0613;
                            }
                        }
                    }
                    
                    // Xử lý trúng item thường
                    --soluongtrung;
                    index[0] = (byte)originalIndex;
                    ++n;
                    final int pc2 = Map.r.nextInt(10000);
                    int time = 0;
                    if (pc2 > 1) {
                        if (pc2 <= 400) {
                            time = 7200;
                        } else if (pc2 <= 700) {
                            time = 4320;
                        } else {
                            time = 1440;
                        }
                    }
                    Char.allItemLuckyDraw.get(originalIndex).addGif(self, time);
                }
            }
    
            // Xử lý phần thưởng phụ
            final Hashtable<Byte, Byte> idselect = new Hashtable<>();
            for (int j = 0; j < soluongtrung; ++j) {
                byte[] glucky = Char.groupLucky[0];
                int ran = Map.r.nextInt(100);
                if (ran < 3) {
                    glucky = Char.groupLucky[3];
                } else if (ran < 8) {
                    glucky = Char.groupLucky[2];
                } else if (ran < 50) {
                    glucky = Char.groupLucky[1];
                }
    
                int idx;
                ItemLuckyDraw checkItem;
                for (idx = glucky[Map.r.nextInt(glucky.length)] + Char.addMore; 
                    idselect.get((byte)idx) != null || 
                    ((checkItem = Char.allItemLuckyDraw.get(idx)).cat == 12 || 
                    (checkItem.cat == 3 && Map.r.nextInt(1000) < 990)); // 99% sẽ bỏ qua cat 3
                    idx = glucky[Map.r.nextInt(glucky.length)] + Char.addMore) {
                    
                    glucky = Char.groupLucky[0];
                    if (self.lvDetail.lv >= 35) {
                        ran = Map.r.nextInt(100);
                        if (ran < 3) {
                            glucky = Char.groupLucky[3];
                        } else if (ran < 8) {
                            glucky = Char.groupLucky[2];
                        } else if (ran < 50) {
                            glucky = Char.groupLucky[1];
                        }
                    }
                }
    
                final ItemLuckyDraw item = Char.allItemLuckyDraw.get(idx);
                if (item.cat == 4) {
                    if (item.id == 0) {
                        self.xu += item.value;
                        info = String.valueOf(info) + item.value + " xu,";
                    } else {
                        final int[] potions = self.potions;
                        final int id = item.id;
                        potions[id] += item.value;
                        try {
                            info = String.valueOf(info) + item.value + " " + LoginHandler.PORTION_NAME[item.id] + ",";
                        } catch (Exception ex2) {}
                    }
                } else if (item.cat == 6) {
                    self.doAddGemItem(item.id);
                    try {
                        info = String.valueOf(info) + " 1 " + Map.gemTemplate[item.id].name + ",";
                    } catch (Exception ex3) {}
                }
                
                idselect.put((byte)idx, (byte)idx);
                index[n++] = (byte)idx;
            }
    
            // Gửi thông báo kết quả
            final Message m = new Message(87);
            try {
                m.dos.writeByte(1);
                m.dos.writeByte(3);
                for (int k = 0; k < 3; ++k) {
                    m.dos.writeByte(index[k]);
                }
                self.sendMessage(m);
            } catch (Exception ex4) {}
    
            // Cập nhật inventory và gem
            self.sendMessage(MessageCreator.createCharInventoryMessage(self, 0));
            self.sendMessage(MessageCreator.createCharGemItem(self));
    
            // Lưu log
            try {
                Database.instance.saveLogThongKeChiTieu("quayso " + Char.allItemLuckyDraw.get(originalIndex).id, 
                                                      0, 1, 1L, "quayso");
            } catch (Exception ex5) {}
    
            // Reset và lưu thông tin
            self.indexItemLuckyDraw = -1;
            Database.instance.saveOrtherLog("", self.charname, info, "quayso");
    
            // Kiểm tra sự kiện
            Char.isSuKienTrungThul2016();
            if (Char.isSuKienHaloween2016()) {
                Map.r.nextInt(100);
            }
            Char.isSuKienHe2017();
    
            // Xử lý book skill pet
            try {
                if (Map.r.nextInt(1000) < 5) {
                    Map.doCreateBookSkillPet(self, 0);
                }
            } catch (Exception ex7) {}
    
        } catch (Exception e) {
            Logger.logError(e, "errorquayso.txt");
        }
    }

}
