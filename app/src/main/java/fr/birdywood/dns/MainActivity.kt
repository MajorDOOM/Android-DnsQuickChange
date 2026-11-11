package fr.birdywood.dns

import android.R.attr.singleLine
import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import android.view.Gravity
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast

class MainActivity : Activity() {
    private lateinit var status: TextView
    private lateinit var hostname: EditText

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        buildUi()
        refreshStatus()
    }

    override fun onResume() {
        super.onResume()
        if (::status.isInitialized) refreshStatus()
    }

    private fun buildUi() {
        val pad = (22 * resources.displayMetrics.density).toInt()
        val layout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER_HORIZONTAL
            setPadding(pad, pad, pad, pad)
        }

        val title = TextView(this).apply {
            text = "DNS rapide"
            textSize = 26f
        }
        status = TextView(this).apply {
            textSize = 16f
            setPadding(0, pad, 0, pad)
        }
        val hostLabel = TextView(this).apply {
            text = "Nom d'hôte du DNS privé"
            textSize = 15f
        }
        hostname = EditText(this).apply {
            maxLines = 1
            hint = DnsSettings.DEFAULT_HOSTNAME
            setText(DnsSettings.hostname(this@MainActivity))
        }
        val save = Button(this).apply {
            text = "Enregistrer le fournisseur"
            setOnClickListener {
                val host = hostname.text.toString().trim()
                if (!DnsSettings.isValidHostname(host)) {
                    hostname.error = "Nom d'hôte invalide"
                    return@setOnClickListener
                }
                if (!ensurePermission()) return@setOnClickListener
                try {
                    // Enregistre le fournisseur sans forcer l'activation du mode privé.
                    check(android.provider.Settings.Global.putString(
                        contentResolver,
                        "private_dns_specifier", //Settings.Global.PRIVATE_DNS_SPECIFIER,
                        host
                    )) { "Écriture refusée" }
                    Toast.makeText(this@MainActivity, "Fournisseur enregistré", Toast.LENGTH_SHORT).show()
                    refreshStatus()
                } catch (e: Exception) {
                    Toast.makeText(this@MainActivity, e.message ?: "Erreur", Toast.LENGTH_LONG).show()
                }
            }
        }
        val automatic = Button(this).apply {
            text = "Passer en DNS automatique"
            setOnClickListener {
                if (!ensurePermission()) return@setOnClickListener
                runAction { DnsSettings.setAutomatic(this@MainActivity) }
            }
        }
        val privateDns = Button(this).apply {
            text = "Activer le DNS privé"
            setOnClickListener {
                if (!ensurePermission()) return@setOnClickListener
                runAction { DnsSettings.setPrivate(this@MainActivity, hostname.text.toString()) }
            }
        }
        val systemSettings = Button(this).apply {
            text = "Ouvrir les paramètres DNS Android"
            setOnClickListener { openSystemDnsSettings() }
        }
        val note = TextView(this).apply {
            text = "La tuile des Paramètres rapides bascule entre DNS automatique et DNS privé avec le fournisseur enregistré ici. Si l'écriture est refusée par le fabricant, utilisez les paramètres Android."
            textSize = 13f
            setPadding(0, pad, 0, 0)
        }

        val note2 = TextView(this).apply {
            text = ".\\adb.exe devices"
            textSize = 13f
            setPadding(0, pad, 0, 0)
        }
        val note3 = TextView(this).apply {
            text = ".\\adb.exe shell pm grant fr.birdywood.dns android.permission.WRITE_SECURE_SETTINGS"
            textSize = 13f
            setPadding(0, pad, 0, 0)
        }

        listOf(title, status, hostLabel, hostname, save, automatic, privateDns, systemSettings, note, note2, note3)
            .forEach { layout.addView(it, LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )) }
        setContentView(layout)
    }

    private fun ensurePermission(): Boolean {
        if (DnsSettings.hasWritePermission(this)) return true
        Toast.makeText(this, "Permission WRITE_SECURE_SETTINGS absente. Accordez-la avec ADB (voir README).", Toast.LENGTH_LONG).show()
        return false
    }

    private fun runAction(action: () -> Unit) {
        try {
            action()
            refreshStatus()
            Toast.makeText(this, "Réglage DNS mis à jour", Toast.LENGTH_SHORT).show()
        } catch (e: Exception) {
            Toast.makeText(this, e.message ?: "Impossible de modifier le DNS", Toast.LENGTH_LONG).show()
        }
    }

    private fun refreshStatus() {
        val mode = DnsSettings.mode(this)
        val label = when (mode) {
            "hostname" -> "DNS privé actif : ${DnsSettings.hostname(this)}"
            "opportunistic", null -> "DNS automatique (mode opportuniste)"
            "off" -> "DNS privé désactivé"
            else -> "Mode DNS : $mode"
        }
        status.text = if (DnsSettings.hasWritePermission(this)) label
            else "$label\n\nPermission ADB non accordée."
    }

    private fun openSystemDnsSettings() {
        try {
            startActivity(Intent("android.settings.PRIVATE_DNS_SETTINGS"))
        } catch (_: Exception) {
            startActivity(Intent(Settings.ACTION_WIRELESS_SETTINGS))
        }
    }
}
