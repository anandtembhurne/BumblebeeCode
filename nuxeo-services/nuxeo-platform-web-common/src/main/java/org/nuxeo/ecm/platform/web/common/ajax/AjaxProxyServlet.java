/*
 * (C) Copyright 2006-2009 Nuxeo SA (http://nuxeo.com/) and others.
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

package org.nuxeo.ecm.platform.web.common.ajax;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import javax.servlet.ServletException;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpMethod;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.methods.InputStreamRequestEntity;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.methods.PutMethod;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.nuxeo.ecm.platform.ui.web.cache.SimpleCacheFilter;
import org.nuxeo.ecm.platform.web.common.ajax.service.AjaxProxyComponent;
import org.nuxeo.ecm.platform.web.common.ajax.service.AjaxProxyService;
import org.nuxeo.ecm.platform.web.common.ajax.service.ProxyURLConfigEntry;
import org.nuxeo.ecm.platform.web.common.requestcontroller.service.LRUCachingMap;
import org.nuxeo.runtime.api.Framework;

/**
 * Simple proxy servlets.
 * <p>
 * Used for Ajax requests that needs to be proxied to avoid XSiteScripting issues.
 * <p>
 * In order to avoid "open proxiying", only urls configured in the {@link AjaxProxyComponent} via the extension point
 * "proxyableURL" can be proxied.
 *
 * @author tiry
 */
public class AjaxProxyServlet extends HttpServlet {

    public static final String X_METHOD_HEADER = "X-Requested-Method";

    protected static AjaxProxyService service;

    protected static Map<String, String> requestsCache = new LRUCachingMap<String, String>(250);

    protected static final ReentrantReadWriteLock cacheLock = new ReentrantReadWriteLock();

    private static final long serialVersionUID = 1L;

    private static final Log log = LogFactory.getLog(AjaxProxyServlet.class);

    protected static AjaxProxyService getService() {
        if (service == null) {
            service = Framework.getLocalService(AjaxProxyService.class);
        }
        return service;
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        handleProxy(req.getMethod(), req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        handleProxy(req.getHeader(X_METHOD_HEADER), req, resp);
    }

    @Override
    protected void doPut(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        handleProxy(req.getMethod(), req, resp);
    }

    protected static void handleProxy(String method, HttpServletRequest req, HttpServletResponse resp)
            throws IOException {
        // fetch parameters
        String requestType = req.getParameter("type");
        if (requestType == null) {
            requestType = "text";
        }
        String targetURL = req.getParameter("url");
        if (targetURL == null) {
            return;
        }
        String cache = req.getParameter("cache");

        ProxyURLConfigEntry entry = getService().getConfigForURL(targetURL);
        if (entry == null || !entry.isGranted()) {
            resp.sendError(HttpServletResponse.SC_FORBIDDEN);
            log.warn("client requested proxying for unauthorized url " + targetURL);
            return;
        }

        String body = null;
        String cacheKey = targetURL;

        if (entry.useCache()) {
            if (entry.useCache()) {
                cacheKey += getSessionId(req);
            }
            try {
                cacheLock.readLock().lock();
                body = requestsCache.get(cacheKey);
            } finally {
                cacheLock.readLock().unlock();
            }
        }

        boolean foundInCache = true;
        if (body == null) {
            foundInCache = false;
            body = doRequest(method, targetURL, req);
        }

        if (!foundInCache && entry.useCache()) {
            try {
                cacheLock.writeLock().lock();
                requestsCache.put(cacheKey, body);
            } finally {
                cacheLock.writeLock().unlock();
            }
        }

        if (requestType.equals("text")) {
            resp.setContentType("text/plain");
        } else if (requestType.equals("xml")) {
            resp.setContentType("text/xml");
        }

        if (cache != null) {
            SimpleCacheFilter.addCacheHeader(resp, cache);
        }

        resp.getWriter().write(body);
        resp.setStatus(HttpServletResponse.SC_OK);
    }

    protected static String getSessionId(HttpServletRequest req) {
        String jSessionId = null;
        for (Cookie cookie : req.getCookies()) {
            if ("JSESSIONID".equalsIgnoreCase(cookie.getName())) {
                jSessionId = cookie.getValue();
                break;
            }
        }
        return jSessionId;
    }

    protected static String doRequest(String method, String targetURL, HttpServletRequest req) throws IOException {
        HttpClient client = new HttpClient();
        HttpMethod httpMethod;

        if ("GET".equals(method)) {
            httpMethod = new GetMethod(targetURL);
        } else if ("POST".equals(method)) {
            httpMethod = new PostMethod(targetURL);
            ((PostMethod) httpMethod).setRequestEntity(new InputStreamRequestEntity(req.getInputStream()));
        } else if ("PUT".equals(method)) {
            httpMethod = new PutMethod(targetURL);
            ((PutMethod) httpMethod).setRequestEntity(new InputStreamRequestEntity(req.getInputStream()));
        } else {
            throw new IllegalStateException("Unknown HTTP method: " + method);
        }

        Map<String, String[]> params = req.getParameterMap();
        for (String paramName : params.keySet()) {
            httpMethod.getParams().setParameter(paramName, params.get(paramName));
        }

        client.executeMethod(httpMethod);
        String body = httpMethod.getResponseBodyAsString();
        httpMethod.releaseConnection();
        return body;
    }

}
