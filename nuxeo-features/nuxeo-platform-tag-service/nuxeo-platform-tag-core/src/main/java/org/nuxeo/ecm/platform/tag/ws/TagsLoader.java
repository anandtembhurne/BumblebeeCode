/*
 * (C) Copyright 2011 Nuxeo SA (http://nuxeo.com/) and others.
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
 *     matic
 */
package org.nuxeo.ecm.platform.tag.ws;

import java.util.List;

import org.nuxeo.ecm.core.api.CoreSession;
import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.ecm.platform.api.ws.DocumentLoader;
import org.nuxeo.ecm.platform.api.ws.DocumentProperty;
import org.nuxeo.ecm.platform.api.ws.session.WSRemotingSession;
import org.nuxeo.ecm.platform.tag.Tag;
import org.nuxeo.ecm.platform.tag.TagService;
import org.nuxeo.runtime.api.Framework;

/**
 * @author matic
 */
public class TagsLoader implements DocumentLoader {

    @Override
    public void fillProperties(DocumentModel doc, List<DocumentProperty> props, WSRemotingSession rs)
            {
        CoreSession session = rs.getDocumentManager();
        TagService srv = Framework.getLocalService(TagService.class);
        if (srv == null) {
            return;
        }
        List<Tag> tags = srv.getDocumentTags(session, doc.getId(), rs.getUsername());
        String value = "";
        String sep = "";
        for (Tag tag : tags) {
            value = value + sep + tag.getLabel();
            sep = ",";
        }
        DocumentProperty prop = new DocumentProperty("tags", value);
        props.add(prop);
    }

}
