package com.example.andriod.myfinalpopularmovies;

import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import com.example.andriod.myfinalpopularmovies.data.MovieContract;
import com.example.andriod.myfinalpopularmovies.models.Movie;
import com.example.andriod.myfinalpopularmovies.models.MyButtonInterface;
import com.example.andriod.myfinalpopularmovies.models.Review;
import com.example.andriod.myfinalpopularmovies.models.Trailer;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;


public class DetailActivityFragment extends Fragment implements MyButtonInterface, Button.OnClickListener
{
    private String LOG_TAG = getClass().getSimpleName();
    private Context mContext;
    private ListView mListView;
    private DetailListViewAdapter mDetailListViewAdapter;
    private Movie mSelectedMovie;
    private Button mToggleButton;
    private boolean mIsTwoPane;

    public DetailActivityFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        Bundle wBundle = getArguments();
        if (wBundle == null && savedInstanceState == null) {
            return null;
        }
        setHasOptionsMenu(true);
        View rootView = inflater.inflate(R.layout.fragment_detail, container, false);
        mContext = getActivity();

        if (savedInstanceState != null) {
            mSelectedMovie = (Movie)savedInstanceState.getSerializable("movie");
            mIsTwoPane = savedInstanceState.getBoolean("isTwoPane");
        }
        else {
            mSelectedMovie = (Movie)wBundle.getSerializable("movie");
            mIsTwoPane = wBundle.getBoolean("isTwoPane");
            if (mSelectedMovie != null) {
                FetchTrailersAndRatingsTask wFetchTrailersAndRatingsTask = new FetchTrailersAndRatingsTask();
                wFetchTrailersAndRatingsTask.execute(new String[]{String.valueOf(mSelectedMovie.getMovieId())});
            }
        }
        if (mSelectedMovie == null) {
            return null;
        }

        //Set the movie title
        TextView wTitleTextView = (TextView)rootView.findViewById(R.id.movieTitle);
        wTitleTextView.setText(mSelectedMovie.getTitle());
        mListView = (ListView)rootView.findViewById(R.id.listView);
        mListView.setOnItemClickListener(new ListView.OnItemClickListener()
        {
            @Override
            public void onItemClick(final AdapterView<?> parent, final View view, final int position, final long id)
            {
                //We know that since this row was clicked, it has to be a trailer row, since
                //those are the only listview rows that are enabled.
                //Get the actual position so that the proper uri can be set
                int actualPosition = position - 2; // Account for the detail row and video trailer title row
                Trailer wTrailer = mSelectedMovie.getTrailers().get(actualPosition);

                final String MOVIE_BASE_URL = getString(R.string.youtube_base_url);
                final String KEY_PARAM = "v";

                Uri trailerUri = Uri.parse(MOVIE_BASE_URL).buildUpon()
                        .appendQueryParameter(KEY_PARAM, wTrailer.getKey())
                        .build();

                startActivity(new Intent(Intent.ACTION_VIEW, trailerUri));
            }
        });

        if (savedInstanceState != null) {
            populateListview();
        }
        return rootView;
    }

    public void setToggleButton(Button button) {
        mToggleButton = button;
    }

    @Override
    public void onSaveInstanceState(final Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putSerializable("movie", mSelectedMovie);
        outState.putBoolean("isTwoPane", mIsTwoPane);
    }

    public void addOrRemoveFavorite()
    {
        if (mSelectedMovie.isFavorite())
        {
            removeMovieFromFavorites();
        }
        else
        {
            addMovieToFavorites();
        }
        mSelectedMovie.setIsFavorite(!mSelectedMovie.isFavorite());
    }

    private void populateListview()
    {
        mDetailListViewAdapter = new DetailListViewAdapter(getActivity(), this, mSelectedMovie, this);
        mListView.setAdapter(mDetailListViewAdapter);
    }

    @Override
    public void onClick(final View v) {
        toggleMovie();
    }

    private void toggleMovie()
    {
        addOrRemoveFavorite();
        setButtonText(mToggleButton);
        // If the user is removing this movie and the sort order is "Favorites" refresh the list
        // Otherwise leave the list alone because nothing will have changed
        // Get the current sort order
        SharedPreferences wPrefs = PreferenceManager.getDefaultSharedPreferences(mContext);
        String wSortOrder = wPrefs.getString(getString(R.string.prefs_sort_key), getString(R.string.prefs_sort_default));
        //Also check to see if this is a 2 pane layout
        if (wSortOrder.equals(getString(R.string.favorites_sort_param)) && mIsTwoPane)
        {
            MainActivityFragment wMainFragment = (MainActivityFragment)getFragmentManager().findFragmentById(R.id.fragment_main);
            wMainFragment.updateMovies();
        }
    }

    public void setButtonText(Button button)
    {
        if (mSelectedMovie.isFavorite())
        {
            button.setText(mContext.getString(R.string.favorite_button_text_remove));
        }
        else
        {
            button.setText(mContext.getString(R.string.favorite_button_text_mark));
        }

    }

    public class FetchTrailersAndRatingsTask extends AsyncTask<String, String, Void>
    {
        private final String LOG_TAG = FetchTrailersAndRatingsTask.class.getSimpleName();

        protected Void doInBackground(String... params) {

            //Determine if this movie is stored in the database as a favorite
            long _id = checkIfMovieExists(mSelectedMovie.getMovieId());
            if (_id != -1)
            {
                mSelectedMovie.setId(_id);
                retrieveMovieDataFromDatabase(_id);
                mSelectedMovie.setIsFavorite(true);
            }
            else
            {
                try
                {
                    // Will contain the raw JSON response as a string.
                    String trailersJsonString = null;
                    String reviewsJsonString = null;
                    String apiKey = getString(R.string.movie_api_key);

                    final String MOVIE_BASE_URL = getString(R.string.rating_trailer_base_url);
                    final String RATING_PATH = "reviews";
                    final String TRAILER_PATH = "videos";
                    final String API_KEY_PARAM = "api_key";
                    final String MOVIE_ID = params[0];

                    Uri trailerUri = Uri.parse(MOVIE_BASE_URL).buildUpon()
                            .appendPath(MOVIE_ID)
                            .appendPath(TRAILER_PATH)
                            .appendQueryParameter(API_KEY_PARAM, apiKey)
                            .build();

                    Uri ratingUri = Uri.parse(MOVIE_BASE_URL).buildUpon()
                            .appendPath(MOVIE_ID)
                            .appendPath(RATING_PATH)
                            .appendQueryParameter(API_KEY_PARAM, apiKey)
                            .build();
                    trailersJsonString = makeRequest(trailerUri);
                    reviewsJsonString = makeRequest(ratingUri);
                    mSelectedMovie.setTrailers(getTrailersFromJson(trailersJsonString));
                    mSelectedMovie.setReviews(getReviewsFromJson(reviewsJsonString));
                }
                catch (Exception e)
                {
                    Log.e(LOG_TAG, e.getMessage(), e);
                    e.printStackTrace();
                    return null;
                }
            }
            return null;
        }

        private void retrieveMovieDataFromDatabase(long id)
        {
            ArrayList<Trailer> wTrailers = new ArrayList<>();
            ArrayList<Review> wReviews = new ArrayList<>();
            Cursor wTrailerCursor = mContext.getContentResolver().query(
                    MovieContract.TrailerEntry.buildTrailerUriWithMovieId(String.valueOf(id)),
                    new String[]{MovieContract.TrailerEntry.COLUMN_TRAILER_NAME,
                            MovieContract.TrailerEntry.COLUMN_TRAILER_KEY},
                    MovieContract.TrailerEntry.COLUMN_MOVIE_ID + MovieContract.SELECTION_SUFFIX,
                    new String[]{ String.valueOf(id) },
                    null
            );
            while (wTrailerCursor.moveToNext())
            {
                Trailer wTrailer = new Trailer();
                int wTrailerNameIndex = wTrailerCursor.getColumnIndex(MovieContract.TrailerEntry.COLUMN_TRAILER_NAME);
                wTrailer.setName(wTrailerCursor.getString(wTrailerNameIndex));
                int wTrailerKeyIndex = wTrailerCursor.getColumnIndex(MovieContract.TrailerEntry.COLUMN_TRAILER_KEY);
                wTrailer.setKey(wTrailerCursor.getString(wTrailerKeyIndex));
                wTrailers.add(wTrailer);
            }

            Cursor wReviewCursor = mContext.getContentResolver().query(
                    MovieContract.ReviewEntry.buildReviewUriWithMovieId(String.valueOf(id)),
                    new String[]{
                            MovieContract.ReviewEntry.COLUMN_REVIEW_ID,
                            MovieContract.ReviewEntry.COLUMN_REVIEW_AUTHOR,
                            MovieContract.ReviewEntry.COLUMN_REVIEW_CONTENT},
                    MovieContract.ReviewEntry.COLUMN_MOVIE_ID + MovieContract.SELECTION_SUFFIX,
                    new String[]{ String.valueOf(id) },
                    null
            );

            while (wReviewCursor.moveToNext())
            {
                Review wReview = new Review();
                int wReviewIdIndex = wReviewCursor.getColumnIndex(MovieContract.ReviewEntry.COLUMN_REVIEW_ID);
                wReview.setId(wReviewCursor.getString(wReviewIdIndex));
                int wReviewAuthorIndex = wReviewCursor.getColumnIndex(MovieContract.ReviewEntry.COLUMN_REVIEW_AUTHOR);
                wReview.setAuthor(wReviewCursor.getString(wReviewAuthorIndex));
                int wReviewContentIndex = wReviewCursor.getColumnIndex(MovieContract.ReviewEntry.COLUMN_REVIEW_CONTENT);
                wReview.setContent(wReviewCursor.getString(wReviewContentIndex));
                wReviews.add(wReview);
            }
            mSelectedMovie.setTrailers(wTrailers);
            mSelectedMovie.setReviews(wReviews);
        }

        private String makeRequest(Uri uri)
        {
            String responseString;
            // These two need to be declared outside the try/catch
            // so that they can be closed in the finally block.
            HttpURLConnection urlConnection = null;
            BufferedReader reader = null;
            try {
                // Construct the URL for the MoviesDB query
                URL url = new URL(uri.toString());
                Log.v(LOG_TAG, "Trailer URI " + uri.toString());
                // Create the request to MoviesDB, and open the connection
                urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setRequestMethod("GET");
                urlConnection.connect();

                // Read the input stream into a String
                InputStream inputStream = urlConnection.getInputStream();
                StringBuffer buffer = new StringBuffer();
                if (inputStream == null) {
                    // Nothing to do.
                    return null;
                }
                reader = new BufferedReader(new InputStreamReader(inputStream));

                String line;
                while ((line = reader.readLine()) != null) {
                    buffer.append(line + "\n");
                }

                if (buffer.length() == 0) {
                    return null;
                }
                responseString = buffer.toString();
            } catch (IOException e) {
                Log.e(LOG_TAG, "Error ", e);
                // If the code didn't successfully get the movie data, there's no point in attemping
                // to parse it.
                return null;
            } finally{
                if (urlConnection != null) {
                    urlConnection.disconnect();
                }
                if (reader != null) {
                    try {
                        reader.close();
                    } catch (final IOException e) {
                        Log.e(LOG_TAG, "Error closing stream", e);
                    }
                }
            }
            return responseString;
        }

        protected void onPostExecute(Void params) {
            populateListview();
        }

        private ArrayList<Review> getReviewsFromJson(String jsonString)
                throws JSONException
        {
            ArrayList<Review> wReviews = new ArrayList<>();
            //Names of json objects
            final String REVIEW_ID = "id";
            final String REVIEW_AUTHOR = "author";
            final String REVIEW_CONTENT = "content";
            final String REVIEW_URL = "url";
            final String REVIEW_RESULTS = "results";

            JSONObject reviewObject = new JSONObject(jsonString);
            JSONArray reviewArray = reviewObject.getJSONArray(REVIEW_RESULTS);
            for (int i = 0; i < reviewArray.length(); i ++)
            {
                try
                {
                    JSONObject thisObject = reviewArray.getJSONObject(i);
                    String reviewId = thisObject.getString(REVIEW_ID);
                    String reviewAuthor = thisObject.getString(REVIEW_AUTHOR);
                    String reviewContent = thisObject.getString(REVIEW_CONTENT);
                    String reviewUrl = thisObject.getString(REVIEW_URL);

                    Review thisReview = new Review();
                    thisReview.setId(reviewId);
                    thisReview.setAuthor(reviewAuthor);
                    thisReview.setContent(reviewContent);
                    wReviews.add(thisReview);
                }
                catch (Exception ex)
                {
                    Log.e(LOG_TAG, "Error parsing json object. " + ex);
                }
            }
            return wReviews;
        }

        private ArrayList<Trailer> getTrailersFromJson(String jsonString)
                throws JSONException
        {
            ArrayList<Trailer> wTrailers = new ArrayList<>();
            //Names of json objects
            final String TRAILER_ID = "id";
            final String TRAILER_KEY = "key";
            final String TRAILER_NAME = "name";
            final String TRAILER_SITE = "site";
            final String TRAILER_RESULTS = "results";

            JSONObject trailerObject = new JSONObject(jsonString);
            JSONArray trailerArray = trailerObject.getJSONArray(TRAILER_RESULTS);
            for (int i = 0; i < trailerArray.length(); i ++)
            {
                JSONObject thisObject = trailerArray.getJSONObject(i);
                String trailerId = thisObject.getString(TRAILER_ID);
                String trailerKey = thisObject.getString(TRAILER_KEY);
                String trailerName = thisObject.getString(TRAILER_NAME);
                String trailerSite = thisObject.getString(TRAILER_SITE);

                Trailer thisTrailer = new Trailer();
                thisTrailer.setId(trailerId);
                thisTrailer.setKey(trailerKey);
                thisTrailer.setName(trailerName);

                wTrailers.add(thisTrailer);
            }

            return wTrailers;
        }

    }

    private void removeMovieFromFavorites()
    {
        int reviewsDelete = deleteReviews();
        int trailersDeleted = deleteTrailers();
        int moviesDeleted = deleteMovie();
    }

    private int deleteReviews()
    {
        String movieIdString = String.valueOf(mSelectedMovie.getId());
        int rowsDeleted = mContext.getContentResolver().delete(
                MovieContract.ReviewEntry.buildReviewUriWithMovieId(movieIdString),
                MovieContract.ReviewEntry.COLUMN_MOVIE_ID + MovieContract.SELECTION_SUFFIX,
                new String[]{movieIdString}
        );
        return rowsDeleted;
    }
    private int deleteTrailers()
    {
        String movieIdString = String.valueOf(mSelectedMovie.getId());
        int rowsDeleted = mContext.getContentResolver().delete(
                MovieContract.TrailerEntry.buildTrailerUriWithMovieId(movieIdString),
                MovieContract.TrailerEntry.COLUMN_MOVIE_ID + MovieContract.SELECTION_SUFFIX,
                new String[]{movieIdString}
        );
        return rowsDeleted;
    }
    private int deleteMovie()
    {
        String movieIdString = String.valueOf(mSelectedMovie.getId());
        int rowsDeleted = mContext.getContentResolver().delete(
                MovieContract.MovieEntry.buildMovieUriWithId(movieIdString),
                MovieContract.MovieEntry._ID + MovieContract.SELECTION_SUFFIX,
                new String[]{movieIdString}
        );
        return rowsDeleted;
    }

    private long addMovieToFavorites()
    {
        long _id = addMovie();
        mSelectedMovie.setId(_id);
        addTrailers();
        addReviews();
        return _id;
    }

    private long checkIfMovieExists(String movieId)
    {
        long _id;

        //Check to see if a movie with this id already exists
        Cursor movieCursor = mContext.getContentResolver().query(
                MovieContract.MovieEntry.buildMovieUriWithId(movieId),
                new String[]{ MovieContract.MovieEntry._ID },
                MovieContract.MovieEntry.COLUMN_MOVIE_ID + MovieContract.SELECTION_SUFFIX,
                new String[]{ movieId },
                null
        );

        if (movieCursor.moveToFirst())
        {
            int movieIdIndex = movieCursor.getColumnIndex(MovieContract.MovieEntry._ID);
            _id = movieCursor.getLong(movieIdIndex);
        }
        else
        {
            _id = -1;
        }
        return _id;
    }

    private long addMovie()
    {
        long _id;
        //Check to see if a movie with this id already exists
        String movieId = String.valueOf(mSelectedMovie.getMovieId());
        Cursor movieCursor = mContext.getContentResolver().query(
                MovieContract.MovieEntry.buildMovieUriWithId(movieId),
                new String[]{ MovieContract.MovieEntry._ID },
                MovieContract.MovieEntry.COLUMN_MOVIE_ID + MovieContract.SELECTION_SUFFIX,
                new String[]{ movieId },
                null
        );

        String dateString = "";
        if (movieCursor.moveToFirst())
        {
            int movieIdIndex = movieCursor.getColumnIndex(MovieContract.MovieEntry._ID);
            _id = movieCursor.getLong(movieIdIndex);
        }
        else
        {
            ContentValues wContentValues = new ContentValues();
            wContentValues.put(MovieContract.MovieEntry.COLUMN_MOVIE_ID, mSelectedMovie.getMovieId());
            wContentValues.put(MovieContract.MovieEntry.COLUMN_MOVIE_TITLE, mSelectedMovie.getTitle());
            wContentValues.put(MovieContract.MovieEntry.COLUMN_MOVIE_BACKDROP_PATH, mSelectedMovie.getBackdropPath());
            wContentValues.put(MovieContract.MovieEntry.COLUMN_MOVIE_POSTER_PATH, mSelectedMovie.getPosterPath());
            wContentValues.put(MovieContract.MovieEntry.COLUMN_MOVIE_OVERVIEW, mSelectedMovie.getOverview());
            wContentValues.put(MovieContract.MovieEntry.COLUMN_MOVIE_POPULARITY, mSelectedMovie.getPopularity());
            wContentValues.put(MovieContract.MovieEntry.COLUMN_MOVIE_VOTE_AVERAGE, mSelectedMovie.getVoteAverage());
            wContentValues.put(MovieContract.MovieEntry.COLUMN_MOVIE_VOTE_COUNT, mSelectedMovie.getVoteCount());
            wContentValues.put(MovieContract.MovieEntry.COLUMN_MOVIE_RELEASE_DATE, String.valueOf(mSelectedMovie.getReleaseDate()));
            Uri insertUri = mContext.getContentResolver().insert(MovieContract.MovieEntry.CONTENT_URI, wContentValues);
            _id = ContentUris.parseId(insertUri);
        }
        return _id;
    }

    private void addTrailers()
    {
        ContentValues[] wContentValues = new ContentValues[mSelectedMovie.getTrailers().size()];
        for (int i = 0; i < mSelectedMovie.getTrailers().size(); i ++)
        {
            Trailer trailer = mSelectedMovie.getTrailers().get(i);
            ContentValues wValue = new ContentValues();
            wValue.put(MovieContract.TrailerEntry.COLUMN_MOVIE_ID, mSelectedMovie.getId());
            wValue.put(MovieContract.TrailerEntry.COLUMN_TRAILER_ID, trailer.getId());
            wValue.put(MovieContract.TrailerEntry.COLUMN_TRAILER_KEY, trailer.getKey());
            wValue.put(MovieContract.TrailerEntry.COLUMN_TRAILER_NAME, trailer.getName());
            wContentValues[i] = wValue;
        }
        int rowsInserted = mContext.getContentResolver().bulkInsert(
                MovieContract.TrailerEntry.CONTENT_URI,
                wContentValues
        );
    }
    private void addReviews()
    {
        ContentValues[] wContentValues = new ContentValues[mSelectedMovie.getReviews().size()];
        for (int i = 0; i < mSelectedMovie.getReviews().size(); i ++)
        {
            Review review = mSelectedMovie.getReviews().get(i);
            ContentValues wValue = new ContentValues();
            wValue.put(MovieContract.ReviewEntry.COLUMN_MOVIE_ID, mSelectedMovie.getId());
            wValue.put(MovieContract.ReviewEntry.COLUMN_REVIEW_ID, review.getId());
            wValue.put(MovieContract.ReviewEntry.COLUMN_REVIEW_CONTENT, review.getContent());
            wValue.put(MovieContract.ReviewEntry.COLUMN_REVIEW_AUTHOR, review.getAuthor());
            wContentValues[i] = wValue;
        }
        int rowsInserted = mContext.getContentResolver().bulkInsert(
                MovieContract.ReviewEntry.CONTENT_URI,
                wContentValues
        );
    }
}