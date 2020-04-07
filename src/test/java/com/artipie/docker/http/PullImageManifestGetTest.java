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

import com.artipie.http.hm.RsHasStatus;
import com.artipie.http.rq.RequestLine;
import com.artipie.http.rs.RsStatus;
import io.reactivex.Flowable;
import java.util.Collections;
import org.hamcrest.MatcherAssert;
import org.junit.jupiter.api.Test;

/**
 * Tests for {@link DockerSlice}.
 * Pull image manifest endpoint.
 *
 * @since 0.2
 */
@SuppressWarnings("PMD.AvoidDuplicateLiterals")
class PullImageManifestGetTest {

    @Test
    void shouldReturnManifestByTag() {
        MatcherAssert.assertThat(
            new DockerSlice().response(
                new RequestLine("GET", "/v2/my-alpine/manifests/1", "HTTP/1.1").toString(),
                Collections.emptyList(),
                Flowable.empty()
            ),
            new RsHasStatus(RsStatus.OK)
        );
    }

    @Test
    void shouldReturnManifestByDigest() {
        MatcherAssert.assertThat(
            new DockerSlice().response(
                new RequestLine(
                    "GET",
                    String.format(
                        "/v2/my-alpine/manifests/%s",
                        "sha256:cb8a924afdf0229ef7515d9e5b3024e23b3eb03ddbba287f4a19c6ac90b8d221"
                    ),
                    "HTTP/1.1"
                ).toString(),
                Collections.emptyList(),
                Flowable.empty()
            ),
            new RsHasStatus(RsStatus.OK)
        );
    }
}
