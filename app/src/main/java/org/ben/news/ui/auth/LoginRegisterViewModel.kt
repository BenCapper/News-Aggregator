package org.ben.news.ui.auth

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.FirebaseUser
import org.ben.news.firebase.FirebaseAuthManager


class LoginRegisterViewModel (app: Application) : AndroidViewModel(app) {

    var firebaseAuthManager : FirebaseAuthManager = FirebaseAuthManager(app)
    var liveFirebaseUser : MutableLiveData<FirebaseUser?> = firebaseAuthManager.liveFirebaseUser

    /**
     * It logs in a user with the given email and password.
     *
     * @param email The email of the user
     * @param password The password of the user.
     */
    fun login(email: String?, password: String?) {
        firebaseAuthManager.login(email, password)
    }

    /**
     * It logs in a user with the given Google ID token.
     *
     * @param idToken The Google ID token of the user
     */
    fun loginWithGoogle(idToken: String) {
        firebaseAuthManager.loginWithGoogle(idToken)
    }

    /**
     * It registers a user with the given email and password.
     *
     * @param email The email address of the user.
     * @param password String?
     */
    fun register(email: String?, password: String?) {
        firebaseAuthManager.register(email, password)
    }

}