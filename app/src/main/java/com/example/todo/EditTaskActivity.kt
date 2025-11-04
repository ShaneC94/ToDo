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
import android.Manifest
import android.content.ContentValues
import android.graphics.Bitmap
import android.graphics.Matrix
import android.net.Uri
import android.provider.MediaStore
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import java.io.File
import java.io.FileOutputStream
import android.widget.ImageView
import android.view.View
import android.content.pm.PackageManager




class EditTaskActivity: AppCompatActivity() {
    private lateinit var dbHelper: TaskDatabaseHelper
    private var taskId: Long = -1
    // Image handling
    private var imageUri: String? = null
    private lateinit var imageView: ImageView

    private lateinit var cameraButton: Button
    private lateinit var galleryButton: Button
    private lateinit var removeImageButton: Button


    private val takePicture = registerForActivityResult(ActivityResultContracts.TakePicturePreview()) { bitmap: Bitmap? ->
        bitmap?.let {
            val scaledBitmap = scaleCenterCrop(it, 800, 800)
            val resolver = applicationContext.contentResolver
            val contentValues = ContentValues().apply {
                put(MediaStore.MediaColumns.DISPLAY_NAME, "task_image_${System.currentTimeMillis()}.jpg")
                put(MediaStore.MediaColumns.MIME_TYPE, "image/jpeg")
                put(MediaStore.MediaColumns.RELATIVE_PATH, "Pictures/ToDoApp")
            }
            val uriObj = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)
            uriObj?.let { uri ->
                resolver.openOutputStream(uri)?.use { outputStream ->
                    scaledBitmap.compress(Bitmap.CompressFormat.JPEG, 95, outputStream)
                }
                imageUri = uri.toString()
                displayImageFromUriString(imageUri)
            }
        }
    }

    private val pickImage = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        uri?.let {
            try {
                val inputStream = contentResolver.openInputStream(it)
                val fileName = "task_image_${System.currentTimeMillis()}.jpg"
                val file = File(filesDir, fileName)
                val outputStream = FileOutputStream(file)
                inputStream?.copyTo(outputStream)
                inputStream?.close()
                outputStream.close()

                imageUri = file.absolutePath
                displayImageFromUriString(imageUri)
            } catch (e: Exception) {
                e.printStackTrace()
                Toast.makeText(this, "Failed to load image", Toast.LENGTH_SHORT).show()
            }
        }
    }



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

        // 🖼️ Initialize image & button views first
        imageView = findViewById(R.id.taskImageView)
        cameraButton = findViewById(R.id.cameraButton)
        galleryButton = findViewById(R.id.galleryButton)
        removeImageButton = findViewById(R.id.removeImageButton)
        imageView.visibility = View.GONE

// ✅ Now the listeners
        cameraButton.setOnClickListener {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                == PackageManager.PERMISSION_GRANTED) {
                takePicture.launch(null)
            } else {
                ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.CAMERA), 100)
            }
        }

        galleryButton.setOnClickListener {
            pickImage.launch("image/*")
        }

        removeImageButton.setOnClickListener {
            if (imageUri != null) {
                imageUri = null
                imageView.setImageDrawable(null)
                imageView.visibility = View.GONE
                Toast.makeText(this, "Image removed", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "No image to remove", Toast.LENGTH_SHORT).show()
            }
        }



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

        imageUri = existingTask.imageUri
        if (!imageUri.isNullOrEmpty()) {
            displayImageFromUriString(imageUri)
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

        val initialColor = getColor(existingTask.colorResId)
        titleLayout.boxBackgroundColor = initialColor
        deadlineLayout.boxBackgroundColor = initialColor
        descriptionLayout.boxBackgroundColor = initialColor

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
                isDone = existingTask.isDone,
                imageUri = imageUri
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
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 100) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                takePicture.launch(null)
            } else {
                Toast.makeText(this, "Camera permission is required to take photos", Toast.LENGTH_SHORT).show()
            }
        }
    }
    private fun displayImageFromUriString(uriString: String?) {
        if (uriString.isNullOrEmpty()) {
            imageView.setImageDrawable(null)
            imageView.visibility = View.GONE
            return
        }
        try {
            val toShow: Uri = when {
                uriString.startsWith("content://") || uriString.startsWith("file://") || uriString.startsWith("http") -> {
                    Uri.parse(uriString)
                }
                else -> {
                    Uri.fromFile(File(uriString))
                }
            }
            imageView.setImageURI(toShow)
            imageView.visibility = View.VISIBLE
        } catch (e: Exception) {
            e.printStackTrace()
            imageView.setImageDrawable(null)
            imageView.visibility = View.GONE
        }
    }
    private fun scaleCenterCrop(source: Bitmap, newWidth: Int, newHeight: Int): Bitmap {
        val scale: Float
        val dx: Float
        val dy: Float
        val width = source.width.toFloat()
        val height = source.height.toFloat()

        if (width * newHeight > newWidth * height) {
            scale = newHeight / height
            dx = (newWidth - width * scale) * 0.5f
            dy = 0f
        } else {
            scale = newWidth / width
            dx = 0f
            dy = (newHeight - height * scale) * 0.5f
        }

        val matrix = Matrix()
        matrix.setScale(scale, scale)
        matrix.postTranslate(dx, dy)

        val result = Bitmap.createBitmap(newWidth, newHeight, Bitmap.Config.ARGB_8888)
        val canvas = android.graphics.Canvas(result)
        canvas.drawBitmap(source, matrix, null)
        return result
    }



}