/*
 * UTILITÉ DU FICHIER :
 * Définit l'énumération Severity qui qualifie le niveau de gravité (criticité)
 * d'un incident réseau détecté dans NetMonitor.
 *
 * FICHIERS RELIÉS :
 * - Incident.java : utilise ce type pour attribuer une priorité d'intervention à chaque incident.
 * - NetMonitorService.java : définit la gravité lors de la création automatique d'un incident.
 * - ConsoleMenu.java : affiche le niveau de gravité dans la liste des incidents.
 */

// Indique que ce fichier appartient au package (dossier) "model"
package model;

// Déclare une énumération publique nommée Severity
public enum Severity {
    // Gravité faible : anomalie mineure sans impact immédiat sur le service
    LOW,
    // Gravité moyenne : dégradation modérée nécessitant une surveillance
    MEDIUM,
    // Gravité haute : seuil critique dépassé (ex : CPU ou RAM saturés)
    HIGH,
    // Gravité critique : équipement totalement inaccessible (état DOWN)
    CRITICAL
// Fin de la déclaration de l'énumération Severity
}