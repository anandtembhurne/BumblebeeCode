/*
 * (C) Copyright 2015 Nuxeo SA (http://nuxeo.com/) and others.
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
 *     Thierry Delprat <tdelprat@nuxeo.com>
 */
package org.nuxeo.elasticsearch.seqgen;

import java.util.NoSuchElementException;

import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.client.Client;
import org.nuxeo.ecm.core.api.NuxeoException;
import org.nuxeo.ecm.core.uidgen.AbstractUIDSequencer;
import org.nuxeo.ecm.core.uidgen.UIDSequencer;
import org.nuxeo.elasticsearch.ElasticSearchConstants;
import org.nuxeo.elasticsearch.api.ElasticSearchAdmin;
import org.nuxeo.runtime.api.Framework;

/**
 * Elasticsearch implementation of {@link UIDSequencer}.
 * <p>
 * Since elasticsearch does not seem to support a notion of native sequence, the implementation uses the auto-increment
 * of the version attribute as described in the <a href=
 * "http://blogs.perl.org/users/clinton_gormley/2011/10/elasticsearchsequence---a-blazing-fast-ticket-server.html"
 * >ElasticSearch::Sequence - a blazing fast ticket server</a> blog post.
 *
 * @since 7.3
 */
public class ESUIDSequencer extends AbstractUIDSequencer {

    protected Client esClient = null;

    protected Client getClient() {
        if (esClient == null) {
            ElasticSearchAdmin esa = Framework.getService(ElasticSearchAdmin.class);
            esClient = esa.getClient();
            try {
                ensureESIndex(esClient);
            } catch (NoSuchElementException | NuxeoException e) {
                esClient = null;
                throw e;
            }
        }
        return esClient;
    }

    @Override
    public void dispose() {
        if (esClient != null) {
            esClient.close();
        }
    }

    @Override
    public long getNextLong(String sequenceName) {
        String source = "{ \"ts\" : " + System.currentTimeMillis() + "}";
        IndexResponse res = getClient().prepareIndex(getESIndexName(), ElasticSearchConstants.SEQ_ID_TYPE, sequenceName).setSource(
                source).execute().actionGet();
        return res.getVersion();
    }

    @Override
    public int getNext(String sequenceName) {
        return (int) getNextLong(sequenceName);
    }

    @Override
    public void init() {
        getClient();
    }

    protected void ensureESIndex(Client esClient) {
        boolean indexExists = esClient.admin().indices().prepareExists(getESIndexName()).execute().actionGet().isExists();
        if (!indexExists) {
            throw new NuxeoException(String.format(
                    "Sequencer %s needs an elasticSearchIndex contribution with type %s", getName(),
                    ElasticSearchConstants.SEQ_ID_TYPE));
        }
    }

    protected String getESIndexName() {
        ElasticSearchAdmin esa = Framework.getService(ElasticSearchAdmin.class);
        return esa.getIndexNameForType(ElasticSearchConstants.SEQ_ID_TYPE);
    }

}
