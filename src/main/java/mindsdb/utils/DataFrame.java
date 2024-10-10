package mindsdb.utils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import kong.unirest.core.json.JSONArray;

public class DataFrame {
    private Map<String, List<Object>> data;

    // Default constructor
    public DataFrame() {
        this.data = new HashMap<>();
    }

    // Constructor to initialize DataFrame from rows and columns
    public DataFrame(List<List<Object>> rows, List<String> columns) {
        this();
        for (int i = 0; i < columns.size(); i++) {
            List<Object> columnData = new ArrayList<>();
            for (List<Object> row : rows) {
                columnData.add(row.get(i));
            }
            this.data.put(columns.get(i), columnData);
        }
    }

    // Constructor to initialize DataFrame from JSON arrays
    public DataFrame(JSONArray columnsJSON, JSONArray rowsJSON) {
        this(convertToListOfLists(rowsJSON), convertToList(columnsJSON));
    }

    // Add column to DataFrame
    public void addColumn(String columnName, List<Object> columnData) {
        data.put(columnName, columnData);
    }

    // Get column data
    public List<Object> getColumn(String columnName) {
        return data.get(columnName);
    }

    // Get all column names
    public List<String> getColumnNames() {
        return new ArrayList<>(data.keySet());
    }

    // Get row count
    public int getRowCount() {
        if (data.isEmpty()) {
            return 0;
        }
        return data.values().iterator().next().size();
    }

    // Get all rows
    public List<Map<String, Object>> getRows() {
        List<Map<String, Object>> rows = new ArrayList<>();
        for (int i = 0; i < getRowCount(); i++) {
            rows.add(getRowMap(i));
        }
        return rows;
    }

    // Get a single row by index
    public Map<String, Object> getRowMap(int index) {
        Map<String, Object> row = new HashMap<>();
        for (String columnName : getColumnNames()) {
            row.put(columnName, data.get(columnName).get(index));
        }
        return row;
    }

    // Pretty print the DataFrame
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(getColumnNames()).append("\n");
        for (int i = 0; i < getRowCount(); i++) {
            sb.append(getRowMap(i)).append("\n");
        }
        return sb.toString();
    }

    // Helper method to convert JSONArray to List of Lists
    private static List<List<Object>> convertToListOfLists(JSONArray jsonArray) {
        List<List<Object>> listOfLists = new ArrayList<>();
        for (int i = 0; i < jsonArray.length(); i++) {
            JSONArray innerArray = jsonArray.getJSONArray(i);
            List<Object> row = new ArrayList<>();
            for (int j = 0; j < innerArray.length(); j++) {
                row.add(innerArray.get(j));
            }
            listOfLists.add(row);
        }
        return listOfLists;
    }

    // Helper method to convert JSONArray to List
    private static List<String> convertToList(JSONArray jsonArray) {
        List<String> list = new ArrayList<>();
        for (int i = 0; i < jsonArray.length(); i++) {
            list.add(jsonArray.getString(i));
        }
        return list;
    }
}