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

package org.nuxeo.osgi.application;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import java.util.regex.Pattern;

/**
 * @author <a href="mailto:bs@nuxeo.com">Bogdan Stefanescu</a>
 */
public class TestMain {

    public static final String CONFIG_FILE = ".properties";

    public static final String BUNDLES = "bundles";

    public static final String INSTALL_DIR = "installdir";

    public static final String LIB_DIR = "libdir";

    public static final Pattern STR_LIST = Pattern.compile("\\s,\\s");

    public static void main(String[] args) throws Exception {
        CommandLineOptions cmdArgs = new CommandLineOptions(args);

        // String clear = cmdArgs.getOption("c");

        File configFile;
        String cfg = cmdArgs.getOption("f");
        if (cfg != null) {
            configFile = new File(cfg);
        } else {
            configFile = new File(CONFIG_FILE);
        }

        String installDirProp;
        String bundlesList = null;
        String libList;
        if (configFile.isFile()) {
            Properties config = new Properties();
            InputStream in = new BufferedInputStream(new FileInputStream(configFile));
            config.load(in);
            installDirProp = config.getProperty(INSTALL_DIR);
            bundlesList = config.getProperty(BUNDLES);
            libList = config.getProperty(LIB_DIR);
        } else {
            installDirProp = cmdArgs.getOption("d");
            bundlesList = cmdArgs.getOption("b");
            libList = cmdArgs.getOption("cp");
        }

        File installDir = null;
        if (installDirProp == null) {
            installDir = new File("."); // current dir
        } else {
            installDir = new File(installDirProp);
        }

        SharedClassLoader cl = new SharedClassLoaderImpl(TestMain.class.getClassLoader());
        if (libList != null) {
            String[] libs = STR_LIST.split(libList, 0);
            // loadLibs(cl, installDir, libs);
        }
    }

}
