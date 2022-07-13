package org.ben.news.ui.zerohedge

import androidx.lifecycle.ViewModelProvider
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import org.ben.news.R

class ZerohedgeFragment : Fragment() {

    companion object {
        fun newInstance() = ZerohedgeFragment()
    }

    private lateinit var viewModel: ZerohedgeViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_zerohedge, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel = ViewModelProvider(this).get(ZerohedgeViewModel::class.java)
        // TODO: Use the ViewModel
    }

}