package ch.ethz.pa;

import soot.tagkit.LineNumberTag;

public class PaLineNumberTag extends LineNumberTag {
	
	public PaLineNumberTag(int ln) {
		super(ln);
	}

	@Override
	public String getName() {
		return "PaLineNumber";
	}
	
	@Override
	public boolean equals(Object o) {
		if (!(o instanceof PaLineNumberTag)) return false;
		PaLineNumberTag other = (PaLineNumberTag) o;
		return other.getLineNumber() == getLineNumber();
	}
}
