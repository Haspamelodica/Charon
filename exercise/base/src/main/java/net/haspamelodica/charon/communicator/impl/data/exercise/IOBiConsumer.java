package net.haspamelodica.charon.communicator.impl.data.exercise;

import java.io.IOException;

public interface IOBiConsumer<A, B>
{
	public void accept(A a, B b) throws IOException;
}
