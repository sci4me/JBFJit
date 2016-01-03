package com.sci.jbfjit.ir.insn;

import com.sci.jbfjit.ir.*;

public final class Left extends Instruction implements IContractable {
	private int count;
	
	public Left(final int count) {
		this.count = count;
	}
	
	@Override
	public void setCount(final int count) {
		this.count = count;
	}
	
	@Override
	public int getCount() {
		return this.count;
	}
}