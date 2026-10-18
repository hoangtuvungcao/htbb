import com.htbb.runtime.VongLapChung;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Vong lap chung (com.htbb.runtime.VongLapChung) thay "mot thread cho moi khu/ban do"
 * (docs/pokemon/TOI_UU_HIEU_NANG.md): tick chay dung chu ky, khu ranh giam nhip nhung hen gio van
 * chay, nguoi vao thi chay ngay, tam dung khi dang luu, dung han khi tat server / go khu, vong goi
 * tin khong bao gio chay song song va khong mat goi, va Region/Map khong con tao thread rieng.
 */
public final class KiemVongLapChung {

    private KiemVongLapChung() {
    }

    static final AtomicBoolean DUNG = new AtomicBoolean(false);
    static final AtomicBoolean LUU = new AtomicBoolean(false);

    public static void chay() {
        Kiem.nhom("Vong lap chung (VongLapChung) - tick khu/ban do tren pool chung");
        VongLapChung.datDieuKien(new VongLapChung.DieuKien() {
            @Override
            public boolean dung() {
                return DUNG.get();
            }
        }, new VongLapChung.DieuKien() {
            @Override
            public boolean dung() {
                return LUU.get();
            }
        });
        try {
            chuKy();
            ranhVaDanhThuc();
            dangLuu();
            dungHan();
            lanNoiTiep();
            tenLuong();
            nguon();
            khungGoiTin();
        } catch (final Exception e) {
            Kiem.dung("khong nem loi: " + e, false);
        } finally {
            VongLapChung.datDieuKien(new VongLapChung.DieuKien() {
                @Override
                public boolean dung() {
                    return false;
                }
            }, new VongLapChung.DieuKien() {
                @Override
                public boolean dung() {
                    return false;
                }
            });
        }
    }

    static void ngu(final long ms) {
        try {
            Thread.sleep(ms);
        } catch (InterruptedException ignored) {
        }
    }

    /** Khu co nguoi: tick deu theo chu ky (fixed delay), khong chay chong. */
    static void chuKy() {
        final AtomicInteger dem = new AtomicInteger();
        final AtomicInteger dangChay = new AtomicInteger();
        final AtomicBoolean chong = new AtomicBoolean(false);
        final VongLapChung.Nhip n = new VongLapChung.Nhip("kiem chu ky", 50L, new Runnable() {
            @Override
            public void run() {
                if (dangChay.incrementAndGet() > 1) {
                    chong.set(true);
                }
                dem.incrementAndGet();
                ngu(5);
                dangChay.decrementAndGet();
            }
        }, new VongLapChung.CoNguoi() {
            @Override
            public boolean coNguoiThat() {
                return true;
            }
        }).batDau(0L);
        ngu(1000);
        n.dung();
        final int d = dem.get();
        Kiem.dung("tick 50 ms chay ~18 lan/giay (dem=" + d + ")", d >= 12 && d <= 22);
        Kiem.sai("tick khong bao gio chay chong voi chinh no", chong.get());
        Kiem.sai("khu co nguoi khong o trang thai ranh", n.dangRanh());
    }

    /** Khu khong co nguoi: giam nhip nhung VAN chay (hen gio hoi sinh/xoa do), nguoi vao -> chay ngay. */
    static void ranhVaDanhThuc() {
        final AtomicInteger dem = new AtomicInteger();
        final AtomicBoolean coNguoi = new AtomicBoolean(false);
        final AtomicInteger lanDanhThuc = new AtomicInteger(-1);
        final long[] luc = new long[1];
        final VongLapChung.Nhip n = new VongLapChung.Nhip("kiem ranh", 50L, new Runnable() {
            @Override
            public void run() {
                dem.incrementAndGet();
                if (coNguoi.get() && lanDanhThuc.get() < 0) {
                    lanDanhThuc.set((int) (System.currentTimeMillis() - luc[0]));
                }
            }
        }, new VongLapChung.CoNguoi() {
            @Override
            public boolean coNguoiThat() {
                return coNguoi.get();
            }
        }).batDau(0L);
        ngu(300);
        Kiem.dung("khu rong chay tick dau tien (hen gio van duoc xu ly)", dem.get() >= 1);
        Kiem.dung("khu rong chuyen sang ranh", n.dangRanh());
        final int truoc = dem.get();
        ngu(1000);
        Kiem.dung("khu ranh tick thua hon nhieu (" + (dem.get() - truoc) + " lan/1 s, chu ky ranh "
                + VongLapChung.CHU_KY_RANH_MS + " ms)", dem.get() - truoc <= 1);
        luc[0] = System.currentTimeMillis();
        coNguoi.set(true);
        n.danhThuc();
        ngu(300);
        Kiem.dung("nguoi vao khu ranh -> tick chay ngay (" + lanDanhThuc.get() + " ms)",
                lanDanhThuc.get() >= 0 && lanDanhThuc.get() < 150);
        final int sau = dem.get();
        ngu(500);
        Kiem.dung("sau khi co nguoi tro lai chu ky 50 ms (" + (dem.get() - sau) + " lan/0,5 s)", dem.get() - sau >= 6);
        Kiem.sai("het ranh", n.dangRanh());
        n.dung();

        // Khu ranh van chay hen gio dung han: chu ky ranh mac dinh 2 s -> trong 4,5 s co 2-3 tick.
        final AtomicInteger dem2 = new AtomicInteger();
        final VongLapChung.Nhip n2 = new VongLapChung.Nhip("kiem hen gio", 50L, new Runnable() {
            @Override
            public void run() {
                dem2.incrementAndGet();
            }
        }, new VongLapChung.CoNguoi() {
            @Override
            public boolean coNguoiThat() {
                return false;
            }
        }).batDau(0L);
        ngu(4500);
        n2.dung();
        Kiem.dung("khu ranh: tick hen gio moi ~2 s (4,5 s -> " + dem2.get() + " lan)", dem2.get() >= 2 && dem2.get() <= 4);
    }

    /** RealController.savingChar: vong cu ngu 100 ms, khong update. */
    static void dangLuu() {
        final AtomicInteger dem = new AtomicInteger();
        LUU.set(true);
        final VongLapChung.Nhip n = new VongLapChung.Nhip("kiem luu", 20L, new Runnable() {
            @Override
            public void run() {
                dem.incrementAndGet();
            }
        }, null).batDau(0L);
        ngu(400);
        Kiem.bang("dang luu nhan vat: tick khong chay", 0, dem.get());
        LUU.set(false);
        ngu(400);
        Kiem.dung("luu xong: tick chay lai (" + dem.get() + ")", dem.get() >= 5);
        n.dung();
    }

    /** dung() (go khu) va isStopServer: khong con tick nao sau do. */
    static void dungHan() {
        final AtomicInteger dem = new AtomicInteger();
        final VongLapChung.Nhip n = new VongLapChung.Nhip("kiem dung", 20L, new Runnable() {
            @Override
            public void run() {
                dem.incrementAndGet();
            }
        }, null).batDau(0L);
        ngu(200);
        n.dung();
        ngu(50);
        final int luc = dem.get();
        ngu(300);
        Kiem.bang("sau dung(): khong tick them", luc, dem.get());

        final AtomicInteger dem2 = new AtomicInteger();
        final VongLapChung.Nhip n2 = new VongLapChung.Nhip("kiem tat server", 20L, new Runnable() {
            @Override
            public void run() {
                dem2.incrementAndGet();
            }
        }, null).batDau(0L);
        ngu(200);
        DUNG.set(true);
        ngu(100);
        final int luc2 = dem2.get();
        ngu(300);
        DUNG.set(false);
        Kiem.bang("tat server (isStopServer): vong dung han", luc2, dem2.get());
        n2.dung();
    }

    /** Vong goi tin: kich tu nhieu luong cung luc -> moi goi xu ly dung 1 lan, khong bao gio song song. */
    static void lanNoiTiep() throws Exception {
        final java.util.concurrent.ConcurrentLinkedQueue<Integer> hang = new java.util.concurrent.ConcurrentLinkedQueue<Integer>();
        final AtomicInteger daXuLy = new AtomicInteger();
        final AtomicInteger dangChay = new AtomicInteger();
        final AtomicBoolean chong = new AtomicBoolean(false);
        final VongLapChung.Lan[] o = new VongLapChung.Lan[1];
        o[0] = new VongLapChung.Lan(new Runnable() {
            @Override
            public void run() {
                if (dangChay.incrementAndGet() > 1) {
                    chong.set(true);
                }
                int i = 0;
                Integer g;
                while ((g = hang.poll()) != null) {
                    daXuLy.incrementAndGet();
                    if (++i == 500) {
                        break;
                    }
                }
                dangChay.decrementAndGet();
                if (!hang.isEmpty()) {
                    o[0].kich();
                }
            }
        });
        final int luong = 8, moiLuong = 5000;
        final CountDownLatch xong = new CountDownLatch(luong);
        for (int t = 0; t < luong; t++) {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    for (int i = 0; i < moiLuong; i++) {
                        hang.add(i);
                        o[0].kich();
                    }
                    xong.countDown();
                }
            }).start();
        }
        xong.await(10, TimeUnit.SECONDS);
        final long het = System.currentTimeMillis() + 5000;
        while (daXuLy.get() < luong * moiLuong && System.currentTimeMillis() < het) {
            ngu(10);
        }
        Kiem.bang("vong goi tin: xu ly du moi goi, khong mat", luong * moiLuong, daXuLy.get());
        Kiem.sai("vong goi tin khong bao gio chay song song voi chinh no", chong.get());
        o[0].dung();
        hang.add(1);
        o[0].kich();
        ngu(100);
        Kiem.bang("vong da dung() thi khong chay nua", luong * moiLuong, daXuLy.get());
    }

    /** Luong pool co ten, so luong co tran. */
    static void tenLuong() {
        final Set<String> ten = ConcurrentHashMap.newKeySet();
        final VongLapChung.Nhip n = new VongLapChung.Nhip("kiem ten", 10L, new Runnable() {
            @Override
            public void run() {
                ten.add(Thread.currentThread().getName());
            }
        }, null).batDau(0L);
        ngu(200);
        n.dung();
        boolean dungTen = !ten.isEmpty();
        for (String s : ten) {
            dungTen &= s.startsWith("HTBB-NHIP-");
        }
        Kiem.dung("luong tick ten HTBB-NHIP-n (" + ten + ")", dungTen);
        int nhip = 0;
        for (Thread t : Thread.getAllStackTraces().keySet()) {
            if (t.getName().startsWith("HTBB-NHIP-")) {
                nhip++;
            }
        }
        Kiem.dung("so luong tick <= " + VongLapChung.SO_LUONG_NHIP + " (" + nhip + ")", nhip <= VongLapChung.SO_LUONG_NHIP);
        final String tt = VongLapChung.tomTatVaXoa(1);
        Kiem.dung("tom tat [NHIP] co thoi gian chay/tre (" + tt + ")", tt.contains("chay p50=") && tt.contains("tre p50="));
    }

    /** Region/Map/MapVongLap khong con tao thread rieng cho moi khu/ban do. */
    static void nguon() throws Exception {
        final String region = new String(Files.readAllBytes(Paths.get("src/real/Region.java")), StandardCharsets.UTF_8);
        final String vongLap = new String(Files.readAllBytes(Paths.get("src/real/MapVongLap.java")), StandardCharsets.UTF_8);
        final String map = new String(Files.readAllBytes(Paths.get("src/real/Map.java")), StandardCharsets.UTF_8);
        Kiem.sai("Region khong tao thread rieng (new Thread)", region.contains("new Thread("));
        Kiem.dung("Region dung VongLapChung", region.contains("VongLapChung.Nhip") && region.contains("VongLapChung.Lan"));
        Kiem.dung("playerJoin danh thuc tick", region.contains("this.danhThucNhip();"));
        final int a = vongLap.indexOf("static void startSandVilage(");
        Kiem.sai("MapVongLap.startSandVilage khong tao thread", a < 0 || vongLap.substring(a).contains("new Thread("));
        final int b = map.indexOf("public void startLeafVilage()");
        final int c = map.indexOf("private void checkTimeCreateWeather()");
        Kiem.sai("Map.startLeafVilage/startWindVilage/run khong tao thread hay vong lap vo han",
                b < 0 || c < b || map.substring(b, c).contains("new Thread(") || map.substring(b, c).contains("while (true)"));
    }

    /** io.Message.toByteArray (mot lan cap phat) phai ra DUNG khung cu: [cmd][len2] / [-128][cmd][len4]. */
    static void khungGoiTin() throws Exception {
        for (int n : new int[]{0, 1, 300, 32767, 32768, 70000}) {
            final io.Message m = new io.Message(-51);
            final byte[] d = new byte[n];
            for (int i = 0; i < n; i++) {
                d[i] = (byte) (i * 31 + 7);
            }
            m.dos.write(d);
            final java.io.ByteArrayOutputStream bo = new java.io.ByteArrayOutputStream();
            final java.io.DataOutputStream o = new java.io.DataOutputStream(bo);
            if (n > 32767) {
                o.writeByte(-128);
            }
            o.writeByte(-51);
            if (n > 32767) {
                o.writeInt(n);
            } else {
                o.writeShort(n);
            }
            o.write(d);
            Kiem.dung("khung goi tin " + n + " byte giong ban cu", java.util.Arrays.equals(bo.toByteArray(), m.toByteArray()));
            Kiem.dung("toByteArray nho lai cung mang", m.toByteArray() == m.toByteArray());
        }
    }
}
