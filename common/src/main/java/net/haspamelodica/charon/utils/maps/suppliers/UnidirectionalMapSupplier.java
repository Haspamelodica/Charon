package net.haspamelodica.charon.utils.maps.suppliers;

import net.haspamelodica.charon.utils.maps.UnidirectionalMap;

public interface UnidirectionalMapSupplier
{
	public <K, V> UnidirectionalMap<K, V> createMap();

	public static UnidirectionalMapSupplier fromMapSupplier(MapSupplier mapSupplier)
	{
		return mapSupplier::createUnidirectionalMap;
	}
}
