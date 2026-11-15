/*
 * UTILITÉ DU FICHIER :
 * Gère toute l'interface utilisateur en ligne de commande (CLI) de NetMonitor :
 * création dynamique du premier administrateur si aucun compte n'existe, écran de
 * connexion, affichage des menus adaptés aux droits (ADMIN ou TECHNICIEN) et exécution
 * de toutes les actions métier dans le terminal.
 *
 * FICHIERS RELIÉS :
 * - AuthService.java : gère l'authentification, les rôles et la création/suppression de comptes.
 * - NetMonitorService.java : exécute les actions sur les équipements, mesures et incidents.
 * - NetworkDevice.java & DeviceState.java : utilisés pour afficher et filtrer les équipements.
 * - Incident.java & User.java & UserRole.java & Event.java : manipulés dans les sous-menus.
 * - Main.java : instancie ConsoleMenu et appelle sa méthode demarrer().
 */

// Indique que ce fichier appartient au package (dossier) "cli"
package cli;

// Importe List pour manipuler les listes retournées par les services
import java.util.List;
// Importe Scanner pour lire les saisies clavier de l'utilisateur dans le terminal
import java.util.Scanner;
// Importe les classes du package model
import model.DeviceState;
import model.Event;
import model.Incident;
import model.NetworkDevice;
import model.User;
import model.UserRole;
// Importe les services métier
import service.AuthService;
import service.NetMonitorService;

// Déclare la classe publique ConsoleMenu représentant l'interface console
public class ConsoleMenu {

    // Référence vers le service d'authentification et de gestion dynamique des comptes
    private final AuthService authService;

    // Référence vers le moteur métier de supervision réseau
    private final NetMonitorService monitorService;

    // Scanner unique pour lire toutes les entrées clavier de l'utilisateur
    private final Scanner scanner;

    // Constructeur recevant les deux services indispensables au fonctionnement du menu
    public ConsoleMenu(AuthService authService, NetMonitorService monitorService) {
        // Enregistre la référence vers AuthService
        this.authService = authService;
        // Enregistre la référence vers NetMonitorService
        this.monitorService = monitorService;
        // Initialise le Scanner sur l'entrée standard (clavier)
        this.scanner = new Scanner(System.in);
    // Fin du constructeur ConsoleMenu
    }

    // Point d'entrée de l'interface console qui boucle tant que l'utilisateur ne quitte pas
    public void demarrer() {
        // Variable contrôlant l'exécution continue de l'application
        boolean enCours = true;

        // Boucle principale de l'application
        while (enCours) {
            // Vérifie si aucun compte n'existe encore dans data/users.json (premier lancement)
            if (authService.aucunUtilisateurEnregistre()) {
                // Lance l'assistant de création dynamique du premier compte administrateur
                creerPremierAdministrateur();
            // Fin du test de premier lancement
            }

            // Vérifie si aucun utilisateur n'est actuellement connecté
            if (!authService.estConnecte()) {
                // Affiche l'écran de connexion (retourne false si l'utilisateur choisit de quitter)
                enCours = afficherEcranConnexion();
            } else {
                // Affiche le tableau de bord et le menu principal adapté au rôle connecté
                afficherMenuPrincipal();
            // Fin du aiguillage connecté / déconnecté
            }
        // Fin de la boucle principale
        }

        // Affiche un message de fermeture propre
        System.out.println("\nFermeture de NetMonitor. À bientôt !");
        // Ferme le scanner clavier avant de quitter
        scanner.close();
    // Fin de la méthode demarrer
    }

    // Assistant affiché uniquement si data/users.json est vide (aucun compte codé en dur)
    private void creerPremierAdministrateur() {
        // Affiche l'en-tête d'initialisation du premier compte
        System.out.println("\n==================================================");
        System.out.println("  INITIALISATION NETMONITOR - PREMIER LANCEMENT");
        System.out.println("  Aucun compte trouvé. Créez l'administrateur :");
        System.out.println("==================================================");

        // Boucle jusqu'à ce que le premier compte administrateur soit créé sans erreur
        while (authService.aucunUtilisateurEnregistre()) {
            // Tente de créer le compte avec les saisies clavier
            try {
                // Demande l'identifiant du compte (ex : USR-01)
                System.out.print("Identifiant (ex: USR-01) : ");
                String id = scanner.nextLine();
                // Demande le nom complet de l'administrateur
                System.out.print("Nom complet : ");
                String nom = scanner.nextLine();
                // Demande le login de connexion
                System.out.print("Login : ");
                String login = scanner.nextLine();
                // Demande le mot de passe
                System.out.print("Mot de passe : ");
                String mdp = scanner.nextLine();

                // Crée dynamiquement le compte avec le rôle ADMIN et le sauvegarde en JSON
                authService.creerUtilisateur(id, nom, login, mdp, UserRole.ADMIN);
                // Confirme la création et la sauvegarde
                System.out.println("-> Compte Administrateur créé et sauvegardé dans data/users.json !");
            // Capture toute erreur de saisie (champ vide)
            } catch (IllegalArgumentException e) {
                // Affiche le message d'erreur et redemande la saisie
                System.out.println("Erreur : " + e.getMessage());
            // Fin du bloc try-catch
            }
        // Fin de la boucle de création
        }
    // Fin de la méthode creerPremierAdministrateur
    }

    // Affiche le menu d'authentification et retourne false si l'utilisateur demande à quitter
    private boolean afficherEcranConnexion() {
        // Affiche l'encadré de connexion
        System.out.println("\n==========================================");
        System.out.println("         NETMONITOR - CONNEXION");
        System.out.println("==========================================");
        System.out.println("1. Se connecter");
        System.out.println("0. Quitter l'application");
        System.out.print("Votre choix : ");

        // Lit le choix saisi par l'utilisateur
        String choix = scanner.nextLine().trim();

        // Si l'utilisateur saisit "0", demande l'arrêt du programme
        if ("0".equals(choix)) {
            // Retourne false pour sortir de la boucle principale
            return false;
        // Fin du test de sortie
        }

        // Si l'utilisateur saisit "1", demande son login et son mot de passe
        if ("1".equals(choix)) {
            // Demande le login
            System.out.print("Login : ");
            String login = scanner.nextLine();
            // Demande le mot de passe
            System.out.print("Mot de passe : ");
            String mdp = scanner.nextLine();

            // Tente d'authentifier l'utilisateur auprès de AuthService
            if (authService.connecter(login, mdp)) {
                // Récupère l'utilisateur connecté pour afficher son nom et son rôle
                User u = authService.getUtilisateurConnecte();
                // Affiche un message de bienvenue personnalisé
                System.out.println("-> Connexion réussie ! Bienvenue " + u.getNom() + " [" + u.getRole() + "].");
            } else {
                // Affiche un message d'échec sans révéler si c'est le login ou le mot de passe
                System.out.println("-> Identifiants incorrects. Réessayez.");
            // Fin de la vérification de connexion
            }
        } else {
            // Signale un choix de menu invalide
            System.out.println("Choix invalide.");
        // Fin du test de choix
        }

        // Retourne true pour maintenir l'application active
        return true;
    // Fin de la méthode afficherEcranConnexion
    }

    // Affiche le tableau de bord synthétique et les options selon le rôle connecté
    private void afficherMenuPrincipal() {
        // Récupère l'utilisateur actuellement authentifié
        User courant = authService.getUtilisateurConnecte();
        // Vérifie si l'utilisateur connecté est administrateur
        boolean estAdmin = authService.estAdmin();

        // Affiche l'en-tête avec le profil connecté et les compteurs du Dashboard
        System.out.println("\n==================================================================");
        System.out.println("  NETMONITOR - TABLEAU DE BORD  |  " + courant.getNom() + " (" + courant.getRole() + ")");
        System.out.println("==================================================================");
        // Affiche les statistiques calculées dynamiquement depuis NetMonitorService
        System.out.printf("  Parc : %d total | UP : %d | WARNING : %d | DOWN : %d | Incidents actifs : %d%n",
                monitorService.listerEquipements().size(),
                monitorService.compterEquipementsParEtat(DeviceState.UP),
                monitorService.compterEquipementsParEtat(DeviceState.WARNING),
                monitorService.compterEquipementsParEtat(DeviceState.DOWN),
                monitorService.compterIncidentsActifs());
        System.out.println("------------------------------------------------------------------");

        // Options accessibles à tous les profils (Administrateur et Technicien)
        System.out.println("1. Afficher tous les équipements");
        System.out.println("2. Rechercher un équipement (nom, IP, lieu)");
        System.out.println("3. Filtrer les équipements par état (UP / WARNING / DOWN)");
        System.out.println("4. Simuler réception de mesures (CPU / RAM / Latence)");
        System.out.println("5. Vérifier les heartbeats (détection équipements DOWN)");
        System.out.println("6. Consulter les incidents");
        System.out.println("7. Prendre en charge un incident (OPEN -> IN_PROGRESS)");
        System.out.println("8. Clôturer un incident (-> CLOSED)");
        System.out.println("9. Consulter le journal des événements (Logs)");

        // Options réservées exclusivement au rôle ADMIN
        if (estAdmin) {
            System.out.println("--- ADMINISTRATION ---");
            System.out.println("10. Ajouter un équipement");
            System.out.println("11. Modifier un équipement");
            System.out.println("12. Supprimer un équipement");
            System.out.println("13. Gérer les utilisateurs (lister / créer / supprimer)");
            System.out.println("14. Configurer les seuils d'alerte (CPU / RAM / Heartbeat)");
        // Fin de l'affichage des options ADMIN
        }

        // Option de déconnexion commune à tous
        System.out.println("0. Se déconnecter");
        System.out.print("Votre choix : ");

        // Lit le choix saisi au clavier
        String choix = scanner.nextLine().trim();

        // Exécute l'action correspondant au numéro saisi
        switch (choix) {
            case "1":
                // Affiche la liste complète des équipements
                actionAfficherEquipements();
                break;
            case "2":
                // Lance une recherche par mot-clé
                actionRechercherEquipement();
                break;
            case "3":
                // Filtre les équipements par état
                actionFiltrerParEtat();
                break;
            case "4":
                // Simule l'envoi de métriques par un équipement
                actionSimulerMesures();
                break;
            case "5":
                // Vérifie le délai de heartbeat de tous les équipements
                int nbDown = monitorService.verifierHeartbeats();
                // Affiche le bilan de la vérification
                System.out.println("-> Vérification terminée : " + nbDown + " nouvel(aux) équipement(s) passé(s) en DOWN.");
                break;
            case "6":
                // Affiche tous les incidents enregistrés
                actionAfficherIncidents();
                break;
            case "7":
                // Permet de prendre en charge un incident ouvert
                actionPrendreEnChargeIncident();
                break;
            case "8":
                // Permet de clôturer un incident résolu
                actionCloturerIncident();
                break;
            case "9":
                // Affiche l'historique chronologique des événements
                actionAfficherEvenements();
                break;
            case "10":
                // Vérifie que l'utilisateur est bien ADMIN avant d'autoriser l'ajout
                if (verifierDroitAdmin()) actionAjouterEquipement();
                break;
            case "11":
                // Vérifie que l'utilisateur est bien ADMIN avant d'autoriser la modification
                if (verifierDroitAdmin()) actionModifierEquipement();
                break;
            case "12":
                // Vérifie que l'utilisateur est bien ADMIN avant d'autoriser la suppression
                if (verifierDroitAdmin()) actionSupprimerEquipement();
                break;
            case "13":
                // Vérifie que l'utilisateur est bien ADMIN avant d'ouvrir la gestion des comptes
                if (verifierDroitAdmin()) actionGererUtilisateurs();
                break;
            case "14":
                // Vérifie que l'utilisateur est bien ADMIN avant de modifier les seuils
                if (verifierDroitAdmin()) actionConfigurerSeuils();
                break;
            case "0":
                // Ferme la session de l'utilisateur courant
                authService.deconnecter();
                // Confirme la déconnexion
                System.out.println("-> Déconnexion effectuée.");
                break;
            default:
                // Affiche un message si le choix saisi ne correspond à aucune option
                System.out.println("Choix invalide.");
        // Fin du switch de navigation
        }
    // Fin de la méthode afficherMenuPrincipal
    }

    // Contrôle de sécurité empêchant un technicien d'exécuter une action administrateur
    private boolean verifierDroitAdmin() {
        // Si l'utilisateur n'est pas ADMIN, affiche un refus d'accès et retourne false
        if (!authService.estAdmin()) {
            // Informe l'utilisateur que l'action est interdite à son profil
            System.out.println("-> Accès refusé : privilèges Administrateur requis.");
            // Bloque l'exécution de l'action
            return false;
        // Fin de la vérification
        }
        // Autorise l'exécution pour un administrateur
        return true;
    // Fin de la méthode verifierDroitAdmin
    }

    // Affiche la liste complète des équipements enregistrés
    private void actionAfficherEquipements() {
        // Récupère la liste des équipements depuis le service métier
        List<NetworkDevice> liste = monitorService.listerEquipements();
        // Affiche un message si le parc est encore vide
        if (liste.isEmpty()) {
            // Informe l'utilisateur
            System.out.println("Aucun équipement enregistré.");
            // Sort de la méthode
            return;
        // Fin du test de liste vide
        }
        // Affiche l'en-tête puis chaque équipement ligne par ligne
        System.out.println("\n--- LISTE DES ÉQUIPEMENTS ---");
        for (NetworkDevice d : liste) {
            // Utilise la méthode toString() de NetworkDevice
            System.out.println(d);
        // Fin de la boucle
        }
    // Fin de la méthode actionAfficherEquipements
    }

    // Recherche des équipements par nom, adresse IP ou localisation
    private void actionRechercherEquipement() {
        // Demande le critère de recherche
        System.out.print("Entrez un nom, une IP, un ID ou un lieu : ");
        String motCle = scanner.nextLine();
        // Lance la recherche dans le service métier
        List<NetworkDevice> resultats = monitorService.rechercherEquipements(motCle);
        // Affiche les résultats trouvés
        if (resultats.isEmpty()) {
            // Informe qu'aucune machine ne correspond
            System.out.println("Aucun équipement trouvé pour '" + motCle + "'.");
        } else {
            // Affiche chaque machine correspondante
            System.out.println("\n--- RÉSULTATS DE RECHERCHE (" + resultats.size() + ") ---");
            for (NetworkDevice d : resultats) {
                // Affiche l'équipement
                System.out.println(d);
            // Fin de la boucle
            }
        // Fin du bloc d'affichage
        }
    // Fin de la méthode actionRechercherEquipement
    }

    // Filtre l'affichage des équipements selon un état choisi
    private void actionFiltrerParEtat() {
        // Demande l'état souhaité
        System.out.print("État à filtrer (UP, WARNING, DOWN, CRITICAL) : ");
        String saisie = scanner.nextLine().trim().toUpperCase();
        // Tente de convertir la saisie en constante DeviceState
        try {
            // Convertit le texte en énumération
            DeviceState etat = DeviceState.valueOf(saisie);
            // Récupère les équipements ayant cet état
            List<NetworkDevice> filtres = monitorService.filtrerParEtat(etat);
            // Affiche les équipements filtrés
            System.out.println("\n--- ÉQUIPEMENTS EN ÉTAT " + etat + " (" + filtres.size() + ") ---");
            for (NetworkDevice d : filtres) {
                // Affiche chaque équipement
                System.out.println(d);
            // Fin de la boucle
            }
        // Capture l'erreur si l'utilisateur a tapé un état inexistant
        } catch (IllegalArgumentException e) {
            // Affiche un message d'erreur clair
            System.out.println("État inconnu. Valeurs possibles : UP, WARNING, DOWN, CRITICAL.");
        // Fin du bloc try-catch
        }
    // Fin de la méthode actionFiltrerParEtat
    }

    // Permet de simuler l'envoi de mesures CPU/RAM/Latence par un équipement
    private void actionSimulerMesures() {
        // Tente de lire et d'enregistrer les métriques saisies
        try {
            // Demande l'identifiant de l'équipement cible
            System.out.print("Identifiant de l'équipement (ex: SRV-01) : ");
            String id = scanner.nextLine();
            // Demande le taux d'utilisation CPU
            System.out.print("Utilisation CPU (%) : ");
            double cpu = Double.parseDouble(scanner.nextLine().replace(",", "."));
            // Demande le taux d'utilisation RAM
            System.out.print("Utilisation RAM (%) : ");
            double ram = Double.parseDouble(scanner.nextLine().replace(",", "."));
            // Demande la latence réseau en ms
            System.out.print("Latence (ms) : ");
            double latence = Double.parseDouble(scanner.nextLine().replace(",", "."));

            // Transmet les mesures au moteur de supervision
            monitorService.enregistrerMesures(id, cpu, ram, latence);
            // Confirme la prise en compte des mesures
            System.out.println("-> Mesures enregistrées et heartbeat mis à jour.");
        // Capture les erreurs de saisie numérique ou d'identifiant introuvable
        } catch (NumberFormatException e) {
            // Signale que la valeur saisie n'est pas un nombre valide
            System.out.println("Erreur : veuillez saisir des valeurs numériques valides.");
        } catch (IllegalArgumentException e) {
            // Affiche le message d'erreur métier
            System.out.println("Erreur : " + e.getMessage());
        // Fin du bloc try-catch
        }
    // Fin de la méthode actionSimulerMesures
    }

    // Affiche la liste de tous les incidents détectés
    private void actionAfficherIncidents() {
        // Récupère la liste des incidents
        List<Incident> liste = monitorService.listerIncidents();
        // Vérifie si la liste est vide
        if (liste.isEmpty()) {
            // Informe qu'aucun incident n'a été déclenché
            System.out.println("Aucun incident enregistré.");
            // Sort de la méthode
            return;
        // Fin du test
        }
        // Affiche chaque incident avec son statut et le technicien assigné
        System.out.println("\n--- LISTE DES INCIDENTS ---");
        for (Incident inc : liste) {
            // Utilise la méthode toString() d'Incident
            System.out.println(inc);
        // Fin de la boucle
        }
    // Fin de la méthode actionAfficherIncidents
    }

    // Permet à l'utilisateur connecté de prendre en charge un incident OPEN
    private void actionPrendreEnChargeIncident() {
        // Demande l'identifiant de l'incident à traiter
        System.out.print("Identifiant de l'incident (ex: INC-1) : ");
        String idInc = scanner.nextLine();
        // Tente d'assigner l'incident à l'utilisateur connecté
        try {
            // Appelle le service métier
            if (monitorService.prendreEnChargeIncident(idInc, authService.getUtilisateurConnecte())) {
                // Confirme la prise en charge
                System.out.println("-> Incident pris en charge (statut passé à IN_PROGRESS).");
            } else {
                // Signale que l'identifiant d'incident n'existe pas
                System.out.println("Incident introuvable.");
            // Fin du test
            }
        // Capture l'exception si l'incident est déjà clôturé
        } catch (IllegalStateException e) {
            // Affiche le message d'erreur
            System.out.println("Erreur : " + e.getMessage());
        // Fin du bloc try-catch
        }
    // Fin de la méthode actionPrendreEnChargeIncident
    }

    // Permet à l'utilisateur connecté de clôturer un incident résolu
    private void actionCloturerIncident() {
        // Demande l'identifiant de l'incident à fermer
        System.out.print("Identifiant de l'incident à clôturer (ex: INC-1) : ");
        String idInc = scanner.nextLine();
        // Tente de clôturer l'incident
        try {
            // Appelle le service métier
            if (monitorService.cloturerIncident(idInc, authService.getUtilisateurConnecte())) {
                // Confirme la clôture
                System.out.println("-> Incident clôturé (statut passé à CLOSED).");
            } else {
                // Signale que l'incident est introuvable
                System.out.println("Incident introuvable.");
            // Fin du test
            }
        // Capture l'exception si l'incident était déjà clôturé
        } catch (IllegalStateException e) {
            // Affiche le message d'erreur
            System.out.println("Erreur : " + e.getMessage());
        // Fin du bloc try-catch
        }
    // Fin de la méthode actionCloturerIncident
    }

    // Affiche l'historique chronologique de tous les événements enregistrés
    private void actionAfficherEvenements() {
        // Récupère le journal des événements
        List<Event> logs = monitorService.listerEvenements();
        // Affiche l'en-tête puis chaque événement horodaté
        System.out.println("\n--- JOURNAL DES ÉVÉNEMENTS (" + logs.size() + ") ---");
        for (Event evt : logs) {
            // Utilise la méthode toString() d'Event
            System.out.println(evt);
        // Fin de la boucle
        }
    // Fin de la méthode actionAfficherEvenements
    }

    // Action réservée à l'ADMIN : ajoute un nouvel équipement réseau
    private void actionAjouterEquipement() {
        // Tente de créer l'équipement avec les informations saisies
        try {
            // Demande l'identifiant unique
            System.out.print("Identifiant unique (ex: SW-001) : ");
            String id = scanner.nextLine();
            // Demande le nom de machine
            System.out.print("Nom / Hostname (ex: Switch-Core) : ");
            String nom = scanner.nextLine();
            // Demande l'adresse IPv4
            System.out.print("Adresse IP (ex: 192.168.1.10) : ");
            String ip = scanner.nextLine();
            // Demande le type d'équipement
            System.out.print("Type (Routeur, Switch, Serveur, Wi-Fi...) : ");
            String type = scanner.nextLine();
            // Demande la localisation physique
            System.out.print("Localisation (ex: Baie A1) : ");
            String lieu = scanner.nextLine();

            // Ajoute l'équipement via le service métier (qui valide l'IP et sauvegarde en JSON)
            monitorService.ajouterEquipement(id, nom, ip, type, lieu);
            // Confirme l'ajout
            System.out.println("-> Équipement ajouté et sauvegardé avec succès !");
        // Capture les erreurs de validation (IP invalide, champ vide, doublon)
        } catch (IllegalArgumentException e) {
            // Affiche la raison du refus
            System.out.println("Erreur : " + e.getMessage());
        // Fin du bloc try-catch
        }
    // Fin de la méthode actionAjouterEquipement
    }

    // Action réservée à l'ADMIN : modifie un équipement existant
    private void actionModifierEquipement() {
        // Demande l'identifiant de l'équipement à modifier
        System.out.print("Identifiant de l'équipement à modifier : ");
        String id = scanner.nextLine();
        // Vérifie d'abord que l'équipement existe
        if (monitorService.rechercherParIdentifiant(id) == null) {
            // Informe que l'identifiant n'existe pas
            System.out.println("Équipement introuvable.");
            // Sort de la méthode
            return;
        // Fin du test d'existence
        }
        // Demande les nouvelles valeurs (laisser vide conserve l'ancienne valeur)
        System.out.println("(Laissez vide et appuyez sur Entrée pour conserver la valeur actuelle)");
        try {
            // Lit le nouveau nom éventuel
            System.out.print("Nouveau nom : ");
            String nom = scanner.nextLine();
            // Lit la nouvelle adresse IP éventuelle
            System.out.print("Nouvelle IP : ");
            String ip = scanner.nextLine();
            // Lit le nouveau type éventuel
            System.out.print("Nouveau type : ");
            String type = scanner.nextLine();
            // Lit la nouvelle localisation éventuelle
            System.out.print("Nouvelle localisation : ");
            String lieu = scanner.nextLine();

            // Applique les modifications via le service métier
            monitorService.modifierEquipement(id, nom, ip, type, lieu);
            // Confirme la mise à jour
            System.out.println("-> Équipement mis à jour !");
        // Capture les erreurs de validation (ex : nouvelle IP invalide)
        } catch (IllegalArgumentException e) {
            // Affiche l'erreur
            System.out.println("Erreur : " + e.getMessage());
        // Fin du bloc try-catch
        }
    // Fin de la méthode actionModifierEquipement
    }

    // Action réservée à l'ADMIN : supprime un équipement du parc
    private void actionSupprimerEquipement() {
        // Demande l'identifiant de l'équipement à supprimer
        System.out.print("Identifiant de l'équipement à supprimer : ");
        String id = scanner.nextLine();
        // Tente la suppression via le service métier
        if (monitorService.supprimerEquipement(id)) {
            // Confirme la suppression
            System.out.println("-> Équipement supprimé et fichier JSON mis à jour.");
        } else {
            // Signale que l'équipement n'a pas été trouvé
            System.out.println("Équipement introuvable.");
        // Fin du test
        }
    // Fin de la méthode actionSupprimerEquipement
    }

    // Action réservée à l'ADMIN : gestion dynamique des comptes (lister, créer, supprimer)
    private void actionGererUtilisateurs() {
        // Affiche le sous-menu de gestion des comptes
        System.out.println("\n--- GESTION DYNAMIQUE DES UTILISATEURS ---");
        System.out.println("1. Lister les utilisateurs");
        System.out.println("2. Créer un nouvel utilisateur");
        System.out.println("3. Supprimer un utilisateur");
        System.out.print("Votre choix : ");
        String choix = scanner.nextLine().trim();

        // Traite le choix de l'administrateur
        switch (choix) {
            case "1":
                // Affiche tous les comptes enregistrés dans data/users.json
                for (User u : authService.listerUtilisateurs()) {
                    // Utilise la méthode toString() de User
                    System.out.println(u);
                // Fin de la boucle
                }
                break;
            case "2":
                // Crée un nouveau compte dynamiquement
                try {
                    // Demande l'identifiant unique du compte
                    System.out.print("Identifiant (ex: USR-02) : ");
                    String id = scanner.nextLine();
                    // Demande le nom complet
                    System.out.print("Nom complet : ");
                    String nom = scanner.nextLine();
                    // Demande le login
                    System.out.print("Login : ");
                    String login = scanner.nextLine();
                    // Demande le mot de passe
                    System.out.print("Mot de passe : ");
                    String mdp = scanner.nextLine();
                    // Demande le rôle (ADMIN ou TECHNICIEN)
                    System.out.print("Rôle (ADMIN ou TECHNICIEN) : ");
                    UserRole role = UserRole.valueOf(scanner.nextLine().trim().toUpperCase());

                    // Crée et sauvegarde l'utilisateur dans data/users.json
                    authService.creerUtilisateur(id, nom, login, mdp, role);
                    // Confirme la création
                    System.out.println("-> Utilisateur créé et sauvegardé dans data/users.json !");
                // Capture les erreurs de saisie ou de rôle inconnu
                } catch (IllegalArgumentException e) {
                    // Affiche l'erreur
                    System.out.println("Erreur : " + e.getMessage());
                // Fin du bloc try-catch
                }
                break;
            case "3":
                // Supprime un compte existant par son identifiant
                try {
                    // Demande l'identifiant du compte à supprimer
                    System.out.print("Identifiant de l'utilisateur à supprimer : ");
                    String idSuppr = scanner.nextLine();
                    // Tente la suppression via AuthService
                    if (authService.supprimerUtilisateur(idSuppr)) {
                        // Confirme la suppression
                        System.out.println("-> Utilisateur supprimé de data/users.json.");
                    } else {
                        // Signale que le compte n'existe pas
                        System.out.println("Utilisateur introuvable.");
                    // Fin du test
                    }
                // Capture les protections (auto-suppression ou dernier admin)
                } catch (IllegalStateException e) {
                    // Affiche le message de protection
                    System.out.println("Erreur : " + e.getMessage());
                // Fin du bloc try-catch
                }
                break;
            default:
                // Signale un choix invalide
                System.out.println("Choix invalide.");
        // Fin du switch
        }
    // Fin de la méthode actionGererUtilisateurs
    }

    // Action réservée à l'ADMIN : configure les seuils de déclenchement des alertes
    private void actionConfigurerSeuils() {
        // Affiche les seuils actuellement en vigueur
        System.out.printf("Seuils actuels : CPU > %.0f%% | RAM > %.0f%% | Mesures consécutives : %d | Timeout Heartbeat : %ds%n",
                monitorService.getSeuilCpu(),
                monitorService.getSeuilRam(),
                monitorService.getMesuresConsecutivesRequises(),
                monitorService.getDelaiHeartbeatSecondes());
        // Tente de lire et d'appliquer les nouveaux seuils
        try {
            // Demande le nouveau seuil CPU
            System.out.print("Nouveau seuil CPU (%) : ");
            double cpu = Double.parseDouble(scanner.nextLine().replace(",", "."));
            // Demande le nouveau seuil RAM
            System.out.print("Nouveau seuil RAM (%) : ");
            double ram = Double.parseDouble(scanner.nextLine().replace(",", "."));
            // Demande le nombre N de mesures consécutives
            System.out.print("Nombre de mesures consécutives avant WARNING : ");
            int n = Integer.parseInt(scanner.nextLine().trim());
            // Demande le délai maximum sans heartbeat
            System.out.print("Délai maximum sans heartbeat (secondes) : ");
            long delai = Long.parseLong(scanner.nextLine().trim());

            // Applique la nouvelle configuration dans le service métier
            monitorService.configurerSeuils(cpu, ram, n, delai);
            // Confirme la mise à jour
            System.out.println("-> Seuils de supervision mis à jour !");
        // Capture les erreurs de saisie numérique ou de plage invalide
        } catch (NumberFormatException e) {
            // Signale une saisie non numérique
            System.out.println("Erreur : veuillez entrer des nombres valides.");
        } catch (IllegalArgumentException e) {
            // Affiche l'erreur métier
            System.out.println("Erreur : " + e.getMessage());
        // Fin du bloc try-catch
        }
    // Fin de la méthode actionConfigurerSeuils
    }
// Fin de la classe ConsoleMenu
}