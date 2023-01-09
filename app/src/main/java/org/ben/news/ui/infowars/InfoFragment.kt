package org.ben.news.ui.infowars

import android.app.AlertDialog
import android.content.Intent
import android.content.res.Configuration
import android.net.Uri
import android.os.Bundle
import android.os.Parcelable
import android.view.*
import androidx.fragment.app.Fragment
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.SearchView
import androidx.core.view.MenuHost
import androidx.core.view.MenuProvider
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.android.gms.ads.MobileAds
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import org.ben.news.R
import org.ben.news.adapters.EmptyAdapter
import org.ben.news.adapters.StoryAdapter
import org.ben.news.adapters.StoryListener
import org.ben.news.databinding.FragmentAmThinkBinding
import org.ben.news.databinding.FragmentInfoBinding
import org.ben.news.firebase.StoryManager
import org.ben.news.helpers.createLoader
import org.ben.news.helpers.hideLoader
import org.ben.news.helpers.showLoader
import org.ben.news.models.StoryModel
import org.ben.news.ui.amthink.AmThinkFragment
import org.ben.news.ui.amthink.AmThinkViewModel
import org.ben.news.ui.auth.LoggedInViewModel
import splitties.alertdialog.appcompat.*
import splitties.snackbar.snack
import splitties.views.textColorResource

class InfoFragment : Fragment(), StoryListener, MenuProvider {

    companion object {
        fun newInstance() = InfoFragment()
    }

    private var _fragBinding: FragmentInfoBinding? = null
    private val fragBinding get() = _fragBinding!!
    lateinit var loader: AlertDialog
    private val loggedInViewModel: LoggedInViewModel by activityViewModels()
    private val infoViewModel: InfoViewModel by activityViewModels()
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
        _fragBinding = FragmentInfoBinding.inflate(inflater, container, false)
        val root = fragBinding.root

        fragBinding.recyclerViewInfo.layoutManager = activity?.let { LinearLayoutManager(it) }
        activity?.findViewById<ImageView>(R.id.toolimg)?.setImageResource(R.drawable.info)
        MobileAds.initialize(this.requireContext()) {}
        val fab = activity?.findViewById<FloatingActionButton>(R.id.fab)
        val bot = activity?.findViewById<BottomNavigationView>(R.id.bottom_nav)
        fab?.visibility = View.INVISIBLE
        fragBinding.recyclerViewInfo.addOnScrollListener(object : RecyclerView.OnScrollListener() {
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
            fragBinding.recyclerViewInfo.smoothScrollToPosition(0)
            bot!!.visibility = View.VISIBLE
        }

        infoViewModel.observableInfoList.observe(viewLifecycleOwner) { story ->
            story?.let {
                render(story as ArrayList<StoryModel>)
                checkSwipeRefresh()
            }
            hideLoader(loader)
            if (fragBinding.recyclerViewInfo.adapter!!.itemCount == 0 && searching != null) {
                val st = ArrayList<StoryModel>()
                st.add(StoryModel(title = "1"))
                fragBinding.recyclerViewInfo.adapter = EmptyAdapter(st, this)
                state?.let { fragBinding.recyclerViewInfo.layoutManager?.onRestoreInstanceState(it) }
            } else if (fragBinding.recyclerViewInfo.adapter!!.itemCount == 0) {
                fragBinding.creepy.visibility = View.VISIBLE
            }
            if (fragBinding.recyclerViewInfo.adapter!!.itemCount > 0) {
                fragBinding.creepy.visibility = View.INVISIBLE
            }
            Glide.with(this).load(R.drawable.bidenlost).into(fragBinding.imageView2)
            val datenow = StoryManager.getDate(day)
            fragBinding.emptydate.text = datenow
            fragBinding.larrow.setOnClickListener {
                if (day < 14) {
                    showLoader(loader, "")
                    day += 1
                    infoViewModel.load(day)
                }
            }
            fragBinding.rarrow.setOnClickListener {
                if (day != 0) {
                    showLoader(loader, "")
                    day -= 1
                    if (day <= 0) {
                        day = 0
                    }
                    infoViewModel.load(day)
                }
            }
        }
        setSwipeRefresh()
        return root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val menuHost: MenuHost = requireActivity()
        menuHost.addMenuProvider(this, viewLifecycleOwner, Lifecycle.State.RESUMED)
        super.onViewCreated(view, savedInstanceState)
    }

    private fun setSwipeRefresh() {
        fragBinding.swipe.setOnRefreshListener {
            fragBinding.swipe.isRefreshing = true
            state = fragBinding.recyclerViewInfo.layoutManager?.onSaveInstanceState()
            infoViewModel.load(day)
        }
    }

    private fun checkSwipeRefresh() {
        if (fragBinding.swipe.isRefreshing)
            fragBinding.swipe.isRefreshing = false
    }


    private fun render(storyList: ArrayList<StoryModel>) {
        fragBinding.recyclerViewInfo.adapter = StoryAdapter(storyList, this)
        state?.let { fragBinding.recyclerViewInfo.layoutManager?.onRestoreInstanceState(it) }
    }

    override fun onResume() {
        infoViewModel.load(day)
        super.onResume()
    }

    override fun onPause() {
        state = fragBinding.recyclerViewInfo.layoutManager?.onSaveInstanceState()
        super.onPause()
    }

    override fun onStoryClick(story: StoryModel) {
        StoryManager.createLiked(loggedInViewModel.liveFirebaseUser.value!!.uid, "history", story)
        val intent = Intent(Intent.ACTION_VIEW).setData(Uri.parse(story.link))
        state = fragBinding.recyclerViewInfo.layoutManager?.onSaveInstanceState()
        startActivity(intent)
    }

    override fun onLike(story: StoryModel) {
        activity?.alertDialog {
            messageResource = R.string.save_art
            okButton {
                StoryManager.createLiked(
                    loggedInViewModel.liveFirebaseUser.value!!.uid,
                    "likes",
                    story
                )
                val params = fragBinding.root.layoutParams as FrameLayout.LayoutParams
                params.gravity = Gravity.CENTER_HORIZONTAL
                view?.snack(R.string.saved_article)
            }
            cancelButton { view?.snack(R.string.save_can) }
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
        state = fragBinding.recyclerViewInfo.layoutManager?.onSaveInstanceState()
        startActivity(shareIntent)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _fragBinding = null
    }

    override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
        menuInflater.inflate(R.menu.menu_all, menu)
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
                    infoViewModel.search(
                        day,
                        newText
                    )
                } else {
                    searching = newText
                    infoViewModel.load(day)
                }
                if (newText == "") {
                    searching = newText
                    infoViewModel.load(day)
                }

                return true
            }
        })
        searchView.setOnCloseListener {
            searching = null
            infoViewModel.load(day)
            false
        }

    }

    override fun onMenuItemSelected(item: MenuItem): Boolean {
        if (item.itemId == R.id.app_bar_right) {
            if (day != 0) {
                showLoader(loader, "")
                day -= 1
                if (day <= 0) {
                    day = 0
                }
                infoViewModel.load(day)
            }
        }
        if (item.itemId == R.id.app_bar_left) {
            if (day < 14) {
                showLoader(loader, "")
                day += 1
                infoViewModel.load(day)
            }
        }
        return false
    }

}