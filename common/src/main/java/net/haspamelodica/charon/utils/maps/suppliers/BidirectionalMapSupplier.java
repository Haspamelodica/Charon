package net.haspamelodica.charon.utils.maps.suppliers;

import net.haspamelodica.charon.utils.maps.BidirectionalMap;

public interface BidirectionalMapSupplier
{
	public <K, V> BidirectionalMap<K, V> createMap();
}
