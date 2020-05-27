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

import com.artipie.asto.Storage;
import com.artipie.asto.memory.InMemoryStorage;
import com.artipie.docker.Digest;
import com.artipie.docker.RepoName;
import com.artipie.docker.asto.AstoDocker;
import com.artipie.docker.asto.AstoUpload;
import com.artipie.docker.asto.BlobKey;
import com.artipie.http.Response;
import com.artipie.http.hm.RsHasHeaders;
import com.artipie.http.hm.RsHasStatus;
import com.artipie.http.rq.RequestLine;
import com.artipie.http.rs.Header;
import com.artipie.http.rs.RsStatus;
import io.reactivex.Flowable;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.Collections;
import java.util.UUID;
import org.hamcrest.MatcherAssert;
import org.hamcrest.core.AllOf;
import org.hamcrest.core.IsEqual;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Tests for {@link DockerSlice}.
 * Upload PUT endpoint.
 *
 * @since 0.2
 * @checkstyle ClassDataAbstractionCouplingCheck (500 lines)
 */
class UploadEntityPutTest {

    /**
     * Storage used in tests.
     */
    private Storage storage;

    /**
     * Slice being tested.
     */
    private DockerSlice slice;

    @BeforeEach
    void setUp() {
        this.storage = new InMemoryStorage();
        this.slice = new DockerSlice(new AstoDocker(this.storage));
    }

    @Test
    void shouldFinishUpload() {
        final String name = "test";
        final String uuid = UUID.randomUUID().toString();
        new AstoUpload(this.storage, new RepoName.Valid(name), uuid)
            .append(Flowable.just(ByteBuffer.wrap("data".getBytes())))
            .toCompletableFuture().join();
        final String digest = String.format(
            "%s:%s",
            "sha256",
            "3a6eb0790f39ac87c94f3856b2dd2c5d110e6811602261a9a923d3bb23adc8b7"
        );
        final Response response = this.slice.response(
            UploadEntityPutTest.requestLine(name, uuid, digest),
            Collections.emptyList(),
            Flowable.empty()
        );
        MatcherAssert.assertThat(
            "Returns 201 status and corresponding headers",
            response,
            new AllOf<>(
                Arrays.asList(
                    new RsHasStatus(RsStatus.CREATED),
                    new RsHasHeaders(
                        new Header("Location", String.format("/v2/%s/blobs/%s", name, digest)),
                        new Header("Content-Length", "0"),
                        new Header("Docker-Content-Digest", digest)
                    )
                )
            )
        );
        MatcherAssert.assertThat(
            "Puts blob into storage",
            this.storage.exists(new BlobKey(new Digest.FromString(digest))).join(),
            new IsEqual<>(true)
        );
    }

    @Test
    void returnsBadRequestWhenDigestsDoNotMatch() {
        final String name = "repo";
        final String uuid = UUID.randomUUID().toString();
        final byte[] content = "something".getBytes();
        new AstoUpload(this.storage, new RepoName.Valid(name), uuid)
            .append(Flowable.just(ByteBuffer.wrap(content)))
            .toCompletableFuture().join();
        MatcherAssert.assertThat(
            "Returns 400 status",
            this.slice.response(
                UploadEntityPutTest.requestLine(name, uuid, "sha256:0000"),
                Collections.emptyList(),
                Flowable.empty()
            ),
            new RsHasStatus(RsStatus.BAD_REQUEST)
        );
        MatcherAssert.assertThat(
            "Does not put blob into storage",
            this.storage.exists(
                new BlobKey(new Digest.Sha256(content))
            ).join(),
            new IsEqual<>(false)
        );
    }

    /**
     * Returns request line.
     * @param name Repo name
     * @param uuid Upload uuid
     * @param digest Digest
     * @return RequestLine instance
     */
    private static String requestLine(final String name, final String uuid, final String digest) {
        return new RequestLine(
            "PUT",
            String.format("/v2/%s/blobs/uploads/%s?digest=%s", name, uuid, digest),
            "HTTP/1.1"
        ).toString();
    }

}
