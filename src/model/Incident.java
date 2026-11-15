/*
 * UTILITÉ DU FICHIER :
 * Modélise un incident réseau détecté sur un équipement supervisé (ex : serveur inaccessible
 * ou surcharge CPU), en gérant son niveau de gravité, son cycle de vie (OPEN, IN_PROGRESS,
 * CLOSED), ses horodatages et le technicien qui le prend en charge.
 *
 * FICHIERS RELIÉS :
 * - NetworkDevice.java : identifie l'équipement réseau touché par l'incident.
 * - Severity.java : qualifie la criticité de l'incident (LOW, MEDIUM, HIGH, CRITICAL).
 * - IncidentState.java : suit l'avancement du traitement (OPEN, IN_PROGRESS, CLOSED).
 * - User.java : représente le technicien assigné à la résolution de l'incident.
 * - NetMonitorService.java : crée automatiquement les incidents lors des alertes.
 * - ConsoleMenu.java : permet de consulter, prendre en charge et clôturer les incidents.
 */

// Indique que ce fichier appartient au package (dossier) "model"
package model;

// Importe la classe LocalDateTime pour enregistrer les dates de création et de clôture
import java.time.LocalDateTime;

// Déclare la classe publique Incident représentant une panne ou anomalie à traiter
public class Incident {

    // Identifiant unique de l'incident (ex : "INC-1", "INC-42")
    private String identifiant;

    // Description détaillée du problème détecté sur le réseau
    private String description;

    // Niveau de gravité de l'incident (LOW, MEDIUM, HIGH ou CRITICAL)
    private Severity gravite;

    // État actuel de l'incident dans son cycle de vie (OPEN, IN_PROGRESS ou CLOSED)
    private IncidentState etat;

    // Référence vers l'équipement réseau concerné par cet incident
    private NetworkDevice equipement;

    // Référence vers l'utilisateur (technicien/admin) assigné à la résolution (null au départ)
    private User technicienAssigne;

    // Date et heure exactes de création de l'incident
    private LocalDateTime dateCreation;

    // Date et heure exactes de clôture de l'incident (null tant qu'il n'est pas résolu)
    private LocalDateTime dateCloture;

    // Constructeur complet initialisant un nouvel incident à l'état OPEN
    public Incident(String identifiant, String description, Severity gravite, NetworkDevice equipement) {
        // Vérifie que l'identifiant de l'incident n'est ni nul ni vide
        if (identifiant == null || identifiant.trim().isEmpty()) {
            // Déclenche une exception si l'identifiant obligatoire est manquant
            throw new IllegalArgumentException("L'identifiant de l'incident est obligatoire.");
        // Fin de la vérification de l'identifiant
        }
        // Vérifie que la description du problème n'est ni nulle ni vide
        if (description == null || description.trim().isEmpty()) {
            // Déclenche une exception si la description obligatoire est manquante
            throw new IllegalArgumentException("La description de l'incident est obligatoire.");
        // Fin de la vérification de la description
        }
        // Vérifie qu'un niveau de gravité valide a bien été fourni
        if (gravite == null) {
            // Déclenche une exception si la gravité est nulle
            throw new IllegalArgumentException("La gravité de l'incident est obligatoire.");
        // Fin de la vérification de la gravité
        }
        // Vérifie que l'incident est bien rattaché à un équipement existant
        if (equipement == null) {
            // Déclenche une exception si aucun équipement n'est associé
            throw new IllegalArgumentException("Un incident doit être associé à un équipement.");
        // Fin de la vérification de l'équipement
        }

        // Enregistre l'identifiant nettoyé des espaces superflus
        this.identifiant = identifiant.trim();
        // Enregistre la description nettoyée des espaces superflus
        this.description = description.trim();
        // Enregistre le niveau de gravité
        this.gravite = gravite;
        // Rattache l'équipement concerné à l'incident
        this.equipement = equipement;
        // Place automatiquement tout nouvel incident à l'étape 1 : OPEN
        this.etat = IncidentState.OPEN;
        // Initialise le technicien assigné à null (personne n'a encore pris le ticket)
        this.technicienAssigne = null;
        // Capture la date et l'heure actuelles comme moment de création
        this.dateCreation = LocalDateTime.now();
        // Laisse la date de clôture à null tant que l'incident n'est pas fermé
        this.dateCloture = null;
    // Fin du constructeur Incident
    }

    // Getter retournant l'identifiant unique de l'incident
    public String getIdentifiant() {
        // Renvoie la valeur de l'attribut identifiant
        return identifiant;
    // Fin du getter getIdentifiant
    }

    // Getter retournant la description du problème
    public String getDescription() {
        // Renvoie la valeur de l'attribut description
        return description;
    // Fin du getter getDescription
    }

    // Getter retournant le niveau de gravité de l'incident
    public Severity getGravite() {
        // Renvoie la valeur de l'attribut gravite
        return gravite;
    // Fin du getter getGravite
    }

    // Getter retournant l'état actuel de l'incident (OPEN, IN_PROGRESS, CLOSED)
    public IncidentState getEtat() {
        // Renvoie la valeur de l'attribut etat
        return etat;
    // Fin du getter getEtat
    }

    // Getter retournant l'équipement réseau touché par l'incident
    public NetworkDevice getEquipement() {
        // Renvoie la référence vers l'objet NetworkDevice
        return equipement;
    // Fin du getter getEquipement
    }

    // Getter retournant le technicien actuellement assigné à l'incident
    public User getTechnicienAssigne() {
        // Renvoie la référence vers l'objet User (ou null si non assigné)
        return technicienAssigne;
    // Fin du getter getTechnicienAssigne
    }

    // Getter retournant la date et l'heure d'ouverture de l'incident
    public LocalDateTime getDateCreation() {
        // Renvoie la valeur de l'attribut dateCreation
        return dateCreation;
    // Fin du getter getDateCreation
    }

    // Getter retournant la date et l'heure de clôture de l'incident
    public LocalDateTime getDateCloture() {
        // Renvoie la valeur de l'attribut dateCloture
        return dateCloture;
    // Fin du getter getDateCloture
    }

    // Méthode permettant à un technicien de prendre en charge l'incident (Étape 2)
    public void prendreEnCharge(User technicien) {
        // Vérifie qu'un utilisateur valide est fourni pour la prise en charge
        if (technicien == null) {
            // Lève une exception si aucun utilisateur n'est passé en paramètre
            throw new IllegalArgumentException("Un technicien valide est requis pour prendre en charge l'incident.");
        // Fin de la vérification du technicien
        }
        // Vérifie que l'incident n'est pas déjà fermé avant de le prendre en charge
        if (this.etat == IncidentState.CLOSED) {
            // Lève une exception car on ne réouvre pas un incident clôturé
            throw new IllegalStateException("Impossible de prendre en charge un incident déjà clôturé.");
        // Fin de la vérification d'état
        }
        // Associe le technicien à cet incident
        this.technicienAssigne = technicien;
        // Fait passer l'état de l'incident à IN_PROGRESS
        this.etat = IncidentState.IN_PROGRESS;
    // Fin de la méthode prendreEnCharge
    }

    // Méthode permettant de marquer l'incident comme résolu et fermé (Étape 3)
    public void cloturer() {
        // Vérifie que l'incident n'est pas déjà clôturé
        if (this.etat == IncidentState.CLOSED) {
            // Lève une exception pour éviter une double clôture
            throw new IllegalStateException("Cet incident est déjà clôturé.");
        // Fin de la vérification d'état
        }
        // Fait passer l'état de l'incident à CLOSED
        this.etat = IncidentState.CLOSED;
        // Enregistre l'horodatage exact de la résolution
        this.dateCloture = LocalDateTime.now();
    // Fin de la méthode cloturer
    }

    // Redéfinition de la méthode toString pour afficher un résumé clair de l'incident
    @Override
    public String toString() {
        // Récupère le nom du technicien assigné ou affiche "Non assigné" s'il est null
        String nomTechnicien = (technicienAssigne != null) ? technicienAssigne.getNom() : "Non assigné";
        // Construit et retourne la ligne formatée décrivant l'incident
        return String.format("[%s] Équipement: %s | Gravité: %s | État: %s | Tech: %s | Problème: %s",
                identifiant, equipement.getNom(), gravite, etat, nomTechnicien, description);
    // Fin de la méthode toString
    }
// Fin de la classe Incident
}