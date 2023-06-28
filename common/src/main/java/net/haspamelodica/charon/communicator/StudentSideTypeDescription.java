package net.haspamelodica.charon.communicator;

import static net.haspamelodica.charon.communicator.StudentSideTypeDescription.Kind.ARRAY;
import static net.haspamelodica.charon.communicator.StudentSideTypeDescription.Kind.CLASS;
import static net.haspamelodica.charon.communicator.StudentSideTypeDescription.Kind.INTERFACE;
import static net.haspamelodica.charon.communicator.StudentSideTypeDescription.Kind.PRIMITIVE;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

import net.haspamelodica.charon.reflection.ReflectionUtils;

public record StudentSideTypeDescription<TYPEREF>(StudentSideTypeDescription.Kind kind, String name,
		Optional<TYPEREF> superclass, List<TYPEREF> superinterfaces, Optional<TYPEREF> componentTypeIfArray)
{
	/** Creates a {@link StudentSideTypeDescription} from the given parameters and checks if the parameters make sense for the given {@link Kind}. */
	public StudentSideTypeDescription(StudentSideTypeDescription.Kind kind, String name,
			Optional<TYPEREF> superclass, List<TYPEREF> superinterfaces,
			Optional<TYPEREF> componentTypeIfArray)
	{
		this.kind = Objects.requireNonNull(kind);
		this.name = Objects.requireNonNull(name);
		this.superclass = Objects.requireNonNull(superclass);
		this.superinterfaces = List.copyOf(superinterfaces);
		this.componentTypeIfArray = Objects.requireNonNull(componentTypeIfArray);

		if((kind == PRIMITIVE) != ReflectionUtils.isPrimitiveName(name))
			throw new IllegalArgumentException(kind == PRIMITIVE
					? "Primitive type had non-primitive name"
					: "Non-primitive type had primitive name");
		//TODO the superclass of ARRAY has to be Object and the superinterfaces of ARRAY have to be [Cloneable, Serializable],
		// but that isn't checked here.
		if(kind != CLASS && kind != ARRAY && superclass().isPresent())
			throw new IllegalArgumentException("Only classes and arrays can have a superclass");
		if(kind != CLASS && kind != INTERFACE && kind != ARRAY && !superinterfaces().isEmpty())
			throw new IllegalArgumentException("Only classes, interfaces and arrays can have superinterfaces");
		if(kind != ARRAY && componentTypeIfArray().isPresent())
			throw new IllegalArgumentException("Only arrays can have a component type");
	}

	public String toString(Function<TYPEREF, String> typerefToString)
	{
		return switch(kind)
		{
			case PRIMITIVE -> name;
			case CLASS -> "class " + name
					+ superclass.map(typerefToString).map(s -> " extends " + s).orElse("")
					+ superinterfacesToString("implements", typerefToString);
			case INTERFACE -> "interface " + name
					+ superinterfacesToString("extends", typerefToString);
			case ARRAY -> "array " + name + "[" + typerefToString.apply(componentTypeIfArray.get()) + "]";
		};
	}

	private String superinterfacesToString(String implementsMarker, Function<TYPEREF, String> typerefToString)
	{
		return superinterfaces.size() != 0
				? " " + implementsMarker + " " + superinterfaces.stream().map(typerefToString).collect(Collectors.joining(", "))
				: "";
	}

	public static enum Kind
	{
		PRIMITIVE,
		ARRAY,
		CLASS,
		INTERFACE;

		public byte encode()
		{
			return (byte) ordinal();
		}
		public static Kind decode(byte raw)
		{
			return values()[raw];
		}
	}
}
