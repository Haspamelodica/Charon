package net.haspamelodica.charon.mockclasses;

import java.util.List;
import java.util.function.Supplier;

import net.bytebuddy.description.modifier.Visibility;
import net.bytebuddy.description.type.TypeDescription;
import net.bytebuddy.dynamic.scaffold.InstrumentedType;

public class PlaygroundDynamicInterfaceProvider implements DynamicInterfaceProvider
{
	private static final String PACKAGE = "net.haspamelodica.charon.mockclasses.";

	@Override
	public ClassInterface interfaceFor(String name)
	{
		TypeDescription typeSupplier = TypeDescription.ForLoadedType.of(Supplier.class);
		TypeDescription typeA = InstrumentedType.Default.of(PACKAGE + "A", TypeDescription.Generic.OBJECT, Visibility.PUBLIC);
		TypeDescription typeB = InstrumentedType.Default.of(PACKAGE + "B", TypeDescription.Generic.OBJECT, Visibility.PUBLIC);
		TypeDescription.Generic typeSupplierOfA = TypeDescription.Generic.Builder.parameterizedType(typeSupplier, typeA).build();
		TypeDescription.Generic typeSupplierOfB = TypeDescription.Generic.Builder.parameterizedType(typeSupplier, typeB).build();
		return switch(name)
		{
			case PACKAGE + "Transformed" -> new ClassInterface(PACKAGE + "Transformed",
					List.of(
							new MethodInterface("run", TypeDescription.VOID, true, List.of()),
							new MethodInterface("runInstanceMethod", TypeDescription.VOID, false, List.of()),
							new MethodInterface("testCyclicType", typeA, false, List.of(typeSupplierOfA))),
					List.of(
							new ConstructorInterface(List.of())));
			case PACKAGE + "A" -> new ClassInterface(PACKAGE + "A",
					List.of(
							new MethodInterface("testCyclicType", typeB, false, List.of(typeSupplierOfB))),
					List.of(
							new ConstructorInterface(List.of())));
			case PACKAGE + "B" -> new ClassInterface(PACKAGE + "B",
					List.of(
							new MethodInterface("testCyclicType", typeA, false, List.of(typeSupplierOfA))),
					List.of(
							new ConstructorInterface(List.of())));
			default -> null;
		};
	}
}
