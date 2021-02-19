package com.example.walkie.view

import android.annotation.SuppressLint
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.observe
import com.example.walkie.R
import com.example.walkie.model.Walk
import com.example.walkie.viewmodel.UserViewModel
import com.kizitonwose.calendarview.model.CalendarDay
import com.kizitonwose.calendarview.model.CalendarMonth
import com.kizitonwose.calendarview.model.DayOwner
import com.kizitonwose.calendarview.model.ScrollMode
import com.kizitonwose.calendarview.ui.DayBinder
import com.kizitonwose.calendarview.ui.MonthHeaderFooterBinder
import kotlinx.android.synthetic.main.fragment_calendar.*
import java.sql.Time
import java.text.DateFormat
import java.time.Month
import java.time.Year
import java.time.YearMonth
import java.time.ZoneId
import java.time.temporal.WeekFields
import java.util.*
import kotlin.math.roundToInt


// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [CalendarFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class CalendarFragment : Fragment() {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null
    private lateinit var viewModel: UserViewModel
    private var walks: List<Walk>? = null
    private var selected: DayViewContainer? = null
    private var previousSelected: DayViewContainer? = null
    private var previousDay: CalendarDay? = null


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

        viewModel = ViewModelProvider(requireActivity()).get(UserViewModel::class.java)


        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_calendar, container, false)
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel.walkViewModel.walks.observe(viewLifecycleOwner, androidx.lifecycle.Observer { liveDataWalks->
            this.walks = liveDataWalks

            walkingCalendar.monthHeaderBinder = object: MonthHeaderFooterBinder<CalendarMonthHeader>{
                override fun create(view: View): CalendarMonthHeader = CalendarMonthHeader(view)

                override fun bind(container: CalendarMonthHeader, month: CalendarMonth) {
                    container.textView.text = Month.of(month.month).toString()+" "+ Year.of(month.year)
                }
            }

            walkingCalendar.dayBinder = object : DayBinder<DayViewContainer> {

                // Called only when a new container is needed.
                override fun create(view: View) = DayViewContainer(view)

                // Called every time we need to reuse a container.
                override fun bind(container: DayViewContainer, day: CalendarDay) {
                    viewModel.walkViewModel.walks.observe(viewLifecycleOwner, androidx.lifecycle.Observer { t->

                    })
                    container.textView.text = day.date.dayOfMonth.toString()
                    if (day.owner == DayOwner.THIS_MONTH) {
                        container.textView.setTextColor(Color.WHITE)
                        container.textView.setBackgroundColor(Color.parseColor("#f46e5f"))
                        container.textView.setOnClickListener {
                            //walkingCalendar.notifyDateChanged(day.date)
                            daySelected(container, day)
                        }
                    } else {
                        container.textView.setTextColor(Color.LTGRAY)
                        container.textView.setBackgroundColor(Color.parseColor("#ffb3a2"))
                    }

                    for(walk in walks!!){
                        if(walk.date.toInstant().atZone(ZoneId.systemDefault()).toLocalDate().toString() == day.date.toString() && walk.isComplete){
                            container.textView.setBackgroundColor(Color.parseColor("#00e68a"))
                        }
                    }
                }
            }
            val currentMonth = YearMonth.now()
            val firstMonth = currentMonth.minusMonths(24)
            val lastMonth = currentMonth.plusMonths(24)
            val firstDayOfWeek = WeekFields.of(Locale.getDefault()).firstDayOfWeek
            walkingCalendar.setup(firstMonth, lastMonth, firstDayOfWeek)
            walkingCalendar.scrollToMonth(currentMonth)
            walkingCalendar.scrollMode = ScrollMode.PAGED
        })
    }


    @SuppressLint("SetTextI18n")
    private fun daySelected(container: DayViewContainer, day: CalendarDay){
        if(selected != null) previousSelected = selected
        selected = container
        selected!!.textView.setBackgroundColor(Color.parseColor("#e6b800"))


        dayDescriptionTextView.text = "You haven't have a walk on that day."
        for(walk in walks!!){
            if(walk.date.toInstant().atZone(ZoneId.systemDefault()).toLocalDate().toString() == day!!.date.toString() && walk.isComplete){
                dayDescriptionTextView.text = "On "+day.date.dayOfMonth+" of "+day.date.month.name.toLowerCase()+" "+day.date.year+" you've finished a "+(walk.length/1000).toFloat()+" kilometers long walk. Congratulations!"
            }
        }






        if(previousSelected != null) {
            previousSelected!!.textView.setBackgroundColor(Color.parseColor("#f46e5f"))
            for(walk in walks!!){
                if(walk.date.toInstant().atZone(ZoneId.systemDefault()).toLocalDate().toString() == previousDay!!.date.toString() && walk.isComplete){
                    previousSelected!!.textView.setBackgroundColor(Color.parseColor("#00e68a"))
                }
            }
        }
        previousDay = day
    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment CalendarFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
                CalendarFragment().apply {
                    arguments = Bundle().apply {
                        putString(ARG_PARAM1, param1)
                        putString(ARG_PARAM2, param2)
                    }
                }
    }
}