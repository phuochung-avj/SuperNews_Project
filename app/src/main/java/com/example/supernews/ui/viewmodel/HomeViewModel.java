package com.example.supernews.ui.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import com.example.supernews.data.model.News;
import com.example.supernews.data.repository.NewsRepository;
import java.util.List;

public class HomeViewModel extends ViewModel {

    private NewsRepository repository;

    private MutableLiveData<List<News>> newsList = new MutableLiveData<>();
    private MutableLiveData<Boolean> isLoading = new MutableLiveData<>();
    private MutableLiveData<String> errorMessage = new MutableLiveData<>();

    public HomeViewModel() {
        repository = new NewsRepository();
    }

    public LiveData<List<News>> getNewsList() { return newsList; }
    public LiveData<Boolean> getIsLoading() { return isLoading; }
    public LiveData<String> getErrorMessage() { return errorMessage; }

    // --- CẬP NHẬT: Nhận thêm scope ---
    public void loadNews(String category, String scope) {
        isLoading.setValue(true);

        // Gọi Repository với 2 tham số lọc
        repository.getNewsData(category, scope, new NewsRepository.OnFirestoreTaskComplete() {
            @Override
            public void onSuccess(List<News> list) {
                newsList.setValue(list);
                isLoading.setValue(false);
            }

            @Override
            public void onError(Exception e) {
                errorMessage.setValue(e.getMessage());
                isLoading.setValue(false);
            }
        });
    }
}