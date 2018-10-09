/*
 * (C) Copyright 2006-2010 Nuxeo SA (http://nuxeo.com/) and others.
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
 *     bstefanescu
 */
package org.nuxeo.ecm.automation.test;

import java.io.IOException;

import org.nuxeo.ecm.automation.client.Session;
import org.nuxeo.ecm.automation.client.jaxrs.impl.HttpAutomationClient;
import org.nuxeo.ecm.core.test.DetectThreadDeadlocksFeature;
import org.nuxeo.ecm.webengine.test.WebEngineFeature;
import org.nuxeo.runtime.test.runner.Deploy;
import org.nuxeo.runtime.test.runner.Features;
import org.nuxeo.runtime.test.runner.FeaturesRunner;
import org.nuxeo.runtime.test.runner.SimpleFeature;

import com.google.inject.Binder;
import com.google.inject.Provider;
import com.google.inject.Scopes;

/**
 * Shortcut to deploy bundles required by automation in your test
 *
 * @since 5.7
 * @author <a href="mailto:bs@nuxeo.com">Bogdan Stefanescu</a>
 */
@Deploy({ "org.nuxeo.ecm.automation.core", "org.nuxeo.ecm.automation.io", "org.nuxeo.ecm.automation.server",
        "org.nuxeo.ecm.automation.features", "org.nuxeo.ecm.platform.query.api" })
@Features({ DetectThreadDeadlocksFeature.class, WebEngineFeature.class })
@DetectThreadDeadlocksFeature.Config(dumpAtTearDown = true)
public class EmbeddedAutomationServerFeature extends SimpleFeature {

    protected static final int HTTP_CONNECTION_TIMEOUT = 60000; // 60 seconds

    protected HttpAutomationClient client;

    protected Session session;

    @Override
    public void afterRun(FeaturesRunner runner) throws Exception {
        if (client != null) {
            client.shutdown();
            client = null;
            session = null;
        }
        super.afterRun(runner);
    }

    @Override
    public void configure(FeaturesRunner runner, Binder binder) {
        super.configure(runner, binder);
        binder.bind(HttpAutomationClient.class).toProvider(new Provider<HttpAutomationClient>() {
            @Override
            public HttpAutomationClient get() {
                if (client == null) {
                    client = getHttpAutomationClient();
                }
                return client;
            }
        }).in(Scopes.SINGLETON);
        binder.bind(Session.class).toProvider(new Provider<Session>() {
            @Override
            public Session get() {
                if (client == null) {
                    client = getHttpAutomationClient();
                }
                if (session == null) {
                    try {
                        session = client.getSession("Administrator", "Administrator");
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                }
                return session;
            }
        }).in(Scopes.SINGLETON);
    }

    protected HttpAutomationClient getHttpAutomationClient() {
        HttpAutomationClient client = new HttpAutomationClient("http://localhost:18080/automation",
                HTTP_CONNECTION_TIMEOUT);
        // Deactivate global operation registry cache to allow tests using this
        // feature in a test suite to deploy different set of operations
        client.setSharedRegistryExpirationDelay(0);
        return client;
    }

}
