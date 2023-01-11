package org.ben.news.ui.historyList

import android.app.AlertDialog
import android.content.Intent
import android.content.res.Configuration
import android.net.Uri
import android.os.Bundle
import android.os.Parcelable
import android.view.*
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.SearchView
import androidx.appcompat.widget.Toolbar
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
import org.ben.news.R
import org.ben.news.adapters.EmptyAdapter
import org.ben.news.adapters.HistoryAdapter
import org.ben.news.adapters.StoryListener
import org.ben.news.databinding.FragmentHistoryListBinding
import org.ben.news.firebase.StoryManager
import org.ben.news.helpers.*
import org.ben.news.models.StoryModel
import org.ben.news.ui.auth.LoggedInViewModel
import org.ben.news.ui.storyList.StoryListFragment
import splitties.alertdialog.appcompat.*
import splitties.snackbar.snack
import splitties.views.textColorResource
import kotlin.collections.ArrayList


class HistoryListFragment : Fragment(), StoryListener, MenuProvider {

    companion object {
        fun newInstance() = StoryListFragment()
    }
    private var _fragBinding: FragmentHistoryListBinding? = null
    private val fragBinding get() = _fragBinding!!
    lateinit var loader : AlertDialog
    private val loggedInViewModel : LoggedInViewModel by activityViewModels()
    private val historyListViewModel: HistoryListViewModel by activityViewModels()
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
        showLoader(loader,"")
        _fragBinding = FragmentHistoryListBinding.inflate(inflater, container, false)
        val root = fragBinding.root

        activity?.findViewById<ImageView>(R.id.toolimg)?.setImageResource(R.drawable.history)
        fragBinding.recyclerViewHistory.layoutManager = activity?.let { LinearLayoutManager(it) }
        MobileAds.initialize(this.requireContext()) {}
        val fab = activity?.findViewById<FloatingActionButton>(R.id.fab)
        val bot = activity?.findViewById<BottomNavigationView>(R.id.bottom_nav)
        fab?.visibility = View.INVISIBLE
        fragBinding.recyclerViewHistory.addOnScrollListener (object : RecyclerView.OnScrollListener(){
            var y = 0
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                y = dy
                super.onScrolled(recyclerView, dx, dy)

            }

            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                super.onScrollStateChanged(recyclerView, newState)
                if (y > 0){
                    fab!!.visibility = View.VISIBLE
                }
                else {
                    fab!!.visibility = View.INVISIBLE
                }
            }
        })
        fab!!.setOnClickListener {
            fragBinding.recyclerViewHistory.smoothScrollToPosition(0)
            bot!!.visibility = View.VISIBLE

        }
        historyListViewModel.observableHistoryList.observe(viewLifecycleOwner) { story ->
            story?.let {
                render(story as ArrayList<StoryModel>)
                checkSwipeRefresh()
            }
            hideLoader(loader)
            if(fragBinding.recyclerViewHistory.adapter!!.itemCount == 0 && searching != null){
                val st = ArrayList<StoryModel>()
                st.add(StoryModel(title="1"))
                fragBinding.recyclerViewHistory.adapter = EmptyAdapter(st, this)
                state?.let { fragBinding.recyclerViewHistory.layoutManager?.onRestoreInstanceState(it) }
            }
            else if(fragBinding.recyclerViewHistory.adapter!!.itemCount == 0){
                fragBinding.creepy.visibility = View.VISIBLE
            }
            if (fragBinding.recyclerViewHistory.adapter!!.itemCount > 0) {
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
                    val adapter = fragBinding.recyclerViewHistory.adapter as HistoryAdapter
                    adapter.removeAt(viewHolder.absoluteAdapterPosition)
                    historyListViewModel.delete(
                        historyListViewModel.liveFirebaseUser.value?.uid!!,
                        (viewHolder.itemView.tag as StoryModel).title
                    )
                }
            }
        }
        val itemTouchDeleteHelper = ItemTouchHelper(swipeDeleteHandler)
        itemTouchDeleteHelper.attachToRecyclerView(fragBinding.recyclerViewHistory)
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
            state = fragBinding.recyclerViewHistory.layoutManager?.onSaveInstanceState()
            historyListViewModel.load()
        }
    }

    private fun checkSwipeRefresh() {
        if (fragBinding.swipe.isRefreshing)
            fragBinding.swipe.isRefreshing = false
    }


    private fun render(storyList: ArrayList<StoryModel>) {
        fragBinding.recyclerViewHistory.adapter = HistoryAdapter(storyList, this)
        state?.let { fragBinding.recyclerViewHistory.layoutManager?.onRestoreInstanceState(it) }
    }

    override fun onResume() {
        activity?.findViewById<Toolbar>(R.id.toolbar)?.visibility = View.VISIBLE
        super.onResume()
        loggedInViewModel.liveFirebaseUser.observe(viewLifecycleOwner) { firebaseUser ->
            if (firebaseUser != null) {
                historyListViewModel.liveFirebaseUser.value = firebaseUser
                historyListViewModel.load()
            }
        }
    }

    override fun onPause() {
        state = fragBinding.recyclerViewHistory.layoutManager?.onSaveInstanceState()
        activity?.findViewById<Toolbar>(R.id.toolbar)?.visibility = View.INVISIBLE
        super.onPause()
    }

    override fun onStoryClick(story: StoryModel) {
        StoryManager.createLiked(loggedInViewModel.liveFirebaseUser.value!!.uid, "history",story)
        val intent = Intent(Intent.ACTION_VIEW).setData(Uri.parse(story.link))
        state = fragBinding.recyclerViewHistory.layoutManager?.onSaveInstanceState()
        startActivity(intent)
    }

    override fun onLike(story: StoryModel) {
        activity?.alertDialog {
            messageResource = R.string.save_art
            okButton { StoryManager.createLiked(loggedInViewModel.liveFirebaseUser.value!!.uid,"likes", story)
                val params = fragBinding.root.layoutParams as FrameLayout.LayoutParams
                params.gravity = Gravity.CENTER_HORIZONTAL
                view?.snack(R.string.saved_article)}
            cancelButton{ view?.snack(R.string.save_can)}
        }?.onShow {
            positiveButton.textColorResource = R.color.black
            negativeButton.textColorResource = splitties.material.colors.R.color.grey_500
            positiveButton.text = "Confirm"
            negativeButton.text = "Cancel"
        }?.show()
    }

    override fun onShare(story: StoryModel) {
        val sendIntent: Intent = Intent().apply {
            action = Intent.ACTION_SEND
            putExtra(Intent.EXTRA_TEXT, story.link)
            putExtra(Intent.EXTRA_TITLE, story.title)
            type = "text/html"
        }
        val shareIntent = Intent.createChooser(sendIntent, null)
        state = fragBinding.recyclerViewHistory.layoutManager?.onSaveInstanceState()
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
        searchView.setOnQueryTextListener(object :  SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                return false
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                if (newText != null) {
                    searching = newText
                    historyListViewModel.search(
                        newText
                    )
                }
                else{
                    searching = newText
                    historyListViewModel.load()
                }
                if (newText == "") {
                    searching = newText
                    historyListViewModel.load()
                }

                return true
            }
        })
        searchView.setOnCloseListener {
            searching = null
            historyListViewModel.load()
            false
        }

    }

    override fun onMenuItemSelected(item: MenuItem): Boolean {
        if( item.itemId == R.id.app_bar_right) {

        }
        if( item.itemId == R.id.app_bar_left) {

        }
        return false
    }
}