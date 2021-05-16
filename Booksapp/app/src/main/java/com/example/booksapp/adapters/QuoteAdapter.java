package com.example.booksapp.adapters;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.MultiAutoCompleteTextView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.booksapp.FullTextSearch;
import com.example.booksapp.R;
import com.example.booksapp.SpaceTokenizer;
import com.example.booksapp.dataModels.QuoteModel;
import com.example.booksapp.helpers.BookStorageHelper;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import static com.example.booksapp.helpers.FirebaseHelper.mBooksRecommendedDatabase;
import static com.example.booksapp.helpers.FirebaseHelper.mQuotesDatabase;

public class QuoteAdapter extends RecyclerView.Adapter<QuoteAdapter.ViewHolder>{
    private ArrayList<QuoteModel> quotes;
    private Context context;

    private FirebaseAuth mAuth = FirebaseAuth.getInstance();
    private FirebaseUser currentUser = mAuth.getCurrentUser();

    private BookStorageHelper bookStorageHelper = BookStorageHelper.getInstance();

    private ArrayList<String> texts_from_book_description = new ArrayList<>();

    public QuoteAdapter(ArrayList<QuoteModel> quotes)
    {
        this.quotes = quotes;
    }

    @NonNull
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        context = viewGroup.getContext();
        View v = LayoutInflater.from(context).inflate(R.layout.row_change_content, viewGroup, false);
        return new QuoteAdapter.ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull final QuoteAdapter.ViewHolder viewHolder, int i) {
        QuoteModel current_quote = quotes.get(i);
        viewHolder.itemView.findViewById(R.id.layout_open_edit).setVisibility(View.GONE);
        viewHolder.itemView.findViewById(R.id.iv_arrow_up).setVisibility(View.GONE);
        viewHolder.itemView.findViewById(R.id.layout_edit_quote).setVisibility(View.GONE);
        viewHolder.itemView.findViewById(R.id.layout_close_edit).setVisibility(View.GONE);
        viewHolder.itemView.findViewById(R.id.layout_like_icons).setVisibility(View.GONE);
        viewHolder.itemView.findViewById(R.id.layout_text_likes).setVisibility(View.GONE);
        viewHolder.itemView.findViewById(R.id.tv_see_replies).setVisibility(View.GONE);
        viewHolder.quoteText.setText(current_quote.getText_quote());

        viewHolder.itemView.findViewById(R.id.iv_arrow_down).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                viewHolder.itemView.findViewById(R.id.iv_arrow_down).setVisibility(View.GONE);
                viewHolder.itemView.findViewById(R.id.layout_open_edit).setVisibility(View.VISIBLE);
                viewHolder.itemView.findViewById(R.id.iv_arrow_up).setVisibility(View.VISIBLE);
            }
        });

        viewHolder.itemView.findViewById(R.id.iv_arrow_up).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                viewHolder.itemView.findViewById(R.id.iv_arrow_up).setVisibility(View.GONE);
                viewHolder.itemView.findViewById(R.id.layout_open_edit).setVisibility(View.GONE);
                viewHolder.itemView.findViewById(R.id.iv_arrow_down).setVisibility(View.VISIBLE);
            }
        });

        viewHolder.edit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                viewHolder.itemView.findViewById(R.id.iv_arrow_up).setVisibility(View.GONE);
                viewHolder.quoteText.setVisibility(View.GONE);
                viewHolder.itemView.findViewById(R.id.layout_open_edit).setVisibility(View.GONE);
                viewHolder.itemView.findViewById(R.id.layout_edit_quote).setVisibility(View.VISIBLE);
                viewHolder.editQuote.setText(viewHolder.quoteText.getText().toString());
                viewHolder.itemView.findViewById(R.id.layout_close_edit).setVisibility(View.VISIBLE);
            }
        });

        viewHolder.delete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(context);
                builder.setMessage("Are you sure you want to delete this quote?")
                        .setPositiveButton("YES", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                if(!(quotes==null)) {
                                    Toast.makeText(context, "Quote deleted successfully", Toast.LENGTH_SHORT).show();
                                    if (currentUser != null) {
                                        mQuotesDatabase.child(bookStorageHelper.getId_book()).child(current_quote.getId()).removeValue();
                                        quotes.remove(current_quote);
                                        notifyItemRemoved(i);
                                        notifyItemRangeChanged(i, quotes.size());
                                    }

                                }
                            }
                        }).setNegativeButton("CANCEL", null);

                AlertDialog alert = builder.create();
                alert.show();
            }
        });

        ListView listView = viewHolder.itemView.findViewById(R.id.words_for_edit_review_lv);
        viewHolder.cancel_edit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                viewHolder.quoteText.setVisibility(View.VISIBLE);
                viewHolder.itemView.findViewById(R.id.iv_arrow_down).setVisibility(View.VISIBLE);
                viewHolder.itemView.findViewById(R.id.layout_edit_quote).setVisibility(View.GONE);
                viewHolder.itemView.findViewById(R.id.layout_close_edit).setVisibility(View.GONE);
                listView.setVisibility(View.GONE);
            }
        });


        getTextFromAllBooks();
        final boolean[] lower_case = {false};
        Character[] end_characters = {'.', ',', ';', '!', '?', ' '};
        viewHolder.editQuote.setTokenizer(new SpaceTokenizer());

        viewHolder.editQuote.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @SuppressLint("SetTextI18n")
            @Override
            public void afterTextChanged(Editable s) {
                if(viewHolder.editQuote.getText().toString().length()>1){
                    String text_input = viewHolder.editQuote.getText().toString();
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
                        listView.setAdapter(new ArrayAdapter<>(context, android.R.layout.simple_list_item_1, words));
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
                String input = viewHolder.editQuote.getText().toString();
                if(input.contains(" ")){
                    StringBuilder input1 = replaceTextWithItemView(input);
                    input = String.valueOf(input1);
                    viewHolder.editQuote.setText(input + parent.getItemAtPosition(position));
                }
                else
                    viewHolder.editQuote.setText(parent.getItemAtPosition(position).toString());
                viewHolder.editQuote.setSelection(viewHolder.editQuote.getText().toString().length());
                listView.setVisibility(View.GONE);
            }
        });

        viewHolder.save_edit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(viewHolder.editQuote.getText().toString().isEmpty()){
                    Toast.makeText(context, "Quote field is empty", Toast.LENGTH_SHORT).show();
                    return;
                }

                HashMap<String, Object> map = new HashMap<>();
                map.put("text_quote", viewHolder.editQuote.getText().toString());
                mQuotesDatabase.child(bookStorageHelper.getId_book()).child(current_quote.getId()).updateChildren(map);
                Toast.makeText(context, "Quote updated successfully", Toast.LENGTH_SHORT).show();
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
    public int getItemCount() {
        return quotes.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder{
        public TextView quoteText;
        public MultiAutoCompleteTextView editQuote;
        public ImageView delete, edit, save_edit, cancel_edit;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            quoteText = itemView.findViewById(R.id.tv_text_quote);
            editQuote = itemView.findViewById(R.id.et_edit_quote);
            edit = itemView.findViewById(R.id.iv_edit_quote);
            delete = itemView.findViewById(R.id.iv_delete_quote);
            save_edit = itemView.findViewById(R.id.iv_save_edit_quote);
            cancel_edit = itemView.findViewById(R.id.iv_clear_edit_quote);
        }
    }
}
