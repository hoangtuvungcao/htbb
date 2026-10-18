package game.model;

public class GemTemplate {
    public short id;
    public int price;
    public short rID;
    public int number = 0;
    public boolean lock = false;

    public GemTemplate() {
    }

    public GemTemplate(int i) {
        this.id = (byte) i;
    }

    public static GemTemplate getGemByID(short s) {
        int size = MainChar.gemItem.size();
        for (int i = 0; i < size; i++) {
            GemTemplate gemTemplate = (GemTemplate) MainChar.gemItem.elementAt(i);
            if (gemTemplate.id == s) {
                return gemTemplate;
            }
        }
        return null;
    }

    public static GemTemplate getGemByRealID(short s) {
        int size = MainChar.gemItem.size();
        for (int i = 0; i < size; i++) {
            GemTemplate gemTemplate = (GemTemplate) MainChar.gemItem.elementAt(i);
            if (gemTemplate.rID == s) {
                return gemTemplate;
            }
        }
        return null;
    }

    public static GemTemplate copyData(GemTemplate gemTemplate, GemTemplate gemTemplate2) {
        gemTemplate2.id = gemTemplate.id;
        gemTemplate2.number = gemTemplate.number;
        gemTemplate2.price = gemTemplate.price;
        gemTemplate2.rID = gemTemplate.rID;
        return gemTemplate2;
    }
}
