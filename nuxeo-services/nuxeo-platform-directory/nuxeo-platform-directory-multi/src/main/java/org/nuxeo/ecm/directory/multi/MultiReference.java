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
 *     Nuxeo - initial API and implementation
 *
 * $Id$
 */

package org.nuxeo.ecm.directory.multi;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.nuxeo.ecm.directory.AbstractReference;
import org.nuxeo.ecm.directory.Directory;
import org.nuxeo.ecm.directory.DirectoryEntryNotFoundException;
import org.nuxeo.ecm.directory.DirectoryException;
import org.nuxeo.ecm.directory.Reference;
import org.nuxeo.ecm.directory.api.DirectoryService;
import org.nuxeo.runtime.api.Framework;

public class MultiReference extends AbstractReference {

    private static final Log log = LogFactory.getLog(MultiReference.class);

    final MultiDirectory dir;

    final String fieldName;

    MultiReference(MultiDirectory dir, String fieldName) {
        this.dir = dir;
        this.fieldName = fieldName;
    }

    @Override
    public void addLinks(String sourceId, List<String> targetIds) throws DirectoryException {
        throw new UnsupportedOperationException();
    }

    @Override
    public void addLinks(List<String> sourceIds, String targetId) throws DirectoryException {
        throw new UnsupportedOperationException();
    }

    protected interface Collector {
        List<String> collect(List<Reference> dir) throws DirectoryException;
    }

    protected List<String> doCollect(Collector extractor) throws DirectoryException {
        DirectoryService dirService = Framework.getService(DirectoryService.class);
        Set<String> ids = new HashSet<String>();
        for (SourceDescriptor src : dir.getDescriptor().sources) {
            for (SubDirectoryDescriptor sub : src.subDirectories) {
                Directory dir = dirService.getDirectory(sub.name);
                if (dir == null) {
                    continue;
                }
                List<Reference> ref = dir.getReferences(fieldName);
                if (ref == null) {
                    continue;
                }
                try {
                    ids.addAll(extractor.collect(ref));
                } catch (DirectoryEntryNotFoundException e) {
                    log.debug(e.getMessage());
                }
            }
        }
        List<String> x = new ArrayList<String>(ids.size());
        x.addAll(ids);
        return x;
    }

    @Override
    public List<String> getSourceIdsForTarget(final String targetId) throws DirectoryException {
        return doCollect(new Collector() {
            @Override
            public List<String> collect(List<Reference> refs) throws DirectoryException {
                List<String> sourceIds = new ArrayList<>(1);
                for (Reference ref : refs) {
                    sourceIds.addAll(ref.getSourceIdsForTarget(targetId));
                }
                return sourceIds;
            }
        });
    }

    @Override
    public List<String> getTargetIdsForSource(final String sourceId) throws DirectoryException {
        return doCollect(new Collector() {
            @Override
            public List<String> collect(List<Reference> refs) throws DirectoryException {
                List<String> targetIds = new ArrayList<>(1);
                for (Reference ref : refs) {
                    targetIds.addAll(ref.getSourceIdsForTarget(sourceId));
                }
                return targetIds;
            }
        });
    }

    @Override
    public void removeLinksForSource(String sourceId) throws DirectoryException {
        throw new UnsupportedOperationException();
    }

    @Override
    public void removeLinksForTarget(String targetId) throws DirectoryException {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setSourceIdsForTarget(String targetId, List<String> sourceIds) throws DirectoryException {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setTargetIdsForSource(String sourceId, List<String> targetIds) throws DirectoryException {
        throw new UnsupportedOperationException();
    }

    /**
     * @since 5.6
     */
    @Override
    public MultiReference clone() {
        MultiReference clone = (MultiReference) super.clone();
        // basic fields are already copied by super.clone()
        return clone;
    }

}
