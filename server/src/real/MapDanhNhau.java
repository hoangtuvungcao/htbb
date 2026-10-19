package real;


import data.Database;
import data.NewClan;
import io.Message;
import java.io.DataInputStream;
import java.io.IOException;
import java.util.Vector;
import server.TeamServer;

/**
 * Than cua ba method danh nhau cua Map: doAttackPlayer (danh nguoi choi),
 * doAttackMonster (danh quai), doAttackMultiTarget (danh dien rong).
 *
 * 1.572 dong nay truoc nam trong real/Map.java. Ca ba method VAN CON trong
 * Map, giu NGUYEN chu ky cu, chi than chuyen sang day - nen cac lop con
 * (MapChauBau, MapChienTruongMoba, MapClan, MapLienDau, MapLoiDai, Region,
 * RegionVantienTran) van override binh thuong.
 */
class MapDanhNhau {

    static void doAttackPlayer(Map map, Char p, Message message) {
        try {
            if (p.isHoangSo() || p.isHoangLoan()) {
                return;
            }

            if (p.cannotAttackWhenBienhinh()) {
                return;
            }

            if (map.nRegion > 0) {
                ((Region) map.ALL_REGION.get(p.region)).doAttackPlayer(p, message);
                return;
            }

            if (p.countHit() || p.freeze() || (com.htbb.pokemon.LauDai.bat() && TrangThaiGame.khongDanhDuoc(p))) { // GD2b: ngu/bang/te
                return;
            }

            if (p.myCountry == -1) {
                return;
            }

            if (p.hp <= 0) {
                p.actorDie();
                return;
            }

            if (!p.checkDurableWeapone()) {
                p.sendMessage(MessageCreator.createServerAlertMessage("Không thể tấn công khi đồ cầm tay bị hư hại. Hãy đến Thợ Map.rèn để sửa lại.", ""));
                return;
            }

            if (p.checkLamthinh() || p.checkRuNgu() || p.checkChoang()) {
                return;
            }

            p.downDurableWeapone();
            boolean inArenaP = map.inArena(p);
            if (p.isKiller && p.pk != 0 && !p.isMonster) {
                return;
            }

            DataInputStream dis = message.dis;
            Char c = map.getChar(dis.readShort());
            if (c == null) {
                return;
            }
            TuDongGame.khiPvp(c); // bi Pokemon khac tan cong: dung Tu dong danh (tu_dong.tat_khi_pvp)

            if (c.isThangBe()) {
                return;
            }

            if (c.isMyHoVe(p)) {
                return;
            }

            if (c.getIdCharThanThu() > -1) {
                return;
            }

            if (c.hp <= 0) {
                if (map.mapId == Map.idMapTown) {
                    Map.giveCardFail(c);
                }

                c.actorDie();
                return;
            }

            if (c.lvDetail.lv < 50 && c.monster != null && c.map.equals(c.monster.map) && p.lvDetail.lv >= 50 && c.map.mapId != Map.idMapTown) {
                return;
            }

            if (p.lvDetail.lv < 40 && c.monster != null && c.monster.map.equals(c.map)) {
                return;
            }

            if (Map.getTown[p.inCountry] && c.timeGiveCardTown > 0L && System.currentTimeMillis() - c.timeGiveCardTown < 50000L) {
                return;
            }

            boolean timeAutoPK = Map.pkAuto;
            boolean haveShield = c.haveShield();
            if (p.inCountry != c.inCountry) {
                return;
            }

            if (p.myCountry != c.myCountry
                    && (p.monster == null || !p.monster.map.equals(p.map))
                    && c.monster == null
                    && (!((Vector) Map.idMapMONSTER.get(p.inCountry)).contains(map.mapId) || Map.openLog)
                    && haveShield
                    && !p.isKiller
                    && !c.isKiller
                    && (p.pk <= 0 || c.pk <= 0 || p.pk == c.pk)
                    && (!Map.nwar[p.inCountry] || Map.nwar[p.inCountry] && map.mapId != Map.idMapTown)) {
                p.sendMessage(MessageCreator.createMsgChat(p.id, "Người chơi đang được bảo hộ."));
                return;
            }

            if ((map.mapIDLoadMap == Map.idMapMateria || !timeAutoPK)
                    && p.killer >= 319000
                    && p.nPKill >= 500
                    && p.isKiller
                    && map.mapId != Map.idMapDautruong
                    && !((Vector) Map.idMapMONSTER.get(p.myCountry)).contains(map.mapId)) {
                Message m = new Message(27);
                m.dos.writeShort(p.id);
                m.dos.writeUTF("Không thể pk thêm.");
                p.sendMessage(m);
                m.cleanup();
                return;
            }

            if (p.checkInParty(c)) {
                return;
            }

            if (p.map.mapIDLoadMap == 0 || p.map.mapIDLoadMap == 80 || p.map.mapIDLoadMap == 70) {
                p.sendMessage(MessageCreator.createMsgChat(p.id, "Không thể đánh khi đang trong làng."));
                return;
            }

            if (c.isBot != -1) {
                return;
            }

            if (p.mapID != c.mapID) {
                return;
            }

            if (p.region != c.region) {
                return;
            }

            if (((Vector) Map.idMapMONSTER.get(p.myCountry)).contains(map.mapId) && !p.isMonster && !c.isMonster) {
                return;
            }

            boolean inArenaC = map.inArena(c);
            if (!Map.inRangeActor(p, c, Map.MAX_RANGE_CHAR[p.charClass])) {
                return;
            }

            if (map.mapId != Map.idMapDautruong && map.mapId != Map.idMapTown && !((Vector) Map.idMapMONSTER.get(p.myCountry)).contains(map.mapId)) {
                if (map.inMapBoss(c)) {
                    if (c.partyID == p.partyID && c.partyID != -1 && p.partyID != -1 || p.idClan == c.idClan && p.idClan > -1 && c.idClan > -1) {
                        return;
                    }
                } else {
                    if ((!timeAutoPK && map.mapIDLoadMap != 118 || p.myCountry == c.myCountry)
                            && !p.isKiller
                            && p.pk == 0
                            && (c.killer == 0 || !c.isKiller)
                            && (p.myCountry == c.myCountry || p.monster == null && c.monster == null)) {
                        return;
                    }

                    if (p.pk > 0 && (p.myCountry == c.myCountry || p.monster == null && c.monster == null)) {
                        if (!timeAutoPK && (c.pk == 0 && !c.isKiller || c.pk == p.pk) && !p.isMonster && map.mapIDLoadMap == 17) {
                            return;
                        }

                        if ((c.pk == 0 && !c.isKiller || c.pk > 0 && c.pk == p.pk) && !p.isMonster && (map.mapIDLoadMap == 17 || p.myCountry == c.myCountry)) {
                            return;
                        }
                    }
                }
            } else if (map.mapId == Map.idMapTown) {
                if ((!inArenaC || !inArenaP) && !Map.getTown[p.inCountry] && p.myCountry == c.myCountry) {
                    return;
                }

                if (Map.getTown[p.inCountry] && p.idClan == c.idClan && p.idClan > -1 && c.idClan > -1) {
                    return;
                }

                if (Map.nwar[p.inCountry] && p.myCountry != Map.nationBeAttack[p.inCountry] && p.myCountry != p.inCountry) {
                    p.sendMessage(MessageCreator.createServerAlertAutoOffMessage("Không thể tấn công tại vùng này."));
                    int[] mapstart = new int[]{9, 481, 482, 483, 484};
                    int toTX = 16 + Database.r.nextInt() % 5;
                    int toTY = 84;
                    int toMapID = mapstart[Map.r.nextInt(MessageCreator.nclone)];
                    map.move2Map(p, toTX, toTY, toMapID, p.inCountry);
                    return;
                }
            } else if ((p.idClan == -1 || c.idClan == -1 || p.idClan == c.idClan)
                    && !((Vector) Map.idMapMONSTER.get(p.myCountry)).contains(map.mapId)
                    && p.myCountry == c.myCountry) {
                return;
            }

            if (p.isKiller) {
                if (p.lvDetail.lv < 10) {
                    return;
                }

                if (c.lvDetail.lv < 10) {
                    return;
                }
            }

            short skill = dis.readByte();
            byte effect = 0;
            int ahp = p.attackDamage;
            int crit = 0;
            int buffAttack = -1;
            int _type = (byte) skill;
            if (_type < 0 || _type >= p.skill.length) {
                return;
            }
            int _level = p.skill[_type] + p.addMoreLevelSkill[_type];
            if (_level <= 0) {
                _level = p.addMoreLevelSkill[_type];
            }

            long now = System.currentTimeMillis();
            if (now - p.timeLastUseSkills[_type] < (long) (CharManager.SKILL_COOLDOWN[p.charClass][_type][_level] * 100)) {
                return;
            }

            p.timeLastUseSkills[_type] = now;
            if (_level <= 0 || !Map.inRangeSkill(p, c, CharManager.getSkillRange((byte) _type, p.charClass))) {
                return;
            }

            if (p.haveTanPhe() > 0 && c.addEffBuff(EffectBuff.TAN_PHE, System.currentTimeMillis() + (long) p.haveTanPhe(), EffectBuff.BY_ACTOR, 0) != null) {
                c.sendEffToChar(c);
                c.sendEffToNearChar();
                c.divSpeed = 2;
                c.sendMessage(MessageCreator.createMainCharInfoMessage(c));
            }

            if (p.haveLamThinh() > 0 && c.addEffBuff(EffectBuff.LAM_THINH, System.currentTimeMillis() + (long) p.haveLamThinh(), EffectBuff.BY_ACTOR, 0) != null) {
                c.sendEffToChar(c);
                c.sendEffToNearChar();
            }

            buffAttack = p.getBuffEffAttack();
            int damage = p.attackDam(c, _type, _level, buffAttack);
            final int damGocLauDai = damage;
            damage /= 5;
            damage -= damage / 3;
            damage = p.subDam(c, damage);
            damage *= CharManager.UP_DAMGE_SKILL[p.charClass][_type][_level - 1];
            boolean critSv = p.havecrit();
            // Ban co tham so: tru "Khang bao kich" tren mat na cua muc tieu.
            boolean baokich = p.haveBaoKich(c);
            if (baokich) {
                damage = p.tkNhanChiMang(damage, 4);
                effect = 4;
            } else if (critSv) {
                damage = p.tkNhanChiMang(damage, 2);
                effect = 2;
                // Mat na cua muc tieu co the hoa giai ky nang pet (lien kich) cua ke danh.
                if (p.petUsing != null && !Char.hoaGiaiKyNangPet(c)) {
                    long pcLienKich = (long) p.petUsing.getLienKich();
                    damage = (int) ((long) damage + (long) damage * pcLienKich / 100L);
                }
            }
            //xả dame Map.rồng băng
            // Nếu vẫn muốn giữ giới hạn để an toàn server, nâng cap:
            if (damage > 75000) {
                damage = 75000 + Map.r.nextInt(100);
            }
            if (com.htbb.pokemon.LauDai.bat()) {
                // GD2 6.1 PvP (chung voi Region): x0,30, chi mang x1,5, tran 8% HP toi da muc tieu.
                long dLd = (long) damGocLauDai * CharManager.UP_DAMGE_SKILL[p.charClass][_type][_level - 1];
                if ((critSv || baokich) && ChiSoTrangBiGame.giuChiMangPvp(p, c)) { // PvP: tran 30%, tru khang chi mang
                    dLd = dLd * com.htbb.pokemon.SatThuongLauDai.CHI_MANG_PHAN_TRAM / 100L;
                    effect = 2;
                } else if (critSv || baokich) {
                    effect = 0;
                }
                damage = com.htbb.pokemon.SatThuongLauDai.pvp((int) Math.min(Integer.MAX_VALUE, dLd), c.maxhp);
            }
            // ---- RỒNG LỚN: XẢ SAU KHI KẸP 100K ----
            if (p != null && p.animalRide != null && p.animalRide.isRongLon()) {
                damage = p.applyRongLonReleaseTo(damage);
            }




            int hphut = 0;
            if (p.haveHutHp() > 0 && p.hp < p.maxhp) {
                int hp = p.haveHutHp();
                hphut = hp;
                if (c.hp < hp) {
                    hphut = c.hp;
                }

                p.hp += hphut;
                p.calculatorHPMP();
                MessageCreator.createMsgUseHpMP(p, hp, 1);
            }

            damage += hphut;
            int mplost = CharManager.SKILL_MP[p.charClass][_type][_level];
            if (p.mp + p.percentBuff[1] < mplost) {
                return;
            }

            p.mp -= mplost;
            if (p.mp <= 0) {
                p.mp = 0;
            }

            damage = c.checkHapthuSatThuong(damage, p);
            // Truyen ke tan cong vao de "Hoa giai ky nang pet" bo duoc Pha trien.
            damage = c.checkGiamSatThuong(damage, p);
            damage = c.checkPassAttack(p, damage);
            ahp = damage;
            if (damage < 0) {
                ahp = 1;
            }

            c.hp = (int) ((long) c.hp - ((long) damage - c.checkMagicShield(damage)));
            c.downDuarable();
            c.checkNewEffectItem(0, (long) (damage / 10), p);
            if (p.charthanthu != null && c.hp > 0) {
                Vector<LiveActor> target = new Vector();
                target.add(c);
                p.charthanthu.doAttack(target);
                c.hp = c.hp - p.getDamtThanThu(c);
            }

            int damNguyetAnh = p.getPCDamNguyetAnh(skill);
            if (c.hp > 0 && damNguyetAnh > 0) {
                c.hp = c.hp - c.maxhp * damNguyetAnh / 100;
                damage += c.maxhp * damNguyetAnh / 100;
                p.sendEffectBuff(c, EffectBuff.EFF_NGUYET_ANH, 1000);
            }

            if (c.isHieuUngCoLongDuongQua() && c.hp <= 0) {
                c.hp = 1;
            }

            if (c.hp > 0) {
                if (ahp > 0) {
                    p.buffAttackSkill(damage, c);
                }

                p.buffSkillKham(c);
                p.charHireAttackDam(c, _type, _level, buffAttack);
            } else {
                Database.instance
                        .saveOrtherLog("", c.charname, c.hp + "_" + ahp + "_" + p.charname + "_" + Map.getNameMap(map.mapId) + "_" + p.region + "_" + c.region, "die");
                if (c.isMonster) {
                    Vector<Char> players = (Vector<Char>) map.allPlayers.get(c.myCountry);
                    if (!Map.openLog) {
                        players = (Vector<Char>) map.allPlayers.get(0);
                    }

                    for (int i = 0; i < players.size(); i++) {
                        Char ccc = (Char) players.get(i);
                        if (ccc != null && ccc.id != p.id && ccc.hp > 0 && !ccc.isMonster) {
                            ccc.potions[102]++;
                            ccc.sendMessage(MessageCreator.createCharInventoryMessage(ccc, 0));
                            ccc.sendMessage(MessageCreator.createMsgChat(ccc.id, "Nhận được Map.rương bạc"));
                            if (ccc.hp <= 0) {
                                ccc.hp = ccc.maxhp;
                                ccc.sendMessage(MessageCreator.createMainCharInfoMessage(ccc));
                                ccc.sendToNearPlayer(MessageCreator.createNew_HMP_Message(ccc, 0));
                            }
                        }
                    }

                    p.potions[101]++;
                    p.sendMessage(MessageCreator.createCharInventoryMessage(p, 0));
                    p.sendMessage(MessageCreator.createMsgChat(p.id, "Nhận được Map.rương vàng"));
                    map.charMonsterDissapear(c);
                    c.hp = c.maxhp;
                    c.mp = c.maxmp;
                    c.sendMessage(MessageCreator.createMainCharInfoMessage(c));
                    c.sendToNearPlayer(MessageCreator.createNew_HMP_Message(c, 0));

                    try {
                        if (Map.r.nextInt(1000) < 5) {
                            Map.doCreateBookSkillPet(p, 0);
                        }
                    } catch (Exception var32) {
                    }
                }

                if (p.isMonster && map.monsterKillChar(p)) {
                    Vector<Char> players = (Vector<Char>) map.allPlayers.get(p.myCountry);

                    for (int ix = 0; ix < players.size(); ix++) {
                        Char player = (Char) players.get(ix);
                        player.hp = player.maxhp;
                        player.mp = player.maxmp;
                        player.sendMessage(MessageCreator.createMainCharInfoMessage(player));
                        player.sendToNearPlayer(MessageCreator.createNew_HMP_Message(player, 0));
                    }

                    map.charMonsterDissapear(p);
                }

                if (c.hp <= 0) {
                    c.hp = 0;
                    if (!c.isCharCopy()) {
                        c.timedie = System.currentTimeMillis();
                        c.timeWaitComeHome = c.timedie;
                        if (p.lvDetail.lv >= 40
                                && c.lvDetail.lv >= 40
                                && p.myCountry != c.myCountry
                                && !map.isMapMonster()
                                && Map.abs(p.lvDetail.lv - c.lvDetail.lv) <= 10) {
                            p.honor += 2;
                            c.honor -= 10;
                            if (c.honor < 0) {
                                c.honor = 0;
                            }

                            p.sendMessage(MessageCreator.createMainCharInfoMessage(p));
                            c.sendMessage(MessageCreator.createMainCharInfoMessage(c));
                        }
                    }

                    if (c.isCharCopy()) {
                        c.actorDie();
                    }

                    c.doSetTimeAutoHoiSinh();
                }

                map.checkTrade(c);
                if (c.mapID == Map.idMapTown && c.idClan != -1 && Map.getTown[p.inCountry] && Map.giveCardFail(c)) {
                    Database.instance.saveLogClan(p.charname, "pcok", "Pha the cua " + c.charname);
                    map.doSend2AllChar(MessageCreator.createServerAlertAutoOffMessage("Đội " + Map.getClaninfoByID(c.idClan).name + " giao thẻ thất bại"), c.myCountry);
                    map.sendAllPlayer(MessageCreator.createMsgStartGetTown(p.inCountry), p.inCountry);
                }

                if (c.isKiller && (map.mapIDLoadMap != 118 || p.monster != null && (p.monster == null || p.monster.map.equals(p.map)))) {
                    if (!((Vector) Map.idMapMONSTER.get(p.myCountry)).contains(map.mapId)) {
                        try {
                            c.desTroy();
                            long xp = c.lvDetail.getExp();
                            long xplost = c.lvDetail.getXPLost(c.killer, c);
                            if (c.isKiller) {
                                c.killer = (short) (c.killer - 5);
                                if (c.killer <= 0) {
                                    c.killer = 0;
                                    c.isKiller = false;
                                }
                            }

                            Database.instance.saveOrtherLog("", c.charname, p.charname + "_" + Map.getNameMap(map.mapId) + "_" + p.region + "_" + c.region, "die");
                            xp -= xplost;
                            c.xpLost += xplost;
                            int currentlv = c.lvDetail.lv;
                            c.lvDetail.setExp(xp, c.oldLv, c.getName(), "map doattackplayer");
                            if (c.lvDetail.lv <= 0) {
                                c.lvDetail.lv = 0;
                                c.lvDetail.percent = 0;
                            }

                            if (currentlv > c.lvDetail.lv) {
                                c.lvDetail.resetExp2Lv(currentlv, c.killer);
                                if (c.killer > 0) {
                                    Database.instance.saveOrtherLog("", c.charname, "tut level do dang trong che do ds " + c.killer, "downlv");
                                }
                            }

                            c.calculateAttrib();
                            c.doSendProperties();
                            Message m = new Message(67);
                            m.dos.writeShort(c.id);
                            m.dos.writeByte(c.isKiller ? 1 : 0);
                            m.dos.writeShort(c.killer);
                            c.sendMessage(m);
                            c.sendToNearPlayer(m);
                            m.cleanup();
                        } catch (Exception var31) {
                            var31.printStackTrace();
                        }
                    }
                } else {
                    c.die_pk = true;
                }

                if (c.myCountry != p.myCountry && map.mapIDLoadMap != 118 && map.mapIDLoadMap != 17 && !map.isMapMonster()) {
                    if (p.myCountry == p.inCountry) {
                        if (!timeAutoPK) {
                            Map.sendAllCharServerExceptWarTown(
                                    17,
                                    MessageCreator.createServerAlertAutoOffMessage(
                                            p.charname
                                            + " "
                                            + Map.nameCountry[p.myCountry]
                                            + " đánh bại "
                                            + c.charname
                                            + " "
                                            + Map.nameCountry[c.myCountry]
                                            + " tại "
                                            + Map.getNameMap(map.mapId)
                                    )
                            );
                        }
                    } else {
                        if (!timeAutoPK) {
                            Map.sendAllCharServerExceptWarTown(
                                    17,
                                    MessageCreator.createServerAlertAutoOffMessage(
                                            c.charname
                                            + " "
                                            + Map.nameCountry[c.myCountry]
                                            + " bị "
                                            + p.charname
                                            + " "
                                            + Map.nameCountry[p.myCountry]
                                            + " đánh bại tại "
                                            + Map.getNameMap(map.mapId)
                                    )
                            );
                        }

                        Map.addCharKiller(p, p.inCountry);
                    }
                }
            }

            if (p.isKiller) {
                if ((p.killer == 0
                        && map.mapId != Map.idMapDautruong
                        && map.mapId != Map.idMapDautruong
                        && !((Vector) Map.idMapMONSTER.get(p.myCountry)).contains(map.mapId)
                        || map.mapId == Map.idMapTown && !Map.nwar[p.inCountry] && !Map.getTown[p.inCountry])
                        && p.killer < 32000) {
                    if (p.charClass == 4) {
                        p.killer = (short) (p.killer + 50);
                    } else {
                        p.killer++;
                    }
                }

                if (c.hp <= 0) {
                    ThangPvpGame.ketQua(p, c); // GD2 7.2 Elo mua (tat co: khong lam gi)
                }
                if (c.hp <= 0
                        && !c.isKiller
                        && (map.mapId != Map.idMapDautruong && map.mapId != Map.idMapTown && !((Vector) Map.idMapMONSTER.get(p.myCountry)).contains(map.mapId)
                        || map.mapId == Map.idMapTown && !Map.nwar[p.inCountry] && !Map.getTown[p.inCountry])) {
                    if (p.killer < 32000) {
                        p.killer = (short) (p.killer + 100);
                    }

                    if (p.nPKill == 0) {
                        p.timeKiller = System.currentTimeMillis();
                    }

                    p.nPKill++;
                    if (p.killer >= 32000) {
                        p.killer = 32000;
                    }

                    c.addListKillMe(p.charname);
                }

                Message msg = new Message(67);
                msg.dos.writeShort(p.id);
                msg.dos.writeByte(1);
                msg.dos.writeShort(p.killer);
                p.sendMessage(msg);
                p.sendToNearPlayer(msg);
                msg.cleanup();
            }

            if (c.hp <= 0 && map.mapId == Map.idMapDautruong && p.idClan > -1) {
                try {
                    NewClan clan = NewClan.getClan(p.idClan);
                    if (clan.level < NewClan.MAX_LEVEL) {
                        NewClan.addXpClan(clan, 1);
                    }

                    clan = NewClan.getClan(c.idClan);
                    NewClan.addXpClan(clan, -1);
                } catch (Exception var30) {
                }
            }

            if (c.hp <= 0 && !((Vector) Map.idMapMONSTER.get(p.myCountry)).contains(map.mapId) && Map.nwar[p.inCountry] && map.mapId == 201) {
                if (!p.checkLienTram(c.charname)) {
                    p.totalKill++;
                    if (p.totalKill > p.maxKill) {
                        p.maxKill = p.totalKill;
                        Map.doAddTopLienTram(p.myCountry, p);
                    }
                }

                c.totalKill = 0;
            }

            Message m = new Message(6);
            m.dos.writeShort(p.id);
            m.dos.writeShort(c.id);
            m.dos.writeByte(skill);
            m.dos.writeInt(ahp);
            m.dos.writeInt(c.hp);
            m.dos.writeByte(effect);
            m.dos.writeByte(CharManager.UP_DAMGE_SKILL[p.charClass][_type][_level - 1]);
            m.dos.writeByte(buffAttack);
            m.dos.writeByte(_level);
            if (com.htbb.pokemon.LauDai.bat()) { // GD2 6.5/6.6: goi kem -95 + dong gop/pha boss
                // PvP (lenh 6): chi goi kem -95
                m.kemTheo = GoiLauDaiGame.don(p, c, ahp);
            }
            p.sendMessage(m);
            p.sendToNearPlayer(m);
            m.cleanup();
        } catch (Exception var33) {
            var33.printStackTrace();
        }
    }


    static void doAttackMonster(Map map, Char p, Message message) throws IOException {
        if (!p.cannotAttackWhenBienhinh()) {
            if (!p.isHoangSo() && !p.isHoangLoan()) {
                if (map.mapIDLoadMap != 0 && map.mapIDLoadMap != 70 && map.mapIDLoadMap != 80) {
                    if (map.nRegion > 0) {
                        ((Region) map.ALL_REGION.get(p.region)).doAttackMonster(p, message);
                    } else if (!p.countHit() && !p.freeze()) {
                        if (p.itemAx != null || map.mapId != 17) {
                            if (p.hp <= 0) {
                                p.actorDie();
                            } else if (!p.checkDurableWeapone()) {
                                p.sendMessage(MessageCreator.createServerAlertMessage("Không thể đánh khi đồ cầm tay bị hỏng. Hãy đến Thợ Map.rèn để sửa lại1.", ""));
                                Message m = new Message(104);

                                try {
                                    m.dos.writeByte(p.typeConfig);
                                    m.dos.writeByte(0);
                                    p.sendMessage(m);
                                } catch (Exception var35) {
                                }
                            } else if (!p.checkLamthinh() && !p.checkRuNgu() && !p.checkChoang()) {
                                p.downDurableWeapone();
                                DataInputStream dis = message.dis;
                                Monster mt = map.getMonster(dis.readShort(), p.inCountry, p.region);
                                short skill = dis.readByte();
                                byte effect = 0;
                                int ahp = p.attackDamage;
                                int crit = 0;
                                int buffAttack = -1;
                                if (buffAttack <= 0) {
                                    if (buffAttack == -1 || buffAttack != 0 || p.skill[5] + p.addMoreLevelSkill[5] != 0) {
                                        if (mt != null && !mt.isDead && !mt.isFinish()) {
                                            if (p.isPetCanAttack(mt)) {
                                                if (Map.inRangeActor(p, mt, Map.MAX_RANGE_CHAR[p.charClass])) {
                                                    if (mt.getCharID() != p.charDBID) {
                                                        if (mt.map.mapId == p.mapID) {
                                                            if (!mt.isMyMonster(p)) {
                                                                int _type = (byte) skill;
                                                                if (_type < 0 || _type >= p.skill.length) {
                                                                    return;
                                                                }
                                                                int _level = p.skill[_type] + p.addMoreLevelSkill[_type];
                                                                if (_level <= 0) {
                                                                    _level = p.addMoreLevelSkill[_type];
                                                                }

                                                                if (_level != 0 && Map.inRangeSkill(p, mt, Map.MAX_RANGE_CHAR[p.charClass])) {
                                                                    long now = System.currentTimeMillis();
                                                                    if (now - p.timeLastUseSkills[_type] >= (long) (CharManager.SKILL_COOLDOWN[p.charClass][_type][_level] * 100)) {
                                                                        if (Map.canAttackMonsterVantieu(mt, p)) {
                                                                            if (mt.isRuongMaquai()) {
                                                                                if (p.itemVukhiThoiTrang == null || !p.itemVukhiThoiTrang.isGayGiangSinh()) {
                                                                                    return;
                                                                                }

                                                                                if (mt.getInCountry() != p.inCountry) {
                                                                                    p.sendMessage(MessageCreator.createMsgChat(p.id, "Không thể đánh Map.rương của làng khác."));
                                                                                    return;
                                                                                }
                                                                            }

                                                                            int hphut = 0;
                                                                            if (p.haveHutHp() > 0 && p.hp < p.maxhp && !mt.isNgocRong() && !mt.isRuongMaquai()) {
                                                                                int hp = p.haveHutHp();
                                                                                hphut = hp;
                                                                                if (mt.hp < hp) {
                                                                                    hphut = mt.hp;
                                                                                }

                                                                                p.hp += hphut;
                                                                                p.calculatorHPMP();
                                                                                MessageCreator.createMsgUseHpMP(p, hp, 1);
                                                                            }

                                                                            p.timeLastUseSkills[_type] = now;
                                                                            buffAttack = p.getBuffEffAttack();
                                                                            if (mt.resistThroughArmor()) {
                                                                                buffAttack = -1;
                                                                            }

                                                                            int damage = p.attackDam(mt, _type, _level, buffAttack);
                                                                            if (mt.isNgocRong()) {
                                                                                if (damage == 1) {
                                                                                    if (p.potions[73] == 0) {
                                                                                        p.sendMessage(MessageCreator.createMsgChat(p.id, "Không có túi nhặt ngọc"));
                                                                                    } else {
                                                                                        mt.onDropItem(map, p);
                                                                                    }
                                                                                } else if (mt.isDead) {
                                                                                    mt.sendActorDie(p);
                                                                                }
                                                                            } else {
                                                                                if (mt.haveDodge()) {
                                                                                    damage = 0;
                                                                                    buffAttack = -1;
                                                                                }

                                                                                if (mt.isMonsterVantieu() && damage >= mt.maxhp / 100) {
                                                                                    damage = mt.maxhp / 100 + Map.r.nextInt() % 100;
                                                                                }

                                                                                damage *= CharManager.UP_DAMGE_SKILL[p.charClass][_type][_level - 1];
                                                                                boolean critSv = p.havecrit();
                                                                                boolean baokich = p.haveBaoKich();
                                                                                if (baokich) {
                                                                                    damage = p.tkNhanChiMang(damage, 4);
                                                                                    effect = 4;
                                                                                } else if (critSv) {
                                                                                    damage = p.tkNhanChiMang(damage, 2);
                                                                                    effect = 2;
                                                                                    if (p.petUsing != null) {
                                                                                        long pcLienKich = (long) p.petUsing.getLienKich();
                                                                                        damage = (int) ((long) damage + (long) damage * pcLienKich / 100L);
                                                                                    }
                                                                                }

                                                                                if (_level <= p.skill[_type] + p.addMoreLevelSkill[_type]) {
                                                                                    int mplost = CharManager.SKILL_MP[p.charClass][_type][_level];
                                                                                    if (p.mp + p.percentBuff[1] >= mplost) {
                                                                                        p.mp -= mplost;
                                                                                        if (p.mp <= 0) {
                                                                                            p.mp = 0;
                                                                                        }

                                                                                        int getXp = mt.getXpReceive(damage);
                                                                                        ahp = damage / CharManager.UP_DAMGE_SKILL[p.charClass][_type][_level - 1];
                                                                                        mt.hp -= damage + hphut;
                                                                                        BossTrungThu.ghiNhanSatThuong(mt, p, damage + hphut);
                                                                                        if (damage > 0 && mt.haveBackDam()) {
                                                                                            int backdam = mt.getBackDam(damage);
                                                                                            Message mbd = MessageCreator.createMsgBuffEffect(5, mt.cat, p, backdam, 0, -1);
                                                                                            p.sendMessage(mbd);
                                                                                            p.sendToNearPlayer(mbd);
                                                                                        }

                                                                                        Message m = null;
                                                                                        if (p.charthanthu != null && mt.hp > 0 && !mt.isBossSonTinhThuyTinh()) {
                                                                                            Vector<LiveActor> target = new Vector();
                                                                                            target.add(mt);
                                                                                            p.charthanthu.doAttack(target);
                                                                                            int damthanthu = p.getDamtThanThu(mt);
                                                                                            getXp += mt.getXpReceive(damthanthu);
                                                                                            mt.hp -= damthanthu;
                                                                                            BossTrungThu.ghiNhanSatThuong(mt, p, damthanthu);
                                                                                        }
                                                                               


                                                                                        if (mt.hp > 0 && !mt.isRuongMaquai()) {
                                                                                            int damNguyetAnh = p.getPCDamNguyetAnh(skill);
                                                                                            if (mt.hp > 0 && damNguyetAnh > 0) {
                                                                                                mt.hp = mt.hp - mt.maxhp * damNguyetAnh / 100;
                                                                                                damage += mt.maxhp * damNguyetAnh / 100;
                                                                                                p.sendEffectBuff(mt, EffectBuff.EFF_NGUYET_ANH, 1000);
                                                                                            }
                                                                                        }

                                                                                        if (mt.hp > 0) {
                                                                                            if (mt.isMonsterVantieu() && p.killer < 200) {
                                                                                                p.isKiller = true;
                                                                                                p.killer = (short) (p.killer + 200);
                                                                                                Message msg = new Message(67);
                                                                                                msg.dos.writeShort(p.id);
                                                                                                msg.dos.writeByte(1);
                                                                                                msg.dos.writeShort(p.killer);
                                                                                                p.sendMessage(msg);
                                                                                                p.sendToNearPlayer(msg);
                                                                                            }

                                                                                            if (mt.target == null) {
                                                                                                mt.target = p;
                                                                                            }

                                                                                            if (ahp > 0) {
                                                                                                p.buffAttackSkill(damage, mt);
                                                                                            }

                                                                                            if (getXp > 0) {
                                                                                                int x2Player = p.getX2();
                                                                                                if (TeamServer.isDouble) {
                                                                                                    int var51 = 0;
                                                                                                }

                                                                                                int dxp = Map.rand10(getXp);
                                                                                                if (dxp == 0) {
                                                                                                    dxp = 1;
                                                                                                }

                                                                                                // Truoc day phan tu cuoi la "dxp" (tu chia cho chinh no -> luon = 1
                                                                                                // bat ke dxp ban dau la bao nhieu, khong phai giam ti le hop ly).
                                                                                                // Doi thanh so cu the, tiep noi day tang dan cua tung mang.
                                                                                                int[] downPercent = new int[]{1, 5, 10, 20};
                                                                                                int targetLv = p.getLevel();
                                                                                                if (targetLv < 40) {
                                                                                                    downPercent = new int[]{1, 2, 3, 4};
                                                                                                }

                                                                                                int delta = targetLv - mt.level;
                                                                                                if (delta > 0) {
                                                                                                    int a = delta / 4;
                                                                                                    if (targetLv < 40) {
                                                                                                        a = delta / 6;
                                                                                                    }

                                                                                                    if (a > 3) {
                                                                                                        a = 3;
                                                                                                    }

                                                                                                    dxp /= ExpLauDai.chiaCu(downPercent[a]);
                                                                                                    if (dxp <= 0) {
                                                                                                        dxp = 1;
                                                                                                    }
                                                                                                }

                                                                                                if (dxp > 0) {
                                                                                                    // GD2 3.4: bat co sv.lau_dai -> HAM DUY NHAT ExpLauDai.traoHaQuai (thay ban sao duoi).
                                                                                                    if (com.htbb.pokemon.LauDai.bat()) {
                                                                                                        ExpLauDai.traoHaQuai(p, mt, dxp, "MapDanhNhau:837");
                                                                                                    } else {
                                                                                                    int newxp = Map.calculatorXpParty(p, dxp);
                                                                                                    if (newxp == dxp) {
                                                                                                        int var62 = dxp * Map.doubleALL;
                                                                                                        var62 = Monster.apDungGiamExpTheoCapNV(var62, p);
                                                                                                        var62 = p.expReceive(var62);
                                                                                                        Map.addXPForChar(
                                                                                                                p,
                                                                                                                (long) (var62 + p.getEffSkillClan(0) * var62 / 100),
                                                                                                                false,
                                                                                                                "map doAttackMonster3"
                                                                                                        );
                                                                                                    } else {
                                                                                                        int nUser = p.party.userParty.size();
                                                                                                        // GD2 1.6: chia theo SO NGUOI THAT (ban cu ep nUser = 5 khi > 1 nguoi).

                                                                                                        int xpReceive = Map.chiaExpDoi(newxp, 80, nUser, 1);
                                                                                                        int maxLv = p.lvDetail.lv;

                                                                                                        for (int i = 0; i < p.party.userParty.size(); i++) {
                                                                                                            Char pp = (Char) p.party.userParty.get(i);
                                                                                                            if (pp.id != p.id
                                                                                                                    && p.near(pp, 320)
                                                                                                                    && pp.mapID == p.mapID
                                                                                                                    && pp.inCountry == p.inCountry
                                                                                                                    && pp.region == p.region) {
                                                                                                                int dlv = Map.abs(maxLv - pp.lvDetail.lv);
                                                                                                                int temp = 1;
                                                                                                                if (dlv <= 5) {
                                                                                                                    temp = xpReceive;
                                                                                                                } else if (dlv <= 10) {
                                                                                                                    temp = xpReceive / 5;
                                                                                                                } else if (dlv <= 20) {
                                                                                                                    temp = xpReceive / 10;
                                                                                                                } else if (dlv <= 30) {
                                                                                                                    temp = xpReceive / 15;
                                                                                                                } else {
                                                                                                                    temp = xpReceive / 20;
                                                                                                                }

                                                                                                                if (temp == 0) {
                                                                                                                    temp = 1;
                                                                                                                }

                                                                                                                if (pp.hp > 0) {
                                                                                                                    temp *= Map.doubleALL;
                                                                                                                    temp = Monster.apDungGiamExpTheoCapNV(temp, pp);
                                                                                                                    temp = pp.expReceive(temp);
                                                                                                                    Map.addXPForChar(
                                                                                                                            pp,
                                                                                                                            (long) (temp + pp.getEffSkillClan(0) * temp / 100),
                                                                                                                            false,
                                                                                                                            "map doAttackMonster1"
                                                                                                                    );
                                                                                                                }
                                                                                                            }
                                                                                                        }

                                                                                                        xpReceive = Map.chiaExpDoi(newxp, 20, 1, Map.doubleALL);
                                                                                                        xpReceive = Monster.apDungGiamExpTheoCapNV(xpReceive, p);
                                                                                                        xpReceive = p.expReceive(xpReceive);
                                                                                                        Map.addXPForChar(
                                                                                                                p,
                                                                                                                (long) (xpReceive + p.getEffSkillClan(0) * xpReceive / 100),
                                                                                                                false,
                                                                                                                "map doAttackMonster2"
                                                                                                        );
                                                                                                    }
                                                                                                    }
                                                                                                }
                                                                                            }

                                                                                            m = new Message(9);
                                                                                            m.dos.writeShort(p.id);
                                                                                            m.dos.writeShort(mt.id);
                                                                                            m.dos.writeByte(skill);
                                                                                            m.dos.writeInt(ahp);
                                                                                            m.dos.writeInt(mt.hp);
                                                                                            m.dos.writeByte(effect);
                                                                                            m.dos
                                                                                                    .writeByte(
                                                                                                            CharManager.UP_DAMGE_SKILL[p.charClass][_type][_level - 1 >= 0 ? _level - 1 : 0]
                                                                                                    );
                                                                                            m.dos.writeByte(buffAttack);
                                                                                            m.dos.writeByte(_level);
                                                                                            if (com.htbb.pokemon.LauDai.bat()) { // GD2 6.5/6.6: goi kem -95 + dong gop/pha boss
                                                                                                BossLauDai.sauDon(p, mt, ahp);
                                                                                                m.kemTheo = GoiLauDaiGame.don(p, mt, ahp);
                                                                                            }
                                                                                            p.sendMessage(m);
                                                                                            p.sendToNearPlayer(m);
                                                                                            p.buffSkillKham(mt);
                                                                                            p.charHireAttackDam(mt, _type, _level, buffAttack);
                                                                                        } else {
                                                                                            if (mt.isMonsterVantieu() && p.myCountry == mt.inCountry) {
                                                                                                p.isKiller = true;
                                                                                                p.killer = (short) (p.killer + 200);
                                                                                                if (p.killer > 32000) {
                                                                                                    p.killer = 32000;
                                                                                                }

                                                                                                Message msg = new Message(67);
                                                                                                msg.dos.writeShort(p.id);
                                                                                                msg.dos.writeByte(1);
                                                                                                msg.dos.writeShort(p.killer);
                                                                                                p.sendMessage(msg);
                                                                                                p.sendToNearPlayer(msg);
                                                                                            }

                                                                                            new Vector();
                                                                                            mt.hp = 0;
                                                                                            Vector<Actor> var53 = mt.onDropItem(map, p);

                                                                                            try {
                                                                                                int dxpx = Map.rand10(mt.xp);
                                                                                                if (dxpx == 0) {
                                                                                                    dxpx = 1;
                                                                                                }

                                                                                                int[] downPercentx = new int[]{1, 5, 10, 20}; // 0.9.6: nhu nhanh don danh (truoc: dxpx -> dxpx/dxpx = 1, va chia them lan nua trong traoHaQuai)
                                                                                                int targetLvx = p.getLevel();
                                                                                                if (targetLvx < 40) {
                                                                                                    downPercentx = new int[]{1, 2, 3, 4};
                                                                                                }

                                                                                                int deltax = targetLvx - mt.level;
                                                                                                if (deltax > 0) {
                                                                                                    int ax = deltax / 4;
                                                                                                    if (targetLvx < 40) {
                                                                                                        ax = deltax / 6;
                                                                                                    }

                                                                                                    if (ax > 3) {
                                                                                                        ax = 3;
                                                                                                    }

                                                                                                    dxpx /= ExpLauDai.chiaCu(downPercentx[ax]);
                                                                                                    if (dxpx <= 0) {
                                                                                                        dxpx = 1;
                                                                                                    }
                                                                                                }

                                                                                                if (dxpx > 0) {
                                                                                                    // GD2 3.4: bat co sv.lau_dai -> HAM DUY NHAT ExpLauDai.traoHaQuai (thay ban sao duoi).
                                                                                                    if (com.htbb.pokemon.LauDai.bat()) {
                                                                                                        ExpLauDai.traoHaQuai(p, mt, dxpx, "MapDanhNhau:974");
                                                                                                    } else {
                                                                                                    int newxp = Map.calculatorXpParty(p, dxpx);
                                                                                                    if (newxp == dxpx) {
                                                                                                        int var65 = dxpx * Map.doubleALL;
                                                                                                        var65 = Monster.apDungGiamExpTheoCapNV(var65, p);
                                                                                                        var65 = p.expReceive(var65);
                                                                                                        Map.addXPForChar(
                                                                                                                p,
                                                                                                                (long) (var65 + p.getEffSkillClan(0) * var65 / 100),
                                                                                                                false,
                                                                                                                "map doAttackMonster3"
                                                                                                        );
                                                                                                    } else {
                                                                                                        int nUser = p.party.userParty.size();
                                                                                                        // GD2 1.6: chia theo SO NGUOI THAT (ban cu ep nUser = 5 khi > 1 nguoi).

                                                                                                        int xpReceive = Map.chiaExpDoi(newxp, 80, nUser, 1);
                                                                                                        int maxLv = p.lvDetail.lv;

                                                                                                        for (int ix = 0; ix < p.party.userParty.size(); ix++) {
                                                                                                            Char pp = (Char) p.party.userParty.get(ix);
                                                                                                            if (pp.id != p.id
                                                                                                                    && p.near(pp, 320)
                                                                                                                    && pp.mapID == p.mapID
                                                                                                                    && pp.inCountry == p.inCountry
                                                                                                                    && pp.region == p.region) {
                                                                                                                int dlvx = Map.abs(maxLv - pp.lvDetail.lv);
                                                                                                                int tempx = 1;
                                                                                                                if (dlvx <= 5) {
                                                                                                                    tempx = xpReceive;
                                                                                                                } else if (dlvx <= 10) {
                                                                                                                    tempx = xpReceive / 5;
                                                                                                                } else if (dlvx <= 20) {
                                                                                                                    tempx = xpReceive / 10;
                                                                                                                } else if (dlvx <= 30) {
                                                                                                                    tempx = xpReceive / 15;
                                                                                                                } else {
                                                                                                                    tempx = xpReceive / 20;
                                                                                                                }

                                                                                                                if (tempx == 0) {
                                                                                                                    tempx = 1;
                                                                                                                }

                                                                                                                if (pp.hp > 0) {
                                                                                                                    tempx *= Map.doubleALL;
                                                                                                                    tempx = Monster.apDungGiamExpTheoCapNV(tempx, pp);
                                                                                                                    tempx = pp.expReceive(tempx);
                                                                                                                    Map.addXPForChar(
                                                                                                                            pp,
                                                                                                                            (long) (tempx + pp.getEffSkillClan(0) * tempx / 100),
                                                                                                                            false,
                                                                                                                            "map doAttackMonster1"
                                                                                                                    );
                                                                                                                }
                                                                                                            }
                                                                                                        }

                                                                                                        xpReceive = Map.chiaExpDoi(newxp, 20, 1, Map.doubleALL);
                                                                                                        xpReceive = Monster.apDungGiamExpTheoCapNV(xpReceive, p);
                                                                                                        xpReceive = p.expReceive(xpReceive);
                                                                                                        Map.addXPForChar(
                                                                                                                p,
                                                                                                                (long) (xpReceive + p.getEffSkillClan(0) * xpReceive / 100),
                                                                                                                false,
                                                                                                                "map doAttackMonster2"
                                                                                                        );
                                                                                                    }
                                                                                                    }
                                                                                                }
                                                                                            } catch (Exception var37) {
                                                                                            }

                                                                                            try {
                                                                                                m = new Message(17);
                                                                                                m.dos.writeShort(p.id);
                                                                                                m.dos.writeShort(mt.id);
                                                                                                m.dos.writeByte(skill);
                                                                                                m.dos.writeInt(ahp);
                                                                                                m.dos.writeByte(effect);
                                                                                                m.dos.writeByte(var53.size());
                                                                                                if (var53.size() > 0) {
                                                                                                    for (Actor e : var53) {
                                                                                                        Map.writeActorPos(m, e, (byte) 0);
                                                                                                    }
                                                                                                }

                                                                                                int xx2 = CharManager.UP_DAMGE_SKILL[p.charClass][_type][_level - 1 >= 0
                                                                                                        ? _level - 1
                                                                                                        : 0];
                                                                                                m.dos.writeByte(xx2);
                                                                                                m.dos.writeByte(buffAttack);
                                                                                                m.dos.writeByte(_level);
                                                                                                if (com.htbb.pokemon.LauDai.bat()) { // GD2 6.5/6.6: goi kem -95 + dong gop/pha boss
                                                                                                    BossLauDai.sauDon(p, mt, ahp);
                                                                                                    m.kemTheo = GoiLauDaiGame.don(p, mt, ahp);
                                                                                                }
                                                                                                p.sendMessage(m);
                                                                                                p.sendToNearPlayer(m);
                                                                                                if (p.receiveQuest && QuestTemplate.QUEST_TYPE[p.questID - 1] == 0) {
                                                                                                    p.checkFinsishQuest(mt.getType(), -1, -1);
                                                                                                }
                                                                                            } catch (Exception var36) {
                                                                                                System.out.println("loi gui thong tin monsterdie ");
                                                                                            }
                                                                                        }
                                                                                    }
                                                                                }
                                                                            }
                                                                        }
                                                                    }
                                                                }
                                                            }
                                                        }
                                                    }
                                                }
                                            }
                                        } else {
                                            if (mt != null) {
                                                Map.onMosterDie(p, mt, (byte) skill, 1, effect, (byte) 0);
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }


    static void doAttackMultiTarget(Map map, Char p, Message message) {
        if (!p.cannotAttackWhenBienhinh()) {
            if (!p.isHoangSo() && !p.isHoangLoan()) {
                if (map.mapIDLoadMap != 0 && map.mapIDLoadMap != 70 && map.mapIDLoadMap != 80) {
                    try {

                        if (map.nRegion > 0) {
                            ((Region) map.ALL_REGION.get(p.region)).doAttackMultiTarget(p, message);
                            return;
                        }

                        if (p.countHit() || p.freeze() || (com.htbb.pokemon.LauDai.bat() && TrangThaiGame.khongDanhDuoc(p))) { // GD2b: ngu/bang/te
                            return;
                        }

                        if (p.itemAx == null && map.mapId == 17) {
                            return;
                        }

                        if (p.hp <= 0) {
                            p.actorDie();
                            return;
                        }

                        if (!p.checkDurableWeapone()) {
                            p.sendMessage(MessageCreator.createServerAlertMessage("Không thể đánh khi đồ cầm tay bị hỏng. Hãy đến Thợ Map.rèn để sửa lại.", ""));
                            Message m = new Message(104);

                            try {
                                m.dos.writeByte(p.typeConfig);
                                m.dos.writeByte(0);
                                p.sendMessage(m);
                            } catch (Exception var41) {
                            }

                            return;
                        }

                        if (p.checkLamthinh() || p.checkRuNgu() || p.checkChoang()) {
                            return;
                        }

                        DataInputStream dis = message.dis;
                        byte skill = dis.readByte();
                        byte effect = 0;
                        int ahp1 = 0;
                        int crit = 0;
                        int buffAttack = -1;
                        if (buffAttack > 0) {
                            return;
                        }

                        if (buffAttack != -1 && buffAttack == 0 && p.skill[5] + p.addMoreLevelSkill[5] == 0) {
                            return;
                        }

                        int nMonster = dis.readByte();
                        Monster firstMonster = null;
                        Monster mt = map.getMonster(dis.readShort(), p.inCountry, p.region);
                        firstMonster = mt;
                        if (mt == null || mt.isDead || mt.isFinish()) {
                            if (mt != null) {
                                Map.onMosterDie(p, mt, skill, 1, effect, (byte) 0);
                            }

                            return;
                        }

                        if (mt.getCharID() == p.charDBID) {
                            return;
                        }

                        if (!Map.inRangeActor(p, mt, Map.MAX_RANGE_CHAR[p.charClass])) {
                            return;
                        }

                        if (!p.isPetCanAttack(mt)) {
                            return;
                        }

                        if (mt.map.mapId != p.mapID) {
                            return;
                        }

                        if (mt.isMyMonster(p)) {
                            return;
                        }

                        if (!Map.canAttackMonsterVantieu(mt, p)) {
                            return;
                        }

                        int _type = skill;
                        int _level = p.skill[skill] + p.addMoreLevelSkill[skill];
                        if (_level <= 0) {
                            _level = p.addMoreLevelSkill[skill];
                        }

                        if (_level <= 0 || !Map.inRangeSkill(p, mt, Map.MAX_RANGE_CHAR[p.charClass])) {
                            return;
                        }

                        long now = System.currentTimeMillis();
                        if (now - p.timeLastUseSkills[skill] < (long) p.coolDown[skill][_level]) {
                            return;
                        }

                        if (mt.isRuongMaquai()) {
                            if (p.itemVukhiThoiTrang == null || !p.itemVukhiThoiTrang.isGayGiangSinh()) {
                                return;
                            }

                            if (mt.getInCountry() != p.inCountry) {
                                p.sendMessage(MessageCreator.createMsgChat(p.id, "Không thể đánh Map.rương của làng khác."));
                                return;
                            }
                        }

                        int hphut = 0;
                        if (p.haveHutHp() > 0 && p.hp < p.maxhp && !mt.isNgocRong() && !mt.isRuongMaquai()) {
                            int hp = p.haveHutHp();
                            hphut = hp;
                            if (mt.hp < hp) {
                                hphut = mt.hp;
                            }

                            p.hp += hphut;
                            p.calculatorHPMP();
                            MessageCreator.createMsgUseHpMP(p, hp, 1);
                        }

                        p.timeLastUseSkills[skill] = now;
                        buffAttack = p.getBuffEffAttack();
                        if (mt.resistThroughArmor()) {
                            buffAttack = -1;
                        }

                        int damage = p.attackDam(mt, skill, _level, buffAttack);
                        if (mt.haveDodge()) {
                            damage = 0;
                            buffAttack = -1;
                        }

                        if (mt.isMonsterVantieu() && damage >= mt.maxhp / 100) {
                            damage = mt.maxhp / 100 + Map.r.nextInt() % 100;
                        }

                        if (mt.isNgocRong()) {
                            if (damage == 1) {
                                if (p.potions[73] == 0) {
                                    p.sendMessage(MessageCreator.createMsgChat(p.id, "Không có túi nhặt ngọc"));
                                } else {
                                    mt.onDropItem(map, p);
                                }
                            } else if (mt.isDead) {
                                mt.sendActorDie(p);
                            }

                            return;
                        }

                        damage *= CharManager.UP_DAMGE_SKILL[p.charClass][skill][_level - 1];
                        boolean critSv = p.havecrit();
                        boolean baokich = p.haveBaoKich();
                        if (baokich) {
                            damage = p.tkNhanChiMang(damage, 4);
                            effect = 4;
                        } else if (critSv) {
                            damage = p.tkNhanChiMang(damage, 2);
                            effect = 2;
                            if (p.petUsing != null) {
                                long pcLienKich = (long) p.petUsing.getLienKich();
                                damage = (int) ((long) damage + (long) damage * pcLienKich / 100L);
                            }
                        }

                        if (_level > p.skill[skill] + p.addMoreLevelSkill[skill]) {
                            return;
                        }

                        damage += hphut;
                        int mplost = CharManager.SKILL_MP[p.charClass][skill][_level];
                        if (p.mp + p.percentBuff[1] < mplost) {
                            return;
                        }

                        p.mp -= mplost;
                        if (p.mp <= 0) {
                            p.mp = 0;
                        }

                        if (damage > 0 && mt.haveBackDam()) {
                            int backdam = mt.getBackDam(damage);
                            Message mbd = MessageCreator.createMsgBuffEffect(5, mt.cat, p, backdam, 0, -1);
                            p.sendMessage(mbd);
                            p.sendToNearPlayer(mbd);
                        }

                        Message m = null;
                        int i = 0;
                        int allXP = 0;
                        Vector<Monster> mst = new Vector();
                        mst.add(mt);
                        byte[] nmonster = new byte[]{5, 5, 8, 10, 10, 10, 10, 10, 10, 10, 10, 10};
                        Vector<Message> msgMonsterDie = new Vector();
                        if (map.mapId == 17) {
                            nmonster = new byte[]{1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1};
                        }

                        Vector<LiveActor> target = new Vector();
                        Vector<LiveActor> muctieu = new Vector();
                        int damNguyetAnh = p.getPCDamNguyetAnh(skill);
                        // Chi cho phep danh nhieu muc tieu neu skill nay thuc su la skill dien
                        // rong (AOE) cua class nay (CharManager.SKILL_AEO). Client co the gui
                        // nham code da muc tieu cho skill don muc tieu; server phai tu kiem tra
                        // thay vi tin tuong client, neu khong quai dung gan se bi an theo.
                        boolean isAoeSkill = false;
                        for (final byte aeoSkill : CharManager.SKILL_AEO[p.charClass]) {
                            if (aeoSkill == skill) {
                                isAoeSkill = true;
                                break;
                            }
                        }

                        while (i < nMonster) {
                            if (i > 0) {
                                mt = map.getMonster(dis.readShort(), p.inCountry, p.region);
                                if (!isAoeSkill) {
                                    i++;
                                    continue;
                                }
                            }

                            if (mt != null) {
                                if (mt.isNgocRong()) {
                                    i++;
                                    continue;
                                }

                                if (!p.isPetCanAttack(mt)) {
                                    continue;
                                }

                                if (mt.isRuongMaquai()) {
                                    if (p.itemVukhiThoiTrang == null || !p.itemVukhiThoiTrang.isGayGiangSinh()) {
                                        i++;
                                        continue;
                                    }

                                    if (mt.getInCountry() != p.inCountry) {
                                        i++;
                                        p.sendMessage(MessageCreator.createMsgChat(p.id, "Không thể đánh Map.rương của làng khác."));
                                        continue;
                                    }
                                }

                                if (i > 0) {
                                    if (mt.isMyMonster(p) || !Map.canAttackMonsterVantieu(mt, p) || !Map.inRangeActor(firstMonster, mt, Map.MAX_RANGE_CHAR[p.charClass])) 
                                    {
                                        i++;
                                        continue;
                                    }

                                    if (mt.isDead) {
                                        // Quai da chet (thuong do mot id trung lap trong cung goi 106 -
                                        // xem Region.java cho phan tich day du): PHAI bo qua ngay, khong
                                        // duoc de roi xuong tinh sat thuong/onDropItem lai. Thieu continue
                                        // o day la ke ho cho phep mot goi tin quay drop + cong exp nhieu
                                        // lan tren MOT con quai (gui trung id lien tiep).
                                        Map.onMosterDie(p, mt, skill, damage, effect, (byte) 0);
                                        i++;
                                        continue;
                                    } else {
                                        mst.add(mt);
                                    }
                                }

                                int dxp = mt.getXpReceive(damage);
                                if (dxp == 0) {
                                    dxp = 1;
                                }

                                // Truoc day phan tu cuoi la "dxp" (tu chia cho chinh no -> luon = 1
                                // bat ke dxp ban dau la bao nhieu). Doi thanh so cu the, tiep noi
                                // day tang dan cua tung mang (giong doAttackMonster).
                                int[] downPercent = new int[]{1, 5, 10, 20};
                                int targetLv = p.getLevel();
                                if (targetLv < 40) {
                                    downPercent = new int[]{1, 2, 3, 4};
                                }

                                int delta = targetLv - mt.level;
                                if (delta > 0) {
                                    int a = delta / 4;
                                    if (targetLv < 40) {
                                        a = delta / 6;
                                    }

                                    if (a > 3) {
                                        a = 3;
                                    }

                                    dxp /= ExpLauDai.chiaCu(downPercent[a]);
                                }

                                if (dxp <= 0) {
                                    dxp = 1;
                                }

                                allXP += ExpLauDai.lechCapMot(dxp, p, mt);
                                int s2dam = damage;
                                if (mt.isMonsterVantieu()) {
                                    s2dam = p.attackDam(mt, _type, _level, buffAttack);
                                    if (s2dam >= mt.maxhp / 100) {
                                        s2dam = mt.maxhp / 100 + Map.r.nextInt() % 100;
                                    }

                                    s2dam *= CharManager.UP_DAMGE_SKILL[p.charClass][_type][_level - 1];
                                }

                                mt.hp -= s2dam;
                                BossTrungThu.ghiNhanSatThuong(mt, p, s2dam);
                                if (p.charthanthu != null && mt.hp > 0 && !mt.isBossSonTinhThuyTinh()) {
                                    target.add(mt);
                                    int damthanthu = p.getDamtThanThu(mt);
                                    allXP += ExpLauDai.lechCapMot(mt.getXpReceive(damthanthu), p, mt);
                                    mt.hp -= damthanthu;
                                    BossTrungThu.ghiNhanSatThuong(mt, p, damthanthu);
                                }

                                if (mt.hp > 0 && mt.hp > 0 && damNguyetAnh > 0) {
                                    mt.hp = mt.hp - mt.maxhp * damNguyetAnh / 100;
                                    damage += mt.maxhp * damNguyetAnh / 100;
                                    p.sendEffectBuff(mt, EffectBuff.EFF_NGUYET_ANH, 1000);
                                }

                                if (mt.hp > 0) {
                                    if (mt.isMonsterVantieu() && p.killer < 200) {
                                        p.isKiller = true;
                                        p.killer = (short) (p.killer + 200);
                                        Message msg = new Message(67);
                                        msg.dos.writeShort(p.id);
                                        msg.dos.writeByte(1);
                                        msg.dos.writeShort(p.killer);
                                        p.sendMessage(msg);
                                        p.sendToNearPlayer(msg);
                                    }

                                    if (mt.target == null) {
                                        mt.target = p;
                                    }
                                } else {
                                    if (mt.isMonsterVantieu() && p.myCountry == mt.inCountry) {
                                        p.isKiller = true;
                                        p.killer = (short) (p.killer + 200);
                                        if (p.killer > 32000) {
                                            p.killer = 32000;
                                        }

                                        Message msg = new Message(67);
                                        msg.dos.writeShort(p.id);
                                        msg.dos.writeByte(1);
                                        msg.dos.writeShort(p.killer);
                                        p.sendMessage(msg);
                                        p.sendToNearPlayer(msg);
                                    }

                                    Vector<Actor> droplist = new Vector();
                                    mt.hp = 0;
                                    if (!mt.isMaterialMons()) {
                                        droplist = mt.onDropItem(map, p);
                                    }

                                    m = new Message(17);
                                    m.dos.writeShort(p.id);
                                    m.dos.writeShort(mt.id);
                                    m.dos.writeByte(skill);
                                    m.dos.writeInt(damage);
                                    m.dos.writeByte(effect);
                                    m.dos.writeByte(droplist.size());
                                    if (droplist.size() > 0) {
                                        for (Actor e : droplist) {
                                            Map.writeActorPos(m, e, p.getSession().isOldVersion);
                                        }
                                    }

                                    int xx2 = CharManager.UP_DAMGE_SKILL[p.charClass][_type][_level - 1 >= 0 ? _level - 1 : 0];
                                    m.dos.writeByte(xx2);
                                    m.dos.writeByte(buffAttack);
                                    m.dos.writeByte(_level);
                                    msgMonsterDie.add(m);
                                    if (p.receiveQuest && QuestTemplate.QUEST_TYPE[p.questID - 1] == 0) {
                                        p.checkFinsishQuest(mt.getType(), -1, -1);
                                    }
                                }

                                if (mt.hp <= 0) {
                                    if (!mt.isBoss || !mt.isCopy()) {
                                        if (mt.isBoss) {
                                            mt.bornTime = System.currentTimeMillis() + 86400000L;
                                            mt.setTimeReBornInEvent(mt.bornTime);
                                            Database.instance.saveEvent(Map.event.getInfo());
                                            Map.removeBossLocation(1);
                                        } else {
                                            mt.setTimeReBorn();
                                        }
                                    }

                                    mt.isDead = true;
                                    mt.target = null;
                                }
                            }

                            i++;
                            if (mst.size() >= nmonster[_level]) {
                                break;
                            }
                        }

                        try {
                            if (p.charthanthu != null && muctieu.size() > 0) {
                                p.charthanthu.doAttack(muctieu);
                            }
                        } catch (Exception var43) {
                        }

                        m = new Message(106);
                        m.dos.writeShort(p.id);
                        m.dos.writeByte(skill);
                        m.dos.writeInt(damage);
                        m.dos.writeByte(effect);
                        m.dos.writeByte(_level);
                        m.dos.writeByte(buffAttack);
                        m.dos.writeByte(mst.size());

                        for (int j = 0; j < mst.size(); j++) {
                            Monster ms = (Monster) mst.elementAt(j);
                            m.dos.writeShort(ms.id);
                            m.dos.writeInt(ms.hp > 0 ? ms.hp : 0);
                        }

                        p.sendMessage(m);
                        p.sendToNearPlayer(m);

                        for (int j = 0; j < msgMonsterDie.size(); j++) {
                            try {
                                p.sendMessage((Message) msgMonsterDie.get(j));
                                p.sendToNearPlayer((Message) msgMonsterDie.get(j));
                            } catch (Exception var42) {
                            }
                        }

                        int dxpx = Map.rand10(allXP);
                        if (dxpx == 0) {
                            dxpx = 1;
                        }

                        if (dxpx > 0) {
                            // GD2 3.4: bat co sv.lau_dai -> HAM DUY NHAT ExpLauDai.traoHaQuai (thay ban sao duoi).
                            if (com.htbb.pokemon.LauDai.bat()) {
                                ExpLauDai.traoHaQuai(p, mt, dxpx, "MapDanhNhau:1559");
                            } else {
                            int newxp = Map.calculatorXpParty(p, dxpx);
                            if (newxp == dxpx) {
                                int var59 = dxpx * Map.doubleALL;
                                var59 = Monster.apDungGiamExpTheoCapNV(var59, p);
                                var59 = p.expReceive(var59);
                                Map.addXPForChar(p, (long) (var59 + p.getEffSkillClan(0) * var59 / 100), false, "map doAttackMultiTarget3");
                            } else {
                                int nUser = p.party.userParty.size();
                                // GD2 1.6: chia theo SO NGUOI THAT (ban cu ep nUser = 5 khi > 1 nguoi).

                                int xpReceive = Map.chiaExpDoi(newxp, 80, nUser, 1);
                                int maxLv = p.lvDetail.lv;

                                for (int k = 0; k < p.party.userParty.size(); k++) {
                                    Char pp = (Char) p.party.userParty.get(k);
                                    if (pp.id != p.id && p.near(pp, 320) && pp.mapID == p.mapID && pp.inCountry == p.inCountry && pp.region == p.region) {
                                        int dlv = Map.abs(maxLv - pp.lvDetail.lv);
                                        int temp = 1;
                                        if (dlv <= 5) {
                                            temp = xpReceive;
                                        } else if (dlv <= 10) {
                                            temp = xpReceive / 5;
                                        } else if (dlv <= 20) {
                                            temp = xpReceive / 10;
                                        } else if (dlv <= 30) {
                                            temp = xpReceive / 15;
                                        } else {
                                            temp = xpReceive / 20;
                                        }

                                        if (temp == 0) {
                                            temp = 1;
                                        }

                                        if (pp.hp > 0) {
                                            temp *= Map.doubleALL;
                                            temp = Monster.apDungGiamExpTheoCapNV(temp, pp);
                                            temp = pp.expReceive(temp);
                                            Map.addXPForChar(pp, (long) (temp + pp.getEffSkillClan(0) * temp / 100), false, "map doAttackMultiTarget1");
                                        }
                                    }
                                }

                                xpReceive = Map.chiaExpDoi(newxp, 20, 1, Map.doubleALL);
                                xpReceive = Monster.apDungGiamExpTheoCapNV(xpReceive, p);
                                xpReceive = p.expReceive(xpReceive);
                                Map.addXPForChar(p, (long) (xpReceive + p.getEffSkillClan(0) * xpReceive / 100), false, "map doAttackMultiTarget2");
                            }
                            }
                        }
                      
                        p.charHireAttackMultiMOnster(mst, _type);
//                         if (Map.r.nextInt(100) < 50) {
//                             p.doAttackSkillNew(mst);
//                         }
                    } catch (Exception var44) {
                        var44.printStackTrace();
                    }
                }
            }
        }
    }

}
