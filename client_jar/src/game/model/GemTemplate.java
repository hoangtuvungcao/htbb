package game.model;

public class GemTemplate {
    public short id;
    public short rID;
    public int price;
    public int number = 0;
    public boolean lock = false;

    public GemTemplate() {
    }

    public GemTemplate(int id) {
        this.id = (byte) id;
    }

    public static GemTemplate getGemByID(short id) {
        for (int i = 0; i < MainChar.gemItem.size(); i++) {
            GemTemplate gem = (GemTemplate) MainChar.gemItem.elementAt(i);
            if (gem.id == id) {
                return gem;
            }
        }
        return null;
    }

    public static GemTemplate getGemByRealID(short rID) {
        for (int i = 0; i < MainChar.gemItem.size(); i++) {
            GemTemplate gem = (GemTemplate) MainChar.gemItem.elementAt(i);
            if (gem.rID == rID) {
                return gem;
            }
        }
        return null;
    }

    public static GemTemplate copyData(GemTemplate from, GemTemplate to) {
        to.id = from.id;
        to.number = from.number;
        to.price = from.price;
        to.rID = from.rID;
        return to;
    }
}
