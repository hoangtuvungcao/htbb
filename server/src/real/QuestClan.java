/*
 * PHUC HOI TU LOI CU (P3): lop nay duoc dich nguoc tu lib/htbb-core.jar - ma loi da bien dich cua
 * game goc - bang Vineflower 1.11.1 roi sua tay toi thieu cho bien dich duoc (--release 8). Giu
 * nguyen ten lop, package va hanh vi; doi ten / don dep de o giai doan sau. Nguon goc: docs/giay-phep.md.
 */
package real;

public class QuestClan {
    public static short[][] idQuest = new short[][]{{0, 1, 2, 3, 4, 5, 6, 7, 8, 9}, {1, 1, 1, 0, 0, 0, 0, 1, 1, 0}};
    public static String[] shortDecript = new String[]{
        "Tìm 100 móng cá sấu tại khu vực Trường Giang. Phần thưởng: 5xp bang hội",
        "Thu thập 100 bộ da ếch tại khu vực Trường giang. Phần thưởng: 5xp bang hội",
        "Lấy 100 cựa gà ở vùng lộc trĩ. Phần thưởng: 5xp bang hội",
        "Giết 100 con Dế khổng lồ ở vùng Sơn lâm. Phần thưởng: 5xp bang hội",
        "Giết 100 con trâu núi ở vùng Sơn Lâm. Phần thưởng: 5xp bang hội",
        "Tiêu diệt sơn tặc ở vùng Sơn Lâm. Phần thưởng: 5xp bang hội",
        "Tiêu diệt hải tặc ở vùng Sơn Lâm. Phần thưởng: 5xp bang hội",
        "Bắt 100 con rết đỏ ở Phong Châu. Phần thưởng: 10xp bang hội",
        "Tìm 100 lông đuôi Hồng kê ở Phong Châu. Phần thưởng: 10xp bang hội",
        "Bắt 100 con hổ đỏ ở Bình lục. Phần thưởng: 10xp bang hội"
    };
    public static String[][] info_receive = new String[][]{
        {"Ta muốn làm 1 sợi dây chuyền từ móng cá sấu, nhưng hiện tại ta đang bận việc. Vậy ngươi hãy đi tìm cho ta 100 móng cá sâu để ta làm dây chuyền"},
        {"Ta đang cần tìm 100 da ếch quỷ đề làm thuốc. Ngươi có thể giúp ta không?"},
        {
                "Nghe nói cựa Gà khổng lồ ở vùng đảo Lộc Trĩ nhọn như kim châm, sắc như dao cạo rất phù hợp để chế tạo vũ khí. Ngươi có thể đi tìm về cho ta không? Ta cần khoảng 100 cái cựa."
        },
        {
                "Thịt Dế khổng lồ ở vùng Sơn Lâm là một món ngon hiếm có, sắp tới có tiệc chiêu đãi anh hùng các bang hội ngươi có thể mang 100 con Dế khổng lồ về đây cho ta không?"
        },
        {
                "Ta nghe nói một con Trâu núi khoẻ gấp 3 lần con trâu nhà, giá mà mang được loài này về thuần hoá phục vụ cho việc cầy bừa thì thật là tốt. Ngươi có thể thuần hoá 100 con Trâu núi cho ta không ?"
        },
        {
                "Bọn Sơn tặc dạo này rất hoành hành, chúng thường chặn đường cướp bóc người qua lại trên các con đường trọng yêu giữa các vùng. Đây đang là một vấn nạn cho mọi người, buộc phải dẹp yên. Theo nguồn tin ta có được sào huyệt của bọn chúng tại phía bắc Sơn Lâm, chúng có khoảng 100 tên. Ngươi hãy tiêu diệt hết bọn chúng cho ta"
        },
        {
                "Vùng biển Lộc Trĩ rất hay xuất hiện Hải Tặc, chúng thường tấn công vào các tàu đánh cá của ngư dân cướp bóc không thương tiếc.Hãy diệt hết bọn chúng mang bình yên lại cho ngư dân lương thiện nhé. Ít nhất phải mang về đây cho ta 100 tên đấy"
        },
        {
                "Loài Rết đỏ kết hợp với một số thảo dược sẽ mang lại hiệu quả thần kỳ cho việc chữa trị nội thương. Ngươi hãy giúp ta tìm 100 con Rết đỏ để ngâm rượu thuốc nhé, liệu ngươi có giúp ta không ?"
        },
        {
                "Các loại áo giáp trân quý ngoài bảo thạch thì đều cần lông đuôi Hồng kê để điểm xuyết nhằm tăng thêm giá trị. Ngươi hãy giúp ta mang về đây 100 lông đuôi Hồng kê, tang đang rất cần. Cẩn thận kẻo bỏ mạng, loài này rất hung dữ chúng thường xuất hiện ở vùng Phong Châu"
        },
        {
                "Khi thời tiết thay đổi ta thường có những cơn đau khắp người, thật sự ta đã già rồi. Tương truyền cao Hổ đỏ có tác dụng giảm đau rất hữu hiệu. Người hãy đi bắt 100 con Hổ đỏ để ta chế biến cao hổ nhé"
        }
    };
    public static String[] title = new String[]{
        "Móng cá sấu",
        "Tìm da ếch",
        "Tìm cựa gà",
        "Bắt dế khổng lồ",
        "Thuần hoá trâu núi",
        "Trừng trị sơn tặc",
        "Trừng trị hải tặc",
        "Tìm rết đỏ",
        "Tìm hồng kê",
        "Tìm hổ đỏ nấu cao"
    };
    public static String[] nameItem = new String[]{
        "móng cá sấu", "da ếch", "cựa gà", "item 4", "item 5", "item 6", "item 7", "rết đỏ", "lông hồng kê", "item 10"
    };
    public static String[] nameItem_resp = new String[]{"Quà 1", "Quà 1", "Quà 1", "Quà 1", "Quà 1", "Quà 1", "Quà 1", "Quà 1", "Quà 1", "Quà 1"};
    public static short[][] npc_quest = new short[][]{
        new short[0],
        {1},
        new short[1],
        new short[0],
        new short[0],
        new short[1],
        new short[0],
        new short[0],
        new short[1],
        new short[0],
        new short[0],
        new short[1],
        new short[0],
        {23},
        new short[1],
        new short[1],
        new short[1],
        new short[1],
        new short[0],
        new short[0],
        new short[0],
        new short[0],
        new short[0],
        new short[0],
        new short[0],
        new short[0],
        new short[0],
        new short[0],
        new short[0],
        new short[0],
        new short[0],
        new short[0],
        new short[0]
    };
    public static short[][][] qItem = new short[][][]{
        {{29, 100, 7}}, {{30, 100, 7}}, {{22, 100, 7}}, {{2, 15, 7}}, {{2, 15, 7}}, {{2, 15, 7}}, {{2, 15, 7}}, {{40, 100, 7}}, {{45, 100, 7}}, {{2, 15, 7}}
    };
    public static short[][][] monsterKill = new short[][][]{
        {{1, 10, 0}},
        {{32, 150, 0}},
        {{35, 1, 0}},
        {{31, 100, 0}},
        {{33, 100, 0}},
        {{34, 100, 0}},
        {{35, 100, 0}},
        {{1, 70, 0}},
        {{1, 80, 0}},
        {{63, 100, 0}},
        {{1, 100, 0}}
    };
    public static short[][] itemGift = new short[][]{
        new short[0], new short[0], new short[0], new short[0], new short[0], new short[0], new short[0], new short[0], new short[0]
    };
    public static short[][] potionGif = new short[][]{
        new short[0], new short[0], new short[0], new short[0], new short[0], new short[0], new short[0], new short[0], new short[0], new short[0]
    };
    public static int[][] xpReceive = new int[][]{{5, 10000}, {5, 15000}, {5, 20000}, {5, 100}, {5, 100}, {5, 100}, {5, 100}, {5, 100}, {5, 100}, {5, 100}};
    public static String[][] info_respone = new String[][]{
        {"1ABC", "1DEF", "1GHJ"},
        {"1ABC", "1DEF", "1GHJ"},
        {"1ABC", "1DEF", "1GHJ"},
        {"1ABC", "1DEF", "1GHJ"},
        {"1ABC", "1DEF", "1GHJ"},
        {"1ABC", "1DEF", "1GHJ"},
        {"1ABC", "1DEF", "1GHJ"},
        {"1ABC", "1DEF", "1GHJ"},
        {"1ABC", "1DEF", "1GHJ"},
        {"1ABC", "1DEF", "1GHJ"}
    };
    public static String[] info_Gif = new String[]{
        "Nhận được 100 xu và 100xp",
        "Nhận được 100 xu và 100xp",
        "Nhận được 100 xu và 100xp",
        "Nhận được 100 xu và 100xp",
        "Nhận được 100 xu và 100xp",
        "Nhận được 100 xu và 100xp",
        "Nhận được 100 xu và 100xp",
        "Đã học kỹ năng ",
        "",
        "Nhận được 200 xu và 200xp",
        "Nhận được 200 xu và 300xp",
        "Nhận được 300 xu và 300xp",
        "Nhận được 300 xu và 800xp",
        "Nhận được 300 xu và 800xp",
        "Nhận được 800 xu và 1200xp",
        "Nhận được 1000 xu và 1500xp",
        "Nhận được 2000 xu ,2000xp, 5HP vừa và 5MP vừa"
    };
}
