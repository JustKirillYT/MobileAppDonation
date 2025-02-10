package com.example.projectlol;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class userViewModel extends ViewModel {
    private final userRepository repository = new userRepository();
    private final MutableLiveData<userModel> userLiveData = new MutableLiveData<>();
    private final MutableLiveData<String> errorLiveData = new MutableLiveData<>();

    public void loadUserData() {
        repository.getUserData(new userRepository.UserDataCallback() {
            @Override
            public void onSuccess(userModel user) {
                userLiveData.postValue(user);
            }

            @Override
            public void onFailure(String error) {
                errorLiveData.postValue(error);
                userLiveData.postValue(null);
            }
        });
    }

    public LiveData<userModel> getUserLiveData() {
        return userLiveData;
    }

    public LiveData<String> getErrorLiveData() {
        return errorLiveData;
    }
}