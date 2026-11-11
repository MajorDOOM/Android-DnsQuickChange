package fr.birdywood.dns

import android.content.Intent
import android.provider.Settings
import android.service.quicksettings.Tile
import android.service.quicksettings.TileService
import android.widget.Toast

class DnsTileService : TileService() {

    override fun onStartListening() {
        super.onStartListening()
        updateTileState()
    }

    override fun onClick() {
        super.onClick()

        if (!DnsSettings.hasWritePermission(this)) {
            Toast.makeText(this, "Permission ADB WRITE_SECURE_SETTINGS manquante", Toast.LENGTH_LONG).show()
            openSystemSettings()
            return
        }

        try {
            if (DnsSettings.isPrivateDns(this)) {
                DnsSettings.setAutomatic(this)
            } else {
                DnsSettings.setPrivate(this, DnsSettings.hostname(this))
            }
            updateTileState()
        } catch (e: Exception) {
            Toast.makeText(this, e.message ?: "Échec du changement DNS", Toast.LENGTH_LONG).show()
            openSystemSettings()
        }
    }

    private fun updateTileState() {
        val tile = qsTile ?: return
        val privateDns = DnsSettings.isPrivateDns(this)
        tile.label = if (privateDns) "DNS privé" else "DNS automatique"
        tile.subtitle = if (privateDns) DnsSettings.hostname(this) else "Mode opportuniste"
        tile.state = if (privateDns) Tile.STATE_ACTIVE else Tile.STATE_INACTIVE
        tile.updateTile()
    }

    private fun openSystemSettings() {
        val intent = Intent("android.settings.PRIVATE_DNS_SETTINGS").addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        try {
            startActivity(intent)
        } catch (_: Exception) {
            val fallback = Intent(Settings.ACTION_WIRELESS_SETTINGS).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            startActivity(fallback)
        }
    }
}
