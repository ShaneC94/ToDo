package com.example.todo

import android.app.DatePickerDialog
import android.content.Intent
import android.icu.util.Calendar
import android.icu.util.TimeZone
import android.os.Bundle
import android.os.PersistableBundle
import android.widget.Button
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.datepicker.MaterialDatePicker
import com.google.android.material.textfield.TextInputLayout
import com.google.android.material.textfield.TextInputEditText
import java.text.SimpleDateFormat
import java.util.Locale

class TaskActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_new_task_generator)

        val deadlineInput = findViewById< TextInputEditText>(R.id.editDeadlineDate)
        val deadlineLayout = findViewById<TextInputLayout>(R.id.deadlineLayout)
        // https://m3.material.io/components/date-pickers/guidelines#d91ce7bc-dbc7-43e3-a802-152f2f9c892a
        // Modal Date Picker
        deadlineLayout.setEndIconOnClickListener {
            val calendar = Calendar.getInstance()
            val year = calendar.get(Calendar.YEAR)
            val month = calendar.get(Calendar.MONTH)
            val day = calendar.get(Calendar.DAY_OF_MONTH)

            val datePicker = DatePickerDialog(this, {_, selYear, selMonth, selDay ->
                val selectedDate = "${selYear}/${selMonth + 1}/${selDay}"
                deadlineInput.setText(selectedDate)
            }, year, month, day)

            datePicker.show()
        }


        //val doneButton = findViewById<Button>(R.id.doneButton)
        val backButton = findViewById<Button>(R.id.backButton)
        val saveButton = findViewById<Button>(R.id.saveButton)
        val titleInput = findViewById<TextInputEditText>(R.id.editTitle)
        // The user does a normal click and is navigated back
//        doneButton.setOnClickListener {
//            val intent = Intent(this, MainActivity::class.java)
//            startActivity(intent)
//            finish()
//        }
        backButton.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
            finish()
        }
        saveButton.setOnClickListener {
            val taskTitle = titleInput.text.toString().trim()
            if (taskTitle.isNotEmpty()) {
                MainActivity.tasks.add(taskTitle)
            }
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
            finish()
        }
        // If the user long clicks, it's ignored and nothing happens
//        doneButton.setOnLongClickListener {
//            true
//        }
        backButton.setOnLongClickListener {
            true
        }
        saveButton.setOnLongClickListener {
            true
        }
    }
}