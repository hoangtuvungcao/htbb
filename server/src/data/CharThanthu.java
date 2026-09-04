package data;

import io.Message;
import io.Session;
import java.util.Vector;
import real.Char;
import real.CharChienTruong;
import real.EffectBuff;
import real.LiveActor;
import real.Map;
import real.MapChienTruongMoba;
import real.MessageCreator;

public class CharThanthu extends Char {
    public long timeAttack = 0L;
    public int tox;
    public int toy;
    public Char owner = null;
    public byte idThanThu = 0;
    boolean follow = true;
    public long timeMove = System.currentTimeMillis() + 400L;
    long timeBuffOwner = System.currentTimeMillis() + 20000L;

    public CharThanthu(Session conn) {
        super(conn);
        this.speed = 8;
    }

    public void updateEffectBuff() {
        super.updateEffectBuff();
    }

    public String getName() {
        return super.getName();
    }

    public void update() {
        if (this.follow) {
            this.moved = false;
            if (Map.abs(this.x - this.tox) > 48) {
                if (this.x < this.tox) {
                    this.x += Map.abs(this.x - this.tox) / 24 * 24;
                } else if (this.x > this.tox) {
                    this.x -= Map.abs(this.x - this.tox) / 24 * 24;
                }

                this.moved = true;
            }

            if (Map.abs(this.y - this.toy) > 48) {
                if (this.y < this.toy) {
                    this.y += Map.abs(this.y - this.toy) / 24 * 24;
                } else if (this.y > this.toy) {
                    this.y -= Map.abs(this.y - this.toy) / 24 * 24;
                }

                this.moved = true;
            }

            if (this.moved) {
                this.owner.sendMessage(this.owner.writeActorPos(new Message(4), this));
                if (!this.owner.isAdmin) {
                    this.owner.sendToNearPlayer(this.owner.writeActorPos(new Message(4), this));
                }
            }
        } else if (System.currentTimeMillis() - this.timeMove > 0L && this.map.equals(this.owner.map)) {
            this.timeMove = System.currentTimeMillis() + 400L;
            this.owner.sendMessage(this.writeActorPos(new Message(4), this));
            if (!this.owner.isAdmin) {
                this.owner.sendToNearPlayer(this.owner.writeActorPos(new Message(4), this));
            }
        }

        this.doBuffOwner();
    }

    public void doAttackAuto(Vector<LiveActor> target) {
        try {
            if (this.owner != null && this.owner.map != null && this.owner.map.isMapChienTruongMoba()) {
                CharChienTruong c = MapChienTruongMoba.getCharChienTruong(this.owner.charname);
                if (c != null && c.getNoiLuc() < 10) {
                    return;
                }
            }

            if (((LiveActor)target.get(0)).cat == 1) {
                Message m = new Message(9);
                m.dos.writeShort(this.id);
                m.dos.writeShort(((LiveActor)target.get(0)).id);
                m.dos.writeByte(0);
                m.dos.writeInt(this.attackDam((LiveActor)target.get(0)));
                m.dos.writeInt(((LiveActor)target.get(0)).hp);
                m.dos.writeByte(0);
                m.dos.writeByte(1);
                m.dos.writeByte(-1);
                m.dos.writeByte(1);
                this.owner.sendMessage(m);
                this.owner.sendToNearPlayer(m);

                for(int i = 0; i < target.size(); ++i) {
                    LiveActor mt = (LiveActor)target.get(i);
                    Message msg = MessageCreator.createMsgAddDynamicNewEffect(EffectBuff.EFF_SKILL_HOA_KY_LAN, 100L, EffectBuff.BY_MAP, mt.id, mt.x / 16, mt.y / 16, 127, EffectBuff.ODER_PAINT[EffectBuff.EFF_SKILL_HOA_KY_LAN], this.attackDam((LiveActor)target.get(0)));
                    this.owner.sendMessage(msg);

                    for(int j = this.owner.nearChars.size() - 1; j >= 0; --j) {
                        try {
                            Char p = this.map.getPlayerByID((Short)this.owner.nearChars.get(j));
                            if (p.getIdCharThanThu() == -1 && p != null && p.id != this.owner.id && p.isBot == -1 && this.near(p, this.rangeAddMonster[0]) && p.map == this.map && p.region == this.region) {
                            }

                            p.sendMessage(msg);
                        } catch (Exception var9) {
                        }
                    }
                }
            } else {
                Char c = (Char)target.get(0);
                Message m = new Message(6);
                m.dos.writeShort(this.id);
                m.dos.writeShort(c.id);
                m.dos.writeByte(0);
                m.dos.writeInt(this.attackDamAuto(c));
                m.dos.writeInt(c.hp);
                m.dos.writeByte(0);
                m.dos.writeByte(1);
                m.dos.writeByte(-1);
                m.dos.writeByte(1);
                this.owner.sendMessage(m);
                this.owner.sendToNearPlayer(m);
                Message msg = MessageCreator.createMsgAddDynamicNewEffect(EffectBuff.EFF_SKILL_HOA_KY_LAN, 100L, EffectBuff.BY_MAP, ((LiveActor)target.get(0)).id, ((LiveActor)target.get(0)).x / 16, ((LiveActor)target.get(0)).y / 16, 127, EffectBuff.ODER_PAINT[EffectBuff.EFF_SKILL_HOA_KY_LAN], this.attackDamAuto(c));
                this.owner.sendMessage(msg);

                for(int j = this.owner.nearChars.size() - 1; j >= 0; --j) {
                    try {
                        Char p = this.map.getPlayerByID((Short)this.owner.nearChars.get(j));
                        if (p.getIdCharThanThu() == -1 && p != null && p.id != this.owner.id && p.isBot == -1 && this.near(p, this.rangeAddMonster[0]) && p.map == this.map && p.region == this.region) {
                        }

                        p.sendMessage(msg);
                    } catch (Exception var8) {
                    }
                }
            }
        } catch (Exception var10) {
        }

    }

    public void doAttack(Vector<LiveActor> target) { //xử lý tấn công
       
        try {
            if (this.owner != null && this.owner.map != null && this.owner.map.isMapChienTruongMoba()) {
                CharChienTruong c = MapChienTruongMoba.getCharChienTruong(this.owner.charname);
                if (c != null && c.getNoiLuc() < 10) {
                    return;
                }
            }

            if (((LiveActor)target.get(0)).cat == 1) {
                Message m = new Message(9);
                m.dos.writeShort(this.id);
                m.dos.writeShort(((LiveActor)target.get(0)).id);
                m.dos.writeByte(0);
                m.dos.writeInt(this.attackDam((LiveActor)target.get(0)));
                m.dos.writeInt(((LiveActor)target.get(0)).hp);
                m.dos.writeByte(0);
                m.dos.writeByte(1);
                m.dos.writeByte(-1);
                m.dos.writeByte(1);
                this.owner.sendMessage(m);
                this.owner.sendToNearPlayer(m);

                for(int i = 0; i < target.size(); ++i) {
                    LiveActor mt = (LiveActor)target.get(i);
                    // Truoc day ghi cung "attack*5/100" (dung cho cap 1) lam hieu ung hien
                    // SAI so voi cap 2/3 - dung lai dung so vua tinh o attackDam nhu
                    // doAttackAuto da lam (dong 91/102 phia tren).
                    Message msg = MessageCreator.createMsgAddDynamicNewEffect(EffectBuff.EFF_SKILL_HOA_KY_LAN, 100L, EffectBuff.BY_MAP, mt.id, mt.x / 16, mt.y / 16, 127, EffectBuff.ODER_PAINT[EffectBuff.EFF_SKILL_HOA_KY_LAN], this.attackDam((LiveActor)target.get(0)));
                    this.owner.sendMessage(msg);

                    for(int j = this.owner.nearChars.size() - 1; j >= 0; --j) {
                        try {
                            Char p = this.map.getPlayerByID((Short)this.owner.nearChars.get(j));
                            if (p.getIdCharThanThu() == -1 && p != null && p.id != this.owner.id && p.isBot == -1 && this.near(p, this.rangeAddMonster[0]) && p.map == this.map && p.region == this.region) {
                            }

                            p.sendMessage(msg);
                        } catch (Exception var9) {
                        }
                    }
                }
            } else {
                Char c = (Char)target.get(0);
                Message m = new Message(6);
                m.dos.writeShort(this.id);
                m.dos.writeShort(c.id);
                m.dos.writeByte(0);
                m.dos.writeInt(this.attackDam(c));
                m.dos.writeInt(c.hp);
                m.dos.writeByte(0);
                m.dos.writeByte(1);
                m.dos.writeByte(-1);
                m.dos.writeByte(1);
                this.owner.sendMessage(m);
                this.owner.sendToNearPlayer(m);
                // Truoc day ghi cung "attack*5/100" - dung lai dung so vua tinh o attackDam.
                Message msg = MessageCreator.createMsgAddDynamicNewEffect(EffectBuff.EFF_SKILL_HOA_KY_LAN, 100L, EffectBuff.BY_MAP, ((LiveActor)target.get(0)).id, ((LiveActor)target.get(0)).x / 16, ((LiveActor)target.get(0)).y / 16, 127, EffectBuff.ODER_PAINT[EffectBuff.EFF_SKILL_HOA_KY_LAN], this.attackDam(c));
                this.owner.sendMessage(msg);

                for(int j = this.owner.nearChars.size() - 1; j >= 0; --j) {
                    try {
                        Char p = this.map.getPlayerByID((Short)this.owner.nearChars.get(j));
                        if (p.getIdCharThanThu() == -1 && p != null && p.id != this.owner.id && p.isBot == -1 && this.near(p, this.rangeAddMonster[0]) && p.map == this.map && p.region == this.region) {
                        }

                        p.sendMessage(msg);
                    } catch (Exception var8) {
                    }
                }
            }
        } catch (Exception var10) {
        }

    }

    public void doBuffOwner() {
        try {
            if (this.owner != null && this.owner.map != null && this.owner.map.isMapChienTruongMoba()) {
                CharChienTruong c = MapChienTruongMoba.getCharChienTruong(this.owner.charname);
                if (c != null && c.getNoiLuc() < 10) {
                    return;
                }
            }

            if (this.getIdCharThanThu() > 0 && System.currentTimeMillis() - this.timeBuffOwner >= 0L) {
                this.timeBuffOwner = System.currentTimeMillis() + 20000L;
                if (this.owner.addEffBuff(this.getIdCharThanThu() == 1 ? EffectBuff.EFF_SHIELD_1 : EffectBuff.EFF_SHIELD_2, System.currentTimeMillis() + 10000L, EffectBuff.BY_ACTOR, 0) != null) {
                    this.owner.sendEffToChar(this.owner);
                    this.owner.sendEffToNearChar();
                    Message m = new Message(6);
                    m.dos.writeShort(this.id);
                    m.dos.writeShort(this.owner.id);
                    m.dos.writeByte(0);
                    m.dos.writeInt(0);
                    m.dos.writeInt(this.owner.hp);
                    m.dos.writeByte(0);
                    m.dos.writeByte(1);
                    m.dos.writeByte(-1);
                    m.dos.writeByte(1);
                    this.owner.sendMessage(m);
                    this.owner.sendToNearPlayer(m);
                    Message msg = MessageCreator.createMsgAddDynamicNewEffect(EffectBuff.EFF_SKILL_HOA_KY_LAN, 100L, EffectBuff.BY_MAP, this.owner.id, this.owner.x / 16, this.owner.y / 16, 127, EffectBuff.ODER_PAINT[EffectBuff.EFF_SKILL_HOA_KY_LAN], 0);
                    this.owner.sendMessage(msg);

                    for(int j = this.owner.nearChars.size() - 1; j >= 0; --j) {
                        try {
                            Char p = this.map.getPlayerByID((Short)this.owner.nearChars.get(j));
                            if (p.getIdCharThanThu() == -1 && p != null && p.id != this.owner.id && p.isBot == -1 && this.near(p, this.rangeAddMonster[0]) && p.map == this.map && p.region == this.region) {
                            }

                            p.sendMessage(msg);
                        } catch (Exception var5) {
                        }
                    }
                }
            }
        } catch (Exception var6) {
        }

    }

    public int attackDamAuto(LiveActor actor) {
        if (this.owner != null && this.owner.map != null && this.owner.map.isMapChienTruongMoba()) {
            CharChienTruong c = MapChienTruongMoba.getCharChienTruong(this.owner.charname);
            if (c != null && c.getNoiLuc() < 10) {
                return 0;
            }
        }

        if (!actor.isRuongMaquai() && !actor.isNgocRong() && !actor.isBossSonTinh() && !actor.isBossThuyTinh() && !actor.isBossSonTinhThuyTinh()) {
            if (actor.cat == 0) {
                if (actor.isBatTu()) {
                    return 0;
                }

                // Cung 2 buoc giam dame ma attackDam() (danh thu cong) da co nhung nhip tu
                // dong nay tung bo sot: chenh lech cap (deltalv, chong nguoi cap thap ha
                // nguoi cap cao) va checkGiamSatThuongTong (dau tu giam sat thuong cua chinh
                // nan nhan) - thieu ca hai khien nguoi choi bi tick tu dong nay an sat thuong
                // KHONG duoc giam theo dung dau tu/loi the cap do cua ho.
                int deltalv = this.owner.lvDetail.lv - actor.getLevel();
                int att = this.owner.attack;
                if (deltalv < 0 && Map.abs(deltalv) > 5) {
                    att -= att * (Map.abs(deltalv) - 5) * 2 / 100;
                }

                // Doc thang tu Hoa Ky Lan dang mac (otherAtt 38 = % tan cong chu, 40 = %
                // maxhp doi thu) thay vi ba nhanh cap 1/2/3 rieng - cong duoc "ao hoa"
                // (+10 diem phan tram, xem Char.chiSoPhanTramHoaKyLan) va tu dong dung
                // voi ca cap 4, 5... sau nay neu them, khong can sua lai cho nay.
                // Cap 3 (otherAtt40=20) danh QUAI: dung % maxhp cua quai; cap 1/2 (chua
                // co otherAtt40) roi ve nhanh % tan cong ben duoi - dung y chang cu.
                final int pcMaxHp = this.owner.chiSoPhanTramHoaKyLan(40);
                if (pcMaxHp > 0) {
                    return actor.checkGiamSatThuongTong(actor.maxhp * pcMaxHp / 100);
                }

                return actor.checkGiamSatThuongTong(att * this.owner.chiSoPhanTramHoaKyLan(38) / 100);
            }

            return this.owner.attack * this.owner.chiSoPhanTramHoaKyLan(38) / 100;
        } else {
            return 0;
        }
    }

    public int attackDam(LiveActor actor) {
        if (this.owner != null && this.owner.map != null && this.owner.map.isMapChienTruongMoba()) {
            CharChienTruong c = MapChienTruongMoba.getCharChienTruong(this.owner.charname);
            if (c != null && c.getNoiLuc() < 10) {
                return 0;
            }
        }

        if (!actor.isRuongMaquai() && !actor.isNgocRong() && !actor.isBossSonTinh() && !actor.isBossThuyTinh() && !actor.isBossSonTinhThuyTinh()) {
            if (actor.cat == 0) {
                if (actor.isBatTu()) {
                    return 0;
                }

                int deltalv = this.owner.lvDetail.lv - actor.getLevel();
                int att = this.owner.attack;
                if (deltalv < 0 && Map.abs(deltalv) > 5) {
                    att -= att * (Map.abs(deltalv) - 5) * 2 / 100;
                }

                // Doc thang tu Hoa Ky Lan dang mac (otherAtt 38, cong "ao hoa" neu co) -
                // GHI CHU: khac attackDamAuto, ham nay CHUA TUNG dung otherAtt 40 (% maxhp
                // doi thu) o BAT KY cap nao, ke ca cap 3 - giu dung hanh vi cu, khong them
                // nhanh maxhp o day de khong doi can bang danh thu cong.
                return actor.checkGiamSatThuongTong(att * this.owner.chiSoPhanTramHoaKyLan(38) / 100);
            } else {
                return this.owner.attack * this.owner.chiSoPhanTramHoaKyLan(38) / 100;
            }
        } else {
            return 0;
        }
    }

    public int getIdCharThanThu() {
        return this.idThanThu;
    }

    public void setXtoYto(int x, int y) {
        this.tox = x;
        this.toy = y;
    }

    public boolean canFocus(Char me) {
        return false;
    }
}
