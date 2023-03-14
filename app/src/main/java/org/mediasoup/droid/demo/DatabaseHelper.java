package org.mediasoup.droid.demo;

import static android.database.sqlite.SQLiteDatabase.openOrCreateDatabase;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.Handler;
import java.util.logging.LogRecord;

public class DatabaseHelper extends SQLiteOpenHelper {

    private static final int VERSION = 1;
    private static final String DBNAME = "UserData.db";//数据库名
    private static final String TABLENAME = "location";//数据表名
    private Context mcontext;


    public static final String CREATE_LOCATION = "create table "+ TABLENAME +"(" +
            "coordinate text," +
            "date text)";

    public DatabaseHelper(Context context){
        super(context,DBNAME,null,VERSION);
        mcontext = context;
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        //调用SQLiteDatabase中的execSQL()执行建表语句
        sqLiteDatabase.execSQL(CREATE_LOCATION);
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase,int i,int i1) {
    }
}
