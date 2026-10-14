import org.objectweb.asm.*;
import java.nio.file.*;

/**
 * Prepends "if (PauseMenu.<guard>(args...)) return;" to one method of a class,
 * leaving every other byte-level detail of the class alone (ClassWriter(0), no frames:
 * the game's classes are version 45 with no StackMapTable).
 *
 * usage: InjectGuards in.class out.class methodName methodDesc guardName guardDesc
 * guardDesc's parameters are loaded from the target method's own arguments, in order
 * (a prefix of them), so a static guard receives the same values.
 */
public class InjectGuards {
    public static void main(String[] a) throws Exception {
        final String mName = a[2], mDesc = a[3], gName = a[4], gDesc = a[5];
        byte[] in = Files.readAllBytes(Paths.get(a[0]));
        ClassReader cr = new ClassReader(in);
        ClassWriter cw = new ClassWriter(cr, 0);
        final int[] hits = {0};
        cr.accept(new ClassVisitor(Opcodes.ASM9, cw) {
            public MethodVisitor visitMethod(int acc, String n, String d, String sig, String[] ex) {
                MethodVisitor mv = super.visitMethod(acc, n, d, sig, ex);
                if (!n.equals(mName) || !d.equals(mDesc)) return mv;
                hits[0]++;
                final boolean isStatic = (acc & Opcodes.ACC_STATIC) != 0;
                final Type[] gArgs = Type.getArgumentTypes(gDesc);
                int stack = 0;
                for (Type t : gArgs) stack += t.getSize();
                final int need = Math.max(stack, 1);
                return new MethodVisitor(Opcodes.ASM9, mv) {
                    public void visitCode() {
                        super.visitCode();
                        int slot = isStatic ? 0 : 1;
                        for (Type t : gArgs) {
                            super.visitVarInsn(t.getOpcode(Opcodes.ILOAD), slot);
                            slot += t.getSize();
                        }
                        super.visitMethodInsn(Opcodes.INVOKESTATIC, "game/render/screen/AutoGuard", gName, gDesc, false);
                        Label go = new Label();
                        super.visitJumpInsn(Opcodes.IFEQ, go);
                        super.visitInsn(Opcodes.RETURN);
                        super.visitLabel(go);
                    }
                    public void visitMaxs(int maxStack, int maxLocals) {
                        super.visitMaxs(Math.max(maxStack, need), maxLocals);
                    }
                };
            }
        }, 0);
        if (hits[0] != 1) throw new IllegalStateException("method matched " + hits[0] + " times");
        Files.write(Paths.get(a[1]), cw.toByteArray());
        System.out.println("injected " + gName + " into " + mName + mDesc);
    }
}
