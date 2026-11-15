/*
 * UTILITÉ DU FICHIER :
 * Définit l'énumération UserRole qui liste les profils (rôles) possibles
 * pour un utilisateur dans NetMonitor, afin de gérer les droits d'accès.
 *
 * FICHIERS RELIÉS :
 * - User.java : utilise ce type pour associer un rôle à chaque compte utilisateur.
 * - AuthService.java : vérifie ce rôle pour autoriser ou bloquer certaines actions.
 * - StorageService.java : convertit ce rôle en texte lors de la sauvegarde JSON.
 * - ConsoleMenu.java : adapte les options du menu affiché selon le rôle connecté.
 */

// Indique que ce fichier appartient au package (dossier) "model"
package model;

// Déclare une énumération publique nommée UserRole
public enum UserRole {
    // Rôle Administrateur : accès complet (équipements, utilisateurs, seuils, incidents)
    ADMIN,
    // Rôle Technicien : accès limité (consultation, prise en charge et clôture des incidents)
    TECHNICIEN
// Fin de la déclaration de l'énumération UserRole
}