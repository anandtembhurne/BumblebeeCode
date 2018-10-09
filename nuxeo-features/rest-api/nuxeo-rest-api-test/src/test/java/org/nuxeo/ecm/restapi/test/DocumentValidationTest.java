/*
 * (C) Copyright 2013 Nuxeo SA (http://nuxeo.com/) and others.
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
 *     dmetzler
 */
package org.nuxeo.ecm.restapi.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.nuxeo.ecm.core.io.registry.MarshallingConstants.EMBED_PROPERTIES;
import static org.nuxeo.ecm.core.io.registry.MarshallingConstants.WILDCARD_VALUE;

import java.io.IOException;

import javax.ws.rs.core.Response;

import org.apache.commons.io.IOUtils;
import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.JsonProcessingException;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.ecm.core.api.PathRef;
import org.nuxeo.ecm.core.test.annotations.Granularity;
import org.nuxeo.ecm.core.test.annotations.RepositoryConfig;
import org.nuxeo.runtime.test.runner.Features;
import org.nuxeo.runtime.test.runner.FeaturesRunner;
import org.nuxeo.runtime.test.runner.Jetty;
import org.nuxeo.runtime.test.runner.LocalDeploy;

import com.sun.jersey.api.client.ClientResponse;

/**
 * Test the CRUD rest API
 *
 * @since 5.7.2
 */
@RunWith(FeaturesRunner.class)
@Features({ RestServerFeature.class })
@Jetty(port = 18090)
@RepositoryConfig(cleanup = Granularity.METHOD, init = RestServerInit.class)
@LocalDeploy({ "org.nuxeo.ecm.restapi.test:test-validation-contrib.xml" })
public class DocumentValidationTest extends BaseTest {

    private static final String VALID_DOC = createDocumentJSON("\"Bill\"", "\"Boquet\"");

    private static final String INVALID_DOC = createDocumentJSON("\"   \"", "\"   \"");

    private static String createDocumentJSON(String firstname, String lastname) {
        String doc = "{";
        doc += "\"entity-type\":\"document\" ,";
        doc += "\"name\":\"doc1\" ,";
        doc += "\"type\":\"ValidatedDocument\" ,";
        doc += "\"properties\" : {";
        doc += "\"vs:users\" : [ { \"firstname\" : " + firstname + " , \"lastname\" : " + lastname + "} ]";
        doc += "}}";
        return doc;
    }

    @Test
    public void testCreateValidDocumentEndpointId() {
        DocumentModel root = session.getDocument(new PathRef("/"));
        ClientResponse response = getResponse(RequestType.POST, "id/" + root.getId(), VALID_DOC);
        assertEquals(Response.Status.CREATED.getStatusCode(), response.getStatus());
    }

    @Test
    public void testCreateValidDocumentEndpointPath() {
        ClientResponse response = getResponse(RequestType.POST, "path/", VALID_DOC);
        assertEquals(Response.Status.CREATED.getStatusCode(), response.getStatus());
    }

    @Test
    public void testCreateDocumentWithViolationEndpointId() throws Exception {
        DocumentModel root = session.getDocument(new PathRef("/"));
        ClientResponse response = getResponse(RequestType.POST, "id/" + root.getId(), INVALID_DOC);
        assertEquals(Response.Status.BAD_REQUEST.getStatusCode(), response.getStatus());
        checkResponseHasErrors(response);
    }

    @Test
    public void testCreateDocumentWithViolationEndpointPath() throws Exception {
        ClientResponse response = getResponse(RequestType.POST, "path/", INVALID_DOC);
        assertEquals(Response.Status.BAD_REQUEST.getStatusCode(), response.getStatus());
        checkResponseHasErrors(response);
    }

    @Test
    public void testSaveValidDocumentEndpointId() {
        DocumentModel doc = session.createDocumentModel("/", "doc1", "ValidatedDocument");
        doc = session.createDocument(doc);
        fetchInvalidations();
        ClientResponse response = getResponse(RequestType.PUT, "id/" + doc.getId(), VALID_DOC);
        assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
    }

    @Test
    public void testSaveValidDocumentEndpointPath() {
        DocumentModel doc = session.createDocumentModel("/", "doc1", "ValidatedDocument");
        doc = session.createDocument(doc);
        fetchInvalidations();
        ClientResponse response = getResponse(RequestType.PUT, "path/doc1", VALID_DOC);
        assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
    }

    @Test
    public void testSaveDocumentWithViolationEndpointId() throws Exception {
        DocumentModel doc = session.createDocumentModel("/", "doc1", "ValidatedDocument");
        doc = session.createDocument(doc);
        fetchInvalidations();
        ClientResponse response = getResponse(RequestType.PUT, "id/" + doc.getId(), INVALID_DOC);
        assertEquals(Response.Status.BAD_REQUEST.getStatusCode(), response.getStatus());
        checkResponseHasErrors(response);
    }

    @Test
    public void testSaveDocumentWithViolationEndpointPath() throws Exception {
        DocumentModel doc = session.createDocumentModel("/", "doc1", "ValidatedDocument");
        doc = session.createDocument(doc);
        fetchInvalidations();
        ClientResponse response = getResponse(RequestType.PUT, "path/doc1", INVALID_DOC);
        assertEquals(Response.Status.BAD_REQUEST.getStatusCode(), response.getStatus());
        checkResponseHasErrors(response);
    }

    @Test
    public void testPropertyLoading() throws Exception {
        DocumentModel doc = session.createDocumentModel("/", "doc1", "ValidatedDocument");
        doc.getProperty("userRefs").addValue("user:Administrator");
        doc = session.createDocument(doc);
        fetchInvalidations();
        ClientResponse response = service.path("path/doc1").queryParam("embed", "*").header(EMBED_PROPERTIES,
                WILDCARD_VALUE).get(ClientResponse.class);
        assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
        IOUtils.copy(response.getEntityInputStream(), System.out);
    }

    private void checkResponseHasErrors(ClientResponse response) throws IOException, JsonProcessingException {
        String responseText = IOUtils.toString(response.getEntityInputStream());
        // System.out.println(responseText);
        JsonNode node = mapper.readTree(responseText);
        assertTrue(node.get("has_error").getValueAsBoolean());
        assertEquals(2, node.get("number").getValueAsInt());
        JsonNode violations = node.get("violations");
        JsonNode violation1 = violations.getElements().next();
        assertEquals("PatternConstraint", violation1.get("constraint").get("name").getTextValue());
        JsonNode violation2 = violations.getElements().next();
        assertEquals("PatternConstraint", violation2.get("constraint").get("name").getTextValue());
    }

}
