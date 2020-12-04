package com.example.secondapp_recyclerview;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

import java.util.List;

import static com.example.secondapp_recyclerview.FirebaseHelper.mBooksReadDatabase;

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
        holder.setValues(bookModel.getAuthor_name(), bookModel.getTitle(), bookModel.getRead_year());
        holder.itemView.findViewById(R.id.iv_delete_image).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v){
                choicesList.remove(position);
                notifyItemRemoved(position);
                Toast.makeText(context,"Book deleted successfully", Toast.LENGTH_SHORT).show();

                mBooksReadDatabase.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                         mBooksReadDatabase.child(bookModel.getId()).removeValue();
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                        Toast.makeText(context, databaseError.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });

            }
        });

        holder.itemView.findViewById(R.id.iv_edit_icon).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v){
                BookStorageHelper bookStorageHelper = BookStorageHelper.getInstance();
                bookStorageHelper.setValues(bookModel.getAuthor_name(), bookModel.getTitle(), bookModel.getRead_year());
                bookStorageHelper.setId_book(bookModel.getId());
                Intent intent = new Intent(context, BookEditActivity.class);
                context.startActivity(intent);
            }
        });


    }

    private void removeBookFromDatabase(DataSnapshot dataSnapshot, BookReadData bookModel) {
        for(DataSnapshot ds : dataSnapshot.getChildren()){
            if(String.valueOf(ds.child("author_name").getValue()).equals(bookModel.getAuthor_name()) && String.valueOf(ds.child("title").getValue()).equals(bookModel.getTitle()))
                mBooksReadDatabase.child(ds.getKey()).removeValue();
        }
    }

    @Override
    public int getItemCount() {
        return choicesList.size();
    }
}
