package net.haspamelodica.charon.util;

import java.util.function.Supplier;

public class LazyValue<V>
{
	private final Supplier<V> createValue;

	private volatile V value;

	public LazyValue(Supplier<V> createValue)
	{
		this.createValue = createValue;
	}

	public V get()
	{
		if(value != null)
			return value;

		synchronized(this)
		{
			if(value != null)
				return value;

			value = createValue.get();
			return value;
		}
	}
}