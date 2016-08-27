package com.tk4218.model;

/**
 * Created by Tk4218 on 6/18/2016.
 */
/**
 *  DBCommunicator Interface <br>
 *
 *  This interface allows for the DataSource created in the Activity to be
 *  used by the Fragments. See {@link DataSource DataSource} for more details.
 * @author tk4218
 *
 */
public interface DBCommunicator {
    public DataSource getDBCommunicator();
}