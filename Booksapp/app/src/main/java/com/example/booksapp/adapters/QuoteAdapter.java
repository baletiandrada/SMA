package com.example.booksapp.adapters;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.booksapp.R;
import com.example.booksapp.dataModels.QuoteModel;
import com.example.booksapp.helpers.BookStorageHelper;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.ArrayList;
import java.util.HashMap;

import static com.example.booksapp.helpers.FirebaseHelper.mQuotesDatabase;

public class QuoteAdapter extends RecyclerView.Adapter<QuoteAdapter.ViewHolder>{
    private ArrayList<QuoteModel> quotes;
    private Context context;

    FirebaseAuth mAuth = FirebaseAuth.getInstance();
    FirebaseUser currentUser = mAuth.getCurrentUser();

    BookStorageHelper bookStorageHelper = BookStorageHelper.getInstance();

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

        viewHolder.cancel_edit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                viewHolder.quoteText.setVisibility(View.VISIBLE);
                viewHolder.itemView.findViewById(R.id.iv_arrow_down).setVisibility(View.VISIBLE);
                viewHolder.itemView.findViewById(R.id.layout_edit_quote).setVisibility(View.GONE);
                viewHolder.itemView.findViewById(R.id.layout_close_edit).setVisibility(View.GONE);
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

    @Override
    public int getItemCount() {
        return quotes.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder{
        public TextView quoteText;
        public EditText editQuote;
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
