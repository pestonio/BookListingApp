package com.example.android.booklistingapp;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    //Variable to log errors as they occur
    //Google Books API URL
    private static final String GOOGLE_BOOKS_URL = "https://www.googleapis.com/books/v1/volumes?q=";
    private static final String LOG_TAG = MainActivity.class.getSimpleName();
    //Variable for the user inputs (text to search for and search button)
    private EditText searchText;
    private Button searchButton;

    //Variable for the ListView to populate
    private ListView listView;

    private ArrayList<Book> bookArrayList;
    private BookListAdapter adapter;
    private TextView emptyJson;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //Find the EditText, Error Text and Button objects
        searchButton = (Button) findViewById(R.id.search_button);
        emptyJson = (TextView) findViewById(R.id.error_text);


        //Find the list_view
        listView = (ListView) findViewById(R.id.list);
        bookArrayList = new ArrayList<Book>();
        adapter = new BookListAdapter(this, bookArrayList);
        listView.setAdapter(adapter);


        searchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                adapter.clear();
                searchText = (EditText) findViewById(R.id.search_text);
                String userInput = searchText.getText().toString().replace(" ", "+");
                if (userInput.isEmpty()) {
                    Context context = getApplicationContext();
                    CharSequence text = "Nothing Entered in Search";
                    int duration = Toast.LENGTH_SHORT;

                    Toast toast = Toast.makeText(context, text, duration);
                    toast.show();
                }
                BookAsyncTask task = new BookAsyncTask();
                task.execute(userInput);
            }
        });
    }

    private void updateUi(List<Book> book) {

        if (book.isEmpty()) {
            emptyJson.setText("No Results Found. Try Alternative Search");
        } else {
            emptyJson.setText("");
            adapter.addAll(book);
            adapter.notifyDataSetChanged();
        }
    }

    //Create a URL from a string
    private URL createUrl(String stringUrl) {
        URL url = null;
        try {
            url = new URL(stringUrl);
        } catch (MalformedURLException exception) {
            Log.e(LOG_TAG, "Error with creating URL", exception);
            return null;
        }
        return url;
    }

    //HTTP request to the URL returning a string
    private String makeHttpRequest(URL url) throws IOException {
        String jsonResponse = "";

        if (url == null) {
            return jsonResponse;
        }
        HttpURLConnection urlConnection = null;
        InputStream inputStream = null;
        try {
            urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setRequestMethod("GET");
            urlConnection.setReadTimeout(10000 /* milliseconds */);
            urlConnection.setConnectTimeout(15000 /* milliseconds */);
            urlConnection.connect();
            inputStream = urlConnection.getInputStream();
            jsonResponse = readFromStream(inputStream);
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (urlConnection != null) {
                urlConnection.disconnect();
            } else {
                Log.e(LOG_TAG, "Error" + urlConnection.getResponseCode());
            }
            if (inputStream != null) {
                // function must handle java.io.IOException here
                inputStream.close();
            }
        }
        return jsonResponse;
    }

    //Convert the input stream into a string
    private String readFromStream(InputStream inputStream) throws IOException {
        StringBuilder output = new StringBuilder();
        if (inputStream != null) {
            InputStreamReader inputStreamReader = new InputStreamReader(inputStream, Charset.forName("UTF-8"));
            BufferedReader reader = new BufferedReader(inputStreamReader);
            String line = reader.readLine();
            while (line != null) {
                output.append(line);
                line = reader.readLine();
            }
        }
        return output.toString();
    }

    private List<Book> extractFeatureFromJson(String bookJSON) {
        List<Book> bookList = new ArrayList<>();
        if (TextUtils.isEmpty(bookJSON)) {
            return null;
        }
        try {
            JSONObject baseJsonObject = new JSONObject(bookJSON);
            JSONArray itemsArray = baseJsonObject.getJSONArray("items");

            for (int i = 0; i < itemsArray.length(); i++) {
                JSONObject responseObject = itemsArray.getJSONObject(i);
                JSONObject volumeInfo = responseObject.getJSONObject("volumeInfo");
                String actualAuthor = "N/A";
                if (volumeInfo.has("authors")) {
                    JSONArray authorsArray = volumeInfo.getJSONArray("authors");
                    actualAuthor = authorsArray.getString(0);
                }
                String bookTitle = volumeInfo.getString("title");
                Book book = new Book(actualAuthor, bookTitle);
                Log.d(LOG_TAG, "extractFeatureFromJson " + book.toString());
                bookList.add(book);
            }
        } catch (JSONException e) {
            Log.e(LOG_TAG, "Problem parsing the Book JSON results", e);
        }
        return bookList;
    }

    //Async Task to perform the network request, get and parse the JSON response
    private class BookAsyncTask extends AsyncTask<String, Void, List<Book>> {

        @Override
        protected List<Book> doInBackground(String... strings) {
            URL url = createUrl(GOOGLE_BOOKS_URL + strings[0] + "&maxResults=20");
            String jsonResponse = "";
            try {
                jsonResponse = makeHttpRequest(url);
            } catch (IOException e) {
                e.printStackTrace();
            }
            List<Book> book = extractFeatureFromJson(jsonResponse);

            return book;
        }

        @Override
        protected void onPostExecute(List<Book> books) {
            if (books == null) {
                return;
            }
            updateUi(books);
        }
    }
}

