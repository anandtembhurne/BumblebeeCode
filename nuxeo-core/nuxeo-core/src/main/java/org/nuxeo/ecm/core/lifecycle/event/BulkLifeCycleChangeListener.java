/*
 * (C) Copyright 2006-2011 Nuxeo SA (http://nuxeo.com/) and others.
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

package org.nuxeo.ecm.core.lifecycle.event;

import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.nuxeo.ecm.core.NXCore;
import org.nuxeo.ecm.core.api.CoreSession;
import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.ecm.core.api.DocumentModelList;
import org.nuxeo.ecm.core.api.LifeCycleConstants;
import org.nuxeo.ecm.core.api.event.CoreEventConstants;
import org.nuxeo.ecm.core.api.event.DocumentEventTypes;
import org.nuxeo.ecm.core.api.impl.DocumentModelListImpl;
import org.nuxeo.ecm.core.event.Event;
import org.nuxeo.ecm.core.event.EventBundle;
import org.nuxeo.ecm.core.event.EventContext;
import org.nuxeo.ecm.core.event.PostCommitEventListener;
import org.nuxeo.ecm.core.event.impl.DocumentEventContext;

/**
 * Listener for life cycle change events.
 * <p>
 * If event occurs on a folder, it will recurse on children to perform the same transition if possible.
 * <p>
 * If the transition event is about marking documents as "deleted", and a child cannot perform the transition, it will
 * be removed.
 * <p>
 * Undelete transitions are not processed, but this listener instead looks for a specific documentUndeleted event. This
 * is because we want to undelete documents (parents) under which we don't want to recurse.
 * <p>
 * Reinit document copy lifeCycle (BulkLifeCycleChangeListener is bound to the event documentCreatedByCopy)
 */
public class BulkLifeCycleChangeListener implements PostCommitEventListener {

    private static final Log log = LogFactory.getLog(BulkLifeCycleChangeListener.class);

    @Override
    public void handleEvent(EventBundle events) {
        if (!events.containsEventName(LifeCycleConstants.TRANSITION_EVENT)
                && !events.containsEventName(LifeCycleConstants.DOCUMENT_UNDELETED)
                && !events.containsEventName(DocumentEventTypes.DOCUMENT_CREATED_BY_COPY)) {
            return;
        }
        for (Event event : events) {
            String name = event.getName();
            if (LifeCycleConstants.TRANSITION_EVENT.equals(name) || LifeCycleConstants.DOCUMENT_UNDELETED.equals(name)
                    || DocumentEventTypes.DOCUMENT_CREATED_BY_COPY.equals(name)) {
                processTransition(event);
            }
        }
    }

    protected void processTransition(Event event) {
        log.debug("Processing lifecycle change in async listener");
        EventContext ctx = event.getContext();
        if (!(ctx instanceof DocumentEventContext)) {
            return;
        }
        DocumentEventContext docCtx = (DocumentEventContext) ctx;
        DocumentModel doc = docCtx.getSourceDocument();
        if (!doc.isFolder() && !DocumentEventTypes.DOCUMENT_CREATED_BY_COPY.equals(event.getName())) {
            return;
        }
        CoreSession session = docCtx.getCoreSession();
        if (session == null) {
            log.error("Can not process lifeCycle change since session is null");
            return;
        }
        String transition;
        String targetState;
        if (DocumentEventTypes.DOCUMENT_CREATED_BY_COPY.equals(event.getName())) {
            if (!Boolean.TRUE.equals(event.getContext().getProperties().get(CoreEventConstants.RESET_LIFECYCLE))) {
                return;
            }
            DocumentModelList docs = new DocumentModelListImpl();
            docs.add(doc);
            if (session.exists(doc.getRef())) {
                reinitDocumentsLifeCyle(session, docs);
                session.save();
            }
        } else {
            if (LifeCycleConstants.TRANSITION_EVENT.equals(event.getName())) {
                transition = (String) docCtx.getProperty(LifeCycleConstants.TRANSTION_EVENT_OPTION_TRANSITION);
                if (isNonRecursiveTransition(transition, doc.getType())) {
                    // transition should not recurse into children
                    return;
                }
                if (LifeCycleConstants.UNDELETE_TRANSITION.equals(transition)) {
                    // not processed (as we can undelete also parents)
                    // a specific event documentUndeleted will be used instead
                    return;
                }
                targetState = (String) docCtx.getProperty(LifeCycleConstants.TRANSTION_EVENT_OPTION_TO);
            } else { // LifeCycleConstants.DOCUMENT_UNDELETED
                transition = LifeCycleConstants.UNDELETE_TRANSITION;
                targetState = ""; // unused
            }
            DocumentModelList docs = session.getChildren(doc.getRef());
            changeDocumentsState(session, docs, transition, targetState);
            session.save();
        }
    }

    protected void reinitDocumentsLifeCyle(CoreSession documentManager, DocumentModelList docs) {
        for (DocumentModel docMod : docs) {
            documentManager.reinitLifeCycleState(docMod.getRef());
            if (docMod.isFolder()) {
                DocumentModelList children = documentManager.query(String.format(
                        "SELECT * FROM Document WHERE ecm:currentLifeCycleState != 'deleted' AND ecm:parentId = '%s'",
                        docMod.getRef()));
                reinitDocumentsLifeCyle(documentManager, children);
            }
        }
    }

    protected boolean isNonRecursiveTransition(String transition, String type) {
        List<String> nonRecursiveTransitions = NXCore.getLifeCycleService().getNonRecursiveTransitionForDocType(type);
        return nonRecursiveTransitions.contains(transition);
    }

    // change doc state and recurse in children
    protected void changeDocumentsState(CoreSession documentManager, DocumentModelList docModelList, String transition,
            String targetState) {
        for (DocumentModel docMod : docModelList) {
            boolean removed = false;
            if (docMod.getCurrentLifeCycleState() == null) {
                if (LifeCycleConstants.DELETED_STATE.equals(targetState)) {
                    log.debug("Doc has no lifecycle, deleting ...");
                    documentManager.removeDocument(docMod.getRef());
                    removed = true;
                }
            } else if (docMod.getAllowedStateTransitions().contains(transition) && !docMod.isProxy()) {
                docMod.followTransition(transition);
            } else {
                if (targetState.equals(docMod.getCurrentLifeCycleState())) {
                    log.debug("Document" + docMod.getRef() + " is already in the target LifeCycle state");
                } else if (LifeCycleConstants.DELETED_STATE.equals(targetState)) {
                    log.debug("Impossible to change state of " + docMod.getRef() + " :removing");
                    documentManager.removeDocument(docMod.getRef());
                    removed = true;
                } else {
                    log.debug("Document" + docMod.getRef() + " has no transition to the target LifeCycle state");
                }
            }
            if (docMod.isFolder() && !removed) {
                changeDocumentsState(documentManager, documentManager.getChildren(docMod.getRef()), transition,
                        targetState);
            }
        }
    }
}
