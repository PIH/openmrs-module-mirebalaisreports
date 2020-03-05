/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.mirebalaisreports.etl;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Copied out of the reporting module SqlRunner class, just to avoid that dependency for now.
 * Can depend on that in the future if helpful
 */
public class StatementParser {

	private static Log log = LogFactory.getLog(StatementParser.class);

	// Regular expression to identify a change in the delimiter.  This ignores spaces, allows delimiter in comment, allows an equals-sign
    private static final Pattern DELIMITER_PATTERN = Pattern.compile("^\\s*(--)?\\s*delimiter\\s*=?\\s*([^\\s]+)+\\s*.*$", Pattern.CASE_INSENSITIVE);

    /**
     * @return a List of statements that are parsed out of the passed sql, ignoring comments, and respecting delimiter assignment
     */
    public static List<String> parseSqlIntoStatements(String sql, String currentDelimiter) {
	    List<String> statements = new ArrayList<String>();
	    StringBuilder currentStatement = new StringBuilder();

	    boolean inMultiLineComment = false;

	    for (String line : sql.split("\\r?\\n")) {

	        // First, trim the line and remove any trailing comments in the form of "statement;  -- Comments here"
	        int delimiterIndex = line.indexOf(currentDelimiter);
	        int dashCommentIndex = line.indexOf("--");
	        if (delimiterIndex > 0 && delimiterIndex < dashCommentIndex) {
                line = line.substring(0, dashCommentIndex);
            }
            line = line.trim();

	        // Check to see if this line is within a multi-line comment, or if it ends a multi-line comment
	        if (inMultiLineComment) {
	            if (isEndOfMultiLineComment(line)) {
	                inMultiLineComment = false;
                }
            }
            // If we are not within a multi-line comment, then process the line, if it is not a single line comment or empty space
            else {
                if (!isEmptyLine(line) && !isSingleLineComment(line)) {

                    // If this line starts a multi-line comment, then ignore it and mark for next iteration
                    if (isStartOfMultiLineComment(line)) {
                        inMultiLineComment = true;
                    }
                    else {
                        // If the line is serving to set a new delimiter, set it and continue
                        String newDelimiter = getNewDelimiter(line);
                        if (newDelimiter != null) {
                            currentDelimiter = newDelimiter;
                        }
                        else {
                            // If we are here, that means that this line is part of an actual sql statement
                            if (line.endsWith(currentDelimiter)) {
                                line = line.substring(0, line.lastIndexOf(currentDelimiter));
                                currentStatement.append(line);
                                statements.add(currentStatement.toString());
                                currentStatement = new StringBuilder();
                            }
                            else {
                                currentStatement.append(line).append("\n");
                            }
                        }
                    }
                }
            }
        }
        if (currentStatement.length() > 0) {
            statements.add(currentStatement.toString());
        }
        return statements;
    }

    //********** CONVENIENCE METHODS **************

    protected static boolean isEmptyLine(String line) {
        return line == null || StringUtils.isBlank(line);
    }

    protected static boolean isSingleLineComment(String line) {
        return line.startsWith("--") || line.startsWith("//") || (isStartOfMultiLineComment(line) && isEndOfMultiLineComment(line));
    }

    protected static boolean isStartOfMultiLineComment(String line) {
        return line.startsWith("/*");
    }

    protected static boolean isEndOfMultiLineComment(String line) {
        return line.endsWith("*/");
    }

    protected static String getNewDelimiter(String line) {
        Matcher matcher = DELIMITER_PATTERN.matcher(line);
        if (matcher.matches()) {
            return matcher.group(2);
        }
        return null;
    }
}
