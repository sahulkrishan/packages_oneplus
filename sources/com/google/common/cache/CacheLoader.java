package com.google.common.cache;

import com.google.common.annotations.Beta;
import com.google.common.annotations.GwtCompatible;
import com.google.common.annotations.GwtIncompatible;
import com.google.common.base.Function;
import com.google.common.base.Preconditions;
import com.google.common.base.Supplier;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.ListenableFutureTask;
import java.io.Serializable;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.Executor;

@GwtCompatible(emulated = true)
public abstract class CacheLoader<K, V> {

    public static final class InvalidCacheLoadException extends RuntimeException {
        public InvalidCacheLoadException(String message) {
            super(message);
        }
    }

    static final class UnsupportedLoadingOperationException extends UnsupportedOperationException {
        UnsupportedLoadingOperationException() {
        }
    }

    private static final class FunctionToCacheLoader<K, V> extends CacheLoader<K, V> implements Serializable {
        private static final long serialVersionUID = 0;
        private final Function<K, V> computingFunction;

        public FunctionToCacheLoader(Function<K, V> computingFunction) {
            this.computingFunction = (Function) Preconditions.checkNotNull(computingFunction);
        }

        public V load(K key) {
            return this.computingFunction.apply(Preconditions.checkNotNull(key));
        }
    }

    private static final class SupplierToCacheLoader<V> extends CacheLoader<Object, V> implements Serializable {
        private static final long serialVersionUID = 0;
        private final Supplier<V> computingSupplier;

        public SupplierToCacheLoader(Supplier<V> computingSupplier) {
            this.computingSupplier = (Supplier) Preconditions.checkNotNull(computingSupplier);
        }

        public V load(Object key) {
            Preconditions.checkNotNull(key);
            return this.computingSupplier.get();
        }
    }

    public abstract V load(K k) throws Exception;

    protected CacheLoader() {
    }

    @GwtIncompatible("Futures")
    public ListenableFuture<V> reload(K key, V oldValue) throws Exception {
        Preconditions.checkNotNull(key);
        Preconditions.checkNotNull(oldValue);
        return Futures.immediateFuture(load(key));
    }

    public Map<K, V> loadAll(Iterable<? extends K> iterable) throws Exception {
        throw new UnsupportedLoadingOperationException();
    }

    @Beta
    public static <K, V> CacheLoader<K, V> from(Function<K, V> function) {
        return new FunctionToCacheLoader(function);
    }

    @Beta
    public static <V> CacheLoader<Object, V> from(Supplier<V> supplier) {
        return new SupplierToCacheLoader(supplier);
    }

    @GwtIncompatible("Executor + Futures")
    @Beta
    public static <K, V> CacheLoader<K, V> asyncReloading(CacheLoader<K, V> loader, final Executor executor) {
        Preconditions.checkNotNull(loader);
        Preconditions.checkNotNull(executor);
        return new CacheLoader<K, V>() {
            public V load(K key) throws Exception {
                return CacheLoader.this.load(key);
            }

            public ListenableFuture<V> reload(final K key, final V oldValue) throws Exception {
                ListenableFutureTask<V> task = ListenableFutureTask.create(new Callable<V>() {
                    public V call() throws Exception {
                        return CacheLoader.this.reload(key, oldValue).get();
                    }
                });
                executor.execute(task);
                return task;
            }

            public Map<K, V> loadAll(Iterable<? extends K> keys) throws Exception {
                return CacheLoader.this.loadAll(keys);
            }
        };
    }
}
