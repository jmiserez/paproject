package test.util;

import java.io.IOException;
import java.io.OutputStream;

/**
 * Clone everything from outputStream to cloneStream
 */
public class CloneOutputStream extends OutputStream{
	private final OutputStream outputStream;
	private final OutputStream cloneStream;

	public CloneOutputStream(OutputStream outputStream, OutputStream cloneStream)
	{
		super();
		this.outputStream = outputStream;
		this.cloneStream = cloneStream;
	}

	@Override
	public void write(byte[] b) throws IOException
	{
		outputStream.write(b);
		cloneStream.write(b);
	}

	@Override
	public void write(byte[] b, int off, int len) throws IOException
	{
		outputStream.write(b, off, len);
		cloneStream.write(b, off, len);
	}

	@Override
	public void write(int b) throws IOException
	{
		outputStream.write(b);
		cloneStream.write(b);
	}
}
