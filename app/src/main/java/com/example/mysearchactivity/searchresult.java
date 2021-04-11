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
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AbsListView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.example.mysearchactivity.adapters.categoryAdapter;
import com.example.mysearchactivity.adapters.categoryWiseAdapter;
import com.example.mysearchactivity.model.CategoryWiseItems;
import com.example.mysearchactivity.model.categories;
import com.example.mysearchactivity.persistant.categoryDao;
import com.example.mysearchactivity.reposataries.categoryRepo;
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

public class searchresult<async> extends AppCompatActivity {

    FirebaseDatabase firebaseDatabase;
    DatabaseReference databaseReference;
    private  categoryWiseViewModel categoryWiseViewModel;
    private  categoryViewModel categoryViewModel;
     categoryWiseAdapter adapter;
     List<CategoryWiseItems> categoryWiseItems = new ArrayList<CategoryWiseItems>();
     List<String> allCategories = new ArrayList<>();

     RecyclerView recyclerView;
    TextView emptyView,search_bar;
    int offset = 0;
    int limit = Constants.Limit;
    private boolean loading = false;
    int scroll_out_items, visibleItemCount, totalItemCount;
    String query;
    LinearLayoutManager mLayoutManager;
    boolean shouldLoad=true;
    ProgressBar progressBar;

    LinearLayout searchBox;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_searchresult);

        emptyView = (TextView) findViewById(R.id.empty_view);
        recyclerView = findViewById(R.id.search_result_list);
        adapter = new categoryWiseAdapter(this);
        recyclerView.setAdapter(adapter);
        mLayoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(mLayoutManager);
        adapter.setCategory(categoryWiseItems);

        categoryWiseViewModel = new ViewModelProvider(this).get(categoryWiseViewModel.class);
        categoryViewModel = new ViewModelProvider(this).get(categoryViewModel.class);


        progressBar = (ProgressBar)findViewById(R.id.progressbar);
        search_bar = (TextView)findViewById(R.id.search_product_name);
        searchBox = (LinearLayout)findViewById(R.id.search);

        searchBox.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(searchresult.this, searchDialog.class);
                intent.setAction(Intent.ACTION_GET_CONTENT);
                intent.putExtra("query", search_bar.getText());
                startActivity(intent);
                finish();
            }
        });

       //  getAllCategories();

        Intent intent = getIntent();
        if (Intent.ACTION_SEARCH.equals(intent.getAction())) {
            query = intent.getStringExtra("query");

            String q= query.substring(query.indexOf(" "));
            search_bar.setText(q);

            Log.d(String.valueOf(searchresult.this),query);
            doMySearch(query,limit,offset);
        }
        fetchDataOnScrolling();

    }

    public  void doMySearch(String Query,int limit,int offset)
    {

        progressBar.setVisibility(View.VISIBLE);
        String tag = Query.split(" ")[0];
        String query = Query.split(" ")[1];
        if(tag .equals("categories"))
        {

            categoryWiseViewModel.getItemByCategory("%"+query+"%",limit,offset).observe(this, new Observer<List<CategoryWiseItems>>() {
                @Override
                public void onChanged(List<CategoryWiseItems> categoryWiseItems1) {
                    if(categoryWiseItems1.size()<limit && categoryWiseItems1.size()>0)
                    {
                        shouldLoad=false;

                    }
                    else if(categoryWiseItems1.size()==0)
                    {
                        recyclerView.setVisibility(View.GONE);
                        emptyView.setVisibility(View.VISIBLE);
                    }
                    else
                    {
                        recyclerView.setVisibility(View.VISIBLE);
                        emptyView.setVisibility(View.GONE);
                    }

                    Log.d(String.valueOf(searchresult.this),"Size of category "+String.valueOf(categoryWiseItems1.size()));
                    categoryWiseItems.addAll(categoryWiseItems1);
                    adapter.notifyDataSetChanged();
                    progressBar.setVisibility(View.GONE);

                }
            });
        }
        else {
           // Log.d(String.valueOf(searchresult.this),"In Items items ");
            categoryWiseViewModel.getItemByName("%" + query + "%",limit,offset).observe(this, new Observer<List<CategoryWiseItems>>() {
                    @Override
                    public void onChanged(@Nullable final List<CategoryWiseItems> categories) {
                        // Update the cached copy of the words in the adapter.
                        if(categories.size()<limit && categories.size()>0)
                        {
                            shouldLoad=false;

                        }
                        if(categories.size()==0)
                        {
                            recyclerView.setVisibility(View.GONE);
                            emptyView.setVisibility(View.VISIBLE);
                        }
                        else
                        {
                            recyclerView.setVisibility(View.VISIBLE);
                            emptyView.setVisibility(View.GONE);
                        }
                        categoryWiseItems.addAll(categories);
                        adapter.notifyDataSetChanged();
                        progressBar.setVisibility(View.GONE);

                    }
                });
            }

       }

       public void fetchDataOnScrolling()
       {

           recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
               @Override
               public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
                   super.onScrollStateChanged(recyclerView, newState);
                   if(newState== AbsListView.OnScrollListener.SCROLL_STATE_TOUCH_SCROLL)
                   {
                       loading=true;
                   }
               }

               @Override
               public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                   super.onScrolled(recyclerView, dx, dy);
                   visibleItemCount = mLayoutManager.getChildCount();//visible items;
                   totalItemCount = mLayoutManager.getItemCount();//total items;
                   scroll_out_items = mLayoutManager.findFirstVisibleItemPosition();// scrooled out  items;

                   if(shouldLoad && loading && scroll_out_items+visibleItemCount >= totalItemCount)
                   {
                       loading=false;
                       offset+=limit;
                       doMySearch(query,limit,offset);

                   }
               }
           });

       }



}