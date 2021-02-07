package com.example.walkie.view

import android.view.View
import android.widget.TextView
import com.example.walkie.R
import com.kizitonwose.calendarview.model.CalendarDay
import com.kizitonwose.calendarview.ui.DayBinder
import com.kizitonwose.calendarview.ui.ViewContainer

class CalendarMonthHeader(view: View) : ViewContainer(view) {
    val textView = view.findViewById<TextView>(R.id.month_header)

}