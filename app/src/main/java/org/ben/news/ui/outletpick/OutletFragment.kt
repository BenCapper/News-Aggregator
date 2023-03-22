package org.ben.news.ui.outletpick

import android.app.AlertDialog
import android.content.res.Configuration
import android.os.Bundle
import android.os.Parcelable
import android.view.*
import androidx.fragment.app.Fragment
import android.widget.ImageView
import androidx.appcompat.widget.Toolbar
import androidx.core.view.MenuHost
import androidx.core.view.MenuProvider
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import org.ben.news.R
import org.ben.news.adapters.*
import org.ben.news.databinding.FragmentOutletBinding
import org.ben.news.helpers.createLoader
import org.ben.news.helpers.showLoader
import org.ben.news.models.StoryModel
import org.ben.news.ui.auth.LoggedInViewModel

class OutletFragment : Fragment(), OutletListener, MenuProvider {

    companion object {
        fun newInstance() = OutletFragment()
    }

    private var _fragBinding: FragmentOutletBinding? = null
    private val fragBinding get() = _fragBinding!!
    lateinit var loader: AlertDialog
    private val loggedInViewModel: LoggedInViewModel by activityViewModels()
    private val outletViewModel: OutletViewModel by activityViewModels()
    var state: Parcelable? = null
    var shuffle: Boolean? = null
    var searching: String? = null
    var day = 0


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
        activity?.findViewById<ImageView>(R.id.toolimg)?.setImageResource(R.drawable.hometit)
        _fragBinding = FragmentOutletBinding.inflate(inflater, container, false)
        val root = fragBinding.root
        fragBinding.recyclerViewOutlet.layoutManager = activity?.let { LinearLayoutManager(it) }
        setSwipeRefresh()
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
            state = fragBinding.recyclerViewOutlet.layoutManager?.onSaveInstanceState()
            outletViewModel.load(day)
        }
    }


    private fun checkSwipeRefresh() {
        if (fragBinding.swipe.isRefreshing)
            fragBinding.swipe.isRefreshing = false
    }


    private fun render(storyList: ArrayList<StoryModel>) {
        fragBinding.recyclerViewOutlet.adapter = OutletAdapter(storyList, this)
        state?.let { fragBinding.recyclerViewOutlet.layoutManager?.onRestoreInstanceState(it) }
    }

    override fun onResume() {
        activity?.findViewById<Toolbar>(R.id.toolbar)?.visibility = View.VISIBLE
        outletViewModel.load(day)
        super.onResume()
    }

    override fun onPause() {
        activity?.findViewById<Toolbar>(R.id.toolbar)?.visibility = View.INVISIBLE
        super.onPause()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _fragBinding = null
    }

    override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
        menuInflater.inflate(R.menu.menu_home, menu)
        when (resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK) {
            Configuration.UI_MODE_NIGHT_YES -> {
                menu.findItem(R.id.app_bar_plus).iconTintList = null
                menu.findItem(R.id.app_bar_r).iconTintList = null
                menu.findItem(R.id.app_bar_l).iconTintList = null
            }
        }
    }

    override fun onMenuItemSelected(item: MenuItem): Boolean {
        val id = item.itemId
        if (item.itemId == R.id.app_bar_cancel) {

        }
        if (item.itemId == R.id.app_bar_confirm) {

        }
        return false
    }
}