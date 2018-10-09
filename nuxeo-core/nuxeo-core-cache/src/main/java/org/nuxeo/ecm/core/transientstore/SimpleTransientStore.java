/*
 * (C) Copyright 2015 Nuxeo SA (http://nuxeo.com/) and others.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * Contributors:
 *     Thierry Delprat <tdelprat@nuxeo.com>
 *     Antoine Taillefer <ataillefer@nuxeo.com>
 */

package org.nuxeo.ecm.core.transientstore;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicLong;

import org.apache.commons.io.FileUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.nuxeo.ecm.core.api.Blob;
import org.nuxeo.ecm.core.cache.Cache;
import org.nuxeo.ecm.core.cache.CacheDescriptor;
import org.nuxeo.ecm.core.cache.CacheService;
import org.nuxeo.ecm.core.cache.CacheServiceImpl;
import org.nuxeo.ecm.core.cache.InMemoryCacheImpl;
import org.nuxeo.ecm.core.transientstore.api.TransientStore;
import org.nuxeo.ecm.core.transientstore.api.TransientStoreConfig;
import org.nuxeo.runtime.api.Framework;

/**
 * Default implementation (i.e., not cluster aware) of the {@link TransientStore}. Uses {@link StorageEntry} as a
 * representation of an entry in the store.
 *
 * @since 7.2
 */
public class SimpleTransientStore extends AbstractTransientStore {

    protected Log log = LogFactory.getLog(SimpleTransientStore.class);

    protected Cache l1Cache;

    protected Cache l2Cache;

    protected CacheDescriptor l1cd;

    protected CacheDescriptor l2cd;

    protected AtomicLong storageSize = new AtomicLong(0);

    public SimpleTransientStore() {
    }

    @Override
    public void init(TransientStoreConfig config) {
        log.debug("Initializing SimpleTransientStore: " + config.getName());
        super.init(config);
        CacheService cs = Framework.getService(CacheService.class);
        if (cs == null) {
            throw new UnsupportedOperationException("Cache service is required");
        }
        // register the caches
        l1cd = getL1CacheConfig();
        l2cd = getL2CacheConfig();
        ((CacheServiceImpl) cs).registerCache(l1cd);
        ((CacheServiceImpl) cs).registerCache(l2cd);

        // get caches
        l1Cache = cs.getCache(l1cd.name);
        l2Cache = cs.getCache(l2cd.name);
    }

    @Override
    public void shutdown() {
        log.debug("Shutting down SimpleTransientStore: " + config.getName());
        CacheService cs = Framework.getService(CacheService.class);
        if (cs != null) {
            if (l1cd != null) {
                ((CacheServiceImpl) cs).unregisterCache(l1cd);
            }
            if (l2cd != null) {
                ((CacheServiceImpl) cs).unregisterCache(l2cd);
            }
        }
    }

    @Override
    public boolean exists(String key) {
        return getL1Cache().hasEntry(key) || getL2Cache().hasEntry(key);
    }

    @Override
    public Set<String> keySet() {
        Set<String> keys = new HashSet<>();
        keys.addAll(getL1Cache().keySet());
        keys.addAll(getL2Cache().keySet());
        return keys;
    }

    @Override
    public void putParameter(String key, String parameter, Serializable value) {
        synchronized (this) {
            StorageEntry entry = getStorageEntry(key);
            if (entry == null) {
                entry = new StorageEntry();
            }
            entry.putParam(parameter, value);
            if (log.isDebugEnabled()) {
                log.debug(String.format("Setting parameter %s to value %s in StorageEntry stored at key %s", parameter,
                        value, key));
            }
            putStorageEntry(key, entry);
        }
    }

    @Override
    public Serializable getParameter(String key, String parameter) {
        StorageEntry entry = getStorageEntry(key);
        if (entry == null) {
            return null;
        }
        Serializable res = entry.getParam(parameter);
        if (log.isDebugEnabled()) {
            log.debug(String.format("Fetched parameter %s from StorageEntry stored at key %s: %s", parameter, key, res));
        }
        return res;
    }

    @Override
    public void putParameters(String key, Map<String, Serializable> parameters) {
        synchronized (this) {
            StorageEntry entry = getStorageEntry(key);
            if (entry == null) {
                entry = new StorageEntry();
            }
            entry.putParams(parameters);
            if (log.isDebugEnabled()) {
                log.debug(String.format("Setting parameters %s in StorageEntry stored at key %s", parameters, key));
            }
            putStorageEntry(key, entry);
        }
    }

    @Override
    public Map<String, Serializable> getParameters(String key) {
        StorageEntry entry = getStorageEntry(key);
        if (entry == null) {
            return null;
        }
        Map<String, Serializable> res = entry.getParams();
        if (log.isDebugEnabled()) {
            log.debug(String.format("Fetched parameters from StorageEntry stored at key %s: %s", key, res));
        }
        return res;
    }

    @Override
    public List<Blob> getBlobs(String key) {
        StorageEntry entry = getStorageEntry(key);
        if (entry == null) {
            return null;
        }
        // Get blob information from the store
        List<Map<String, String>> blobInfos = entry.getBlobInfos();
        if (blobInfos == null) {
            return new ArrayList<>();
        }
        // Load blobs from the file system
        return loadBlobs(blobInfos);
    }

    @Override
    public long getSize(String key) {
        StorageEntry entry = getStorageEntry(key);
        if (entry == null) {
            return -1;
        }
        long size = entry.getSize();
        if (log.isDebugEnabled()) {
            log.debug(String.format("Fetched field \"size\" from StorageEntry stored at key %s: %d", key, size));
        }
        return size;
    }

    @Override
    public boolean isCompleted(String key) {
        StorageEntry entry = getStorageEntry(key);
        boolean completed = entry != null && entry.isCompleted();
        if (log.isDebugEnabled()) {
            log.debug(String.format("Fetched field \"completed\" from StorageEntry stored at key %s: %s", key,
                    completed));
        }
        return completed;
    }

    @Override
    public void setCompleted(String key, boolean completed) {
        synchronized (this) {
            StorageEntry entry = getStorageEntry(key);
            if (entry == null) {
                entry = new StorageEntry();
            }
            entry.setCompleted(completed);
            if (log.isDebugEnabled()) {
                log.debug(String.format("Setting field \"completed\" to value %s in StorageEntry stored at key %s",
                        completed, key));
            }
            putStorageEntry(key, entry);
        }
    }

    @Override
    public void remove(String key) {
        synchronized (this) {
            StorageEntry entry = (StorageEntry) getL1Cache().get(key);
            if (entry == null) {
                entry = (StorageEntry) getL2Cache().get(key);
                if (log.isDebugEnabled()) {
                    log.debug(String.format("Invalidating StorageEntry stored at key %s form L2 cache", key));
                }
                getL2Cache().invalidate(key);
            } else {
                if (log.isDebugEnabled()) {
                    log.debug(String.format("Invalidating StorageEntry stored at key %s form L1 cache", key));
                }
                getL1Cache().invalidate(key);
            }
            if (entry != null) {
                long entrySize = entry.getSize();
                if (entrySize > 0) {
                    decrementStorageSize(entrySize);
                }
            }
            FileUtils.deleteQuietly(getCachingDirectory(key));
        }
    }

    @Override
    public void release(String key) {
        StorageEntry entry = (StorageEntry) getL1Cache().get(key);
        if (entry != null) {
            if (log.isDebugEnabled()) {
                log.debug(String.format("Invalidating StorageEntry stored at key %s form L1 cache", key));
            }
            getL1Cache().invalidate(key);
            if (getStorageSize() <= config.getTargetMaxSizeMB() * (1024 * 1024) || config.getTargetMaxSizeMB() < 0) {
                if (log.isDebugEnabled()) {
                    log.debug(String.format("Putting StorageEntry at key %s in L2 cache", key));
                }
                getL2Cache().put(key, entry);
            }
        }
    }

    @Override
    protected void persistBlobs(String key, long sizeOfBlobs, List<Map<String, String>> blobInfos) {
        synchronized (this) {
            StorageEntry entry = getStorageEntry(key);
            // Update storage size
            if (entry == null) {
                if (sizeOfBlobs > 0) {
                    incrementStorageSize(sizeOfBlobs);
                }
                entry = new StorageEntry();
            } else {
                incrementStorageSize(sizeOfBlobs - entry.getSize());
            }
            // Update entry size
            entry.setSize(sizeOfBlobs);
            // Set blob information
            entry.setBlobInfos(blobInfos);
            if (log.isDebugEnabled()) {
                log.debug(String.format("Setting blobs %s in StorageEntry stored at key %s", blobInfos, key));
            }
            putStorageEntry(key, entry);
        }
    }

    @Override
    public long getStorageSize() {
        int intStorageSize = (int) storageSize.get();
        if (log.isDebugEnabled()) {
            log.debug(String.format("Fetched storage size of store %s: %d", config.getName(), intStorageSize));
        }
        return intStorageSize;
    }

    @Override
    protected void setStorageSize(long newSize) {
        if (log.isDebugEnabled()) {
            log.debug(String.format("Setting storage size of store %s to %d", config.getName(), newSize));
        }
        storageSize.set(newSize);
    }

    @Override
    protected long incrementStorageSize(long size) {
        long incremented = storageSize.addAndGet(size);
        if (log.isDebugEnabled()) {
            log.debug(String.format("Incremented storage size of store %s to %s", config.getName(), incremented));
        }
        return incremented;
    }

    @Override
    protected long decrementStorageSize(long size) {
        long decremented = storageSize.addAndGet(-size);
        if (log.isDebugEnabled()) {
            log.debug(String.format("Decremented storage size of store %s to %s", config.getName(), decremented));
        }
        return decremented;
    }

    @Override
    protected void removeAllEntries() {
        log.debug("Invalidating all entries from L1 and L2 caches");
        getL1Cache().invalidateAll();
        getL2Cache().invalidateAll();
    }

    public Cache getL1Cache() {
        return l1Cache;
    }

    public Cache getL2Cache() {
        return l2Cache;
    }

    protected CacheDescriptor getL1CacheConfig() {
        return new TransientCacheConfig(config.getName() + "L1", config.getFirstLevelTTL());
    }

    protected CacheDescriptor getL2CacheConfig() {
        return new TransientCacheConfig(config.getName() + "L2", config.getSecondLevelTTL());
    }

    protected class TransientCacheConfig extends CacheDescriptor {

        TransientCacheConfig(String name, int ttl) {
            super();
            super.name = name;
            super.implClass = getCacheImplClass();
            super.ttl = ttl;
        }
    }

    protected Class<? extends Cache> getCacheImplClass() {
        return InMemoryCacheImpl.class;
    }

    /**
     * Returns the {@link StorageEntry} representing the entry with the given {@code key} or {@code null} if it doesn't
     * exist.
     */
    protected StorageEntry getStorageEntry(String key) {
        StorageEntry entry = (StorageEntry) getL1Cache().get(key);
        if (entry == null) {
            entry = (StorageEntry) getL2Cache().get(key);
        }
        return entry;
    }

    /**
     * Stores the given {@code entry} with the given {@code key}.
     * <p>
     * If an entry exists with the given {@code key} it is overwritten.
     */
    protected void putStorageEntry(String key, StorageEntry entry) {
        getL1Cache().put(key, entry);
    }

}
