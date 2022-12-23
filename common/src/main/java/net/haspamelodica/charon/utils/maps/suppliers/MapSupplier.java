package net.haspamelodica.charon.utils.maps.suppliers;

import java.util.Map;

import net.haspamelodica.charon.utils.maps.UnidirectionalMap;
import net.haspamelodica.charon.utils.maps.UnidirectionalMapImpl;

public interface MapSupplier
{
	public <K, V> Map<K, V> createMap();

	public default <K, V> UnidirectionalMap<K, V> createUnidirectionalMap()
	{
		return new UnidirectionalMapImpl<>(this);
	}
}