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
package com.artipie.docker.misc;

import com.artipie.asto.Concatenation;
import com.artipie.asto.Content;
import com.artipie.asto.Remaining;
import hu.akarnokd.rxjava2.interop.SingleInterop;
import io.reactivex.Single;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.CompletionStage;
import org.reactivestreams.Publisher;

/**
 * Read bytes from content to memory.
 * Using this class keep in mind that it reads ByteBuffer from publisher into memory and is not
 * suitable for large content.
 * @since 0.3
 */
public final class ReadContentAs {

    /**
     * Content to read bytes from.
     */
    private final Content content;

    /**
     * Ctor.
     * @param content Content
     */
    public ReadContentAs(final Content content) {
        this.content = content;
    }

    /**
     * Ctor.
     * @param content Content
     */
    public ReadContentAs(final Publisher<ByteBuffer> content) {
        this(new Content.From(content));
    }

    /**
     * Reads bytes from content into memory.
     * @param restore Restore position
     * @return Byte array as CompletionStage
     */
    public CompletionStage<byte[]> bytes(final boolean restore) {
        return this.single(restore).to(SingleInterop.get());
    }

    /**
     * Reads bytes from content as {@link StandardCharsets#US_ASCII} string.
     * @param restore Restore position
     * @return String as CompletionStage
     */
    public CompletionStage<String> asciiString(final boolean restore) {
        return this.single(restore)
            .map(bytes -> new String(bytes, StandardCharsets.US_ASCII))
            .to(SingleInterop.get());
    }

    /**
     * Single bytes value response.
     * @param restore Restore position
     * @return Single of bytes
     */
    public Single<byte[]> single(final boolean restore) {
        return new Concatenation(this.content)
            .single()
            .map(buf -> new Remaining(buf, restore))
            .map(Remaining::bytes);
    }

}
