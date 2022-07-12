package org.ben.news.ui.likedList

import android.app.AlertDialog
import android.content.Intent
import android.net.Uri
import androidx.lifecycle.ViewModelProvider
import android.os.Bundle
import android.os.Parcelable
import android.view.*
import android.widget.ImageView
import android.widget.SearchView
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.storage.FirebaseStorage
import org.ben.news.R
import org.ben.news.adapters.NoSaveAdapter
import org.ben.news.adapters.StoryAdapter
import org.ben.news.adapters.StoryListener
import org.ben.news.adapters.StoryNoSaveListener
import org.ben.news.databinding.FragmentLikedListBinding
import org.ben.news.databinding.FragmentStoryListBinding
import org.ben.news.firebase.StoryManager
import org.ben.news.helpers.SwipeToDeleteCallback
import org.ben.news.helpers.createLoader
import org.ben.news.helpers.hideLoader
import org.ben.news.helpers.showLoader
import org.ben.news.models.StoryModel
import org.ben.news.ui.auth.LoggedInViewModel
import org.ben.news.ui.storyList.StoryListFragment
import org.ben.news.ui.storyList.StoryListViewModel
import splitties.snackbar.snack

class LikedListFragment : Fragment(), StoryNoSaveListener {

    companion object {
        fun newInstance() = StoryListFragment()
    }
    private var _fragBinding: FragmentLikedListBinding? = null
    private val fragBinding get() = _fragBinding!!
    lateinit var loader : AlertDialog
    private val loggedInViewModel : LoggedInViewModel by activityViewModels()
    private val likedListViewModel: LikedListViewModel by activityViewModels()
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

        _fragBinding = FragmentLikedListBinding.inflate(inflater, container, false)
        val root = fragBinding.root
        loader = createLoader(requireActivity())
        activity?.title = getString(R.string.nav_host)
        fragBinding.recyclerViewLiked.layoutManager = activity?.let { LinearLayoutManager(it) }

        showLoader(loader, "Downloading Stories")


        likedListViewModel.observableLikedList.observe(viewLifecycleOwner) { story ->
            story?.let {
                render(story as ArrayList<StoryModel>)
                hideLoader(loader)
            }
        }

        val swipeDeleteHandler = object : SwipeToDeleteCallback(requireContext()) {
            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                showLoader(loader, "Deleting History Article")
                val adapter = fragBinding.recyclerViewLiked.adapter as NoSaveAdapter
                adapter.removeAt(viewHolder.absoluteAdapterPosition)
                likedListViewModel.delete(
                    likedListViewModel.liveFirebaseUser.value?.uid!!,
                    (viewHolder.itemView.tag as StoryModel).title
                )
                hideLoader(loader)
            }
        }
        val itemTouchDeleteHelper = ItemTouchHelper(swipeDeleteHandler)
        itemTouchDeleteHelper.attachToRecyclerView(fragBinding.recyclerViewLiked)

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
                    likedListViewModel.search(
                        newText
                    )
                }
                return true
            }
        })
        super.onCreateOptionsMenu(menu, inflater)
    }


    private fun render(storyList: ArrayList<StoryModel>) {
        fragBinding.recyclerViewLiked.adapter = NoSaveAdapter(storyList, this)
        state?.let { fragBinding.recyclerViewLiked.layoutManager?.onRestoreInstanceState(it) }
    }


    override fun onResume() {
        super.onResume()
        showLoader(loader, "Downloading stories")
        loggedInViewModel.liveFirebaseUser.observe(viewLifecycleOwner) { firebaseUser ->
            if (firebaseUser != null) {
                likedListViewModel.liveFirebaseUser.value = firebaseUser
                likedListViewModel.load()
            }
        }

    }

    override fun onStoryClick(story: StoryModel) {
        StoryManager.create(loggedInViewModel.liveFirebaseUser.value!!.uid, "history",story)
        val intent = Intent(Intent.ACTION_VIEW).setData(Uri.parse(story.link))
        state = fragBinding.recyclerViewLiked.layoutManager?.onSaveInstanceState()
        startActivity(intent)
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _fragBinding = null
    }
}