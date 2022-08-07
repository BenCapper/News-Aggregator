package org.ben.news.ui.home

import android.annotation.SuppressLint
import android.content.Intent
import android.content.SharedPreferences
import android.content.res.Configuration
import android.net.Uri
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Switch
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.appcompat.app.AppCompatDelegate.MODE_NIGHT_NO
import androidx.appcompat.app.AppCompatDelegate.MODE_NIGHT_YES
import androidx.appcompat.widget.Toolbar
import androidx.drawerlayout.widget.DrawerLayout
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.findNavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.*
import androidx.preference.PreferenceManager
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.internal.NavigationMenuItemView
import com.google.firebase.auth.FirebaseUser
import org.ben.news.R
import org.ben.news.databinding.HomeBinding
import org.ben.news.databinding.NavHeaderBinding
import org.ben.news.firebase.FirebaseImageManager
import org.ben.news.helpers.readImageUri
import org.ben.news.helpers.showImagePicker
import org.ben.news.ui.auth.LoggedInViewModel
import org.ben.news.ui.auth.Login
import org.ben.news.ui.storyList.StoryListViewModel
import timber.log.Timber


class Home : AppCompatActivity() {

    private lateinit var drawerLayout: DrawerLayout
    private lateinit var homeBinding : HomeBinding
    private lateinit var navHeaderBinding : NavHeaderBinding
    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var loggedInViewModel : LoggedInViewModel
    private lateinit var headerView : View
    private lateinit var intentLauncher : ActivityResultLauncher<Intent>
    private lateinit var sharedPreferences: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        homeBinding = HomeBinding.inflate(layoutInflater)
        setContentView(homeBinding.root)
        drawerLayout = homeBinding.drawerLayout
        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)

        val navHostFragment = supportFragmentManager.
        findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        val navController = navHostFragment.navController

        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.

        appBarConfiguration = AppBarConfiguration(setOf(
            R.id.storyListFragment,
            R.id.likedListFragment,
            R.id.historyListFragment,
            R.id.abcFragment,
            R.id.blazeFragment,
            R.id.bonginoFragment,
            R.id.breitbartFragment,
            R.id.cbsFragment,
            R.id.callerFragment,
            R.id.euronFragment,
            R.id.gbFragment,
            R.id.globalFragment,
            R.id.griptFragment,
            R.id.gatewayFragment,
            R.id.pmillFragment,
            R.id.politicoFragment,
            R.id.revolverFragment,
            R.id.rteFragment,
            R.id.skyFragment,
            R.id.timcastFragment,
            R.id.voxFragment,
            R.id.yahooFragment,
            R.id.zerohedgeFragment), drawerLayout)
        setupActionBarWithNavController(navController, appBarConfiguration)

        val navView = homeBinding.navView
        navView.itemIconTintList = null
        navView.setupWithNavController(navController)

        initNavHeader()
    }

    @SuppressLint("MissingPermission")
    /**
     * If the user grants the permission, update the current location, otherwise use a default location
     *
     * @param requestCode The request code passed in requestPermissions(android.app.Activity, String[],
     * int)
     * @param permissions The permissions that were requested.
     * @param grantResults An array of the results of each permission
     */
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }

    public override fun onStart() {
        super.onStart()
        /* This is the code that is used to observe the logged in user and update the nav header with
        the user's details. */
        loggedInViewModel = ViewModelProvider(this)[LoggedInViewModel::class.java]
        loggedInViewModel.liveFirebaseUser.observe(this) { firebaseUser ->
            if (firebaseUser != null) {
                updateNavHeader(firebaseUser)
                navHeaderBinding.navHeaderEmail.text = loggedInViewModel.liveFirebaseUser.value?.email!!.split("@")[0]
            }
        }
        loggedInViewModel.loggedOut.observe(this) { loggedOut ->
            if (loggedOut) {
                startActivity(Intent(this, Login::class.java))
            }
        }
        registerImagePickerCallback()
    }

    /**
     * We get the header view from the navigation view, bind it to a NavHeaderBinding object, and set
     * an onClickListener on the imageView
     */
    private fun initNavHeader() {
        Timber.i("Init Nav Header")
        headerView = homeBinding.navView.getHeaderView(0)
        navHeaderBinding = NavHeaderBinding.bind(headerView)

        navHeaderBinding.imageView.setOnClickListener {
            showImagePicker(intentLauncher)
        }
    }

    private fun updateNavHeader(currentUser: FirebaseUser) {
        FirebaseImageManager.checkStorageForExistingProfilePic(currentUser.uid)
        FirebaseImageManager.imageUri.observe(this) { result ->
            if (result == Uri.EMPTY) {
                Timber.i("NO Existing imageUri")
                if (currentUser.photoUrl != null) {
                    //if you're a google user
                    FirebaseImageManager.updateUserImage(
                        currentUser.uid,
                        currentUser.photoUrl,
                        navHeaderBinding.imageView,
                        true
                    )
                } else {
                    Timber.i("Loading Existing Default imageUri")
                    FirebaseImageManager.updateDefaultImage(
                        currentUser.uid,
                        R.drawable.ic_person,
                        navHeaderBinding.imageView
                    )
                }
            } else // load existing image from firebase
            {
                Timber.i("Loading Existing imageUri")
                FirebaseImageManager.updateUserImage(
                    currentUser.uid,
                    FirebaseImageManager.imageUri.value,
                    navHeaderBinding.imageView, false
                )
            }
        }
        navHeaderBinding.navHeaderEmail.text = currentUser.email
        if(currentUser.displayName != null)
            navHeaderBinding.navHeaderEmail.text = currentUser.displayName
    }

    override fun onSupportNavigateUp(): Boolean {
        val navController = findNavController(R.id.nav_host_fragment)
        return navController.navigateUp(appBarConfiguration) || super.onSupportNavigateUp()
    }

    /**
     * It logs the user out and redirects them to the login page.
     *
     * @param item MenuItem - The menu item that was clicked.
     */
    fun signOut(item: MenuItem) {
        loggedInViewModel.logOut()
        val intent = Intent(this, Login::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK)
        startActivity(intent)
    }

    fun savedArticles(item: MenuItem) {
        findNavController(R.id.nav_host_fragment).navigate(R.id.likedListFragment)
        findViewById<DrawerLayout>(R.id.drawer_layout).closeDrawers()
        Timber.i("UserId = ${loggedInViewModel.liveFirebaseUser.value!!.uid}")
    }

    fun historyArticles(item: MenuItem) {
        findNavController(R.id.nav_host_fragment).navigate(R.id.historyListFragment)
        findViewById<DrawerLayout>(R.id.drawer_layout).closeDrawers()
        Timber.i("UserId = ${loggedInViewModel.liveFirebaseUser.value!!.uid}")
    }

    fun homeArticles(item: MenuItem) {
        findNavController(R.id.nav_host_fragment).navigate(R.id.storyListFragment)
        findViewById<DrawerLayout>(R.id.drawer_layout).closeDrawers()
        Timber.i("UserId = ${loggedInViewModel.liveFirebaseUser.value!!.uid}")
    }

    fun yahArticles(item: MenuItem) {
        findNavController(R.id.nav_host_fragment).navigate(R.id.yahooFragment)
        findViewById<DrawerLayout>(R.id.drawer_layout).closeDrawers()
        Timber.i("UserId = ${loggedInViewModel.liveFirebaseUser.value!!.uid}")
    }

    fun canArticles(item: MenuItem) {
        findNavController(R.id.nav_host_fragment).navigate(R.id.canFragment)
        findViewById<DrawerLayout>(R.id.drawer_layout).closeDrawers()
        Timber.i("UserId = ${loggedInViewModel.liveFirebaseUser.value!!.uid}")
    }

    fun euArticles(item: MenuItem) {
        findNavController(R.id.nav_host_fragment).navigate(R.id.euFragment)
        findViewById<DrawerLayout>(R.id.drawer_layout).closeDrawers()
        Timber.i("UserId = ${loggedInViewModel.liveFirebaseUser.value!!.uid}")
    }

    fun ieArticles(item: MenuItem) {
        findNavController(R.id.nav_host_fragment).navigate(R.id.ieFragment)
        findViewById<DrawerLayout>(R.id.drawer_layout).closeDrawers()
        Timber.i("UserId = ${loggedInViewModel.liveFirebaseUser.value!!.uid}")
    }

    fun ukArticles(item: MenuItem) {
        findNavController(R.id.nav_host_fragment).navigate(R.id.ukFragment)
        findViewById<DrawerLayout>(R.id.drawer_layout).closeDrawers()
        Timber.i("UserId = ${loggedInViewModel.liveFirebaseUser.value!!.uid}")
    }

    fun usArticles(item: MenuItem) {
        findNavController(R.id.nav_host_fragment).navigate(R.id.usFragment)
        findViewById<DrawerLayout>(R.id.drawer_layout).closeDrawers()
        Timber.i("UserId = ${loggedInViewModel.liveFirebaseUser.value!!.uid}")
    }

    fun voxArticles(item: MenuItem) {
        findNavController(R.id.nav_host_fragment).navigate(R.id.voxFragment)
        findViewById<DrawerLayout>(R.id.drawer_layout).closeDrawers()
        Timber.i("UserId = ${loggedInViewModel.liveFirebaseUser.value!!.uid}")
    }

    fun euronArticles(item: MenuItem) {
        findNavController(R.id.nav_host_fragment).navigate(R.id.euronFragment)
        findViewById<DrawerLayout>(R.id.drawer_layout).closeDrawers()
        Timber.i("UserId = ${loggedInViewModel.liveFirebaseUser.value!!.uid}")
    }

    fun gloArticles(item: MenuItem) {
        findNavController(R.id.nav_host_fragment).navigate(R.id.globalFragment)
        findViewById<DrawerLayout>(R.id.drawer_layout).closeDrawers()
        Timber.i("UserId = ${loggedInViewModel.liveFirebaseUser.value!!.uid}")
    }

    fun breitbartArticles(item: MenuItem) {
        findNavController(R.id.nav_host_fragment).navigate(R.id.breitbartFragment)
        findViewById<DrawerLayout>(R.id.drawer_layout).closeDrawers()
        Timber.i("UserId = ${loggedInViewModel.liveFirebaseUser.value!!.uid}")
    }

    fun abcArticles(item: MenuItem) {
        findNavController(R.id.nav_host_fragment).navigate(R.id.abcFragment)
        findViewById<DrawerLayout>(R.id.drawer_layout).closeDrawers()
        Timber.i("UserId = ${loggedInViewModel.liveFirebaseUser.value!!.uid}")
    }

    fun cbsArticles(item: MenuItem) {
        findNavController(R.id.nav_host_fragment).navigate(R.id.cbsFragment)
        findViewById<DrawerLayout>(R.id.drawer_layout).closeDrawers()
        Timber.i("UserId = ${loggedInViewModel.liveFirebaseUser.value!!.uid}")
    }

    fun skyArticles(item: MenuItem) {
        findNavController(R.id.nav_host_fragment).navigate(R.id.skyFragment)
        findViewById<DrawerLayout>(R.id.drawer_layout).closeDrawers()
        Timber.i("UserId = ${loggedInViewModel.liveFirebaseUser.value!!.uid}")
    }

    fun rteArticles(item: MenuItem) {
        findNavController(R.id.nav_host_fragment).navigate(R.id.rteFragment)
        findViewById<DrawerLayout>(R.id.drawer_layout).closeDrawers()
        Timber.i("UserId = ${loggedInViewModel.liveFirebaseUser.value!!.uid}")
    }

    fun blazeArticles(item: MenuItem) {
        findNavController(R.id.nav_host_fragment).navigate(R.id.blazeFragment)
        findViewById<DrawerLayout>(R.id.drawer_layout).closeDrawers()
        Timber.i("UserId = ${loggedInViewModel.liveFirebaseUser.value!!.uid}")
    }

    fun callerArticles(item: MenuItem) {
        findNavController(R.id.nav_host_fragment).navigate(R.id.callerFragment)
        findViewById<DrawerLayout>(R.id.drawer_layout).closeDrawers()
        Timber.i("UserId = ${loggedInViewModel.liveFirebaseUser.value!!.uid}")
    }

    fun bongArticles(item: MenuItem) {
        findNavController(R.id.nav_host_fragment).navigate(R.id.bonginoFragment)
        findViewById<DrawerLayout>(R.id.drawer_layout).closeDrawers()
        Timber.i("UserId = ${loggedInViewModel.liveFirebaseUser.value!!.uid}")
    }

    fun gatewayArticles(item: MenuItem) {
        findNavController(R.id.nav_host_fragment).navigate(R.id.gatewayFragment)
        findViewById<DrawerLayout>(R.id.drawer_layout).closeDrawers()
        Timber.i("UserId = ${loggedInViewModel.liveFirebaseUser.value!!.uid}")
    }

    fun gbArticles(item: MenuItem) {
        findNavController(R.id.nav_host_fragment).navigate(R.id.gbFragment)
        findViewById<DrawerLayout>(R.id.drawer_layout).closeDrawers()
        Timber.i("UserId = ${loggedInViewModel.liveFirebaseUser.value!!.uid}")
    }

    fun griptArticles(item: MenuItem) {
        findNavController(R.id.nav_host_fragment).navigate(R.id.griptFragment)
        findViewById<DrawerLayout>(R.id.drawer_layout).closeDrawers()
        Timber.i("UserId = ${loggedInViewModel.liveFirebaseUser.value!!.uid}")
    }

    fun pMillArticles(item: MenuItem) {
        findNavController(R.id.nav_host_fragment).navigate(R.id.pmillFragment)
        findViewById<DrawerLayout>(R.id.drawer_layout).closeDrawers()
        Timber.i("UserId = ${loggedInViewModel.liveFirebaseUser.value!!.uid}")
    }

    fun politicoArticles(item: MenuItem) {
        findNavController(R.id.nav_host_fragment).navigate(R.id.politicoFragment)
        findViewById<DrawerLayout>(R.id.drawer_layout).closeDrawers()
        Timber.i("UserId = ${loggedInViewModel.liveFirebaseUser.value!!.uid}")
    }

    fun revArticles(item: MenuItem) {
        findNavController(R.id.nav_host_fragment).navigate(R.id.revolverFragment)
        findViewById<DrawerLayout>(R.id.drawer_layout).closeDrawers()
        Timber.i("UserId = ${loggedInViewModel.liveFirebaseUser.value!!.uid}")
    }

    fun timArticles(item: MenuItem) {
        findNavController(R.id.nav_host_fragment).navigate(R.id.timcastFragment)
        findViewById<DrawerLayout>(R.id.drawer_layout).closeDrawers()
        Timber.i("UserId = ${loggedInViewModel.liveFirebaseUser.value!!.uid}")
    }

    fun zeroArticles(item: MenuItem) {
        findNavController(R.id.nav_host_fragment).navigate(R.id.zerohedgeFragment)
        findViewById<DrawerLayout>(R.id.drawer_layout).closeDrawers()
        Timber.i("UserId = ${loggedInViewModel.liveFirebaseUser.value!!.uid}")
    }

    fun theme(item: MenuItem) {
        val currentNightMode = resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this)
        when (currentNightMode) {
            Configuration.UI_MODE_NIGHT_NO -> {
                sharedPreferences.edit().clear().commit()
                sharedPreferences.edit().putString("night_mode", "night_mode").commit()
                AppCompatDelegate.setDefaultNightMode(MODE_NIGHT_YES)
            }
            Configuration.UI_MODE_NIGHT_YES -> {
                sharedPreferences.edit().clear().commit()
                sharedPreferences.edit().putString("light_mode", "light_mode").commit()
                AppCompatDelegate.setDefaultNightMode(MODE_NIGHT_NO)

            }
        }
    }

    private fun registerImagePickerCallback() {
        intentLauncher =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
                when(result.resultCode){
                    RESULT_OK -> {
                        if (result.data != null) {
                            Timber.i("registerPickerCallback() ${readImageUri(result.resultCode, result.data).toString()}")
                            FirebaseImageManager
                                .updateUserImage(loggedInViewModel.liveFirebaseUser.value!!.uid,
                                    readImageUri(result.resultCode, result.data),
                                    navHeaderBinding.imageView,
                                    true)
                        } // end of if
                    }
                    RESULT_CANCELED -> { } else -> { }
                }
            }
    }
}