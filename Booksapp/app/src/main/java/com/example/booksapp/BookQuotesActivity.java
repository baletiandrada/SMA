package com.example.booksapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.widget.NestedScrollView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.annotation.SuppressLint;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.MultiAutoCompleteTextView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.booksapp.adapters.QuoteAdapter;
import com.example.booksapp.dataModels.QuoteModel;
import com.example.booksapp.helpers.BookStorageHelper;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static com.example.booksapp.helpers.FirebaseHelper.mBooksReadDatabase;
import static com.example.booksapp.helpers.FirebaseHelper.mBooksRecommendedDatabase;
import static com.example.booksapp.helpers.FirebaseHelper.mQuotesDatabase;

public class BookQuotesActivity extends AppCompatActivity {

    private TextView title, author;
    private RecyclerView recyclerView;
    private QuoteAdapter quoteAdapter;
    private List<QuoteModel> quotes_list = new ArrayList<QuoteModel>();
    FloatingActionButton fab;
    private MultiAutoCompleteTextView add_quote_text;
    private Button add_quote_btn, cancel_add_quote_btn;


    private FirebaseAuth mAuth = FirebaseAuth.getInstance();

    BookStorageHelper bookStorageHelper = BookStorageHelper.getInstance();

    ArrayList<String> texts_from_book_description = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_book_quotes);

        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,WindowManager.LayoutParams.FLAG_FULLSCREEN);
        getSupportActionBar().hide();

        initializeViews();
        title.setText(bookStorageHelper.getBook_title());
        author.setText(bookStorageHelper.getAuthor_name());

        mQuotesDatabase.child(bookStorageHelper.getId_book()).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                getData(snapshot);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(getApplicationContext(), error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });

        final NestedScrollView nestedScrollview = findViewById(R.id.quotes_activ_nested_scroll_view);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                findViewById(R.id.card_view_add_quote).setVisibility(View.VISIBLE);

                nestedScrollview.post(new Runnable() {
                    @Override
                    public void run() {
                        nestedScrollview.fullScroll(NestedScrollView.FOCUS_UP);
                    }
                });

                final Handler handler = new Handler();
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        add_quote_text.requestFocus();
                    }
                }, 10);
            }
        });


        getTextFromAllBooks();
        final boolean[] lower_case = {false};
        Character[] end_characters = {'.', ',', ';', '!', '?', ' '};
        add_quote_text.setTokenizer(new SpaceTokenizer());
        ListView listView = findViewById(R.id.words_for_quote_lv);
        add_quote_text.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @SuppressLint("SetTextI18n")
            @Override
            public void afterTextChanged(Editable s) {
                if(add_quote_text.getText().toString().length()>1){
                    String text_input = add_quote_text.getText().toString();
                    List<String> text_splitted = Arrays.asList(text_input.split("\\s+"));
                    String last_word = text_splitted.get(text_splitted.size()-1);
                    if(last_word.length()>1 && !Arrays.asList(end_characters).contains(text_input.charAt(text_input.length()-1)))
                        listView.setVisibility(View.VISIBLE);
                    else
                        listView.setVisibility(View.GONE);
                    if(!texts_from_book_description.contains(last_word)){
                        if (text_splitted.size() > 1) {
                            String word_bef_last = text_splitted.get(text_splitted.size()-2);
                            if(word_bef_last.endsWith(".") || word_bef_last.endsWith("?") || word_bef_last.endsWith("!")){
                                texts_from_book_description = changeFirstLetter(texts_from_book_description);
                                lower_case[0] = false;
                            }
                            else if(!lower_case[0]){
                                texts_from_book_description = listToLowerCase(texts_from_book_description);
                                lower_case[0] = true;
                            }
                        }

                        ArrayList<String> words = FullTextSearch.searchForText(last_word, texts_from_book_description);
                        listView.setAdapter(new ArrayAdapter<>(getApplicationContext(), android.R.layout.simple_list_item_1, words));
                    }
                    else listView.setVisibility(View.GONE);
                }
                else{
                    listView.setVisibility(View.GONE);
                    texts_from_book_description = changeFirstLetter(texts_from_book_description);
                    lower_case[0] = false;
                }
            }
        });

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @SuppressLint("SetTextI18n")
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String input = add_quote_text.getText().toString();
                if(input.contains(" ")){
                    StringBuilder input1 = replaceTextWithItemView(input);
                    input = String.valueOf(input1);
                    add_quote_text.setText(input + parent.getItemAtPosition(position));
                }
                else
                    add_quote_text.setText(parent.getItemAtPosition(position).toString());
                add_quote_text.setSelection(add_quote_text.getText().toString().length());
                listView.setVisibility(View.GONE);
            }
        });

        add_quote_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addToDatabase(v);
            }
        });

        cancel_add_quote_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                findViewById(R.id.card_view_add_quote).setVisibility(View.GONE);
            }
        });

    }

    public void getTextFromAllBooks(){
        mBooksRecommendedDatabase.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for(DataSnapshot dataSnapshot: snapshot.getChildren()){
                    String description = String.valueOf(dataSnapshot.child("description").getValue());
                    if(description!=null){
                        description = description.toLowerCase();
                        Character[] end_characters = {'.', ',', ';', '!', '?'};
                        ArrayList<Character> end_characters_arraylist = new ArrayList<>();
                        end_characters_arraylist.addAll(Arrays.asList(end_characters));
                        List<String> words = Arrays.asList(description.split("\\s+"));
                        for (String word : words) {
                            if(end_characters_arraylist.contains(word.charAt(word.length()-1))){
                                String substring = word.substring(0, word.length()-1);
                                word = substring;
                            }
                            if (!texts_from_book_description.contains(word)) {
                                texts_from_book_description.add(word);
                            }
                        }
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    public ArrayList<String> listToLowerCase(ArrayList<String> words_list){
        ArrayList<String> words_list_aux = new ArrayList<>();
        for(String word : words_list){
            word = word.toLowerCase();
            words_list_aux.add(word);
        }
        words_list.clear();
        words_list.addAll(words_list_aux);
        return words_list;
    }

    public ArrayList<String> changeFirstLetter(ArrayList<String> words_list){
        ArrayList<String> words_list_aux = new ArrayList<>();
        for(String word : words_list){
            if(word.charAt(0)>='a' && word.charAt(0)<='z'){
                String firstCh = String.valueOf(word.charAt(0));
                firstCh = firstCh.toUpperCase();
                String substring = word.substring(1);
                firstCh = firstCh.concat(substring);
                words_list_aux.add(firstCh);
            }
            else words_list_aux.add(word);
        }
        words_list.clear();
        words_list.addAll(words_list_aux);
        return words_list;
    }

    public StringBuilder replaceTextWithItemView(String text){
        StringBuilder input1 = new StringBuilder();
        input1.append(text);
        input1.reverse();
        input1.replace(0, input1.indexOf(" "), "");
        input1.reverse();
        return input1;
    }



    @Override
    public void onStart() {
        super.onStart();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) {
            goToLoginActivity();
        }
    }

    public void goToLoginActivity(){
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
    }

    @Override
    public void onResume(){
        super.onResume();
        mQuotesDatabase.child(bookStorageHelper.getId_book()).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                getData(snapshot);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(getApplicationContext(), error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void getData(DataSnapshot dataSnapshot) {
        quotes_list.removeAll(quotes_list);
        for(DataSnapshot ds : dataSnapshot.getChildren()){
            String text = String.valueOf(ds.child("text_quote").getValue());
            QuoteModel newQuote = new QuoteModel(text);
            newQuote.setId(String.valueOf(ds.getKey()));
            quotes_list.add(newQuote);
        }
        if(!quotes_list.isEmpty())
            setRecyclerView();
    }

    public void setRecyclerView(){
        quoteAdapter = new QuoteAdapter((ArrayList<QuoteModel>) quotes_list);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(quoteAdapter);
    }

    public void addToDatabase(View view) {
        if (add_quote_text.getText() == null || add_quote_text.getText().toString().isEmpty())
        {
            Toast.makeText(getApplicationContext(), "Review field is empty", Toast.LENGTH_SHORT).show();
            return;
        }

        QuoteModel newQuote = new QuoteModel(add_quote_text.getText().toString());
        String quote_id = mBooksReadDatabase.child(bookStorageHelper.getId_book()).push().getKey();
        mQuotesDatabase.child(bookStorageHelper.getId_book()).child(quote_id).setValue(newQuote);
        Toast.makeText(getApplicationContext(), "Quote added successfully", Toast.LENGTH_SHORT).show();
        add_quote_text.setText(null);
        findViewById(R.id.card_view_add_quote).setVisibility(View.GONE);
    }

    public void initializeViews(){
        title = findViewById(R.id.tv_title_info);
        author = findViewById(R.id.tv_author_info);
        recyclerView = findViewById(R.id.rv_quotes_info);
        fab = findViewById(R.id.fab_info);

        add_quote_text = findViewById(R.id.et_add_quote);
        add_quote_btn = findViewById(R.id.btn_add_quote);
        cancel_add_quote_btn = findViewById(R.id.btn_cancel_add_quote);
        findViewById(R.id.card_view_add_quote).setVisibility(View.GONE);
    }
}