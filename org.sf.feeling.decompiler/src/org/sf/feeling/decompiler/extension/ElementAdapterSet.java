package org.sf.feeling.decompiler.extension;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.TreeSet;
import java.util.logging.Level;

/**
 * ElementAdapterSet
 */
class ElementAdapterSet extends TreeSet<DecompilerAdapter> {

	private static final long serialVersionUID = -3451274084543012212L;

	private static Comparator comparator = new Comparator() {

		@Override
		public int compare(Object o1, Object o2) {
			if (o1 instanceof DecompilerAdapter && o2 instanceof DecompilerAdapter) {
				DecompilerAdapter adapter1 = (DecompilerAdapter) o1;
				DecompilerAdapter adapter2 = (DecompilerAdapter) o2;
				if (adapter1.equals(adapter2)) {
					return 0;
				}
				int value = adapter1.getPriority() - adapter2.getPriority();
				return value == 0 ? 1 : value;
			}
			return 0;
		}
	};

	private List<String> overwriteList;

	private boolean overWrittenAdaptersRemoved;

	/**
	 * A TreeSet sorted by ElementAdapter.getPriority( ).
	 */
	public ElementAdapterSet() {
		super(comparator);
	}

	@Override
	public boolean add(DecompilerAdapter o) {
		if (o instanceof DecompilerAdapter) {
			// cached overwrited adapters
			DecompilerAdapter adapter = (DecompilerAdapter) o;
			String[] overwriteIds = adapter.getOverwrite();
			if (overwriteIds != null && overwriteIds.length > 0) {
				if (this.overwriteList == null) {
					this.overwriteList = new ArrayList<>();
				}
				Collections.addAll(overwriteList, overwriteIds);
			}
			return super.add(o);
		}
		return false;
	}

	/**
	 * remove overwritten adapters.
	 */
	public void removeOverwrittenAdapters() {
		if (!overWrittenAdaptersRemoved && this.overwriteList != null) {
			for (Iterator<DecompilerAdapter> iterator = this.iterator(); iterator.hasNext();) {
				DecompilerAdapter adapter = iterator.next();
				if (this.overwriteList.contains(adapter.getId())) {
					iterator.remove();
					DecompilerAdapterManager.logger.log(Level.FINE, "<" + adapter.getId() + "> is overwritten."); //$NON-NLS-1$ //$NON-NLS-2$
				}
			}
			this.overWrittenAdaptersRemoved = true;
		}
	}
}