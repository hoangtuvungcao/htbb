package real;


import java.io.DataInputStream;
import data.Text;
import server.TeamServer;
import java.util.Random;
import data.Database;
import io.Message;
import java.io.IOException;

import data.CharInfo;
import data.Pet;
import java.util.Vector;

/**
 * Than cac method cap nhat trang thai va di chuyen cua Char: vong cap nhat
 * phu, cap nhat nguoi choi gan minh, xu ly di chuyen, cong kinh nghiem,
 * buff ky nang, hieu ung thu cuoi.
 *
 * Tach tu real/Char.java. Cac method VAN CON trong Char, giu NGUYEN chu ky
 * cu, chi than chuyen sang day. Tham so "Char self" thay cho "this".
 *
 * KHONG gom Char.update (613 dong) vi than no co LOP AN DANH - lop an danh
 * bat "this" ngam nen chuyen sang static la hong.
 */
class CharCapNhat {

    static int expReceive(Char self, int xpReceive) {
        int dxp = 0;
        if (self.petUsing != null) {
            // Nhan voi xpReceive (kinh nghiem thuong vua nhan), khong phai dxp - dxp con = 0
            // tai day nen ban cu nhan-voi-0 khien bonus EXP cua pet luon bang 0.
            dxp = xpReceive * self.petUsing.getPcUpExp() / 100;
        }

        // xp canh
        int xpCanh = 0;
        if (self.wModel.CanhModel != null) {
            xpCanh = (int)(xpReceive * (self.wModel.CanhModel.otherAtt[50] / 100.0));
        }

        int xpCaiTrang = 0;
        if (self.isBatGioi()) {
            xpCaiTrang = (int)(xpReceive * (self.getLevelCaiTrang() / 100.0));
        }
        // xp mat na
        int xpMatNa = 0;
        if (self.wModel.matNa != null) {
            xpMatNa = (int)(xpReceive * (self.wModel.matNa.otherAtt[50] / 100.0));
        }

        int expPhaohoa = 0;
        int expPhaohoa2 = 0;
        if (Map.x3ExpDotPhao > 0) {
            expPhaohoa = xpReceive;
        }
        if (self.x2Phaohoa > 0) {
            expPhaohoa2 = xpReceive / 2;
        }
        int x2Player = self.getX2();
        int expWedding = 0;
        try {
            if (self.nameHusband_wife.equals("")) {
                final Char p = CharManager.instance.getCharByCharName(self.nameHusband_wife);
                if (p != null && p.mapID == self.mapID) {
                    expWedding = xpReceive * 2 / 100;
                }
            }
        } catch (final Exception ex) {
        }
        if (Char.getDayOpen(0L).equals("2015-04-28") || Char.getDayOpen(0L).equals("2015-04-30") || Char.getDayOpen(0L).equals("2015-05-01")) {
            x2Player = 2;
        }
        final String daykm = Char.getDayOpen(0L);
        final String time = Char.getDayTime(0L);
        for (int i = 0; i < Char.timex50.size(); ++i) {
            if (daykm.equals(Char.timex50.get(i)) || time.compareTo((String) Char.timex50.get(i)) < 0) {
                x2Player = 1;
                break;
            }
        }
        for (int i = 0; i < Char.timex2.size(); ++i) {
            if (daykm.equals(Char.timex2.get(i)) || time.compareTo((String) Char.timex2.get(i)) < 0) {
                x2Player = 2;
                break;
            }
        }
        int i = 0;
        while (i < Char.timex3.size()) {
            if (daykm.equals(Char.timex3.get(i)) || time.compareTo((String) Char.timex3.get(i)) < 0) {
                if (self.lvDetail.lv < Char.Lvx3Exp) {
                    x2Player = 4;
                    break;
                }
                break;
            } else {
                ++i;
            }
        }
        int expEf = 0;
        if (self.idCuiDot > -1) {
            final int[] pcef = {5, 10, 20};
            expEf = xpReceive * pcef[self.idCuiDot] / 100;
        }
        int exfhove = 0;
        if (self.idNgtuyet == 3) {
            exfhove = xpReceive * 20 / 100;
        }
        if (Map.doubleALL > 1) {
            x2Player = 0;
        }
        if (x2Player == 1) {
            xpReceive += xpReceive / 2;
        } else if (x2Player == 2) {
            xpReceive *= x2Player;
        } else if (x2Player == 3) {
            int exp50 = xpReceive / 2;
            if (exp50 <= 0) {
                exp50 = 1;
            }
            xpReceive = xpReceive * 2 + exp50;
        } else if (x2Player == 4) {
            xpReceive *= 3;
        }
        // todo exp game ở đây
        xpReceive *= (3 * Char.TangEXP);
//        if (Char.getDayOpen(0L).equals("2023-12-15") || Char.getDayOpen(0L).equals("2023-12-16")
//                || Char.getDayOpen(0L).equals("2023-12-17") || Char.getDayOpen(0L).equals("2023-12-18")
//                || Char.getDayOpen(0L).equals("2023-12-19")) {
//            xpReceive *= 5;
//        } 
       int  exp = xpReceive + xpCanh + xpCaiTrang + xpMatNa + expWedding + dxp + expEf + exfhove + expPhaohoa + expPhaohoa2;
        if (self.lvDetail.lv >= 75 && self.lvDetail.lv < 80) {
            exp = (int)(exp * 0.65); // Giảm 35% exp
        } else if (self.lvDetail.lv >= 80) {
            exp = (int)(exp * 0.60); // Giảm 40% exp
        }
        return exp;
    }

    static void buffAttackSkill(Char self, final Monster mt, final int d) {
        final Random r = java.util.concurrent.ThreadLocalRandom.current(); // GD2 1.1: khong reseed theo ms
        try {
            int damPhan = d * ChiSoTrangBiGame.phanDonPhanTram(self.attNguHanh[4], false) / 100; // tran 15% khi bat
            if (self.charClass == 0 && self.arrayBuff[4] != -1) {
                final int n = r.nextInt(100);
                if (n < 70) {
                    final int damPhan2 = d * CharManager.SKILL_DAM_PERCENT[self.charClass][5][self.skill[5] + self.addMoreLevelSkill[5]] / 100;
                    if (damPhan2 > damPhan) {
                        damPhan = damPhan2;
                    }
                }
            }
            if (damPhan > 0) {
                final Message m = new Message(89);
                m.dos.writeShort(mt.id);
                m.dos.writeByte(mt.cat);
                m.dos.writeByte(0);
                m.dos.writeShort(damPhan);
                m.dos.writeByte(5);
                m.dos.writeByte(-1);
                m.dos.writeByte(0);
                mt.hp -= damPhan;
                self.sendMessage(m);
                self.sendToNearPlayer(m);
            }
        } catch (final Exception ex) {
        }
    }

    static void checkNewEffectPet(Char self, final LiveActor beAttack) {
        boolean issend = false;
        beAttack.checkActiveSkillPet();
        if ((beAttack.isActiveTienKhi() && !self.dangHoaGiaiKyNangPet()) || beAttack.isActive8sRongLon()) {
            return;
        }
        if (beAttack.isBossSonTinhThuyTinh()) {
            return;
        }
        Pet petUsing = self.petUsing;
        if (self.map != null && self.map.isMapChienTruongMoba()) {
            final CharChienTruong c = MapChienTruongMoba.getCharChienTruong(self.charname);
            if (c != null && c.getNoiLuc() < 5) {
                petUsing = null;
            }
        }
        if (petUsing != null) {
            int pc = 0;
            if (petUsing.isPetCu()) {
                pc = petUsing.getPcRuNgu();
                if (Map.r.nextInt(1000) < pc && beAttack.addEffBuff(EffectBuff.RU_NGU, System.currentTimeMillis() + 10000L, EffectBuff.BY_ACTOR, 0) != null) {
                    if (beAttack.cat == 0) {
                        beAttack.sendEffToChar((Char) beAttack);
                    }
                    issend = true;
                }
                if (beAttack.cat == 0) {
                    final Char p = (Char) beAttack;
                    if (!p.isBienHinh()) {
                        final int[] bienhinh = petUsing.checkBienHinh();
                        if (bienhinh[0] > -1) {
                            beAttack.setNguoiTuyet(6);
                            self.timeNguoiTuyet = System.currentTimeMillis() + bienhinh[0];
                            self.pcGiamSatThuongByCuMeo = (byte) bienhinh[1];
                        }
                    }
                }
            } else if (petUsing.isPetRong()) {
                pc = petUsing.getPcChoang();
                if (Map.r.nextInt(1000) < pc && beAttack.addEffBuff(EffectBuff.CHOANG, System.currentTimeMillis() + 5000L, EffectBuff.BY_ACTOR, 0) != null && beAttack.cat == 0) {
                    beAttack.sendEffToChar((Char) beAttack);
                    issend = true;
                }
                if (beAttack.cat == 0) {
                    final Char p = (Char) beAttack;
                    final int[] bienhinh = petUsing.checkHoangSo();
                    if (bienhinh[0] > -1) {
                        EffectBuff ef = null;
                        if ((ef = beAttack.addEffBuff(EffectBuff.HOANG_LOAN, System.currentTimeMillis() + bienhinh[1], EffectBuff.BY_ACTOR, 0)) != null) {
                            final Message msg = MessageCreator.createMsgNewEffectSkill5Long(bienhinh[0], bienhinh[1], p.id, p.cat, false, false);
                            p.sendMessage(msg);
                            p.sendToNearPlayer(msg);
                        }
                    }
                }
            } else if (petUsing.isPetYeuTinh()) {
                pc = petUsing.getPcBong();
                if (Map.r.nextInt(1000) < pc) {
                    EffectBuff ef2 = null;
                    if ((ef2 = beAttack.addEffBuff(EffectBuff.BONG, System.currentTimeMillis() + 5000L, EffectBuff.BY_ACTOR, 0)) != null) {
                        if (beAttack.cat == 0) {
                            beAttack.sendEffToChar((Char) beAttack);
                            issend = true;
                        }
                        ef2.dam = petUsing.getPcUpSatThuongBong();
                    }
                }
                if (beAttack.cat == 0) {
                    final Char p = (Char) beAttack;
                    final int[] bienhinh = petUsing.checkYemBua();
                    if (bienhinh[0] > -1) {
                        EffectBuff ef = null;
                        if ((ef = beAttack.addEffBuff(EffectBuff.YEM_BUA, System.currentTimeMillis() + bienhinh[1], EffectBuff.BY_ACTOR, 0)) != null) {
                            final Message msg = MessageCreator.createMsgNewEffectSkill5Long(bienhinh[0], bienhinh[1], p.id, p.cat, false, false);
                            p.sendMessage(msg);
                            p.sendToNearPlayer(msg);
                            final int mplost = self.mp * bienhinh[2] / 100;
                            self.mp -= mplost;
                            if (self.mp < 0) {
                                self.mp = 0;
                            }
                            MessageCreator.createMsgUseHpMP(self, -mplost, 4);
                        }
                    }
                }
            } else if (petUsing.isPetDoi()) {
                pc = petUsing.getpcTrungDoc();
                if (Map.r.nextInt(1000) < pc) {
                    EffectBuff ef2 = null;
                    if ((ef2 = beAttack.addEffBuff(EffectBuff.TRUNG_DOC, System.currentTimeMillis() + 5000L, EffectBuff.BY_ACTOR, 0)) != null) {
                        if (beAttack.cat == 0) {
                            beAttack.sendEffToChar((Char) beAttack);
                            issend = true;
                        }
                        ef2.dam = petUsing.getDoc();
                    }
                }
                if (beAttack.cat == 0) {
                    final Char p = (Char) beAttack;
                    final int[] bienhinh = petUsing.checkVetThuongSau();
                    if (bienhinh[0] > -1) {
                        EffectBuff ef = null;
                        if ((ef = beAttack.addEffBuff(EffectBuff.VET_THUONG_SAU, System.currentTimeMillis() + bienhinh[1], EffectBuff.BY_ACTOR, 0)) != null) {
                            final Message msg = MessageCreator.createMsgNewEffectSkill5Long(bienhinh[0], bienhinh[1], p.id, p.cat, false, false);
                            p.sendMessage(msg);
                            p.sendToNearPlayer(msg);
                        }
                    }
                }
            } else if (petUsing.isPetDaiBang()) {
                pc = petUsing.getpcSuyYeu();
                if (Map.r.nextInt(1000) < pc) {
                    EffectBuff ef2 = null;
                    if ((ef2 = beAttack.addEffBuff(EffectBuff.SUY_YEU, System.currentTimeMillis() + 5000L, EffectBuff.BY_ACTOR, 0)) != null) {
                        if (beAttack.cat == 0) {
                            beAttack.sendEffToChar((Char) beAttack);
                            issend = true;
                        }
                        ef2.dam = petUsing.getPcTacDongSuyYeu();
                    }
                }
            }
        }
        if (issend) {
            beAttack.sendEffToNearChar();
        }
    }

    static void updateNearChar(Char self) {
        final Message mActor = new Message(4);
        boolean havedata = false;
        for (int j = self.nearChars.size() - 1; j >= 0; --j) {
            try {
                final Char c = self.map.getPlayerByID(self.nearChars.elementAt(j));
                // Chan lop hai cho NPC "ADMIN": vong them nearChars ben duoi da khong cho
                // no vao, nhung neu co duong nao khac ghi duoc vao thi cung khong lam moi
                // vi tri de client ve no ra.
                if (Map.laNpcAnCuaAdmin(c) && !self.isAdmin) {
                    continue;
                }
                if (c != null) {
                    final boolean givcard = Char.givingCard(c.inCountry, c.id);
                    if (c.exit) {
                        self.nearChars.remove(j);
                        self.charOutRange(c);
                    // "&& !c.hienThiMoiRegion" PHAI co o day, y het nhanh loc region cua vong
                    // duoi (dong ~402). Hai vong nay noi ve CUNG mot danh sach nearChars:
                    //   vong nay  : ai khac region -> xoa khoi nearChars + charOutRange (XOA hinh)
                    //   vong duoi : ai khac region ma hienThiMoiRegion -> van them + writeActorPos (VE hinh)
                    //
                    // LOI DA XAY RA THAT (05/09/2026, nguoi choi bao "nhay, 1 giay 1-2 lan, cu an
                    // roi hien"): ban va 02/09/2026 (xem KiemNpcHoaTieuNhieuKhu) them ngoai le
                    // hienThiMoiRegion cho bot "hoa tieu"/"doi tieu" - nhung CHI them vao vong duoi.
                    // Vong tren van xoa. Ket qua la moi tick 300ms (Map.DELAY_UPDATE_MAP): vong duoi
                    // ve bot ra, vong tren xoa bot di, tick sau lai ve... dung nhip nhay nguoi choi
                    // ta. Bot cang o gan cang de thay.
                    //
                    // Chi xay ra tren ban do nRegion = 0 (Char.getAllPlayerMap chi lay dung region
                    // cua minh khi nRegion > 0, nen o do khong bao gio gap ai khac region).
                    } else if ((c.inCountry != self.inCountry && c.isBot == -1 && !self.isPublicMap()) || (c.mapID != self.mapID && c.isBot == -1) || (c.mapID == self.mapID && c.region != self.region && !c.hienThiMoiRegion)) {
                        self.nearChars.remove(j);
                        self.charOutRange(c);
                    } else {
                        final boolean near = self.near(c, self.rangeAddMonster[0]);
                        if ((near && c.isBot != -1) || ((self.checkInParty(c) || givcard) && !c.isAdmin)) {
                            boolean send = true;
                            if (c.isBot == -35 && !Char.canSendRaoVat()) {
                                send = false;
                            }
                            if (send) {
                                self.writeActorPos(mActor, c);
                                havedata = true;
                            }
                        }
                    }
                } else {
                    self.nearChars.remove(j);
                    self.map.removePlayer(self.inCountry, c);
                }
            } catch (final Exception e) {
                // TRUOC DAY "break": mot loi rieng le o BAT KY doi tuong nao trong nearChars
                // (vi du mot nguoi choi vua thoat giua chung, con so du lieu chua kip don) lam
                // GAY LUON ca vong lap, bo qua het cac doi tuong CON LAI (chi so thap hon, vi
                // vong nay duyet tu cuoi ve dau) - kem theo khong gui duoc goi vi tri lam moi
                // cho ho trong tick nay. "continue": chi bo qua DUNG mot doi tuong loi, cac
                // doi tuong khac van duoc xu ly binh thuong.
                continue;
            }
        }
        if (self.map.mapIDLoadMap == 0 || self.map.mapIDLoadMap == 70 || self.map.mapIDLoadMap == 80) {
            havedata = true;
            for (int i = 0; i < Map.npcVilage.size(); ++i) {
                if (!TeamServer.isServerIndo()) {
                    final Char aa = Map.npcVilage.get(i);
                    // NPC "admin": nguoi choi thuong khong thay. Rieng admin van thay de
                    // cham vao mo bang setting - mo tu NPC thi khong con o che do chat
                    // nen dieu khien bang phim mui tren binh thuong.
                    if (Map.laNpcAnCuaAdmin(aa) && !self.isAdmin) {
                        continue;
                    }
                    int xx = 0;
                    int yy = 0;
                    // Chan bien y het Char.writeActorPos - xem chu thich dai o ham do.
                    // Vong lap NAY khong nam trong try/catch nao: mot ArrayIndexOutOfBounds
                    // o day nem thang ra khoi updateNearChar, nen goi vi tri (mActor) khong
                    // duoc gui VA ca vong quet nguoi choi o duoi cung khong chay not trong
                    // tick do. posNPCInVilage luon dai 3 (MapNapTaiNguyen dat
                    // "new short[][]{{..},{..},{..}}"), con inCountry thi khong chac.
                    if (aa.posNPCInVilage != null
                            && self.inCountry >= 0
                            && self.inCountry < aa.posNPCInVilage.length
                            && aa.posNPCInVilage[self.inCountry] != null
                            && aa.posNPCInVilage[self.inCountry].length >= 2) {
                        xx = aa.posNPCInVilage[self.inCountry][0];
                        yy = aa.posNPCInVilage[self.inCountry][1];
                    }
                    if (self.near(self.x, self.y, xx, yy, 120)) {
                        self.writeActorPos(mActor, Map.npcVilage.get(i));
                    }
                }
            }
        }
        for (int i = 0; i < self.map.npcPrivateMap.size(); ++i) {
            havedata = true;
            final Char aa = self.map.npcPrivateMap.get(i);
            final int xx = aa.x;
            final int yy = aa.y;
            if (self.near(self.x, self.y, xx, yy, 120)) {
                self.writeActorPos(mActor, aa);
            }
        }
        if (havedata) {
            self.sendMessage(mActor);
        }
        havedata = false;
        if (!self.map.isDun()) {
            final Message mActor2 = new Message(4);
            final Vector<Char> players = self.getAllPlayerMap();
            for (int k = players.size() - 1; k >= 0; --k) {
                try {
                    final Char p1 = players.get(k);
                    // DAY LA CHO LAM LO NPC "ADMIN". No nam trong Map.npcVilage nhung
                    // MapKhoiTao con addPlayer() no vao map.allPlayers, nen vong quet nay
                    // cung nhat duoc no. Bo loc duy nhat o duoi la `!p1.isAdmin`, ma bot
                    // nay khong bao gio duoc dat isAdmin (mac dinh false) -> lot qua het
                    // va nguoi choi thuong nhin thay NPC dang le chi admin moi thay.
                    // Bo qua han o day: khong gui, va cung khong ghi vao nearChars nen
                    // vong lam moi vi tri ben tren cung khong co gi de gui lai.
                    if (Map.laNpcAnCuaAdmin(p1) && !self.isAdmin) {
                        continue;
                    }
                    if (p1 != null) {
                        final boolean givcard2 = Char.givingCard(p1.inCountry, p1.id);
                        if (self != p1) {
                            if ((p1.mapID != self.mapID && p1.isBot == -1) || (p1.mapID == self.mapID && p1.region != self.region && !p1.hienThiMoiRegion) || (p1.isBot == -1 && p1.inCountry != self.inCountry && !self.isPublicMap())) {
                                self.charOutRange(p1);
                                try {
                                    self.map.removePlayer(self.inCountry, p1);
                                } catch (final Exception ex) {
                                }
                            } else if (!p1.exit) {
                                if (!UtilHTBB.coId(self.nearChars, p1.id)) {
                                    if (self.nearChars.size() - self.nNpc < self.nNearchar || p1.pk != 0 || p1.isKiller || self.checkInParty(p1) || p1.isBot != -1 || self.mapID == Map.idMapDautruong || self.map.isMapChienTruongMoba() || (self.mapID == Map.idMapTown && (Map.nwar[self.myCountry] || Map.getTown[self.myCountry]))) {
                                        final boolean near2 = self.near(p1, self.rangeAddMonster[0]);
                                        if ((near2 && p1.inCountry == self.inCountry) || (self.isPublicMap() && near2) || p1.isBot != -1) {
                                            final Message mb = self.sendBuffToUser(p1);
                                            self.sendMessage(mb);
                                            mb.cleanup();
                                            final Message mkef = self.sendEffKham(p1);
                                            if (mkef != null) {
                                                self.sendMessage(mkef);
                                            }
                                            p1.sendEffToChar(self);
                                            boolean send2 = true;
                                            if (p1.isBot == -35 && !Char.canSendRaoVat()) {
                                                send2 = false;
                                            }
                                            if (!p1.isAdmin && send2) {
                                                self.writeActorPos(mActor2, p1);
                                                havedata = true;
                                            }
                                            if (p1.isBot != -1) {
                                                ++self.nNpc;
                                            }
                                            self.nearChars.add(p1.id);
                                        }
                                    }
                                } else if (!self.near(p1, self.rangeRemoveMonster[0]) && !self.checkInParty(p1) && p1.isBot == -1 && !givcard2) {
                                    // PHAI xoa theo ID (Short), KHONG duoc truyen ca doi tuong Char.
                                    //
                                    // LOI DA XAY RA THAT (05/09/2026, nguoi choi bao "nhan vat cu
                                    // an roi hien, 1 giay nhay 1-2 lan"): nearChars la Vector<Short>
                                    // (dong 432 them "p1.id"), nhung o day truyen "p1" la mot Char.
                                    // Java khop vao Vector.remove(Object) - tim mot Char trong danh
                                    // sach toan Short nen KHONG BAO GIO tim thay, khong xoa gi ca,
                                    // va lang le tra ve false (khong he bao loi).
                                    //
                                    // Hau qua: id nguoi choi o xa KHONG bao gio roi khoi nearChars,
                                    // nen dieu kien nay dung MOI VONG - server ban lai goi "xoa nhan
                                    // vat nay" (charOutRange) deu deu MOI 300ms (Map.DELAY_UPDATE_MAP),
                                    // trong khi goi di chuyen cua ho lai lam client ve lai ngay. Do
                                    // dung la nhip nhay ~3 lan/giay nguoi choi thay.
                                    // Ba cho xoa khac trong tep nay (dong 315/318/334) dung dang chi
                                    // so remove(j) nen van chay dung - chi rieng cho nay sai.
                                    //
                                    // Sua kem: vi id khong bao gio bi xoa, o "nguoi choi o gan"
                                    // (nNearchar, mac dinh 8) cung khong bao gio duoc tra lai - di
                                    // qua 8 nguoi la nguoi thu 9 tro di khong con duoc dang ky nua.
                                    self.nearChars.remove(Short.valueOf(p1.id));
                                    self.charOutRange(p1);
                                }
                            }
                        }
                    }
                } catch (final Exception e2) {
                    // Cung ly do voi vong tren (self.nearChars): "break" o day tung lam MOI
                    // doi tuong con lai trong danh sach (chi so thap hon k hien tai, vi vong
                    // nay cung duyet tu cuoi ve dau) khong bao gio duoc xet toi trong tick nay
                    // - ke ca cac bot NPC dat vao map tu luc khoi dong (o dau danh sach, tuc
                    // chi so THAP nhat, nen la nhom de mat nhat). Trong luc dong nguoi (su
                    // kien boss the gioi...), chi can MOT nguoi choi trong danh sach gay loi
                    // (vi du vua thoat giua chung) la NPC nhu "hoa tieu"/"xa phu" bien mat het
                    // cho toi lan tick sau co may man khong loi (su co 02/09/2026). "continue"
                    // giu dung y dinh chan doan (in ra roi bo qua) nhung khong lam hong ca
                    // nhung doi tuong con lai.
                    System.out.println("LOI TIM NEARCHAR TRONG CHAR ");
                    continue;
                }
            }
            if (havedata) {
                self.sendMessage(mActor2);
            }
        }
    }

    static void subUpdate(Char self) {
        if (self.isBot != -1) {
            return;
        }
        self.handleModelEffects();
        if (self.hp > 0 && self.getIdEffTool5l() > -1) {
            if (self.wModel.isBachMi() || self.wModel.isThuyVuHongSam()) {
                if (System.currentTimeMillis() - self.timeSendEffTool5l >= 0L && System.currentTimeMillis() - self.timeBuffGiamSatThuong <= 0L) {
                    final Message mef = MessageCreator.createMsgNewEffectSkill5Long(self.getIdEffTool5l(), 10000, self.id, self.cat, true, false);
                    self.sendMessage(mef);
                    self.sendToNearPlayer(mef);
                    self.timeSendEffTool5l = System.currentTimeMillis() + 30000L;
                }
            } else if (System.currentTimeMillis() - self.timeSendEffTool5l >= 0L) {
               
                final Message mef = MessageCreator.createMsgNewEffectSkill5Long(self.getIdEffTool5l() , 5000, self.id, self.cat, true, false);
                self.sendMessage(mef);
                self.sendToNearPlayer(mef);
                self.timeSendEffTool5l = System.currentTimeMillis() + 10000L;
            }
        }
        if (self.forbitChat > 0 && (System.currentTimeMillis() - self.timeForbitChat) / 60000L >= 1L) {
            --self.forbitChat;
            if (self.forbitChat == 0) {
                self.sendMessage(MessageCreator.createMsgChat(self.id, "Thời gian cấm chat của bạn đã kết thúc."));
            }
            self.timeForbitChat = System.currentTimeMillis();
        }
        ++self.countSendchat;
        if (self.countSendchat >= 4) {
            if (self.allMsgChatRieng.size() > 0) {
                try {
                    final InfoChatRieng info = self.allMsgChatRieng.remove(0);
                    final Message m = new Message(-5);
                    m.dos.writeByte(1);
                    m.dos.writeUTF(info.charChat);
                    m.dos.writeUTF(info.infoChat);
                    self.sendMessage(m);
                } catch (final Exception ex) {
                }
            }
            self.countSendchat = 0;
        }
        if (self.vip > 0 && Map.ALL_CHAR_VIP.get(self.charname) == null) {
            self.vip = 0;
            try {
                self.calculateAttrib();
                self.sendMessage(MessageCreator.createMainCharInfoMessage(self));
                self.sendToNearPlayer(MessageCreator.createCharInfo(self));
            } catch (final Exception ex2) {
            }
        }
        if (!self.dayLogin.equals(Char.getDayOpen(0L))) {
            self.countnvVulan = 0;
            self.maxKill = 0;
            self.usingSummons = 0;
            self.dayLogin = Char.getDayOpen(0L);
            self.countQuestClan = 0;
            self.pointActiveQuest += 100;
            self.receiveQuestVulan = 0;
            self.receiveGiftEvent = 0;
            self.nBinhTra = 100;
            self.nHopMut = 100;
            self.countTime = 0;
            self.countMonsterKill = 0;
            self.flower = 0;
            self.timeXP = 0;
            self.giveMoneyClan = 1;
            self.diemQuaiHomNay = 0;
            self.soQuaiHomNay = 0;
            LauDaiNhanVat.khiDoiNgay(self); // GD2 7.1 lich dang nhap (tat co: khong lam gi)
            if (self.pointActiveQuest > 300) {
                self.pointActiveQuest = 300;
            }
            if (self.subQuest.id_quest > -1) {
                self.subQuest.reset();
                self.subQuest.id_quest = -1;
                MessageCreator.createMsgCharQuest(self, 0);
            }

            self.luot_van_tieu = 3;
            // Toi da 2 luot MUA them the van tieu/ngay (khong cong them theo vip) + 3
            // luot mien phi = 5 tieu/ngay. LUU Y: potions[82] (kho ve "the van tieu"
            // chua dung) KHONG duoc reset/void o day du user muon "mua ngay nao dung
            // het ngay do" - o cung index nay con dung chung cho nguyen lieu "tre" cua
            // su kien Trung Thu (doMenuNpcHangNga/MapMenuNpc.java), reset nham se xoa
            // mat tre cua nguoi choi dang giu cho su kien dang/sap dien ra. Thay vao do
            // chan tran o DIEM DUNG (doUsePotion.java case 82: luot_van_tieu >= 5 thi
            // tu choi + hoan lai ve) - ve co tich tru bao nhieu cung khong the vuot qua
            // 5 tieu/ngay tren thuc te.
            self.buy_luot_van_tieu = 2;
            self.cuop_tieu = 5;
            if (self.lvDetail.lv >= 34) {
                if (self.ntangtoc < 6) {
                    final int[] potions = self.potions;
                    final int n = 7;
                    potions[n] += 6 - self.ntangtoc;
                    Database.instance.saveOrtherLog("", self.charname, "tang " + (6 - self.ntangtoc) + " binh tang  toc", "btt");
                    self.ntangtoc = 6;
                } else if (self.ntangtoc == 6 && self.potions[7] < 6) {
                    final int[] potions2 = self.potions;
                    final int n2 = 7;
                    potions2[n2] += 6;
                    Database.instance.saveOrtherLog("", self.charname, "tang 6 binh tang  toc", "btt");
                }
            }
            // Bo sung kiem toan A1: +1000 luong khoa/ngay/nhan vat cap >= 40 truoc day cong thang
            // vao truong (bo qua Char.KHOA_TIEN, tran, log). Tat co: van 1000 nhung qua addLuongLock
            // + log. Bat co sv.lau_dai: BO - lich dang nhap 7 ngay (3..15) thay the (LauDaiNhanVat.khiDoiNgay).
            if (self.lvDetail.lv >= 40 && !com.htbb.pokemon.LauDai.bat()) {
                self.addLuongLock(1000);
                Database.instance.saveOrtherLog("", self.charname, "qua doi ngay +1000 luong khoa (cap >= 40)", "luongngay");
            }
            if (self.vip > 0 && self.vip <= 3) {
                final int[] potions3 = self.potions;
                final int n3 = 100;
                potions3[n3] += Char.soluong_loa_vip[self.vip];
                self.sendMessage(MessageCreator.createCharInventoryMessage(self, 0));
                self.sendMessage(MessageCreator.createMsgChat(self.id, "Nhận được " + self.vip + " loa."));
                Database.instance.saveOrtherLog("", self.charname, "nhan loa vip", "loavip");
            }
            if (Char.isDemTrungThu() && self.lvDetail.lv >= 20) {
                self.potions[125] = 1;
                self.sendMessage(MessageCreator.createCharInventoryMessage(self, 0));
                self.sendMessage(MessageCreator.createMsgChat(self.id, "Nhận được hộp quà trung thu."));
                Database.instance.saveOrtherLog("", self.charname, "nhan qua tang trung thu", "hopquatrungthu");
            }
            if ((Char.isDemHaloween() && self.lvDetail.lv >= 40 && self.receiveGiftOldChar == 1) || self.dayLogin.equals("2017-10-30")) {
                self.potions[141] = 1;
                Database.instance.saveOrtherLog("", self.charname, "nhan qua tang haloween", "hopquahlw");
            }
//            if (self.lvDetail.lv >= 40 && isDemHaloween2023()) {
//                int[] valuemn = {5, 5, 10, 30, 50};
//                int randomMN = Map.r.nextInt(valuemn.length);
//                Item newitem = Map.doCreateMatNa3(self, 10080, Char.ID_MAT_NA[randomMN], randomMN, valuemn[randomMN]);
//                self.sendMessage(MessageCreator.createServerAlertMessage("Nhận " + newitem.getName() + " 7 ngày", ""));
//                Database.instance.saveOrtherLog("", self.charname, "nhan qua tang haloween", "hopquahlw");
//            }
            if (Char.isDemNoel()) {
                final int lv = self.lvDetail.lv;
            }
            if (!Map.openLog && self.dayLogin.compareTo("2014-08-06") < 0) {
                self.doAddGiftSn();
            }
            if (UtilHTBB.getDayOfMonth() == 1) {
                Database.instance.check();
                Database.instance.getDiemNapXaiLuong(self);
            }
        }
        if (self.potions[78] > 0 && self.idClan == -1) {
            self.potions[78] = 0;
            self.potions[28] = 0;
            if (self.rankGov == 0) {
                self.potions[85] = 0;
                self.calculateAttrib();
                try {
                    self.sendMessage(MessageCreator.createMainCharInfoMessage(self));
                } catch (final IOException ex3) {
                }
            }
            self.sendMessage(MessageCreator.createCharWearingMessage(self, self));
            self.sendToNearPlayer(MessageCreator.createCharInfo(self));
            MessageCreator.createMsgCharMonster(self, self);
        }
        boolean sendupdate = false;
        if (self.fruit != null) {
            for (int i = 0; i < self.fruit.length; ++i) {
                if (self.fruit[i] != null) {
                    final boolean a = self.fruit[i].update(self);
                    if (!sendupdate) {
                        sendupdate = a;
                    }
                }
            }
        }
        if (sendupdate) {
            MessageCreator.createMsgCharFruit(self, 0, 0);
        }
        final Message m = new Message(4);
        boolean haveData = false;
        if (!self.map.isDun() && self.isBot == -1) {
            for (int j = 0; j < self.nearMons.size(); ++j) {
                // nearMons co the bi luong khac don (doi map / dich chuyen) GIUA lan kiem size va
                // get(j) -> ArrayIndexOutOfBounds (bat duoc khi chay 50 bot, GD2). Doc an toan, het
                // phan tu thi dung vong lap; moi remove(j) ben duoi cung kiem j < size.
                final Short idQuaiGan;
                try {
                    idQuaiGan = self.nearMons.get(j);
                } catch (final ArrayIndexOutOfBoundsException hetPhanTu) {
                    break;
                }
                final Monster mt = self.map.getMonster(idQuaiGan, (self.mapID == 225 || self.mapID == 226) ? self.idWedding : self.inCountry, self.region);
                if (mt != null) {
                    if (mt.isDead || !self.near(mt, self.rangeRemoveMonster[0])) {
                        // Kiem j < size: nearMons co the bi luong khac don (doi map/dich chuyen) giua
                        // vong lap -> remove(j) nem ArrayIndexOutOfBounds (thay khi test tai 50 bot).
                        if (Map.isNewVersion && !mt.isBoss && mt.getMonsterTemplate().id != Map.idGhost && j < self.nearMons.size()) {
                            self.nearMons.remove(j);
                        }
                    } else if (!mt.isMonsterVantieu()) {
                        if ((!Map.isNewVersion || mt.isBoss || mt.getMonsterTemplate().id == Map.idGhost || mt.isMaterialMons()) && (!mt.isBossVanTienTran() || mt.isBossTruRong())) {
                            self.writeActorPos(m, mt);
                            haveData = true;
                        }
                        if (mt.isBoss() && System.currentTimeMillis() - mt.timeAddBuff >= 0L && mt.addEffBuff(EffectBuff.BUFF_DAM_BOSS, System.currentTimeMillis() + 15000L, EffectBuff.BY_ACTOR, 0) != null) {
                            mt.sendEffToChar(self);
                            mt.timeAddBuff = System.currentTimeMillis() + 20000L;
                        }
                        if ((self.near(mt, self.map.isvanTienTran() ? 240 : 120) || mt.isBoss || mt.idTemplate == 83) && self.hp > 0 && mt.target == null && mt.isActive() && !self.isAdmin && mt.isEnemy(self)
                        && !VatPhamDungLauDai.xuaQuai(self, mt)) { // GD2b thuoc xua quai
                            if (mt.region == self.region) {
                                mt.target = self;
                            } else if (j < self.nearMons.size()) {
                                self.nearMons.remove(j);
                            }
                        }
                    }
                } else if (j < self.nearMons.size()) {
                    self.nearMons.remove(j);
                }
            }
        }
        if (haveData) {
            self.sendMessage(m);
            if (!Map.isNewVersion) {
                self.nearMons.removeAllElements();
            }
        }
        self.moved = false;
        self.checkOutDelay();
        if (self.isMonster && self.map != null && self.map.haveCharMonter[Map.openLog ? 0 : self.myCountry] && System.currentTimeMillis() - self.map.timeLiveMonster[Map.openLog ? 0 : self.myCountry] >= 300000L) {
            self.map.charMonsterDissapear(self);
        }
        if (self.isMonster && !Map.idMapMONSTER.get(Map.openLog ? 0 : self.myCountry).contains(self.map.mapId)) {
            self.map.charMonsterDissapear(self);
        }
    }

    static void setBuff2Char(Char self, final Message message) {
        

        try {
            if (self.hp <= 0) {
                return;
            }
            final int charID = message.dis.readShort();
            final int skilltype = message.dis.readByte();
            final int buffEff = message.dis.readByte();
            if (buffEff == 27 && self.charClass != 2) {
                return;
            }
            if (!CharManager.isBuffActive(self.charClass, buffEff)) {
                self.sendMessage(MessageCreator.createServerAlertMessage(Text.CAN_NOT_USE, ""));
                return;
            }
            final int bid = CharManager.getSkillBuffByID(self.charClass, buffEff);
            if (bid != -1) {
                Char c = null;
                if (charID == self.id) {
                    c = self;
                } else {
                    c = self.getCharFromNearChar(charID);
                }
                if (c == null) {
                    final Message m = MessageCreator.createServerAlertMessage(Text.NGUOI_CHOI_DUNG_QUA_XA, "");
                    self.sendMessage(m);
                    m.cleanup();
                    return;
                }
                final int buff = bid;
                if (buff == -1) {
                    final Message i = MessageCreator.createServerAlertMessage("Pokémon bạn chọn không có kỹ năng này.", "");
                    self.sendMessage(i);
                    i.cleanup();
                    return;
                }
                if (self.skill[buff + 4] + self.addMoreLevelSkill[buff + 4] == 0) {
                    return;
                }
                final int mplost = CharManager.SKILL_MP[self.charClass][buff][self.skill[buff + 4] + self.addMoreLevelSkill[buff + 4]];
                if (self.mp + self.percentBuff[1] < mplost) {
                    return;
                }
                self.mp -= mplost;
                int div = 1;
                if (buffEff != 27) {
                    div = 2;
                }
                if (System.currentTimeMillis() - self.timeLastUseSkills[buff + 4] < CharManager.SKILL_COOLDOWN[self.charClass][buff + 4][self.skill[buff + 4] + self.addMoreLevelSkill[buff + 4]] * 100 / div) {
                    if (buffEff == 27) {
                        final long time = CharManager.SKILL_COOLDOWN[self.charClass][buff + 4][self.skill[buff + 4] + self.addMoreLevelSkill[buff + 4]] / 10 - (System.currentTimeMillis() - self.timeLastUseSkills[buff + 4]) / 1000L;
                        self.sendMessage(MessageCreator.createServerAlertMessage("Chỉ có thể sử dụng sau " + time + "s", ""));
                    }
                    return;
                }
                int[] tempBuff;
                int j;
                for (tempBuff = new int[]{19, 20, 22, 23, 24, 25, 27}, j = 0; j < tempBuff.length && tempBuff[j] != buffEff; ++j) {
                }
                if (buffEff == 27) {
                    if (c.inCountry != self.inCountry) {
                        return;
                    }
                    if (c.mapID != self.mapID) {
                        return;
                    }
                    if (c.region != self.region) {
                        return;
                    }
                    if (c.hp > 0) {
                        final Message k = MessageCreator.createServerAlertMessage("Không thể hồi sinh người còn HP.", "");
                        self.sendMessage(k);
                        k.cleanup();
                        return;
                    }
                    if (!c.canHSChiemThanh() && Map.getTown[self.inCountry]) {
                        long time2 = 1L - (System.currentTimeMillis() - c.timeWaitComeHome) / 1000L;
                        if (time2 < 1L) {
                            time2 = 1L;
                        }
                        try {
                            Thread.sleep(System.currentTimeMillis() - c.timeWaitComeHome);
                        } catch (final Exception ex) {
                        }
                    }
                }
                c.upPercentAtribute(c, buffEff, j, self.skill[buff + 4] + self.addMoreLevelSkill[buff + 4], self);
                final Message k = new Message(51);
                k.dos.writeByte(1);
                k.dos.writeShort(charID);
                k.dos.writeByte(buffEff);
                c.timeUseBuff[j] = System.currentTimeMillis();
                c.countDown[j] = 0;
                c.lvBuff[j] = (byte) (self.skill[buff + 4] + self.addMoreLevelSkill[buff + 4]);
                c.timeOut[j] = self.timeOut[j];
                k.dos.writeShort(self.timeOut[j]);
                c.arrayBuff[j] = (byte) buffEff;
                k.dos.writeByte(self.skill[buff + 4] + self.addMoreLevelSkill[buff + 4]);
                self.timeLastUseSkills[buff + 4] = System.currentTimeMillis();
                self.sendMessage(k);
                if (c.id != self.id) {
                    c.sendMessage(k);
                    c.sendToNearPlayer(k);
                } else {
                    self.sendToNearPlayer(k);
                }
                k.cleanup();
            }
        } catch (final Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Luu nhan vat khi mot PHIEN luyen tu dong ket thuc.
     *
     * Truoc day ca duong luyen do (thu cong lan tu dong) khong he goi luu mot lan nao:
     * moi thay doi - muc cuong hoa, chi so mon do, so luyen kim duoc da dot, xu da tru -
     * chi nam trong bo nho cho toi lan luu tu dong ke tiep (5 den 11 phut mot lan) hoac
     * cho toi luc thoat game. Ma mot phien luyen tu dong chay moi ~300ms mot nhip, tuc
     * vai tram luot trong nua phut. Mat ket noi bat thuong giua khoang do la mat sach,
     * dung cam giac "vao lai bi lui ve truoc luc lam do".
     *
     * Chi luu MOT lan khi phien ket thuc, khong luu moi nhip: luu la mot cau UPDATE
     * lon tren bang MyISAM (khoa ca bang), lam moi nhip se ghim ca server.
     */
    private static void luuSauKhiLuyen(final Char self) {
        try {
            Database.instance.saveCharAuto(self);
        } catch (final Exception ex) {
            ex.printStackTrace();
        }
    }

    static void doAutoImbue(Char self) {
        if (!self.startAutoImbue) {
            return;
        }
        final int[] percent = {100, 90, 85, 80, 70, 30, 15, 10, 7, 4, 3, 2, 1, 1, 0};
        final Item item = self.getItemFormVector(self.iItems, (short) self.itemImbue);
        // iItems.contains: nhip nay chay tren luong cap nhat cua Map/Region trong khi mot
        // lenh tuc thoi khac cua CHINH nguoi choi (gui kho/rao ban/giao dich, chay tren
        // luong phien rieng) co the vua dua item nay ra khoi iItems - coi nhu khong tim
        // thay, di vao chinh nhanh else ben duoi thay vi tiep tuc sua doi mot item da roi tui.
        if (item != null && self.iItems.contains(item)) {
            if (!Map.isItemCanImbue(item.getTemplate().type)) {
                // sendStopAutoImbue = tat co + goi -69 (mo khoa ScreenDapDo) + bao loi.
                self.sendStopAutoImbue("Không thể cường hoá trang bị này");
                self.idGemLkd = -1;
                return;
            }
            if (item.isManhSuuTap() || item.isTBSuuTap() || item.isCanh() || item.isCaiTrangSuuTap()) {
                self.sendStopAutoImbue("Không thể cường hoá ");
                self.idGemLkd = -1;
                return;
            }
            boolean isLock = false;
            final int slDaMayMan = self.getSLMM();
            String infoGemItem = String.valueOf(self.idGemLkd) + "_" + self.lkdLock;
            if (self.idGemBH > -1) {
                infoGemItem = String.valueOf(infoGemItem) + " BH " + self.idGemBH + "_" + self.BHLock;
            }
            if (self.idGemMM > -1) {
                infoGemItem = String.valueOf(infoGemItem) + " DMM " + self.idGemMM + "_" + slDaMayMan + "_" + self.MMLock;
            }
            if (self.lkdLock || self.BHLock || self.MMLock) {
                isLock = true;
            }
            if (item.plus < self.plus && com.htbb.pokemon.LauDai.bat()) {
                // GD2 2.5: ban dai han - cung ham voi nang cap tay (NangCapLauDaiGame).
                autoImbueLauDai(self, item);
                return;
            }
            if (item.plus < self.plus) {
                // Kiem TRUOC khi roll: ban cu tru xu/nguyen lieu o CUOI nhip (dong ben duoi)
                // roi moi kiem "con du cho nhip SAU khong" - nhip HIEN TAI van roll/ap dung
                // mien phi neu vua het xu/nguyen lieu dung luc nay, va self.xu co the bi tru
                // AM (khong duoc Math.max(...,0) nhu truong luong). Chuyen kiem tra ra truoc.
                if (!self.enoughtLkd() || self.xu < 5000L) {
                    self.idGemLkd = -1;
                    final Message jThieu = new Message(-69);
                    try {
                        jThieu.dos.writeByte(1);
                        jThieu.dos.writeUTF("Dừng luyện tự động (hết nguyên liệu hoặc xu), hiện tại +" + item.plus);
                        jThieu.dos.writeByte(1);
                        self.sendMessage(jThieu);
                    } catch (final Exception exThieu) {
                    }
                    self.startAutoImbue = false;
                    luuSauKhiLuyen(self);
                    return;
                }
                final int pcInsu = Map.random(100);
                final int bh = self.getPcBHAuto();
                final int pc = Map.random(100);
                final int up = self.calculatePercenUp(self, percent[item.plus]);
                if (pc <= percent[item.plus] + up) {
                    final Item item2 = item;
                    ++item2.plus;
                    final int[] plus = {4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 6, 10};
                    for (int i = 0; i < 10; ++i) {
                        final short[] atb = item.atb;
                        final int n = i;
                        atb[n] += (short) ((i > 0) ? 1 : plus[item.plus]);
                    }
                    Message m = new Message(-69);
                    try {
                        m.dos.writeByte(1);
                        m.dos.writeUTF("Thành công +" + item.plus);
                        m.dos.writeByte(0);
                        self.sendMessage(m);
                        final String sub = String.valueOf(item.getTemplate().name) + "_" + item.plus + "|" + item.getAttribute() + "|" + item.getDBInfo();
                        Database.instance.saveOrtherLog("tob_log_other_kham", self.charname, String.valueOf(infoGemItem) + " imbue" + sub + " > ", "ibok");
                    } catch (final Exception ex) {
                    }
                    if (item.plus == self.plus) {
                        self.startAutoImbue = false;
                        m = new Message(-69);
                        try {
                            m.dos.writeByte(1);
                            m.dos.writeUTF("Hoàn thành +" + item.plus);
                            m.dos.writeByte(1);
                            self.sendMessage(m);
                        } catch (final Exception ex2) {
                        }
                        luuSauKhiLuyen(self);
                    }
                } else {
                    final Message j = new Message(-69);
                    try {
                        if (pcInsu > bh) {
                            final Item item3 = item;
                            --item3.plus;
                            if (item.plus > 0) {
                                final int[] plus2 = {4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 6, 10};
                                for (int k = 0; k < 10; ++k) {
                                    final short[] atb2 = item.atb;
                                    final int n2 = k;
                                    atb2[n2] -= (short) ((k > 0) ? 1 : plus2[item.plus + 1]);
                                    if (item.atb[k] < 0) {
                                        item.atb[k] = 0;
                                    }
                                }
                            }
                        }
                        j.dos.writeByte(1);
                        j.dos.writeUTF("Thất bại +" + item.plus);
                        j.dos.writeByte(0);
                        self.sendMessage(j);
                        final String sub2 = String.valueOf(item.getTemplate().name) + "_" + item.plus + "|" + item.getAttribute() + "|" + item.getDBInfo();
                        Database.instance.saveOrtherLog("tob_log_other_kham", self.charname, String.valueOf(infoGemItem) + " imbue" + sub2 + " > ", (pcInsu <= bh) ? "fib" : "fibd");
                        j.cleanup();
                    } catch (final Exception e) {
                        e.printStackTrace();
                    }
                }
                if (isLock) {
                    item.lock = 1;
                }
                self.delGem(self.idGemLkd, 1, self.lkdLock);
                if (self.idGemBH > -1) {
                    self.delGem(self.idGemBH, 1, self.BHLock);
                }
                if (self.idGemMM > -1) {
                    self.delGem(self.idGemMM, slDaMayMan, self.MMLock);
                }
                self.sendMessage(MessageCreator.createCharGemItem(self));
                try {
                    self.sendMessage(MessageCreator.createMsgItemInfo(item));
                } catch (final Exception e2) {
                    e2.printStackTrace();
                }
                if (!self.startAutoImbue) {
                    self.idGemLkd = -1;
                }
                self.xu -= 5000L;
                self.sendMessage(MessageCreator.createCharInventoryMessage(self, 0));
                if (self.startAutoImbue && (!self.enoughtLkd() || self.xu < 5000L)) {
                    self.idGemLkd = -1;
                    final Message j = new Message(-69);
                    try {
                        j.dos.writeByte(1);
                        // lo #7 loi.xlsx: truoc day luon ghi "Hoan thanh +X" ke ca khi
                        // dung giua chung vi het nguyen lieu/xu (chua toi muc muon), khien
                        // nguoi choi tuong la loi he thong. Tach ro 2 truong hop.
                        if (item.plus >= self.plus) {
                            j.dos.writeUTF("Hoàn thành +" + item.plus);
                        } else {
                            j.dos.writeUTF("Dừng luyện tự động (hết nguyên liệu hoặc xu), hiện tại +" + item.plus);
                        }
                        j.dos.writeByte(1);
                        self.sendMessage(j);
                    } catch (final Exception ex3) {
                    }
                    self.startAutoImbue = false;
                    luuSauKhiLuyen(self);
                }
            } else {
                self.idGemLkd = -1;
                self.sendStopAutoImbue("Vật phẩm đã đạt mức mong muốn");
            }
        } else {
            // lo #7 loi.xlsx: item null (da ban/gui kho/het han thue giua luc dang auto)
            // truoc day khong co nhanh else - co startAutoImbue ket true vinh vien, lam
            // lan bam Start ke tiep bi MapEpNgoc.doAutoImbue tu choi am tham (khong phan
            // hoi gi), nguoi choi thay nhu "bam la loi".
            self.idGemLkd = -1;
            self.sendStopAutoImbue("Không tìm thấy vật phẩm để luyện, đã dừng luyện tự động");
        }
    }

    /** Mot nhip luyen tu dong khi LauDai.bat(): het xu/da/luong khoa thi dung. */
    static void autoImbueLauDai(Char self, final Item item) {
        final String[] ra = {""};
        final int kieu = self.lkdLock ? 1 : 0;
        final int kq = NangCapLauDaiGame.thu(self, item, self.idGemLkd, kieu, self.idGemBH > -1, ra);
        boolean dung = false;
        String tb = ra[0];
        if (kq == NangCapLauDaiGame.THIEU) {
            dung = true;
            tb = "Dừng luyện tự động: " + ra[0] + " Hiện tại: +" + item.plus + ".";
        } else if (item.plus >= self.plus || kq == NangCapLauDaiGame.DA_TOI_DA) {
            dung = true;
            tb = "Hoàn thành: +" + item.plus + ".";
        }
        final Message m = new Message(-69);
        try {
            m.dos.writeByte(1);
            m.dos.writeUTF(tb);
            m.dos.writeByte(dung ? 1 : 0);
            self.sendMessage(m);
        } catch (final Exception ignored) {
        }
        self.sendMessage(MessageCreator.createCharGemItem(self));
        try {
            self.sendMessage(MessageCreator.createMsgItemInfo(item));
        } catch (final Exception ignored) {
        }
        self.sendMessage(MessageCreator.createCharInventoryMessage(self, 0));
        if (dung) {
            self.startAutoImbue = false;
            self.idGemLkd = -1;
            final Char nv = self; // (test KiemMatDoKhiTaoDo dem 3 duong cu dang "luuSauKhiLuyen(self)")
            luuSauKhiLuyen(nv);
        }
    }

    static void doPlayerMove(Char self, final Message message) {
        QuaHoatDong.hanhDong(self); // GD2: giay hoat dong theo tai khoan (tat co: khong lam gi)
        NhiemVuEngine.khiDiChuyen(self); // GD2b: dung tai diem / den noi
        try {
            final DataInputStream dis = message.dis;
            final long now = System.currentTimeMillis();
            if (self.beStune || self.freeze() || self.isStune() || self.isHoangLoan()) {
                final Message m = new Message(4);
                self.writeActorPos(m, self);
                self.sendMessage(m);
                return;
            }
            if (self.map.givingCard(self)) {
                final Message m = new Message(4);
                self.writeActorPos(m, self);
                self.sendMessage(m);
                return;
            }
            final int x = dis.readUnsignedShort();
            final int y = dis.readUnsignedShort();
            if ((self.x != x || self.y != y) && self.hp <= 0) {
                self.map.doUseHP(self, (short) 4, -1000000);
                self.actorDie();
                return;
            }
            if (!self.map.canMove(x, y)) {
                final Message i = new Message(4);
                self.writeActorPos(i, self);
                self.sendMessage(i);
                return;
            }
            if (self.map.mapId == Map.idMapTown && Map.getTown[self.inCountry] && !MapTown.congthanh.get(self.inCountry).isDead && !self.isAdmin && y < 1206) {
                self.y = 1280;
                final Message i = new Message(4);
                self.writeActorPos(i, self);
                self.sendMessage(i);
                return;
            }
            if (self.x == x && self.y == y) {
                return;
            }
            self.isThodia = false;
            if (self.hp > 0) {
                final long t = now - self.timeMove;
                // Gian cach toi thieu giua 2 buoc cung duoc dat theo toc do goc.
                // Di nhanh hon thi client gui goi day hon, neu van ep du 200ms
                // thi server ngu bu phan chenh lech -> chay giat cuc. Rut ngan
                // gian cach dung theo ti le toc do.
                long gianCach = 200L;
                final int tocDoBuoc = self.speed + self.getThanKhiSpeedBonus();
                if (tocDoBuoc > 5) {
                    gianCach = 200L * 5L / tocDoBuoc;
                }
                if (t < gianCach) {
                    Thread.sleep(gianCach - t);
                }
                int distance = (self.mapID == 17) ? 96 : 50;
                if (self.checkTanPhe()) {
                    distance = ((self.mapID == 17) ? 48 : 25);
                }
                self.timeMove = System.currentTimeMillis();
                self.timeLastMove = System.currentTimeMillis();
                self.lastMovex = x;
                self.lastMovey = y;
                self.lastMovex = 0;
                self.lastMovey = 0;
                if (self.getLevel() < 3) {
                    distance = 1000;
                }
                // Nguong tren duoc dat co dinh theo toc do goc (5). Khi nhan vat
                // di nhanh hon — cuoi thu, hoac co dong "Tang toc do di chuyen"
                // cua than khi — moi goi di chuyen cua client vuot qua nguong,
                // server tra ve vi tri cu nen nhin thay giat lui. Noi nguong
                // theo dung ti le toc do that de van chan duoc hack toc do.
                final int tocDoThat = self.speed + self.getThanKhiSpeedBonus();
                if (tocDoThat > 5 && distance < 1000) {
                    final int nguongMoi = distance * tocDoThat / 5;
                    if (nguongMoi > distance) {
                        distance = nguongMoi;
                    }
                }
                if (Map.abs(self.x - x) <= distance && Map.abs(self.y - y) <= distance) {
                    if (self.mapID == Map.idMapChanNui && !self.isDoChangeMap && Char.isOnLocaltion(x, y)) {
                        final CharInfo cinfo = MapChauBau.all_char_nui_kho_bau.get(self.charname);
                        MapChauBau map = (MapChauBau) RealController.mapList.get(42);
                        final RegionNuiChauBau rg = map.createNewRegion();
                        self.idRegionNuiChauBau = rg.idRegion;
                        map.loadTru(rg, rg.isChauBau = (Map.r.nextInt(500) < (cinfo.isLockGift ? 2 : 5)), self.lvDetail.lv);
                        rg.tend = cinfo.timeNuiChaubau;
                        if (rg.isChauBau) {
                            rg.tend = System.currentTimeMillis() + 180000L;
                            cinfo.timeNuiChaubau = rg.tend;
                            self.sendInfoNuiChauBau();
                            Database.instance.saveOrtherLog("", self.charname, "vao duoc nui chau bau", "chaubau");
                            Database.instance.saveLogThongKeChiTieu("VAO DUOC NUI CHAU BAU", 0, 1, 0L, "nui chau bau");
                        }
                        self.isDoChangeMap = true;
                        self.map.move2Map(self, 36 + Map.r.nextInt() % 5, 75 + Map.r.nextInt() % 5, 42, 0);
                        return;
                    }
                    if (self.mapID == Map.idMapChoLoiDai && Char.isOnLocaltionSanhChoLoiDai(x, y)) {
                        self.map.doReturnVillage(self);
                        return;
                    }
                    final int criX = self.x / 16;
                    final int criY = self.y / 16;
                    final int lastX = x / 16;
                    final int lastY = y / 16;
                    final Positions p1 = new Positions();
                    p1.x1 = criX;
                    p1.y1 = criY;
                    final Positions p2 = new Positions();
                    p2.x1 = lastX;
                    p2.y1 = lastY;
                    if (!self.checkToPoint(p1, p2)) {
                        final Message j = new Message(4);
                        self.writeActorPos(j, self);
                        self.sendMessage(j);
                        return;
                    }
                    self.x = x;
                    self.y = y;
                    final Message j = new Message(4);
                    self.writeActorPos(j, self);
                    try {
                        final Vector<Char> players = self.map.getAllPlayer(self.inCountry, self.region);
                        for (int k = 0; k < players.size(); ++k) {
                            if (players.get(k).id != self.id) {
                                players.get(k).sendMessage(j);
                            }
                        }
                        j.cleanup();
                    } catch (final Exception ex) {
                    }
                    if (self.charthanthu != null && (Map.abs(self.x - self.charthanthu.x) > 48 || Map.abs(self.y - self.charthanthu.y) > 48)) {
                        self.charthanthu.setXtoYto(x, y);
                    }
                    if (self.charCopy != null) {
                        self.charCopy.setXtoYto(x, y);
                    }
                    if (self.charHire != null && !Map.inRangeActor(self, self.charHire, 60)) {
                        self.charHire.setXtoYto(x, y);
                    }
                } else {
                    final Message l = new Message(4);
                    self.writeActorPos(l, self);
                    self.sendMessage(l);
                }
            }
            self.moved = true;
        } catch (final Exception ex2) {
        }
    }

}
