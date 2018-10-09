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
 *     Nuxeo - initial API and implementation
 * $Id$
 */

package org.nuxeo.ecm.directory;

import java.util.Collection;
import java.util.List;

import org.nuxeo.ecm.directory.api.DirectoryDeleteConstraint;

/**
 * The directory interface.
 * <p>
 * This interface is implemented in order to create an NXDirectory. One should implement this interface in order to
 * create either a new Directory implementation or a new Directory Source.
 *
 * @author glefter@nuxeo.com
 */
// TODO: maybe separate Directory implementation and Directory Source
public interface Directory {

    /**
     * Gets the unique name of the directory, used for registering.
     *
     * @return the unique directory name
     * @throws DirectoryException
     */
    String getName() throws DirectoryException;

    /**
     * Gets the schema name used by this directory.
     *
     * @return the schema name
     * @throws DirectoryException
     */
    String getSchema() throws DirectoryException;

    /**
     * Gets the name of the parent directory. This is used for hierarchical vocabularies.
     *
     * @return the name of the parent directory, or null.
     */
    String getParentDirectory() throws DirectoryException;

    /**
     * Gets the id field of the schema for this directory.
     *
     * @return the id field.
     * @throws DirectoryException
     */
    String getIdField() throws DirectoryException;

    /**
     * Gets the password field of the schema for this directory.
     *
     * @return the password field.
     * @throws DirectoryException
     */
    String getPasswordField() throws DirectoryException;

    /**
     * Checks if this directory is read-only.
     *
     * @since 8.2
     */
    boolean isReadOnly();

    /**
     * Shuts down the directory.
     *
     * @throws DirectoryException
     */
    void shutdown() throws DirectoryException;

    /**
     * Creates a session for accessing entries in this directory.
     *
     * @return a Session object
     * @throws DirectoryException if a session cannot be created
     */
    Session getSession() throws DirectoryException;

    /**
     * Lookup a Reference by field name.
     *
     * @return the matching reference implementation or null
     * @throws DirectoryException
     * @deprecated since 7.4, kept for compatibility with old code, use {@link #getReferences(String)} instead
     */
    @Deprecated
    Reference getReference(String referenceFieldName) throws DirectoryException;

    /**
     * Lookup the References by field name.
     *
     * @return the matching references implementation or null
     * @throws DirectoryException
     */
    List<Reference> getReferences(String referenceFieldName) throws DirectoryException;

    /**
     * Lookup all References defined on the directory.
     *
     * @return all registered references
     * @throws DirectoryException
     */
    Collection<Reference> getReferences() throws DirectoryException;

    /**
     * Gets the cache instance of the directory
     *
     * @return the cache of the directory
     * @throws DirectoryException
     */
    DirectoryCache getCache() throws DirectoryException;

    /**
     * Invalidates the cache instance of the directory
     *
     * @throws DirectoryException
     */
    void invalidateDirectoryCache() throws DirectoryException;

    /**
     * Returns {@code true} if this directory is a multi tenant directory, {@code false} otherwise.
     *
     * @since 5.6
     */
    boolean isMultiTenant() throws DirectoryException;

    /**
     * @since 8.4
     */
    List<String> getTypes();

    /**
     * @since 8.4
     */
    List<DirectoryDeleteConstraint> getDirectoryDeleteConstraints();

}
