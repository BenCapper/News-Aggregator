package org.ben.news.ui.storyList

import android.app.AlertDialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Parcelable
import android.os.UserManager
import android.view.*
import android.widget.ImageView
import android.widget.SearchView
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.MobileAds
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.storage.FirebaseStorage
import org.ben.news.R
import org.ben.news.adapters.StoryAdapter
import org.ben.news.adapters.StoryListener
import org.ben.news.databinding.FragmentStoryListBinding
import org.ben.news.firebase.StoryManager
import org.ben.news.helpers.createLoader
import org.ben.news.helpers.hideLoader
import org.ben.news.helpers.showLoader
import org.ben.news.models.StoryModel
import org.ben.news.ui.auth.LoggedInViewModel
import splitties.snackbar.snack


class StoryListFragment : Fragment(), StoryListener {

    companion object {
        fun newInstance() = StoryListFragment()
    }
    private var _fragBinding: FragmentStoryListBinding? = null
    private val fragBinding get() = _fragBinding!!
    lateinit var loader : AlertDialog
    private val loggedInViewModel : LoggedInViewModel by activityViewModels()
    private val storyListViewModel: StoryListViewModel by activityViewModels()
    var state: Parcelable? = null



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)

    }


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        loader = createLoader(requireActivity())
        showLoader(loader,"")
        _fragBinding = FragmentStoryListBinding.inflate(inflater, container, false)
        val root = fragBinding.root
        fragBinding.recyclerView.layoutManager = activity?.let { LinearLayoutManager(it) }
        activity?.findViewById<BottomNavigationView>(R.id.bottom_nav)?.itemIconTintList = null
        activity?.findViewById<BottomNavigationView>(R.id.bottom_nav)?.visibility = View.VISIBLE
        activity?.findViewById<ImageView>(R.id.toolimg)?.setImageResource(R.drawable.logo)

        MobileAds.initialize(this.context!!) {}


        storyListViewModel.observableStoryList.observe(viewLifecycleOwner) { story ->
            story?.let {
                render(story as ArrayList<StoryModel>)
            }
            hideLoader(loader)
        }

        return root
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.menu_home, menu)

        /* Finding the search bar in the menu and setting it to the search view. */
        val item = menu.findItem(R.id.app_bar_search)
        val searchView = item.actionView as SearchView


        /* This is the code that is executed when the search bar is used. It searches the database for
        the building that the user is searching for. */
        searchView.setOnQueryTextListener(object :  SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                return false
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                if (newText != null) {
                    storyListViewModel.search(
                        newText
                    )
                }
                else {
                    storyListViewModel.load()
                }
                return true
            }
        })
        super.onCreateOptionsMenu(menu, inflater)
    }


    private fun render(storyList: ArrayList<StoryModel>) {
        fragBinding.recyclerView.adapter = StoryAdapter(storyList, this)
        state?.let { fragBinding.recyclerView.layoutManager?.onRestoreInstanceState(it) }
    }


    override fun onResume() {
        super.onResume()
        //showLoader(loader,"")
        loggedInViewModel.liveFirebaseUser.observe(viewLifecycleOwner) { firebaseUser ->
            if (firebaseUser != null) {
                storyListViewModel.liveFirebaseUser.value = firebaseUser
                storyListViewModel.load()
            }
        }
    }

    override fun onPause() {
        state = fragBinding.recyclerView.layoutManager?.onSaveInstanceState()
        super.onPause()
    }

    override fun onStoryClick(story: StoryModel) {
        StoryManager.create(loggedInViewModel.liveFirebaseUser.value!!.uid,"history", story)
        val intent = Intent(Intent.ACTION_VIEW).setData(Uri.parse(story.link))
        state = fragBinding.recyclerView.layoutManager?.onSaveInstanceState()
        startActivity(intent)
    }

    override fun onLike(story: StoryModel) {
        StoryManager.create(loggedInViewModel.liveFirebaseUser.value!!.uid,"likes", story)
        view?.snack(R.string.saved_article)
    }

    override fun onShare(story: StoryModel) {
        val sendIntent: Intent = Intent().apply {
            action = Intent.ACTION_SEND
            putExtra(Intent.EXTRA_TEXT, story.link)
            putExtra(Intent.EXTRA_TITLE, story.title)
            type = "text/html"
        }

        val shareIntent = Intent.createChooser(sendIntent, null)
        state = fragBinding.recyclerView.layoutManager?.onSaveInstanceState()
        startActivity(shareIntent)
    }
    override fun onDestroyView() {
        super.onDestroyView()
        _fragBinding = null
    }
}