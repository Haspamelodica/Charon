package sorter;

import static net.haspamelodica.charon.annotations.StudentSideInstanceKind.Kind.CLASS;
import static net.haspamelodica.charon.annotations.StudentSideInstanceMethodKind.Kind.INSTANCE_FIELD_GETTER;
import static net.haspamelodica.charon.annotations.StudentSideInstanceMethodKind.Kind.INSTANCE_METHOD;
import static net.haspamelodica.charon.annotations.StudentSidePrototypeMethodKind.Kind.CONSTRUCTOR;

import net.haspamelodica.charon.StudentSideInstance;
import net.haspamelodica.charon.StudentSidePrototype;
import net.haspamelodica.charon.annotations.PrototypeClass;
import net.haspamelodica.charon.annotations.StudentSideInstanceKind;
import net.haspamelodica.charon.annotations.StudentSideInstanceMethodKind;
import net.haspamelodica.charon.annotations.StudentSidePrototypeMethodKind;
import net.haspamelodica.charon.annotations.UseSerDes;
import net.haspamelodica.charon.marshaling.StringSerDes;

@StudentSideInstanceKind(CLASS)
@PrototypeClass(StringArrayList.Prototype.class)
public interface StringArrayList extends StudentSideInstance
{
	@StudentSideInstanceMethodKind(INSTANCE_METHOD)
	// SerDeses (short for Serializer/Deserializer) allow transmitting entire objects safely to and from the student JVM.
	// SerDeses to use have to be explicitly mentioned using UseSerDes. This can be done for an entire SSI or just for a specific method.
	// No SerDeses are necessary for SSIs.
	@UseSerDes(StringSerDes.class)
	public void add(String string);

	@StudentSideInstanceMethodKind(INSTANCE_METHOD)
	@UseSerDes(StringSerDes.class)
	public String get(int i);

	@StudentSideInstanceMethodKind(INSTANCE_FIELD_GETTER)
	public int length();

	public interface Prototype extends StudentSidePrototype<StringArrayList>
	{
		@StudentSidePrototypeMethodKind(CONSTRUCTOR)
		public StringArrayList new_(int capacity);
	}
}
