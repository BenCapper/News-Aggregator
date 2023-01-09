package org.ben.news.ui.likedList

import android.app.AlertDialog
import android.content.Intent
import android.content.res.Configuration
import android.net.Uri
import android.os.Bundle
import android.os.Parcelable
import android.view.*
import android.widget.ImageView
import android.widget.SearchView
import androidx.core.view.MenuHost
import androidx.core.view.MenuProvider
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.android.gms.ads.MobileAds
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.storage.FirebaseStorage
import org.ben.news.R
import org.ben.news.adapters.*
import org.ben.news.databinding.FragmentLikedListBinding
import org.ben.news.firebase.StoryManager
import org.ben.news.helpers.*
import org.ben.news.models.StoryModel
import org.ben.news.ui.auth.LoggedInViewModel
import org.ben.news.ui.storyList.StoryListFragment
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList


class LikedListFragment : Fragment(), StorySaveListener, StoryListener, MenuProvider {

    companion object {
        fun newInstance() = StoryListFragment()
    }

    private var _fragBinding: FragmentLikedListBinding? = null
    private val fragBinding get() = _fragBinding!!
    lateinit var loader: AlertDialog
    private val loggedInViewModel: LoggedInViewModel by activityViewModels()
    private val likedListViewModel: LikedListViewModel by activityViewModels()
    private var storage = FirebaseStorage.getInstance().reference
    var state: Parcelable? = null
    var day = 0
    var searching: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        loader = createLoader(requireActivity())
        showLoader(loader, "")
        _fragBinding = FragmentLikedListBinding.inflate(inflater, container, false)
        val root = fragBinding.root

        fragBinding.recyclerViewLiked.layoutManager = activity?.let { LinearLayoutManager(it) }
        activity?.findViewById<ImageView>(R.id.toolimg)?.setImageResource(R.drawable.saved)
        MobileAds.initialize(this.requireContext()) {}
        val fab = activity?.findViewById<FloatingActionButton>(R.id.fab)
        val bot = activity?.findViewById<BottomNavigationView>(R.id.bottom_nav)
        fab?.visibility = View.INVISIBLE
        fragBinding.recyclerViewLiked.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            var y = 0
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                y = dy
                super.onScrolled(recyclerView, dx, dy)

            }

            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                super.onScrollStateChanged(recyclerView, newState)
                if (y > 0) {
                    fab!!.visibility = View.VISIBLE
                } else {
                    fab!!.visibility = View.INVISIBLE
                }
            }
        })
        fab!!.setOnClickListener {
            fragBinding.recyclerViewLiked.smoothScrollToPosition(0)
            bot!!.visibility = View.VISIBLE

        }
        likedListViewModel.observableLikedList.observe(viewLifecycleOwner) { story ->
            story?.let {
                render(story as ArrayList<StoryModel>)
                checkSwipeRefresh()
            }
            hideLoader(loader)
            if (fragBinding.recyclerViewLiked.adapter!!.itemCount == 0 && searching != null) {
                val st = ArrayList<StoryModel>()
                st.add(StoryModel(title = "1"))
                fragBinding.recyclerViewLiked.adapter = EmptyAdapter(st, this)
                state?.let { fragBinding.recyclerViewLiked.layoutManager?.onRestoreInstanceState(it) }
            } else if (fragBinding.recyclerViewLiked.adapter!!.itemCount == 0) {
                fragBinding.creepy.visibility = View.VISIBLE
            }
            if (fragBinding.recyclerViewLiked.adapter!!.itemCount > 0) {
                fragBinding.creepy.visibility = View.INVISIBLE
            }
            Glide.with(this).load(R.drawable.bidenlost).into(fragBinding.imageView2)
            fragBinding.emptydate.visibility = View.INVISIBLE
            fragBinding.larrow.visibility = View.INVISIBLE
            fragBinding.rarrow.visibility = View.INVISIBLE

        }
        setSwipeRefresh()

        val swipeDeleteHandler = object : SwipeToDeleteLikedCallback(requireContext()) {
            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                if (viewHolder.itemViewType != 1) {
                    val adapter = fragBinding.recyclerViewLiked.adapter as NoSaveAdapter
                    adapter.removeAt(viewHolder.absoluteAdapterPosition)
                    likedListViewModel.delete(
                        likedListViewModel.liveFirebaseUser.value?.uid!!,
                        (viewHolder.itemView.tag as StoryModel).title
                    )
                }
            }
        }
        val itemTouchDeleteHelper = ItemTouchHelper(swipeDeleteHandler)
        itemTouchDeleteHelper.attachToRecyclerView(fragBinding.recyclerViewLiked)
        return root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val menuHost: MenuHost = requireActivity()
        menuHost.addMenuProvider(this, viewLifecycleOwner, Lifecycle.State.RESUMED)
    }

    private fun setSwipeRefresh() {
        fragBinding.swipe.setOnRefreshListener {
            fragBinding.swipe.isRefreshing = true
            state = fragBinding.recyclerViewLiked.layoutManager?.onSaveInstanceState()
            likedListViewModel.load()
        }
    }

    private fun checkSwipeRefresh() {
        if (fragBinding.swipe.isRefreshing)
            fragBinding.swipe.isRefreshing = false
    }

    private fun render(storyList: ArrayList<StoryModel>) {
        fragBinding.recyclerViewLiked.adapter = NoSaveAdapter(storyList, this)
        state?.let { fragBinding.recyclerViewLiked.layoutManager?.onRestoreInstanceState(it) }
    }

    override fun onResume() {
        super.onResume()
        loggedInViewModel.liveFirebaseUser.observe(viewLifecycleOwner) { firebaseUser ->
            if (firebaseUser != null) {
                likedListViewModel.liveFirebaseUser.value = firebaseUser
                likedListViewModel.load()
            }
        }
    }

    override fun onPause() {
        state = fragBinding.recyclerViewLiked.layoutManager?.onSaveInstanceState()
        super.onPause()
    }

    override fun onStoryClick(story: StoryModel) {
        StoryManager.createLiked(loggedInViewModel.liveFirebaseUser.value!!.uid, "history", story)
        val intent = Intent(Intent.ACTION_VIEW).setData(Uri.parse(story.link))
        state = fragBinding.recyclerViewLiked.layoutManager?.onSaveInstanceState()
        startActivity(intent)
    }

    override fun onLike(story: StoryModel) {
    }

    override fun onShare(story: StoryModel) {
        val sendIntent: Intent = Intent().apply {
            action = Intent.ACTION_SEND
            putExtra(Intent.EXTRA_TEXT, story.link)
            putExtra(Intent.EXTRA_TITLE, story.title)
            type = "text/html"
        }
        val shareIntent = Intent.createChooser(sendIntent, null)
        state = fragBinding.recyclerViewLiked.layoutManager?.onSaveInstanceState()
        startActivity(shareIntent)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _fragBinding = null
    }

    override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
        menuInflater.inflate(R.menu.menu_liked, menu)
        when (resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK) {
            Configuration.UI_MODE_NIGHT_YES -> {
                menu.findItem(R.id.app_bar_right).iconTintList = null
                menu.findItem(R.id.app_bar_left).iconTintList = null
            }
        }
        /* Finding the search bar in the menu and setting it to the search view. */
        val item = menu.findItem(R.id.app_bar_search)
        val searchView = item.actionView as SearchView

        /* This is the code that is executed when the search bar is used. It searches the database for
        the building that the user is searching for. */
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                return false
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                if (newText != null) {
                    searching = newText
                    likedListViewModel.search(
                        newText
                    )
                } else {
                    searching = newText
                    likedListViewModel.load()
                }
                if (newText == "") {
                    searching = newText
                    likedListViewModel.load()
                }

                return true
            }
        })
        searchView.setOnCloseListener {
            searching = null
            likedListViewModel.load()
            false
        }
    }

    override fun onMenuItemSelected(item: MenuItem): Boolean {
        if (item.itemId == R.id.app_bar_right) {

        }
        if (item.itemId == R.id.app_bar_left) {

        }
        return false
    }
}