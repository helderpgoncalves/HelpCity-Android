package com.example.helpcity

import android.os.Bundle
import android.view.View.GONE
import android.view.View.VISIBLE
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.helpcity.api.EndPoints
import com.example.helpcity.api.ServiceBuilder
import com.example.helpcity.api.User
import kotlinx.android.synthetic.main.activity_login.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

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
                    postLogin(email, password)
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

    // FUNÇÃO DE PEDIDO POST PARA LOGIN
    private fun postLogin(email: String, password: String) {
        val request = ServiceBuilder.buildService(EndPoints::class.java)
        val call = request.userLogin(email, password)

        call.enqueue(object : Callback<User> {
            override fun onResponse(call: Call<User>, response: Response<User>) {
                if (response.isSuccessful) {

                    val user = response.body()!!
                    Toast.makeText(this@LoginActivity, R.string.login_success, Toast.LENGTH_SHORT)
                        .show()
                    AppPreferences.isLogin = true
                    AppPreferences.email = user.email
                    AppPreferences.username = user.name
                    AppPreferences.password = password
                    AppPreferences.id = user.id

                    setupLoginLayout()
                }
            }

            override fun onFailure(call: Call<User>, t: Throwable) {
                Toast.makeText(this@LoginActivity, t.message, Toast.LENGTH_SHORT).show()
                AppPreferences.isLogin = false
                AppPreferences.email = ""
                AppPreferences.password = ""
                AppPreferences.username = ""
            }
        })
    }
}
