/*
 * UTILITÉ DU FICHIER :
 * Définit l'énumération DeviceState qui liste tous les états fonctionnels
 * possibles d'un équipement réseau supervisé dans NetMonitor.
 *
 * FICHIERS RELIÉS :
 * - NetworkDevice.java : utilise ce type pour stocker l'état de chaque machine.
 * - StorageService.java : convertit cet état en texte lors de la sauvegarde JSON.
 * - NetMonitorService.java : modifie cet état selon les seuils CPU/RAM ou le heartbeat.
 * - ConsoleMenu.java : affiche cet état dans la console.
 */

// Indique que ce fichier appartient au package (dossier) "model"
package model;

// Déclare une énumération publique nommée DeviceState
public enum DeviceState {
    // État normal : l'équipement répond et ses métriques sont sous les seuils
    UP,
    // État d'alerte : l'équipement répond mais présente une anomalie (CPU/RAM/latence)
    WARNING,
    // État indisponible : l'équipement ne répond plus (rupture de heartbeat)
    DOWN,
    // État critique : panne majeure nécessitant une intervention urgente
    CRITICAL
// Fin de la déclaration de l'énumération DeviceState
}