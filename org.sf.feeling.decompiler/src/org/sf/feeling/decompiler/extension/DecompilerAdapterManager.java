/*******************************************************************************
 * Copyright (c) 2017 Chen Chao and other ECD project contributors.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/

package org.sf.feeling.decompiler.extension;

import java.lang.reflect.Proxy;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.eclipse.core.expressions.EvaluationContext;
import org.eclipse.core.expressions.EvaluationResult;
import org.eclipse.core.expressions.ExpressionConverter;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IAdapterFactory;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.core.runtime.IExtensionRegistry;
import org.eclipse.core.runtime.Platform;

/**
 * DecompilerAdapterManager
 */
public class DecompilerAdapterManager {

	public static final String ADAPTERS_EXTENSION_ID = "org.sf.feeling.decompiler.decompilerAdapters"; //$NON-NLS-1$

	protected static final Logger logger = Logger.getLogger(DecompilerAdapterManager.class.getName());

	private static final Map<Class<?>, Set<?>> ADAPTERS_MAP = new HashMap() {

		private static final long serialVersionUID = 534728316184090251L;

		@Override
		public Object get(Object key) {
			Object obj = super.get(key);
			if (obj == null) {
				obj = new ElementAdapterSet();
				// need sync?
				// obj = Collections.synchronizedSortedSet( new
				// ElementAdapterSet( ) );
				put(key, obj);
			}
			return obj;
		}

	};

	static {
		// initial adaptersMap
		IExtensionRegistry registry = Platform.getExtensionRegistry();
		IExtensionPoint extensionPoint = registry.getExtensionPoint(ADAPTERS_EXTENSION_ID);
		if (extensionPoint != null) {
			IConfigurationElement[] elementArr = extensionPoint.getConfigurationElements();
			for (IConfigurationElement element : elementArr) {
				String adaptableClassName = element.getAttribute("class"); //$NON-NLS-1$
				Class adaptableType = null;

				IConfigurationElement[] adaptersConfigArr = element.getChildren("adapter"); //$NON-NLS-1$
				for (IConfigurationElement adapterConfig : adaptersConfigArr) {
					String adapterClassName = null;
					Class adapterType = null;

					try {
						DecompilerAdapter adapter = new DecompilerAdapter();
						adapter.setId(adapterConfig.getAttribute("id")); //$NON-NLS-1$

						adapter.setSingleton(!"false".equals( //$NON-NLS-1$
								adapterConfig.getAttribute("singleton"))); //$NON-NLS-1$

						if (adapterConfig.getAttribute("class") != null //$NON-NLS-1$
								&& !adapterConfig.getAttribute("class") //$NON-NLS-1$
										.equals("")) //$NON-NLS-1$
						{
							adapter.setAdapterInstance(adapterConfig.createExecutableExtension("class")); //$NON-NLS-1$

							if (!adapter.isSingleton()) {
								// cache the config element to create new instance
								adapter.setAdapterConfig(adapterConfig);
							}
						} else if (adapterConfig.getAttribute("factory") != null //$NON-NLS-1$
								&& !adapterConfig.getAttribute("factory") //$NON-NLS-1$
										.equals("")) //$NON-NLS-1$
						{
							adapter.setFactory((IAdapterFactory) adapterConfig.createExecutableExtension("factory")); //$NON-NLS-1$
						}

						if (adaptableType == null) {
							adaptableType = classForName(adaptableClassName, adapter.getAdapterInstance(),
									adapter.getFactory());
						}

						adapter.setAdaptableType(adaptableType);

						adapterClassName = adapterConfig.getAttribute("type"); //$NON-NLS-1$

						adapterType = classForName(adapterClassName, adapter.getAdapterInstance(),
								adapter.getFactory());

						adapter.setAdapterType(adapterType);

						if (adapterConfig.getAttribute("priority") != null //$NON-NLS-1$
								&& !adapterConfig.getAttribute("priority") //$NON-NLS-1$
										.equals("")) //$NON-NLS-1$
						{
							try {
								adapter.setPriority(Integer.parseInt(adapterConfig.getAttribute("priority"))); //$NON-NLS-1$
							} catch (NumberFormatException e) {
							}
						}

						if (adapterConfig.getAttribute("overwrite") != null //$NON-NLS-1$
								&& !adapterConfig.getAttribute("overwrite") //$NON-NLS-1$
										.equals("")) //$NON-NLS-1$
						{
							adapter.setOverwrite(adapterConfig.getAttribute("overwrite") //$NON-NLS-1$
									.split(";")); //$NON-NLS-1$
						}
						adapter.setIncludeWorkbenchContribute("true".equals(adapterConfig.getAttribute( //$NON-NLS-1$
								"includeWorkbenchContribute"))); //$NON-NLS-1$

						IConfigurationElement[] enablements = adapterConfig.getChildren("enablement"); //$NON-NLS-1$
						if (enablements != null && enablements.length > 0) {
							adapter.setExpression(ExpressionConverter.getDefault().perform(enablements[0]));
						}
						registerAdapter(adaptableType, adapter);
					} catch (ClassNotFoundException ce) {
						if (adaptableType == null) {
							System.out.println(MessageFormat.format("Adaptable Type class '{0}' not found!", //$NON-NLS-1$
									new Object[] { adaptableClassName }));
							logger.log(Level.SEVERE, ce.getMessage(), ce);
						} else {
							System.out.println(MessageFormat.format("Adapter Type class '{0}' not found!", //$NON-NLS-1$
									new Object[] { adapterClassName }));
							logger.log(Level.SEVERE, ce.getMessage(), ce);
						}
					} catch (Exception e) {
						System.out.println("Register adapter error!"); //$NON-NLS-1$
						logger.log(Level.SEVERE, e.getMessage(), e);
					}
				}
			}
		}
	}

	private static Class<?> classForName(String className, Object adapterInstance, IAdapterFactory adapterFacotry)
			throws ClassNotFoundException {
		Class<?> clazz = null;

		if (adapterInstance != null) {
			try {
				clazz = adapterInstance.getClass().getClassLoader().loadClass(className);
			} catch (ClassNotFoundException ex) {
				// fail over
			}
		}

		if (clazz == null && adapterFacotry != null) {
			try {
				clazz = adapterFacotry.getClass().getClassLoader().loadClass(className);
			} catch (ClassNotFoundException ex) {
				// it is possible that the default bundle classloader is unaware
				// of this class, but the adaptor factory can load it in some
				// other way. See bug 200068.
				Class<?>[] adapterClassArr = adapterFacotry.getAdapterList();
				if (adapterClassArr != null) {
					for (Class<?> adapterClass : adapterClassArr) {
						if (className.equals(adapterClass.getName())) {
							clazz = adapterClass;
							break;
						}
					}
				}
			}
		}

		if (clazz == null) {
			clazz = Class.forName(className);
		}

		return clazz;
	}

	public static void registerAdapter(Class adaptableType, DecompilerAdapter adapter) {
		synchronized (ADAPTERS_MAP) {
			Set adapterSet = ADAPTERS_MAP.get(adaptableType);
			adapterSet.add(adapter);
			// if ( adapterSet.add( adapter ) )
			// System.out.println( "Register adapter for "
			// + adaptableType.getName( )
			// + " "
			// + adapter.getId( ) );
			// else
			// System.out.println( "fail Register adapter for "
			// + adaptableType.getName( )
			// + " "
			// + adapter.getId( ) );
		}
	}

	public static Object getAdapter(Object adaptableObject, Class adapterType) {
		List<?> adapterObjects = getAdapterList(adaptableObject, adapterType);
		if (adapterObjects == null || adapterObjects.size() == 0) {
			return null;
		}
		if (adapterObjects.size() == 1) {
			return adapterObjects.get(0);
		}
		return Proxy.newProxyInstance(adapterType.getClassLoader(), new Class[] { adapterType },
				new DecompilerAdapterInvocationHandler(adapterObjects));
	}

	public static <T> List<T> getAdapterList(Object adaptableObject, Class<T> adapterType) {
		Set<DecompilerAdapter> adapters = getAdaptersInternal(adaptableObject);
		if (adapters == null) {
			return null;
		}

		List<T> adapterObjects = new ArrayList<>(adapters.size());
		for (DecompilerAdapter adapter : adapters) {
			if (adapter.getExpression() != null) {
				EvaluationContext context = new EvaluationContext(null, adaptableObject);
				context.setAllowPluginActivation(true);
				try {
					if (adapter.getExpression().evaluate(context) != EvaluationResult.TRUE) {
						continue;
					}
				} catch (CoreException e) {
				}
			}
			Object obj = adapter.getAdater(adaptableObject);
			if (obj != null && adapterType.isAssignableFrom(obj.getClass())) {
				adapterObjects.add((T) obj);
			}
		}

		return adapterObjects;
	}

	private static Set<DecompilerAdapter> getAdaptersInternal(Object adaptableObject) {
		Set<Class<?>> keys = ADAPTERS_MAP.keySet();
		ElementAdapterSet adapters = null;
		for (Class<?> clazz : keys) {
			// adaptable is the instance of the key class or its subclass.
			if (clazz.isAssignableFrom(adaptableObject.getClass())) {
				if (adapters == null) {
					adapters = new ElementAdapterSet();
				}
				Set<?> set = ADAPTERS_MAP.get(clazz);
				for (Object obj : set) {
					adapters.add((DecompilerAdapter) obj);
				}
			}
		}
		if (adapters != null) {
			adapters.removeOverwrittenAdapters();
		}
		return adapters;
	}

}
