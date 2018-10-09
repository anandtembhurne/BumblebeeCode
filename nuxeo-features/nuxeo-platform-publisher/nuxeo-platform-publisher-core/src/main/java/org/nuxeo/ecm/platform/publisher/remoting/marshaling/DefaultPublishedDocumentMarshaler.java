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

package org.nuxeo.ecm.platform.publisher.remoting.marshaling;

import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.DocumentFactory;
import org.dom4j.DocumentHelper;
import org.dom4j.QName;
import org.nuxeo.ecm.core.api.DocumentRef;
import org.nuxeo.ecm.core.api.IdRef;
import org.nuxeo.ecm.core.api.NuxeoException;
import org.nuxeo.ecm.core.api.PathRef;
import org.nuxeo.ecm.platform.publisher.api.PublishedDocument;
import org.nuxeo.ecm.platform.publisher.remoting.marshaling.basic.BasicPublishedDocument;
import org.nuxeo.ecm.platform.publisher.remoting.marshaling.interfaces.PublishedDocumentMarshaler;

/**
 * {@link PublishedDocument} marshaler using simple XML representation.
 *
 * @author tiry
 */
public class DefaultPublishedDocumentMarshaler extends AbstractDefaultXMLMarshaler implements
        PublishedDocumentMarshaler {

    protected static QName rootTag = DocumentFactory.getInstance().createQName("publishedDocument",
            publisherSerializerNSPrefix, publisherSerializerNS);

    protected static QName sourceRefTag = DocumentFactory.getInstance().createQName("sourceRef",
            publisherSerializerNSPrefix, publisherSerializerNS);

    protected static QName repositoryNameTag = DocumentFactory.getInstance().createQName("repositoryName",
            publisherSerializerNSPrefix, publisherSerializerNS);

    protected static QName serverNameTag = DocumentFactory.getInstance().createQName("serverName",
            publisherSerializerNSPrefix, publisherSerializerNS);

    protected static QName versionLabelTag = DocumentFactory.getInstance().createQName("versionLabel",
            publisherSerializerNSPrefix, publisherSerializerNS);

    protected static QName pathTag = DocumentFactory.getInstance().createQName("path", publisherSerializerNSPrefix,
            publisherSerializerNS);

    protected static QName parentPathTag = DocumentFactory.getInstance().createQName("parentPath",
            publisherSerializerNSPrefix, publisherSerializerNS);

    protected static QName isPendingTag = DocumentFactory.getInstance().createQName("isPending",
            publisherSerializerNSPrefix, publisherSerializerNS);

    public String marshalPublishedDocument(PublishedDocument pubDoc) {
        if (pubDoc == null)
            return "";

        org.dom4j.Element rootElem = DocumentFactory.getInstance().createElement(rootTag);
        rootElem.addNamespace(publisherSerializerNSPrefix, publisherSerializerNS);
        org.dom4j.Document rootDoc = DocumentFactory.getInstance().createDocument(rootElem);

        org.dom4j.Element sourceElem = rootElem.addElement(sourceRefTag);
        sourceElem.setText(pubDoc.getSourceDocumentRef().toString());

        org.dom4j.Element repoElem = rootElem.addElement(repositoryNameTag);
        repoElem.setText(pubDoc.getSourceRepositoryName());

        org.dom4j.Element srvElem = rootElem.addElement(serverNameTag);
        srvElem.setText(pubDoc.getSourceServer());

        org.dom4j.Element versionElem = rootElem.addElement(versionLabelTag);
        versionElem.setText("" + pubDoc.getSourceVersionLabel());

        org.dom4j.Element pathElem = rootElem.addElement(pathTag);
        pathElem.setText("" + pubDoc.getPath());

        org.dom4j.Element parentPathElem = rootElem.addElement(parentPathTag);
        parentPathElem.setText("" + pubDoc.getParentPath());

        org.dom4j.Element isPendingElem = rootElem.addElement(isPendingTag);
        isPendingElem.setText("" + pubDoc.isPending());

        String data = rootDoc.asXML();

        return cleanUpXml(data);

    }

    public PublishedDocument unMarshalPublishedDocument(String data) {

        PublishedDocument pubDoc;
        try {
            Document document = DocumentHelper.parseText(data);
            org.dom4j.Element rootElem = document.getRootElement();

            String strDocRef = rootElem.element(sourceRefTag).getTextTrim();
            DocumentRef docRef;
            if (strDocRef.startsWith("/"))
                docRef = new PathRef(strDocRef);
            else
                docRef = new IdRef(strDocRef);

            String repo = rootElem.element(repositoryNameTag).getTextTrim();
            String server = rootElem.element(serverNameTag).getTextTrim();
            String version = rootElem.element(versionLabelTag).getTextTrim();
            String path = rootElem.element(pathTag).getTextTrim();
            String parentPath = rootElem.element(parentPathTag).getTextTrim();
            boolean isPending = Boolean.parseBoolean(rootElem.element(isPendingTag).getTextTrim());

            pubDoc = new BasicPublishedDocument(docRef, repo, server, version, path, parentPath, isPending);
        } catch (DocumentException e) {
            throw new NuxeoException("Unable to unmarshal Published Document", e);
        }
        return pubDoc;
    }

}
