package ch.ethz.pa;

public interface AbstractDomain {
	public void copyFrom(AbstractDomain other);
	public AbstractDomain plus(AbstractDomain a);
	public AbstractDomain minus(AbstractDomain a);
	public AbstractDomain multiply(AbstractDomain a);
	public AbstractDomain join(AbstractDomain a);
	public AbstractDomain meet(AbstractDomain a);
	public AbstractDomain getTop();
	public AbstractDomain getBot();
}
