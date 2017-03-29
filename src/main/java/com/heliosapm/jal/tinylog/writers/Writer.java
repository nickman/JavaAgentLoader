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

package com.heliosapm.jal.tinylog.writers;

import java.util.Set;

import com.heliosapm.jal.tinylog.Configuration;
import com.heliosapm.jal.tinylog.LogEntry;

/**
 * Writers output created log entries from {@link com.heliosapm.jal.tinylog.Logger Logger}.
 *
 * <p>
 * The annotation {@link com.heliosapm.jal.tinylog.writers.PropertiesSupport PropertiesSupport} must be added to the implemented
 * writer class and the implemented writer must be registered as service in "META-INF/services/com.heliosapm.jal.tinylog.writers"
 * in order to make the writer available by properties files and system properties.
 * </p>
 *
 * <p>
 * Example:<br>
 * <code>
 * {@literal @}PropertiesSupport(name = "example",
 * properties = { {@literal @}Property(name = "filename", type = String.class), {@literal @}Property(name = "backups", type = int.class) })<br>
 * public final class ExampleWriter implements Writer {
 * </code>
 * </p>
 *
 * <p>
 * A writer must have a constructor that matches to the defined properties.
 * </p>
 */
public interface Writer {

	/**
	 * Get all log entry values that are required by this writer.
	 *
	 * @return Required values for log entry
	 */
	Set<LogEntryValue> getRequiredLogEntryValues();

	/**
	 * Initialize the writer (open a file for example).
	 *
	 * @param configuration
	 *            Configuration of logger
	 *
	 * @throws Exception
	 *             Failed to initialize the writer
	 */
	void init(Configuration configuration) throws Exception;

	/**
	 * Write a log entry.
	 *
	 * @param logEntry
	 *            Log entry to output
	 *
	 * @throws Exception
	 *             Failed to write the log entry
	 */
	void write(LogEntry logEntry) throws Exception;

	/**
	 * Flush this writer and force any buffered data to output.
	 *
	 * @throws Exception
	 *             Failed to flush
	 */
	void flush() throws Exception;

	/**
	 * Close the writer and release all resources. If a writer required a <code>close()</code> call, it should be
	 * registered in the <code>init()</code> method at {@link com.heliosapm.jal.tinylog.writers.VMShutdownHook VMShutdownHook} for
	 * automatically shutdown when VM will be shutdown.
	 *
	 * @throws Exception
	 *             Failed to close the writer
	 */
	void close() throws Exception;

}
