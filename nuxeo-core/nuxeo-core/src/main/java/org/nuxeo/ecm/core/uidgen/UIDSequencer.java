/*
 * (C) Copyright 2006-2007 Nuxeo SA (http://nuxeo.com/) and others.
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
 *     Dragos Mihalache
 */
package org.nuxeo.ecm.core.uidgen;

/**
 * UID Sequencer interface defines a method to retrieve next ids based on a given key.
 */
public interface UIDSequencer {

    /**
     * Gets the sequencer name.
     *
     * @since 7.4
     */
    String getName();

    /**
     * Sets the sequencer name.
     *
     * @since 7.4
     */
    void setName(String name);

    /**
     * Init Sequencer
     *
     * @since 7.3
     */
    void init();

    /**
     * Initializes the sequencer with the given key to at least the given id.
     * <p>
     * A sequence can only be incremented, so if its current id is greater than the given id the sequence won't be
     * decremented to reach the given id.
     *
     * @since 7.4
     */
    void initSequence(String key, int id);

    /**
     * For the given key returns the incremented UID which is also stored in the same sequence entry. This is a
     * "one time use" function for a document.
     *
     * @param key
     * @return
     */
    int getNext(String key);

    /**
     * Extends {@link UIDSequencer#getNext(java.lang.String)} to return a long value. This method is compatible
     * with getNext in the integer range.
     *
     * @since 8.3
     */
    long getNextLong(String key);

    /**
     * Cleanup callback
     *
     * @since 7.3
     */
    void dispose();

}
