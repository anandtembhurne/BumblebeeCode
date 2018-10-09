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
 *
 * $Id$
 */

package org.nuxeo.ecm.core.query.sql.model;

import java.util.ArrayList;

/**
 * @author <a href="mailto:bs@nuxeo.com">Bogdan Stefanescu</a>
 */
public class ReferenceList extends ArrayList<Reference> implements Operand {

    private static final long serialVersionUID = -1024570813196737623L;

    @Override
    public void accept(IVisitor visitor) {
        visitor.visitReferenceList(this);
    }

    @Override
    public String toString() {
        StringBuilder buf = new StringBuilder();
        if (isEmpty()) {
            return "";
        }
        buf.append(get(0).toString());
        for (int i = 1, size = size(); i < size; i++) {
            buf.append(", ").append(get(i).toString());
        }
        return buf.toString();
    }

}
