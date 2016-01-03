package com.sci.jbfjit.opt;

import java.util.*;

import com.sci.jbfjit.ir.*;
import com.sci.jbfjit.ir.insn.*;

public final class Opt {
	public static void optimize(final List<Instruction> ir) {
		Contract.run(ir);
		ClearLoopRemoval.run(ir);
	}
	
	private Opt() {
		
	}
}