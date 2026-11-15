/*
 * UTILITÉ DU FICHIER :
 * Cœur métier de NetMonitor : gère le parc d'équipements réseau (ajout, modification,
 * suppression, recherche, filtrage), le contrôle des seuils (CPU/RAM/latence) et du
 * heartbeat, la génération et le suivi des incidents, le journal d'événements et les
 * statistiques du tableau de bord.
 *
 * FICHIERS RELIÉS :
 * - NetworkDevice.java & DeviceState.java : gère les équipements et leurs états.
 * - Incident.java & IncidentState.java & Severity.java : crée et met à jour les tickets.
 * - Event.java : enregistre chronologiquement chaque action et anomalie détectée.
 * - User.java : associe un technicien lors de la prise en charge d'un incident.
 * - StorageService.java : sauvegarde automatiquement les équipements dans data/devices.json.
 * - ConsoleMenu.java : appelle toutes ces méthodes métier depuis l'interface terminal.
 */

// Indique que ce fichier appartient au package (dossier) "service"
package service;

// Importe Duration pour calculer le temps écoulé depuis le dernier heartbeat
import java.time.Duration;
// Importe LocalDateTime pour comparer l'heure actuelle au dernier heartbeat
import java.time.LocalDateTime;
// Importe ArrayList pour stocker les listes dynamiques en mémoire
import java.util.ArrayList;
// Importe HashMap pour mémoriser le compteur de dépassements consécutifs par équipement
import java.util.HashMap;
// Importe List pour typer les collections retournées
import java.util.List;
// Importe Map pour associer chaque identifiant d'équipement à son compteur d'alerte
import java.util.Map;
// Importe toutes les classes du package model
import model.DeviceState;
import model.Event;
import model.Incident;
import model.IncidentState;
import model.NetworkDevice;
import model.Severity;
import model.User;

// Déclare la classe publique NetMonitorService regroupant la logique métier de supervision
public class NetMonitorService {

    // Référence vers le service de persistance JSON des équipements
    private final StorageService storageService;

    // Liste dynamique en mémoire de tous les équipements supervisés
    private final List<NetworkDevice> equipements;

    // Liste dynamique en mémoire de tous les incidents détectés
    private final List<Incident> incidents;

    // Liste chronologique de tous les événements (logs) survenus dans l'application
    private final List<Event> evenements;

    // Compteur de mesures consécutives en dépassement de seuil par identifiant d'équipement
    private final Map<String, Integer> compteursDepassement;

    // Seuil d'alerte CPU en pourcentage (configurable par l'administrateur, 90% par défaut)
    private double seuilCpu;

    // Seuil d'alerte RAM en pourcentage (configurable par l'administrateur, 90% par défaut)
    private double seuilRam;

    // Nombre N de mesures consécutives requises avant de passer en WARNING (2 par défaut)
    private int mesuresConsecutivesRequises;

    // Délai maximum en secondes sans heartbeat avant de déclarer un équipement DOWN (30s par défaut)
    private long delaiHeartbeatSecondes;

    // Compteur interne servant à générer automatiquement les IDs d'incidents (INC-1, INC-2...)
    private int prochainNumeroIncident;

    // Compteur interne servant à générer automatiquement les IDs d'événements (EVT-1, EVT-2...)
    private int prochainNumeroEvenement;

    // Constructeur initialisant le service métier et rechargeant les équipements sauvegardés
    public NetMonitorService(StorageService storageService) {
        // Enregistre la référence vers le service de persistance
        this.storageService = storageService;
        // Charge les équipements existants depuis data/devices.json
        this.equipements = new ArrayList<>(storageService.chargerEquipements());
        // Initialise la liste des incidents vide au démarrage
        this.incidents = new ArrayList<>();
        // Initialise la liste des événements vide au démarrage
        this.evenements = new ArrayList<>();
        // Initialise la map des compteurs de dépassement de seuil
        this.compteursDepassement = new HashMap<>();
        // Définit le seuil CPU par défaut à 90.0 %
        this.seuilCpu = 90.0;
        // Définit le seuil RAM par défaut à 90.0 %
        this.seuilRam = 90.0;
        // Définit le nombre de mesures consécutives à 2 par défaut
        this.mesuresConsecutivesRequises = 2;
        // Définit le timeout de heartbeat à 30 secondes par défaut
        this.delaiHeartbeatSecondes = 30;
        // Initialise la numérotation des incidents à 1
        this.prochainNumeroIncident = 1;
        // Initialise la numérotation des événements à 1
        this.prochainNumeroEvenement = 1;
        // Enregistre un événement de démarrage du moteur de supervision
        enregistrerEvenement("Démarrage du service NetMonitor (" + equipements.size() + " équipement(s) chargé(s)).");
    // Fin du constructeur NetMonitorService
    }

    // Méthode privée ajoutant une nouvelle entrée horodatée dans le journal des événements
    private void enregistrerEvenement(String description) {
        // Génère un identifiant unique séquentiel (ex : "EVT-1")
        String idEvt = "EVT-" + prochainNumeroEvenement++;
        // Ajoute le nouvel événement à la liste chronologique
        evenements.add(new Event(idEvt, description));
    // Fin de la méthode enregistrerEvenement
    }

    // Méthode privée créant un incident uniquement s'il n'en existe pas déjà un ouvert sur cet équipement
    private void creerIncidentSiAbsent(NetworkDevice device, Severity gravite, String description) {
        // Parcourt les incidents existants pour éviter de créer des doublons sur la même panne
        for (Incident inc : incidents) {
            // Vérifie si l'incident concerne le même équipement et n'est pas encore clôturé
            if (inc.getEquipement().getIdentifiant().equalsIgnoreCase(device.getIdentifiant())
                    && inc.getEtat() != IncidentState.CLOSED) {
                // Sort immédiatement car un ticket actif existe déjà pour cet équipement
                return;
            // Fin du test d'incident actif
            }
        // Fin de la boucle de vérification
        }
        // Génère l'identifiant unique du nouvel incident (ex : "INC-1")
        String idInc = "INC-" + prochainNumeroIncident++;
        // Instancie le nouvel incident à l'état OPEN
        Incident nouvelIncident = new Incident(idInc, description, gravite, device);
        // Ajoute l'incident à la liste globale
        incidents.add(nouvelIncident);
        // Journalise la création automatique de l'incident
        enregistrerEvenement("INCIDENT OUVERT [" + idInc + "] sur " + device.getNom() + " (" + gravite + ") : " + description);
    // Fin de la méthode creerIncidentSiAbsent
    }

    // Ajoute un nouvel équipement au parc, journalise l'action et sauvegarde en JSON
    public NetworkDevice ajouterEquipement(String identifiant, String nom, String ip, String type, String localisation) {
        // Vérifie qu'aucun équipement ne possède déjà cet identifiant
        if (rechercherParIdentifiant(identifiant) != null) {
            // Lève une exception en cas de doublon d'identifiant
            throw new IllegalArgumentException("Un équipement avec l'identifiant '" + identifiant + "' existe déjà.");
        // Fin de la vérification d'identifiant
        }
        // Instancie l'équipement (valide le format IP et les champs obligatoires)
        NetworkDevice device = new NetworkDevice(identifiant, nom, ip, type, localisation);
        // Ajoute l'équipement à la liste en mémoire
        equipements.add(device);
        // Initialise son compteur de dépassement de seuil à 0
        compteursDepassement.put(device.getIdentifiant(), 0);
        // Sauvegarde immédiatement la liste mise à jour dans data/devices.json
        storageService.sauvegarderEquipements(equipements);
        // Journalise l'ajout de l'équipement
        enregistrerEvenement("Ajout de l'équipement " + device.getNom() + " [" + device.getIdentifiant() + "] (" + device.getIp() + ").");
        // Retourne l'équipement créé
        return device;
    // Fin de la méthode ajouterEquipement
    }

    // Modifie les informations principales d'un équipement existant et sauvegarde en JSON
    public boolean modifierEquipement(String identifiant, String nouveauNom, String nouvelleIp, String nouveauType, String nouvelleLocalisation) {
        // Recherche l'équipement cible par son identifiant
        NetworkDevice device = rechercherParIdentifiant(identifiant);
        // Retourne false si l'équipement est introuvable
        if (device == null) {
            // Indique que la modification n'a pas pu avoir lieu
            return false;
        // Fin du test d'existence
        }
        // Met à jour le nom si une nouvelle valeur non vide est fournie
        if (nouveauNom != null && !nouveauNom.trim().isEmpty()) {
            // Applique le nouveau nom
            device.setNom(nouveauNom);
        // Fin de la mise à jour du nom
        }
        // Met à jour l'adresse IP si une nouvelle valeur non vide est fournie (avec validation IPv4)
        if (nouvelleIp != null && !nouvelleIp.trim().isEmpty()) {
            // Applique la nouvelle adresse IP
            device.setIp(nouvelleIp);
        // Fin de la mise à jour de l'IP
        }
        // Met à jour le type si une nouvelle valeur non vide est fournie
        if (nouveauType != null && !nouveauType.trim().isEmpty()) {
            // Applique le nouveau type
            device.setType(nouveauType);
        // Fin de la mise à jour du type
        }
        // Met à jour la localisation si une nouvelle valeur non vide est fournie
        if (nouvelleLocalisation != null && !nouvelleLocalisation.trim().isEmpty()) {
            // Applique la nouvelle localisation
            device.setLocalisation(nouvelleLocalisation);
        // Fin de la mise à jour de la localisation
        }
        // Sauvegarde les modifications dans data/devices.json
        storageService.sauvegarderEquipements(equipements);
        // Journalise la modification
        enregistrerEvenement("Modification de l'équipement [" + device.getIdentifiant() + "] (" + device.getNom() + ").");
        // Retourne true pour confirmer la mise à jour
        return true;
    // Fin de la méthode modifierEquipement
    }

    // Supprime un équipement par son identifiant et met à jour le fichier JSON
    public boolean supprimerEquipement(String identifiant) {
        // Recherche l'équipement cible par son identifiant
        NetworkDevice device = rechercherParIdentifiant(identifiant);
        // Retourne false si l'équipement n'existe pas
        if (device == null) {
            // Indique que la suppression a échoué
            return false;
        // Fin du test d'existence
        }
        // Retire l'équipement de la liste en mémoire
        equipements.remove(device);
        // Supprime son compteur de dépassement associé
        compteursDepassement.remove(device.getIdentifiant());
        // Sauvegarde la liste mise à jour dans data/devices.json
        storageService.sauvegarderEquipements(equipements);
        // Journalise la suppression de l'équipement
        enregistrerEvenement("Suppression de l'équipement [" + device.getIdentifiant() + "] (" + device.getNom() + ").");
        // Retourne true pour confirmer la suppression
        return true;
    // Fin de la méthode supprimerEquipement
    }

    // Recherche un équipement précis par son identifiant unique (insensible à la casse)
    public NetworkDevice rechercherParIdentifiant(String identifiant) {
        // Retourne null si le paramètre est nul
        if (identifiant == null) {
            // Renvoie null
            return null;
        // Fin du test de nullité
        }
        // Parcourt la liste des équipements
        for (NetworkDevice dev : equipements) {
            // Compare l'identifiant sans tenir compte des majuscules/minuscules
            if (dev.getIdentifiant().equalsIgnoreCase(identifiant.trim())) {
                // Retourne l'équipement trouvé
                return dev;
            // Fin de la comparaison
            }
        // Fin de la boucle
        }
        // Retourne null si aucun équipement ne correspond
        return null;
    // Fin de la méthode rechercherParIdentifiant
    }

    // Recherche rapide multi-critères : par nom, adresse IP ou localisation (partiel et insensible à la casse)
    public List<NetworkDevice> rechercherEquipements(String motCle) {
        // Prépare la liste des résultats correspondants
        List<NetworkDevice> resultats = new ArrayList<>();
        // Si le mot-clé est vide, retourne tous les équipements
        if (motCle == null || motCle.trim().isEmpty()) {
            // Renvoie la liste complète
            return new ArrayList<>(equipements);
        // Fin du test de mot-clé vide
        }
        // Convertit le mot-clé en minuscules pour une recherche insensible à la casse
        String filtre = motCle.trim().toLowerCase();
        // Parcourt tous les équipements enregistrés
        for (NetworkDevice dev : equipements) {
            // Vérifie si le nom, l'IP, l'identifiant ou la localisation contient le mot-clé
            if (dev.getNom().toLowerCase().contains(filtre)
                    || dev.getIp().toLowerCase().contains(filtre)
                    || dev.getIdentifiant().toLowerCase().contains(filtre)
                    || dev.getLocalisation().toLowerCase().contains(filtre)) {
                // Ajoute l'équipement aux résultats
                resultats.add(dev);
            // Fin du test de correspondance
            }
        // Fin de la boucle
        }
        // Retourne la liste des équipements trouvés
        return resultats;
    // Fin de la méthode rechercherEquipements
    }

    // Filtre la liste des équipements selon un état fonctionnel donné (UP, WARNING, DOWN, CRITICAL)
    public List<NetworkDevice> filtrerParEtat(DeviceState etatRecherche) {
        // Prépare la liste filtrée
        List<NetworkDevice> resultats = new ArrayList<>();
        // Parcourt tous les équipements
        for (NetworkDevice dev : equipements) {
            // Vérifie si l'état de l'équipement correspond à l'état demandé
            if (dev.getEtat() == etatRecherche) {
                // Ajoute l'équipement à la liste filtrée
                resultats.add(dev);
            // Fin du test d'état
            }
        // Fin de la boucle
        }
        // Retourne les équipements filtrés
        return resultats;
    // Fin de la méthode filtrerParEtat
    }

    // Met à jour les métriques d'un équipement, actualise son heartbeat et évalue les seuils d'alerte
    public void enregistrerMesures(String identifiant, double cpu, double ram, double latence) {
        // Recherche l'équipement concerné
        NetworkDevice device = rechercherParIdentifiant(identifiant);
        // Lève une exception si l'équipement n'existe pas
        if (device == null) {
            // Signale que l'identifiant est inconnu
            throw new IllegalArgumentException("Équipement introuvable : " + identifiant);
        // Fin du test d'existence
        }

        // Met à jour les métriques CPU, RAM et latence sur l'équipement
        device.setCpu(cpu);
        device.setRam(ram);
        device.setLatence(latence);
        // Actualise le heartbeat car l'équipement vient d'envoyer ses données
        device.updateHeartbeat();

        // Journalise la réception des mesures de supervision
        enregistrerEvenement(String.format("Mesures reçues de %s : CPU=%.1f%%, RAM=%.1f%%, Latence=%.1fms.",
                device.getNom(), cpu, ram, latence));

        // Vérifie si le CPU ou la RAM dépasse le seuil configuré par l'administrateur
        boolean seuilDepasse = (cpu > seuilCpu) || (ram > seuilRam);

        // Si un seuil est dépassé, incrémente le compteur de mesures consécutives anormales
        if (seuilDepasse) {
            // Récupère le compteur actuel (0 par défaut) et l'incrémente de 1
            int consecutifs = compteursDepassement.getOrDefault(device.getIdentifiant(), 0) + 1;
            // Enregistre la nouvelle valeur du compteur
            compteursDepassement.put(device.getIdentifiant(), consecutifs);

            // Vérifie si le nombre N de mesures consécutives requises est atteint
            if (consecutifs >= mesuresConsecutivesRequises) {
                // Passe l'état de l'équipement à WARNING
                device.setEtat(DeviceState.WARNING);
                // Génère automatiquement un incident de gravité HIGH
                creerIncidentSiAbsent(device, Severity.HIGH,
                        String.format("Dépassement de seuil persistant (CPU: %.1f%%, RAM: %.1f%%)", cpu, ram));
            // Fin du test de N mesures consécutives
            }
        } else {
            // Réinitialise le compteur à 0 dès que les mesures reviennent sous les seuils
            compteursDepassement.put(device.getIdentifiant(), 0);
            // Si l'équipement était en WARNING ou DOWN, il repasse à l'état normal UP
            if (device.getEtat() != DeviceState.UP) {
                // Rétablit l'état UP
                device.setEtat(DeviceState.UP);
                // Journalise le retour à la normale
                enregistrerEvenement("Retour à la normale (UP) pour l'équipement " + device.getNom() + ".");
            // Fin du test de retour à la normale
            }
        // Fin du bloc d'évaluation des seuils
        }

        // Sauvegarde le nouvel état et les métriques dans data/devices.json
        storageService.sauvegarderEquipements(equipements);
    // Fin de la méthode enregistrerMesures
    }

    // Vérifie tous les équipements pour détecter une rupture de heartbeat supérieure au délai configuré
    public int verifierHeartbeats() {
        // Compte le nombre d'équipements nouvellement passés en DOWN lors de cette vérification
        int nouveauxDown = 0;
        // Récupère l'heure actuelle du système
        LocalDateTime maintenant = LocalDateTime.now();

        // Parcourt l'ensemble du parc d'équipements
        for (NetworkDevice dev : equipements) {
            // Calcule le nombre de secondes écoulées depuis le dernier contact de l'équipement
            long secondesSansContact = Duration.between(dev.getLastHeartbeat(), maintenant).getSeconds();
            // Vérifie si le délai maximal est dépassé et que l'équipement n'est pas déjà DOWN
            if (secondesSansContact > delaiHeartbeatSecondes && dev.getEtat() != DeviceState.DOWN) {
                // Passe automatiquement l'état de l'équipement à DOWN
                dev.setEtat(DeviceState.DOWN);
                // Incrémente le compteur d'équipements tombés en panne
                nouveauxDown++;
                // Journalise la rupture de heartbeat
                enregistrerEvenement("ALERTE HEARTBEAT : " + dev.getNom() + " injoignable depuis " + secondesSansContact + "s -> DOWN.");
                // Génère automatiquement un incident CRITICAL
                creerIncidentSiAbsent(dev, Severity.CRITICAL,
                        "Rupture de heartbeat (aucun contact depuis " + secondesSansContact + " secondes)");
            // Fin du test de rupture de heartbeat
            }
        // Fin de la boucle sur les équipements
        }

        // Sauvegarde dans data/devices.json si au moins un équipement a changé d'état
        if (nouveauxDown > 0) {
            // Met à jour le fichier JSON des équipements
            storageService.sauvegarderEquipements(equipements);
        // Fin du test de sauvegarde
        }
        // Retourne le nombre d'équipements détectés indisponibles
        return nouveauxDown;
    // Fin de la méthode verifierHeartbeats
    }

    // Permet à un technicien ou administrateur de prendre en charge un incident ouvert
    public boolean prendreEnChargeIncident(String idIncident, User technicien) {
        // Recherche l'incident par