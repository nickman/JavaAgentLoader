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
package com.heliosapm.shorthand.attach.agent.installer;

import java.io.File;
import java.io.InputStream;
import java.lang.instrument.Instrumentation;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.management.ObjectName;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import com.heliosapm.utils.classload.IsolatedClassLoader;
import com.heliosapm.utils.jar.JarBuilder;
import com.heliosapm.utils.jar.ResourceMerger;


/**
 * <p>Title: GenericAgentInstaller</p>
 * <p>Description: The executable component of a stub agent, installed to assist in boot-strapping another agent
 * without loading it into the boot class path.</p> 
 * <p>Company: Helios Development Group LLC</p>
 * @author Whitehead (nwhitehead AT heliosdev DOT org)
 * <p><code>com.heliosapm.shorthand.attach.agent.installer.GenericAgentInstaller</code></p>
 */
public class GenericAgentInstaller {
	/** Static instance logger */
	private static final Logger log = Logger.getLogger(GenericAgentInstaller.class.getName());
	
	public static final String AGENT_FILE_ARG = "javaAgentJar";
	public static final String SHARED_CLASS_SPEC = "META-INF/shared-classes.xml";
	/*
	 * read real-agent jar location from agent args
	 * create isolated class loader for real-agent
	 * create MLET for class loader
	 * read agent class name from real-agent jar manifest
	 * create  real-agent instance and initialize with agent commands (not including real agent jar location)
	 * Shared interfaces (both in boot and isolated)
	 * 
	 * Shared Classes Spec:  (XML)
	 * <shared spec="">
	 * 	<resource name="" classLoaderRef=""/>
	 * 	<merger class=""/>
	 * </shared>
	 */
	
	
	/**
	 * Creates a new GenericAgentInstaller
	 */
	private GenericAgentInstaller() {
		// No Op 
	}
	
	/**
	 * The agent premain
	 * @param agentArgs The agent argument string
	 * @param inst The instrumentation
	 */
	public static void premain(final String agentArgs, final Instrumentation inst) {
		try {
			// TODO
		} catch (Throwable ex) {
			ex.printStackTrace(System.err);
		}
	}

	/**
	 * The agent premain with no instrumentation
	 * @param agentArgs The agent argument string
	 */
	public static void premain(final String agentArgs) {
		premain(agentArgs, null);
	}
	
	/**
	 * The agent main
	 * @param agentArgs The agent argument string
	 * @param inst The instrumentation
	 */
	public static void agentmain(final String agentArgs, final Instrumentation inst) {
		premain(agentArgs, inst);
	}

	/**
	 * The agent main with no instrumentation
	 * @param agentArgs The agent argument string
	 */
	public static void agentmain(final String agentArgs) {
		premain(agentArgs, null);
	}
	

//	 * Shared Classes Spec:  (XML)
//	 * <shared spec="" jarprefix="">
//	 * 	<resource name="" recurse="true/false"/>
//	 * 	<merger class=""/>
//	 * </shared>
	
	
	public static IsolatedClassLoader isolateActualAgentJar(final URL url) {
		return new IsolatedClassLoader(
				objectName(new StringBuilder("com.heliosapm.classloader:service=IsolatedClassLoader,jar=").append(url.getFile())), 
				url);		
	}
	
	public static File generateSharedClassJar(final ClassLoader classLoader) {
		InputStream specInputStream = null;
		try {
			specInputStream = classLoader.getResourceAsStream(SHARED_CLASS_SPEC);
			if(specInputStream!=null) {				
				final Node rootNode = parseXML(specInputStream);
				final String spec = getAttributeValueByName(rootNode, "spec");
				final String jarPrefix = getAttributeValueByName(rootNode, "jarprefix");
				final List<String[]> resources = new ArrayList<String[]>();
				final List<String> mergers = new ArrayList<String>();
				for(Node rNode: getChildNodesByName(rootNode, "resource", false)) {
					resources.add(new String[]{getAttributeValueByName(rNode, "name"), "" + getAttributeBoolByName(rNode, "recurse")});
				}
				for(Node mNode: getChildNodesByName(rootNode, "merger", false)) {
					mergers.add(getAttributeValueByName(mNode, "class"));
				}
				JarBuilder jb = new JarBuilder(File.createTempFile(jarPrefix + "-stub-", ".jar"), true)
						.manifestBuilder()
						.specTitle(spec==null ? ("Shared Class Stub Jar [" + jarPrefix + "]") : spec)
						.done();
				for(String[] rez: resources) {
					final boolean recurse = Boolean.parseBoolean(rez[1]);
					jb.res(rez[0]).classLoader(classLoader).recurse(recurse).apply();
				}
				for(String merg: mergers) {
					jb.addResourceMerger((ResourceMerger)Class.forName(merg, true, classLoader).newInstance());
				}
				File f = jb.build();
				log.log(Level.FINER, "Stub Jar:" + f);
				return f;
			}
			return null;
		} catch (Exception ex) {
			throw new RuntimeException("Failed to create agent stub jar", ex);
		}
	}
	
	/**
	 * Creates a JMX ObjectName
	 * @param name The string to build the ObjectName from
	 * @return the built ObjectName
	 */
	public static ObjectName objectName(final CharSequence name) {
		try {
			return new ObjectName(name.toString().trim());
		} catch (Exception ex) {
			throw new RuntimeException("Failed to create ObjectName from [" + name + "]", ex);
		}
	}
	
	/**
	 * Parses an input source and generates an XML document.
	 * @param istr An XML source input stream
	 * @return An XML doucument.
	 */
	public static Node parseXML(final InputStream istr) {
		InputSource is = null;
		Document doc = null;
		try {		 
		  is = new InputSource(istr);
		  DocumentBuilder documentBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
		  doc = documentBuilder.parse(is);		  
		  return doc.getDocumentElement();
	  } catch (Exception e) {
		  log.log(Level.SEVERE, "Failed to parse XML source", e);
		  return null;
	  } finally {
		  if(istr!=null) try { istr.close(); } catch (Exception x) {/* No Op */}
	  }
	}

	  /**
	   * Helper Method. Searches through the child nodes of an element and returns an array of the matching nodes.
	   * @param element Element
	   * @param name String
	   * @param caseSensitive boolean
	   * @return ArrayList
	   */
	  public static List<Node> getChildNodesByName(final Node element, final String name, final boolean caseSensitive) {
	    ArrayList<Node> nodes = new ArrayList<Node>();
	    NodeList list = element.getChildNodes();
	    for (int i = 0; i < list.getLength(); i++) {
	      Node node = list.item(i);
	      if (caseSensitive) {
	        if (node.getNodeName().equals(name)) nodes.add(node);
	      }
	      else {
	        if (node.getNodeName().equalsIgnoreCase(name)) nodes.add(node);
	      }
	    }
	    return nodes;
	  }
	  
	  /**
	   * Searches throgh the passed NamedNodeMap for an attribute and returns it if it is found.
	   * If it is not found, returns a null.
	   * @param node The node to read the attribute from
	   * @param name String
	   * @return String
	   */
	  public static String getAttributeValueByName(final Node node, final String name) {
		  final NamedNodeMap nnm = node.getAttributes();
		  if(nnm==null) return null;
		  for(int i = 0; i < nnm.getLength(); i++) {
			  Attr attr = (Attr)nnm.item(i);
			  if(attr.getName().equalsIgnoreCase(name)) {
				  return attr.getValue();
			  }
		  }
		  return null;
	  }
	  
    /**
     * Returns the value of a named node attribute in the form of a boolean
	 * @param node The node to retrieve the attribute from
	 * @param name The name of the attribute
	 * @return true or false  (defaults to false if node is not found
	 */
	  public static boolean getAttributeBoolByName(final Node node, final String name) {
		  final String val = getAttributeValueByName(node, name);
		  return val==null ? false : val.trim().toLowerCase().equals("true");
	  }
	  
	  
	
}
