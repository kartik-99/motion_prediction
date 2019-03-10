package com.example.kartik.motionprediction

import android.app.Service
import android.content.Context
import android.content.Intent
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.*
import java.io.File
import java.io.FileNotFoundException
import java.io.FileOutputStream
import java.io.IOException
import android.app.Activity
import android.app.NotificationManager
import android.support.v4.app.NotificationCompat
import android.util.Log


class SensorService: Service() {

    //status variables
    var status :Int = -1
    lateinit var username : String

    //file variables
    var bigRecordNumber = 0
    var recordNumber = 0
    val SAMPLING_PERIOD = 3333
    var data = arrayOf(0f,0f,0f,0f,0f,0f,0f,0f,0f)
    lateinit var dataString :String
    var dataHandler = Handler()
    lateinit var dataRunnable : Runnable
    var DELAY : Long = 20
    var INDIVIDUAL_RECORD_SIZE = 64

    //Service variables
    lateinit var activity: Callbacks
    private var myBinder = MyLocalBinder()
    var updateUIHandler = Handler()
    lateinit var serviceRunnable: Runnable
    //val t = Timer()

    //Sensor variables
    lateinit var sensorManager : SensorManager
    lateinit var accelerometerListener :SensorEventListener
    lateinit var gyroscopeListener : SensorEventListener
    lateinit var linearAccelerometerListener : SensorEventListener

    //notification vars
    lateinit var notificationManager: NotificationManager
    lateinit var notification: NotificationCompat.Builder
    var NOTIFICATION_ID = 1
    var CHANNEL_ID = "channel"


    var CUSTOM_LOG = "Custom_Log"

    override fun onBind(intent: Intent?): IBinder {
        Log.d(CUSTOM_LOG, "Onbind")
        return myBinder
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {

        status = intent?.getStringExtra("status")?.toInt()!!
        username = intent?.getStringExtra("name")
        bigRecordNumber = intent?.getStringExtra("bigRecordNumber").toInt()

        initialize()
        setupSensorManager()
        startDataRecording()
        showNotification()
        Log.d(CUSTOM_LOG, "OnStartCommand")
        return super.onStartCommand(intent, flags, startId)
    }

    override fun onRebind(intent: Intent?) {
        setupSensorManager()
        Log.d(CUSTOM_LOG, "OnRebind")
        showNotification()
        super.onRebind(intent)
    }

    override fun onUnbind(intent: Intent?): Boolean {

        //unregister the listeners
        sensorManager.unregisterListener(accelerometerListener)
        sensorManager.unregisterListener(gyroscopeListener)
        sensorManager.unregisterListener(linearAccelerometerListener)

        //stop the timer task
        stopDataRecording()
        notificationManager.cancel(NOTIFICATION_ID)

        Log.d(CUSTOM_LOG, "Unbind")
        return super.onUnbind(intent)
    }

    fun initialize(){

        serviceRunnable = object : Runnable {
            override fun run() {
                activity?.updateClient(bigRecordNumber, status)
                updateUIHandler.postDelayed(this, 1000)
            }
        }

        accelerometerListener = object :SensorEventListener{
            override fun onSensorChanged(event: SensorEvent?) {
                data[0] = event!!.values[0]
                data[1] = event.values[1]
                data[2] = event.values[2]
            }

            override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
        }

        gyroscopeListener = object :SensorEventListener{
            override fun onSensorChanged(event: SensorEvent?) {
                data[3] = event!!.values[0]
                data[4] = event.values[1]
                data[5] = event.values[2]
            }

            override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
        }

        linearAccelerometerListener = object :SensorEventListener{
            override fun onSensorChanged(event: SensorEvent?) {
                data[6] = event!!.values[0]
                data[7] = event.values[1]
                data[8] = event.values[2]
            }
            override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
        }

        notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notification = NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle("Motion : Data")
                .setContentText("Sensor Service Running")
                .setSmallIcon(R.drawable.notification_icon_background)
                .setOngoing(true)

    }

    private fun setupSensorManager() {
        sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager
        sensorManager.registerListener(accelerometerListener, sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER), SAMPLING_PERIOD)
        sensorManager.registerListener(gyroscopeListener, sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE), SAMPLING_PERIOD)
        sensorManager.registerListener(linearAccelerometerListener, sensorManager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION), SAMPLING_PERIOD)
    }

    private fun startDataRecording() {
        Log.d(CUSTOM_LOG, "Started Recording")
        dataHandler.postDelayed(object :Runnable{
            override fun run() {
                recordData()
                dataRunnable = this
                dataHandler.postDelayed(dataRunnable, DELAY)
            }
        }, DELAY)

    }

    fun recordData(){
        if(recordNumber == 0)
            dataString = Integer.toString(bigRecordNumber) + " " + Integer.toString(status) + "\n"

        for (i in 0..8)
            dataString = dataString + java.lang.Float.toString(data[i])+" "

        dataString += "\n"
        recordNumber++

        if(recordNumber == INDIVIDUAL_RECORD_SIZE){
            writeIntoFile(dataString)
            bigRecordNumber++
            recordNumber = 0
            updateUIHandler.postDelayed(serviceRunnable, 0)
        }

    }

    private fun stopDataRecording(){
        Log.d(CUSTOM_LOG, "Stopped Recording")
        dataHandler.removeCallbacks(dataRunnable)
        updateUIHandler.removeCallbacks(serviceRunnable)
    }

    fun showNotification(){
        notificationManager.notify(NOTIFICATION_ID, notification.build())
    }

    private fun writeIntoFile(newData: String) {
        val path = Environment.getExternalStorageDirectory()
        val filename = username + "_data.txt"
        path.mkdirs()
        val file = File(path, filename)
        try {
            val fos = FileOutputStream(file, true)
            fos.write(newData.toByteArray())
            fos.close()
        } catch (e: FileNotFoundException) {
            e.printStackTrace()
        } catch (e: IOException) {
            e.printStackTrace()
        }

    }

    fun updateStatus(status: Int){
        this.status = status
    }

    fun registerClient(activity: Activity) {
        this.activity = activity as Callbacks
    }

    interface Callbacks {
        fun updateClient(bigRecordNumber:Int, status:Int)
    }



    inner class MyLocalBinder : Binder(){
        fun getService() : SensorService{
            return this@SensorService
        }
    }
}