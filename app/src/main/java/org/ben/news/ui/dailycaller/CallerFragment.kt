package org.ben.news.ui.dailycaller

import androidx.lifecycle.ViewModelProvider
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import org.ben.news.R

class CallerFragment : Fragment() {

    companion object {
        fun newInstance() = CallerFragment()
    }

    private lateinit var viewModel: CallerViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_caller, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel = ViewModelProvider(this).get(CallerViewModel::class.java)
        // TODO: Use the ViewModel
    }

}