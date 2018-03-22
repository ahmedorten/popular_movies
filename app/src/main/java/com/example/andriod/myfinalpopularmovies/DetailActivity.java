package com.example.andriod.myfinalpopularmovies;

import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;


import com.example.andriod.myfinalpopularmovies.models.Movie;

public class DetailActivity extends ActionBarActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);

       // getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        if (savedInstanceState == null)
        {
            DetailActivityFragment wDetailFragment = new DetailActivityFragment();
            Bundle wBundle = new Bundle();
            Movie wMovie = (Movie)getIntent().getSerializableExtra("movie");
            wBundle.putSerializable("movie", wMovie);
            wDetailFragment.setArguments(wBundle);
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.movie_detail_container, wDetailFragment)
                    .commit();
        }
    }
}
