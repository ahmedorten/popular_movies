package com.example.andriod.myfinalpopularmovies;


import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.andriod.myfinalpopularmovies.models.Movie;
import com.example.andriod.myfinalpopularmovies.models.MyButtonInterface;
import com.example.andriod.myfinalpopularmovies.models.Review;
import com.example.andriod.myfinalpopularmovies.models.Trailer;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

public class DetailListViewAdapter extends BaseAdapter {


    private Context mContext;
    private Movie mMovie;
    private ArrayList<Review> mReviews;
    private ArrayList<Trailer> mTrailers;
    private final String LOG_TAG = DetailListViewAdapter.class.getSimpleName();
    private MyButtonInterface mMyButtonInterface;
    private Button mFavoriteButton;
    private DetailActivityFragment mFragment;

    public DetailListViewAdapter(Context context, DetailActivityFragment fragment, Movie movie, MyButtonInterface myButtonInterface)
    {
        mContext = context;
        mMovie = movie;
        mReviews = mMovie.getReviews();
        mTrailers = mMovie.getTrailers();
        mMyButtonInterface = myButtonInterface;
        mFragment = fragment;
    }

    @Override
    public int getCount()
    {
        if (mReviews.size() > 0 || mTrailers.size() > 0)
        {
            return 2 + mReviews.size() + mTrailers.size();
        }
        else return 1;
    }

    @Override
    public Object getItem(final int position)
    {
        return null;
    }

    @Override
    public long getItemId(final int position)
    {
        return 0;
    }

    @Override
    public View getView(final int position, View convertView, final ViewGroup parent)
    {
        LayoutInflater inflater = ((Activity) mContext).getLayoutInflater();
        if (position == 0)
        {
            convertView = inflater.inflate(R.layout.detail_rows, null);
            convertView = getDetailView(convertView);
        }
        else if (position == 1 && mTrailers.size() > 0)
        {
            convertView = inflater.inflate(R.layout.first_trailer_row, null);
        }
        else if (position > 1 && position <= mTrailers.size() + 1) // This is a trailer row
        {
            int arrayPosition = position - 2;
            convertView = inflater.inflate(R.layout.trailer_rows, null);
            convertView = getTrailerView(convertView, arrayPosition);
            TextView wTextView = (TextView)convertView.findViewById(R.id.textView);
            wTextView.setText(mTrailers.get(arrayPosition).getName());
        }
        else if (position > mTrailers.size()) // This is a rating row
        {
            int arrayPosition = position - (mTrailers.size() + 2);
            if (arrayPosition == 0)
            {
                convertView = inflater.inflate(R.layout.first_review_row, null);
            }
            else
            {
                convertView = inflater.inflate(R.layout.review_rows, null);
            }
            convertView = getReviewView(convertView, arrayPosition);
        }

        return convertView;
    }

    @Override
    public boolean areAllItemsEnabled()
    {
        return true;
    }

    @Override
    public boolean isEnabled(final int position)
    {
        if (position > 1 && position <= mTrailers.size() + 1)
        {
            return true;
        }
        return false;
    }

    private View getTrailerView(View convertView, int arrayPosition)
    {
        Trailer wTrailer = mTrailers.get(arrayPosition);
        return convertView;
    }

    private View getReviewView(View convertView, int arrayPosition)
    {
        Review wReview = mReviews.get(arrayPosition);
        TextView wAuthorTextView = (TextView)convertView.findViewById(R.id.AuthorTextview);
        wAuthorTextView.setText(wReview.getAuthor());
        TextView wReviewTextView = (TextView)convertView.findViewById(R.id.reviewTextview);
        wReviewTextView.setText(wReview.getContent());
        return convertView;
    }

    private View getDetailView(View rootView)
    {
        //Set the poster image
        ImageView wPosterImage = (ImageView)rootView.findViewById(R.id.MoviesPoster);
        String imageBaseUrl = mContext.getString(R.string.poster_base_url);
        String imagePosterPath = mMovie.mPosterPath;
        String imagePath = imageBaseUrl + imagePosterPath;
        Picasso.with(mContext).load(imagePath).into(wPosterImage);

        //Set the release date
        TextView wReleaseDate = (TextView)rootView.findViewById(R.id.ReleaseYear);
        wReleaseDate.setText(String.valueOf(mMovie.getReleaseDate()).substring(0,4));

        //Set the button and listener
        mFavoriteButton = (Button)rootView.findViewById(R.id.FavoriteButton);
        mFragment.setToggleButton(mFavoriteButton);
        setButtonText();

        //Set the listener inside of the DetailFragment so the star in the action bar can have
        //the same action
        mFavoriteButton.setOnClickListener((DetailActivityFragment)mFragment);

        //Set the rating
        TextView wRatingTextView = (TextView)rootView.findViewById(R.id.MovieRating);
        wRatingTextView.setText(mMovie.getVoteAverage() + " " + mContext.getString(R.string.vote_suffix));

        //Set the overview
        TextView wOverview = (TextView)rootView.findViewById(R.id.MovieOverview);
        String wOverviewText = mMovie.getOverview();
        if (wOverviewText != null)
        {
            if (wOverviewText.equals("null"))
            {
                wOverviewText = mContext.getString(R.string.synopsis_not_available);
            }
        }
        wOverview.setText(wOverviewText);
        return rootView;
    }

    public void setButtonText()
    {
        if (mFavoriteButton == null) return;
        if (mMovie.isFavorite())
        {
            mFavoriteButton.setText(mContext.getString(R.string.favorite_button_text_remove));
        }
        else
        {
            mFavoriteButton.setText(mContext.getString(R.string.favorite_button_text_mark));
        }

    }

}
