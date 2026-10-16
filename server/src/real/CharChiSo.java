package real;

import com.kpah.pokemon.CheDoPokemon;
import com.kpah.pokemon.SatThuongHe;


import server.TeamServer;
import io.Message;
import java.io.IOException;

import data.TuBinhTemplate;
import data.Pet;
import data.Animal;
import java.util.Vector;

/**
 * Than cac method tinh chi so chien dau cua Char: sat thuong, phong thu,
 * cong, HP/MP toi da, hieu ung than khi khi danh trung.
 *
 * Tach tu real/Char.java. Cac method VAN CON trong Char, giu NGUYEN chu ky
 * cu, chi than chuyen sang day. Tham so "Char self" thay cho "this".
 */
class CharChiSo {

    static void calculateMaxHPMP(Char self) {
        self.secondHealth = 0;
        Pet petUsing = self.petUsing;
        Animal animalRide = self.animalRide;
        if (self.map != null && self.map.isMapChienTruongMoba()) {
            final CharChienTruong c = MapChienTruongMoba.getCharChienTruong(self.charname);
            if (c != null && c.getNoiLuc() < 5) {
                petUsing = null;
            }
            if (c != null && c.getNoiLuc() < 7) {
                animalRide = null;
            }
        }
        for (int i = 0; i < self.wItems[self.slotWearing].length; ++i) {
            final Item item = self.wItems[self.slotWearing][i];
            if (item != null) {
                if (item.getType() == 12 && item.level <= self.lvDetail.lv) {
                    self.secondHealth += item.atb[5];
                }
                if (item.getType() == 19 && item.level <= self.lvDetail.lv) {
                    self.secondHealth += item.newAtb[1] * 1000;
                }
            }
        }
        switch (self.charClass) {
            case 0: {
                self.maxhp = (((petUsing != null) ? petUsing.getHealth() : 0) + (self.health + self.tnSucKhoe()) + self.getEffSkillClan(3) + self.upAbility[0] + self.abilityKham[3] + self.secondHealth + ((animalRide != null) ? animalRide.att[6] : 0)) * 80;
                self.maxmp = (((petUsing != null) ? petUsing.getspirit() : 0) + (self.spirit + self.tnTinhThan()) + self.getEffSkillClan(3) + self.upAbility[1] + self.abilityKham[2] + ((animalRide != null) ? animalRide.att[8] : 0)) * 20;
                break;
            }
            case 1: {
                self.maxhp = (((petUsing != null) ? petUsing.getHealth() : 0) + (self.health + self.tnSucKhoe()) + self.getEffSkillClan(3) + self.secondHealth + self.abilityKham[3] + self.upAbility[0] + ((animalRide != null) ? animalRide.att[6] : 0)) * 70;
                self.maxmp = (((petUsing != null) ? petUsing.getspirit() : 0) + (self.spirit + self.tnTinhThan()) + self.getEffSkillClan(3) + self.upAbility[1] + self.abilityKham[2] + ((animalRide != null) ? animalRide.att[8] : 0)) * 20;
                break;
            }
            case 2: {
                self.maxhp = (((petUsing != null) ? petUsing.getHealth() : 0) + (self.health + self.tnSucKhoe()) + self.getEffSkillClan(3) + self.secondHealth + self.abilityKham[3] + self.upAbility[0] + ((animalRide != null) ? animalRide.att[6] : 0)) * 60;
                self.maxmp = (((petUsing != null) ? petUsing.getspirit() : 0) + (self.spirit + self.tnTinhThan()) + self.getEffSkillClan(3) + self.upAbility[1] + self.abilityKham[2] + ((animalRide != null) ? animalRide.att[8] : 0)) * 50;
                self.maxmp += self.maxmp * self.buffPass[0] / 100;
                break;
            }
            case 3: {
                self.maxhp = (((petUsing != null) ? petUsing.getHealth() : 0) + (self.health + self.tnSucKhoe()) + self.getEffSkillClan(3) + self.upAbility[0] + self.secondHealth + self.abilityKham[3] + ((animalRide != null) ? animalRide.att[6] : 0)) * 70;
                self.maxmp = (((petUsing != null) ? petUsing.getspirit() : 0) + (self.spirit + self.tnTinhThan()) + self.getEffSkillClan(3) + self.upAbility[1] + self.abilityKham[2] + ((animalRide != null) ? animalRide.att[8] : 0)) * 20;
                break;
            }
            case 4: {
                self.maxhp = (((petUsing != null) ? petUsing.getHealth() : 0) + (self.health + self.tnSucKhoe()) + self.getEffSkillClan(3) + self.secondHealth + self.upAbility[0] + self.abilityKham[3] + ((animalRide != null) ? animalRide.att[6] : 0)) * 60;
                self.maxmp = (((petUsing != null) ? petUsing.getspirit() : 0) + (self.spirit + self.tnTinhThan()) + self.getEffSkillClan(3) + self.upAbility[1] + self.abilityKham[2] + ((animalRide != null) ? animalRide.att[8] : 0)) * 20;
                break;
            }
        }
        self.maxhp += ((animalRide != null) ? animalRide.att[3] : 0) + self.newAtb[0] * 1000;
        self.maxmp += ((animalRide != null) ? animalRide.att[4] : 0) + self.newAtb[1] * 1000;
        self.maxhp += self.getEffSkillClan(2);
        if (self.rideHorse == 1) {
            self.maxhp += 700;
            self.maxmp += 700;
            self.speed = 6;
        } else if (self.rideHorse == 2) {
            self.maxhp += 1000;
            self.maxmp += 1000;
        }
        if (self.wModel != null) {
            self.maxhp += self.wModel.getHp();
            self.maxmp += self.wModel.getMp();
        }
        


        long pcUphp = 0L;
        long pcUpmp = 0L;
        if (self.fullSet15 >= 10) {
            pcUphp = 12L;
            pcUpmp = 12L;
        } else if (self.fullSet15 >= 6) {
            pcUphp = 8L;
            pcUpmp = 8L;
        }
        if (petUsing != null) {
            pcUphp += petUsing.getCuongTrang();
            pcUpmp += petUsing.getCuongThan();
        }
        self.maxhp += (int) (self.maxhp * pcUphp / 100L);
        self.maxmp += (int) (self.maxmp * pcUpmp / 100L);
        int atkTubinh = 0;
        int atkTubinh2 = 0;
        for (int j = 0; j < self.tubinh.size(); ++j) {
            atkTubinh += self.tubinh.get(j).getHP();
            atkTubinh2 += self.tubinh.get(j).getMP();
        }
        self.maxhp += atkTubinh;
        self.maxmp += atkTubinh2;
        if (self.map != null && self.map.isMapChienTruongMoba()) {
            final CharChienTruong c2 = MapChienTruongMoba.getCharChienTruong(self.charname);
            if (c2 != null) {
                final long noiluc = c2.getNoiLuc();
                self.maxhp = (int) (self.maxhp * noiluc / 10L);
                self.maxmp = (int) (self.maxmp * noiluc / 10L);
            }
        }
        // Cai Tao Cai Trang: dong "Tinh Hoa" cong % HP toi da - va CHI HP (yeu cau
        // 02/09/2026; truoc do con cong ca MP va tan cong, da bo). Xem real/CaiTaoCaiTrang.
        if (self.wModel != null && (self.wModel.isCaiTaoMiNuong() || self.wModel.isCaiTaoSonTinh())) {
            final int percentCaiTao = self.wModel.itemModel.CaiTao;
            self.maxhp += (int) (self.maxhp * (percentCaiTao / 100.0));
        }
        if (self.isBatGioi()) {
            int level = self.getLevelCaiTrang();
        if (level >= 3 && level < 8) {
                self.maxhp += 1000;      // Level 3-7: chỉ +1000
            } else if (level >= 8 && level < 14) {
                self.maxhp += 3000;      // Level 8-13: chỉ +3000
            } else if (level == 14) {
                self.maxhp += 5000;      // Level 14: chỉ +5000
            } else if (level == 15) {
                self.maxhp += 10000;     // Level 15: chỉ +10000
            }
        }
        // SKIN LINH THU (Phuc Thu): cong THANG, dat SAU moi khoi nhan phan tram o tren
        // (fullset, pet, noi luc chien truong, cai tao) va TRUOC hai dong chan hp/mp.
        // Dat truoc cac khoi phan tram thi 2000 mau se bi nhan len theo, thanh nhieu hon
        // con so ghi trong mo ta; dat sau hai dong chan thi hp co the vuot maxhp.
        // Che do Pokemon (B1): cong % HP theo giai doan tien hoa (LoaiPokemon.phanTramGiaiDoan).
        if (com.kpah.pokemon.CheDoPokemon.bat() && self.pokeDex > 0) {
            self.maxhp += (int) ((long) self.maxhp * com.kpah.pokemon.LoaiPokemon.phanTramGiaiDoan(self.pokeDex) / 100L);
        }
        self.maxhp += SkinLinhThu.congHp(self);
        self.maxmp += SkinLinhThu.congMp(self);
        if (self.hp > self.maxhp) {
            self.hp = self.maxhp;
        }
        if (self.mp > self.maxmp) {
            self.mp = self.maxmp;
        }
    }

    static void calculateDefend(Char self) {
        self.setlan = 0;
        self.doclan = 0;
        self.banglan = 0;
        self.lualan = 0;
        self.baokich = 0;
        self.khangbanglan = 0;
        self.khangsetlan = 0;
        self.khangdoclan = 0;
        self.khanglualan = 0;
        self.hoihp = 0;
        self.hoimp = 0;
        self.tyleRotItem = 0;
        self.tylerotXu = 0;
        self.tanphe = 0;
        self.hutmau = 0;
        self.lamthinh = 0;
        self.tyleRotItem = 0;
        self.tylerotXu = 0;
        Pet petUsing = self.petUsing;
        Animal animalRide = self.animalRide;
        if (self.map != null && self.map.isMapChienTruongMoba()) {
            final CharChienTruong c = MapChienTruongMoba.getCharChienTruong(self.charname);
            if (c != null && c.getNoiLuc() < 5) {
                petUsing = null;
            }
            if (c != null && c.getNoiLuc() < 7) {
                animalRide = null;
            }
        }
        int defend1 = ((petUsing != null) ? petUsing.getagitity() : 0) + (self.agitity + self.tnKheoLeo()) + self.getEffSkillClan(3) + self.newAtb[3] + self.upAbility[3] + self.abilityKham[1] + ((animalRide != null) ? animalRide.att[7] : 0);
        int defen4 = (self.agitity + self.tnKheoLeo()) + self.getEffSkillClan(3) + self.newAtb[3] + self.upAbility[2] + self.abilityKham[1] + ((animalRide != null) ? animalRide.att[8] : 0);
        int defendVat = 0;
        int defendMa = 0;
        if (self.charClass == 3) {
            defend1 = defend1 * 4 / 3;
        }
        if (self.charClass == 4) {
            if (self.isSaTang() ) {
                int lv = self.getLevelCaiTrang();
                if (lv >= 1 && lv <= 15) {
                    defen4 = (self.agitity + self.tnKheoLeo()) + lv + self.getEffSkillClan(3) + self.newAtb[3] + self.upAbility[3] + self.abilityKham[1] + ((animalRide != null) ? animalRide.att[7] : 0);
                    defend1 = defend1 * 11 / 10;
                    defen4 = defen4 * 11 / 10;
                 
                }
            } else {
            defen4 = (self.agitity + self.tnKheoLeo()) + self.getEffSkillClan(3) + self.newAtb[3] + self.upAbility[3] + self.abilityKham[1] + ((animalRide != null) ? animalRide.att[7] : 0);
            defend1 = defend1 * 11 / 10;
            defen4 = defen4 * 11 / 10;
            }
        }
        int upMa = 0;
        int upVat = 0;
        int upPTma = 0;
        int upPtvat = 0;
        int upvP = 0;
        int upmP = 0;
        int upThuTbChar = 0;
        for (int i = 0; i < self.wItems[self.slotWearing].length; ++i) {
            final Item item = self.wItems[self.slotWearing][i];
            if (item != null) {
                final int t = item.getTemplate().type;
                if (item.level <= self.lvDetail.lv) {
                    if (t == 19) {
                        if (item.magic_physic == 0) {
                            defendMa += item.atb[6];
                            defendVat += item.atb[1];
                        } else if (item.magic_physic == 1) {
                            defendVat += item.atb[1];
                            defendMa += item.atb[6];
                        }
                        upPTma += item.otherAtt[2];
                        upPtvat += item.otherAtt[3];
                        upmP += item.otherAtt[2];
                        upvP += item.otherAtt[3];
                        self.baokich = item.otherAtt[4];
                    }
                    if (Map.isWeapone(t) || t == 12) {
                        self.hutmau += item.otherAtt[16];
                        self.tanphe += item.otherAtt[19];
                        self.lamthinh += item.otherAtt[21];
                    } else {
                        self.hoihp += item.otherAtt[17];
                        self.hoimp += item.otherAtt[18];
                        self.tyleRotItem += item.otherAtt[22];
                        self.tylerotXu += item.otherAtt[23];
                        self.khangbanglan += item.otherAtt[24];
                        self.khangsetlan += item.otherAtt[25];
                        self.khangdoclan += item.otherAtt[26];
                        self.khanglualan += item.otherAtt[31];
                        upThuTbChar += item.otherAtt[27];
                    }
                    if (t == 0) {
                        if (item.magic_physic == 2) {
                            defendVat += item.atb[1] * 2 / 10;
                            defendMa += item.atb[1] * 2 / 10;
                        } else if (item.magic_physic == 0) {
                            defendMa += item.atb[6] * 2;
                            defendVat += item.atb[1] * 2;
                        } else if (item.magic_physic == 1) {
                            defendVat += item.atb[1] * 2;
                            defendMa += item.atb[6] * 2;
                        }
                        upMa += item.lockAtb[1];
                        upVat += item.lockAtb[2];
                        upmP += item.lockAtb[1];
                        upvP += item.lockAtb[2];
                    }
                    if (t == 1) {
                        if (item.magic_physic == 2) {
                            defendVat += item.atb[1] / 10;
                            defendMa += item.atb[1] / 10;
                        } else if (item.magic_physic == 0) {
                            defendMa += item.atb[6];
                            defendVat += item.atb[1];
                        } else if (item.magic_physic == 1) {
                            defendVat += item.atb[1];
                            defendMa += item.atb[6];
                        }
                        upMa += item.lockAtb[1];
                        upVat += item.lockAtb[2];
                        upmP += item.lockAtb[1];
                        upvP += item.lockAtb[2];
                    }
                    if (t == 11) {
                        if (item.magic_physic == 2) {
                            defendVat += item.atb[1] / 10;
                            defendMa += item.atb[1] / 10;
                        } else if (item.magic_physic == 0) {
                            defendMa += item.atb[6];
                            defendVat += item.atb[1];
                        } else if (item.magic_physic == 1) {
                            defendVat += item.atb[1];
                            defendMa += item.atb[6];
                        }
                        upMa += item.lockAtb[1];
                        upVat += item.lockAtb[2];
                        upvP += item.lockAtb[2];
                        upmP += item.lockAtb[1];
                    }
                    if (t == 2) {
                        if (item.magic_physic == 2) {
                            defendVat += item.atb[1] / 10;
                            defendMa += item.atb[1] / 10;
                        } else if (item.magic_physic == 0) {
                            defendMa += item.atb[6];
                            defendVat += item.atb[1];
                        } else if (item.magic_physic == 1) {
                            defendVat += item.atb[1];
                            defendMa += item.atb[6];
                        }
                        upMa += item.lockAtb[1];
                        upVat += item.lockAtb[2];
                        upvP += item.lockAtb[2];
                        upmP += item.lockAtb[1];
                    }
                    if (t == 12) {
                        upMa += item.lockAtb[1];
                        upVat += item.lockAtb[2];
                        upvP += item.lockAtb[2];
                        upmP += item.lockAtb[1];
                    }
                    if (t == 10) {
                        if (item.magic_physic == 2) {
                            defendVat += item.atb[1] / 10;
                            defendMa += item.atb[1] / 10;
                        } else if (item.magic_physic == 0) {
                            defendMa += item.atb[6];
                            defendVat += item.atb[1];
                        } else if (item.magic_physic == 1) {
                            defendVat += item.atb[1];
                            defendMa += item.atb[6];
                        }
                        upMa += item.lockAtb[1];
                        upVat += item.lockAtb[2];
                        upvP += item.lockAtb[2];
                        upmP += item.lockAtb[1];
                    }
                    self.setlan += item.otherAtt[5];
                    self.doclan += item.otherAtt[6];
                    self.banglan += item.otherAtt[7];
                    self.lualan += item.otherAtt[28];
                }
            }
        }
      
        if (self.isLinhThueLan()) {
            self.lualan += 5;
      
        }
   
        if (self.itemHoaKyLan != null) {
            // chiSoPhanTramHoaKyLan cong them khi dang "ao hoa": bon o khang lan nay do bang
            // PHAN NGHIN (r.nextInt(1000) < khang) nen +10 diem phan tram = +100 - xem
            // Char.congAoHoaPhanTram (mot nguon cho ca tran lan hien thi tui do).
            self.khangbanglan += self.chiSoPhanTramHoaKyLan(24);
            self.khangsetlan += self.chiSoPhanTramHoaKyLan(25);
            self.khangdoclan += self.chiSoPhanTramHoaKyLan(26);
            self.khanglualan += self.chiSoPhanTramHoaKyLan(31);
        }
        // Hut mau cua mat na. Vong lap tren chi duyet wItems (o trang bi) nen mat na —
        // nam trong wModel — khong bao gio duoc cong, du tooltip van ghi "Hút máu: 10".
        if (self.wModel != null && self.wModel.matNa != null) {
            self.hutmau += self.wModel.matNa.otherAtt[16];
        }
        int defThuma = 0;
        int defThuvat = 0;
        int defThumaP = 0;
        int defThuvatP = 0;
        int upvatp = 0;
        int upmap = 0;
        final Vector<Item> awItems = self.getAnimalWearing();
        for (final Item item2 : awItems) {
            final int t2 = item2.getTemplate().type;
            if (t2 == 16 || t2 == 18 || t2 == 14) {
                if (item2.magic_physic == 0) {
                    defThuma += item2.atb[6];
                    defThuvat += item2.atb[1];
                    defThumaP += item2.atb[6];
                    defThuvatP += item2.atb[1];
                } else if (item2.magic_physic == 1) {
                    defThuvat += item2.atb[1];
                    defThuma += item2.atb[6];
                    defThuvatP += item2.atb[1];
                    defThumaP += item2.atb[6];
                }
                upMa += item2.lockAtb[1];
                upVat += item2.lockAtb[2];
                upvatp += item2.lockAtb[1];
                upmap += item2.lockAtb[2];
                upvP += item2.lockAtb[2];
                upmP += item2.lockAtb[1];
            }
        }
        // ---- THAN KHI: diem phong thu co ban + diem giap khi o to doi ----
        int tkThu = self.getThanKhiRaw(151);
        if (self.idClan != -1) {
            tkThu += self.getThanKhiRaw(157); // dang o to doi / bang hoi
        }
        defend1 += tkThu;
        defen4 += tkThu;
        self.defend_physic = defend1 + defendVat;
        self.defend_magic = defen4 + defendMa;
        self.defendVatPlayer = defend1 + defendVat;
        self.defendMaPlayer = defen4 + defendMa;
        if (self.charClass == 3) {
            self.defend_physic += self.defend_physic * self.buffPass[0] / 100;
            self.defend_magic += self.defend_magic * self.buffPass[0] / 100;
            upvP += self.buffPass[0];
            upmP += self.buffPass[0];
        }
        int defPet = 0;
        if (petUsing != null) {
            self.banglan += (short) petUsing.getBangLan();
            self.setlan += (short) petUsing.getSetLan();
            self.lualan += (short) petUsing.getLuaLan();
            self.doclan += (short) petUsing.getDocLan();
            defPet = petUsing.getPcUpDef();
            self.baokich += (short) petUsing.getbaoKich();
        }
        upvP += defPet;
        upmP += defPet;
        if (self.rankGov == 0) {
            upVat += 5;
            upMa += 5;
            upvP += 5;
            upmP += 5;
        }
        if (self.timeUp5Attribute > 0L) {
            upVat += 5;
            upMa += 5;
        }
        if (self.vip > 1) {
            upVat += 5;
            upMa += 5;
            if (self.vip == 3) {
                self.baokich += 30;
            }
        }
        if (petUsing != null) {
            upMa += petUsing.getCanKhon()[1] + petUsing.getKienCo();
            upVat += petUsing.getKimCang()[1] + petUsing.getKienCo();
            upThuTbChar += petUsing.getHoThe();
        }
        // SKIN LINH THU (Chien Lang): "+20% thu cho chu nhan". Cong vao ca BON bo dem
        // phan tram giong het cach rankGov/vip lam o tren: upVat/upMa ra thu hien trong
        // bang nhan vat, upvP/upmP ra thu dung khi danh nhau voi nguoi choi
        // (defendVatPlayer/defendMaPlayer, dong 516-517). Thieu cap sau thi nguoi choi
        // thay +20% trong bang ma vao PK lai khong duoc gi.
        final int pcThuSkin = SkinLinhThu.pcThu(self);
        if (pcThuSkin > 0) {
            upVat += pcThuSkin;
            upMa += pcThuSkin;
            upvP += pcThuSkin;
            upmP += pcThuSkin;
        }
        // SKIN LINH THU (Kim Cang Long): "Bao kich" ngau nhien theo vat pham (khac ca bon
        // skin tren, khong doc tu bang hang so) - xem SkinLinhThu.kimCangLongBaoKich.
        self.baokich += (short) SkinLinhThu.kimCangLongBaoKich(self);
        self.defend_physic += upVat * self.defend_physic / 100;
        self.defend_magic += upMa * self.defend_magic / 100;
        if (animalRide != null) {
            self.baokich += (short) animalRide.getBaoKich();
            self.defend_magic += self.defend_magic * (animalRide.att[1] + animalRide.attUp[1]) / 100;
            self.defend_physic += self.defend_physic * (animalRide.att[0] + animalRide.attUp[0]) / 100;
            upvP += animalRide.att[0] + animalRide.attUp[0];
            upmP += animalRide.att[1] + animalRide.attUp[1];
            byte upm = 0;
            byte upv = 0;
            if (self.isFullsetAnimal >= 5) {
                upm += 12;
                upv += 12;
                upvP += 12;
                upmP += 12;
            } else if (self.isFullsetAnimal >= 3) {
                upv += 8;
                upm += 8;
                upvP += 8;
                upmP += 8;
            }
            final int defThumaGocTruocThuCuoi = defThuma;
            final int defThuvatGocTruocThuCuoi = defThuvat;
            defThuma += defThuma * (animalRide.att[1] + animalRide.attUp[1] + upMa + upm) / 100;
            defThuvat += defThuvat * (animalRide.att[0] + animalRide.attUp[0] + upVat + upv) / 100;
            defThumaP += defThumaGocTruocThuCuoi * (animalRide.att[1] + animalRide.attUp[1] + upmap + upm) / 100;
            defThuvatP += defThuvatGocTruocThuCuoi * (animalRide.att[0] + animalRide.attUp[0] + upvatp + upv) / 100;
        }
        int upfullset = 0;
        if (self.married == 1) {
            if (self.potions[88] > 0) {
                upfullset = 2;
            } else if (self.potions[89] > 0) {
                upfullset = 4;
            } else if (self.potions[90] > 0) {
                upfullset = 7;
            }
        }
        if (self.fullSet15 >= 10) {
            upfullset += 12;
        } else if (self.fullSet15 >= 6) {
            upfullset += 8;
        } else if (self.fullSet13 >= 10) {
            upfullset += 4;
        } else if (self.fullSet10 >= 10) {
            ++upfullset;
        }
        upvP += upfullset;
        upmP += upfullset;
        if (self.wModel != null) {
            self.defend_magic += self.defend_magic * (self.wModel.getDefend() + upfullset + defPet) / 100;
            self.defend_physic += self.defend_physic * (self.wModel.getDefend() + upfullset + defPet) / 100;
            upvP += self.wModel.getDefend() + upfullset + defPet;
            upmP += self.wModel.getDefend() + upfullset + defPet;
            self.baokich += (short) self.wModel.getbaoKich();
            // Truoc day dong duoi nam trong "if (self.baokich > 0)", nghia la bao kich cua
            // mat na / canh chi duoc cong khi nhan vat DA co bao kich tu nguon khac. Ai chi
            // deo moi mat na thi 3% ghi tren tooltip bi bo di. Bo chot do.
            self.baokich += (short) self.wModel.getbaoKichMatna();
        } else {
            self.defend_magic += self.defend_magic * (upfullset + defPet) / 100;
            self.defend_physic += self.defend_physic * (upfullset + defPet) / 100;
            upvP += upfullset + defPet;
            upmP += upfullset + defPet;
        }
        self.defend_magic += defThuma + defendMa * upPTma / 100 + defendMa * upThuTbChar / 1000;
        self.defend_physic += defThuvat + defendVat * upPtvat / 100 + defendVat * upThuTbChar / 1000;
        int atkTubinh = 0;
        for (int j = 0; j < self.tubinh.size(); ++j) {
            atkTubinh += self.tubinh.get(j).getDef();
        }
        self.defend_magic += atkTubinh;
        self.defend_physic += atkTubinh;
        self.defendMaPlayer += defThumaP + defendMa * upmP / 100 + atkTubinh + defendMa * upThuTbChar / 1000;
        self.defendVatPlayer += defThuvatP + defendVat * upvP / 100 + atkTubinh + defendVat * upThuTbChar / 1000;
        if (self.charClass == 3) {
            self.defend_physic += defend1 + defendVat;
            self.defend_magic += defen4 + defendMa;
            self.defendVatPlayer += defend1 + defendVat;
            self.defendMaPlayer += defen4 + defendMa;
        }
        if (self.map != null && self.map.isMapChienTruongMoba()) {
            final CharChienTruong c2 = MapChienTruongMoba.getCharChienTruong(self.charname);
            if (c2 != null) {
                final long noiluc = c2.getNoiLuc();
                self.defend_physic = (int) (self.defend_physic * noiluc / 10L);
                self.defend_magic = (int) (self.defend_magic * noiluc / 10L);
                self.baokich = (short) (self.baokich * noiluc / 10L);
            }
        }
    }

    static int attackDam(Char self, final LiveActor actor, final int idSkill, final int level, final int bubbAttack) {
        actor.isCheckActiveBuffGiamSatThuong();
        // Quay mot lan cho ca don danh nay. Cac diem kiem tra (ne tranh, tien khi, pha
        // trien) deu doc lai ket qua nay chu khong quay lai, neu khong mot don se an
        // toi ba lan 10% -> ti le thuc te cao hon con so ghi tren mat na.
        self.rollHoaGiaiKyNangPet();
        Pet petUsing = self.petUsing;
        Animal animalRide = self.animalRide;
        if (self.map != null && self.map.isMapChienTruongMoba()) {
            final CharChienTruong c = MapChienTruongMoba.getCharChienTruong(self.charname);
            if (c != null && c.getNoiLuc() < 5) {
                petUsing = null;
            }
            if (c != null && c.getNoiLuc() < 7) {
                animalRide = null;
            }
        }
        try {
            // Cong thuc rieng cho cuoc (itemAx, item type 13) CHI ap dung trong khu mo
            // (mapId == 17 - xem MapDanhNhau/MapTown/MapLienDau, deu bat buoc phai co
            // cuoc moi danh duoc trong map nay). Truoc day khong kiem tra map: mac cuoc
            // roi ra khoi khu mo (chet/hoi sinh/duoc keo di/GM warp...) ma quen thao ra
            // thi MOI don danh o BAT KY dau (PK, dau truong, bang hoi, boss...) deu bi
            // ep cung theo cong thuc nay, bo qua toan bo ne tranh/chi mang/phong thu/roi
            // do khi trung boss ben duoi - loi nghiem trong, sua 13/09/2026.
            if (self.itemAx != null && self.map != null && self.map.mapId == 17) {
                return self.itemAx.atb[0] * 2;
            }
            if (actor.isMonsterMoba() && (actor.isLienHoaTru() || actor.isBossTruRong()) && !actor.canAttack(self)) {
                return 0;
            }
            actor.doHoaNguoiTuyetKhiBiDanh(self);
            actor.do8sRongLon(self);
            if (self.isKiller && actor.isKhangDoSat() && self.map.isMapTrain()) {
                return 0;
            }
            if (actor.isBossGetItemByHit()) {
                if (actor.isBossSonTinh() && !self.wModel.isThuyTinh()) {
                    return 0;
                }
                if (idSkill > 0) {
                    actor.checkReceivePotion(self);
                }
            }
            if (actor.isBossThuyTinh()) {
                if (!self.wModel.isSonTinh()) {
                    return 0;
                }
                if (idSkill > 0) {
                    actor.checkReceivePotion(self);
                }
            }
            if (actor.isNgocRong()) {
                if (actor.getInCountry() != self.inCountry) {
                    self.sendMessage(MessageCreator.createMsgChat(self.id, "Không thể nhặt ngọc của làng khác."));
                    return 0;
                }
                return 1;
            } else {
                if (!actor.isRuongMaquai()) {
                    int def = actor.getDefendPhysic() + actor.percentBuff[0];
                    int att = self.getAttack();
                    // SKIN LINH THU (Xich Duong): noi tai "mau duoi 30% thi tang 50% tan
                    // cong". Cong o day chu khong o calculateAttack: ham do chi chay lai
                    // khi mac/thao do, con noi tai nay bat tat theo tung giay — de o do
                    // thi no chi an dung luc nguoi choi tinh co doi trang bi.
                    final int pcTanCongSkin = SkinLinhThu.pcCongTanCong(self);
                    if (pcTanCongSkin > 0) {
                        att += att * pcTanCongSkin / 100;
                    }
                    // Thêm logic HoaHinh ở đây
                    if (actor.cat == 0 && self.getAnimalRide() != null && 
                    self.getAnimalRide().isHoaHinh == 1 && Map.r.nextInt(100) < 100) { // tile hoahinh
                    
                    actor.applyHoaHinhDefenseReduction();
                    
                    if (actor instanceof Char) {
                        Char target = (Char)actor;
                        try {
                            target.sendMessage(
                                MessageCreator.createServerAlertAutoOffMessage(
                                    "Bị giảm " + actor.currentDefReduc + "% giáp trong 10 giây!"
                                )
                            );
                            
                            target.sendMessage(MessageCreator.createMainCharInfoMessage(target));
                            target.sendToNearPlayer(MessageCreator.createCharInfo(target));     
                            
                        } catch (IOException e) {
                            e.printStackTrace(); 
                        }
                    }
                }
                    if (self.idRuouUong > -1) {
                        final int[] pcf = {5, 10, 20};
                        att += pcf[self.idRuouUong] * att / 100;
                    }
                    boolean isBossBaoCat = false;
                    final CharChienTruong c2 = MapChienTruongMoba.getCharChienTruong(self.charname);
                    final CharChienTruong ca = MapChienTruongMoba.getCharChienTruong(actor.getName());
                    if (self.map != null && self.map.isMapChienTruongMoba()) {
                        int pcup = c2.isHonNguyen();
                        pcup += c2.getBuffHeHoa();
                        if (actor.isBoss()) {
                            pcup += c2.isHoaCong();
                        }
                        att += att * pcup / 100;
                    }
                    if (actor.cat == 0) {
                        if (((Char) actor).isBatTu()) {
                            return 0;
                        }
                        if (self.wModel.wpModel != null) {
                            int pcngtuyet = self.wModel.getPCNgtuyet();
                            if (self.isAdmin && Map.adminTest) {
                                pcngtuyet = 100;
                                
                            }
                            if (Map.r.nextInt(100) < pcngtuyet) {
                                ((Char) actor).setNguoiTuyet(2);
                                MessageCreator.createMsgCharMonster(self, self);
                            }
                        }
                        if (self.isCharMagic()) {
                            def = actor.getDefendMagic() + actor.getBuffDefCB(1, true);
                        }
                        if (((Char) actor).isMonster) {
                            if (actor.getDefendPhysic() + actor.percentBuff[0] > actor.getDefendMagic() + actor.getBuffDefCB(1, true)) {
                                def = actor.getDefendPhysic() + actor.percentBuff[0];
                            } else {
                                def = actor.getDefendMagic() + actor.getBuffDefCB(1, true);
                            }
                            def *= 3;
                            att *= 2;
                        }
                        final int deltalv = self.lvDetail.lv - actor.getLevel();
                        if (self.map != null && self.map.isMapChienTruongMoba() && ca != null) {
                            int pcup2 = ca.getBuffHeKim();
                            pcup2 += ca.isHonDon();
                            def += def * pcup2 / 100;
                        }
                        if (!self.map.isMapChienTruongMoba() || (self.map.isMapChienTruongMoba() && !actor.isCharMonster())) {
                            if (deltalv < 0) {
                                def += def * Map.abs(deltalv / 6);
                                if (Map.abs(deltalv) > 5 && Map.abs(deltalv) <= 10) {
                                    att /= 2;
                                } else if (Map.abs(deltalv) > 10 && Map.abs(deltalv) <= 15) {
                                    att /= 3;
                                } else if (Map.abs(deltalv) > 15 && Map.abs(deltalv) <= 20) {
                                    att /= 10;
                                } else if (Map.abs(deltalv) > 20) {
                                    att /= 50;
                                }
                            } else if (deltalv > 0) {
                                if (Map.abs(deltalv) > 5 && Map.abs(deltalv) <= 10) {
                                    def /= 2;
                                } else if (Map.abs(deltalv) > 10 && Map.abs(deltalv) <= 15) {
                                    def /= 3;
                                } else if (Map.abs(deltalv) > 15 && Map.abs(deltalv) <= 20) {
                                    def /= 10;
                                } else if (Map.abs(deltalv) > 20) {
                                    def /= 50;
                                }
                            }
                        }
                        if (actor.getAttack() <= 0 || def <= 0) {
                            def = 0;
                        }
                        if (self.map != null && self.map.isMapChienTruongMoba() && Map.r.nextInt(100) <= c2.getBuffHeMoc()) {
                            final CharChienTruong cc = MapChienTruongMoba.getCharChienTruong(actor.getName());
                            if (cc != null && System.currentTimeMillis() - cc.timeCamLang >= 0L) {
                                cc.timeCamLang = System.currentTimeMillis() + 3000L;
                                ((Char) actor).sendInfoChienTruong(Char.ID_CAM_LANG, 3);
                            }
                        }
                    } else if (actor.cat == 1) {
                        final Monster mt = (Monster) actor;
                        isBossBaoCat = mt.isBossBaoCat();
                        if (!actor.isMonsterVantieu() && !((Monster) actor).isBoss && mt.idTemplate != 36 && mt.idTemplate != 37 && mt.idTemplate != 43 && mt.idTemplate != 46 && !self.map.isMapChienTruongMoba()) {
                            final int tempLv = actor.getLevel() - self.lvDetail.lv;
                            // Chi phat khi quai cao hon nhan vat tu 10 level tro len (truoc day
                            // phat ngay tu 1 level chenh lech, qua nang voi quai dau game giap thap).
                            if (tempLv >= 10) {
                                def += tempLv * 100;
                                att /= 10;
                            } else if (tempLv < 0) {
                                att += Map.abs(20 * tempLv);
                            }
                        }
                    }
                    if (att < 0) {
                        att = 5 + Map.r.nextInt(10);
                    }
                    if (def < 0) {
                        def = 5 + Map.r.nextInt(10);
                    }
                    if (!self.map.isMapChienTruongMoba() || (self.map.isMapChienTruongMoba() && !actor.isCharMonster())) {
                        if (CheDoPokemon.bat()) {
                            // Che do Pokemon (viec S4): khac he 18 he thay ngu hanh.
                            // He tan cong = he 1 cua nguoi danh (giai doan 3 se la he chieu).
                            att = SatThuongHe.apDung(att, self.hePokemon1(), actor.hePokemon1(), actor.hePokemon2());
                        } else if ((self.he + 2) % 5 == actor.he) {
                            def -= def * 5 / 100;
                            att += att * 10 / 100;
                        } else if ((self.he + 3) % 5 == actor.he) {
                            if (actor.cat == 0) {
                                def += def * 7 / 100;
                            } else if (actor.cat == 1) {
                                def += def * 10 / 100;
                            }
                            att -= att * ((actor.cat == 1) ? 2 : 5) / 100;
                        }
                    }
                    if (self.idNgtuyet == 1 && actor.cat == 1 && !((Monster) actor).isBoss) {
                        att *= 2;
                    }
                    def += def * actor.getValueLua(1) / 100;
                    att += self.getValueLua(2) * att / 100;
                    if (self.checkSuyYeu()) {
                        final EffectBuff ef = self.hashEffBuff.get(EffectBuff.SUY_YEU);
                        att -= att * ef.dam / 100;
                    }
                    int pcgiamdef = 0;
                    if (actor.checkSuyYeu()) {
                        final EffectBuff ef2 = actor.hashEffBuff.get(EffectBuff.SUY_YEU);
                        pcgiamdef = ef2.dam;
                    }
                    def -= def * pcgiamdef / 100;
                    if (c2 != null && actor.isTruMoba()) {
                        def = att * (10 - c2.getNoiLuc()) / 10;
                    }
                    // ---- THAN KHI: xuyen thu — bo qua mot phan giap muc tieu ----
                    // Dung dung loai giap dang duoc tru o tren: lop ma phap danh
                    // vao thu ma nen an dong "Xuyen thu ma", con lai la thu vat.
                    final int tkXt = self.getTkXuyenThu(self.isCharMagic());
                    if (tkXt > 0 && def > 0) {
                        def -= (int) ((long) def * tkXt / 1000L);
                        if (def < 0) {
                            def = 0;
                        }
                    }
                    // ---- MAT NA: bo qua tc ma / bo qua tc vat ----
                    // Cung y nghia voi "xuyen thu" cua than khi ngay tren, nen dung chung
                    // cach chon loai giap: lop ma phap an dong "Bo qua tc ma" (mat na cao),
                    // lop vat ly an dong "Bo qua tc vat" (mat na tho).
                    final int mnBoQuaThu = self.getPcBoQuaThuMatNa();
                    if (mnBoQuaThu > 0 && def > 0) {
                        def -= def * mnBoQuaThu / 100;
                        if (def < 0) {
                            def = 0;
                        }
                    }
                    int dam = att - def;
                    if (dam < 0) {
                        dam = 5 + Map.r.nextInt(10);
                    }
                    dam = dam * 110 / 100 + Map.r.nextInt() % 10;
                    if (bubbAttack > -1 && !actor.isTruMoba() && (self.charClass == 0 || bubbAttack == 1)) {
                        dam = att;
                    }
                    if (dam < 5) {
                        dam = 5 + Map.r.nextInt(10);
                    }
                    if (actor.cat == 1 && self.isFullParty() && !actor.isMonsterVantieu() && !actor.isTruMoba()) {
                        dam += dam * 20 / 100;
                    }
                    int finalDame = dam;
                    int skillDamPercent = CharManager.SKILL_DAM_PERCENT[self.charClass][idSkill][level];
                    if (actor.isTruMoba() && c2 != null && self.map.isMapChienTruongMoba() && actor.isTruMoba()) {
                        skillDamPercent = skillDamPercent * c2.getNoiLuc() / 10;
                    }
                    finalDame = finalDame * skillDamPercent / 100;
                    if (idSkill >= 3 && self.wModel != null) {
                        if (self.wModel.haveDameAnhSang() && Map.r.nextInt(100) < 20 && !actor.haveDamAnhsang()) {
                            finalDame += Map.r.nextInt(5000) + 5001;
                        }
                        if (self.wModel.haveDameBongtoi() && Map.r.nextInt(100) < 20 && !actor.haveDamBongtoi()) {
                            finalDame += Map.r.nextInt(5000) + 5001;
                        }
                        if (self.wModel.haveHutMau()) {
                            self.hp += 100;
                            MessageCreator.createMsgUseHpMP(self, 100, 1);
                        }
                        if (self.wModel.haveHoangSo() && actor.cat == 0 && Map.r.nextInt(100) < 10) {
                            boolean issend = false;
                            if (actor.addEffBuff(EffectBuff.EFF_HOANG_SO, System.currentTimeMillis() + 5000L, EffectBuff.BY_ACTOR, 0) != null) {
                                actor.sendEffToChar((Char) actor);
                                issend = true;
                            }
                            if (issend) {
                                actor.sendEffToNearChar();
                            }
                        }
                    }
                    if (isBossBaoCat) {
                        if (finalDame <= 100) {
                            finalDame = self.lvDetail.lv * 50 + Map.r.nextInt() % 50;
                        } else if (finalDame > actor.maxhp * 20 / 100) {
                            finalDame = actor.maxhp * 20 / 100 + Map.r.nextInt(1000);
                        }
                    }
                    if ((actor.isMonsterVanTienTran() || actor.isBossTruRong()) && !actor.isTruMoba()) {
                        final long maxHP = actor.maxhp;
                        if (!actor.isBossBaoCat() && !actor.isBossTruRong()) {
                            int pcmin = 10 - actor.getWave();
                            int pcmmax = 15 - actor.getWave();
                            if (idSkill < 2) {
                                pcmin = idSkill + 1;
                                pcmmax = idSkill + 1;
                            }
                            if (finalDame < maxHP * pcmin / 1000L) {
                                finalDame = (int) (maxHP * pcmin / 1000L + Map.r.nextInt(100));
                            } else if (finalDame > maxHP * pcmmax / 1000L) {
                                finalDame = (int) (maxHP * pcmmax / 1000L + Map.r.nextInt(100));
                            }
                            if (idSkill >= 2) {
                                final long addmore = self.skill[idSkill];
                                finalDame += (int) (self.attack * addmore / 1000L);
                            }
                        } else if (!actor.isBossTruRong()) {
                            int pcmin = 6 - actor.getWave();
                            int pcmmax = 8 - actor.getWave();
                            if (idSkill < 2) {
                                pcmin = idSkill + 1;
                                pcmmax = idSkill + 1;
                            }
                            if (finalDame < maxHP * pcmin / 1000L) {
                                finalDame = (int) (maxHP * pcmin / 1000L + Map.r.nextInt(100));
                            } else if (finalDame > maxHP * pcmmax / 1000L) {
                                finalDame = (int) (maxHP * pcmmax / 1000L + Map.r.nextInt(100));
                            }
                            if (idSkill >= 2) {
                                final long addmore = self.skill[idSkill];
                                finalDame += (int) (self.attack * addmore / 1000L);
                                if (actor.isLienHoaTru()) {
                                    finalDame -= finalDame / 3;
                                }
                            }
                        } else {
                            int pcmin = 3 - actor.getWave() + idSkill;
                            int pcmmax = 6 - actor.getWave() + idSkill;
                            if (idSkill < 2) {
                                pcmin = 1;
                                pcmmax = 1;
                            }
                            if (finalDame < maxHP * pcmin / 10000L) {
                                finalDame = self.lvDetail.lv * 50 + Map.r.nextInt() % 50;
                            } else if (finalDame > maxHP * pcmmax / 1000L) {
                                finalDame = (int) (maxHP * pcmmax / 1000L + Map.r.nextInt(100));
                            }
                            if (idSkill >= 2) {
                                final long addmore = self.skill[idSkill];
                                finalDame += (int) (self.attack * addmore / 10000L);
                                finalDame -= finalDame / 3;
                            }
                        }
                    }
                    if (finalDame <= 0) {
                        finalDame = 1;
                    }


                    if (self.isLinhThueLan()) {
                        // Tăng cố định 2% sát thương và làm tròn lên
                        double bonusDamagePercent = 2.0;
                        int originalDamage = finalDame;
                        
                        // Sử dụng Math.ceil để làm tròn lên
                        finalDame = (int)Math.ceil(finalDame + (finalDame * bonusDamagePercent / 100));
                       
                    }


                    if (petUsing != null && petUsing.isPetKhi() && self.idNgtuyet == 5 && self.isCrazy == -1) {
                        finalDame *= 3;
                    }
                    if (petUsing != null && ((self.map.isMapChienTruongMoba() && c2.getNoiLuc() >= 5) || !self.map.isMapChienTruongMoba())) {
                        finalDame += petUsing.getSatThuong();
                    }
                    // Thien nhan (ne don) la ky nang pet -> bi hoa giai thi khong tinh
                    // phan ne cua pet nua, chi con ne tranh goc + ne tranh than khi.
                    int pcNeTranh = actor.haveNeTranh();
                    if (self.dangHoaGiaiKyNangPet() && actor instanceof Char) {
                        pcNeTranh = ((Char) actor).haveNeTranhBoQuaPet();
                    }
                    if (Map.r.nextInt(1000) < pcNeTranh) {
                        finalDame = 0;
                    }
                    if (actor.isNgu()) {
                        actor.huyEff(self, EffectBuff.RU_NGU);
                    }
                    self.checkNewEffectPet(actor);
                    if (actor.checkBong()) {
                        final EffectBuff ef3 = actor.hashEffBuff.get(EffectBuff.BONG);
                        finalDame += finalDame * ef3.dam / 100;
                    }
                    // Sat thuong chuan: UU TIEN % CAO NHAT giua vktt dang mac o "thoi trang"
                    // (itemVukhiThoiTrang, dai 758-772) VA than binh vinh vien dang la vu khi
                    // CHINH (dai 773-787) - KHONG cong don ca hai khi cung mang (yeu cau nguoi
                    // dung 20/09/2026). Truoc day chi doc itemVukhiThoiTrang nen than binh that
                    // dang cam khong tu kich hoat duoc dong nay neu khong deo them mot vktt khac.
                    final Item vktGoc = self.itemVukhiThoiTrang;
                    final Item thanBinhThat = self.getThanBinhDangMangOVuKhiChinh();
                    Item nguonSatThuongChuan = vktGoc;
                    if (thanBinhThat != null
                            && (vktGoc == null || thanBinhThat.otherAtt[54] > vktGoc.otherAtt[54])) {
                        nguonSatThuongChuan = thanBinhThat;
                    }
                    if (nguonSatThuongChuan != null
                            && Map.r.nextInt(1000) < nguonSatThuongChuan.otherAtt[56]
                            && !actor.isBossTruRong()) {
                        final long mhp = actor.maxhp;
                        final long st = nguonSatThuongChuan.otherAtt[54] * mhp / 1000L;
                        finalDame += (int) st;
                    }
                    // SKIN LINH THU (Kim Cang Long): "Sat thuong chuan" ngau nhien theo vat
                    // pham, cong THANG moi don theo % maxHP MUC TIEU - CUNG loai tru Tru Rong
                    // voi dong tren (xem CharChiSo.java:929, cung tinh than voi "sat thuong
                    // chuan" cua vu khi thoi trang o dong 929-933, nhung day KHONG co ti le
                    // proc rieng - luon cong khi da roll duoc dong nay).
                    if (!actor.isBossTruRong()) {
                        final int pcSTC = SkinLinhThu.kimCangLongSatThuongChuan(self);
                        if (pcSTC > 0) {
                            finalDame += (int) ((long) actor.maxhp * pcSTC / 100L);
                        }
                    }
                    if (actor.cat == 0) {
                        final Char p = (Char) actor;
                        if (p.getPetUsing() != null) {
                            final int[] battu = p.getPetUsing().checkBatTu(p);
                            if (battu[0] > -1) {
                                EffectBuff ef4 = null;
                                if ((ef4 = p.addEffBuff(EffectBuff.BAT_TU, System.currentTimeMillis() + battu[1], EffectBuff.BY_ACTOR, 0)) != null) {
                                    final Message msg = MessageCreator.createMsgNewEffectSkill5Long(battu[0], battu[1], p.id, p.cat, true, false);
                                    p.sendMessage(msg);
                                    p.sendToNearPlayer(msg);
                                }
                                finalDame = 0;
                            }
                        }
                    }
                    if (self.isCuMeo()) {
                        finalDame -= finalDame * self.pcGiamSatThuongByCuMeo / 100;
                    }
                    if (actor.isBossSonTinhThuyTinh()) {
                        final int d = actor.getMaxDamPerHit();
                        if (d > 0 && finalDame > d) {
                            finalDame = d;
                        }
                    }
                    if ((!self.isAdmin || !Map.adminTest) && (actor.isLienHoaTru() || (actor.isBoss() && actor.isMonsterMoba()))) {
                        TeamServer.isServerLocal();
                    }

//                    if (self.damageAccumulated > 0) {
//                        finalDame += self.damageAccumulated;
//                        self.damageAccumulated = 0;
//                    }
                    finalDame = applyThanKhiOnHit(self, actor, finalDame);
                    return finalDame;
                }
                if (self.itemVukhiThoiTrang == null || !self.itemVukhiThoiTrang.isGayGiangSinh()) {
                    return 0;
                }
                if (actor.getInCountry() != self.inCountry) {
                    self.sendMessage(MessageCreator.createMsgChat(self.id, "Không thể đánh rương của làng khác."));
                    return 0;
                }
                return 1;
            }
        } catch (final Exception e) {
            e.printStackTrace();
            return 0;
        }
    }

    static int applyThanKhiOnHit(Char self, final LiveActor actor, int finalDame) {
        if (actor == null || finalDame <= 0) {
            return finalDame;
        }
        try {
            final boolean targetLaChar = (actor.cat == 0 && actor instanceof Char);
            final boolean targetLaBoss = actor.isBoss() || actor.isTruMoba();

            // ---------- BEN TAN CONG: cong them sat thuong ----------
            if (self.itemThanKhi != null) {
                final int tkHp = self.getTkStTheoHp();
                // Khong tinh vao Tru Rong: cong THANG theo % maxhp MUC TIEU, khong co tran, khong
                // qua ti le proc nao - maxhp cang cao thi dam cang lon theo dung ti le, nen tang
                // mau boss (VD 100 trieu -> 2 ty) chi lam dam nay tang theo chu khong lam boss
                // song lau hon. Cung mot tinh than voi loai tru o "sat thuong chuan" (dong ~929).
                if (tkHp > 0 && actor.maxhp > 0 && !actor.isBossTruRong()) {
                    finalDame += (int) ((long) actor.maxhp * tkHp / 1000L);
                }
                final int tkMp = self.getTkStTheoMp();
                if (tkMp > 0 && targetLaChar) {
                    final int mpMucTieu = ((Char) actor).maxmp;
                    if (mpMucTieu > 0) {
                        finalDame += (int) ((long) mpMucTieu * tkMp / 1000L);
                    }
                }
                if (targetLaBoss) {
                    final int tkBoss = self.getTkStBoss();
                    if (tkBoss > 0) {
                        finalDame += (int) ((long) finalDame * tkBoss / 1000L);
                    }
                } else if (actor.cat == 1) {
                    final int tkQuai = self.getTkStQuai();
                    if (tkQuai > 0) {
                        finalDame += (int) ((long) finalDame * tkQuai / 1000L);
                    }
                }
            }

            // ---------- BEN PHONG THU: than khi cua MUC TIEU ----------
            // Duong nay la Char danh nguoi/quai, nen phia bi danh chi ap dung
            // duoc "Giam ST tu nguoi choi" va khang he. "Giam ST tu Boss" nam
            // o duong quai danh nguoi, xu ly trong subDamThanKhi().
            if (targetLaChar) {
                final Char bidanh = (Char) actor;
                int giam = 0;
                if (bidanh.itemThanKhi != null) {
                    giam += bidanh.getTkGiamStPlayer();
                    giam += bidanh.getTkKhangTheoClass(self.charClass);
                }
                // Khang ngu hanh cua thu cuoi muc tieu — cung la khang theo mon phai nen
                // cong chung mot ro voi khang he cua than khi, roi mot lan Char.tkGiamSatThuong
                // (da chan tran 80%) de hai nguon khong the cong nhau thanh bat tu.
                giam += bidanh.getKhangNguHanhThuCuoi(self.charClass);
                if (giam > 0) {
                    finalDame = Char.tkGiamSatThuong(finalDame, giam);
                }
            }
            if (finalDame < 0) {
                finalDame = 0;
            }

            // ---------- MAT NA: chuyen sat thuong thanh hp ----------
            // Khong nam trong khoi than khi ben duoi vi mat na doc lap voi than khi.
            final int pcMauMatNa = self.getPcChuyenStThanhHpMatNa();
            if (pcMauMatNa > 0 && finalDame > 0 && self.hp < self.maxhp) {
                final int hoiMatNa = (int) ((long) finalDame * pcMauMatNa / 100L);
                if (hoiMatNa > 0) {
                    self.hp += hoiMatNa;
                    if (self.hp > self.maxhp) {
                        self.hp = self.maxhp;
                    }
                }
            }

            if (self.itemThanKhi != null) {
                // ---------- HUT HP / MP theo sat thuong gay ra ----------
                final int pcHutHp = self.getTkHutHp();
                if (pcHutHp > 0 && self.hp < self.maxhp) {
                    final int hoi = (int) ((long) finalDame * pcHutHp / 100L);
                    if (hoi > 0) {
                        self.hp += hoi;
                        if (self.hp > self.maxhp) {
                            self.hp = self.maxhp;
                        }
                    }
                }
                final int pcHutMp = self.getTkHutMp();
                if (pcHutMp > 0 && self.mp < self.maxmp) {
                    final int hoi2 = (int) ((long) finalDame * pcHutMp / 100L);
                    if (hoi2 > 0) {
                        self.mp += hoi2;
                        if (self.mp > self.maxmp) {
                            self.mp = self.maxmp;
                        }
                    }
                }

                // ---------- CAM LANG ----------
                // Dung EffectBuff.LAM_THINH — co san va da duoc haveLamThinh()
                // cua game kiem tra khi dung ky nang.
                final int pcCamLang = self.getTkCamLang();
                if (pcCamLang > 0 && Map.r.nextInt(100) < pcCamLang) {
                    actor.addEffBuff(EffectBuff.LAM_THINH, System.currentTimeMillis() + 3000L, EffectBuff.BY_ACTOR, 0);
                }

                // ---------- GIAM SUC MANH + PHONG THU CUA DICH ----------
                // EffectBuff.SUY_YEU dung san trong attackDam: tru ca att lan
                // def theo ef.dam phan tram, khop dung mo ta cua dong nay.
                final int tkSuyYeu = self.getTkGiamSucManhDich();
                if (tkSuyYeu > 0) {
                    // Thoi gian doi theo cap (3..8 giay), khong con cam cung 3 giay.
                    actor.addEffBuff(EffectBuff.SUY_YEU, System.currentTimeMillis() + Char.tkSuyYeuThoiGian(tkSuyYeu),
                            EffectBuff.BY_ACTOR, tkSuyYeu / 10);
                }
            }

            // ---------- PHAN DON cua muc tieu ----------
            if (targetLaChar) {
                final Char bidanh = (Char) actor;

                // ---------- KHANG XUYEN GIAP cua pet dang cuoi (hoa hinh) ----------
                final Pet petBiDanh = bidanh.getPetUsing();
                if (petBiDanh != null && finalDame > 0) {
                    finalDame = petBiDanh.applyKhangXuyenGiap(finalDame);
                }

                // ---------- KHANG XUYEN GIAP cua linh thu dang cuoi (hoa hinh) ----------
                final Animal animalBiDanh = bidanh.getAnimalRide();
                if (animalBiDanh != null && finalDame > 0) {
                    finalDame = animalBiDanh.applyKhangXuyenGiap(finalDame);
                }

                final int pcPhan = bidanh.getTkPhanDon();
                if (pcPhan > 0 && finalDame > 0) {
                    int damPhan = (int) ((long) finalDame * pcPhan / 100L);
                    // Noi tai "Tra dua" cua nguoi DANH (self): cuoi Rong Lon + mac Hoa Ky Lan
                    // thi chi an mot nua don phan don.
                    damPhan = self.giamSatThuongPhanDon(damPhan);
                    if (damPhan > 0) {
                        if (damPhan > self.hp) {
                            damPhan = self.hp;
                        }
                        self.hp -= damPhan;
                        if (self.hp < 0) {
                            self.hp = 0;
                        }
                    }
                }
            }
        } catch (final Exception e) {
            e.printStackTrace();
        }
        return finalDame;
    }

    static void calculateAttack(Char self) {
        Vector<Item> wItems = self.getCharWearing();
        Pet petUsing = self.petUsing;
        Animal animalRide = self.animalRide;
        if (self.map != null && self.map.isMapChienTruongMoba()) {
            CharChienTruong c = MapChienTruongMoba.getCharChienTruong(self.charname);
            if (c != null && c.getNoiLuc() < 5) {
                petUsing = null;
            }
            if (c != null && c.getNoiLuc() < 7) {
                animalRide = null;
            }
        }
        int attack1 = (petUsing != null) ? petUsing.getStrength() : 0;
        // cải trang
        if (self.isSaTang() && (self.charClass == 0 || self.charClass == 1 || self.charClass == 3)) {
            int lv = self.getLevelCaiTrang();
            if (lv >= 1 && lv <= 15) {
                attack1 = attack1 + (lv * 1);
             
            }
        }
        // KHONG con ep (short) o bay cong thuc duoi day.
        //
        // LOI DA XAY RA THAT (05/09/2026, nguoi choi bao): Cung thu 49k tan cong, mac skin
        // Tien Loc (+20% diem tiem nang) vao thi tan cong TUT XUONG con 47k thay vi tang.
        //
        // Goc: attack1 la int, moi so hang deu la int, nhung ket qua lai bi ep (short) —
        // tran 32767. Kheo leo/suc manh la short (toi da 32767) nen ve trai da du lon de
        // vuot tran sau khi nhan 5/3 hay 4/3. Vuot tran thi so QUAY VONG SANG AM: nhan vat
        // cang manh tan cong cang tut, va cong them chi so (skin, buff, do moc) co the lam
        // GIAM tan cong dung luc no vua vuot qua moc 32767.
        //
        // Do thu bang chinh cong thuc Cung thu (nhan 5/3, phu tro 300):
        //     kheo leo 16000 -> khong skin 27166 | co Tien Loc  32500  (+5334, dung)
        //     kheo leo 17000 -> khong skin 28833 | co Tien Loc -31036  (-59869, SAI)
        //     kheo leo 19600 -> khong skin -32370 (da am san du KHONG mac skin gi)
        //
        // Bo ep kieu la het: khong so hang nao can 16 bit, va ca ham tu day tro xuong
        // (attack2, cac moc %, thu cuoi, pet, noi luc) deu tinh bang int.
        switch (self.charClass) {
            case 0:
                attack1 += ((self.strength + self.tnSucManh()) + self.getEffSkillClan(3) + self.newAtb[2] + self.upAbility[0] + self.abilityKham[0] + ((animalRide != null) ? animalRide.att[5] : 0)) * 4 / 3;
                break;
            case 1:
                attack1 += ((self.strength + self.tnSucManh()) + self.getEffSkillClan(3) + self.newAtb[2] + self.upAbility[0] + self.abilityKham[0] + ((animalRide != null) ? animalRide.att[5] : 0)) * 11 / 10;
                if (self.skill[5] + self.addMoreLevelSkill[5] > 0) {
                    int pc = attack1 * self.buffPass[0] / 100;
                    if (pc <= 0) {
                        pc = 0;
                    }
                    attack1 += pc;
                }
                attack1 += attack1 / 2;
                break;
            case 2:
                if (self.isSaTang() && (self.charClass == 2)) {
                    int lv = self.getLevelCaiTrang();
                    if (lv >= 1 && lv <= 15) {
                    attack1 += ((self.spirit + self.tnTinhThan()) + lv + self.getEffSkillClan(3) + self.newAtb[4] + self.upAbility[3] + self.abilityKham[2]) * 5 / 3 + ((animalRide != null) ? animalRide.att[8] : 0) + ((self.strength + self.tnSucManh()) + self.abilityKham[0]) / 15;
                    }
                } else {
                    attack1 += ((self.spirit + self.tnTinhThan()) + self.getEffSkillClan(3) + self.newAtb[4] + self.upAbility[3] + self.abilityKham[2]) * 5 / 3 + ((animalRide != null) ? animalRide.att[8] : 0) + ((self.strength + self.tnSucManh()) + self.abilityKham[0]) / 15;
                }
                break;
            case 3:
                attack1 += (self.strength + self.tnSucManh()) + self.getEffSkillClan(3) + self.newAtb[2] + self.upAbility[0] + self.abilityKham[0] + ((animalRide != null) ? animalRide.att[5] : 0);
                if ((self.strength + self.tnSucManh()) >= (self.agitity + self.tnKheoLeo())) {
                    attack1 = attack1 * 2 / 3;
                }
                break;
            case 4:
                if (self.isSaTang() && (self.charClass == 4 )) {
                    int lv = self.getLevelCaiTrang();
                    if (lv >= 1 && lv <= 15) {
                        attack1 += ((self.agitity + self.tnKheoLeo()) + lv+ self.getEffSkillClan(3) + self.newAtb[3] + self.upAbility[1] + ((animalRide != null) ? animalRide.att[7] : 0) + self.abilityKham[1]) * 5 / 3;
                    }
                } else {
                    attack1 += ((self.agitity + self.tnKheoLeo()) + self.getEffSkillClan(3) + self.newAtb[3] + self.upAbility[1] + ((animalRide != null) ? animalRide.att[7] : 0) + self.abilityKham[1]) * 5 / 3;
                }
                break;
        }
        int attack2 = 0;
        int upAttack = 0;
        int upatkP = 0;
        for (Item item : wItems) {
            int t = (item.getTemplate()).type;
            if (item.level > self.lvDetail.lv) {
                continue;
            }
            if (t == 6 || t == 7
                    || t == 4 || t == 5
                    || t == 3) {
                attack2 += item.atb[0] + item.getValueUpByVuKhiThoiTrang(item.atb[0], self.itemVukhiThoiTrang);
                upAttack += item.lockAtb[0];
                upatkP += item.lockAtb[0];
            }
            if (t == 9) {
                attack2 += item.atb[0];
                upAttack += item.lockAtb[0];
                upatkP += item.lockAtb[0];
            }
            if (t == 8) {
                attack2 += item.atb[0];
                upAttack += item.lockAtb[0];
                upatkP += item.lockAtb[0];
            }
        }
        Vector<Item> awItems = self.getAnimalWearing();
        for (Item item : awItems) {
            int t = (item.getTemplate()).type;
            if (t == 17 || t == 15) {
                attack2 += item.atb[0];
                upAttack += item.lockAtb[0];
                upatkP += item.lockAtb[0];
            }
        }

        self.attackPlayer = attack1 + attack2;
        self.attack = attack1 + attack2;
        // ---- THAN KHI: cong diem roi cong phan tram (tinh tren chi so goc) ----
        final int tkDiem = self.getThanKhiDiemSatThuong();
        // tkDiem KHONG cong o day: dac ta ghi "Tang diem sau cung", ma tu day
        // tro xuong con hang loat he so nhan (upAttack, cac moc %, thu cuoi,
        // pet, noi luc, cai tao) se khuech dai no len nhieu lan. Cong o cuoi ham.
        final int tkPc = self.getThanKhiPhanTramSatThuong();
        if (tkPc > 0) {
            // self.attack: cong ngay, tinh tren CHI SO GOC (dung mo ta:
            // "tang dua vao chi so goc, khong phai chi so tong sau cung").
            self.attack += (attack1 + attack2) * tkPc / 100;
            // self.attackPlayer: KHONG cong tay o day. No dung co che khac —
            // don het % vao upatkP roi nhan mot lan duy nhat o cuoi ham
            // (self.attackPlayer += self.attackPlayer * upatkP / 100).
            // Cong ca hai cho la an phan tram HAI LAN.
            upatkP += tkPc;
        }
        upatkP += self.attNguHanh[2];
        self.attack += self.attack * self.attNguHanh[2] / 100;
        upatkP += self.lockAtb[0];
        self.attack += self.attack * self.lockAtb[0] / 100;
        self.attack += (animalRide != null) ? (self.attack * animalRide.att[2] / 100) : 0;
        upatkP += (animalRide != null) ? animalRide.att[2] : 0;
        if (animalRide != null) {
            upAttack += animalRide.attUp[2];
            upatkP += animalRide.attUp[2];
            if (self.isFullsetAnimal >= 5) {
                upAttack += 12;
                upatkP += 12;
            } else if (self.isFullsetAnimal >= 3) {
                upAttack += 8;
                upatkP += 8;
            }
        }
        if (self.rankGov == 0 && self.rideHorse > 0) {
            upAttack += 5;
            upatkP += 5;
        }
        if (self.idNgtuyet == 3) {
            upAttack += 20;
            upatkP += 20;
        }
        if (self.timeUp5Attribute > 0L) {
            upAttack += 5;
            upatkP += 5;
        }
        if (petUsing != null) {
            upAttack += petUsing.getDoatMenh();
        }
        if (self.vip > 0) {
            upAttack += 5;
        }
        // SKIN LINH THU (Kim Cang Long): "Tang cong" ngau nhien theo vat pham - xem
        // SkinLinhThu.kimCangLongTangCong. Cong vao ca hai bo dem % nhu cac dong tren, dung
        // truoc khi nhan mot lan duy nhat o hai dong ke tiep.
        final int pcTangCongSkin = SkinLinhThu.kimCangLongTangCong(self);
        upAttack += pcTangCongSkin;
        upatkP += pcTangCongSkin;
        self.attack += self.attack * upAttack / 100;
        if (self.fullSet15 >= 10) {
            self.attack += self.attack * 12 / 100;
            upatkP += 12;
        } else if (self.fullSet15 >= 6) {
            self.attack += self.attack * 8 / 100;
            upatkP += 8;
        } else if (self.fullSet13 >= 10) {
            self.attack += self.attack * 4 / 100;
            upatkP += 4;
        } else if (self.fullSet10 >= 10) {
            self.attack += self.attack * 1 / 100;
            upatkP++;
        }
        if (self.wModel != null) {
            self.attack += self.attack * (self.wModel.getAttack() + ((petUsing != null) ? petUsing.getPcUpDam() : 0)) / 100;
            upatkP += self.wModel.getAttack() + ((petUsing != null) ? petUsing.getPcUpDam() : 0);
        } else {
            self.attack += self.attack * ((petUsing != null) ? petUsing.getPcUpDam() : 0) / 100;
            upatkP += (petUsing != null) ? petUsing.getPcUpDam() : 0;
        }
        int atkTubinh = 0;
        for (int i = 0; i < self.tubinh.size(); i++) {
            atkTubinh += ((TuBinhTemplate) self.tubinh.get(i)).getAttack();
        }
        self.attack += atkTubinh;
        self.attackPlayer += self.attackPlayer * upatkP / 100 + atkTubinh;
        if (self.charClass == 3) {
            self.attack += attack1 + attack2;
            self.attackPlayer += attack1 + attack2;
        }
        if (self.map != null && self.map.isMapChienTruongMoba()) {
            CharChienTruong c = MapChienTruongMoba.getCharChienTruong(self.charname);
            if (c != null) {
                long noiluc = c.getNoiLuc();
                if (noiluc < 10L) {
                    self.attack = (int) (self.attack * noiluc / 10L + (self.attack / 20));
                    self.attackPlayer = (int) (self.attackPlayer * noiluc / 10L + (self.attackPlayer / 20));
                }
            }
        }
        // Che do Pokemon (B1): cong % tan cong theo giai doan tien hoa, truoc diem Than Khi.
        if (com.kpah.pokemon.CheDoPokemon.bat() && self.pokeDex > 0) {
            final int ptGd = com.kpah.pokemon.LoaiPokemon.phanTramGiaiDoan(self.pokeDex);
            self.attack += (int) ((long) self.attack * ptGd / 100L);
            self.attackPlayer += (int) ((long) self.attackPlayer * ptGd / 100L);
        }
        // ---- THAN KHI: "Tang diem sat thuong" — cong SAU CUNG ----
        // Dat o cuoi ham nen khong bi bat ky he so nhan nao phia tren khuech dai,
        // dung nhu mo ta "Tang diem sau cung".
        if (tkDiem > 0) {
            self.attack += tkDiem;
            self.attackPlayer += tkDiem;
        }
    }

}
