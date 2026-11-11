package fr.birdywood.dns

import android.content.Context
import android.provider.Settings

/**
 * Android Private DNS modes:
 * - opportunistic: automatic DNS / DNS supplied by the network, with opportunistic DoT
 * - hostname: strict Private DNS using the configured provider hostname
 * - off: Private DNS disabled
 */
object DnsSettings {
    const val DEFAULT_HOSTNAME = "dns.adguard.com"
    private const val MODE_OPPORTUNISTIC = "opportunistic"
    private const val MODE_HOSTNAME = "hostname"

    fun hasWritePermission(context: Context): Boolean =
        context.checkSelfPermission(android.Manifest.permission.WRITE_SECURE_SETTINGS) ==
            android.content.pm.PackageManager.PERMISSION_GRANTED

    fun mode(context: Context): String? =
        Settings.Global.getString(context.contentResolver, "private_dns_mode")//Settings.Global.PRIVATE_DNS_MODE)

    fun hostname(context: Context): String =
        Settings.Global.getString(
            context.contentResolver,
            "private_dns_specifier"//Settings.Global.PRIVATE_DNS_SPECIFIER
        )?.takeIf { it.isNotBlank() } ?: DEFAULT_HOSTNAME

    fun isPrivateDns(context: Context): Boolean =
        mode(context) == MODE_HOSTNAME

    fun setAutomatic(context: Context) {
        check(hasWritePermission(context)) { "WRITE_SECURE_SETTINGS non accordée" }
        check(Settings.Global.putString(
            context.contentResolver,
            "private_dns_mode", //Settings.Global.PRIVATE_DNS_MODE,
            MODE_OPPORTUNISTIC
        )) { "Impossible d'écrire le mode DNS automatique" }
    }

    fun setPrivate(context: Context, host: String) {
        require(isValidHostname(host)) { "Nom d'hôte DNS invalide" }
        check(hasWritePermission(context)) { "WRITE_SECURE_SETTINGS non accordée" }

        // Écrire d'abord le fournisseur, puis activer le mode strict.
        check(Settings.Global.putString(
            context.contentResolver,
            "private_dns_specifier",//Settings.Global.PRIVATE_DNS_SPECIFIER,
            host.trim()
        )) { "Impossible d'enregistrer le fournisseur DNS" }

        check(Settings.Global.putString(
            context.contentResolver,
            "private_dns_mode", //Settings.Global.PRIVATE_DNS_MODE,
            MODE_HOSTNAME
        )) { "Impossible d'activer le DNS privé" }
    }

    fun isValidHostname(value: String): Boolean {
        val host = value.trim().removeSuffix(".")
        if (host.isEmpty() || host.length > 253 || host.any { it.isWhitespace() }) return false
        return host.split('.').all { label ->
            label.isNotEmpty() && label.length <= 63 &&
                label.first().isLetterOrDigit() &&
                label.last().isLetterOrDigit() &&
                label.all { it.isLetterOrDigit() || it == '-' }
        }
    }
}
