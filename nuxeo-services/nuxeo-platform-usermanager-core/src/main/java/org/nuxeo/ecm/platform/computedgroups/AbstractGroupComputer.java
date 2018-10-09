/*
 * (C) Copyright 2006-2007 Nuxeo SA (http://nuxeo.com/) and others.
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
 *     Thierry Delprat
 * *
 */

package org.nuxeo.ecm.platform.computedgroups;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.nuxeo.ecm.platform.usermanager.UserManager;
import org.nuxeo.runtime.api.Framework;

/**
 * Base class for {@link GroupComputer} implementation. Provides a naive implementation for searchGroups method.
 *
 * @author Thierry Delprat
 */
public abstract class AbstractGroupComputer implements GroupComputer {

    protected UserManager getUM() {
        return Framework.getLocalService(UserManager.class);
    }

    /**
     * Default implementation that searches on all ids for a match.
     */
    public List<String> searchGroups(Map<String, Serializable> filter, Set<String> fulltext) {

        List<String> result = new ArrayList<String>();
        String grpName = (String) filter.get(getUM().getGroupIdField());
        if (grpName != null) {
            List<String> allGroupIds = getAllGroupIds();
            if (allGroupIds != null) {
                for (String vGroupName : allGroupIds) {
                    if (vGroupName.startsWith(grpName)) {
                        if (!result.contains(vGroupName)) {
                            result.add(vGroupName);
                        }
                    }
                }
            }
        }
        return result;
    }

    /**
     * Default implementation that returns true if method {@link GroupComputer#getAllGroupIds()} contains given group
     * name.
     */
    public boolean hasGroup(String name) {
        List<String> allGroupIds = getAllGroupIds();
        if (allGroupIds != null) {
            return allGroupIds.contains(name);
        }
        return false;
    }

}
