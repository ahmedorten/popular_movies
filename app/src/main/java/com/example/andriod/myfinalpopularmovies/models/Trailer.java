package com.example.andriod.myfinalpopularmovies.models;


import java.io.Serializable;

public class Trailer implements Serializable
{
    public String mId;
    public String mKey;
    public String mName;

    public String getId()
    {
        return mId;
    }

    public void setId(final String id)
    {
        mId = id;
    }

    public String getKey()
    {
        return mKey;
    }

    public void setKey(final String key)
    {
        mKey = key;
    }

    public String getName()
    {
        return mName;
    }

    public void setName(final String name)
    {
        mName = name;
    }

}