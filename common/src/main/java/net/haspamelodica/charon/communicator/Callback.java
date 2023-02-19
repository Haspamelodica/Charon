package net.haspamelodica.charon.communicator;

import java.util.List;

public interface Callback<REF>
{
	public REF callInstanceMethod(String cn, String name, String returnClassname, List<String> params, REF receiverRef, List<REF> argRefs);
}
