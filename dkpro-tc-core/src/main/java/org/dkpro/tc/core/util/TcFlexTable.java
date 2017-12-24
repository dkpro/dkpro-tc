/*******************************************************************************
 * Copyright 2017
 * Ubiquitous Knowledge Processing (UKP) Lab
 * Technische Universität Darmstadt
 *   
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *   
 *   http://www.apache.org/licenses/LICENSE-2.0
 *   
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/
package org.dkpro.tc.core.util;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.PrintSetup;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.dkpro.lab.storage.StreamReader;
import org.dkpro.lab.storage.StreamWriter;

import au.com.bytecode.opencsv.CSVReader;
import au.com.bytecode.opencsv.CSVWriter;

/**
 * Conveniently create a tabular data structure which may be persisted to and read from a CSV file
 * or serialized in several other formats.
 */
public class TcFlexTable<V>
{
    private static final Object PRESENT = new Object();
    private LinkedHashMap<String, Object> columns;
    private Map<String, Map<String, V>> rows;
    private V defaultValue;
    private String formatString;
    private boolean writeSorted = true;
    private boolean compact = true;
    private Class<V> dataClass;
    private Locale locale = Locale.ENGLISH;

    {
        columns = new LinkedHashMap<String, Object>();
        rows = new LinkedHashMap<String, Map<String, V>>();
    }

    private TcFlexTable(Class<V> aDataClass)
    {
        dataClass = aDataClass;
    }

    public static <C> TcFlexTable<C> forClass(Class<C> aClass)
    {
        return new TcFlexTable<C>(aClass);
    }

    /**
     * If a cell contains no value, this value is returned when asking for the cell value.
     */
    public void setDefaultValue(V aDefaultValue)
    {
        defaultValue = aDefaultValue;
    }

    /**
     * Set the format use to render cell values. Per default this is set to {@code null} so the
     * {@link String#valueOf(Object)} method is used for rendering. If this is set, the method
     * {@link String#format(String, Object...)} is used instead.
     * 
     * @param aFormatString
     *            a format string
     * 
     * @see String#format(String, Object...)
     */
    public void setFormatString(String aFormatString)
    {
        formatString = aFormatString;
    }
    
    public Locale getLocale()
    {
        return locale;
    }

    /**
     * Setting the locale to a non-English locale may break the detection of numeric values in
     * writers.
     */
    public void setLocale(Locale aLocale)
    {
        locale = aLocale;
    }

    /**
     * Add a new row. If the row already exists, it is overwritten.
     *
     * @param aId
     *            the row ID.
     * @param aRow
     *            the row data.
     */
    public void addRow(String aId, Map<String, ? extends V> aRow)
    {
        LinkedHashMap<String, V> row = new LinkedHashMap<String, V>();
        if (aRow != null) {
            for (String key : aRow.keySet()) {
                columns.put(key, PRESENT);
            }
            row.putAll(aRow);
        }
        rows.put(aId, row);
    }

    /**
     * Append new columns to an existing row. If no row with the given ID is present, a new one is
     * created.
     *
     * @param aId
     *            the row ID.
     * @param aRow
     *            the row data.
     */
    public void addToRow(String aId, Map<String, ? extends V> aRow)
    {
        Map<String, V> row = rows.get(aId);
        if (row == null) {
            addRow(aId, aRow);
        }
        else {
            for (String key : aRow.keySet()) {
                columns.put(key, PRESENT);
            }
            row.putAll(aRow);
        }
    }

    public Map<String, V> getRow(String aId)
    {
        return rows.get(aId);
    }

    public void addColumns(String... aColumnNames)
    {
        for (String key : aColumnNames) {
            columns.put(key, PRESENT);
        }
    }

    public void setColumns(String... aColumnNames)
    {
        columns.clear();
        addColumns(aColumnNames);
    }

    public String[] getColumnIds()
    {
        Set<String> keySet = columns.keySet();
        return keySet.toArray(new String[keySet.size()]);
    }

    /**
     * Enable/disable compact rendering mode. In compact mode, invariant columns may be rendered as
     * a separate section in the output or totally omitted. This is turned on by default. To always
     * render all columns, disable this.
     */
    public void setCompact(boolean aCompact)
    {
        compact = aCompact;
    }

    public boolean isCompact()
    {
        return compact;
    }

    /**
     * Enable/disable automatic sorting of rows by ID. This is turned on by default. To render rows
     * in the order they were added to the table, disable this.
     */
    public void setSortRows(boolean aWriteSorted)
    {
        writeSorted = aWriteSorted;
    }

    protected String[] getCompactColumnIds(boolean aAllSame)
    {
        List<String> colIds = new ArrayList<String>();

        columns: for (String colId : columns.keySet()) {
            String lastValue = null;
            for (String rowId : getRowIds()) {
                String value = getValueAsString(rowId, colId);
                if (lastValue != null && !lastValue.equals(value)) {
                    // not all the same
                    if (!aAllSame) {
                        colIds.add(colId);
                    }
                    continue columns;
                }
                lastValue = value;
            }

            if (aAllSame) {
                colIds.add(colId);
            }
        }

        return colIds.toArray(new String[colIds.size()]);
    }

    public String[] getRowIds()
    {
        String[] rowIds = rows.keySet().toArray(new String[rows.size()]);
        if (writeSorted) {
            Arrays.sort(rowIds);
        }
        return rowIds;
    }

    public V getValue(String aRowId, String aColId)
    {
        Map<String, V> row = rows.get(aRowId);
        if (row == null) {
            return defaultValue;
        }
        V value = row.get(aColId);
        if (value == null) {
            return defaultValue;
        }
        return value;
    }

    public String getValueAsString(String aRowId, String aColId)
    {
        V value = getValue(aRowId, aColId);
        
        //suppress the full reader description text
        if(isReaderDescription(value.toString())){
        	int start = value.toString().indexOf("implementationName");
        	int end = value.toString().indexOf("\n", start);
        	if(start < 0|| end <0 || end >= start){
        		return "";
        	}
        	return value.toString().substring(start, end);
        }
        
        if (formatString != null) {
            return String.format(locale, formatString, value);
        }
        else {
            return String.valueOf(value);
        }
    }

    private boolean isReaderDescription(String string) {
		return string.startsWith("org.apache.uima.collection.impl.CollectionReaderDescription_impl:");
	}

	public StreamWriter getTextWriter()
    {
        return new StreamWriter()
        {
            @Override
            public void write(OutputStream aStream)
                throws Exception
            {
                String[] colIds = TcFlexTable.this.compact ? getCompactColumnIds(false)
                        : getColumnIds();
                
                // Obtain the width of the columns based on their content and headers
                // col 0 is reserved here for the rowId width
                int colWidths[] = new int[colIds.length+1];
                for (String rowId : getRowIds()) {
                    colWidths[0] = Math.max(colWidths[0], rowId.length());
                }
                for (int i = 1; i < colWidths.length; i++) {
                    colWidths[i] = Math.max(colWidths[i], colIds[i-1].length());
                    for (String rowId : getRowIds()) {
                        colWidths[i] = Math.max(colWidths[i], getValueAsString(rowId, colIds[i-1]).length());
                    }
                }

                StringBuilder separator = new StringBuilder();
                for (int w : colWidths) {
                    if (separator.length() > 0) {
                        separator.append("-+-");
                    }
                    separator.append(StringUtils.repeat("-", w));
                }
                separator.insert(0, "+-");
                separator.append("-+");

                PrintWriter writer = new PrintWriter(new OutputStreamWriter(aStream, "UTF-8"));
                
                // Render header column
                writer.println(separator);
                writer.print("| ");
                writer.print(StringUtils.repeat(" ", colWidths[0]));
                for (int i = 0; i < colIds.length; i++) {
                    writer.print(" | ");
                    // Remember: colWidth[0] is the rowId width!
                    writer.print(StringUtils.center(colIds[i], colWidths[i+1]));
                }
                writer.println(" |");
                writer.println(separator);
                
                // Render body
                for (String rowId : getRowIds()) {
                    writer.print("| ");
                    writer.print(StringUtils.rightPad(rowId, colWidths[0]));
                    for (int i = 0; i < colIds.length; i++) {
                        writer.print(" | ");
                        // Remember: colWidth[0] is the rowId width!
                        String val = getValueAsString(rowId, colIds[i]);
                        if (isDouble(val)) {
                            writer.print(StringUtils.leftPad(val, colWidths[i + 1]));
                        }
                        else {
                            writer.print(StringUtils.rightPad(val, colWidths[i + 1]));
                        }
                    }
                    writer.println(" |");
                }
                
                // Closing separator
                writer.println(separator);
             
                writer.flush();
            }
            
            private boolean isDouble(String val)
            {
                try {
                    Double.parseDouble(val);
                }
                catch (NumberFormatException ex) {
                    return false;
                }

                return true;
            }
        };
    }
    
    public StreamWriter getTWikiWriter()
    {
        return new StreamWriter()
        {
            protected PrintWriter writer;

            @Override
            public void write(OutputStream aStream)
                throws Exception
            {
                writer = new PrintWriter(new OutputStreamWriter(aStream, "UTF-8"));

                if (compact && rows.size() > 0) {
                    String firstRowId = getRowIds()[0];
                    String[] colIds = getCompactColumnIds(true);
                    for (String colId : colIds) {
                        writer.print("| *");
                        writer.print(colId.replace('|', ' '));
                        writer.print("* | ");
                        writer.print(getValueAsString(firstRowId, colId).replace('|', ' '));
                        writer.println(" |");
                    }
                    writer.println();
                    writer.println();
                }

                String[] colIds = compact ? getCompactColumnIds(false) : getColumnIds();
                String[] buf = new String[colIds.length + 1];
                {
                    int i = 1;
                    buf[0] = "ID";
                    for (String col : colIds) {
                        buf[i] = col.replace('|', ' ');
                        i++;
                    }
                }
                printHeaderRow(buf);

                for (String rowId : getRowIds()) {
                    buf[0] = rowId;
                    int i = 1;
                    for (String colId : colIds) {
                        buf[i] = getValueAsString(rowId, colId).replace('|', ' ');
                        i++;
                    }
                    printRow(buf);
                }

                writer.flush();
            }

            protected void printHeaderRow(String[] aHeaders)
            {
                writer.print("| *");
                writer.print(StringUtils.join(aHeaders, "* | *"));
                writer.println("* |");
            }

            protected void printRow(String[] aCells)
            {
                writer.print("| !");
                writer.print(StringUtils.join(aCells, " | "));
                writer.println(" |");
            }
        };
    }

    /**
     * Returns a LaTeX writer to write the FlexTable to a Latex file. (without rounding any figures)
     * 
     */
    public StreamWriter getLatexWriter()
    {
        return this.getLatexWriter(-1, -1);
    }

    /**
     * Returns a LaTeX writer to write the FlexTable to a Latex file. To get a writer for a
     * transposed version of the table, call table.transpose() prior to retrieving the writer.
     * 
     * @param decimalPlacesForDouble
     *            How many decimal places should double values have; if set to -1, the values won't
     *            be rounded.
     * @param decimalPlacesForPercentages
     *            How many decimal places should percentage values have; if set to -1, the values
     *            won't be rounded.
     */
    public StreamWriter getLatexWriter(final int decimalPlacesForDouble,
            final int decimalPlacesForPercentages)
    {
        return new StreamWriter()
        {
            @Override
            public void write(OutputStream aStream)
                throws Exception
            {
                PrintWriter writer = new PrintWriter(new OutputStreamWriter(aStream, "UTF-8"));
                writer.print("\\begin{tabular}");
                String[] colIds = getColumnIds();
                writer.print("{");
                for (int i = 0; i <= colIds.length; i++) {
                    writer.print(" l");
                }
                writer.println(" }");

                writer.print("\\small");

                String[] buf = new String[colIds.length + 1];
                {
                    int i = 1;
                    buf[0] = "ID";
                    for (String col : colIds) {
                        buf[i] = escapeForLatex(col.replace('|', ' '));
                        i++;
                    }
                }
                writer.println("\\hline");
                writer.print(StringUtils.join(buf, " & "));
                writer.println("\\\\");

                for (String rowId : getRowIds()) {
                    String rowVal = doStringReplace(rowId);
                    buf[0] = escapeForLatex(rowVal);
                    int i = 1;
                    for (String colId : colIds) {
                        String val = getValueAsString(rowId, colId);

                        val = convertNumbers(decimalPlacesForDouble, decimalPlacesForPercentages,
                                val);

                        buf[i] = escapeForLatex(val);
                        i++;
                    }
                    writer.print(StringUtils.join(buf, " & "));
                    writer.println("\\\\");
                }

                writer.println("\\hline");
                writer.println("\\end{tabular}");
                writer.flush();
            }

            private String convertNumbers(final int decimalPlacesForDouble,
                    final int decimalPlacesForPercentages, String val)
            {
                if (decimalPlacesForPercentages != -1) {
                    val = doRoundDouble(val, decimalPlacesForPercentages);
                }
                else if (decimalPlacesForDouble != -1 && isDouble(val)) {
                    val = doRoundDouble(val, decimalPlacesForDouble);
                }
                return val;
            }

            private String doStringReplace(String val)
            {
                val = val.replace("org.dkpro.tc.core.task.", "");
                val = val.replace("org.dkpro.tc.ml.", "");

                return val;
            }

            private String doRoundDouble(String val, int decimalPlaces)
            {
                double d = Double.parseDouble(val);

                int temp = (int) (d * Math.pow(10, decimalPlaces));  
                Double newD = (temp) / Math.pow(10, decimalPlaces);

                return newD.toString();
            }

            private boolean isDouble(String val)
            {
                try {
                    Double.parseDouble(val); 
                }
                catch (NumberFormatException ex) {
                    return false;
                }

                return true;
            }

            private String escapeForLatex(String val)
            {
                val = val.replace("_", "\\_");

                return val;
            }
        };
    }

    /**
     * Method to transpose the data in the table, turning columns into rows and vice versa.
     */
    public void transposeTable()
    {
        LinkedHashMap<String, Object> newColumns = new LinkedHashMap<>();
        Map<String, Map<String, V>> newRows = new LinkedHashMap<>();

        for (String columnHeader : columns.keySet()) {
            String newRowID = columnHeader;

            Map<String, V> row = new LinkedHashMap<>();
            Map<String, V> oldRow;

            for (String rowID : rows.keySet()) {
                if (!newColumns.containsKey(rowID)) {
                    newColumns.put(rowID, PRESENT);
                }

                oldRow = rows.get(rowID);
                V value = oldRow.get(newRowID);

                row.put(rowID, value);
            }

            newRows.put(newRowID, row);
        }

        columns = newColumns;
        rows = newRows;
    }

    public StreamWriter getCsvWriter()
    {
        return new StreamWriter()
        {
            @Override
            public void write(OutputStream aStream)
                throws Exception
            {
                String[] colIds = getColumnIds();

                CSVWriter writer = new CSVWriter(new OutputStreamWriter(aStream, "UTF-8"));
                String[] buf = new String[TcFlexTable.this.columns.size() + 1];
                {
                    int i = 1;
                    buf[0] = "ID";
                    for (String col : colIds) {
                        buf[i] = col;
                        i++;
                    }
                }
                writer.writeNext(buf);

                for (String rowId : getRowIds()) {
                    buf[0] = rowId;
                    int i = 1;
                    for (String colId : colIds) {
                        buf[i] = getValueAsString(rowId, colId);
                        i++;
                    }
                    writer.writeNext(buf);
                }

                writer.flush();
                writer.close();
            }
        };
    }

    public StreamReader getCsvReader()
    {
        return new StreamReader()
        {
            @Override
            public void read(InputStream aStream)
                throws IOException
            {
                try {
                    CSVReader reader = new CSVReader(new InputStreamReader(aStream, "UTF-8"));
                    String[] headers = reader.readNext();
                    Method converter = TcFlexTable.this.dataClass.getMethod("valueOf", String.class);

                    String[] data;
                    while ((data = reader.readNext()) != null) {
                        Map<String, V> row = new LinkedHashMap<String, V>();
                        for (int i = 1; i < headers.length; i++) {
                            @SuppressWarnings("unchecked")
                            V value = (V) converter.invoke(null, data[i]);
                            row.put(headers[i], value);
                        }
                        addRow(data[0], row);
                    }
                    reader.close();
                }
                catch (IOException e) {
                    throw e;
                }
                catch (NoSuchMethodException e) {
					throw new IOException("Data class "+TcFlexTable.this.dataClass.getName()+" does not have a "+
							"public static Object valueOf(String) method - unable unmarshall the "+
							"data.");
                }
                catch (Exception e) {
                    throw new IOException(e);
                }
            }
        };
    }

    public StreamWriter getExcelWriter()
    {
        return new StreamWriter()
        {
            @Override
            public void write(OutputStream aStream)
                throws Exception
            {
                String[] colIds = TcFlexTable.this.compact ? getCompactColumnIds(false)
                        : getColumnIds();

                Workbook wb = new HSSFWorkbook();
                Sheet sheet = wb.createSheet("Summary");

                PrintSetup printSetup = sheet.getPrintSetup();
                printSetup.setLandscape(true);
                sheet.setFitToPage(true);
                sheet.setHorizontallyCenter(true);

                // Header row
                {
                    Row row = sheet.createRow(0);
                    Cell rowIdCell = row.createCell(0);
                    rowIdCell.setCellValue("ID");

                    int colNum = 1;
                    for (String colId : colIds) {
                        Cell cell = row.createCell(colNum);
                        cell.setCellValue(colId);
                        colNum++;
                    }
                }

                // Body rows
                {
                    int rowNum = 1;
                    for (String rowId : getRowIds()) {
                        Row row = sheet.createRow(rowNum);
                        Cell rowIdCell = row.createCell(0);
                        rowIdCell.setCellValue(rowId);

                        int colNum = 1;
                        for (String colId : colIds) {
                            Cell cell = row.createCell(colNum);
                            String value = getValueAsString(rowId, colId);
                            try {
                                cell.setCellValue(Double.valueOf(value));
                            }
                            catch (NumberFormatException e) {
                                cell.setCellValue(value);
                            }
                            colNum++;
                        }
                        rowNum++;
                    }
                }

                wb.write(aStream);
                wb.close();
            }
        };
    }
}
