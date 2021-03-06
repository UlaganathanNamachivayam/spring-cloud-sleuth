/*
 * Copyright 2013-2016 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.springframework.cloud.sleuth.instrument.async;

import org.springframework.cloud.sleuth.Span;
import org.springframework.cloud.sleuth.SpanNamer;
import org.springframework.cloud.sleuth.TraceKeys;
import org.springframework.cloud.sleuth.TraceRunnable;
import org.springframework.cloud.sleuth.Tracer;

/**
 * Runnable that continues a span if there is one and creates new that is a
 * local component span if there was no tracing present.
 *
 * @author Marcin Grzejszczak
 * @since 1.0.10
 */
class SpanContinuingTraceRunnable extends TraceRunnable {

	private final LocalComponentTraceRunnable traceRunnable;

	public SpanContinuingTraceRunnable(Tracer tracer, TraceKeys traceKeys,
			SpanNamer spanNamer, Runnable delegate) {
		super(tracer, spanNamer, delegate);
		this.traceRunnable = new LocalComponentTraceRunnable(tracer, traceKeys, spanNamer, delegate);
	}

	public SpanContinuingTraceRunnable(Tracer tracer, TraceKeys traceKeys,
			SpanNamer spanNamer, Runnable delegate, String name) {
		super(tracer, spanNamer, delegate, name);
		this.traceRunnable = new LocalComponentTraceRunnable(tracer, traceKeys, spanNamer, delegate, name);
	}

	@Override
	public void run() {
		Span span = startSpan();
		try {
			this.getDelegate().run();
		}
		finally {
			close(span);
		}
	}

	@Override
	protected Span startSpan() {
		Span span = this.getParent();
		if (span == null) {
			return this.traceRunnable.startSpan();
		}
		return continueSpan(span);
	}

	@Override protected void close(Span span) {
		if (this.getParent() == null) {
			super.close(span);
		} else {
			super.detachSpan(span);
		}
	}
}
