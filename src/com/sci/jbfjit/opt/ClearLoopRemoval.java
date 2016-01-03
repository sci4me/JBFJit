package com.sci.jbfjit.opt;

import java.util.List;
import java.util.ArrayList;

import com.sci.jbfjit.ir.*;
import com.sci.jbfjit.ir.insn.*;

public final class ClearLoopRemoval {
	public static void run(final List<Instruction> ir) {
		final List<Instruction> nir = new ArrayList<>();
		
		for(int i = 0; i < ir.size(); i++) {
			if(i + 2 < ir.size()) {
				final Instruction a = ir.get(i);
				final Instruction b = ir.get(i + 1);
				final Instruction c = ir.get(i + 2);
				
				if(a instanceof Open && b instanceof Sub && c instanceof Close) {
					nir.add(new Set(0));
				} else {
					nir.add(a);
					nir.add(b);
					nir.add(c);
				}

				i += 2;
			} else {
				nir.add(ir.get(i));
			}
		}
		
		ir.clear();
		ir.addAll(nir);
	}

	private ClearLoopRemoval() {
		
	}
}