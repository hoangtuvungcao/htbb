package real;


import Constant.Checker;
import data.InfoQuestTemplate;
import java.util.Date;
import server.TeamServer;
import data.Database;
import io.Message;
import data.NewClan;
import java.io.IOException;

import data.TuBinhTemplate;
import data.FruitTemplate;
import data.ModelWearing;
import data.Animal;
import java.util.Vector;

/**
 * Than cua Char.initInfoFromDB va Char.initPinfo - nap toan bo du lieu nhan
 * vat tu CSDL luc dang nhap (chi so, trang bi, hanh trang, ky nang, bang hoi,
 * nhiem vu, thu cuoi, buff...).
 *
 * 1.602 dong nay truoc nam trong real/Char.java. Ca hai method VAN CON trong
 * Char, giu NGUYEN chu ky public cu, chi than chuyen sang day - nen bytecode
 * trong lib/htbb-core.jar va cac lop con (CharThanthu, CharCopy) khong bi anh
 * huong. Tham so "Char self" thay cho "this" truoc day.
 */
class CharNapDuLieu {

    static void initPinfo(Char self) {
        final byte[] he = {4, 2, 0, 3, 1};
        final short[] basic = {400, 400, 500, 350, 350};
        if (self.dbInfo != null) {
            final String[] data = Char.split(self.dbInfo, ",");
            try {
                self.headStyle = Byte.parseByte(data[0]);
                final byte gender = Byte.parseByte(data[1]);
                if (gender > 0) {
                    if (self.headStyle % 2 != 0 && gender == 1) {
                        self.gender = 2;
                    } else {
                        self.gender = gender;
                        self.isNewClient = true;
                    }
                } else if (self.headStyle % 2 == 0) {
                    self.gender = 1;
                } else {
                    self.gender = 2;
                }
                self.charClass = Byte.parseByte(data[3]);
                self.he = he[self.charClass];
                self.basic = basic[self.charClass];
                self.oldLv = 0;
                try {
                    self.oldLv = Integer.parseInt(data[20]);
                } catch (final Exception ex) {
                }
                final long xp = Long.parseLong(data[4]);
                if (self.oldLv != -5) {
                    self.lvDetail.setExp(xp, self.oldLv, self.charname, "char initPinfo");
                    self.oldLv = -5;
                } else {
                    self.lvDetail.setExpNew(xp);
                }
                self.lastLV = Short.parseShort(data[5]);
                if (self.lastLV < self.lvDetail.lv) {
                    self.lastLV = (short) self.lvDetail.lv;
                }
                // Pokemon (viec S2/S3): suy loai tu class + cap neu tob_char.dex
                // = 0, va dat he/he2 theo loai (thay ngu hanh he[charClass] o tren).
                TienHoaPokemon.napLoai(self);
                self.hp = Integer.parseInt(data[6]);
                if (self.hp <= 0) {
                    self.hp = 1;
                }
                self.mp = Integer.parseInt(data[7]);
                self.mapID = Short.parseShort(data[8]);
                self.mapID = Checker.resetMapId(self.mapID);
                self.x = Integer.parseInt(data[9]);
                self.y = Integer.parseInt(data[10]);
                self.killer = Short.parseShort(data[11]);
                if (self.killer >= 32000 || self.killer < 0) {
                    self.killer = 32000;
                }
                self.timeKiller = Long.parseLong(data[12]);
                self.nPKill = Short.parseShort(data[13]);
                self.fullGoldTime = Long.parseLong(data[14]);
                self.halfGoldTime = Long.parseLong(data[15]);
                self.isAdmin = (Integer.parseInt(data[16]) == 1);
            } catch (final Exception ex2) {
            }
            
            if (!self.charname.equals("admin") && !TeamServer.adminAccounts.contains(self.charname.toLowerCase()) && self.isAdmin)  self.isAdmin = false;  // huy admin neu ten khong nam trong sv.adminAccounts
            
            try {
                self.config(self.typeConfig = Byte.parseByte(data[17]));
            } catch (final Exception ex3) {
            }
            try {
                try {
                    self.autoGetItem = Byte.parseByte(data[18]);
                    self.autoSkill = Byte.parseByte(data[19]);
                    self.autoSkill = 1;
                    self.autoGetItem = 1;
                    self.rideHorse = Byte.parseByte(data[22]);
                    final byte mWearing = Byte.parseByte(data[23]);
                    if (mWearing > -1 && mWearing != 21 && mWearing != 22) {
                    }
                } catch (final Exception ex4) {
                }
                try {
                    self.pointCongHienClan = Integer.parseInt(data[24]);
                    self.isOnlineToDay = (Byte.parseByte(data[25]) == 1);
                    self.rcvGifByLv = Byte.parseByte(data[26]);
                    self.rcvGifByHour = Byte.parseByte(data[27]);
                    self.rcvGifByMonth = Byte.parseByte(data[28]);
                    self.totalDayInMonth = Byte.parseByte(data[29]);
                    self.dateStartOnline = data[32];
                    self.lvHouse = Byte.parseByte(data[33]);
                    self.lvStore = Byte.parseByte(data[34]);
                    self.maxBag = Byte.parseByte(data[35]);
                    self.slotWearing = Byte.parseByte(data[36]);
                    self.nMoonCake2Exp = Short.parseShort(data[37]);
                    self.receiveLG = Byte.parseByte(data[38]);
                    self.myCountry = Byte.parseByte(data[39]);
                } catch (final Exception ex5) {
                }
                try {
                    self.setCountry();
                    if (self.myCountry >= 0) {
                        self.inCountry = self.myCountry;
                        self.session.sendMessage(MessageCreator.createMessageLocation(self.myCountry));
                    }
                } catch (final Exception ex6) {
                }
                try {
                    self.gif35 = Byte.parseByte(data[40]);
                } catch (final Exception ex7) {
                }
                try {
                    self.luongNap = Long.parseLong(data[41]);
                    self.luonglost = Long.parseLong(data[42]);
                    self.dayLogin = data[43].trim();
                    self.forbitChat = Byte.parseByte(data[44]);
                    self.usingSummons = Byte.parseByte(data[45]);
                    self.maxKill = Short.parseShort(data[46]);
                    self.honor = Integer.parseInt(data[47]);
                    self.xichtho_thienlyma = Byte.parseByte(data[48]);
                    self.married = Byte.parseByte(data[49]);
                    self.idWedding = Integer.parseInt(data[50]);
                    self.nameHusband_wife = data[51];
                    self.rcvGiftWedding = Byte.parseByte(data[52]);
                    self.passWord = data[53];
                    self.countQuestClan = Byte.parseByte(data[54]);
                    self.pointActiveQuest = Short.parseShort(data[55]);
                    self.idArena = Integer.parseInt(data[56]);
                    self.pArena = Integer.parseInt(data[57]);
                    self.idMainQuest = Short.parseShort(data[58]);
                    self.nBinhTra = Byte.parseByte(data[59]);
                    self.nHopMut = Byte.parseByte(data[60]);
                    self.gif83 = Byte.parseByte(data[61]);
                    self.ntangtoc = Byte.parseByte(data[62]);
                    self.subpk = Byte.parseByte(data[63]);
                    self.receiveQuestVulan = Byte.parseByte(data[64]);
                    self.timeUp5Attribute = Long.parseLong(data[65]);
                    self.typeTieu = Byte.parseByte(data[66]);
                    self.timeOutGame = Long.parseLong(data[67]);
                    self.region = Short.parseShort(data[68]);
                    self.lockLuong = Integer.parseInt(data[69]);
                    self.minuteExp = Integer.parseInt(data[70]);
                    if (self.minuteExp > 0) {
                        self.fullGoldTime = System.currentTimeMillis();
                        self.x2 = 2;
                    }
                    self.luot_van_tieu = Byte.parseByte(data[71]);
//                    if (self.luot_van_tieu >= 3) {
//                        self.luot_van_tieu = 0;
//                    }
                    self.buy_luot_van_tieu = Byte.parseByte(data[72]);
//                    if (self.buy_luot_van_tieu >= 1) {
//                        self.buy_luot_van_tieu = 0;
//                    }
                    self.cuop_tieu = Byte.parseByte(data[73]);
                    self.timeJoinClan = Long.parseLong(data[74]);
                    self.giveMoneyClan = Byte.parseByte(data[75]);
                    self.minuteSocola = Short.parseShort(data[76]);
                    self.receiveGiftEvent = Byte.parseByte(data[77]);
                    self.idTongKim = Short.parseShort(data[78]);
                    self.idTeamTongKim = Byte.parseByte(data[79]);
                    self.diemdoivePH = Integer.parseInt(data[80]);
                    self.winPH = Byte.parseByte(data[81]);
                    self.timeExistCharHire = Long.parseLong(data[82]);
                    self.classlinh = Byte.parseByte(data[83]);
                    self.lvlinhthue = Byte.parseByte(data[84]);
                    self.genderlinh = Byte.parseByte(data[85]);
                    self.lvlinh = Byte.parseByte(data[86]);
                    self.countnvVulan = Byte.parseByte(data[87]);
                    self.firstNapMoney = Byte.parseByte(data[88]);
                    self.timeUseTheMuaBan = Long.parseLong(data[89]);
                    self.banphaohoa = Integer.parseInt(data[90]);
                    self.idTongKim = -1;
                    self.x2Phaohoa = Integer.parseInt(data[91]);
                    self.dayCamChat = data[92].trim();
                    self.countTime = Byte.parseByte(data[93]);
                    self.receiveGiftOldChar = Byte.parseByte(data[94]);
                    self.buychoi = Byte.parseByte(data[95]);
                    self.buylongden = Byte.parseByte(data[96]);
                    self.xuxai = Long.parseLong(data[97]);
                    self.luongxai = Long.parseLong(data[98]);
                    self.vetangluong = Integer.parseInt(data[99]);
                    self.countMonsterKill = Integer.parseInt(data[100]);
                    self.hlt_buy = Short.parseShort(data[101]);
                    self.luongxainhanRuong = Integer.parseInt(data[102]);
                    self.idEffDanhHieu = Integer.parseInt(data[103]);
                    self.minuteOnline = Short.parseShort(data[104]);
                    self.timeInClan = Long.parseLong(data[105]);
                    self.nhomThidau = Byte.parseByte(data[106]);
                    self.vip = Byte.parseByte(data[107]);
                    self.diemNapVip = Integer.parseInt(data[108]);
                    self.diemxaiVip = Integer.parseInt(data[109]);
                    self.rcvInviteVip = (Integer.parseInt(data[110]) == 1);
                    self.isCaiTrang = (Integer.parseInt(data[111]));
                } catch (final Exception ex8) {
                }
                try {
                    self.diemDoiThuong = Integer.parseInt(data[112]);
                    self.diemQuaiHomNay = Integer.parseInt(data[113]);
                    self.soQuaiHomNay = Integer.parseInt(data[114]);
                } catch (final Exception ex8b) {
                }
                try {
                    self.timeAoHoaHoaKyLan = Long.parseLong(data[115]);
                } catch (final Exception ex8c) {
                }
                try {
                    self.thuongLv35 = Byte.parseByte(data[116]);
                } catch (final Exception ex8d) {
                }
                try {
                    self.isNghienAuto = (Integer.parseInt(data[117]) == 1);
                    self.isAutoBan = (Integer.parseInt(data[118]) == 1);
                } catch (final Exception ex8e) {
                }
                try {
                    // Trung Thu 2026 (moi 19/09/2026) - "diem tieu xai", xem khai bao field.
                    self.luongDaXaiTrungThu = Long.parseLong(data[119]);
                } catch (final Exception ex8f) {
                }
                try {
                    // Bai luyen cong (moi 22/09/2026) - cong tac "Auto Ve Map Up".
                    self.isAutoVeMapUp = (Integer.parseInt(data[120]) == 1);
                } catch (final Exception ex8g) {
                }
                try {
                    self.idAutoSkill = Byte.parseByte(data[30]);
                    self.idAutoGetitem = Byte.parseByte(data[31]);
                } catch (final Exception ex9) {
                }
                try {
                    self.autoComeHome = Byte.parseByte(data[21]);
                } catch (final Exception ex10) {
                }
                if (self.autoComeHome > 1 || self.autoComeHome < 0) {
                    self.autoComeHome = 0;
                }
                if (TeamServer.server == 100) {
                    self.passWord = "";
                }
                if (self.minuteSocola > 0) {
                    try {
                        self.sendMessage(MessageCreator.createServerAlertAutoOffMessage("Thời gian sử dụng hạt mầm của bạn còn lại " + self.minuteSocola + " phút"));
                    } catch (final IOException ex11) {
                    }
                }
            } catch (final Exception ex12) {
            }
        }
    }

    static void initInfoFromDB(Char self, final Vector<Item> charItem, final Vector<Date> lastlog) {
        try {
            self.timeOnlineSukien = System.currentTimeMillis();
            self.timeSendAlertQuangCao = System.currentTimeMillis() + 15000L;
            self.initWItem();
            String info = "";
            initPinfo(self);
            self.lastLog = Char.split(self.lastLog.trim(), ".")[0].trim();
            try {
                self.initOnlineReceiveGif(lastlog.get(0));
            } catch (final Exception ex) {
            }
            if (self.lastLog.compareTo("2010-01-01 00:00:00") == 0) {
                self.receiveGiftOldChar = 0;
            }
            if (self.idArena > -1 && ArenaMap.getArenaData(self) == null) {
                self.idArena = -1;
            }
            try {
                if (self.receiveLG != 64) {
                    self.char_quest = Database.instance.loadCharQuest(self.charDBID);
                }
                self.receiveLG = 64;
                if (self.char_quest != null) {
                    self.idMainQuest = (short) self.char_quest.id_quest;
                    if (self.idMainQuest >= Map.allMainQuest.size()) {
                        self.idMainQuest = (short) Map.allMainQuest.size();
                    }
                    final String itemget = new StringBuilder(String.valueOf(self.char_quest.nItem[0])).toString();
                    final String monskilled = new StringBuilder(String.valueOf(self.char_quest.nMonster[0])).toString();
                    final int finish = self.char_quest.finish;
                    final int state = self.char_quest.receive;
                    final String stateQ = String.valueOf(self.char_quest.id_quest) + "," + ((finish == 1) ? finish : ((state == 1) ? 2 : 0)) + ",0";
                    self.initQuest(itemget, monskilled, stateQ);
                    if (self.mainQuest.id_quest >= Map.allMainQuest.size()) {
                        self.mainQuest.id_quest = (short) (Map.allMainQuest.size() - 1);
                        self.mainQuest.state_quest = NewCharQuest.DONE;
                    }
                    Database.instance.addCharQuest(self.charDBID, self.getInfoQuestSave());
                    final InfoQuestTemplate infoQuest = self.mainQuest.getTemplate();
                    final int iditem = infoQuest.getItemGet()[0];
                    if (iditem < 32000) {
                        self.itemquest[iditem] = self.char_quest.nItem[0];
                    }
                    Database.instance.isCheckCharQuest(self.charDBID);
                } else {
                    if (!Map.openLog && self.lastLV <= 2 && self.idMainQuest > 8 && TeamServer.server == 4) {
                        self.idMainQuest = 0;
                        self.itemget = "";
                    }
                    self.initQuest(self.itemget, self.monskilled, self.finish);
                    if (self.itemget.equals("") && self.itemget.equals("") && self.itemget.equals("")) {
                        Database.instance.addCharQuest(self.charDBID, self.getInfoQuestSave());
                    }
                    final String[] data = Char.split(self.itquest, ",");
                    for (int i = 0; i < data.length; ++i) {
                        self.itemquest[i] = Short.parseShort(data[i]);
                    }
                }
            } catch (final Exception ex2) {
            }
            // GD2: nap ho so dai han + chuyen nhiem vu chinh sang chuoi moi (tat co: khong lam gi).
            LauDaiNhanVat.khiNap(self);
            NhiemVuLauDaiGame.khiNapTatCo(self); // tat co sau khi da bat: tra nhiem vu chinh ve cho cu
            self.itemget = "";
            self.monskilled = "";
            self.finish = "";
            self.itquest = "";
            try {
                if (self.dayLogin.compareTo("2013-05-10") < 0) {
                    try {
                        if (self.animalRide != null) {
                            self.rideHorse = Animal.rideHorse[self.animalRide.idImg];
                        }
                    } catch (final Exception ex3) {
                    }
                    try {
                        final String infoNl = "";
                        for (int i = 0; i < self.listGemitemLock.length; ++i) {
                            if (self.listGemitemLock[i] > 0) {
                                info = String.valueOf(info) + self.listGemitem[i] + " _ " + self.listGemitemLock[i] + " > ";
                                final int[] listGemitem = self.listGemitem;
                                final int n = i;
                                listGemitem[n] -= self.listGemitemLock[i];
                                if (self.listGemitem[i] < 0) {
                                    self.listGemitem[i] = 0;
                                }
                            }
                        }
                        if (!infoNl.equals("")) {
                            Database.instance.saveOrtherLog("", self.charname, infoNl, "tachnl");
                        }
                    } catch (final Exception e) {
                        e.printStackTrace();
                    }
                }
                if (self.rideHorse <= 0) {
                    if (self.animalRide != null) {
                        self.animalRide.place = 0;
                        self.animalRide = null;
                    }
                } else {
                    if (self.animalRide != null && Animal.rideHorse[self.animalRide.idImg] != self.rideHorse) {
                        self.animalRide.place = 0;
                        self.animalRide = null;
                        self.rideHorse = 0;
                    }
                    if (self.animalRide != null) {
                        self.idImgHorse = Animal.idImgPaint[self.animalRide.idImg];
                    } else if (self.rideHorse == 1) {
                        if (self.xichtho_thienlyma == 1) {
                            self.idImgHorse = 0;
                        } else if (self.xichtho_thienlyma == 2) {
                            self.idImgHorse = 1;
                        }
                    }
                }
            } catch (final Exception ex4) {
                ex4.printStackTrace();
            }
            if (self.xu < 0L) {
                self.xu = 0L;
            }
            if (TeamServer.isServerLienDau()) {
                self.isAdmin = false;
            }
            if (self.isAdmin && Map.isAcountAdmin(self.getSession().username)) {
                self.isAdmin = false;
            }
            // TEST: cấp quyền admin cho các tài khoản cấu hình trong sv.adminAccounts
            if (TeamServer.adminAccounts.contains(self.charname.toLowerCase())) {
                // SAN SANG PRODUCTION: sv.gmCanCoPInfo=1 -> KHONG cap theo ten nua, giu ket qua
                // tinh tu pInfo[16]=1 VA ten trong danh sach (o tren). 0 = nhu cu (chi can ten).
                if (!TeamServer.gmCanCoPInfo) {
                    self.isAdmin = true;
                }
            }
            // todo sửa map player ở đây
            if (self.lvDetail.lv > 1 && (self.mapID == -1 || self.mapID == 105 || (self.mapID >= 30 && self.mapID <= 39))) {
                self.mapID = 0;
            }

            self.mapID = Checker.resetMapId(self.mapID);

            if (self.mapID == -1 || self.mapID == 105) {
                self.mapID = 105;
                self.x = 176;
                self.y = 272;
            } else {
                final int[][] mapstart = {{70, 1701}, {0, 301}, {80, 1901}};
                if (self.myCountry > -1) {
                    if (self.mapID == 228 || self.mapID == 227 || self.mapID == 42 || self.mapID == 41 || self.mapID == 40 || self.mapID == 17 || self.mapID == 25 || self.mapID == 111 || self.mapID == 118 || self.mapID == 201 || self.mapID == 202 || self.mapID == 203 || self.mapID == 204 || self.mapID == 205 || self.mapID == 207 || self.mapID == 225 || self.mapID == 226 || RealController.mapList.get(self.mapID) == null || self.mapID == 0 || self.mapID == 301 || self.mapID == 302 || self.mapID == 303 || self.mapID == 304 || self.mapID == 206 || self.mapID == 208 || self.mapID == 209 || self.mapID == 210 || self.mapID == 70 || self.mapID == 1701 || self.mapID == 1702 || self.mapID == 1703 || self.mapID == 1704 || self.mapID == 203 || self.mapID == 4361 || self.mapID == 4362 || self.mapID == 4363 || self.mapID == 4364 || self.mapID == 4365 || self.mapID == 4366) {
                        self.mapID = mapstart[self.myCountry][Map.r.nextInt(mapstart[self.myCountry].length)];
                        final int[][] pos = {{10, 23, 14, 38, 30, 35, 21, 49}, {22, 41, 27, 32, 8, 30, 18, 11}, {10, 23, 14, 38, 30, 35, 21, 49}};
                        final int index = Map.r.nextInt(4);
                        self.x = pos[self.myCountry][index * 2] * 16 + Map.r.nextInt() % 16;
                        self.y = pos[self.myCountry][index * 2 + 1] * 16 + Map.r.nextInt() % 16;
                        self.region = 0;
                    }
                } else {
                    final int[] mapstartDun = {117, 2641};
                    final int ran = Map.r.nextInt(mapstartDun.length);
                    self.mapID = mapstartDun[ran];
                    self.x = 624 + Database.r.nextInt() % 5;
                    self.y = 512 + Database.r.nextInt() % 5;
                }
            }
            int count = 0;
            if (self.dbSkill != null) {
                final String[] data2 = Char.split(self.dbSkill, ",");
                for (int j = 0; j < data2.length; ++j) {
                    try {
                        if (j < 15) {
                            self.skill[j] = Byte.parseByte(data2[j]);
                            count += self.skill[j];
                        } else if (j >= 15 && j < 35) {
                            self.skillClan[j - 15] = Short.parseShort(data2[j]);
                        } else {
                            self.skillLvClan[j - 35] = Byte.parseByte(data2[j]);
                        }
                    } catch (final Exception e2) {
                        e2.printStackTrace();
                    }
                }
                // Game goc mo chieu 0 qua nhiem vu cu so 8 (Char.onSendGiftQuest) hoac khi vao lai
                // tu cap 3. Chuoi nhiem vu moi (sv.lau_dai, data_quest_moi 901+) khong co nhiem vu 8
                // ma nhiem vu thu 3 (903) da bat ha quai o cap 2 -> nhan vat moi khong danh duoc gi
                // (Region/MapDanhNhau.doAttackMonster bo qua khi cap chieu = 0). BotChucNang
                // danh_quai bat duoc 18/10/2026. Mo chieu 0 ngay tu dau khi bat co.
                if ((self.lvDetail.lv >= 3 || com.htbb.pokemon.LauDai.bat()) && self.skill[0] == 0) {
                    self.skill[0] = 1;
                }
            }
            if (self.killer > 0) {
                self.isKiller = true;
            }
            final NewClan c = Map.getClaninfoByID(self.idClan);
            if (c != null) {
                if (c != null) {
                    for (int j = 0; j < c.timeUseSkill.length; ++j) {
                        self.skillClan[j] = c.skillClan[j];
                        self.timeUseSkillClan[j] = c.timeUseSkill[j];
                        self.skillLvClan[j] = c.skillLvClan[j];
                    }
                }
                if (!c.master.toLowerCase().equals(self.charname.toLowerCase())) {
                    if (self.rankClan <= 0) {
                        self.rankClan = 3;
                    }
                } else {
                    self.rankClan = 0;
                    if (Map.idClanTown[self.myCountry] == self.idClan) {
                        Char.gov[self.myCountry].king = self.charname;
                        Database.instance.saveEvent(Map.event.getInfo());
                    }
                }
            } else {
                self.rankClan = 3;
                self.idClan = -1;
            }
            if (self.dbPotion != null) {
                final String[] data3 = Char.split(self.dbPotion, ",");
                for (int k = 0; k < data3.length; ++k) {
                    try {
                        if (k == 0) {
                            if (self.xu < 0L) {
                                self.xu = 0L;
                            }
                        } else {
                            self.potions[k] = Integer.parseInt(data3[k]);
                            if (self.potions[k] < 0) {
                                self.potions[k] = 0;
                            }
                        }
                    } catch (final Exception ex5) {
                    }
                }
                self.potions[36] = 0;
                self.potions[85] = 0;
                self.potions[87] = 0;
                self.potions[62] = 0;
            }
            if (self.married == 0 && self.idWedding == -1) {
                self.potions[92] = 0;
                self.potions[88] = 0;
                self.potions[89] = 0;
                self.potions[90] = 0;
                self.potions[99] = 0;
                self.potions[98] = 0;
            }
            if (self.dayLogin.compareTo("2020-07-02") < 0 || self.dayLogin.equals("")) {
                self.potions[70] = 0;
            }
            if (Char.getDayOpen(0L).equals("2023-07-03") && self.dayLogin.compareTo("2023-07-03") < 0) {
                if (!Map.openLog) {
                    self.doAddGiftSn();
                }
            } else if (Char.getDayOpen(0L).equals("2023-07-04") && self.dayLogin.compareTo("2023-07-04") < 0) {
                if (!Map.openLog) {
                    self.doAddGiftSn();
                }
            } else if (Char.getDayOpen(0L).equals("2023-07-05") && self.dayLogin.compareTo("2023-07-05") < 0 && !Map.openLog) {
                self.doAddGiftSn();
            }
            if (self.subpk == 0) {
                self.killer -= 200;
                if (self.killer < 0) {
                    self.killer = 0;
                    self.isKiller = false;
                }
                self.subpk = 1;
            }
            if (self.pointActiveQuest > 300) {
                self.pointActiveQuest = 300;
            }
            if (self.nMoonCake2Exp != 15503) {
                self.luongNap = 0L;
                self.luonglost = 0L;
                self.flower = 0;
                self.nMoonCake2Exp = 15503;
                self.potions[75] = 0;
                self.potions[120] = 0;
                self.potions[121] = 0;
                self.potions[122] = 0;
                self.potions[117] = 0;
                if (self.lvDetail.lv >= 34) {
                    self.potions[7] = 6;
                    self.ntangtoc = 6;
                }
            }
            if (self.gif83 != 14) {
                self.luongNap = 0L;
                self.luonglost = 0L;
                self.gif83 = 14;
                self.potions[80] = 0;
                self.potions[119] = 0;
                self.potions[117] = 0;
                self.potions[75] = 0;
                self.potions[35] = 0;
                self.potions[82] = 0;
                self.potions[97] = 0;
                self.potions[116] = 0;
            }
            if (self.lastLog.compareTo("2015-03-07 16:25:00") < 0) {
                self.flower = 0;
            }
            if (self.lastLog.compareTo("2014-12-24 16:30:00") < 0) {
                self.potions[117] = 0;
            }
            if (self.lastLog.compareTo("2014-12-24 16:30:00") < 0) {
                self.potions[117] = 0;
            }
            if (self.lastLog.compareTo("2015-08-26 10:30:00") < 0) {
                self.potions[118] = 0;
                self.luongNap = 0L;
            }
            if (self.lastLog.compareTo("2015-10-30 17:10:00") < 0) {
                self.potions[72] = 0;
                self.potions[71] = 0;
                self.potions[74] = 0;
                self.potions[77] = 0;
                self.potions[120] = 0;
                self.potions[83] = 0;
                self.potions[118] = 0;
                self.potions[121] = 0;
                self.potions[125] = 0;
                self.potions[122] = 0;
                self.potions[117] = 0;
                self.potions[105] = 0;
                self.potions[119] = 0;
                self.potions[124] = 0;
                self.potions[104] = 0;
                self.luongNap = 0L;
            }
            if (self.lastLog.compareTo("2016-08-09 10:10:00") < 0) {
                self.potions[72] = 0;
                self.potions[71] = 0;
                self.potions[74] = 0;
                self.potions[77] = 0;
                self.potions[120] = 0;
                self.potions[121] = 0;
            }
            if (self.lastLog.compareTo("2016-08-10 11:05:00") < 0) {
                final int[] potions = self.potions;
                final int n2 = 120;
                potions[n2] += self.potions[125];
                self.potions[125] = 0;
            }
            if (self.lastLog.compareTo("2016-04-15 10:10:00") < 0) {
                self.potions[72] = 0;
                self.potions[71] = 0;
                self.potions[74] = 0;
                self.potions[77] = 0;
                self.potions[120] = 0;
                self.potions[83] = 0;
                self.potions[104] = 0;
                self.potions[125] = 0;
                self.potions[105] = 0;
                self.potions[119] = 0;
                self.luongNap = 0L;
                self.nBinhTra = 20;
            }
            if (self.lastLog.compareTo("2016-02-04 16:22:00") < 0) {
                self.potions[74] = 0;
                self.potions[120] = 0;
                self.potions[83] = 0;
                self.potions[121] = 0;
                self.potions[75] = 0;
                self.potions[72] = 0;
                self.potions[71] = 0;
                self.potions[77] = 0;
                self.potions[105] = 0;
                self.potions[119] = 0;
                self.potions[125] = 0;
                self.potions[104] = 0;
                self.potions[122] = 0;
                self.potions[117] = 0;
                self.potions[124] = 0;
                self.potions[81] = 0;
                self.luongNap = 0L;
            }
            if (self.lastLog.compareTo("2015-12-19 10:15:00") < 0) {
                self.potions[71] = 0;
                self.potions[74] = 0;
                self.potions[77] = 0;
                self.potions[120] = 0;
                self.potions[83] = 0;
                self.potions[121] = 0;
                self.potions[125] = 0;
                self.potions[105] = 0;
                self.potions[119] = 0;
                self.nBinhTra = 10;
                self.luongNap = 0L;
            }
            if (self.lastLog.compareTo("2015-09-26 10:15:00") < 0) {
                self.potions[72] = 0;
                self.potions[71] = 0;
                self.potions[74] = 0;
                self.potions[77] = 0;
                self.potions[120] = 0;
                self.potions[83] = 0;
                self.potions[125] = 0;
                self.potions[122] = 0;
                self.potions[117] = 0;
                self.potions[105] = 0;
                self.potions[119] = 0;
                self.potions[124] = 0;
                self.potions[104] = 0;
                self.potions[121] = 0;
                self.flower = 0;
                self.luongNap = 0L;
            }
            if (self.lastLog.compareTo("2015-04-23 16:05:00") < 0) {
                self.potions[121] = 0;
                self.potions[122] = 0;
                self.potions[119] = 0;
                self.potions[125] = 0;
                self.potions[117] = 0;
                self.potions[120] = 0;
                self.luongNap = 0L;
            }
            if (self.lastLog.compareTo("2016-09-09 16:05:00") < 0) {
                self.potions[72] = 0;
                self.potions[71] = 0;
                self.potions[74] = 0;
                self.potions[77] = 0;
                self.potions[120] = 0;
                self.potions[83] = 0;
                self.potions[104] = 0;
                self.potions[117] = 0;
                self.potions[105] = 0;
                self.potions[119] = 0;
                self.potions[124] = 0;
                self.potions[122] = 0;
                self.potions[142] = 0;
                self.luongNap = 0L;
                self.nBinhTra = 20;
            }
            if (self.lastLog.compareTo("2017-09-05 14:00:00") < 0 && !Map.openLog && TeamServer.server == 3) {
                try {
                    for (int l = 0; l < Char.nameTop100DaiViet.length; ++l) {
                        if (self.charname.toLowerCase().equals(Char.nameTop100DaiViet[l].toLowerCase())) {
                            self.doAddSpecialItem(0, 3);
                            final int slsc = 2;
                            final int slcc = 2;
                            for (int m = 0; m < GemTemplate.ID_MATERIAL_LOW[5].length; ++m) {
                                self.doAddGemItem(GemTemplate.ID_MATERIAL_LOW[5][m], slsc, false);
                                self.doAddGemItem(GemTemplate.ID_MATERIAL_HIGHT[5][m], slcc, false);
                            }
                            Database.instance.saveOrtherLog("", self.charname, "nhan qua top 100 3 ve gio vang, nlcc,sc moi loai 2 vien", "top100");
                            break;
                        }
                    }
                } catch (final Exception ex6) {
                }
            }
            if (self.lastLog.compareTo("2016-10-27 16:15:00") < 0) {
                self.potions[71] = 0;
                self.potions[72] = 0;
                self.potions[73] = 0;
                self.potions[120] = 0;
                self.potions[77] = 0;
                self.potions[83] = 0;
                self.potions[105] = 0;
                self.potions[104] = 0;
                self.potions[142] = 0;
                self.potions[119] = 0;
                self.potions[117] = 0;
                self.potions[122] = 0;
                self.potions[124] = 0;
                self.potions[74] = 0;
                self.potions[125] = 0;
                self.potions[121] = 0;
                self.luongNap = 0L;
            }
            if (self.lastLog.compareTo("2016-12-22 15:15:00") < 0) {
                self.potions[71] = 0;
                self.potions[72] = 0;
                self.potions[73] = 0;
                self.potions[74] = 0;
                self.potions[77] = 0;
                self.potions[83] = 0;
                self.potions[105] = 0;
                self.potions[104] = 0;
                self.potions[142] = 0;
                self.potions[119] = 0;
                self.potions[117] = 0;
                self.potions[125] = 0;
                self.nBinhTra = 10;
                final int[] id = {72, 73, 74};
                self.nHopMut = (byte) id[Map.r.nextInt(id.length)];
                self.luongNap = 0L;
            }
            if (self.lastLog.compareTo("2017-01-12 15:15:00") < 0) {
                self.potions[71] = 0;
            }
            if (self.lastLog.compareTo("2017-01-24 15:15:00") < 0) {
                self.potions[72] = 0;
                self.potions[73] = 0;
                self.potions[74] = 0;
                self.potions[77] = 0;
                self.potions[105] = 0;
                self.potions[104] = 0;
                self.potions[83] = 0;
                self.potions[142] = 0;
                self.potions[119] = 0;
                self.potions[117] = 0;
                self.potions[125] = 0;
                self.potions[120] = 0;
                self.potions[121] = 0;
                self.banphaohoa = 0;
            }
            if (self.lastLog.compareTo("2017-04-29 10:15:00") < 0) {
                self.potions[72] = 0;
                self.potions[73] = 0;
                self.potions[74] = 0;
                self.potions[77] = 0;
                self.potions[105] = 0;
                self.potions[104] = 0;
                self.potions[83] = 0;
                self.potions[142] = 0;
                self.potions[119] = 0;
                self.potions[117] = 0;
                self.potions[125] = 0;
                self.potions[120] = 0;
                self.potions[121] = 0;
                self.receiveQuestVulan = 0;
                self.countnvVulan = 0;
            }
            if (self.lastLog.compareTo("2017-10-03 15:10:00") < 0) {
                self.potions[72] = 0;
                self.potions[73] = 0;
                self.potions[74] = 0;
                self.potions[77] = 0;
                self.potions[105] = 0;
                self.potions[104] = 0;
                self.potions[83] = 0;
                self.potions[142] = 0;
                self.potions[119] = 0;
                self.potions[117] = 0;
                self.potions[122] = 0;
                self.potions[120] = 0;
                self.potions[121] = 0;
                self.potions[124] = 0;
                self.banphaohoa = 0;
            }
            if (self.lastLog.compareTo("2017-12-20 15:05:00") < 0) {
                self.potions[72] = 0;
                self.potions[73] = 0;
                self.potions[74] = 0;
                self.potions[77] = 0;
                self.potions[83] = 0;
                self.potions[105] = 0;
                self.potions[104] = 0;
                self.potions[142] = 0;
                self.potions[119] = 0;
                self.potions[117] = 0;
                self.potions[122] = 0;
                self.potions[120] = 0;
                self.potions[121] = 0;
                self.potions[124] = 0;
                self.banphaohoa = 0;
                self.nBinhTra = 10;
                self.luongNap = 0L;
                final int[] id = {73, 77};
                self.nHopMut = (byte) id[Map.r.nextInt(id.length)];
            }
            if (Char.isSuKienNoel() && self.nHopMut == -1) {
                final int[] id = {73, 77};
                self.nHopMut = (byte) id[Map.r.nextInt(id.length)];
            }
            if (self.lastLog.compareTo("2018-09-21 16:30:00") < 0) {
                self.potions[72] = 0;
                self.potions[73] = 0;
                self.potions[74] = 0;
                self.potions[77] = 0;
                self.potions[105] = 0;
                self.potions[142] = 0;
                self.potions[119] = 0;
                self.potions[117] = 0;
                self.potions[122] = 0;
                self.potions[120] = 0;
                self.potions[121] = 0;
                self.potions[124] = 0;
                self.potions[125] = 0;
                self.banphaohoa = 0;
                self.nBinhTra = 0;
                self.luongNap = 0L;
                self.nHopMut = 20;
                self.receiveGiftEvent = 0;
            }
            if (self.lastLog.compareTo("2018-02-09 16:00:00") < 0) {
                if (self.banphaohoa >= 10 || self.potions[105] > 10 || self.potions[142] > 100 || self.potions[119] > 100) {
                    Database.instance.saveOrtherLog("", self.charname, "tich thu " + self.banphaohoa + ",phao: " + self.potions[105] + ", thuoc: " + self.potions[119] + ", giay: " + self.potions[142], "tichthu");
                    self.potions[72] = 0;
                    self.potions[73] = 0;
                    self.potions[74] = 0;
                    self.potions[77] = 0;
                    self.potions[105] = 0;
                    self.potions[142] = 0;
                    self.potions[119] = 0;
                    self.potions[117] = 0;
                    self.potions[122] = 0;
                    self.potions[120] = 0;
                    self.banphaohoa = 0;
                    self.nBinhTra = 10;
                    self.luongNap = 0L;
                }
                self.potions[104] = 0;
                self.potions[83] = 0;
                self.potions[121] = 0;
                self.potions[124] = 0;
                if (self.potions[117] > 100 || self.potions[122] > 100 || self.potions[120] > 100) {
                    self.potions[119] = 0;
                    self.potions[117] = 0;
                    self.potions[122] = 0;
                }
                if (self.potions[121] > 10) {
                    self.potions[121] = 0;
                }
            }
            if (self.lastLog.compareTo("2018-02-12 10:14:00") < 0) {
                self.countTime = 0;
            }
            if (self.lastLog.compareTo("2018-05-30 14:26:00") < 0) {
                self.potions[72] = 0;
                self.potions[73] = 0;
                self.potions[74] = 0;
                self.potions[77] = 0;
                self.potions[104] = 0;
                self.potions[83] = 0;
                self.receiveQuestVulan = 0;
                self.countnvVulan = 0;
            }
            if (self.lastLog.compareTo("2018-07-02 15:15:00") < 0) {
                self.potions[72] = 0;
                self.potions[73] = 0;
                self.banphaohoa = 0;
            }
            if (self.lastLog.compareTo("2019-10-30 14:10:00") < 0) {
                self.potions[72] = 0;
                self.potions[73] = 0;
                self.potions[74] = 0;
                self.potions[77] = 0;
                self.potions[105] = 0;
                self.potions[142] = 0;
                self.winPH = 0;
            }
            if (Map.ALL_CHAR_VIP.get(self.charname) == null) {
                self.vip = 0;
                self.rcvInviteVip = true;
            } else {
                final int a = Map.ALL_CHAR_VIP.get(self.charname);
                self.vip = (byte) a;
            }
            if (!self.dayLogin.equals(Char.getDayOpen(0L))) {
                self.countnvVulan = 0;
                self.usingSummons = 0;
                self.maxKill = 0;
                self.countQuestClan = 0;
                self.countMonsterKill = 0;
                self.nHopMut = 100;
                self.nBinhTra = 100;
                self.timeXP = 0;
                self.pointActiveQuest += 100;
                self.receiveGiftEvent = 0;
                self.flower = 0;
                self.giveMoneyClan = 1;
                if (self.subQuest.id_quest > -1) {
                    self.subQuest.reset();
                    self.subQuest.id_quest = -1;
                    MessageCreator.createMsgCharQuest(self, 0);
                }
                if (self.lvDetail.lv >= 34) {
                    if (self.ntangtoc == 6) {
                        if (self.potions[7] < 6) {
                            final int[] potions2 = self.potions;
                            final int n3 = 7;
                            potions2[n3] += 6;
                            Database.instance.saveOrtherLog("", self.charname, "tang 6 binh tang  toc", "btt");
                        }
                    } else if (self.ntangtoc < 6) {
                        final int[] potions3 = self.potions;
                        final int n4 = 7;
                        potions3[n4] += 6 - self.ntangtoc;
                        Database.instance.saveOrtherLog("", self.charname, "tang " + (6 - self.ntangtoc) + " binh tang  toc", "btt");
                        self.ntangtoc = 6;
                    }
                }
                self.luot_van_tieu = 3;
//                if (self.luot_van_tieu >= 3) {
//                    self.luot_van_tieu = 0;
//                }
                // Toi da 2 luot MUA/ngay (khong cong vip) + 3 mien phi = 5 tieu/ngay -
                // xem giai thich day du o CharCapNhat.java (cung mot cap nhat, khong
                // dung toi potions[82] vi trung index voi tre Trung Thu).
                self.buy_luot_van_tieu = 2;
//                self.buy_luot_van_tieu = 0;
                self.cuop_tieu = 5;
                self.countTime = 0;
                if (self.lvDetail.lv >= 10) {
                    try {
                        self.receiveGiftDay();
                    } catch (final Exception ex7) {
                    }
                }
                if (self.vip > 0 && self.vip <= 3) {
                    final int[] potions4 = self.potions;
                    final int n5 = 100;
                    potions4[n5] += Char.soluong_loa_vip[self.vip];
                    self.sendMessage(MessageCreator.createCharInventoryMessage(self, 0));
                    self.sendMessage(MessageCreator.createMsgChat(self.id, "Nhận được " + self.vip + " loa."));
                    Database.instance.saveOrtherLog("", self.charname, "nhan loa vip", "loavip");
                }
                if (Char.isDemNoel()) {
                    final int lv = self.lvDetail.lv;
                }
                if (Char.isDemHaloween() && self.lvDetail.lv >= 40 && self.receiveGiftOldChar == 1) {
                    self.potions[141] = 1;
                    Database.instance.saveOrtherLog("", self.charname, "nhan qua tang haloween", "hopquahlw");
                }
                if (Char.isDemTrungThu() && self.lvDetail.lv >= 20) {
                    self.potions[125] = 1;
                    Database.instance.saveOrtherLog("", self.charname, "nhan qua tang trung thu", "hopquatrungthu");
                }
            }
          
           
            if (self.lastLog.compareTo("2019-03-08 10:46:00") < 0) {
                self.potions[72] = 0;
                if (self.isCharNu() && Char.getDayOpen(0L).equals("2019-03-08")) {
                    Item newItem = null;
                    final int[] idchoi = {617, 618, 619};
                    String infotang = "";
                    for (int m = 0; m < idchoi.length; ++m) {
                        final int idtemp = idchoi[m];
                        newItem = new Item((ItemTemplates) Map.itemTemplates.get(5).get(idtemp), false, 0, 0, idtemp);
                        newItem.id = self.getIDItem();
                        newItem.owner = self.charDBID;
                        newItem.level = newItem.getTemplate().level;
                        newItem.identify = self.charDBID;
                        newItem.clazz = self.charClass;
                        newItem.createAttChoi();
                        newItem.dateCreate = Char.getDayOpen(0L);
                        newItem.timeLoan = System.currentTimeMillis();
                        newItem.minuteExist = 4320;
                        self.iItems.add(newItem);
                        self.sendMessage(MessageCreator.createServerAlertMessage("Nhận được " + newItem.getName() + " thời hạn " + newItem.minuteExist + " phút", ""));
                        self.sendMessage(MessageCreator.createCharInventoryMessage(self, 1));
                        infotang = String.valueOf(infotang) + ">" + newItem.getName() + "_" + newItem.getDBInfo() + "|" + newItem.getAttribute() + "_" + newItem.level + " thoi han " + newItem.minuteExist;
                    }
                    self.sendMessage(MessageCreator.createServerAlertMessage("Bạn đã nhận được Chổi nhân ngày 8/3. Chúc bạn 8/3 thật vui vẻ và hạnh phúc.", ""));
                    self.sendMessage(MessageCreator.createCharInventoryMessage(self, 1));
                    Database.instance.saveOrtherLog("", self.charname, infotang, "qua83");
                }
            }
            if (self.lastLog.compareTo("2018-12-21 16:35:00") < 0) {
                self.potions[72] = 0;
                self.potions[73] = 0;
                self.potions[74] = 0;
                self.potions[77] = 0;
                self.potions[105] = 0;
                self.potions[142] = 0;
                self.potions[119] = 0;
                self.potions[117] = 0;
                self.potions[122] = 0;
                self.potions[120] = 0;
                self.potions[121] = 0;
                self.potions[124] = 0;
                self.potions[125] = 0;
                self.banphaohoa = 0;
                self.nBinhTra = 10;
                self.luongNap = 0L;
            }
        
            if (self.lastLog.compareTo("2020-01-17 16:20:00") < 0) {
                self.potions[72] = 0;
                self.potions[74] = 0;
                self.potions[77] = 0;
                self.potions[120] = 0;
                self.potions[121] = 0;
                self.potions[125] = 0;
                self.potions[122] = 0;
                self.potions[117] = 0;
                self.potions[105] = 0;
                self.potions[142] = 0;
                self.potions[119] = 0;
                self.potions[124] = 0;
                self.luongNap = 0L;
                self.banphaohoa = 0;
                self.nBinhTra = 100;
            }
            if ((self.lastLog.compareTo("2020-03-26 10:40:00") < 0 && Map.openLog) || (self.lastLog.compareTo("2020-03-26 11:19:00") < 0 && !Map.openLog)) {
                self.potions[77] = 0;
                self.potions[105] = 0;
                self.potions[142] = 0;
                self.potions[117] = 0;
                self.potions[121] = 0;
                self.luongxai = 0L;
            }
            if (self.lastLog.compareTo("2020-02-25 17:40:00") < 0) {
                self.potions[72] = 0;
                self.potions[74] = 0;
            }
            if (self.lastLog.compareTo("2020-04-29 15:43:00") < 0) {
                self.potions[77] = 0;
                self.potions[105] = 0;
                self.potions[142] = 0;
            }
            if (self.lastLog.compareTo(Map.openLog ? "2020-08-06 15:10:00" : "2020-08-06 16:18:00") < 0) {
                self.potions[77] = 0;
                self.potions[105] = 0;
                self.potions[118] = 0;
                self.luongxai = 0L;
                self.luongxainhanRuong = 0L;
                self.receiveQuestVulan = 0;
                self.countnvVulan = 0;
            }
        
            if (Char.isSuKienNoel() && self.nHopMut == -1) {
                final int[] id = {73, 77};
                self.nHopMut = (byte) id[Map.r.nextInt(id.length)];
            }
            self.canReceiveGiftOffline = Database.instance.checkAddGiftEventGame(self.charname);
            if (!Map.openLog && self.lastLog.compareTo("2016-12-29 10:15:00") < 0) {
                Database.instance.traGemNgude(self);
            }
            if (self.lastLog.compareTo("2017-10-20 10:10:00") < 0 && Char.getDayOpen(0L).equals("2017-10-20") && self.isCharNu()) {
                final int[] potions6 = self.potions;
                final int n7 = 122;
                ++potions6[n7];
                self.sendMessage(MessageCreator.createCharInventoryMessage(self, 0));
                self.sendMessage(MessageCreator.createMsgChat(self.id, "Nhận được hộp quà nhân ngày 20-10"));
                Database.instance.saveOrtherLog("", self.charname, "nhan qua trung thu khi choi qua dem", "nhanhopqua2010");
            }
            if (self.lastLog.compareTo("2020-04-09 11:00:00") < 0) {
                self.potions[119] = 0;
                self.potions[120] = 0;
                self.luongxai = 250L;
            }
            if (self.lastLog.compareTo("2020-09-22 10:15:00") < 0) {
                self.potions[77] = 0;
                self.potions[105] = 0;
                self.potions[117] = 0;
                self.potions[142] = 0;
                self.potions[120] = 0;
                self.potions[121] = 0;
                self.potions[122] = 0;
                self.potions[124] = 0;
                self.potions[125] = 0;
                self.potions[133] = 0;
                self.potions[134] = 0;
                self.potions[135] = 0;
                self.potions[136] = 0;
                self.luongNap = 0L;
                self.banphaohoa = 0;
                self.nBinhTra = 100;
                self.nHopMut = 100;
            }
            if (self.lastLog.compareTo("2020-09-23 16:28:00") < 0 && self.getLuong() <= 0 && self.potions[135] > 1) {
                Database.instance.saveOrtherLog("", self.charname, "tich thu " + self.potions[135] + " hat mam", "thuhatmam");
                self.potions[135] = 0;
            }
            if (self.lastLog.compareTo("2020-07-01 14:05:00") < 0) {
                self.potions[73] = 0;
                for (int j = 0; j < GemTemplate.ID_NGOC_RONG.length; ++j) {
                    try {
                        self.delGem(GemTemplate.ID_NGOC_RONG[j], self.listGemitem[GemTemplate.ID_NGOC_RONG[j]], false);
                        self.delGem(GemTemplate.ID_NGOC_RONG[j], self.listGemitemLock[GemTemplate.ID_NGOC_RONG[j]], true);
                    } catch (final Exception ex8) {
                    }
                }
            }
            if (self.lastLog.compareTo("2020-10-22 10:00:00") < 0) {
                self.potions[115] = 0;
            }
            if (self.lastLog.compareTo("2022-10-25 00:00:00") >= 0) {
                self.potions[137] = 0;
                self.potions[138] = 0;
                self.potions[139] = 0;
                self.potions[140] = 0;
                self.potions[141] = 0;
                self.potions[143] = 0;
                self.potions[144] = 0;
                self.potions[118] = 0;
                self.luongxainhanRuong = 0L;
            }
            if (self.lastLog.compareTo("2020-12-17 10:45:00") < 0) {
                for (int j = 0; j < GemTemplate.ID_NGOC_RONG.length; ++j) {
                    try {
                        self.delGem(GemTemplate.ID_NGOC_RONG[j], self.listGemitem[GemTemplate.ID_NGOC_RONG[j]], false);
                        self.delGem(GemTemplate.ID_NGOC_RONG[j], self.listGemitemLock[GemTemplate.ID_NGOC_RONG[j]], true);
                    } catch (final Exception ex9) {
                    }
                }
                self.potions[146] = 0;
                self.potions[145] = 0;
                self.potions[147] = 0;
            }
            if (self.lastLog.compareTo("2021-02-02 15:30:00") < 0) {
                self.potions[122] = 0;
                self.potions[155] = 0;
                self.potions[149] = 0;
                self.potions[150] = 0;
                self.potions[148] = 0;
                self.potions[154] = 0;
                self.potions[153] = 0;
                self.potions[151] = 0;
                self.potions[152] = 0;
                self.luongNap = 0L;
                self.luongxai = 0L;
                self.luongxainhanRuong = 0L;
                self.diemNapVip = 0;
                self.diemxaiVip = 0;
            }
            if (self.lastLog.compareTo("2020-10-28 10:50:00") < 0) {
                final int luongdiem = Database.instance.getDiemLuongXaiNhanDiem(self.charname);
                if (self.luongxainhanRuong < luongdiem) {
                    self.luongxainhanRuong = luongdiem;
                }
            }
            if (!TeamServer.isServerHoaLu() && self.lastLog.compareTo("2020-04-14 16:42:00") < 0) {
                self.potions[119] = 0;
                self.potions[120] = 0;
            }
          
            Database.instance.getDiemNapXaiLuong(self);
            Database.instance.getCharDanhHieu(self);
            DanhHieuGame.napChoChar(self); // bo dem danh hieu (tob_danh_hieu_dem)
            self.dayLogin = Char.getDayOpen(0L);
            if (self.dbBasic != null) {
                final String[] data3 = Char.split(self.dbBasic, ",");
                self.strength = (short) Map.abs(Short.parseShort(data3[0]));
                self.agitity = (short) Map.abs(Short.parseShort(data3[1]));
                self.spirit = (short) Map.abs(Short.parseShort(data3[2]));
                self.health = (short) Map.abs(Short.parseShort(data3[3]));
                self.luck = (short) Map.abs(Short.parseShort(data3[4]));
                self.basepoint = (short) Map.abs(Short.parseShort(data3[5]));
                self.skillpoint = (short) Map.abs(Short.parseShort(data3[6]));
            }
            // Kiem tra diem tiem nang (chong sua chi so): ap dung cho moi nhan vat.
            final int allBasicPoint = self.lvDetail.getBasicPoint(self.lastLV) + (self.lastLV - 1) * 5;
            final int tempSt = self.strength - Database.basicPoint[self.charClass][0];
            final int tempAgitity = self.agitity - Database.basicPoint[self.charClass][1];
            final int tempSpirit = self.spirit - Database.basicPoint[self.charClass][2];
            final int tempHealth = self.health - Database.basicPoint[self.charClass][3];
            final int tempLuck = self.luck - Database.basicPoint[self.charClass][4];
            final int tong = tempAgitity + tempHealth + tempLuck + tempSpirit + tempSt;
            if (tong > allBasicPoint || self.basepoint > allBasicPoint) {
                self.basepoint = (short) (allBasicPoint - (self.lastLV - 1) * 5);
                self.skillpoint = (short) (self.lastLV - 1);
                self.hp = 100;
                self.mp = 100;
                self.strength = (short) (Database.basicPoint[self.charClass][0] + (self.lastLV - 1));
                self.agitity = (short) (Database.basicPoint[self.charClass][1] + (self.lastLV - 1));
                self.spirit = (short) (Database.basicPoint[self.charClass][2] + (self.lastLV - 1));
                self.health = (short) (Database.basicPoint[self.charClass][3] + (self.lastLV - 1));
                self.luck = (short) (Database.basicPoint[self.charClass][4] + (self.lastLV - 1));
                final int firstSkill = self.skill[0];
                for (int i2 = 0; i2 < self.skill.length; ++i2) {
                    if (self.skill[i2] >= 0) {
                        self.skill[i2] = 0;
                    }
                }
                if (firstSkill > 0 || self.lvDetail.lv >= 3) {
                    self.skill[0] = 1;
                }
            }
            if (self.skillpoint < 0) {
                self.skillpoint = 0;
            }
            if (self.basepoint < 0) {
                self.basepoint = 0;
            }
            if (self.fullGoldTime != 0L) {
                self.x2 = 2;
            } else if (self.halfGoldTime != 0L) {
                self.x2 = 1;
            }
            if (self.idClan == -1) {
                self.skillClan = new short[20];
            }
            if (count + self.skillpoint > self.lastLV && !self.isAdmin) {
                for (int j = 0; j < self.skill.length; ++j) {
                    if (self.skill[j] > 0) {
                        self.skill[j] = 0;
                    }
                }
                if (self.lastLV >= 3 && self.skill[0] == 0) {
                    self.skill[0] = 1;
                }
                self.skillpoint = (short) (self.lastLV - 1);
                if (self.skillpoint < 0) {
                    self.skillpoint = 0;
                }
            }
            final int openGif = 0;
            final String logError = "";
            self.setitemInvenBag(0);
            self.setitemInvenBag(1);
            self.setItemWearing();
            self.setItemTuiDo(self.tuido);
            self.invent = "";
            self.equip = "";
            self.bag = "";
            self.tuido = "";
            int iItemCount = 0;
            final int wItemCount = 0;
            final int ao = 0;
            final int quan = 0;
            final int gang = 0;
            final int non = 0;
            final int vukhi = 0;
            final int nhan = 0;
            final int daychuyen = 0;
            final int giay = 0;
            final int ngoc = 0;
            final int cuoc = 0;
            final int aochoang = 0;
            final int ao2 = 0;
            final int quan2 = 0;
            final int gang2 = 0;
            final int non2 = 0;
            final int vukhi2 = 0;
            final int nhan2 = 0;
            final int daychuyen2 = 0;
            final int giay2 = 0;
            final int ngoc2 = 0;
            final int cuoc2 = 0;
            final int aochoang2 = 0;
            int nAnimalItemDel = openGif * 10;
            final int ring = 0;
            final int ring2 = 0;
            int posNhan = -1;
            if (charItem != null) {
                for (int i3 = 0; i3 < charItem.size(); ++i3) {
                    final Item item = charItem.get(i3);
                    try {
                        item.initInfoFromDB();
                        if (item.getMinuteLoan(false) <= 0 && item.minuteExist > 0) {
                            try {
                                self.sendMessage(MessageCreator.createServerAlertAutoOffMessage("Thời gian sử dụng " + item.getTemplate().name + " đã hết"));
                                Database.instance.deleteItem(item.dbid);
                                // 16/09/2026: them dbid (theo yeu cau "log chi tiet hon").
                                Database.instance.saveOrtherLog("tob_log_other_item", self.charname, "dbid" + item.dbid + " " + item.getTemplate().name + "|" + item.getDBInfo() + "|" + item.getAttribute(), "timeout");
                            } catch (final Exception ex10) {
                            }
                        } else if (item.owner == self.charDBID) {
                            if (item.identify == 0) {
                                item.identify = self.charDBID;
                            }
                            if (item.dbAttribute != null) {
                                final String[] data4 = Char.split(item.dbAttribute, ",");
                                for (byte j2 = 0; j2 < data4.length; ++j2) {
                                    try {
                                        if (j2 < 33) {
                                            item.atb[j2] = Short.parseShort(data4[j2].trim());
                                        } else if (j2 < 43) {
                                            item.newAtb[j2 - 33] = Byte.parseByte(data4[j2].trim());
                                        } else if (j2 < 58) {
                                            item.addMoreLevelSkill[j2 - 43] = Byte.parseByte(data4[j2].trim());
                                        } else if (j2 < 61) {
                                            item.lockAtb[j2 - 58] = Byte.parseByte(data4[j2].trim());
                                        } else {
                                            item.otherAtt[j2 - 61] = Short.parseShort(data4[j2].trim());
                                        }
                                    } catch (final Exception e3) {
                                        e3.printStackTrace();
                                    }
                                }
                                try {
                                    if (item.resetItem != 5000 || (Map.isWearingAnimal(item.getTemplate().type) && !item.isHaveAttLock())) {
                                        if (Map.isWearingAnimal(item.getTemplate().type) && item.colorName == 1) {
                                            switch (item.hangItem) {
                                                case 1: {
                                                    item.lockItemAnimal(new byte[]{0, 0, 0, 0, 0, 1});
                                                    break;
                                                }
                                                case 2: {
                                                    item.lockItemAnimal(new byte[]{0, 0, 0, 0, 1, 0});
                                                    break;
                                                }
                                                case 3: {
                                                    item.lockItemAnimal(new byte[]{0, 0, 0, 1, 0, 0});
                                                    break;
                                                }
                                                case 4: {
                                                    item.lockItemAnimal(new byte[]{0, 0, 1, 0, 0, 0});
                                                    break;
                                                }
                                                case 5: {
                                                    final Item item2 = item;
                                                    final byte[] material = new byte[6];
                                                    material[1] = 1;
                                                    item2.lockItemAnimal(material);
                                                    break;
                                                }
                                            }
                                        }
                                        item.resetItem = 5000;
                                    }
                                } catch (final Exception ex11) {
                                }
                                item.setAnotherSkill();
                            }
                            if (item.mDurable == -1) {
                                item.mDurable = (short) (item.durable * 10);
                            }
                            if (item.getTemplate().type == 12 && item.atb[5] == 0) {
                                item.atb[5] = item.getTemplate().atb[5];
                                for (int m2 = 0; m2 < item.plus; ++m2) {
                                    final short[] atb = item.atb;
                                    final int n8 = 5;
                                    ++atb[n8];
                                }
                            }
                            if (item.getTemplate().type == 13) {
                                if (self.itemAx == null) {
                                    if (item.place == 1) {
                                        self.itemAx = item;
                                    }
                                } else if (item.place == 1) {
                                    item.place = 0;
                                }
                            }
                            if (nAnimalItemDel > 0 && Map.isWearingAnimal(item.getTemplate().type) && item.dateCreate.equals("2013-04-16") && item.colorName != 1) {
                                // SERVER TU XOA DO NGAY LUC NAP NHAN VAT — truoc day khong
                                // ghi lai gi. Nguoi choi chi thay mat mon do sau khi dang
                                // nhap, khong the truy ra vi sao. Day la duong mat do kho
                                // lan nhat trong tat ca, vi no khong do thao tac nao cua ho.
                                Database.instance.saveLogMatVatPham(self, item,
                                        "server xoa luc nap nhan vat (thu cuoi ngay 2013-04-16, con lai "
                                      + (nAnimalItemDel - 1) + " luot xoa)");
                                Database.instance.deleteItem(item.dbid);
                                --nAnimalItemDel;
                            } else {
                                switch (item.place) {
                                    case 0: {
                                        ++iItemCount;
                                        item.id = self.getIDItem();
                                        self.iItems.add(item);
                                        break;
                                    }
                                    case 1: {
                                        if (wItemCount >= 20) {
                                            break;
                                        }
                                        item.id = self.getIDItem();
                                        final int t = item.getTemplate().type;
                                        // Skin linh thu (type 30) cung phai vao awItems —
                                        // xem chu thich dai o setItemWearing. Day la nhanh
                                        // nap kieu cu (doc bang tob_item theo place), chi
                                        // chay khi cot equip rong, nhung van phai vá cho
                                        // dong bo, khong thi hai duong nap ra hai ket qua.
                                        if (Map.isWearingAnimal(t)
                                                || SkinThuCuoi.laItemSkin(item.getTemplate().id)) {
                                            self.awItems.add(item);
                                            break;
                                        }
                                        boolean vk = false;
                                        if (t == 6 || t == 7 || t == 4 || t == 5 || t == 3) {
                                            vk = true;
                                        }
                                        if (item.clazz != self.charClass && item.getTemplate().type != 13 && item.getTemplate().type != 19) {
                                            item.place = 0;
                                            self.iItems.add(item);
                                            break;
                                        }
                                        if (item.getTemplate().gender != 0 && self.gender != 0 && item.getTemplate().type != 13 && item.getTemplate().type != 19 && item.getTemplate().gender != self.gender) {
                                            break;
                                        }
                                        if (item.level > self.lastLV) {
                                            break;
                                        }
                                        if (t == 8) {
                                            if (posNhan == -1) {
                                                posNhan = item.pos;
                                            } else if (posNhan == item.pos) {
                                                if (item.pos == 0) {
                                                    posNhan = 1;
                                                } else {
                                                    posNhan = 0;
                                                }
                                            }
                                        }
                                        final int index2 = Char.getIndexItemWearing(t, posNhan);
                                        if (index2 > -1 && self.wItems[item.slotWearing][index2] == null) {
                                            self.wItems[item.slotWearing][index2] = item;
                                            break;
                                        }
                                        item.slotWearing = 0;
                                        item.place = 0;
                                        self.iItems.add(item);
                                        break;
                                    }
                                    case 2: {
                                        item.id = self.getIDItem();
                                        break;
                                    }
                                    case 3: {
                                        item.id = self.getIDItem();
                                        self.bItem.add(item);
                                        break;
                                    }
                                    case 4: {
                                        if (self.wModel != null) {
                                            item.place = 0;
                                            self.iItems.add(item);
                                            break;
                                        }
                                        (self.wModel = new ModelWearing()).setItemModel(self, item, item.getTemplate().atb[4]);
                                        break;
                                    }
                                    case 5: {
                                        item.id = self.getIDItem();
                                        break;
                                    }
                                }
                            }
                        }
                    } catch (final Exception ex12) {
                    }
                }
            }
            self.reclass = 1;
            try {
                self.dbSkill = null;
                self.dbPotion = null;
                self.dbBasic = null;
                self.dbCharQuest = null;
                self.dbInfo = null;
                if (Map.onOffSound) {
                    self.sendMessage(MessageCreator.createMsgSound(0));
                }
            } catch (final Exception ex13) {
            }
            final short[] time = Char.AUTOSAVE_MINUTES;
            self.timeSave = System.currentTimeMillis() + time[Map.r.nextInt(time.length)] * 60 * 1000;
            String noCp = "Chơi quá 180 phút mỗi ngày sẽ hại sức khoẻ.";
            try {
                if (Map.checkRcvXP && Map.openLog) {
                    self.sendMessage(MessageCreator.createServerAlertAutoOffMessage(noCp));
                } else {
                    self.sendMessage(MessageCreator.createServerAlertAutoOffMessage(noCp));
                }
                // self.sendMessage(MessageCreator.createServerAlertAutoOffMessage("5HQuoc.ME - Ngũ Hành Quốc chào mừng bạn đến vùng đất mới."));
            } catch (final Exception ex14) {
            }
            Label_11022:
            {
                if (!Char.getDayOpen(0L).equals("2015-02-26") && !Char.getDayOpen(0L).equals("2015-02-27")) {
                    if (!Char.getDayOpen(0L).equals("2015-02-28")) {
                        break Label_11022;
                    }
                }
                try {
                    if (Map.openLog) {
                        self.sendMessage(MessageCreator.createServerAlertAutoOffMessage("Từ 26/02-28/02 Khuyến mãi 50% các loại thẻ. 100% carot và thẻ mệnh giá từ 500k trở lên."));
                    } else {
                        self.sendMessage(MessageCreator.createServerAlertAutoOffMessage("Từ 26/02-28/02 Khuyến mãi 100% các loại thẻ, sms, vcoin."));
                    }
                } catch (final Exception ex15) {
                }
            }
            if (Map.pkAuto) {
                try {
                    self.sendMessage(MessageCreator.createServerAlertAutoOffMessage("Đang trong thời gian pk tự do."));
                } catch (final IOException ex16) {
                }
            }
            if (self.haveShield()) {
                try {
                    self.sendMessage(MessageCreator.createServerAlertAutoOffMessage("Bạn đang được bảo hộ"));
                } catch (final IOException ex17) {
                }
            }
            if (self.idClan > -1 && self.myCountry > -1) {
                self.rankGov = Char.gov[self.myCountry].getRankGov(self.charname);
                if (self.rankGov > -1) {
                    if (self.rankGov < 2) {
                        self.potions[87] = 1;
                    }
                    if (self.rankGov == 0) {
                        self.potions[85] = 1;
                    }
                    self.potions[78] = 1;
                    self.potions[28] = 1;
                    if (self.potions[27] == 0) {
                        self.potions[27] = 1;
                    }
                }
            }
            if (self.lvDetail.lv >= 34) {
                try {
                    final FruitTemplate[] fr = Database.instance.loadCharFruit(self);
                    if (fr == null) {
                        Database.instance.addCharFruit(self.charDBID);
                    } else {
                        self.fruit = fr;
                    }
                    self.createFruit();
                    MessageCreator.createMsgCharFruit(self, 0, 0);
                } catch (final Exception e4) {
                    e4.printStackTrace();
                }
            }
            if (self.lvDetail.lv >= 40) {
                Vector<TuBinhTemplate> tb = Database.instance.loadCharTubinh(self);
                if (self.lastLog.compareTo("2017-10-09 09:28:00") < 0 && Map.openLog && TeamServer.server == 3) {
                    final Vector<TuBinhTemplate> tb2 = Database.instance.loadCharTubinh0610(self);
                    if (tb.size() == 0) {
                        tb = tb2;
                    } else {
                        for (int i4 = 0; i4 < tb2.size(); ++i4) {
                            final TuBinhTemplate tubinh = tb2.get(i4);
                            for (int j3 = 0; j3 < tb.size(); ++j3) {
                                final TuBinhTemplate tbchinh = tb.get(j3);
                                if (tbchinh.id == tubinh.id) {
                                    for (int k2 = 0; k2 < tubinh.manh.length; ++k2) {
                                        tbchinh.addManh(k2, self, tubinh.manh[k2]);
                                    }
                                    break;
                                }
                            }
                        }
                    }
                }
                if (tb.size() == 0) {
                    Database.instance.addCharTubinh(self.charDBID);
                } else {
                    self.tubinh = tb;
                }
                self.createTubinh();
                MessageCreator.createMsgCharTuBinh(self);
            }
            self.getInfoWebWearing();
            if (self.firstNapMoney == 0 && Char.isSuKienHalowwen2015() && Map.openLog) {
                final String kq = Database.instance.getFirstNap(self);
                if (!kq.equals("")) {
                    self.sendMessage(MessageCreator.createServerAlertAutoOffMessage(kq));
                    self.firstNapMoney = 1;
                    Database.instance.saveOrtherLog("", self.charname, kq, "firstx2");
                }
            }
        } catch (final Exception e5) {
            e5.printStackTrace();
        }
//        if (self.potions[70] > 0) {
//            System.out.println("SO LUONG PH " + self.potions[70]);
//            if (self.potions[70] > 2) {
//                Database.instance.banChar(self.charname, 1);
//                CharManager.instance.kickPlayer(self, "char 3");
//            }
//            Logger.logString(String.valueOf(self.charname) + self.potions[70], String.valueOf(self.charname) + ".txt");
//        }
        self.monster = MonsterVantieu.getMonsterVantieu(self.charDBID);
        if (self.monster == null && self.typeTieu > -1) {
            self.typeTieu = -1;
            try {
                self.sendMessage(MessageCreator.createServerAlertAutoOffMessage("Chuyến giao hàng của bạn đã kết thúc."));
            } catch (final IOException e6) {
                e6.printStackTrace();
            }
        } else if (self.monster != null) {
            try {
                if (self.monster.isFinish()) {
                    self.monster.actorDie();
                    self.typeTieu = -1;
                } else {
                    self.sendMessage(MessageCreator.createServerAlertAutoOffMessage("Ponyta giao hàng đang ở " + Map.getNameMap(self.monster.map.mapId) + " toạ độ " + self.monster.x / 16 + " " + self.monster.y / 16));
                }
            } catch (final IOException e6) {
                e6.printStackTrace();
            }
        }
        if (self.isCharNu() && Char.getDayOpen(0L).equals("2017-03-08")) {
            try {
                final Message msg = MessageCreator.createMsgWeather(5, 99, 60000);
                self.sendMessage(msg);
            } catch (final Exception ex18) {
            }
            if (self.lastLog.compareTo("2017-03-08 10:57:00") <= 0) {
                self.createPet(4, 10080);
            }
        }
        try {
            Database.instance.addCharEventTrungthu(self);
        } catch (final Exception ex19) {
        }
    }

}
