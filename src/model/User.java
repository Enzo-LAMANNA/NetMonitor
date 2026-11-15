/*
 * UTILITÉ DU FICHIER :
 * Modélise un compte utilisateur de NetMonitor avec son identifiant, son nom complet,
 * son login de connexion, son mot de passe et son rôle (ADMIN ou TECHNICIEN).
 *
 * FICHIERS RELIÉS :
 * - UserRole.java : utilisé pour typer l'attribut "role" et définir les droits d'accès.
 * - Incident.java : référence un User comme technicien assigné à la résolution d'une panne.
 * - StorageService.java : sauvegarde et charge dynamiquement les utilisateurs dans data/users.json.
 * - AuthService.java : gère la création, la suppression et l'authentification des objets User.
 * - ConsoleMenu.java : permet à l'administrateur de créer ou supprimer des utilisateurs.
 */

// Indique que ce fichier appartient au package (dossier) "model"
package model;

// Déclare la classe publique User représentant un utilisateur de l'application
public class User {

    // Identifiant unique de l'utilisateur (ex : "USR-01")
    private String identifiant;

    // Nom complet affiché de l'utilisateur (ex : "Alice Martin")
    private String nom;

    // Identifiant de connexion saisi au formulaire d'authentification (ex : "alice")
    private String login;

    // Mot de passe associé au compte pour vérifier l'authentification
    private String motDePasse;

    // Rôle définissant les permissions de l'utilisateur (ADMIN ou TECHNICIEN)
    private UserRole role;

    // Constructeur complet initialisant un utilisateur avec validation des champs obligatoires
    public User(String identifiant, String nom, String login, String motDePasse, UserRole role) {
        // Vérifie que l'identifiant n'est ni nul ni vide
        if (identifiant == null || identifiant.trim().isEmpty()) {
            // Déclenche une exception si l'identifiant obligatoire est manquant
            throw new IllegalArgumentException("L'identifiant de l'utilisateur est obligatoire.");
        // Fin de la vérification de l'identifiant
        }
        // Vérifie que le nom complet n'est ni nul ni vide
        if (nom == null || nom.trim().isEmpty()) {
            // Déclenche une exception si le nom obligatoire est manquant
            throw new IllegalArgumentException("Le nom de l'utilisateur est obligatoire.");
        // Fin de la vérification du nom
        }
        // Vérifie que le login de connexion n'est ni nul ni vide
        if (login == null || login.trim().isEmpty()) {
            // Déclenche une exception si le login obligatoire est manquant
            throw new IllegalArgumentException("Le login est obligatoire.");
        // Fin de la vérification du login
        }
        // Vérifie que le mot de passe n'est ni nul ni vide
        if (motDePasse == null || motDePasse.trim().isEmpty()) {
            // Déclenche une exception si le mot de passe obligatoire est manquant
            throw new IllegalArgumentException("Le mot de passe est obligatoire.");
        // Fin de la vérification du mot de passe
        }
        // Vérifie qu'un rôle valide (ADMIN ou TECHNICIEN) a bien été fourni
        if (role == null) {
            // Déclenche une exception si aucun rôle n'est attribué
            throw new IllegalArgumentException("Le rôle de l'utilisateur est obligatoire.");
        // Fin de la vérification du rôle
        }

        // Enregistre l'identifiant nettoyé des espaces superflus
        this.identifiant = identifiant.trim();
        // Enregistre le nom complet nettoyé des espaces superflus
        this.nom = nom.trim();
        // Enregistre le login nettoyé des espaces superflus
        this.login = login.trim();
        // Enregistre le mot de passe tel quel
        this.motDePasse = motDePasse;
        // Enregistre le rôle attribué à l'utilisateur
        this.role = role;
    // Fin du constructeur User
    }

    // Getter retournant l'identifiant unique de l'utilisateur
    public String getIdentifiant() {
        // Renvoie la valeur de l'attribut identifiant
        return identifiant;
    // Fin du getter getIdentifiant
    }

    // Getter retournant le nom complet de l'utilisateur
    public String getNom() {
        // Renvoie la valeur de l'attribut nom
        return nom;
    // Fin du getter getNom
    }

    // Setter permettant de modifier le nom complet de l'utilisateur
    public void setNom(String nom) {
        // Vérifie que le nouveau nom n'est pas vide
        if (nom == null || nom.trim().isEmpty()) {
            // Lève une exception si le nouveau nom est vide
            throw new IllegalArgumentException("Le nom ne peut pas être vide.");
        // Fin de la vérification
        }
        // Met à jour l'attribut nom
        this.nom = nom.trim();
    // Fin du setter setNom
    }

    // Getter retournant le login de connexion de l'utilisateur
    public String getLogin() {
        // Renvoie la valeur de l'attribut login
        return login;
    // Fin du getter getLogin
    }

    // Getter retournant le mot de passe (utilisé par StorageService pour la sauvegarde)
    public String getMotDePasse() {
        // Renvoie la valeur de l'attribut motDePasse
        return motDePasse;
    // Fin du getter getMotDePasse
    }

    // Setter permettant de modifier le mot de passe de l'utilisateur
    public void setMotDePasse(String motDePasse) {
        // Vérifie que le nouveau mot de passe n'est pas vide
        if (motDePasse == null || motDePasse.trim().isEmpty()) {
            // Lève une exception si le nouveau mot de passe est vide
            throw new IllegalArgumentException("Le mot de passe ne peut pas être vide.");
        // Fin de la vérification
        }
        // Met à jour l'attribut motDePasse
        this.motDePasse = motDePasse;
    // Fin du setter setMotDePasse
    }

    // Getter retournant le rôle de l'utilisateur (ADMIN ou TECHNICIEN)
    public UserRole getRole() {
        // Renvoie la valeur de l'attribut role
        return role;
    // Fin du getter getRole
    }

    // Setter permettant de modifier le rôle de l'utilisateur
    public void setRole(UserRole role) {
        // Vérifie que le nouveau rôle n'est pas nul
        if (role == null) {
            // Lève une exception si le rôle est nul
            throw new IllegalArgumentException("Le rôle ne peut pas être nul.");
        // Fin de la vérification
        }
        // Met à jour l'attribut role
        this.role = role;
    // Fin du setter setRole
    }

    // Méthode vérifiant si le mot de passe saisi correspond à celui du compte
    public boolean verifierMotDePasse(String motDePasseSaisi) {
        // Retourne faux si aucune saisie n'a été fournie
        if (motDePasseSaisi == null) {
            // Indique que la vérification échoue
            return false;
        // Fin du test de nullité
        }
        // Compare exactement le mot de passe enregistré avec celui saisi
        return this.motDePasse.equals(motDePasseSaisi);
    // Fin de la méthode verifierMotDePasse
    }

    // Redéfinition de la méthode toString pour afficher le compte sans exposer le mot de passe
    @Override
    public String toString() {
        // Construit et retourne une chaîne lisible avec l'ID, le nom, le login et le rôle
        return String.format("[%s] %s (Login: %s) | Rôle: %s", identifiant, nom, login, role);
    // Fin de la méthode toString
    }
// Fin de la classe User
}