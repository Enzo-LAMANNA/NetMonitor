/*
 * UTILITÉ DU FICHIER :
 * Modélise un événement horodaté (journalisation / log) survenu dans NetMonitor,
 * comme l'ajout d'un équipement, un changement d'état ou le déclenchement d'une alerte.
 *
 * FICHIERS RELIÉS :
 * - NetMonitorService.java : crée et stocke chronologiquement les objets Event dans un journal.
 * - ConsoleMenu.java : permet de consulter l'historique des événements enregistrés.
 */

// Indique que ce fichier appartient au package (dossier) "model"
package model;

// Importe la classe LocalDateTime pour dater précisément chaque événement
import java.time.LocalDateTime;
// Importe DateTimeFormatter pour afficher la date et l'heure sous un format lisible
import java.time.format.DateTimeFormatter;

// Déclare la classe publique Event représentant une entrée du journal d'événements
public class Event {

    // Identifiant unique de l'événement (ex : "EVT-1", "EVT-2")
    private String identifiant;

    // Description textuelle de l'action ou de l'anomalie survenue
    private String description;

    // Date et heure exactes auxquelles l'événement s'est produit
    private LocalDateTime timestamp;

    // Constructeur initialisant un événement avec validation des champs obligatoires
    public Event(String identifiant, String description) {
        // Vérifie que l'identifiant de l'événement n'est ni nul ni vide
        if (identifiant == null || identifiant.trim().isEmpty()) {
            // Déclenche une exception si l'identifiant obligatoire est manquant
            throw new IllegalArgumentException("L'identifiant de l'événement est obligatoire.");
        // Fin de la vérification de l'identifiant
        }
        // Vérifie que la description de l'événement n'est ni nulle ni vide
        if (description == null || description.trim().isEmpty()) {
            // Déclenche une exception si la description obligatoire est manquante
            throw new IllegalArgumentException("La description de l'événement est obligatoire.");
        // Fin de la vérification de la description
        }

        // Enregistre l'identifiant nettoyé des espaces superflus
        this.identifiant = identifiant.trim();
        // Enregistre la description nettoyée des espaces superflus
        this.description = description.trim();
        // Capture automatiquement la date et l'heure actuelles du système
        this.timestamp = LocalDateTime.now();
    // Fin du constructeur Event
    }

    // Getter retournant l'identifiant unique de l'événement
    public String getIdentifiant() {
        // Renvoie la valeur de l'attribut identifiant
        return identifiant;
    // Fin du getter getIdentifiant
    }

    // Getter retournant la description textuelle de l'événement
    public String getDescription() {
        // Renvoie la valeur de l'attribut description
        return description;
    // Fin du getter getDescription
    }

    // Getter retournant l'horodatage exact de l'événement
    public LocalDateTime getTimestamp() {
        // Renvoie la valeur de l'attribut timestamp
        return timestamp;
    // Fin du getter getTimestamp
    }

    // Redéfinition de la méthode toString pour formater l'événement comme dans un fichier log
    @Override
    public String toString() {
        // Définit un format d'affichage lisible pour l'horodatage (ex : 14:32:17)
        DateTimeFormatter formatHeure = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");
        // Retourne la ligne formatée associant l'ID, l'heure et la description
        return String.format("[%s] %s - %s", identifiant, timestamp.format(formatHeure), description);
    // Fin de la méthode toString
    }
// Fin de la classe Event
}