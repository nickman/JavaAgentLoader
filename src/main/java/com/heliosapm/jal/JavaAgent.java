/**
HeliosAPM JavaAgentLoader

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
package com.heliosapm.jal;

import java.lang.instrument.Instrumentation;

/**
 * <p>Title: JavaAgent</p>
 * <p>Description: </p> 
 * <p>Company: Helios Development Group LLC</p>
 * @author Whitehead (nwhitehead AT heliosdev DOT org)
 * <p><code>com.heliosapm.jal.JavaAgent</code></p>
 * <p>Tasks:<ol>
 * 	<li>Agent arguments/options:<ul>
 * 		<li><b>--agent</b>: in the form AGENT-JAR (url) SPACE AGENT-ARGS (1 string)</li>
 * 		<li></li>
 * 		<li></li>
 * 		<li></li>
 * 		<li></li>
 *  </li>
 *  <li>For each agent jar:<ol>
 *  	<li>Read URL into ByteBuffer</li>
 *  	<li>Extract manifest</li>
 *  	<li>Find Premain-Class, or Agent-Class class name</li>
 *  </ol></li>
 *  <li></li>
 *  <li></li>
 *  <li></li>
 *  <li></li>
 *  <li></li>
 *  
 * </ol></p>
 */

public class JavaAgent {
	/** The provided instrumentation instance */
	public static Instrumentation INSTRUMENTATION = null;
	
	/**
	 * The agent premain
	 * @param agentArgs The agent argument string
	 * @param inst The instrumentation
	 */
	public static void premain(final String agentArgs, final Instrumentation inst) {
		INSTRUMENTATION = inst;
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
	

}
