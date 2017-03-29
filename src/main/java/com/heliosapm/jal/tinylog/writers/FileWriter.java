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

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.EnumSet;
import java.util.Set;

import com.heliosapm.jal.tinylog.Configuration;
import com.heliosapm.jal.tinylog.EnvironmentHelper;
import com.heliosapm.jal.tinylog.LogEntry;

/**
 * Writes log entries to a file.
 */
@PropertiesSupport(name = "file", properties = { @Property(name = "filename", type = String.class),
		@Property(name = "buffered", type = boolean.class, optional = true), @Property(name = "append", type = boolean.class, optional = true) })
public final class FileWriter implements Writer {

	private static final int BUFFER_SIZE = 64 * 1024;

	private final String filename;
	private final boolean buffered;
	private final boolean append;
	private OutputStream stream;

	/**
	 * @param filename
	 *            Filename of the log file
	 */
	public FileWriter(final String filename) {
		this(filename, false, false);
	}

	/**
	 * @param filename
	 *            Filename of the log file
	 * @param buffered
	 *            Buffered writing
	 */
	public FileWriter(final String filename, final boolean buffered) {
		this(filename, buffered, false);
	}
	
	/**
	 * @param filename
	 *            Filename of the log file
	 * @param buffered
	 *            Buffered writing
	 * @param append
	 *            Continuing existing file
	 */
	public FileWriter(final String filename, final boolean buffered, final boolean append) {
		this.filename = PathResolver.resolve(filename);
		this.buffered = buffered;
		this.append = append;
	}
	
	/**
	 * Helper constructor with wrapper class parameters for {@link com.heliosapm.jal.tinylog.PropertiesLoader PropertiesLoader}.
	 * 
	 * @param filename
	 *            Filename of the log file
	 * @param buffered
	 *            Buffered writing
	 * @param append
	 *            Continuing existing file
	 */
	FileWriter(final String filename, final Boolean buffered, final Boolean append) {
		this.filename = filename;
		this.buffered = buffered == null ? false : buffered;
		this.append = append == null ? false : append;
	}

	@Override
	public Set<LogEntryValue> getRequiredLogEntryValues() {
		return EnumSet.of(LogEntryValue.RENDERED_LOG_ENTRY);
	}

	/**
	 * Get the filename of the log file.
	 *
	 * @return Filename of the log file
	 */
	public String getFilename() {
		return filename;
	}

	/**
	 * Determine whether buffered writing is enabled.
	 *
	 * @return <code>true</code> if buffered writing is enabled, otherwise <code>false</code>
	 */
	public boolean isBuffered() {
		return buffered;
	}
	
	/**
	 * Determine whether appending is enabled.
	 *
	 * @return <code>true</code> if appending is enabled, otherwise <code>false</code>
	 */
	public boolean isAppending() {
		return append;
	}

	@Override
	public void init(final Configuration configuration) throws IOException {
		File file = new File(filename);
		EnvironmentHelper.makeDirectories(file);

		if (buffered) {
			stream = new BufferedOutputStream(new FileOutputStream(file, append), BUFFER_SIZE);
		} else {
			stream = new FileOutputStream(file, append);
		}

		VMShutdownHook.register(this);
	}

	@Override
	public void write(final LogEntry logEntry) throws IOException {
		byte[] data = logEntry.getRenderedLogEntry().getBytes();
		synchronized (stream) {
			stream.write(data);
		}
	}

	@Override
	public void flush() throws IOException {
		if (buffered) {
			synchronized (stream) {
				stream.flush();
			}
		}
	}

	/**
	 * Close the log file.
	 *
	 * @throws IOException
	 *             Failed to close the log file
	 */
	@Override
	public void close() throws IOException {
		synchronized (stream) {
			VMShutdownHook.unregister(this);
			stream.close();
		}
	}

}
