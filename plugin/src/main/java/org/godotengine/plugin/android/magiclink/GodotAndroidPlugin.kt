package org.godotengine.plugin.android.magiclink

import GlobalContext
import android.app.Activity
import android.content.Intent
import android.util.Log
import android.view.View
import link.magic.android.Magic
import link.magic.android.core.relayer.urlBuilder.network.CustomNodeConfiguration
import link.magic.android.extension.oauth.requestConfiguration.OAuthProvider
import link.magic.android.modules.user.response.GetInfoResponse
import link.magic.android.modules.user.response.IsLoggedInResponse
import org.godotengine.godot.Godot
import org.godotengine.godot.plugin.GodotPlugin
import org.godotengine.godot.plugin.SignalInfo
import org.godotengine.godot.plugin.UsedByGodot
import org.web3j.protocol.Web3j
import org.web3j.protocol.geth.Geth

class GodotAndroidPlugin(godot: Godot) : GodotPlugin(godot) {
    companion object {
        const val LOGIN_REQUEST_CODE = 1
        const val SIGN_REQUEST_CODE = 2
        const val LOGOUT_REQUEST_CODE = 3

        val CONNECTION_STATE_SIGNAL = SignalInfo("connection_state", String::class.java)
        val CONNECTED_SIGNAL = SignalInfo("connected", String::class.java)
        val SIGNED_MESSAGE_SIGNAL = SignalInfo("signed_message", String::class.java)
        val LOGOUT_SIGNAL = SignalInfo("on_logout")
    }

    override fun getPluginSignals() = setOf(CONNECTION_STATE_SIGNAL, CONNECTED_SIGNAL, SIGNED_MESSAGE_SIGNAL, LOGOUT_SIGNAL)

    override fun getPluginName() = BuildConfig.GODOT_PLUGIN_NAME

    override fun onMainCreate(activity: Activity?): View? {
        Log.d("MagicLinkActivity", "onMainCreate called")
        return null
    }

    @UsedByGodot
    private fun setup(magicKey: String, callbackUrl: String, network: String): Boolean {
        activity?.let { activity ->
            runOnUiThread {
                if (network == "polygon") {
                    val customNode = CustomNodeConfiguration("https://polygon-rpc.com/", "137")
                    GlobalContext.magic =
                        Magic(activity.applicationContext, magicKey, customNode)
                } else {
                    GlobalContext.magic = Magic(activity.applicationContext, magicKey)
                }

                GlobalContext.web3j = Web3j.build(GlobalContext.magic.rpcProvider)
                GlobalContext.gethWeb3j = Geth.build(GlobalContext.magic.rpcProvider)

                GlobalContext.callbackUrl = callbackUrl
            }
            Log.d("MagicLinkActivity", "Setup done")
            return true
        }
        return false
    }

    @UsedByGodot
    private fun checkConnection() {
        activity?.let { act ->
            runOnUiThread {
                Log.d("MagicLinkActivity", "Checking connection:")
                val result = GlobalContext.magic.user.isLoggedIn(act)
                result.whenComplete { isLogged: IsLoggedInResponse?, error: Throwable? ->
                    if (error != null) {
                        error.localizedMessage?.let { Log.e("MagicLinkActivity", it) }
                    }

                    if (isLogged != null) {
                        Log.d(
                            "MagicLinkActivity",
                            "isLogged.result: ${isLogged.result}"
                        )
                        val isConnected = isLogged.result.toString()
                        emitSignal(CONNECTION_STATE_SIGNAL.name, isConnected)

                        if (isLogged.result) {
                            reportAddress()
                        }
                    } else {
                        emitSignal(CONNECTION_STATE_SIGNAL.name, "false")
                    }
                }
            }
        }
    }

    private fun reportAddress() {
        activity?.let { act ->
            runOnUiThread {
                Log.d("MagicLinkActivity", "Getting metadata:")
                val result = GlobalContext.magic.user.getInfo(act);
                result.whenComplete { metadata: GetInfoResponse?, error: Throwable? ->
                    if (error != null) {
                        error.localizedMessage?.let { Log.e("MagicLinkActivity", it) }
                    }

                    if (metadata != null) {
                        Log.d(
                            "MagicLinkActivity",
                            "Metadata address: ${metadata.result.publicAddress}"
                        )
                        GlobalContext.address = metadata.result.publicAddress
                        emitSignal(CONNECTED_SIGNAL.name, metadata.result.publicAddress)
                    } else {
                        emitSignal(CONNECTED_SIGNAL.name, "")
                    }
                }
            }
        }
    }

    @UsedByGodot
    private fun loginEmailOTP(email: String) {
        activity?.let { activity ->
            val intent = Intent(activity, MagicLinkActivity::class.java)
            intent.putExtra("type", MagicLinkActivityType.LOGIN_EMAIL)
            intent.putExtra("email", email)
            activity.startActivityForResult(intent, LOGIN_REQUEST_CODE)
        }
    }

    private fun getOAuthProvider(providerName: String): OAuthProvider? {
        return try {
            OAuthProvider.valueOf(providerName.uppercase())
        } catch (e: IllegalArgumentException) {
            null
        }
    }

    @UsedByGodot
    private fun loginSocial(oauthProvider: String) {
        val provider = getOAuthProvider(oauthProvider) ?: return

        activity?.let { activity ->
            val intent = Intent(activity, MagicLinkActivity::class.java)
            intent.putExtra("type", MagicLinkActivityType.LOGIN_SOCIAL)
            intent.putExtra("oauthProvider", provider)
            activity.startActivityForResult(intent, LOGIN_REQUEST_CODE)
        }
    }

    @UsedByGodot
    private fun logout() {
        activity?.let { activity ->
            val intent = Intent(activity, MagicLinkActivity::class.java)
            intent.putExtra("type", MagicLinkActivityType.LOGOUT)
            activity.startActivityForResult(intent, LOGOUT_REQUEST_CODE)
        }
    }

    @UsedByGodot
    private fun openWallet() {
        activity?.let { activity ->
            val intent = Intent(activity, MagicLinkActivity::class.java)
            intent.putExtra("type", MagicLinkActivityType.OPEN_WALLET)
            activity.startActivity(intent)
        }
    }

    @UsedByGodot
    private fun sign(message: String) {
        activity?.let { activity ->
            val intent = Intent(activity, MagicLinkActivity::class.java)
            intent.putExtra("type", MagicLinkActivityType.SIGN)
            intent.putExtra("message", message)
            intent.putExtra("address", GlobalContext.address)
            activity.startActivityForResult(intent, SIGN_REQUEST_CODE)
        }
    }

    override fun onMainActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onMainActivityResult(requestCode, resultCode, data)

        Log.d(
            "MagicLinkActivity",
            "onMainActivityResult: $requestCode $data"
        )

        when (requestCode) {
            LOGIN_REQUEST_CODE -> reportAddress()
            SIGN_REQUEST_CODE -> emitSignal(GodotAndroidPlugin.SIGNED_MESSAGE_SIGNAL.name, data?.getStringExtra("signature"))
            LOGOUT_REQUEST_CODE -> emitSignal(GodotAndroidPlugin.LOGOUT_SIGNAL.name)
        }
    }

    override fun onMainDestroy() {
        GlobalContext.magic = null as Magic
        super.onMainDestroy()
    }
}
