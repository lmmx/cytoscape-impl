package org.cytoscape.tableimport.internal.reader;

/*
 * #%L
 * Cytoscape Table Import Impl (table-import-impl)
 * $Id:$
 * $HeadURL:$
 * %%
 * Copyright (C) 2006 - 2013 The Cytoscape Consortium
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as 
 * published by the Free Software Foundation, either version 2.1 of the 
 * License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Lesser Public License for more details.
 * 
 * You should have received a copy of the GNU General Lesser Public 
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/lgpl-2.1.html>.
 * #L%
 */

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import au.com.bytecode.opencsv.CSVReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.cytoscape.model.CyTable;
import org.cytoscape.tableimport.internal.util.URLUtil;

/**
 * Basic text table reader for attributes.<br>
 *
 * <p>
 * based on the given parameters, map the text table to CyAttributes.
 * </p>
 *
 * @author kono
 *
 */
public class DefaultAttributeTableReader implements TextTableReader {
	/**
	 * Lines begin with this charactor will be considered as comment lines.
	 */
	private static final int DEF_KEY_COLUMN = 0;
	private final URL source;
	private AttributeMappingParameters mapping;
	private final AttributeLineParser parser;
	private static final Logger logger = LoggerFactory.getLogger(DefaultAttributeTableReader.class);
	
	// Number of mapped attributes.
	private int globalCounter = 0;

	/*
	 * Reader will read entries from this line.
	 */
	private final int startLineNumber;
	private String commentChar = null;
	
	
	private InputStream is = null;
	
	/**
	 * Constructor.<br>
	 *
	 * @param source
	 * @param objectType
	 * @param delimiters
	 * @throws Exception
	 */
	public DefaultAttributeTableReader(final URL source, final ObjectType objectType,
	                                   final List<String> delimiters) throws Exception {
		this(source, objectType, delimiters, null, DEF_KEY_COLUMN, null, null, null, null, null, 0);
	}

	/**
	 * Creates a new DefaultAttributeTableReader object.
	 *
	 * @param source  DOCUMENT ME!
	 * @param objectType  DOCUMENT ME!
	 * @param delimiters  DOCUMENT ME!
	 * @param key  DOCUMENT ME!
	 * @param columnNames  DOCUMENT ME!
	 *
	 * @throws Exception  DOCUMENT ME!
	 */
	public DefaultAttributeTableReader(final URL source, final ObjectType objectType,
	                                   final List<String> delimiters, final int key,
	                                   final String[] columnNames) throws Exception {
		this(source, objectType, delimiters, null, DEF_KEY_COLUMN, null, null, columnNames, null,
		     null, 0);
	}

	/**
	 * Constructor with full options.<br>
	 *
	 * @param source
	 *            Source file URL (can be remote or local)
	 * @param objectType
	 * @param delimiters
	 * @param listDelimiter
	 * @param keyIndex
	 * @param mappingAttribute
	 * @param aliasIndexList
	 * @param attributeNames
	 * @param attributeTypes
	 * @param importFlag
	 * @param startLineNumber
	 * @throws Exception
	 */
	public DefaultAttributeTableReader(final URL source, final ObjectType objectType,
	                                   final List<String> delimiters, final String listDelimiter,
	                                   final int keyIndex, final String mappingAttribute,
	                                   final List<Integer> aliasIndexList,
	                                   final String[] attributeNames, final Byte[] attributeTypes,
	                                   final boolean[] importFlag, final int startLineNumber)
	    throws Exception {
		this.source = source;
		this.startLineNumber = startLineNumber;
		this.mapping = new AttributeMappingParameters( delimiters, listDelimiter,
		                                             keyIndex,
		                                            attributeNames, attributeTypes, null,
		                                           importFlag, true);
		this.parser = new AttributeLineParser(mapping);
	}

	/**
	 * Creates a new DefaultAttributeTableReader object.
	 *
	 * @param source  DOCUMENT ME!
	 * @param mapping  DOCUMENT ME!
	 * @param startLineNumber  DOCUMENT ME!
	 * @param commentChar  DOCUMENT ME!
	 */

	public DefaultAttributeTableReader(final URL source, AttributeMappingParameters mapping,
            final int startLineNumber, final String commentChar) {
		this.source = source;
		this.mapping = mapping;
		this.startLineNumber = startLineNumber;
		this.parser = new AttributeLineParser(mapping);
		this.commentChar = commentChar;
	}

	public DefaultAttributeTableReader(final URL source, AttributeMappingParameters mapping, InputStream is) {
		this.source = source;
		this.mapping = mapping;
		this.startLineNumber = mapping.getStartLineNumber();
		this.parser = new AttributeLineParser(mapping);
		this.commentChar = mapping.getCommentChar();

		this.is = is;
	}

	
	/**
	 *  DOCUMENT ME!
	 *
	 * @return  DOCUMENT ME!
	 */
	public List getColumnNames() {
		List<String> colNamesList = new ArrayList<String>();

		for (String name : mapping.getAttributeNames()) {
			colNamesList.add(name);
		}

		return colNamesList;
	}

	/**
	 * Read table from the data source.
	 */
	public void readTable(CyTable table) throws IOException {
		//InputStream is = null;

		try {
			BufferedReader bufRd = null;

			if (is == null){
				is = URLUtil.getInputStream(source);				
			}
			try {

				//This data is shared by both the OpenCSV and the old method of reading files.
				int lineCount = 0;
				bufRd = new BufferedReader(new InputStreamReader(is,Charset.forName("UTF-8").newDecoder()));
				/*
				 * Read & extract one line at a time. The line can be Tab delimited,
				 */
				final String delimiter = mapping.getDelimiterRegEx();

				//If the delimiter contains a comma, treat the file as a CSV file.
				if( delimiter.contains(TextFileDelimiters.COMMA.toString()) && mapping.getDelimiters().size() == 1 )
				{
					//Use OpenCSV.. New method...
					CSVReader reader = new CSVReader(bufRd);
					String [] rowData; //Note that rowData is roughly equivalent to "parts" in the old code.
					while ((rowData = reader.readNext()) != null)
					{
						// If key dos not exists, ignore the line.
						if(lineCount >= startLineNumber && rowData.length>=mapping.getKeyIndex()+1) {
							try {
								//if(importAll) {
								parser.parseAll(table, rowData);
								//} else
								//	parser.parseEntry(table, parts);
							} catch (Exception ex) {
								logger.warn("Couldn't parse row from OpenCSV: "+ lineCount);
							}
							globalCounter++;
						}
						lineCount++;
					}

				}
				else //Use the "old" method for splitting the lines.
				{
					String line;
					String[] parts = null;
					while ((line = bufRd.readLine()) != null) {
						/*
						 * Ignore Empty & Commnet lines.
						 */
						if ((commentChar != null) && line.startsWith(commentChar)) {
							// Do nothing
						} else if ((lineCount >= startLineNumber) && (line.trim().length() > 0)) {
							parts = line.split(delimiter);
							// If key dos not exists, ignore the line.
							if(parts.length>=mapping.getKeyIndex()+1) {
								try {
								//if(importAll) {
									parser.parseAll(table, parts);
								//} else
								//	parser.parseEntry(table, parts);
								} catch (Exception ex) {
									logger.warn("Couldn't parse row: "+ lineCount);
								}
								globalCounter++;
							}
						}

						lineCount++;
					}
				}
			}
			finally {
				if (bufRd != null) {
					bufRd.close();
				}
			}
		}
		finally {
			if (is != null) {
				is.close();
			}
		}
	}

	/**
	 *  DOCUMENT ME!
	 *
	 * @return  DOCUMENT ME!
	 */
	public String getReport() {
		final StringBuilder sb = new StringBuilder();
		final Map<String, Object> invalid = parser.getInvalidMap();
		sb.append(globalCounter + " entries are loaded and mapped into table.");
		
		if(invalid.size() > 0) {
			sb.append("\n\nThe following enties are invalid and were not imported:\n");
			int limit = 10;
			for(String key: invalid.keySet()) {
				sb.append(key + " = " + invalid.get(key) + "\n");
				if ( limit-- <= 0 ) {
					sb.append("Approximately " + (invalid.size() - 10) + 
					          " additional entries were not imported...");
					break;
				}
			}
		}
		return sb.toString();
	}
	
	public MappingParameter getMappingParameter() {
		return mapping;
	}
}
