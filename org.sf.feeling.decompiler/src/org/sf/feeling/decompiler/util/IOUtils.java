/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.sf.feeling.decompiler.util;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.Closeable;
import java.io.EOFException;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Reader;
import java.io.Writer;
import java.net.HttpURLConnection;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.URL;
import java.net.URLConnection;
import java.nio.ByteBuffer;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.Selector;
import java.nio.charset.Charset;

/**
 * General IO stream manipulation utilities.
 * <p>
 * This class provides static utility methods for input/output operations.
 * <ul>
 * <li><b>[Deprecated]</b> closeQuietly - these methods close a stream ignoring
 * nulls and exceptions
 * <li>toXxx/read - these methods read data from a stream
 * <li>write - these methods write data to a stream
 * <li>copy - these methods copy all the data from one stream to another
 * <li>contentEquals - these methods compare the content of two streams
 * </ul>
 * <p>
 * The byte-to-char methods and char-to-byte methods involve a conversion step.
 * Two methods are provided in each case, one that uses the platform default
 * encoding and the other which allows you to specify an encoding. You are
 * encouraged to always specify an encoding because relying on the platform
 * default can lead to unexpected results, for example when moving from
 * development to production.
 * <p>
 * All the methods in this class that read a stream are buffered internally.
 * This means that there is no cause to use a <code>BufferedInputStream</code>
 * or <code>BufferedReader</code>. The default buffer size of 4K has been shown
 * to be efficient in tests.
 * <p>
 * The various copy methods all delegate the actual copying to one of the
 * following methods:
 * <ul>
 * <li>{@link #copyLarge(InputStream, OutputStream, byte[])}</li>
 * <li>{@link #copyLarge(InputStream, OutputStream, long, long, byte[])}</li>
 * <li>{@link #copyLarge(Reader, Writer, char[])}</li>
 * <li>{@link #copyLarge(Reader, Writer, long, long, char[])}</li>
 * </ul>
 * For example, {@link #copy(InputStream, OutputStream)} calls
 * {@link #copyLarge(InputStream, OutputStream)} which calls
 * {@link #copy(InputStream, OutputStream, int)} which creates the buffer and
 * calls {@link #copyLarge(InputStream, OutputStream, byte[])}.
 * <p>
 * Applications can re-use buffers by using the underlying methods directly.
 * This may improve performance for applications that need to do a lot of
 * copying.
 * <p>
 * Wherever possible, the methods in this class do <em>not</em> flush or close
 * the stream. This is to avoid making non-portable assumptions about the
 * streams' origin and further use. Thus the caller is still responsible for
 * closing streams after use.
 * <p>
 * Origin of code: Excalibur.
 *
 */
public class IOUtils {
	// NOTE: This class is focused on InputStream, OutputStream, Reader and
	// Writer. Each method should take at least one of these as a parameter,
	// or return one of them.

	/**
	 * Represents the end-of-file (or stream).
	 * 
	 * @since 2.5 (made public)
	 */
	public static final int EOF = -1;

	/**
	 * The Unix directory separator character.
	 */
	public static final char DIR_SEPARATOR_UNIX = '/';
	/**
	 * The Windows directory separator character.
	 */
	public static final char DIR_SEPARATOR_WINDOWS = '\\';
	/**
	 * The system directory separator character.
	 */
	public static final char DIR_SEPARATOR = File.separatorChar;
	/**
	 * The Unix line separator string.
	 */
	public static final String LINE_SEPARATOR_UNIX = "\n"; //$NON-NLS-1$
	/**
	 * The Windows line separator string.
	 */
	public static final String LINE_SEPARATOR_WINDOWS = "\r\n"; //$NON-NLS-1$

	/**
	 * The default buffer size ({@value}) to use for
	 * {@link #copyLarge(InputStream, OutputStream)} and
	 * {@link #copyLarge(Reader, Writer)}
	 */
	private static final int DEFAULT_BUFFER_SIZE = 1024 * 4;

	/**
	 * The default buffer size to use for the skip() methods.
	 */
	private static final int SKIP_BUFFER_SIZE = 2048;

	// Allocated in the relevant skip method if necessary.
	/*
	 * These buffers are static and are shared between threads. This is possible
	 * because the buffers are write-only - the contents are never read.
	 *
	 * N.B. there is no need to synchronize when creating these because: - we don't
	 * care if the buffer is created multiple times (the data is ignored) - we
	 * always use the same size buffer, so if it it is recreated it will still be OK
	 * (if the buffer size were variable, we would need to synch. to ensure some
	 * other thread did not create a smaller one)
	 */
	private static char[] SKIP_CHAR_BUFFER;
	private static byte[] SKIP_BYTE_BUFFER;

	/**
	 * Instances should NOT be constructed in standard programming.
	 */
	public IOUtils() {
		super();
	}

	// -----------------------------------------------------------------------

	/**
	 * Closes a URLConnection.
	 *
	 * @param conn the connection to close.
	 * @since 2.4
	 */
	public static void close(final URLConnection conn) {
		if (conn instanceof HttpURLConnection) {
			((HttpURLConnection) conn).disconnect();
		}
	}

	/**
	 * Closes an <code>Reader</code> unconditionally.
	 * <p>
	 * Equivalent to {@link Reader#close()}, except any exceptions will be ignored.
	 * This is typically used in finally blocks.
	 * <p>
	 * Example code:
	 * 
	 * <pre>
	 * char[] data = new char[1024];
	 * Reader in = null;
	 * try {
	 * 	in = new FileReader("foo.txt");
	 * 	in.read(data);
	 * 	in.close(); // close errors are handled
	 * } catch (Exception e) {
	 * 	// error handling
	 * } finally {
	 * 	IOUtils.closeQuietly(in);
	 * }
	 * </pre>
	 *
	 * @param input the Reader to close, may be null or already closed
	 *
	 * @deprecated As of 2.6 removed without replacement. Please use the
	 *             try-with-resources statement or handle suppressed exceptions
	 *             manually.
	 * @see Throwable#addSuppressed(java.lang.Throwable)
	 */
	@Deprecated
	public static void closeQuietly(final Reader input) {
		closeQuietly((Closeable) input);
	}

	/**
	 * Closes an <code>Writer</code> unconditionally.
	 * <p>
	 * Equivalent to {@link Writer#close()}, except any exceptions will be ignored.
	 * This is typically used in finally blocks.
	 * <p>
	 * Example code:
	 * 
	 * <pre>
	 * Writer out = null;
	 * try {
	 * 	out = new StringWriter();
	 * 	out.write("Hello World");
	 * 	out.close(); // close errors are handled
	 * } catch (Exception e) {
	 * 	// error handling
	 * } finally {
	 * 	IOUtils.closeQuietly(out);
	 * }
	 * </pre>
	 *
	 * @param output the Writer to close, may be null or already closed
	 *
	 * @deprecated As of 2.6 removed without replacement. Please use the
	 *             try-with-resources statement or handle suppressed exceptions
	 *             manually.
	 * @see Throwable#addSuppressed(java.lang.Throwable)
	 */
	@Deprecated
	public static void closeQuietly(final Writer output) {
		closeQuietly((Closeable) output);
	}

	/**
	 * Closes an <code>InputStream</code> unconditionally.
	 * <p>
	 * Equivalent to {@link InputStream#close()}, except any exceptions will be
	 * ignored. This is typically used in finally blocks.
	 * <p>
	 * Example code:
	 * 
	 * <pre>
	 * byte[] data = new byte[1024];
	 * InputStream in = null;
	 * try {
	 * 	in = new FileInputStream("foo.txt");
	 * 	in.read(data);
	 * 	in.close(); // close errors are handled
	 * } catch (Exception e) {
	 * 	// error handling
	 * } finally {
	 * 	IOUtils.closeQuietly(in);
	 * }
	 * </pre>
	 *
	 * @param input the InputStream to close, may be null or already closed
	 *
	 * @deprecated As of 2.6 removed without replacement. Please use the
	 *             try-with-resources statement or handle suppressed exceptions
	 *             manually.
	 * @see Throwable#addSuppressed(java.lang.Throwable)
	 */
	@Deprecated
	public static void closeQuietly(final InputStream input) {
		closeQuietly((Closeable) input);
	}

	/**
	 * Closes an <code>OutputStream</code> unconditionally.
	 * <p>
	 * Equivalent to {@link OutputStream#close()}, except any exceptions will be
	 * ignored. This is typically used in finally blocks.
	 * <p>
	 * Example code:
	 * 
	 * <pre>
	 * byte[] data = "Hello, World".getBytes();
	 *
	 * OutputStream out = null;
	 * try {
	 * 	out = new FileOutputStream("foo.txt");
	 * 	out.write(data);
	 * 	out.close(); // close errors are handled
	 * } catch (IOException e) {
	 * 	// error handling
	 * } finally {
	 * 	IOUtils.closeQuietly(out);
	 * }
	 * </pre>
	 *
	 * @param output the OutputStream to close, may be null or already closed
	 *
	 * @deprecated As of 2.6 removed without replacement. Please use the
	 *             try-with-resources statement or handle suppressed exceptions
	 *             manually.
	 * @see Throwable#addSuppressed(java.lang.Throwable)
	 */
	@Deprecated
	public static void closeQuietly(final OutputStream output) {
		closeQuietly((Closeable) output);
	}

	/**
	 * Closes a <code>Closeable</code> unconditionally.
	 * <p>
	 * Equivalent to {@link Closeable#close()}, except any exceptions will be
	 * ignored. This is typically used in finally blocks.
	 * <p>
	 * Example code:
	 * </p>
	 * 
	 * <pre>
	 * Closeable closeable = null;
	 * try {
	 * 	closeable = new FileReader(&quot;foo.txt&quot;);
	 * 	// process closeable
	 * 	closeable.close();
	 * } catch (Exception e) {
	 * 	// error handling
	 * } finally {
	 * 	IOUtils.closeQuietly(closeable);
	 * }
	 * </pre>
	 * <p>
	 * Closing all streams:
	 * </p>
	 * 
	 * <pre>
	 * try {
	 * 	return IOUtils.copy(inputStream, outputStream);
	 * } finally {
	 * 	IOUtils.closeQuietly(inputStream);
	 * 	IOUtils.closeQuietly(outputStream);
	 * }
	 * </pre>
	 *
	 * @param closeable the objects to close, may be null or already closed
	 * @since 2.0
	 *
	 * @deprecated As of 2.6 removed without replacement. Please use the
	 *             try-with-resources statement or handle suppressed exceptions
	 *             manually.
	 * @see Throwable#addSuppressed(java.lang.Throwable)
	 */
	@Deprecated
	public static void closeQuietly(final Closeable closeable) {
		try {
			if (closeable != null) {
				closeable.close();
			}
		} catch (final IOException ioe) {
			// ignore
		}
	}

	/**
	 * Closes a <code>Closeable</code> unconditionally.
	 * <p>
	 * Equivalent to {@link Closeable#close()}, except any exceptions will be
	 * ignored.
	 * <p>
	 * This is typically used in finally blocks to ensure that the closeable is
	 * closed even if an Exception was thrown before the normal close statement was
	 * reached. <br>
	 * <b>It should not be used to replace the close statement(s) which should be
	 * present for the non-exceptional case.</b> <br>
	 * It is only intended to simplify tidying up where normal processing has
	 * already failed and reporting close failure as well is not necessary or
	 * useful.
	 * <p>
	 * Example code:
	 * </p>
	 * 
	 * <pre>
	 * Closeable closeable = null;
	 * try {
	 *     closeable = new FileReader(&quot;foo.txt&quot;);
	 *     // processing using the closeable; may throw an Exception
	 *     closeable.close(); // Normal close - exceptions not ignored
	 * } catch (Exception e) {
	 *     // error handling
	 * } finally {
	 *     <b>IOUtils.closeQuietly(closeable); // In case normal close was skipped due to Exception</b>
	 * }
	 * </pre>
	 * <p>
	 * Closing all streams: <br>
	 * 
	 * <pre>
	 * try {
	 * 	return IOUtils.copy(inputStream, outputStream);
	 * } finally {
	 * 	IOUtils.closeQuietly(inputStream, outputStream);
	 * }
	 * </pre>
	 *
	 * @param closeables the objects to close, may be null or already closed
	 * @see #closeQuietly(Closeable)
	 * @since 2.5
	 *
	 * @deprecated As of 2.6 removed without replacement. Please use the
	 *             try-with-resources statement or handle suppressed exceptions
	 *             manually.
	 * @see Throwable#addSuppressed(java.lang.Throwable)
	 */
	@Deprecated
	public static void closeQuietly(final Closeable... closeables) {
		if (closeables == null) {
			return;
		}
		for (final Closeable closeable : closeables) {
			closeQuietly(closeable);
		}
	}

	/**
	 * Closes a <code>Socket</code> unconditionally.
	 * <p>
	 * Equivalent to {@link Socket#close()}, except any exceptions will be ignored.
	 * This is typically used in finally blocks.
	 * <p>
	 * Example code:
	 * 
	 * <pre>
	 * Socket socket = null;
	 * try {
	 * 	socket = new Socket("http://www.foo.com/", 80);
	 * 	// process socket
	 * 	socket.close();
	 * } catch (Exception e) {
	 * 	// error handling
	 * } finally {
	 * 	IOUtils.closeQuietly(socket);
	 * }
	 * </pre>
	 *
	 * @param sock the Socket to close, may be null or already closed
	 * @since 2.0
	 *
	 * @deprecated As of 2.6 removed without replacement. Please use the
	 *             try-with-resources statement or handle suppressed exceptions
	 *             manually.
	 * @see Throwable#addSuppressed(java.lang.Throwable)
	 */
	@Deprecated
	public static void closeQuietly(final Socket sock) {
		if (sock != null) {
			try {
				sock.close();
			} catch (final IOException ioe) {
				// ignored
			}
		}
	}

	/**
	 * Closes a <code>Selector</code> unconditionally.
	 * <p>
	 * Equivalent to {@link Selector#close()}, except any exceptions will be
	 * ignored. This is typically used in finally blocks.
	 * <p>
	 * Example code:
	 * 
	 * <pre>
	 * Selector selector = null;
	 * try {
	 * 	selector = Selector.open();
	 * 	// process socket
	 *
	 * } catch (Exception e) {
	 * 	// error handling
	 * } finally {
	 * 	IOUtils.closeQuietly(selector);
	 * }
	 * </pre>
	 *
	 * @param selector the Selector to close, may be null or already closed
	 * @since 2.2
	 *
	 * @deprecated As of 2.6 removed without replacement. Please use the
	 *             try-with-resources statement or handle suppressed exceptions
	 *             manually.
	 * @see Throwable#addSuppressed(java.lang.Throwable)
	 */
	@Deprecated
	public static void closeQuietly(final Selector selector) {
		if (selector != null) {
			try {
				selector.close();
			} catch (final IOException ioe) {
				// ignored
			}
		}
	}

	/**
	 * Closes a <code>ServerSocket</code> unconditionally.
	 * <p>
	 * Equivalent to {@link ServerSocket#close()}, except any exceptions will be
	 * ignored. This is typically used in finally blocks.
	 * <p>
	 * Example code:
	 * 
	 * <pre>
	 * ServerSocket socket = null;
	 * try {
	 * 	socket = new ServerSocket();
	 * 	// process socket
	 * 	socket.close();
	 * } catch (Exception e) {
	 * 	// error handling
	 * } finally {
	 * 	IOUtils.closeQuietly(socket);
	 * }
	 * </pre>
	 *
	 * @param sock the ServerSocket to close, may be null or already closed
	 * @since 2.2
	 *
	 * @deprecated As of 2.6 removed without replacement. Please use the
	 *             try-with-resources statement or handle suppressed exceptions
	 *             manually.
	 * @see Throwable#addSuppressed(java.lang.Throwable)
	 */
	@Deprecated
	public static void closeQuietly(final ServerSocket sock) {
		if (sock != null) {
			try {
				sock.close();
			} catch (final IOException ioe) {
				// ignored
			}
		}
	}

	/**
	 * Returns the given reader if it is a {@link BufferedReader}, otherwise creates
	 * a BufferedReader from the given reader.
	 *
	 * @param reader the reader to wrap or return (not null)
	 * @return the given reader or a new {@link BufferedReader} for the given reader
	 * @throws NullPointerException if the input parameter is null
	 * @see #buffer(Reader)
	 * @since 2.2
	 */
	public static BufferedReader toBufferedReader(final Reader reader) {
		return reader instanceof BufferedReader ? (BufferedReader) reader : new BufferedReader(reader);
	}

	/**
	 * Returns the given reader if it is a {@link BufferedReader}, otherwise creates
	 * a BufferedReader from the given reader.
	 *
	 * @param reader the reader to wrap or return (not null)
	 * @param size   the buffer size, if a new BufferedReader is created.
	 * @return the given reader or a new {@link BufferedReader} for the given reader
	 * @throws NullPointerException if the input parameter is null
	 * @see #buffer(Reader)
	 * @since 2.5
	 */
	public static BufferedReader toBufferedReader(final Reader reader, int size) {
		return reader instanceof BufferedReader ? (BufferedReader) reader : new BufferedReader(reader, size);
	}

	/**
	 * Returns the given reader if it is already a {@link BufferedReader}, otherwise
	 * creates a BufferedReader from the given reader.
	 *
	 * @param reader the reader to wrap or return (not null)
	 * @return the given reader or a new {@link BufferedReader} for the given reader
	 * @throws NullPointerException if the input parameter is null
	 * @since 2.5
	 */
	public static BufferedReader buffer(final Reader reader) {
		return reader instanceof BufferedReader ? (BufferedReader) reader : new BufferedReader(reader);
	}

	/**
	 * Returns the given reader if it is already a {@link BufferedReader}, otherwise
	 * creates a BufferedReader from the given reader.
	 *
	 * @param reader the reader to wrap or return (not null)
	 * @param size   the buffer size, if a new BufferedReader is created.
	 * @return the given reader or a new {@link BufferedReader} for the given reader
	 * @throws NullPointerException if the input parameter is null
	 * @since 2.5
	 */
	public static BufferedReader buffer(final Reader reader, int size) {
		return reader instanceof BufferedReader ? (BufferedReader) reader : new BufferedReader(reader, size);
	}

	/**
	 * Returns the given Writer if it is already a {@link BufferedWriter}, otherwise
	 * creates a BufferedWriter from the given Writer.
	 *
	 * @param writer the Writer to wrap or return (not null)
	 * @return the given Writer or a new {@link BufferedWriter} for the given Writer
	 * @throws NullPointerException if the input parameter is null
	 * @since 2.5
	 */
	public static BufferedWriter buffer(final Writer writer) {
		return writer instanceof BufferedWriter ? (BufferedWriter) writer : new BufferedWriter(writer);
	}

	/**
	 * Returns the given Writer if it is already a {@link BufferedWriter}, otherwise
	 * creates a BufferedWriter from the given Writer.
	 *
	 * @param writer the Writer to wrap or return (not null)
	 * @param size   the buffer size, if a new BufferedWriter is created.
	 * @return the given Writer or a new {@link BufferedWriter} for the given Writer
	 * @throws NullPointerException if the input parameter is null
	 * @since 2.5
	 */
	public static BufferedWriter buffer(final Writer writer, int size) {
		return writer instanceof BufferedWriter ? (BufferedWriter) writer : new BufferedWriter(writer, size);
	}

	/**
	 * Returns the given OutputStream if it is already a
	 * {@link BufferedOutputStream}, otherwise creates a BufferedOutputStream from
	 * the given OutputStream.
	 *
	 * @param outputStream the OutputStream to wrap or return (not null)
	 * @return the given OutputStream or a new {@link BufferedOutputStream} for the
	 *         given OutputStream
	 * @throws NullPointerException if the input parameter is null
	 * @since 2.5
	 */
	public static BufferedOutputStream buffer(final OutputStream outputStream) {
		// reject null early on rather than waiting for IO operation to fail
		if (outputStream == null) { // not checked by BufferedOutputStream
			throw new NullPointerException();
		}
		return outputStream instanceof BufferedOutputStream ? (BufferedOutputStream) outputStream
				: new BufferedOutputStream(outputStream);
	}

	/**
	 * Returns the given OutputStream if it is already a
	 * {@link BufferedOutputStream}, otherwise creates a BufferedOutputStream from
	 * the given OutputStream.
	 *
	 * @param outputStream the OutputStream to wrap or return (not null)
	 * @param size         the buffer size, if a new BufferedOutputStream is
	 *                     created.
	 * @return the given OutputStream or a new {@link BufferedOutputStream} for the
	 *         given OutputStream
	 * @throws NullPointerException if the input parameter is null
	 * @since 2.5
	 */
	public static BufferedOutputStream buffer(final OutputStream outputStream, int size) {
		// reject null early on rather than waiting for IO operation to fail
		if (outputStream == null) { // not checked by BufferedOutputStream
			throw new NullPointerException();
		}
		return outputStream instanceof BufferedOutputStream ? (BufferedOutputStream) outputStream
				: new BufferedOutputStream(outputStream, size);
	}

	/**
	 * Returns the given InputStream if it is already a {@link BufferedInputStream},
	 * otherwise creates a BufferedInputStream from the given InputStream.
	 *
	 * @param inputStream the InputStream to wrap or return (not null)
	 * @return the given InputStream or a new {@link BufferedInputStream} for the
	 *         given InputStream
	 * @throws NullPointerException if the input parameter is null
	 * @since 2.5
	 */
	public static BufferedInputStream buffer(final InputStream inputStream) {
		// reject null early on rather than waiting for IO operation to fail
		if (inputStream == null) { // not checked by BufferedInputStream
			throw new NullPointerException();
		}
		return inputStream instanceof BufferedInputStream ? (BufferedInputStream) inputStream
				: new BufferedInputStream(inputStream);
	}

	/**
	 * Returns the given InputStream if it is already a {@link BufferedInputStream},
	 * otherwise creates a BufferedInputStream from the given InputStream.
	 *
	 * @param inputStream the InputStream to wrap or return (not null)
	 * @param size        the buffer size, if a new BufferedInputStream is created.
	 * @return the given InputStream or a new {@link BufferedInputStream} for the
	 *         given InputStream
	 * @throws NullPointerException if the input parameter is null
	 * @since 2.5
	 */
	public static BufferedInputStream buffer(final InputStream inputStream, int size) {
		// reject null early on rather than waiting for IO operation to fail
		if (inputStream == null) { // not checked by BufferedInputStream
			throw new NullPointerException();
		}
		return inputStream instanceof BufferedInputStream ? (BufferedInputStream) inputStream
				: new BufferedInputStream(inputStream, size);
	}

	/**
	 * Gets contents of an <code>InputStream</code> as a <code>byte[]</code>. Use
	 * this method instead of <code>toByteArray(InputStream)</code> when
	 * <code>InputStream</code> size is known. <b>NOTE:</b> the method checks that
	 * the length can safely be cast to an int without truncation before using
	 * {@link IOUtils#toByteArray(java.io.InputStream, int)} to read into the byte
	 * array. (Arrays can have no more than Integer.MAX_VALUE entries anyway)
	 *
	 * @param input the <code>InputStream</code> to read from
	 * @param size  the size of <code>InputStream</code>
	 * @return the requested byte array
	 * @throws IOException              if an I/O error occurs or
	 *                                  <code>InputStream</code> size differ from
	 *                                  parameter size
	 * @throws IllegalArgumentException if size is less than zero or size is greater
	 *                                  than Integer.MAX_VALUE
	 * @see IOUtils#toByteArray(java.io.InputStream, int)
	 * @since 2.1
	 */
	public static byte[] toByteArray(final InputStream input, final long size) throws IOException {

		if (size > Integer.MAX_VALUE) {
			throw new IllegalArgumentException("Size cannot be greater than Integer max value: " + size); //$NON-NLS-1$
		}

		return toByteArray(input, (int) size);
	}

	/**
	 * Gets the contents of an <code>InputStream</code> as a <code>byte[]</code>.
	 * Use this method instead of <code>toByteArray(InputStream)</code> when
	 * <code>InputStream</code> size is known
	 *
	 * @param input the <code>InputStream</code> to read from
	 * @param size  the size of <code>InputStream</code>
	 * @return the requested byte array
	 * @throws IOException              if an I/O error occurs or
	 *                                  <code>InputStream</code> size differ from
	 *                                  parameter size
	 * @throws IllegalArgumentException if size is less than zero
	 * @since 2.1
	 */
	public static byte[] toByteArray(final InputStream input, final int size) throws IOException {

		if (size < 0) {
			throw new IllegalArgumentException("Size must be equal or greater than zero: " + size); //$NON-NLS-1$
		}

		if (size == 0) {
			return new byte[0];
		}

		final byte[] data = new byte[size];
		int offset = 0;
		int readed;

		while (offset < size && (readed = input.read(data, offset, size - offset)) != EOF) {
			offset += readed;
		}

		if (offset != size) {
			throw new IOException("Unexpected readed size. current: " //$NON-NLS-1$
					+ offset + ", excepted: " //$NON-NLS-1$
					+ size);
		}

		return data;
	}

	/**
	 * Gets a URL pointing to the given classpath resource.
	 *
	 * <p>
	 * It is expected the given <code>name</code> to be absolute. The behavior is
	 * not well-defined otherwise.
	 * </p>
	 *
	 * @param name name of the desired resource
	 * @return the requested URL
	 * @throws IOException if an I/O error occurs
	 * 
	 * @since 2.6
	 */
	public static URL resourceToURL(final String name) throws IOException {
		return resourceToURL(name, null);
	}

	/**
	 * Gets a URL pointing to the given classpath resource.
	 *
	 * <p>
	 * It is expected the given <code>name</code> to be absolute. The behavior is
	 * not well-defined otherwise.
	 * </p>
	 *
	 * @param name        name of the desired resource
	 * @param classLoader the class loader that the resolution of the resource is
	 *                    delegated to
	 * @return the requested URL
	 * @throws IOException if an I/O error occurs
	 * 
	 * @since 2.6
	 */
	public static URL resourceToURL(final String name, final ClassLoader classLoader) throws IOException {
		// What about the thread context class loader?
		// What about the system class loader?
		final URL resource = classLoader == null ? IOUtils.class.getResource(name) : classLoader.getResource(name);

		if (resource == null) {
			throw new IOException("Resource not found: " + name); //$NON-NLS-1$
		}

		return resource;
	}

	// -----------------------------------------------------------------------

	/**
	 * Converts the specified CharSequence to an input stream, encoded as bytes
	 * using the default character encoding of the platform.
	 *
	 * @param input the CharSequence to convert
	 * @return an input stream
	 * @since 2.0
	 * @deprecated 2.5 use {@link #toInputStream(CharSequence, Charset)} instead
	 */
	@Deprecated
	public static InputStream toInputStream(final CharSequence input) {
		return toInputStream(input, Charset.defaultCharset());
	}

	/**
	 * Converts the specified CharSequence to an input stream, encoded as bytes
	 * using the specified character encoding.
	 *
	 * @param input    the CharSequence to convert
	 * @param encoding the encoding to use, null means platform default
	 * @return an input stream
	 * @since 2.3
	 */
	public static InputStream toInputStream(final CharSequence input, final Charset encoding) {
		return toInputStream(input.toString(), encoding);
	}

	// write byte[]
	// -----------------------------------------------------------------------

	/**
	 * Writes bytes from a <code>byte[]</code> to an <code>OutputStream</code>.
	 *
	 * @param data   the byte array to write, do not modify during output, null
	 *               ignored
	 * @param output the <code>OutputStream</code> to write to
	 * @throws NullPointerException if output is null
	 * @throws IOException          if an I/O error occurs
	 * @since 1.1
	 */
	public static void write(final byte[] data, final OutputStream output) throws IOException {
		if (data != null) {
			output.write(data);
		}
	}

	/**
	 * Writes bytes from a <code>byte[]</code> to an <code>OutputStream</code> using
	 * chunked writes. This is intended for writing very large byte arrays which
	 * might otherwise cause excessive memory usage if the native code has to
	 * allocate a copy.
	 *
	 * @param data   the byte array to write, do not modify during output, null
	 *               ignored
	 * @param output the <code>OutputStream</code> to write to
	 * @throws NullPointerException if output is null
	 * @throws IOException          if an I/O error occurs
	 * @since 2.5
	 */
	public static void writeChunked(final byte[] data, final OutputStream output) throws IOException {
		if (data != null) {
			int bytes = data.length;
			int offset = 0;
			while (bytes > 0) {
				int chunk = Math.min(bytes, DEFAULT_BUFFER_SIZE);
				output.write(data, offset, chunk);
				bytes -= chunk;
				offset += chunk;
			}
		}
	}

	// write char[]
	// -----------------------------------------------------------------------

	/**
	 * Writes chars from a <code>char[]</code> to a <code>Writer</code>
	 *
	 * @param data   the char array to write, do not modify during output, null
	 *               ignored
	 * @param output the <code>Writer</code> to write to
	 * @throws NullPointerException if output is null
	 * @throws IOException          if an I/O error occurs
	 * @since 1.1
	 */
	public static void write(final char[] data, final Writer output) throws IOException {
		if (data != null) {
			output.write(data);
		}
	}

	/**
	 * Writes chars from a <code>char[]</code> to a <code>Writer</code> using
	 * chunked writes. This is intended for writing very large byte arrays which
	 * might otherwise cause excessive memory usage if the native code has to
	 * allocate a copy.
	 *
	 * @param data   the char array to write, do not modify during output, null
	 *               ignored
	 * @param output the <code>Writer</code> to write to
	 * @throws NullPointerException if output is null
	 * @throws IOException          if an I/O error occurs
	 * @since 2.5
	 */
	public static void writeChunked(final char[] data, final Writer output) throws IOException {
		if (data != null) {
			int bytes = data.length;
			int offset = 0;
			while (bytes > 0) {
				int chunk = Math.min(bytes, DEFAULT_BUFFER_SIZE);
				output.write(data, offset, chunk);
				bytes -= chunk;
				offset += chunk;
			}
		}
	}

	// write CharSequence
	// -----------------------------------------------------------------------

	/**
	 * Writes chars from a <code>CharSequence</code> to a <code>Writer</code>.
	 *
	 * @param data   the <code>CharSequence</code> to write, null ignored
	 * @param output the <code>Writer</code> to write to
	 * @throws NullPointerException if output is null
	 * @throws IOException          if an I/O error occurs
	 * @since 2.0
	 */
	public static void write(final CharSequence data, final Writer output) throws IOException {
		if (data != null) {
			write(data.toString(), output);
		}
	}

	/**
	 * Writes chars from a <code>CharSequence</code> to bytes on an
	 * <code>OutputStream</code> using the default character encoding of the
	 * platform.
	 * <p>
	 * This method uses {@link String#getBytes()}.
	 *
	 * @param data   the <code>CharSequence</code> to write, null ignored
	 * @param output the <code>OutputStream</code> to write to
	 * @throws NullPointerException if output is null
	 * @throws IOException          if an I/O error occurs
	 * @since 2.0
	 * @deprecated 2.5 use {@link #write(CharSequence, OutputStream, Charset)}
	 *             instead
	 */
	@Deprecated
	public static void write(final CharSequence data, final OutputStream output) throws IOException {
		write(data, output, Charset.defaultCharset());
	}

	/**
	 * Writes chars from a <code>CharSequence</code> to bytes on an
	 * <code>OutputStream</code> using the specified character encoding.
	 * <p>
	 * This method uses {@link String#getBytes(String)}.
	 *
	 * @param data     the <code>CharSequence</code> to write, null ignored
	 * @param output   the <code>OutputStream</code> to write to
	 * @param encoding the encoding to use, null means platform default
	 * @throws NullPointerException if output is null
	 * @throws IOException          if an I/O error occurs
	 * @since 2.3
	 */
	public static void write(final CharSequence data, final OutputStream output, final Charset encoding)
			throws IOException {
		if (data != null) {
			write(data.toString(), output, encoding);
		}
	}

	// copy from InputStream
	// -----------------------------------------------------------------------

	/**
	 * Copies bytes from an <code>InputStream</code> to an
	 * <code>OutputStream</code>.
	 * <p>
	 * This method buffers the input internally, so there is no need to use a
	 * <code>BufferedInputStream</code>.
	 * <p>
	 * Large streams (over 2GB) will return a bytes copied value of <code>-1</code>
	 * after the copy has completed since the correct number of bytes cannot be
	 * returned as an int. For large streams use the
	 * <code>copyLarge(InputStream, OutputStream)</code> method.
	 *
	 * @param input  the <code>InputStream</code> to read from
	 * @param output the <code>OutputStream</code> to write to
	 * @return the number of bytes copied, or -1 if &gt; Integer.MAX_VALUE
	 * @throws NullPointerException if the input or output is null
	 * @throws IOException          if an I/O error occurs
	 * @since 1.1
	 */
	public static int copy(final InputStream input, final OutputStream output) throws IOException {
		final long count = copyLarge(input, output);
		if (count > Integer.MAX_VALUE) {
			return -1;
		}
		return (int) count;
	}

	/**
	 * Copies bytes from an <code>InputStream</code> to an <code>OutputStream</code>
	 * using an internal buffer of the given size.
	 * <p>
	 * This method buffers the input internally, so there is no need to use a
	 * <code>BufferedInputStream</code>.
	 * <p>
	 *
	 * @param input      the <code>InputStream</code> to read from
	 * @param output     the <code>OutputStream</code> to write to
	 * @param bufferSize the bufferSize used to copy from the input to the output
	 * @return the number of bytes copied
	 * @throws NullPointerException if the input or output is null
	 * @throws IOException          if an I/O error occurs
	 * @since 2.5
	 */
	public static long copy(final InputStream input, final OutputStream output, final int bufferSize)
			throws IOException {
		return copyLarge(input, output, new byte[bufferSize]);
	}

	/**
	 * Copies bytes from a large (over 2GB) <code>InputStream</code> to an
	 * <code>OutputStream</code>.
	 * <p>
	 * This method buffers the input internally, so there is no need to use a
	 * <code>BufferedInputStream</code>.
	 * <p>
	 * The buffer size is given by {@link #DEFAULT_BUFFER_SIZE}.
	 *
	 * @param input  the <code>InputStream</code> to read from
	 * @param output the <code>OutputStream</code> to write to
	 * @return the number of bytes copied
	 * @throws NullPointerException if the input or output is null
	 * @throws IOException          if an I/O error occurs
	 * @since 1.3
	 */
	public static long copyLarge(final InputStream input, final OutputStream output) throws IOException {
		return copy(input, output, DEFAULT_BUFFER_SIZE);
	}

	/**
	 * Copies bytes from a large (over 2GB) <code>InputStream</code> to an
	 * <code>OutputStream</code>.
	 * <p>
	 * This method uses the provided buffer, so there is no need to use a
	 * <code>BufferedInputStream</code>.
	 * <p>
	 *
	 * @param input  the <code>InputStream</code> to read from
	 * @param output the <code>OutputStream</code> to write to
	 * @param buffer the buffer to use for the copy
	 * @return the number of bytes copied
	 * @throws NullPointerException if the input or output is null
	 * @throws IOException          if an I/O error occurs
	 * @since 2.2
	 */
	public static long copyLarge(final InputStream input, final OutputStream output, final byte[] buffer)
			throws IOException {
		long count = 0;
		int n;
		while (EOF != (n = input.read(buffer))) {
			output.write(buffer, 0, n);
			count += n;
		}
		return count;
	}

	/**
	 * Copies some or all bytes from a large (over 2GB) <code>InputStream</code> to
	 * an <code>OutputStream</code>, optionally skipping input bytes.
	 * <p>
	 * This method buffers the input internally, so there is no need to use a
	 * <code>BufferedInputStream</code>.
	 * </p>
	 * <p>
	 * Note that the implementation uses {@link #skip(InputStream, long)}. This
	 * means that the method may be considerably less efficient than using the
	 * actual skip implementation, this is done to guarantee that the correct number
	 * of characters are skipped.
	 * </p>
	 * The buffer size is given by {@link #DEFAULT_BUFFER_SIZE}.
	 *
	 * @param input       the <code>InputStream</code> to read from
	 * @param output      the <code>OutputStream</code> to write to
	 * @param inputOffset : number of bytes to skip from input before copying -ve
	 *                    values are ignored
	 * @param length      : number of bytes to copy. -ve means all
	 * @return the number of bytes copied
	 * @throws NullPointerException if the input or output is null
	 * @throws IOException          if an I/O error occurs
	 * @since 2.2
	 */
	public static long copyLarge(final InputStream input, final OutputStream output, final long inputOffset,
			final long length) throws IOException {
		return copyLarge(input, output, inputOffset, length, new byte[DEFAULT_BUFFER_SIZE]);
	}

	/**
	 * Copies some or all bytes from a large (over 2GB) <code>InputStream</code> to
	 * an <code>OutputStream</code>, optionally skipping input bytes.
	 * <p>
	 * This method uses the provided buffer, so there is no need to use a
	 * <code>BufferedInputStream</code>.
	 * </p>
	 * <p>
	 * Note that the implementation uses {@link #skip(InputStream, long)}. This
	 * means that the method may be considerably less efficient than using the
	 * actual skip implementation, this is done to guarantee that the correct number
	 * of characters are skipped.
	 * </p>
	 *
	 * @param input       the <code>InputStream</code> to read from
	 * @param output      the <code>OutputStream</code> to write to
	 * @param inputOffset : number of bytes to skip from input before copying -ve
	 *                    values are ignored
	 * @param length      : number of bytes to copy. -ve means all
	 * @param buffer      the buffer to use for the copy
	 * @return the number of bytes copied
	 * @throws NullPointerException if the input or output is null
	 * @throws IOException          if an I/O error occurs
	 * @since 2.2
	 */
	public static long copyLarge(final InputStream input, final OutputStream output, final long inputOffset,
			final long length, final byte[] buffer) throws IOException {
		if (inputOffset > 0) {
			skipFully(input, inputOffset);
		}
		if (length == 0) {
			return 0;
		}
		final int bufferLength = buffer.length;
		int bytesToRead = bufferLength;
		if (length > 0 && length < bufferLength) {
			bytesToRead = (int) length;
		}
		int read;
		long totalRead = 0;
		while (bytesToRead > 0 && EOF != (read = input.read(buffer, 0, bytesToRead))) {
			output.write(buffer, 0, read);
			totalRead += read;
			if (length > 0) { // only adjust length if not reading to the end
								// Note the cast must work because buffer.length is an integer
				bytesToRead = (int) Math.min(length - totalRead, bufferLength);
			}
		}
		return totalRead;
	}

	// content equals
	// -----------------------------------------------------------------------

	/**
	 * Compares the contents of two Streams to determine if they are equal or not.
	 * <p>
	 * This method buffers the input internally using
	 * <code>BufferedInputStream</code> if they are not already buffered.
	 *
	 * @param input1 the first stream
	 * @param input2 the second stream
	 * @return true if the content of the streams are equal or they both don't
	 *         exist, false otherwise
	 * @throws NullPointerException if either input is null
	 * @throws IOException          if an I/O error occurs
	 */
	public static boolean contentEquals(InputStream input1, InputStream input2) throws IOException {
		if (input1 == input2) {
			return true;
		}
		if (!(input1 instanceof BufferedInputStream)) {
			input1 = new BufferedInputStream(input1);
		}
		if (!(input2 instanceof BufferedInputStream)) {
			input2 = new BufferedInputStream(input2);
		}

		int ch = input1.read();
		while (EOF != ch) {
			final int ch2 = input2.read();
			if (ch != ch2) {
				return false;
			}
			ch = input1.read();
		}

		final int ch2 = input2.read();
		return ch2 == EOF;
	}

	/**
	 * Compares the contents of two Readers to determine if they are equal or not.
	 * <p>
	 * This method buffers the input internally using <code>BufferedReader</code> if
	 * they are not already buffered.
	 *
	 * @param input1 the first reader
	 * @param input2 the second reader
	 * @return true if the content of the readers are equal or they both don't
	 *         exist, false otherwise
	 * @throws NullPointerException if either input is null
	 * @throws IOException          if an I/O error occurs
	 * @since 1.1
	 */
	public static boolean contentEquals(Reader input1, Reader input2) throws IOException {
		if (input1 == input2) {
			return true;
		}

		input1 = toBufferedReader(input1);
		input2 = toBufferedReader(input2);

		int ch = input1.read();
		while (EOF != ch) {
			final int ch2 = input2.read();
			if (ch != ch2) {
				return false;
			}
			ch = input1.read();
		}

		final int ch2 = input2.read();
		return ch2 == EOF;
	}

	/**
	 * Compares the contents of two Readers to determine if they are equal or not,
	 * ignoring EOL characters.
	 * <p>
	 * This method buffers the input internally using <code>BufferedReader</code> if
	 * they are not already buffered.
	 *
	 * @param input1 the first reader
	 * @param input2 the second reader
	 * @return true if the content of the readers are equal (ignoring EOL
	 *         differences), false otherwise
	 * @throws NullPointerException if either input is null
	 * @throws IOException          if an I/O error occurs
	 * @since 2.2
	 */
	public static boolean contentEqualsIgnoreEOL(final Reader input1, final Reader input2) throws IOException {
		if (input1 == input2) {
			return true;
		}
		final BufferedReader br1 = toBufferedReader(input1);
		final BufferedReader br2 = toBufferedReader(input2);

		String line1 = br1.readLine();
		String line2 = br2.readLine();
		while (line1 != null && line2 != null && line1.equals(line2)) {
			line1 = br1.readLine();
			line2 = br2.readLine();
		}
		return line1 == null ? line2 == null ? true : false : line1.equals(line2);
	}

	/**
	 * Skips bytes from an input byte stream. This implementation guarantees that it
	 * will read as many bytes as possible before giving up; this may not always be
	 * the case for skip() implementations in subclasses of {@link InputStream}.
	 * <p>
	 * Note that the implementation uses {@link InputStream#read(byte[], int, int)}
	 * rather than delegating to {@link InputStream#skip(long)}. This means that the
	 * method may be considerably less efficient than using the actual skip
	 * implementation, this is done to guarantee that the correct number of bytes
	 * are skipped.
	 * </p>
	 *
	 * @param input  byte stream to skip
	 * @param toSkip number of bytes to skip.
	 * @return number of bytes actually skipped.
	 * @throws IOException              if there is a problem reading the file
	 * @throws IllegalArgumentException if toSkip is negative
	 * @see InputStream#skip(long)
	 * @see <a href="https://issues.apache.org/jira/browse/IO-203">IO-203 - Add
	 *      skipFully() method for InputStreams</a>
	 * @since 2.0
	 */
	public static long skip(final InputStream input, final long toSkip) throws IOException {
		if (toSkip < 0) {
			throw new IllegalArgumentException("Skip count must be non-negative, actual: " + toSkip); //$NON-NLS-1$
		}
		/*
		 * N.B. no need to synchronize this because: - we don't care if the buffer is
		 * created multiple times (the data is ignored) - we always use the same size
		 * buffer, so if it it is recreated it will still be OK (if the buffer size were
		 * variable, we would need to synch. to ensure some other thread did not create
		 * a smaller one)
		 */
		if (SKIP_BYTE_BUFFER == null) {
			SKIP_BYTE_BUFFER = new byte[SKIP_BUFFER_SIZE];
		}
		long remain = toSkip;
		while (remain > 0) {
			// See https://issues.apache.org/jira/browse/IO-203 for why we use
			// read() rather than delegating to skip()
			final long n = input.read(SKIP_BYTE_BUFFER, 0, (int) Math.min(remain, SKIP_BUFFER_SIZE));
			if (n < 0) { // EOF
				break;
			}
			remain -= n;
		}
		return toSkip - remain;
	}

	/**
	 * Skips bytes from a ReadableByteChannel. This implementation guarantees that
	 * it will read as many bytes as possible before giving up.
	 *
	 * @param input  ReadableByteChannel to skip
	 * @param toSkip number of bytes to skip.
	 * @return number of bytes actually skipped.
	 * @throws IOException              if there is a problem reading the
	 *                                  ReadableByteChannel
	 * @throws IllegalArgumentException if toSkip is negative
	 * @since 2.5
	 */
	public static long skip(final ReadableByteChannel input, final long toSkip) throws IOException {
		if (toSkip < 0) {
			throw new IllegalArgumentException("Skip count must be non-negative, actual: " + toSkip); //$NON-NLS-1$
		}
		final ByteBuffer skipByteBuffer = ByteBuffer.allocate((int) Math.min(toSkip, SKIP_BUFFER_SIZE));
		long remain = toSkip;
		while (remain > 0) {
			skipByteBuffer.position(0);
			skipByteBuffer.limit((int) Math.min(remain, SKIP_BUFFER_SIZE));
			final int n = input.read(skipByteBuffer);
			if (n == EOF) {
				break;
			}
			remain -= n;
		}
		return toSkip - remain;
	}

	/**
	 * Skips characters from an input character stream. This implementation
	 * guarantees that it will read as many characters as possible before giving up;
	 * this may not always be the case for skip() implementations in subclasses of
	 * {@link Reader}.
	 * <p>
	 * Note that the implementation uses {@link Reader#read(char[], int, int)}
	 * rather than delegating to {@link Reader#skip(long)}. This means that the
	 * method may be considerably less efficient than using the actual skip
	 * implementation, this is done to guarantee that the correct number of
	 * characters are skipped.
	 * </p>
	 *
	 * @param input  character stream to skip
	 * @param toSkip number of characters to skip.
	 * @return number of characters actually skipped.
	 * @throws IOException              if there is a problem reading the file
	 * @throws IllegalArgumentException if toSkip is negative
	 * @see Reader#skip(long)
	 * @see <a href="https://issues.apache.org/jira/browse/IO-203">IO-203 - Add
	 *      skipFully() method for InputStreams</a>
	 * @since 2.0
	 */
	public static long skip(final Reader input, final long toSkip) throws IOException {
		if (toSkip < 0) {
			throw new IllegalArgumentException("Skip count must be non-negative, actual: " + toSkip); //$NON-NLS-1$
		}
		/*
		 * N.B. no need to synchronize this because: - we don't care if the buffer is
		 * created multiple times (the data is ignored) - we always use the same size
		 * buffer, so if it it is recreated it will still be OK (if the buffer size were
		 * variable, we would need to synch. to ensure some other thread did not create
		 * a smaller one)
		 */
		if (SKIP_CHAR_BUFFER == null) {
			SKIP_CHAR_BUFFER = new char[SKIP_BUFFER_SIZE];
		}
		long remain = toSkip;
		while (remain > 0) {
			// See https://issues.apache.org/jira/browse/IO-203 for why we use
			// read() rather than delegating to skip()
			final long n = input.read(SKIP_CHAR_BUFFER, 0, (int) Math.min(remain, SKIP_BUFFER_SIZE));
			if (n < 0) { // EOF
				break;
			}
			remain -= n;
		}
		return toSkip - remain;
	}

	/**
	 * Skips the requested number of bytes or fail if there are not enough left.
	 * <p>
	 * This allows for the possibility that {@link InputStream#skip(long)} may not
	 * skip as many bytes as requested (most likely because of reaching EOF).
	 * <p>
	 * Note that the implementation uses {@link #skip(InputStream, long)}. This
	 * means that the method may be considerably less efficient than using the
	 * actual skip implementation, this is done to guarantee that the correct number
	 * of characters are skipped.
	 * </p>
	 *
	 * @param input  stream to skip
	 * @param toSkip the number of bytes to skip
	 * @throws IOException              if there is a problem reading the file
	 * @throws IllegalArgumentException if toSkip is negative
	 * @throws EOFException             if the number of bytes skipped was incorrect
	 * @see InputStream#skip(long)
	 * @since 2.0
	 */
	public static void skipFully(final InputStream input, final long toSkip) throws IOException {
		if (toSkip < 0) {
			throw new IllegalArgumentException("Bytes to skip must not be negative: " + toSkip); //$NON-NLS-1$
		}
		final long skipped = skip(input, toSkip);
		if (skipped != toSkip) {
			throw new EOFException("Bytes to skip: " + toSkip + " actual: " + skipped); //$NON-NLS-1$ //$NON-NLS-2$
		}
	}

	/**
	 * Skips the requested number of bytes or fail if there are not enough left.
	 *
	 * @param input  ReadableByteChannel to skip
	 * @param toSkip the number of bytes to skip
	 * @throws IOException              if there is a problem reading the
	 *                                  ReadableByteChannel
	 * @throws IllegalArgumentException if toSkip is negative
	 * @throws EOFException             if the number of bytes skipped was incorrect
	 * @since 2.5
	 */
	public static void skipFully(final ReadableByteChannel input, final long toSkip) throws IOException {
		if (toSkip < 0) {
			throw new IllegalArgumentException("Bytes to skip must not be negative: " + toSkip); //$NON-NLS-1$
		}
		final long skipped = skip(input, toSkip);
		if (skipped != toSkip) {
			throw new EOFException("Bytes to skip: " + toSkip + " actual: " + skipped); //$NON-NLS-1$ //$NON-NLS-2$
		}
	}

	/**
	 * Skips the requested number of characters or fail if there are not enough
	 * left.
	 * <p>
	 * This allows for the possibility that {@link Reader#skip(long)} may not skip
	 * as many characters as requested (most likely because of reaching EOF).
	 * <p>
	 * Note that the implementation uses {@link #skip(Reader, long)}. This means
	 * that the method may be considerably less efficient than using the actual skip
	 * implementation, this is done to guarantee that the correct number of
	 * characters are skipped.
	 * </p>
	 *
	 * @param input  stream to skip
	 * @param toSkip the number of characters to skip
	 * @throws IOException              if there is a problem reading the file
	 * @throws IllegalArgumentException if toSkip is negative
	 * @throws EOFException             if the number of characters skipped was
	 *                                  incorrect
	 * @see Reader#skip(long)
	 * @since 2.0
	 */
	public static void skipFully(final Reader input, final long toSkip) throws IOException {
		final long skipped = skip(input, toSkip);
		if (skipped != toSkip) {
			throw new EOFException("Chars to skip: " + toSkip + " actual: " + skipped); //$NON-NLS-1$ //$NON-NLS-2$
		}
	}

	/**
	 * Reads characters from an input character stream. This implementation
	 * guarantees that it will read as many characters as possible before giving up;
	 * this may not always be the case for subclasses of {@link Reader}.
	 *
	 * @param input  where to read input from
	 * @param buffer destination
	 * @param offset initial offset into buffer
	 * @param length length to read, must be &gt;= 0
	 * @return actual length read; may be less than requested if EOF was reached
	 * @throws IOException if a read error occurs
	 * @since 2.2
	 */
	public static int read(final Reader input, final char[] buffer, final int offset, final int length)
			throws IOException {
		if (length < 0) {
			throw new IllegalArgumentException("Length must not be negative: " + length); //$NON-NLS-1$
		}
		int remaining = length;
		while (remaining > 0) {
			final int location = length - remaining;
			final int count = input.read(buffer, offset + location, remaining);
			if (EOF == count) { // EOF
				break;
			}
			remaining -= count;
		}
		return length - remaining;
	}

	/**
	 * Reads characters from an input character stream. This implementation
	 * guarantees that it will read as many characters as possible before giving up;
	 * this may not always be the case for subclasses of {@link Reader}.
	 *
	 * @param input  where to read input from
	 * @param buffer destination
	 * @return actual length read; may be less than requested if EOF was reached
	 * @throws IOException if a read error occurs
	 * @since 2.2
	 */
	public static int read(final Reader input, final char[] buffer) throws IOException {
		return read(input, buffer, 0, buffer.length);
	}

	/**
	 * Reads bytes from an input stream. This implementation guarantees that it will
	 * read as many bytes as possible before giving up; this may not always be the
	 * case for subclasses of {@link InputStream}.
	 *
	 * @param input  where to read input from
	 * @param buffer destination
	 * @param offset initial offset into buffer
	 * @param length length to read, must be &gt;= 0
	 * @return actual length read; may be less than requested if EOF was reached
	 * @throws IOException if a read error occurs
	 * @since 2.2
	 */
	public static int read(final InputStream input, final byte[] buffer, final int offset, final int length)
			throws IOException {
		if (length < 0) {
			throw new IllegalArgumentException("Length must not be negative: " + length); //$NON-NLS-1$
		}
		int remaining = length;
		while (remaining > 0) {
			final int location = length - remaining;
			final int count = input.read(buffer, offset + location, remaining);
			if (EOF == count) { // EOF
				break;
			}
			remaining -= count;
		}
		return length - remaining;
	}

	/**
	 * Reads bytes from an input stream. This implementation guarantees that it will
	 * read as many bytes as possible before giving up; this may not always be the
	 * case for subclasses of {@link InputStream}.
	 *
	 * @param input  where to read input from
	 * @param buffer destination
	 * @return actual length read; may be less than requested if EOF was reached
	 * @throws IOException if a read error occurs
	 * @since 2.2
	 */
	public static int read(final InputStream input, final byte[] buffer) throws IOException {
		return read(input, buffer, 0, buffer.length);
	}

	/**
	 * Reads bytes from a ReadableByteChannel.
	 * <p>
	 * This implementation guarantees that it will read as many bytes as possible
	 * before giving up; this may not always be the case for subclasses of
	 * {@link ReadableByteChannel}.
	 *
	 * @param input  the byte channel to read
	 * @param buffer byte buffer destination
	 * @return the actual length read; may be less than requested if EOF was reached
	 * @throws IOException if a read error occurs
	 * @since 2.5
	 */
	public static int read(final ReadableByteChannel input, final ByteBuffer buffer) throws IOException {
		final int length = buffer.remaining();
		while (buffer.remaining() > 0) {
			final int count = input.read(buffer);
			if (EOF == count) { // EOF
				break;
			}
		}
		return length - buffer.remaining();
	}

	/**
	 * Reads the requested number of characters or fail if there are not enough
	 * left.
	 * <p>
	 * This allows for the possibility that {@link Reader#read(char[], int, int)}
	 * may not read as many characters as requested (most likely because of reaching
	 * EOF).
	 *
	 * @param input  where to read input from
	 * @param buffer destination
	 * @param offset initial offset into buffer
	 * @param length length to read, must be &gt;= 0
	 * @throws IOException              if there is a problem reading the file
	 * @throws IllegalArgumentException if length is negative
	 * @throws EOFException             if the number of characters read was
	 *                                  incorrect
	 * @since 2.2
	 */
	public static void readFully(final Reader input, final char[] buffer, final int offset, final int length)
			throws IOException {
		final int actual = read(input, buffer, offset, length);
		if (actual != length) {
			throw new EOFException("Length to read: " + length + " actual: " + actual); //$NON-NLS-1$ //$NON-NLS-2$
		}
	}

	/**
	 * Reads the requested number of characters or fail if there are not enough
	 * left.
	 * <p>
	 * This allows for the possibility that {@link Reader#read(char[], int, int)}
	 * may not read as many characters as requested (most likely because of reaching
	 * EOF).
	 *
	 * @param input  where to read input from
	 * @param buffer destination
	 * @throws IOException              if there is a problem reading the file
	 * @throws IllegalArgumentException if length is negative
	 * @throws EOFException             if the number of characters read was
	 *                                  incorrect
	 * @since 2.2
	 */
	public static void readFully(final Reader input, final char[] buffer) throws IOException {
		readFully(input, buffer, 0, buffer.length);
	}

	/**
	 * Reads the requested number of bytes or fail if there are not enough left.
	 * <p>
	 * This allows for the possibility that
	 * {@link InputStream#read(byte[], int, int)} may not read as many bytes as
	 * requested (most likely because of reaching EOF).
	 *
	 * @param input  where to read input from
	 * @param buffer destination
	 * @param offset initial offset into buffer
	 * @param length length to read, must be &gt;= 0
	 * @throws IOException              if there is a problem reading the file
	 * @throws IllegalArgumentException if length is negative
	 * @throws EOFException             if the number of bytes read was incorrect
	 * @since 2.2
	 */
	public static void readFully(final InputStream input, final byte[] buffer, final int offset, final int length)
			throws IOException {
		final int actual = read(input, buffer, offset, length);
		if (actual != length) {
			throw new EOFException("Length to read: " + length + " actual: " + actual); //$NON-NLS-1$ //$NON-NLS-2$
		}
	}

	/**
	 * Reads the requested number of bytes or fail if there are not enough left.
	 * <p>
	 * This allows for the possibility that
	 * {@link InputStream#read(byte[], int, int)} may not read as many bytes as
	 * requested (most likely because of reaching EOF).
	 *
	 * @param input  where to read input from
	 * @param buffer destination
	 * @throws IOException              if there is a problem reading the file
	 * @throws IllegalArgumentException if length is negative
	 * @throws EOFException             if the number of bytes read was incorrect
	 * @since 2.2
	 */
	public static void readFully(final InputStream input, final byte[] buffer) throws IOException {
		readFully(input, buffer, 0, buffer.length);
	}

	/**
	 * Reads the requested number of bytes or fail if there are not enough left.
	 * <p>
	 * This allows for the possibility that
	 * {@link InputStream#read(byte[], int, int)} may not read as many bytes as
	 * requested (most likely because of reaching EOF).
	 *
	 * @param input  where to read input from
	 * @param length length to read, must be &gt;= 0
	 * @return the bytes read from input
	 * @throws IOException              if there is a problem reading the file
	 * @throws IllegalArgumentException if length is negative
	 * @throws EOFException             if the number of bytes read was incorrect
	 * @since 2.5
	 */
	public static byte[] readFully(final InputStream input, final int length) throws IOException {
		final byte[] buffer = new byte[length];
		readFully(input, buffer, 0, buffer.length);
		return buffer;
	}

	/**
	 * Reads the requested number of bytes or fail if there are not enough left.
	 * <p>
	 * This allows for the possibility that
	 * {@link ReadableByteChannel#read(ByteBuffer)} may not read as many bytes as
	 * requested (most likely because of reaching EOF).
	 *
	 * @param input  the byte channel to read
	 * @param buffer byte buffer destination
	 * @throws IOException  if there is a problem reading the file
	 * @throws EOFException if the number of bytes read was incorrect
	 * @since 2.5
	 */
	public static void readFully(final ReadableByteChannel input, final ByteBuffer buffer) throws IOException {
		final int expected = buffer.remaining();
		final int actual = read(input, buffer);
		if (actual != expected) {
			throw new EOFException("Length to read: " + expected + " actual: " + actual); //$NON-NLS-1$ //$NON-NLS-2$
		}
	}

	public static byte[] readInputStream(InputStream inStream) throws Exception {
		ByteArrayOutputStream outStream = new ByteArrayOutputStream();
		byte[] buffer = new byte[1024];
		int len = 0;
		while ((len = inStream.read(buffer)) != -1) {
			outStream.write(buffer, 0, len);
		}
		byte[] data = outStream.toByteArray();
		outStream.close();
		inStream.close();
		return data;
	}

}