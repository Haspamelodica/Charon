package net.haspamelodica.charon;

import java.io.IOException;

import net.haspamelodica.charon.utils.IORunnable;

public interface CloseableStudentSide extends AutoCloseable
{
	public StudentSide getStudentSide();
	@Override
	public void close() throws IOException;

	public static CloseableStudentSide wrapIgnoringClose(StudentSide studentSide)
	{
		return wrap(studentSide, () ->
		{});
	}

	public static CloseableStudentSide wrap(StudentSide studentSide, IORunnable closeAction)
	{
		return new CloseableStudentSide()
		{
			@Override
			public StudentSide getStudentSide()
			{
				return studentSide;
			}

			@Override
			public void close() throws IOException
			{
				closeAction.run();
			}
		};
	}

}
