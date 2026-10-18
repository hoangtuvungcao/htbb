package data;

import io.Message;
import io.Session;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.nio.file.Files;
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
    /** Icon vat pham Pokemon map/icon_pokemon/&lt;n&gt;.png, phuc vu o id 14000+n. */
    public static byte[][] imgIconPokemon = new byte[0][];
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
    /** Bang khung RIENG cho duong ve PET, nap tu map/d_effect/<id>.pet (null = dung <id>). Xem moTaPet(). */
    public static byte[][] dataPetEffect;
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
    static short nIconItem = 866; // update item moi (865 = icon skin Kim Cang Long)
//    static short nIconItem = 632;
    public static byte version = 1;

    /** Nap map/icon_pokemon/0..n-1.png (dung o file dau tien thieu). Thieu thu muc -> 0 icon. */
    static void napIconPokemon() {
        final java.util.List<byte[]> ds = new java.util.ArrayList<byte[]>();
        for (int i = 0; i < com.htbb.pokemon.IconPokemon.SO_TOI_DA; i++) {
            final java.io.File f = new java.io.File(com.htbb.pokemon.IconPokemon.THU_MUC + i + ".png");
            if (!f.isFile()) {
                break;
            }
            try {
                ds.add(java.nio.file.Files.readAllBytes(f.toPath()));
            } catch (final Exception e) {
                break;
            }
        }
        imgIconPokemon = ds.toArray(new byte[0][]);
        System.out.println("ICON POKEMON -> " + imgIconPokemon.length + " anh tu " + com.htbb.pokemon.IconPokemon.THU_MUC);
    }

    /** Bo tile Pokemon (G7.A) do tools/pokemon/DoiTileMap.java sinh; KHONG nham voi map/img_tile/ (khong ai doc). */
    public static final String THU_MUC_TILE_POKEMON = "map/pokemon_tile/";

    /**
     * Hai anh tile server gui trong goi dang nhap (LoginHandler, chi khi client xin vsTile == -1;
     * do dai ghi bang writeShort nen moi anh phai < 32768 byte). Bat che do Pokemon va co du bo
     * tile moi thi gui bo moi, khong thi giu bo goc.
     */
    public static String[] duongTileGuiClient() {
        final String[] cu = new String[]{"map/t_hang.png", "map/t_thanh.png"};
        if (!com.htbb.pokemon.CheDoPokemon.bat()) {
            return cu;
        }
        final String[] moi = new String[]{THU_MUC_TILE_POKEMON + "t_hang.png", THU_MUC_TILE_POKEMON + "t_thanh.png"};
        for (final String duong : moi) {
            final java.io.File f = new java.io.File(duong);
            if (!f.isFile() || f.length() > Short.MAX_VALUE) {
                return cu;
            }
        }
        return moi;
    }

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

            // ==== SUA 02/09/2026: duong dan thieu chu "s" ====
            // Ban cu doc "map/imgEff/" nhung thu muc that la "map/imgEffs/" (63 file,
            // khop dung nMonster). Ket qua: FileInputStream nem loi ngay o j=0, ma try
            // lai nam NGOAI vong lap nen ca vong dung luon -> CA 63 O DEU NULL.
            //
            // Hau qua: moi yeu cau anh hieu ung skill (id 9000..9062 trong sendByteImg)
            // deu nem NullPointerException tai `m.dos.write(imgEffectSkill[...])`, tuc
            // NEM TRUOC `s.sendMessage(m)` - nen client KHONG NHAN DUOC PHAN HOI NAO ca
            // (khac voi nhan goi rong). Client ngoi cho mot anh khong bao gio toi.
            // Da thay that trong log: "[ANH] LOI phuc vu anh id 9006/9008/9036".
            //
            // Vi thu muc sai ten nen loi nay xay ra tren MOI may, ke ca VPS - khong phai
            // rieng may nha. test/KiemAnhHieuUng.java ghim lai de khong tai dien.
            //
            // try chuyen VAO TRONG vong lap: mot file thieu chi mat dung anh do, khong
            // con keo theo toan bo cac anh sau no.
            int soAnhEffSkill = 0;
            for (j = 0; j < nMonster; ++j) {
                try {
                    fis = new FileInputStream("map/imgEffs/" + j + ".png");
                    dis = new DataInputStream(fis);
                    imgEffectSkill[j] = new byte[dis.available()];
                    dis.read(imgEffectSkill[j], 0, imgEffectSkill[j].length);
                    ++soAnhEffSkill;

                    try {
                        dis.close();
                    } catch (Exception var1244) {
                    }

                    try {
                        fis.close();
                    } catch (Exception var1243) {
                    }
                } catch (Exception var1253) {
                }
            }
            // In ra de lan sau thieu anh thi THAY NGAY, thay vi im lang roi NPE luc phuc vu.
            System.out.println("ANH HIEU UNG SKILL -> " + soAnhEffSkill + "/" + nMonster
                    + " tu map/imgEffs/");

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
            // 301 (truoc la 300) de nap them map/d_effect/300 - "Set danh" cua skin Kim
            // Cang Long (EffectBuff.SET_DANH = 300+8700), o hoan toan moi chua ton tai
            // truoc do, khong dung lai bat ky file cu nao.
            nMonster = 301;
            imgDynamicEffect = new byte[nMonster][];
            dataDynamicEffect = new byte[nMonster][];
            dataPetEffect = new byte[nMonster][];

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
                    // Bang khung RIENG cho duong ve PET (neu co): map/d_effect/<j>.pet. Dung khi anh
                    // <j>.png la luoi sprite cua hieu ung / danh hieu - bang <j> viet cho duong ve
                    // hieu ung, pet doc se hong - nhung van muon pet dung DUNG anh do. Hien co
                    // 69.pet cho pet Lan (tools/TaoPetLan.java sinh ra). Xem moTaPet().
                    final File filePet = new File("map/d_effect/" + j + ".pet");
                    if (filePet.isFile()) {
                        dataPetEffect[j] = Files.readAllBytes(filePet.toPath());
                    }
                } catch (Exception var1232) {
//                    var1232.printStackTrace();
                }
            }

            imgTile = new byte[2][];
            String[] urlTile = duongTileGuiClient();

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

            napIconPokemon();
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
                    fis = new FileInputStream(com.htbb.pokemon.TrangTriPokemon.duong("map/tree/" + i));
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
                    fis = new FileInputStream(com.htbb.pokemon.TrangTriPokemon.duong("map/cay/" + j + ".png"));
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

            // Dai id anh pet ma sendByteImg phuc vu la 5200..5499, tuc chi so 0..299.
            // Ban cu chi cap 20 o nen moi yeu cau anh pet co chi so >= 20 deu nem
            // ArrayIndexOutOfBounds NGAY GIUA sendByteImg - va vi lenh gui nam ben
            // trong khoi try nen client khong nhan duoc goi tra loi nao, no hoi lai
            // mai. Log VPS ngay 02/09/2026: 2.947 lan, tap trung o id 5221/5269/5235/
            // 5249/5244, va co mot lan ket noi bi cat vi "601 goi tai nguyen".
            // Cap du ca dai: o nao khong co file thi de null, sendByteImg tu lo.
            // (map/pet/ hien co 0..20, 44, 81 - ban cu bo qua ca 20.png lan 44.png.)
            final int nPet = 300;
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

    /** Id anh da bao thieu roi - moi id bao mot lan, khong de log ngap (297 dong cho rieng 5269). */
    private static final java.util.Set<Integer> daBaoThieu =
            java.util.Collections.synchronizedSet(new java.util.HashSet<Integer>());

    /**
     * PNG 1x1 trong suot chuan (67 byte) - phuong an cuoi neu ImageIO khong chay duoc.
     * Chi dung khi anhTrongSuot() khong tao duoc anh 32x96; anh 1x1 lam AnimalMove chia
     * chieu cao cho so khung ra 0, nen khong phai lua chon dau.
     */
    public static final byte[] PNG_1X1_TRONG_SUOT = {
        (byte) 0x89, 0x50, 0x4E, 0x47, 0x0D, 0x0A, 0x1A, 0x0A, 0x00, 0x00, 0x00, 0x0D, 0x49, 0x48, 0x44, 0x52,
        0x00, 0x00, 0x00, 0x01, 0x00, 0x00, 0x00, 0x01, 0x08, 0x06, 0x00, 0x00, 0x00, 0x1F, 0x15, (byte) 0xC4,
        (byte) 0x89, 0x00, 0x00, 0x00, 0x0A, 0x49, 0x44, 0x41, 0x54, 0x78, (byte) 0x9C, 0x63, 0x00, 0x01, 0x00, 0x00,
        0x05, 0x00, 0x01, 0x0D, 0x0A, 0x2D, (byte) 0xB4, 0x00, 0x00, 0x00, 0x00, 0x49, 0x45, 0x4E, 0x44, (byte) 0xAE,
        0x42, 0x60, (byte) 0x82
    };

    /** Kich thuoc anh thay the. 96 cao de AnimalMove chia cho so khung (toi 96) van ra >= 1. */
    public static final int ANH_TRONG_SUOT_RONG = 32;
    public static final int ANH_TRONG_SUOT_CAO = 96;

    private static byte[] anhTrongSuot;

    /**
     * Anh PNG trong suot 32x96, tao mot lan bang ImageIO (chay duoc khong can man hinh).
     * Tra ve cho MOI yeu cau anh ma server khong co file - xem ghiAnh().
     */
    public static synchronized byte[] anhTrongSuot() {
        if (anhTrongSuot == null) {
            try {
                final java.awt.image.BufferedImage bi = new java.awt.image.BufferedImage(
                        ANH_TRONG_SUOT_RONG, ANH_TRONG_SUOT_CAO, java.awt.image.BufferedImage.TYPE_INT_ARGB);
                final java.io.ByteArrayOutputStream bo = new java.io.ByteArrayOutputStream();
                if (!javax.imageio.ImageIO.write(bi, "png", bo) || bo.size() < PNG_1X1_TRONG_SUOT.length) {
                    throw new IllegalStateException("ImageIO khong ghi duoc png");
                }
                anhTrongSuot = bo.toByteArray();
            } catch (final Throwable t) {
                System.out.println("[ANH] khong tao duoc anh trong suot 32x96 (" + t + "), dung PNG 1x1");
                anhTrongSuot = PNG_1X1_TRONG_SUOT.clone();
            }
        }
        return anhTrongSuot;
    }

    /**
     * Ghi mot anh vao goi tra loi, co kiem bien va kiem null.
     *
     * VI SAO PHAI CO. Truoc day moi nhanh trong sendByteImg goi thang
     * m.dos.write(kho[chiSo]). Chi mot chi so ngoai bien (hoac mot o null vi thieu file)
     * la nem ngoai le NGAY GIUA ham - ma lenh gui s.sendMessage(m) lai nam BEN TRONG
     * cung khoi try, nen client KHONG nhan duoc goi tra loi nao. Client danh dau anh do
     * la "chua tai" roi hoi lai, mai mai.
     *
     * Do la mot vong lap co that: log VPS ngay 02/09/2026 co 2.947 dong
     * "[ANH] LOI phuc vu anh id ..." (nhieu nhat la 5221 va 5269 - dai anh pet, luc do
     * mang imgPet chi co 20 o), va mot ket noi bi cat vi "601 goi tai nguyen trong
     * 5000ms, lenh cuoi=-51" - dung la nguoi choi bi van ra khoi game vi mot anh thieu.
     *
     * THIEU ANH THI TRA ANH TRONG SUOT, KHONG TRA GOI RONG. Ban 2d778f5 (sang 02/09/2026)
     * tra goi 0 byte de client khong hoi lai - nhung client (MessageHandler case -51) dat
     * img.isLoad = false TRUOC roi moi Image.createImage(0 byte) -> img.img null. Tu do moi
     * lan ve pet (AnimalMove.paint: imgIcon.img.getHeight()) la NullPointerException ngay
     * trong vong ve actor -> ca NHAN VAT bien mat, nguoi choi bao "vao map boss khong hien
     * thi nhan vat". Dinh that voi id 5221 (map/pet/21.png khong co, ma 105 pet Rong/Cu/Tho
     * bay... deu mang idImg 21) va 5269 (Lan hoa hinh). Anh trong suot hop le thi client ve
     * "khong gi ca" va thoi hoi - dung ca hai muc tieu.
     */
    private static void ghiAnh(final Message m, final byte[][] kho, final int chiSo, final int idImg)
            throws java.io.IOException {
        if (kho != null && chiSo >= 0 && chiSo < kho.length && kho[chiSo] != null) {
            m.dos.write(kho[chiSo]);
            return;
        }
        m.dos.write(anhTrongSuot());
        if (daBaoThieu.add(Integer.valueOf(idImg))) {
            System.out.println("[ANH] thieu anh id " + idImg + " (chi so " + chiSo
                    + (kho == null ? ", kho chua nap" : ", kho co " + kho.length + " o")
                    + ") - tra anh trong suot de client khong loi ve, khong hoi lai (bao mot lan)");
        }
    }

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
            // Che do Pokemon: anh thuoc/ngoc/thu/vat pham nhiem vu/trai cay (chi so 1 byte, khong
            // co cot DB de tro sang dai 14000+) tra icon PMD thay cho anh goc. Xem IconPokemon.
            final int iconPmd = com.htbb.pokemon.CheDoPokemon.bat() ? com.htbb.pokemon.IconPokemon.doiAnh(idImg) : -1;
            if (iconPmd >= 0) {
                ghiAnh(m, imgIconPokemon, iconPmd, idImg);
                s.sendMessage(m);
                return;
            }
            // ==== DAI 800-806 BI CHIEM: DUNG dat xstart cua vat pham vao day ====
            // Dai nay KHONG doc map/icon/<id>.png ma phuc vu tu mang imgOther
            // (= map/other/0..5.png). Vat pham nao co xstart trong dai nay thi:
            //   800      -> map/other/0.png la BAN DO THE GIOI co lon. Client ve icon bang
            //               drawImage(img, x, y, HCENTER|VCENTER) nen anh to PHU KIN UI.
            //   801..805 -> ra map/other/1..5.png, tuc hien sai anh.
            //   806      -> imgOther chi co 6 phan tu (0..5) nen k=6 truot dieu kien duoi
            //               va server gui goi KHONG CO BYTE ANH NAO. Ben client,
            //               MessageHandler case -51 da dat img.isLoad = false TRUOC khi
            //               goi Image.createImage; anh rong lam createImage nem loi nen
            //               img.img giu nguyen null -> tu do MOI LAN paintIcon deu
            //               NullPointerException, UI thung cho den khi tat han client.
            // Trieu chung nhin thay: mo hanh trang thi mot phan UI bi anh ban do de len,
            // hoac o vat pham trong tron roi giao dien vo tu do tro di.
            // Da tung dinh that: bay vat pham 872-878 deu tro vao dai nay, phai doi sang
            // map/icon/815-819, 827, 828. Xem sql/README.md muc "Icon vat pham".
            if (idImg < nIconItem && !(idImg >= 800 && idImg <= 806)) {
                ghiAnh(m, allImg, idImg, idImg);
            } else if (idImg >= 800 && idImg <= 806) {
                // imgOther chi co 6 phan tu (0..5) nhung dai nay rong 7 (800..806): 806 truoc
                // day khong ghi gi ca (goi rong -> client NPE, xem chuoi loi o tren). Nay qua
                // ghiAnh de thieu thi tra anh trong suot nhu moi dai khac.
                ghiAnh(m, imgOther, idImg - 800, idImg);
            } else if (idImg >= 900 && idImg < 1000) {
                ghiAnh(m, imgOther, idImg - 900, idImg);
            } else if (idImg >= 1000 && idImg < 2600) {
      
                ghiAnh(m, iconClanImg, idImg - 1000, idImg);
            } else if (idImg >= 2600 && idImg < 3200) {
                ghiAnh(m, iconSkillClanImg, idImg - 2600, idImg);
            } else if (idImg >= 3200 && idImg < 4200) {
                ghiAnh(m, imgTreeFarm, idImg - 3200, idImg);
            } else if (idImg >= 4200 && idImg < 5200) {
                ghiAnh(m, imgHouseFarm, idImg - 4200, idImg);
            } else if (idImg >= 5200 && idImg < 5500) {
                ghiAnh(m, imgPet, idImg - 5200, idImg);
            } else if (idImg >= 5500 && idImg < 6500) {
                ghiAnh(m, imgPotion, idImg - 5500, idImg);
            } else if (idImg >= 6500 && idImg < 7500) {
                ghiAnh(m, imgGem, idImg - 6500, idImg);
            } else if (idImg >= 7500 && idImg < 8000) {
                ghiAnh(m, imgIconAnimal, idImg - 7500, idImg);
            } else if (idImg >= 8000 && idImg < 8200) {
                ghiAnh(m, imgIconQuest, idImg - 8000, idImg);
            } else if (idImg >= 8200 && idImg < 8500) {
                ghiAnh(m, imgFruit, idImg - 8200, idImg);
            } else if (idImg >= 8500 && idImg < 8700) {
                ghiAnh(m, imgCoVat, idImg - 8500, idImg);
            } else if (idImg >= 8700 && idImg < 9000) {
                ghiAnh(m, imgDynamicEffect, idImg - 8700, idImg);
             
            } else if (idImg >= 9000 && idImg < 10000) {
                ghiAnh(m, imgEffectSkill, idImg - 9000, idImg);
               
            } else if (idImg >= 10000 && idImg < 11000) {
                ghiAnh(m, imgEffectArrow, idImg - 10000, idImg);
               
            } else if (idImg >= 11000 && idImg < 12000) {
                ghiAnh(m, iconClanImg, idImg - 11000, idImg);
            } else if (idImg >= 12000 && idImg < 12100) {
                ghiAnh(m, imgThanThu, idImg - 12000, idImg);
            } else if (com.htbb.pokemon.IconPokemon.thuocDai(idImg)) {
                // Icon vat pham Pokemon (map/icon_pokemon/), xem com.htbb.pokemon.IconPokemon.
                ghiAnh(m, imgIconPokemon, idImg - com.htbb.pokemon.IconPokemon.ID_DAU, idImg);
            } else if (idImg >= real.SkinThuCuoi.ID_ANH_BAN_DO
                    && idImg < real.SkinThuCuoi.ID_ANH_BAN_DO + real.SkinThuCuoi.SO_SKIN) {
                // Skin linh thu. Dai id nay chua tung duoc dung (dai cu cao nhat het o
                // 12099) nen khong client nao co san anh cu cua id nay trong bo dem —
                // do la ly do chon dai moi thay vi dung lai id d_effect nao do.
                final byte[] anh = anhBanDoSkinThu(idImg - real.SkinThuCuoi.ID_ANH_BAN_DO);
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
     * Bang khung cho DUONG VE PET cua id hieu ung nay: uu tien map/d_effect/<id>.pet, khong co
     * thi dung bang chung <id>. Client giu hai bang rieng (Pet.ALL_PET_DATA cho pet,
     * DataSkillEff.ALL_DATA_EFF cho hieu ung) nhung dung chung anh <id>.png, nen mot anh co the
     * mang hai bang khung. MessageCreator (goi 26, khoi id_eff_pet_tool) goi ham nay.
     */
    public static byte[] moTaPet(final int idEff) {
        if (idEff < 0 || dataDynamicEffect == null || idEff >= dataDynamicEffect.length) {
            return null;
        }
        if (dataPetEffect != null && dataPetEffect[idEff] != null) {
            return dataPetEffect[idEff];
        }
        return dataDynamicEffect[idEff];
    }

    /**
     * Nap bo anh skin linh thu tu thu muc RIENG map/skinthu/.
     *
     * De rieng chu khong chen vao map/d_effect/ vi thu muc do da kin cho: DataGame chi
     * nap 0..299 (dai id 8700..8999 cua sendByteImg) va ca 300 id deu da co file. Thu
     * muc rieng + dai id rieng thi them bao nhieu skin cung duoc, va khong dung den mot
     * file anh cu nao.
     *
     * Thieu file thi skin do bi TAT HAN (SkinThuCuoi.coDuAnh tra false), con thu ve lai
     * hinh that. Phai tat han chu khong the tat mot nua: bat skin la tat duong ve "ngua
     * co dien" (userHorse = 0), ma thieu du lieu frame thi duong "thu cuoi tool" cung
     * khong ve duoc gi — nguoi choi se cuoi hu khong.
     */
    private static void napSkinThu() {
        final int n = real.SkinThuCuoi.SO_SKIN;
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
