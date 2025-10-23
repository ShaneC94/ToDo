package com.example.todo

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.widget.EditText
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton

class MainActivity : AppCompatActivity() {

    private lateinit var adapter: TaskAdapter
    private lateinit var dbHelper: TaskDatabaseHelper
    private var allTasks = mutableListOf<Task>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)

        dbHelper = TaskDatabaseHelper(this)
        allTasks = dbHelper.getAllTasks()

        val searchInput = findViewById<EditText>(R.id.searchInput)
        val fab = findViewById<FloatingActionButton>(R.id.taskFab)
        val recyclerView = findViewById<RecyclerView>(R.id.taskRecyclerView)

        adapter = TaskAdapter(allTasks, dbHelper)
        recyclerView.adapter = adapter
        recyclerView.layoutManager = LinearLayoutManager(this)

        searchInput.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val query = s.toString().trim()
                val filtered = if (query.isEmpty()) {
                    allTasks
                } else {
                    allTasks.filter { it.title.contains(query, ignoreCase = true) }
                }
                adapter.updateList(filtered)
            }

            override fun afterTextChanged(s: Editable?) {}
        })

        fab.setOnClickListener {
            val intent = Intent(this, TaskActivity::class.java)
            startActivity(intent)
        }
    }

    override fun onResume() {
        super.onResume()
        allTasks = dbHelper.getAllTasks()
        adapter.updateList(allTasks)
    }
}
