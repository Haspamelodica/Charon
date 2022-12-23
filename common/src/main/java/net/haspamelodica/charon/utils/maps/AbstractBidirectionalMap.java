package net.haspamelodica.charon.utils.maps;

public abstract class AbstractBidirectionalMap<K, V> implements BidirectionalMap<K, V>
{
	@Override
	public String toString()
	{
		return defaultToString();
	}
}
