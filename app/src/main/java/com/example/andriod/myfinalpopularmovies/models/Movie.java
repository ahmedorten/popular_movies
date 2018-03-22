package com.example.andriod.myfinalpopularmovies.models;

import java.io.Serializable;
import java.util.ArrayList;

public class Movie implements Serializable
{
    public long mId;
    public String mMovieId;
    public String mBackdropPath;
    public String mPosterPath;
    public String mTitle;
    public String mOverview;
    public double mPopularity;
    public double mVoteAverage;
    public int mVoteCount;
    public long mReleaseDate;
    public ArrayList<Trailer> mTrailers;
    public ArrayList<Review> mReviews;
    public boolean mIsFavorite;

    public Movie(){
    }

    public Movie(String movieId, String title, String backdropPath, String posterPath,
                 String overview, double popularity, double voteAverage, int voteCount,
                 long releaseDate)
    {
        this.mMovieId = movieId;
        this.mTitle = title;
        this.mBackdropPath = backdropPath;
        this.mPosterPath = posterPath;
        this.mOverview = overview;
        this.mPopularity = popularity;
        this.mVoteAverage = voteAverage;
        this.mVoteCount = voteCount;
        this.mReleaseDate = releaseDate;
    }

    public long getId()
    {
        return mId;
    }

    public void setId(final long id)
    {
        mId = id;
    }

    public String getMovieId()
    {
        return mMovieId;
    }

    public void setMovieId(final String movieId)
    {
        mMovieId = movieId;
    }

    public String getBackdropPath()
    {
        return mBackdropPath;
    }

    public void setBackdropPath(final String backdropPath)
    {
        mBackdropPath = backdropPath;
    }

    public String getPosterPath()
    {
        return mPosterPath;
    }

    public void setPosterPath(final String posterPath)
    {
        mPosterPath = posterPath;
    }

    public String getTitle()
    {
        return mTitle;
    }

    public void setTitle(final String title)
    {
        mTitle = title;
    }

    public String getOverview()
    {
        return mOverview;
    }

    public void setOverview(final String overview)
    {
        mOverview = overview;
    }

    public long getReleaseDate()
    {
        return mReleaseDate;
    }

    public void setReleaseDate(final long releaseDate)
    {
        mReleaseDate = releaseDate;
    }

    public double getPopularity()
    {
        return mPopularity;
    }

    public void setPopularity(final double popularity)
    {
        mPopularity = popularity;
    }

    public double getVoteAverage()
    {
        return mVoteAverage;
    }

    public void setVoteAverage(final double voteAverage)
    {
        mVoteAverage = voteAverage;
    }

    public int getVoteCount()
    {
        return mVoteCount;
    }

    public void setVoteCount(final int voteCount)
    {
        mVoteCount = voteCount;
    }

    public ArrayList<Trailer> getTrailers()
    {
        return mTrailers;
    }

    public void setTrailers(final ArrayList<Trailer> trailers)
    {
        mTrailers = trailers;
    }

    public ArrayList<Review> getReviews()
    {
        return mReviews;
    }

    public void setReviews(final ArrayList<Review> reviews)
    {
        mReviews = reviews;
    }

    public boolean isFavorite()
    {
        return mIsFavorite;
    }

    public void setIsFavorite(final boolean isFavorite)
    {
        mIsFavorite = isFavorite;
    }
}