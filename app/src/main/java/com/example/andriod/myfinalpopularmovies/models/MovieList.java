package com.example.andriod.myfinalpopularmovies.models;

import java.io.Serializable;
import java.util.ArrayList;

public class MovieList implements Serializable
{
    public ArrayList<Movie> mMovies;

    public ArrayList<Movie> getMovies()
    {
        return mMovies;
    }

    public void setMovies(final ArrayList<Movie> movies)
    {
        mMovies = movies;
    }
}