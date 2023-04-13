package net.haspamelodica.charon.utils.maps.suppliers;

import net.haspamelodica.charon.utils.maps.UnidirectionalMapWithRemovalIterator;

public interface UnidirectionalMapWithRemovalIteratorSupplier extends UnidirectionalMapSupplier
{
	@Override
	public <K, V> UnidirectionalMapWithRemovalIterator<K, V> createMap();

	public static UnidirectionalMapWithRemovalIteratorSupplier fromMapSupplier(MapSupplier mapSupplier)
	{
		return mapSupplier::createUnidirectionalMap;
	}
}
