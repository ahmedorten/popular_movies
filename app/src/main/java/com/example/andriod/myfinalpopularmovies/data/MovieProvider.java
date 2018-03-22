package com.example.andriod.myfinalpopularmovies.data;


import android.annotation.TargetApi;
import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;

public class MovieProvider extends ContentProvider
{
    private static UriMatcher sUriMatcher = buildUriMatcher();
    private MovieDbHelper mOpenHelper;

    static final int MOVIES = 100;
    static final int MOVIE = 101;
    static final int TRAILERS_BY_MOVIE_ID = 102;
    static final int TRAILER = 103;
    static final int REVIEWS_BY_MOVIE_ID = 104;
    static final int REVIEW = 105;
    static final int MOVIE_WITH_ID = 106;


    private Cursor getTrailersByMovieId(String selection, String[] selectionArgs, String[] projection, String sortOrder)
    {
        return mOpenHelper.getReadableDatabase().query(
                MovieContract.TrailerEntry.TABLE_NAME,
                projection,
                selection,
                selectionArgs,
                null,
                null,
                sortOrder
        );
    }

    private Cursor getReviewsByMovieId(String selection, String[] selectionArgs, String[] projection, String sortOrder)
    {
        return mOpenHelper.getReadableDatabase().query(
                MovieContract.ReviewEntry.TABLE_NAME,
                projection,
                selection,
                selectionArgs,
                null,
                null,
                sortOrder
        );
    }

    private Cursor getAllMovies(final String[] projection, final String sortOrder)
    {
        return mOpenHelper.getReadableDatabase().query(
                MovieContract.MovieEntry.TABLE_NAME,
                projection,
                null,
                null,
                null,
                null,
                sortOrder
        );
    }

    private static final String sMovieIdSelection = MovieContract.MovieEntry.TABLE_NAME + "." +
            MovieContract.MovieEntry.COLUMN_MOVIE_ID;

    private Cursor getMovieById(Uri uri, String[] projection, String sortOrder)
    {
        long movieId = MovieContract.MovieEntry.getMovieIdFromUri(uri);
        String[] selectionArgs;
        selectionArgs = new String[]{Long.toString( movieId )};
        return mOpenHelper.getReadableDatabase().query(
                MovieContract.MovieEntry.TABLE_NAME,
                projection,
                sMovieIdSelection + " = ?",
                selectionArgs,
                null,
                null,
                sortOrder
        );

    }

    static UriMatcher buildUriMatcher()
    {
        final UriMatcher matcher = new UriMatcher(UriMatcher.NO_MATCH);
        final String authority = MovieContract.CONTENT_AUTHORITY;

        //For each URI, create a corresponding code
        matcher.addURI(authority, MovieContract.PATH_MOVIE, MOVIE);
        matcher.addURI(authority, MovieContract.PATH_MOVIES, MOVIES);
        matcher.addURI(authority, MovieContract.PATH_MOVIE + "/*", MOVIE_WITH_ID);
        matcher.addURI(authority, MovieContract.PATH_TRAILER, TRAILER);
        matcher.addURI(authority, MovieContract.PATH_TRAILER + "/*", TRAILERS_BY_MOVIE_ID);
        matcher.addURI(authority, MovieContract.PATH_REVIEW, REVIEW);
        matcher.addURI(authority, MovieContract.PATH_REVIEW + "/*", REVIEWS_BY_MOVIE_ID);

        return matcher;
    }

    @Override
    public boolean onCreate()
    {
        mOpenHelper = new MovieDbHelper(getContext());
        return false;
    }

    @Override
    public Cursor query(final Uri uri, final String[] projection, final String selection, final String[] selectionArgs, final String sortOrder)
    {
        Cursor retCursor;
        switch (sUriMatcher.match(uri))
        {
            case MOVIES:
                retCursor = getAllMovies(projection, sortOrder);
                break;
            case MOVIE_WITH_ID:
                retCursor = getMovieById(uri, projection, sortOrder);
                break;
            case TRAILERS_BY_MOVIE_ID:
                retCursor = getTrailersByMovieId(selection, selectionArgs, projection,  sortOrder);
                break;
            case REVIEWS_BY_MOVIE_ID:
                retCursor = getReviewsByMovieId(selection, selectionArgs, projection, sortOrder);
                break;
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
        retCursor.setNotificationUri(getContext().getContentResolver(), uri);
        return retCursor;
    }

    @Override
    public String getType(final Uri uri)
    {
        //Use the uri matcher to determine what kind of URI this is
        final int match = sUriMatcher.match(uri);

        switch (match)
        {
            case MOVIE_WITH_ID:
                return MovieContract.MovieEntry.CONTENT_ITEM_TYPE;
            case MOVIES:
                return MovieContract.MovieEntry.CONTENT_TYPE;
            case MOVIE:
                return MovieContract.MovieEntry.CONTENT_ITEM_TYPE;
            case TRAILERS_BY_MOVIE_ID:
                return MovieContract.TrailerEntry.CONTENT_TYPE;
            case TRAILER:
                return MovieContract.TrailerEntry.CONTENT_ITEM_TYPE;
            case REVIEWS_BY_MOVIE_ID:
                return MovieContract.ReviewEntry.CONTENT_TYPE;
            case REVIEW:
                return MovieContract.ReviewEntry.CONTENT_ITEM_TYPE;
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
    }

    @Override
    public Uri insert(final Uri uri, final ContentValues values)
    {
        final SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        final int match = sUriMatcher.match(uri);
        Uri returnUri;

        switch (match)
        {
            case MOVIE:
            {
                long _id = db.insert(MovieContract.MovieEntry.TABLE_NAME, null, values);
                if (_id > 0)
                    returnUri = MovieContract.MovieEntry.buildMovieUri(_id);
                else
                    throw new android.database.SQLException("Failed to insert row into " + uri);
                break;
            }
            case TRAILER:
            {
                long _id = db.insert(MovieContract.TrailerEntry.TABLE_NAME, null, values);
                if (_id > 0)
                    returnUri = MovieContract.TrailerEntry.buildTrailerUri(_id);
                else
                    throw new android.database.SQLException("Failed to insert row into " + uri);
                break;
            }
            case REVIEW:
            {
                long _id = db.insert(MovieContract.ReviewEntry.TABLE_NAME, null, values);
                if (_id > 0)
                    returnUri = MovieContract.ReviewEntry.buildReviewUri(_id);
                else
                    throw new android.database.SQLException("Failed to insert row at " + uri);
                break;
            }
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
        getContext().getContentResolver().notifyChange(uri, null);
        return returnUri;
    }

    @Override
    public int delete(final Uri uri, String selection, final String[] selectionArgs)
    {
        final SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        final int match = sUriMatcher.match(uri);
        int rowsDeleted;
        if (null == selection)
            selection = "1";
        switch (match)
        {
            case MOVIE_WITH_ID:
            {
                rowsDeleted = db.delete(MovieContract.MovieEntry.TABLE_NAME, selection, selectionArgs);
                break;
            }
            case TRAILERS_BY_MOVIE_ID:
            {
                rowsDeleted = db.delete(MovieContract.TrailerEntry.TABLE_NAME, selection, selectionArgs);
                break;
            }
            case REVIEWS_BY_MOVIE_ID:
            {
                rowsDeleted = db.delete(MovieContract.ReviewEntry.TABLE_NAME, selection, selectionArgs);
                break;
            }
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
        if (rowsDeleted != 0)
            getContext().getContentResolver().notifyChange(uri, null);
        return rowsDeleted;
    }

    @Override
    public int update(final Uri uri, final ContentValues values, final String selection, final String[] selectionArgs)
    {
        final SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        final int match = sUriMatcher.match(uri);

        int rowsUpdated;

        switch (match)
        {
            case MOVIE:
            {
                rowsUpdated = db.update(MovieContract.MovieEntry.TABLE_NAME, values, selection, selectionArgs);
                break;
            }
            case TRAILER:
            {
                rowsUpdated = db.update(MovieContract.TrailerEntry.TABLE_NAME, values, selection, selectionArgs);
                break;
            }
            case REVIEW:
            {
                rowsUpdated = db.update(MovieContract.ReviewEntry.TABLE_NAME, values, selection, selectionArgs);
                break;
            }
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
        if (rowsUpdated != 0)
            getContext().getContentResolver().notifyChange(uri, null);
        return rowsUpdated;
    }

    @Override
    public int bulkInsert(final Uri uri, final ContentValues[] values)
    {
        final SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        final int match = sUriMatcher.match(uri);
        switch (match)
        {
            case TRAILER:
            {
                db.beginTransaction();
                int returnCount = 0;
                try
                {
                    for (ContentValues value : values)
                    {
                        long _id = db.insert(MovieContract.TrailerEntry.TABLE_NAME, null, value);
                        if (_id != -1)
                            returnCount++;
                    }
                    db.setTransactionSuccessful();
                }
                finally
                {
                    db.endTransaction();
                }
                getContext().getContentResolver().notifyChange(uri, null);
                return returnCount;
            }
            case REVIEW:
            {
                db.beginTransaction();
                int returnCount = 0;
                try
                {
                    for (ContentValues value : values)
                    {
                        long _id = db.insert(MovieContract.ReviewEntry.TABLE_NAME, null, value);
                        if (_id != -1)
                            returnCount++;
                    }
                    db.setTransactionSuccessful();
                }
                finally
                {
                    db.endTransaction();
                }
                return returnCount;
            }
            default:
                return super.bulkInsert(uri, values);
        }

    }

    // You do not need to call this method. This is a method specifically to assist the testing
    // framework in running smoothly. You can read more at:
    // http://developer.android.com/reference/android/content/ContentProvider.html#shutdown()
    @Override
    @TargetApi(11)
    public void shutdown() {
        mOpenHelper.close();
        super.shutdown();
    }
}
