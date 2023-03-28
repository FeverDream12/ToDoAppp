package com.example.todoapp

import android.content.Intent
import androidx.fragment.app.Fragment
import android.os.Bundle
import android.view.MenuItem
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.ActionBarDrawerToggle
import com.google.android.material.bottomnavigation.BottomNavigationView
import androidx.appcompat.app.AppCompatActivity
import androidx.drawerlayout.widget.DrawerLayout
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.todoapp.ui.notes.NoteItem.NoteItemAdapter
import com.example.todoapp.ui.notes.NoteItem.NoteItemClickListener
import com.example.todoapp.ui.home.TaskItem.TaskItemAdapter
import com.example.todoapp.ui.home.TaskItem.TaskItemClickListener
import com.example.todoapp.ui.notes.NoteItem.NoteItem
import com.example.todoapp.ui.notes.NoteItem.NoteItemModelFactory
import com.example.todoapp.ui.notes.NoteItem.NoteViewModel
import com.example.todoapp.ui.home.TaskItem.TaskItem
import com.example.todoapp.databinding.ActivityMainBinding
import com.example.todoapp.ui.home.TaskItem.TaskItemModelFactory
import com.example.todoapp.ui.home.TaskItem.TaskViewModel
import com.google.android.material.navigation.NavigationView

class MainActivity : AppCompatActivity(), TaskItemClickListener, NoteItemClickListener {

    private lateinit var toggle: ActionBarDrawerToggle
    private lateinit var binding: ActivityMainBinding
    private val taskViewModel: TaskViewModel by viewModels {
        TaskItemModelFactory((application as TodoApplication).repository)
    }
    private val noteViewModel: NoteViewModel by viewModels {
        NoteItemModelFactory((application as TodoApplication).notesRepository)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val navView: BottomNavigationView = binding.navView

        val navController = findNavController(R.id.nav_host_fragment_activity_main)
        val appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.navigation_home, R.id.navigation_dashboard, R.id.navigation_notifications
            )
        )

        setupActionBarWithNavController(navController, appBarConfiguration)
        navView.setupWithNavController(navController)


        binding.sideNavMenu.setNavigationItemSelectedListener {
            when(it.itemId){
                R.id.nav_stat -> {
                    val int = Intent(this,StatisticActivity::class.java)
                    startActivity(int)
                }
                R.id.fav_tasks -> Toast.makeText(applicationContext, "Tasks", Toast.LENGTH_SHORT).show()
                R.id.fav_notes -> Toast.makeText(applicationContext, "Notes", Toast.LENGTH_SHORT).show()

            }
            true
        }

        setRecycleView()
        setNotesRecycleView()
    }

    private fun setRecycleView() {
        val activity = this
        taskViewModel.taskItems.observe(this){
            binding.listRecycleView.apply {
                layoutManager = LinearLayoutManager(applicationContext)
                adapter = TaskItemAdapter(it, activity)
            }
        }
    }

    private fun setNotesRecycleView() {
        val activity = this

        noteViewModel.noteItems.observe(this){
            binding.notesRecycleView.apply {
                layoutManager = LinearLayoutManager(applicationContext)
                adapter = NoteItemAdapter(it, activity)
            }
        }

    }

    override fun editTaskItem(taskItem: TaskItem) {
        NewTaskSheet(taskItem).show(supportFragmentManager, "editTaskTag")
    }

    override fun completeTaskItem(taskItem: TaskItem) {
        taskViewModel.setTaskCompleted(taskItem)
    }

    override fun deleteTaskItem(taskItem: TaskItem) {
        taskViewModel.deleteTaskItem(taskItem)
    }

    override fun editNoteItem(noteItem: NoteItem) {
        NewNoteSheet(noteItem).show(supportFragmentManager, "editTaskTag")
    }

    override fun deleteNoteItem(noteItem: NoteItem) {
        TODO("Not yet implemented")
    }

    private fun setFragment(fragment: Fragment, title: String){
        val fragmentManager = supportFragmentManager
        val fragmentTransaction = fragmentManager.beginTransaction()
        fragmentTransaction.replace(R.id.frameLay,fragment)
        fragmentTransaction.commit()
        binding.drawer.closeDrawers()
    }


}