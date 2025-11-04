package com.example.todo

import android.app.DatePickerDialog
import android.content.Intent
import android.icu.util.Calendar
import android.os.Bundle
import android.widget.Button
import android.widget.RadioGroup
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
class EditTaskActivity: AppCompatActivity() {
    private lateinit var dbHelper: TaskDatabaseHelper
    private var taskId: Long = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_new_task_generator)

        dbHelper = TaskDatabaseHelper(this)//define dbHelper object


        val deadlineInput = findViewById<TextInputEditText>(R.id.editDeadlineDate)
        val deadlineLayout = findViewById<TextInputLayout>(R.id.deadlineLayout)
        val titleInput = findViewById<TextInputEditText>(R.id.editTitle)
        val titleLayout = findViewById<TextInputLayout>(R.id.titleLayout)
        val descriptionInput = findViewById<TextInputEditText>(R.id.editDescription)
        val descriptionLayout = findViewById<TextInputLayout>(R.id.descriptionLayout)
        val backButton = findViewById<Button>(R.id.backButton)
        val saveButton = findViewById<Button>(R.id.saveButton)
        val colorGroup = findViewById<RadioGroup>(R.id.colorPickerGroup)

        //get task id from intent
        taskId = intent.getLongExtra("TASK_ID", -1L)
        if (taskId == -1L) {
            Toast.makeText(this, "Invalid task ID", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        val existingTask = dbHelper.getTaskById(taskId)
        if (existingTask == null) {
            Toast.makeText(this, "Task not found in database", Toast.LENGTH_SHORT).show()
            finish()
            return
        }
        //put all the data in the input boxes
        titleInput.setText(existingTask?.title)
        deadlineInput.setText(if (existingTask?.deadline == "No deadline") "" else existingTask?.deadline)
        descriptionInput.setText(if (existingTask?.description == "No description") "" else existingTask?.description)

        val colorButtonId = when (existingTask?.colorResId) {
            R.color.task_blue -> R.id.colorBlue
            R.color.task_yellow -> R.id.colorYellow
            R.color.task_pink -> R.id.colorPink
            R.color.task_orange -> R.id.colorOrange
            else -> R.id.colorBlue
        }
        colorGroup.check(colorButtonId)



        // Live color change on the input boxes
        colorGroup.setOnCheckedChangeListener { _, checkedId ->
            val selectedColorId = when (checkedId) {
                R.id.colorBlue -> R.color.task_blue
                R.id.colorYellow -> R.color.task_yellow
                R.id.colorPink -> R.color.task_pink
                R.id.colorOrange -> R.color.task_orange
                else -> R.color.meadow_beige
            }

            val color = getColor(selectedColorId)

            titleLayout.boxBackgroundColor = color
            deadlineLayout.boxBackgroundColor = color
            descriptionLayout.boxBackgroundColor = color
        }

        // Modal Date Picker
        deadlineLayout.setEndIconOnClickListener {
            val calendar = Calendar.getInstance()
            val year = calendar.get(Calendar.YEAR)
            val month = calendar.get(Calendar.MONTH)
            val day = calendar.get(Calendar.DAY_OF_MONTH)

            //opens with today's date as default
            val datePicker = DatePickerDialog(this, { _, selYear, selMonth, selDay ->
                val selectedDate = "${selYear}/${selMonth + 1}/${selDay}"
                deadlineInput.setText(selectedDate)
            }, year, month, day)
            datePicker.show()
        }
        // The user does a normal click and is navigated back without saving
        backButton.setOnClickListener {
            finish() // go back to MainActivity without recreating it
        }
        // The user does a normal click and is navigated back with saving
        //and task generation
        saveButton.setOnClickListener {
            val taskTitle = titleInput.text.toString().trim()
            if (taskTitle.isEmpty()) {
                titleInput.error = "A title is required"
                Toast.makeText(this, "Title is required!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            val taskDeadline = if (deadlineInput.text.toString().trim().isEmpty()) {
                "No deadline"
            } else {
                deadlineInput.text.toString().trim()
            }
            val taskDescription = if (descriptionInput.text.toString().trim().isEmpty()) {
                "No description"
            } else {
                descriptionInput.text.toString().trim()
            }

            val selectedColorId = when (colorGroup.checkedRadioButtonId) {
                R.id.colorBlue -> R.color.task_blue
                R.id.colorYellow -> R.color.task_yellow
                R.id.colorPink -> R.color.task_pink
                R.id.colorOrange -> R.color.task_orange
                else -> R.color.meadow_beige
            }


                val updatedTask = Task(
                    id = taskId,
                    title = taskTitle,
                    deadline = taskDeadline,
                    description = taskDescription,
                    colorResId = selectedColorId,
                    isDone = existingTask.isDone
                )
                dbHelper.updateTask(updatedTask) //update the task




            Toast.makeText(this, "Task saved successfully!", Toast.LENGTH_SHORT).show()
            setResult(RESULT_OK, Intent())
            finish() // go back to MainActivity without recreating it
        }
        // If the user long clicks, it's ignored and nothing happens
        backButton.setOnLongClickListener { true }
        saveButton.setOnLongClickListener { true }
    }
}