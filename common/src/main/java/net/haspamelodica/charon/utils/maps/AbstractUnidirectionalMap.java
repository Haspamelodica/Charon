package net.haspamelodica.charon.utils.maps;

public abstract class AbstractUnidirectionalMap<K, V> implements UnidirectionalMap<K, V>
{
	@Override
	public String toString()
	{
		return defaultToString();
	}
}
