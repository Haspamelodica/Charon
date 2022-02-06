package net.haspamelodica.studentcodeseparator.communicator.impl.data.exercise;

import java.io.IOException;

public interface IOConsumer<A>
{
	public void accept(A a) throws IOException;
}