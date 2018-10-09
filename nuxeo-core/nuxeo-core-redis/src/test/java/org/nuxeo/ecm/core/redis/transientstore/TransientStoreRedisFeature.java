
package org.nuxeo.ecm.core.redis.transientstore;

import org.nuxeo.ecm.core.redis.RedisFeature;
import org.nuxeo.ecm.core.test.DefaultRepositoryInit;
import org.nuxeo.ecm.core.test.annotations.RepositoryConfig;
import org.nuxeo.runtime.test.runner.Deploy;
import org.nuxeo.runtime.test.runner.Features;
import org.nuxeo.runtime.test.runner.SimpleFeature;
import org.nuxeo.transientstore.test.TransientStoreFeature;

@Features({ TransientStoreFeature.class, RedisFeature.class })
@RepositoryConfig(init = DefaultRepositoryInit.class)
@Deploy("org.nuxeo.ecm.core.redis.tests:test-redis-transientstore-contrib.xml")
public class TransientStoreRedisFeature extends SimpleFeature {

}
