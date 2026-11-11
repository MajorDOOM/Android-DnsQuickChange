# DNS rapide — application Android Kotlin

Tuile Paramètres rapides qui bascule entre :
- **DNS automatique** (`private_dns_mode=opportunistic`)
- **DNS privé strict** (`private_dns_mode=hostname`) avec le fournisseur configuré dans l'application.

Android ne donne pas `WRITE_SECURE_SETTINGS` à une application ordinaire. Le projet déclare cette permission, mais il faut l'accorder une seule fois avec ADB. Aucun root n'est nécessaire.

## Prérequis
- Android 10 (API 29) ou plus récent.
- Android Studio avec JDK 17.
- ADB (Android Platform Tools), pour l'autorisation initiale uniquement.

## Installation et autorisation unique

1. Ouvrir ce projet dans Android Studio.
2. Compiler et installer l'application sur le téléphone.
3. Activer les Options pour les développeurs et le Débogage USB.
4. Connecter le téléphone au PC et accepter la clé RSA de débogage.
5. Dans un terminal sur le PC, exécuter une seule fois :

```bash
.\adb.exe devices
.\adb.exe shell pm grant fr.birdywood.dns android.permission.WRITE_SECURE_SETTINGS
```

La commande `.\adb.exe devices` vérifie simplement la connexion. La commande `pm grant` accorde la permission à l'application. Déconnectez ensuite le câble et désactivez le débogage USB si vous le souhaitez.

Si l'application est désinstallée puis réinstallée, ou si les données/permissions sont réinitialisées, il peut être nécessaire de réaccorder la permission.

## Utilisation

1. Ouvrir **DNS rapide**.
2. Saisir un nom d'hôte DNS-over-TLS, par exemple `dns.google` ou `one.one.one.one`.
3. Appuyer sur **Enregistrer le fournisseur**.
4. Ouvrir les Paramètres rapides, appuyer sur Modifier et ajouter la tuile **DNS**.
5. Un appui sur la tuile alterne entre DNS automatique et DNS privé.

La tuile indique son état : « DNS automatique » ou « DNS privé » avec le nom du fournisseur. L'application conserve le nom d'hôte dans le réglage système Android.

## Notes et limites

- Le mode automatique est le mode Android « opportunistic » : Android utilise le DNS fourni par le réseau et tente DNS-over-TLS lorsque le serveur le prend en charge. Ce n'est pas le mode `off`.
- Le mode privé strict échoue volontairement en cas d'indisponibilité du fournisseur DNS-over-TLS, ce qui peut empêcher la résolution DNS.
- Certains constructeurs peuvent bloquer ou réinitialiser des modifications de `Settings.Global`, même après accord de `WRITE_SECURE_SETTINGS`. Dans ce cas, le bouton des paramètres Android permet de faire le changement manuellement.
- La tuile actualise son état à chaque ouverture/écoute des Paramètres rapides.
- Le projet ne configure pas de VPN et ne modifie pas les DNS applicatifs personnalisés.

## Compilation APK

Dans Android Studio : **Build > Build Bundle(s) / APK(s) > Build APK(s)**.
L'APK debug est normalement généré dans `app/build/outputs/apk/debug/app-debug.apk`.

## Identifiants
- Application ID : `fr.birdywood.dns`
- Version : `2.0`
- Min SDK : 37
- Compile / Target SDK : 37
