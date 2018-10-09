/*
 * (C) Copyright 2016 Nuxeo SA (http://nuxeo.com/) and others.
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
 *     Vladimir Pasquier <vpasquier@nuxeo.com>
 */
package org.nuxeo.ecm.collections.core.automation;

import org.nuxeo.ecm.automation.OperationException;
import org.nuxeo.ecm.automation.core.Constants;
import org.nuxeo.ecm.automation.core.annotations.Context;
import org.nuxeo.ecm.automation.core.annotations.Operation;
import org.nuxeo.ecm.automation.core.annotations.OperationMethod;
import org.nuxeo.ecm.collections.api.FavoritesManager;
import org.nuxeo.ecm.core.api.CoreSession;
import org.nuxeo.ecm.core.api.DocumentModel;

/**
 * Fetch the favorites document root collection.
 *
 * @since 8.3
 */
@Operation(id = FetchFavorites.ID, category = Constants.CAT_DOCUMENT, label = "Fetch favorites root collection", description = "Fetch the favorites document root collection.")
public class FetchFavorites {

    public static final String ID = "Favorite.Fetch";

    @Context
    protected CoreSession session;

    @Context
    protected FavoritesManager favoritesManager;

    @OperationMethod
    public DocumentModel run() throws OperationException {
        return favoritesManager.getFavorites(session.getRootDocument(), session);
    }
}
