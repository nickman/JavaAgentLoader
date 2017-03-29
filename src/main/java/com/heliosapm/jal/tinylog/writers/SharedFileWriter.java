/*
 * Copyright 2013 Martin Winandy
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

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;
import java.util.EnumSet;
import java.util.Set;

import com.heliosapm.jal.tinylog.Configuration;
import com.heliosapm.jal.tinylog.EnvironmentHelper;
import com.heliosapm.jal.tinylog.InternalLogger;
import com.heliosapm.jal.tinylog.LogEntry;

/**
 * Writes log entries to a shared file.
 *
 * Multiple instances of a program are allowed to log into the same file.
 */
@PropertiesSupport(name = "sharedfile", properties = { @Property(name = "filename", type = String.class),
		@Property(name = "append", type = boolean.class, optional = true) })
public final class SharedFileWriter implements Writer {

	private final File file;
	private final boolean append;
	private final Object mutex;
	private FileOutputStream stream;

	/**
	 * @param filename
	 *            Filename of the log file
	 */
	public SharedFileWriter(final String filename) {
		this(filename, false);
	}
	
	/**
	 * @param filename
	 *            Filename of the log file
	 * @param append
	 *            Continuing existing file
	 */
	public SharedFileWriter(final String filename, final boolean append) {
		this.file = new File(PathResolver.resolve(filename));
		this.mutex = new Object();
		
		if (append || EnvironmentHelper.isWindows()) {
			this.append = append;
		} else {
			InternalLogger.warn("Shared file writer supports starting new log files only on Windows. Therefore \"append\" will be set automatically to \"true\".");
			this.append = true;
		}
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
		return file.getAbsolutePath();
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
		if (file.isFile()) {
			if (!append) {
				file.delete();
			}
		} else {
			EnvironmentHelper.makeDirectories(file);
		}

		stream = new FileOutputStream(file, true);
		VMShutdownHook.register(this);
	}

	@Override
	public void write(final LogEntry logEntry) throws IOException {
		FileChannel channel = stream.getChannel();
		byte[] data = logEntry.getRenderedLogEntry().getBytes();

		synchronized (mutex) {
			FileLock lock = channel.lock();
			try {
				stream.write(data);
			} finally {
				lock.release();
			}
		}
	}

	@Override
	public void flush() {
		// Do nothing
	}

	/**
	 * Close the log file.
	 *
	 * @throws IOException
	 *             Failed to close the log file
	 */
	@Override
	public void close() throws IOException {
		synchronized (mutex) {
			VMShutdownHook.unregister(this);
			stream.close();
		}
	}

}
