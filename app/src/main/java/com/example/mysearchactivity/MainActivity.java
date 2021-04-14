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
import com.google.firebase.database.ChildEventListener;
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
    categoryWiseViewModel mcategoryWiseViewModel;


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




        databaseReference = firebaseDatabase.getReference("categoryWiseItems");

        mcategoryWiseViewModel = new ViewModelProvider(this).get(categoryWiseViewModel.class);


        databaseReference.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

                String category_of_item = snapshot.getKey();
                for(DataSnapshot snapshot2:snapshot.getChildren()) {
                    Map<String, String> value = (Map<String, String>) snapshot2.getValue();
                    getDataFromFireBase(value,category_of_item);
                }
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

                String category_of_item = snapshot.getKey();
                getUpdatedDataFromCategory(category_of_item);

            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot snapshot) {

                String category_of_items = snapshot.getKey();
                mcategoryWiseViewModel.deleteCategoryByName(category_of_items);
            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });


        CategoryWiseItems category = new CategoryWiseItems("1","1", 12F,"10",1F,"1200",
                "Fruits & Vegetables",
                " https://firebasestorage.googleapis.com/v0/b/tiwari-mart.appspot.com/o/category_images%2FFoodgrains%2C%20Oil%20%26%20Masala%2FFoodgrains-Oil-Masala-1-min.png?alt=media&token=8d2107e8-fe47-4971-851d-dd8684861db9"
                ,10F,true);


        CategoryWiseItems category1 = new CategoryWiseItems("2","2", 11F,"10",2F,"1200",
                "Fruits & Vegetables",
                " https://firebasestorage.googleapis.com/v0/b/tiwari-mart.appspot.com/o/category_images%2FFoodgrains%2C%20Oil%20%26%20Masala%2FFoodgrains-Oil-Masala-1-min.png?alt=media&token=8d2107e8-fe47-4971-851d-dd8684861db9"
                ,11F,true);

        CategoryWiseItems category2 = new CategoryWiseItems("3","3", 11F,"102",4F,"1200",
                "Fruits & Vegetables",
                " https://firebasestorage.googleapis.com/v0/b/tiwari-mart.appspot.com/o/category_images%2FFoodgrains%2C%20Oil%20%26%20Masala%2FFoodgrains-Oil-Masala-1-min.png?alt=media&token=8d2107e8-fe47-4971-851d-dd8684861db9"
                ,124F,true);

        CategoryWiseItems category3 = new CategoryWiseItems("4","4", 11F,"102",5F,"1200",
                "Fruits & Vegetables",
                " https://firebasestorage.googleapis.com/v0/b/tiwari-mart.appspot.com/o/category_images%2FFoodgrains%2C%20Oil%20%26%20Masala%2FFoodgrains-Oil-Masala-1-min.png?alt=media&token=8d2107e8-fe47-4971-851d-dd8684861db9"
                ,5F,true);
        CategoryWiseItems category4 = new CategoryWiseItems("5","5", 11F,"102",2F,"1200",
                "Fruits & Vegetables",
                " https://firebasestorage.googleapis.com/v0/b/tiwari-mart.appspot.com/o/category_images%2FFoodgrains%2C%20Oil%20%26%20Masala%2FFoodgrains-Oil-Masala-1-min.png?alt=media&token=8d2107e8-fe47-4971-851d-dd8684861db9"
                ,100F,true);
        CategoryWiseItems category5 = new CategoryWiseItems("5","6", 11F,"102",5F,"1200",
                "Fruits & Vegetables",
                " https://firebasestorage.googleapis.com/v0/b/tiwari-mart.appspot.com/o/category_images%2FFoodgrains%2C%20Oil%20%26%20Masala%2FFoodgrains-Oil-Masala-1-min.png?alt=media&token=8d2107e8-fe47-4971-851d-dd8684861db9"
                ,200F,true);

        CategoryWiseItems category6 = new CategoryWiseItems("6","7", 11F,"102",3F,"1200",
                "Fruits & Vegetables",
                " https://firebasestorage.googleapis.com/v0/b/tiwari-mart.appspot.com/o/category_images%2FFoodgrains%2C%20Oil%20%26%20Masala%2FFoodgrains-Oil-Masala-1-min.png?alt=media&token=8d2107e8-fe47-4971-851d-dd8684861db9"
                ,300F,true);

        CategoryWiseItems category8 = new CategoryWiseItems("hello8","8", 11F,"10",2F,"1200",
                "Fruits & Vegetables",
                " https://firebasestorage.googleapis.com/v0/b/tiwari-mart.appspot.com/o/category_images%2FFoodgrains%2C%20Oil%20%26%20Masala%2FFoodgrains-Oil-Masala-1-min.png?alt=media&token=8d2107e8-fe47-4971-851d-dd8684861db9"
                ,2F,true);

        CategoryWiseItems category10 = new CategoryWiseItems("hello10","9", 11F,"102",1F,"1200",
                "Fruits & Vegetables",
                " https://firebasestorage.googleapis.com/v0/b/tiwari-mart.appspot.com/o/category_images%2FFoodgrains%2C%20Oil%20%26%20Masala%2FFoodgrains-Oil-Masala-1-min.png?alt=media&token=8d2107e8-fe47-4971-851d-dd8684861db9"
                ,20F,true);

        CategoryWiseItems category11 = new CategoryWiseItems("hello11","10", 11F,"10",3F,"1200",
                "Fruits & Vegetables",
                " https://firebasestorage.googleapis.com/v0/b/tiwari-mart.appspot.com/o/category_images%2FFoodgrains%2C%20Oil%20%26%20Masala%2FFoodgrains-Oil-Masala-1-min.png?alt=media&token=8d2107e8-fe47-4971-851d-dd8684861db9"
                ,30F,true);

        CategoryWiseItems category9 = new CategoryWiseItems("hello9","11", 11F,"10",5F,"1200",
                "Fruits & Vegetables",
                " https://firebasestorage.googleapis.com/v0/b/tiwari-mart.appspot.com/o/category_images%2FFoodgrains%2C%20Oil%20%26%20Masala%2FFoodgrains-Oil-Masala-1-min.png?alt=media&token=8d2107e8-fe47-4971-851d-dd8684861db9"
                ,12.34F,true);
        CategoryWiseItems category7 = new CategoryWiseItems("hello17","12", 11F,"1230",1F,"1200",
                "Fruits & Vegetables",
                " https://firebasestorage.googleapis.com/v0/b/tiwari-mart.appspot.com/o/category_images%2FFoodgrains%2C%20Oil%20%26%20Masala%2FFoodgrains-Oil-Masala-1-min.png?alt=media&token=8d2107e8-fe47-4971-851d-dd8684861db9"
                ,100F,true);

     /*   CategoryWiseItems category13 = new CategoryWiseItems("hello13","janvi", 11F,"1030",5F,"1200",
                "Fruits & Vegetables",
                " https://firebasestorage.googleapis.com/v0/b/tiwari-mart.appspot.com/o/category_images%2FFoodgrains%2C%20Oil%20%26%20Masala%2FFoodgrains-Oil-Masala-1-min.png?alt=media&token=8d2107e8-fe47-4971-851d-dd8684861db9"
                ,12.34F,true);
*/


        mcategoryWiseViewModel.insert(category);
        mcategoryWiseViewModel.insert(category1);
        mcategoryWiseViewModel.insert(category2);
        mcategoryWiseViewModel.insert(category3);
        mcategoryWiseViewModel.insert(category4);
        mcategoryWiseViewModel.insert(category5);
        mcategoryWiseViewModel.insert(category6);
        mcategoryWiseViewModel.insert(category8);

        mcategoryWiseViewModel.insert(category9);
        mcategoryWiseViewModel.insert(category10);
        mcategoryWiseViewModel.insert(category11);
        mcategoryWiseViewModel.insert(category7);
     //   mcategoryWiseViewModel.insert(category13);






/*        databaseReference.addValueEventListener(new ValueEventListener() {
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
                            Float discount = (float)obj.getDouble("discount");
                            String ratingCount = obj.getString("peopleRatingCount");
                            Float rating = (float) obj.getDouble("ratingTotal");
                            String price = obj.getString("price");
                            String item_id =obj.getString("key");
                            boolean in_stock = obj.getBoolean("inStock");


                            JSONParser parser = new JSONParser();
                            JSONArray array = new JSONArray(ImageUrl);
                            String image =array.getString(0);
                            Log.d(String.valueOf(MainActivity.this),image);


                            Float discount1 = Float.valueOf(discount);
                            Float price1 = Float.valueOf(price);
                            Float price_with_discount = price1 - ((price1*discount1)/100);

                            CategoryWiseItems category = new CategoryWiseItems(item_id,name, discount,ratingCount,rating,price,category_of_item,image,price_with_discount,in_stock);
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
        });*/

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

    public void deleteDataFromRoom(Map<String,String> value,String category_of_item)
    {
        JSONObject obj = new JSONObject(value);
        try {
            String name = obj.getString("name");
            String  ImageUrl = obj.getString("photosUrl");
            Float discount = (float)obj.getDouble("discount");
            String ratingCount = obj.getString("peopleRatingCount");
            Float rating = (float) obj.getDouble("ratingTotal");
            String price = obj.getString("price");
            String item_id =obj.getString("key");
            boolean in_stock = obj.getBoolean("inStock");

            JSONParser parser = new JSONParser();
            JSONArray array = new JSONArray(ImageUrl);
            String image =array.getString(0);
            Log.d(String.valueOf(MainActivity.this),image);


            Float discount1 = Float.valueOf(discount);
            Float price1 = Float.valueOf(price);
            Float price_with_discount = price1 - ((price1*discount1)/100);

            CategoryWiseItems category = new CategoryWiseItems(item_id,name, discount,ratingCount,rating,price,category_of_item,image,price_with_discount,in_stock);
            mcategoryWiseViewModel.delete(category);

        } catch (JSONException e) {
            e.printStackTrace();

        }

    }

    public void getDataFromFireBase(Map<String,String> value,String category_of_item)
    {

        JSONObject obj = new JSONObject(value);
        try {
            String name = obj.getString("name");
            String  ImageUrl = obj.getString("photosUrl");
            Float discount = (float)obj.getDouble("discount");
            String ratingCount = obj.getString("peopleRatingCount");
            Float rating = (float) obj.getDouble("ratingTotal");
            String price = obj.getString("price");
            String item_id =obj.getString("key");
            boolean in_stock = obj.getBoolean("inStock");

            JSONParser parser = new JSONParser();
            JSONArray array = new JSONArray(ImageUrl);
            String image =array.getString(0);
          //  Log.d(String.valueOf(MainActivity.this),image);


            Float discount1 = Float.valueOf(discount);
            Float price1 = Float.valueOf(price);
            Float price_with_discount = price1 - ((price1*discount1)/100);

            CategoryWiseItems category = new CategoryWiseItems(item_id,name, discount,ratingCount,rating,price,category_of_item,image,price_with_discount,in_stock);
            mcategoryWiseViewModel.insert(category);

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public void getUpdatedDataFromCategory(String category_of_item)
    {
        databaseReference.child(category_of_item).addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                Map<String, String> value = (Map<String, String>) snapshot.getValue();
                getDataFromFireBase(value,category_of_item);

            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

                Map<String, String> value = (Map<String, String>) snapshot.getValue();
                getDataFromFireBase(value,category_of_item);

            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot snapshot) {

                Map<String, String> value = (Map<String, String>) snapshot.getValue();
                deleteDataFromRoom(value,category_of_item);


            }
            @Override
            public void onChildMoved(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {


            }
        });
    }
}