package com.example.andriod.myfinalpopularmovies.models;

import java.io.Serializable;

public class Review implements Serializable
{
    public String movId;
    public String movAuthor;
    public String movContent;

    public String getId()
    {
        return movId;
    }

    public void setId(final String id)
    {
        movId = id;
    }

    public String getAuthor()
    {
        return movAuthor;
    }

    public void setAuthor(final String author)
    {
        movAuthor = author;
    }

    public String getContent()
    {
        return movContent;
    }

    public void setContent(final String content)
    {
        movContent = content;
    }
}