/*
 * (C) Copyright 2006-2015 Nuxeo SA (http://nuxeo.com/) and others.
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
 */

package org.nuxeo.ecm.platform.commandline.executor.tests;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.nuxeo.ecm.platform.commandline.executor.api.CommandLineExecutorService;
import org.nuxeo.ecm.platform.commandline.executor.api.CommandNotAvailable;
import org.nuxeo.runtime.api.Framework;
import org.nuxeo.runtime.test.NXRuntimeTestCase;

/**
 * Test service and EPs.
 *
 * @author tiry
 */
public class TestService extends NXRuntimeTestCase {

    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();
        deployBundle("org.nuxeo.ecm.platform.commandline.executor");
    }

    @Test
    public void testServiceExist() {
        CommandLineExecutorService cles = Framework.getLocalService(CommandLineExecutorService.class);
        assertNotNull(cles);
    }

    @Test
    public void testCmdRegistration() throws Exception {
        CommandLineExecutorService cles = Framework.getLocalService(CommandLineExecutorService.class);
        assertNotNull(cles);

        deployContrib("org.nuxeo.ecm.platform.commandline.executor", "OSGI-INF/commandline-aspell-test-contribs.xml");
        List<String> cmds = cles.getRegistredCommands();
        assertNotNull(cmds);
        assertEquals(1, cmds.size());
        assertTrue(cmds.contains("aspell"));

        deployContrib("org.nuxeo.ecm.platform.commandline.executor", "OSGI-INF/commandline-imagemagic-test-contrib.xml");
        cmds = cles.getRegistredCommands();
        assertNotNull(cmds);
        assertEquals(2, cmds.size());
        assertTrue(cmds.contains("identify"));

        deployContrib("org.nuxeo.ecm.platform.commandline.executor",
                "OSGI-INF/commandline-imagemagic-test-contrib2.xml");
        cmds = cles.getRegistredCommands();
        assertNotNull(cmds);
        assertEquals(1, cmds.size());
        assertFalse(cmds.contains("identify"));
    }

    @Test
    public void testCmdAvailable() {
        CommandLineExecutorService cles = Framework.getLocalService(CommandLineExecutorService.class);
        assertNotNull(cles);

        /**
         * deployContrib("org.nuxeo.ecm.platform.commandline.executor","OSGI-INF/commandline-aspell-test-contribs.xml");
         * List<String> cmds = cles.getAvailableCommands(); assertNotNull(cmds); assertEquals(1, cmds.size());
         * assertTrue(cmds.contains("aspell")); deployContrib("org.nuxeo.ecm.platform.commandline.executor",
         * "OSGI-INF/commandline-imagemagic-test-contrib.xml"); cmds = cles.getAvailableCommands(); assertNotNull(cmds);
         * assertEquals(2, cmds.size()); assertTrue(cmds.contains("identify"));
         * deployContrib("org.nuxeo.ecm.platform.commandline.executor","OSGI-INF/commandline-dummy-test-contrib.xml");
         * cmds = cles.getAvailableCommands(); assertEquals(2, cmds.size());
         * assertFalse(cmds.contains("cmdThatDoNotExist")); CommandAvailability ca =
         * cles.getCommandAvailability("cmdThatDoNotExist"); assertFalse(ca.isAvailable());
         * assertNotNull(ca.getErrorMessage()); System.out.println(ca.getErrorMessage());
         * assertNotNull(ca.getInstallMessage());
         * assertTrue(ca.getInstallMessage().contains("need to install this command that does not"));
         * System.out.println(ca.getInstallMessage());
         **/
    }

    @Test
    public void testCmdExecption() {
        CommandLineExecutorService cles = Framework.getLocalService(CommandLineExecutorService.class);
        assertNotNull(cles);

        try {
            cles.execCommand("IDon'tExist", null);
            fail("No Exception has been raised");
        } catch (CommandNotAvailable e) {
            String msg = e.getErrorMessage();
            assertNotNull(msg);
        }
    }

}
