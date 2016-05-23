/*
 * Copyright 2000-2013 JetBrains s.r.o.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.vcsreader.lang;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.concurrent.*;
import java.util.concurrent.Semaphore;

import static org.vcsreader.lang.Pair.pair;

/**
 * Fork of com.intellij.util.concurrency.FutureResult
 */
public class FutureResult<T> implements Future<T> {
	private final Semaphore semaphore = new Semaphore(0);
	private volatile Pair<Object, Boolean> pair;

	public synchronized void set(@Nullable T result) {
		assertNotSet();

		pair = pair((Object) result, true);
		semaphore.release();
	}

	public synchronized void setException(@NotNull Throwable e) {
		assertNotSet();

		pair = pair((Object) e, false);
		semaphore.release();
	}

	public synchronized void reset() {
		try {
			// wait till readers get their results
			if (isDone()) semaphore.acquire();
		} catch (InterruptedException ignore) {
			return;
		}
		pair = null;
	}

	private void assertNotSet() {
		if (isDone()) throw new IllegalStateException("Result is already set");
	}

	@Override
	public boolean cancel(boolean mayInterruptIfRunning) {
		return false;
	}

	@Override
	public boolean isCancelled() {
		return false;
	}

	@Override
	public boolean isDone() {
		return pair != null;
	}

	@Override
	public T get() throws InterruptedException, ExecutionException {
		semaphore.acquire();
		try {
			return doGet();
		} finally {
			semaphore.release();
		}
	}

	@Override
	public T get(long timeout, @NotNull TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
		if (!semaphore.tryAcquire(timeout, unit)) throw new TimeoutException();
		try {
			return doGet();
		} finally {
			semaphore.release();
		}
	}

	@Nullable
	private T doGet() throws ExecutionException {
		Pair<Object, Boolean> pair = this.pair;
		if (pair == null) return null;

		if (!pair.second) throw new ExecutionException(((Throwable) pair.first).getMessage(), (Throwable) pair.first);
		//noinspection unchecked
		return (T) pair.first;
	}
}
