package com.sci.jbfjit.jit;

import java.io.*;
import java.util.List;
import java.util.Deque;
import java.util.ArrayDeque;

import org.objectweb.asm.*;
import static org.objectweb.asm.Opcodes.*;

import com.sci.jbfjit.ir.*;
import com.sci.jbfjit.ir.insn.*;

public final class JIT {
	private static final JITClassLoader classLoader = new JITClassLoader();
	private static final String CLASS_NAME_PREFIX = "JittedBFCode$";
	
	private static int counter;
	
	public static Runnable jit(final List<Instruction> ir) {
		final ClassWriter cw = new ClassWriter(ClassWriter.COMPUTE_FRAMES | ClassWriter.COMPUTE_MAXS);
		
		final String className = CLASS_NAME_PREFIX + JIT.counter;
		JIT.counter++;
		
		cw.visit(52, ACC_PUBLIC | ACC_SUPER | ACC_SYNTHETIC, className, null, "java/lang/Object", new String[]{Type.getInternalName(Runnable.class)});
		cw.visitSource(null, null);
		
		JIT.generateFields(cw);
		JIT.generateConstructor(cw, className);
		JIT.generateRun(cw, ir, className);
		
		try {
			return (Runnable) JIT.classLoader.loadClass(className, cw.toByteArray()).newInstance();
		} catch(final Throwable t) {
			t.printStackTrace();
			System.exit(1);
		}
		
		return null;
	}
	
	private static void generateFields(final ClassWriter cw) {
		cw.visitField(ACC_PRIVATE | ACC_SYNTHETIC, "data", "[I", null, null).visitEnd();
		cw.visitField(ACC_PRIVATE | ACC_SYNTHETIC, "dp", "I", null, null).visitEnd();
	}
	
	private static void generateConstructor(final ClassWriter cw, final String className) {
        final MethodVisitor mv = cw.visitMethod(ACC_PUBLIC, "<init>", "()V", null, null);
		
		mv.visitVarInsn(ALOAD, 0);
		mv.visitLdcInsn(30000);
		mv.visitIntInsn(NEWARRAY, T_INT);
		mv.visitFieldInsn(PUTFIELD, className, "data", "[I");
		
        mv.visitVarInsn(ALOAD, 0);
        mv.visitMethodInsn(INVOKESPECIAL, "java/lang/Object", "<init>", "()V", false);
        mv.visitInsn(RETURN);
        mv.visitMaxs(0, 0);
        mv.visitEnd();
    }
	
	private static void generateRun(final ClassWriter cw, final List<Instruction> ir, final String className) {
		final MethodVisitor rmv = cw.visitMethod(ACC_PUBLIC | ACC_SYNTHETIC, "run", "()V", null, null);

		rmv.visitVarInsn(ALOAD, 0);
		rmv.visitMethodInsn(INVOKEVIRTUAL, className, "m0", "()V");

		final LoopHolder loopHolder = new LoopHolder();

		final Deque<MethodVisitor> methods = new ArrayDeque<>();
		MethodVisitor mv = cw.visitMethod(ACC_PRIVATE | ACC_SYNTHETIC, "m0", "()V", null, null);
		int methodIndex = 0;
		for(int i = 0; i < ir.size(); i++) {
			final Instruction insn = ir.get(i);
			
			if(insn instanceof Open) {
				methods.push(mv);
			
				JIT.generateInstruction(mv, insn, loopHolder, className);
				
				methodIndex++;
				
				mv.visitVarInsn(ALOAD, 0);
				mv.visitMethodInsn(INVOKEVIRTUAL, className, "m" + methodIndex, "()V");
				
				mv = cw.visitMethod(ACC_PRIVATE | ACC_SYNTHETIC, "m" + methodIndex, "()V", null, null);
			} else if(insn instanceof Close) {
				mv.visitInsn(RETURN);
				mv.visitMaxs(0, 0);
				mv.visitEnd();
				
				mv = methods.pop();
				
				JIT.generateInstruction(mv, insn, loopHolder, className);
			} else {
				JIT.generateInstruction(mv, insn, loopHolder, className);
			}
		}	
		mv.visitInsn(RETURN);
		mv.visitMaxs(0, 0);
		mv.visitEnd();
		
		rmv.visitInsn(RETURN);
		rmv.visitMaxs(0, 0);
		rmv.visitEnd();
	}
	
	public static void generateInstruction(final MethodVisitor mv, final Instruction insn, final LoopHolder loopHolder, final String className) {
		if(insn instanceof Add) {
			mv.visitVarInsn(ALOAD, 0);
			mv.visitFieldInsn(GETFIELD, className, "data", "[I");
			mv.visitVarInsn(ALOAD, 0);
			mv.visitFieldInsn(GETFIELD, className, "dp", "I");
			mv.visitInsn(DUP2);
			mv.visitInsn(IALOAD);
			mv.visitLdcInsn(((Add) insn).getCount());
			mv.visitInsn(IADD);
			mv.visitInsn(IASTORE);
		} else if(insn instanceof Sub) {
			mv.visitVarInsn(ALOAD, 0);
			mv.visitFieldInsn(GETFIELD, className, "data", "[I");
			mv.visitVarInsn(ALOAD, 0);
			mv.visitFieldInsn(GETFIELD, className, "dp", "I");
			mv.visitInsn(DUP2);
			mv.visitInsn(IALOAD);
			mv.visitLdcInsn(((Sub) insn).getCount());
			mv.visitInsn(ISUB);
			mv.visitInsn(IASTORE);
		} else if(insn instanceof Right) {
			mv.visitVarInsn(ALOAD, 0);
			mv.visitInsn(DUP);
			mv.visitFieldInsn(GETFIELD, className, "dp", "I");
			mv.visitLdcInsn(((Right) insn).getCount());
			mv.visitInsn(IADD);
			mv.visitFieldInsn(PUTFIELD, className, "dp", "I");
		} else if(insn instanceof Left) {
			mv.visitVarInsn(ALOAD, 0);
			mv.visitInsn(DUP);
			mv.visitFieldInsn(GETFIELD, className, "dp", "I");
			mv.visitLdcInsn(((Left) insn).getCount());
			mv.visitInsn(ISUB);
			mv.visitFieldInsn(PUTFIELD, className, "dp", "I");
		} else if(insn instanceof In) {
			mv.visitVarInsn(ALOAD, 0);
			mv.visitFieldInsn(GETFIELD, className, "data", "[I");
			mv.visitVarInsn(ALOAD, 0);
			mv.visitFieldInsn(GETFIELD, className, "dp", "I");
			mv.visitMethodInsn(INVOKESTATIC, "com/sci/jbfjit/jit/JIT", "getchar", "()I");
			mv.visitInsn(IASTORE);
		} else if(insn instanceof Out) {
			mv.visitFieldInsn(GETSTATIC, "java/lang/System", "out", "Ljava/io/PrintStream;");
			mv.visitVarInsn(ALOAD, 0);
			mv.visitFieldInsn(GETFIELD, className, "data", "[I");
			mv.visitVarInsn(ALOAD, 0);
			mv.visitFieldInsn(GETFIELD, className, "dp", "I");
			mv.visitInsn(IALOAD);
			mv.visitInsn(I2C);
			mv.visitMethodInsn(INVOKEVIRTUAL, "java/io/PrintStream", "print", "(C)V");
		} else if(insn instanceof Open) {
			if(loopHolder.loop != null) {
				loopHolder.loops.push(loopHolder.loop);
			}
			
			final Loop loop = new Loop();
			loop.start = new Label();
			loop.end = new Label();
			loopHolder.loop = loop;
			
			mv.visitVarInsn(ALOAD, 0);
			mv.visitFieldInsn(GETFIELD, className, "data", "[I");
			mv.visitVarInsn(ALOAD, 0);
			mv.visitFieldInsn(GETFIELD, className, "dp", "I");
			mv.visitInsn(IALOAD);
			mv.visitJumpInsn(IFEQ, loop.end);
			mv.visitLabel(loop.start);
		} else if(insn instanceof Close) {
			mv.visitVarInsn(ALOAD, 0);
			mv.visitFieldInsn(GETFIELD, className, "data", "[I");
			mv.visitVarInsn(ALOAD, 0);
			mv.visitFieldInsn(GETFIELD, className, "dp", "I");
			mv.visitInsn(IALOAD);
			mv.visitJumpInsn(IFNE, loopHolder.loop.start);
			mv.visitLabel(loopHolder.loop.end);
			
			if(!loopHolder.loops.isEmpty()) {
				loopHolder.loop = loopHolder.loops.pop();
			}
		} else if(insn instanceof Set) {
			mv.visitVarInsn(ALOAD, 0);
			mv.visitFieldInsn(GETFIELD, className, "data", "[I");
			mv.visitVarInsn(ALOAD, 0);
			mv.visitFieldInsn(GETFIELD, className, "dp", "I");
			mv.visitLdcInsn(((Set) insn).getValue());
			mv.visitInsn(IASTORE);	
		}
	}
	
	private static class LoopHolder {
		public Deque<Loop> loops;
		public Loop loop;
		
		public LoopHolder() {
			this.loops = new ArrayDeque<>();
		}
	}
	
	private static class Loop {
		public Label start;
		public Label end;
	}
	
	public static int getchar() {
		try {
			return System.in.read();
		} catch(final Throwable t) {
			return 0;
		}
	}
	
	private JIT() {
		
	}
}