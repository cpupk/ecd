package org.sf.feeling.decompiler.util;

import java.util.Comparator;

import org.sf.feeling.decompiler.editor.IDecompilerDescriptor;

/**
 * Sort available decompiler plugins by their
 * {@link IDecompilerDescriptor#getDefaultPriority()}.
 */
public class DefaultDecompilerDescriptorComparator implements Comparator<IDecompilerDescriptor> {

	@Override
	public int compare(IDecompilerDescriptor o1, IDecompilerDescriptor o2) {
		int res = Integer.compare(o2.getDefaultPriority(), o1.getDefaultPriority());
		if (res != 0) {
			return res;
		}
		return o1.getDecompilerType().compareTo(o2.getDecompilerType());
	}

}
