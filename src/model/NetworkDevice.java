/*
 * UTILITÉ DU FICHIER :
 * Modélise un équipement réseau supervisé (routeur, switch, serveur, point d'accès Wi-Fi)
 * avec ses informations d'identification, sa localisation, ses métriques de performance
 * (CPU, RAM, latence), son état fonctionnel et l'horodatage de son dernier heartbeat.
 *
 * FICHIERS RELIÉS :
 * - DeviceState.java : utilisé pour typer l'attribut "etat" de l'équipement.
 * - Incident.java : référence un NetworkDevice comme équipement touché par une panne.
 * - StorageService.java : sérialise et désérialise cet objet dans data/devices.json.
 * - NetMonitorService.java : gère la collection d'équipements, les seuils et le heartbeat.
 * - ConsoleMenu.java : permet d'ajouter, modifier, rechercher et afficher ces équipements.
 */

// Indique que ce fichier appartient au package (dossier) "model"
package model;

// Importe la classe LocalDateTime pour horodater le dernier signe de vie (heartbeat)
import java.time.LocalDateTime;

// Déclare la classe publique NetworkDevice représentant un équipement informatique
public class NetworkDevice {

    // Identifiant unique de l'équipement (ex : "SW-001", "RTR-01")
    private String identifiant;

    // Nom d'hôte ou hostname de la machine (ex : "SRV-WEB")
    private String nom;

    // Adresse IPv4 de l'équipement au format X.X.X.X (ex : "192.168.1.10")
    private String ip;

    // Type matériel de l'équipement (ex : "Routeur", "Switch", "Serveur", "Wi-Fi")
    private String type;

    // Emplacement physique ou géographique de l'équipement (ex : "Salle Serveurs A")
    private String localisation;

    // Taux d'utilisation du processeur CPU en pourcentage (de 0.0 à 100.0)
    private double cpu;

    // Taux d'utilisation de la mémoire vive RAM en pourcentage (de 0.0 à 100.0)
    private double ram;

    // Temps de réponse réseau (latence) exprimé en millisecondes
    private double latence;

    // Date et heure exactes du dernier message "heartbeat" reçu de l'équipement
    private LocalDateTime lastHeartbeat;

    // État fonctionnel actuel de l'équipement (UP, WARNING, DOWN ou CRITICAL)
    private DeviceState etat;

    // Constructeur simplifié à 4 paramètres (initialise la localisation par défaut)
    public NetworkDevice(String identifiant, String nom, String ip, String type) {
        // Appelle le constructeur complet en passant "Non définie" comme localisation
        this(identifiant, nom, ip, type, "Non définie");
    // Fin du constructeur simplifié
    }

    // Constructeur complet à 5 paramètres avec validation stricte des données saisies
    public NetworkDevice(String identifiant, String nom, String ip, String type, String localisation) {
        // Vérifie que l'identifiant n'est ni nul ni composé uniquement d'espaces
        if (identifiant == null || identifiant.trim().isEmpty()) {
            // Déclenche une exception si l'identifiant obligatoire est vide
            throw new IllegalArgumentException("L'identifiant de l'équipement est obligatoire.");
        // Fin de la vérification de l'identifiant
        }
        // Vérifie que le nom de la machine n'est ni nul ni vide
        if (nom == null || nom.trim().isEmpty()) {
            // Déclenche une exception si le nom obligatoire est vide
            throw new IllegalArgumentException("Le nom de l'équipement est obligatoire.");
        // Fin de la vérification du nom
        }
        // Vérifie que le type d'équipement n'est ni nul ni vide
        if (type == null || type.trim().isEmpty()) {
            // Déclenche une exception si le type obligatoire est vide
            throw new IllegalArgumentException("Le type de l'équipement est obligatoire.");
        // Fin de la vérification du type
        }
        // Vérifie que l'adresse IP respecte bien le format IPv4 X.X.X.X
        if (!estIpValide(ip)) {
            // Déclenche une exception si l'adresse IP est invalide
            throw new IllegalArgumentException("Format d'adresse IP invalide : " + ip);
        // Fin de la vérification de l'adresse IP
        }

        // Enregistre l'identifiant nettoyé des espaces superflus
        this.identifiant = identifiant.trim();
        // Enregistre le nom nettoyé des espaces superflus
        this.nom = nom.trim();
        // Enregistre l'adresse IP validée
        this.ip = ip.trim();
        // Enregistre le type nettoyé des espaces superflus
        this.type = type.trim();
        // Enregistre la localisation (ou "Non définie" si le paramètre est vide)
        this.localisation = (localisation == null || localisation.trim().isEmpty()) ? "Non définie" : localisation.trim();
        // Initialise l'utilisation CPU à 0.0 % au démarrage
        this.cpu = 0.0;
        // Initialise l'utilisation RAM à 0.0 % au démarrage
        this.ram = 0.0;
        // Initialise la latence à 0.0 ms au démarrage
        this.latence = 0.0;
        // Enregistre l'heure actuelle comme premier contact (heartbeat)
        this.lastHeartbeat = LocalDateTime.now();
        // Place l'équipement dans l'état normal UP par défaut
        this.etat = DeviceState.UP;
    // Fin du constructeur complet
    }

    // Méthode utilitaire statique vérifiant qu'une chaîne est une adresse IPv4 valide
    public static boolean estIpValide(String ip) {
        // Retourne faux immédiatement si la chaîne est nulle
        if (ip == null) {
            // Indique que l'IP n'est pas valide
            return false;
        // Fin du test de nullité
        }
        // Vérifie que la chaîne correspond à 4 nombres séparés par des points (0-255)
        return ip.trim().matches("^((25[0-5]|2[0-4]\\d|[01]?\\d\\d?)\\.){3}(25[0-5]|2[0-4]\\d|[01]?\\d\\d?)$");
    // Fin de la méthode estIpValide
    }

    // Getter retournant l'identifiant unique de l'équipement
    public String getIdentifiant() {
        // Renvoie la valeur de l'attribut identifiant
        return identifiant;
    // Fin du getter getIdentifiant
    }

    // Getter retournant le nom d'hôte de l'équipement
    public String getNom() {
        // Renvoie la valeur de l'attribut nom
        return nom;
    // Fin du getter getNom
    }

    // Setter permettant de modifier le nom de l'équipement
    public void setNom(String nom) {
        // Vérifie que le nouveau nom n'est pas vide avant modification
        if (nom == null || nom.trim().isEmpty()) {
            // Lève une exception si le nouveau nom est vide
            throw new IllegalArgumentException("Le nom ne peut pas être vide.");
        // Fin de la vérification
        }
        // Met à jour l'attribut nom
        this.nom = nom.trim();
    // Fin du setter setNom
    }

    // Getter retournant l'adresse IP de l'équipement
    public String getIp() {
        // Renvoie la valeur de l'attribut ip
        return ip;
    // Fin du getter getIp
    }

    // Setter permettant de modifier l'adresse IP avec contrôle de format
    public void setIp(String ip) {
        // Vérifie que la nouvelle adresse IP respecte le format IPv4
        if (!estIpValide(ip)) {
            // Lève une exception si le format est incorrect
            throw new IllegalArgumentException("Format d'adresse IP invalide : " + ip);
        // Fin de la vérification
        }
        // Met à jour l'attribut ip
        this.ip = ip.trim();
    // Fin du setter setIp
    }

    // Getter retournant le type d'équipement
    public String getType() {
        // Renvoie la valeur de l'attribut type
        return type;
    // Fin du getter getType
    }

    // Setter permettant de modifier le type d'équipement
    public void setType(String type) {
        // Vérifie que le nouveau type n'est pas vide
        if (type == null || type.trim().isEmpty()) {
            // Lève une exception si le type est vide
            throw new IllegalArgumentException("Le type ne peut pas être vide.");
        // Fin de la vérification
        }
        // Met à jour l'attribut type
        this.type = type.trim();
    // Fin du setter setType
    }

    // Getter retournant la localisation physique de l'équipement
    public String getLocalisation() {
        // Renvoie la valeur de l'attribut localisation
        return localisation;
    // Fin du getter getLocalisation
    }

    // Setter permettant de modifier la localisation de l'équipement
    public void setLocalisation(String localisation) {
        // Met à jour la localisation (ou remet "Non définie" si vide)
        this.localisation = (localisation == null || localisation.trim().isEmpty()) ? "Non définie" : localisation.trim();
    // Fin du setter setLocalisation
    }

    // Getter retournant le pourcentage d'utilisation CPU
    public double getCpu() {
        // Renvoie la valeur de l'attribut cpu
        return cpu;
    // Fin du getter getCpu
    }

    // Setter permettant de mettre à jour l'utilisation CPU
    public void setCpu(double cpu) {
        // Met à jour l'attribut cpu
        this.cpu = cpu;
    // Fin du setter setCpu
    }

    // Getter retournant le pourcentage d'utilisation RAM
    public double getRam() {
        // Renvoie la valeur de l'attribut ram
        return ram;
    // Fin du getter getRam
    }

    // Setter permettant de mettre à jour l'utilisation RAM
    public void setRam(double ram) {
        // Met à jour l'attribut ram
        this.ram = ram;
    // Fin du setter setRam
    }

    // Getter retournant la latence réseau en millisecondes
    public double getLatence() {
        // Renvoie la valeur de l'attribut latence
        return latence;
    // Fin du getter getLatence
    }

    // Setter permettant de mettre à jour la latence réseau
    public void setLatence(double latence) {
        // Met à jour l'attribut latence
        this.latence = latence;
    // Fin du setter setLatence
    }

    // Getter retournant l'horodatage du dernier heartbeat reçu
    public LocalDateTime getLastHeartbeat() {
        // Renvoie la valeur de l'attribut lastHeartbeat
        return lastHeartbeat;
    // Fin du getter getLastHeartbeat
    }

    // Setter permettant de restaurer un horodatage de heartbeat précis (utile au chargement)
    public void setLastHeartbeat(LocalDateTime lastHeartbeat) {
        // Met à jour l'attribut lastHeartbeat
        this.lastHeartbeat = lastHeartbeat;
    // Fin du setter setLastHeartbeat
    }

    // Méthode actualisant le heartbeat à la date et l'heure courantes
    public void updateHeartbeat() {
        // Remplace lastHeartbeat par l'instant présent
        this.lastHeartbeat = LocalDateTime.now();
    // Fin de la méthode updateHeartbeat
    }

    // Getter retournant l'état fonctionnel actuel de l'équipement
    public DeviceState getEtat() {
        // Renvoie la valeur de l'attribut etat
        return etat;
    // Fin du getter getEtat
    }

    // Setter permettant de modifier l'état fonctionnel de l'équipement
    public void setEtat(DeviceState etat) {
        // Met à jour l'attribut etat
        this.etat = etat;
    // Fin du setter setEtat
    }

    // Redéfinition de la méthode toString pour formater l'affichage d'un équipement
    @Override
    public String toString() {
        // Construit et retourne une ligne lisible résumant toutes les données de l'équipement
        return String.format("[%s] %s (%s) | IP: %s | Lieu: %s | État: %s | CPU: %.1f%% | RAM: %.1f%% | Latence: %.1f ms",
                identifiant, nom, type, ip, localisation, etat, cpu, ram, latence);
    // Fin de la méthode toString
    }
// Fin de la classe NetworkDevice
}