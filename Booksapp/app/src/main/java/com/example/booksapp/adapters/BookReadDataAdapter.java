package com.example.booksapp.adapters;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.booksapp.AppConstants;
import com.example.booksapp.BookEditActivity;
import com.example.booksapp.dataModels.BookReadData;
import com.example.booksapp.helpers.BookStorageHelper;
import com.example.booksapp.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

import java.util.List;

import static com.example.booksapp.helpers.FirebaseHelper.mBooksReadDatabase;

public class BookReadDataAdapter extends RecyclerView.Adapter<BookReadDataViewHolder>{
    private List<BookReadData> choicesList;
    private Context context;

    public BookReadDataAdapter(List<BookReadData> bookList){
        this.choicesList = bookList;
    }

    @NonNull
    @Override
    public BookReadDataViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        context = parent.getContext();
        LayoutInflater inflater = LayoutInflater.from(context);
        View contactView = inflater. inflate(R.layout.row_book_read_data, parent, false);
        BookReadDataViewHolder viewHolder = new BookReadDataViewHolder(contactView);
        return viewHolder;
    }


    @Override
    public void onBindViewHolder(@NonNull BookReadDataViewHolder holder, int position) {
        BookReadData bookModel = choicesList.get(position);
        holder.itemView.findViewById(R.id.tv_genre).setVisibility(View.GONE);
        holder.setValues(bookModel.getAuthor_name(), bookModel.getTitle(), bookModel.getRead_month(), bookModel.getRead_year());
        holder.itemView.findViewById(R.id.iv_delete_image).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(context);
                builder.setMessage("Are you sure you want to delete this book?")
                        .setPositiveButton("YES", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                if(!(choicesList==null)) {
                                    choicesList.remove(position);
                                    notifyItemRemoved(position);
                                    Toast.makeText(context, "Book deleted successfully", Toast.LENGTH_SHORT).show();

                                    FirebaseAuth mAuth = FirebaseAuth.getInstance();
                                    FirebaseUser currentUser = mAuth.getCurrentUser();
                                    if (currentUser != null)
                                        mBooksReadDatabase.child(currentUser.getUid()).child(bookModel.getId()).removeValue();
                                }
                            }
                        }).setNegativeButton("CANCEL", null);

                AlertDialog alert = builder.create();
                alert.show();
            }
        });

        holder.itemView.findViewById(R.id.iv_edit_icon).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v){
                BookStorageHelper bookStorageHelper = BookStorageHelper.getInstance();
                bookStorageHelper.setValues(bookModel.getAuthor_name(), bookModel.getTitle(), bookModel.getRead_month(), bookModel.getRead_year());
                bookStorageHelper.setId_book(bookModel.getId());
                Intent intent = new Intent(context, BookEditActivity.class);
                String param_bookTable_value = "Read books";
                intent.putExtra(AppConstants.param_bookTable, param_bookTable_value);
                context.startActivity(intent);
            }
        });
        holder.itemView.findViewById(R.id.iv_check_book_image).setVisibility(View.GONE);
    }

    @Override
    public int getItemCount() {
        return choicesList.size();
    }
}