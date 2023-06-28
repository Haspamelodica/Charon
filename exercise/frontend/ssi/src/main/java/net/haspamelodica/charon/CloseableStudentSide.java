package net.haspamelodica.charon;

import java.io.IOException;

public interface CloseableStudentSide extends AutoCloseable
{
	public StudentSide getStudentSide();
	@Override
	public void close() throws IOException;

	public static CloseableStudentSide wrapIgnoringClose(StudentSide studentSide)
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
				//ignore
			}
		};
	}
}
