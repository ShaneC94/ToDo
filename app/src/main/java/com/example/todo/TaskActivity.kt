package com.example.todo

import android.app.DatePickerDialog
import android.icu.util.Calendar
import android.os.Bundle
import android.widget.Button
import android.widget.RadioGroup
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout

class TaskActivity : AppCompatActivity() {

    private lateinit var dbHelper: TaskDatabaseHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_new_task_generator)

        dbHelper = TaskDatabaseHelper(this)

        val deadlineInput = findViewById<TextInputEditText>(R.id.editDeadlineDate)
        val deadlineLayout = findViewById<TextInputLayout>(R.id.deadlineLayout)
        val titleInput = findViewById<TextInputEditText>(R.id.editTitle)
        val titleLayout = findViewById<TextInputLayout>(R.id.titleLayout)
        val descriptionInput = findViewById<TextInputEditText>(R.id.editDescription)
        val descriptionLayout = findViewById<TextInputLayout>(R.id.descriptionLayout)
        val backButton = findViewById<Button>(R.id.backButton)
        val saveButton = findViewById<Button>(R.id.saveButton)
        val colorGroup = findViewById<RadioGroup>(R.id.colorPickerGroup)

        colorGroup.setOnCheckedChangeListener { _, checkedId ->
            val selectedColorId = when (checkedId) {
                R.id.colorBlue -> R.color.task_blue
                R.id.colorYellow -> R.color.task_yellow
                R.id.colorPink -> R.color.task_pink
                R.id.colorOrange -> R.color.task_orange
                else -> R.color.meadow_beige
            }
            val color = getColor(selectedColorId)
            titleLayout.setBoxBackgroundColor(color)
            deadlineLayout.setBoxBackgroundColor(color)
            descriptionLayout.setBoxBackgroundColor(color)
        }

        deadlineLayout.setEndIconOnClickListener {
            val calendar = Calendar.getInstance()
            val datePicker = DatePickerDialog(
                this,
                { _, year, month, day ->
                    val selectedDate = "$year/${month + 1}/$day"
                    deadlineInput.setText(selectedDate)
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
            )
            datePicker.show()
        }

        backButton.setOnClickListener { finish() }

        saveButton.setOnClickListener {
            val title = titleInput.text.toString().trim()
            if (title.isEmpty()) {
                titleInput.error = "Title is required"
                Toast.makeText(this, "Title is required!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val deadline = if (deadlineInput.text.toString().trim().isEmpty()) "No deadline" else deadlineInput.text.toString().trim()
            val description = if (descriptionInput.text.toString().trim().isEmpty()) "No description" else descriptionInput.text.toString().trim()
            val selectedColorId = when (colorGroup.checkedRadioButtonId) {
                R.id.colorBlue -> R.color.task_blue
                R.id.colorYellow -> R.color.task_yellow
                R.id.colorPink -> R.color.task_pink
                R.id.colorOrange -> R.color.task_orange
                else -> R.color.meadow_beige
            }

            val task = Task(title, deadline, description, false, selectedColorId)
            val success = dbHelper.insertTask(task)

            if (success) {
                Toast.makeText(this, "Task saved successfully!", Toast.LENGTH_SHORT).show()
                finish()
            } else {
                Toast.makeText(this, "Error saving task.", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
