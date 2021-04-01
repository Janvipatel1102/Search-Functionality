package com.example.mysearchactivity;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.loader.app.LoaderManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.SearchManager;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import com.example.mysearchactivity.adapters.categoryAdapter;
import com.example.mysearchactivity.adapters.categoryWiseAdapter;
import com.example.mysearchactivity.model.CategoryWiseItems;
import com.example.mysearchactivity.model.categories;
import com.example.mysearchactivity.viewModels.categoryViewModel;
import com.example.mysearchactivity.viewModels.categoryWiseViewModel;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.json.JSONException;
import org.json.JSONObject;


import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class searchresult extends AppCompatActivity {

    FirebaseDatabase firebaseDatabase;
    DatabaseReference databaseReference;
    private  categoryWiseViewModel categoryWiseViewModel;
    private  categoryViewModel categoryViewModel;
     categoryWiseAdapter adapter;
     List<String> allCategories = new ArrayList<>();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_searchresult);


        RecyclerView recyclerView = findViewById(R.id.itemrecyclerview);
        adapter = new categoryWiseAdapter(this);
        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        categoryWiseViewModel = new ViewModelProvider(this).get(categoryWiseViewModel.class);
        categoryViewModel = new ViewModelProvider(this).get(categoryViewModel.class);


        //Log.d(String.valueOf(searchresult.this),"In items ");
        Intent intent = getIntent();
        if (Intent.ACTION_SEARCH.equals(intent.getAction())) {
            String query = intent.getStringExtra(SearchManager.QUERY);
            doMySearch(query);
        }

    }

    public  void doMySearch(String Query)
    {

        categoryViewModel.getAllCategoriesNames().observe(this, new Observer<List<String>>() {
            @Override
            public void onChanged(List<String> strings) {

                allCategories = strings;
                Log.d(String.valueOf(searchresult.this),"In items "+String.valueOf(allCategories.size()));
            }
        });

        Log.d(String.valueOf(searchresult.this),String.valueOf(allCategories.size()));
        if(allCategories.contains(Query))
        {

            Log.d(String.valueOf(searchresult.this),"In Category items ");

            categoryWiseViewModel.getItemByCategory("%"+Query+"%").observe(this, new Observer<List<CategoryWiseItems>>() {
                @Override
                public void onChanged(List<CategoryWiseItems> categoryWiseItems) {
                    adapter.setCategory(categoryWiseItems);
                }
            });
        }
        else {
            Log.d(String.valueOf(searchresult.this),"In Items items ");
            categoryWiseViewModel.getItemByName("%" + Query + "%").observe(this, new Observer<List<CategoryWiseItems>>() {
                    @Override
                    public void onChanged(@Nullable final List<CategoryWiseItems> categories) {
                        // Update the cached copy of the words in the adapter.
                        adapter.setCategory(categories);

                    }
                });
            }

       }

}