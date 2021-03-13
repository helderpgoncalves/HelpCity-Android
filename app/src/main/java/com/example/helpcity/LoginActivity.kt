package com.example.helpcity

import android.os.Bundle
import android.view.View.GONE
import android.view.View.VISIBLE
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_login.*
import kotlinx.android.synthetic.main.activity_main.*


class LoginActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        setupLoginLayout()

        btnLogin.setOnClickListener {
            if (AppPreferences.isLogin) {
                AppPreferences.isLogin = false
                AppPreferences.email = ""
                AppPreferences.password = ""
                Toast.makeText(this, R.string.logout_success, Toast.LENGTH_SHORT).show()
            } else {
                val email = etEmail.text.toString()
                val password = etPassword.text.toString()
                if (email.isNotBlank() && password.isNotBlank()) {
                    AppPreferences.isLogin = true
                    AppPreferences.email = email
                    AppPreferences.password = password
                    Toast.makeText(this, R.string.login_success, Toast.LENGTH_SHORT).show()
                    finish()
                } else {
                    Toast.makeText(this, R.string.login_validation, Toast.LENGTH_SHORT).show()
                }
            }
            setupLoginLayout()
        }
    }

    private fun setupLoginLayout() {
        if (AppPreferences.isLogin) {
            etEmail.visibility = GONE
            etPassword.visibility = GONE
            btnLogin.text = getString(R.string.logout)
        } else {
            etEmail.visibility = VISIBLE
            etPassword.visibility = VISIBLE
            btnLogin.text = getString(R.string.login)
        }
    }
}