
package org.sf.feeling.decompiler.jad.decompiler;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.resource.ImageDescriptor;
import org.sf.feeling.decompiler.editor.BaseDecompilerSourceMapper;
import org.sf.feeling.decompiler.editor.IDecompiler;
import org.sf.feeling.decompiler.editor.IDecompilerDescriptor;
import org.sf.feeling.decompiler.jad.JadDecompilerPlugin;
import org.sf.feeling.decompiler.jad.actions.DecompileWithJadAction;
import org.sf.feeling.decompiler.jad.i18n.Messages;

public class JadDecompilerDescriptor implements IDecompilerDescriptor
{

	private JadDecompiler decompiler = null;

	private BaseDecompilerSourceMapper sourceMapper = null;

	private Action decompileAction = null;

	@Override
	public String getDecompilerType( )
	{
		return JadDecompilerPlugin.decompilerType;
	}

	@Override
	public String getDecompilerPreferenceLabel( )
	{
		return Messages.getString( "JadDecompilerDescriptor.PreferenceLabel" ); //$NON-NLS-1$
	}

	@Override
	public IDecompiler getDecompiler( )
	{
		if ( decompiler == null )
			decompiler = new JadDecompiler( );
		return decompiler;
	}

	@Override
	public BaseDecompilerSourceMapper getDecompilerSourceMapper( )
	{
		if ( sourceMapper == null )
		{
			sourceMapper = new JadSourceMapper( );
		}
		return sourceMapper;
	}

	@Override
	public Action getDecompileAction( )
	{
		if ( decompileAction == null )
		{
			decompileAction = new DecompileWithJadAction( );
		}
		return decompileAction;
	}

	@Override
	public boolean isEnabled( )
	{
		return true;
	}

	@Override
	public boolean isDefault( )
	{
		return false;
	}

	@Override
	public ImageDescriptor getDecompilerIcon( )
	{
		return JadDecompilerPlugin.getImageDescriptor( "icons/jad_16.gif" ); //$NON-NLS-1$ ;
	}

}
