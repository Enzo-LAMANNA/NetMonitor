/*
 * UTILITÉ DU FICHIER :
 * Point d'entrée principal (méthode main) de l'application NetMonitor.
 * Il instancie et relie entre eux les services de persistance, d'authentification,
 * de supervision réseau et l'interface utilisateur en ligne de commande.
 *
 * FICHIERS RELIÉS :
 * - StorageService.java : initialise la sauvegarde JSON dans le dossier data/.
 * - AuthService.java : gère dynamiquement les utilisateurs et la connexion.
 * - NetMonitorService.java : gère les équipements, mesures, alertes et incidents.
 * - ConsoleMenu.java : lance l'interface interactive dans le terminal.
 */

// Importe la classe ConsoleMenu depuis le package cli
import cli.ConsoleMenu;
// Importe File pour détecter automatiquement l'emplacement du dossier racine du projet
import java.io.File;
// Importe le service d'authentification depuis le package service
import service.AuthService;
// Importe le moteur métier de supervision depuis le package service
import service.NetMonitorService;
// Importe le service de persistance JSON depuis le package service
import service.StorageService;

// Déclare la classe publique principale Main
public class Main {

    // Point d'entrée exécuté par la machine virtuelle Java au lancement du programme
    public static void main(String[] args) {
        // Détermine le chemin vers le dossier "data" à la racine du projet NetMonitor
        String cheminData = determinerDossierData();

        // Initialise le service de stockage JSON (crée data/devices.json et data/users.json)
        StorageService storageService = new StorageService(cheminData);

        // Initialise le service d'authentification et charge les utilisateurs existants
        AuthService authService = new AuthService(storageService);

        // Initialise le service de supervision réseau et charge les équipements existants
        NetMonitorService monitorService = new NetMonitorService(storageService);

        // Instancie le menu console en lui fournissant les services nécessaires
        ConsoleMenu menu = new ConsoleMenu(authService, monitorService);

        // Démarre la boucle interactive de l'application dans le terminal
        menu.demarrer();
    // Fin de la méthode main
    }

    // Méthode utilitaire garantissant que le dossier "data" soit toujours placé à la racine
    private static String determinerDossierData() {
        // Vérifie si le programme est lancé depuis le sous-dossier "src"
        File dossierParentSrc = new File("../src");
        // Si "../src" existe, remonte d'un niveau pour viser "NetMonitor/data"
        if (dossierParentSrc.exists() && dossierParentSrc.isDirectory()) {
            // Retourne le chemin relatif vers la racine du projet
            return "../data";
        // Fin du test de répertoire courant
        }
        // Sinon (lancement depuis la racine NetMonitor), utilise directement "data"
        return "data";
    // Fin de la méthode determinerDossierData
    }
// Fin de la classe Main
}