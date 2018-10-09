/*
 * (C) Copyright 2014 Nuxeo SA (http://nuxeo.com/) and others.
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
 *     Nicolas Chapurlat <nchapurlat@nuxeo.com>
 */

package org.nuxeo.ecm.core.api.validation;

import java.util.Arrays;

import javax.inject.Inject;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.nuxeo.common.utils.Path;
import org.nuxeo.ecm.core.api.CoreSession;
import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.ecm.core.api.impl.DocumentModelImpl;
import org.nuxeo.ecm.core.api.validation.DocumentValidationService.Forcing;
import org.nuxeo.ecm.core.test.CoreFeature;
import org.nuxeo.ecm.core.test.annotations.Granularity;
import org.nuxeo.ecm.core.test.annotations.RepositoryConfig;
import org.nuxeo.runtime.test.runner.Deploy;
import org.nuxeo.runtime.test.runner.Features;
import org.nuxeo.runtime.test.runner.FeaturesRunner;

// Look at the @Test(expected=...) to understand the tests
// for those tests, both saveDocument, createDocument and importDocument validation context are enable by default
@RunWith(FeaturesRunner.class)
@Deploy({ "org.nuxeo.ecm.core.test.tests:OSGI-INF/test-validation-activation-contrib.xml" })
@Features(CoreFeature.class)
@RepositoryConfig(cleanup = Granularity.METHOD)
public class TestDocumentValidationActivation {

    private static final int VALID = 12345;

    private static final int INVALID = -12345;

    private static final String SIMPLE_FIELD = "vs:groupCode";

    @Inject
    protected DocumentValidationService validator;

    @Inject
    protected CoreSession session;

    @Test
    public void testOnCreateDocumentActivationWithoutViolation() {
        DocumentModel doc;
        doc = session.createDocumentModel("/", "doc1", "ValidatedUserGroup");
        doc.setPropertyValue(SIMPLE_FIELD, VALID);
        doc = session.createDocument(doc);
    }

    @Test
    public void testOnCreateDocumentActivationWithViolationNotDirty() {
        DocumentModel doc;
        doc = session.createDocumentModel("/", "doc1", "ValidatedUserGroup");
        doc = session.createDocument(doc);
    }

    @Test(expected = DocumentValidationException.class)
    public void testOnCreateDocumentActivationWithViolation() {
        DocumentModel doc;
        doc = session.createDocumentModel("/", "doc1", "ValidatedUserGroup");
        doc.setPropertyValue(SIMPLE_FIELD, INVALID);
        doc = session.createDocument(doc);
    }

    @Test
    public void testOnCreateDocumentActivationWithViolationIgnored() {
        DocumentModel doc;
        doc = session.createDocumentModel("/", "doc1", "ValidatedUserGroup");
        doc.setPropertyValue(SIMPLE_FIELD, INVALID);
        doc.putContextData(DocumentValidationService.CTX_MAP_KEY, Forcing.TURN_OFF);
        doc = session.createDocument(doc);
    }

    @Test(expected = DocumentValidationException.class)
    public void testOnCreateDocumentActivationWithViolationForced() {
        DocumentModel doc;
        doc = session.createDocumentModel("/", "doc1", "ValidatedUserGroup");
        doc.setPropertyValue(SIMPLE_FIELD, INVALID);
        doc.putContextData(DocumentValidationService.CTX_MAP_KEY, Forcing.TURN_ON);
        doc = session.createDocument(doc);
    }

    @Test(expected = DocumentValidationException.class)
    public void testOnCreateDocumentActivationWithViolationNoForcing() {
        DocumentModel doc;
        doc = session.createDocumentModel("/", "doc1", "ValidatedUserGroup");
        doc.setPropertyValue(SIMPLE_FIELD, INVALID);
        doc.putContextData(DocumentValidationService.CTX_MAP_KEY, Forcing.USUAL);
        doc = session.createDocument(doc);
    }

    @Test
    public void testOnSaveDocumentActivationWithoutViolation() {
        DocumentModel doc;
        doc = session.createDocumentModel("/", "doc1", "ValidatedUserGroup");
        doc = session.createDocument(doc);
        doc.setPropertyValue(SIMPLE_FIELD, VALID);
        doc = session.saveDocument(doc);
    }

    @Test
    public void testOnSaveDocumentActivationWithViolationNotDirty() {
        DocumentModel doc;
        doc = session.createDocumentModel("/", "doc1", "ValidatedUserGroup");
        doc = session.createDocument(doc);
        doc = session.saveDocument(doc);
    }

    @Test(expected = DocumentValidationException.class)
    public void testOnSaveDocumentActivationWithViolation() {
        DocumentModel doc;
        doc = session.createDocumentModel("/", "doc1", "ValidatedUserGroup");
        doc = session.createDocument(doc);
        doc.setPropertyValue(SIMPLE_FIELD, INVALID);
        doc = session.saveDocument(doc);
    }

    @Test
    public void testOnSaveDocumentActivationWithViolationIgnored() {
        DocumentModel doc;
        doc = session.createDocumentModel("/", "doc1", "ValidatedUserGroup");
        doc = session.createDocument(doc);
        doc.setPropertyValue(SIMPLE_FIELD, INVALID);
        doc.putContextData(DocumentValidationService.CTX_MAP_KEY, Forcing.TURN_OFF);
        doc = session.saveDocument(doc);
    }

    @Test(expected = DocumentValidationException.class)
    public void testOnSaveDocumentActivationWithViolationForced() {
        DocumentModel doc;
        doc = session.createDocumentModel("/", "doc1", "ValidatedUserGroup");
        doc = session.createDocument(doc);
        doc.setPropertyValue(SIMPLE_FIELD, INVALID);
        doc.putContextData(DocumentValidationService.CTX_MAP_KEY, Forcing.TURN_ON);
        doc = session.saveDocument(doc);
    }

    @Test(expected = DocumentValidationException.class)
    public void testOnSaveDocumentActivationWithViolationNoForcing() {
        DocumentModel doc;
        doc = session.createDocumentModel("/", "doc1", "ValidatedUserGroup");
        doc = session.createDocument(doc);
        doc.setPropertyValue(SIMPLE_FIELD, INVALID);
        doc.putContextData(DocumentValidationService.CTX_MAP_KEY, Forcing.USUAL);
        doc = session.saveDocument(doc);
    }

    @Test
    public void testOnImportDocumentActivationWithoutViolation() {
        DocumentModel doc = new DocumentModelImpl(null, "ValidatedUserGroup", "12345", new Path("doc1"), null, null,
                null, null, null, null, null);
        doc.setPropertyValue(SIMPLE_FIELD, VALID);
        session.importDocuments(Arrays.asList(doc));
    }

    @Test(expected = DocumentValidationException.class)
    public void testOnImportDocumentActivationWithViolation() {
        DocumentModel doc = new DocumentModelImpl(null, "ValidatedUserGroup", "12345", new Path("doc1"), null, null,
                null, null, null, null, null);
        doc.setPropertyValue(SIMPLE_FIELD, INVALID);
        session.importDocuments(Arrays.asList(doc));
    }

    @Test
    public void testOnImportDocumentActivationWithViolationIgnored() {
        DocumentModel doc = new DocumentModelImpl(null, "ValidatedUserGroup", "12345", new Path("doc1"), null, null,
                null, null, null, null, null);
        doc.setPropertyValue(SIMPLE_FIELD, INVALID);
        doc.putContextData(DocumentValidationService.CTX_MAP_KEY, Forcing.TURN_OFF);
        session.importDocuments(Arrays.asList(doc));
    }

    @Test(expected = DocumentValidationException.class)
    public void testOnImportDocumentActivationWithViolationForced() {
        DocumentModel doc = new DocumentModelImpl(null, "ValidatedUserGroup", "12345", new Path("doc1"), null, null,
                null, null, null, null, null);
        doc.setPropertyValue(SIMPLE_FIELD, INVALID);
        doc.putContextData(DocumentValidationService.CTX_MAP_KEY, Forcing.TURN_ON);
        session.importDocuments(Arrays.asList(doc));
    }

    @Test(expected = DocumentValidationException.class)
    public void testOnImportDocumentActivationWithViolationNoForcing() {
        DocumentModel doc = new DocumentModelImpl(null, "ValidatedUserGroup", "12345", new Path("doc1"), null, null,
                null, null, null, null, null);
        doc.setPropertyValue(SIMPLE_FIELD, INVALID);
        doc.putContextData(DocumentValidationService.CTX_MAP_KEY, Forcing.USUAL);
        session.importDocuments(Arrays.asList(doc));
    }

}
