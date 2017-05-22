
package org.sf.feeling.decompiler.editor;

import org.eclipse.jdt.internal.core.ClassFile;
import org.eclipse.jdt.internal.core.ImportContainer;

public class ClassImportContainer extends ImportContainer
{

	protected ClassImportContainer( ClassFile parent )
	{
		super(null );
		this.parent = parent;
	}
}
