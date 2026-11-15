/*
 * UTILITÉ DU FICHIER :
 * Gère dynamiquement les utilisateurs de NetMonitor (création, suppression, liste),
 * l'authentification (connexion/déconnexion) et la vérification des privilèges selon le rôle
 * (ADMIN ou TECHNICIEN), sans aucun compte codé en dur dans le programme.
 *
 * FICHIERS RELIÉS :
 * - User.java & UserRole.java : manipule les comptes et vérifie les rôles ADMIN/TECHNICIEN.
 * - StorageService.java : charge et sauvegarde automatiquement la liste dans data/users.json.
 * - ConsoleMenu.java : utilise ce service pour l'écran de connexion et le menu admin.
 * - Main.java : initialise AuthService au démarrage de l'application.
 */

// Indique que ce fichier appartient au package (dossier) "service"
package service;

// Importe ArrayList pour manipuler la liste dynamique des utilisateurs en mémoire
import java.util.ArrayList;
// Importe List pour typer les collections d'utilisateurs
import java.util.List;
// Importe la classe User depuis le package model
import model.User;
// Importe l'énumération UserRole depuis le package model
import model.UserRole;

// Déclare la classe publique AuthService chargée des comptes et de la session active
public class AuthService {

    // Référence vers le service de persistance JSON pour sauvegarder les comptes sur disque
    private final StorageService storageService;

    // Liste dynamique en mémoire contenant tous les utilisateurs enregistrés
    private final List<User> utilisateurs;

    // Référence vers l'utilisateur actuellement connecté (null si personne n'est connecté)
    private User utilisateurConnecte;

    // Constructeur recevant le StorageService et chargeant les comptes existants depuis le JSON
    public AuthService(StorageService storageService) {
        // Enregistre la référence vers le service de stockage
        this.storageService = storageService;
        // Charge la liste des utilisateurs sauvegardés dans data/users.json
        this.utilisateurs = new ArrayList<>(storageService.chargerUtilisateurs());
        // Initialise la session active à null (aucun utilisateur connecté au lancement)
        this.utilisateurConnecte = null;
    // Fin du constructeur AuthService
    }

    // Indique s'il n'existe encore aucun compte dans l'application (premier lancement)
    public boolean aucunUtilisateurEnregistre() {
        // Retourne true si la liste des utilisateurs est totalement vide
        return utilisateurs.isEmpty();
    // Fin de la méthode aucunUtilisateurEnregistre
    }

    // Crée dynamiquement un nouvel utilisateur et le sauvegarde immédiatement dans users.json
    public User creerUtilisateur(String identifiant, String nom, String login, String motDePasse, UserRole role) {
        // Vérifie que l'identifiant demandé n'est pas déjà utilisé par un autre compte
        if (rechercherParIdentifiant(identifiant) != null) {
            // Lève une exception pour empêcher les doublons d'identifiant
            throw new IllegalArgumentException("Un utilisateur avec l'identifiant '" + identifiant + "' existe déjà.");
        // Fin de la vérification d'identifiant unique
        }
        // Vérifie que le login de connexion n'est pas déjà pris
        if (rechercherParLogin(login) != null) {
            // Lève une exception pour empêcher les doublons de login
            throw new IllegalArgumentException("Le login '" + login + "' est déjà utilisé.");
        // Fin de la vérification de login unique
        }

        // Instancie le nouvel objet User (les champs obligatoires sont validés par le constructeur)
        User nouvelUtilisateur = new User(identifiant, nom, login, motDePasse, role);
        // Ajoute le nouvel utilisateur à la liste dynamique en mémoire
        utilisateurs.add(nouvelUtilisateur);
        // Sauvegarde immédiatement la liste mise à jour dans data/users.json
        storageService.sauvegarderUtilisateurs(utilisateurs);
        // Retourne l'utilisateur créé
        return nouvelUtilisateur;
    // Fin de la méthode creerUtilisateur
    }

    // Supprime dynamiquement un utilisateur par son identifiant et met à jour users.json
    public boolean supprimerUtilisateur(String identifiant) {
        // Recherche l'utilisateur correspondant à l'identifiant fourni
        User cible = rechercherParIdentifiant(identifiant);
        // Retourne false si aucun utilisateur ne correspond à cet identifiant
        if (cible == null) {
            // Indique que la suppression n'a pas pu avoir lieu
            return false;
        // Fin du test d'existence
        }
        // Empêche un administrateur connecté de supprimer son propre compte actif
        if (utilisateurConnecte != null && utilisateurConnecte.getIdentifiant().equalsIgnoreCase(cible.getIdentifiant())) {
            // Lève une exception pour protéger la session courante
            throw new IllegalStateException("Impossible de supprimer le compte actuellement connecté.");
        // Fin de la vérification d'auto-suppression
        }
        // Empêche de supprimer le dernier administrateur du système pour éviter de bloquer l'accès
        if (cible.getRole() == UserRole.ADMIN && compterAdministrateurs() <= 1) {
            // Lève une exception car il doit toujours rester au moins un administrateur
            throw new IllegalStateException("Impossible de supprimer le dernier administrateur du système.");
        // Fin de la vérification du dernier admin
        }

        // Retire l'utilisateur de la liste en mémoire
        utilisateurs.remove(cible);
        // Sauvegarde immédiatement la liste mise à jour dans data/users.json
        storageService.sauvegarderUtilisateurs(utilisateurs);
        // Retourne true pour confirmer la suppression
        return true;
    // Fin de la méthode supprimerUtilisateur
    }

    // Compte combien d'utilisateurs possèdent le rôle ADMIN
    private int compterAdministrateurs() {
        // Initialise le compteur d'administrateurs à 0
        int compteur = 0;
        // Parcourt tous les utilisateurs enregistrés
        for (User u : utilisateurs) {
            // Vérifie si l'utilisateur courant a le rôle ADMIN
            if (u.getRole() == UserRole.ADMIN) {
                // Incrémente le compteur
                compteur++;
            // Fin du test de rôle
            }
        // Fin de la boucle
        }
        // Retourne le nombre total d'administrateurs trouvés
        return compteur;
    // Fin de la méthode compterAdministrateurs
    }

    // Recherche un utilisateur par son identifiant unique (insensible à la casse)
    public User rechercherParIdentifiant(String identifiant) {
        // Retourne null si l'identifiant recherché est vide
        if (identifiant == null) {
            // Renvoie null
            return null;
        // Fin du test de nullité
        }
        // Parcourt la liste des utilisateurs
        for (User u : utilisateurs) {
            // Compare l'identifiant sans tenir compte des majuscules/minuscules
            if (u.getIdentifiant().equalsIgnoreCase(identifiant.trim())) {
                // Retourne l'utilisateur trouvé
                return u;
            // Fin de la comparaison
            }
        // Fin de la boucle
        }
        // Retourne null si aucun utilisateur ne correspond
        return null;
    // Fin de la méthode rechercherParIdentifiant
    }

    // Recherche un utilisateur par son login de connexion (insensible à la casse)
    public User rechercherParLogin(String login) {
        // Retourne null si le login recherché est vide
        if (login == null) {
            // Renvoie null
            return null;
        // Fin du test de nullité
        }
        // Parcourt la liste des utilisateurs
        for (User u : utilisateurs) {
            // Compare le login sans tenir compte des majuscules/minuscules
            if (u.getLogin().equalsIgnoreCase(login.trim())) {
                // Retourne l'utilisateur trouvé
                return u;
            // Fin de la comparaison
            }
        // Fin de la boucle
        }
        // Retourne null si aucun utilisateur ne correspond
        return null;
    // Fin de la méthode rechercherParLogin
    }

    // Vérifie les identifiants de connexion et ouvre la session si valides
    public boolean connecter(String login, String motDePasse) {
        // Recherche le compte correspondant au login saisi
        User candidat = rechercherParLogin(login);
        // Vérifie que le compte existe et que le mot de passe correspond exactement
        if (candidat != null && candidat.verifierMotDePasse(motDePasse)) {
            // Enregistre l'utilisateur comme étant connecté dans la session active
            this.utilisateurConnecte = candidat;
            // Retourne true pour valider la connexion
            return true;
        // Fin de la vérification des identifiants
        }
        // Retourne false en cas de login inconnu ou de mot de passe incorrect
        return false;
    // Fin de la méthode connecter
    }

    // Ferme la session de l'utilisateur actuellement connecté
    public void deconnecter() {
        // Réinitialise l'utilisateur connecté à null
        this.utilisateurConnecte = null;
    // Fin de la méthode deconnecter
    }

    // Retourne l'utilisateur actuellement authentifié (ou null si déconnecté)
    public User getUtilisateurConnecte() {
        // Renvoie la référence vers l'utilisateur de la session active
        return utilisateurConnecte;
    // Fin du getter getUtilisateurConnecte
    }

    // Indique si une session utilisateur est actuellement ouverte
    public boolean estConnecte() {
        // Retourne true si un utilisateur est connecté
        return utilisateurConnecte != null;
    // Fin de la méthode estConnecte
    }

    // Indique si l'utilisateur connecté possède les privilèges Administrateur
    public boolean estAdmin() {
        // Vérifie qu'un utilisateur est connecté et que son rôle est ADMIN
        return utilisateurConnecte != null && utilisateurConnecte.getRole() == UserRole.ADMIN;
    // Fin de la méthode estAdmin
    }

    // Retourne une copie de la liste de tous les utilisateurs enregistrés
    public List<User> listerUtilisateurs() {
        // Renvoie une nouvelle liste pour protéger la collection interne
        return new ArrayList<>(utilisateurs);
    // Fin de la méthode listerUtilisateurs
    }
// Fin de la classe AuthService
}