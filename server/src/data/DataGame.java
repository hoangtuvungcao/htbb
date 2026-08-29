package data;

import io.Message;
import io.Session;
import java.io.DataInputStream;
import java.io.FileInputStream;
import java.util.Iterator;
import java.util.Set;
import real.Char;
import real.Map;
import server.TeamServer;

public class DataGame {

    public static byte[][] imageMonster;
    public static byte[][][] cloth;
    public static byte[][] trees;
    public static byte[][] effData;
    public static byte[] effDataLocal;
    public static byte[][] imgEff;
    public static byte[][] imgIconTrangBi;
    public static byte[][] imgOther;
    public static byte[][] allImg;
    public static byte[][] iconClanImg;
    public static byte[][] iconSkillClanImg;
    public static byte[][] imgCharMonster;
    public static byte[] hFrameImgCharMonster = new byte[]{60, 41, 41, 61, 64, 64, 31, 64, 64,57};
    public static byte[][] imgTreeFarm;
    public static byte[][] imgHouseFarm;
    public static byte[][] imgAvtAnimal;
    public static byte[][] byteArrayMap;
    public static byte[][] imgPet;
    public static byte[][] imgThanThu;
    public static byte[][] byteDataThanThu;
    public static byte[][] imgPotion;
    public static byte[][] imgGem;
    public static byte[][] imgIconAnimal;
    public static byte[][] imgIconQuest;
    public static byte[][] imgTile;
    public static byte[][] imgFruit;
    public static byte[][] imgCoVat;
    public static byte[][] imgDynamicEffect;
    public static byte[][] dataDynamicEffect;
    public static byte[][] imgEffectSkill;
    public static byte[][] imgEffectArrow;
    public static byte[][] dataBoss;
    /** Atlas con thu tren ban do cua skin linh thu (map/skinthu/&lt;n&gt;.png). */
    public static byte[][] imgSkinThu;
    /** Du lieu frame di kem atlas tren (map/skinthu/&lt;n&gt;). */
    public static byte[][] dataSkinThu;
    // Nang vua du, KHONG nang du: id nao < nIconItem ma khong co file thi allImg[id]
    // la null, va sendByteImg se nem NPE roi bo qua ca lenh gui -> client hoi mai khong
    // bao gio duoc tra loi. Nang du la tu tao them may id "ho den" nhu vay.
    //
    // 855 -> 860: them nam anh avatar cua canh, map/icon/855..859.png. Chung khong
    // phai icon vat pham ma la dai 3 khung ve o o xem truoc cua so Trang bi, giong
    // cach anh phi phong 614/625/708/714 dang lam. Xem Char.getIdBigPhiPhong().
    //
    // 860 -> 865: icon 5 vat pham skin linh thu, map/icon/860..864.png.
    // KHONG dung lai dai 855..859 nhu ban skin dau tien: sau khi tinh nang bi go, dung
    // nam id do da duoc giao cho canh (commit 2c651ce). Dat de len la o xem truoc cua
    // canh hien ra con thu.
    static short nIconItem = 865; // update item moi
//    static short nIconItem = 632;
    public static byte version = 1;

    public static void load() {
        try {
            int nMonster = 139;
            imageMonster = new byte[nMonster][];
            FileInputStream fis = null;
            DataInputStream dis = null;

            int j;
            for (j = 0; j < nMonster; ++j) {
                try {
                    fis = new FileInputStream("map/m/" + j);
                    dis = new DataInputStream(fis);
                    imageMonster[j] = new byte[dis.available()];
                    dis.read(imageMonster[j], 0, imageMonster[j].length);

                    try {
                        dis.close();
                    } catch (Exception var1246) {
                    }

                    try {
                        fis.close();
                    } catch (Exception var1245) {
                    }
                } catch (Exception var1247) {
//                    var1247.printStackTrace();
                }
            }

            nMonster = 63;
            imgEffectSkill = new byte[nMonster][];

            try {
                for (j = 0; j < nMonster; ++j) {
                    fis = new FileInputStream("map/imgEff/" + j + ".png");
                    dis = new DataInputStream(fis);
                    imgEffectSkill[j] = new byte[dis.available()];
                    dis.read(imgEffectSkill[j], 0, imgEffectSkill[j].length);

                    try {
                        dis.close();
                    } catch (Exception var1244) {
                    }

                    try {
                        fis.close();
                    } catch (Exception var1243) {
                    }
                }
            } catch (Exception var1253) {
            }

            nMonster = 3;
            imgThanThu = new byte[nMonster][];
            byteDataThanThu = new byte[nMonster][];

            try {
                for (j = 0; j < nMonster; ++j) {
                    fis = new FileInputStream("map/thanthu/" + j + ".png");
                    dis = new DataInputStream(fis);
                    imgThanThu[j] = new byte[dis.available()];
                    dis.read(imgThanThu[j], 0, imgThanThu[j].length);

                    try {
                        dis.close();
                    } catch (Exception var1242) {
                    }

                    try {
                        fis.close();
                    } catch (Exception var1241) {
                    }

                    fis = new FileInputStream("map/thanthu/" + j);
                    dis = new DataInputStream(fis);
                    byteDataThanThu[j] = new byte[dis.available()];
                    dis.read(byteDataThanThu[j], 0, byteDataThanThu[j].length);

                    try {
                        dis.close();
                    } catch (Exception var1240) {
                    }

                    try {
                        fis.close();
                    } catch (Exception var1239) {
                    }
                }
            } catch (Exception var1252) {
            }

            nMonster = 13;
            dataBoss = new byte[nMonster][];

            try {
                for (j = 0; j < nMonster; ++j) {
                    fis = new FileInputStream("map/boss/" + j);
                    dis = new DataInputStream(fis);
                    dataBoss[j] = new byte[dis.available()];
                    dis.read(dataBoss[j], 0, dataBoss[j].length);

                    try {
                        dis.close();
                    } catch (Exception var1238) {
                    }

                    try {
                        fis.close();
                    } catch (Exception var1237) {
                    }
                }
            } catch (Exception var1251) {
                var1251.printStackTrace();
            }

            nMonster = 20;

            try {
                imgEffectArrow = new byte[nMonster][];

                for (j = 0; j < nMonster; ++j) {
                    fis = new FileInputStream("map/imgArrow/" + j + ".png");
                    dis = new DataInputStream(fis);
                    imgEffectArrow[j] = new byte[dis.available()];
                    dis.read(imgEffectArrow[j], 0, imgEffectArrow[j].length);

                    try {
                        dis.close();
                    } catch (Exception var1236) {
                    }

                    try {
                        fis.close();
                    } catch (Exception var1235) {
                    }
                }
            } catch (Exception var1250) {
            }

            nMonster = 10;
            imgCharMonster = new byte[nMonster][];

            for (j = 0; j < nMonster; ++j) {
                fis = new FileInputStream("map/monster/" + j + ".png");
                dis = new DataInputStream(fis);
                imgCharMonster[j] = new byte[dis.available()];
                dis.read(imgCharMonster[j], 0, imgCharMonster[j].length);

                try {
                    dis.close();
                } catch (Exception var1234) {
                }

                try {
                    fis.close();
                } catch (Exception var1233) {
                }
            }
            // TODO id lớn nhất trong d_effect
            nMonster = 300;
            imgDynamicEffect = new byte[nMonster][];
            dataDynamicEffect = new byte[nMonster][];

            for (j = 0; j < nMonster; ++j) {
                try {
                    fis = new FileInputStream("map/d_effect/" + j + ".png");
                    dis = new DataInputStream(fis);
                    imgDynamicEffect[j] = new byte[dis.available()];
                    dis.read(imgDynamicEffect[j], 0, imgDynamicEffect[j].length);
                    
                    
                   
                    try {
                        dis.close();
                    } catch (Exception var1231) {
                    }

                    try {
                        fis.close();
                    } catch (Exception var1230) {
                    }

                    fis = new FileInputStream("map/d_effect/" + j);
                    dis = new DataInputStream(fis);
                    dataDynamicEffect[j] = new byte[dis.available()];
                    dis.read(dataDynamicEffect[j], 0, dataDynamicEffect[j].length);
                    
                    try {
                        dis.close();
                    } catch (Exception var1229) {
                    }

                    try {
                        fis.close();
                    } catch (Exception var1228) {
                    }
                } catch (Exception var1232) {
//                    var1232.printStackTrace();
                }
            }

            imgTile = new byte[2][];
            String[] urlTile = new String[]{"map/t_hang.png", "map/t_thanh.png"};

            int i;
            for (i = 0; i < 2; ++i) {
                fis = new FileInputStream(urlTile[i]);
                dis = new DataInputStream(fis);
                imgTile[i] = new byte[dis.available()];
                dis.read(imgTile[i], 0, imgTile[i].length);

                try {
                    dis.close();
                } catch (Exception var1227) {
                }

                try {
                    fis.close();
                } catch (Exception var1226) {
                }
            }

            allImg = new byte[nIconItem][];

            for (i = 0; i < nIconItem; ++i) {
                try {
                    fis = new FileInputStream("map/icon/" + i + ".png");
                    dis = new DataInputStream(fis);
                    allImg[i] = new byte[dis.available()];
                    dis.read(allImg[i], 0, allImg[i].length);
                } catch (Exception var1224) {
//                    var1224.printStackTrace();
//                    System.out.println("thieu file icon : " + i + ".png");

                } finally {
                    try {
                        dis.close();
                    } catch (Exception var1181) {
                    }

                    try {
                        fis.close();
                    } catch (Exception var1180) {
                    }

                }
            }

            int nfruit = 15;
            imgFruit = new byte[nfruit][];

            for (i = 0; i < nfruit; ++i) {
                try {
                    fis = new FileInputStream("map/fruit/b" + i + ".png");
                    dis = new DataInputStream(fis);
                    imgFruit[i] = new byte[dis.available()];
                    dis.read(imgFruit[i], 0, imgFruit[i].length);
                } catch (Exception var1222) {
                    var1222.printStackTrace();
                } finally {
                    try {
                        dis.close();
                    } catch (Exception var1179) {
                    }

                    try {
                        fis.close();
                    } catch (Exception var1178) {
                    }

                }
            }

            nfruit = 16;
            imgCoVat = new byte[nfruit][];

            for (i = 0; i < nfruit; ++i) {
                try {
                    fis = new FileInputStream("map/tb/" + i + ".png");
                    dis = new DataInputStream(fis);
                    imgCoVat[i] = new byte[dis.available()];
                    dis.read(imgCoVat[i], 0, imgCoVat[i].length);
                } catch (Exception var1220) {
                    var1220.printStackTrace();
                } finally {
                    try {
                        dis.close();
                    } catch (Exception var1177) {
                    }

                    try {
                        fis.close();
                    } catch (Exception var1176) {
                    }

                }
            }

            imgOther = new byte[6][];

            for (i = 0; i < 6; ++i) {
                try {
                    fis = new FileInputStream("map/other/" + i + ".png");
                    dis = new DataInputStream(fis);
                    imgOther[i] = new byte[dis.available()];
                    dis.read(imgOther[i], 0, imgOther[i].length);
                } catch (Exception var1218) {
                    var1218.printStackTrace();
                } finally {
                    try {
                        dis.close();
                    } catch (Exception var1175) {
                    }

                    try {
                        fis.close();
                    } catch (Exception var1174) {
                    }

                }
            }

            iconClanImg = new byte[Map.MAX_CLAN][];

            for (i = 0; i < Map.MAX_CLAN; ++i) {
                try {
                    fis = new FileInputStream("map/iconclan/" + i + ".png");
                    dis = new DataInputStream(fis);
                    iconClanImg[i] = new byte[dis.available()];
                    dis.read(iconClanImg[i], 0, iconClanImg[i].length);
                } catch (Exception var1216) {
                    var1216.printStackTrace();
                } finally {
                    try {
                        dis.close();
                    } catch (Exception var1173) {
                    }

                    try {
                        fis.close();
                    } catch (Exception var1172) {
                    }

                }
            }

            iconSkillClanImg = new byte[8][];

            for (i = 0; i < 8; ++i) {
                try {
                    fis = new FileInputStream("map/iconskillclan/" + i + ".png");
                    dis = new DataInputStream(fis);
                    iconSkillClanImg[i] = new byte[dis.available()];
                    dis.read(iconSkillClanImg[i], 0, iconSkillClanImg[i].length);
                } catch (Exception var1214) {
                    var1214.printStackTrace();
                } finally {
                    try {
                        dis.close();
                    } catch (Exception var1171) {
                    }

                    try {
                        fis.close();
                    } catch (Exception var1170) {
                    }

                }
            }

            trees = new byte[53][];

            for (i = 0; i < 53; ++i) {
                try {
                    fis = new FileInputStream("map/tree/" + i);
                    dis = new DataInputStream(fis);
                    trees[i] = new byte[dis.available()];
                    dis.read(trees[i], 0, trees[i].length);
                } catch (Exception var1212) {
                } finally {
                    try {
                        dis.close();
                    } catch (Exception var1169) {
                    }

                    try {
                        fis.close();
                    } catch (Exception var1168) {
                    }

                }
            }

            String[] url = new String[]{"map/hieuung/4", "map/hieuung/6", "map/hieuung/1", "map/hieuung/2"};
            imgEff = new byte[4][];
            effData = new byte[4][];

            for (j = 0; j < 4; ++j) {
                try {
                    fis = new FileInputStream(url[j] + ".png");
                    dis = new DataInputStream(fis);
                    imgEff[j] = new byte[dis.available()];
                    dis.read(imgEff[j], 0, imgEff[j].length);
                    fis = new FileInputStream(url[j] + "_data");
                    dis = new DataInputStream(fis);
                    effData[j] = new byte[dis.available()];
                    dis.read(effData[j], 0, effData[j].length);
                } catch (Exception var1210) {
                    var1210.printStackTrace();
                } finally {
                    try {
                        dis.close();
                    } catch (Exception var1167) {
                    }

                    try {
                        fis.close();
                    } catch (Exception var1166) {
                    }

                }
            }

            imgTreeFarm = new byte[95][];

            for (j = 0; j < 95; ++j) {
                try {
                    fis = new FileInputStream("map/cay/" + j + ".png");
                    dis = new DataInputStream(fis);
                    imgTreeFarm[j] = new byte[dis.available()];
                    dis.read(imgTreeFarm[j], 0, imgTreeFarm[j].length);
                } catch (Exception var1207) {
                }

                try {
                    dis.close();
                } catch (Exception var1208) {
                }

                try {
                    fis.close();
                } catch (Exception var1209) {
                }
            }

            imgHouseFarm = new byte[32][];

            for (j = 0; j < 32; ++j) {
                try {
                    fis = new FileInputStream("map/farm/" + j + ".png");
                    dis = new DataInputStream(fis);
                    imgHouseFarm[j] = new byte[dis.available()];
                    dis.read(imgHouseFarm[j], 0, imgHouseFarm[j].length);
                } catch (Exception var1205) {
                } finally {
                    try {
                        dis.close();
                    } catch (Exception var1165) {
                    }

                    try {
                        fis.close();
                    } catch (Exception var1164) {
                    }

                }
            }

            imgAvtAnimal = new byte[21][];

            for (j = 0; j < 21; ++j) {
                try {
                    fis = new FileInputStream("map/avtathu/" + j + ".png");
                    dis = new DataInputStream(fis);
                    imgAvtAnimal[j] = new byte[dis.available()];
                    dis.read(imgAvtAnimal[j], 0, imgAvtAnimal[j].length);
                //    System.out.println("img avt thu " + j + " " + imgAvtAnimal[j]);
                } catch (Exception var1202) {
                    var1202.printStackTrace();
                }

                try {
                    dis.close();
                } catch (Exception var1203) {
                }

                try {
                    fis.close();
                } catch (Exception var1204) {
                }
            }

            byte nPet = 20;
            imgPet = new byte[nPet][];

            for (j = 0; j < nPet; ++j) {
                try {
                    fis = new FileInputStream("map/pet/" + j + ".png");
                    dis = new DataInputStream(fis);
                    imgPet[j] = new byte[dis.available()];
                    dis.read(imgPet[j], 0, imgPet[j].length);
                } catch (Exception var1199) {
                }

                try {
                    dis.close();
                } catch (Exception var1200) {
                }

                try {
                    fis.close();
                } catch (Exception var1201) {
                }
            }

            napSkinThu();

            try {
                byte nIconQuest = (byte) ItemQuest.NAME_ITEM_QUEST.length;
                imgIconQuest = new byte[nIconQuest][];

                for (j = 0; j < nIconQuest; ++j) {
                    try {
                        fis = new FileInputStream("map/iconquest/" + j + ".png");
                        dis = new DataInputStream(fis);
                        imgIconQuest[j] = new byte[dis.available()];
                        dis.read(imgIconQuest[j], 0, imgIconQuest[j].length);
                    } catch (Exception var1196) {
                    }

                    try {
                        dis.close();
                    } catch (Exception var1197) {
                    }

                    try {
                        fis.close();
                    } catch (Exception var1198) {
                    }
                }
            } catch (Exception var1249) {
            }

            try {
                cloth = new byte[6][][];
                // So bo phan cua tung loai: chan, than, dau, non, ao choang, co.
                // Non nang 67 -> 75 de nap them 8 file map/c/hat/67..74 - do la
                // "non mat na", chi hien o o xem truoc cua so Trang bi con tren ban
                // do thi trong suot (xem Char.getIdHatMatNa).
                byte[] n = new byte[]{81, 81, 84, 75, 16, 3};
                String[] name = new String[]{"map/c/leg/", "map/c/body/", "map/c/head/", "map/c/hat/", "map/c/coat/", "map/c/flag/"};

                for (i = 0; i < 6; ++i) {
                    cloth[i] = new byte[n[i]][];

                    for (j = 0; j < n[i]; ++j) {
                        try {
                            fis = new FileInputStream(name[i] + j);
                            dis = new DataInputStream(fis);
                            cloth[i][j] = new byte[dis.available()];
                            dis.read(cloth[i][j], 0, cloth[i][j].length);
                        } catch (Exception var1195) {
                            var1195.printStackTrace();
                        }

                        try {
                            dis.close();
                        } catch (Exception var1194) {
                        }

                        try {
                            fis.close();
                        } catch (Exception var1193) {
                        }
                    }
                }
            } catch (Exception var1248) {
                var1248.printStackTrace();
            }

            nMonster = 256;
            imgPotion = new byte[nMonster][];

            for (j = 0; j < nMonster; ++j) {
                try {
                    fis = new FileInputStream("map/images_potion/" + j + ".png");
                    dis = new DataInputStream(fis);
                    imgPotion[j] = new byte[dis.available()];
                    dis.read(imgPotion[j], 0, imgPotion[j].length);

                    try {
                        dis.close();
                    } catch (Exception var1191) {
                    }

                    try {
                        fis.close();
                    } catch (Exception var1190) {
                    }
                } catch (Exception var1192) {
                    var1192.printStackTrace();
                }
            }

            nMonster = 96;
            imgGem = new byte[nMonster][];

            for (j = 0; j < nMonster; ++j) {
                try {
                    fis = new FileInputStream("map/images_gem/" + j + ".png");
                    dis = new DataInputStream(fis);
                    imgGem[j] = new byte[dis.available()];
                    dis.read(imgGem[j], 0, imgGem[j].length);

                    try {
                        dis.close();
                    } catch (Exception var1188) {
                    }

                    try {
                        fis.close();
                    } catch (Exception var1187) {
                    }
                } catch (Exception var1189) {
                    var1189.printStackTrace();
                }
            }

            nMonster = 58;
            imgIconAnimal = new byte[nMonster][];

            for (j = 0; j < nMonster; ++j) {
                try {
                    fis = new FileInputStream("map/images_animal/" + j + ".png");
                    dis = new DataInputStream(fis);
                    imgIconAnimal[j] = new byte[dis.available()];
                    dis.read(imgIconAnimal[j], 0, imgIconAnimal[j].length);
                } catch (Exception var1184) {
                }

                try {
                    dis.close();
                } catch (Exception var1185) {
                }

                try {
                    fis.close();
                } catch (Exception var1186) {
                }
            }

            try {
                fis = new FileInputStream("map/EFF_PACK");
                dis = new DataInputStream(fis);
                effDataLocal = new byte[dis.available()];
                dis.read(effDataLocal, 0, effDataLocal.length);
            } catch (Exception var1182) {
                var1182.printStackTrace();
            } finally {
                try {
                    dis.close();
                } catch (Exception var1163) {
                }

                try {
                    fis.close();
                } catch (Exception var1162) {
                }

            }
        } catch (Exception var1254) {
            var1254.printStackTrace();
        }

    }

    /** Id anh da ghi vao log roi - chi de chan doan, moi id ghi mot lan. */
    private static final java.util.Set<Integer> daXinAnh =
            java.util.Collections.synchronizedSet(new java.util.HashSet<Integer>());

    public static void sendByteImg(Session s, Message msg) {
        short idImg = -1;   // khai bao NGOAI try de catch ben duoi doc duoc
        try {
            idImg = msg.dis.readShort();

            // CHAN DOAN linh thu tool: ghi lai MOI id anh client tung xin, moi id
            // MOT lan (dedupe de khong lam day log - client xin lai lien tuc).
            //
            // Truoc do chi ghi dai >= 8700 va thay TRONG TRON, ke ca anh vu khi thoi
            // trang / toc / ao / phi phong - tat ca deu di chung duong DataSkillEff
            // .paintTopHorse -> getImgIcon nhu thu cuoi. "Khong xin gi trong dai do"
            // la mot manh moi manh hon "khong xin 8764": no noi vong ve cac bo phan
            // dong CHUA BAO GIO chay, chu khong rieng con thu. Ghi ca dai duoi de doi
            // chieu: client co xin anh nao khong, hay im lang hoan toan.
            if (daXinAnh.add(Integer.valueOf(idImg))) {
                System.out.println("[ANH] client xin anh id " + idImg
                        + (idImg >= 8700 && idImg < 9000 ? "  <= dai bo phan dong (tool)" : ""));
            }
         
        
            Message m = new Message(-51);
            m.dos.writeShort(idImg);
            if (idImg < nIconItem && !(idImg >= 800 && idImg <= 806)) {
                m.dos.write(allImg[idImg]);
            } else if (idImg >= 800 && idImg <= 806) {
                // imgOther chi co 6 phan tu (0..5) nhung dai nay rong 7 (800..806),
                // idImg = 806 se ném ArrayIndexOutOfBounds khi phuc vu anh.
                int k = idImg - 800;
                if (k >= 0 && k < imgOther.length && imgOther[k] != null) {
                    m.dos.write(imgOther[k]);
                }
            } else if (idImg >= 900 && idImg < 1000) {
               
                m.dos.write(imgOther[idImg - 900]);
            } else if (idImg >= 1000 && idImg < 2600) {
      
                m.dos.write(iconClanImg[idImg - 1000]);
            } else if (idImg >= 2600 && idImg < 3200) {
                m.dos.write(iconSkillClanImg[idImg - 2600]);
            } else if (idImg >= 3200 && idImg < 4200) {
                m.dos.write(imgTreeFarm[idImg - 3200]);
            } else if (idImg >= 4200 && idImg < 5200) {
                m.dos.write(imgHouseFarm[idImg - 4200]);
            } else if (idImg >= 5200 && idImg < 5500) {
                m.dos.write(imgPet[idImg - 5200]);
            } else if (idImg >= 5500 && idImg < 6500) {
                m.dos.write(imgPotion[idImg - 5500]);
            } else if (idImg >= 6500 && idImg < 7500) {
                m.dos.write(imgGem[idImg - 6500]);
            } else if (idImg >= 7500 && idImg < 8000) {
                m.dos.write(imgIconAnimal[idImg - 7500]);
            } else if (idImg >= 8000 && idImg < 8200) {
                m.dos.write(imgIconQuest[idImg - 8000]);
            } else if (idImg >= 8200 && idImg < 8500) {
                m.dos.write(imgFruit[idImg - 8200]);
            } else if (idImg >= 8500 && idImg < 8700) {
                m.dos.write(imgCoVat[idImg - 8500]);
            } else if (idImg >= 8700 && idImg < 9000) {
                m.dos.write(imgDynamicEffect[idImg - 8700]);
             
            } else if (idImg >= 9000 && idImg < 10000) {
                m.dos.write(imgEffectSkill[idImg - 9000]);
               
            } else if (idImg >= 10000 && idImg < 11000) {
                m.dos.write(imgEffectArrow[idImg - 10000]);
               
            } else if (idImg >= 11000 && idImg < 12000) {
                m.dos.write(iconClanImg[idImg - 11000]);
            } else if (idImg >= 12000 && idImg < 12100) {
                m.dos.write(imgThanThu[idImg - 12000]);
            } else if (idImg >= real.SkinLinhThu.ID_ANH_BAN_DO
                    && idImg < real.SkinLinhThu.ID_ANH_BAN_DO + real.SkinLinhThu.SO_SKIN) {
                // Skin linh thu. Dai id nay chua tung duoc dung (dai cu cao nhat het o
                // 12099) nen khong client nao co san anh cu cua id nay trong bo dem —
                // do la ly do chon dai moi thay vi dung lai id d_effect nao do.
                final byte[] anh = anhBanDoSkinThu(idImg - real.SkinLinhThu.ID_ANH_BAN_DO);
                if (anh != null) {
                    m.dos.write(anh);
                }
            } else {
                // Id khong khop DAI NAO: goi tin gui di chi co id, KHONG co anh.
                // Client nhan anh rong -> ve sai ma khong bao gi. Ghi log de thay.
                System.out.println("[ANH] id " + idImg + " khong thuoc dai nao - gui goi RONG");
            }

            s.sendMessage(m);
        } catch (Exception var4) {
            // Truoc day dong nay bi comment nen MOI loi phuc vu anh deu im lang -
            // khong the chan doan duoc gi. Bat lai, in kem id de biet anh nao hong.
            System.out.println("[ANH] LOI phuc vu anh id " + idImg + ": " + var4);
            var4.printStackTrace();
        }

    }

    /**
     * Nap bo anh skin linh thu tu thu muc RIENG map/skinthu/.
     *
     * De rieng chu khong chen vao map/d_effect/ vi thu muc do da kin cho: DataGame chi
     * nap 0..299 (dai id 8700..8999 cua sendByteImg) va ca 300 id deu da co file. Thu
     * muc rieng + dai id rieng thi them bao nhieu skin cung duoc, va khong dung den mot
     * file anh cu nao.
     *
     * Thieu file thi skin do bi TAT HAN (SkinLinhThu.coDuAnh tra false), con thu ve lai
     * hinh that. Phai tat han chu khong the tat mot nua: bat skin la tat duong ve "ngua
     * co dien" (userHorse = 0), ma thieu du lieu frame thi duong "thu cuoi tool" cung
     * khong ve duoc gi — nguoi choi se cuoi hu khong.
     */
    private static void napSkinThu() {
        final int n = real.SkinLinhThu.SO_SKIN;
        imgSkinThu = new byte[n][];
        dataSkinThu = new byte[n][];
        for (int i = 0; i < n; i++) {
            imgSkinThu[i] = docFile("map/skinthu/" + i + ".png");
            dataSkinThu[i] = docFile("map/skinthu/" + i);
            if (imgSkinThu[i] == null || dataSkinThu[i] == null) {
                System.out.println("CANH BAO: thieu file skin linh thu " + i
                        + " (can du map/skinthu/" + i + ".png va " + i + ")"
                        + " -> TAT skin nay, ai dang mac se thay con thu that."
                        + " Dat du ca hai file roi bat lai server.");
            }
        }
    }

    /** Doc ca file thanh mang byte, null neu khong co file hoac loi doc. */
    private static byte[] docFile(final String duong) {
        try (FileInputStream fis = new FileInputStream(duong);
             DataInputStream dis = new DataInputStream(fis)) {
            final byte[] b = new byte[dis.available()];
            dis.readFully(b);
            return b;
        } catch (Exception e) {
            return null;
        }
    }

    /** Du lieu frame cua skin thu n, null neu khong co. */
    public static byte[] duLieuSkinThu(final int n) {
        return (dataSkinThu != null && n >= 0 && n < dataSkinThu.length) ? dataSkinThu[n] : null;
    }

    /** Atlas con thu tren ban do cua skin thu n, null neu khong co. */
    public static byte[] anhBanDoSkinThu(final int n) {
        return (imgSkinThu != null && n >= 0 && n < imgSkinThu.length) ? imgSkinThu[n] : null;
    }

    public static void sendMsgGetDataEff(Session s, Message msg) {
        try {
            int ideff = msg.dis.readByte();
            Message m = new Message(-49);
            m.dos.writeByte(1);
            m.dos.writeByte(ideff);
            m.dos.writeShort(imgEff[ideff].length);
            m.dos.write(imgEff[ideff]);
            m.dos.write(effData[ideff]);
            s.sendMessage(m);
        } catch (Exception var4) {
            var4.printStackTrace();
        }

    }

    public static void sendMsgCloth2Player(Session s, Message msg) {
        try {
            int type = msg.dis.readByte();
            Message m = new Message(-48);
            m.dos.writeByte(type);
            if (type > -1) {
                m.dos.writeShort(trees[type].length);
                m.dos.write(trees[type]);
            } else if (type == -1) {
                byte typeCloth = msg.dis.readByte();
                byte id = msg.dis.readByte();
                m.dos.writeByte(typeCloth);
                m.dos.writeByte(id);
                m.dos.write(cloth[typeCloth][id]);
            }

            s.sendMessage(m);
        } catch (Exception var6) {
        }

    }

    public static void processMessage(Char player, Message msg, Session s) {
        try {
            if (msg.command == -47) {
                byte palate = msg.dis.readByte();
                byte spalate = msg.dis.readByte();
                short idTemplate = msg.dis.readShort();
                Message m = new Message(-47);
                m.dos.writeShort(idTemplate);
                m.dos.writeBoolean(true);
                m.dos.writeShort(imageMonster[palate].length);
                m.dos.write(imageMonster[palate]);
                player.sendMessage(m);
            }
        } catch (Exception var7) {
        }

    }

    

    
    public static void sendInfoCCU(Session s) {
        try {
            Message msg = new Message(127);
            String info = "-1," + (TeamServer.maxCCU - TeamServer.maxCCUAD) + "," + TeamServer.maxCCUAD + ",0";
            Set<String> set = Database.allProvider.keySet();

            CCUProvider ccu;
            for (Iterator all = set.iterator(); all.hasNext(); info = info + "," + ccu.provider + "," + ccu.ccuFirmWare[0] + "," + ccu.ccuFirmWare[1] + ",0") {
                ccu = (CCUProvider) Database.allProvider.get(all.next());
            }

            msg.dos.writeByte(0);
            msg.dos.writeShort(TeamServer.maxCCU);
            msg.dos.writeShort(0);
            msg.dos.writeShort(0);
            msg.dos.writeUTF(info);
            s.sendMessage(msg);
        } catch (Exception var6) {
        }

    }

    public static void loadCodeNgude() {
    }
}
