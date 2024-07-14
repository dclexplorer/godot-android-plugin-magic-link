extends Control

# Replace with your MAGIC PUBLISHABLE API KEY
@export var magic_key: String = ""

var ethereum_wallet: MagicLinkPluginInterface = MagicLinkPluginInterface.new()


func _ready():
	ethereum_wallet.setup(magic_key, "ethereum")
	prints("Ready!!")


# gdlint:ignore = async-function-name
func _on_button_is_connected_pressed():
	prints("IsConnected: ", await ethereum_wallet.async_check_connection())


func _on_button_open_wallet_pressed():
	ethereum_wallet.open_wallet()


# gdlint:ignore = async-function-name
func _on_button_sign_message_pressed():
	prints("Signature:", await ethereum_wallet.async_sign(%TextEditSignMessage.text))


# gdlint:ignore = async-function-name
func _on_button_login_pressed():
	await ethereum_wallet.async_login_email(%TextEditEmail.text)


func _on_button_google_pressed():
	await ethereum_wallet.async_login_social("google")


func _on_button_x_pressed():
	await ethereum_wallet.async_login_social("twitter")


func _on_button_apple_pressed():
	await ethereum_wallet.async_login_social("apple")


func _on_button_meta_pressed():
	await ethereum_wallet.async_login_social("facebook")


func _on_button_discord_pressed():
	await ethereum_wallet.async_login_social("discord")
