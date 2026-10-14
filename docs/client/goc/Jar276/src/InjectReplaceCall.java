import org.objectweb.asm.*;
import java.nio.file.*;

/**
 * Replaces every INVOKEVIRTUAL owner.name(desc) in one class with an INVOKESTATIC to
 * newOwner.newName(newDesc) taking the receiver as its first argument (same stack shape).
 * Untouched methods are copied byte-for-byte (ClassWriter(cr, 0)); no frames (version 45).
 *
 * usage: InjectReplaceCall in.class out.class owner name desc newOwner newName newDesc expectedCount
 */
public class InjectReplaceCall {
    public static void main(String[] a) throws Exception {
        final String owner = a[2], name = a[3], desc = a[4], nOwner = a[5], nName = a[6], nDesc = a[7];
        final int expected = Integer.parseInt(a[8]);
        ClassReader cr = new ClassReader(Files.readAllBytes(Paths.get(a[0])));
        ClassWriter cw = new ClassWriter(cr, 0);
        final int[] hits = {0};
        cr.accept(new ClassVisitor(Opcodes.ASM9, cw) {
            public MethodVisitor visitMethod(int acc, String n, String d, String sig, String[] ex) {
                return new MethodVisitor(Opcodes.ASM9, super.visitMethod(acc, n, d, sig, ex)) {
                    public void visitMethodInsn(int op, String o, String mn, String md, boolean itf) {
                        if (op == Opcodes.INVOKEVIRTUAL && o.equals(owner) && mn.equals(name) && md.equals(desc)) {
                            hits[0]++;
                            super.visitMethodInsn(Opcodes.INVOKESTATIC, nOwner, nName, nDesc, false);
                            return;
                        }
                        super.visitMethodInsn(op, o, mn, md, itf);
                    }
                };
            }
        }, 0);
        if (hits[0] != expected) throw new IllegalStateException("replaced " + hits[0] + ", expected " + expected);
        Files.write(Paths.get(a[1]), cw.toByteArray());
        System.out.println("replaced " + hits[0] + " call(s)");
    }
}
