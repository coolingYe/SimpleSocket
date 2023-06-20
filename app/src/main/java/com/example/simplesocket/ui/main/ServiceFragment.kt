package com.example.simplesocket.ui.main

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.example.simplesocket.databinding.FragmentServiceBinding
import com.example.simplesocket.server.Callback
import com.example.simplesocket.server.SocketServer

/**
 * A placeholder fragment containing a simple view.
 */
class ServiceFragment : Fragment(), Callback {

    private val TAG = ServiceFragment::class.java.simpleName
    private lateinit var pageViewModel: PageViewModel
    private var _binding: FragmentServiceBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        pageViewModel = ViewModelProvider(this).get(PageViewModel::class.java).apply {
            setIndex(arguments?.getInt(ARG_SECTION_NUMBER) ?: 1)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        _binding = FragmentServiceBinding.inflate(inflater, container, false)
        val root = binding.root

        val textView: TextView = binding.sectionLabel
        pageViewModel.text.observe(viewLifecycleOwner, Observer {
            textView.text = it
        })
        return root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.buttonStart.setOnClickListener {
            SocketServer.startServer(this)
        }

        binding.buttonStop.setOnClickListener {
            SocketServer.stopServer()
        }
    }

    companion object {
        /**
         * The fragment argument representing the section number for this
         * fragment.
         */
        private const val ARG_SECTION_NUMBER = "section_number"

        /**
         * Returns a new instance of this fragment for the given section
         * number.
         */
        @JvmStatic
        fun newInstance(sectionNumber: Int): ServiceFragment {
            return ServiceFragment().apply {
                arguments = Bundle().apply {
                    putInt(ARG_SECTION_NUMBER, sectionNumber)
                }
            }
        }
    }

    override fun resultMsg(ipAddress: String, msg: String) {
        Log.d(TAG, "ipAddress: $ipAddress msg: $msg")
    }

    override fun otherMsg(msg: String) {
        Log.d(TAG, "otherMsg: $msg")
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}