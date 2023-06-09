package com.example.todoapp.mainMenu.calendar

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.todoapp.databinding.CalendarItemCellBinding
import com.example.todoapp.mainMenu.home.TaskItem.TaskItem
import java.time.YearMonth

class CalendarAdapter(
    private val daysInMonth: ArrayList<String>,
    private val clickListener: CalendarItemClickListener,
    private val yearMonth: YearMonth,
    private val selectedDay: String,
    private val taskList : ArrayList<TaskItem>
): RecyclerView.Adapter<CalendarViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CalendarViewHolder {

        val from = LayoutInflater.from(parent.context)
        val binding = CalendarItemCellBinding.inflate(from, parent, false)
        return CalendarViewHolder(parent.context, binding, clickListener,taskList)
    }

    override fun getItemCount(): Int {
        return daysInMonth.size
    }

    override fun onBindViewHolder(holder: CalendarViewHolder, position: Int) {
        holder.dayMonth.text = daysInMonth[position]
        holder.bindCalendarItem(yearMonth,selectedDay)
    }

}