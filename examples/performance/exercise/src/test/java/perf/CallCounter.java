package perf;

import static net.haspamelodica.charon.annotations.StudentSideInstanceKind.Kind.CLASS;
import static net.haspamelodica.charon.annotations.StudentSideInstanceMethodKind.Kind.INSTANCE_METHOD;
import static net.haspamelodica.charon.annotations.StudentSidePrototypeMethodKind.Kind.CONSTRUCTOR;
import static net.haspamelodica.charon.annotations.StudentSidePrototypeMethodKind.Kind.STATIC_METHOD;

import net.haspamelodica.charon.StudentSideInstance;
import net.haspamelodica.charon.StudentSidePrototype;
import net.haspamelodica.charon.annotations.PrototypeClass;
import net.haspamelodica.charon.annotations.StudentSideInstanceKind;
import net.haspamelodica.charon.annotations.StudentSideInstanceMethodKind;
import net.haspamelodica.charon.annotations.StudentSidePrototypeMethodKind;

@StudentSideInstanceKind(CLASS)
@PrototypeClass(CallCounter.Prototype.class)
public interface CallCounter extends StudentSideInstance
{
	@StudentSideInstanceMethodKind(INSTANCE_METHOD)
	public void call();

	@StudentSideInstanceMethodKind(INSTANCE_METHOD)
	public void call(
			long unusedParam00, long unusedParam01, long unusedParam02, long unusedParam03,
			long unusedParam04, long unusedParam05, long unusedParam06, long unusedParam07,
			long unusedParam08, long unusedParam09, long unusedParam10, long unusedParam11,
			long unusedParam12, long unusedParam13, long unusedParam14, long unusedParam15);

	@StudentSideInstanceMethodKind(INSTANCE_METHOD)
	public int getCallCount();

	public static interface Prototype extends StudentSidePrototype<CallCounter>
	{
		@StudentSidePrototypeMethodKind(CONSTRUCTOR)
		public CallCounter new_();

		@StudentSidePrototypeMethodKind(STATIC_METHOD)
		public void callForNSeconds(CallbackInterface callback, double secondsToRunFor);

		@StudentSidePrototypeMethodKind(STATIC_METHOD)
		public void callForNSecondsWithUnchangingUnusedParams(CallbackInterface callback, double secondsToRunFor);
	}
}
