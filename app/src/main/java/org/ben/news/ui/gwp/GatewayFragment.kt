package org.ben.news.ui.gwp

import androidx.lifecycle.ViewModelProvider
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import org.ben.news.R

class GatewayFragment : Fragment() {

    companion object {
        fun newInstance() = GatewayFragment()
    }

    private lateinit var viewModel: GatewayViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_gateway, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel = ViewModelProvider(this).get(GatewayViewModel::class.java)
        // TODO: Use the ViewModel
    }

}