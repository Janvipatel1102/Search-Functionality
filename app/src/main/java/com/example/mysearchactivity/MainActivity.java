package com.example.mysearchactivity;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import io.reactivex.Observable;

import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;

import com.example.mysearchactivity.adapters.categoryAdapter;
import com.example.mysearchactivity.model.CategoryWiseItems;
import com.example.mysearchactivity.model.categories;
import com.example.mysearchactivity.viewModels.categoryViewModel;
import com.example.mysearchactivity.viewModels.categoryWiseViewModel;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import android.app.SearchManager;
import android.view.MenuItem;
import android.widget.SearchView;
import android.widget.SearchView.OnQueryTextListener;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.util.List;
import java.util.Map;

public class MainActivity extends AppCompatActivity {

    FirebaseDatabase firebaseDatabase;
    DatabaseReference databaseReference;

    private categoryViewModel mcategoryViewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        firebaseDatabase = FirebaseDatabase.getInstance();
        databaseReference = firebaseDatabase.getReference("categories");
        mcategoryViewModel = new ViewModelProvider(this).get(categoryViewModel.class);

        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                mcategoryViewModel.deletAllCategories();
                for(DataSnapshot snapshot1:snapshot.getChildren())
                {
                    categories category=snapshot1.getValue(categories.class);
                    mcategoryViewModel.insert(category);
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                //   Toast.makeText(MainActivity.class, "Fail to get data.", Toast.LENGTH_SHORT).show();
            }
        });



       /* RecyclerView recyclerView = findViewById(R.id.recyclerview);
        final categoryAdapter adapter = new categoryAdapter(this);
        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));


        mcategoryViewModel.getAllCategories().observe(this, new Observer<List<categories>>() {
            @Override
            public void onChanged(@Nullable final List<categories> categories) {
                // Update the cached copy of the words in the adapter.
               adapter.setCategory(categories);

            }
        });*/



        databaseReference = firebaseDatabase.getReference("categoryWiseItems");
        categoryWiseViewModel mcategoryWiseViewModel;

        mcategoryWiseViewModel = new ViewModelProvider(this).get(categoryWiseViewModel.class);

        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                mcategoryWiseViewModel.deletAllCategoryWiseItems();

                for(DataSnapshot snapshot1:snapshot.getChildren())
                {
                    String category_of_item = snapshot1.getKey();
                    for(DataSnapshot snapshot2:snapshot1.getChildren())
                    {
                        Map<String, String> value = (Map<String, String>) snapshot2.getValue();

                        JSONObject obj = new JSONObject(value);
                        try {
                            String name = obj.getString("name");
                            String  ImageUrl = obj.getString("photosUrl");
                            String discount = obj.getString("discount");
                            String ratingCount = obj.getString("peopleRatingCount");
                            String rating = obj.getString("ratingTotal");
                            String price = obj.getString("price");
                            String item_id =obj.getString("key");

                            JSONParser parser = new JSONParser();
                            JSONArray array = new JSONArray(ImageUrl);
                            String image =array.getString(0);
                            Log.d(String.valueOf(MainActivity.this),image);


                            CategoryWiseItems category = new CategoryWiseItems(item_id,name, discount,ratingCount,rating,price,category_of_item,image);
                            mcategoryWiseViewModel.insert(category);

                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }

                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                //   Toast.makeText(MainActivity.class, "Fail to get data.", Toast.LENGTH_SHORT).show();
            }
        });

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.searchmenu, menu);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {

        switch (item.getItemId())
        {
            case R.id.search:
                Intent intent = new Intent(MainActivity.this,searchDialog.class);
                startActivity(intent);
                return  true;
            default:
                return super.onOptionsItemSelected(item);
        }

    }
}