/*
 * Copyright 2014 Martin Winandy
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

package com.heliosapm.jal.tinylog;

import com.heliosapm.jal.tinylog.writers.Writer;

/**
 * Writer definition. Contains the writer and it's severity level.
 */
final class WriterDefinition {

	private final Writer writer;
	private final Level level;
	private final String formatPattern;

	/**
	 * @param writer
	 *            Writer to output all log entries
	 */
	WriterDefinition(final Writer writer) {
		this.writer = writer;
		this.level = null;
		this.formatPattern = null;
	}

	/**
	 * 
	 * @param writer
	 *            Writer to output log entries with a specified severity level.
	 * @param level
	 *            Writer will output entries with the same or higher severity level
	 */
	WriterDefinition(final Writer writer, final Level level) {
		this.writer = writer;
		this.level = level;
		this.formatPattern = null;
	}

	/**
	 * 
	 * @param writer
	 *            Writer to output log entries with a specified severity level.
	 * @param formatPattern
	 *            Format pattern for log entries
	 */
	WriterDefinition(final Writer writer, final String formatPattern) {
		this.writer = writer;
		this.level = null;
		this.formatPattern = formatPattern;
	}

	/**
	 * 
	 * @param writer
	 *            Writer to output log entries with a specified severity level.
	 * @param level
	 *            Writer will output entries with the same or higher severity level
	 * @param formatPattern
	 *            Format pattern for log entries
	 */
	WriterDefinition(final Writer writer, final Level level, final String formatPattern) {
		this.writer = writer;
		this.level = level;
		this.formatPattern = formatPattern;
	}

	/**
	 * Get the writer.
	 * 
	 * @return Writer
	 */
	Writer getWriter() {
		return writer;
	}

	/**
	 * Get the severity level.
	 * 
	 * @return Severity level
	 */
	Level getLevel() {
		return level;
	}

	/**
	 * Get the format pattern for log entries.
	 * 
	 * @return Format pattern for log entries
	 */
	String getFormatPattern() {
		return formatPattern;
	}
	
	/**
	 * Get an instance with filled severity level and format pattern.
	 * @param defaultLevel Severity level if not set
	 * @param defaultFormatPattern Format pattern if not set
	 * @return New or same writer definition
	 */
	WriterDefinition fill(final Level defaultLevel, final String defaultFormatPattern) {
		if (level == null && formatPattern == null) {
			return new WriterDefinition(writer, defaultLevel, defaultFormatPattern);
		} else if (level == null) {
			return new WriterDefinition(writer, defaultLevel, formatPattern);
		} else if (formatPattern == null) {
			return new WriterDefinition(writer, level, defaultFormatPattern);
		} else {
			return this;
		}
	}

}
