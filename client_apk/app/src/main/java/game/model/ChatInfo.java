package game.model;

public class ChatInfo {
    public int ID;
    public String content;
    public String name;

    public ChatInfo() {
    }

    public ChatInfo(String str, String str2, int i) {
        this.name = str;
        this.content = str2.toLowerCase();
    }
}
