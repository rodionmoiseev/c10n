package c10n;

public interface C10NFactory {
	public <T> T get(Class<T> c10nInterface);

	public void configure(AbstractC10NConfiguration conf);	
}
