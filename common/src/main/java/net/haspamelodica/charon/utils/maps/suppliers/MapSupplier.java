package net.haspamelodica.charon.utils.maps.suppliers;

import java.util.Map;

import net.haspamelodica.charon.utils.maps.UnidirectionalMapImpl;
import net.haspamelodica.charon.utils.maps.UnidirectionalMapWithRemovalIterator;

public interface MapSupplier
{
	public <K, V> Map<K, V> createMap();

	public default <K, V> UnidirectionalMapWithRemovalIterator<K, V> createUnidirectionalMap()
	{
		return new UnidirectionalMapImpl<>(this);
	}
}