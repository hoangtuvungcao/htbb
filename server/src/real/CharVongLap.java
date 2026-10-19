package real;


import io.ThreadPool;
import java.util.Date;
import data.Text;
import server.TeamServer;
import data.Database;
import real.cmd.LoginHandler;
import io.Message;
import java.io.IOException;

import data.CharInfo;
import data.ItemLuckyDraw;
import java.util.Calendar;
import java.util.Hashtable;
import data.Animal;
import java.util.Vector;

/**
 * Than cac method nang cua Char: vong cap nhat chinh (update), thong tin hien
 * thi, gui vat pham, doc thong tin tu DB, trang bi thu cuoi, tui do, xo so,
 * qua Noel.
 *
 * Tach tu real/Char.java. Cac method VAN CON trong Char, giu NGUYEN chu ky cu,
 * chi than chuyen sang day. Tham so "Char self" thay cho "this".
 *
 * Char.update co LOP AN DANH (new Runnable(){...}) nhung da kiem: lop do chi
 * dung bien cuc bo "pp", KHONG dung "this" cua Char - nen chuyen sang ngu canh
 * static van dung. (Lop an danh chi thanh van de khi no bat "this" ngam.)
 */
class CharVongLap {

    static void update(Char self) {
        if (self.isBot != -1) {
            self.hp = 1;
            return;
        }
        // "Luyen do tu dong" (dap do tu dong): MapEpNgoc.doAutoImbue chi BAT co
        // startAutoImbue, con moi lan dap that su phai do vong lap map goi
        // Char.doAutoImbue(). Truoc day cho goi do CHI ton tai trong OfflineMap
        // (map id -1 - map du phong cho char co mapID khong ton tai), nen voi nguoi
        // choi dang dung o map binh thuong thi co duoc bat len roi khong ai chay:
        // client ket vinh vien o hop thoai "Xin cho". Dat o day vi ca ba vong lap map
        // (update/updateSand/updateWind, moi 300ms) deu goi Char.update() cho tung
        // nguoi choi - cung nhip ma OfflineMap dung. OfflineMap goi thang
        // p.doAutoImbue() va KHONG goi p.update(), nen khong bi dap doi.
        if (self.startAutoImbue) {
            try {
                self.doAutoImbue();
            } catch (final Exception ex) {
                // Khong de loi thoat ra ngoai: vong lap nguoi choi o MapVongLap bat
                // Exception bang "break", mot loi o day se lam dung cap nhat CA map.
                self.sendStopAutoImbue("Lỗi khi luyện tự động, đã dừng");
                self.idGemLkd = -1;
                ex.printStackTrace();
            }
        }
        if (self.currentDanhHieu == null || (self.currentDanhHieu != null && self.currentDanhHieu.isExpire())) {
            self.doOffDanhHieu();
        }
        if (self.killer > 32000) {
            self.killer = 32000;
        }
        if (self.monsTerThuThap != null && self.monsTerThuThap.isFinishThuThap()) {
            self.monsTerThuThap.doCancelThuthap();
            self.monsTerThuThap.actorDie();
            self.doAddGifThungGoNuiChauBau(self.lvDetail.lv);
            self.monsTerThuThap = null;
        }
        if (self.map != null && self.map.isMapTrain() && !self.map.canMove(self.x, self.y)) {
            final Hashtable<Short, Monster> mons = self.map.getAllMons(self.inCountry, self.region);
            if (mons.size() >= 0) {
                final short id = (short) Map.r.nextInt(mons.size() - mons.size() / 4);
                final Monster ms = mons.get(id);
                self.x = ms.default_x;
                self.y = ms.default_y;
                final Message m = new Message(4);
                self.writeActorPos(m, self);
                self.sendMessage(m);
            }
        }
        CharInfo ccb = null;
        if (self.map != null && (ccb = MapChauBau.all_char_nui_kho_bau.get(self.charname)) != null && ccb.getTimeNuiChauBau() <= 0L && (self.map.isMapChanNui() || self.map.isMapNuiChauBau())) {
            self.map.doReturnVillage(self);
        }
        // "Auto Ve Map Up" (them 22/09/2026) - hai buoc doc lap, dat trong Char.actorDie()
        // khi chet tai bai luyen cong (xem chu thich o do): buoc 1 (3s) dua ve lang VA hoi
        // sinh luon (doReturnVillage khong tu hoi HP/MP, phai lam o day giong het cach
        // nhanh timeHoiSinh phia duoi lam); buoc 2 (5s) bay lai dung vi tri up, tru phi
        // theo hang Vip (mien phi rieng vip==3, xem BaiLuyenTap.PHI_AUTO_VE_MAP_UP).
        if (self.timeVeLangSauChet > 0L && System.currentTimeMillis() >= self.timeVeLangSauChet) {
            self.timeVeLangSauChet = 0L;
            if (self.map != null) {
                self.map.doReturnVillage(self);
                self.hp = self.maxhp;
                self.mp = self.maxmp;
                self.desTroy();
                try {
                    self.sendMessage(MessageCreator.createMainCharInfoMessage(self));
                    self.sendToNearPlayer(MessageCreator.createNew_HMP_Message(self, 0));
                } catch (final Exception exVeLang) {
                }
            }
        }
        if (self.timeVeBaiSauChet > 0L && System.currentTimeMillis() >= self.timeVeBaiSauChet) {
            self.timeVeBaiSauChet = 0L;
            if (self.viTriUpMapId > 0) {
                final boolean mienPhi = self.vip >= 3;
                if (mienPhi || self.truXuNeuDu(BaiLuyenTap.giaDichChuyen(self, BaiLuyenTap.PHI_AUTO_VE_MAP_UP), false, "auto ve map up")) {
                    self.map.move2Map(self, self.viTriUpTileX, self.viTriUpTileY, self.viTriUpMapId, self.inCountry);
                    self.sendMessage(MessageCreator.createServerAlertMessage(
                            "Đã tự động đưa bạn về vị trí luyện tập trước đó.", ""));
                } else {
                    self.sendMessage(MessageCreator.createServerAlertMessage(
                            "Không đủ " + BaiLuyenTap.giaDichChuyen(self, BaiLuyenTap.PHI_AUTO_VE_MAP_UP) + " xu để tự động bay lại vị trí luyện tập.", ""));
                }
                self.viTriUpMapId = -1;
            }
        }
        HoiSinhGame.nhip(self); // guc / hoi sinh: tu dong, cho dong doi, het Met moi
        ++self.countSecond;
        if (self.map != null && (self.map.isMapTrain() || self.map.isMapBoss() || self.map.isMapChienTruongMoba()) && self.hp <= 0 && self.timeHoiSinh > 0L && self.countSecond % 5 == 0) {
            final int second = (int) ((self.timeHoiSinh - System.currentTimeMillis()) / 1000L);
            if (second > 0) {
                if (!self.map.isMapChienTruongMoba()) {
                    self.sendMessage(MessageCreator.createMsgChat(self.id, "Hồi sinh sau " + second));
                }
            } else {
                self.timeHoiSinh = 0L;
                self.hp = self.maxhp;
                self.mp = self.maxmp;
                self.desTroy();
                try {
                    self.sendMessage(MessageCreator.createMainCharInfoMessage(self));
                    self.sendToNearPlayer(MessageCreator.createNew_HMP_Message(self, 0));
                    self.sendMessage(MessageCreator.createCharInventoryMessage(self, 0));
                    self.sendPetEff();
                    if (self.map.isMapChienTruongMoba()) {
                        final CharChienTruong c = MapChienTruongMoba.getCharChienTruong(self.charname);
                        self.x = MapChienTruongMoba.POS_REVIVAL[c.team][0] * 16;
                        self.y = MapChienTruongMoba.POS_REVIVAL[c.team][1] * 16;
                        self.map.move2Map(self, self.x / 16, self.y / 16, 40, self.inCountry);
                        self.doSendProperties();
                        self.sendInfoChienTruong(Char.ID_ALL_KHU_CHIEN_TRUONG_MOBA, 0);
                        self.sendInfoChienTruong(Char.ID_NOI_LUC, 0);
                        self.map.getRegionMoba(self.region).sendInfoPlayer(self);
                        return;
                    }
                    final Message i = new Message(4);
                    self.writeActorPos(i, self);
                    self.sendMessage(i);
                } catch (final Exception ex) {
                }
            }
        }
        if (self.countSecond >= 300) {
            try {
                self.checkExpireMDAmor();
                self.checkExpirePet();
                self.checkExpireItemInentry();
                self.checkExpireAoHoaHoaKyLan();
            } catch (final Exception ex2) {
            }
            ++self.minuteOnline;
            if (self.minuteOnline > 120) {
                self.minuteOnline = 0;
                boolean conSuatFanCung;
                synchronized (Char.KHOA_FAN_CUNG) {
                    conSuatFanCung = Char.TOTAL_FANCUNG > 0;
                    if (conSuatFanCung) {
                        --Char.TOTAL_FANCUNG;
                    }
                }
                if (conSuatFanCung) {
                    // TODO thêm danh hiệu fan cứng ở đây
                    final boolean isNew = self.addDanhHieu(8788, 1440L);
                    if (isNew) {
                        self.sendMessage(MessageCreator.createMsgChat(self.id, "Nhận được danh hiệu Fan cứng"));
                    } else {
                        synchronized (Char.KHOA_FAN_CUNG) {
                            ++Char.TOTAL_FANCUNG;
                        }
                    }
                }
            }
            self.countSecond = 0;
        }
        boolean DH_Daigia = true;
//        if (self.diemNapVip >= 200000 && DH_Daigia) {
//            final boolean isNew = self.addDanhHieu_VIP(8770, 43200L);
//            if (isNew) {
//                self.sendMessage(MessageCreator.createMsgChat(self.id, "Nhận được danh hiệu Đại gia."));
//            }
//        }
        if (self.charCopy != null) {
            if (self.hp <= 0 || (!self.charCopy.map.equals(self.map) && self.charCopy.isFollow() && !self.isDoChangeMap)) {
                if (Char.isSuKienMiniChucNu()) {
                    self.sendMessage(MessageCreator.createMsgChat(self.id, "Nhiệm vụ tìm Illumise thất bại"));
                } else {
                    self.sendMessage(MessageCreator.createMsgChat(self.id, "Nhiệm vụ tìm trẻ lạc thất bại"));
                }
                self.charCopyDissapear();
            } else {
                self.charCopy.update();
            }
        }
        self.updateLuLan();
        if (self.charthanthu != null) {
            if (self.myCountry == self.inCountry && !self.isAdmin && System.currentTimeMillis() - self.charthanthu.timeAttack >= 0L) {
                self.charthanthu.timeAttack = System.currentTimeMillis() + (Map.r.nextInt(3) + 2) * 1000;
                final Vector<Char> players = self.getAllPlayerMap();
                try {
                    final Vector<LiveActor> target = new Vector<LiveActor>();
                    for (int j = players.size() - 1; j >= 0; --j) {
                        final Char p = players.get(j);
                        if (Map.inRangeActor(p, self, 120) && p.id != self.id && !p.isAdmin && p.isBot == -1 && p.hp > 0 && (p.isKiller || (p.myCountry != self.myCountry && p.inCountry == self.myCountry)) && !Map.isMapThanh(self.map)) {
                            target.add(p);
                            final Char char1 = p;
                            char1.hp -= self.getDamtThanThuAuto(p);
                            p.sendMessage(MessageCreator.createNew_HMP_Message(p, 0));
                            p.sendToNearPlayer(MessageCreator.createNew_HMP_Message(p, 0));
                            if (p.hp <= 0) {
                                p.actorDie();
                            }
                        }
                    }
                    if (target.size() > 0) {
              
                        self.charthanthu.doAttackAuto(target);
                    }
                } catch (final Exception ex3) {
                }
            }
            self.charthanthu.update();
        }
        if (self.charHire != null) {
            if (self.charHire.myCountry != self.myCountry) {
                self.charHire.myCountry = self.myCountry;
            }
            self.charHire.update();
        }
        self.updateEffectBuff();
        if (self.monster == null) {
            self.monster = MonsterVantieu.HAS_MONSTER_VANTIEU.get(self.charDBID);
        }
        if (self.timeUp5Attribute > 0L && System.currentTimeMillis() - self.timeUp5Attribute > 0L) {
            self.calculateAttrib();
            self.timeUp5Attribute = 0L;
            try {
                self.sendMessage(MessageCreator.createMainCharInfoMessage(self));
            } catch (final Exception ex4) {
            }
        }
        if (self.timeExistCharHire > 0L && System.currentTimeMillis() - self.timeExistCharHire > 0L && self.charHire != null) {
            self.charhireDissapear();
            self.timeExistCharHire = 0L;
            self.lvlinh = 0;
            self.lvlinhthue = 0;
            self.classlinh = 0;
            self.sendMessage(MessageCreator.createMsgChat(self.id, "Pokémon hỗ trợ đã hết hạn"));
            Database.instance.saveOrtherLog("", self.charname, "linh thue het han:c= " + self.classlinh + ",g=" + self.genderlinh + ",lvl=" + self.lvlinhthue + ",lvcl=" + self.lvlinh, "lhethan");
        }
        if (self.isNguoiTuyet() && System.currentTimeMillis() - self.timeNguoiTuyet >= 0L) {
            if (self.idNgtuyet == 3) {
                try {
                    self.sendMessage(MessageCreator.createMainCharInfoMessage(self));
                } catch (final IOException ex5) {
                }
            }
            self.idNgtuyet = -1;
            self.map.charNguoiTuyetDissapear(self);
        }
        // hoa hinh
        if (self.currentDefReduc > 0) {
            long currentTime = System.currentTimeMillis();
            // Kiểm tra nếu đã quá 10s kể từ lần giảm đầu tiên
            if (self.timeFirstDefReduc > 0 && currentTime - self.timeFirstDefReduc >= 10000) {
                // Reset toàn bộ
                self.currentDefReduc = 0;
                self.timeFirstDefReduc = 0L;
                self.timeResetDefReduc = 0L;
                
                // Thông báo cho người chơi
                try {
                    self.sendMessage(MessageCreator.createServerAlertAutoOffMessage("Đã hết hiệu ứng giảm giáp!"));
                    // Cập nhật thông tin cho người chơi và người chơi xung quanh
                    Message msg = MessageCreator.createCharInfo(self);
                    self.sendMessage(msg);
                    self.sendToNearPlayer(msg);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        if (System.currentTimeMillis() - self.timeRegentHpMp >= 5000L && self.hp > 0) {
            if (self.petUsing != null && self.petUsing.isPetKhi() && Map.r.nextInt(100) < 50) {
                self.setNguoiTuyet(5);
            }
            if (self.haveRegentHP() > 0 && self.hp < self.maxhp && self.hp > 0 && !self.isVetThuongSau() && !self.checkChoang()) {
                final int a = self.haveRegentHP();
                self.hp += a;
                if (self.calculatorHPMP()) {
                    try {
                        self.sendMessage(MessageCreator.createMainCharInfoMessage(self));
                    } catch (final Exception ex6) {
                    }
                }
                try {
                    self.sendMessage(MessageCreator.createNew_HMP_Message(self, a));
                    self.sendToNearPlayer(MessageCreator.createNew_HMP_Message(self, a));
                } catch (final IOException ex7) {
                }
            }
            if (self.haveRegentMP() > 0 && self.mp < self.maxmp && self.hp > 0 && !self.isYemBua()) {
                final int a = self.haveRegentMP();
                self.mp += a;
                self.calculatorHPMP();
                try {
                    self.sendMessage(MessageCreator.createMainCharInfoMessage(self));
                } catch (final Exception ex8) {
                }
                MessageCreator.createMsgUseHpMP(self, a, 4);
            }
            self.timeRegentHpMp = System.currentTimeMillis();
            final Vector<EffectBuff> rm = new Vector<EffectBuff>();
            for (int k = 0; k < self.ALL_BUFF_INMAP.size(); ++k) {
                final EffectBuff ef = self.ALL_BUFF_INMAP.get(k);
                if (ef.timeOut()) {
                    rm.add(ef);
                    if (ef.idEff == EffectBuff.LUA_NHO || ef.idEff == EffectBuff.LUA_VUA || ef.idEff == EffectBuff.LUA_TO) {
                        self.idCuiDot = -1;
                    }
                }
            }
            for (int k = 0; k < rm.size(); ++k) {
                self.ALL_BUFF_INMAP.remove(rm.get(k));
            }
        }
        if (self.monster != null) {
            if (self.monster.map.mapId == self.map.mapId) {
                self.monster.target = self;
            }
            try {
                if (self.monster.isFinish()) {
                    final Message l = new Message(90);
                    l.dos.writeShort(self.monster.id);
                    l.dos.writeByte(self.monster.cat);
                    if (self.monster.map != null) {
                        self.monster.map.sendAllPlayer(l, self.monster.inCountry);
                    }
                    self.monster = null;
                    self.typeTieu = -1;
                }
            } catch (final Exception e) {
                e.printStackTrace();
            }
        }
        if (self.timeWaitComeHome > 0L && System.currentTimeMillis() - self.timeWaitComeHome > 30000L && self.mapID == Map.idMapTown) {
            try {
                if (self.hp <= 0) {
                    final int[][] mapstart = {{70, 1701}, {0, 301}, {80, 1901}};
                    self.hp = 1;
                    self.desTroy();
                    try {
                        self.sendMessage(MessageCreator.createMainCharInfoMessage(self));
                    } catch (final IOException ex9) {
                    }
                    final int idVilage = mapstart[self.inCountry][Map.r.nextInt(mapstart[self.inCountry].length)];
                    self.map.move2Map(self, 16 + Database.r.nextInt() % 5, 24 + Database.r.nextInt(20), idVilage, self.inCountry);
                    self.resetTimeLastuseSkill();
                    if (self.petUsing != null && !self.petUsing.isPetTool() && self.petUsing.getIdEffPet() > -1 && self.addEffBuff(self.petUsing.getIdEffPet(), System.currentTimeMillis() + 320000000L, EffectBuff.BY_ACTOR, 0) != null) {
                        self.sendEffToChar(self);
                    }
                    self.timeWaitComeHome = 0L;
                } else {
                    self.timeWaitComeHome = 0L;
                }
            } catch (final Exception ex10) {
            }
        }
        if (self.minuteSocola > 0 && System.currentTimeMillis() - self.timeEatSocola >= 60000L) {
            --self.minuteSocola;
            self.timeEatSocola = System.currentTimeMillis();
            try {
                if (self.minuteSocola <= 0) {
                    self.sendMessage(MessageCreator.createServerAlertAutoOffMessage("Thời gian sử dụng hạt mầm của bạn đã hết"));
                    Database.instance.saveOrtherLog("", self.charname, "loc may man het gio", "ohoatuyet");
                }
            } catch (final IOException ex11) {
            }
        }
        if (self.lasTimeMove > 0L && System.currentTimeMillis() - self.lasTimeMove > 0L) {
            self.lasTimeMove = System.currentTimeMillis() + 3000L;
            self.sendInfoMove2Near();
        }
        if (self.animalRide != null) {
            final byte timeout = self.animalRide.timeOutFood();
            if (timeout > -1) {
                try {
                    self.sendMessage(MessageCreator.createServerAlertAutoOffMessage(String.valueOf(LoginHandler.PORTION_NAME[timeout + 112]) + " đã hết tác dụng."));
                    Database.instance.saveOrtherLog("", self.charname, LoginHandler.PORTION_NAME[timeout + 112], "aexpire");
                    self.calculateAttrib();
                    self.sendMessage(MessageCreator.createMainCharInfoMessage(self));
                    self.sendMessage(MessageCreator.createCharWearingMessage(self, self));
                } catch (final Exception e2) {
                    e2.printStackTrace();
                }
            }
        }
        try {
            if (self.millisOnlineStart == 0L) {
                self.millisOnlineStart = System.currentTimeMillis();
            }
            final Calendar calGame = Calendar.getInstance();
            final Calendar calNow = Calendar.getInstance();
            calGame.setTime(new Date(self.millisOnlineStart));
            if (!self.dateStartOnline.trim().equals("0") && !self.dateStartOnline.trim().equals("") && self.calCheck == null) {
                try {
                    final int y = Integer.parseInt(self.dateStartOnline.substring(0, 2)) + 2000;
                    final int m2 = Integer.parseInt(self.dateStartOnline.substring(2, 4));
                    final int d = Integer.parseInt(self.dateStartOnline.substring(4, 6));
                    (self.calCheck = Calendar.getInstance()).set(y, m2, d);
                } catch (final Exception ex12) {
                }
            }
            if (calGame.get(5) != calNow.get(5)) {
                self.millisOnlineStart = System.currentTimeMillis();
                self.rcvGifByHour = 1;
                self.isOnlineToDay = false;
                ++self.totalDayInMonth;
            }
            if (calGame.get(2) != calNow.get(2)) {
                self.totalDayInMonth = 1;
                self.rcvGifByMonth = 1;
            }
            self.millisOnline = System.currentTimeMillis() - self.millisOnlineStart;
            self.bonusTime();
            self.bonusDay();
            self.bonusMonth();
            self.bonusLevel();
        } catch (final Exception e) {
            e.printStackTrace();
        }
        self.beAttack = false;
        try {
            if ((Map.checkRcvXP || (Char.isAcountTest(self) && Char.onOffTime180 == 1)) && Map.openLog && System.currentTimeMillis() - self.startPlay >= 60000L) {
                ++self.timeXP;
                self.startPlay = System.currentTimeMillis();
                if (self.timeXP == 179) {
                    self.sendMessage(MessageCreator.createServerAlertMessage("Bạn đã chơi được " + self.timeXP + " phút. Bạn sẽ không thể tiếp tục chơi sau 1 phút nữa.", ""));
                } else if (self.timeXP == 180) {
                    self.sendMessage(MessageCreator.createServerAlertMessage("Bạn đã chơi được " + self.timeXP + " phút. Bạn không thể tiếp tục chơi tiếp.", ""));
                    CharManager.instance.kickPlayer(self, "char 4");
                }
            }
            if (self.timeCrazy > 0L && System.currentTimeMillis() - self.timeCrazy > ((self.isCrazy == 0) ? 60000 : 30000)) {
                self.timeCrazy = 0L;
                self.isCrazy(self.isCrazy);
                self.sendMessage(MessageCreator.createMainCharInfoMessage(self));
                self.sendToNearPlayer(MessageCreator.createCharInfo(self));
                MessageCreator.createMsgCharMonster(self, self);
            }
            if (self.timeExit > 0L && System.currentTimeMillis() - self.timeExit > 5000L) {
                CharManager.instance.kickPlayer(self, "char 5");
                return;
            }
            if (self.mapID != 204) {
                if (!MessageCreator.isVersion162(self) && self.timedie > 0L && System.currentTimeMillis() - self.timedie >= 1000L) {
                    // Guc / hoi sinh moi (HoiSinhGame): hop thoai lua chon thay popup "hoi sinh tai cho" cu.
                    if (!HoiSinhGame.sauMotGiay(self) && self.myCountry > -1 && !Map.idMapMONSTER.get(self.myCountry).contains(self.map.mapId)) {
                        final int[] cost = {1000, 2000, 3000, 4000, 5000, 10000, 15000, 20000, 30000, 60000, 70000, 500000, 1000000, 1300000, 1500000, 2000000, 2500000, 3000000, 4500000};
                        int cp = cost[Math.min(self.lvDetail.lv / 5, cost.length - 1)];
                        if (self.mapID == 112 || self.mapID == 2541 || self.mapID == 2542 || self.mapID == 2543 || self.mapID == 2544) {
                            cp = 500000;
                        // } else if (self.mapID == 206) {
                        //     cp = 3000000;
                        } else {
                            if (self.myCountry != self.inCountry && self.charthanthu != null) {
                                cp = cost[Math.min(self.lastLV / 5, cost.length - 1)];
                            }
                            if (self.charthanthu != null && self.isKiller) {
                                cp *= 2;
                            }
                        }
                        if (self.mapID == 204 || self.mapID == 205) {
                            cp = 0;
                        }
                        if (cp > 0) {
                            if (self.potions[119] > 0) {
                                final String[] menu = {"Dùng cỏ hồi sinh", "Hs bằng xu (" + cp + " xu)"};
                                self.sendMessage(MessageCreator.createMsgMenu(menu, -802, 0));
                            } else {
                                self.sendMessage(MessageCreator.createMsgPopUp(self.id, 1, "Bạn muốn hồi sinh tại chỗ không. Chi phí " + cp + " xu"));
                            }
                        } else {
                            self.sendMessage(MessageCreator.createMsgPopUp(self.id, 1, "Bạn muốn hồi sinh tại chỗ không?"));
                        }
                    }
                    self.timedie = 0L;
                }
                if (self.kickOutTown()) {
                    final int[] mapstart2 = {9, 481, 482, 483, 484};
                    final int homeX = 31 + Database.r.nextInt() % 5;
                    final int homeY = 79 + Database.r.nextInt(20);
                    self.map.move2Map(self, homeX, homeY, mapstart2[Map.r.nextInt(MessageCreator.nclone)], self.inCountry);
                }
            } else if (self.timedie > 0L && System.currentTimeMillis() - self.timedie >= 10000L) {
                self.hp = self.maxhp;
                self.mp = self.maxmp;
                self.sendMessage(MessageCreator.createMainCharInfoMessage(self));
                self.sendToNearPlayer(MessageCreator.createNew_HMP_Message(self, 0));
                self.timedie = 0L;
            }
            if (self.timeSendHelp > 0L && ((System.currentTimeMillis() - self.timeSendHelp > 3000L && self.indexHelp < Text.help.length) || self.indexHelp == 0)) {
                final Message l = new Message(27);
                l.dos.writeShort(self.idBot);
                l.dos.writeUTF(Text.help[self.indexHelp++]);
                self.timeSendHelp = System.currentTimeMillis();
                final Char bot = CharManager.instance.getByCharID(self.idBot);
                if (bot != null && self.near(bot, 220)) {
                    self.sendMessage(l);
                } else {
                    self.indexHelp = Text.help.length;
                    self.timeSendHelp = 0L;
                }
            }
            if (self.timeStartHack > 0L && System.currentTimeMillis() - self.timeStartHack > 30000L) {
                CharManager.instance.kickPlayer(self, "char 6");
                return;
            }
            if (Map.config == 0) {
                self.resetConfig();
            } else {
                self.nNearchar = 30000;
            }
            if (self.nNearchar != 30000) {
                if ((self.myCountry > -1 && self.mapID == Map.idMapDautruong) || self.mapID == 204 || self.mapID == 205 || (self.mapID == Map.idMapTown && Map.getTown[self.myCountry])) {
                    self.nNearchar = 30000;
                } else if (Map.config == 0) {
                    self.resetConfig();
                }
            }
            if (self.autoGetItem == 1) {
                try {
                    while (self.idItem.size() > 0) {
                        self.map.doGetItem(self, self.idItem.remove(0));
                    }
                    while (self.idPotion.size() > 0) {
                        self.map.doGetPotion(self, self.idPotion.remove(0));
                    }
                    while (self.idGem.size() > 0) {
                        self.map.doGetGemItem(self, 6, self.idGem.remove(0));
                    }
                    while (self.idSP.size() > 0) {
                        self.map.doGetGemItem(self, 7, self.idSP.remove(0));
                    }
                    while (self.idItemQuest.size() > 0) {
                        self.map.doGetItemQuest(self, self.idItemQuest.remove(0));
                    }
                } catch (final Exception ex13) {
                }
            }
            if (self.beStune && System.currentTimeMillis() > self.timeBeStune) {
                self.beStune = false;
            }
            self.updateGoldTime();
            if (System.currentTimeMillis() - self.timeWait >= TeamServer.timeBoardRun && self.map != null) {
                if (self.gotoIsland && (self.mapID == 106 || self.mapID == 2421 || self.mapID == 2422 || self.mapID == 2423 || self.mapID == 2424)) {
                    int id2 = self.mapID % 2420;
                    if (self.mapID == 106) {
                        id2 = 0;
                    }
                    if (id2 != 0) {
                        id2 += 2440;
                    } else {
                        id2 = 107;
                    }
                    self.map.move2Map(self, 19 + Map.random(3) + 1, 10 + Map.random(3) + 1, id2, self.inCountry);
                }
                self.gotoIsland = false;
            }
            if (self.tDelay > 0 && System.currentTimeMillis() - self.timeOutPoinson >= self.tDelay * 1000) 
            {
                if (self.hpDocLan > 0) 
                {
                    self.hp += self.hpDocLan;
                    if (self.calculatorHPMP()) {
                        try {
                            self.sendMessage(MessageCreator.createMainCharInfoMessage(self));
                        } catch (final Exception ex14) {
                        }
                    }
                    try {
                        self.sendMessage(MessageCreator.createNew_HMP_Message(self, self.hpDocLan));
                        self.sendToNearPlayer(MessageCreator.createNew_HMP_Message(self, self.hpDocLan));
                    } catch (final IOException ex15) {
                    }
                }
                self.hp -= self.poinson;
                self.totalTime -= (byte) self.tDelay;
                self.timeOutPoinson = System.currentTimeMillis();
                if (self.totalTime <= 0) 
                {
                    self.totalTime = 36;
                    self.tDelay = 0;
                    self.poinson = 0;
                    self.hpDocLan = 0;
                }
                if (self.hp <= 0) 
                {
                    self.hp = 0;
                    self.totalTime = 36;
                    self.tDelay = 0;
//                    System.out.println("update khi nhan vat die: " + charname + ", hp còn lại: " + self.hp);
                    self.actorDie();
                    self.desTroy();
                    if (self.isMonster) 
                    {
                        final Vector<Char> players = self.map.getAllPlayer(Map.openLog ? 0 : self.myCountry, self.region);
                        for (int k = 0; k < players.size(); ++k) {
                            final Char ccc = players.get(k);
                            if (ccc != null && ccc.hp > 0 && !ccc.isMonster) {
                                final int[] potions = ccc.potions;
                                final int n = 102;
                                ++potions[n];
                                ccc.sendMessage(MessageCreator.createCharInventoryMessage(ccc, 0));
                                ccc.sendMessage(MessageCreator.createMsgChat(ccc.id, "Nhận được rương bạc"));
                            }
                        }
                        self.map.charMonsterDissapear(self);
                        self.hp = self.maxhp;
                        self.mp = self.maxmp;
                        self.sendMessage(MessageCreator.createMainCharInfoMessage(self));
                        self.sendToNearPlayer(MessageCreator.createNew_HMP_Message(self, 0));
                    }
                    if (Map.giveCardFail(self)) {
                        self.map.doSend2AllChar(MessageCreator.createServerAlertAutoOffMessage(String.valueOf(self.charname) + " giao thẻ thất bại"), self.myCountry);
                        self.map.sendAllPlayer(MessageCreator.createMsgStartGetTown(self.inCountry), self.inCountry);
                    }
                    self.map.onActorDie(self);
                }
            }
            if (self.isKiller && System.currentTimeMillis() - self.timeKiller >= 86400000L && self.nPKill > 0) {
                self.nPKill = 0;
                self.timeKiller = 0L;
            }
            for (int i2 = 0; i2 < self.arrayBuff.length; ++i2) {
                if (self.arrayBuff[i2] != -1) {
                    final long t = System.currentTimeMillis() - self.timeUseBuff[i2];
                    if (t / 1000L > self.countDown[i2]) {
                        final short[] countDown = self.countDown;
                        final int n2 = i2;
                        ++countDown[n2];
                        if (self.countDown[i2] >= self.timeOut[i2]) {
                            if (self.arrayBuff[i2] == 25 || self.arrayBuff[i2] == 20) {
                                Message m3 = null;
                                try {
                                    if (self.arrayBuff[i2] == 25) {
                                        self.percentBuff[1] = 0;
                                        self.percentBuff[2] = 0;
                                        self.calculatorHPMP();
                                    } else if (self.arrayBuff[i2] == 20) {
                                        self.percentBuff[0] = 0;
                                    }
                                    m3 = MessageCreator.createMainCharInfoMessage(self);
                                    self.sendMessage(m3);
                                } catch (final Exception ex16) {
                                } finally {
                                    try {
                                        m3.cleanup();
                                    } catch (final Exception ex17) {
                                    }
                                }
                                try {
                                    m3.cleanup();
                                } catch (final Exception ex18) {
                                }
                            }
                            self.arrayBuff[i2] = -1;
                            self.countDown[i2] = 0;
                        }
                    }
                }
            }
            self.updateNearChar();
            self.subUpdate();
        } catch (final Exception e) {
            e.printStackTrace();
            System.out.println("LOI TRONG UPDATE CHAR " + self.mapID);
        }
        self.updateEffKham();
        if (!AdminHandler.isStopServer && System.currentTimeMillis() - self.timeSave > 0L && !AdminHandler.isStopServer) {
            final Char pp = self;
            ThreadPool.instant.workList.add(new Runnable() {
                @Override
                public void run() {
                    Database.instance.saveCharAuto(pp);
                }
            });
            Database.instance.saveOrtherLog("", self.charname, String.valueOf(self.lvDetail.lv) + "_" + self.lvDetail.getExp() + "_" + self.xpLost + "_" + self.xu + "_" + self.luong + " xutim: " + self.xutim + " xumat: " + self.xumat, "autosave");
            Database.instance.saveOrtherLog("", self.charname, String.valueOf(self.xu) + "_" + self.xumat + "_" + self.xutim, "logxu");
            String[] info = self.getListGem();
            info = self.getInfoSaveTubinh();
           
            self.allGemGet = new int[Map.gemTemplate.length];
            self.allGemUse = new int[Map.gemTemplate.length];
            self.allGemGetLock = new int[Map.gemTemplate.length];
            self.allGemUseLock = new int[Map.gemTemplate.length];
            final short[] time = Char.AUTOSAVE_MINUTES;
            self.timeSave = System.currentTimeMillis() + time[Map.r.nextInt(time.length)] * 60 * 1000;
        }
    }

    static int getIdHeadDynamic(Char self) {
       
           if(self.isBatGioi() && self.isBatCaiTrang())
           {
            if(self.getLevelCaiTrang() >= 13)
            {
                return 8700 + 249;
            } else {
                return 8700 + 238;
            }
           }
           if(self.isSaTang() && self.isBatCaiTrang()){
            if(self.getLevelCaiTrang() >= 13){
                return 8700 + 252;
            } else {
                return 8700 + 241;
            }
           }
        // todo thêm đầu mặt nạ
            if (self.wModel.isCaiTaoSonTinh()) {
                return 8700 + 281;
            }
            if (self.wModel.isCaiTaoMiNuong()) {
                return 8700 + 278;
            }
            if (self.wModel.isGiapRong()) {
                return 8700 + 229;
            }
            if (self.wModel.isMeoXanh()) {
                return 8700 + 216;
            }
            if (self.wModel.isMeoQuyToc()) {
                return 8700 + 213;
            }

        if (!self.isTatMatNa()) {

            if (self.wModel.isMatNaTho()) {
                return 8700 + 223;
            }
            if (self.wModel.isMatNaCao()) {
                return 8700 + 222;
            }

            if (self.wModel.isMatNaBiNgo()) {
                return Char.ID_MAT_NA_BI_NGO;
            }
            if (self.wModel.isMatNaQuy()) {
                return Char.ID_MAT_NA_QUY;
            }
            if (self.wModel.isMatNaSoi()) {
                return Char.ID_MAT_NA_SOI;
            }
            if (self.wModel.isMatNaZoombi()) {
                return Char.ID_MAT_NA_ZOOMBI;
            }
            if (self.wModel.isMatNaPanda()) {
                return Char.ID_MAT_NA_PANDA;
            }
            if (self.wModel.isMatNaOngDia()) {
                // So hardcode rieng nhu 837/838 (8700+223 / 8700+222), KHONG doc tu
                // ideff trong DB vi ideff cua 872 la -1.
                // map/d_effect/244 da co san trong repo tu 09/2024 va co cung cau truc
                // voi 222/223: nSmallImage=3, nframe=18, tongPart=18. Anh 244.png la
                // ba khung mat ong dia (truoc / sau gay / nghieng), bang mau trung
                // 14/14 voi icon 807. 244 cung da nam san trong id_eff_tool_ngu_long
                // nen goi 26 luc dang nhap da gui khung hinh di roi.
                return 8700 + 244;
            }
        }
        

        if (self.wModel.isTieuLongNu()) {
            return Char.ID_HEAD_TIEU_LONG_NU;
        }
        if (self.wModel.isDuongQua()) {
            return Char.ID_HEAD_DUONG_QUA;
        }
        if (self.wModel.isTienNu()) {
            return EffectBuff.EFF_TOC_TIEN_NU;
        }
        if (self.wModel.isTinhQuan()) {
            return EffectBuff.EFF_TOC_TINH_QUAN;
        }
        if (self.wModel.isAoDaiNam()) {
            return EffectBuff.EFF_NON_AO_DAI_NAM;
        }
        if (self.wModel.isAoDaiNu()) {
            return EffectBuff.EFF_NON_AO_DAI_NU;
        }
        
         // TODO THÊM ÁO THỜI TRANG (3) HEAD
        if (self.wModel.isNgheThuong()) {
            return 8700 + 175;
        }
        if (self.wModel.isNgocNu()) {
            return 8700 + 178;
        }       
        if (self.wModel.isXichCuoc()) {
            return 8700 + 181;
        }       
        if (self.wModel.isKimDong()) {
            return 8700 + 185;
        }       
        if (self.wModel.isLoiCong()) {
            return 8700 + 172;
        }
        if (self.wModel.isLacLongQuan()) {
            return 8700 + 119;
        }
        if (self.wModel.isAuCo()) {
            return 8700 + 116;
        }
        // Ba Thien Ho: mu dau ho. Nam mau xanh-vang (194), nu mau do-vang (191).
        // Sprite da co san trong map/d_effect va da nam trong id_eff_tool_ngu_long.
        if (self.wModel.isBaThienHoNam()) {
            return 8700 + 194;
        }
        if (self.wModel.isBaThienHoNu()) {
            return 8700 + 191;
        }
        // Bon bo bong da, moi bo mot bo sprite rieng, ba so lien nhau theo thu tu
        // head / body / leg:
        //   788 nam ao do          153 / 154 / 155
        //   789 nu  ao trang       156 / 157 / 158
        //   791 nam ao cam         160 / 161 / 162
        //   792 nu  ao do-trang soc 163 / 164 / 165
        if (self.wModel.isBongDaNamAoDo()) {
            return 8700 + 153;
        }
        if (self.wModel.isBongDaNuAoTrang()) {
            return 8700 + 156;
        }
        if (self.wModel.isBongDaNamAoCam()) {
            return 8700 + 160;
        }
        if (self.wModel.isBongDaNuAoSoc()) {
            return 8700 + 163;
        }
        if (self.wModel.isAonoelNam2024()) {
            return 8700 + 262;
        }
        if (self.wModel.isAonoelNu2024()) {
            return 8700 + 259;
        }

        return -1;
    }

    static String getDBInfo(Char self) {
        String pInfo = new StringBuilder().append(self.headStyle).toString();
        pInfo = String.valueOf(pInfo) + "," + self.gender;
        pInfo = String.valueOf(pInfo) + "," + self.charname;
        pInfo = String.valueOf(pInfo) + "," + self.charClass;
        pInfo = String.valueOf(pInfo) + "," + self.lvDetail.getExp();
        pInfo = String.valueOf(pInfo) + "," + self.lastLV;
        pInfo = String.valueOf(pInfo) + "," + self.hp;
        pInfo = String.valueOf(pInfo) + "," + self.mp;
        pInfo = String.valueOf(pInfo) + "," + self.mapID;
        pInfo = String.valueOf(pInfo) + "," + self.x;
        pInfo = String.valueOf(pInfo) + "," + self.y;
        pInfo = String.valueOf(pInfo) + "," + self.killer;
        pInfo = String.valueOf(pInfo) + "," + self.timeKiller;
        pInfo = String.valueOf(pInfo) + "," + self.nPKill;
        pInfo = String.valueOf(pInfo) + "," + self.fullGoldTime;
        pInfo = String.valueOf(pInfo) + "," + self.halfGoldTime;
        pInfo = String.valueOf(pInfo) + "," + (self.isAdmin ? 1 : 0);
        pInfo = String.valueOf(pInfo) + "," + self.typeConfig;
        pInfo = String.valueOf(pInfo) + "," + self.autoGetItem;
        pInfo = String.valueOf(pInfo) + "," + self.autoSkill;
        pInfo = String.valueOf(pInfo) + "," + self.oldLv;
        pInfo = String.valueOf(pInfo) + "," + self.autoComeHome;
        pInfo = String.valueOf(pInfo) + "," + self.rideHorse;
        pInfo = String.valueOf(pInfo) + "," + self.wModel.idModel;
        pInfo = String.valueOf(pInfo) + "," + self.pointCongHienClan;
        pInfo = String.valueOf(pInfo) + "," + (self.isOnlineToDay ? 1 : 0);
        pInfo = String.valueOf(pInfo) + "," + self.rcvGifByLv;
        pInfo = String.valueOf(pInfo) + "," + self.rcvGifByHour;
        pInfo = String.valueOf(pInfo) + "," + self.rcvGifByMonth;
        pInfo = String.valueOf(pInfo) + "," + self.totalDayInMonth;
        pInfo = String.valueOf(pInfo) + "," + self.idAutoSkill;
        pInfo = String.valueOf(pInfo) + "," + self.idAutoGetitem;
        pInfo = String.valueOf(pInfo) + "," + self.dateStartOnline;
        pInfo = String.valueOf(pInfo) + "," + self.lvHouse;
        pInfo = String.valueOf(pInfo) + "," + self.lvStore;
        pInfo = String.valueOf(pInfo) + "," + self.maxBag;
        pInfo = String.valueOf(pInfo) + "," + self.slotWearing;
        pInfo = String.valueOf(pInfo) + "," + self.nMoonCake2Exp;
        pInfo = String.valueOf(pInfo) + "," + self.receiveLG;
        pInfo = String.valueOf(pInfo) + "," + self.myCountry;
        pInfo = String.valueOf(pInfo) + "," + self.gif35;
        pInfo = String.valueOf(pInfo) + "," + self.luongNap;
        pInfo = String.valueOf(pInfo) + "," + self.luonglost;
        pInfo = String.valueOf(pInfo) + "," + self.dayLogin;
        pInfo = String.valueOf(pInfo) + "," + self.forbitChat;
        pInfo = String.valueOf(pInfo) + "," + self.usingSummons;
        pInfo = String.valueOf(pInfo) + "," + self.maxKill;
        pInfo = String.valueOf(pInfo) + "," + self.honor;
        pInfo = String.valueOf(pInfo) + "," + self.xichtho_thienlyma;
        pInfo = String.valueOf(pInfo) + "," + self.married;
        pInfo = String.valueOf(pInfo) + "," + self.idWedding;
        pInfo = String.valueOf(pInfo) + "," + self.nameHusband_wife;
        pInfo = String.valueOf(pInfo) + "," + self.rcvGiftWedding;
        pInfo = String.valueOf(pInfo) + "," + self.passWord;
        pInfo = String.valueOf(pInfo) + "," + self.countQuestClan;
        pInfo = String.valueOf(pInfo) + "," + self.pointActiveQuest;
        pInfo = String.valueOf(pInfo) + "," + self.idArena;
        pInfo = String.valueOf(pInfo) + "," + self.pArena;
        pInfo = String.valueOf(pInfo) + "," + self.idMainQuest;
        pInfo = String.valueOf(pInfo) + "," + self.nBinhTra;
        pInfo = String.valueOf(pInfo) + "," + self.nHopMut;
        pInfo = String.valueOf(pInfo) + "," + self.gif83;
        pInfo = String.valueOf(pInfo) + "," + self.ntangtoc;
        pInfo = String.valueOf(pInfo) + "," + self.subpk;
        pInfo = String.valueOf(pInfo) + "," + self.receiveQuestVulan;
        pInfo = String.valueOf(pInfo) + "," + self.timeUp5Attribute;
        pInfo = String.valueOf(pInfo) + "," + self.typeTieu;
        pInfo = String.valueOf(pInfo) + "," + self.timeOutGame;
        pInfo = String.valueOf(pInfo) + "," + self.region;
        // Qua getter chu KHONG doc truong tho: getLuongLock() chan gia tri am ve 0 va bao
        // mot lan (xem Char.java). Cung ham luu nay ghi cot `luonglock` bang p.getLuongLock(),
        // nen neu o day doc truong tho thi hai cho ghi ra hai so khac nhau khi dang am.
        pInfo = String.valueOf(pInfo) + "," + self.getLuongLock();
        pInfo = String.valueOf(pInfo) + "," + self.minuteExp;
        pInfo = String.valueOf(pInfo) + "," + self.luot_van_tieu;
        pInfo = String.valueOf(pInfo) + "," + self.buy_luot_van_tieu;
        pInfo = String.valueOf(pInfo) + "," + self.cuop_tieu;
        pInfo = String.valueOf(pInfo) + "," + self.timeJoinClan;
        pInfo = String.valueOf(pInfo) + "," + self.giveMoneyClan;
        pInfo = String.valueOf(pInfo) + "," + self.minuteSocola;
        pInfo = String.valueOf(pInfo) + "," + self.receiveGiftEvent;
        pInfo = String.valueOf(pInfo) + "," + self.idTongKim;
        pInfo = String.valueOf(pInfo) + "," + self.idTeamTongKim;
        pInfo = String.valueOf(pInfo) + "," + self.diemdoivePH;
        pInfo = String.valueOf(pInfo) + "," + self.winPH;
        pInfo = String.valueOf(pInfo) + "," + self.timeExistCharHire;
        pInfo = String.valueOf(pInfo) + "," + self.classlinh;
        pInfo = String.valueOf(pInfo) + "," + self.lvlinhthue;
        pInfo = String.valueOf(pInfo) + "," + self.genderlinh;
        pInfo = String.valueOf(pInfo) + "," + self.lvlinh;
        pInfo = String.valueOf(pInfo) + "," + self.countnvVulan;
        pInfo = String.valueOf(pInfo) + "," + self.firstNapMoney;
        pInfo = String.valueOf(pInfo) + "," + self.timeUseTheMuaBan;
        pInfo = String.valueOf(pInfo) + "," + self.banphaohoa;
        pInfo = String.valueOf(pInfo) + "," + self.x2Phaohoa;
        pInfo = String.valueOf(pInfo) + "," + self.dayCamChat;
        pInfo = String.valueOf(pInfo) + "," + self.countTime;
        pInfo = String.valueOf(pInfo) + "," + self.receiveGiftOldChar;
        pInfo = String.valueOf(pInfo) + "," + self.buychoi;
        pInfo = String.valueOf(pInfo) + "," + self.buylongden;
        pInfo = String.valueOf(pInfo) + "," + self.xuxai;
        pInfo = String.valueOf(pInfo) + "," + self.luongxai;
        pInfo = String.valueOf(pInfo) + "," + self.vetangluong;
        pInfo = String.valueOf(pInfo) + "," + self.countMonsterKill;
        pInfo = String.valueOf(pInfo) + "," + self.hlt_buy;
        pInfo = String.valueOf(pInfo) + "," + self.luongxainhanRuong;
        pInfo = String.valueOf(pInfo) + "," + self.idEffDanhHieu;
        pInfo = String.valueOf(pInfo) + "," + self.minuteOnline;
        pInfo = String.valueOf(pInfo) + "," + self.timeInClan;
        pInfo = String.valueOf(pInfo) + "," + self.nhomThidau;
        pInfo = String.valueOf(pInfo) + "," + self.vip;
        pInfo = String.valueOf(pInfo) + "," + self.diemNapVip;
        pInfo = String.valueOf(pInfo) + "," + self.diemxaiVip;
        pInfo = String.valueOf(pInfo) + "," + (self.rcvInviteVip ? 1 : 0);
        pInfo = String.valueOf(pInfo) + "," + (self.isCaiTrang);
        pInfo = String.valueOf(pInfo) + "," + self.diemDoiThuong;
        pInfo = String.valueOf(pInfo) + "," + self.diemQuaiHomNay;
        pInfo = String.valueOf(pInfo) + "," + self.soQuaiHomNay;
        pInfo = String.valueOf(pInfo) + "," + self.timeAoHoaHoaKyLan;
        pInfo = String.valueOf(pInfo) + "," + self.thuongLv35;
        pInfo = String.valueOf(pInfo) + "," + (self.isNghienAuto ? 1 : 0);
        pInfo = String.valueOf(pInfo) + "," + (self.isAutoBan ? 1 : 0);
        pInfo = String.valueOf(pInfo) + "," + self.luongDaXaiTrungThu;
        pInfo = String.valueOf(pInfo) + "," + (self.isAutoVeMapUp ? 1 : 0);

        return pInfo;
    }

    static void setItemTuiDo(Char self, String info) {
        if (info == null) {
            return;
        }
        try {
            if (info.startsWith(">")) {
                info = info.substring(1, info.length());
            }
        } catch (final Exception ex) {
        }
        
        if (!info.equals("")) {
            final String[] allitem = Char.split(info, ">");
            int size = allitem.length;
            if (size > 130) {
                size = 130;
            }
            
            for (int i = 0; i < size; ++i) {
                try {
                    final String[] dtitem = Char.split(allitem[i], "|");
                    final Item item = new Item();
                    item.dbInfo = dtitem[0];
                    item.initInfoFromDB();
                    item.owner = self.charDBID;
                    item.setTemplate(item.tempateID, item.clazz, item.clazz, item.tempateID);
    
                    // Only keep essential time-based checks
                    if (item.getMinuteLoan(false) <= 0 && item.minuteExist > 0) {
                        try {
                            self.sendMessage(MessageCreator.createServerAlertAutoOffMessage("Thời gian sử dụng " + item.getTemplate().name + " đã hết"));
                            Database.instance.deleteItem(item.dbid);
                            // 16/09/2026: them dbid (theo yeu cau "log chi tiet hon").
                            Database.instance.saveOrtherLog("tob_log_other_item", self.charname, "dbid" + item.dbid + " " + item.getTemplate().name + "|" + item.getDBInfo() + "|" + item.getAttribute(), "timeout");
                        } catch (final Exception ex2) {
                        }
                        continue;
                    }
    
                    // Process item attributes
                    final String[] data = Char.split(dtitem[1], ",");
                    String countnguhanh = "";
                    for (int j = 0; j < data.length; ++j) {
                        try {
                            if (j < 33) {
                                item.atb[j] = Short.parseShort(data[j].trim());
                                if (j >= 28 && item.atb[j] > 0 && j != item.idNguHanh2) {
                                    countnguhanh = String.valueOf(countnguhanh) + j + ",";
                                }
                            } else if (j < 43) {
                                item.newAtb[j - 33] = Byte.parseByte(data[j].trim());
                            } else if (j < 58) {
                                item.addMoreLevelSkill[j - 43] = Byte.parseByte(data[j].trim());
                            } else if (j < 61) {
                                item.lockAtb[j - 58] = Byte.parseByte(data[j].trim());
                            } else {
                                item.otherAtt[j - 61] = Short.parseShort(data[j].trim());
                            }
                        } catch (final Exception ex3) {
                        }
                    }
    
                    // Handle animal items
                    if (Map.isWearingAnimal(item.getTemplate().type)) {
                        if (!item.isHaveAttLock() && item.colorName == 1) {
                            switch (item.hangItem) {
                                case 1:
                                    item.lockItemAnimal(new byte[]{0, 0, 0, 0, 0, 1});
                                    break;
                                case 2:
                                    item.lockItemAnimal(new byte[]{0, 0, 0, 0, 1, 0});
                                    break;
                                case 3:
                                    item.lockItemAnimal(new byte[]{0, 0, 0, 1, 0, 0});
                                    break;
                                case 4:
                                    item.lockItemAnimal(new byte[]{0, 0, 1, 0, 0, 0});
                                    break;
                                case 5:
                                    byte[] material = new byte[6];
                                    material[1] = 1;
                                    item.lockItemAnimal(material);
                                    break;
                            }
                        }
                        item.reFixItemAnimal(self.charname);
                    }
    
                    // Set server-specific char seal
                    if (TeamServer.isServerLienDau() && self.charname.indexOf("_") > -1 && !item.charSeal.equals(self.charname)) {
                        item.charSeal = self.charname;
                    }
    
                    // Final item setup
                    if (ItemLuckyDraw.isChoi(item.getTemplate().id)) {
                        item.atb[9] = 2;
                    }
                    item.id = self.getIDItem();
                    self.bagItems.add(item);
    
                } catch (final Exception e2) {
                    e2.printStackTrace();
                }
            }
        }
    }

    static void sendItem(Char self, final int type, final int idGif) {
        if (self.lvDetail.lv > 30) {
            return;
        }
        if (self.lastLV > 30) {
            return;
        }
        final Calendar calNow = Calendar.getInstance();
        if (self.calCheck != null && calNow.getTime().getTime() - self.calCheck.getTime().getTime() > 604800000L) {
            return;
        }
        switch (type) {
            case 0: {
                if (idGif == 0) {
                    final int[] potions = self.potions;
                    final int n = 22;
                    potions[n] += 50;
                    final int[] potions2 = self.potions;
                    final int n2 = 24;
                    potions2[n2] += 50;
                    final int[] potions3 = self.potions;
                    final int n3 = 84;
                    potions3[n3] += 3;
                    final int[] potions4 = self.potions;
                    final int n4 = 79;
                    ++potions4[n4];
                } else if (idGif == 1) {
                    final int[] potions5 = self.potions;
                    final int n5 = 22;
                    potions5[n5] += 50;
                    final int[] potions6 = self.potions;
                    final int n6 = 24;
                    potions6[n6] += 50;
                    final int[] potions7 = self.potions;
                    final int n7 = 84;
                    potions7[n7] += 3;
                    final int[] potions8 = self.potions;
                    final int n8 = 79;
                    potions8[n8] += 3;
                    Map.addGoldTimeTicket(self, 1);
                } else if (idGif == 2) {
                    final int[] potions9 = self.potions;
                    final int n9 = 22;
                    potions9[n9] += 100;
                    final int[] potions10 = self.potions;
                    final int n10 = 24;
                    potions10[n10] += 100;
                    final int[] potions11 = self.potions;
                    final int n11 = 84;
                    potions11[n11] += 3;
                    final int[] potions12 = self.potions;
                    final int n12 = 79;
                    potions12[n12] += 5;
                } else if (idGif == 3) {
                    final int[] potions13 = self.potions;
                    final int n13 = 22;
                    potions13[n13] += 100;
                    final int[] potions14 = self.potions;
                    final int n14 = 24;
                    potions14[n14] += 100;
                    final int[] potions15 = self.potions;
                    final int n15 = 84;
                    potions15[n15] += 3;
                    final int[] potions16 = self.potions;
                    final int n16 = 79;
                    potions16[n16] += 7;
                }
                try {
                    self.sendMessage(MessageCreator.createCharInventoryMessage(self, 0));
                    self.sendMessage(MessageCreator.createServerAlertMessage("Bạn đã nhận được quà.", ""));
                } catch (final Exception ex) {
                }
                Database.instance.saveOrtherLog("", self.charname, String.valueOf(type) + " nhan goi qua " + idGif, "tanthu");
                break;
            }
            case 1: {
                if (idGif == 0) {
                    final int[] potions17 = self.potions;
                    final int n17 = 79;
                    potions17[n17] += 3;
                } else if (idGif == 1) {
                    final int[] potions18 = self.potions;
                    final int n18 = 22;
                    potions18[n18] += 50;
                    final int[] potions19 = self.potions;
                    final int n19 = 24;
                    potions19[n19] += 50;
                } else if (idGif == 2) {
                    final int[] potions20 = self.potions;
                    final int n20 = 79;
                    potions20[n20] += 3;
                } else if (idGif == 3) {
                    final int[] idModel = {50, 55, 51, 54};
                    int rd = Map.r.nextInt(2);
                    if (self.headStyle % 2 == 0) {
                        rd += 2;
                    }
                    final ShopTemplate item = Map.getShopTemplate(idModel[rd]);
                    Map.doBuyModelClothe(self, item, 3);
                    self.sendMessage(MessageCreator.createCharInventoryMessage(self, 1));
                } else if (idGif == 4) {
                    final int[] potions21 = self.potions;
                    final int n21 = 79;
                    potions21[n21] += 5;
                } else if (idGif == 5) {
                    final int[] potions22 = self.potions;
                    final int n22 = 79;
                    potions22[n22] += 7;
                }
                try {
                    self.sendMessage(MessageCreator.createCharInventoryMessage(self, 0));
                    self.sendMessage(MessageCreator.createServerAlertMessage("Bạn đã nhận được quà.", ""));
                } catch (final Exception ex2) {
                }
                Database.instance.saveOrtherLog("", self.charname, String.valueOf(type) + " nhan goi qua " + idGif, "tanthu");
            }
            case 2: {
            }
            case 3: {
            }
        }
    }

    static Vector<Item> getAnimalWearing(Char self) {
        final Vector<Item> a = new Vector<Item>();
        byte houyen = 0;
        byte bandap = 0;
        byte non = 0;
        byte giap = 0;
        byte yen = 0;
        byte skin = 0;
        boolean bInventory = false;
        final Vector<Item> itBackInventory = new Vector<Item>();
        if (self.animalRide != null) {
            for (int i = 0; i < self.awItems.size(); ++i) {
                final Item it = self.awItems.get(i);
                // Vat pham SKIN LINH THU khong gan theo tung con thu: nguoi choi chon
                // skin theo NHAN VAT, doi con thu nao cung giu nguyen skin do. Nen o
                // day nhan no bat ke idAnimal, roi buoc lai vao con dang cuoi de lan
                // luu tiep theo va cac cho khac (exchangeItemAnimal) van khop.
                //
                // Loc theo ID TEMPLATE chu khong theo type: type 30 khong nam trong
                // dai isWearingAnimal (14..18) — xem real.SkinThuCuoi.ID_ITEM.
                if (it.getTemplate() != null && SkinThuCuoi.laItemSkin(it.getTemplate().id)) {
                    if (skin == 0) {
                        skin = 1;
                        it.idAnimal = self.animalRide.dbid;
                        a.add(it);
                    } else {
                        it.place = 0;
                        it.idAnimal = -1;
                        bInventory = true;
                        itBackInventory.add(it);
                    }
                    continue;
                }
                if (it.idAnimal == self.animalRide.dbid) {
                    if (it.getType() == 17) {
                        if (bandap == 0) {
                            a.add(it);
                            bandap = 1;
                        } else {
                            it.place = 0;
                            bInventory = true;
                        }
                    }
                    if (it.getType() == 16) {
                        if (non == 0) {
                            non = 1;
                            a.add(it);
                        } else {
                            it.place = 0;
                            bInventory = true;
                            itBackInventory.add(it);
                        }
                    }
                    if (it.getType() == 15) {
                        if (houyen == 0) {
                            houyen = 1;
                            a.add(it);
                        } else {
                            it.place = 0;
                            bInventory = true;
                            itBackInventory.add(it);
                        }
                    }
                    if (it.getType() == 18) {
                        if (yen == 0) {
                            yen = 1;
                            a.add(it);
                        } else {
                            it.place = 0;
                            bInventory = true;
                            itBackInventory.add(it);
                        }
                    }
                    if (it.getType() == 14) {
                        if (giap == 0) {
                            giap = 1;
                            a.add(it);
                        } else {
                            it.place = 0;
                            bInventory = true;
                            itBackInventory.add(it);
                        }
                    }
                } else {
                    boolean exist = false;
                    for (byte j = 0; j < self.animal.size(); ++j) {
                        final Animal anim = self.animal.get(j);
                        if (anim.dbid == it.idAnimal) {
                            exist = true;
                            bInventory = true;
                            break;
                        }
                    }
                    if (!exist) {
                        it.place = 0;
                        itBackInventory.add(it);
                    }
                }
            }
        }
        if (bInventory) {
            for (int i = 0; i < itBackInventory.size(); ++i) {
                final Item it = itBackInventory.get(i);
                self.awItems.remove(it);
                self.iItems.add(it);
                it.idAnimal = -1;
            }
            self.sendMessage(MessageCreator.createCharInventoryMessage(self, 1));
        }
        return a;
    }

    static void doOpenTuiQuaMayManNoel(Char self) {
        if (self.isFullInventory()) {
            self.sendMessage(MessageCreator.createServerAlertMessage("Hành trang đầy", ""));
            return;
        }
        Map.addXpCharEvent(self, 50000L, false, "char doOpenHopQuaVang");
        final int[] per = {500, 500, 500, 500, 500, 500, 500, 500, 500, 500, 100, 250, 250, 0, 250, 100, 200, 200, 200, 100};
        final int[] idgift = {GemTemplate.DA_TIEN_GIAI, GemTemplate.XUONG_C3, GemTemplate.XUONG_C4, GemTemplate.XUONG_C5, 35, 114, 113, 112, 93, 95, -1, -2, 35, -3, 117, -4, 676, 677, 84, 99};
        final int[] typegift = {6, 6, 6, 6, 4, 4, 4, 4, 4, 4, -1, -2, 4, -3, 4, -4, 3, 3, 4, -5};
        final int[] soluongmax = {1, 1, 1, 1, 1, 1, 1, 1, 50, 50, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1};
        final int[] soluongmin = {1, 1, 1, 1, 1, 1, 1, 1, 50, 50, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1};
        String info = "Chúc mừng bạn nhận được ";
        final Hashtable<Integer, Integer> indexReceive = new Hashtable<Integer, Integer>();
        for (int i = 0; i < 3; ++i) {
            int index = Char.getIndexRandom(per);
            for (int count = 0; count++ < 100 && indexReceive.get(index) != null; index = Char.getIndexRandom(per)) {
            }
            indexReceive.put(index, index);
            if (typegift[index] == -3) {
                final int time = Map.r.nextInt(2) + 1;
                self.createHeo(time, false);
                info = String.valueOf(info) + " đường khang, ";
            } else if (typegift[index] == -5) {
                int[] valuemn = {5, 5, 10, 30, 50};
                int randomMN = Map.r.nextInt(valuemn.length);
                Item newitem = Map.doCreateMatNa(self, 10080, Char.ID_MAT_NA[randomMN], randomMN, valuemn[randomMN]);
                self.sendMessage(MessageCreator.createServerAlertMessage("Nhận " + newitem.getName() + " 7 ngày", ""));
            } else if (typegift[index] == -4) {
                final int[] minute = {1440};
                final Item newItem = ItemLuckyDraw.createtemCoat(750, self, Map.r.nextInt(2), true, minute[Map.r.nextInt(minute.length)]);
                self.sendMessage(MessageCreator.createCharInventoryMessage(self, 1));
                info = String.valueOf(info) + newItem.getName() + " thời hạn " + newItem.minuteExist + " phút";
                self.sendMessage(MessageCreator.createMsgChat(self.id, info));
            } else if (typegift[index] == -2) {
                final int time = Map.r.nextInt(2) + 1;
                final Item it = Char.createPhiPhongMaxBaoKich(self, Map.r.nextInt(2), time * 24 * 60, null, false, false, 0, true);
                info = String.valueOf(info) + it.getName() + ", ";
            } else if (typegift[index] == -1) {
                final int[] idthe = {678, 679, 680};
                int iditem = idthe[Map.r.nextInt(idthe.length)];
                if (self.lvDetail.lv < 60) {
                    iditem = 678;
                } else if (self.lvDetail.lv < 70) {
                    iditem = 679;
                } else {
                    iditem = 680;
                }
                Map.doAddItemIsNotWearing(self, iditem, 0);
                final ItemTemplates template = (ItemTemplates) Map.itemTemplates.get(0).get(new Integer(iditem));
                info = String.valueOf(info) + template.name + ", ";
            } else if (typegift[index] == 6) {
                int soluong = soluongmin[index];
                if (soluongmin[index] < soluongmax[index]) {
                    soluong += Map.r.nextInt(soluongmax[index] - soluongmin[index] + 1);
                }
                self.doAddGemItem(idgift[index], soluong, true);
                info = String.valueOf(info) + soluongmax[index] + " " + Map.gemTemplate[idgift[index]].name + ", ";
                self.sendMessage(MessageCreator.createCharGemItem(self));
            } else if (typegift[index] == 4) {
                int soluong = soluongmin[index];
                if (soluongmin[index] < soluongmax[index]) {
                    soluong += Map.r.nextInt(soluongmax[index] - soluongmin[index] + 1);
                }
                final int[] potions = self.potions;
                final int n = idgift[index];
                potions[n] += soluong;
                info = String.valueOf(info) + soluong + " " + LoginHandler.PORTION_NAME[idgift[index]] + ", ";
                self.sendMessage(MessageCreator.createCharInventoryMessage(self, 0));
                if (idgift[index] == 9) {
                    try {
                        com.htbb.chongspam.ThongBaoGop.dua("nhan_thuong", String.valueOf(self.charname) + " đã nhận được Berry Razz vàng khi mở bao lì xì");
                    } catch (final Exception ex) {
                    }
                }
            } else if (typegift[index] == 3) {
                if (self.isFullInventory()) {
                    final int xu = 5000 + Map.r.nextInt(100000);
                    self.addXu(xu, "char 8");
                    info = String.valueOf(info) + xu + " xu" + ", ";
                    self.sendMessage(MessageCreator.createCharInventoryMessage(self, 0));
                } else {
                    final ItemTemplates template2 = (ItemTemplates) Map.itemTemplates.get(5).get(idgift[index]);
                    Item newItem = null;
                    newItem = new Item(template2, false, 0, 0, template2.id);
                    newItem.id = self.getIDItem();
                    newItem.owner = self.charDBID;
                    newItem.level = newItem.getTemplate().level;
                    self.iItems.add(newItem);
                    newItem.identify = self.charDBID;
                    self.sendMessage(MessageCreator.createCharInventoryMessage(self, 1));
                    info = String.valueOf(info) + newItem.getName();
                }
            }
        }
        self.sendMessage(MessageCreator.createServerAlertMessage(info, ""));
        Database.instance.saveOrtherLog("", self.charname, info, "hopquamayman");
    }

    static void doDoiQuaDacBietNoel2020(Char self) {
        if (self.countSlotInventory() < 5) {
            self.sendMessage(MessageCreator.createServerAlertMessage("Hành trang phải còn tối thiếu 3 ô trống mới có thể đổi quà", ""));
            return;
        }
        for (int i = 0; i < GemTemplate.ID_NGOC_RONG.length; ++i) {
            self.delGem(GemTemplate.ID_NGOC_RONG[i], 1, true);
        }
        final int[] soluongmax = {3, 10, 100, 100, 5, 1, 1, 1, 1, 5, 20, 20, 20, 10, 100, 1, 10, 1, 1, 1, 20, 3, 1, 1};
        final int[] soluongmin = {1, 5, 50, 50, 3, 1, 1, 1, 1, 3, 10, 10, 10, 5, 50, 1, 5, 1, 1, 1, 10, 1, 1, 1};
        final int[] per = {500, 500, 500, 500, 500, 500, 500, 10, 10, 500, 250, 250, 250, 150, 350, 50, 0, 150, 150, 0, 500, 50, 500, 150};
        final int[] idgift = {69, 100, 93, 95, 58, 35, GemTemplate.NGOC_HUYEN_MINH_4, GemTemplate.LKD_30, GemTemplate.LKD_35, GemTemplate.XUONG_C1, GemTemplate.BOT_TRANG, GemTemplate.BOT_XANH, GemTemplate.BOT_DO, GemTemplate.BOT_XANH_LA, GemTemplate.DA_MAY_MAN_C3, GemTemplate.DA_NGU_HOP_6, -4, -5, -6, -7, 19, -8, 77, -9};
        final int[] typegift = {4, 4, 4, 4, 4, 4, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, -4, -5, -6, -7, 4, -8, 4, -9};
        final int sl = Map.r.nextInt(3) + 3;
        String info = "Chúc mừng bạn nhận được ";
        final Hashtable<Integer, Integer> indexReceive = new Hashtable<Integer, Integer>();
        for (int j = 0; j < sl; ++j) {
            int index = Char.getIndexRandom(per);
            for (int count = 0; count++ < 100 && indexReceive.get(index) != null; index = Char.getIndexRandom(per)) {
            }
            indexReceive.put(index, index);
            if (typegift[index] == -8) {
                final int lk = Map.r.nextInt(3) + 1;
                self.addLuongLock(lk);
                info = String.valueOf(info) + com.htbb.pokemon.TienTeChu.chu(com.htbb.pokemon.TienTeChu.LUONG_KHOA, lk) + ", ";
            } else if (typegift[index] == -9) {
                final int[] minute = {4320};
                final Item newItem = ItemLuckyDraw.createtemCoat(750, self, Map.r.nextInt(2), true, minute[Map.r.nextInt(minute.length)]);
                self.sendMessage(MessageCreator.createCharInventoryMessage(self, 1));
                info = String.valueOf(info) + newItem.getName() + " thời hạn " + newItem.minuteExist + " phút";
                self.sendMessage(MessageCreator.createMsgChat(self.id, info));
            } else if (typegift[index] == -7) {
                self.doCreatePetBachThu(14400, false);
                info = String.valueOf(info) + " pet Sandshrew, ";
            } else if (typegift[index] == -6) {
                final Item it = Map.doBuyModelClothe(self, self.isCharNam() ? 725 : 726, 10, 1);
                info = String.valueOf(info) + " " + it.getName();
            } else if (typegift[index] == -5) {
                final int time = 3;
                self.createHeo(time, false);
                info = String.valueOf(info) + " Piloswine, ";
            } else if (typegift[index] == -4) {
                int soluong = soluongmin[index];
                if (soluongmin[index] < soluongmax[index]) {
                    soluong += Map.r.nextInt(soluongmax[index] - soluongmin[index] + 1);
                }
                final int indexTB = Map.r.nextInt(GemTemplate.ID_TU_BINH.length);
                final int idtubinh = GemTemplate.ID_TU_BINH[indexTB][Map.r.nextInt(GemTemplate.ID_TU_BINH[indexTB].length)];
                self.doAddGemItem(idtubinh, soluong, false);
                info = String.valueOf(info) + soluong + " " + Map.gemTemplate[idtubinh].name + ", ";
                self.sendMessage(MessageCreator.createCharGemItem(self));
            } else if (typegift[index] == 6) {
                int soluong = soluongmin[index];
                if (soluongmin[index] < soluongmax[index]) {
                    soluong += Map.r.nextInt(soluongmax[index] - soluongmin[index] + 1);
                }
                self.doAddGemItem(idgift[index], soluong, false);
                info = String.valueOf(info) + soluong + " " + Map.gemTemplate[idgift[index]].name + ", ";
                self.sendMessage(MessageCreator.createCharGemItem(self));
            } else if (typegift[index] == 4) {
                int soluong = soluongmin[index];
                if (soluongmin[index] < soluongmax[index]) {
                    soluong += Map.r.nextInt(soluongmax[index] - soluongmin[index] + 1);
                }
                final int[] potions = self.potions;
                final int n = idgift[index];
                potions[n] += soluong;
                info = String.valueOf(info) + soluong + " " + LoginHandler.PORTION_NAME[idgift[index]] + ", ";
                self.sendMessage(MessageCreator.createCharInventoryMessage(self, 0));
            } else if (typegift[index] == 3) {
                if (self.isFullInventory()) {
                    final int xu = 5000 + Map.r.nextInt(100000);
                    self.addXu(xu, "char 9");
                    info = String.valueOf(info) + xu + " xu" + ", ";
                    self.sendMessage(MessageCreator.createCharInventoryMessage(self, 0));
                } else {
                    final ItemTemplates template = (ItemTemplates) Map.itemTemplates.get(5).get(idgift[index]);
                    Item newItem = null;
                    newItem = new Item(template, false, 0, 0, template.id);
                    newItem.id = self.getIDItem();
                    newItem.owner = self.charDBID;
                    newItem.level = newItem.getTemplate().level;
                    self.iItems.add(newItem);
                    newItem.identify = self.charDBID;
                    self.sendMessage(MessageCreator.createCharInventoryMessage(self, 1));
                    info = String.valueOf(info) + newItem.getName();
                }
            }
        }
        self.sendMessage(MessageCreator.createCharInventoryMessage(self, 1));
        self.sendMessage(MessageCreator.createCharInventoryMessage(self, 0));
        self.sendMessage(MessageCreator.createCharInventoryMessage(self, 2));
        self.sendMessage(MessageCreator.createCharGemItem(self));
        self.sendMessage(MessageCreator.createServerAlertMessage(info, ""));
        Database.instance.saveOrtherLog("", self.charname, info, "quadacbiet");
        Database.instance.saveLogThongKeChiTieu("DOI 7 VIEN NGOC RONG", 0, 1, 1L, "ngoc");
    }

    static boolean checkReceiveTrungLinhThuRuongBatbao(final Char p) {
        synchronized (Char.KHOA_MO_RUONG) {
        ++Char.total_open;
        System.out.println("check trung trung: nblucky: " + Char.numberLucky + ", nbrandom= " + Char.numberRandom + ", totalopen= " + Char.total_open);
        if (TeamServer.isServerKinhMon()) {
            if (Char.total_open == Char.numberRandom) {
                Database.instance.saveOrtherLog("", p.charname, "check trung trung: nblucky: " + Char.numberLucky + ", nbrandom= " + Char.numberRandom + ", totalopen= " + Char.total_open, "iftrung");
                if (Char.total_open <= 150) {
                    Char.numberRandom = 151 + Map.r.nextInt(300);
                } else if (Char.total_open <= 450) {
                    Char.numberRandom = 451 + Map.r.nextInt(182);
                }
                Database.instance.saveOrtherLog("", p.charname, "random lai so trung: nblucky: " + Char.numberLucky + ", nbrandom= " + Char.numberRandom + ", totalopen= " + Char.total_open, "iftrung");
                try {
                    Map.sendAllCharServer(-2, MessageCreator.createServerAlertAutoOffMessage("Chúc mừng " + p.charname + " đã trúng mặt nạ vĩnh viễn"));
                } catch (final IOException ex) {
                }
                return true;
            }
        } else if (TeamServer.isServerHoangMy()) {
            if (Char.total_open == Char.numberRandom) {
                Database.instance.saveOrtherLog("", p.charname, "check trung trung: nblucky: " + Char.numberLucky + ", nbrandom= " + Char.numberRandom + ", totalopen= " + Char.total_open, "iftrung");
                if (Char.total_open <= 150) {
                    Char.numberRandom = 151 + Map.r.nextInt(300);
                } else if (Char.total_open <= 450) {
                    Char.numberRandom = 451 + Map.r.nextInt(182);
                }
                Database.instance.saveOrtherLog("", p.charname, "random lai so trung: nblucky: " + Char.numberLucky + ", nbrandom= " + Char.numberRandom + ", totalopen= " + Char.total_open, "iftrung");
                try {
                    Map.sendAllCharServer(-2, MessageCreator.createServerAlertAutoOffMessage("Chúc mừng " + p.charname + " đã trúng mặt nạ vĩnh viễn"));
                } catch (final IOException ex2) {
                }
                return true;
            }
        } else if (TeamServer.isServerHoaLu()) {
            if (Char.total_open == Char.numberRandom) {
                Database.instance.saveOrtherLog("", p.charname, "check trung trung: nblucky: " + Char.numberLucky + ", nbrandom= " + Char.numberRandom + ", totalopen= " + Char.total_open, "iftrung");
                if (Char.total_open <= 150) {
                    Char.numberRandom = 151 + Map.r.nextInt(300);
                } else if (Char.total_open <= 450) {
                    Char.numberRandom = 451 + Map.r.nextInt(182);
                }
                Database.instance.saveOrtherLog("", p.charname, "random lai so trung: nblucky: " + Char.numberLucky + ", nbrandom= " + Char.numberRandom + ", totalopen= " + Char.total_open, "iftrung");
                try {
                    Map.sendAllCharServer(-2, MessageCreator.createServerAlertAutoOffMessage("Chúc mừng " + p.charname + " đã trúng mặt nạ vĩnh viễn"));
                } catch (final IOException ex3) {
                }
                return true;
            }
        } else if (TeamServer.isServerTrangTien()) {
            if (Char.total_open == Char.numberRandom) {
                Database.instance.saveOrtherLog("", p.charname, "check trung trung: nblucky: " + Char.numberLucky + ", nbrandom= " + Char.numberRandom + ", totalopen= " + Char.total_open, "iftrung");
                if (Char.total_open <= 150) {
                    Char.numberRandom = 151 + Map.r.nextInt(300);
                } else if (Char.total_open <= 450) {
                    Char.numberRandom = 451 + Map.r.nextInt(182);
                }
                Database.instance.saveOrtherLog("", p.charname, "random lai so trung: nblucky: " + Char.numberLucky + ", nbrandom= " + Char.numberRandom + ", totalopen= " + Char.total_open, "iftrung");
                try {
                    Map.sendAllCharServer(-2, MessageCreator.createServerAlertAutoOffMessage("Chúc mừng " + p.charname + " đã trúng mặt nạ vĩnh viễn"));
                } catch (final IOException ex4) {
                }
                return true;
            }
        } else if (TeamServer.isServerDaiLa()) {
            if (Char.total_open == Char.numberRandom) {
                Database.instance.saveOrtherLog("", p.charname, "check trung trung: nblucky: " + Char.numberLucky + ", nbrandom= " + Char.numberRandom + ", totalopen= " + Char.total_open, "iftrung");
                if (Char.total_open <= 150) {
                    Char.numberRandom = 151 + Map.r.nextInt(300);
                } else if (Char.total_open <= 450) {
                    Char.numberRandom = 451 + Map.r.nextInt(182);
                }
                Database.instance.saveOrtherLog("", p.charname, "random lai so trung: nblucky: " + Char.numberLucky + ", nbrandom= " + Char.numberRandom + ", totalopen= " + Char.total_open, "iftrung");
                try {
                    Map.sendAllCharServer(-2, MessageCreator.createServerAlertAutoOffMessage("Chúc mừng " + p.charname + " đã trúng mặt nạ vĩnh viễn"));
                } catch (final IOException ex5) {
                }
                return true;
            }
        } else if (TeamServer.isServerLocal()) {
            if (Char.total_open == Char.numberRandom) {
                Database.instance.saveOrtherLog("", p.charname, "check trung trung: nblucky: " + Char.numberLucky + ", nbrandom= " + Char.numberRandom + ", totalopen= " + Char.total_open, "iftrung");
                System.err.println("TRUNG MAT NA VV NE " + Char.numberRandom + " total open " + Char.total_open);
                if (Char.total_open <= 15) {
                    Char.numberRandom = 16 + Map.r.nextInt(20);
                } else if (Char.total_open <= 35) {
                    Char.numberRandom = 36 + Map.r.nextInt(30);
                }
                Database.instance.saveOrtherLog("", p.charname, "random lai so trung: nblucky: " + Char.numberLucky + ", nbrandom= " + Char.numberRandom + ", totalopen= " + Char.total_open, "iftrung");
                try {
                    Map.sendAllCharServer(-2, MessageCreator.createServerAlertAutoOffMessage("Chúc mừng " + p.charname + " đã trúng mặt nạ vĩnh viễn"));
                } catch (final IOException ex6) {
                }
                return true;
            }
        } else if (Char.total_open == Char.numberLucky || Char.total_open == Char.numberRandom || Char.total_open == 500 || Char.total_open == 1500 || Char.total_open == 3500) {
            Database.instance.saveOrtherLog("", p.charname, "check trung trung: nblucky: " + Char.numberLucky + ", nbrandom= " + Char.numberRandom + ", totalopen= " + Char.total_open, "iftrung");
            if (Char.total_open == 500) {
                Char.numberRandom = 501 + Map.r.nextInt(1000);
            } else if (Char.total_open == 1500) {
                Char.numberRandom = 1501 + Map.r.nextInt(2000);
            } else if (Char.total_open == 3500) {
                Char.numberRandom = 3501 + Map.r.nextInt(1500);
            }
            Database.instance.saveOrtherLog("", p.charname, "random lai so trung: nblucky: " + Char.numberLucky + ", nbrandom= " + Char.numberRandom + ", totalopen= " + Char.total_open, "iftrung");
            return false;
        }
        return false;
        }
    }

    static void startThreadXoso() {
        new Thread() {
            @Override
            public void run() {
                while (!AdminHandler.isStopServer) {
                    try {
                        Thread.sleep(900000L);
                        Map.sendAllCharServer(-1, MessageCreator.createServerAlertAutoOffMessage("Trò chơi dành cho người chơi trên 12 tuổi. Chơi quá 180 phút mỗi ngày sẽ hại sức khỏe"));
                    } catch (final Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        }.start();
        new Thread() {
            @Override
            public void run() {
                while (!AdminHandler.isStopServer) {
                    try {
                        if ((System.currentTimeMillis() - Char.timeXoso) / 1000L >= 120L) {
                            if (Char.ALL_PLAYER_XOSO.size() > 1) {
                                final int[] value = new int[Char.ALL_PLAYER_XOSO.size()];
                                final String[] namexoso = new String[Char.ALL_PLAYER_XOSO.size()];
                                final Vector<PlayerXoso> allplayer = new Vector<PlayerXoso>();
                                int i = 0;
                                for (final PlayerXoso pxs : Char.ALL_PLAYER_XOSO.values()) {
                                    value[i] = pxs.money;
                                    namexoso[i] = pxs.name;
                                    pxs.index = i;
                                    allplayer.add(pxs);
                                    ++i;
                                }
                                int tempindex;
                                int index;
                                Char pwin;
                                int[] value2;
                                PlayerXoso pxs2;
                                for (index = (tempindex = Char.getIndexRandomXoso(value)), pwin = CharManager.instance.getCharByCharName(namexoso[index]); pwin == null && allplayer.size() > 0; pwin = CharManager.instance.getCharByCharName(namexoso[index])) {
                                    allplayer.remove(tempindex);
                                    if (allplayer.size() == 0) {
                                        break;
                                    }
                                    value2 = new int[allplayer.size()];
                                    for (i = 0; i < allplayer.size(); ++i) {
                                        pxs2 = allplayer.get(i);
                                        value2[i] = pxs2.money;
                                    }
                                    tempindex = Char.getIndexRandomXoso(value2);
                                    if (tempindex == -1) {
                                        break;
                                    }
                                    index = allplayer.get(tempindex).index;
                                }
                                // Sua 20/09/2026 (theo yeu cau nguoi dung): thue CO DINH
                                // Char.TAX_PC_XOSO_XU, khong con scale theo so nguoi choi nhu truoc.
                                final int tax = Char.TAX_PC_XOSO_XU;
                                // Chup lai hu tien va DONG VONG NGAY, truoc khi phat thuong - xem
                                // chu thich day du o Char.startThreadXosoLow (ban thuong) cho cung
                                // loi: saveCharAuto co the nem loi tu doan dau ham khong boc
                                // try/catch, catch cua vong lap nuot loi ma khong dong vong, va
                                // 100ms sau lai addXu them mot lan nua cho cung nguoi thang.
                                final long huTien = Char.MONEY_XOSO;
                                final long moneyWin = huTien - huTien * tax / 100L;
                                final long moneytest = value[index];
                                final long tl = moneytest * 100L / huTien;
                                Char.MONEY_XOSO = 0L;
                                Char.timeXoso = System.currentTimeMillis();
                                Char.ALL_PLAYER_XOSO.clear();
                                String info = "Không tìm thấy người chiến thắng";
                                if (pwin != null) {
                                    info = "Chúc mừng " + namexoso[index].toUpperCase() + " đã chiến thắng " + Map.getDotNumber(moneyWin) + " vàng trong trò chơi Vòng xoay may mắn";
                                    if (tl <= 2L) {
                                        info = "Chúc mừng " + namexoso[index].toUpperCase() + " đặt " + Map.getDotNumber(moneytest) + " vàng và chiến thắng " + Map.getDotNumber(moneyWin) + " vàng trong trò chơi Vòng xoay may mắn";
                                    }
                                    Map.sendAllCharServer(-1, MessageCreator.createServerAlertAutoOffMessage(info));
                                    Database.instance.resetXoso(namexoso[index], value[index], moneyWin, huTien * tax / 100L);
                                    Char.charNameXosoWin = namexoso[index];
                                    Char.moneyCharXosowin = moneyWin;
                                    Char.moneyCharXosoBid = value[index];
                                    pwin.addXu(moneyWin, "vxmm vip");
                                    pwin.sendMessage(MessageCreator.createCharInventoryMessage(pwin, 0));
                                    Database.instance.addCharWinXoSo(pwin, moneyWin);
                                    Database.instance.saveCharAuto(pwin);
                                } else {
                                    Char.charNameXosoWin = "";
                                    Char.moneyCharXosowin = 0;
                                    Char.moneyCharXosoBid = 0;
                                    Database.instance.resetXoso("ko char win", value[index], moneyWin, huTien * tax / 100L);
                                }
                            } else {
                                Char.timeXoso = System.currentTimeMillis();
                            }
                        }
                    } catch (final Exception e) {
                        e.printStackTrace();
                    }
                    try {
                        Thread.sleep(100L);
                    } catch (final Exception ex) {
                    }
                }
            }
        }.start();
    }

}
