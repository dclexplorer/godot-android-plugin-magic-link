# Godot Android Magic Link Plugin

Simple plugin for integrating Godot with Magic Link

Based on: https://github.com/m4gr3d/Godot-Android-Plugin-Template/

Usage:

```gdscript
extends Control

var ethereum_wallet: MagicLinkPluginInterface = MagicLinkPluginInterface.new()


func _ready():
	ethereum_wallet.setup("ethereum")
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

```