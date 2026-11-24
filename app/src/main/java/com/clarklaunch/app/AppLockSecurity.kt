package com.clarklaunch.app

import android.content.Context
import android.util.Base64
import java.security.SecureRandom
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.PBEKeySpec

/**
 * Salted PBKDF2 PIN storage. This protects launches routed through ClarkLaunch,
 * not external intents, notifications, system settings, or other launchers.
 */
object AppLockSecurity {
    private const val PREFS = "clarklaunch"
    private const val SALT_KEY = "app_lock_salt"
    private const val HASH_KEY = "app_lock_hash"
    private const val ITERATIONS = 120_000
    private const val KEY_LENGTH = 256

    fun hasPin(context: Context): Boolean {
        val prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        return prefs.contains(SALT_KEY) && prefs.contains(HASH_KEY)
    }

    fun setPin(context: Context, pin: String) {
        require(pin.length in 4..8 && pin.all(Char::isDigit))
        val salt = ByteArray(16).also { SecureRandom().nextBytes(it) }
        val hash = derive(pin, salt)
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).edit()
            .putString(SALT_KEY, Base64.encodeToString(salt, Base64.NO_WRAP))
            .putString(HASH_KEY, Base64.encodeToString(hash, Base64.NO_WRAP))
            .apply()
    }

    fun verify(context: Context, pin: String): Boolean {
        val prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        val saltText = prefs.getString(SALT_KEY, null) ?: return false
        val hashText = prefs.getString(HASH_KEY, null) ?: return false
        return try {
            val salt = Base64.decode(saltText, Base64.NO_WRAP)
            val expected = Base64.decode(hashText, Base64.NO_WRAP)
            derive(pin, salt).contentEquals(expected)
        } catch (_: Exception) {
            false
        }
    }

    private fun derive(pin: String, salt: ByteArray): ByteArray {
        val spec = PBEKeySpec(pin.toCharArray(), salt, ITERATIONS, KEY_LENGTH)
        return try {
            SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256").generateSecret(spec).encoded
        } finally {
            spec.clearPassword()
        }
    }
}
