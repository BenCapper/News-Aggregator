package org.ben.news.ui.us

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
import com.google.android.material.bottomnavigation.BottomNavigationView
import org.ben.news.R
import org.ben.news.adapters.StoryAdapter
import org.ben.news.adapters.StoryListener
import org.ben.news.databinding.FragmentUsBinding
import org.ben.news.firebase.StoryManager
import org.ben.news.helpers.createLoader
import org.ben.news.helpers.hideLoader
import org.ben.news.helpers.showLoader
import org.ben.news.models.StoryModel
import org.ben.news.ui.auth.LoggedInViewModel
import splitties.snackbar.snack


class UsFragment : Fragment(), StoryListener {

    companion object {
        fun newInstance() = UsFragment()
    }
    private var _fragBinding: FragmentUsBinding? = null
    private val fragBinding get() = _fragBinding!!
    lateinit var loader : AlertDialog
    private val loggedInViewModel : LoggedInViewModel by activityViewModels()
    private val usViewModel: UsViewModel by activityViewModels()
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
        _fragBinding = FragmentUsBinding.inflate(inflater, container, false)
        val root = fragBinding.root
        fragBinding.recyclerViewUs.layoutManager = activity?.let { LinearLayoutManager(it) }
        activity?.findViewById<ImageView>(R.id.toolimg)?.setImageResource(R.drawable.american)
        activity?.findViewById<BottomNavigationView>(R.id.bottom_nav)?.visibility = View.INVISIBLE

        MobileAds.initialize(this.context!!) {}

        usViewModel.observableUsList.observe(viewLifecycleOwner) { story ->
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
                    usViewModel.search(
                        newText
                    )
                }
                else {
                    usViewModel.load()
                }
                return true
            }
        })
        super.onCreateOptionsMenu(menu, inflater)
    }

    private fun render(storyList: ArrayList<StoryModel>) {
        fragBinding.recyclerViewUs.adapter = StoryAdapter(storyList, this)
        state?.let { fragBinding.recyclerViewUs.layoutManager?.onRestoreInstanceState(it) }
    }

    override fun onResume() {
        super.onResume()
        showLoader(loader,"")
        loggedInViewModel.liveFirebaseUser.observe(viewLifecycleOwner) { firebaseUser ->
            if (firebaseUser != null) {
                usViewModel.liveFirebaseUser.value = firebaseUser
                usViewModel.load()
            }
        }
    }

    override fun onPause() {
        state = fragBinding.recyclerViewUs.layoutManager?.onSaveInstanceState()
        super.onPause()
    }

    override fun onStoryClick(story: StoryModel) {
        StoryManager.create(loggedInViewModel.liveFirebaseUser.value!!.uid,"history", story)
        val intent = Intent(Intent.ACTION_VIEW).setData(Uri.parse(story.link))
        state = fragBinding.recyclerViewUs.layoutManager?.onSaveInstanceState()
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
        state = fragBinding.recyclerViewUs.layoutManager?.onSaveInstanceState()
        startActivity(shareIntent)
    }
    override fun onDestroyView() {
        super.onDestroyView()
        _fragBinding = null
    }
}