/*
 * MIT License
 *
 * Copyright (c) 2020 Artipie
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package com.artipie.docker.http;

import com.artipie.asto.memory.InMemoryStorage;
import com.artipie.docker.asto.AstoDocker;
import com.artipie.http.Response;
import com.artipie.http.auth.Permissions;
import com.artipie.http.headers.Header;
import com.artipie.http.hm.ResponseMatcher;
import com.artipie.http.rq.RequestLine;
import com.artipie.http.rq.RqMethod;
import io.reactivex.Flowable;
import java.util.Collections;
import org.hamcrest.MatcherAssert;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Tests for {@link DockerSlice}.
 * Base GET endpoint.
 *
 * @since 0.1
 * @checkstyle ClassDataAbstractionCouplingCheck (2 lines)
 */
@SuppressWarnings("PMD.AvoidDuplicateLiterals")
class BaseEntityGetTest {

    /**
     * Slice being tested.
     */
    private DockerSlice slice;

    /**
     * User with right permissions.
     */
    private TestAuthentication.User user;

    @BeforeEach
    void setUp() {
        this.user = TestAuthentication.ALICE;
        this.slice = new DockerSlice(
            new AstoDocker(new InMemoryStorage()),
            new Permissions.Single(this.user.name(), DockerSlice.READ),
            new TestAuthentication()
        );
    }

    @Test
    void shouldRespondOkToVersionCheck() {
        final Response response = this.slice.response(
            new RequestLine(RqMethod.GET, "/v2/").toString(),
            this.user.headers(),
            Flowable.empty()
        );
        MatcherAssert.assertThat(
            response,
            new ResponseMatcher(
                new Header("Docker-Distribution-API-Version", "registry/2.0")
            )
        );
    }

    @Test
    void shouldReturnUnauthorizedWhenNoAuth() {
        MatcherAssert.assertThat(
            this.slice.response(
                new RequestLine(RqMethod.GET, "/v2/").toString(),
                Collections.emptyList(),
                Flowable.empty()
            ),
            new IsUnauthorizedResponse()
        );
    }
}
