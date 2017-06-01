/*******************************************************************************
 * Copyright (c) 2017 Chen Chao(cnfree2000@hotmail.com).
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *  Chen Chao  - initial API and implementation
 *******************************************************************************/

package org.sf.feeling.decompiler.update.util;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class ExecutorUtil
{

	private final static ExecutorService poll = Executors.newFixedThreadPool( 1 );

	private final static ScheduledExecutorService scheduledPool = Executors.newScheduledThreadPool( 1 );

	public static <T> void submitTask( Callable<T> task )
	{
		poll.submit( task );
	}

	public static <T> void submitScheduledTask( Runnable task, long initialDelay, long period, TimeUnit unit )
	{
		scheduledPool.scheduleAtFixedRate( task, initialDelay, period, unit );
	}

}
