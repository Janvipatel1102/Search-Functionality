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
import android.widget.CheckBox;
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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class searchresult<async> extends AppCompatActivity {

    FirebaseDatabase firebaseDatabase;
    DatabaseReference databaseReference;
    private  categoryWiseViewModel categoryWiseViewModel;
    private  categoryViewModel categoryViewModel;
     categoryWiseAdapter adapter;
     List<CategoryWiseItems> categoryWiseItems = new ArrayList<CategoryWiseItems>(),tempCategoryWiseItems = new ArrayList<>()
             ,filterCategoryItems = new ArrayList<>();
     List<String> allCategories = new ArrayList<>();
     RecyclerView recyclerView;
    TextView emptyView,search_bar;
 //   int offset = 0;
   // int limit = Constants.Limit;
    private boolean loading = false;
    int scroll_out_items, visibleItemCount, totalItemCount;
    String query;
    LinearLayoutManager mLayoutManager;
    boolean shouldLoad=true;
    ProgressBar progressBar;
    LinearLayout searchBox;


    BottomSheetDialog bottomSheetDialog,bottomSheetDialogFilter;
    RadioButton price_low,price_high,relevance,popularity;
    View sheetView,filterSheetView;

    Button sort_button,filter_button;


    TextView price_filter,discount_filter,rating_filter,avaliability_filter,clear_filter;
    LinearLayout price_filter_layout,discount_filter_layout,rating_filter_layout,avaibility_filter_layout;

    CheckBox below_250,between_251_500,between_501_1000,between_1001_2000
            ,between_2001_5000,between_5001_10000,above_100001,more_then_70,more_then_60,more_then_50,more_then_40
            ,more_then_30,below_30,above_4,above_3,above_2,above_1,include_out_of_stock,exclude_out_of_stock;
    Button apply_filter;
    boolean ischecked= false,dis_below_30 = false;


    Map<Integer,Integer> map=new HashMap<Integer,Integer>();




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
            doMySearch(query);
        }
      //  fetchDataOnScrolling();


        sort_button = (Button) findViewById(R.id.sort_button);
        filter_button = (Button)findViewById(R.id.filter_button);

        bottomSheetDialog = new BottomSheetDialog(searchresult.this,R.style.BottomSheetTheme);
        sheetView = LayoutInflater.from(getApplicationContext()).inflate(R.layout.sort_bottom_sheet,
                findViewById(R.id.bottom_sheet));

        bottomSheetDialogFilter = new BottomSheetDialog(searchresult.this,R.style.BottomSheetTheme);
        filterSheetView = LayoutInflater.from(getApplicationContext()).inflate(R.layout.filter_bottom_sheet,
                findViewById(R.id.bottom_filter_sheet_main));

        filter_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                filterItems();
            }
        });



        sort_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                sortItems();
            }
        });








    }

    public  void doMySearch(String Query)
    {

        progressBar.setVisibility(View.VISIBLE);
        String tag = Query.split(" ")[0];
        String query = Query.split(" ")[1];
        if(tag .equals("categories"))
        {
            categoryWiseViewModel.getItemByCategory("%"+query+"%").observe(this, new Observer<List<CategoryWiseItems>>() {
                @Override
                public void onChanged(List<CategoryWiseItems> categoryWiseItems1) {
                   /* if(categoryWiseItems1.size()<limit && categoryWiseItems1.size()>0)
                    {
                        shouldLoad=false;

                    }
                   */
                    if(categoryWiseItems1.size()==0)
                    {
                        recyclerView.setVisibility(View.GONE);
                        emptyView.setVisibility(View.VISIBLE);
                    }
                    else
                    {
                        recyclerView.setVisibility(View.VISIBLE);
                        emptyView.setVisibility(View.GONE);
                    }

                //    Log.d(String.valueOf(searchresult.this),"Size of category "+String.valueOf(categoryWiseItems1.size()));
                   categoryWiseItems.clear();
                    tempCategoryWiseItems.clear();
                    categoryWiseItems.addAll( categoryWiseItems1);
                    Log.d(String.valueOf(searchresult.this),"Size of category "+String.valueOf(categoryWiseItems.size()));
                    tempCategoryWiseItems.addAll(categoryWiseItems1);
                    adapter.notifyDataSetChanged();
                    progressBar.setVisibility(View.GONE);

                }
            });
        }
        else {
           // Log.d(String.valueOf(searchresult.this),"In Items items ");
            categoryWiseViewModel.getItemByName("%" + query + "%").observe(this, new Observer<List<CategoryWiseItems>>() {
                    @Override
                    public void onChanged(@Nullable final List<CategoryWiseItems> categories) {
                        // Update the cached copy of the words in the adapter.
                     /*   if(categories.size()<limit && categories.size()>0)
                        {
                            shouldLoad=false;

                        }
                     */   if(categories.size()==0)
                        {
                            recyclerView.setVisibility(View.GONE);
                            emptyView.setVisibility(View.VISIBLE);
                        }
                        else
                        {
                            recyclerView.setVisibility(View.VISIBLE);
                            emptyView.setVisibility(View.GONE);
                        }
                        categoryWiseItems.clear();
                        tempCategoryWiseItems.clear();
                        categoryWiseItems.addAll(categories);
                        tempCategoryWiseItems.addAll(categories);
                        adapter.notifyDataSetChanged();
                        progressBar.setVisibility(View.GONE);

                    }
                });
            }

       }

      /* public void fetchDataOnScrolling()
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
*/

        public void sortItems()
        {

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
                     //   Toast.makeText(searchresult.this, "Price law", Toast.LENGTH_SHORT).show();
                      //  price_low.setChecked(false);
                       // relevance.setChecked(false);
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
                      //  Toast.makeText(searchresult.this, "Price high", Toast.LENGTH_SHORT).show();
                      //  price_high.setChecked(false);
                       // relevance.setChecked(false);
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
                      //  Toast.makeText(searchresult.this, "Relevance ", Toast.LENGTH_SHORT).show();
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
                        //relevance.setChecked(false);
                      //  Toast.makeText(searchresult.this, "Popularity", Toast.LENGTH_SHORT).show();
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
        categoryWiseItems.clear();
        categoryWiseItems.addAll(tempCategoryWiseItems);
        adapter.notifyDataSetChanged();
    }



    //filter Items

    public  void filterItems()
    {

        price_filter = filterSheetView.findViewById(R.id.price_filter);
        discount_filter = filterSheetView.findViewById(R.id.discount_filter);
        avaliability_filter = filterSheetView.findViewById(R.id.avaliability_filter);
        rating_filter = filterSheetView.findViewById(R.id.rating_filter);

        price_filter_layout = filterSheetView.findViewById(R.id.price_filter_layout);
        discount_filter_layout = filterSheetView.findViewById(R.id.discount_filter_layout);
        avaibility_filter_layout = filterSheetView.findViewById(R.id.outof_stock_filter_layout);
        rating_filter_layout = filterSheetView.findViewById(R.id.customer_filter_layout);

        apply_filter = filterSheetView.findViewById(R.id.apply_button);
        clear_filter = filterSheetView.findViewById(R.id.clear_filters);

        below_250 = filterSheetView.findViewById(R.id.below_250);
        between_251_500 =filterSheetView.findViewById(R.id.between_251_500);
        between_501_1000 = filterSheetView.findViewById(R.id.between_501_1000);
        between_1001_2000 = filterSheetView.findViewById(R.id.between_1001_2000);
        between_2001_5000 = filterSheetView.findViewById(R.id.between_2001_5000);
        between_5001_10000 = filterSheetView.findViewById(R.id.between_5001_10000);
        above_100001 = filterSheetView.findViewById(R.id.above_10001);

        more_then_30 = filterSheetView.findViewById(R.id.more_than_30);
        more_then_40 = filterSheetView.findViewById(R.id.more_than_40);
        more_then_50 = filterSheetView.findViewById(R.id.more_than_50);
        more_then_60 = filterSheetView.findViewById(R.id.more_than_60);
        more_then_70 = filterSheetView.findViewById(R.id.more_than_70);
        below_30 = filterSheetView.findViewById(R.id.below_30);

        above_1 = filterSheetView.findViewById(R.id.more_than_1);
        above_2 = filterSheetView.findViewById(R.id.more_than_2);
        above_3 = filterSheetView.findViewById(R.id.more_than_3);
        above_4 = filterSheetView.findViewById(R.id.more_than_4);


      //  include_out_of_stock = filterSheetView.findViewById(R.id.include_out_of_stock);
        exclude_out_of_stock = filterSheetView.findViewById(R.id.exclude_out_of_stock);

        price_filter.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                price_filter.setBackgroundColor(Color.WHITE);
                discount_filter.setBackgroundColor(getResources().getColor(R.color.lightest_grey));
                avaliability_filter.setBackgroundColor(getResources().getColor(R.color.lightest_grey));
                rating_filter.setBackgroundColor(getResources().getColor(R.color.lightest_grey));



                price_filter_layout.setVisibility(View.VISIBLE);
                discount_filter_layout.setVisibility(View.GONE);
                avaibility_filter_layout.setVisibility(View.GONE);
                rating_filter_layout.setVisibility(View.GONE);


            }
        });



        discount_filter.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                discount_filter.setBackgroundColor(Color.WHITE);
                price_filter.setBackgroundColor(getResources().getColor(R.color.lightest_grey));
                avaliability_filter.setBackgroundColor(getResources().getColor(R.color.lightest_grey));
                rating_filter.setBackgroundColor(getResources().getColor(R.color.lightest_grey));


                discount_filter_layout.setVisibility(View.VISIBLE);
                price_filter_layout.setVisibility(View.GONE);
                avaibility_filter_layout.setVisibility(View.GONE);
                rating_filter_layout.setVisibility(View.GONE);


            }
        });


        avaliability_filter.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                avaliability_filter.setBackgroundColor(Color.WHITE);
                discount_filter.setBackgroundColor(getResources().getColor(R.color.lightest_grey));
                price_filter.setBackgroundColor(getResources().getColor(R.color.lightest_grey));
                rating_filter.setBackgroundColor(getResources().getColor(R.color.lightest_grey));


                avaibility_filter_layout.setVisibility(View.VISIBLE);
                discount_filter_layout.setVisibility(View.GONE);
                price_filter_layout.setVisibility(View.GONE);
                rating_filter_layout.setVisibility(View.GONE);


            }
        });

        rating_filter.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                rating_filter.setBackgroundColor(Color.WHITE);
                discount_filter.setBackgroundColor(getResources().getColor(R.color.lightest_grey));
                avaliability_filter.setBackgroundColor(getResources().getColor(R.color.lightest_grey));
                price_filter.setBackgroundColor(getResources().getColor(R.color.lightest_grey));


                rating_filter_layout.setVisibility(View.VISIBLE);
                discount_filter_layout.setVisibility(View.GONE);
                price_filter_layout.setVisibility(View.GONE);
                avaibility_filter_layout.setVisibility(View.GONE);
            }
        });





        apply_filter.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if(below_250.isChecked())
                {
                    ischecked = true;
                    Toast.makeText(searchresult.this, "below 250 ", Toast.LENGTH_SHORT).show();
                    price_filter_list(0F,250F);
                }
                if(between_251_500.isChecked())
                {
                    ischecked = true;
                    Toast.makeText(searchresult.this, "between 251 to 500 ", Toast.LENGTH_SHORT).show();
                    price_filter_list(251F,500F);

                }

                if(between_501_1000.isChecked())
                {
                    ischecked = true;
                    Toast.makeText(searchresult.this, "between 501 to 1000 ", Toast.LENGTH_SHORT).show();
                    price_filter_list(501F,1000F);

                }

                if(between_1001_2000.isChecked())
                {
                    ischecked = true;
                    Toast.makeText(searchresult.this, "between 1001 to 2000 ", Toast.LENGTH_SHORT).show();
                    price_filter_list(1001F,2000F);

                }

                if(between_2001_5000.isChecked())
                {
                    ischecked = true;
                    Toast.makeText(searchresult.this, "between 2001 to 5000 ", Toast.LENGTH_SHORT).show();
                    price_filter_list(2001F,5000F);

                }

                if(between_5001_10000.isChecked())
                {
                    ischecked = true;
                    Toast.makeText(searchresult.this, "between 5001 to 10000 ", Toast.LENGTH_SHORT).show();
                    price_filter_list(5001F,10000F);

                }
                if(above_100001.isChecked())
                {
                    ischecked = true;
                    Toast.makeText(searchresult.this, "between 100001 ", Toast.LENGTH_SHORT).show();
                    price_filter_list(100001F, Float.MAX_VALUE);
                }



                //discount

                if(more_then_70.isChecked())
                {
                    ischecked = true;
                    Toast.makeText(searchresult.this, "more then 70", Toast.LENGTH_SHORT).show();
                    discount_filter_list(70F);

                }
                if(more_then_60.isChecked())
                {
                    ischecked = true;
                    Toast.makeText(searchresult.this, "more then 60", Toast.LENGTH_SHORT).show();
                    discount_filter_list(60F);

                }
                if(more_then_50.isChecked())
                {
                    ischecked = true;
                    Toast.makeText(searchresult.this, "more then 50", Toast.LENGTH_SHORT).show();
                    discount_filter_list(50F);

                }
                if(more_then_40.isChecked())
                {
                    ischecked = true;
                    Toast.makeText(searchresult.this, "more then 40", Toast.LENGTH_SHORT).show();
                    discount_filter_list(40F);

                }
                if(more_then_30.isChecked())
                {
                    ischecked = true;
                    Toast.makeText(searchresult.this, "more then 30", Toast.LENGTH_SHORT).show();
                    discount_filter_list(30F);

                }

                if(below_30.isChecked())
                {
                    ischecked = true;
                    dis_below_30 = true;
                    Toast.makeText(searchresult.this, "below then 70", Toast.LENGTH_SHORT).show();
                    discount_filter_list(30F);

                }

                    //rating filters


                if(above_1.isChecked())
                {
                    ischecked = true;
                    Toast.makeText(searchresult.this, "above 1  rating", Toast.LENGTH_SHORT).show();
                    rating_filter_list(1F);

                }

                if(above_2.isChecked())
                {
                    ischecked = true;
                    Toast.makeText(searchresult.this, "above 2  rating", Toast.LENGTH_SHORT).show();
                    rating_filter_list(2F);

                }

                if(above_3.isChecked())
                {
                    ischecked = true;
                    Toast.makeText(searchresult.this, "above 3  rating", Toast.LENGTH_SHORT).show();
                    rating_filter_list(3F);

                }

                if(above_4.isChecked())
                {
                    ischecked = true;
                    Toast.makeText(searchresult.this, "above 4  rating", Toast.LENGTH_SHORT).show();
                    rating_filter_list(4F);

                }

                    //instock filters
                if(exclude_out_of_stock.isChecked())
                {
                    ischecked = true;
                    Toast.makeText(searchresult.this, "exclude out of stock", Toast.LENGTH_SHORT).show();
                    exclude_out_of_stock_filter_list();
                }



                Log.d(String.valueOf(searchresult.this), String.valueOf(filterCategoryItems.size()));
                bottomSheetDialogFilter.dismiss();
                categoryWiseItems.clear();

                if(ischecked)
                {
                    categoryWiseItems.addAll(filterCategoryItems);
                    filterCategoryItems.clear();

                }
                else
                {
                    categoryWiseItems.addAll(tempCategoryWiseItems);

                }
                adapter.notifyDataSetChanged();
                ischecked=false;
                map.clear();
            }
        });

        clear_filter.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                bottomSheetDialogFilter.dismiss();
                Toast.makeText(searchresult.this, "clear filters", Toast.LENGTH_SHORT).show();
                categoryWiseItems.clear();
                filterCategoryItems.clear();
                categoryWiseItems.addAll(tempCategoryWiseItems);
                adapter.notifyDataSetChanged();
                ischecked=false;
                allCheckBoxFalse();
                map.clear();
            }
        });



        bottomSheetDialogFilter.setContentView(filterSheetView);
        bottomSheetDialogFilter.show();

    }

    public void price_filter_list(Float min_price,Float max_price)
    {

        for(int i=0;i<categoryWiseItems.size();i++)
        {
            CategoryWiseItems cartModel = categoryWiseItems.get(i);
            Float price = cartModel.getPrice_with_discount();
            if((price>=min_price) && (price<=max_price) && (!map.containsKey(i)))
            {
                map.put(i,1);
                filterCategoryItems.add(cartModel);
            }

        }
    }

    public void discount_filter_list(Float discount)
    {

        for(int i=0;i<categoryWiseItems.size();i++)
        {
            CategoryWiseItems cartModel = categoryWiseItems.get(i);
            Float discount1 = cartModel.getDiscount();

            if(dis_below_30)
            {
                if((discount1<=discount) && (!map.containsKey(i)))
                {
                    map.put(i,1);
                    filterCategoryItems.add(cartModel);
                }
            }
            else if((discount1>=discount) && (!map.containsKey(i)))
            {
                map.put(i,1);
                filterCategoryItems.add(cartModel);
            }

        }
        dis_below_30=false;
    }



    public  void rating_filter_list(Float rating)
    {

        for(int i=0;i<categoryWiseItems.size();i++)
        {
            CategoryWiseItems cartModel = categoryWiseItems.get(i);
            Float item_rating = cartModel.getRating();
            if((item_rating>=rating) && (!map.containsKey(i)))
            {
                map.put(i,1);
                filterCategoryItems.add(cartModel);
            }

        }

    }

    public void exclude_out_of_stock_filter_list()
    {
        for(int i=0;i<categoryWiseItems.size();i++)
        {
            CategoryWiseItems cartModel = categoryWiseItems.get(i);
            boolean instock = cartModel.isIn_stock();
            if((!instock) && (!map.containsKey(i)))
            {
                map.put(i,1);
                filterCategoryItems.add(cartModel);
            }

        }

    }




    public void allCheckBoxFalse()
    {
        filterSheetView = LayoutInflater.from(getApplicationContext()).inflate(R.layout.filter_bottom_sheet,
                findViewById(R.id.bottom_filter_sheet_main));

    }



}