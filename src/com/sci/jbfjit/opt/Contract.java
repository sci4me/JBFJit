package com.sci.jbfjit.opt;

import java.util.*;

import com.sci.jbfjit.ir.*;
import com.sci.jbfjit.ir.insn.*;

public final class Contract {
	public static void run(final List<Instruction> ir) {
		final List<Instruction> nir = new ArrayList<>();
		
		for(int i = 0; i < ir.size(); i++) {
			final Instruction insn = ir.get(i);
			
			if(insn instanceof IContractable) {
				int count = 1;
				
				while(i + 1 < ir.size() && ir.get(i + 1).getClass().equals(insn.getClass())) {
					i++;
					count++;
				}
			
				((IContractable) insn).setCount(count);	
			}
			
			nir.add(insn);
		}
		
		ir.clear();
		ir.addAll(nir);
	}

	private Contract() {
		
	}
}