package net.haspamelodica.studentcodeseparator.communicator;

import java.util.List;

import net.haspamelodica.studentcodeseparator.refs.Ref;

public interface Callback<REF extends Ref<?, ?>>
{
	public REF callInstanceMethod(String cn, String name, String returnClassname, List<String> params, REF receiverRef, List<REF> argRefs);
}
