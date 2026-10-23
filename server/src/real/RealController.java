/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package real;

import Constant.Logger2;
import data.Animal;
import data.CreateItemTemplate;
import data.DataGame;
import data.Database;
import data.InfoItemCreate;
import data.ItemChangeColor;
import data.ItemLuckyDraw;
import data.OtherHandle;
import data.UserLogger;
import io.Message;
import io.Session;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Calendar;
import java.util.Collection;
import java.util.Hashtable;
import java.util.Vector;
import static real.Map.ALL_CHAR_VIP;
import static real.Map.ID_GEM_TU_BINH;
import static real.Map.ID_THE_MUA_BAN;
import static real.Map.MAX_VIP1;
import static real.Map.MAX_VIP2;
import static real.Map.VIP;
import static real.Map.addXpCharEvent;
import static real.Map.gemTemplate;
import static real.Map.idMapSend2Client;
import static real.Map.itemTemplates;
import static real.Map.okChedo;
import static real.Map.r;
import static real.Map.sendAllCharServer;
import real.cmd.CreateCharHandler;
import real.cmd.ICommandHandler;
import real.cmd.LoginHandler;
import real.cmd.PingHandler;
import real.cmd.QuestHandler;
import real.cmd.RequestCharInfoHandler;
import real.cmd.RequestNPCInfoHandler;
import real.cmd.RequestTemplateInfo;
import real.cmd.SelectCharHandler;
import server.ServerController;
import server.TeamServer;
import util.Logger;

/**
 *
 *
 */
public class RealController extends ServerController {

    public IDGen idGen;
    public static Hashtable<Integer, Map> mapList = new Hashtable();
    private static Hashtable<Integer, ICommandHandler> commandHandler = new Hashtable();
    public boolean running = true;
    public static boolean savingChar;
    public static RealController intance;
    public static String maquee = "";
    public static String maquee2 = "";
    public static String[] dayAlertMarquest2 = new String[]{""};
    public static OtherHandle otherHandle = new OtherHandle();
    public static XaphuTemplate[] xaphu = new XaphuTemplate[]{new XaphuTemplate(new short[]{3, 4, 5}, new short[]{5, 27, 58}, new short[]{20, 15, 12}, new short[]{500, 500, 750}), new XaphuTemplate(new short[]{6, 7}, new short[]{37, 9}, new short[]{9, 86}, new short[]{500, 750}), new XaphuTemplate(new short[]{5, 6}, new short[]{58, 37}, new short[]{12, 9}, new short[]{750, 750}), new XaphuTemplate(new short[]{4, 7, 8}, new short[]{27, 9, 25}, new short[]{15, 86, 73}, new short[]{500, 500, 750}), new XaphuTemplate(new short[]{3, 4, 8, 9}, new short[]{5, 27, 25, 17}, new short[]{20, 15, 73, 91}, new short[]{500, 750, 500, 750}), new XaphuTemplate(new short[]{5, 9}, new short[]{58, 17}, new short[]{12, 91}, new short[]{500, 500}), new XaphuTemplate(new short[]{5, 6}, new short[]{58, 37}, new short[]{12, 9}, new short[]{750, 500}), new XaphuTemplate(new short[]{7, 6}, new short[]{9, 37}, new short[]{86, 9}, new short[]{500, 750}), new XaphuTemplate(new short[1], new short[]{24}, new short[]{39}, new short[]{1500})};
    public static Vector<Map> all_map_train = new Vector();
    public static Hashtable<Byte, Byte> privateCmd = new Hashtable();
    public static boolean resetTime;

    static {
        try {
            byte[] pCmd = new byte[]{-45, -37, -46, -33, -28, -27, -24, -71, -70, -23, -22, -21, -19, -18, -16, -17, -12, -7, -8, -6, 110, -5, 109, 108, 104, 102, 101, 72, 100, 88, 86, 82, -2, -3, 7, 21, 31, 27, 61, 51, 52, 53, 55, 67, -69, -68, -67, -66, 87, -63, -62, -61, -59, -54, -57, -53, -52, -41, -36, -35, -34, -30, -31, -32, -15, -14, -13, -11, -9, 107, 106, 103, 92, 85, 81, 78, 77, 75, 76, 74, 71, 70, 68, 9, 6, 18, 19, -65, 24, 28, 29, 34, 36, 49, 50, 22, -113};

            for (int i = 0; i < pCmd.length; ++i) {
                privateCmd.put(pCmd[i], pCmd[i]);
            }
        } catch (Exception var2) {
        }

        resetTime = false;
    }

    public void initMapHTBB() {
        Map m = null;
        mapList.put(new Integer(-1), new OfflineMap(-1));
        mapList.put(new Integer(0), new Map(0, 0, 1, 0, 0));
        mapList.put(new Integer(301), new Map(301, 0, 1, 0, 0));
        mapList.put(new Integer(70), new Map(70, 0, 1, 70, 0));
        mapList.put(new Integer(1701), new Map(1701, 0, 1, 70, 0));
        mapList.put(new Integer(80), new Map(80, 0, 1, 80, 0));
        mapList.put(new Integer(1901), new Map(1901, 0, 1, 80, 0));
        mapList.put(new Integer(1), m = new Map(1, -1, 1, 1, 0));
        all_map_train.add(m);
        mapList.put(new Integer(321), m = new Map(321, -1, 1, 1, 0));
        all_map_train.add(m);
        mapList.put(new Integer(322), m = new Map(322, -1, 1, 1, 0));
        all_map_train.add(m);
        mapList.put(new Integer(323), m = new Map(323, -1, 0, 1, 0));
        all_map_train.add(m);
        mapList.put(new Integer(324), m = new Map(324, -1, 0, 1, 0));
        all_map_train.add(m);
        mapList.put(new Integer(2), m = new Map(2, -1, 1, 2, 0));
        all_map_train.add(m);
        mapList.put(new Integer(341), m = new Map(341, -1, 1, 2, 0));
        all_map_train.add(m);
        mapList.put(new Integer(342), m = new Map(342, -1, 1, 2, 0));
        all_map_train.add(m);
        mapList.put(new Integer(343), m = new Map(343, -1, 0, 2, 0));
        all_map_train.add(m);
        mapList.put(new Integer(344), m = new Map(344, -1, 0, 2, 0));
        all_map_train.add(m);
        mapList.put(new Integer(3), m = new Map(3, 1, 1, 3, 0));
        all_map_train.add(m);
        mapList.put(new Integer(361), m = new Map(361, 1, 1, 3, 0));
        all_map_train.add(m);
        mapList.put(new Integer(362), m = new Map(362, 1, 1, 3, 0));
        all_map_train.add(m);
        mapList.put(new Integer(363), m = new Map(363, 1, 0, 3, 0));
        all_map_train.add(m);
        mapList.put(new Integer(364), m = new Map(364, 1, 0, 3, 0));
        all_map_train.add(m);
        mapList.put(new Integer(4), m = new Map(4, 2, 1, 4, 0));
        all_map_train.add(m);
        mapList.put(new Integer(381), m = new Map(381, 2, 1, 4, 0));
        all_map_train.add(m);
        mapList.put(new Integer(382), m = new Map(382, 2, 1, 4, 0));
        all_map_train.add(m);
        mapList.put(new Integer(383), m = new Map(383, 2, 0, 4, 0));
        all_map_train.add(m);
        mapList.put(new Integer(384), m = new Map(384, 2, 0, 4, 0));
        all_map_train.add(m);
        mapList.put(new Integer(5), m = new Map(5, 3, 1, 5, 0));
        all_map_train.add(m);
        mapList.put(new Integer(401), m = new Map(401, 3, 1, 5, 0));
        all_map_train.add(m);
        mapList.put(new Integer(402), m = new Map(402, 3, 1, 5, 0));
        all_map_train.add(m);
        mapList.put(new Integer(403), m = new Map(403, 3, 0, 5, 0));
        all_map_train.add(m);
        mapList.put(new Integer(404), m = new Map(404, 3, 0, 5, 0));
        all_map_train.add(m);
        mapList.put(new Integer(6), m = new Map(6, 4, 1, 6, 0));
        all_map_train.add(m);
        mapList.put(new Integer(421), m = new Map(421, 4, 1, 6, 0));
        all_map_train.add(m);
        mapList.put(new Integer(422), m = new Map(422, 4, 1, 6, 0));
        all_map_train.add(m);
        mapList.put(new Integer(423), m = new Map(423, 4, 0, 6, 0));
        all_map_train.add(m);
        mapList.put(new Integer(424), m = new Map(424, 4, 0, 6, 0));
        all_map_train.add(m);
        mapList.put(new Integer(7), m = new Map(7, 5, 1, 7, 0));
        all_map_train.add(m);
        mapList.put(new Integer(441), m = new Map(441, 5, 1, 7, 0));
        all_map_train.add(m);
        mapList.put(new Integer(442), m = new Map(442, 5, 1, 7, 0));
        all_map_train.add(m);
        mapList.put(new Integer(443), m = new Map(443, 5, 0, 7, 0));
        all_map_train.add(m);
        mapList.put(new Integer(444), m = new Map(444, 5, 0, 7, 0));
        all_map_train.add(m);
        mapList.put(new Integer(8), m = new Map(8, 6, 1, 8, 0));
        all_map_train.add(m);
        mapList.put(new Integer(461), m = new Map(461, 6, 1, 8, 0));
        all_map_train.add(m);
        mapList.put(new Integer(462), m = new Map(462, 6, 1, 8, 0));
        all_map_train.add(m);
        mapList.put(new Integer(463), m = new Map(463, 6, 0, 8, 0));
        all_map_train.add(m);
        mapList.put(new Integer(464), new Map(464, 6, 0, 8, 0));
        mapList.put(new Integer(9), m = new Map(9, 7, 1, 9, 0));
        all_map_train.add(m);
        mapList.put(new Integer(481), m = new Map(481, 7, 1, 9, 0));
        all_map_train.add(m);
        mapList.put(new Integer(482), m = new Map(482, 7, 1, 9, 0));
        all_map_train.add(m);
        mapList.put(new Integer(483), m = new Map(483, 7, 0, 9, 0));
        all_map_train.add(m);
        mapList.put(new Integer(484), m = new Map(484, 7, 0, 9, 0));
        all_map_train.add(m);
        mapList.put(new Integer(10), m = new Map(10, 8, 1, 10, 0));
        all_map_train.add(m);
        mapList.put(new Integer(501), m = new Map(501, 8, 1, 10, 0));
        all_map_train.add(m);
        mapList.put(new Integer(502), m = new Map(502, 8, 1, 10, 0));
        all_map_train.add(m);
        mapList.put(new Integer(503), m = new Map(503, 8, 0, 10, 0));
        all_map_train.add(m);
        mapList.put(new Integer(504), m = new Map(504, 8, 0, 10, 0));
        all_map_train.add(m);
        mapList.put(new Integer(11), m = new Map(11, -1, 1, 11, 0));
        all_map_train.add(m);
        mapList.put(new Integer(521), m = new Map(521, -1, 1, 11, 0));
        all_map_train.add(m);
        mapList.put(new Integer(522), m = new Map(522, -1, 1, 11, 0));
        all_map_train.add(m);
        mapList.put(new Integer(523), m = new Map(523, -1, 0, 11, 0));
        all_map_train.add(m);
        mapList.put(new Integer(524), m = new Map(524, -1, 0, 11, 0));
        all_map_train.add(m);
        mapList.put(new Integer(12), new Map(12, -1, 1, 12, 0));
        mapList.put(new Integer(541), new Map(541, -1, 1, 12, 0));
        mapList.put(new Integer(542), new Map(542, -1, 1, 12, 0));
        mapList.put(new Integer(543), new Map(543, -1, 0, 12, 0));
        mapList.put(new Integer(544), new Map(544, -1, 0, 12, 0));
        mapList.put(new Integer(105), new Map(105, -1, 1, 105, 0));
        mapList.put(new Integer(106), new Map(106, -1, 1, 106, 0));
        mapList.put(new Integer(2421), new Map(2421, -1, 1, 106, 0));
        mapList.put(new Integer(2422), new Map(2422, -1, 1, 106, 0));
        mapList.put(new Integer(2423), new Map(2423, -1, 0, 106, 0));
        mapList.put(new Integer(2424), new Map(2424, -1, 0, 106, 0));
        mapList.put(new Integer(107), new Map(107, -1, 1, 107, 0));
        mapList.put(new Integer(2441), new Map(2441, -1, 1, 107, 0));
        mapList.put(new Integer(2442), new Map(2442, -1, 1, 107, 0));
        mapList.put(new Integer(2443), new Map(2443, -1, 0, 107, 0));
        mapList.put(new Integer(2444), new Map(2444, -1, 0, 107, 0));
        mapList.put(new Integer(110), new Map(110, -1, 1, 110, 0));
        mapList.put(new Integer(2501), new Map(2501, -1, 1, 110, 0));
        mapList.put(new Integer(2502), new Map(2502, -1, 1, 110, 0));
        mapList.put(new Integer(2503), new Map(2503, -1, 0, 110, 0));
        mapList.put(new Integer(2504), new Map(2504, -1, 0, 110, 0));
        if (TeamServer.server != 0) {
            mapList.put(new Integer(30), new MapLienDau(30, -1, 1, 201));
            mapList.put(new Integer(31), new MapLienDau(31, -1, 1, 201));
            if (TeamServer.isServerLienDau()) {
                mapList.put(new Integer(32), new MapLienDau(32, -1, 1, 201));
                mapList.put(new Integer(33), new MapLienDau(33, -1, 1, 201));
                mapList.put(new Integer(34), new MapLienDau(34, -1, 1, 201));
                mapList.put(new Integer(35), new MapLienDau(35, -1, 1, 201));
                mapList.put(new Integer(36), new MapLienDau(36, -1, 1, 201));
                mapList.put(new Integer(37), new MapLienDau(37, -1, 1, 201));
                mapList.put(new Integer(38), new MapLienDau(38, -1, 1, 201));
                mapList.put(new Integer(39), new MapLienDau(39, -1, 1, 201));
            }
        }

        mapList.put(new Integer(40), new MapChienTruongMoba(40, -1, 1, 40));
        mapList.put(new Integer(41), new Map(41, -1, 1, 41, 0));
        mapList.put(new Integer(42), new MapChauBau(42, -1, 1, 42));

        try {
            mapList.put(new Integer(227), new MapLoiDai(227, -1, 1, 227, 0));
            mapList.put(new Integer(228), new Map(228, -1, 1, 228, 0));
        } catch (Exception var3) {
        }

        mapList.put(new Integer(111), new MapBoss(111, -1, 1, 111));
        mapList.put(new Integer(112), new Map(112, -1, 1, 112, 0));
        mapList.put(new Integer(2541), new Map(2541, -1, 1, 112, 0));
        mapList.put(new Integer(2542), new Map(2542, -1, 1, 112, 0));
        mapList.put(new Integer(2543), new Map(2543, -1, 0, 112, 0));
        mapList.put(new Integer(2544), new Map(2544, -1, 0, 112, 0));
        mapList.put(new Integer(201), new MapTown(201, -1, 1, 201, 0));
        mapList.put(new Integer(202), new Map(202, -1, 1, 202, 0));
        mapList.put(new Integer(118), new Map(118, -1, 1, 118, 0));
        mapList.put(new Integer(225), new PrivateMap(225, -1, 1, 225));
        mapList.put(new Integer(226), new PrivateMap(226, -1, 1, 226));
        mapList.put(new Integer(204), new ArenaMap(204, -1, 1, 204));
        mapList.put(new Integer(205), new ArenaMap(205, -1, 1, 205));
        mapList.put(new Integer(206), new MapClan(206, -1, 1, 206));
        mapList.put(new Integer(208), new MapVanTienTran(208, -1, 1, 25, 0));
        mapList.put(new Integer(209), new MapVanTienTran(209, -1, 1, 25, 0));
        mapList.put(new Integer(210), new MapVanTienTran(210, -1, 1, 25, 0));
        mapList.put(new Integer(203), new Map(203, -1, 1, 203, 0));
        mapList.put(new Integer(4361), new Map(4361, -1, 1, 203, 0));
        mapList.put(new Integer(4362), new Map(4362, -1, 1, 203, 0));
        mapList.put(new Integer(4363), new Map(4363, -1, 1, 203, 0));
        mapList.put(new Integer(4364), new Map(4364, -1, 1, 203, 0));
        mapList.put(new Integer(4365), new Map(4365, -1, 1, 203, 0));
        mapList.put(new Integer(4366), new Map(4366, -1, 1, 203, 0));
        mapList.put(new Integer(13), m = new Map(13, -1, 1, 13, 0));
        all_map_train.add(m);
        mapList.put(new Integer(561), m = new Map(561, -1, 1, 13, 0));
        all_map_train.add(m);
        mapList.put(new Integer(562), m = new Map(562, -1, 1, 13, 0));
        all_map_train.add(m);
        mapList.put(new Integer(563), m = new Map(563, -1, 0, 13, 0));
        all_map_train.add(m);
        mapList.put(new Integer(564), m = new Map(564, -1, 0, 13, 0));
        all_map_train.add(m);
        int nregion = 10;
        if (TeamServer.server == 0 || TeamServer.server == 50) {
            nregion = 5;
        }

        mapList.put(new Integer(14), m = new Map(14, -1, 1, 14, nregion));
        m.setPostRegion(new short[]{42, 6});
        all_map_train.add(m);
        mapList.put(new Integer(581), m = new Map(581, -1, 1, 14, nregion));
        m.setPostRegion(new short[]{42, 6});
        all_map_train.add(m);
        mapList.put(new Integer(582), m = new Map(582, -1, 1, 14, nregion));
        m.setPostRegion(new short[]{42, 6});
        all_map_train.add(m);
        mapList.put(new Integer(583), m = new Map(583, -1, 0, 14, nregion));
        m.setPostRegion(new short[]{42, 6});
        all_map_train.add(m);
        mapList.put(new Integer(584), m = new Map(584, -1, 0, 14, nregion));
        m.setPostRegion(new short[]{42, 6});
        all_map_train.add(m);
        mapList.put(new Integer(15), m = new Map(15, -1, 1, 15, nregion));
        m.setPostRegion(new short[]{4, 17});
        all_map_train.add(m);
        mapList.put(new Integer(601), m = new Map(601, -1, 1, 15, nregion));
        m.setPostRegion(new short[]{4, 17});
        all_map_train.add(m);
        mapList.put(new Integer(602), m = new Map(602, -1, 1, 15, nregion));
        m.setPostRegion(new short[]{4, 17});
        all_map_train.add(m);
        mapList.put(new Integer(603), m = new Map(603, -1, 0, 15, nregion));
        m.setPostRegion(new short[]{4, 17});
        all_map_train.add(m);
        mapList.put(new Integer(604), m = new Map(604, -1, 0, 15, nregion));
        m.setPostRegion(new short[]{4, 17});
        all_map_train.add(m);
        mapList.put(new Integer(113), m = new Map(113, -1, 0, 113, nregion));
        m.setPostRegion(new short[]{97, 7});
        all_map_train.add(m);
        mapList.put(new Integer(2561), m = new Map(2561, -1, 1, 113, nregion));
        m.setPostRegion(new short[]{97, 7});
        all_map_train.add(m);
        mapList.put(new Integer(2562), m = new Map(2562, -1, 1, 113, nregion));
        m.setPostRegion(new short[]{97, 7});
        all_map_train.add(m);
        mapList.put(new Integer(2563), m = new Map(2563, -1, 0, 113, nregion));
        m.setPostRegion(new short[]{97, 7});
        all_map_train.add(m);
        mapList.put(new Integer(2564), m = new Map(2564, -1, 0, 113, nregion));
        m.setPostRegion(new short[]{97, 7});
        all_map_train.add(m);
        mapList.put(new Integer(114), m = new Map(114, -1, 1, 114, nregion));
        m.setPostRegion(new short[]{69, 5});
        mapList.put(new Integer(2581), m = new Map(2581, -1, 1, 114, nregion));
        m.setPostRegion(new short[]{69, 5});
        mapList.put(new Integer(2582), m = new Map(2582, -1, 1, 114, nregion));
        m.setPostRegion(new short[]{69, 5});
        mapList.put(new Integer(2583), m = new Map(2583, -1, 0, 114, nregion));
        m.setPostRegion(new short[]{69, 5});
        mapList.put(new Integer(2584), m = new Map(2584, -1, 0, 114, nregion));
        m.setPostRegion(new short[]{69, 5});
        mapList.put(new Integer(17), new MaterialMap(17, -1, 1, 17));
        mapList.put(new Integer(25), new MapBoss(25, -1, 1, 25));
        mapList.put(new Integer(19), m = new Map(19, -1, 1, 19, nregion));
        m.setPostRegion(new short[]{47, 56});
        all_map_train.add(m);
        mapList.put(new Integer(681), m = new Map(681, -1, 1, 19, nregion));
        m.setPostRegion(new short[]{47, 56});
        all_map_train.add(m);
        mapList.put(new Integer(682), m = new Map(682, -1, 1, 19, nregion));
        m.setPostRegion(new short[]{47, 56});
        all_map_train.add(m);
        mapList.put(new Integer(683), m = new Map(683, -1, 0, 19, nregion));
        m.setPostRegion(new short[]{47, 56});
        all_map_train.add(m);
        mapList.put(new Integer(684), m = new Map(684, -1, 0, 19, nregion));
        m.setPostRegion(new short[]{47, 56});
        all_map_train.add(m);
        mapList.put(new Integer(18), m = new Map(18, -1, 1, 18, nregion));
        m.setPostRegion(new short[]{38, 55});
        all_map_train.add(m);
        mapList.put(new Integer(661), m = new Map(661, -1, 1, 18, nregion));
        m.setPostRegion(new short[]{38, 55});
        all_map_train.add(m);
        mapList.put(new Integer(662), m = new Map(662, -1, 1, 18, nregion));
        m.setPostRegion(new short[]{38, 55});
        all_map_train.add(m);
        mapList.put(new Integer(663), m = new Map(663, -1, 0, 18, nregion));
        m.setPostRegion(new short[]{38, 55});
        all_map_train.add(m);
        mapList.put(new Integer(664), m = new Map(664, -1, 0, 18, nregion));
        m.setPostRegion(new short[]{38, 55});
        all_map_train.add(m);
        mapList.put(new Integer(20), m = new Map(20, -1, 1, 20, 10));
        m.setPostRegion(new short[]{4, 51});
        all_map_train.add(m);
        mapList.put(new Integer(701), m = new Map(701, -1, 1, 20, nregion));
        m.setPostRegion(new short[]{4, 51});
        all_map_train.add(m);
        mapList.put(new Integer(702), m = new Map(702, -1, 1, 20, nregion));
        m.setPostRegion(new short[]{4, 51});
        all_map_train.add(m);
        mapList.put(new Integer(703), m = new Map(703, -1, 0, 20, nregion));
        m.setPostRegion(new short[]{4, 51});
        all_map_train.add(m);
        mapList.put(new Integer(704), m = new Map(704, -1, 0, 20, nregion));
        m.setPostRegion(new short[]{4, 51});
        all_map_train.add(m);
        mapList.put(new Integer(21), m = new Map(21, -1, 1, 21, nregion));
        m.setPostRegion(new short[]{29, 4});
        all_map_train.add(m);
        mapList.put(new Integer(721), m = new Map(721, -1, 1, 21, nregion));
        m.setPostRegion(new short[]{29, 4});
        all_map_train.add(m);
        mapList.put(new Integer(722), m = new Map(722, -1, 1, 21, nregion));
        m.setPostRegion(new short[]{29, 4});
        all_map_train.add(m);
        mapList.put(new Integer(723), m = new Map(723, -1, 0, 21, nregion));
        m.setPostRegion(new short[]{29, 4});
        all_map_train.add(m);
        mapList.put(new Integer(724), m = new Map(724, -1, 0, 21, nregion));
        m.setPostRegion(new short[]{29, 4});
        all_map_train.add(m);
        mapList.put(new Integer(22), m = new Map(22, -1, 1, 22, nregion));
        m.setPostRegion(new short[]{2, 53});
        mapList.put(new Integer(741), m = new Map(741, -1, 1, 22, nregion));
        m.setPostRegion(new short[]{2, 53});
        mapList.put(new Integer(742), m = new Map(742, -1, 1, 22, nregion));
        m.setPostRegion(new short[]{2, 53});
        mapList.put(new Integer(743), m = new Map(743, -1, 0, 22, nregion));
        m.setPostRegion(new short[]{2, 53});
        mapList.put(new Integer(744), m = new Map(744, -1, 0, 22, nregion));
        m.setPostRegion(new short[]{2, 53});
        mapList.put(new Integer(117), new DungeonMap(117, -1, 1, 117));
        mapList.put(new Integer(2641), new DungeonMap(2641, -1, 1, 117));
        mapList.put(new Integer(23), m = new Map(23, -1, 1, 23, nregion));
        m.setPostRegion(new short[]{39, 8});
        mapList.put(new Integer(761), m = new Map(761, -1, 1, 23, nregion));
        m.setPostRegion(new short[]{39, 8});
        mapList.put(new Integer(762), m = new Map(762, -1, 1, 23, nregion));
        m.setPostRegion(new short[]{39, 8});
        mapList.put(new Integer(763), m = new Map(763, -1, 0, 23, nregion));
        m.setPostRegion(new short[]{39, 8});
        mapList.put(new Integer(764), m = new Map(764, -1, 0, 23, nregion));
        m.setPostRegion(new short[]{39, 8});
        mapList.put(new Integer(24), m = new Map(24, -1, 1, 24, nregion));
        m.setPostRegion(new short[]{33, 5});
        mapList.put(new Integer(781), m = new Map(781, -1, 1, 24, nregion));
        m.setPostRegion(new short[]{33, 5});
        mapList.put(new Integer(782), m = new Map(782, -1, 1, 24, nregion));
        m.setPostRegion(new short[]{33, 5});
        mapList.put(new Integer(783), m = new Map(783, -1, 0, 24, nregion));
        m.setPostRegion(new short[]{33, 5});
        mapList.put(new Integer(784), m = new Map(784, -1, 0, 24, nregion));
        m.setPostRegion(new short[]{33, 5});

        mapList.put(new Integer(26), m = new Map(26, -1, 1, 26, nregion));
        m.setPostRegion(new short[]{30, 54});

        try {

            // todo xử lý thêm map
            // Vi tri vao map (Canh Rung Dong) doi tu {40,50} sang {55,55} - gan dau map hon.
            mapList.put(new Integer(28), m = new Map(28, -1, 1, 28, nregion));
            all_map_train.add(m);
            m.setPostRegion(new short[]{55, 55});

            mapList.put(new Integer(861), m = new Map(861, -1, 1, 28, nregion));
            all_map_train.add(m);
            m.setPostRegion(new short[]{55, 55});
            mapList.put(new Integer(862), m = new Map(862, -1, 1, 28, nregion));
            all_map_train.add(m);
            m.setPostRegion(new short[]{55, 55});
            mapList.put(new Integer(863), m = new Map(863, -1, 1, 28, nregion));
            all_map_train.add(m);
            m.setPostRegion(new short[]{55, 55});
            mapList.put(new Integer(864), m = new Map(864, -1, 1, 28, nregion));
            all_map_train.add(m);
            m.setPostRegion(new short[]{55, 55});

        } catch (Exception e) {
            e.printStackTrace();
        }

        System.out.println("TONG MAP TRAIN ROI NGOC RONG: " + all_map_train.size());
        // Vung Hoenn (nam 2). Bang locationMap/mapID noi them o add_more_idMapSend2Client.
        VungHoenn.taoMap(mapList);
        // 0.9.7: ban do phong rieng (Pho ban Ham nguc 4381 nen 117, san Cup PvP 4382 nen 227) - real.PhongRiengGame.
        PhongRiengGame.taoBanDo(mapList);
    }

    public static void add_more_idMapSend2Client() {
        // TODO thêm map thì nhớ thêm cả cái lồn này
        Map.mapChange.put((short) 18, new short[]{18, 14, 19, 28});
        Map.mapChange.put((short) 28, new short[]{28, 18});

        Map.nameMap = new String[][][]{
            {{"Dao Châu", "Đông Dao Châu", "Tây Dao Châu", "Nam Dao Châu", "Bắc Dao Châu"},
            {"Bến tàu", "Bến đông", "Bến tây", "Bến nam", "Bến bắc"},
            {"Tiên du", "Đông Tiên Du", "Tây Tiên Du", "Nam Tiên Du", "Bắc Tiên Du"},
            {"Khu mua bán", "Khu đông", "Khu tây", "Khu nam", "Khu bắc"}},//0
            {{"Sơn Nam", "Đông Sơn Nam", "", "", ""},
            {"Kỳ bố", "Đông Kỳ bố", "Tây Kỳ bố", "Nam Kỳ bố", "Bắc Kỳ bố"}},//1
            {{"Sơn Nam", "Đông Sơn Nam", "", "", ""},
            {"Phù liệt", "Đông Phù liệt", "Tây Phù liệt", "Nam Phù liệt", "Bắc Phù liệt"}},//2
            {{"Tiên du", "Đông Tiên du", "Tây Tiên du", "Nam Tiên du", "Bắc Tiên du"},
            {"Kỳ bố", "Đông Kỳ bố", "Tây Kỳ bố", "Nam Kỳ bố", "Bắc Kỳ bố"},
            {"Hàm tử", "Đông Hàm tử", "Tây Hàm tử", "Nam Hàm tử", "Bắc Hàm tử"}},//3
            {{"Dao Châu", "Đông Dao Châu", "Tây Dao Châu", "Nam Dao Châu", "Bắc Dao Châu"},
            {"Hang động", "Hang phía đông", "Hang phía tây", "Hang phía nam", "Hang phía bắc"},
            {"Phù liệt", "Đông Phù liệt", "Tây Phù liệt", "Nam Phù liệt", "Bắc Phù liệt"}},//4
            {{"Phù liệt", "Đông Phù liệt", "Tây Phù liệt", "Nam Phù liệt", "Bắc Phù liệt"},
            {"Thạch giang", "Đông Thạch giang", "Tây Thạch giang", "Nam Thạch giang", "Bắc Thạch giang"}},//5
            {{"Hàm tử", "Đông Hàm tử", "Tây Hàm tử", "Nam Hàm tử", "Bắc Hàm tử"},
            {"Đông sơn", "Đông Đông sơn", "Tây Đông sơn", "Nam Đông sơn", "Bắc Đông sơn"}},//6
            {{"Thạch giang", "Đông Thạch giang", "Tây Thạch giang", "Nam Thạch giang", "Bắc Thạch giang"},
            {"Tử quan", "Đông Tử quan", "Tây Tử quan", "Nam Tử quan", "Bắc Tử quan"}},//7
            {{"Đông sơn", "Đông Đông sơn", "Tây Đông sơn", "Nam Đông sơn", "Bắc Đông sơn"},
            {"Trường giang", "Đông Trường giang", "Tây Trường giang", "Nam Trường giang", "Bắc Trường giang"}},//8
            {{"Tử quan", "Đông Tử quan", "Tây Tử quan", "Nam Tử quan", "Bắc Tử quan"},
            {"Cao nguyên Indigo", "Biên giới", "", "", ""}},//9
            {{"Sơn lâm", "Đông Sơn lâm", "Tây Sơn lâm", "Nam Sơn lâm", "Bắc Sơn lâm"},
            {"Phong châu", "Đông Phong châu", "Tây Phong châu", "Nam Phong châu", "Bắc Phong châu"},
            {"Bình lục", "Đông Bình lục", "Tây Bình lục", "Nam Bình lục", "Bắc Bình lục"}},//10
            {{"Lộc trĩ", "Đông Lộc trĩ", "Tây Lộc trĩ", "Nam Lộc trĩ", "Bắc Lộc trĩ"},
            {"Bảo thái", "Đông Bảo thái", "Tây Bảo thái", "Nam Bảo thái", "Bắc Bảo thái"},
            {"Hầm tối", "Đông Hầm tối", "Tây Hầm tối", "Nam Hầm tối", "Bắc Hầm tối"},
            {"Hồi hồ", "Đông Hồi hồ", "Tây Hồi hồ", "Nam Hồi hồ", "Bắc Hồi hồ"}},//11
            {{"Sơn Nam", "Đông Sơn Nam", "", "", ""}},//12
            {{"Sơn Nam", "Đông Sơn Nam", "", "", ""}},//13
            {{"Kỳ bố", "Đông Kỳ bố", "Tây Kỳ bố", "Nam Kỳ bố", "Bắc Kỳ bố"},
            {"Hang thằn lằn", "Hang Đại Bàng", "", "", ""},
            {"Hang Darkrai", "", "", "", ""}},//14
            new String[0][],//15
            {{"Hang động", "Hang phía đông", "Hang phía tây", "Hang phía nam", "Hang phía bắc"}},//16
            {{"Hang động", "Hang phía đông", "Hang phía tây", "Hang phía nam", "Hang phía bắc"}},//17
            {{"Sơn Nam", "Đông Sơn Nam", "", "", ""}},//18
            {{"Trường giang", "Đông Trường giang", "Tây Trường giang", "Nam Trường giang", "Bắc Trường giang"},
            {"Trường giang", "Đông Trường giang", "Tây Trường giang", "Nam Trường giang", "Bắc Trường giang"},
            {"Trường giang", "Đông Trường giang", "Tây Trường giang", "Nam Trường giang", "Bắc Trường giang"},
            {"", "", "", "", ""}},//19
            {{"Lộc trĩ", "Đông Lộc trĩ", "Tây Lộc trĩ", "Nam Lộc trĩ", "Bắc Lộc trĩ"},
            {"Siêu loại", "Đông Siêu loại", "Tây Siêu loại", "Nam Siêu loại", "Bắc Siêu loại"}},//20
            {{"Lộc trĩ", "Đông Lộc trĩ", "Tây Lộc trĩ", "Nam Lộc trĩ", "Bắc Lộc trĩ"},
            {"Cẩm khê", "Đông Cẩm khê", "Tây Cẩm khê", "Nam Cẩm khê", "Bắc Cẩm khê"},
            {"Bạch hạc", "Đông Bạch hạc", "Tây Bạch hạc", "Nam Bạch hạc", "Bắc Bạch hạc"}},//21
            {{"Sơn lâm", "Đông Sơn lâm", "Tây Sơn lâm", "Nam Sơn lâm", "Bắc Sơn lâm"},
            {"Tế giang", "Đông Tế giang", "Tây Tế giang", "Nam Tế giang", "Bắc Tế giang"},
            {"Đỗ đông", "Đông Đỗ đông", "Tây Đỗ đông", "Nam Đỗ đông", "Bắc Đỗ đông"},
            {"Rừng già", "", "", "", ""}},//22
            {{"Sơn lâm", "Đông Sơn lâm", "Tây Sơn lâm", "Nam Sơn lâm", "Bắc Sơn lâm"}},//23
            {{"Sơn lâm", "Đông Sơn lâm", "Tây Sơn lâm", "Nam Sơn lâm", "Bắc Sơn lâm"},
            {"Siêu loại", "Đông Siêu loại", "Tây Siêu loại", "Nam Siêu loại", "Bắc Siêu loại"}},//24
            {{"Bạch hạc", "Đông Bạch hạc", "Tây Bạch hạc", "Nam Bạch hạc", "Bắc Bạch hạc"},
            {"Bảo thái", "Đông Bảo thái", "Tây Bảo thái", "Nam Bảo thái", "Bắc Bảo thái"},
            {"Siêu loại", "Đông Siêu loại", "Tây Siêu loại", "Nam Siêu loại", "Bắc Siêu loại"}},//25
            {{"Bình lục", "Đông Bình lục", "Tây Bình lục", "Nam Bình lục", "Bắc Bình lục"},
            {"Tế giang", "Đông Tế giang", "Tây Tế giang", "Nam Tế giang", "Bắc Tế giang"},
            {"Cánh rừng đông", "", "", "", ""}}, //name mapto //26
            {{"Tế giang", "Đông Tế giang", "Tây Tế giang", "Nam Tế giang", "Bắc Tế giang"},
            {"Phong châu", "Đông Phong châu", "Tây Phong châu", "Nam Phong châu", "Bắc Phong châu"},
            {"Hồi hồ", "Đông Hồi hồ", "Tây Hồi hồ", "Nam Hồi hồ", "Bắc Hồi hồ"}},
            {{"Bạch hạc", "Đông Bạch hạc", "Tây Bạch hạc", "Nam Bạch hạc", "Bắc Bạch hạc"}}, // map 28
            new String[0][],
            new String[0][],
            new String[0][],
            {{"Cẩm khê", "Đông Cẩm khê", "Tây Cẩm khê", "Nam Cẩm khê", "Bắc Cẩm khê"},
            {"Đỗ đông", "Đông Đỗ đông", "Tây Đỗ đông", "Nam Đỗ đông", "Bắc Đỗ đông"}}, new String[0][], {{"Dao Châu", "Đông Dao Châu", "Tây Dao Châu", "Nam Dao Châu", "Bắc Dao Châu"},
            {"Bến tàu", "Bến đông", "Bến tây", "Bến nam", "Bến bắc"},
            {"Tiên du", "Đông Tiên Du", "Tây Tiên Du", "Nam Tiên Du", "Bắc Tiên Du"},
            {"Khu mua bán", "Khu đông", "Khu tây", "Khu nam", "Khu bắc"}},
            {{"Dao Châu", "Đông Dao Châu", "Tây Dao Châu", "Nam Dao Châu", "Bắc Dao Châu"},
            {"Bến tàu", "Bến đông", "Bến tây", "Bến nam", "Bến bắc"},
            {"Tiên du", "Đông Tiên Du", "Tây Tiên Du", "Nam Tiên Du", "Bắc Tiên Du"},
            {"Khu mua bán", "Khu đông", "Khu tây", "Khu nam", "Khu bắc"}},
            {{"Biên giới", "", "", "", ""},
            {"Bình lục", "Đông Bình lục", "Tây Bình lục", "Nam Bình lục", "Bắc Bình lục"}},
            {{"Biên giới", "", "", "", ""},
            {"Bảo thái", "Đông Bảo thái", "Tây Bảo thái", "Nam Bảo thái", "Bắc Bảo thái"}},
            {{"Cao nguyên Indigo", "", "", "", ""}},
            {{"Bảo thái", "Đông Bảo thái", "Tây Bảo thái", "Nam Bảo thái", "Bắc Bảo thái"}}, new String[0][], new String[0][], new String[0][], new String[0][], {{"Trường giang", "Đông Trường giang", "Tây Trường giang", "Nam Trường giang", "Bắc Trường giang"},
            {"Trường giang", "Đông Trường giang", "Tây Trường giang", "Nam Trường giang", "Bắc Trường giang"},
            {"Trường giang", "Đông Trường giang", "Tây Trường giang", "Nam Trường giang", "Bắc Trường giang"},
            {"", "", "", "", ""}},
            {{"Trường giang", "Đông Trường giang", "Tây Trường giang", "Nam Trường giang", "Bắc Trường giang"},
            {"Trường giang", "Đông Trường giang", "Tây Trường giang", "Nam Trường giang", "Bắc Trường giang"},
            {"Trường giang", "Đông Trường giang", "Tây Trường giang", "Nam Trường giang", "Bắc Trường giang"},
            {"", "", "", "", ""}},
            {{"Trường giang", "Đông Trường giang", "Tây Trường giang", "Nam Trường giang", "Bắc Trường giang"},
            {"Trường giang", "Đông Trường giang", "Tây Trường giang", "Nam Trường giang", "Bắc Trường giang"},
            {"Trường giang", "Đông Trường giang", "Tây Trường giang", "Nam Trường giang", "Bắc Trường giang"},
            {"", "", "", "", ""}},
            {{"Trường giang", "Đông Trường giang", "Tây Trường giang", "Nam Trường giang", "Bắc Trường giang"},
            {"Trường giang", "Đông Trường giang", "Tây Trường giang", "Nam Trường giang", "Bắc Trường giang"},
            {"Trường giang", "Đông Trường giang", "Tây Trường giang", "Nam Trường giang", "Bắc Trường giang"},
            {"", "", "", "", ""}},
            {{"Trường giang", "Đông Trường giang", "Tây Trường giang", "Nam Trường giang", "Bắc Trường giang"},
            {"Trường giang", "Đông Trường giang", "Tây Trường giang", "Nam Trường giang", "Bắc Trường giang"},
            {"Trường giang", "Đông Trường giang", "Tây Trường giang", "Nam Trường giang", "Bắc Trường giang"},
            {"", "", "", "", ""}},
            {{"Trường giang", "Đông Trường giang", "Tây Trường giang", "Nam Trường giang", "Bắc Trường giang"},
            {"Trường giang", "Đông Trường giang", "Tây Trường giang", "Nam Trường giang", "Bắc Trường giang"},
            {"Trường giang", "Đông Trường giang", "Tây Trường giang", "Nam Trường giang", "Bắc Trường giang"},
            {"", "", "", "", ""}},
            {{"Trường giang", "Đông Trường giang", "Tây Trường giang", "Nam Trường giang", "Bắc Trường giang"},
            {"Trường giang", "Đông Trường giang", "Tây Trường giang", "Nam Trường giang", "Bắc Trường giang"},
            {"Trường giang", "Đông Trường giang", "Tây Trường giang", "Nam Trường giang", "Bắc Trường giang"},
            {"", "", "", "", ""}},
            {{"Trường giang", "Đông Trường giang", "Tây Trường giang", "Nam Trường giang", "Bắc Trường giang"},
            {"Trường giang", "Đông Trường giang", "Tây Trường giang", "Nam Trường giang", "Bắc Trường giang"},
            {"Trường giang", "Đông Trường giang", "Tây Trường giang", "Nam Trường giang", "Bắc Trường giang"},
            {"", "", "", "", ""}},
            {{"Trường giang", "Đông Trường giang", "Tây Trường giang", "Nam Trường giang", "Bắc Trường giang"},
            {"Trường giang", "Đông Trường giang", "Tây Trường giang", "Nam Trường giang", "Bắc Trường giang"},
            {"Trường giang", "Đông Trường giang", "Tây Trường giang", "Nam Trường giang", "Bắc Trường giang"},
            {"", "", "", "", ""}},
            {{"Trường giang", "Đông Trường giang", "Tây Trường giang", "Nam Trường giang", "Bắc Trường giang"},
            {"Trường giang", "Đông Trường giang", "Tây Trường giang", "Nam Trường giang", "Bắc Trường giang"},
            {"Trường giang", "Đông Trường giang", "Tây Trường giang", "Nam Trường giang", "Bắc Trường giang"},
            {"", "", "", "", ""}},
            {{"", "", "", "", ""},
            {"", "", "", "", ""},
            {"", "", "", "", ""}},
            {{"Về làng", "", "", "", ""},
            {"Về làng", "", "", "", ""},
            {"Về làng", "", "", "", ""}},
            {{"", "", "", "", ""},
            {"", "", "", "", ""},
            {"", "", "", "", ""}},
            {{"", "", "", "", ""},
            {"", "", "", "", ""},
            {"", "", "", "", ""}},
            {{"", "", "", "", ""},
            {"", "", "", "", ""},
            {"", "", "", "", ""}}};
        Map.locationMap = new short[][][]{
            {{1, 1, 61, 321, 1, 61, 322, 1, 61, 323, 1, 61, 324, 1, 61},
            {106, 1, 9, 2421, 1, 9, 2422, 1, 9, 2423, 1, 9, 2424, 1, 9},
            {2, 15, 1, 341, 15, 1, 342, 15, 1, 343, 15, 1, 344, 15, 1},
            {12, 46, 13, 541, 46, 13, 542, 46, 13, 543, 46, 13, 544, 46, 13}},
            {{0, 17, 2, 301, 17, 2, 302, 17, 2, 303, 17, 2, 304, 17, 2},
            {4, 1, 12, 381, 1, 12, 382, 1, 12, 383, 1, 12, 384, 1, 12}},
            {{0, 23, 58, 301, 23, 58, 302, 23, 58, 303, 23, 58, 304, 23, 58},
            {3, 1, 22, 361, 1, 22, 362, 1, 22, 363, 1, 22, 364, 1, 22}},
            {{2, 126, 21, 341, 126, 21, 342, 126, 21, 343, 126, 21, 344, 126, 21},
            {4, 93, 98, 381, 93, 98, 382, 93, 98, 383, 93, 98, 384, 93, 98},
            {5, 71, 1, 401, 71, 1, 402, 71, 1, 403, 71, 1, 404, 71, 1}},
            {{1, 78, 11, 321, 78, 11, 322, 78, 11, 323, 78, 11, 324, 78, 11},
            {110, 18, 79, 2501, 18, 79, 2502, 18, 79, 2503, 18, 79, 2504, 18, 79},
            {3, 91, 3, 361, 91, 3, 362, 91, 3, 363, 91, 3, 364, 91, 3}},
            {{3, 91, 97, 361, 91, 97, 362, 91, 97, 363, 91, 97, 364, 91, 97},
            {6, 40, 2, 421, 40, 2, 422, 40, 2, 423, 40, 2, 424, 40, 2}},
            {{5, 65, 77, 401, 65, 77, 402, 65, 77, 403, 65, 77, 404, 65, 77},
            {7, 2, 91, 441, 2, 91, 442, 2, 91, 443, 2, 91, 444, 2, 91}},
            {{6, 45, 44, 421, 45, 44, 422, 45, 44, 423, 45, 44, 424, 45, 44},
            {8, 27, 77, 461, 27, 77, 462, 27, 77, 463, 27, 77, 464, 27, 77}},
            {{7, 32, 2, 441, 32, 2, 442, 32, 2, 443, 32, 2, 444, 32, 2},
            {9, 28, 98, 481, 28, 98, 482, 28, 98, 483, 28, 98, 484, 28, 98}},
            {{8, 48, 2, 461, 48, 2, 462, 48, 2, 463, 48, 2, 464, 48, 2},
            {201, 39, 90, 118, 10, 20, 30, 39, 90, 463, 39, 77, 464, 39, 77}},
            {{11, 77, 67, 521, 77, 67, 522, 77, 67, 523, 77, 67, 524, 77, 67},
            {13, 5, 51, 561, 5, 51, 562, 5, 51, 563, 5, 51, 564, 5, 51},
            {14, 77, 29, 581, 77, 29, 582, 77, 29, 583, 77, 29, 584, 77, 29}},
            {{10, 97, 2, 501, 97, 2, 502, 97, 2, 503, 97, 2, 504, 97, 2},
            {15, 4, 22, 601, 4, 22, 602, 4, 22, 603, 4, 22, 604, 4, 22},
            {113, 97, 4, 2561, 97, 4, 2562, 97, 4, 2563, 97, 4, 2564, 97, 4},
            {114, 74, 1, 2581, 74, 1, 2582, 74, 1, 2583, 74, 1, 2584, 74, 1}},
            {{0, 2, 32, 301, 2, 32, 302, 2, 32, 303, 2, 32, 304, 2, 32}},
            {{0, 58, 30, 301, 58, 30, 302, 58, 30, 303, 58, 30, 304, 58, 30}},
            {{4, 49, 5, 381, 49, 5, 382, 49, 5, 383, 49, 5, 384, 49, 5},
            {112, 52, 32, 2541, 52, 32, 2542, 52, 32, 2543, 52, 32, 2544, 52, 32},
            {111, 24, 28, 2521, 24, 28, 2522, 24, 28, 2523, 24, 28, 2524, 24, 28}},
            new short[0][], {{110, 1, 9, 2501, 1, 9, 2502, 1, 9, 22303, 1, 9, 2504, 1, 9}},
            {{110, 77, 1, 2501, 77, 1, 2502, 77, 1, 2503, 77, 1, 2504, 77, 1}},
            {{0, 24, 39, 301, 24, 39, 302, 24, 39, 303, 24, 39, 304, 24, 39}},
            {{9, 33, 1, 481, 33, 1, 482, 33, 1, 483, 33, 1, 484, 33, 1},
            {9, 33, 1, 481, 33, 1, 482, 33, 1, 483, 33, 1, 484, 33, 1},
            {9, 33, 1, 481, 33, 1, 482, 33, 1, 483, 33, 1, 484, 33, 1},
            {206, 5, 47, 206, 5, 47, 206, 5, 47, 206, 5, 47, 206, 5, 47}},
            {{10, 127, 43, 501, 127, 43, 502, 127, 43, 503, 127, 43, 504, 127, 43},
            {20, 25, 1, 701, 25, 1, 702, 25, 1, 703, 25, 1, 704, 25, 1}},
            {{10, 1, 52, 501, 1, 52, 502, 1, 52, 503, 1, 52, 504, 1, 52},
            {23, 54, 2, 761, 54, 2, 762, 54, 2, 763, 54, 2, 764, 54, 2},
            {18, 34, 58, 661, 34, 58, 662, 34, 58, 663, 34, 58, 664, 34, 58}},
            {{11, 31, 4, 521, 31, 4, 522, 31, 4, 523, 31, 4, 524, 31, 4},
            {19, 1, 22, 681, 1, 22, 682, 1, 22, 683, 1, 22, 684, 1, 22},
            {24, 28, 3, 781, 28, 3, 782, 28, 3, 783, 28, 3, 784, 28, 3},
            {26, 22, 55, 821, 22, 55, 822, 22, 55, 823, 22, 55, 824, 22, 55}},
            {{11, 80, 2, 521, 80, 2, 522, 80, 2, 523, 80, 2, 524, 80, 2}},
            {{11, 148, 9, 521, 148, 9, 522, 148, 9, 523, 148, 9, 524, 148, 9},
            {20, 69, 43, 701, 69, 43, 702, 69, 43, 703, 69, 43, 704, 69, 43}},
            {{18, 1, 33, 661, 1, 33, 662, 1, 33, 663, 1, 33, 664, 1, 33},
            {15, 98, 78, 601, 98, 78, 602, 98, 78, 603, 98, 78, 604, 98, 78},
            {20, 1, 47, 701, 1, 47, 702, 1, 47, 703, 1, 47, 704, 1, 47}},
            {
                {14, 36, 1, 581, 36, 1, 582, 36, 1, 583, 36, 1, 584, 36, 1},
                {19, 43, 58, 681, 43, 58, 682, 43, 58, 683, 43, 58, 684, 43, 58},
                {28, 43, 55, 861, 43, 55, 862, 43, 55, 863, 43, 55, 864, 43, 55}

            }, // map 26 qua map 28 ở đây

            {{19, 75, 27, 681, 75, 27, 682, 75, 27, 683, 75, 27, 684, 75, 27},
            {13, 37, 98, 561, 37, 98, 562, 37, 98, 563, 37, 98, 564, 37, 98},
            {114, 1, 76, 2581, 1, 76, 2582, 1, 76, 2583, 1, 76, 2584, 1, 76}},
            {
                {18, 20, 6, 661, 20, 6, 662, 20, 6, 663, 20, 6, 664, 20, 6}
            },
            new short[0][], new short[0][], new short[0][], {{23, 57, 66, 761, 57, 66, 762, 57, 66, 763, 57, 66, 764, 57, 66},
            {24, 9, 67, 781, 9, 67, 782, 9, 67, 783, 9, 67, 784, 9, 67}},
            new short[0][], {{1, 1, 61, 321, 1, 61, 322, 1, 61, 323, 1, 61, 324, 1, 61},
            {106, 1, 9, 2421, 1, 9, 2422, 1, 9, 2423, 1, 9, 2424, 1, 9},
            {2, 15, 1, 341, 15, 1, 342, 15, 1, 343, 15, 1, 344, 15, 1},
            {12, 46, 13, 541, 46, 13, 542, 46, 13, 543, 46, 13, 544, 46, 13}},
            {{1, 1, 61, 321, 1, 61, 322, 1, 61, 323, 1, 61, 324, 1, 61},
            {106, 1, 9, 2421, 1, 9, 2422, 1, 9, 2423, 1, 9, 2424, 1, 9},
            {2, 15, 1, 341, 15, 1, 342, 15, 1, 343, 15, 1, 344, 15, 1},
            {12, 46, 13, 541, 46, 13, 542, 46, 13, 543, 46, 13, 544, 46, 13}},
            {{118, 2, 20, 2661, 2, 20, 2662, 2, 20, 2663, 2, 20, 2664, 2, 20},
            {14, 71, 77, 581, 71, 77, 582, 71, 77, 583, 71, 77, 584, 71, 77}},
            {{118, 68, 2, 2661, 68, 2, 2662, 68, 2, 2663, 68, 2, 2664, 68, 2},
            {15, 48, 97, 601, 48, 97, 602, 48, 97, 603, 48, 97, 604, 48, 97}},
            {{201, 54, 10, 201, 54, 10, 201, 54, 10, 201, 54, 10, 201, 54, 10}},
            {{15, 65, 4, 601, 65, 4, 602, 65, 4, 603, 65, 4, 604, 65, 4}},
            new short[0][], new short[0][], new short[0][], new short[0][], {{9, 33, 1, 481, 33, 1, 482, 33, 1, 483, 33, 1, 484, 33, 1},
            {9, 33, 1, 481, 33, 1, 482, 33, 1, 483, 33, 1, 484, 33, 1},
            {9, 33, 1, 481, 33, 1, 482, 33, 1, 483, 33, 1, 484, 33, 1},
            {206, 5, 47, 206, 5, 47, 206, 5, 47, 206, 5, 47, 206, 5, 47}},
            {{9, 33, 1, 481, 33, 1, 482, 33, 1, 483, 33, 1, 484, 33, 1},
            {9, 33, 1, 481, 33, 1, 482, 33, 1, 483, 33, 1, 484, 33, 1},
            {9, 33, 1, 481, 33, 1, 482, 33, 1, 483, 33, 1, 484, 33, 1},
            {206, 5, 47, 206, 5, 47, 206, 5, 47, 206, 5, 47, 206, 5, 47}},
            {{9, 33, 1, 481, 33, 1, 482, 33, 1, 483, 33, 1, 484, 33, 1},
            {9, 33, 1, 481, 33, 1, 482, 33, 1, 483, 33, 1, 484, 33, 1},
            {9, 33, 1, 481, 33, 1, 482, 33, 1, 483, 33, 1, 484, 33, 1},
            {206, 5, 47, 206, 5, 47, 206, 5, 47, 206, 5, 47, 206, 5, 47}},
            {{9, 33, 1, 481, 33, 1, 482, 33, 1, 483, 33, 1, 484, 33, 1},
            {9, 33, 1, 481, 33, 1, 482, 33, 1, 483, 33, 1, 484, 33, 1},
            {9, 33, 1, 481, 33, 1, 482, 33, 1, 483, 33, 1, 484, 33, 1},
            {206, 5, 47, 206, 5, 47, 206, 5, 47, 206, 5, 47, 206, 5, 47}},
            {{9, 33, 1, 481, 33, 1, 482, 33, 1, 483, 33, 1, 484, 33, 1},
            {9, 33, 1, 481, 33, 1, 482, 33, 1, 483, 33, 1, 484, 33, 1},
            {9, 33, 1, 481, 33, 1, 482, 33, 1, 483, 33, 1, 484, 33, 1},
            {206, 5, 47, 206, 5, 47, 206, 5, 47, 206, 5, 47, 206, 5, 47}},
            {{9, 33, 1, 481, 33, 1, 482, 33, 1, 483, 33, 1, 484, 33, 1},
            {9, 33, 1, 481, 33, 1, 482, 33, 1, 483, 33, 1, 484, 33, 1},
            {9, 33, 1, 481, 33, 1, 482, 33, 1, 483, 33, 1, 484, 33, 1},
            {206, 5, 47, 206, 5, 47, 206, 5, 47, 206, 5, 47, 206, 5, 47}},
            {{9, 33, 1, 481, 33, 1, 482, 33, 1, 483, 33, 1, 484, 33, 1},
            {9, 33, 1, 481, 33, 1, 482, 33, 1, 483, 33, 1, 484, 33, 1},
            {9, 33, 1, 481, 33, 1, 482, 33, 1, 483, 33, 1, 484, 33, 1},
            {206, 5, 47, 206, 5, 47, 206, 5, 47, 206, 5, 47, 206, 5, 47}},
            {{9, 33, 1, 481, 33, 1, 482, 33, 1, 483, 33, 1, 484, 33, 1},
            {9, 33, 1, 481, 33, 1, 482, 33, 1, 483, 33, 1, 484, 33, 1},
            {9, 33, 1, 481, 33, 1, 482, 33, 1, 483, 33, 1, 484, 33, 1},
            {206, 5, 47, 206, 5, 47, 206, 5, 47, 206, 5, 47, 206, 5, 47}},
            {{9, 33, 1, 481, 33, 1, 482, 33, 1, 483, 33, 1, 484, 33, 1},
            {9, 33, 1, 481, 33, 1, 482, 33, 1, 483, 33, 1, 484, 33, 1},
            {9, 33, 1, 481, 33, 1, 482, 33, 1, 483, 33, 1, 484, 33, 1},
            {206, 5, 47, 206, 5, 47, 206, 5, 47, 206, 5, 47, 206, 5, 47}},
            {{9, 33, 1, 481, 33, 1, 482, 33, 1, 483, 33, 1, 484, 33, 1},
            {9, 33, 1, 481, 33, 1, 482, 33, 1, 483, 33, 1, 484, 33, 1},
            {9, 33, 1, 481, 33, 1, 482, 33, 1, 483, 33, 1, 484, 33, 1},
            {206, 5, 47, 206, 5, 47, 206, 5, 47, 206, 5, 47, 206, 5, 47}},
            new short[0][],
            {{0, 33, 1, 0, 33, 1, 0, 33, 1, 0, 33, 1, 0, 33, 1}},
            new short[0][], new short[0][], new short[0][]
        };

        Map.localXaphu = new short[][]{{0, 24, 39, 301, 24, 39, 302, 24, 39, 303, 24, 39, 304, 24, 39},
        {1, 1, 61, 321, 1, 61, 322, 1, 61, 323, 1, 61, 324, 1, 61},
        {2, 15, 1, 341, 15, 1, 342, 15, 1, 343, 15, 1, 344, 15, 1},
        {3, 5, 20, 361, 5, 20, 362, 5, 20, 363, 5, 20, 364, 5, 20},
        {4, 27, 15, 381, 27, 15, 382, 27, 15, 383, 27, 15, 384, 27, 15},
        {5, 58, 12, 401, 58, 12, 402, 58, 12, 403, 58, 12, 404, 58, 12},
        {6, 37, 9, 421, 37, 9, 422, 37, 9, 423, 37, 9, 424, 37, 9},
        {7, 9, 86, 441, 9, 86, 442, 9, 86, 443, 9, 86, 444, 9, 86},
        {8, 25, 73, 461, 25, 73, 462, 25, 73, 463, 25, 73, 464, 25, 73},
        {9, 17, 91, 481, 17, 91, 482, 17, 91, 483, 17, 91, 484, 17, 91},
        {10, 0, 0, 501, 0, 0, 502, 0, 0, 503, 0, 0, 504, 0, 0},
        {11, 0, 0, 521, 0, 0, 522, 0, 0, 523, 0, 0, 524, 0, 0},
        {12, 0, 0, 541, 0, 0, 542, 0, 0, 543, 0, 0, 544, 0, 0},
        {106, 0, 0, 2421, 0, 0, 2422, 0, 0, 2423, 0, 0, 2424, 0, 0},
        {110, 0, 0, 2501, 0, 0, 2502, 0, 0, 2503, 0, 0, 2504, 0, 0},
        {107, 0, 0, 2441, 0, 0, 2442, 0, 0, 2443, 0, 0, 2444, 0, 0},
        {111, 0, 0, 2521, 0, 0, 2522, 0, 0, 2523, 0, 0, 2524, 0, 0},
        {112, 0, 0, 2541, 0, 0, 2542, 0, 0, 2543, 0, 0, 2544, 0, 0},
        {202, 0, 0, 4341, 0, 0, 4342, 0, 0, 4343, 0, 0, 4344, 0, 0},
        {201, 0, 0, 4321, 0, 0, 4322, 0, 0, 4323, 0, 0, 4324, 0, 0}};

        Map.namMap = new String[][]{
            {"Sơn Nam", "Đông Sơn Nam", "Tây Sơn Nam", "Nam Sơn Nam", "Bắc Sơn Nam"},
            {"Dao Châu", "Đông Dao Châu", "Tây Dao Châu", "Nam Dao Châu", "Bắc Dao Châu"},
            {"Tiên du", "Đông Tiên Du", "Tây Tiên Du", "Nam Tiên Du", "Bắc Tiên Du"},
            {"Phù liệt", "Đông Phù liệt", "Tây Phù liệt", "Nam Phù liệt", "Bắc Phù liệt"},
            {"Kỳ bố", "Đông Kỳ bố", "Tây Kỳ bố", "Nam Kỳ bố", "Bắc Kỳ bố"},
            {"Hàm tử", "Đông Hàm tử", "Tây Hàm tử", "Nam Hàm tử", "Bắc Hàm tử"},
            {"Thạch giang", "Đông Thạch giang", "Tây Thạch giang", "Nam Thạch giang", "Bắc Thạch giang"},
            {"Đông sơn", "Đông Đông sơn", "Tây Đông sơn", "Nam Đông sơn", "Bắc Đông sơn"},
            {"Tử quan", "Đông Tử quan", "Tây Tử quan", "Nam Tử quan", "Bắc Tử quan"},
            {"Trường giang", "Đông Trường giang", "Tây Trường giang", "Nam Trường giang", "Bắc Trường giang"},
            {"Lộc trĩ", "Đông Lộc trĩ", "Tây Lộc trĩ", "Nam Lộc trĩ", "Bắc Lộc trĩ"},
            {"Sơn lâm", "Đông Sơn lâm", "Tây Sơn lâm", "Nam Sơn lâm", "Bắc Sơn lâm"},
            {"Khu mua bán", "Khu đông", "Khu tây", "Khu nam", "Khu bắc"},
            {"Bến tàu", "Bến đông", "Bến tây", "Bến nam", "Bến bắc"},
            {"Hang động", "Hang phía đông", "Hang phía tây", "Hang phía nam", "Hang phía bắc"},
            {"Thuyền", "Thuyền bến đông", "Thuyền bến tây", "Thuyền bến nam", "Thuyền bến bắc"},
            {"Hang Thằn Lằn", "Hang Đại Bàng", "Hang thằn lằn tây", "Hang thằn lằn nam", "Hang thằn lằn bắc"},
            {"Hang Darkrai", "Hang Darkrai phía đông", "Hang Darkrai phía tây", "Hang Darkrai phía nam", "Hang Darkrai phía bắc"},
            {"Đấu trường", "", "", "", ""},
            {"Cao nguyên Indigo", "", "", "", ""},
            {"Phong châu", "Đông Phong châu", "Tây Phong châu", "Nam Phong châu", "Bắc Phong châu"},
            {"Bình lục", "Đông Bình lục", "Tây Bình lục", "Nam Bình lục", "Bắc Bình lục"},
            {"Bảo thái", "Đông Bảo thái", "Tây Bảo thái", "Nam Bảo thái", "Bắc Bảo thái"},
            {"Hầm tối", "Đông Hầm tối", "Tây Hầm tối", "Nam Hầm tối", "Bắc Hầm tối"},
            {"Hồi hồ", "Đông Hồi hồ", "Tây Hồi hồ", "Nam Hồi hồ", "Bắc Hồi hồ"},
            {"Tế giang", "Đông Tế giang", "Tây Tế giang", "Nam Tế giang", "Bắc Tế giang"},
            {"Bạch hạc", "Đông Bạch hạc", "Tây Bạch hạc", "Nam Bạch hạc", "Bắc Bạch hạc"},
            {"Siêu Loại", "Đông Siêu Loại", "Tây Siêu Loại", "Nam Siêu Loại", "Bắc Siêu Loại"},
            {"Bình Kiều", "Đông Bình Kiều", "Tây Bình Kiều", "Nam Bình Kiều", "Bắc Bình Kiều"},
            {"Chiến hạm", "Đông Chiến hạm", "Tây Chiến hạm", "Nam Chiến hạm", "Bắc Chiến hạm"},
            {"Đấu trường la mã", "Đấu trường la mã", "Đấu trường la mã", "Đấu trường la mã", "Đấu trường la mã", "Đấu trường la mã", "Đấu trường la mã"},
            {"Ngục tối", "", "", "", ""},
            {"Biên giới", "", "", "", ""},
            {"Khu mỏ", "", "", "", ""},
            {"Dương đông", "Đông Dương đông", "Tây Dương đông", "Nam Dương đông", "Bắc Dương đông"},
            {"Lâm tây", "Đông Lâm tây", "Tây Lâm tây", "Nam Lâm tây", "Bắc Lâm tây"},
            {"Cẩm khê", "Đông Cẩm khê", "Tây Cẩm khê", "Nam Cẩm khê", "Bắc Cẩm khê"},
            {"Đỗ đông", "Đông Đỗ đông", "Tây Đỗ đông", "Nam Đỗ đông", "Bắc Đỗ đông"},
            {"Lãnh địa đội", "", "", "", ""},
            {"Rừng già", "", "", "", ""},
            {"TYT 5x", "", "", "", ""},
            {"TYT 6x", "", "", "", ""},
            {"TYT 7x", "", "", "", ""},
            {"Chien truong", "", "", "", ""},
            {"Cánh Rừng Đông", "", "", "", ""},//28
            {"Sân Liên minh 2", "", "", "", ""},//29
            {"Sân Liên minh 3", "", "", "", ""},//30
            {"Sân Liên minh 4", "", "", "", ""},//31
            {"Sân Liên minh 5", "", "", "", ""},//32
            {"Sân Liên minh 6", "", "", "", ""},//33
            {"Sân Liên minh 7", "", "", "", ""},//34
            {"Sân Liên minh 8", "", "", "", ""},//35
            {"Sân Liên minh 9", "", "", "", ""},//36
            {"Sân Liên minh 10", "", "", "", ""},//37
            {"Chiến trường", "", "", "", ""},//38
            {"Cân núi", "", "", "", ""},//39
            {"Núi kho báu", "", "", "", ""},//40
            {"Sàn đấu", "", "", "", ""},//41
            {"Sảnh chờ", "", "", "", ""},//42
        };
        Map.idArrMap = new short[]{0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 22, 23, 24, 25, 26, 27, 28, 29, 30, 31, 32, 33, 34, 35, 36, 37, 38, 39, 40, 41, 42, 43, 44, 45, 46, 47, 48, 49, 50, 51, 52, 53, 54, 55, 56, 57, 58};
        Map.mapID = new short[][]{
            {0, 301, 302, 303, 304},//0
            {1, 321, 322, 323, 324},//1
            {2, 341, 342, 343, 344},//2
            {3, 361, 362, 363, 364},//3
            {4, 381, 382, 383, 384},//4
            {5, 401, 402, 403, 404},//5
            {6, 421, 422, 423, 424},//6
            {7, 441, 442, 443, 444},//7
            {8, 461, 462, 463, 464},//8
            {9, 481, 482, 483, 484},//9
            {10, 501, 502, 503, 504},//10
            {11, 521, 522, 523, 524},//11
            {12, 541, 542, 543, 544},//12
            {106, 2421, 2422, 2423, 2424},//13
            {110, 2501, 2502, 2503, 2504},//14
            {107, 2441, 2442, 2443, 2444},//15
            {112, 2541, 2542, 2543, 2544},//16
            {111, 2521, 2522, 2523, 2524},//17
            {202, 202, 202, 202, 202},//18
            {201, 201, 201, 201, 201},//19
            {13, 561, 562, 563, 564},//20
            {14, 581, 582, 583, 584},//21
            {15, 601, 602, 603, 604},//22
            {113, 2561, 2562, 2563, 2564},//23
            {114, 2581, 2582, 2583, 2584},//24
            {19, 681, 682, 683, 684},//25
            {18, 661, 662, 663, 664},//26
            {20, 701, 702, 703, 704},//27
            {28, 861, 862, 863, 864},//map28
            {22, 741, 742, 743, 744},
            {203, 4361, 4362, 4363, 4364, 4365, 4366},
            {117, 117, 117, 117, 117},
            {118, 118, 118, 118, 118},
            {17, 17, 17, 17, 17},
            {70, 1701, 1702, 1703, 1704},
            {80, 1901, 1902, 1903, 1904},
            {23, 761, 762, 763, 764},
            {24, 781, 782, 783, 784},
            {206, 206, 206, 206, 206},
            {26, 821, 822, 823, 824},
            {208, 208, 208, 208, 208},
            {209, 209, 209, 209, 209},
            {210, 210, 210, 210, 210},
            {27, 27, 27, 27, 27},
            {30, 30, 30, 30, 30},
            {31, 31, 31, 31, 31},
            {32, 32, 32, 32, 32},
            {33, 33, 33, 33, 33},
            {34, 34, 34, 34, 34},
            {35, 35, 35, 35, 35},
            {36, 36, 36, 36, 36},
            {37, 37, 37, 37, 37},
            {38, 38, 38, 38, 38},
            {39, 39, 39, 39, 39},
            {40, 40, 40, 40, 40},
            {41, 41, 41, 41, 41},
            {42, 42, 42, 42, 42},
            {227, 227, 227, 227, 227},
            {228, 228, 228, 228, 228}};

        FileInputStream fis;
        try {
            short[] id = new short[]{28};

            for (short i = 0; i < id.length; ++i) {
                try {
                    fis = null;
                    DataInputStream dis = null;
                    fis = new FileInputStream("cMap/" + id[i]);
                    dis = new DataInputStream(fis);
                    byte[] byteMap = new byte[dis.available()];
                    dis.read(byteMap, 0, byteMap.length);

                    try {
                        dis.close();
                    } catch (Exception var22) {
                    }

                    try {
                        fis.close();
                    } catch (Exception var21) {
                    }

                    idMapSend2Client.put(id[i], byteMap);
                } catch (Exception var23) {
                    var23.printStackTrace();
                }
            }
        } catch (Exception var25) {
            var25.printStackTrace();
        }
        // Vung Hoenn: noi hang vao cac bang tren (sau khi da gan lai) + loi vao o cong bac map 9.
        VungHoenn.dangKyBang();
        // Ten ban do: ghi de ten kiem hiep goc o tren bang ten trong SQL data_map_ten (P6, real.TenBanDoMayChu).
        TenBanDoMayChu.batDau();
        KhoaBanDoGame.batDau(); // khoa ban do theo cap / cua ai (data_ban_do_mo_khoa)
    }

    public void initData() {
        this.initMapHTBB();
        System.out.println("KHOI TAO XONG ");
        Map.loadAllLocaltionMap();
        commandHandler.put(1, new LoginHandler());
        // Lenh 39 = DANG KY TRONG GAME (jar/Android/PC), cung quy tac + han muc gop voi web
        // (HTBB-Web/shared/data/quy-tac-dang-ky.json), co cau do chong bot. Tat bang
        // server.ini sv.dangKyTrongGame=0. KHONG dung lai real.RequestRegisterHandler cu cua
        // lib/htbb-core.jar (khong han muc, khong dau vet). Xem real.cmd.DangKyTrongGame.
        commandHandler.put(39, new real.cmd.DangKyTrongGame());
        commandHandler.put(23, new RequestNPCInfoHandler());
        commandHandler.put(14, new CreateCharHandler());
        commandHandler.put(13, new SelectCharHandler());
        commandHandler.put(5, new RequestCharInfoHandler());
        commandHandler.put(11, new PingHandler());
        commandHandler.put(47, new AdminHandler());
        commandHandler.put(-64, new QuestHandler());
        commandHandler.put(-58, new SellItemHandler());
        commandHandler.put(100, new RequestTemplateInfo());
        Map.loadBoss(0);
        Map.loadBoss(1);
        Map.loadBoss(2); // Trung Thu 2026 - Boss Truong Giang (real/BossTrungThu.java)
        Map.loadGhost();
    }

    public RealController() {
        super(2);
        intance = this;
        this.idGen = new IDGen(32000);
        this.idGen.setMaxCatalory(7, new int[]{0, 1, 3, 4, 5, 6, 7}, new int[]{32000, 32000, 32000, 32000, 32000, 32000, 32000});
    }

    public static ICommandHandler getHandler(int cmd) {
        return (ICommandHandler) commandHandler.get(cmd);
    }

    public static ICommandHandler getHandler() {
        return (ICommandHandler) commandHandler.get(47);
    }

    public void processMessage(Session session, Message message) {
        while (true) {
            try {
                if (savingChar) {
                    try {
                        Thread.sleep(100L);
                    } catch (InterruptedException var7) {
                    }
                    continue;
                }

                ICommandHandler handler = (ICommandHandler) commandHandler.get(message.command);
                if (handler != null) {
                    handler.process(session, message);
                } else {
                    Char p = CharManager.instance.getByUserID(session.userID);
                    if (message.command == -47) {
                        DataGame.processMessage(p, message, session);
                    } else if (message.command == -48) {
                        DataGame.sendMsgCloth2Player(session, message);
                    } else if (message.command == -49) {
                        DataGame.sendMsgGetDataEff(session, message);
                    } else if (message.command == -51) {
                        DataGame.sendByteImg(session, message);
                    } else if (message.command == 127) {
                        DataGame.sendInfoCCU(session);
                    } else if (p != null && p.userID > 0 && p.isBot == -1 && p.map != null) {
                        p.map.notifyMap();
                        if (message.command == 4) {
                            if (!Map.testMove) {
                                p.doPlayerMove(message);
                            } else {
                                p.map.addPlayerMessage(p, message);
                            }
                        } else if (this.privateMessage((byte) message.command)) {

                            switch (message.command) {
                                case -45:
                                    doCuoiThu.doCuoiThu(p, message);

                                    break;
                                case 85:
                                    doBuySpecialItem.doBuySpecialItem(p, message);
                                    break;
                                case 22:
                                    // Tu dong danh: nhip uong thuoc nguoi that luc dang tu dong (TuDongGame)
                                    if (!TuDongGame.kiemLenh(p, 22)) {
                                        break;
                                    }
                                    HanhDongVatPham.boc(p, HanhDongVatPham.DUNG, () -> DungVatPham.dungThuoc(null, p, message)); // -> doUsePotion.doUsePotion, co phan hoi
                                    break;
                                case -30:// doMenu(player, message);
                                    int idNpc = message.dis.readShort();
                                    int idMenu = message.dis.readUnsignedByte();
                                    int idOptionMenu = message.dis.readByte();
                                    boolean defHandle = false;
                                    // TODO hàm gọi xem Menu NPC
                                    // Logger2.DebugLogic("do Menu : idNpc: %s, idMenu: %s, idOptionMenu: %s".formatted(idNpc, idMenu, idOptionMenu));
                                    switch (idNpc) {

                                        case -65:

                                            if (idOptionMenu == 3) {
                                                // 5 nến
                                                p.sendMessage(MessageCreator.createMsgInputText(-65, idMenu, "Số lượng cần mua", 0));
                                                return;
                                            }
                                            if (idOptionMenu == 4) {
                                                if (idOptionMenu == 4) {
                                                    if (p.potions[136] < 20) {
                                                        p.sendMessage(MessageCreator.createServerAlertMessage("Không đủ nến.", ""));
                                                    } else if (p.isFullInventory()) {
                                                        p.sendMessage(MessageCreator.createServerAlertMessage("Hành trang đã đầy.", ""));
                                                    // truLuongNeuDu (kiem-va-tru nguyen tu) THAY the cap "getLuong() < 10000"
                                                    // + subLuong o cuoi: gói -30 nay chay tren luong phien, mot lenh tieu
                                                    // luong khac (vd -76 mua shop) chay tren luong ban do co the xen vao
                                                    // giua - ban cu tru nen SAU khi da cap vu khi, subLuong lai chan ve 0
                                                    // ma khong bao loi nen nhan duoc ca hai ma chi tra mot lan.
                                                    } else if (!p.truLuongNeuDu(10000L)) {
                                                        p.sendMessage(MessageCreator.createServerAlertMessage("Không đủ lượng.", ""));
                                                    } else {
                                                        p.potions[136] = p.potions[136] - 20;
                                                        p.createNguyetLinhTruong(p, 0, 10080, (Item) null, true, true, 1, false);
                                                        String info = "Chúc mừng bạn nhận được Gậy Ánh Trăng 7 ngày";
                                                        p.sendMessage(MessageCreator.createMsgChat(p.id, info));
                                                        p.sendMessage(MessageCreator.createCharInventoryMessage(p, 0));
                                                        Database.instance.saveLogThongKeChiTieu("doiNLT", 0, 1, 10000L, "luong");
                                                        Database.instance.saveOrtherLog("", p.charname, "doi NLT 20 nen 10000 luong", "doiNLT");

                                                    }
                                                    return;
                                                }
                                            }

                                            if (!Char.isFinishAllSuKienTrungThul2016()) {
                                                doDotDen(p, idOptionMenu);
                                            }
                                            break;

                                        case -800:

                                            if (idMenu < 0 || idMenu >= p.animal.size()) {
                                                return;
                                            }
                                            if (idOptionMenu != 0) {
                                                if (idOptionMenu == 1) {
                                                    if (!p.map.isMapOffline) {
                                                        Animal anim = (Animal) p.animal.get(idMenu);
                                                        if (!anim.isAnimalCanTang() || p.map.isMapOffline || anim.place == 1) {
                                                            return;
                                                        }

                                                        p.thuChoTang = anim; // real.QuanLyThuGame: khong tha con dang cho tang
                                                        p.sendMessage(MessageCreator.createMsgInputText(-32048, idMenu, "Tên người nhận", 0));
                                                    } else {
                                                        Animal anim = (Animal) p.animal.get(idMenu);
                                                        if (!anim.isPoro() && !anim.isLan() && !anim.isAnimalCanTang() || !p.map.isMapOffline || anim.place == 1) {
                                                            return;
                                                        }

                                                        p.animal.remove(idMenu);
                                                        p.sendMessage(MessageCreator.createCharInventoryMessage(p, 2));
                                                        Database.instance.delAnimal(anim.dbid);
                                                        Database.instance.saveOrtherLog("", p.charname, "ban pet " + anim.getAttribute() + " > " + anim.getName() + " > " + anim.idImg, "sellanimal");
                                                    }
                                                }
                                            } else {
                                                Animal anim = (Animal) p.animal.get(idMenu);
                                                p.map.doCuoiThu(p, 0, anim.id);
                                            }
                                            break;

                                        case -50:
                                            CharThiDau c;
                                            String[] ld;
                                            int win;
                                            int index;
                                            // Muc "Ho Tro" (xoa pet / xoa thu cuoi) o npc tong tieu dau.
                                            // PHAI xu ly ngay tai day: npc -50 co case rieng trong switch nay
                                            // va khong bao gio set defHandle = true, nen Map.doMenu KHONG he
                                            // nhan duoc npc -50 -> nhanh Ho Tro dat trong Map.doMenu la code chet
                                            // (do la ly do bam Ho Tro khong hien thi gi).
                                            // idMenu 10 = menu 6 muc (Ho Tro o vi tri 5),
                                            // idMenu 0  = menu 7 muc khi con luot van tieu (Ho Tro o vi tri 6).
                                            if ((idMenu == 10 && idOptionMenu == 5) || (idMenu == 0 && idOptionMenu == 6)) {
                                                p.map.showMenuHoTro(p);
                                                return;
                                            }
                                            if (idMenu != 0) {
                                                if (idMenu != 10) {
                                                    if (idMenu != 11) {
                                                        if (idMenu == 12) {
                                                            if (idOptionMenu < 0 || idOptionMenu >= MapLoiDai.listMatch.size()) {
                                                                return;
                                                            }
                                                            MatchLoiDai match = (MatchLoiDai) MapLoiDai.listMatch.get(idOptionMenu);
                                                            p.idRegionLoidai = match.idRegion;
                                                            p.map.move2Map(p, 6, 10, Map.idMapLoiDai, 0);
                                                        }
                                                    } else {
                                                        ld = p.getAllNamDanhHieu();
                                                        if (ld.length <= 1) {
                                                            p.doOffDanhHieu();
                                                        } else if (idOptionMenu != ld.length - 1) {
                                                            p.doSelectDanhHieu(idOptionMenu);
                                                        } else {
                                                            p.doOffDanhHieu();
                                                        }
                                                    }
                                                } else if (idOptionMenu != 0) {
                                                    if (idOptionMenu != 1) {
                                                        if (idOptionMenu != 2) {
                                                            if (idOptionMenu != 3) {
                                                                if (idOptionMenu == 4) {
                                                                    if (MapLoiDai.listMatch.size() == 0) {
                                                                        p.sendMessage(MessageCreator.createMsgChat(p.id, "Các trận sàn đấu chưa bắt đầu"));
                                                                        return;
                                                                    }

                                                                    ld = new String[MapLoiDai.listMatch.size()];

                                                                    for (win = 0; win < MapLoiDai.listMatch.size(); ++win) {
                                                                        ld[win] = ((MatchLoiDai) MapLoiDai.listMatch.get(win)).getName();
                                                                    }

                                                                    p.sendMessage(MessageCreator.createMsgMenu((String[]) ld, idNpc, 12));
                                                                }
                                                            } else {
                                                                if (p.nhomThidau == -1) {
                                                                    p.sendMessage(MessageCreator.createMsgChat(p.id, "Không thể vào sảnh chờ khi chưa đăng ký"));
                                                                    return;
                                                                }

                                                                index = Map.r.nextInt(MapLoiDai.pos_sanh_cho.length / 2);
                                                                p.map.move2Map(p, MapLoiDai.pos_sanh_cho[index * 2], MapLoiDai.pos_sanh_cho[index * 2 + 1], Map.idMapChoLoiDai, 0);
                                                            }
                                                        } else {
                                                            if (!MapLoiDai.isRunLoiDai()) {
                                                                p.sendMessage(MessageCreator.createMsgChat(p.id, "Không thể đăng ký khi sàn đấu chưa mở."));
                                                                return;
                                                            }

                                                            if (p.lvDetail.lv < 40) {
                                                                p.sendMessage(MessageCreator.createMsgChat(p.id, "Hãy chăm chỉ tập luyện đạt đến cấp độ 40 rồi đến gặp ta nhé."));
                                                                return;
                                                            }

                                                            index = p.lvDetail.lv;
                                                            win = index / 10 - 4;
                                                            if (p.nhomThidau > -1) {
                                                                win = p.nhomThidau;
                                                            }

                                                            if (((Hashtable) Map.ALL_CHAR_LOI_DAI.get(win)).get(p.charDBID) != null) {
                                                                if (p.nhomThidau == -1) {
                                                                    p.nhomThidau = (byte) win;
                                                                }

                                                                p.sendMessage(MessageCreator.createMsgChat(p.id, "Đã đăng ký tham gia rồi thì hãy chăm chỉ tập luyện để có thể giành được chức vô địch làm rạng danh cho dòng Pokémon của mình nhé"));
                                                                return;
                                                            }

                                                            p.nhomThidau = (byte) win;
                                                            c = new CharThiDau(p.charname, p.lvDetail.lv, p.charDBID);
                                                            c.nhom = win;
                                                            ((Hashtable) Map.ALL_CHAR_LOI_DAI.get(win)).put(p.charDBID, c);
                                                            p.sendMessage(MessageCreator.createMsgChat(p.id, "Đăng ký thành công.Hãy chăm chỉ tập luyện để có thể giành được chức vô địch làm rạng danh cho dòng Pokémon của mình nhé."));
                                                            Database.instance.doAddCharThachDau(c, win, true);
                                                            Database.instance.saveOrtherLog("", p.charname, "dang ky loi dai", "regloidai");
                                                        }
                                                    } else {
                                                        ld = p.getAllNamDanhHieu();
                                                        p.sendMessage(MessageCreator.createMsgMenu((String[]) ld, idNpc, 11));
                                                    }
                                                } else {
                                                    if (p.rideHorse <= 0) {
                                                        p.sendMessage(MessageCreator.createServerAlertMessage("Bạn đang không cưỡi Pokémon nào.", ""));
                                                    } else {
                                                        doXuongThuCuoi.doXuongThuCuoi(p, 0);
                                                    }
                                                }
                                            } else if (idOptionMenu != 0) {
                                                if (idOptionMenu != 1) {
                                                    if (idOptionMenu != 2) {
                                                        if (idOptionMenu != 3) {
                                                            if (idOptionMenu != 4) {
                                                                if (idOptionMenu == 5) {
                                                                    if (MapLoiDai.listMatch.size() == 0) {
                                                                        p.sendMessage(MessageCreator.createMsgChat(p.id, "Các trận sàn đấu chưa bắt đầu"));
                                                                        return;
                                                                    }

                                                                    ld = new String[MapLoiDai.listMatch.size()];

                                                                    for (win = 0; win < MapLoiDai.listMatch.size(); ++win) {
                                                                        ld[win] = ((MatchLoiDai) MapLoiDai.listMatch.get(win)).getName();
                                                                    }

                                                                    p.sendMessage(MessageCreator.createMsgMenu((String[]) ld, idNpc, 12));
                                                                }
                                                            } else {
                                                                if (p.nhomThidau == -1) {
                                                                    p.sendMessage(MessageCreator.createMsgChat(p.id, "Không thể vào sảnh chờ khi chưa đăng ký"));
                                                                    return;
                                                                }

                                                                index = Map.r.nextInt(MapLoiDai.pos_sanh_cho.length / 2);
                                                                p.map.move2Map(p, MapLoiDai.pos_sanh_cho[index * 2], MapLoiDai.pos_sanh_cho[index * 2 + 1], Map.idMapChoLoiDai, 0);
                                                            }
                                                        } else {
                                                            if (!MapLoiDai.isRunLoiDai()) {
                                                                p.sendMessage(MessageCreator.createMsgChat(p.id, "Không thể đăng ký khi sàn đấu chưa mở."));
                                                                return;
                                                            }

                                                            if (p.lvDetail.lv < 40) {
                                                                p.sendMessage(MessageCreator.createMsgChat(p.id, "Hãy chăm chỉ tập luyện đạt đến cấp độ 40 rồi đến gặp ta nhé."));
                                                                return;
                                                            }

                                                            index = p.lvDetail.lv;
                                                            win = index / 10 - 4;
                                                            if (p.nhomThidau > -1) {
                                                                win = p.nhomThidau;
                                                            }

                                                            if (((Hashtable) Map.ALL_CHAR_LOI_DAI.get(win)).get(p.charDBID) != null) {
                                                                if (p.nhomThidau == -1) {
                                                                    p.nhomThidau = (byte) win;
                                                                }

                                                                p.sendMessage(MessageCreator.createMsgChat(p.id, "Đã đăng ký tham gia rồi thì hãy chăm chỉ tập luyện để có thể giành được chức vô địch làm rạng danh cho dòng Pokémon của mình nhé"));
                                                                return;
                                                            }

                                                            p.nhomThidau = (byte) win;
                                                            c = new CharThiDau(p.charname, p.lvDetail.lv, p.charDBID);
                                                            c.nhom = win;
                                                            ((Hashtable) Map.ALL_CHAR_LOI_DAI.get(win)).put(p.charDBID, c);
                                                            p.sendMessage(MessageCreator.createMsgChat(p.id, "Đăng ký thành công.Hãy chăm chỉ tập luyện để có thể giành được chức vô địch làm rạng danh cho dòng Pokémon của mình nhé."));
                                                            Database.instance.doAddCharThachDau(c, win, true);
                                                            Database.instance.saveOrtherLog("", p.charname, "dang ky loi dai", "regloidai");
                                                        }
                                                    } else {
                                                        ld = p.getAllNamDanhHieu();
                                                        p.sendMessage(MessageCreator.createMsgMenu((String[]) ld, idNpc, 11));
                                                    }
                                                } else {
                                                    if (p.rideHorse <= 0) {
                                                        p.sendMessage(MessageCreator.createServerAlertMessage("Bạn đang không cưỡi Pokémon nào.", ""));
                                                    } else {
                                                        doXuongThuCuoi.doXuongThuCuoi(p, 0);
                                                    }
                                                }
                                            } else if (p.map.mapIDLoadMap != 0 || p.map.mapIDLoadMap != 70) {
                                                doMenuNpcNhanTieu(p, idNpc, idMenu, idOptionMenu);
                                            }

                                            break;
                                        case -51:
                                            TraHang.excecute(p);
                                            break;
                                        case -49: // NPC su kien: cung thu tu uu tien voi menu (real.SuKienNpc)
                                            SuKienNpc.xuLy(p.map, p, idNpc, idMenu, idOptionMenu);
                                            break;
                                        case -8:
                                            if (!Map.isMapThanh(p.map) && !Map.isMapLang(p.map)) {
                                                return;
                                            }
                                            // Menu goc da bo "Hoat dong gio" (vi tri 4, chi bao "Sap ra mat"): hien thi -> goc.
                                            if (idMenu == 0 && idOptionMenu >= 4 && !TeamServer.isServerLienDau()) {
                                                idOptionMenu++;
                                            }
                                            this.doCreateItem(p, idNpc, idMenu, idOptionMenu);
                                            break;
                                        default:
                                            defHandle = true;
                                            break;

                                    }

                                    if (defHandle) {
                                        byte[] data;
                                        try (ByteArrayOutputStream bos = new ByteArrayOutputStream(); DataOutputStream dos = new DataOutputStream(bos)) {
                                            dos.writeShort(idNpc);
                                            dos.writeByte(idMenu);
                                            dos.writeByte(idOptionMenu);
                                            dos.flush();
                                            bos.flush();
                                            data = bos.toByteArray();
                                        } catch (Exception e) {
                                            data = null;
                                        }
                                        if (data != null) {
                                            p.map.processMessage(p, new io.Message(-30, data));
                                        }
                                    }

                                    break;
                                case -31: // doProcessInputText()
                                    int idActor = message.dis.readShort();
                                    int idMenuInputText = message.dis.readByte();
                                    final String textGoc = message.dis.readUTF();
                                    // The dat ten Pokemon: can chuoi GOC (chu hoa, co dau) - goi dong goi lai ben duoi ha chu thuong
                                    if (DatTenPetGame.xuLyNhap(p, idActor, textGoc)) {
                                        break;
                                    }
                                    String text = textGoc.trim().toLowerCase();
                                    // TODO hàm xử lý input

                                    //System.out.println("noi dung nhap: " + idActor + " / " +idMenuInputText+ " / " + text + " / " + p.id);
                                    if (idActor == -65) {
                                        if (p.isFullInventory()) {
                                            p.sendMessage(MessageCreator.createServerAlertMessage("Chừa hành trang ra rồi mua.", ""));
                                            return;
                                        }
                                        int quantity;
                                        try {
                                            quantity = Integer.parseInt(text);
                                        } catch (Exception exNum) {
                                            p.sendMessage(MessageCreator.createServerAlertMessage("Số lượng không hợp lệ.", ""));
                                            return;
                                        }
                                        if (quantity <= 0 || quantity > 100000) {
                                            p.sendMessage(MessageCreator.createServerAlertMessage("Số lượng không hợp lệ.", ""));
                                            return;
                                        }
                                        long giatien = quantity * 2L;

                                        // truLuongNeuDu: kiem-va-tru nguyen tu, TRU TRUOC roi moi cap nen. Gói -31
                                        // nam trong privateCmd nen chay tren luong phien; mot lenh tieu luong khac
                                        // (vd -76 mua shop) chay tren luong ban do co the xen giua "kiem" va "tru"
                                        // cu, khien ca hai deu qua duoc kiem va cung tru tren mot so luong - o day
                                        // con nang hon vi ban cu CONG nen TRUOC roi moi tru, nen subLuong chan ve 0
                                        // (het luong giua chung) van khong lam mat so nen da nhan.
                                        if (p.truLuongNeuDu(giatien)) {
                                            p.potions[136] = p.potions[136] + quantity;

                                            p.sendMessage(MessageCreator.createServerAlertMessage("Mua nến thành công " + quantity + " nến.", ""));
                                            p.sendMessage(MessageCreator.createCharInventoryMessage(p, 0));
                                        } else {
                                            p.sendMessage(MessageCreator.createServerAlertMessage("Không đủ lượng để mua.", ""));
                                        }

                                        return;
                                    }

                                    switch (idMenuInputText) {
                                        case 7:
                                            Database.instance.newDoGetGiftCode(text, p);
                                            break;
                                        default:
                                            byte[] data;
                                            try (ByteArrayOutputStream bos = new ByteArrayOutputStream(); DataOutputStream dos = new DataOutputStream(bos)) {
                                                dos.writeShort(idActor);
                                                dos.writeByte(idMenuInputText);
                                                dos.writeUTF(text);
                                                dos.flush();
                                                bos.flush();
                                                data = bos.toByteArray();
                                            } catch (Exception e) {
                                                data = null;
                                            }
                                            if (data != null) {
                                                try {
                                                    p.map.processMessage(p, new io.Message(-31, data));
                                                } catch (Exception e) {
                                                    System.err.println("loi menuinput: " + idMenuInputText);
                                                    e.printStackTrace();
                                                }
                                            }
                                            break;
                                    }
                                    break;
                                default:
                                    p.map.processMessage(p, message);
                                    break;
                            }
                        } else {
                            p.map.addPlayerMessage(p, message);
                        }
                    } else {
                        try {
                            otherHandle.process(session, message);
                        } catch (Exception var6) {
                        }
                    }
                }
            } catch (Exception var8) {
                Logger.logError(var8);
            }

            return;
        }
    }

    public void doDotDen(Char player, int idOptionMenu) {
        if (!Char.isFinishAllSuKienTrungThul2016()) {
            if (idOptionMenu <= 2) {
                // Sua 19/09/2026 (yeu cau nguoi dung): DOT den (idOptionMenu==0) tieu 1 Nen
                // dung nhu truoc; THA den (1/2) phai tieu 1 BANH NUONG (khong phai Nen nua) -
                // truoc day ca 3 lua chon deu doi hoi/tru Nen giong het nhau, sai voi spec
                // "Tha den... mat luong + 1 banh nuong".
                final boolean laThaDen = (idOptionMenu != 0);
                // Sua 19/09/2026 (yeu cau nguoi dung, spec "Thap nen se mat 3 phut"): chi ap
                // dung cooldown cho DOT den (khong anh huong Tha den, spec khong noi gi ve
                // cooldown cho Tha den).
                if (!laThaDen && System.currentTimeMillis() - player.lanDotDenGanNhat < 180000L) {
                    final long conLai = (180000L - (System.currentTimeMillis() - player.lanDotDenGanNhat)) / 1000L;
                    player.sendMessage(MessageCreator.createServerAlertMessage("Bạn cần chờ " + conLai + " giây nữa mới có thể thắp nến tiếp.", ""));
                    return;
                }
                final boolean duNguyenLieu = laThaDen ? (player.potions[125] >= 1) : (player.potions[136] >= 1);
                if (duNguyenLieu) {
                    if (idOptionMenu != 1 || player.getLuong() >= 20) {
                        if (idOptionMenu != 2 || player.getxu() >= 10000000L) {
                            // Sua 19/09/2026: 100.000 exp truoc day CONG THANG vo dieu kien moi lan
                            // dot/tha den (khong loai tru voi thuong ngau nhien), sai voi spec "100.000
                            // exp" la MOT trong cac phan thuong NGAU NHIEN - da bo dong cong thang nay,
                            // chuyen thanh 1 o ngau nhien moi (typegift -8) trong bang duoi.
                            // Trung Thu 2026: HP/MP 7k tang tu 50~100 len 200~300 (idx 0/1) theo
                            // spec moi; idx 26 (typegift -3, phi phong THOI HAN trung lap voi
                            // idx 25, dang TAT vi per=0) doi thanh "phi phong VINH VIEN" va BAT
                            // lai (per 0 -> 50); them 1 o moi idx 36 cho "Lan Su Tu thoi han"
                            // (typegift -7, xem nhanh xu ly ben duoi); them idx 37 cho "100.000 exp"
                            // (typegift -8).
                            // Them idx 38 (Thuoc hoa hinh KHOA, typegift -9, moi 19/09/2026) -
                            // spec doi day PHAI luon khoa, khac voi cach typegift 6 (Bot tien
                            // giai) dang random khoa/khong khoa 50-50.
                            int[] per = new int[]{
                                5000, 5000, 5000, 300, 100, 100, 50, 3000, 3000, 100,
                                100, 100, 100, 100, 100, 5000, 5000, 5000, 5000, 5000,
                                500, 500, 500, 500, 500, 200, 50, 50, 5000, 5000,
                                5000, 50, 50, 100, 10, 50, 30, 500, 100
                            };

                            int[] idgift = new int[]{
                                93, 95, 117, 35, 75, 84, 100, GemTemplate.BOT_TRANG, GemTemplate.BOT_XANH, GemTemplate.BOT_DO,
                                -1, -1, -1, -1, -1, -1, -1, -1, -1, -1,
                                -1, -1, -1, -1, -1, -2, -3, GemTemplate.BOT_XANH_LA, 114, 112, 113, 119,
                                -4, -5, -6, Potion.TYPE_THE_THEM_LUOT_VAN_TIEU, -7, -8, 268
                            };

                            int[] typegift = new int[]{
                                4, 4, 4, 4, 4, 4, 4, 6, 6, 6,
                                -1, -1, -1, -1, -1, -1, -1, -1, -1, -1,
                                -1, -1, -1, -1, -1, -2, -3, 6, 4, 4, 4, 4,
                                -4, -5, -6, 4, -7, -8, -9
                            };

                            int[] soluongmax = new int[]{
                                300, 300, 3, 1, 1, 1, 1, 3, 3, 3,
                                10000, 20000, 30000, 40000, 50000, 100, 200, 300, 400, 500,
                                50, 60, 70, 80, 90, 1, 1, 3, 1, 1, 1, 1,
                                1, 1, 1, 1, 1, 1, 1, 1
                            };

                            int[] soluongmin = new int[]{
                                200, 200, 1, 1, 1, 1, 1, 1, 1, 1,
                                10000, 20000, 30000, 40000, 50000, 100, 200, 300, 400, 500,
                                50, 60, 70, 80, 90, 1, 1, 1, 1, 1, 1, 1,
                                1, 1, 1, 1, 1, 1, 1
                            };
                            String info = "Chúc mừng bạn nhận được ";
                            int index = Char.getIndexRandom(per);
                            if (typegift[index] == -2) {
                                if (!player.isFullInventory()) {
                                    int[] minute = new int[]{1440, 4320, 7200, 10080};
                                    Item newItem = ItemLuckyDraw.createItemCoat(player, r.nextInt(2), false, minute[r.nextInt(minute.length)]);
                                    player.iItems.add(newItem);
                                    player.sendMessage(MessageCreator.createCharInventoryMessage(player, 1));
                                    info = info + newItem.getName() + " thời hạn " + newItem.minuteExist + " phút";
                                    player.sendMessage(MessageCreator.createMsgChat(player.id, info));
                                } else {
                                    player.addXu((long) (100 + r.nextInt(100)), "map 58");
                                    info = info + soluongmax[index] + " xu";
                                    player.sendMessage(MessageCreator.createMsgChat(player.id, info));
                                    player.sendMessage(MessageCreator.createCharInventoryMessage(player, 0));
                                }
                            } else if (typegift[index] == -3) {
                                // Trung Thu 2026: doi tu "Phi Phong thoi han" (trung voi idx 25)
                                // sang "Phi Phong VINH VIEN" (minute = -1) theo spec moi.
                                if (!player.isFullInventory()) {
                                    Item newItem = ItemLuckyDraw.createItemCoat(player, r.nextInt(2), false, -1);
                                    player.iItems.add(newItem);
                                    player.sendMessage(MessageCreator.createCharInventoryMessage(player, 1));
                                    info = info + newItem.getName() + " vĩnh viễn";
                                    player.sendMessage(MessageCreator.createMsgChat(player.id, info));
                                } else {
                                    player.addXu((long) (100 + r.nextInt(100)), "map 55");
                                    info = info + soluongmax[index] + " xu";
                                    player.sendMessage(MessageCreator.createMsgChat(player.id, info));
                                    player.sendMessage(MessageCreator.createCharInventoryMessage(player, 0));
                                }
                            } else if (typegift[index] == -7) {
                                // Trung Thu 2026: "Thu cuoi Lan Su Tu thoi han" - them moi theo spec.
                                int[] minute = new int[]{1440, 4320, 7200, 10080};
                                player.createLan((long) minute[r.nextInt(minute.length)], true, 95);
                                info = info + "Suicune";
                                player.sendMessage(MessageCreator.createMsgChat(player.id, info));
                            } else if (typegift[index] == -8) {
                                // Trung Thu 2026 (sua 19/09/2026): "100.000 exp" la MOT trong cac
                                // phan thuong ngau nhien (xem chu thich o dau doDotDen), khong con
                                // cong thang vo dieu kien nua.
                                addXpCharEvent(player, 100000L, false, "doDotDen");
                                info = info + "100.000 exp";
                                player.sendMessage(MessageCreator.createMsgChat(player.id, info));
                            } else if (typegift[index] == -5) {
                                // Phi Phong
                                if (!player.isFullInventory()) {
                                    int[] minute = new int[]{1440, 4320, 7200, 10080};
                                    Item newItem = ItemLuckyDraw.createtemCoat(742, player, r.nextInt(2), true, minute[r.nextInt(minute.length)]);
                                    player.sendMessage(MessageCreator.createCharInventoryMessage(player, 1));
                                    info = info + newItem.getName() + " thời hạn " + newItem.minuteExist + " phút";
                                    player.sendMessage(MessageCreator.createMsgChat(player.id, info));
                                } else {
                                    player.addXu((long) (100 + r.nextInt(100)), "map 55");
                                    info = info + soluongmax[index] + " xu";
                                    player.sendMessage(MessageCreator.createMsgChat(player.id, info));
                                    player.sendMessage(MessageCreator.createCharInventoryMessage(player, 0));
                                }
                            } else if (typegift[index] == -4) {
                                // Tranh
                                int soluong = soluongmin[index];
                                if (soluongmin[index] < soluongmax[index]) {
                                    soluong += r.nextInt(soluongmax[index] - soluongmin[index] + 1);
                                }

                                boolean lock = false;
                                int idgem = ID_GEM_TU_BINH[2 + r.nextInt(2)][r.nextInt(ID_GEM_TU_BINH[0].length)];
                                player.doAddGemItem(idgem, soluong, lock);
                                info = info + soluong + " " + gemTemplate[idgem].name + (lock ? " khoá" : "");
                                player.sendMessage(MessageCreator.createCharGemItem(player));
                                player.sendMessage(MessageCreator.createMsgChat(player.id, info));
                            } else if (typegift[index] == -6) {
                                // dbt
                                int time = r.nextInt(3) + 3;
                                player.createPoro((long) time, false);
                                info = info + " Wooloo";
                            } else if (typegift[index] == -1) {
                                player.addXu((long) soluongmax[index], "map 56");
                                info = info + soluongmax[index] + " xu";
                                player.sendMessage(MessageCreator.createMsgChat(player.id, info));
                                player.sendMessage(MessageCreator.createCharInventoryMessage(player, 0));
                            } else if (typegift[index] == -9) {
                                // Thuoc hoa hinh KHOA (moi 19/09/2026) - luon khoa, khac voi
                                // typegift 6 (Bot tien giai) dang random khoa/khong khoa.
                                player.doAddGemItem(idgift[index], 1, true);
                                info = info + "Thuốc hóa hình (khóa)";
                                player.sendMessage(MessageCreator.createCharGemItem(player));
                                player.sendMessage(MessageCreator.createMsgChat(player.id, info));

                                // TYPEGIFT = 4 potion - TYPEGIFT = 6 gem,
                            } else if (typegift[index] == 4) {
                                //  item Potion hp.mp-nhân sâm tùm lum
                                int soluong = soluongmin[index];
                                if (soluongmin[index] < soluongmax[index]) {
                                    soluong += r.nextInt(soluongmax[index] - soluongmin[index] + 1);
                                }

                                player.potions[idgift[index]] = player.potions[idgift[index]] + soluong;
                                info = info + soluong + " " + LoginHandler.PORTION_NAME[idgift[index]];
                                player.sendMessage(MessageCreator.createMsgChat(player.id, info));
                                player.sendMessage(MessageCreator.createCharInventoryMessage(player, 0));
                            } else if (typegift[index] == 6) {
                                /// type = 6 gem
                                int soluong = soluongmin[index];
                                if (soluongmin[index] < soluongmax[index]) {
                                    soluong += r.nextInt(soluongmax[index] - soluongmin[index] + 1);
                                }

                                boolean lock = r.nextInt(2) == 1;
                                player.doAddGemItem(idgift[index], soluong, lock);
                                info = info + soluong + " " + gemTemplate[idgift[index]].name + (lock ? " khoá" : "");
                                player.sendMessage(MessageCreator.createCharGemItem(player));
                                player.sendMessage(MessageCreator.createMsgChat(player.id, info));
                                if (idgift[index] == GemTemplate.LKD_35 || idgift[index] == GemTemplate.LKD_40) {
                                    try {
                                        sendAllCharServer(
                                                -1,
                                                MessageCreator.createServerAlertAutoOffMessage(
                                                        player.charname + " đã nhận được " + gemTemplate[idgift[index]].name + " đốt đèn"
                                                )
                                        );
                                    } catch (IOException var13) {
                                    }
                                }
                            }

                            // Sua 19/09/2026: dot den (0) tru Nen, tha den (1/2) tru Banh nuong -
                            // xem chu thich dau ham.
                            if (laThaDen) {
                                player.potions[125]--;
                                if (player.potions[125] < 0) {
                                    player.potions[125] = 0;
                                }
                            } else {
                                player.potions[136]--;
                                if (player.potions[136] < 0) {
                                    player.potions[136] = 0;
                                }
                                player.lanDotDenGanNhat = System.currentTimeMillis();
                            }

                            Database.instance
                                    .saveOrtherLog("", player.charname, info + " " + (idOptionMenu == 1 ? "dung luong " : (idOptionMenu == 2 ? "dung xu" : "")), "dotden");
                            if (idOptionMenu != 0) {
                                EffectBuff ef = new EffectBuff(EffectBuff.THA_DEN, System.currentTimeMillis() + 3000L);
                                ef.type = EffectBuff.BY_ACTOR;
                                ef.damFly = 0;
                                if (Char.isSuKienTrungThul2016()) {
                                    player.banphaohoa++;
                                    Database.instance.addCharEventTrungthu(player);
                                }

                                player.sendMessage(
                                        MessageCreator.createMsgAddDynamicNewEffect(
                                                ef.idEff, ef.timeExist, ef.type, player.id, player.x / 16, player.y / 16, player.cat, EffectBuff.ODER_PAINT[ef.idEff], ef.damFly
                                        )
                                );
                                player.sendToNearPlayer(
                                        MessageCreator.createMsgAddDynamicNewEffect(
                                                ef.idEff, ef.timeExist, ef.type, player.id, player.x / 16, player.y / 16, player.cat, EffectBuff.ODER_PAINT[ef.idEff], ef.damFly
                                        )
                                );
                            }

                            if (idOptionMenu == 1) {
                                player.subLuong(20L);
                                Database.instance.saveLogThongKeChiTieu("tha den luong", 0, 1, 20L, "luong");
                                Database.instance.saveOrtherLog("", player.charname, "tha den tra 20 luong", "thaden");
                                xuLyCanMocThaDen(player);
                            } else if (idOptionMenu == 2) {
                                player.subXu(10000000L, false, "");
                                Database.instance.saveLogThongKeChiTieu("tha den xu", 0, 1, 10000000L, "xu");
                                Database.instance.saveOrtherLog("", player.charname, "tha den tra 10000000 xu", "thaden");
                                xuLyCanMocThaDen(player);
                            }

                            player.sendMessage(MessageCreator.createCharInventoryMessage(player, 0));
                        } else {
                            player.sendMessage(MessageCreator.createServerAlertMessage("Không đủ xu để thực hiện.", ""));
                        }
                    } else {
                        player.sendMessage(MessageCreator.createServerAlertMessage("Không đủ lượng để thực hiện.", ""));
                    }
                } else {
                    player.sendMessage(MessageCreator.createServerAlertMessage(
                            laThaDen ? "Không đủ bánh nướng để thả đèn, bạn cần có ít nhất 1 bánh nướng."
                                    : "Không đủ nến để đốt đèn bạn cần có ít nhất 1 nến.", ""));
                }
            }
        }
    }

    /**
     * Trung Thu 2026: tang bo dem "can moc tha den" DUNG CHUNG TOAN SERVER (data.DatabaseCanMocThaDen -
     * khong phan biet lang/quoc gia, khong reset theo su kien bat/tat, nguoi dung xac nhan 16/09/2026).
     * Chi goi cho THA DEN THANH CONG (idOptionMenu 1/2 - co tra phi), khong tinh nhanh dot den mien phi
     * (idOptionMenu 0). Cac moc ca nhan (300/3000) duoc nhan tay tai NPC Hang Nga, khong tu dong phat o day.
     *
     * Sua 25/09/2026: them tangCaNhan() dem RIENG so lan tha den cua nhan vat nay (khong phan
     * biet tra luong/xu) - dung cho "Bang xep hang" o MapSuKien.doMenuTrungThu2016 (idOptionMenu 6),
     * truoc day sap theo tong luong tieu ca su kien (sai voi ten "top tha den", bo qua nguoi tra xu).
     */
    private void xuLyCanMocThaDen(final Char player) {
        data.DatabaseCanMocThaDen.tangCaNhan(player.charDBID);
        final long tongMoi = data.DatabaseCanMocThaDen.tangVaLayTong();
        if (tongMoi == data.DatabaseCanMocThaDen.MOC_TOAN_SERVER_X2_EXP) {
            try {
                Map.sendAllCharServer(-1, MessageCreator.createServerAlertAutoOffMessage(
                        "Toàn server đã đốt/thả đủ 10.000 lượt đèn Trung Thu! Hôm nay toàn bộ Pokémon online được X2 EXP!"));
            } catch (final java.io.IOException ex) {
            }
        }
    }

    private void doMenuNpcNhanTieu(Char player, int idNpc, int idMenu, int idOptionMenu) {
        if (!Map.openvantieu) {
            player.sendMessage(MessageCreator.createServerAlertMessage("Chức năng đang bảo trì", ""));
        } else {
            try {
                Calendar cal = Calendar.getInstance();
                int ihour = cal.get(11);
                if ((ihour < 9 || ihour >= 21) && TeamServer.server != 100) {
                    player.sendMessage(MessageCreator.createServerAlertMessage("Chỉ có thể nhận hàng từ 9h đến 21h hàng ngày", ""));
                    return;
                }

                if (player.lvDetail.lv < 40) {
                    player.sendMessage(MessageCreator.createServerAlertMessage("Chức năng chỉ dành cho Pokémon từ cấp độ 40 trở lên", ""));
                    return;
                }

                if (player.myCountry != player.inCountry) {
                    return;
                }

                if (player.map.mapIDLoadMap != 0 && player.map.mapIDLoadMap != 70 && player.map.mapIDLoadMap != 80) {
                    return;
                }

                if (idMenu == 0) {
                    Char.doInitMonter(player, idOptionMenu);
                }
            } catch (Exception var7) {
                var7.printStackTrace();
            }

        }
    }

 

    public boolean privateMessage(byte cmd) {
        return privateCmd.get(cmd) != null;
    }

    public static void checkResetTimeXP() {
        if (Map.checkRcvXP) {
            Calendar cl = Calendar.getInstance();
            int iHour = cl.get(11);
            if (iHour == 0 && !resetTime) {
                resetTime = true;
                Database.instance.updateAllTimeXP();

                for (int i = 0; i < CharManager.instance.vChars.size(); ++i) {
                    Char p = (Char) CharManager.instance.vChars.get(i);
                    p.timeXP = 0;
                }
            } else if (iHour != 0 && resetTime) {
                resetTime = false;
            }
        }

    }

    /**
     * Mot phien vua dut. CHI duoc dong den nhan vat cua DUNG phien do.
     *
     * Truoc day ham nay tra cuu nhan vat theo conn.userID roi kick luon, khong he
     * doi chieu xem nhan vat ay co thuoc phien nay khong. Khi nguoi choi bi cat roi
     * vao lai ngay (auto tu ket noi lai trong vai giay), phien CU dut sau khi phien
     * MOI da dang nhap xong: luc do getByUserID tra ve nhan vat cua phien MOI, va
     * phien cu di kick chinh nhan vat dang choi - nguoi choi bi van ra khoi game,
     * va moi thay doi ke tu lan luu truoc do bi doc bang ban chup cu.
     */
    public void onDisconnected(Session conn) {
        if (conn.userID != -1) {
            Char p = CharManager.instance.getByUserID(conn.userID);
            if (p != null && p.getSession() == conn) {
                CharManager.instance.kickPlayer(p, "onDisconnected realcontrol");
                p.exit = true;
            }
        }

    }

    public void saveAllChar(boolean shutdownServer) {
        savingChar = true;
        Collection<Map> maps = mapList.values();
        boolean ready = false;
        System.out.println("savechar ");
        Vector<Char> allchar = (Vector) CharManager.instance.vChars.clone();

        for (int i = 0; i < allchar.size(); ++i) {
            try {
                Char c = (Char) allchar.elementAt(i);
                if (c.isBot == -1) {
                    Database.instance.saveChar(c, true);
                    Database.instance.saveCharQuest(c.charDBID, c.char_quest);
                   
                    Database.instance.saveLogActiveUser(c.charname + "_logout", String.valueOf(c.lvDetail.lv));
                    String[] info = c.getListGem();
                                      info = c.getInfoSaveTubinh();
                    
                    UserLogger.flushToFile(c.charname);
                }
            } catch (Exception var8) {
            }
        }

        System.out.println("SAVE XONG");
    }

    private void doCreateItem(Char player, int idNpc, int idMenu, int idOptionMenu) {
        try {
            if (idMenu == 0) {

                switch (idOptionMenu) {
                    case 5:
                        String text = "Đã ";
                        if (player.isTatPhiPhong()) {
                            player.setBatPhiPhong();
                            text += "bật";
                        } else {
                            text += "tắt";
                            player.setTatPhiPhong();
                        }
                        text += " ruy băng thành công";
                        try {
                            player.sendMessage(MessageCreator.createMainCharInfoMessage(player));
                            player.sendToNearPlayer(MessageCreator.createCharInfo(player));
                            player.sendMessage(MessageCreator.createAnimalWearingMessage(player, player));
                            MessageCreator.createMsgCharMonster(player, player);
                        } catch (Exception var7) {
                            var7.printStackTrace();
                        }
                        player.sendMessage(MessageCreator.createServerAlertMessage(text, ""));
                        return;

                    case 6:
                        text = "Đã ";
                        if (player.isTatMatNa()) {
                            player.setBatMatNa();
                            text += "bật";
                        } else {
                            text += "tắt";
                            player.setTatMatNa();
                        }
                        text += " mặt nạ thành công";
                        try {
                            player.sendMessage(MessageCreator.createMainCharInfoMessage(player));
                            player.sendToNearPlayer(MessageCreator.createCharInfo(player));
                            player.sendMessage(MessageCreator.createAnimalWearingMessage(player, player));
                            // Nut nay doi ca Char.getIdHatMatNa(), tuc doi ca so non
                            // trong getIDModel(). Ma goi 3 (createMainCharInfoMessage)
                            // chi mang lop phu tren ban do - getIdHeadDynamic/Body/Leg -
                            // KHONG mang idModel. Thieu goi 15 nay thi ban do an mat na
                            // roi ma o xem truoc cua so Trang bi van giu mat na cu, mai
                            // toi luc co viec khac day lai goi 15 moi khop.
                            // Cac nut ben canh (phi phong, canh) khong can vi chung chi
                            // doi getIdBigPhiPhong, thu von da nam trong goi 3.
                            player.sendMessage(MessageCreator.createCharWearingMessage(player, player));
                            MessageCreator.createMsgCharMonster(player, player);
                        } catch (Exception var7) {
                            var7.printStackTrace();
                        }
                        player.sendMessage(MessageCreator.createServerAlertMessage(text, ""));
                        return;
                     case 7:
                        if (player.isTatCanh()) {
                            player.setBatCanh();
                            text = "bật";
                        } else {
                            text = "tắt";
                            player.setTatCanh();
                        }
                        text += " cánh thành công";
                        try {
                            player.sendMessage(MessageCreator.createMainCharInfoMessage(player));
                            player.sendToNearPlayer(MessageCreator.createCharInfo(player));
                            player.sendMessage(MessageCreator.createAnimalWearingMessage(player, player));
                            MessageCreator.createMsgCharMonster(player, player);
                        } catch (Exception var7) {
                            var7.printStackTrace();
                        }
                        player.sendMessage(MessageCreator.createServerAlertMessage(text, ""));
                        return;
                        // Doi ten nhan vat (19/09/2026) - chen giua menu (sau "canh", truoc "Tinh
                        // Luyen Than Binh") theo yeu cau nguoi dung, phai khop vi tri trong mang
                        // menu (RequestNPCInfoHandler case -8).
                        case 8:
                        player.sendMessage(MessageCreator.createMsgInputText(
                                CharDoiTenNhanVat.INPUT_DOI_TEN, 0,
                                "Nhập tên mới (phí " + com.htbb.pokemon.NapTienGame.giaDoiTen(CharDoiTenNhanVat.COST_LUONG) + " lượng)", 0)); // nap tien: gia cua hang
                        return;
                        case 9:
                        if (!player.checkVuKhiThanBinh()) {
                            // Sua 20/09/2026: thong bao cu cua nhanh nay ("khong can gia han")
                            // copy nham tu case 10 (Gia Han) - day la case Tinh Luyen, dung sai
                            // ngu canh. Ngoai ra khoi kiem tra GiaHan>=100 ngay duoi da bi xoa:
                            // checkVuKhiThanBinh() da chan het truong hop TinhLuyen==100 (luc do
                            // tempateID nhay sang dai 773-787 nen checkVuKhiThanBinh() tra ve
                            // false), nen TinhLuyen luon < 100 tai day - kiem GiaHan (0..5) voi
                            // nguong 100 la kiem SAI TRUONG va khong bao gio dung ca.
                            player.sendMessage(MessageCreator.createServerAlertMessage(
                                    player.daTinhLuyenToiDa()
                                            ? "Bảo vật cầm tay này đã tinh luyện tối đa 100%, không thể tinh luyện thêm."
                                            : "Bạn phải mặc bảo vật cầm tay mới có thể tinh luyện", ""));
                            return;
                        }
                        if (player.getLuong() <1) {
                            player.sendMessage(MessageCreator.createServerAlertMessage("Không đủ 1 lượng để tinh luyện", ""));
                            return;
                        }
                        if (player.getxu() < 3000000) {
                            player.sendMessage(MessageCreator.createServerAlertMessage("Bạn cần có 3.000.000 Xu để tinh luyện", ""));
                            return;
                        }
                    
                        if(!player.CheckNguyenLieuTL()){
                            player.sendMessage(MessageCreator.createServerAlertMessage("Bạn còn thiếu nguyên liệu: " + player.getNguyenLieuTL(), ""));
                            return;
                        }

                        // Hien vu khi than binh dang mac truoc de nguoi choi xem chi so
                        // hien tai, thay vi nhay thang vao o chon nguyen lieu nhu truoc day.
                        // Bam "Tiep tuc" o shop nay (type 7) moi thuc su mo danh sach
                        // nguyen lieu that - xem Map.doBuyItemShopNew, nhanh type == 7.
                        final Vector<Item> vuKhiDangMac = new Vector<>();
                        vuKhiDangMac.addElement(player.itemVukhiThoiTrang);
                        player.sendMessage(MessageCreator.createMessageShopNew(player, vuKhiDangMac, "Tiếp tục", "Bảo Vật Cầm Tay", 7));
                        return;
                        case 10:
                        if (!player.checkVuKhiThanBinh()) {
                            player.sendMessage(MessageCreator.createServerAlertMessage("Không có vật phẩm để gia hạn", ""));
                            return;
                        }
                    
                        if (player.itemVukhiThoiTrang.GiaHan == 0) {
                            player.sendMessage(MessageCreator.createServerAlertMessage("Trang bị đã hết số lần gia hạn", ""));
                            return;
                        }
                    
                        if(player.itemVukhiThoiTrang.minuteExist <= 0){
                            player.sendMessage(MessageCreator.createServerAlertMessage("Vật phẩm vĩnh viễn không cần gia hạn", ""));
                            return;
                        }
                    
                        // Tính số lần gia hạn đã dùng
                        int lanGiaHan = 6 - player.itemVukhiThoiTrang.GiaHan;
                        int giaLuong = 0;
                        switch (lanGiaHan) {
                            case 1: giaLuong = 5000; break;
                            case 2: giaLuong = 10000; break;
                            case 3: giaLuong = 15000; break;
                            case 4: giaLuong = 20000; break;
                            case 5: giaLuong = 25000; break;
                            default: giaLuong = 5000; break;
                        }
                    
                        if (player.getLuong() < giaLuong) {
                            player.sendMessage(MessageCreator.createServerAlertMessage("Không đủ " + giaLuong + " lượng để gia hạn", ""));
                            return;
                        }
                    
                        long time = player.itemVukhiThoiTrang.minuteExist;
                        String[] timeInfo = Char.split(Char.getDayTimeByTime(player.itemVukhiThoiTrang.timeLoan + time * 60000L), " ");
                    
                        player.sendMessage(MessageCreator.createMsgPopUp(
                            player.id,
                            123,
                            "Bạn có muốn gia hạn đồ cầm tay " + player.itemVukhiThoiTrang.getTemplate().name +
                            " đang có thời hạn " + timeInfo[0] +
                            " thêm 7 ngày với giá " + giaLuong + " lượng không ?"
                        ));
                        return;  
                    // "Go skin thu" (them 11/09/2026, doi cho voi Than Khi 11/09/2026) - muc
                    // rieng dat NGAY TRUOC "Than Khi" trong menu chinh NPC Tho Ren Than Bi,
                    // bam la go luon, khong mo submenu. Goi lai dung ham dung cho lenh chat
                    // "boskin": tu no da tu kiem khong co skin dang mac / hanh trang day va
                    // bao loi phu hop.
                    case 15:
                        SkinThuCuoi.thaoSkin(player);
                        return;

                    // Than Khi doi xuong cuoi menu (05/09/2026), roi lui xuong case 15 khi
                    // "Go skin thu" chen vao truoc no (11/09/2026), roi lui tiep xuong case 16
                    // khi "Doi ten nhan vat" chen vao giua menu (19/09/2026) - case nay phai
                    // khop vi tri trong mang menu (RequestNPCInfoHandler case -8).
                    case 16:
                        String[] menu = new String[]
                        {
                            "Tạo", "Nâng cấp", "Đổi dòng", "Tháo"
                        };

                        player.sendMessage(MessageCreator.createMsgMenu(menu, -517, 0));

                        return;
                        // cải tạo cải trang
                        case 11:
                        // Cai Tao Cai Trang: mo thang danh sach bo thoi trang cai tao duoc (bo
                        // dang mac + hanh trang). Luat, gia theo nhom, ti le: xem real/CaiTaoCaiTrang.
                        // Submenu "tinh hoa bao dam" (idMenu 90) da bo ngay 02/09/2026.
                        player.map.showCaiTaoMaterialList(player);
                        return;
                   case 12:
                            menu = new String[]{"Kho Mảnh", "Bộ Sưu Tập", "Cải Trang", player.isBatCaiTrang() ? "Tắt Cải Trang" : "Bật Cải Trang"};
                            player.sendMessage(MessageCreator.createMsgMenu(menu, -518, 0));

                        return;

                    // Hoa hinh Pet (them 31/08/2026) - port tu ban goc khac, doi
                    // dieu kien theo pet da co san (itemPet.level>=2 = da sieu tien
                    // hoa qua doTienHoa()), dung lai item 268 (lo thuoc hoa hinh)
                    // dang dung cho hoa hinh Thu de khong phai tao vat pham moi.
                    case 13:
                        // Hoa Hinh Pet -> pet Lan (luat 02/09/2026, xem data/Pet.java): pet nuoi VINH
                        // VIEN cap n + n x 10 binh hoa hinh (item 268) = pet Lan cap n, han 10 ngay,
                        // KHONG ton luong. Hoa hinh lai luc nao cung duoc: tinh lai 10 ngay, cap Lan
                        // theo cap pet hien tai. Truoc 02/09 la len tung cap 1->5, moi cap 10 x cap
                        // binh + 5000 luong, doi pet sieu tien hoa - da bo.
                        if (player.petUsing == null || player.petUsing.itemPet == null) {
                            player.sendMessage(MessageCreator.createServerAlertMessage("Bạn cần trang bị một Pet để hoá hình", ""));
                            return;
                        }
                        if (player.petUsing.getTimeExpire() != 32000) {
                            player.sendMessage(MessageCreator.createServerAlertMessage("Chỉ hoá hình được Pet nuôi vĩnh viễn", ""));
                            return;
                        }
                        // Hinh Lan chi ve duoc cho pet DA TIEN HOA (TYPE_PET_TOOL - xem getType() va
                        // MessageCreator): pet chua tien hoa se nhan chi so ma khong doi hinh, nen chan.
                        if (player.petUsing.itemPet.level < 1) {
                            player.sendMessage(MessageCreator.createServerAlertMessage("Pet phải tiến hoá rồi mới hoá hình được", ""));
                            return;
                        }
                        final int capPetHoaHinh = player.petUsing.capPet();
                        final int soBinhHoaHinh = data.Pet.soBinhHoaHinhCan(capPetHoaHinh);
                        if (soBinhHoaHinh < 0) {
                            player.sendMessage(MessageCreator.createServerAlertMessage("Pet chưa có cấp, không hoá hình được", ""));
                            return;
                        }
                        if (player.listGemitem[268] < soBinhHoaHinh && player.listGemitemLock[268] < soBinhHoaHinh) {
                            player.sendMessage(MessageCreator.createServerAlertMessage(
                                    "Cần " + soBinhHoaHinh + " bình hoá hình (cấp " + capPetHoaHinh + " x 10) để hoá hình thành Pet Lân cấp " + capPetHoaHinh, ""));
                            return;
                        }
                        if (player.listGemitemLock[268] >= soBinhHoaHinh) {
                            player.delGem(268, soBinhHoaHinh, true);
                        } else {
                            player.delGem(268, soBinhHoaHinh, false);
                        }

                        if (!player.petUsing.isPetTool() && player.petUsing.getIdEffPet() > -1) {
                            player.huyEff(player, player.petUsing.getIdEffPet());
                        }
                        final int capHoaHinhCu = player.petUsing.capHoaHinh;
                        player.petUsing.capHoaHinh = (byte) capPetHoaHinh;
                        player.petUsing.idHoaHinhPet = 67;
                        player.petUsing.timeEndHoaHinhPet = System.currentTimeMillis() + data.Pet.HAN_HOA_HINH_MS;
                        player.petUsing.initKhangXuyenGiapWhenHoaHinh();
                        player.sendPetEff();
                        Database.instance.savePet(player.petUsing);

                        player.sendMessage(MessageCreator.createCharGemItem(player));
                        // Gui hinh Lan cho ca minh lan nguoi xung quanh - truoc day chi gui cho minh.
                        player.sendMessage(MessageCreator.createCharWearingMessage(player, player));
                        player.sendToNearPlayer(MessageCreator.createCharWearingMessage(player, player));
                        final int[] tsKxg = data.Pet.thongSoKhangXuyenGiap(capPetHoaHinh);
                        player.sendMessage(MessageCreator.createServerAlertMessage(
                                "Hoá hình thành công: Pet Lân cấp " + capPetHoaHinh + " trong 10 ngày. Kháng xuyên giáp +" + tsKxg[0]
                                + "% (tỉ lệ " + tsKxg[1] + "%, duy trì " + tsKxg[2] + "s, hồi chiêu " + tsKxg[3] + "s)", ""));
                        Database.instance.saveOrtherLog("", player.charname,
                                "hoa hinh pet Lan cap " + capPetHoaHinh + " (truoc: " + capHoaHinhCu + ") | tru " + soBinhHoaHinh + " binh 268", "hoahinhpet");
                        return;

                    // Bat/tat HIEN THI skin linh thu (them 01/09/2026) — chi an hinh anh
                    // con thu, khong dong toi chi so/noi tai da mac. Xem SkinThuCuoi.hienThi.
                    // So case doi tu 15 xuong 14 (01/09/2026): gop "Tinh Hoa Trang Phuc"
                    // (case 14 cu) vao chung case 11 "Cai Tao Cai Trang" nen xoa mot muc
                    // menu rieng, muc nay tut xuong dung mot bac trong mang menu.
                    case 14:
                        String textSkinThu = "Đã ";
                        if (player.isTatSkinLinhThu()) {
                            player.setBatSkinLinhThu();
                            textSkinThu += "bật";
                        } else {
                            textSkinThu += "tắt";
                            player.setTatSkinLinhThu();
                        }
                        textSkinThu += " hiển thị skin thú thành công";
                        try {
                            player.sendMessage(MessageCreator.createMainCharInfoMessage(player));
                            player.sendToNearPlayer(MessageCreator.createCharInfo(player));
                            if (player.animalRide != null) {
                                player.sendMessage(MessageCreator.createAnimalWearingMessage(player, player));
                            }
                        } catch (Exception var7) {
                            var7.printStackTrace();
                        }
                        player.sendMessage(MessageCreator.createServerAlertMessage(textSkinThu, ""));
                        return;

                    case 4:
                        player.sendMessage(MessageCreator.createServerAlertMessage("Sắp ra mắt.", ""));
                        return;

//                int gioHoatDong = player.runtimeCharProperties.getHourOnline();
//                String[] menu = new String[]{
//                    String.format("Hoạt động 1 giờ (%s/1)", gioHoatDong),
//                    String.format("Hoạt động 3 giờ (%s/3)", gioHoatDong),
//                    String.format("Hoạt động 5 giờ (%s/5)", gioHoatDong),
//                    String.format("Hoạt động 10 giờ (%s/10)", gioHoatDong)
//                };
//
//                player.sendMessage(MessageCreator.createMsgMenu(menu, idNpc, 100));
//                return;
                    default:
                        break;
                }

            }

            switch (idMenu) {
                case 100:
                    // thanh vien: qua online (ca he cu lan QuaHoatDong).
                    if (com.htbb.pokemon.ThanhVien.chanVaBao(player, "qua_online")) {
                        break;
                    }
                    // Yeu cau 17/10/2026: bat co sv.lau_dai -> qua hoat dong theo tai khoan (QuaHoatDong).
                    if (com.htbb.pokemon.LauDai.bat()) {
                        player.sendMessage(MessageCreator.createServerAlertMessage(QuaHoatDong.nhan(player, idOptionMenu), ""));
                        break;
                    }
                    int gioHoatDong = player.runtimeCharProperties.getHourOnline();
                    switch (idOptionMenu) {
                        case 0:
                            if (player.runtimeCharProperties.isNhanQua1h()) {
                                player.sendMessage(MessageCreator.createServerAlertMessage("Bạn đã nhận quà này rồi", ""));
                                break;
                            }
                            if (gioHoatDong < 1) {
                                player.sendMessage(MessageCreator.createServerAlertMessage("Chưa hoạt động đủ thời gian", ""));
                                break;
                            }
                            // thêm quà ?????? ok để nghĩ mai thêm :v
                            player.sendMessage(MessageCreator.createServerAlertMessage("Tính năng đang được cập nhật.", ""));
                            player.runtimeCharProperties.doNhanQuaOnline(player.charDBID, idOptionMenu);
                            break;
                        case 1:
                            if (player.runtimeCharProperties.isNhanQua3h()) {
                                player.sendMessage(MessageCreator.createServerAlertMessage("Bạn đã nhận quà này rồi", ""));
                                break;
                            }
                            if (gioHoatDong < 3) {
                                player.sendMessage(MessageCreator.createServerAlertMessage("Chưa hoạt động đủ thời gian", ""));
                                break;
                            }
                            player.sendMessage(MessageCreator.createServerAlertMessage("Tính năng đang được cập nhật.", ""));

                            player.runtimeCharProperties.doNhanQuaOnline(player.charDBID, idOptionMenu);
                            break;
                        case 2:
                            if (player.runtimeCharProperties.isNhanQua5h()) {
                                player.sendMessage(MessageCreator.createServerAlertMessage("Bạn đã nhận quà này rồi", ""));
                                break;
                            }
                            if (gioHoatDong < 5) {
                                player.sendMessage(MessageCreator.createServerAlertMessage("Chưa hoạt động đủ thời gian", ""));
                                break;
                            }
                            player.sendMessage(MessageCreator.createServerAlertMessage("Tính năng đang được cập nhật.", ""));

                            player.runtimeCharProperties.doNhanQuaOnline(player.charDBID, idOptionMenu);
                            break;
                        case 3:
                            if (player.runtimeCharProperties.isNhanQua10h()) {
                                player.sendMessage(MessageCreator.createServerAlertMessage("Bạn đã nhận quà này rồi", ""));
                                break;
                            }
                            if (gioHoatDong < 10) {
                                player.sendMessage(MessageCreator.createServerAlertMessage("Chưa hoạt động đủ thời gian", ""));
                                break;
                            }
                            player.sendMessage(MessageCreator.createServerAlertMessage("Tính năng đang được cập nhật.", ""));

                            player.runtimeCharProperties.doNhanQuaOnline(player.charDBID, idOptionMenu);
                            break;
                        default:
                            break;
                    }
                    break;
                default:
                    break;
            }
        } catch (Exception e) {
            e.printStackTrace();
            Logger2.DebugLogic("lỗi nhận quà hoạt động");
        }

        try {
            int i;
            if (idMenu == 0 && idOptionMenu == 3) {
                if (UtilHTBB.getDayOfMonth() == Char.dayEndVip && UtilHTBB.getHour() >= Char.timeEndVip) {
                    player.sendMessage(MessageCreator.createServerAlertMessage("Không thể nhận thành tích trong thời gian này", ""));
                    return;
                }

                int[] diemxai = new int[]{0, 500};
                int[] diemnap = new int[]{500, 1000};
                i = Database.instance.countCharVip(2);
                int allCaCung = Database.instance.countCharVip(1);
                int[] var10000;
                ItemShop newItem;
                if (player.vip == 0) {
                    if (player.diemxaiVip >= diemxai[1] && player.diemNapVip >= diemnap[1] && i < MAX_VIP2) {
                        player.vip = 2;
                        var10000 = player.potions;
                        var10000[100] += Char.soluong_loa_vip[player.vip];
                        player.buy_luot_van_tieu = (byte) (player.buy_luot_van_tieu + 2);
                        newItem = new ItemShop((ItemTemplates) ((Hashtable) itemTemplates.get(5)).get(ID_THE_MUA_BAN), false, 0, 0, ID_THE_MUA_BAN);
                        newItem.id = player.getIDItem();
                        newItem.owner = player.charDBID;
                        newItem.level = newItem.getTemplate().level;
                        player.iItems.add(newItem);
                        newItem.identify = player.charDBID;
                        newItem.clazz = 0;
                        newItem.isItemShop = true;
                        newItem.timeLoan = System.currentTimeMillis();
                        newItem.minuteExist = 43200;
                        newItem.dateCreate = Char.getDayTime(0L);
                        player.sendMessage(MessageCreator.createCharInventoryMessage(player, 0));
                        player.sendMessage(MessageCreator.createCharInventoryMessage(player, 1));
                        player.sendMessage(MessageCreator.createMsgChat(player.id, "Nhận được 2 loa và 1 thẻ mua bán thời hạn 30 ngày và thêm 2 lượt mua thẻ giao hàng."));
                        Database.instance.saveOrtherLog("", player.charname, "nhan loa vip va the mua ban 30 ngay va them 2 luot mua the van tieu", "loavip");
                        if (ALL_CHAR_VIP.get(player.charname) == null) {
                            Database.instance.doAddCharVip(player.charname, player.vip);
                            ALL_CHAR_VIP.put(player.charname, Integer.valueOf(player.vip));
                        } else {
                            ALL_CHAR_VIP.put(player.charname, Integer.valueOf(player.vip));
                            Database.instance.doUpdateCharVip(player.charname, player.vip);
                        }
                    } else if (player.diemxaiVip >= diemxai[0] && player.diemNapVip >= diemnap[0] && allCaCung < MAX_VIP1) {
                        player.vip = 1;
                        ++player.buy_luot_van_tieu;
                        var10000 = player.potions;
                        var10000[100] += Char.soluong_loa_vip[player.vip];
                        player.sendMessage(MessageCreator.createCharInventoryMessage(player, 0));
                        player.sendMessage(MessageCreator.createMsgChat(player.id, "Nhận được 1 loa và thêm 1 lượt mua thẻ giao hàng."));
                        Database.instance.saveOrtherLog("", player.charname, "nhan loa vip, them 1 luot mua the van tieu", "loavip");
                        if (ALL_CHAR_VIP.get(player.charname) == null) {
                            Database.instance.doAddCharVip(player.charname, player.vip);
                            ALL_CHAR_VIP.put(player.charname, Integer.valueOf(player.vip));
                        } else {
                            ALL_CHAR_VIP.put(player.charname, Integer.valueOf(player.vip));
                            Database.instance.doUpdateCharVip(player.charname, player.vip);
                        }
                    } else {
                        player.sendMessage(MessageCreator.createMsgChat(player.id, VIP[1] + " và " + VIP[2] + " đã hết slot hoặc không đủ điều kiện"));
                    }
                } else if (player.vip == 1) {
                    if (player.diemxaiVip >= diemxai[1] && player.diemNapVip >= diemnap[1] && i < MAX_VIP2) {
                        player.vip = 2;
                        player.buy_luot_van_tieu = (byte) (player.buy_luot_van_tieu + 2);
                        var10000 = player.potions;
                        var10000[100] += Char.soluong_loa_vip[player.vip];
                        newItem = new ItemShop(itemTemplates.get(5).get(ID_THE_MUA_BAN), false, 0, 0, ID_THE_MUA_BAN);
                        newItem.id = player.getIDItem();
                        newItem.owner = player.charDBID;
                        newItem.level = newItem.getTemplate().level;
                        player.iItems.add(newItem);
                        newItem.identify = player.charDBID;
                        newItem.clazz = 0;
                        newItem.isItemShop = true;
                        newItem.timeLoan = System.currentTimeMillis();
                        newItem.minuteExist = 43200;
                        newItem.dateCreate = Char.getDayTime(0L);
                        player.sendMessage(MessageCreator.createCharInventoryMessage(player, 0));
                        player.sendMessage(MessageCreator.createCharInventoryMessage(player, 1));
                        player.sendMessage(MessageCreator.createMsgChat(player.id, "Nhận được 2 loa và 1 thẻ mua bán thời hạn 30 ngày và thêm 2 lượt mua thẻ giao hàng."));
                        Database.instance.saveOrtherLog("", player.charname, "nhan loa vip va the mua ban 30 ngay, them 2 luot mua the van tieu", "loavip");
                        if (ALL_CHAR_VIP.get(player.charname) == null) {
                            Database.instance.doAddCharVip(player.charname, player.vip);
                            ALL_CHAR_VIP.put(player.charname, Integer.valueOf(player.vip));
                        } else {
                            ALL_CHAR_VIP.put(player.charname, Integer.valueOf(player.vip));
                            Database.instance.doUpdateCharVip(player.charname, player.vip);
                        }
                    } else {
                        player.sendMessage(MessageCreator.createMsgChat(player.id, VIP[2] + " đã hết slot hoặc không đủ điều kiện"));
                    }
                }

                if (player.vip > 0) {
                    player.calculateAttrib();
                    player.sendMessage(MessageCreator.createServerAlertMessage("Bạn đã đạt thành tích " + VIP[player.vip], ""));
                    player.sendMessage(MessageCreator.createMainCharInfoMessage(player));
                    player.sendToNearPlayer(MessageCreator.createCharInfo(player));
                }

                return;
            }

            if (!okChedo) {
                player.sendMessage(MessageCreator.createServerAlertMessage("Chức năng đang bảo trì.", ""));
                return;
            }

            if (player.isFullInventory()) {
                player.sendMessage(MessageCreator.createMsgChat(player.id, "Hành trang đã đầy"));
                return;
            }

            String[] menu;
            if (idMenu == 0) {
                player.infoItemCreate = null;
                if (idOptionMenu == 0) {
                    menu = new String[]{"Than lửa", "Hạt giống thần", "Khăn lụa", "Nước thần", "Nam châm"};
                    player.sendMessage(MessageCreator.createMsgMenu((String[]) menu, idNpc, 1));
                } else if (idOptionMenu == 1) {
                    menu = new String[]{"Trang bị đặc biệt", "Trang bị vật lý"};
                    player.sendMessage(MessageCreator.createMsgMenu((String[]) menu, idNpc, 3));
                } else if (idOptionMenu == 2) {
                    menu = new String[]{"Cấp độ 30", "Cấp độ 35", "Cấp độ 40", "Cấp độ 45", "Cấp độ 50", "Cấp độ 55", "Cấp độ 60", "Cấp độ 65", "Cấp độ 70", "Cấp độ 75", "Cấp độ 80", "Cấp độ 85"};
                    player.sendMessage(MessageCreator.createMsgMenu((String[]) menu, idNpc, 7));
                }
            } else {
                if (idMenu == 1) {
                    player.infoItemCreate = new InfoItemCreate();
                    String nameItem = "";
                    switch (idOptionMenu) {
                        case 0:
                            nameItem = "Than lửa cấp độ ";
                            player.infoItemCreate.charClass = 0;
                            player.infoItemCreate.typeItem = 3;
                            break;
                        case 1:
                            nameItem = "Hạt giống thần cấp độ ";
                            player.infoItemCreate.charClass = 1;
                            player.infoItemCreate.typeItem = 4;
                            break;
                        case 2:
                            nameItem = "Bút cấp độ ";
                            player.infoItemCreate.charClass = 2;
                            player.infoItemCreate.typeItem = 5;
                            break;
                        case 3:
                            nameItem = "Búa cấp độ ";
                            player.infoItemCreate.charClass = 3;
                            player.infoItemCreate.typeItem = 6;
                            break;
                        case 4:
                            nameItem = "Cung cấp độ ";
                            player.infoItemCreate.charClass = 4;
                            player.infoItemCreate.typeItem = 7;
                    }

                    if (!nameItem.equals("")) {
                        menu = new String[14];

                        for (i = 0; i < menu.length; ++i) {
                            byte[] id = CreateItemTemplate.getIDMAterial(player.infoItemCreate.typeItem, player.infoItemCreate.magic_physic, 0);
                            Vector<Byte> soluong = CreateItemTemplate.getTotalMaterial(i, 0);
                            String infoMaterial = "";

                            for (int j = 0; j < soluong.size(); ++j) {
                                infoMaterial = infoMaterial + CreateItemTemplate.nameMaterial[id[j]] + " " + soluong.get(j) + " ";
                            }

                            menu[i] = nameItem + (i * 5 + 21) + "(" + infoMaterial + ")";
                        }

                        player.sendMessage(MessageCreator.createMsgMenu((String[]) menu, idNpc, 2));
                    }
                } else if (idMenu == 2) {
                    player.infoItemCreate.level = (byte) (idOptionMenu * 5 + 21);
                    player.infoItemCreate.idOptionMenu = (short) idOptionMenu;
                    player.infoItemCreate.gender = 0;
                    player.sendMessage(MessageCreator.createMsgMenu((String[]) (new String[]{"Nguyên liệu thường", "Nguyên liệu khóa", "Nguyên liệu tổng hợp"}), idNpc, 11));
                } else if (idMenu == 3) {
                    player.infoItemCreate = new InfoItemCreate();
                    menu = new String[]{"Dòng Charmander", "Dòng Bulbasaur", "Dòng Eevee", "Dòng Squirtle", "Dòng Pichu"};
                    player.sendMessage(MessageCreator.createMsgMenu((String[]) menu, idNpc, 4));
                    player.infoItemCreate.magic_physic = (byte) idOptionMenu;
                } else if (idMenu == 4) {
                    player.infoItemCreate.charClass = (byte) idOptionMenu;
                    menu = new String[]{"Dòng Charmander", "Dòng Bulbasaur", "Dòng Eevee", "Dòng Squirtle", "Dòng Pichu"};
                    menu = new String[]{"áo " + InfoItemCreate.nameMagic_physic[player.infoItemCreate.magic_physic] + " " + menu[idOptionMenu], "Quần " + InfoItemCreate.nameMagic_physic[player.infoItemCreate.magic_physic] + " " + menu[idOptionMenu], "Nón " + InfoItemCreate.nameMagic_physic[player.infoItemCreate.magic_physic] + " " + menu[idOptionMenu], "Giày " + InfoItemCreate.nameMagic_physic[player.infoItemCreate.magic_physic] + " " + menu[idOptionMenu], "Găng " + InfoItemCreate.nameMagic_physic[player.infoItemCreate.magic_physic] + " " + menu[idOptionMenu], "Nhẫn " + InfoItemCreate.nameMagic_physic[player.infoItemCreate.magic_physic] + " " + menu[idOptionMenu], "Dây chuyền " + InfoItemCreate.nameMagic_physic[player.infoItemCreate.magic_physic] + " " + menu[idOptionMenu], "Ngọc " + InfoItemCreate.nameMagic_physic[player.infoItemCreate.magic_physic] + " " + menu[idOptionMenu]};
                    player.sendMessage(MessageCreator.createMsgMenu((String[]) menu, idNpc, 5));
                } else if (idMenu == 5) {
                    byte[] id;
                    Vector soluong;
                    String infoMaterial;
                    int j;
                    if (idOptionMenu < 3) {
                        player.infoItemCreate.typeItem = (byte) idOptionMenu;
                        menu = new String[28];

                        for (i = 0; i < menu.length; ++i) {
                            id = CreateItemTemplate.getIDMAterial(player.infoItemCreate.typeItem, player.infoItemCreate.magic_physic, 0);
                            soluong = CreateItemTemplate.getTotalMaterial(i / 2, 0);
                            infoMaterial = "";

                            for (j = 0; j < soluong.size(); ++j) {
                                infoMaterial = infoMaterial + CreateItemTemplate.nameMaterial[id[j]] + " " + soluong.get(j) + " ";
                            }

                            menu[i] = InfoItemCreate.nameAmor[idOptionMenu] + InfoItemCreate.nameMagic_physic[player.infoItemCreate.magic_physic] + " " + (i % 2 == 0 ? "Nữ" : "Nam") + " lv" + (20 + i / 2 * 5) + "(" + infoMaterial + ")";
                        }

                        player.sendMessage(MessageCreator.createMsgMenu((String[]) menu, idNpc, 6));
                    } else {
                        if (idOptionMenu < 5) {
                            player.infoItemCreate.typeItem = (byte) (idOptionMenu + 7);
                        } else if (idOptionMenu < 7) {
                            player.infoItemCreate.typeItem = (byte) (idOptionMenu + 3);
                        } else {
                            player.infoItemCreate.typeItem = 12;
                        }

                        menu = new String[14];

                        for (i = 0; i < menu.length; ++i) {
                            id = CreateItemTemplate.getIDMAterial(player.infoItemCreate.typeItem, player.infoItemCreate.magic_physic, 0);
                            soluong = CreateItemTemplate.getTotalMaterial(i, 0);
                            infoMaterial = "";

                            for (j = 0; j < soluong.size(); ++j) {
                                infoMaterial = infoMaterial + CreateItemTemplate.nameMaterial[id[j]] + " " + soluong.get(j) + " ";
                            }

                            menu[i] = InfoItemCreate.nameAmor[idOptionMenu] + InfoItemCreate.nameMagic_physic[player.infoItemCreate.magic_physic] + " " + " lv" + (20 + i * 5) + "(" + infoMaterial + ")";
                        }

                        player.sendMessage(MessageCreator.createMsgMenu((String[]) menu, idNpc, 6));
                    }
                } else if (idMenu == 6) {
                    if (player.infoItemCreate.typeItem < 3) {
                        player.infoItemCreate.gender = (byte) (idOptionMenu % 2 == 0 ? 2 : 1);
                        player.infoItemCreate.level = (byte) (idOptionMenu / 2 * 5 + 20);
                    } else {
                        player.infoItemCreate.level = (byte) (idOptionMenu * 5 + 20);
                    }

                    player.infoItemCreate.idOptionMenu = (short) (player.infoItemCreate.typeItem < 3 ? idOptionMenu / 2 : idOptionMenu);
                    player.sendMessage(MessageCreator.createMsgMenu((String[]) (new String[]{"Nguyên liệu thường", "Nguyên liệu khóa", "Nguyên liệu tổng hợp"}), idNpc, 11));
                } else if (idMenu == 7) {
                    player.itChangeColor = new ItemChangeColor(idOptionMenu);
                    menu = new String[]{"Đặc biệt", "Vật lý"};
                    player.sendMessage(MessageCreator.createMsgMenu((String[]) menu, idNpc, 8));
                } else if (idMenu == 8) {
                    player.itChangeColor.magic_physic = (byte) idOptionMenu;
                    menu = new String[]{"Trang bị xanh dương", "Trang bị đỏ", "Trang bị hoàn mỹ"};
                    player.sendMessage(MessageCreator.createMsgMenu((String[]) menu, idNpc, 9));
                } else if (idMenu == 9) {
                    if (player.itChangeColor == null) {
                        return;
                    }

                    player.itChangeColor.idColor = (byte) idOptionMenu;
                    player.sendMessage(MessageCreator.createMsgMenu((String[]) (new String[]{"nguyên liệu thường", "Nguyên liệu khóa", "Nguyên liệu tổng hợp"}), idNpc, 10));
                } else if (idMenu == 10) {
                    player.itChangeColor.materialUse = (byte) idOptionMenu;
                    ItemChangeColor.createMsgChangColorItem(player, player.itChangeColor.level, player.itChangeColor.idColor);
                } else if (idMenu == 11) {
                    player.infoItemCreate.materialUse = (byte) idOptionMenu;
                    player.sendMessage(MessageCreator.createMsgCreateItemClothing((ItemTemplates) ((Hashtable) itemTemplates.get(5)).get(new Integer(player.infoItemCreate.idTemplate = getTemplateItemCreate(player.infoItemCreate.level, player.infoItemCreate.typeItem, player.infoItemCreate.charClass, player.infoItemCreate.gender))), player.infoItemCreate.typeItem, player.infoItemCreate.idOptionMenu, player.infoItemCreate.magic_physic, player.infoItemCreate.materialUse));
                }
            }
        } catch (Exception var12) {
            var12.printStackTrace();
        }

    }

    //todo thêm đồ 85 vào cái đống idTemplateItemCreate
    private short getTemplateItemCreate(int level, int type, int charClass, int gender) {
        short[] var10000 = new short[]{400, 413, 426, 439, 452};
        var10000 = new short[]{268, 292, 316, 340, 352, 364, 376, 388};
        short[][] idTempateItemCreate = new short[][]{
            {268, 269, 270, 271, 272, 273, 274, 275, 276, 277, 278, 279, 280, 281, 282, 283, 284, 285, 286, 287, 288, 289, 290, 291, 640, 639, 642, 641}, //áo
            {292, 293, 294, 295, 296, 297, 298, 299, 300, 301, 302, 303, 304, 305, 306, 307, 308, 309, 310, 311, 312, 313, 314, 315, 644, 643, 646, 645}, //quần
            {316, 317, 318, 319, 320, 321, 322, 323, 324, 325, 326, 327, 328, 329, 330, 331, 332, 333, 334, 335, 336, 337, 338, 339, 648, 647, 650, 649},//nón
            {400, 401, 402, 403, 404, 405, 406, 407, 408, 409, 410, 411, 412, 893},//kiếm
            {413, 414, 415, 416, 417, 418, 419, 420, 421, 422, 423, 424, 425, 894},//đao
            {426, 427, 428, 429, 430, 431, 432, 433, 434, 435, 436, 437, 438, 895},//but
            {439, 440, 441, 442, 443, 444, 445, 446, 447, 448, 449, 450, 451, 896},//bua
            {452, 453, 454, 455, 456, 457, 458, 459, 460, 461, 462, 463, 464, 897},//cung
            {364, 365, 366, 367, 368, 369, 370, 371, 372, 373, 374, 375, 651, 652},//nhan
            {376, 377, 378, 379, 380, 381, 382, 383, 384, 385, 386, 387, 655, 656},//day chuyen
            {340, 341, 342, 343, 344, 345, 346, 347, 348, 349, 350, 351, 659, 660},//giay
            {352, 353, 354, 355, 356, 357, 358, 359, 360, 361, 362, 363, 663, 664},//gang
            {388, 389, 390, 391, 392, 393, 394, 395, 396, 397, 398, 399, 667, 668}//ngoc

        };
        int index = (level - 21) / 5;
        if (type < 3) {
            index = (level - 20) / 5 * 2 + (gender == 1 ? 1 : 0);
        } else if (type > 7) {
            index = (level - 20) / 5;
        }

        return index > -1 ? idTempateItemCreate[type][index] : -1;
    }

    
    
    
    
    public void onDisconnectedFromLocalPlace(Session conn) {
    }
}
