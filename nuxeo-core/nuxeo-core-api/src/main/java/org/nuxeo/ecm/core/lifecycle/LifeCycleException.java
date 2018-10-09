/*
 * (C) Copyright 2006-2015 Nuxeo SA (http://nuxeo.com/) and others.
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
 *     Julien Anguenot
 *     Florent Guillaume
 */
package org.nuxeo.ecm.core.lifecycle;

import org.nuxeo.ecm.core.api.NuxeoException;

/**
 * Deprecated and never thrown, kept for compatibility so that old code catching this still works.
 * <p>
 * Use {@link org.nuxeo.ecm.core.api.LifeCycleException} instead.
 *
 * @deprecated since 7.4, use org.nuxeo.ecm.core.api.LifeCycleException instead
 */
@Deprecated
public class LifeCycleException extends NuxeoException {

    private static final long serialVersionUID = 1L;

    public LifeCycleException() {
    }

    public LifeCycleException(String message) {
        super(message);
    }

    public LifeCycleException(String message, Throwable cause) {
        super(message, cause);
    }

    public LifeCycleException(Throwable cause) {
        super(cause);
    }

}
