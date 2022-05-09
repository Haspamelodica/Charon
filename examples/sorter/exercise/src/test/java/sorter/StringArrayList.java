package sorter;

import static net.haspamelodica.charon.annotations.StudentSideInstanceKind.Kind.CLASS;
import static net.haspamelodica.charon.annotations.StudentSideInstanceMethodKind.Kind.INSTANCE_FIELD_GETTER;
import static net.haspamelodica.charon.annotations.StudentSideInstanceMethodKind.Kind.INSTANCE_METHOD;
import static net.haspamelodica.charon.annotations.StudentSidePrototypeMethodKind.Kind.CONSTRUCTOR;

import net.haspamelodica.charon.StudentSideInstance;
import net.haspamelodica.charon.StudentSidePrototype;
import net.haspamelodica.charon.annotations.StudentSideInstanceKind;
import net.haspamelodica.charon.annotations.StudentSideInstanceMethodKind;
import net.haspamelodica.charon.annotations.StudentSidePrototypeMethodKind;
import net.haspamelodica.charon.annotations.UseSerializer;
import net.haspamelodica.charon.serialization.StringSerializer;

@StudentSideInstanceKind(CLASS)
public interface StringArrayList extends StudentSideInstance
{
	@StudentSideInstanceMethodKind(INSTANCE_METHOD)
	// Serializers allow transmitting entire objects safely to and from the student JVM.
	// Serializers to use have to be explicitly mentioned using UseSerializer. This can be done for an entire SSI or just for a specific method.
	// No serializers are neccessary for SSIs.
	@UseSerializer(StringSerializer.class)
	public void add(String string);

	@StudentSideInstanceMethodKind(INSTANCE_METHOD)
	@UseSerializer(StringSerializer.class)
	public String get(int i);

	@StudentSideInstanceMethodKind(INSTANCE_FIELD_GETTER)
	public int length();

	public interface Prototype extends StudentSidePrototype<StringArrayList>
	{
		@StudentSidePrototypeMethodKind(CONSTRUCTOR)
		public StringArrayList new_(int capacity);
	}
}
