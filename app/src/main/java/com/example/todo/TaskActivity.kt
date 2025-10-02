package com.example.todo

import android.app.DatePickerDialog
import android.content.Intent
import android.icu.util.Calendar
import android.os.Bundle
import android.widget.Button
import android.widget.RadioGroup
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.textfield.TextInputLayout
import com.google.android.material.textfield.TextInputEditText

class TaskActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_new_task_generator)

        val deadlineInput = findViewById< TextInputEditText>(R.id.editDeadlineDate)
        val deadlineLayout = findViewById<TextInputLayout>(R.id.deadlineLayout)
        val titleInput = findViewById<TextInputEditText>(R.id.editTitle)
        val titleLayout = findViewById<TextInputLayout>(R.id.titleLayout)
        val descriptionInput = findViewById<TextInputEditText>(R.id.editDescription)
        val descriptionLayout = findViewById<TextInputLayout>(R.id.descriptionLayout)
        val backButton = findViewById<Button>(R.id.backButton)
        val saveButton = findViewById<Button>(R.id.saveButton)
        val colorGroup = findViewById<RadioGroup>(R.id.colorPickerGroup)

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

            titleLayout.setBoxBackgroundColor(color)
            deadlineLayout.setBoxBackgroundColor(color)
            descriptionLayout.setBoxBackgroundColor(color)
        }

        // Modal Date Picker
        deadlineLayout.setEndIconOnClickListener {
            val calendar = Calendar.getInstance()
            val year = calendar.get(Calendar.YEAR)
            val month = calendar.get(Calendar.MONTH)
            val day = calendar.get(Calendar.DAY_OF_MONTH)

            //opens with today's date as default
            val datePicker = DatePickerDialog(this, {_, selYear, selMonth, selDay ->
                val selectedDate = "${selYear}/${selMonth + 1}/${selDay}"
                deadlineInput.setText(selectedDate)
            }, year, month, day)
            datePicker.show()
        }
        // The user does a normal click and is navigated back without saving
        backButton.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
            finish()
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

            MainActivity.tasks.add(Task(taskTitle, taskDeadline, taskDescription, false, selectedColorId))
            Toast.makeText(this, "Task saved successfully!", Toast.LENGTH_SHORT).show()

            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
            finish()
        }
        // If the user long clicks, it's ignored and nothing happens
        backButton.setOnLongClickListener { true }
        saveButton.setOnLongClickListener { true }
    }
}