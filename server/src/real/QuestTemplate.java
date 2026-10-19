/*
 * PHUC HOI TU LOI CU (P3): lop nay duoc dich nguoc tu lib/htbb-core.jar - ma loi da bien dich cua
 * game goc - bang Vineflower 1.11.1 roi sua tay toi thieu cho bien dich duoc (--release 8). Giu
 * nguyen ten lop, package va hanh vi; doi ten / don dep de o giai doan sau. Nguon goc: docs/giay-phep.md.
 */
package real;

public class QuestTemplate {
    public static final byte TYPE_KILL_MONS = 0;
    public static final byte TYPE_TRANSFORM = 1;
    public static final byte TYPE_GET_ITEM = 2;
    public static final byte TYPE_TALK = 3;
    public static final byte TYPE_KILL_MONSTER_LEVEL = 4;
    public static final int MAX_QUEST = 11;
    public static short[] QUEST_ID = new short[]{
        0,
        1,
        2,
        3,
        4,
        5,
        6,
        7,
        8,
        9,
        10,
        11,
        12,
        13,
        14,
        15,
        16,
        17,
        18,
        19,
        20,
        21,
        22,
        23,
        24,
        25,
        26,
        27,
        28,
        29,
        30,
        31,
        32,
        33,
        34,
        35,
        36,
        37,
        38,
        39,
        40,
        41,
        42,
        43,
        44,
        45,
        46,
        47,
        48,
        49,
        50,
        51,
        52,
        53,
        54,
        55,
        56,
        57,
        58,
        59,
        60,
        61,
        62,
        63,
        64,
        65,
        66,
        67,
        68,
        69,
        70,
        71,
        72,
        73,
        74,
        75,
        76,
        77,
        78,
        79
    };
    public static short[] QUEST_TYPE = new short[]{3, 3, 3, 3, 3, 3, 3, 3, 3, 0, 0, 0, 0, 1, 0, 0, 2};
    public static String[] title_quest = new String[]{
        "Nhiệm vụ ra mắt tiền bối",
        "Nhiệm vụ ra mắt tiền bối",
        "Nhiệm vụ ra mắt tiền bối",
        "Nhiệm vụ ra mắt tiền bối",
        "Nhiệm vụ ra mắt tiền bối",
        "Nhiệm vụ ra mắt tiền bối",
        "Nhiệm vụ ra mắt tiền bối",
        "Nhiệm vụ ra mắt tiền bối",
        "Nhiệm vụ ra mắt tiền bối",
        "Nhiệm vụ ra mắt thành hoàng",
        "N.vụ diệt sâu phá hại mùa màng",
        "N.vụ bắt rắn ngâm rượu",
        "N.vụ diệt chuột phá hại mùa màng",
        "N.vụ giao thư cho Hỏa Xích",
        "N.vụ tìm nguyên liệu làm bánh bao",
        "N.Vụ sửa nhà cho trưởng làng",
        "N.Vụ điều chế dược phẩm"
    };
    public static short[] npc_quest_receive = new short[]{5, 2, 3, 1, 9, 6, 22, 21, 5, 1, 6, 6, 6, 3, 1, 5, 1};
    public static short[][] npc_quest_response = new short[][]{{5}, {2}, {3}, {1}, {9}, {6}, {22}, {21}, {5}, {1}, {6}, {6}, {6}, {3}, {1}, {5}, {1}};
    public static String[][][] info_response_quest = new String[][][]{
        {new String[0]},
        {new String[0]},
        {new String[0]},
        {new String[0]},
        {new String[0]},
        {new String[0]},
        {new String[0]},
        {new String[0]},
        {new String[0]},
        {{"1Tốt lắm, ta cảm ơn con !"}},
        {{"1Chà,ngươi làm nhanh thế!", "1Ta nghĩ cũng phải hơn nửa ngày mới xong chứ", "1ngươi làm ta thật bất ngờ !"}},
        {{"1Chà,thật cảm ơn ngươi!", "1Ngươi thật tài giỏi và tốt bụng."}},
        {{"1Nhờ có ngươi mà bọn chuột đã bớt phá hại", "1thu hoạch cũng được khá hơn", "1Thật cảm ơn ngươi"}},
        {{"1Được tin lão Hỏa Xích khỏe mạnh ta thật an lòng", "1Thật vất vả cho ngươi", "1Đây ta tặng ngươi cái áo", "1để cảm ơn ngươi đã giúp ta."}},
        {{"1Con về hơi trễ so với dự kiến của ta", "1nhưng không sao vẫn còn kịp", "1ta có chút quà cho con đây."}},
        {{"1Thật là vất vả cho con", "1Bao giờ nhà sửa xong thì đến ăn tân gia nhà ta nhé."}},
        {{"1Cảm ơn con !", "1Hãy nhận chút quà của ta."}}
    };
    public static short[][] npc_quest = new short[][]{
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
    public static String[][] info_npc_quest = new String[][]{
        new String[0],
        new String[0],
        new String[0],
        new String[0],
        new String[0],
        new String[0],
        new String[0],
        new String[0],
        new String[0],
        new String[0],
        new String[0],
        new String[0],
        new String[0],
        {
                "1Oh,Thiết Bì huynh gửi thư cho ta àh?",
                "1Thật quá bất ngờ !",
                "1Ta cũng định ít bữa nữa thu xếp đến thăm huynh ấy",
                "1chưa kịp đi thì huynh ấy đã cho người đưa thư đến hỏi thăm.",
                "1ta đã xem xong thư để ta hồi âm cho huynh ấy vài chữ, ngươi đợi nhé !",
                "1Hãy giúp ta chuyển lời cảm ơn đến Thiết Bì huynh nhé",
                "1Phiền ngươi nhắn với huynh ấy",
                "1vài bữa nữa ta sẽ đến thăm huynh ấy sau",
                "1Thật cảm ơn ngươi rất nhiều."
        },
        {"15"},
        {"16"},
        {"17"},
        new String[0],
        new String[0],
        new String[0],
        new String[0],
        new String[0],
        new String[0],
        new String[0],
        new String[0],
        new String[0],
        new String[0],
        new String[0],
        new String[0],
        new String[0],
        new String[0],
        new String[0],
        new String[0],
        new String[0],
        new String[0],
        new String[0],
        new String[0],
        new String[0],
        new String[0],
        new String[0],
        new String[0],
        new String[0],
        new String[0],
        new String[0],
        new String[0],
        new String[0],
        new String[0],
        new String[0],
        new String[0],
        new String[0]
    };
    public static short[][] item_receive = new short[][]{{1}};
    public static short[][] item_quest = new short[][]{{1}};
    public static String[] nameItemQuest = new String[]{
        "",
        "",
        "",
        "",
        "",
        "",
        "",
        "",
        "",
        "",
        "",
        "",
        "",
        "Lá thư của Thiết bì",
        "15",
        "16",
        "Nọc bò cạp",
        "",
        "",
        "",
        "",
        "",
        "",
        "",
        "",
        "",
        "",
        "",
        "",
        "",
        "",
        "",
        "",
        "",
        "",
        "",
        "",
        "",
        "",
        "",
        "",
        "",
        "",
        "",
        "",
        "",
        "",
        "",
        "",
        "",
        "",
        "",
        "",
        "",
        "",
        "",
        "",
        "",
        "",
        "",
        "",
        "",
        "",
        "",
        ""
    };
    public static short[][][] monster_item = new short[][][]{
        {new short[0]},
        {{2, 15, 10}},
        {new short[0]},
        {new short[0]},
        {new short[0]},
        {new short[0]},
        {new short[0]},
        {new short[0]},
        {new short[0]},
        {new short[0]},
        {new short[0]},
        {new short[0]},
        {new short[0]},
        {new short[0]},
        {new short[0]},
        {new short[0]},
        {{16, 20, 10}}
    };
    public static final String[] PORTION_ITEM_NAME = new String[]{
        "Lá thư của Thiết bì", "Nọc độc sâu", "Nọc nhện", "Lá cây", "Mai rùa", "Nanh heo", "Da rắn", "Chân rết"
    };
    private static final int[][][] monster_kill = new int[][][]{
        {{1, 10, 0}},
        {{1, 10, 0}},
        {{1, 10, 0}},
        {{1, 10, 0}},
        {{1, 10, 0}},
        {{1, 10, 0}},
        {{1, 10, 0}},
        {{1, 10, 0}},
        {{1, 10, 0}},
        {{1, 20, 0}},
        {{2, 40, 0}},
        {{5, 60, 0}},
        {{9, 60, 0}},
        {new int[0]},
        {{15, 60, 0}},
        {{10, 60, 0}},
        {new int[0]}
    };
    public static String[] decription_quest = new String[]{
        "",
        "Lấy 15 Diêm tinh từ bọn sâu mang về cho Hắc Ngưu",
        "Giao bảng giá cho dì Út HP",
        "",
        "",
        "",
        "",
        "",
        "",
        "Giết 20 con nhím sau đó quay về gặp bà Tám",
        "Giết 40 con sâu mang về cho phú ông",
        "Giết 60 con rắn mang về cho phú ông",
        "Giết 60 con chuột mang về cho phú ông",
        "Hãy mang lá thư của ta cho Hỏa Xích đang sống ở Kỳ bố",
        "Đi đến Thạch Giang và giết 60 con heo mang về cho ta làm nguyên liệu bánh bao",
        "Đi đến Phù Liệt giết 60 quỷ hoa sau đó về báo cho trưởng làng",
        "Đi đến Đông Sơn tìm 20 nọc độc của bò cạp mang về cho Bà Tám."
    };
    public static String[][] content_quest = new String[][]{
        {
                "1Chào con",
                "0Chào Trưởng làng",
                "1con là cháu ông Ba Lực trên vùng Lâm Viên phải không",
                "0Dạ phải ạ",
                "1như đã hứa với ông ấy",
                "1ta sẽ giới thiệu con gia nhập làng nghĩa sỹ Sơn Nam",
                "1Ta luôn hoan nghênh các thanh niên có nhiệt huyết với giang sơn đất nước",
                "1bênh vực kẻ yếu, sống vì lẽ phải",
                "1Con hãy đi chào hỏi các bà con cô bác trong làng",
                "1và làm một số việc giúp họ coi như là chào hỏi",
                "1và biết được nhiều hơn về mọi thứ nhé.",
                "1Đầu tiên con hãy đi gặp anh Hắc ngưu thợ rèn.",
                "1Anh ta là chuyên gia vũ khí của làng này đó.",
                "0Dạ, chào trưởng làng con đi",
                "1Uhm"
        },
        {
                "1Khà khà !!",
                "1Ta đã được nghe qua câu chuyện của ngươi từ trưởng làng",
                "1ngươi còn trẻ mà đã biết ý thức sống vì lẽ phải",
                "1Đó là điều rất đáng quý !",
                "1Ta là Hắc Ngưu",
                "1niềm đam mê của ta là chế tạo và buôn bán vũ khí",
                "1Nếu có thắc mắc điều gì liên quan đến vũ khí ngươi cứ đến tìm ta",
                "1Giờ thì ngươi hãy đến ra mắt lão Thiết Bì đi nào."
        },
        {
                "1Anh hùng xuất thiếu niên, anh hùng xuất thiếu niên !",
                "1Ta là Thiết Bì, người chế tạo giáp duy nhất của làng này",
                "1mọi loại giáp trụ ta đều am hiểu",
                "1Cần gì về giáp thì cứ đến tìm ta nhé !",
                "1Cũng xế trưa rồi.",
                "1giờ này chắc bà Tám tạp hóa cũng đang chờ ngươi đấy",
                "1hãy qua mà chào hỏi bà ấy đi"
        },
        {
                "1Phù ! Ta đã đợi con từ sáng sớm đến giờ",
                "1Nghe chuyện về hoàn cảnh của con mà ta thương quá",
                "1Nhà ta chuyên kinh doanh các loại tạp phẩm có thể phục vụ cho cả làng.",
                "1. Anh Bảy thủ kho đang háo hức được gặp mặt con",
                "1hãy đến nhà anh ta sau khi ăn xong nhé."
        },
        {
                "1Ha ha ! Tiểu đệ đây rồi",
                "1Lạ thay, tuy chưa gặp mặt lần nào",
                "1nhưng huynh cảm thấy như đã quen tiểu đệ từ lâu lắm rồi vậy",
                "1Thật là sảng khoái",
                "1Tính huynh vốn ít nói nên đệ đừng ngại",
                "1Sau này trên đường bôn tẩu giang hồ",
                "1nếu sưu tầm được thứ gì trân quí mà không tiện mang bên mình",
                "1cứ đưa ta bảo quản",
                "1Bảo đảm không ai có thể lấy được của đệ.",
                "1Giờ thì đệ hãy đến ra mắt Phú ông",
                "1cứ tìm nhà nào to và đẹp nhất làng chính là nhà ông ấy."
        },
        {
                "1Rất vui khi được gặp ngươi",
                "1",
                "1Ngươi tuy mồ côi nhưng lại ngoan hiền và có chí lớn",
                "1thật là tốt khi ngươi đứng vào hàng ngũ nghĩa sỹ Sơn Nam",
                "1Nếu cần các loại vật liệu luyện thần binh thì cứ đến tìm ta",
                "1Ta đã chuyển lời giới thiệu ngươi với Nhật thương nhân bạn ta",
                "1hãy đến ra mắt ông ta nhé."
        },
        {
                "1Chào chàng trai trẻ !",
                "1Ta thường đi giao thương khắp nơi",
                "1Ta và ngươi thật là có duyên tương ngộ",
                "1Ha ha ha ",
                "1Khi bôn ba giang hồ",
                "1cần phải có một chút bản lĩnh.",
                "1Hãy đến gặp Lâm tướng quân để học hỏi nha.",
                "1Lâm tướng quân đang chờ ngươi tại doanh trại của ông ấy",
                "1Hãy nhanh chân đến gặp ông ấy đi nào."
        },
        {
                "1Hây dà ! Cuối cùng cũng đã đến !",
                "1Có thực sự ngươi muốn gia nhập làng nghĩa sỹ Sơn Nam không?",
                "1Nơi đây không có chổ dung thân cho kẻ lười biếng và vô dụng nhé",
                "1Hãy cố gắng luyện tập nhiều",
                "1ta thấy tiền đồ ngươi có vẻ sáng lạn đấy",
                "1Ta luôn có những nhiệm vụ đang cần người giải quyết",
                "1khi nào thực sự tự tin để làm hãy đến tìm ta.",
                "1Nhân đây ta sẽ dạy ngươi 1 chiêu thức",
                "1Nó sẽ giúp ngươi khi bắt đầu cuộc hành tẩu.",
                "1Bây giờ ngươi hãy quay lại gặp trưởng làng",
                "1Ông ấy có 1 vài điều muốn nói với ngươi trước khi ngươi lên đường."
        },
        {
                "1Chúc mừng con",
                "1Từ giờ con đã là thành viên của làng nghĩa sỹ Sơn Nam",
                "1Hãy cố gắng đừng phụ lòng mọi người kỳ vọng vào con",
                "0Dạ vâng. Con sẽ cố gắng."
        },
        {
                "1Con biết không,Sơn Nam là một ngôi làng đã có lịch sử lâu đời.>1Nơi đây không chỉ là nơi ta sinh sống>1mà còn là nơi lưu giữ ý chí kiêu hùng và tôn chỉ của những người đầu tiên đi mở cõi và giữ nước>1Nơi đây cũng là quê hương thứ 2 của bao bậc anh hùng hào kiệt nhân sỹ tứ phương>1ai trong họ cũng tự hào là thành viên nơi đây và tự hào là con cháu của xứ sở Rồng Tiên>1Họ là tấm gương và là nguồn động viên cho các lớp người đi sau>1con hãy cố gắng để nối tiếp truyền thống làng ta nhé>1Để ta làm bữa cơm cho con cúng ra mắt thành hoàng làng>1con hãy kiếm 20 con nhím về đây cho ta nhé."
        },
        {
                "1Nhà ta có mấy thửa ruộng trước cổng làng đang bị dịch sâu hại lúa",
                "1ngươi hãy giúp ta diệt sâu",
                "1để chúng không phá lúa nhà ta nữa nhé",
                "1Chừng nào xong thì quay về đây báo cho ta hay nhé"
        },
        {
                "1Ah, ta muốn ngâm một hủ rượu rắn để tiếp đãi các anh hùng nghĩa sĩ khi có dịp",
                "1Ta nghe nói vùng Tiên Du lân cận làng ta",
                "1cách đây chừng 5 dặm đường có rất nhiều rắn lục",
                "1ngươi hãy đến đó và bắt dùm ta 60 con rắn",
                "1Cẩn thận đừng để chúng cắn, chết chứ chả chơi !"
        },
        {
                "1Haiiiiii !",
                "1Mùa màng năm nay của làng ta ở ruộng xa Phù Liệt thất bát quá",
                "1Do bọn chuột từ đâu kéo về sinh sôi nảy nở quá nhiều",
                "1ngươi hãy giúp dân làng diệt lũ chuột để chúng khỏi phá hại cây lúa."
        },
        {
                "1Ta có một người bạn tâm giao cùng học nghệ năm xưa sống tại vùng Kỳ Bố",
                "1lão tên là Hỏa Xích.",
                "1Ta và lão tình như thủ túc",
                "1lâu ngày không gặp sinh lòng nhớ thương bạn hiền",
                "1ta muốn gửi thư hỏi thăm xem lão độ này sinh sống thế nào",
                "1Ngươi hãy giúp ta mang phong thư này trao tận tay lão",
                "1Đi nhanh rồi về, ta rất mong tin lão."
        },
        {
                "1cả làng rất thích ăn bánh bao do ta làm nên hôm nay ta muốn đãi họ một bữa",
                "1Sáng này ta ra chợ mua hết thịt heo ngoài đó về để làm nhân bánh nhưng vẫn chưa đủ",
                "1con hãy tìm về đây cho ta 60 con heo nhé",
                "1Ta nghe nói dạo này vùng Thạch Giang xuất hiện rất nhiều heo rừng",
                "1con hãy đến đấy thử xem.",
                "1Đi nhanh rồi về cho kịp !"
        },
        {
                "1Nhà của ta đã lâu lắm không tu sửa gì nên nó đã quá cũ kĩ",
                "1đã có nhiều chổ bị mối mọt ăn mục",
                "1Con hãy giúp ta  sửa lại căn nhà cho kịp mùa mưa đang đến",
                "1vùng phù liệt nổi tiếng nhiều gỗ tốt",
                "1con hãy đến đấy tìm gỗ giúp ta nhé",
                "1Rừng thiêng nước độc",
                "1trên núi có rất nhiều hoa ăn thịt và quái thú con phải hết sức cẩn thận"
        },
        {"1Ta đang cần nọc độc của bò cạp để điều chế một số dược phẩm", "1con hãy đến đông sơn tìm 40 nọc bò cạp mang về cho ta nhé."}
    };
    public static short[][] gold_xp_gift = new short[][]{
        {100, 100},
        {100, 100},
        {100, 100},
        {100, 100},
        {100, 100},
        {100, 100},
        {100, 100},
        {100, 100},
        {0, 100},
        {200, 200},
        {200, 300},
        {300, 300},
        {500, 800},
        {500, 800},
        {800, 1200},
        {1000, 1500},
        {2000, 2000}
    };
    public static short[][] potionGift = new short[][]{
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
        new short[0],
        new short[0],
        new short[0],
        new short[0],
        new short[0],
        {0, 0, 5, 0, 0, 0, 5}
    };
    public static String xu = "xu";
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

    public static int[][] getMonsterKill(int idQuest) {
        return monster_kill[idQuest];
    }

    public static boolean isQuestOfNPC(int idQuest, int idNpc) {
        return npc_quest_receive[idQuest] == idNpc;
    }

    public static short[] getItemReceive(int idQuest) {
        return item_receive[idQuest];
    }

    public static short[] getPotionReceive(int idQuest) {
        return potionGift[idQuest];
    }

    public static short[] getItemQuest(int idQuest) {
        return item_quest[idQuest];
    }

    public static String getContent(int idQuest, int index) {
        try {
            return content_quest[idQuest][index];
        } catch (Exception var3) {
            return "";
        }
    }

    public static int getTypeQuest(int idQuest) {
        return idQuest - 1 >= npc_quest_receive.length ? -1 : QUEST_TYPE[idQuest - 1];
    }

    public static String getDecription(int idQuest) {
        return decription_quest[idQuest];
    }
}
