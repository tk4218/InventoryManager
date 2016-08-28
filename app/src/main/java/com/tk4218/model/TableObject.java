package com.tk4218.model;

import android.database.Cursor;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

/**
 * TableObject <br>
 * <br>
 *
 * This class is basically an extension of the Cursor Object. <br>
 * TableObject adds the ability to search data returned from a query
 * by the column name rather than by just the column index. For more
 * information on the Cursor Object, see {@link Cursor Cursor}. <br>
 * <br>
 * You can access all the methods in the Cursor Object by simply using: <br>
 * tableObject.cursor.[methodName]
 *
 * @author tk4218
 *
 */

public class TableObject {

    public static int SORT_ASCENDING = 1;
    public static int SORT_DESCENDING = 2;

    private HashMap<String, List<Object>> tableObject;
    private int rowCount;
    private int currentPosition;

    public TableObject(Cursor cursor){
        rowCount = cursor.getCount();
        tableObject = new HashMap<String, List<Object>>();
        //Create Columns in HashMap
        for(int i =0; i < cursor.getColumnCount(); i++){
            tableObject.put(cursor.getColumnName(i), new ArrayList<>());
        }
        cursor.moveToFirst();
        for(int i = 0; i < rowCount; i++){
            for(int j = 0; j < cursor.getColumnCount(); j++){
                Object item = new Object();
                switch (cursor.getType(j)){
                    case Cursor.FIELD_TYPE_FLOAT:
                        item = cursor.getFloat(j);
                        break;
                    case Cursor.FIELD_TYPE_INTEGER:
                        item = cursor.getInt(j);
                        break;
                    case Cursor.FIELD_TYPE_STRING:
                        item = cursor.getString(j);
                        break;
                    case Cursor.FIELD_TYPE_BLOB:
                        item = cursor.getBlob(j);
                        break;
                    default:
                        break;
                }
                tableObject.get(cursor.getColumnName(j)).add(i, item);
            }
            cursor.moveToNext();
        }
        currentPosition = 0;
    }

    public TableObject(HashMap<String, List<Object>> tableObject, int rowCount){
        this.tableObject = tableObject;
        this.rowCount = rowCount;
        currentPosition = 0;
    }

    public String getString(String columnName){
        return (String)tableObject.get(columnName).get(currentPosition);
    }

    public void setString(String columnName, String value){
        tableObject.get(columnName).set(currentPosition, value);
    }

    public int getInt(String columnName){
        return (int)tableObject.get(columnName).get(currentPosition);
    }

    public void setInt(String columnName, int value){
        tableObject.get(columnName).set(currentPosition, value);
    }

    public double getFloat(String columnName){
        return (float)tableObject.get(columnName).get(currentPosition);
    }

    public boolean getBln(String columnName){
        return (int)tableObject.get(columnName).get(currentPosition) == 1;
    }

    public int sum(String columnName){
        int sum = 0;
        for(int i = 0; i < tableObject.get(columnName).size(); i++){
            sum += (int)tableObject.get(columnName).get(i);
        }
        return sum;
    }
    public long countWhereMatches(String filterColumn, Object filterValue){
        long count = 0;
        for(int i = 0; i < getRowCount(); i++){
            if(tableObject.get(filterColumn).get(i) == filterValue){
                count++;
            }
        }
        return count;
    }

    public TableObject filter(String filterColumn, Object filterValue){
        Set<String> columnSet = tableObject.keySet();
        HashMap<String, List<Object>> newTableObject = new HashMap<String, List<Object>>();

        int count = 0;
        for(int i = 0; i < getRowCount(); i++){
            if(tableObject.get(filterColumn).get(i).toString().equals(filterValue.toString())){
                if(count == 0){
                    for(int j = 0; j < columnSet.toArray().length; j++){
                        newTableObject.put((String)columnSet.toArray()[j], new ArrayList<Object>());
                    }
                }
                for(int j = 0; j < columnSet.toArray().length; j++){
                    newTableObject.get(columnSet.toArray()[j]).add(count, tableObject.get(columnSet.toArray()[j]).get(i));
                }
                count++;
            }
        }
        return new TableObject(newTableObject, count);
    }

    public boolean findFirst(String filterColumn, Object filterValue){
        for(int i =0; i < getRowCount(); i++){
            if(tableObject.get(filterColumn).get(i) == filterValue){
                currentPosition = i;
                return true;
            }
        }
        return false;
    }

    public void sort(String sortColumn, int sortOrder){
        if(tableObject.get(sortColumn).get(0).getClass().equals(Integer.class)){
            sortInt(sortColumn, sortOrder);
        }else if(tableObject.get(sortColumn).get(0).getClass().equals(String.class)){
            sortString(sortColumn, sortOrder);
        }
    }

    private void sortInt(String sortColumn, int sortOrder){
        Set<String> columnSet = tableObject.keySet();
        Object temp;
        for(int i = 1; i < rowCount; i++){
            for(int j = i; j > 0; j--){
                if((sortOrder == SORT_ASCENDING && (Integer)tableObject.get(sortColumn).get(j) > (Integer)tableObject.get(sortColumn).get(j-1))
                        ||(sortOrder == SORT_DESCENDING && (Integer)tableObject.get(sortColumn).get(j) < (Integer)tableObject.get(sortColumn).get(j-1))){
                    for(int column = 0; column < columnSet.toArray().length; column++){
                        String columnName = (String)columnSet.toArray()[column];
                        temp = tableObject.get(columnName).get(j);
                        tableObject.get(columnName).set(j,tableObject.get(columnName).get(j-1));
                        tableObject.get(columnName).set(j-1, temp);
                    }
                }
            }
        }
    }

    private void sortString(String sortColumn, int sortOrder){
        Set<String> columnSet = tableObject.keySet();
        Object temp;
        for(int i = 1; i < rowCount; i++){
            for(int j = i; j > 0; j--){
                if((sortOrder == SORT_ASCENDING && tableObject.get(sortColumn).get(j).toString().compareTo(tableObject.get(sortColumn).get(j-1).toString()) < 0)
                        ||(sortOrder == SORT_DESCENDING &&tableObject.get(sortColumn).get(j).toString().compareTo(tableObject.get(sortColumn).get(j-1).toString()) > 0)){
                    for(int column = 0; column < columnSet.toArray().length; column++){
                        String columnName = (String)columnSet.toArray()[column];
                        temp = tableObject.get(columnName).get(j);
                        tableObject.get(columnName).set(j,tableObject.get(columnName).get(j-1));
                        tableObject.get(columnName).set(j-1, temp);
                    }
                }
            }
        }
    }

    public int getRowCount(){
        return rowCount;
    }
    public int getRow(){
        return currentPosition;
    }
    public void moveToPosition(int position){
        currentPosition = position;
    }
    public boolean EOF(){
        return currentPosition >= getRowCount();
    }
    public void moveFirst(){
     currentPosition = 0;
    }
    public boolean moveNext(){
        currentPosition++;
        return EOF();
    }
}
