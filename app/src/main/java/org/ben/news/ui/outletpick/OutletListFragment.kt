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
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.bottomappbar.BottomAppBar
import com.google.android.material.bottomnavigation.BottomNavigationView
import org.ben.news.R
import org.ben.news.adapters.*
import org.ben.news.databinding.FragmentOutletlistBinding
import org.ben.news.helpers.createLoader
import org.ben.news.helpers.showLoader
import org.ben.news.models.OutletModel
import org.ben.news.models.StoryModel
import org.ben.news.ui.auth.LoggedInViewModel

class OutletListFragment : Fragment(), OutletListener, MenuProvider {

    companion object {
        fun newInstance() = OutletListFragment()
    }

    private var _fragBinding: FragmentOutletlistBinding? = null
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
        activity?.findViewById<ImageView>(R.id.toolimg)?.setImageResource(R.drawable.selfeed)
        activity?.findViewById<BottomAppBar>(R.id.bottomAppBar)?.visibility = View.INVISIBLE
        _fragBinding = FragmentOutletlistBinding.inflate(inflater, container, false)
        val root = fragBinding.root
        fragBinding.recyclerViewOutlet.layoutManager = activity?.let { LinearLayoutManager(it) }

        outletViewModel.observableOutletList.observe(viewLifecycleOwner) { outlet ->
            outlet?.let {
                render(outlet as ArrayList<OutletModel>)
            }
        }
        return root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val menuHost: MenuHost = requireActivity()
        menuHost.addMenuProvider(this, viewLifecycleOwner, Lifecycle.State.RESUMED)
    }


    private fun render(outletList: ArrayList<OutletModel>) {
        fragBinding.recyclerViewOutlet.adapter = OutletAdapter(outletList, this)
        state?.let { fragBinding.recyclerViewOutlet.layoutManager?.onRestoreInstanceState(it) }
    }

    override fun onResume() {
        activity?.findViewById<Toolbar>(R.id.toolbar)?.visibility = View.VISIBLE
        super.onResume()
        loggedInViewModel.liveFirebaseUser.observe(viewLifecycleOwner) { firebaseUser ->
            if (firebaseUser != null) {
                outletViewModel.liveFirebaseUser.value = firebaseUser
                outletViewModel.load()
            }
        }
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
        menuInflater.inflate(R.menu.menu_outlets, menu)
        when (resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK) {
            Configuration.UI_MODE_NIGHT_YES -> {
                menu.findItem(R.id.app_bar_confirm).iconTintList = null
            }
        }
    }

    override fun onMenuItemSelected(item: MenuItem): Boolean {
        if (item.itemId == R.id.app_bar_confirm) {
            outletViewModel.saveOutlets(loggedInViewModel.liveFirebaseUser.value!!.uid, outletViewModel.observableOutletList.value!!)
            findNavController().navigate(R.id.action_outletListFragment_to_feedFragment)
        }
        return false
    }

    override fun onRadio(outlet: OutletModel) {
        outlet.selected = !outlet.selected
    }
}