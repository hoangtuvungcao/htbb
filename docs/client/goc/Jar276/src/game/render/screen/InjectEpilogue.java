import org.objectweb.asm.*;
import java.nio.file.*;

/**
 * Inserts "aload_1; invokestatic owner.name(desc)" before every RETURN of one instance
 * method (used to draw an overlay at the end of Canvas.paint(Graphics)). Other methods are
 * copied byte-for-byte (ClassWriter(cr, 0)); the class stays version 45 with no frames.
 *
 * usage: InjectEpilogue in.class out.class methodName methodDesc owner name desc
 */
public class InjectEpilogue {
    public static void main(String[] a) throws Exception {
        final String mName = a[2], mDesc = a[3], owner = a[4], name = a[5], desc = a[6];
        ClassReader cr = new ClassReader(Files.readAllBytes(Paths.get(a[0])));
        ClassWriter cw = new ClassWriter(cr, 0);
        final int[] hits = {0, 0};
        cr.accept(new ClassVisitor(Opcodes.ASM9, cw) {
            public MethodVisitor visitMethod(int acc, String n, String d, String sig, String[] ex) {
                MethodVisitor mv = super.visitMethod(acc, n, d, sig, ex);
                if (!n.equals(mName) || !d.equals(mDesc)) return mv;
                hits[0]++;
                return new MethodVisitor(Opcodes.ASM9, mv) {
                    public void visitInsn(int op) {
                        if (op == Opcodes.RETURN) {
                            super.visitVarInsn(Opcodes.ALOAD, 1);
                            super.visitMethodInsn(Opcodes.INVOKESTATIC, owner, name, desc, false);
                            hits[1]++;
                        }
                        super.visitInsn(op);
                    }
                    public void visitMaxs(int s, int l) {
                        super.visitMaxs(Math.max(s, 1), l);
                    }
                };
            }
        }, 0);
        if (hits[0] != 1 || hits[1] == 0) throw new IllegalStateException("method " + hits[0] + " returns " + hits[1]);
        Files.write(Paths.get(a[1]), cw.toByteArray());
        System.out.println("epilogue before " + hits[1] + " return(s) of " + mName);
    }
}
