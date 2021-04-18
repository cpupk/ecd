
package org.sf.feeling.decompiler.cfr.decompiler;

import java.io.Closeable;

import org.benf.cfr.reader.state.TypeUsageInformation;
import org.benf.cfr.reader.util.getopt.Options;
import org.benf.cfr.reader.util.output.IllegalIdentifierDump;
import org.benf.cfr.reader.util.output.MethodErrorCollector;
import org.benf.cfr.reader.util.output.StringStreamDumper;

public class StringDumper extends StringStreamDumper implements Closeable {

	public StringDumper(MethodErrorCollector methodErrorCollector, StringBuilder sb,
			TypeUsageInformation typeUsageInformation, Options options, IllegalIdentifierDump illegalIdentifierDump) {
		super(methodErrorCollector, sb, typeUsageInformation, options, illegalIdentifierDump);
	}

}
