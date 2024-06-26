/*
 * Copyright (C) 2013 The Guava Authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */

package com.google.common.hash;

import static com.google.common.base.Preconditions.checkNotNull;

import com.google.common.annotations.Beta;
import com.google.errorprone.annotations.CanIgnoreReturnValue;
import org.checkerframework.checker.index.qual.LTLengthOf;
import org.checkerframework.checker.index.qual.NonNegative;
import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * An {@link InputStream} that maintains a hash of the data read from it.
 *
 * @author Qian Huang
 * @since 16.0
 */
@Beta
@ElementTypesAreNonnullByDefault
public final class HashingInputStream extends FilterInputStream {
  private final Hasher hasher;

  /**
   * Creates an input stream that hashes using the given {@link HashFunction} and delegates all data
   * read from it to the underlying {@link InputStream}.
   *
   * <p>The {@link InputStream} should not be read from before or after the hand-off.
   */
  public HashingInputStream(HashFunction hashFunction, InputStream in) {
    super(checkNotNull(in));
    this.hasher = checkNotNull(hashFunction.newHasher());
  }

  /**
   * Reads the next byte of data from the underlying input stream and updates the hasher with the
   * byte read.
   */
  @Override
  @CanIgnoreReturnValue
  @SuppressWarnings("index:override.return")
  public int read() throws IOException {
    int b = in.read();
    if (b != -1) {
      hasher.putByte((byte) b);
    }
    return b;
  }

  /**
   * Reads the specified bytes of data from the underlying input stream and updates the hasher with
   * the bytes read.
   */
  @Override
  @CanIgnoreReturnValue
  @SuppressWarnings({"index:override.return", // FilterInputStream#read should be annotated as
      // @NonNegative @LTLengthOf(value = "#1",offset = "#2 - 1") int read()
      "upperbound:argument", // (1): param `off` should be annotated as @LTLengthOf(value = "#1",offset = "numOfBytesRead - 1")
      // However, numOfBytesRead is declared inside method body.
      "lowerbound:assignment" // (2): Input#stream() should be annotated as `@NonNegative int read()`
  })
  public int read(byte[] bytes, @NonNegative int off, @NonNegative int len) throws IOException {
    @NonNegative int numOfBytesRead = in.read(bytes, off, len);//(2)
    if (numOfBytesRead != -1) {
      hasher.putBytes(bytes, off, numOfBytesRead);//(1)
    }
    return numOfBytesRead;
  }

  /**
   * mark() is not supported for HashingInputStream
   *
   * @return {@code false} always
   */
  @Override
  public boolean markSupported() {
    return false;
  }

  /** mark() is not supported for HashingInputStream */
  @Override
  public void mark(int readlimit) {}

  /**
   * reset() is not supported for HashingInputStream.
   *
   * @throws IOException this operation is not supported
   */
  @Override
  public void reset() throws IOException {
    throw new IOException("reset not supported");
  }

  /**
   * Returns the {@link HashCode} based on the data read from this stream. The result is unspecified
   * if this method is called more than once on the same instance.
   */
  public HashCode hash() {
    return hasher.hash();
  }
}
