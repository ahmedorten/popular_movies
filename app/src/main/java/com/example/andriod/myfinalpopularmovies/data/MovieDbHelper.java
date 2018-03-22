package com.example.andriod.myfinalpopularmovies.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.example.andriod.myfinalpopularmovies.data.MovieContract.MovieEntry;
import com.example.andriod.myfinalpopularmovies.data.MovieContract.ReviewEntry;
import com.example.andriod.myfinalpopularmovies.data.MovieContract.TrailerEntry;


public class MovieDbHelper extends SQLiteOpenHelper
{
    // If you change the database schema, you must increment the database version.
    private static final int DATABASE_VERSION = 1;

    static final String DATABASE_NAME = "movies.db";

    public MovieDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        // Create a table to hold movies.

        final String SQL_CREATE_MOVIE_TABLE = "CREATE TABLE " + MovieContract.MovieEntry.TABLE_NAME + " (" +
                MovieEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                MovieEntry.COLUMN_MOVIE_ID + " TEXT UNIQUE NOT NULL, " +
                MovieEntry.COLUMN_MOVIE_TITLE + " TEXT NOT NULL, " +
                MovieEntry.COLUMN_MOVIE_BACKDROP_PATH + " TEXT NOT NULL, " +
                MovieEntry.COLUMN_MOVIE_POSTER_PATH + " TEXT NOT NULL, " +
                MovieEntry.COLUMN_MOVIE_OVERVIEW + " TEXT, " +
                MovieEntry.COLUMN_MOVIE_POPULARITY + " REAL NOT NULL, " +
                MovieEntry.COLUMN_MOVIE_VOTE_AVERAGE + " REAL NOT NULL, " +
                MovieEntry.COLUMN_MOVIE_VOTE_COUNT + " REAL NOT NULL, " +
                MovieEntry.COLUMN_MOVIE_RELEASE_DATE + " REAL " +
                " );";

        // Create a table to hold the trailers belonging to a movie
        final String SQL_CREATE_TRAILER_TABLE = "CREATE TABLE " + TrailerEntry.TABLE_NAME + " (" +
                TrailerEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                TrailerEntry.COLUMN_MOVIE_ID + " INTEGER NOT NULL, " +
                TrailerEntry.COLUMN_TRAILER_ID + " TEXT NOT NULL, " +
                TrailerEntry.COLUMN_TRAILER_KEY + " TEXT NOT NULL, " +
                TrailerEntry.COLUMN_TRAILER_NAME + " TEXT NOT NULL, " +
                //Set the movie id as a foreign key to the movie table
                " FOREIGN KEY (" + ReviewEntry.COLUMN_MOVIE_ID + ") REFERENCES " +
                MovieEntry.TABLE_NAME + " (" + MovieEntry._ID + ")" +
                " );";

        // Create a table to hold the reviews belonging to a movie
        final String SQL_CREATE_REVIEW_TABLE = "CREATE TABLE " + ReviewEntry.TABLE_NAME + " (" +
                ReviewEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                ReviewEntry.COLUMN_MOVIE_ID + " INTEGER NOT NULL, " +
                ReviewEntry.COLUMN_REVIEW_ID + " TEXT NOT NULL, " +
                ReviewEntry.COLUMN_REVIEW_CONTENT + " TEXT NOT NULL, " +
                ReviewEntry.COLUMN_REVIEW_AUTHOR + " TEXT NOT NULL, " +
                //Set the movie id as a foreign key to the movie table
                " FOREIGN KEY (" + ReviewEntry.COLUMN_MOVIE_ID + ") REFERENCES " +
                MovieEntry.TABLE_NAME + " (" + MovieEntry._ID + ")" +
                " )";

        sqLiteDatabase.execSQL(SQL_CREATE_MOVIE_TABLE);
        sqLiteDatabase.execSQL(SQL_CREATE_TRAILER_TABLE);
        sqLiteDatabase.execSQL(SQL_CREATE_REVIEW_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int oldVersion, int newVersion) {
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + MovieEntry.TABLE_NAME);
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + TrailerEntry.TABLE_NAME);
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + ReviewEntry.TABLE_NAME);
        onCreate(sqLiteDatabase);
    }
}