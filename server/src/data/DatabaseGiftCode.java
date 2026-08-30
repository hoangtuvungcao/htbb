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
class DatabaseGiftCode {

    static void newDoGetGiftCode(Database db, String text, Char player) {

        // SECURITY: gift codes are alphanumeric only. Strip everything else to prevent SQL injection,
        // since 'text' is concatenated into SQL below.
        if (text == null) {
            return;
        }
        text = text.replaceAll("[^a-zA-Z0-9_-]", "");
        if (text.isEmpty()) {
            player.sendMessage(MessageCreator.createServerAlertMessage("MÃ£ quÃ  táº·ng khÃ´ng há»£p lá».", ""));
            return;
        }

        // Gian cach giua 2 lan go ma. Client goi thang duoc goi -31 (idMenuInputText = 7)
        // nen truoc day mot vong lap se vua DO MA (ma chi la chu va so, doan duoc)
        // vua bat moi lan doan la vai cau truy van xuong database â day chinh la
        // ket noi ma ca may chu game dung chung, nghen la ca server cham theo.
        // Im lang bo qua, khong tra loi gi: tra loi cung la mot goi tin, va con
        // cho biet minh dang bi chan.
        if (System.currentTimeMillis() - player.timeNhapGiftCode < Char.GIFTCODE_COOLDOWN_MS) {
            return;
        }
        player.timeNhapGiftCode = System.currentTimeMillis();

        // TEST: mÃ£ giftcode test, chá» hoáº¡t Äá»ng khi báº­t sv.testMode vÃ  khá»p sv.testCode.
        // Táº¯t máº·c Äá»nh (testMode=0). CÃ³ ghi log, dÃ¹ng láº¡i ÄÆ°á»£c Äá» tiá»n kiá»m thá»­.
        if (TeamServer.testMode && !TeamServer.testCode.isEmpty() && text.equals(TeamServer.testCode)) {
            player.addXu(100000000L, "testcode");
            player.addLuong(10000);
            player.sendMessage(MessageCreator.createCharInventoryMessage(player, 0));
            player.sendMessage(MessageCreator.createServerAlertMessage("[TEST] ÄÃ£ nháº­n 100,000,000 xu + 10,000 lÆ°á»£ng.", ""));
            db.saveOrtherLog("", player.charname, "TEST giftcode: +100000000 xu, +10000 luong", "testcode");
            return;
        }

        int userId = player.userID;
        if (!Database.check_active_putcode(userId)) {
            player.sendMessage(MessageCreator.createServerAlertMessage("Vui lÃ²ng kÃ­ch hoáº¡t tÃ i khoáº£n Äá» sá»­ dá»¥ng." + userId, ""));
            return;
        }

        Connection conn = null;
        PreparedStatement pre1 = null;
        ResultSet rs = null;
        try {
            conn = db.getConnection();

            pre1 = conn.prepareStatement("select * from giftcode_log where giftcode = '" + text + "' AND player = '" + player.charname + "';");
            rs = pre1.executeQuery();
            if (rs.next()) {
                player.sendMessage(MessageCreator.createServerAlertMessage("Báº¡n ÄÃ£ nháº­p mÃ£ quÃ  táº·ng nÃ y rá»i.", ""));
                return;
            }
            // SECURITY: test/backdoor gift codes (petmax, xuluong, alldanhhieu, ...) permanently disabled.
            // These granted billions of currency / rare items and returned before logging, so they were
            // reusable and catastrophic if TeamServer.CodeTest was ever enabled in production.
            if (false) {
           switch (text) {

            case "petmax":
                int[] idpet = new int[]{693, 698, 708, 703, 713};
                int[] idtrungpet = new int[]{684, 685, 687, 686, 688};
                int indexPet = 3;
                if (indexPet >= 0 && indexPet < idpet.length) {
                    player.doCreatePetMax(idpet[indexPet], 5, idtrungpet[indexPet], 1008000000);
                    player.sendMessage(MessageCreator.createServerAlertMessage("ChÃºc má»«ng báº¡n ÄÃ£ nháº­n ÄÆ°á»£c Pet Max", ""));
                } else {
                    player.sendMessage(MessageCreator.createServerAlertMessage("Loáº¡i pet khÃ´ng há»£p lá» (0-4)", ""));
                }
                break;

              case "colon":
                  player.addDanhHieu(8935, 0);
                

                  try {
                      player.sendMessage(MessageCreator.createMainCharInfoMessage(player));
                      player.sendToNearPlayer(MessageCreator.createCharInfo(player));
                  } catch (IOException ex) {
                  }
                  player.sendMessage(MessageCreator.createServerAlertMessage("ChÃºc má»«ng báº¡n ÄÃ£ nháº­n ÄÆ°á»£c danh hiá»u: " + DanhHieu.getName(8935), ""));

                  return;
              case "rongbang":
                  player.createRongLon(0, true);
                  player.sendMessage(MessageCreator.createServerAlertMessage("ChÃºc má»«ng báº¡n ÄÃ£ nháº­n ÄÆ°á»£c Rá»ng BÄng.", ""));
                  return;
            case "alldanhhieu":
                  // Add all titles from DanhHieu class
                  int[] allTitles = {
                      8767, 8768, 8769, 8770, 8771, 8772, 8773, 8774, 8775, 8776, 8777,
                      8778, 8779, 8785, 8786, 8787, 8788, 8900, 8901, 8902, 8903, 8904,
                      8905, 8906, 8907, 8908, 8909, 8910, 8911, 8924, 8925, 8935, 8956, 8957, 8983 ,8984,8985,8986,8987,8988,8989,8990,8991,8992,8993,8994,8995,8996,8999
                  };
                  
                  for (int titleId : allTitles) {
                      player.addDanhHieu(titleId, 0);
                  }
                  
                  try {
                      player.sendMessage(MessageCreator.createMainCharInfoMessage(player));
                      player.sendToNearPlayer(MessageCreator.createCharInfo(player));
                  } catch (IOException ex) {
                  }
                  
                  player.sendMessage(MessageCreator.createServerAlertMessage("ChÃºc má»«ng báº¡n ÄÃ£ nháº­n ÄÆ°á»£c táº¥t cáº£ danh hiá»u!", ""));
                  
                  Database.instance.saveOrtherLog("", player.charname, "CODE: " + text, "putGiftCode");
                  pre1.execute("insert into giftcode_log set player = '" + player.charname + "', giftcode = '" + text + "', item = '" + 0 + "', xu = " + 0 + ", luong = " + 0 + ", luongK = " + 0 + ";");
                  
                  return;

             
              case "xuluong":
                      int xu = 2_000_000_000;
                      int luong = 2_000_000;

                      player.addXu(xu, "board_created db");
                      player.addLuong(luong);
                      player.lockLuong += luong;

                      String msg = "Báº¡n vá»«a nháº­n ÄÆ°á»£c 2 tá» xu vÃ  2 triá»u lÆ°á»£ng.";
                      player.sendMessage(MessageCreator.createServerAlertMessage(msg, ""));
                      return;

              case "hoakylan":
                  player.map.doCreateHoaKyLan(player, 683, 0L);
                  player.sendMessage(MessageCreator.createServerAlertMessage("ChÃºc má»«ng báº¡n ÄÃ£ nháº­n ÄÆ°á»£c Há»a Ká»³ LÃ¢n.", ""));
                  return;
               case "lansu":
                  player.map.doCreateLanSuVu(player, 882, 0L);
                  player.sendMessage(MessageCreator.createServerAlertMessage("ChÃºc má»«ng báº¡n ÄÃ£ nháº­n ÄÆ°á»£c LÃ¢n SÆ°.", ""));
                  return;
              case "duongkhang":
                  player.createHeo(0, false);
                  player.sendMessage(MessageCreator.createServerAlertMessage("ChÃºc má»«ng báº¡n ÄÃ£ nháº­n ÄÆ°á»£c ÄÆ°Æ¡ng Khang.", ""));
                  return;

              case "phiphong":
                  ItemLuckyDraw.createtemCoat(750, player, 0, true, 0);
                  ItemLuckyDraw.createtemCoat(750, player, 1, true, 0);
                  return;

              case "phuonghoang":
                  player.createPhuongHoang(0, Animal.PHUONG_HOANG, true);
                  player.createPhuongHoang(0, Animal.PHUONG_HOANG_BANG, true);
                  return;

              case "matna":
                  Item newitem = Map.doCreateMatNa(player, 525948, 837, 10, 30);
                  Item newitem2 = Map.doCreateMatNa(player, 525948, 838, 11, 30);
                  

                  int[] valuemn = {5, 5, 10, 30, 50};
                  Item newitem3 = Map.doCreateMatNa(player, -1, ID_MAT_NA[0], 0, valuemn[0]);
                  Item newitem4 = Map.doCreateMatNa(player, -1, ID_MAT_NA[1], 1, valuemn[1]);
                  Item newitem5 = Map.doCreateMatNa(player, -1, ID_MAT_NA[2], 2, valuemn[2]);
                  Item newitem6 = Map.doCreateMatNa(player, -1, ID_MAT_NA[3], 3, valuemn[3]);
                  Item newitem7 = Map.doCreateMatNa(player, -1, ID_MAT_NA[4], 4, valuemn[4]);   
                  player.sendMessage(MessageCreator.createServerAlertMessage("Nháº­n ÄÆ°á»£c táº¥t cáº£ máº·t náº¡ 365 ngÃ y", ""));

                  return;

              case "nguyenlieu":

                        player.doAddGemItem(4, 9999, true); // bao hiem 90
                        player.doAddGemItem(156, 9999, true); // DA MAY MAN 6
                        player.doAddGemItem(250, 9999, true); // DA_TIEN_GIAI
                        player.doAddGemItem(108, 9999, true);
                        player.doAddGemItem(66, 9999, true);
                        player.doAddGemItem(73, 9999, true);
                        player.doAddGemItem(80, 9999, true);
                        player.doAddGemItem(87, 9999, true);
                        player.doAddGemItem(94, 9999, true);
                        player.doAddGemItem(101, 9999, true);
                        player.doAddGemItem(108, 9999, true);
                        player.doAddGemItem(24, 100, true);
                        player.doAddGemItem(25, 100, true);
                        player.doAddGemItem(26, 100, true);
                        player.doAddGemItem(27, 100, true);
                        player.doAddGemItem(60, 100, true);
                        player.doAddGemItem(61, 100, true);
                        player.doAddGemItem(62, 100, true);
                        player.doAddGemItem(63, 100, true);
                        player.doAddGemItem(64, 100, true);
                        player.doAddGemItem(65, 100, true);
                        player.doAddGemItem(122, 9999, true);
                        player.doAddGemItem(115, 9999, true);
                        player.doAddGemItem(129, 9999, true);
                        player.doAddGemItem(136, 9999, true);
                        player.doAddGemItem(148, 9999, true);
                        player.doAddGemItem(142, 9999, true);
                        player.doAddGemItem(154, 9999, true);
                        player.doAddGemItem(228, 9999, true);
                        player.doAddGemItem(234, 9999, true);
                        player.doAddGemItem(240, 9999, true); //NGOC_HUYEN_MINH_6

                        player.doAddGemItem(249, 9999, true); // BOT_XANH_LA
                        player.doAddGemItem(66, 9999, true); // LKD_35
                        player.potions[9] = player.potions[9] + 9999; // NHAN SAM
                        player.calculateAttrib();
                        for (int j = 0; j < 10; j++) {
                            GiftCode.AddItem(player, 720, 0, 0, -1); // HLT
//                        GiftCode.AddItem(player, 714, 0, 0, -1); // THD
                        }
                        player.sendMessage(MessageCreator.createCharGemItem(player));
                        player.sendMessage(MessageCreator.createCharInventoryMessage(player, 0));
                        player.sendMessage(MessageCreator.createServerAlertMessage("ChÃºc má»«ng báº¡n ÄÃ£ nháº­n Full NguyÃªn Liá»u Cáº§n thiáº¿t.", ""));
                        return;
                        
                        case "tuhondan":
                        player.potions[83] = player.potions[83] + 9999; // tuhondan
                        player.sendMessage(MessageCreator.createCharGemItem(player));
                        player.sendMessage(MessageCreator.createCharInventoryMessage(player, 0));
                        player.sendMessage(MessageCreator.createServerAlertMessage("ChÃºc má»«ng báº¡n ÄÃ£ nháº­n x9999 tá»¥ há»n Äan.", ""));
                        return;
                        
               case "tranh": {
                        // Tá»© BÃ¬nh â 5 máº£nh má»i loáº¡i (ID: 159 â 174, 16 ID)
                        int[] tuBinh = {
                            159, 160, 161, 162, 163, 164, 165, 166, 167, 168, 169, 170,
                            171, 172, 173, 174
                        };
                        for (int gemId : tuBinh) {
                            player.doAddGemItem(gemId, 5, true);
                        }

                        // Tá»© Nháº¡c â 10 máº£nh má»i loáº¡i (ID: 179 â 194, 16 ID)
                        int[] tuNhac = {
                            179, 180, 181, 182, 183, 184, 185, 186, 187, 188, 189, 190,
                            191, 192, 193, 194
                        };
                        for (int gemId : tuNhac) {
                            player.doAddGemItem(gemId, 10, true);
                        }

                        // Tá»© KhoÃ¡i â 15 máº£nh má»i loáº¡i (ID: 195 â 210, 16 ID)
                        int[] tuKhoai = {
                            195, 196, 197, 198, 199, 200, 201, 202, 203, 204, 205, 206,
                            207, 208, 209, 210
                        };
                        for (int gemId : tuKhoai) {
                            player.doAddGemItem(gemId, 15, true);
                        }

                        // Tá»© Linh â 20 máº£nh má»i loáº¡i (ID: 211 â 226, 16 ID)
                        int[] tuLinh = {
                            211, 212, 213, 214, 215, 216, 217, 218, 219, 220, 221, 222,
                            223, 224, 225, 226
                        };
                        for (int gemId : tuLinh) {
                            player.doAddGemItem(gemId, 20, true);
                        }

                        player.sendMessage(MessageCreator.createCharGemItem(player));
                        player.sendMessage(MessageCreator.createCharInventoryMessage(player, 0));
                        player.sendMessage(MessageCreator.createServerAlertMessage("Báº¡n ÄÃ£ nháº­n full tranh!", ""));
                        return;
                    }         
              case "sachpet":
                  player.potions[Potion.TYPE_SACH_SKILL_PET] += 50;
                  player.sendMessage(MessageCreator.createCharInventoryMessage(player, 0));
                      return;
              case "pet":
                        int[] petEggIds = {684, 685, 686, 687, 688};
                        for (int id : petEggIds) {
                            for (int i = 0; i < 10; i++) {
                                GiftCode.AddItem(player, id, 0, 0, -1);
                            }
                        }
                        player.sendMessage(MessageCreator.createServerAlertMessage("ChÃºc má»«ng báº¡n ÄÃ£ nháº­n Trá»©ng Pet", ""));
                        return;
              case "sutu":
                        player.createSuTuBang(0, true);
                        player.createSuTu(0, true);
                        player.sendMessage(MessageCreator.createServerAlertMessage("ChÃºc má»«ng báº¡n ÄÃ£ nháº­n ÄÆ°á»£c SÆ° Tá»­.", ""));
                  return;
                        
              case "lansutu":
                  player.createLan(525948, false, 95);
              return;
               
               default:
                   break;
           }
        }

            String sql = "select * from giftcode where giftcode='" + text + "' and type = 0 limit 1";
            pre1 = conn.prepareStatement(sql);
            rs = pre1.executeQuery();

            if (rs.next()) {
                int xu = rs.getInt("xu");
                int luong = rs.getInt("luong");
                int luongkhoa = rs.getInt("luongLock");
                String item = rs.getString("item");
                String code = rs.getString("giftcode");
                int limit = rs.getInt("limit_use");

                if (limit == 0) {
                    player.sendMessage(MessageCreator.createServerAlertMessage("MÃ£ QuÃ  Táº·ng ÄÃ£ háº¿t lÆ°á»£t sá»­ dá»¥ng.", ""));
                    return;
                }

                if (xu != 0) {
                    player.addXu(xu, "nhan tu giftcode" + code);
                }
                if (luong != 0) {
                    player.addLuong(luong);
                }

                if (luongkhoa != 0) {
                    player.addLuongLock(luongkhoa);
                }

                if (item != null && !item.equals("NULL") && !item.isEmpty()) {
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
                            // Danh sÃ¡ch ID danh hiá»u cÃ³ sáºµn
                            int[] availableTitles = {
                                8767, 8768, 8769, 8770, 8771, 8772, 8773, 8774, 8775, 8776, 8777,
                                8778, 8779, 8785, 8786, 8787, 8788, 8900, 8901, 8902, 8903, 8904,
                                8905, 8906, 8907, 8908, 8909, 8910, 8911, 8924, 8925, 8935, 8956, 8957
                            };
                            
                            // Náº¿u ID = 0, chá»n ngáº«u nhiÃªn má»t danh hiá»u
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
                        } else if (tachTime[0].equals("ITEM")) {
                            player.createItemSoLuong(Integer.parseInt(tachTime[1]), Integer.parseInt(tachTime[2]), player);
                        } else if (tachTime[0].equals("MATNA")) {
                            int[] valuemn = {5, 5, 10, 30, 50};
                            int randomMN = Map.r.nextInt(valuemn.length);
                            if (Integer.parseInt(tachTime[1]) > -1) {
                                randomMN = Integer.parseInt(tachTime[1]);
                            }
                            Item newitem = Map.doCreateMatNa(player, Integer.parseInt(tachTime[3]), ID_MAT_NA[randomMN], randomMN, valuemn[randomMN]);
                            player.sendMessage(MessageCreator.createServerAlertMessage("Nháº­n " + newitem.getName() + " 7 ngÃ y", ""));
                        // ... existing code ...
                    } else if (tachTime[0].equals("MATNA2")) {
                        // Táº¡o máº·t náº¡ vá»i option vÃ  value random
                        int[] optionValues = {5, 5, 10, 30, 50}; // CÃ¡c giÃ¡ trá» value cÃ³ thá»
                        int randomIndex = Map.r.nextInt(optionValues.length);
                        int matnaID = Integer.parseInt(tachTime[1]); // ID máº·t náº¡
                        int option = randomIndex; // Option máº·t náº¡ (0-4)
                        int value = optionValues[randomIndex];  // GiÃ¡ trá» option tÆ°Æ¡ng á»©ng
                        Item newitem = Map.doCreateMatNa2(player, Integer.parseInt(tachTime[3]), matnaID, option, value);
                        player.sendMessage(MessageCreator.createServerAlertMessage("Nháº­n " + newitem.getName() + (Integer.parseInt(tachTime[3]) > 0 ? " " + Integer.parseInt(tachTime[3])/1440 + " ngÃ y" : " vÄ©nh viá»n"), ""));    
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

                            ///  todo REGEX quÃ  tÃ­ch lÅ©y 
                        } else if (tachTime[0].equals("LANT")) {
                            player.createLan(Integer.parseInt(tachTime[1]), false, 95);

                        } else if (tachTime[0].equals("DKT")) {
                            player.createHeo(Integer.parseInt(tachTime[1]), true);
                            
                            
                        } else if (tachTime[0].equals("PHDS")) {
                            
                            Animal anima = null;
                            anima = new Animal();
                            anima.name = "PhÆ°á»£ng hoÃ ng Äa sáº¯c";
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
                        }
                    }
                }

                player.sendMessage(MessageCreator.createCharInventoryMessage(player, 0));
                player.sendMessage(MessageCreator.createServerAlertMessage("ChÃºc má»«ng báº¡n ÄÃ£ nháº­n ÄÆ°á»£c pháº§n quÃ  tá»«: " + code + ".", ""));
                Database.instance.saveOrtherLog("", player.charname, "CODE: " + code, "putGiftCode");
                sql = "insert into giftcode_log set player = '" + player.charname + "', giftcode = '" + text + "', item = '" + item + "', xu = " + xu + ", luong = " + luong + ", luongK = " + luongkhoa + ";";
                pre1.execute(sql);

                if (limit >= 1) {
                    String sql_updatelimit = "UPDATE giftcode SET limit_use = limit_use - '1' WHERE giftcode = '" + code + "'";
                    pre1.execute(sql_updatelimit);
                }
                System.out.println(limit);
            } else {
                player.sendMessage(MessageCreator.createServerAlertMessage("MÃ£ quÃ  táº·ng khÃ´ng tá»n táº¡i.", ""));
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
                respond = "1|0|0|" + 10000 + "|DB:686:1:-1|1|má»c náº¡p 10K lÆ°á»£ng";
                player.tichluy = 10000;
                stm.execute("update `tob_char` set tichluy = '10000' where charname = '" + player.charname + "'");
                stm.close();
                Database.connGiftCode.free(conn);
                return respond;
            }
            if (daNap >= 20000 && player.tichluy < 20000) {
                Connection conn = Database.connGiftCode.getConnection();
                Statement stm = conn.createStatement();
                respond = "1|0|0|" + 20000 + "|DB:687:1:0,PP:0:1:43200|1|má»c náº¡p 20K lÆ°á»£ng";
                player.tichluy = 20000;
                stm.execute("update `tob_char` set tichluy = '20000' where charname = '" + player.charname + "'");
                stm.close();
                Database.connGiftCode.free(conn);
                return respond;
            }
            if (daNap >= 40000 && player.tichluy < 40000) {
                Connection conn = Database.connGiftCode.getConnection();
                Statement stm = conn.createStatement();
                respond = "1|0|0|" + 40000 + "|PT:74:1:0,PP:0:1:129600|1|má»c náº¡p 40k lÆ°á»£ng";
                player.tichluy = 40000;
                stm.execute("update `tob_char` set tichluy = '40000' where charname = '" + player.charname + "'");
                stm.close();
                Database.connGiftCode.free(conn);
                return respond;
            }
            if (daNap >= 80000 && player.tichluy < 80000) {
                Connection conn = Database.connGiftCode.getConnection();
                Statement stm = conn.createStatement();
                respond = "1|0|0|" + 80000 + "|DB:684:1:-1,PP:0:1:-1,PP:0:1:0,HLT:1:1|1|má»c náº¡p 80k lÆ°á»£ng";
                player.tichluy = 80000;
                stm.execute("update `tob_char` set tichluy = '80000' where charname = '" + player.charname + "'");
                stm.close();
                Database.connGiftCode.free(conn);
                return respond;
            }

            if (daNap >= 120000 && player.tichluy < 120000) {
                Connection conn = Database.connGiftCode.getConnection();
                Statement stm = conn.createStatement();
                respond = "1|0|0|" + 120000 + "|DB:684:1:-1,AOSHOP:723:1:0,AOSHOP:724:1:0,GEM:249:300:-1,PET:5:1:0|1|má»c náº¡p 120K lÆ°á»£ng";
                player.tichluy = 120000;
                stm.execute("update `tob_char` set tichluy = '120000' where charname = '" + player.charname + "'");
                stm.close();
                Database.connGiftCode.free(conn);
                return respond;

            }
            if (daNap >= 200000 && player.tichluy < 200000) {
                Connection conn = Database.connGiftCode.getConnection();
                Statement stm = conn.createStatement();
                respond = "1|0|0|" + 200000 + "|DB:688:1:-1,HLT:0:1,CHOI:617:1:43200:-1,BT:0:1:43200|1|má»c náº¡p 200k LÆ°á»£ng";
                player.tichluy = 200000;
                stm.execute("update `tob_char` set tichluy = '200000' where charname = '" + player.charname + "'");
                stm.close();
                Database.connGiftCode.free(conn);
                return respond;

            }
            
            if (daNap >= 400000 && player.tichluy < 400000) {
                Connection conn = Database.connGiftCode.getConnection();
                Statement stm = conn.createStatement();
                respond = "1|0|0|400000|DB:688:1:-1,HLT:0:1,BT:0:1:0,GEM:249:1000:-1,AOSHOP:725:1:15,AOSHOP:726:1:15|1|má»c náº¡p 400k LÆ°á»£ng";
                player.tichluy = 400000;
                stm.execute("update `tob_char` set tichluy = '400000' where charname = '" + player.charname + "'");
                stm.close();
                Database.connGiftCode.free(conn);
                return respond;

            }
            
            if (daNap >= 600000 && player.tichluy < 600000) {
                Connection conn = Database.connGiftCode.getConnection();
                Statement stm = conn.createStatement();

                respond = "1|0|0|" + 600000 + "|DB:685:1:-1,DB:688:1:-1,PPV:750:1:21600:1,GEM:249:500:-1,HLT:0:1,TRANGPHUC:725:1:30,TRANGPHUC:726:1:30|1|má»c náº¡p 600k LÆ°á»£ng";
                player.tichluy = 600000;
                stm.execute("update `tob_char` set tichluy = '600000' where charname = '" + player.charname + "'");
                stm.close();
                Database.connGiftCode.free(conn);
                return respond;
            }


            if (daNap >= 1000000 && player.tichluy < 1000000) {
                Connection conn = Database.connGiftCode.getConnection();
                Statement stm = conn.createStatement();
                respond = "1|0|0|" + 1000000 + "|DB:685:1:-1,DB:688:1:-1,PPV:742:1:0:1,PPV:742:1:0:0,MATNA:3:1:-1,DH:8770:1:0|1|má»c náº¡p 1 Triá»u LÆ°á»£ng.";
                player.tichluy = 1000000;
                stm.execute("update `tob_char` set tichluy = '1000000' where charname = '" + player.charname + "'");
                stm.close();
                Database.connGiftCode.free(conn);
                return respond;
            }
            if (daNap >= 1200000 && player.tichluy < 1200000) {
                Connection conn = Database.connGiftCode.getConnection();
                Statement stm = conn.createStatement();
                respond = "1|0|0|" + 1200000 + "|PT:70:1:0,CHOI:617:1:0,MATNA:2:1:-1,TRANGPHUC:725:1:0,TRANGPHUC:726:1:0|1|má»c náº¡p 1 Triá»u 2 LÆ°á»£ng.";
                player.tichluy = 1200000;
                stm.execute("update `tob_char` set tichluy = '1200000' where charname = '" + player.charname + "'");
                stm.close();
                Database.connGiftCode.free(conn);
                return respond;
            }
            if (daNap >= 1400000 && player.tichluy < 1400000) {
                Connection conn = Database.connGiftCode.getConnection();
                Statement stm = conn.createStatement();
                respond = "1|0|0|1400000|PT:115:1:0,CHOI:729:1:0:0,DB:899:1:0|1|má»c náº¡p 1 Triá»u 4 LÆ°á»£ng.";
                player.tichluy = 1400000;
                stm.execute("update `tob_char` set tichluy = '1400000' where charname = '" + player.charname + "'");
                stm.close();
                Database.connGiftCode.free(conn);
                return respond;
            } // tá»« ÄÃ¢y trá» xuá»ng lÃ  lá»i Ã  ? á»«m

            if (daNap >= 1600000 && player.tichluy < 1600000) {
                Connection conn = Database.connGiftCode.getConnection();
                Statement stm = conn.createStatement();
                respond = "1|0|0|1600000|PT:72:1:-1,DB:898:1:0,HOVELAN:882:1:43200,CHOI:618:1:0:0|1|má»c náº¡p 1 trieu 6 luong.";
                player.tichluy = 1600000;
                stm.execute("update `tob_char` set tichluy = '1600000' where charname = '" + player.charname + "'");
                stm.close();
                Database.connGiftCode.free(conn);
                return respond;
            }
            if (daNap >= 1800000 && player.tichluy < 1800000) {

                Connection conn = Database.connGiftCode.getConnection();
                Statement stm = conn.createStatement();

                respond = "1|0|0|1800000|MATNA:4:1:-1,VKMAX:0:1:0,VKTBMAX:0:1:0,HOVELAN:882:1:86400,PPV:750:1:0:0,DB:901:1:0|1|má»c náº¡p 1 Triá»u 8 LÆ°á»£ng.";
                player.tichluy = 1800000;
                stm.execute("update `tob_char` set tichluy = '1800000' where charname = '" + player.charname + "'");
                stm.close();
                Database.connGiftCode.free(conn);
                return respond;
            }

            if (daNap >= 2000000 && player.tichluy < 2000000) {
                Connection conn = Database.connGiftCode.getConnection();
                Statement stm = conn.createStatement();
                respond = "1|0|0|2000000|MNT:14400:837:10:5,MNC:14400:837:10:5,DB:638:1:-1,DB:902:1:0,TRANGPHUC:839:1:0,VKTBMAX:0:1:0|1|má»c náº¡p 2tr LÆ°á»£ng.";
                player.tichluy = 2000000;
                stm.execute("update `tob_char` set tichluy = '2000000' where charname = '" + player.charname + "'");
                stm.close();
                Database.connGiftCode.free(conn);
                return respond;
            }

            if (daNap >= 2200000 && player.tichluy < 2200000) {
                Connection conn = Database.connGiftCode.getConnection();
                Statement stm = conn.createStatement();
                respond = "1|0|0|2200000|MNT:0:837:10:5,MNC:0:837:10:5,GEM:249:5000:-1,HOVELAN:882:1:108000,NLT:0:1:0|1|má»c náº¡p 2tr2 LÆ°á»£ng.";
                player.tichluy = 2200000;
                stm.execute("update `tob_char` set tichluy = '2200000' where charname = '" + player.charname + "'");
                stm.close();
                Database.connGiftCode.free(conn);
                return respond;
            }
            

            if (daNap >= 2600000 && player.tichluy < 2600000) {
                Connection conn = Database.connGiftCode.getConnection();
                Statement stm = conn.createStatement();
                respond = "1|0|0|2600000|PT:71:1:-1,PT:86:1:-1,GEM:249:5000:-1,DB:685:1:-1,DB:688:1:-1,HOVELAN:882:1:129600,|1|má»c náº¡p 2tr6 LÆ°á»£ng.";
                player.tichluy = 2600000;
                stm.execute("update `tob_char` set tichluy = '2600000' where charname = '" + player.charname + "'");
                stm.close();
                Database.connGiftCode.free(conn);
                return respond;
            }
//            if (daNap >= 2800000 && player.tichluy < 2800000) {
//                Connection conn = Database.connGiftCode.getConnection();
//                Statement stm = conn.createStatement();
//                respond = "1|0|0|2800000|SUTU:-1,DB:581:1:-1|1|má»c náº¡p 2tr8 LÆ°á»£ng.";
//                player.tichluy = 2800000;
//                stm.execute("update `tob_char` set tichluy = '2800000' where charname = '" + player.charname + "'");
//                stm.close();
//                Database.connGiftCode.free(conn);
//                return respond;
//            }
            
            if (daNap >= 3000000 && player.tichluy < 3000000) {
                Connection conn = Database.connGiftCode.getConnection();
                Statement stm = conn.createStatement();
                respond = "1|0|0|3000000|VKTBLV:80:5:0,DB:577:1:-1,DB:578:1:-1|1|má»c náº¡p 3tr LÆ°á»£ng.";
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
                String[] name = {"KhÃ¡ng hoáº£", "KhÃ¡ng thuá»·", "KhÃ¡ng má»c", "KhÃ¡ng kim", "KhÃ¡ng thá»", "GiÃ¡ lÆ°á»£ng", "Sá» lÆ°á»£ng"};
                if (TeamServer.isServerLienDau()) {
                    name = new String[]{"KhÃ¡ng hoáº£", "KhÃ¡ng thuá»·", "KhÃ¡ng má»c", "KhÃ¡ng kim", "KhÃ¡ng thá»"};
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
                String[] data = {"750, 'Phi Phong Huyá»n VÅ©', 19, 15, 0, 0, 1, 480, 0, 170, 0, 0, 0, 0, 0, 0, 0, 0, 0, -1, 626, 0, -1, 10080, -1, '', 8806,0", "751, 'PhÃ¹ phu thÃª', 28, 0, -1, 0, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, -1, 627, 0, -1, 0, -1, '', -1,0", "752, 'RÆ°Æ¡ng nguyÃªn liá»u cao cáº¥p', 28, 0, -1, 0, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, -1, 596, 0, -1, 0, -1, '', -1,0", "753, 'RÆ°Æ¡ng nguyÃªn liá»u sÆ¡ cáº¥p', 28, 0, -1, 0, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, -1, 596, 0, -1, 0, -1, '', -1,0", "754, 'Ão dÃ i nam \nTÄng 100% bÄng ,sÃ©t ,Äá»c ,lá»­a lan', 0, 0, 0, 1, 1, 480, 10, 10, 5000, 5000, 0, 0, 0, 0, 0, 1, 0, -1, 630, 0, -1, 10080, -1, '', -1, 0", "755, 'Ão dÃ i ná»¯ \nTÄng 100% bÄng ,sÃ©t ,Äá»c ,lá»­a lan', 0, 0, 0, 2, 1, 480, 10, 10, 5000, 5000, 0, 0, 0, 0, 0, 1, 0, -1, 631, 0, -1, 10080, -1, '', -1, 0"};
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
                        if (UtilKPAH.getHour() == 5 || UtilKPAH.getHour() == 6) {
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
