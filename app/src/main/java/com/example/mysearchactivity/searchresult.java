package com.example.mysearchactivity;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
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
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AbsListView;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

import com.example.mysearchactivity.adapters.categoryAdapter;
import com.example.mysearchactivity.adapters.categoryWiseAdapter;
import com.example.mysearchactivity.model.CategoryWiseItems;
import com.example.mysearchactivity.model.categories;
import com.example.mysearchactivity.persistant.categoryDao;
import com.example.mysearchactivity.reposataries.categoryRepo;
import com.example.mysearchactivity.viewModels.categoryViewModel;
import com.example.mysearchactivity.viewModels.categoryWiseViewModel;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.json.JSONException;
import org.json.JSONObject;


import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

public class searchresult<async> extends AppCompatActivity {

    FirebaseDatabase firebaseDatabase;
    DatabaseReference databaseReference;
    private  categoryWiseViewModel categoryWiseViewModel;
    private  categoryViewModel categoryViewModel;
     categoryWiseAdapter adapter;
     List<CategoryWiseItems> categoryWiseItems = new ArrayList<CategoryWiseItems>(),tempCategoryWiseItems = new ArrayList<>();
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


    BottomSheetDialog bottomSheetDialog;
    RadioButton price_low,price_high,relevance,popularity;

    Button sort_button;

    Comparator<CategoryWiseItems> compareByPrice = new Comparator<CategoryWiseItems>() {
        @Override
        public int compare(CategoryWiseItems o1, CategoryWiseItems o2) {
            return o1.getPrice_with_discount().compareTo(o2.getPrice_with_discount());
        }
    };


    Comparator<CategoryWiseItems> compareByPopularity = new Comparator<CategoryWiseItems>() {
        @Override
        public int compare(CategoryWiseItems o1, CategoryWiseItems o2) {
            return o1.getRating().compareTo(o2.getRating());
        }
    };


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


        sort_button = (Button) findViewById(R.id.sort_button);
        sort_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                sortItems();
            }
        });




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
                    tempCategoryWiseItems = categoryWiseItems;
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
                        tempCategoryWiseItems = categoryWiseItems;
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


        public void sortItems()
        {
            bottomSheetDialog = new BottomSheetDialog(searchresult.this,R.style.BottomSheetTheme);
            View sheetView = LayoutInflater.from(getApplicationContext()).inflate(R.layout.sort_bottom_sheet,
                    findViewById(R.id.bottom_sheet));

            price_low = (RadioButton)sheetView.findViewById(R.id.price_low);
            price_high = (RadioButton)sheetView.findViewById(R.id.price_high);
            relevance = (RadioButton)sheetView.findViewById(R.id.relevance);
            popularity = (RadioButton)sheetView.findViewById(R.id.popularity);


            price_low.setOnClickListener(new View.OnClickListener() {
                @RequiresApi(api = Build.VERSION_CODES.N)
                @Override
                public void onClick(View v) {
                    if(price_low.isChecked())
                    {
                        sort_list_price_low_to_high();
                        Toast.makeText(searchresult.this, "Price law", Toast.LENGTH_SHORT).show();
                      //  price_low.setChecked(false);
                        bottomSheetDialog.dismiss();
                    }
                }
            });


            price_high.setOnClickListener(new View.OnClickListener() {
                @RequiresApi(api = Build.VERSION_CODES.N)
                @Override
                public void onClick(View v) {
                    if(price_high.isChecked())
                    {
                        sort_list_price_high_to_low();
                        Toast.makeText(searchresult.this, "Price high", Toast.LENGTH_SHORT).show();
                      //  price_high.setChecked(false);
                        bottomSheetDialog.dismiss();
                    }
                }
            });


            relevance.setOnClickListener(new View.OnClickListener() {
                @RequiresApi(api = Build.VERSION_CODES.N)
                @Override
                public void onClick(View v) {
                    if(relevance.isChecked())
                    {

                        sort_list_price_relevance();
                        Toast.makeText(searchresult.this, "Relevance ", Toast.LENGTH_SHORT).show();
                        bottomSheetDialog.dismiss();
                    }
                }
            });

            popularity.setOnClickListener(new View.OnClickListener() {
                @RequiresApi(api = Build.VERSION_CODES.N)
                @Override
                public void onClick(View v) {
                    if(popularity.isChecked())
                    {
                        sort_list_price_popularity();
                        Toast.makeText(searchresult.this, "Popularity", Toast.LENGTH_SHORT).show();
                        bottomSheetDialog.dismiss();
                    }
                }
            });


            bottomSheetDialog.setContentView(sheetView);
            bottomSheetDialog.show();


        }

    public void sort_list_price_low_to_high()
    {

        Collections.sort(categoryWiseItems, compareByPrice);
        adapter.notifyDataSetChanged();

    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    public void sort_list_price_high_to_low()
    {

        Collections.sort(categoryWiseItems, compareByPrice.reversed());
        adapter.notifyDataSetChanged();
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    public void sort_list_price_popularity()
    {

        Collections.sort(categoryWiseItems, compareByPopularity.reversed());
        adapter.notifyDataSetChanged();
    }
    public void sort_list_price_relevance()
    {

        categoryWiseItems = tempCategoryWiseItems;
        adapter.notifyDataSetChanged();
    }

}