package com.example.plusplusbattery

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.BatteryManager
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {

    private lateinit var tvBatteryLevel: TextView
    private lateinit var tvStatus: TextView
    private lateinit var tvTemperature: TextView
    private lateinit var tvHealth: TextView
    private lateinit var tvVoltage: TextView
    private lateinit var tvCycleCount: TextView
    private lateinit var tvChargeCounter: TextView
    private lateinit var tvBatteryPercentage: TextView
    private lateinit var tvBatteryCurrent: TextView

    // create a handler and a runnable to update battery info periodically
    private val handler = Handler(Looper.getMainLooper())

    private val runnable = object : Runnable {
        override fun run() {
            updateBatteryInfo()
            handler.postDelayed(this, 1000)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        tvBatteryLevel = findViewById(R.id.tvBatteryLevel)
        tvStatus = findViewById(R.id.tvStatus)
        tvTemperature = findViewById(R.id.tvTemperature)
        tvHealth = findViewById(R.id.tvHealth)
        tvVoltage = findViewById(R.id.tvVoltage)
        tvCycleCount = findViewById(R.id.tvCycleCount)
        tvChargeCounter = findViewById(R.id.tvChargeCounter)
        tvBatteryPercentage = findViewById(R.id.tvBatteryPercentage)
        tvBatteryCurrent = findViewById(R.id.tvBatteryCurrent)

        val btnShowInstructions = findViewById<Button>(R.id.btnShowInstructions)
        btnShowInstructions.setOnClickListener {
            showInstructions()
        }

        // initial update, and start the periodic update
        updateBatteryInfo()
        handler.postDelayed(runnable, 5000)
    }

    private fun updateBatteryInfo() {
        val batteryStatus: Intent? = this.registerReceiver(null, IntentFilter(Intent.ACTION_BATTERY_CHANGED))
        batteryStatus?.let {
            val level = it.getIntExtra(BatteryManager.EXTRA_LEVEL, -1)
            val scale = it.getIntExtra(BatteryManager.EXTRA_SCALE, -1)
            val temp = it.getIntExtra(BatteryManager.EXTRA_TEMPERATURE, 0)
            val voltage = it.getIntExtra(BatteryManager.EXTRA_VOLTAGE, 0)
            val status = it.getIntExtra(BatteryManager.EXTRA_STATUS, -1)
            val health = it.getIntExtra(BatteryManager.EXTRA_HEALTH, 0)
            val cycleCount = it.getIntExtra(BatteryManager.EXTRA_CYCLE_COUNT, -1)

            tvBatteryLevel.text = getString(R.string.battery_level, level * 100 / scale)
            tvStatus.text = getString(R.string.battery_status, getStatusString(status))
            tvTemperature.text = getString(R.string.battery_temperature, temp / 10.0)
            tvHealth.text = getString(R.string.battery_health, getHealthString(health))
            tvVoltage.text = getString(R.string.battery_voltage, voltage)
            tvCycleCount.text = getString(R.string.battery_cycle_count, if (cycleCount != -1) cycleCount.toString() else getString(R.string.full_charge_capacity_unavailable))

            val batteryManager = getSystemService(Context.BATTERY_SERVICE) as BatteryManager
            val chargeCounter = batteryManager.getIntProperty(BatteryManager.BATTERY_PROPERTY_CHARGE_COUNTER)
            val chargeCounterInmAh = chargeCounter / 1000.0

            tvChargeCounter.text = if (chargeCounter != Int.MIN_VALUE) {
                getString(R.string.battery_charge_counter, chargeCounterInmAh)
            } else {
                getString(R.string.full_charge_capacity_unavailable)
            }

            val currentNow = batteryManager.getIntProperty(BatteryManager.BATTERY_PROPERTY_CURRENT_NOW)
            tvBatteryCurrent.text = getString(R.string.battery_current, currentNow)

            displayBatteryPercentage()
        }
    }

    private fun getStatusString(status: Int) = when (status) {
        BatteryManager.BATTERY_STATUS_CHARGING -> getString(R.string.battery_status_charging)
        BatteryManager.BATTERY_STATUS_DISCHARGING -> getString(R.string.battery_status_discharging)
        BatteryManager.BATTERY_STATUS_FULL -> getString(R.string.battery_status_full)
        else -> getString(R.string.battery_status_not_charging)
    }

    private fun getHealthString(health: Int) = when (health) {
        BatteryManager.BATTERY_HEALTH_GOOD -> getString(R.string.battery_health_good)
        BatteryManager.BATTERY_HEALTH_OVERHEAT -> getString(R.string.battery_health_overheat)
        BatteryManager.BATTERY_HEALTH_DEAD -> getString(R.string.battery_health_dead)
        else -> getString(R.string.battery_health_unknown)
    }

    @SuppressLint("StringFormatMatches")
    private fun displayBatteryPercentage() {
        val batteryManager = getSystemService(Context.BATTERY_SERVICE) as BatteryManager
        val batteryPercent = batteryManager.getIntProperty(BatteryManager.BATTERY_PROPERTY_CAPACITY)
        val currentNow = batteryManager.getIntProperty(BatteryManager.BATTERY_PROPERTY_CURRENT_NOW)
        if (currentNow == 0 && batteryPercent == 100) {
            val chargeCounter = batteryManager.getIntProperty(BatteryManager.BATTERY_PROPERTY_CHARGE_COUNTER)
            val fullChargeCapacity = if (batteryPercent > 0) (chargeCounter / (batteryPercent / 100.0)).toInt() / 1000 else -1
            if (fullChargeCapacity != -1) {
                saveFullChargeCapacity(fullChargeCapacity)
                tvBatteryPercentage.text = getString(R.string.full_charge_capacity, fullChargeCapacity)
            }
        } else {
            val savedCapacity = getSavedFullChargeCapacity()
            if (currentNow != 0) {
                tvBatteryPercentage.text = getString(R.string.full_charge_capacity_calc)
                if (savedCapacity != -1) {
                    tvBatteryPercentage.append("\n" + getString(R.string.last_time_estimated_capacity, savedCapacity))
                }
            }
            else {
                tvBatteryPercentage.text = getString(R.string.full_charge_capacity_unavailable)
            }
        }
    }

    private fun saveFullChargeCapacity(capacity: Int) {
        val sharedPrefs = getSharedPreferences("BatteryInfo", Context.MODE_PRIVATE)
        sharedPrefs.edit().putInt("FullChargeCapacity", capacity).apply()
    }

    private fun getSavedFullChargeCapacity(): Int {
        val sharedPrefs = getSharedPreferences("BatteryInfo", Context.MODE_PRIVATE)
        return sharedPrefs.getInt("FullChargeCapacity", -1)  // Return -1 if not set
    }

    private fun showInstructions() {
        val instructionsText = getString(R.string.instructions_content).trimIndent()
        AlertDialog.Builder(this)
            .setTitle(getString(R.string.instructions_title))
            .setMessage(instructionsText)
            .setPositiveButton(getString(R.string.ok_button), null)
            .show()
    }

    override fun onDestroy() {
        super.onDestroy()
        handler.removeCallbacks(runnable)
    }
}
