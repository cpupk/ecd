/*******************************************************************************
 * Copyright (c) 2017 Chen Chao and other ECD project contributors.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/

package org.sf.feeling.decompiler.extension;

import org.eclipse.core.expressions.Expression;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IAdapterFactory;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.Platform;

/**
 * DecompilerAdapter
 */
public class DecompilerAdapter {

	/**
	 * Comment for <code>id</code>
	 */
	private String id;
	/**
	 * Priority for this DecompilerAdapter.
	 */
	private int priority = 2;
	/**
	 * A id array of DecompilerAdapters to overwite (hide).
	 */
	private String[] overwrite;
	/**
	 * Does this DecompilerAdapter look for workbench adapter chain.
	 */
	private boolean includeWorkbenchContribute;
	/**
	 * Adapterabe type.
	 */
	private Class adaptableType;
	/**
	 * Target adapter type.
	 */
	private Class adapterType;

	private IConfigurationElement adapterConfig;

	/**
	 * Comment for <code>adapterInstance</code>
	 */
	private Object adapterInstance;
	/**
	 * Comment for <code>factory</code>
	 */
	private IAdapterFactory factory;
	/**
	 * Comment for <code>isSingleton</code>
	 */
	private boolean isSingleton;
	/**
	 * Adatper object instance.
	 */
	private Object cachedAdapter;

	private Expression expression;

	// getters and setters
	public Class getAdaptableType() {
		return adaptableType;
	}

	public void setAdaptableType(Class adaptableType) {
		this.adaptableType = adaptableType;
	}

	public IAdapterFactory getFactory() {
		return factory;
	}

	public void setFactory(IAdapterFactory factory) {
		this.factory = factory;
	}

	public boolean isIncludeWorkbenchContribute() {
		return includeWorkbenchContribute;
	}

	public void setIncludeWorkbenchContribute(boolean includeWorkbenchContribute) {
		this.includeWorkbenchContribute = includeWorkbenchContribute;
	}

	public String[] getOverwrite() {
		return overwrite;
	}

	public void setOverwrite(String[] overwrite) {
		this.overwrite = overwrite;
	}

	public int getPriority() {
		return priority;
	}

	public void setPriority(int priority) {
		this.priority = priority;
	}

	public Class getAdapterType() {
		return adapterType;
	}

	public void setAdapterType(Class type) {
		this.adapterType = type;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public Object getAdapterInstance() {
		return adapterInstance;
	}

	public void setAdapterInstance(Object adapterInstance) {
		this.adapterInstance = adapterInstance;
	}

	public void setAdapterConfig(IConfigurationElement config) {
		this.adapterConfig = config;
	}

	public boolean isSingleton() {
		return isSingleton;
	}

	public void setSingleton(boolean isSingleton) {
		this.isSingleton = isSingleton;
	}

	public Expression getExpression() {
		return expression;
	}

	public void setExpression(Expression expression) {
		this.expression = expression;
	}

	// public methods
	// FIXME singleton, factory
	public Object getAdater(Object adaptableObject) {
		if (this.cachedAdapter != null && this.isSingleton) {
			return this.cachedAdapter;
		}

		if (this.adapterInstance != null) {
			if (!isSingleton && adapterConfig != null) {
				try {
					return adapterConfig.createExecutableExtension("class"); //$NON-NLS-1$
				} catch (CoreException e) {
					e.printStackTrace();
				}
			}

			return this.adapterInstance;
		}

		Object apt = null;

		if (this.factory != null) {
			apt = this.factory.getAdapter(adaptableObject, this.adapterType);
		}
		if (apt == null && this.includeWorkbenchContribute) {
			apt = Platform.getAdapterManager().getAdapter(adaptableObject, this.adapterType);
		}

		if (this.isSingleton) {
			// only when is singleton, we cache the instance
			this.cachedAdapter = apt;
		}

		return apt;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == this) {
			return true;
		}

		if (!(obj instanceof DecompilerAdapter)) {
			return false;
		}

		return this.getId().equals(((DecompilerAdapter) obj).getId());
	}

	@Override
	public int hashCode() {
		return this.getId().hashCode();
	}

	@Override
	public String toString() {
		return this.getId();
	}
}
