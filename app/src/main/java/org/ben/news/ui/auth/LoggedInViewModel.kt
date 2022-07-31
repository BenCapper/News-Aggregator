package org.ben.news.ui.auth

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import com.google.firebase.auth.FirebaseUser
import org.ben.news.firebase.FirebaseAuthManager


class LoggedInViewModel(app: Application) : AndroidViewModel(app) {

    var firebaseAuthManager : FirebaseAuthManager = FirebaseAuthManager(app)
    var liveFirebaseUser : MutableLiveData<FirebaseUser> = firebaseAuthManager.liveFirebaseUser
    var loggedOut : MutableLiveData<Boolean> = firebaseAuthManager.loggedOut

    /**
     * It logs out the user.
     */
    fun logOut() {
        firebaseAuthManager.logOut()
    }
}