package org.sf.feeling.decompiler.fernflower.decompiler;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.resource.ImageDescriptor;
import org.sf.feeling.decompiler.editor.BaseDecompilerSourceMapper;
import org.sf.feeling.decompiler.editor.IDecompiler;
import org.sf.feeling.decompiler.editor.IDecompilerDescriptor;
import org.sf.feeling.decompiler.fernflower.FernFlowerDecompilerPlugin;
import org.sf.feeling.decompiler.fernflower.actions.DecompileWithFernFlowerAction;
import org.sf.feeling.decompiler.fernflower.i18n.Messages;

public class FernFlowerDecompilerDescriptor implements IDecompilerDescriptor {
	private FernFlowerDecompiler decompiler = null;
	private FernFlowerSourceMapper sourceMapper = null;
	private Action decompileAction;

	@Override
	public String getDecompilerType() {
		return FernFlowerDecompilerPlugin.decompilerType;
	}

	@Override
	public String getDecompilerPreferenceLabel() {
		return Messages.getString("FernFlowerDecompilerDescriptor.PreferenceLabel"); //$NON-NLS-1$
	}

	@Override
	public IDecompiler getDecompiler() {
		if (decompiler == null) {
			decompiler = new FernFlowerDecompiler();
		}
		return decompiler;

	}

	@Override
	public BaseDecompilerSourceMapper getDecompilerSourceMapper() {
		if (sourceMapper == null) {
			sourceMapper = new FernFlowerSourceMapper();
		}
		return sourceMapper;
	}

	@Override
	public Action getDecompileAction() {
		if (decompileAction == null) {
			decompileAction = new DecompileWithFernFlowerAction();
		}
		return decompileAction;
	}

	@Override
	public boolean isEnabled() {
		return true;
	}

	@Override
	public int getDefaultPriority() {
		return 100;
	}

	@Override
	public ImageDescriptor getDecompilerIcon() {
		return FernFlowerDecompilerPlugin.getImageDescriptor("icons/fernflower_16.png"); //$NON-NLS-1$
	}

}
