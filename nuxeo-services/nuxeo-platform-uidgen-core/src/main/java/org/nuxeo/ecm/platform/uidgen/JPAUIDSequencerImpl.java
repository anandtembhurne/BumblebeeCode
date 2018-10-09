/*
 * (C) Copyright 2006-2010 Nuxeo SA (http://nuxeo.com/) and others.
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
 *     bstefanescu
 */
package org.nuxeo.ecm.platform.uidgen;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import javax.persistence.EntityManager;
import javax.persistence.NoResultException;

import org.nuxeo.common.utils.ExceptionUtils;
import org.nuxeo.ecm.core.api.NuxeoException;
import org.nuxeo.ecm.core.persistence.PersistenceProvider;
import org.nuxeo.ecm.core.persistence.PersistenceProvider.RunCallback;
import org.nuxeo.ecm.core.uidgen.AbstractUIDSequencer;
import org.nuxeo.ecm.core.persistence.PersistenceProviderFactory;
import org.nuxeo.runtime.api.Framework;
import org.nuxeo.runtime.transaction.TransactionHelper;

/**
 * This implementation uses a static persistence provider to be able to instantiate this class without passing by
 * Framework.getService -> this is to avoid potential problems do to sequencer factories. Anyway sequencer factories
 * should be removed (I don't think they are really needed).
 *
 * @author <a href="mailto:bs@nuxeo.com">Bogdan Stefanescu</a>
 */
public class JPAUIDSequencerImpl extends AbstractUIDSequencer {

    private static volatile PersistenceProvider persistenceProvider;

    protected ThreadPoolExecutor tpe = new ThreadPoolExecutor(1, 2, 10, TimeUnit.SECONDS,
            new LinkedBlockingQueue<Runnable>(1000));

    public JPAUIDSequencerImpl() {
    }

    @Override
    public void init() {
        // NOP
    }

    /**
     * Must be called when the service is no longer needed
     */
    @Override
    public void dispose() {
        deactivatePersistenceProvider();
        tpe.shutdownNow();
    }

    protected PersistenceProvider getOrCreatePersistenceProvider() {
        if (persistenceProvider == null) {
            synchronized (JPAUIDSequencerImpl.class) {
                if (persistenceProvider == null) {
                    activatePersistenceProvider();
                }
            }
        }
        return persistenceProvider;
    }

    protected static void activatePersistenceProvider() {
        Thread thread = Thread.currentThread();
        ClassLoader last = thread.getContextClassLoader();
        try {
            thread.setContextClassLoader(PersistenceProvider.class.getClassLoader());
            PersistenceProviderFactory persistenceProviderFactory = Framework.getLocalService(PersistenceProviderFactory.class);
            persistenceProvider = persistenceProviderFactory.newProvider("NXUIDSequencer");
            persistenceProvider.openPersistenceUnit();
        } finally {
            thread.setContextClassLoader(last);
        }
    }

    private static void deactivatePersistenceProvider() {
        if (persistenceProvider != null) {
            synchronized (JPAUIDSequencerImpl.class) {
                if (persistenceProvider != null) {
                    persistenceProvider.closePersistenceUnit();
                    persistenceProvider = null;
                }
            }
        }
    }

    protected class SeqRunner implements Runnable {

        protected final String key;

        protected int result;

        protected boolean completed = false;

        public SeqRunner(final String key) {
            this.key = key;
        }

        @Override
        public void run() {
            TransactionHelper.startTransaction();
            try {
                result = doGetNext(key);
                completed = true;
            } finally {
                TransactionHelper.commitOrRollbackTransaction();
            }

        }

        public int getResult() {
            return result;
        }

        public boolean isCompleted() {
            return completed;
        }

    }

    @Override
    public int getNext(final String key) {

        SeqRunner runner = new SeqRunner(key);

        Future<?> future = tpe.submit(runner);

        try {
            future.get();
        } catch (InterruptedException | ExecutionException e) { // deals with interrupt below
            ExceptionUtils.checkInterrupt(e);
            throw new NuxeoException(e);
        }

        return runner.getResult();

    }

    @SuppressWarnings("boxing")
    protected int doGetNext(final String key) {
        return getOrCreatePersistenceProvider().run(true, new RunCallback<Integer>() {
            @Override
            public Integer runWith(EntityManager em) {
                return getNext(em, key);
            }
        });
    }

    protected int getNext(EntityManager em, String key) {
        UIDSequenceBean seq;
        try {
            seq = (UIDSequenceBean) em.createNamedQuery("UIDSequence.findByKey").setParameter("key", key).getSingleResult();
            // createQuery("FROM UIDSequenceBean seq WHERE seq.key = :key")
        } catch (NoResultException e) {
            seq = new UIDSequenceBean(key);
            em.persist(seq);
        }
        return seq.nextIndex();
    }

}
