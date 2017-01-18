package com.example.android.booklistingapp;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.ArrayList;

/**
 * Created by pestonio on 07/09/2016.
 */
public class BookListAdapter extends ArrayAdapter<Book> {

    public BookListAdapter(Context context, ArrayList<Book> books) {
        super(context, 0, books);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent){
        View listItemView = convertView;
        if (listItemView == null){
            listItemView = LayoutInflater.from(getContext()).inflate(R.layout.list_item, parent, false);

            Book currentBook = getItem(position);
            TextView authorView = (TextView) listItemView.findViewById(R.id.author);
            authorView.setText(currentBook.getAuthor());
            TextView bookTitleView = (TextView) listItemView.findViewById(R.id.book_title);
            bookTitleView.setText(currentBook.getBookTitle());
        }
        else {
        }
        return listItemView;
    }
}
