/*
 * (C) Copyright 2006-2009 Nuxeo SA (http://nuxeo.com/) and others.
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
 *     Nuxeo
 */

package org.nuxeo.ecm.platform.publisher.remoting.client;

import org.nuxeo.ecm.core.api.CoreSession;
import org.nuxeo.ecm.core.api.DocumentLocation;
import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.ecm.core.api.impl.DocumentLocationImpl;
import org.nuxeo.ecm.platform.publisher.api.*;
import org.nuxeo.ecm.platform.publisher.impl.service.AbstractRemotableTree;
import org.nuxeo.ecm.platform.publisher.impl.service.ProxyNode;
import org.nuxeo.ecm.platform.publisher.remoting.marshaling.DefaultMarshaler;
import org.nuxeo.ecm.platform.publisher.remoting.marshaling.basic.BasicPublicationNode;
import org.nuxeo.ecm.platform.publisher.remoting.marshaling.interfaces.RemotePublisherMarshaler;
import org.nuxeo.ecm.platform.publisher.remoting.restProxies.RemotePublicationTreeManagerRestProxy;
import org.nuxeo.runtime.api.Framework;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * {@link PublicationTree} implementation that points to a remote tree on a remote server.
 *
 * @author tiry
 */
public class ClientRemotePublicationTree extends AbstractRemotableTree implements PublicationTree {

    private static final long serialVersionUID = 1L;

    protected static final String ORGSERVER_KEY = "originalServer";

    protected static final String USERNAME_KEY = "userName";

    protected static final String PASSWORD_KEY = "password";

    protected static final String BASEURL_KEY = "baseURL";

    protected static final String MARSHALER_KEY = "marshaler";

    protected static final String TARGETTREENAME_KEY = "targetTree";

    public static final String ICON_EXPANDED_KEY = "iconExpanded";

    public static final String ICON_COLLAPSED_KEY = "iconCollapsed";

    public static final String TITLE_KEY = "title";

    protected String targetTreeName;

    protected String name;

    protected String serverSessionId;

    protected String title;

    protected String treeTitle;

    protected String rootPath;

    protected String nodeType;

    protected String iconCollapsed = "/icons/folder.gif";

    protected String iconExpanded = "/icons/folder_open.gif";

    protected PublishedDocumentFactory factory;

    protected ClientRemotePublicationNode rootNode;

    protected CoreSession coreSession;

    public ClientRemotePublicationTree() {
        // empty
    }

    protected String getTargetTreeName() {
        return targetTreeName;
    }

    @Override
    protected PublicationNode switchToClientNode(PublicationNode node) {
        return new ClientRemotePublicationNode(configName, sessionId, node, serverSessionId, treeService,
                getTargetTreeName());
    }

    @Override
    protected PublicationNode switchToServerNode(PublicationNode node) {
        if (node instanceof ClientRemotePublicationNode) {
            ClientRemotePublicationNode cNode = (ClientRemotePublicationNode) node;
            return new BasicPublicationNode(cNode.getNodeType(), cNode.getPath(), cNode.getTitle(),
                    cNode.getUnwrappedTreeName(), serverSessionId);
        }
        if (node instanceof ProxyNode) {
            ProxyNode rNode = (ProxyNode) node;
            return new BasicPublicationNode(rNode.getNodeType(), rNode.getPath(), rNode.getTitle(),
                    getTargetTreeName(), serverSessionId);
        } else {
            return node;
        }
    }

    public void initTree(String sid, CoreSession coreSession, Map<String, String> parameters,
            PublishedDocumentFactory factory, String configName, String title) {

        this.sessionId = sid;
        this.name = "Remote";
        this.configName = configName;
        this.factory = factory;
        this.coreSession = coreSession;
        treeTitle = title != null ? title : configName;

        String userName = parameters.get(USERNAME_KEY);
        String password = parameters.get(PASSWORD_KEY);
        String baseURL = Framework.expandVars(parameters.get(BASEURL_KEY));
        String marshalerClassName = parameters.get(MARSHALER_KEY);

        targetTreeName = parameters.get(TARGETTREENAME_KEY);
        if (targetTreeName == null)
            targetTreeName = name;

        if (parameters.containsKey(ICON_COLLAPSED_KEY)) {
            iconCollapsed = parameters.get(ICON_COLLAPSED_KEY);
        }
        if (parameters.containsKey(ICON_EXPANDED_KEY)) {
            iconExpanded = parameters.get(ICON_EXPANDED_KEY);
        }

        RemotePublisherMarshaler marshaler;

        if (marshalerClassName == null) {
            marshaler = new DefaultMarshaler();
        } else {
            try {
                marshaler = (RemotePublisherMarshaler) Class.forName(marshalerClassName).newInstance();
            } catch (ReflectiveOperationException e) {
                marshaler = new DefaultMarshaler();
            }
        }

        marshaler.setAssociatedCoreSession(coreSession);
        marshaler.setParameters(parameters);

        treeService = new RemotePublicationTreeManagerRestProxy(baseURL, userName, password, marshaler);

        Map<String, String> remoteParameters = new HashMap<String, String>();
        Map<String, String> rTree = treeService.initRemoteSession(targetTreeName, remoteParameters);

        serverSessionId = rTree.get("sessionId");
        this.title = rTree.get("title");
        rootPath = rTree.get("path");
        nodeType = rTree.get("nodeType");

        PublicationNode basicRootNode = new BasicPublicationNode(nodeType, rootPath, this.title, configName, sessionId);
        rootNode = new ClientRemotePublicationNode(configName, sessionId, basicRootNode, serverSessionId, treeService,
                getTargetTreeName());

    }

    @Override
    protected RemotePublicationTreeManager getTreeService() {
        return treeService;
    }

    /*
     * public List<PublicationNode> getTree() { return rootNode.getChildrenNodes(); }
     */

    public List<PublishedDocument> getChildrenDocuments() {
        return rootNode.getChildrenDocuments();
    }

    public String getPath() {
        return rootPath;
    }

    public String getTitle() {
        return title;
    }

    public String getTreeTitle() {
        return treeTitle;
    }

    public String getName() {
        return name;
    }

    public String getTreeType() {
        return "RemoteTree";
    }

    public String getNodeType() {
        return nodeType;
    }

    public String getTreeConfigName() {
        return configName;
    }

    public String getSessionId() {
        return sessionId;
    }

    public String getRemoteSessionId() {
        return getServerTreeSessionId();
    }

    @Override
    protected String getServerTreeSessionId() {
        return serverSessionId;
    }

    public PublishedDocument publish(DocumentModel doc, PublicationNode targetNode, Map<String, String> params)
            {

        doc = factory.snapshotDocumentBeforePublish(doc);
        return super.publish(doc, targetNode, params);
    }

    public List<PublicationNode> getChildrenNodes() {
        return rootNode.getChildrenNodes();
    }

    public String getType() {
        return this.getClass().getSimpleName();
    }

    @Override
    public List<PublishedDocument> getExistingPublishedDocument(DocumentLocation docLoc) {

        List<PublishedDocument> allPubDocs = new ArrayList<PublishedDocument>();

        List<DocumentModel> possibleDocsToCheck = new ArrayList<DocumentModel>();

        DocumentModel livedoc = coreSession.getDocument(docLoc.getDocRef());
        if (!livedoc.isVersion()) {
            possibleDocsToCheck = coreSession.getVersions(docLoc.getDocRef());
        }

        possibleDocsToCheck.add(0, livedoc);

        for (DocumentModel doc : possibleDocsToCheck) {
            List<PublishedDocument> pubDocs = getTreeService().getExistingPublishedDocument(getServerTreeSessionId(),
                    new DocumentLocationImpl(doc));
            allPubDocs.addAll(pubDocs);
        }

        return allPubDocs;
    }

    public String getIconExpanded() {
        return iconExpanded;
    }

    public String getIconCollapsed() {
        return iconCollapsed;
    }

}
