package org.godotengine.plugin.android.magiclink

import GlobalContext
import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.ProgressBar
import androidx.appcompat.app.AppCompatActivity
import link.magic.android.extension.oauth.oauth
import link.magic.android.extension.oauth.requestConfiguration.OAuthConfiguration
import link.magic.android.extension.oauth.requestConfiguration.OAuthProvider
import link.magic.android.modules.auth.requestConfiguration.LoginWithEmailOTPConfiguration
import link.magic.android.modules.auth.response.DIDToken
import link.magic.android.modules.wallet.response.ShowWalletResponse
import org.godotengine.godot.plugin.SignalInfo
import org.godotengine.plugin.android.magiclink.GodotAndroidPlugin.Companion.LOGIN_REQUEST_CODE
import org.godotengine.plugin.android.magiclink.GodotAndroidPlugin.Companion.SIGN_REQUEST_CODE
import org.web3j.protocol.admin.methods.response.PersonalSign

enum class MagicLinkActivityType {
    LOGIN_EMAIL,
    LOGIN_SOCIAL,
    SIGN,
    OPEN_WALLET,
}

class MagicLinkActivity : AppCompatActivity() {

    private lateinit var progressBar: ProgressBar

    override fun onCreate(savedInstanceState: Bundle?) {
        GlobalContext.magic.setContext(this)

        Log.d("MagicLinkActivity", "onCreate 1")
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_magic_link)

        progressBar = findViewById(R.id.progressBar)

        // Simulate loading data
        progressBar.visibility = ProgressBar.VISIBLE

        Log.d("MagicLinkActivity", "onCreate")
        Log.d("MagicLinkActivity", "Starting... ${intent.getSerializableExtra("type") as MagicLinkActivityType?}")

        when (intent.getSerializableExtra("type") as MagicLinkActivityType?) {
            MagicLinkActivityType.OPEN_WALLET -> {
                val result = GlobalContext.magic.wallet.showUI(this)
                result.whenComplete { _: ShowWalletResponse?, _: Throwable? ->
                    finish()
                }
            }
            MagicLinkActivityType.SIGN -> {
                val message = intent.getStringExtra("message")!!
                val address = intent.getStringExtra("address")!!
                Log.d("MagicLinkActivity", "Starting sign...")

                val signedMessage =
                    GlobalContext.gethWeb3j.personalSign(message, address, "").sendAsync()

                signedMessage.whenComplete { sig: PersonalSign?, error: Throwable? ->
                    if (error != null) {
                        error.localizedMessage?.let { Log.e("MagicLinkActivity", it) }
                    }
                    if (sig != null && !sig.hasError()) {
                        Log.d("MagicLinkActivity", "Signature: ${sig.result}")

                        val resultIntent = Intent()
                        resultIntent.putExtra("signature", sig.result.toString())
                        setResult(RESULT_OK, resultIntent)
                        finishActivity(SIGN_REQUEST_CODE)
                    } else {
                        Log.d("MagicLinkActivity", "Eth Sign... Something went wrong")
                        finishActivity(SIGN_REQUEST_CODE)
                    }
                    finish()
                }
            }
            MagicLinkActivityType.LOGIN_EMAIL -> {
                val email = intent.getStringExtra("email")!!
                val configuration = LoginWithEmailOTPConfiguration(email)
                val result = GlobalContext.magic.auth.loginWithEmailOTP(this, configuration)

                result.whenComplete { response: DIDToken?, error: Throwable? ->
                    if (error != null) {
                        error.localizedMessage?.let { Log.e("MagicLinkActivity", it) }
                    }
                    if (response != null && !response.hasError()) {
                        Log.d("MagicLinkActivity", "You're logged in!" + response.result)
                    } else {
                        Log.d("MagicLinkActivity", "Not Logged in")
                    }
                    finishActivity(LOGIN_REQUEST_CODE)
                    finish()
                }
            }
            MagicLinkActivityType.LOGIN_SOCIAL -> {
                val oauthProvider = intent.getSerializableExtra("oauthProvider") as OAuthProvider?
                val configuration = OAuthConfiguration(oauthProvider!!, "org.decentraland.godotexplorer://callback")
                Log.d("MagicLinkActivity", "Social Login...")
                GlobalContext.magic.oauth.loginWithPopup(this, configuration)
                Log.d("MagicLinkActivity", "Done?")
            }
            else -> {
                finish()
            }
        }
    }

    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        Log.d("MagicLinkActivity", "Result... requestCode=${requestCode} resultCode=${resultCode} ${data?.toString()}")
        if (requestCode == 0) { // Result social login
            finishActivity(LOGIN_REQUEST_CODE)
            finish()
        }
    }

    @SuppressLint("MissingSuperCall")
    override fun onBackPressed() {
        // Do nothing to disable the back button
    }
}
