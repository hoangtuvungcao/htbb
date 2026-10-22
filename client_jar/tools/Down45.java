import org.objectweb.asm.*;
import java.nio.file.*;
import java.util.*;

/**
 * Hạ class do javac mới sinh về dạng máy J2ME chạy được:
 *  - version 45.3 (CLDC 1.0);
 *  - StringBuilder -> StringBuffer (CLDC không có StringBuilder, javac sinh nó cho mọi phép nối chuỗi;
 *    microemulator chạy trên JVM máy tính nên vẫn chạy, giả lập CLDC thật thì vỡ).
 * Đồng thời KIỂM: lỗi build nếu code dùng thứ CLDC 1.0 không có (Objects, autoboxing valueOf, lambda,
 * class literal X.class, Iterator/for-each trên Collection...) - sửa source cho hợp J2ME.
 *   java Down45 x vào1 ra1 vào2 ra2 ...
 */
public class Down45 {
  static final List<String> loi = new ArrayList<>();

  static String sb(String s) {
    return s == null ? null : s.replace("java/lang/StringBuilder", "java/lang/StringBuffer");
  }

  static final String[] CAM_LOP = {"java/util/Objects", "java/lang/invoke/", "java/lang/Iterable", "java/util/Iterator",
      "java/lang/AutoCloseable", "java/util/ArrayList", "java/util/HashMap", "java/util/List", "java/util/Map"};
  static final String[] HOP = {"java/lang/Integer", "java/lang/Long", "java/lang/Short", "java/lang/Byte",
      "java/lang/Character", "java/lang/Boolean", "java/lang/Float", "java/lang/Double"};

  // ---- 0.9.7.3: KIỂM TRẮNG theo API chuẩn (CoffeeCat iOS: System.nanoTime() không có -> mọi nhịp game ném lỗi).
  // Danh sách đen ở trên bỏ sót mọi hàm Java SE trên lớp CLDC có thật (System.nanoTime, String.isEmpty...): nay MỌI
  // tham chiếu tới lớp java/* và javax/microedition/* phải có trong jar API chuẩn (-Dapi=cldcapi11.jar:midpapi20.jar
  // [:thư mục api WMA]), kể cả hàm / trường kế thừa từ lớp cha. Không đặt -Dapi thì bỏ qua kiểm này.
  static final Map<String, String[]> API_CHA = new HashMap<>();
  static final Map<String, Set<String>> API_THANH = new HashMap<>();

  static void napApi(String ds) throws Exception {
    for (String duong : ds.split(java.io.File.pathSeparator)) {
      if (duong.isEmpty()) continue;
      Path p = Paths.get(duong);
      List<byte[]> lop = new ArrayList<>();
      if (Files.isDirectory(p)) {
        try (java.util.stream.Stream<Path> st = Files.walk(p)) {
          for (Path x : (Iterable<Path>) st.filter(y -> y.toString().endsWith(".class"))::iterator) lop.add(Files.readAllBytes(x));
        }
      } else {
        try (java.util.zip.ZipFile z = new java.util.zip.ZipFile(p.toFile())) {
          for (java.util.Enumeration<? extends java.util.zip.ZipEntry> e = z.entries(); e.hasMoreElements(); ) {
            java.util.zip.ZipEntry en = e.nextElement();
            if (en.getName().endsWith(".class")) lop.add(z.getInputStream(en).readAllBytes());
          }
        }
      }
      for (byte[] b : lop) {
        ClassReader cr = new ClassReader(b);
        final Set<String> tv = new HashSet<>();
        String[] cha = new String[1 + cr.getInterfaces().length];
        cha[0] = cr.getSuperName();
        System.arraycopy(cr.getInterfaces(), 0, cha, 1, cr.getInterfaces().length);
        cr.accept(new ClassVisitor(Opcodes.ASM9) {
          public FieldVisitor visitField(int acc, String n, String d, String sg, Object v) {
            if ((acc & Opcodes.ACC_PRIVATE) == 0) tv.add(n + ":" + d);
            return null;
          }
          public MethodVisitor visitMethod(int acc, String n, String d, String sg, String[] ex) {
            if ((acc & Opcodes.ACC_PRIVATE) == 0) tv.add(n + d);
            return null;
          }
        }, ClassReader.SKIP_CODE);
        API_CHA.put(cr.getClassName(), cha);
        API_THANH.put(cr.getClassName(), tv);
      }
    }
  }

  static boolean laApi(String o) {
    return o.startsWith("java/") || o.startsWith("javax/microedition/") || o.startsWith("javax/wireless/");
  }

  static boolean coThanh(String o, String khoa, int sau) {
    if (o == null || sau > 20) return false;
    Set<String> tv = API_THANH.get(o);
    if (tv == null) return false;
    if (tv.contains(khoa)) return true;
    for (String c : API_CHA.get(o)) if (coThanh(c, khoa, sau + 1)) return true;
    return false;
  }

  /** o.n + d (đã đổi StringBuilder) có trong API chuẩn? field: d là kiểu trường. */
  static void kiemApi(String lop, String o, String n, String d, boolean truong) {
    if (API_THANH.isEmpty() || o.startsWith("[") || !laApi(o)) return;
    if (!API_THANH.containsKey(o)) {
      loi.add(lop + ": lop khong co trong CLDC 1.1 / MIDP 2.0: " + o);
      return;
    }
    if (!coThanh(o, truong ? n + ":" + d : n + d, 0)) loi.add(lop + ": khong co trong CLDC 1.1 / MIDP 2.0: " + o + "." + n + d);
  }

  static void kiemLop(String lop, String t) {
    if (API_THANH.isEmpty() || t == null) return;
    String o = t.startsWith("[") ? Type.getType(t).getElementType().getInternalName() : t;
    if (t.startsWith("[") && Type.getType(t).getElementType().getSort() != Type.OBJECT) return;
    if (laApi(o) && !API_THANH.containsKey(o)) loi.add(lop + ": lop khong co trong CLDC 1.1 / MIDP 2.0: " + o);
  }

  static void soat(String lop, String o, String n, String d) {
    for (String c : CAM_LOP) {
      if (o.startsWith(c)) loi.add(lop + ": " + o + "." + n + d);
    }
    if (n.equals("valueOf") && Arrays.asList(HOP).contains(o) && !d.startsWith("(Ljava/lang/String;")) {
      loi.add(lop + ": autoboxing " + o + "." + n + d);
    }
    if (n.equals("addSuppressed")) loi.add(lop + ": try-with-resources " + o + "." + n);
  }

  public static void main(String[] a) throws Exception {
    if (System.getProperty("api") != null) napApi(System.getProperty("api"));
    if (a[0].equals("d")) {
      // java Down45 d <thư mục vào> <thư mục ra>: hạ cả cây class
      Path vao = Paths.get(a[1]), ra = Paths.get(a[2]);
      List<String> ds = new ArrayList<>();
      ds.add("x");
      try (java.util.stream.Stream<Path> st = Files.walk(vao)) {
        for (Path p : (Iterable<Path>) st.filter(x -> x.toString().endsWith(".class"))::iterator) {
          Path o = ra.resolve(vao.relativize(p));
          Files.createDirectories(o.getParent());
          ds.add(p.toString());
          ds.add(o.toString());
        }
      }
      a = ds.toArray(new String[0]);
    }
    for (int i = 1; i < a.length; i += 2) {
      byte[] in = Files.readAllBytes(Paths.get(a[i]));
      ClassReader cr = new ClassReader(in);
      ClassWriter cw = new ClassWriter(0);
      final String lop = cr.getClassName();
      cr.accept(new ClassVisitor(Opcodes.ASM9, cw) {
        public void visit(int v, int acc, String n, String sig, String sup, String[] itf) {
          super.visit((3 << 16) | 45, acc, n, sig, sup, itf);
          kiemLop(lop, sb(sup));
          if (itf != null) for (String x : itf) kiemLop(lop, x);
        }
        public FieldVisitor visitField(int acc, String n, String desc, String sig, Object val) {
          return super.visitField(acc, n, sb(desc), sb(sig), val);
        }
        public MethodVisitor visitMethod(int acc, String n, String desc, String sig, String[] ex) {
          return new MethodVisitor(Opcodes.ASM9, super.visitMethod(acc, n, sb(desc), sb(sig), ex)) {
            public void visitTypeInsn(int op, String t) { kiemLop(lop, sb(t)); super.visitTypeInsn(op, sb(t)); }
            public void visitFieldInsn(int op, String o, String n2, String d) {
              soat(lop, o, n2, d);
              kiemApi(lop, sb(o), n2, sb(d), true);
              super.visitFieldInsn(op, sb(o), n2, sb(d));
            }
            public void visitMethodInsn(int op, String o, String n2, String d, boolean itf) {
              soat(lop, o, n2, d);
              kiemApi(lop, sb(o), n2, sb(d), false);
              super.visitMethodInsn(op, sb(o), n2, sb(d), itf);
            }
            public void visitInvokeDynamicInsn(String n2, String d, Handle h, Object... b) {
              loi.add(lop + ": lambda / invokedynamic " + n2 + d);
              super.visitInvokeDynamicInsn(n2, d, h, b);
            }
            public void visitLocalVariable(String n2, String d, String s, Label a1, Label a2, int i) { super.visitLocalVariable(n2, sb(d), sb(s), a1, a2, i); }
            public void visitMultiANewArrayInsn(String d, int dims) { super.visitMultiANewArrayInsn(sb(d), dims); }
            public void visitTryCatchBlock(Label a1, Label a2, Label a3, String t) { kiemLop(lop, sb(t)); super.visitTryCatchBlock(a1, a2, a3, sb(t)); }
            public void visitLdcInsn(Object o) {
              if (o instanceof Type) {
                // class 45 không có hằng CONSTANT_Class trong ldc (X.class) - dùng Class.forName("...")
                loi.add(lop + ": class literal " + o);
                o = Type.getType(sb(((Type) o).getDescriptor()));
              }
              super.visitLdcInsn(o);
            }
          };
        }
      }, ClassReader.SKIP_FRAMES);
      Files.write(Paths.get(a[i+1]), cw.toByteArray());
    }
    if (!loi.isEmpty()) {
      System.err.println("Code dung API ma may J2ME (CLDC 1.1 / MIDP 2.0) khong co:");
      for (String s : new TreeSet<>(loi)) System.err.println("  " + s);
      System.exit(1);
    }
  }
}
