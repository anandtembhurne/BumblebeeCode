/*
 * (C) Copyright 2007 Nuxeo SA (http://nuxeo.com/) and others.
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
 *
 * $Id$
 */

package org.nuxeo.ecm.platform.comment.api;

import java.util.List;

import org.nuxeo.ecm.core.api.DocumentModel;

/**
 * @author <a href="mailto:glefter@nuxeo.com">George Lefter</a>
 */
public interface CommentManager {

    List<DocumentModel> getComments(DocumentModel docModel);

    List<DocumentModel> getComments(DocumentModel docModel, DocumentModel parent);

    /**
     * @deprecated CommentManager cannot find the author if invoked remotely so one should use
     *             {@link #createComment(DocumentModel, String, String)}
     */
    @Deprecated
    DocumentModel createComment(DocumentModel docModel, String comment);

    /**
     * Creates a comment document model, filling its properties with given info and linking it to given document.
     *
     * @param docModel the document to comment
     * @param comment the comment content
     * @param author the comment author
     * @return the comment document model.
     */
    DocumentModel createComment(DocumentModel docModel, String comment, String author);

    DocumentModel createComment(DocumentModel docModel, DocumentModel comment);

    DocumentModel createComment(DocumentModel docModel, DocumentModel parent, DocumentModel child);

    void deleteComment(DocumentModel docModel, DocumentModel comment);

    /**
     * Gets documents in relation with a particular comment.
     *
     * @param comment the comment
     * @return the list of documents
     */
    List<DocumentModel> getDocumentsForComment(DocumentModel comment);

    /**
     * Gets thread in relation with a given comment (post or comment)
     *
     * @param comment
     * @return
     * @since 5.5
     */
    DocumentModel getThreadForComment(DocumentModel comment);

    /**
     * Creates a comment document model. It gives opportunity to save the comments in a specified location.
     *
     * @param docModel the document to comment
     * @param comment the comment content
     * @param path the location path
     * @return the comment document model.
     */
    DocumentModel createLocatedComment(DocumentModel docModel, DocumentModel comment, String path);

}
