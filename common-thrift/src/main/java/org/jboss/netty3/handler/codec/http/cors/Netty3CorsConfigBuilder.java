/*
 * Licensed to Elasticsearch under one or more contributor
 * license agreements. See the NOTICE file distributed with
 * this work for additional information regarding copyright
 * ownership. Elasticsearch licenses this file to you under
 * the Apache License, Version 2.0 (the "License"); you may
 * not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.jboss.netty3.handler.codec.http.cors;

import org.jboss.netty.handler.codec.http.HttpMethod;

import com.google.common.base.Optional;

import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.regex.Pattern;

/**
 * Builder used to configure and build a {@link Netty3CorsConfig} instance.
 *
 * This class was lifted from the Netty project:
 *  https://github.com/netty/netty
 */
public final class Netty3CorsConfigBuilder {

    /**
     * Creates a Builder instance with it's origin set to '*'.
     *
     * @return Builder to support method chaining.
     */
    public static Netty3CorsConfigBuilder forAnyOrigin() {
        return new Netty3CorsConfigBuilder();
    }

    /**
     * Creates a {@link Netty3CorsConfigBuilder} instance with the specified origin.
     *
     * @return {@link Netty3CorsConfigBuilder} to support method chaining.
     */
    public static Netty3CorsConfigBuilder forOrigin(final String origin) {
        if ("*".equals(origin)) {
            return new Netty3CorsConfigBuilder();
        }
        return new Netty3CorsConfigBuilder(origin);
    }


    /**
     * Create a {@link Netty3CorsConfigBuilder} instance with the specified pattern origin.
     *
     * @param pattern the regular expression pattern to match incoming origins on.
     * @return {@link Netty3CorsConfigBuilder} with the configured origin pattern.
     */
    public static Netty3CorsConfigBuilder forPattern(final Pattern pattern) {
        if (pattern == null) {
            throw new IllegalArgumentException("CORS pattern cannot be null");
        }
        return new Netty3CorsConfigBuilder(pattern);
    }

    /**
     * Creates a {@link Netty3CorsConfigBuilder} instance with the specified origins.
     *
     * @return {@link Netty3CorsConfigBuilder} to support method chaining.
     */
    public static Netty3CorsConfigBuilder forOrigins(final String... origins) {
        return new Netty3CorsConfigBuilder(origins);
    }

    Optional<Set<String>> origins;
    Optional<Pattern> pattern;
    final boolean anyOrigin;
    boolean allowNullOrigin;
    boolean enabled = true;
    boolean allowCredentials;
    long maxAge;
    final Set<HttpMethod> requestMethods = new HashSet<>();
    final Set<String> requestHeaders = new HashSet<>();
    final Map<CharSequence, Callable<?>> preflightHeaders = new HashMap<>();
    private boolean noPreflightHeaders;
    boolean shortCircuit;

    /**
     * Creates a new Builder instance with the origin passed in.
     *
     * @param origins the origin to be used for this builder.
     */
    Netty3CorsConfigBuilder(final String... origins) {
        this.origins = Optional.<Set<String>>of(new LinkedHashSet<>(Arrays.asList(origins)));
        pattern = Optional.absent();
        anyOrigin = false;
    }

    /**
     * Creates a new Builder instance allowing any origin, "*" which is the
     * wildcard origin.
     *
     */
    Netty3CorsConfigBuilder() {
        anyOrigin = true;
        origins = Optional.absent();
        pattern = Optional.absent();
    }

    /**
     * Creates a new Builder instance allowing any origin that matches the pattern.
     *
     * @param pattern the pattern to match against for incoming origins.
     */
    Netty3CorsConfigBuilder(final Pattern pattern) {
        this.pattern = Optional.of(pattern);
        origins = Optional.absent();
        anyOrigin = false;
    }

    /**
     * Web browsers may set the 'Origin' request header to 'null' if a resource is loaded
     * from the local file system. Calling this method will enable a successful CORS response
     * with a wildcard for the CORS response header 'Access-Control-Allow-Origin'.
     *
     * @return {@link Netty3CorsConfigBuilder} to support method chaining.
     */
    Netty3CorsConfigBuilder allowNullOrigin() {
        allowNullOrigin = true;
        return this;
    }

    /**
     * Disables CORS support.
     *
     * @return {@link Netty3CorsConfigBuilder} to support method chaining.
     */
    public Netty3CorsConfigBuilder disable() {
        enabled = false;
        return this;
    }

    /**
     * By default cookies are not included in CORS requests, but this method will enable cookies to
     * be added to CORS requests. Calling this method will set the CORS 'Access-Control-Allow-Credentials'
     * response header to true.
     *
     * Please note, that cookie support needs to be enabled on the client side as well.
     * The client needs to opt-in to send cookies by calling:
     * <pre>
     * xhr.withCredentials = true;
     * </pre>
     * The default value for 'withCredentials' is false in which case no cookies are sent.
     * Setting this to true will included cookies in cross origin requests.
     *
     * @return {@link Netty3CorsConfigBuilder} to support method chaining.
     */
    public Netty3CorsConfigBuilder allowCredentials() {
        allowCredentials = true;
        return this;
    }

    /**
     * When making a preflight request the client has to perform two request with can be inefficient.
     * This setting will set the CORS 'Access-Control-Max-Age' response header and enables the
     * caching of the preflight response for the specified time. During this time no preflight
     * request will be made.
     *
     * @param max the maximum time, in seconds, that the preflight response may be cached.
     * @return {@link Netty3CorsConfigBuilder} to support method chaining.
     */
    public Netty3CorsConfigBuilder maxAge(final long max) {
        maxAge = max;
        return this;
    }

    /**
     * Specifies the allowed set of HTTP Request Methods that should be returned in the
     * CORS 'Access-Control-Request-Method' response header.
     *
     * @param methods the {@link HttpMethod}s that should be allowed.
     * @return {@link Netty3CorsConfigBuilder} to support method chaining.
     */
    public Netty3CorsConfigBuilder allowedRequestMethods(final Set<HttpMethod> methods) {
        requestMethods.addAll(methods);
        return this;
    }
    /**
     * Specifies the allowed set of HTTP Request Methods that should be returned in the
     * CORS 'Access-Control-Request-Method' response header.
     *
     * @param methods the {@link HttpMethod}s that should be allowed.
     * @return {@link Netty3CorsConfigBuilder} to support method chaining.
     */
    public Netty3CorsConfigBuilder allowedRequestMethods(final HttpMethod... methods) {
        requestMethods.addAll(Arrays.asList(methods));
        return this;
    }
    /**
     * Specifies the if headers that should be returned in the CORS 'Access-Control-Allow-Headers'
     * response header.
     *
     * If a client specifies headers on the request, for example by calling:
     * <pre>
     * xhr.setRequestHeader('My-Custom-Header', "SomeValue");
     * </pre>
     * the server will receive the above header name in the 'Access-Control-Request-Headers' of the
     * preflight request. The server will then decide if it allows this header to be sent for the
     * real request (remember that a preflight is not the real request but a request asking the server
     * if it allow a request).
     *
     * @param headers the headers to be added to the preflight 'Access-Control-Allow-Headers' response header.
     * @return {@link Netty3CorsConfigBuilder} to support method chaining.
     */
    public Netty3CorsConfigBuilder allowedRequestHeaders(final Set<String> headers) {
        requestHeaders.addAll(headers);
        return this;
    }
    /**
     * Specifies the if headers that should be returned in the CORS 'Access-Control-Allow-Headers'
     * response header.
     *
     * If a client specifies headers on the request, for example by calling:
     * <pre>
     * xhr.setRequestHeader('My-Custom-Header', "SomeValue");
     * </pre>
     * the server will receive the above header name in the 'Access-Control-Request-Headers' of the
     * preflight request. The server will then decide if it allows this header to be sent for the
     * real request (remember that a preflight is not the real request but a request asking the server
     * if it allow a request).
     *
     * @param headers the headers to be added to the preflight 'Access-Control-Allow-Headers' response header.
     * @return {@link Netty3CorsConfigBuilder} to support method chaining.
     */
    public Netty3CorsConfigBuilder allowedRequestHeaders(final String... headers) {
        requestHeaders.addAll(Arrays.asList(headers));
        return this;
    }
    /**
     * Returns HTTP response headers that should be added to a CORS preflight response.
     *
     * An intermediary like a load balancer might require that a CORS preflight request
     * have certain headers set. This enables such headers to be added.
     *
     * @param name the name of the HTTP header.
     * @param values the values for the HTTP header.
     * @return {@link Netty3CorsConfigBuilder} to support method chaining.
     */
    public Netty3CorsConfigBuilder preflightResponseHeader(final CharSequence name, final Object... values) {
        if (values.length == 1) {
            preflightHeaders.put(name, new ConstantValueGenerator(values[0]));
        } else {
            preflightResponseHeader(name, Arrays.asList(values));
        }
        return this;
    }

    /**
     * Returns HTTP response headers that should be added to a CORS preflight response.
     *
     * An intermediary like a load balancer might require that a CORS preflight request
     * have certain headers set. This enables such headers to be added.
     *
     * @param name the name of the HTTP header.
     * @param value the values for the HTTP header.
     * @param <T> the type of values that the Iterable contains.
     * @return {@link Netty3CorsConfigBuilder} to support method chaining.
     */
    public <T> Netty3CorsConfigBuilder preflightResponseHeader(final CharSequence name, final Iterable<T> value) {
        preflightHeaders.put(name, new ConstantValueGenerator(value));
        return this;
    }

    /**
     * Returns HTTP response headers that should be added to a CORS preflight response.
     *
     * An intermediary like a load balancer might require that a CORS preflight request
     * have certain headers set. This enables such headers to be added.
     *
     * Some values must be dynamically created when the HTTP response is created, for
     * example the 'Date' response header. This can be accomplished by using a Callable
     * which will have its 'call' method invoked when the HTTP response is created.
     *
     * @param name the name of the HTTP header.
     * @param valueGenerator a Callable which will be invoked at HTTP response creation.
     * @param <T> the type of the value that the Callable can return.
     * @return {@link Netty3CorsConfigBuilder} to support method chaining.
     */
    public <T> Netty3CorsConfigBuilder preflightResponseHeader(final CharSequence name, final Callable<T> valueGenerator) {
        preflightHeaders.put(name, valueGenerator);
        return this;
    }

    /**
     * Specifies that no preflight response headers should be added to a preflight response.
     *
     * @return {@link Netty3CorsConfigBuilder} to support method chaining.
     */
    public Netty3CorsConfigBuilder noPreflightResponseHeaders() {
        noPreflightHeaders = true;
        return this;
    }

    /**
     * Specifies that a CORS request should be rejected if it's invalid before being
     * further processing.
     *
     * CORS headers are set after a request is processed. This may not always be desired
     * and this setting will check that the Origin is valid and if it is not valid no
     * further processing will take place, and a error will be returned to the calling client.
     *
     * @return {@link Netty3CorsConfigBuilder} to support method chaining.
     */
    public Netty3CorsConfigBuilder shortCircuit() {
        shortCircuit = true;
        return this;
    }

    /**
     * Builds a {@link Netty3CorsConfig} with settings specified by previous method calls.
     *
     * @return {@link Netty3CorsConfig} the configured CorsConfig instance.
     */
    public Netty3CorsConfig build() {
        if (preflightHeaders.isEmpty() && !noPreflightHeaders) {
            preflightHeaders.put("date", DateValueGenerator.INSTANCE);
            preflightHeaders.put("content-length", new ConstantValueGenerator("0"));
        }
        return new Netty3CorsConfig(this);
    }

    /**
     * This class is used for preflight HTTP response values that do not need to be
     * generated, but instead the value is "static" in that the same value will be returned
     * for each call.
     */
    private static final class ConstantValueGenerator implements Callable<Object> {

        private final Object value;

        /**
         * Sole constructor.
         *
         * @param value the value that will be returned when the call method is invoked.
         */
        private ConstantValueGenerator(final Object value) {
            if (value == null) {
                throw new IllegalArgumentException("value must not be null");
            }
            this.value = value;
        }

        @Override
        public Object call() {
            return value;
        }
    }

    /**
     * This callable is used for the DATE preflight HTTP response HTTP header.
     * It's value must be generated when the response is generated, hence will be
     * different for every call.
     */
    private static final class DateValueGenerator implements Callable<Date> {

        static final DateValueGenerator INSTANCE = new DateValueGenerator();

        @Override
        public Date call() throws Exception {
            return new Date();
        }
    }
}