package com.sci.jbfjit;

import java.io.*;
import java.nio.file.*;
import java.nio.charset.*;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.Stack;

import com.sci.jbfjit.ir.*;
import com.sci.jbfjit.ir.insn.*;
import com.sci.jbfjit.jit.*;
import com.sci.jbfjit.opt.*;

public final class Main {
	public static void main(final String[] args) throws Throwable {
		if(args.length != 1) {
			System.out.println("Usage: jbfjit <file>");
			return;
		}
		
		final List<Instruction> ir = IR.parse(Main.readFile(args[0]));
		
		Opt.optimize(ir);
		
		final Runnable jitted = JIT.jit(ir);
		
		final long start = System.currentTimeMillis();
		jitted.run();
		final long end = System.currentTimeMillis();
		final long delay = end - start;
		
		System.out.printf(
			"%02d:%02d:%02d%n",
			((int) ((delay / (1000*60*60)) % 24)),
			((int) ((delay / (1000*60)) % 60)),
			((int) (delay / 1000) % 60)
		);
	}
	
	private static String readFile(final String path) throws IOException 
	{
		final byte[] encoded = Files.readAllBytes(Paths.get(path));
		return new String(encoded, Charset.defaultCharset());
	}
	
	private Main() {
		
	}
}