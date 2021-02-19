package com.example.walkie.view

import android.content.ClipData
import android.content.DialogInterface
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.view.menu.MenuView
import androidx.navigation.NavController
import androidx.navigation.NavHostController
import androidx.navigation.findNavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.fragment.NavHostFragment.findNavController
import com.example.walkie.R
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.fragment_app_bar.*
import java.time.Duration


// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [AppBarFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class AppBarFragment : Fragment() {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null

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
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_app_bar, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        topAppBar.setOnMenuItemClickListener {item->
        when(item.itemId){
            R.id.option_1->{
                val difficultyLevels = arrayOf("Beginner (Walk length: xkm to xkm)", "Intermediate (Walk length: xkm to xkm)", "Tryhard (Walk length: xkm to xkm)")
                activity?.let {
                        val builder = AlertDialog.Builder(it)
                        builder.setTitle("Pick walks difficulty level")
                                .setItems(difficultyLevels,
                                        DialogInterface.OnClickListener { dialog, which ->
                                            // The 'which' argument contains the index position
                                            // of the selected item
                                        })
                        builder.create()
                    builder.show()
                    } ?: throw IllegalStateException("Activity cannot be null")
                true
            }
            R.id.option_2->{
                true
            }
            R.id.option_3->{
                activity?.let {
                    // Use the Builder class for convenient dialog construction
                    val builder = AlertDialog.Builder(it)
                    builder.setTitle("Walkie - about app")
                            .setMessage("Walkie was created to help people go out of their homes more, which is important for both physical and mental health especially during pandemic situation.\n\nMotivational factors in this case are achievements that you can collect and improve as well as calendar that you can check anytime to make sure your walking routine works.")
                            .setPositiveButton("OK",
                                    DialogInterface.OnClickListener { dialog, id ->
                                        // FIRE ZE MISSILES!
                                    })
                    // Create the AlertDialog object and return it
                    builder.create()
                    builder.show()
                } ?: throw IllegalStateException("Activity cannot be null")
                true
            }
            else->{
                false
            }
        }

        }


    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment AppBar.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            AppBarFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
}