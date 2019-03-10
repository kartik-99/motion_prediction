package com.example.kartik.motionprediction

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.graphics.Color
import android.os.*
import android.support.v7.app.AppCompatActivity
import android.view.View
import android.widget.Toast
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity(), SensorService.Callbacks{


    lateinit var username :String
    var status :Int = 0
    var bigRecordNumber = 0
    var connectionStatus = false
    var serviceStatus = false
    var doubleBackToExitPressedOnce = false

    lateinit var serviceIntent :Intent
    lateinit var recievedIntent: Intent

    lateinit var sensorService : SensorService
    lateinit var serviceConnection : ServiceConnection

    var STATUS_SITTING = 0
    var STATUS_LAYING = 1
    var STATUS_STANDING = 2
    var STATUS_WALKING = 3
    var STATUS_RUNNING = 4
    var STATUS_UPSTAIRS = 5
    var STATUS_DOWNSTAIRS = 6


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        //initializing variables
        recievedIntent = getIntent()
        initialize()

        //setting up ui
        setContentView(R.layout.activity_main)
        title = "$username : Data"
        updateButtonUI()
        updateControlButtonUI()
        disableButtons()
    }

    override fun onBackPressed() {
        if (doubleBackToExitPressedOnce) {
            finish()
        }

        this.doubleBackToExitPressedOnce = true
        Toast.makeText(this, "Press back again to exit", Toast.LENGTH_SHORT).show()

        Handler().postDelayed({ doubleBackToExitPressedOnce = false }, 2000)
    }

    private fun initialize() {

        username = recievedIntent.getStringExtra("name")
        status = recievedIntent.getStringExtra("status").toInt()

        sensorService = SensorService()

        serviceConnection = object : ServiceConnection{
            override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
                val binder = service as SensorService.MyLocalBinder
                sensorService = binder.getService()
                sensorService.registerClient(this@MainActivity)
                connectionStatus = true
                statusTextView.text = "Connected to Service"
                statusTextView.setTextColor(Color.GREEN)
            }

            override fun onServiceDisconnected(name: ComponentName?) {
                connectionStatus = false
                statusTextView.text = "Disonnected to Service"
                statusTextView.setTextColor(Color.RED)
            }

        }

        initializeSensorService()

    }

    fun initializeSensorService(){
        serviceIntent = Intent(this@MainActivity, SensorService::class.java)
        serviceIntent.putExtra("name", username)
        serviceIntent.putExtra("status", status.toString())
        serviceIntent.putExtra("bigRecordNumber", bigRecordNumber.toString())
    }

    private fun updateControlButtonUI() {
        if(serviceStatus){
            controlButton.text = "Stop service"
            controlButton.setBackgroundColor(Color.RED)
        }else{
            controlButton.text = "Start service"
            controlButton.setBackgroundColor(Color.GREEN)
        }
    }

    private fun updateButtonUI(){
        standingButton.setBackgroundColor(Color.RED)
        sittingButton.setBackgroundColor(Color.RED)
        layingButton.setBackgroundColor(Color.RED)
        walkingButton.setBackgroundColor(Color.RED)
        runningButton.setBackgroundColor(Color.RED)
        upstairsButton.setBackgroundColor(Color.RED)
        downstairsButton.setBackgroundColor(Color.RED)

        when(status){
            STATUS_SITTING -> sittingButton.setBackgroundColor(Color.GREEN)
            STATUS_LAYING -> layingButton.setBackgroundColor(Color.GREEN)
            STATUS_STANDING -> standingButton.setBackgroundColor(Color.GREEN)
            STATUS_WALKING -> walkingButton.setBackgroundColor(Color.GREEN)
            STATUS_RUNNING -> runningButton.setBackgroundColor(Color.GREEN)
            STATUS_UPSTAIRS -> upstairsButton.setBackgroundColor(Color.GREEN)
            STATUS_DOWNSTAIRS -> downstairsButton.setBackgroundColor(Color.GREEN)
        }
    }

    fun disableButtons(){
        standingButton.isEnabled = false
        sittingButton.isEnabled = false
        layingButton.isEnabled = false
        walkingButton.isEnabled = false
        runningButton.isEnabled = false
        upstairsButton.isEnabled = false
        downstairsButton.isEnabled = false
    }

    fun enableButtons(){
        standingButton.isEnabled = true
        sittingButton.isEnabled = true
        layingButton.isEnabled = true
        walkingButton.isEnabled = true
        runningButton.isEnabled = true
        upstairsButton.isEnabled = true
        downstairsButton.isEnabled = true
    }

    private fun startSensorService() {
        initializeSensorService()
        startService(serviceIntent)
        bindService(serviceIntent, serviceConnection, Context.BIND_AUTO_CREATE)
        serviceStatus = true
    }

    private fun stopSensorService(){
        unbindService(serviceConnection)
        stopService(serviceIntent)
        serviceStatus = false
    }

    override fun updateClient(bigRecordNumber:Int, status:Int) {
        this.bigRecordNumber =  bigRecordNumber

        bigRecordTextView.text = this.bigRecordNumber.toString()
        when(status){
            STATUS_SITTING -> latestRecordTextView.text = "Sitting"
            STATUS_STANDING -> latestRecordTextView.text = "Standing"
            STATUS_LAYING -> latestRecordTextView.text = "Laying"
            STATUS_WALKING -> latestRecordTextView.text = "Walking"
            STATUS_RUNNING -> latestRecordTextView.text = "Running"
            STATUS_UPSTAIRS -> latestRecordTextView.text = "Upstairs"
            STATUS_DOWNSTAIRS-> latestRecordTextView.text = "Downstairs"
        }
    }

    //button functions
    fun connectionButton(view:View){
        if(serviceStatus){
            stopSensorService()
            disableButtons()
        }
        else{
            startSensorService()
            enableButtons()
        }
        updateControlButtonUI()
    }



    fun sitting(view: View){
        status = STATUS_SITTING
        sensorService.updateStatus(status)
        updateButtonUI()
    }
    fun standing(view: View){
        status = STATUS_STANDING
        sensorService.updateStatus(status)
        updateButtonUI()
    }
    fun laying(view: View){
        status = STATUS_LAYING
        sensorService.updateStatus(status)
        updateButtonUI()
    }
    fun walking(view: View){
        status = STATUS_WALKING
        sensorService.updateStatus(status)
        updateButtonUI()
    }
    fun running(view: View){
        status = STATUS_RUNNING
        sensorService.updateStatus(status)
        updateButtonUI()
    }
    fun upstairs(view: View){
        status = STATUS_UPSTAIRS
        sensorService.updateStatus(status)
        updateButtonUI()
    }
    fun downstairs(view: View){
        status = STATUS_DOWNSTAIRS
        sensorService.updateStatus(status)
        updateButtonUI()
    }

}
