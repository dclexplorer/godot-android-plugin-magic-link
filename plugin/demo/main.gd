extends Control


# Replace with your MAGIC PUBLISHABLE API KEY
@export var magic_key: String = ""
@export var callback_url: String = "" # example: link.magic.demo://callback

var ethereum_wallet: MagicLinkPluginInterface = MagicLinkPluginInterface.new()

@onready var tab_bar = %TabBar

# gdlint:ignore = async-function-name
func _ready():
	%SignaturePopup.hide()
	tab_bar.current_tab = 0 # Loading
	
	ethereum_wallet.setup(magic_key, "ethereum")
	
	ethereum_wallet.connected.connect(self._on_connected)
	ethereum_wallet.logout.connect(self._on_logout)
	
	if !await ethereum_wallet.async_check_connection():
		tab_bar.current_tab = 1 # Login


func _on_connected(address: String):
	%LabelAddress.text = address
	tab_bar.current_tab = 2 # Connected
	
func _on_logout():
	tab_bar.current_tab = 1 # Login

# gdlint:ignore = async-function-name
func _on_button_is_connected_pressed():
	prints("IsConnected: ", await ethereum_wallet.async_check_connection())


func _on_button_open_wallet_pressed():
	ethereum_wallet.open_wallet()


# gdlint:ignore = async-function-name
func _on_button_sign_message_pressed():
	var signature = await ethereum_wallet.async_sign(%TextEditSignMessage.text)
	%LabelSignature.text = signature
	%SignaturePopup.show()


# gdlint:ignore = async-function-name
func _on_button_login_pressed():
	tab_bar.current_tab = 0 # Loading
	await ethereum_wallet.async_login_email(%TextEditEmail.text)


func _on_button_google_pressed():
	tab_bar.current_tab = 0 # Loading
	await ethereum_wallet.async_login_social("google")


func _on_button_x_pressed():
	tab_bar.current_tab = 0 # Loading
	await ethereum_wallet.async_login_social("twitter")


func _on_button_apple_pressed():
	tab_bar.current_tab = 0 # Loading
	await ethereum_wallet.async_login_social("apple")


func _on_button_meta_pressed():
	tab_bar.current_tab = 0 # Loading
	await ethereum_wallet.async_login_social("facebook")


func _on_button_discord_pressed():
	tab_bar.current_tab = 0 # Loading
	await ethereum_wallet.async_login_social("discord")


func _on_button_logout_pressed():
	tab_bar.current_tab = 0 # Loading
	await ethereum_wallet.async_logout()


func _on_button_close_signature_pressed():
	%SignaturePopup.hide()
