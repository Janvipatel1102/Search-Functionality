package com.example.mysearchactivity.viewModels;

import android.app.Application;
import android.content.ClipboardManager;
import android.database.Cursor;

import com.example.mysearchactivity.model.CategoryWiseItems;
import com.example.mysearchactivity.reposataries.categoryRepo;
import com.example.mysearchactivity.reposataries.categoryWiseRepo;

import java.util.List;

import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

public class categoryWiseViewModel extends AndroidViewModel {

    private categoryWiseRepo mRepository;

    private LiveData<List<CategoryWiseItems>> mAllCategoryWiseItems,mItemByCategories;

    public categoryWiseViewModel (Application application) {

        super(application);
        mRepository = new categoryWiseRepo(application);
        mAllCategoryWiseItems = mRepository.getmAllCategoryWiseItems();
    }

    public LiveData<List<CategoryWiseItems>> getAllCategoryWiseItems() { return mAllCategoryWiseItems; }

    public void insert(CategoryWiseItems category) { mRepository.insert(category); }

    public void deletAllCategoryWiseItems() { mRepository.deleteAll(); }

  public LiveData<List<CategoryWiseItems>> getItemByName(String query,int limit,int offset)
   {
       return  mRepository.getItemByName(query,limit,offset);
   }
    public LiveData<List<CategoryWiseItems>> getItemByCategory(String query,int limit,int offset)
    {
        return  mRepository.getItemByCategories(query,limit,offset );
    }

    public LiveData<List<String>> getNameByName(String query)
    {
        return  mRepository.getNameByName(query);
    }



}