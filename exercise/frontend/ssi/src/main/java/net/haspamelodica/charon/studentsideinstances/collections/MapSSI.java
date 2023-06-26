package net.haspamelodica.charon.studentsideinstances.collections;

import static net.haspamelodica.charon.annotations.StudentSideInstanceKind.Kind.INTERFACE;
import static net.haspamelodica.charon.annotations.StudentSideInstanceMethodKind.Kind.INSTANCE_METHOD;
import static net.haspamelodica.charon.annotations.StudentSidePrototypeMethodKind.Kind.STATIC_METHOD;

import java.util.Map;

import net.haspamelodica.charon.StudentSideInstance;
import net.haspamelodica.charon.StudentSidePrototype;
import net.haspamelodica.charon.annotations.OverrideStudentSideNameByClass;
import net.haspamelodica.charon.annotations.PrototypeClass;
import net.haspamelodica.charon.annotations.StudentSideInstanceKind;
import net.haspamelodica.charon.annotations.StudentSideInstanceMethodKind;
import net.haspamelodica.charon.annotations.StudentSidePrototypeMethodKind;

@StudentSideInstanceKind(INTERFACE)
@OverrideStudentSideNameByClass(Map.class)
@PrototypeClass(MapSSI.Prototype.class)
public interface MapSSI<K extends StudentSideInstance, V extends StudentSideInstance> extends StudentSideInstance
{
	@StudentSideInstanceMethodKind(INSTANCE_METHOD)
	public int size();

	@StudentSideInstanceMethodKind(INSTANCE_METHOD)
	public boolean isEmpty();

	@StudentSideInstanceMethodKind(INSTANCE_METHOD)
	public boolean containsKey(StudentSideInstance key);

	@StudentSideInstanceMethodKind(INSTANCE_METHOD)
	public boolean containsValue(StudentSideInstance value);

	@StudentSideInstanceMethodKind(INSTANCE_METHOD)
	public V get(StudentSideInstance key);

	@StudentSideInstanceMethodKind(INSTANCE_METHOD)
	public V put(K key, V value);

	@StudentSideInstanceMethodKind(INSTANCE_METHOD)
	public V remove(StudentSideInstance key);

	@StudentSideInstanceMethodKind(INSTANCE_METHOD)
	public void putAll(MapSSI<? extends K, ? extends V> m);

	@StudentSideInstanceMethodKind(INSTANCE_METHOD)
	public void clear();

	@StudentSideInstanceMethodKind(INSTANCE_METHOD)
	public CollectionSSI<V> values();

	@StudentSideInstanceMethodKind(INSTANCE_METHOD)
	public V getOrDefault(StudentSideInstance key, V defaultValue);

	@StudentSideInstanceMethodKind(INSTANCE_METHOD)
	public V putIfAbsent(K key, V value);

	@StudentSideInstanceMethodKind(INSTANCE_METHOD)
	public boolean remove(StudentSideInstance key, StudentSideInstance value);

	@StudentSideInstanceMethodKind(INSTANCE_METHOD)
	public Object replace(K key, V oldValue, V newValue);

	@StudentSideInstanceMethodKind(INSTANCE_METHOD)
	public boolean replace(K key, V value);

	public static interface Prototype extends StudentSidePrototype<MapSSI<?, ?>>
	{
		@StudentSidePrototypeMethodKind(STATIC_METHOD)
		public <K extends StudentSideInstance, V extends StudentSideInstance> MapSSI<K, V> of();

		@StudentSidePrototypeMethodKind(STATIC_METHOD)
		public <K extends StudentSideInstance, V extends StudentSideInstance> MapSSI<K, V> of(K k1, V v1);

		@StudentSidePrototypeMethodKind(STATIC_METHOD)
		public <K extends StudentSideInstance, V extends StudentSideInstance> MapSSI<K, V> of(K k1, V v1, K k2, V v2);

		@StudentSidePrototypeMethodKind(STATIC_METHOD)
		public <K extends StudentSideInstance, V extends StudentSideInstance> MapSSI<K, V> of(K k1, V v1, K k2, V v2, K k3, V v3);

		@StudentSidePrototypeMethodKind(STATIC_METHOD)
		public <K extends StudentSideInstance, V extends StudentSideInstance> MapSSI<K, V> of(K k1, V v1, K k2, V v2, K k3, V v3, K k4, V v4);

		@StudentSidePrototypeMethodKind(STATIC_METHOD)
		public <K extends StudentSideInstance, V extends StudentSideInstance> MapSSI<K, V> of(K k1, V v1, K k2, V v2, K k3, V v3, K k4, V v4, K k5, V v5);

		@StudentSidePrototypeMethodKind(STATIC_METHOD)
		public <K extends StudentSideInstance, V extends StudentSideInstance> MapSSI<K, V> of(K k1, V v1, K k2, V v2, K k3, V v3, K k4, V v4, K k5, V v5, K k6, V v6);

		@StudentSidePrototypeMethodKind(STATIC_METHOD)
		public <K extends StudentSideInstance, V extends StudentSideInstance> MapSSI<K, V> of(K k1, V v1, K k2, V v2, K k3, V v3, K k4, V v4, K k5, V v5, K k6, V v6, K k7, V v7);

		@StudentSidePrototypeMethodKind(STATIC_METHOD)
		public <K extends StudentSideInstance, V extends StudentSideInstance> MapSSI<K, V> of(K k1, V v1, K k2, V v2, K k3, V v3, K k4, V v4, K k5, V v5, K k6, V v6, K k7, V v7, K k8, V v8);

		@StudentSidePrototypeMethodKind(STATIC_METHOD)
		public <K extends StudentSideInstance, V extends StudentSideInstance> MapSSI<K, V> of(K k1, V v1, K k2, V v2, K k3, V v3, K k4, V v4, K k5, V v5, K k6, V v6, K k7, V v7, K k8, V v8, K k9, V v9);

		@StudentSidePrototypeMethodKind(STATIC_METHOD)
		public <K extends StudentSideInstance, V extends StudentSideInstance> MapSSI<K, V> of(K k1, V v1, K k2, V v2, K k3, V v3, K k4, V v4, K k5, V v5, K k6, V v6, K k7, V v7, K k8, V v8, K k9, V v9, K k10, V v10);
	}
}

