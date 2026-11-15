/*
 * UTILITÉ DU FICHIER :
 * Définit l'énumération IncidentState qui représente les trois étapes du cycle de vie
 * d'un incident réseau dans NetMonitor (ouverture, prise en charge, clôture).
 *
 * FICHIERS RELIÉS :
 * - Incident.java : utilise ce type pour suivre l'avancement de chaque incident.
 * - NetMonitorService.java : filtre les incidents ouverts pour les statistiques du Dashboard.
 * - ConsoleMenu.java : permet aux techniciens et administrateurs de changer cet état.
 */

// Indique que ce fichier appartient au package (dossier) "model"
package model;

// Déclare une énumération publique nommée IncidentState
public enum IncidentState {
    // État initial : l'incident vient d'être détecté et attend un technicien
    OPEN,
    // État en cours : un technicien a pris en charge l'incident et travaille dessus
    IN_PROGRESS,
    // État final : le problème est résolu et l'incident est clôturé
    CLOSED
// Fin de la déclaration de l'énumération IncidentState
}