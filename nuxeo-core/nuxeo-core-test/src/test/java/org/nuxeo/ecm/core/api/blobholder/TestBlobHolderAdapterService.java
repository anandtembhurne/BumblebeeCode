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

package org.nuxeo.ecm.core.api.blobholder;

import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

import org.nuxeo.ecm.core.api.Constants;
import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.ecm.core.api.impl.DocumentModelImpl;
import org.nuxeo.runtime.api.Framework;
import org.nuxeo.runtime.test.NXRuntimeTestCase;

public class TestBlobHolderAdapterService extends NXRuntimeTestCase {

    @Before
    public void setUp() throws Exception {
        super.setUp();
        deployBundle("org.nuxeo.ecm.core.schema");
        deployBundle("org.nuxeo.ecm.core.api");
    }

    @Test
    public void testService() throws Exception {
        BlobHolderAdapterService bhas = Framework.getLocalService(BlobHolderAdapterService.class);
        assertNotNull(bhas);
    }

    @Test
    public void testContrib() throws Exception {
        assertEquals(0, BlobHolderAdapterComponent.getFactoryNames().size());
        deployContrib(Constants.CORE_TEST_TESTS_BUNDLE, "test-blob-holder-adapters-contrib.xml");
        assertEquals(1, BlobHolderAdapterComponent.getFactoryNames().size());

        BlobHolderAdapterService bhas = Framework.getLocalService(BlobHolderAdapterService.class);
        assertNotNull(bhas);

        DocumentModel doc = new DocumentModelImpl("Test");
        BlobHolder bh = bhas.getBlobHolderAdapter(doc);

        assertNotNull(bh);

        assertTrue(bh.getFilePath().startsWith("Test"));
        assertEquals("Test", bh.getBlob().getString());
    }

}
