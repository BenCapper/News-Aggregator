package org.ben.news.ui.skyuk

import android.app.AlertDialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Parcelable
import android.view.*
import androidx.fragment.app.Fragment
import android.widget.ImageView
import android.widget.SearchView
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.gms.ads.MobileAds
import org.ben.news.R
import org.ben.news.adapters.StoryAdapter
import org.ben.news.adapters.StoryListener
import org.ben.news.databinding.FragmentSkyBinding
import org.ben.news.firebase.StoryManager
import org.ben.news.helpers.createLoader
import org.ben.news.helpers.hideLoader
import org.ben.news.helpers.showLoader
import org.ben.news.models.StoryModel
import org.ben.news.ui.auth.LoggedInViewModel
import splitties.snackbar.snack


class SkyFragment : Fragment(), StoryListener {

    companion object {
        fun newInstance() = SkyFragment()
    }
    private var _fragBinding: FragmentSkyBinding? = null
    private val fragBinding get() = _fragBinding!!
    lateinit var loader : AlertDialog
    private val loggedInViewModel : LoggedInViewModel by activityViewModels()
    private val skyViewModel: SkyViewModel by activityViewModels()
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
        _fragBinding = FragmentSkyBinding.inflate(inflater, container, false)
        val root = fragBinding.root
        fragBinding.recyclerViewSky.layoutManager = activity?.let { LinearLayoutManager(it) }
        activity?.findViewById<ImageView>(R.id.toolimg)?.setImageResource(R.drawable.sky)

        MobileAds.initialize(this.context!!) {}

        skyViewModel.observableSkyList.observe(viewLifecycleOwner) { story ->
            story?.let {
                render(story as ArrayList<StoryModel>)
                checkSwipeRefresh()
            }
            hideLoader(loader)
        }
        setSwipeRefresh()
        return root
    }

    private fun setSwipeRefresh() {
        fragBinding.swipe.setOnRefreshListener {
            fragBinding.swipe.isRefreshing = true
            skyViewModel.load()
        }
    }

    private fun checkSwipeRefresh() {
        if (fragBinding.swipe.isRefreshing)
            fragBinding.swipe.isRefreshing = false
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
                    skyViewModel.search(
                        newText
                    )
                }
                else{
                    skyViewModel.load()
                }
                if (newText == "") {
                    skyViewModel.load()
                }

                return true
            }
        })
        searchView.setOnCloseListener ( object : SearchView.OnCloseListener{
            override fun onClose(): Boolean {
                skyViewModel.load()
                return false
            }
        })
        super.onCreateOptionsMenu(menu, inflater)
    }

    private fun render(storyList: ArrayList<StoryModel>) {
        fragBinding.recyclerViewSky.adapter = StoryAdapter(storyList, this)
        state?.let { fragBinding.recyclerViewSky.layoutManager?.onRestoreInstanceState(it) }
    }

    override fun onResume() {
        super.onResume()
        showLoader(loader,"")
        loggedInViewModel.liveFirebaseUser.observe(viewLifecycleOwner) { firebaseUser ->
            if (firebaseUser != null) {
                skyViewModel.liveFirebaseUser.value = firebaseUser
                skyViewModel.load()
            }
        }
    }

    override fun onPause() {
        state = fragBinding.recyclerViewSky.layoutManager?.onSaveInstanceState()
        super.onPause()
    }

    override fun onStoryClick(story: StoryModel) {
        StoryManager.create(loggedInViewModel.liveFirebaseUser.value!!.uid,"history", story)
        val intent = Intent(Intent.ACTION_VIEW).setData(Uri.parse(story.link))
        state = fragBinding.recyclerViewSky.layoutManager?.onSaveInstanceState()
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
        state = fragBinding.recyclerViewSky.layoutManager?.onSaveInstanceState()
        startActivity(shareIntent)
    }
    override fun onDestroyView() {
        super.onDestroyView()
        _fragBinding = null
    }
}