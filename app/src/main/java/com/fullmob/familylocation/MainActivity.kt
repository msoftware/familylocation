package com.fullmob.familylocation

import androidx.core.app.ActivityCompat.checkSelfPermission
import androidx.core.app.ActivityCompat.requestPermissions

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.widget.Toast
import com.google.android.material.bottomnavigation.BottomNavigationView
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.fullmob.FamilyLocationApp
import java.io.FileDescriptor
import java.io.PrintWriter

class MainActivity : AppCompatActivity() {

    companion object {
        const val SEND_SMS: Int = 1005
        const val RECEIVE_SMS: Int = 1006
        const val READ_SMS: Int = 1007
        const val READ_CONTACTS: Int = 1008
        const val ACCESS_COARSE_LOCATION: Int = 1009
        const val ACCESS_FINE_LOCATION: Int = 1010
        const val READ_PHONE_STATE: Int = 10011
        const val ACCESS_NETWORK_STATE: Int = 10012
        const val CHANGE_NETWORK_STATE: Int = 10013
        const val ACCESS_WIFI_STATE: Int = 10014
        const val CHANGE_WIFI_STATE: Int = 10015
    }

    private val permissions = arrayOf(
        Manifest.permission.RECEIVE_SMS,
        Manifest.permission.SEND_SMS,
        Manifest.permission.READ_SMS,
        Manifest.permission.READ_CONTACTS,
        Manifest.permission.READ_PHONE_STATE,
        Manifest.permission.ACCESS_COARSE_LOCATION,
        Manifest.permission.ACCESS_FINE_LOCATION,
        Manifest.permission.ACCESS_NETWORK_STATE,
        Manifest.permission.CHANGE_NETWORK_STATE,
        Manifest.permission.ACCESS_WIFI_STATE,
        Manifest.permission.CHANGE_WIFI_STATE
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val navView: BottomNavigationView = findViewById(R.id.nav_view)

        val navController = findNavController(R.id.nav_host_fragment)
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        val appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.navigation_home, R.id.navigation_dashboard, R.id.navigation_notifications
            )
        )
        setupActionBarWithNavController(navController, appBarConfiguration)
        navView.setupWithNavController(navController)

        if (!areAllPermissionsGranted()) {
            requestAllPermissions()
            return
        }
        showToast("All Permissions Granted")
        (application as? FamilyLocationApp)?.cellTowerProvider?.getCellTowerIds()
    }

    private fun requestAllPermissions() {
        permissions.forEach {
            if (!isPermissionGranted(it, requestPermission = true)) return
        }

    }

    private fun areAllPermissionsGranted(): Boolean {
        permissions.forEach {
            if (!isPermissionGranted(it, requestPermission = false)) {
                return false
            }
        }
        return true
    }

    private fun isPermissionGranted(
        permission: String,
        requestPermission: Boolean
    ): Boolean {
        if (checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
            if (requestPermission) {
                requestPermissions(this, arrayOf(permission), codeForPermission(permission))
            }
            return false
        }
        return true
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        if (grantResults.contains(PackageManager.PERMISSION_GRANTED)) {
            when (requestCode) {
                SEND_SMS,
                RECEIVE_SMS,
                READ_SMS,
                READ_CONTACTS,
                ACCESS_COARSE_LOCATION,
                ACCESS_FINE_LOCATION,
                READ_PHONE_STATE,
                ACCESS_NETWORK_STATE,
                CHANGE_NETWORK_STATE,
                ACCESS_WIFI_STATE,
                CHANGE_WIFI_STATE
                -> onPermissionGranted()
            }
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }

    private fun codeForPermission(permission: String): Int {
        return when (permission) {
            Manifest.permission.RECEIVE_SMS -> RECEIVE_SMS
            Manifest.permission.SEND_SMS -> SEND_SMS
            Manifest.permission.READ_SMS -> READ_SMS
            Manifest.permission.READ_CONTACTS -> READ_CONTACTS
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.ACCESS_FINE_LOCATION -> ACCESS_COARSE_LOCATION
            Manifest.permission.READ_PHONE_STATE -> READ_PHONE_STATE
            Manifest.permission.ACCESS_WIFI_STATE -> ACCESS_WIFI_STATE
            Manifest.permission.CHANGE_WIFI_STATE -> CHANGE_WIFI_STATE
            Manifest.permission.ACCESS_NETWORK_STATE -> ACCESS_NETWORK_STATE
            Manifest.permission.CHANGE_NETWORK_STATE -> CHANGE_NETWORK_STATE
            else -> 0
        }
    }

    private fun onPermissionGranted() {
        requestAllPermissions()
    }


    private fun showToast(msg: String) {
        Toast.makeText(this@MainActivity, msg, Toast.LENGTH_SHORT).show()
    }
}
