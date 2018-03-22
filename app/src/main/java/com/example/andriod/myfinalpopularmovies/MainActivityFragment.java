package com.example.andriod.myfinalpopularmovies;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Configuration;
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
import android.widget.GridView;

import com.example.andriod.myfinalpopularmovies.data.MovieContract;
import com.example.andriod.myfinalpopularmovies.models.Movie;
import com.example.andriod.myfinalpopularmovies.models.MovieList;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.DecimalFormat;
import java.util.ArrayList;

/**
 * A placeholder fragment containing a simple view.
 */
public class MainActivityFragment extends Fragment {

    public MainActivityFragment() {
    }
    private String LOG_TAG = getClass().getSimpleName();
    private GridView mGridView;
    private ImageAdapter mImageAdapter;
    private String mSortOrder;
    private SharedPreferences mPrefs;
    private Context mContext;
    private boolean mTwoPane;
    private Callback mCallback;


    public interface Callback
    {
        public boolean isTwodesign();
        public void onItemClick(Movie movie);
    }



    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState)
    {
        View rootView = inflater.inflate(R.layout.fragment_main, container, false);
        mContext = getActivity();

        mCallback = (MainActivity)mContext;
        mTwoPane = mCallback.isTwodesign();

        //Get the current sort order
        mPrefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
        mSortOrder = mPrefs.getString(getString(R.string.prefs_sort_key), getString(R.string.prefs_sort_default));

        mGridView = (GridView)rootView.findViewById(R.id.GridMovieView);
        mImageAdapter = new ImageAdapter(getActivity(), new ArrayList<Movie>(), mTwoPane);
        mGridView.setAdapter(mImageAdapter);
        mGridView.setOnItemClickListener(new AdapterView.OnItemClickListener()
        {
            public void onItemClick(AdapterView<?> parent, View v,
                                    int position, long id)
            {
                Movie wSelectedMovie = (Movie) mImageAdapter.getMovies().get(position);
                mCallback.onItemClick(wSelectedMovie);
            }
        });

        if (savedInstanceState != null)
        {
            //See if the sort order has changed since the last time this screen was visible
            mSortOrder = savedInstanceState.getString("sortOrder");
            String wSortOption = mPrefs.getString(getString(R.string.prefs_sort_key), getString(R.string.prefs_sort_default));
            if (!mSortOrder.equals(wSortOption))
            {
                //The user has changed the sort order in preferences.  Update the list to the new
                //sort order
                mSortOrder = wSortOption;
                updateMovies();
            }
            else
            {
                MovieList wMovieList = (MovieList)savedInstanceState.getSerializable("movies");
                mImageAdapter = new ImageAdapter(getActivity(), wMovieList.getMovies(), mTwoPane);
                mGridView.setAdapter(mImageAdapter);
            }
        }
        return rootView;
    }

    @Override
    public void onSaveInstanceState(final Bundle outState)
    {
        super.onSaveInstanceState(outState);
        //Save the first viewable position of the gridview so this position can be reset
        //when the view is loaded.
        outState.putInt("gridviewScrollPosition", mGridView.getFirstVisiblePosition());
        outState.putString("sortOrder", mSortOrder);
        //Create an instance of MovieList to pass as a serializable to the onSaveInstanceState method
        MovieList wMovieList = new MovieList();
        wMovieList.setMovies(mImageAdapter.getMovies());
        outState.putSerializable("movies", wMovieList);
    }

    public void updateMovies()
    {
        mImageAdapter.clear();
        mImageAdapter.notifyDataSetChanged();
        SharedPreferences wPrefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
        mSortOrder = wPrefs.getString(getString(R.string.prefs_sort_key), getString(R.string.prefs_sort_default));
        FetchMoviesTask wFetchMoviesTask = new FetchMoviesTask();
        wFetchMoviesTask.execute(new String[]{mSortOrder});
    }

    @Override
    public void onStart()
    {
        super.onStart();
        Configuration config = getResources().getConfiguration();
        if (config.orientation == Configuration.ORIENTATION_PORTRAIT || mTwoPane)
        {
            mGridView.setNumColumns(2);
        }
        else
        {
            mGridView.setNumColumns(3);
        }
        String wSortOption = mPrefs.getString(getString(R.string.prefs_sort_key), getString(R.string.prefs_sort_default));
        if (mGridView.getAdapter().getCount() == 0)
        {
            updateMovies();
        }
        //Always refresh if viewing favorites in case one is removed while in detail view
        else if (!mSortOrder.equals(wSortOption) || mSortOrder.equals(getString(R.string.favorites_sort_param)))
        {
            //The user has changed the sort order in preferences.  Update the list to the new
            //sort order
            mSortOrder = wSortOption;
            updateMovies();
        }
    }

    public class FetchMoviesTask extends AsyncTask<String, Void, Void>
    {
        private final String LOG_TAG = FetchMoviesTask.class.getSimpleName();

        protected Void doInBackground(String... params) {
            // These two need to be declared outside the try/catch
            // so that they can be closed in the finally block.
            HttpURLConnection urlConnection = null;
            BufferedReader reader = null;

            // Will contain the raw JSON response as a string.
            String moviesJsonString = null;

            String apiKey = getString(R.string.movie_api_key);

            try {
                if (null != params && params[0].equals(getString(R.string.favorites_sort_param)))
                {
                    ArrayList<Movie> wMovies = new ArrayList<>();
                    //Get movies with db query
                    //Set image adapter
                    Cursor wMovieCursor = mContext.getContentResolver().query(
                            MovieContract.MovieEntry.CONTENT_URI_ALL,
                            MovieContract.MovieEntry.FULL_PROJECTION,
                            null, null,
                            MovieContract.MovieEntry.COLUMN_MOVIE_VOTE_AVERAGE + " DESC"
                    );

                    while (wMovieCursor.moveToNext())
                    {
                        Movie wMovie = new Movie();
                        wMovie.setId(wMovieCursor.getInt(MovieContract.MovieEntry.COLUMN_ID_INT));
                        wMovie.setMovieId(wMovieCursor.getString(MovieContract.MovieEntry.COLUMN_MOVIE_ID_INT));
                        wMovie.setTitle(wMovieCursor.getString(MovieContract.MovieEntry.COLUMN_MOVIE_TITLE_INT));
                        wMovie.setBackdropPath(wMovieCursor.getString(MovieContract.MovieEntry.COLUMN_MOVIE_BACKDROP_PATH_INT));
                        wMovie.setPosterPath(wMovieCursor.getString(MovieContract.MovieEntry.COLUMN_MOVIE_POSTER_PATH_INT));
                        wMovie.setOverview(wMovieCursor.getString(MovieContract.MovieEntry.COLUMN_MOVIE_OVERVIEW_INT));
                        wMovie.setPopularity(wMovieCursor.getFloat(MovieContract.MovieEntry.COLUMN_MOVIE_POPULARITY_INT));

                        double wVoteAverage = wMovieCursor.getFloat(MovieContract.MovieEntry.COLUMN_MOVIE_VOTE_AVERAGE_INT);
                        DecimalFormat wFormat = new DecimalFormat("#.#");
                        wVoteAverage = Double.valueOf(wFormat.format(wVoteAverage));

                        wMovie.setVoteAverage(wVoteAverage);
                        wMovie.setVoteCount(wMovieCursor.getInt(MovieContract.MovieEntry.COLUMN_MOVIE_VOTE_COUNT_INT));
                        wMovie.setReleaseDate(wMovieCursor.getLong(MovieContract.MovieEntry.COLUMN_MOVIE_RELEASE_DATE_INT));
                        wMovies.add(wMovie);
                    }

                    mImageAdapter.setMovies(wMovies);
                }
                else
                {
                    // Construct the URL for the MoviesDB query
                    //https://api.themoviedb.org/3/discover/movie?sort_by=popularity.desc&api_key=@APIKEY@
                    final String MOVIE_BASE_URL = getString(R.string.movie_base_url);
                    final String SORT_BY_PARAM = "sort_by";
                    final String API_KEY_PARAM = "api_key";

                    Uri builtUri = Uri.parse(MOVIE_BASE_URL).buildUpon()
                            .appendQueryParameter(SORT_BY_PARAM, params[0])
                            .appendQueryParameter(API_KEY_PARAM, apiKey)
                            .build();

                    URL url = new URL(builtUri.toString());
                    Log.v(LOG_TAG, "Built URI " + builtUri.toString());

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
                    moviesJsonString = buffer.toString();
                    mImageAdapter.clear();
                    try
                    {
                        mImageAdapter.setMovies(getMovieDataFromJson(moviesJsonString));
                    } catch (JSONException e)
                    {
                        e.printStackTrace();
                    }
                }

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
            return null;
        }

        protected void onPostExecute(Void params) {
            mImageAdapter.notifyDataSetChanged();
        }
        private ArrayList<Movie> getMovieDataFromJson(String movieJsonStr)
                throws JSONException
        {
            ArrayList<Movie> wMovies = new ArrayList<>();

            // These are the names of the JSON objects that need to be extracted.
            final String MOVIE_RESULTS = "results";
            final String MOVIE_BACKDROP_PATH = "backdrop_path";
            final String MOVIE_ID = "id";
            final String MOVIE_OVERVIEW = "overview";
            final String MOVIE_RELEASE_DATE = "release_date";
            final String MOVIE_POSTER_PATH = "poster_path";
            final String MOVIE_POPULARITY = "popularity";
            final String MOVIE_TITLE = "title";
            final String MOVIE_VOTE_AVERAGE = "vote_average";
            final String MOVIE_VOTE_COUNT = "vote_count";

            JSONObject movieJson = new JSONObject(movieJsonStr);
            JSONArray movieArray = movieJson.getJSONArray(MOVIE_RESULTS);

            for(int i = 0; i < movieArray.length(); i++)
            {
                try
                {
                    String backdropPath;
                    String movieId;
                    String overview;
                    long releaseDate;
                    String posterPath;
                    double popularity = 0;
                    String title;
                    double voteAverage = 0;
                    int voteCount = 0;

                    JSONObject thisMovie = movieArray.getJSONObject(i);

                    backdropPath = thisMovie.getString(MOVIE_BACKDROP_PATH);
                    movieId = thisMovie.getString(MOVIE_ID);
                    overview = thisMovie.getString(MOVIE_OVERVIEW);
                    releaseDate = Long.parseLong(thisMovie.getString(MOVIE_RELEASE_DATE).replace("-", ""));
                    posterPath = thisMovie.getString(MOVIE_POSTER_PATH);
                    popularity = Double.parseDouble(thisMovie.getString(MOVIE_POPULARITY));
                    title = thisMovie.getString(MOVIE_TITLE);
                    voteAverage = Double.parseDouble(thisMovie.getString(MOVIE_VOTE_AVERAGE));
                    voteCount = Integer.parseInt(thisMovie.getString(MOVIE_VOTE_COUNT));

                    Movie thisMovieObject = new Movie(movieId, title, backdropPath, posterPath,
                            overview, popularity, voteAverage, voteCount, releaseDate);
                    wMovies.add(thisMovieObject);
                }
                catch (Exception ex)
                {
                    Log.e(LOG_TAG, "Failed to add movie. Error: " + ex.toString());
                }
            }
            return wMovies;
        }
    }
}
