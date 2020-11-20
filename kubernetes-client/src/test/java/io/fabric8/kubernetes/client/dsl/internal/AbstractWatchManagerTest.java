/**
 * Copyright (C) 2015 Red Hat, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.fabric8.kubernetes.client.dsl.internal;

import io.fabric8.kubernetes.api.model.ListOptions;
import io.fabric8.kubernetes.client.Watcher;
import io.fabric8.kubernetes.client.WatcherException;
import okhttp3.OkHttpClient;
import okhttp3.WebSocket;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

class AbstractWatchManagerTest {

  @Test
  @DisplayName("closeEvent, is idempotent, multiple calls only close watcher once")
  void closeEventIsIdempotent() {
    // Given
    final AtomicInteger closeCount = new AtomicInteger();
    final Watcher<Object> watcher = new WatcherAdapter<Object>() {

      @Override
      public void onClose(WatcherException cause) {
        closeCount.addAndGet(1);
      }
    };
    final WatchManager<Object> awm = new WatchManager<>(
      watcher, mock(ListOptions.class, RETURNS_DEEP_STUBS), 0, 0, 0,
      mock(OkHttpClient.class));
    // When
    for (int it = 0; it < 10; it++) {
      awm.closeEvent(null);
    }
    // Then
    assertThat(closeCount.get()).isEqualTo(1);
  }

  @Test
  @DisplayName("closeWebSocket, closes web socket with 1000 code (Normal Closure)")
  void closeWebSocket() {
    // Given
    final WebSocket webSocket = mock(WebSocket.class);
    // When
    AbstractWatchManager.closeWebSocket(webSocket);
    // Then
    verify(webSocket, times(1)).close(1000, null);
  }

  private static class WatcherAdapter<T> implements Watcher<T> {
    @Override
    public void eventReceived(Action action, T resource) {

    }
    @Override
    public void onClose(WatcherException cause) {

    }
  }

  private static final class WatchManager<T> extends AbstractWatchManager<T> {
    public WatchManager(Watcher<T> watcher, ListOptions listOptions, int reconnectLimit, int reconnectInterval, int maxIntervalExponent, OkHttpClient clonedClient) {
      super(watcher, listOptions, reconnectLimit, reconnectInterval, maxIntervalExponent, clonedClient);
    }
    @Override
    public void close() {

    }
  }
}
