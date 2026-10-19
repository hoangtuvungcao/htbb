/*
 * PHUC HOI TU LOI CU (P3): lop nay duoc dich nguoc tu lib/htbb-core.jar - ma loi da bien dich cua
 * game goc - bang Vineflower 1.11.1 roi sua tay toi thieu cho bien dich duoc (--release 8). Giu
 * nguyen ten lop, package va hanh vi; doi ten / don dep de o giai doan sau. Nguon goc: docs/giay-phep.md.
 */
package data;

public class PetTemplate {
    public String name = "";
    public int timeExpire;
    public byte idImg = 0;
    public byte nFrame;
    public byte wFrame;
    public byte hFrame;
    public byte idImgPaint;
    public byte idTemplate = 0;
    public byte typeMoney = 0;
    public byte type_move = 0;
    public int value = 0;

    public PetTemplate(
        byte idTemplate, String name, int timeExpire, byte idImg, byte nFrame, byte wFrame, byte hFrame, int value, byte typeMoney, byte idImgPaint, int typemove
    ) {
        this.idTemplate = idTemplate;
        this.name = name;
        this.timeExpire = timeExpire;
        this.idImg = idImg;
        this.value = value;
        this.typeMoney = typeMoney;
        this.nFrame = nFrame;
        this.wFrame = wFrame;
        this.hFrame = hFrame;
        this.idImgPaint = idImgPaint;
        this.type_move = (byte)typemove;
    }
}
