package com.app.publicsafetycomission.databaseRef;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.util.ArrayList;
import java.util.HashMap;


public class DBHelperClass extends SQLiteOpenHelper {

    Context context;
    public DBHelperClass(Context context) {
        super(context, DataBaseConstant.DB_DATABASE_PSC, null, DataBaseConstant.VERSION);

    }

    @Override
    public void onCreate(SQLiteDatabase database) {

        String CREATE_DISTRICTS_TABLE = "CREATE TABLE " +DataBaseConstant.TABLE_DISTRICTS+
                "("+DataBaseConstant.TAG_DIST_ID+" INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL  UNIQUE," +
                ""+DataBaseConstant.TAG_DIST_NAME+" VARCHAR(200));";

        database.execSQL(CREATE_DISTRICTS_TABLE);
        Log.e(DataBaseConstant.PSC_LOGCAT, "Districts Table Created !");

        String CREATE_CATEGORY_TABLE = "CREATE TABLE " +DataBaseConstant.TABLE_CATEGORIES+
                "("+DataBaseConstant.TAG_CATEG_ID+" INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL  UNIQUE," +
                ""+DataBaseConstant.TAG_CATEG_NAME+" VARCHAR(200));";

        database.execSQL(CREATE_CATEGORY_TABLE);
        Log.e(DataBaseConstant.PSC_LOGCAT, "Category Table Created !");

    }
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        String query1;
        query1 = "DROP TABLE IF EXISTS " +DataBaseConstant.TABLE_DISTRICTS;
        db.execSQL(query1);

        String query2;
        query2 = "DROP TABLE IF EXISTS " +DataBaseConstant.TABLE_CATEGORIES;
        db.execSQL(query2);
    }

    public void addDistricts(String dist_id, String dist_name){
        try {
            SQLiteDatabase database = this.getWritableDatabase();
            ContentValues values = new ContentValues();
            values.put(DataBaseConstant.TAG_DIST_ID, dist_id);
            values.put(DataBaseConstant.TAG_DIST_NAME, dist_name);
            database.insert(DataBaseConstant.TABLE_DISTRICTS, null, values);
            database.close();
            Log.e(DataBaseConstant.PSC_LOGCAT, "Districts Stored Successfully");

        }
        catch (Exception e){
            Log.e("Exception",e.toString());
        }

    }

    public void addCategories(String cat_id, String cat_name){
        try {
            SQLiteDatabase database = this.getWritableDatabase();
            ContentValues values = new ContentValues();
            values.put(DataBaseConstant.TAG_CATEG_ID, cat_id);
            values.put(DataBaseConstant.TAG_CATEG_NAME, cat_name);
            database.insert(DataBaseConstant.TABLE_CATEGORIES, null, values);
            database.close();
            Log.e(DataBaseConstant.PSC_LOGCAT, "Categories Stored Successfully");

        }
        catch (Exception e){
            Log.e("Exception",e.toString());
        }

    }

    public ArrayList<HashMap<String, String>> getDistrictData() {
        try{
            ArrayList<HashMap<String, String>> wordList;
            wordList = new ArrayList<HashMap<String, String>>();
            String selectQuery = "SELECT *  FROM " +DataBaseConstant.TABLE_DISTRICTS;
            SQLiteDatabase database = this.getWritableDatabase();
            Cursor cursor = database.rawQuery(selectQuery, null);
            if (cursor.moveToFirst()) {
                do {
                    HashMap<String, String> map = new HashMap<String, String>();
                    map.put(DataBaseConstant.TAG_DIST_ID,cursor.getString(0));
                    map.put(DataBaseConstant.TAG_DIST_NAME, cursor.getString(1));
                    Log.e("cursor.getString(0)",cursor.getString(0));
                    Log.e("cursor.getString(1)",cursor.getString(1));
                    wordList.add(map);
                } while (cursor.moveToNext());
            }
            return wordList;
        }catch (Exception e){

        }
        return null;
    }

    public ArrayList<HashMap<String, String>> getCategoriesData() {
        try{
            ArrayList<HashMap<String, String>> wordList;
            wordList = new ArrayList<HashMap<String, String>>();
            String selectQuery = "SELECT *  FROM " +DataBaseConstant.TABLE_CATEGORIES;
            SQLiteDatabase database = this.getWritableDatabase();
            Cursor cursor = database.rawQuery(selectQuery, null);
            if (cursor.moveToFirst()) {
                do {
                    HashMap<String, String> maps = new HashMap<String, String>();
                    maps.put(DataBaseConstant.TAG_CATEG_ID,cursor.getString(0));
                    maps.put(DataBaseConstant.TAG_CATEG_NAME, cursor.getString(1));
                    Log.e("cursor.getString(0)",cursor.getString(0));
                    Log.e("cursor.getString(1)",cursor.getString(1));
                    wordList.add(maps);
                } while (cursor.moveToNext());
            }
            return wordList;
        }catch (Exception e){

        }
        return null;
    }

    public void deleteDistrictTables() {
        try {
            SQLiteDatabase database = this.getWritableDatabase();
            String deleteCountriesQuery = "DELETE FROM " + DataBaseConstant.TABLE_DISTRICTS;
            database.execSQL(deleteCountriesQuery);
            Log.d(DataBaseConstant.PSC_LOGCAT, "Districts Data Deleted !");

        }catch (Exception e){

        }
    }

    public void deleteCategoriesTables() {
        try {
            SQLiteDatabase database = this.getWritableDatabase();
            String deleteCountriesQuery = "DELETE FROM " + DataBaseConstant.TABLE_CATEGORIES;
            database.execSQL(deleteCountriesQuery);
            Log.d(DataBaseConstant.PSC_LOGCAT, "Categories Data Deleted !");

        }catch (Exception e){

        }
    }
}

