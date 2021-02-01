
package org.sf.feeling.decompiler.cfr.decompiler;

import java.io.IOException;
import java.io.StringWriter;

import org.benf.cfr.reader.entities.Method;
import org.benf.cfr.reader.state.TypeUsageInformation;
import org.benf.cfr.reader.util.getopt.Options;
import org.benf.cfr.reader.util.output.IllegalIdentifierDump;
import org.benf.cfr.reader.util.output.StreamDumper;

public class StringDumper extends StreamDumper {

	private StringWriter sw = new StringWriter();

	public StringDumper(TypeUsageInformation typeUsageInformation, Options options,
			IllegalIdentifierDump illegalIdentifierDump) {
		super(typeUsageInformation, options, illegalIdentifierDump);
	}

	@Override
	public void addSummaryError(Method paramMethod, String paramString) {

	}

	@Override
	public void close() {
		try {
			sw.close();
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	protected void write(String source) {
		sw.write(source);
	}

	@Override
	public String toString() {
		return sw.toString();
	}
}
