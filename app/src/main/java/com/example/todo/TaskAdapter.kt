package com.example.todo

import android.content.Intent
import android.net.Uri
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import java.io.File
import androidx.core.net.toUri

class TaskAdapter(
    private var taskList: List<Task>
) : RecyclerView.Adapter<TaskAdapter.TaskViewHolder>() {

    private lateinit var dbHelper: TaskDatabaseHelper


    //tracks which item is expanded - allows for both to expand individually
    //null if none are expanded
    private var expandedTitles = mutableSetOf<Int>()
    private var expandedDescriptions = mutableSetOf<Int>()


    inner class TaskViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val checkBox: CheckBox = itemView.findViewById(R.id.taskCheckBox)
        val title: TextView = itemView.findViewById(R.id.taskTitle)
        val deadline: TextView = itemView.findViewById(R.id.taskDeadline)
        val description: TextView = itemView.findViewById(R.id.taskDescription)
        val deleteButton: ImageButton = itemView.findViewById(R.id.imageButton)
        val editButton: ImageButton = itemView.findViewById(R.id.editbutton)
        val imagePreview: ImageView = itemView.findViewById(R.id.taskImagePreview)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TaskViewHolder {
        //initialize dbHelper
        if (!::dbHelper.isInitialized) {
            dbHelper = TaskDatabaseHelper(parent.context)
        }

        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_task, parent, false)
        return TaskViewHolder(view)
    }

    override fun onBindViewHolder(holder: TaskViewHolder, position: Int) {
        val task = taskList[position]

        holder.title.text = task.title
        holder.deadline.text = "Deadline: ${task.deadline}"
        holder.description.text = task.description
        holder.checkBox.isChecked = task.isDone
        holder.checkBox.setOnCheckedChangeListener(null) //clear previous listener

        // Dynamically show/hide image preview
        if (!task.imageUri.isNullOrEmpty()) {
            try {
                val uri = task.imageUri.toUri()
                holder.imagePreview.setImageURI(uri)
                holder.imagePreview.visibility = View.VISIBLE
            } catch (e: Exception) {
                e.printStackTrace()
                holder.imagePreview.setImageDrawable(null)
                holder.imagePreview.visibility = View.GONE
            }
        } else {
            holder.imagePreview.setImageDrawable(null)
            holder.imagePreview.visibility = View.GONE
        }

        holder.checkBox.setOnCheckedChangeListener { _, isChecked ->
            task.isDone = isChecked
            //update database when checkbox state changes
            dbHelper.updateTask(task)
        }

        holder.deleteButton.setOnClickListener {
            dbHelper.deleteTask(task.id)//delete the task
            val newList = dbHelper.getAllTasks()//create new list
            updateList(newList)//update the list

            //show toast confirmation
            Toast.makeText(
            holder.itemView.context,
            "Task deleted!",
            Toast.LENGTH_SHORT
            ).show()
        }

        holder.editButton.setOnClickListener {
            val context = holder.itemView.context
            val intent = Intent(context, EditTaskActivity::class.java)
            intent.putExtra("TASK_ID", task.id) // "TASK_ID" must match the key in EditTaskActivity
            context.startActivity(intent)
        }


        //Apply background color
        (holder.itemView as androidx.cardview.widget.CardView).setCardBackgroundColor(
            holder.itemView.context.getColor(task.colorResId)
        )
        //expand/collapse title
        if (expandedTitles.contains(position)) {
            holder.title.maxLines = Int.MAX_VALUE
            holder.title.ellipsize = null
        } else {
            holder.title.maxLines = 2
            holder.title.ellipsize = TextUtils.TruncateAt.END
        }

        //expand/collapse description
        if (expandedDescriptions.contains(position)) {
            holder.description.maxLines = Int.MAX_VALUE
            holder.description.ellipsize = null
        } else {
            holder.description.maxLines = 2
            holder.description.ellipsize = TextUtils.TruncateAt.END
        }

        //toggle title click listener
        holder.title.setOnClickListener {
            if (expandedTitles.contains(position)) {
                expandedTitles.remove(position)
            } else {
                expandedTitles.add(position)
            }
            notifyItemChanged(position)
        }

        //toggle description click listener
        holder.description.setOnClickListener {
            if (expandedDescriptions.contains(position)) {
                expandedDescriptions.remove(position)
            } else {
                expandedDescriptions.add(position)
            }
            notifyItemChanged(position)
        }


    }

    //tells recyclerview how many tasks exist
    override fun getItemCount(): Int = taskList.size

    //refreshes adapter when the list changes
    fun updateList(newList: List<Task>) {
        taskList = newList
        expandedTitles.clear()
        expandedDescriptions.clear()
        notifyDataSetChanged()
    }


}