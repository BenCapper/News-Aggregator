package org.ben.news.firebase

import android.app.Application
import androidx.lifecycle.MutableLiveData
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import org.ben.news.models.StoryModel
import org.ben.news.models.UserModel
import timber.log.Timber
import java.util.HashMap

class FirebaseAuthManager(application: Application) {

    private var application: Application? = null

    var firebaseAuth: FirebaseAuth? = null
    var liveFirebaseUser = MutableLiveData<FirebaseUser>()
    var loggedOut = MutableLiveData<Boolean>()
    var errorStatus = MutableLiveData<Boolean>()

    init {
        this.application = application
        firebaseAuth = FirebaseAuth.getInstance()

        if (firebaseAuth!!.currentUser != null) {
            liveFirebaseUser.postValue(firebaseAuth!!.currentUser)
            loggedOut.postValue(false)
            errorStatus.postValue(false)
        }
    }

    /**
     * > The function takes in two parameters, email and password, and then uses Firebase's
     * signInWithEmailAndPassword function to sign in the user. If the sign in is successful, the
     * current user is posted to the liveFirebaseUser MutableLiveData object, and the errorStatus
     * MutableLiveData object is set to false. If the sign in is unsuccessful, the errorStatus
     * MutableLiveData object is set to true
     *
     * @param email The email address of the user.
     * @param password String? - The password of the user
     */
    fun login(email: String?, password: String?) {
        firebaseAuth!!.signInWithEmailAndPassword(email!!, password!!)
            .addOnCompleteListener(application!!.mainExecutor) { task ->
                if (task.isSuccessful) {
                    liveFirebaseUser.postValue(firebaseAuth!!.currentUser)
                    errorStatus.postValue(false)
                } else {
                    Timber.i("Login Failure: $task.exception!!.message")
                    errorStatus.postValue(true)
                }
            }
    }

    /**
     * We're creating a new user with the email and password provided by the user, and if the task is
     * successful, we're posting the current user to the liveFirebaseUser MutableLiveData object, and
     * if the task is unsuccessful, we're posting an error to the errorStatus MutableLiveData object
     *
     * @param email String? - The email address of the user
     * @param password String? - The password for the new account.
     */
    fun register(email: String?, password: String?) {
        firebaseAuth!!.createUserWithEmailAndPassword(email!!, password!!)
            .addOnCompleteListener(application!!.mainExecutor) { task ->
                if (task.isSuccessful) {
                    liveFirebaseUser.postValue(firebaseAuth!!.currentUser)
                    firebaseAuth!!.currentUser?.let { create(it) }
                    errorStatus.postValue(false)
                } else {
                    Timber.i("Registration Failure: $task.exception!!.message")
                    errorStatus.postValue(true)
                }
            }
    }

   private fun create(firebaseUser: FirebaseUser) {
        Timber.i("Firebase DB Reference : ${StoryManager.database}")
        val user: UserModel = UserModel(firebaseUser.uid)
        val userValues = user.toMap()

        val childAdd = HashMap<String, Any>()
        childAdd["/users/${user.id}"] = userValues


        StoryManager.database.updateChildren(childAdd)
    }
    /**
     * The function logs out the user and sets the loggedOut and errorStatus LiveData objects to true
     * and false respectively
     */
    fun logOut() {
        firebaseAuth!!.signOut()
        loggedOut.postValue(true)
        errorStatus.postValue(false)
    }
}