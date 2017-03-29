/*
 * Copyright 2012 Martin Winandy
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 */

package com.heliosapm.jal.tinylog.policies;

import java.io.File;
import java.io.IOException;

import com.heliosapm.jal.tinylog.Configuration;

/**
 * Policies define rollover strategies for {@link com.heliosapm.jal.tinylog.writers.RollingFileWriter RollingFileWriter}.
 *
 * <p>
 * The annotation {@link com.heliosapm.jal.tinylog.policies.PropertiesSupport PropertiesSupport} must be added to the implemented
 * policy class and the implemented policy must be registered as service in "META-INF/services/com.heliosapm.jal.tinylog.policies"
 * in order to make the policy available by properties files and system properties.
 * </p>
 *
 * <p>
 * Example:<br>
 * <code>
 * {@literal @}PropertiesSupport(name = "startup")<br>
 * public final class StartupPolicy implements Policy {
 * </code>
 * </p>
 *
 * <p>
 * A policy must have a default constructor without any parameters. Optionally it can have an additional constructor
 * with a string parameter if the policy supports parameters.
 * </p>
 */
public interface Policy {

	/**
	 * Initialize the policy.
	 *
	 * @param configuration
	 *            Configuration of logger
	 */
	void init(Configuration configuration);

	/**
	 * Determine whether a log file can be continued.
	 *
	 * @param logFile
	 *            Log file to continue
	 * @return <code>true</code> to continue the log file, <code>false</code> to trigger a rollover
	 *
	 * @throws IOException
	 *             Failed to handle log file
	 */
	boolean check(File logFile) throws IOException;

	/**
	 * Determine whether a log entry can be written into the current log file.
	 *
	 * @param logEntry
	 *            Log entry to write
	 * @return <code>true</code> to continue the current log file, <code>false</code> to trigger a rollover
	 */
	boolean check(String logEntry);

	/**
	 * The log file was rolled and reset the policy.
	 */
	void reset();

}
