/*
 * (C) Copyright 2011 Nuxeo SA (http://nuxeo.com/) and others.
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
 */
package org.nuxeo.ecm.platform.ui.web.auth.service;

import org.nuxeo.common.xmap.annotation.XNode;
import org.nuxeo.common.xmap.annotation.XObject;
import org.nuxeo.ecm.platform.ui.web.auth.NuxeoAuthenticationFilter;

@XObject("preFilter")
public class AuthPreFilterDescriptor implements Comparable<AuthPreFilterDescriptor> {

    private static final long serialVersionUID = 237654398643289764L;

    @XNode("@name")
    protected String name;

    @XNode("@enabled")
    protected boolean enabled = true;

    @XNode("@class")
    protected Class<NuxeoAuthenticationFilter> className;

    @XNode("@order")
    protected int order = 10;

    public String getName() {
        return name;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public Class<NuxeoAuthenticationFilter> getClassName() {
        return className;
    }

    public Integer getOrder() {
        return order;
    }

    @Override
    public int compareTo(AuthPreFilterDescriptor o) {
        return this.getOrder().compareTo(o.getOrder());
    }

}
