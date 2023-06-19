package helloworld;

import static net.haspamelodica.charon.annotations.StudentSideInstanceKind.Kind.CLASS;
import static net.haspamelodica.charon.annotations.StudentSidePrototypeMethodKind.Kind.STATIC_METHOD;

import net.haspamelodica.charon.StudentSideInstance;
import net.haspamelodica.charon.StudentSidePrototype;
import net.haspamelodica.charon.annotations.StudentSideInstanceKind;
import net.haspamelodica.charon.annotations.StudentSidePrototypeMethodKind;
import net.haspamelodica.charon.annotations.UseSerDes;
import net.haspamelodica.charon.marshaling.StringSerDes;

@StudentSideInstanceKind(CLASS)
public interface HelloWorld extends StudentSideInstance
{
	public static interface Prototype extends StudentSidePrototype<HelloWorld>
	{
		@StudentSidePrototypeMethodKind(STATIC_METHOD)
		@UseSerDes(StringSerDes.class)
		public String helloWorld();
	}
}
