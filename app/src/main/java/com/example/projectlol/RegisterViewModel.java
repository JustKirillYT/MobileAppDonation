package com.example.projectlol;

import android.text.TextUtils;
import android.util.Patterns;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;

import java.util.Objects;

public class RegisterViewModel extends ViewModel {
    private final MutableLiveData<String> errorMessage = new MutableLiveData<>();
    private final MutableLiveData<Boolean> _navigateToMain = new MutableLiveData<>();
    private final FirebaseAuth auth = FirebaseAuth.getInstance();
    private MutableLiveData<Boolean> _isLoading = new MutableLiveData<>();
    LiveData<Boolean> isLoading = _isLoading;

    public LiveData<Boolean> navigateToMain() {
        return _navigateToMain;
    }

    public void registerUser(String email, String password) {


        if (TextUtils.isEmpty(email)) {
            errorMessage.postValue("Введите email");
            return;
        }

        if (TextUtils.isEmpty(password)) {
            errorMessage.postValue("Введите пароль");
            return;
        }
        _isLoading.setValue(true);
        // Проверка формата email
        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            errorMessage.postValue("Неверный формат email");
            return;
        }

        // Проверка длины пароля
        if (password.length() < 6) {
            errorMessage.postValue("Пароль должен содержать минимум 6 символов");
            return;
        }


        auth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    _isLoading.setValue(false);
                    if (!task.isSuccessful()) {
                        // Обработка ошибок Firebase
                        Exception exception = task.getException();
                        String errorMsg = "Ошибка регистрации";

                        if (exception instanceof FirebaseAuthUserCollisionException) {
                            errorMsg = "Пользователь с таким email уже существует";
                        } else if (exception instanceof FirebaseAuthException) {
                            String errorCode = ((FirebaseAuthException) exception).getErrorCode();
                            switch (errorCode) {
                                case "ERROR_EMAIL_ALREADY_IN_USE":
                                case "ERROR_ACCOUNT_EXISTS_WITH_DIFFERENT_CREDENTIAL":
                                    errorMsg = "Пользователь с таким email уже существует";
                                    break;
                                case "ERROR_WEAK_PASSWORD":
                                    errorMsg = "Пароль слишком простой";
                                    break;
                                case "ERROR_INVALID_EMAIL":
                                    errorMsg = "Неверный формат email";
                                    break;
                            }
                        }
                        errorMessage.postValue(errorMsg);
                    } else {
                        _navigateToMain.setValue(true);
                        errorMessage.postValue("Успешная регистрация!");
                    }
                });



    }
    public LiveData<String> getErrorMessage() {
        return errorMessage;
    }
    public void resetNavigation() {
        _navigateToMain.setValue(false);
    }

}
