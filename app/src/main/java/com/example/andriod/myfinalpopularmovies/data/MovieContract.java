package com.example.andriod.myfinalpopularmovies.data;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.net.Uri;
import android.provider.BaseColumns;

public class MovieContract {
    public static final String CONTENT_AUTHORITY = "com.example.andriod.myfinalpopularmovies";

    // Use CONTENT_AUTHORITY to create the base of all URI's which apps will use to contact
    // the content provider.
    public static final Uri BASE_CONTENT_URI = Uri.parse("content://" + CONTENT_AUTHORITY);

    public static final String SELECTION_SUFFIX = " = ?";

    // Paths appended to uris
    public static final String PATH_MOVIE = "movie";
    public static final String PATH_MOVIES = "movies";
    public static final String PATH_TRAILER = "trailer";
    public static final String PATH_REVIEW = "review";

    /* Inner class that defines the table contents of the location table */
    public static final class MovieEntry implements BaseColumns
    {

        public static final Uri CONTENT_URI =
                BASE_CONTENT_URI.buildUpon().appendPath(PATH_MOVIE).build();

        public static final Uri CONTENT_URI_ALL =
                BASE_CONTENT_URI.buildUpon().appendPath(PATH_MOVIES).build();


        public static final String CONTENT_TYPE =
                ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_MOVIE;

        public static final String CONTENT_ITEM_TYPE =
                ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_MOVIE;

        // Table name
        public static final String TABLE_NAME = "movie";

        // Table columns
        public static final String COLUMN_MOVIE_ID = "movie_id";
        public static final String COLUMN_MOVIE_TITLE = "movie_title";
        public static final String COLUMN_MOVIE_BACKDROP_PATH = "movie_backdrop_path";
        public static final String COLUMN_MOVIE_POSTER_PATH = "movie_poster_path";
        public static final String COLUMN_MOVIE_OVERVIEW = "movie_overview";
        public static final String COLUMN_MOVIE_POPULARITY = "movie_popularity";
        public static final String COLUMN_MOVIE_VOTE_AVERAGE = "movie_vote_average";
        public static final String COLUMN_MOVIE_VOTE_COUNT = "movie_vote_count";
        public static final String COLUMN_MOVIE_RELEASE_DATE = "movie_release_date";

        public static final int COLUMN_ID_INT = 0;
        public static final int COLUMN_MOVIE_ID_INT = 1;
        public static final int COLUMN_MOVIE_TITLE_INT = 2;
        public static final int COLUMN_MOVIE_BACKDROP_PATH_INT = 3;
        public static final int COLUMN_MOVIE_POSTER_PATH_INT = 4;
        public static final int COLUMN_MOVIE_OVERVIEW_INT = 5;
        public static final int COLUMN_MOVIE_POPULARITY_INT = 6;
        public static final int COLUMN_MOVIE_VOTE_AVERAGE_INT = 7;
        public static final int COLUMN_MOVIE_VOTE_COUNT_INT = 8;
        public static final int COLUMN_MOVIE_RELEASE_DATE_INT = 9;

        public static final String[] FULL_PROJECTION = new String[]{
                _ID,
                COLUMN_MOVIE_ID,
                COLUMN_MOVIE_TITLE,
                COLUMN_MOVIE_BACKDROP_PATH,
                COLUMN_MOVIE_POSTER_PATH,
                COLUMN_MOVIE_OVERVIEW,
                COLUMN_MOVIE_POPULARITY,
                COLUMN_MOVIE_VOTE_AVERAGE,
                COLUMN_MOVIE_VOTE_COUNT,
                COLUMN_MOVIE_RELEASE_DATE
        };

        public static Uri buildMovieUri(long id) {
            return ContentUris.withAppendedId(CONTENT_URI, id);
        }

        public static long getMovieIdFromUri(Uri uri)
        {
            String movieIdString = uri.getPathSegments().get(1);
            if (null != movieIdString && movieIdString.length() > 0)
            {
                return Long.parseLong(movieIdString);
            }
            else
                return 0;
        }

        public static Uri buildMovieUriWithId(String movieId)
        {
            return CONTENT_URI.buildUpon().appendPath(movieId).build();
        }
    }

    public static final class ReviewEntry implements BaseColumns
    {
        public static final Uri CONTENT_URI =
                BASE_CONTENT_URI.buildUpon().appendPath(PATH_REVIEW).build();

        public static final String CONTENT_TYPE =
                ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_REVIEW;
        public static final String CONTENT_ITEM_TYPE =
                ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_REVIEW;

        public static final String TABLE_NAME = "reviews";

        public static final String COLUMN_MOVIE_ID = "movie_id";
        public static final String COLUMN_REVIEW_ID = "review_id";
        public static final String COLUMN_REVIEW_CONTENT = "review_content";
        public static final String COLUMN_REVIEW_AUTHOR = "review_author";

        public static Uri buildReviewUri(long id)
        {
            return ContentUris.withAppendedId(CONTENT_URI, id);
        }

        public static Uri buildReviewUriWithMovieId(String movieId) {
            return CONTENT_URI.buildUpon().appendPath(movieId).build();
        }

    }

    public static final class TrailerEntry implements BaseColumns {

        public static final Uri CONTENT_URI =
                BASE_CONTENT_URI.buildUpon().appendPath(PATH_TRAILER).build();

        public static final String CONTENT_TYPE =
                ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_TRAILER;
        public static final String CONTENT_ITEM_TYPE =
                ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_TRAILER;

        public static final String TABLE_NAME = "trailers";

        public static final String COLUMN_MOVIE_ID = "movie_id";
        public static final String COLUMN_TRAILER_ID = "trailer_id";
        public static final String COLUMN_TRAILER_KEY = "trailer_key";
        public static final String COLUMN_TRAILER_NAME = "trailer_name";

        public static Uri buildTrailerUri(long id) {
            return ContentUris.withAppendedId(CONTENT_URI, id);
        }

        public static Uri buildTrailerUriWithMovieId(String movieId) {
            return CONTENT_URI.buildUpon().appendPath(movieId).build();
        }
    }
}
