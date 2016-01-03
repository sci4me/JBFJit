package com.sci.jbfjit.ir;

import java.util.*;

import com.sci.jbfjit.ir.insn.*;

public final class IR {
	public static List<Instruction> parse(final String code) {
		final List<Instruction> result = new ArrayList<>();
		
		for(int i = 0; i < code.length(); i++) {
			switch(code.charAt(i)) {
				case '+':
					result.add(new Add(1));
					break;
				case '-':
					result.add(new Sub(1));
					break;
				case '>':
					result.add(new Right(1));
					break;
				case '<':
					result.add(new Left(1));
					break;
				case ',':
					result.add(new In());
					break;
				case '.':
					result.add(new Out());
					break;
				case '[':
					result.add(new Open());
					break;
				case ']':
					result.add(new Close());
					break;
			}
		}
		
		return result;
	}

	private IR() {
		
	}
}