package org.ben.news.ui.conflict

import android.app.AlertDialog
import android.content.Intent
import android.content.res.Configuration
import android.net.Uri
import android.os.Bundle
import android.os.Parcelable
import android.view.*
import android.widget.FrameLayout
import androidx.fragment.app.Fragment
import android.widget.ImageView
import android.widget.SearchView
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.android.gms.ads.MobileAds
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import org.ben.news.R
import org.ben.news.adapters.*
import org.ben.news.databinding.FragmentConflictBinding
import org.ben.news.firebase.StoryManager
import org.ben.news.helpers.createLoader
import org.ben.news.helpers.hideLoader
import org.ben.news.helpers.showLoader
import org.ben.news.models.DoubleStoryModel
import org.ben.news.models.StoryModel
import org.ben.news.ui.auth.LoggedInViewModel
import splitties.alertdialog.appcompat.*
import splitties.snackbar.snack
import splitties.views.textColorResource
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

class ConflictFragment : Fragment(), DoubleStoryListener, StoryListener {

    companion object {
        fun newInstance() = ConflictFragment()
    }
    private var _fragBinding: FragmentConflictBinding? = null
    private val fragBinding get() = _fragBinding!!
    lateinit var loader : AlertDialog
    private val loggedInViewModel : LoggedInViewModel by activityViewModels()
    private val conViewModel: ConflictViewModel by activityViewModels()
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
        _fragBinding = FragmentConflictBinding.inflate(inflater, container, false)
        val root = fragBinding.root
        fragBinding.recyclerViewCon.layoutManager = activity?.let { LinearLayoutManager(it) }
        activity?.findViewById<ImageView>(R.id.toolimg)?.setImageResource(R.drawable.both)
        MobileAds.initialize(this.requireContext()) {}
        val fab = activity?.findViewById<FloatingActionButton>(R.id.fab)
        val bot = activity?.findViewById<BottomNavigationView>(R.id.bottom_nav)
        fab?.visibility = View.INVISIBLE
        fragBinding.recyclerViewCon.addOnScrollListener (object : RecyclerView.OnScrollListener(){
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
            fragBinding.recyclerViewCon.smoothScrollToPosition(0)
            bot!!.visibility = View.VISIBLE
        }

        conViewModel.observableConList.observe(viewLifecycleOwner) { story ->
            story?.let {
                render(story as ArrayList<DoubleStoryModel>)
                checkSwipeRefresh()
            }
            hideLoader(loader)
            if(fragBinding.recyclerViewCon.adapter!!.itemCount == 0 && searching != null){
                val st = ArrayList<StoryModel>()
                st.add(StoryModel(title="1"))
                fragBinding.recyclerViewCon.adapter = EmptyAdapter(st, this)
                state?.let { fragBinding.recyclerViewCon.layoutManager?.onRestoreInstanceState(it) }
            }
            else if(fragBinding.recyclerViewCon.adapter!!.itemCount == 0){
                fragBinding.creepy.visibility = View.VISIBLE
            }
            if (fragBinding.recyclerViewCon.adapter!!.itemCount > 0)
                fragBinding.creepy.visibility = View.INVISIBLE
                Glide.with(this).load(R.drawable.bidenlost).into(fragBinding.imageView2)
                val datenow = StoryManager.getDate(day)
                fragBinding.emptydate.text = datenow
                fragBinding.larrow.setOnClickListener {
                    if (day < 30) {
                        showLoader(loader, "")
                        day += 1
                        conViewModel.load(day)
                    }
                }
                fragBinding.rarrow.setOnClickListener {
                    showLoader(loader,"")
                    day -= 1
                    if (day <= 0 ){
                        day = 0
                    }
                    conViewModel.load(day)
                }

        }
        setSwipeRefresh()

        return root
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if( item.itemId == R.id.app_bar_right) {
            if(day != 0) {
                showLoader(loader, "")
                day -= 1
                if (day <= 0) {
                    day = 0
                }
                conViewModel.load(day)
            }
        }
        if( item.itemId == R.id.app_bar_left) {
            if (day < 30) {
                showLoader(loader, "")
                day += 1
                conViewModel.load(day)
            }
        }
        return super.onOptionsItemSelected(item)
    }

    private fun setSwipeRefresh() {
        fragBinding.swipe.setOnRefreshListener {
            fragBinding.swipe.isRefreshing = true
            state = fragBinding.recyclerViewCon.layoutManager?.onSaveInstanceState()
            conViewModel.load(day)
        }
    }

    private fun checkSwipeRefresh() {
        if (fragBinding.swipe.isRefreshing)
            fragBinding.swipe.isRefreshing = false
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.menu_all, menu)
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
                    conViewModel.search(
                        day,
                        newText
                    )
                }
                else{
                    searching = newText
                    conViewModel.load(day)
                }
                if (newText == "") {
                    searching = newText
                    conViewModel.load(day)
                }

                return true
            }
        })
        searchView.setOnCloseListener {
            searching = null
            conViewModel.load(day)
            false
        }
        super.onCreateOptionsMenu(menu, inflater)
    }


    private fun render(storyList: ArrayList<DoubleStoryModel>) {
        fragBinding.recyclerViewCon.adapter = ConflictAdapter(storyList, this)
        state?.let { fragBinding.recyclerViewCon.layoutManager?.onRestoreInstanceState(it) }
    }

    override fun onResume() {
        conViewModel.load(day)
        super.onResume()
    }

    override fun onPause() {
        state = fragBinding.recyclerViewCon.layoutManager?.onSaveInstanceState()
        super.onPause()
    }

    override fun onStoryClick(story: StoryModel) {
    }

    override fun onLike(story: StoryModel) {
    }

    override fun onShare(story: StoryModel) {
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _fragBinding = null
    }

    override fun onRightClick(story: DoubleStoryModel) {
        val article = StoryModel(story.title1,story.date1,story.outlet1,story.storage_link1,story.order,story.link1)
        StoryManager.create(loggedInViewModel.liveFirebaseUser.value!!.uid,"history", article)
        val intent = Intent(Intent.ACTION_VIEW).setData(Uri.parse(story.link1))

        state = fragBinding.recyclerViewCon.layoutManager?.onSaveInstanceState()

        startActivity(intent)
    }

    override fun onLeftClick(story: DoubleStoryModel) {
        val article = StoryModel(story.title2,story.date2,story.outlet2,story.storage_link2,story.order,story.link2)
        StoryManager.create(loggedInViewModel.liveFirebaseUser.value!!.uid,"history", article)
        val intent = Intent(Intent.ACTION_VIEW).setData(Uri.parse(story.link2))

        state = fragBinding.recyclerViewCon.layoutManager?.onSaveInstanceState()

        startActivity(intent)
    }

    override fun onLikeRight(story: DoubleStoryModel) {
        val article = StoryModel(story.title1,story.date1,story.outlet1,story.storage_link1,story.order,story.link1)
        activity?.alertDialog {
            messageResource = R.string.save_art
            okButton { StoryManager.create(loggedInViewModel.liveFirebaseUser.value!!.uid,"likes", article)
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

    override fun onLikeLeft(story: DoubleStoryModel) {
        val article = StoryModel(story.title2,story.date2,story.outlet2,story.storage_link2,story.order,story.link2)
        activity?.alertDialog {
            messageResource = R.string.save_art
            okButton { StoryManager.create(loggedInViewModel.liveFirebaseUser.value!!.uid,"likes", article)
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

    override fun onShareRight(story: DoubleStoryModel) {
        val sendIntent: Intent = Intent().apply {
            action = Intent.ACTION_SEND
            putExtra(Intent.EXTRA_TEXT, story.link1)
            putExtra(Intent.EXTRA_TITLE, story.title1)
            type = "text/html"
        }

        val shareIntent = Intent.createChooser(sendIntent, null)
        state = fragBinding.recyclerViewCon.layoutManager?.onSaveInstanceState()
        startActivity(shareIntent)
    }

    override fun onShareLeft(story: DoubleStoryModel) {
        val sendIntent: Intent = Intent().apply {
            action = Intent.ACTION_SEND
            putExtra(Intent.EXTRA_TEXT, story.link2)
            putExtra(Intent.EXTRA_TITLE, story.title2)
            type = "text/html"
        }

        val shareIntent = Intent.createChooser(sendIntent, null)
        state = fragBinding.recyclerViewCon.layoutManager?.onSaveInstanceState()
        startActivity(shareIntent)
    }

}