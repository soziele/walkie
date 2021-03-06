package com.example.walkie.view

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.walkie.R
import com.example.walkie.model.Achievement
import com.example.walkie.model.enums.Difficulty
import com.example.walkie.viewmodel.AchievementViewModel
import com.example.walkie.viewmodel.AchievementsListAdapter
import com.example.walkie.viewmodel.StateViewModel
import com.example.walkie.viewmodel.UserViewModel
import com.google.gson.Gson
import kotlinx.android.synthetic.main.fragment_achievements.*

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [AchievementsFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class AchievementsFragment : Fragment() {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null
    private lateinit var myadapter: AchievementsListAdapter
    private lateinit var recyclerView: RecyclerView
    private lateinit var myLayoutManager: LinearLayoutManager
    private lateinit var viewModel: UserViewModel
    private lateinit var stateViewModel: StateViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {


        stateViewModel = ViewModelProvider(requireActivity()).get(StateViewModel::class.java)
        viewModel = ViewModelProvider(requireActivity()).get(UserViewModel::class.java)
        myadapter = AchievementsListAdapter(viewModel.achievementViewModel.achievements, viewModel, stateViewModel, requireContext(), requireActivity(), viewLifecycleOwner)
        myLayoutManager= LinearLayoutManager(context)

        viewModel.achievementViewModel.achievements.observe(viewLifecycleOwner, Observer { t ->
            if(t.isEmpty()) {
                viewModel.achievementViewModel.seedDatabase()
            }

            myadapter.notifyDataSetChanged()
        })

        return inflater.inflate(R.layout.fragment_achievements, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        stateViewModel = ViewModelProvider(this).get(StateViewModel::class.java)

        recyclerView = achievements_list.apply {
            this.layoutManager = myLayoutManager
            this.adapter = myadapter
        }
    }



    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment AchievementsFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            AchievementsFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
}