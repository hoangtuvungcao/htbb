/*
 * PHUC HOI TU LOI CU (P3): lop nay duoc dich nguoc tu lib/htbb-core.jar - ma loi da bien dich cua
 * game goc - bang Vineflower 1.11.1 roi sua tay toi thieu cho bien dich duoc (--release 8). Giu
 * nguyen ten lop, package va hanh vi; doi ten / don dep de o giai doan sau. Nguon goc: docs/giay-phep.md.
 */
package real;

import data.ItemLuckyDraw;
import java.util.Vector;

public class ItemShop extends Item {
    public int luong = 0;
    public int xu = 0;
    public int soluong = 1;
    public int timeExpire = 0;

    public ItemShop() {
    }

    public ItemShop(ItemTemplates template, boolean isAddRandomValue, int clazz, int classTemplate, int idtemplate) {
        super(template, isAddRandomValue, clazz, classTemplate, idtemplate);
    }

    @Override
    public String getName() {
        return super.getName();
    }

    @Override
    public String getCharSeal() {
        if (this.minuteExist > 0 && this.timeLoan > 0L) {
            long time = this.minuteExist;
            String[] info = Char.split(Char.getDayTimeByTime(this.timeLoan + time * 60000L), " ");
            return !this.charSeal.equals("") ? this.charSeal + "\nhh: " + info[0] + "\n-" + info[1] : "hh: " + info[0] + "\n-" + info[1];
        } else {
            String info = this.charSeal;
            if (this.isRuongNguyenLieu()) {
                info = info + "- Mở ra ngẫu nhiên nguyên liệu cấp 4,5.";
            }

            if (this.xu > 0) {
                info = info + "\n2- Giá xu: " + UtilKPAH.getDotNumber(this.xu);
            }

            return info;
        }
    }

    @Override
    public void setTimeExpire(int timeExpire) {
        this.timeExpire = timeExpire;
    }

    @Override
    public int getTimeExpire() {
        return this.timeExpire;
    }

    @Override
    public Vector<InfoItemAttribute> getInfoAtbItem() {
        Vector<InfoItemAttribute> allAtb = new Vector<>();
        if (Map.isModelClothes(this.getTemplate().atb[9])) {
            if (this.atb[0] > 0) {
                allAtb.add(new InfoItemAttribute((short)58, this.atb[0]));
            }

            if (this.atb[1] > 0) {
                allAtb.add(new InfoItemAttribute((short)59, this.atb[1]));
                allAtb.add(new InfoItemAttribute((short)60, this.atb[1]));
            }

            if (this.atb[2] > 0) {
                allAtb.add(new InfoItemAttribute((short)33, this.atb[2] / 1000));
            }

            if (this.atb[3] > 0) {
                allAtb.add(new InfoItemAttribute((short)34, this.atb[3] / 1000));
            }

            if (this.tempateID == 577 || this.tempateID == 578) {
                allAtb.add(new InfoItemAttribute((short)69, 500));
                allAtb.add(new InfoItemAttribute((short)70, 500));
                allAtb.add(new InfoItemAttribute((short)71, 500));
                allAtb.add(new InfoItemAttribute((short)90, 500));
            }

            if (this.tempateID == 725 || this.tempateID == 726) {
                allAtb.add(new InfoItemAttribute((short)118, 500));
            }

            if (this.tempateID == 743 || this.tempateID == 744) {
                allAtb.add(new InfoItemAttribute((short)119, 100));
            }

            if (ItemLuckyDraw.isChoi(this.tempateID) || this.isMatna()) {
                for (short i = 0; i < this.atb.length; i++) {
                    if (i < 10) {
                        if (attribShowedForType[this.getType()][i] && this.atb[i] > 0) {
                            allAtb.add(new InfoItemAttribute(i, this.atb[i]));
                            this.allAtb.put(i, new InfoItemAttribute(i, this.atb[i]));
                        }
                    } else if (this.atb[i] > 0) {
                        allAtb.add(new InfoItemAttribute(i, this.atb[i]));
                    }
                }

                int count = this.atb.length;

                for (short ix = 0; ix < this.newAtb.length; ix++) {
                    if (this.newAtb[ix] > 0) {
                        allAtb.add(new InfoItemAttribute((short)(ix + count), this.newAtb[ix]));
                    }
                }

                count += this.newAtb.length;

                for (short ixx = 0; ixx < this.addMoreLevelSkill.length; ixx++) {
                    if (this.addMoreLevelSkill[ixx] > 0) {
                        allAtb.add(new InfoItemAttribute((short)(ixx + count), this.addMoreLevelSkill[ixx]));
                    }
                }

                count += this.addMoreLevelSkill.length;

                for (short ixxx = 0; ixxx < this.lockAtb.length; ixxx++) {
                    if (this.lockAtb[ixxx] > 0) {
                        allAtb.add(new InfoItemAttribute((short)(ixxx + count), this.lockAtb[ixxx]));
                    }
                }

                count += this.lockAtb.length;

                for (short ixxxx = 0; ixxxx < this.otherAtt.length; ixxxx++) {
                    if (this.otherAtt[ixxxx] > 0) {
                        allAtb.add(new InfoItemAttribute((short)(ixxxx + count), this.otherAtt[ixxxx]));
                    }
                }
            }
        } else {
            for (short ixxxxx = 0; ixxxxx < this.atb.length; ixxxxx++) {
                if (ixxxxx < 10) {
                    if (attribShowedForType[this.getType()][ixxxxx] && this.atb[ixxxxx] > 0) {
                        allAtb.add(new InfoItemAttribute(ixxxxx, this.atb[ixxxxx]));
                        this.allAtb.put(ixxxxx, new InfoItemAttribute(ixxxxx, this.atb[ixxxxx]));
                    }
                } else if (this.atb[ixxxxx] > 0) {
                    allAtb.add(new InfoItemAttribute(ixxxxx, this.atb[ixxxxx]));
                }
            }

            int count = this.atb.length;

            for (short ixxxxxx = 0; ixxxxxx < this.newAtb.length; ixxxxxx++) {
                if (this.newAtb[ixxxxxx] > 0) {
                    allAtb.add(new InfoItemAttribute((short)(ixxxxxx + count), this.newAtb[ixxxxxx]));
                }
            }

            count += this.newAtb.length;

            for (short ixxxxxxx = 0; ixxxxxxx < this.addMoreLevelSkill.length; ixxxxxxx++) {
                if (this.addMoreLevelSkill[ixxxxxxx] > 0) {
                    allAtb.add(new InfoItemAttribute((short)(ixxxxxxx + count), this.addMoreLevelSkill[ixxxxxxx]));
                }
            }

            count += this.addMoreLevelSkill.length;

            for (short ixxxxxxxx = 0; ixxxxxxxx < this.lockAtb.length; ixxxxxxxx++) {
                if (this.lockAtb[ixxxxxxxx] > 0) {
                    allAtb.add(new InfoItemAttribute((short)(ixxxxxxxx + count), this.lockAtb[ixxxxxxxx]));
                }
            }

            count += this.lockAtb.length;

            for (short ixxxxxxxxx = 0; ixxxxxxxxx < this.otherAtt.length; ixxxxxxxxx++) {
                if (this.otherAtt[ixxxxxxxxx] > 0) {
                    allAtb.add(new InfoItemAttribute((short)(ixxxxxxxxx + count), this.otherAtt[ixxxxxxxxx]));
                }
            }
        }

        allAtb.add(new InfoItemAttribute((short)126, this.soluong));
        if (this.luong > 0) {
            allAtb.add(new InfoItemAttribute((short)125, this.luong));
        }

        return allAtb;
    }

    @Override
    public void setSoluong(int sl) {
        this.soluong = sl;
    }

    @Override
    public void setXuSell(int xu) {
        this.xu = xu;
    }

    @Override
    public void setLuongSell(int l) {
        this.luong = l;
    }

    @Override
    public int getSoluong() {
        return this.soluong;
    }

    @Override
    public int getXuSell() {
        return this.xu;
    }

    @Override
    public int getLuongSell() {
        return this.luong;
    }
}
