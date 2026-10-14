import org.objectweb.asm.*;
import java.nio.file.*;
public class Down45 {
  static String sb(String s) {
    return s == null ? null : s.replace("java/lang/StringBuilder", "java/lang/StringBuffer");
  }
  public static void main(String[] a) throws Exception {
    for (int i = 1; i < a.length; i += 2) {
      byte[] in = Files.readAllBytes(Paths.get(a[i]));
      ClassReader cr = new ClassReader(in);
      ClassWriter cw = new ClassWriter(0);
      cr.accept(new ClassVisitor(Opcodes.ASM9, cw) {
        public void visit(int v, int acc, String n, String sig, String sup, String[] itf) {
          super.visit((3 << 16) | 45, acc, n, sig, sup, itf);
        }
        // J2ME (CLDC) KHONG co java.lang.StringBuilder - javac sinh ra no cho moi phep noi chuoi.
        // microemulator chay tren JVM may tinh nen van chay, gia lap CLDC that (CoffeeCat) thi
        // vo. Doi het sang StringBuffer (cung ten ham, co trong CLDC).
        public FieldVisitor visitField(int acc, String n, String desc, String sig, Object val) {
          return super.visitField(acc, n, sb(desc), sb(sig), val);
        }
        public MethodVisitor visitMethod(int acc, String n, String desc, String sig, String[] ex) {
          return new MethodVisitor(Opcodes.ASM9, super.visitMethod(acc, n, sb(desc), sb(sig), ex)) {
            public void visitTypeInsn(int op, String t) { super.visitTypeInsn(op, sb(t)); }
            public void visitFieldInsn(int op, String o, String n2, String d) { super.visitFieldInsn(op, sb(o), n2, sb(d)); }
            public void visitMethodInsn(int op, String o, String n2, String d, boolean itf) { super.visitMethodInsn(op, sb(o), n2, sb(d), itf); }
            public void visitLocalVariable(String n2, String d, String s, Label a1, Label a2, int i) { super.visitLocalVariable(n2, sb(d), sb(s), a1, a2, i); }
            public void visitMultiANewArrayInsn(String d, int dims) { super.visitMultiANewArrayInsn(sb(d), dims); }
            public void visitTryCatchBlock(Label a1, Label a2, Label a3, String t) { super.visitTryCatchBlock(a1, a2, a3, sb(t)); }
            public void visitLdcInsn(Object o) {
              if (o instanceof Type) { o = Type.getType(sb(((Type) o).getDescriptor())); }
              super.visitLdcInsn(o);
            }
          };
        }
      }, ClassReader.SKIP_FRAMES);
      Files.write(Paths.get(a[i+1]), cw.toByteArray());
    }
  }
}
