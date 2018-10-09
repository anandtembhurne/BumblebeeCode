/*
 * (C) Copyright 2006-2014 Nuxeo SA (http://nuxeo.com/) and others.
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
 */
package org.nuxeo.ecm.core.redis.retry;

import org.apache.commons.logging.LogFactory;

public class Retry {

    public interface Block<T> {
        T retry() throws FailException, ContinueException;
    }

    public interface Policy {
        boolean allow();

        void pause();
    }

    public static class ContinueException extends Exception {

        private static final long serialVersionUID = 1L;

        public ContinueException(Throwable cause) {
            super(cause);
        }

    }

    public static class FailException extends Exception {

        public FailException(String message) {
            super(message);
        }

        public FailException(Throwable cause) {
            super(cause);
        }

        private static final long serialVersionUID = 1L;

    }

    public <T> T retry(Block<T> block, Policy policy) throws FailException {
        FailException causes = new FailException(
                "Cannot execute block, retry policy failed, check supressed exception for more infos");
        while (policy.allow()) {
            try {
                return block.retry();
            } catch (ContinueException error) {
                causes.addSuppressed(error.getCause());
            } catch (FailException error) {
                causes.addSuppressed(error.getCause());
                throw causes;
            }
            policy.pause();
        }
        throw causes;
    }

}
