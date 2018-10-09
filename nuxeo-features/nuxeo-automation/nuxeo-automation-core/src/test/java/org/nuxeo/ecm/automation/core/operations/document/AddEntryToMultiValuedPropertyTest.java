///*
// * (C) Copyright 2006-2013 Nuxeo SA (http://nuxeo.com/) and others.
// *
// * Licensed under the Apache License, Version 2.0 (the "License");
// * you may not use this file except in compliance with the License.
// * You may obtain a copy of the License at
// *
// *     http://www.apache.org/licenses/LICENSE-2.0
// *
// * Unless required by applicable law or agreed to in writing, software
// * distributed under the License is distributed on an "AS IS" BASIS,
// * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// * See the License for the specific language governing permissions and
// * limitations under the License.
// *
// * Contributors:
// *     Benjamin JALON <bjalon@nuxeo.com>
// */
//package org.nuxeo.ecm.automation.core.operations.document;
//
//import static junit.framework.Assert.assertEquals;
//import static junit.framework.Assert.assertNull;
//import static junit.framework.Assert.fail;
//
//import javax.inject.Inject;
//
//import org.junit.Before;
//import org.junit.Test;
//import org.junit.runner.RunWith;
//import org.nuxeo.ecm.core.api.CoreSession;
//import org.nuxeo.ecm.core.api.DocumentModel;
//import org.nuxeo.ecm.core.test.CoreFeature;
//import org.nuxeo.runtime.test.runner.Deploy;
//import org.nuxeo.runtime.test.runner.Features;
//import org.nuxeo.runtime.test.runner.FeaturesRunner;
//import org.nuxeo.runtime.test.runner.LocalDeploy;
//
///**
// * @since 5.7
// */
//@RunWith(FeaturesRunner.class)
//@Features(CoreFeature.class)
//@Deploy("org.nuxeo.ecm.automation.core")
//@LocalDeploy("org.nuxeo.ecm.automation.core:OSGI-INF/my-automation-doc-type-contrib.xml")
//public class AddEntryToMultiValuedPropertyTest {
//
//    @Inject
//    CoreSession session;
//
//    private DocumentModel doc;
//
//    private AddEntryToMultiValuedProperty operation;
//
//    @Before
//    public void setup() {
//        doc = session.createDocumentModel("/", "test", "MyDocument");
//        session.createDocument(doc);
//        session.save();
//
//        operation = new AddEntryToMultiValuedProperty();
//        operation.checkExists = true;
//        operation.save = true;
//        operation.session = session;
//        operation.value = "Test";
//        operation.xpath = "lists:string";
//    }
//
//    @Test
//    public void shouldAddEntryIntoStringList() throws Exception {
//        assertNull(doc.getPropertyValue("lists:string"));
//
//        operation.run(doc);
//        String[] value = (String[]) doc.getPropertyValue("lists:string");
//        assertEquals(1, value.length);
//        assertEquals("Test", value[0]);
//
//        operation.value = "Test2";
//        operation.run(doc);
//        value = (String[]) doc.getPropertyValue("lists:string");
//        assertEquals(2, value.length);
//        assertEquals("Test2", value[1]);
//
//        operation.value = "Test2";
//        operation.run(doc);
//        value = (String[]) doc.getPropertyValue("lists:string");
//        assertEquals(2, value.length);
//        assertEquals("Test2", value[1]);
//
//        operation.checkExists = false;
//
//        operation.value = "Test2";
//        operation.run(doc);
//        value = (String[]) doc.getPropertyValue("lists:string");
//        assertEquals(3, value.length);
//    }
//
//    @Test
//    public void shouldFailedForComplextType() throws Exception {
//        doc = session.createDocumentModel("/", "test", "File");
//        session.createDocument(doc);
//        session.save();
//
//        operation.xpath = "files:files";
//
//        try {
//            operation.run(doc);
//            fail();
//        } catch (UnsupportedOperationException e) {
//            assertEquals("Manage only lists of scalar items", e.getMessage());
//        } catch (Exception e) {
//            fail();
//        }
//
//    }
//
//    @Test
//    public void shouldFailedIfToTypeNotMatch() throws Exception {
//        operation.value = Boolean.FALSE;
//
//        try {
//            operation.run(doc);
//            fail();
//        } catch (UnsupportedOperationException e) {
//            assertEquals("Given type \"false\" value is not a string type", e.getMessage());
//        } catch (Exception e) {
//            fail();
//        }
//
//    }
//
//}
