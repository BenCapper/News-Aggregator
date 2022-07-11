package org.ben.news.ui.storyList

import android.app.AlertDialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Parcelable
import android.os.UserManager
import android.view.*
import android.widget.SearchView
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
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
    private var storage = FirebaseStorage.getInstance().reference
    var state: Parcelable? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)

    }


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        _fragBinding = FragmentStoryListBinding.inflate(inflater, container, false)
        val root = fragBinding.root
        loader = createLoader(requireActivity())
        activity?.title = getString(R.string.nav_host)
        fragBinding.recyclerView.layoutManager = activity?.let { LinearLayoutManager(it) }

        showLoader(loader, "Downloading Stories")


        storyListViewModel.observableStoryList.observe(viewLifecycleOwner) { story ->
            story?.let {
                render(story as ArrayList<StoryModel>)
                hideLoader(loader)
            }
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
        showLoader(loader, "Downloading stories")
        loggedInViewModel.liveFirebaseUser.observe(viewLifecycleOwner) { firebaseUser ->
            if (firebaseUser != null) {
                storyListViewModel.liveFirebaseUser.value = firebaseUser
                storyListViewModel.load()
            }
        }

    }

    override fun onStoryClick(story: StoryModel) {
        val intent = Intent(Intent.ACTION_VIEW).setData(Uri.parse(story.link))
        state = fragBinding.recyclerView.layoutManager?.onSaveInstanceState()
        startActivity(intent)
    }

    override fun onLike(story: StoryModel) {
        StoryManager.create(loggedInViewModel.liveFirebaseUser.value!!.uid, story)
        view?.snack(R.string.saved_article)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _fragBinding = null
    }
}