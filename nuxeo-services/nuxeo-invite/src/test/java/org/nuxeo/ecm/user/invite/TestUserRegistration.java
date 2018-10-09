/*
 * (C) Copyright 2014-2015 Nuxeo SA (http://nuxeo.com/) and others.
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
 */
package org.nuxeo.ecm.user.invite;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.nuxeo.ecm.user.invite.UserRegistrationConfiguration.DEFAULT_CONFIGURATION_NAME;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;
import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.ecm.core.api.DocumentModelList;
import org.nuxeo.ecm.platform.usermanager.exceptions.UserAlreadyExistsException;

/**
 * @author <a href="mailto:akervern@nuxeo.com">Arnaud Kervern</a>
 */
public class TestUserRegistration extends AbstractUserRegistration {

    @Before
    public void init() {
        initializeRegistrations();
    }

    @Test
    public void testTestContribution() {
        DocumentModel doc = session.createDocumentModel("TestRegistration");
        assertTrue(doc.hasFacet("UserInvitation"));

        assertNotNull(userRegistrationService);
        UserRegistrationConfiguration config = userRegistrationService.getConfiguration();
        assertEquals("Workspace", config.getContainerDocType());
    }

    @Test
    public void testBasicUserRegistration() {
        UserRegistrationConfiguration configuration = ((UserInvitationComponent) userRegistrationService).configurations.get(DEFAULT_CONFIGURATION_NAME);
        // User info
        DocumentModel userInfo = session.createDocumentModel(configuration.getRequestDocType());
        userInfo.setPropertyValue("userinfo:login", "jolivier");
        userInfo.setPropertyValue("userinfo:firstName", "jolivier");
        userInfo.setPropertyValue("userinfo:lastName", "jolivier");
        userInfo.setPropertyValue("userinfo:email", "oolivier@dummy.com");
        userInfo.setPropertyValue("userinfo:tenantId", "tenant-a");

        DocumentModelList users = userManager.searchUsers("jolivier");
        assertEquals(0, users.size());

        String requestId = userRegistrationService.submitRegistrationRequest(userInfo,
                new HashMap<String, Serializable>(), UserInvitationService.ValidationMethod.NONE, true);
        userRegistrationService.validateRegistration(requestId, new HashMap<String, Serializable>());

        users = userManager.searchUsers("jolivier");
        DocumentModel jolivier = users.get(0);
        assertEquals(1, userManager.searchUsers("jolivier").size());
        assertEquals("tenant-a", jolivier.getPropertyValue("tenantId"));
    }

    @Test
    public void testBasicUserRegistrationWithLoginChanged() {
        UserRegistrationConfiguration configuration = ((UserInvitationComponent) userRegistrationService).configurations.get(DEFAULT_CONFIGURATION_NAME);
        // User info
        DocumentModel userInfo = session.createDocumentModel(configuration.getRequestDocType());
        String templogin = "templogin";
        String newUser = "newUser";

        userInfo.setPropertyValue("userinfo:login", templogin);
        userInfo.setPropertyValue("userinfo:firstName", "John");
        userInfo.setPropertyValue("userinfo:lastName", "Olivier");
        userInfo.setPropertyValue("userinfo:email", templogin + "@dummy.com");

        assertEquals(0, userManager.searchUsers(templogin).size());
        assertEquals(0, userManager.searchUsers(newUser).size());

        String requestId = userRegistrationService.submitRegistrationRequest(userInfo,
                new HashMap<String, Serializable>(), UserInvitationService.ValidationMethod.NONE, true);
        Map<String, Serializable> additionnalInfos = new HashMap<>();
        additionnalInfos.put("userinfo:login", newUser);
        userRegistrationService.validateRegistration(requestId, additionnalInfos);

        assertEquals(0, userManager.searchUsers(templogin).size());
        assertEquals(1, userManager.searchUsers(newUser).size());
    }

    @Test(expected = UserAlreadyExistsException.class)
    public void testMultipleRegistrationWithSameEmail() {
        UserRegistrationConfiguration configuration = ((UserInvitationComponent) userRegistrationService).configurations.get(DEFAULT_CONFIGURATION_NAME);
        // User info
        DocumentModel userInfo = session.createDocumentModel(configuration.getRequestDocType());
        String templogin = "templogin";
        String newUser = "newUser";

        userInfo.setPropertyValue("userinfo:login", templogin);
        userInfo.setPropertyValue("userinfo:firstName", "John");
        userInfo.setPropertyValue("userinfo:lastName", "Olivier");
        userInfo.setPropertyValue("userinfo:email", templogin + "@dummy.com");

        String requestId = userRegistrationService.submitRegistrationRequest(userInfo,
                new HashMap<String, Serializable>(0), UserInvitationService.ValidationMethod.NONE, true);
        Map<String, Serializable> additionnalInfos = new HashMap<>();
        additionnalInfos.put("userinfo:login", newUser);
        userRegistrationService.validateRegistration(requestId, additionnalInfos);

        userInfo.setPropertyValue("userinfo:login", templogin + '1');
        // Must throw a UserAlreadyExistsException
        userRegistrationService.submitRegistrationRequest(userInfo, new HashMap<String, Serializable>(0),
                UserInvitationService.ValidationMethod.NONE, true);
    }
}
