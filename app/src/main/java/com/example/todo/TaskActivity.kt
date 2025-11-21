package com.example.todo

import android.app.DatePickerDialog
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
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
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Matrix
import android.net.Uri
import android.provider.MediaStore
import android.view.View
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import android.widget.ImageView
import java.io.File
import java.io.FileOutputStream
import androidx.core.graphics.createBitmap

class TaskActivity : AppCompatActivity(), SensorEventListener {

    private lateinit var dbHelper: TaskDatabaseHelper
    private lateinit var imageView: ImageView
    private var imageUri: String? = null

    //Light Sensor variables
    private lateinit var sensorManager: SensorManager
    private var lightSensor: Sensor? = null
    private lateinit var rootLayout: View

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_new_task_generator)

        dbHelper = TaskDatabaseHelper(this) // define dbHelper object

        // Light sensor setup
        sensorManager = getSystemService(SENSOR_SERVICE) as SensorManager
        lightSensor = sensorManager.getDefaultSensor(Sensor.TYPE_LIGHT)

        rootLayout = findViewById(R.id.taskScreen)

        val deadlineInput = findViewById<TextInputEditText>(R.id.editDeadlineDate)
        val deadlineLayout = findViewById<TextInputLayout>(R.id.deadlineLayout)
        val titleInput = findViewById<TextInputEditText>(R.id.editTitle)
        val titleLayout = findViewById<TextInputLayout>(R.id.titleLayout)
        val descriptionInput = findViewById<TextInputEditText>(R.id.editDescription)
        val descriptionLayout = findViewById<TextInputLayout>(R.id.descriptionLayout)
        val backButton = findViewById<Button>(R.id.backButton)
        val saveButton = findViewById<Button>(R.id.saveButton)
        val colorGroup = findViewById<RadioGroup>(R.id.colorPickerGroup)

        // Image-related views
        imageView = findViewById(R.id.taskImageView)
        val cameraButton = findViewById<Button>(R.id.cameraButton)
        val galleryButton = findViewById<Button>(R.id.galleryButton)
        val removeImageButton = findViewById<Button>(R.id.removeImageButton)
        imageView.visibility = View.GONE

        // Camera and Gallery Logic
        val takePicture = registerForActivityResult(ActivityResultContracts.TakePicturePreview()) { bitmap ->
            bitmap?.let {
                val scaledBitmap = scaleCenterCrop(it, 800, 800)
                val resolver = applicationContext.contentResolver
                val contentValues = ContentValues().apply {
                    put(MediaStore.MediaColumns.DISPLAY_NAME, "task_image_${System.currentTimeMillis()}.jpg")
                    put(MediaStore.MediaColumns.MIME_TYPE, "image/jpeg")
                    put(MediaStore.MediaColumns.RELATIVE_PATH, "Pictures/ToDoApp")
                }
                val imageUriObj = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)
                imageUriObj?.let { uri ->
                    resolver.openOutputStream(uri)?.use { outputStream ->
                        scaledBitmap.compress(Bitmap.CompressFormat.JPEG, 95, outputStream)
                    }
                    imageUri = uri.toString()
                    imageView.setImageBitmap(scaledBitmap)
                    imageView.visibility = View.VISIBLE
                }
            }
        }

        val pickImage = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
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
                    imageView.setImageURI(Uri.fromFile(file))
                    imageView.visibility = View.VISIBLE
                } catch (e: Exception) {
                    e.printStackTrace()
                    Toast.makeText(this, "Failed to load image", Toast.LENGTH_SHORT).show()
                }
            }
        }

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

            // opens with today's date as default
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
        saveButton.setOnClickListener {
            val taskTitle = titleInput.text.toString().trim()
            if (taskTitle.isEmpty()) {
                titleInput.error = "A title is required"
                Toast.makeText(this, "Title is required!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            val taskDeadline = deadlineInput.text.toString().trim().ifEmpty {
                "No deadline"
            }
            val taskDescription = descriptionInput.text.toString().trim().ifEmpty {
                "No description"
            }

            val selectedColorId = when (colorGroup.checkedRadioButtonId) {
                R.id.colorBlue -> R.color.task_blue
                R.id.colorYellow -> R.color.task_yellow
                R.id.colorPink -> R.color.task_pink
                R.id.colorOrange -> R.color.task_orange
                else -> R.color.meadow_beige
            }

            if (taskTitle.isNotEmpty()) {
                val newTask = Task(
                    title = taskTitle,
                    deadline = taskDeadline,
                    description = taskDescription,
                    colorResId = selectedColorId,
                    imageUri = imageUri // store image path
                )
                dbHelper.addTask(newTask)
            }

            Toast.makeText(this, "Task saved successfully!", Toast.LENGTH_SHORT).show()
            finish() // go back to MainActivity without recreating it
        }

        // If the user long clicks, it's ignored and nothing happens
        backButton.setOnLongClickListener { true }
        saveButton.setOnLongClickListener { true }
    }

    override fun onResume() {
        super.onResume()

        // register light listener
        lightSensor?.also {
            sensorManager.registerListener(this, it, SensorManager.SENSOR_DELAY_NORMAL)
        }
    }

    override fun onPause() {
        super.onPause()
        // unregister sensor
        sensorManager.unregisterListener(this)
    }

    override fun onSensorChanged(event: SensorEvent?) {
        if (event?.sensor?.type == Sensor.TYPE_LIGHT) {
            val lux = event.values[0]

            val colorRes = when {
                lux < 100 -> R.color.meadow_dark_olive
                lux in 100.0..2000.0 -> R.color.meadow_dim_green
                lux in 2001.0..7500.0 -> R.color.meadow_soft_gold
                lux in 7501.0..10000.0 -> R.color.meadow_sky_blue
                else -> R.color.meadow_light_bg
            }

            rootLayout.setBackgroundColor(getColor(colorRes))
        }
    }


    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}

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

        val result = createBitmap(newWidth, newHeight)
        val canvas = android.graphics.Canvas(result)
        canvas.drawBitmap(source, matrix, null)
        return result
    }
}
