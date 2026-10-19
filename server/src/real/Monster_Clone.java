/*
 * PHUC HOI TU LOI CU (P3): lop nay duoc dich nguoc tu lib/htbb-core.jar - ma loi da bien dich cua
 * game goc - bang Vineflower 1.11.1 roi sua tay toi thieu cho bien dich duoc (--release 8). Giu
 * nguyen ten lop, package va hanh vi; doi ten / don dep de o giai doan sau. Nguon goc: docs/giay-phep.md.
 */
package real;

public class Monster_Clone extends Monster {
    public int xpClone = 0;
    public int goldRcv = 0;
    public boolean isActive = false;

    public Monster_Clone(Map mapLiveIn, MonsterTemplate template, int x, int y, int country) {
        super(mapLiveIn, template, x, y, country);
    }

    public Monster_Clone(byte cat) {
        super(cat);
    }

    @Override
    public void update() {
        if (this.isDead) {
            long now = System.currentTimeMillis();
            if (now > this.bornTime && this.idTemplate != 83) {
                this.bornTime = now;
                this.isDead = false;
                this.timeOutPoinson = 0L;
                this.poinson = 0;
                this.hp = this.maxhp;
                this.xp = this.xpClone;
                this.tDelay = 0;
                this.target = null;
                this.x = this.default_x;
                this.y = this.default_y;
            }
        } else {
            if (System.currentTimeMillis() - this.timeOutPoinson >= this.tDelay * 1000 && this.tDelay > 0) {
                this.getXpReceive(this.poinson);
                this.hp = this.hp - this.poinson;
                this.totalTime = (byte)(this.totalTime - this.tDelay);
                this.timeOutPoinson = System.currentTimeMillis();
                if (this.totalTime == 0) {
                    this.tDelay = 0;
                    this.totalTime = 36;
                }

                if (this.hp <= 0) {
                    this.actorDie();
                    this.totalTime = 36;
                    this.tDelay = 0;
                }
            }

            if (this.beStune) {
                if (System.currentTimeMillis() > this.timeBeStune) {
                    this.beStune = false;
                }
            } else {
                if (this.target == null) {
                    if (!this.beStune && !this.freeze()) {
                        this.move();
                    }
                } else {
                    label114: {
                        if (this.target.getSession() != null && (this.target.getSession() == null || !this.target.getSession().exit)) {
                            if (this.target.map.equals(this.map) && this.target.region == this.region && this.target.inCountry == this.inCountry) {
                                if (this.target.hp <= 0) {
                                    if (Map.isNewVersion) {
                                        this.target.sendMessage(MessageCreator.createMsgMonsterRemoveTarget(this.id));
                                        this.target.sendToNearPlayer(MessageCreator.createMsgMonsterRemoveTarget(this.id));
                                        this.x = this.default_x;
                                        this.y = this.default_y;
                                    }

                                    this.target = null;
                                    return;
                                }

                                if (this.target == null
                                    || this.target.hp <= 0
                                    || (this.getMonsterTemplate().move != 1 || Math.abs(this.target.x - this.x) > 32 || Math.abs(this.target.y - this.y) > 32)
                                        && (this.getMonsterTemplate().move != 0 || Math.abs(this.target.x - this.x) > 96 || Math.abs(this.target.y - this.y) > 96)) {
                                    this.move();
                                } else {
                                    this.target.isCheckActiveBuffGiamSatThuong();
                                    this.attack();
                                }
                                break label114;
                            }

                            if (Map.isNewVersion) {
                                this.target.sendMessage(MessageCreator.createMsgMonsterRemoveTarget(this.id));
                                this.target.sendToNearPlayer(MessageCreator.createMsgMonsterRemoveTarget(this.id));
                                this.x = this.default_x;
                                this.y = this.default_y;
                            }

                            this.target = null;
                            return;
                        }

                        if (Map.isNewVersion) {
                            this.target.sendMessage(MessageCreator.createMsgMonsterRemoveTarget(this.id));
                            this.target.sendToNearPlayer(MessageCreator.createMsgMonsterRemoveTarget(this.id));
                            this.x = this.default_x;
                            this.y = this.default_y;
                        }

                        this.target = null;
                        return;
                    }
                }

                this.updateEffKham();
            }
        }
    }

    @Override
    public boolean isActive() {
        return this.isActive;
    }

    @Override
    public int getGoldRcv() {
        return this.goldRcv;
    }
}
