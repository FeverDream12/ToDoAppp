package com.example.todoapp

import android.annotation.SuppressLint
import android.app.*
import android.content.Context
import android.content.Intent
import android.content.res.Resources.Theme
import android.os.Bundle
import android.text.Editable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.example.todoapp.ui.home.TaskItem.TaskItem
import com.example.todoapp.databinding.FragmentNewTaskSheetBinding
import com.example.todoapp.ui.home.TaskItem.TaskViewModel
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.LocalTime
import java.util.*
import kotlin.random.Random.Default.nextInt

class NewTaskSheet(var taskItem: TaskItem?) : BottomSheetDialogFragment() {

    private lateinit var binding: FragmentNewTaskSheetBinding
    private lateinit var taskViewModel: TaskViewModel
    private var dueTime: LocalTime? = null
    private var dueDate: String? = null
    private var gotNot: Boolean = false


    @SuppressLint("ResourceAsColor")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val activity = requireActivity()

        if(taskItem != null){
            binding.taskTitle.text = "Изменить задачу"
            val editable = Editable.Factory.getInstance()
            binding.name.text = editable.newEditable(taskItem!!.name)
            binding.desc.text = editable.newEditable(taskItem!!.desc)
            if (taskItem!!.dueTime() != null){
                dueTime = taskItem!!.dueTime()!!
            }
            if(taskItem!!.dueDate != null){
                dueDate = taskItem!!.dueDate!!
                binding.DueText.text = "Отложено на: " + taskItem!!.dueDate
            }
        }else{
            binding.taskTitle.text = "Новая задача"
        }

        taskViewModel = ViewModelProvider(activity)[TaskViewModel::class.java]

        binding.saveButton.setOnClickListener{
            saveAction()
        }
        binding.timePickerButton.setOnClickListener{
            openTimePicker()
            binding.notifButton.visibility = ImageButton.VISIBLE
        }

        binding.notifButton.setOnClickListener{
            if(dueTime != null && dueDate != null){
                gotNot = !gotNot

                if(gotNot){
                    Toast.makeText(context, "Уведомление установленно", Toast.LENGTH_SHORT).show()
                }else{
                    Toast.makeText(context, "Уведомление отменено", Toast.LENGTH_SHORT).show()
                }

            }
        }

        val calendar = Calendar.getInstance()


        binding.dueDatePicker.setOnClickListener{

            val datePicker = DatePickerDialog.OnDateSetListener{ view, year, month, dayOfMonth ->
                calendar.set(Calendar.YEAR,year)
                calendar.set(Calendar.MONTH,month)
                calendar.set(Calendar.DAY_OF_MONTH,dayOfMonth)

                val myFormat = "yyyy-MM-dd"
                val simpleDateFormat = SimpleDateFormat(myFormat, Locale.KOREA)
                dueDate = simpleDateFormat.format(calendar.time)
            }

            DatePickerDialog(activity, datePicker, calendar.get(Calendar.YEAR),calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH) ).show()

        }
    }

    private fun scheduleNotification(id: Int) {
        val intent = Intent(requireContext(), NotificationReceiver::class.java)
        val title = binding.name.text.toString()
        val message = binding.desc.text.toString()

        intent.putExtra(titleExtra,title)
        intent.putExtra(messageExtra,message)

        val pendingIntent = PendingIntent.getBroadcast(requireContext(), id,intent,PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT)
        val alarmManager = requireContext().getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val df = SimpleDateFormat("yyyy-MM-dd-HH:mm")
        //val time : Long = df.parse("2023.03.30-13:15").time
        val timeStr : String = dueDate + "-" + dueTime?.toString()
        val time : Long = df.parse(timeStr).time

        alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP,time,pendingIntent)
    }

    private fun cancelScheduledNotification(id: Int) {
        val intent = Intent(requireContext(), NotificationReceiver::class.java)

        val pendingIntent = PendingIntent.getBroadcast(requireContext(), id,intent,PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT)
        val alarmManager = requireContext().getSystemService(Context.ALARM_SERVICE) as AlarmManager

        alarmManager.cancel(pendingIntent)
    }


    private fun openTimePicker() {
        if(dueTime == null)
            dueTime = LocalTime.now()
        val listener = TimePickerDialog.OnTimeSetListener{_, selectedHour, selectedMinute ->
            dueTime = LocalTime.of(selectedHour,selectedMinute)
        }
        val dialog = TimePickerDialog(activity, listener, dueTime!!.hour,dueTime!!.minute, true)
        dialog.setTitle("Время")
        dialog.show()
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = FragmentNewTaskSheetBinding.inflate( inflater,container, false)
        return binding.root
    }

    private fun saveAction() {
        val name = binding.name.text.toString()
        val desc = binding.desc.text.toString()
        val dueTimeString = if(dueTime == null) null else TaskItem.timeFormatter.format(dueTime)

        if(name != ""){
            if(taskItem == null)
            {
                val newTask : TaskItem
                if(dueDate == null){
                    newTask = TaskItem(name,desc,dueTimeString,null, LocalDate.now().toString(),"live",null)
                }
                else{
                    newTask = TaskItem(name,desc,dueTimeString,null, dueDate,"live",null)
                }

                if(gotNot){
                    newTask!!.notificationId = nextInt()
                    scheduleNotification(newTask!!.notificationId!!)
                }

                taskViewModel.addTaskItem(newTask)
            }else
            {
                taskItem!!.name = name
                taskItem!!.desc = desc

                taskItem!!.dueTimeString = dueTimeString

                if(dueDate == null){
                    taskItem!!.dueDate = taskItem!!.dueDate
                }
                else{
                    taskItem!!.dueDate = dueDate
                }

                if(gotNot){
                    taskItem!!.notificationId = nextInt()
                    scheduleNotification(taskItem!!.notificationId!!)
                }

                if(!gotNot && taskItem!!.notificationId != null){
                    cancelScheduledNotification(taskItem!!.notificationId!!)
                    taskItem!!.notificationId = null
                }

                taskViewModel.updateTaskItem(taskItem!!)
            }

            binding.name.setText("")
            binding.desc.setText("")

            dismiss()
        }
    }

}