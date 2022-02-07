package net.haspamelodica.studentcodeseparator.communicator.impl.data.exercise;

import java.io.IOException;

public interface IOSupplier<R>
{
	public R get() throws IOException;
}
