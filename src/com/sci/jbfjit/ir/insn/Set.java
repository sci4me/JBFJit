package com.sci.jbfjit.ir.insn;

import com.sci.jbfjit.ir.*;

public final class Set extends Instruction {
	private int value;
	
	public Set(final int value) {
		this.value = value;
	}
	
	public void setValue(final int value) {
		this.value = value;
	}
	
	public int getValue() {
		return this.value;
	}
}