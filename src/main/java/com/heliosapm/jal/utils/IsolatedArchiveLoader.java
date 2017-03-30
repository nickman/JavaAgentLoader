/**
Licensed to the Apache Software Foundation (ASF) under one
or more contributor license agreements.  See the NOTICE file
distributed with this work for additional information
regarding copyright ownership.  The ASF licenses this file
to you under the Apache License, Version 2.0 (the
"License"); you may not use this file except in compliance
with the License.  You may obtain a copy of the License at

  http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing,
software distributed under the License is distributed on an
"AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
KIND, either express or implied.  See the License for the
specific language governing permissions and limitations
under the License.
 */
package com.heliosapm.jal.utils;

import java.io.IOException;
import java.io.InputStream;
import java.lang.management.ManagementFactory;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Enumeration;
import java.util.List;



/**
 * <p>Title: IsolatedArchiveLoader</p>
 * <p>Description: Isolating classloader that restricts the classes it loads to those available from the passed URLs and the root system classloader.</p> 
 * <p>Company: Helios Development Group LLC</p>
 * @author Whitehead (nwhitehead AT heliosdev DOT org)
 * <p><code>com.heliosapm.utils.classload.IsolatedArchiveLoader</code></p>
 */

public class IsolatedArchiveLoader extends URLClassLoader {
	/** This classloader's parent which is delegated system class loads */
	protected static SystemOnlyClassLoader ncl;
	
	/** The debug agent library */
	public static final String AGENT_LIB = "-agentlib:";
	/** The legacy debug agent library */
	public static final String LEGACY_AGENT_LIB = "-Xrunjdwp:";
	
	
	/**
	 * Determines if this JVM is running with the debug agent enabled
	 * @return true if this JVM is running with the debug agent enabled, false otherwise
	 */
	public static boolean isDebugAgentLoaded() {
		List<String> inputArguments = ManagementFactory.getRuntimeMXBean().getInputArguments();
		for(String s: inputArguments) {
			if(s.trim().startsWith(AGENT_LIB) || s.trim().startsWith(LEGACY_AGENT_LIB)) return true;
		}
		return false;
	}
	

	/**
	 * Creates a new IsolatedArchiveLoader that restricts its classloading to the passed URLs. 
	 * @param urls The URLs this classloader will load from
	 */
	public IsolatedArchiveLoader(URL...urls)  {
		super(urls, getNullClassLoader());		
		StringBuilder b = new StringBuilder();
		if(urls!=null) {
			for(URL url: urls) {
				b.append("\n\t").append(url);
			}
		}
		System.out.println("Isolated Class Loader for URLs: [" + b + "]");
	}
	
	/**
	 * Returns the SystemOnlyClassLoader that will be an IsolatedClassLoader's parent.
	 * @return the SystemOnlyClassLoader
	 */
	private static ClassLoader getNullClassLoader()  {
		ncl = new SystemOnlyClassLoader();
		return ncl;
	}
	
	
	/**
	 * Attempts to load the named the class from the configured URLs and if not found, delegates to the parent.
	 * @param name The class name
	 * @return the loaded class
	 * @throws ClassNotFoundException
	 */
	private Class<?> loadSystemClass(String name) throws ClassNotFoundException {
		try {
			Class<?> clazz = super.findClass(name);
			if(isDebugAgentLoaded()) {
				System.out.println("IsolatedArchiveLoader [" + name + "]");
			}			
			return clazz;
		} catch (ClassNotFoundException cle) {
			return ncl.forReal(name);
		}
	}
	

	public URL getResource(String name) {
		URL url = super.getResource(name);
		if(url==null) {
			url = ncl.getRealResource(name);
		} else {
			if(isDebugAgentLoaded()) {
				System.out.println("IsolatedArchiveLoader [" + name + "]");
			}						
		}
		return url;
	}
	
	public Enumeration<URL> getResources(String name) throws IOException {
		Enumeration<URL> en = super.getResources(name);
		if(en==null) {
			en = ncl.getRealResources(name);
		} else {
			if(isDebugAgentLoaded()) {
				System.out.println("IsolatedArchiveLoader [" + name + "]");
			}			
		}
		return en;
	}
	
	public InputStream getResourceAsStream(String name) {
		InputStream is = super.getResourceAsStream(name);
		if(is==null) {
			is = ncl.getRealResourceAsStream(name);
		} else {
			if(isDebugAgentLoaded()) {
				System.out.println("IsolatedArchiveLoader [" + name + "]");
			}			
		}
		return is;
	}

	/**
	 * Attempts to load the named the class from the configured URLs and if not found, delegates to the parent.
	 * @param name The class name
	 * @param resolve true to resolve the class.
	 * @return the loaded class
	 * @throws ClassNotFoundException
	 */	
	protected Class<?> loadClass(String name, boolean resolve) throws ClassNotFoundException {
		return loadSystemClass(name);
	}
	
	/**
	 * Attempts to load the named the class from the configured URLs and if not found, delegates to the parent.
	 * @param name The class name
	 * @return the loaded class
	 * @throws ClassNotFoundException
	 */
	public Class<?> loadClass(String name) throws ClassNotFoundException {
		return loadSystemClass(name);
	}	
	
	/**
	 * Attempts to load the named the class from the configured URLs and if not found, delegates to the parent.
	 * @param name The class name
	 * @return the loaded class
	 * @throws ClassNotFoundException
	 */	
	protected Class<?> findClass(String name) throws ClassNotFoundException {
		return loadSystemClass(name);
	}
}
