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

package org.springframework.cloud.sleuth.instrument.web.client.feign;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;

import feign.Client;
import feign.Feign;
import feign.hystrix.HystrixFeign;

/**
 * Contains {@link Feign.Builder} implementation that delegates execution
 * {@link HystrixFeign} with tracing components
 * that close spans upon completion of request processing.
 *
 * @author Marcin Grzejszczak
 *
 * @since 1.0.4
 */
final class SleuthHystrixFeignBuilder {

	private SleuthHystrixFeignBuilder() {}

	static Feign.Builder builder(BeanFactory beanFactory) {
		return HystrixFeign.builder()
				.client(client(beanFactory));
	}

	private static Client client(BeanFactory beanFactory) {
		try {
			Client client = beanFactory.getBean(Client.class);
			return (Client) new TraceFeignObjectWrapper(beanFactory).wrap(client);
		} catch (BeansException e) {
			return new TraceFeignClient(beanFactory);
		}
	}
}
