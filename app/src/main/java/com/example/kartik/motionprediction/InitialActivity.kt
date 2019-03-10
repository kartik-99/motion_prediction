package com.example.kartik.motionprediction

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.hardware.Sensor
import android.hardware.SensorManager
import android.os.Bundle
import android.os.Environment
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast
import kotlinx.android.synthetic.main.activity_initial.*

class InitialActivity : Activity() {

    val status = arrayOf("Sitting", "Laying", "standing", "Walking", "Running", "Going upstairs", "going downstairs")
    var isSdAvailable = false
    var isSdWritable = false
    var errorText :String? = null
    var choice = -1
    private val REQUEST_WRITE_ACCESS = 200
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_initial)

        if(checkSensors()){
            noSensor_textview.visibility = View.GONE
            initial_view.visibility = View.VISIBLE
            setupSpinner()
            checkSdProperties()
            askPermissions()
        }else{
            noSensor_textview.visibility = View.VISIBLE
            initial_view.visibility = View.GONE
            noSensor_textview.text = errorText
        }
    }

    private fun askPermissions() {
        if (ContextCompat.checkSelfPermission(this@InitialActivity, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this@InitialActivity, arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE), REQUEST_WRITE_ACCESS)
        }
    }

    private fun setupSpinner() {
        val arrayAdapter = ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, status)
        activity_spinner.adapter = arrayAdapter
        activity_spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View, position: Int, id: Long) {
                choice = position
            }

            override fun onNothingSelected(parent: AdapterView<*>) {

            }
        }
    }

    private fun checkSdProperties() {
        val state = Environment.getExternalStorageState()
        if (Environment.MEDIA_MOUNTED == state) {
            isSdAvailable = true
            isSdWritable = true
        } else if (Environment.MEDIA_MOUNTED_READ_ONLY == state) {
            isSdAvailable = true
            isSdWritable = false
        } else {
            isSdAvailable = false
            isSdWritable = false
        }
    }

    fun startData(view:View){
        if(isSdAvailable && isSdWritable){
            val name = name_editText.text.toString()
            if (!isAlpha(name)) {
                Toast.makeText(this@InitialActivity, "Name can only have alphabets", Toast.LENGTH_SHORT).show()
                return
            }
            if (choice == -1) {
                Toast.makeText(this@InitialActivity, "Please select present activity", Toast.LENGTH_SHORT).show()
                return
            }

            val intent = Intent(this, MainActivity::class.java)
            intent.putExtra("name", name)
            intent.putExtra("status", Integer.toString(choice))
            Toast.makeText(applicationContext, name, Toast.LENGTH_SHORT).show()
            startActivity(intent)
        }
        else{
            if(!isSdAvailable)
                Toast.makeText(this, "No storage location available", Toast.LENGTH_SHORT).show()
            else
                Toast.makeText(this, "Storage location is not writable", Toast.LENGTH_SHORT).show()
        }
    }

    fun isAlpha(name: String): Boolean {
        return name.matches("[a-zA-Z]+".toRegex())
    }

    private fun checkSensors(): Boolean {
        val sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager
        if (sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER) == null && sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE) == null)
            errorText = "No accelerometer and gyroscope on this device"
        else if (sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER) == null && sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE) != null)
            errorText = "No accelerometer and gyroscope on this device"
        else if (sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER) != null && sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE) == null)
            errorText = "No accelerometer and gyroscope on this device"
        else
            return true
        return false
    }

}
