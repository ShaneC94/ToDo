package com.example.todo

import android.content.Intent
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.EditText
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import kotlin.math.sqrt

class MainActivity : AppCompatActivity(), SensorEventListener {

    private lateinit var adapter: TaskAdapter //initialized later in onCreate
    private lateinit var dbHelper: TaskDatabaseHelper //initialized later in onCreate
    private var allTasks = listOf<Task>() //holds all tasks

    //Sensor variables (Accelerometer for shake detection)
    private lateinit var sensorManager: SensorManager
    private var accelerometer: Sensor? = null
    private var shakeThreshold = 2f //controls sensitivity of shaking
    private var lastAccel = 0f
    private var currentAccel = 0f
    private var accel = 0f
    private var lastShakeTime = 0L


    // light sensor variables
    private var lightSensor: Sensor? = null
    private lateinit var rootLayout: View

    //init, set layout, UI configuration
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)

        dbHelper = TaskDatabaseHelper(this) //define helper here

        // connect root layout for background color changes
        rootLayout = findViewById(R.id.mainScreen)

        // initialize sensor manager
        sensorManager = getSystemService(SENSOR_SERVICE) as SensorManager

        // accelerometer initialization
        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
        accel = 10f
        currentAccel = SensorManager.GRAVITY_EARTH
        lastAccel = SensorManager.GRAVITY_EARTH

        // light sensor initialization
        lightSensor = sensorManager.getDefaultSensor(Sensor.TYPE_LIGHT)

        //connect xml views to kotlin var
        val searchInput = findViewById<EditText>(R.id.searchInput)
        val fab = findViewById<FloatingActionButton>(R.id.taskFab)
        val recyclerView = findViewById<RecyclerView>(R.id.taskRecyclerView)

        //recyclerview setup
        recyclerView.layoutManager = LinearLayoutManager(this)
        adapter = TaskAdapter(emptyList())
        recyclerView.adapter = adapter

        //search functionality
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

        //handles the FAB click
        fab.setOnClickListener {
            val intent = Intent(this, TaskActivity::class.java)
            startActivity(intent)
        }
    }

    override fun onResume() {
        super.onResume()

        //pull from database
        allTasks = dbHelper.getAllTasks()
        adapter.updateList(allTasks)

        //register accelerometer listener
        accelerometer?.also {
            sensorManager.registerListener(this, it, SensorManager.SENSOR_DELAY_GAME)
        }

        // register light sensor listener
        lightSensor?.also {
            sensorManager.registerListener(this, it, SensorManager.SENSOR_DELAY_GAME)
        }
    }

    override fun onPause() {
        super.onPause()
        // unregister accelerometer listener
        sensorManager.unregisterListener(this)
    }

    //detect shake motion to clear search bar
    override fun onSensorChanged(event: SensorEvent?) {
        if (event?.sensor?.type == Sensor.TYPE_ACCELEROMETER) {

            val x = event.values[0]
            val y = event.values[1]
            val z = event.values[2]

            lastAccel = currentAccel
            currentAccel = sqrt((x * x + y * y + z * z).toDouble()).toFloat()
            val delta = currentAccel - lastAccel
            accel = accel * 0.9f + (delta * 3f)

            val now = System.currentTimeMillis()

            if (accel > shakeThreshold && now - lastShakeTime > 800) {
                lastShakeTime = now
                val searchInput = findViewById<EditText>(R.id.searchInput)
                searchInput.setText("")
                adapter.updateList(allTasks)
                toast("Shake detected — search cleared!")
            }
        }


        // change background color based on ambient light
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

    private fun toast(msg: String) {
        android.widget.Toast.makeText(this, msg, android.widget.Toast.LENGTH_SHORT).show()
    }
}
