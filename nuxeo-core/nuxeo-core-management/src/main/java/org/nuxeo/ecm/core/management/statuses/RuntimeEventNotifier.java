/*
 * (C) Copyright 2006-2011 Nuxeo SA (http://nuxeo.com/) and others.
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
 */

package org.nuxeo.ecm.core.management.statuses;

import org.nuxeo.runtime.api.Framework;
import org.nuxeo.runtime.services.event.Event;
import org.nuxeo.runtime.services.event.EventService;

public class RuntimeEventNotifier implements Notifier {

    public static final String RUNTIME_EVENT_TOPIC = "administrativeStatus";

    @Override
    public void notifyEvent(String eventName, String instanceIdentifier, String serviceIdentifier) {

        Event evnt = new Event(RUNTIME_EVENT_TOPIC, eventName, instanceIdentifier, serviceIdentifier);
        EventService evtService = Framework.getLocalService(EventService.class);
        evtService.sendEvent(evnt);

    }

}
