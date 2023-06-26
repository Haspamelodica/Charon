package net.haspamelodica.charon;

import static net.haspamelodica.charon.annotations.StudentSideInstanceMethodKind.Kind.INSTANCE_FIELD_GETTER;
import static net.haspamelodica.charon.annotations.StudentSideInstanceMethodKind.Kind.INSTANCE_FIELD_SETTER;
import static net.haspamelodica.charon.annotations.StudentSideInstanceMethodKind.Kind.INSTANCE_METHOD;
import static net.haspamelodica.charon.annotations.StudentSidePrototypeMethodKind.Kind.CONSTRUCTOR;
import static net.haspamelodica.charon.annotations.StudentSidePrototypeMethodKind.Kind.STATIC_FIELD_GETTER;
import static net.haspamelodica.charon.annotations.StudentSidePrototypeMethodKind.Kind.STATIC_FIELD_SETTER;
import static net.haspamelodica.charon.annotations.StudentSidePrototypeMethodKind.Kind.STATIC_METHOD;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Member;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import net.haspamelodica.charon.annotations.PrototypeClass;
import net.haspamelodica.charon.annotations.StudentSideInstanceKind;
import net.haspamelodica.charon.annotations.StudentSideInstanceMethodKind;
import net.haspamelodica.charon.annotations.StudentSidePrototypeMethodKind;
import net.haspamelodica.charon.annotations.UseSerDes;
import net.haspamelodica.charon.marshaling.StringSerDes;

public class GeneratePrototype
{
	private final Class<?>	clazz;
	private boolean			needsStringSerDes;

	public GeneratePrototype(String classname) throws ClassNotFoundException
	{
		this(Class.forName(classname));
	}
	public GeneratePrototype(Class<?> clazz)
	{
		this.clazz = clazz;
	}

	public String generatePrototype() throws ClassNotFoundException
	{
		List<String> instanceBodies = new ArrayList<>();
		List<String> prototypeBodies = new ArrayList<>();
		for(MemberPrototypeGenerator<?> generator : (Iterable<MemberPrototypeGenerator<?>>) Stream.concat(Stream.concat(
				Arrays.stream(clazz.getDeclaredFields()).<MemberPrototypeGenerator<?>> flatMap(
						f -> Stream.of(new FieldGetterPrototypeGenerator(f), new FieldSetterPrototypeGenerator(f))),
				Arrays.stream(clazz.getDeclaredMethods()).map(MethodPrototypeGenerator::new)),
				Arrays.stream(clazz.getDeclaredConstructors()).map(ConstructorPrototypeGenerator::new))::iterator)
		{
			(generator.isInstance() ? instanceBodies : prototypeBodies).add(generator.generateMemberPrototype());
		}

		String result = "";

		result += "package " + clazz.getPackageName() + ";\n";
		result += "\n";

		if(needsStringSerDes)
		{
			result += "import " + UseSerDes.class.getCanonicalName() + ";\n";
			result += "import " + StringSerDes.class.getCanonicalName() + ";\n";
		}
		result += "import " + PrototypeClass.class.getCanonicalName() + ";\n";
		result += "import " + StudentSideInstance.class.getCanonicalName() + ";\n";
		result += "import " + StudentSidePrototype.class.getCanonicalName() + ";\n";
		result += "import " + StudentSideInstanceKind.class.getCanonicalName() + ";\n";
		result += "import " + StudentSideInstanceMethodKind.class.getCanonicalName() + ";\n";
		result += "import " + StudentSidePrototypeMethodKind.class.getCanonicalName() + ";\n";
		result += "\n";
		result += "import static " + StudentSideInstanceKind.Kind.class.getCanonicalName() + ".*;\n";
		result += "import static " + StudentSideInstanceMethodKind.Kind.class.getCanonicalName() + ".*;\n";
		result += "import static " + StudentSidePrototypeMethodKind.Kind.class.getCanonicalName() + ".*;\n";
		result += "\n";

		//TODO actually support arrays
		//TODO handle collections
		//TODO handle generics
		result += "@" + StudentSideInstanceKind.class.getSimpleName() + "(" + (clazz.isInterface() ? "INTERFACE" : clazz.isArray() ? "ARRAY" : "CLASS") + ")\n";
		result += "@" + PrototypeClass.class.getSimpleName() + "(" + clazz.getSimpleName() + ".Prototype.class)\n";
		result += "public interface " + clazz.getSimpleName() + " extends " + StudentSideInstance.class.getSimpleName() + "\n";
		result += "{\n";

		result += instanceBodies.stream().map(s -> s + "\n").collect(Collectors.joining());

		result += "\tpublic static interface Prototype extends " + StudentSidePrototype.class.getSimpleName() + "<" + clazz.getSimpleName() + ">\n";
		result += "\t{\n";
		result += prototypeBodies.stream().collect(Collectors.joining("\n"));
		result += "\t}\n";

		result += "}\n";

		return result;
	}

	private void updateImportsByOccurringType(Class<?> type)
	{
		needsStringSerDes |= type == String.class;
	}

	private class FieldGetterPrototypeGenerator extends MemberPrototypeGenerator<Field>
	{
		private FieldGetterPrototypeGenerator(Field field)
		{
			super(field);
		}

		@Override
		protected StudentSideInstanceMethodKind.Kind kindInstance()
		{
			return INSTANCE_FIELD_GETTER;
		}

		@Override
		protected StudentSidePrototypeMethodKind.Kind kindPrototype()
		{
			return STATIC_FIELD_GETTER;
		}

		@Override
		public String name()
		{
			return member.getName();
		}

		@Override
		protected Class<?> returnType()
		{
			return member.getType();
		}

		@Override
		protected List<Param> params()
		{
			return List.of();
		}
	}

	private class FieldSetterPrototypeGenerator extends MemberPrototypeGenerator<Field>
	{
		private FieldSetterPrototypeGenerator(Field field)
		{
			super(field);
		}

		@Override
		protected StudentSideInstanceMethodKind.Kind kindInstance()
		{
			return INSTANCE_FIELD_SETTER;
		}

		@Override
		protected StudentSidePrototypeMethodKind.Kind kindPrototype()
		{
			return STATIC_FIELD_SETTER;
		}

		@Override
		public String name()
		{
			return member.getName();
		}

		@Override
		protected Class<?> returnType()
		{
			return void.class;
		}

		@Override
		protected List<Param> params()
		{
			return List.of(new Param(member.getType(), member.getName()));
		}
	}

	private class MethodPrototypeGenerator extends MemberPrototypeGenerator<Method>
	{
		private MethodPrototypeGenerator(Method method)
		{
			super(method);
		}

		@Override
		protected StudentSideInstanceMethodKind.Kind kindInstance()
		{
			return INSTANCE_METHOD;
		}

		@Override
		protected StudentSidePrototypeMethodKind.Kind kindPrototype()
		{
			return STATIC_METHOD;
		}

		@Override
		public String name()
		{
			return member.getName();
		}

		@Override
		protected Class<?> returnType()
		{
			return member.getReturnType();
		}

		@Override
		protected List<Param> params()
		{
			return Arrays.stream(member.getParameters()).map(p -> new Param(p.getType(), p.getName())).toList();
		}
	}

	private class ConstructorPrototypeGenerator extends MemberPrototypeGenerator<Constructor<?>>
	{
		private ConstructorPrototypeGenerator(Constructor<?> constructor)
		{
			super(constructor);
		}

		@Override
		protected StudentSideInstanceMethodKind.Kind kindInstance()
		{
			throw new IllegalStateException("Found non-static constructor");
		}

		@Override
		protected StudentSidePrototypeMethodKind.Kind kindPrototype()
		{
			return CONSTRUCTOR;
		}

		@Override
		public String name()
		{
			return "new_";
		}

		@Override
		protected Class<?> returnType()
		{
			return clazz;
		}

		@Override
		protected List<Param> params()
		{
			return Arrays.stream(member.getParameters()).map(p -> new Param(p.getType(), p.getName())).toList();
		}
	}

	private abstract class MemberPrototypeGenerator<M extends Member>
	{
		protected final M	member;
		private boolean		needsStringSerDes;

		public MemberPrototypeGenerator(M member)
		{
			this.member = member;
		}

		public boolean isInstance()
		{
			return !(member instanceof Constructor || Modifier.isStatic(member.getModifiers()));
		}

		public String generateMemberPrototype()
		{
			String body = generatePrototypeBody();

			String result = "";

			if(needsStringSerDes)
				result += (isInstance() ? "" : "\t") + "\t@" + UseSerDes.class.getSimpleName() + "(" + StringSerDes.class.getSimpleName() + ".class)\n";
			result += body;

			return result;
		}

		private String generatePrototypeBody()
		{
			Class<?> returnType = returnType();
			List<Param> params = params();
			updateImportsAndAnnotationsByOccurringType(returnType);
			for(Param param : params)
				updateImportsAndAnnotationsByOccurringType(param.type());

			String body = "";
			if(isInstance())
			{
				body += "\t@" + StudentSideInstanceMethodKind.class.getSimpleName() + "(" + kindInstance() + ")\n";
			} else
			{

				body += "\t\t@" + StudentSidePrototypeMethodKind.class.getSimpleName() + "(" + kindPrototype() + ")\n";
				body += "\t";
			}
			body += "\tpublic " + returnType.getSimpleName() + " " + name();
			body += params.stream().map(Param::generate).collect(Collectors.joining(", ", "(", ")")) + ";\n";
			return body;
		}

		private void updateImportsAndAnnotationsByOccurringType(Class<?> type)
		{
			needsStringSerDes |= type == String.class;
			GeneratePrototype.this.updateImportsByOccurringType(type);
		}
		protected abstract StudentSideInstanceMethodKind.Kind kindInstance();
		protected abstract StudentSidePrototypeMethodKind.Kind kindPrototype();
		protected abstract String name();
		protected abstract Class<?> returnType();
		protected abstract List<Param> params();
		protected record Param(Class<?> type, String name)
		{
			public String generate()
			{
				return type.getSimpleName() + " " + name;
			}
		}
	}

	public static void main(String[] args) throws ClassNotFoundException
	{
		for(String classname : args)
			System.out.println(new GeneratePrototype(classname).generatePrototype());
	}
}
