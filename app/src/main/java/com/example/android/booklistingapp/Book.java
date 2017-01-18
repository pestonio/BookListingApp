package com.example.android.booklistingapp;

import java.util.ArrayList;

/**
 * Created by pestonio on 07/09/2016.
 */
public class Book extends ArrayList<Book> {

    private String mAuthor;
    private String mBookTitle;

    public Book(String author, String bookTitle) {
        mAuthor = author;
        mBookTitle = bookTitle;
    }

    public String getAuthor() {
        return mAuthor;
    }

    public String getBookTitle() {
        return mBookTitle;
    }
}
