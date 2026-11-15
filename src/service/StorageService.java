/*
 * UTILITÉ DU FICHIER :
 * Assure la persistance des données de NetMonitor au format JSON structuré dans le
 * dossier "data/" (data/devices.json et data/users.json), afin que les équipements
 * et les comptes utilisateurs créés ou supprimés soient conservés après fermeture.
 *
 * FICHIERS RELIÉS :
 * - NetworkDevice.java & DeviceState.java : objets équipements sauvegardés et rechargés.
 * - User.java & UserRole.java : objets utilisateurs sauvegardés et rechargés dynamiquement.
 * - AuthService.java : appelle ce service pour enregistrer et charger les utilisateurs.
 * - NetMonitorService.java : appelle ce service pour enregistrer et charger les équipements.
 */

// Indique que ce fichier appartient au package (dossier) "service"
package service;

// Importe File pour vérifier et créer les dossiers et fichiers sur le disque
import java.io.File;
// Importe IOException pour gérer proprement les erreurs de lecture ou d'écriture
import java.io.IOException;
// Importe StandardCharsets pour garantir l'encodage UTF-8 des fichiers JSON
import java.nio.charset.StandardCharsets;
// Importe Files pour lire et écrire le contenu complet des fichiers simplement
import java.nio.file.Files;
// Importe Path pour manipuler les chemins de fichiers de manière portable
import java.nio.file.Path;
// Importe Paths pour construire un objet Path à partir d'une chaîne de caractères
import java.nio.file.Paths;
// Importe ArrayList pour stocker les listes d'objets rechargés
import java.util.ArrayList;
// Importe List pour typer les collections d'équipements et d'utilisateurs
import java.util.List;
// Importe Matcher pour extraire les champs texte du format JSON
import java.util.regex.Matcher;
// Importe Pattern pour définir les expressions régulières de lecture JSON
import java.util.regex.Pattern;
// Importe l'énumération DeviceState depuis le package model
import model.DeviceState;
// Importe la classe NetworkDevice depuis le package model
import model.NetworkDevice;
// Importe la classe User depuis le package model
import model.User;
// Importe l'énumération UserRole depuis le package model
import model.UserRole;

// Déclare la classe publique StorageService chargée de la persistance JSON locale
public class StorageService {

    // Chemin du dossier contenant les fichiers de sauvegarde JSON
    private final String dossierData;

    // Chemin complet vers le fichier JSON des équipements
    private final String cheminDevices;

    // Chemin complet vers le fichier JSON des utilisateurs
    private final String cheminUsers;

    // Constructeur par défaut utilisant le dossier "data" à la racine du projet
    public StorageService() {
        // Appelle le constructeur paramétré avec le dossier "data"
        this("data");
    // Fin du constructeur par défaut
    }

    // Constructeur permettant de spécifier un dossier de stockage personnalisé
    public StorageService(String dossierData) {
        // Enregistre le nom du dossier de stockage
        this.dossierData = dossierData;
        // Construit le chemin vers le fichier des équipements
        this.cheminDevices = dossierData + File.separator + "devices.json";
        // Construit le chemin vers le fichier des utilisateurs
        this.cheminUsers = dossierData + File.separator + "users.json";
        // Crée physiquement le dossier sur le disque s'il n'existe pas encore
        initialiserDossier();
    // Fin du constructeur paramétré
    }

    // Méthode privée garantissant l'existence du dossier de sauvegarde
    private void initialiserDossier() {
        // Crée un objet File pointant vers le dossier de données
        File dossier = new File(dossierData);
        // Vérifie si le dossier n'existe pas encore sur le disque
        if (!dossier.exists()) {
            // Crée le dossier (et ses parents si nécessaire)
            dossier.mkdirs();
        // Fin de la condition d'existence
        }
    // Fin de la méthode initialiserDossier
    }

    // Méthode utilitaire échappant les guillemets et antislashs pour produire un JSON valide
    private String echapperJson(String texte) {
        // Retourne une chaîne vide si le texte fourni est nul
        if (texte == null) {
            // Renvoie vide par sécurité
            return "";
        // Fin du test de nullité
        }
        // Remplace les antislashs puis les guillemets doubles par leur version échappée
        return texte.replace("\\", "\\\\").replace("\"", "\\\"");
    // Fin de la méthode echapperJson
    }

    // Méthode utilitaire extrayant la valeur associée à une clé dans un bloc JSON
    private String extraireChampJson(String blocJson, String cle) {
        // Construit un motif regex recherchant "cle": "valeur"
        Pattern motif = Pattern.compile("\"" + Pattern.quote(cle) + "\"\\s*:\\s*\"((?:\\\\\"|[^\"])*)\"");
        // Applique le motif sur le bloc JSON fourni
        Matcher chercheur = motif.matcher(blocJson);
        // Vérifie si la clé a été trouvée dans le bloc
        if (chercheur.find()) {
            // Retourne la valeur trouvée en déséchappant les guillemets et antislashs
            return chercheur.group(1).replace("\\\"", "\"").replace("\\\\", "\\");
        // Fin du test de correspondance
        }
        // Retourne une chaîne vide si la clé est absente
        return "";
    // Fin de la méthode extraireChampJson
    }

    // Sauvegarde la liste complète des équipements dans data/devices.json
    public boolean sauvegarderEquipements(List<NetworkDevice> equipements) {
        // S'assure que le dossier data existe avant d'écrire
        initialiserDossier();
        // Initialise un StringBuilder pour construire le contenu JSON formaté
        StringBuilder json = new StringBuilder();
        // Ouvre le tableau JSON principal
        json.append("[\n");

        // Parcourt tous les équipements de la liste avec un index
        for (int i = 0; i < equipements.size(); i++) {
            // Récupère l'équipement courant
            NetworkDevice dev = equipements.get(i);
            // Ouvre l'objet JSON de l'équipement
            json.append("  {\n");
            // Écrit l'identifiant unique
            json.append("    \"identifiant\": \"").append(echapperJson(dev.getIdentifiant())).append("\",\n");
            // Écrit le nom de la machine
            json.append("    \"nom\": \"").append(echapperJson(dev.getNom())).append("\",\n");
            // Écrit l'adresse IP
            json.append("    \"ip\": \"").append(echapperJson(dev.getIp())).append("\",\n");
            // Écrit le type d'équipement
            json.append("    \"type\": \"").append(echapperJson(dev.getType())).append("\",\n");
            // Écrit la localisation
            json.append("    \"localisation\": \"").append(echapperJson(dev.getLocalisation())).append("\",\n");
            // Écrit l'état fonctionnel sous forme de chaîne
            json.append("    \"etat\": \"").append(dev.getEtat().name()).append("\",\n");
            // Écrit la valeur CPU formatée avec un point décimal
            json.append("    \"cpu\": \"").append(dev.getCpu()).append("\",\n");
            // Écrit la valeur RAM formatée avec un point décimal
            json.append("    \"ram\": \"").append(dev.getRam()).append("\",\n");
            // Écrit la valeur de latence formatée avec un point décimal
            json.append("    \"latence\": \"").append(dev.getLatence()).append("\"\n");
            // Ferme l'objet JSON et ajoute une virgule sauf pour le dernier élément
            json.append(i < equipements.size() - 1 ? "  },\n" : "  }\n");
        // Fin de la boucle sur les équipements
        }

        // Ferme le tableau JSON principal
        json.append("]\n");

        // Tente d'écrire la chaîne JSON dans le fichier sur le disque
        try {
            // Écrit le texte en UTF-8 dans data/devices.json
            Files.writeString(Paths.get(cheminDevices), json.toString(), StandardCharsets.UTF_8);
            // Retourne true pour confirmer le succès de la sauvegarde
            return true;
        // Capture toute erreur d'entrée/sortie (disque plein, droits refusés...)
        } catch (IOException e) {
            // Affiche un message d'erreur explicite sans faire planter l'application
            System.err.println("Erreur lors de la sauvegarde des équipements : " + e.getMessage());
            // Retourne false pour signaler l'échec
            return false;
        // Fin du bloc try-catch
        }
    // Fin de la méthode sauvegarderEquipements
    }

    // Charge et reconstruit la liste des équipements depuis data/devices.json
    public List<NetworkDevice> chargerEquipements() {
        // Initialise la liste qui contiendra les équipements lus
        List<NetworkDevice> liste = new ArrayList<>();
        // Récupère le chemin vers data/devices.json
        Path path = Paths.get(cheminDevices);

        // Vérifie si le fichier n'existe pas encore (premier lancement)
        if (!Files.exists(path)) {
            // Retourne simplement une liste vide
            return liste;
        // Fin de la vérification d'existence
        }

        // Tente de lire et analyser le fichier JSON
        try {
            // Lit l'intégralité du fichier JSON en UTF-8
            String contenu = Files.readString(path, StandardCharsets.UTF_8);
            // Découpe le contenu pour isoler chaque bloc d'objet { ... }
            Matcher blocMatcher = Pattern.compile("\\{([^{}]*)\\}").matcher(contenu);

            // Parcourt chaque objet JSON trouvé dans le tableau
            while (blocMatcher.find()) {
                // Récupère le texte intérieur de l'objet JSON courant
                String bloc = blocMatcher.group(1);
                // Extrait l'identifiant
                String id = extraireChampJson(bloc, "identifiant");
                // Extrait le nom
                String nom = extraireChampJson(bloc, "nom");
                // Extrait l'adresse IP
                String ip = extraireChampJson(bloc, "ip");
                // Extrait le type
                String type = extraireChampJson(bloc, "type");
                // Extrait la localisation
                String localisation = extraireChampJson(bloc, "localisation");
                // Extrait l'état sous forme de texte
                String etatTexte = extraireChampJson(bloc, "etat");

                // Reconstruit l'objet NetworkDevice à partir des données lues
                NetworkDevice device = new NetworkDevice(id, nom, ip, type, localisation);
                // Restaure l'état s'il est valide dans l'énumération DeviceState
                if (!etatTexte.isEmpty()) {
                    // Convertit le texte en constante DeviceState
                    device.setEtat(DeviceState.valueOf(etatTexte));
                // Fin de la restauration de l'état
                }

                // Extrait et restaure la valeur CPU si présente
                String cpuTexte = extraireChampJson(bloc, "cpu");
                // Vérifie que le champ CPU n'est pas vide
                if (!cpuTexte.isEmpty()) {
                    // Convertit en double et met à jour le CPU
                    device.setCpu(Double.parseDouble(cpuTexte));
                // Fin du bloc CPU
                }

                // Extrait et restaure la valeur RAM si présente
                String ramTexte = extraireChampJson(bloc, "ram");
                // Vérifie que le champ RAM n'est pas vide
                if (!ramTexte.isEmpty()) {
                    // Convertit en double et met à jour la RAM
                    device.setRam(Double.parseDouble(ramTexte));
                // Fin du bloc RAM
                }

                // Extrait et restaure la latence si présente
                String latenceTexte = extraireChampJson(bloc, "latence");
                // Vérifie que le champ latence n'est pas vide
                if (!latenceTexte.isEmpty()) {
                    // Convertit en double et met à jour la latence
                    device.setLatence(Double.parseDouble(latenceTexte));
                // Fin du bloc latence
                }

                // Ajoute l'équipement reconstruit à la liste
                liste.add(device);
            // Fin de la boucle sur les objets JSON
            }
        // Capture les erreurs de lecture fichier ou de format corrompu
        } catch (IOException | IllegalArgumentException e) {
            // Affiche un message d'erreur clair dans le terminal
            System.err.println("Erreur lors du chargement des équipements : " + e.getMessage());
        // Fin du bloc try-catch
        }

        // Retourne la liste des équipements chargés depuis le disque
        return liste;
    // Fin de la méthode chargerEquipements
    }

    // Sauvegarde dynamiquement la liste des utilisateurs dans data/users.json
    public boolean sauvegarderUtilisateurs(List<User> utilisateurs) {
        // S'assure que le dossier data existe avant d'écrire
        initialiserDossier();
        // Initialise un StringBuilder pour construire le JSON des utilisateurs
        StringBuilder json = new StringBuilder();
        // Ouvre le tableau JSON principal
        json.append("[\n");

        // Parcourt tous les utilisateurs enregistrés
        for (int i = 0; i < utilisateurs.size(); i++) {
            // Récupère l'utilisateur courant
            User user = utilisateurs.get(i);
            // Ouvre l'objet JSON de l'utilisateur
            json.append("  {\n");
            // Écrit l'identifiant unique
            json.append("    \"identifiant\": \"").append(echapperJson(user.getIdentifiant())).append("\",\n");
            // Écrit le nom complet
            json.append("    \"nom\": \"").append(echapperJson(user.getNom())).append("\",\n");
            // Écrit le login de connexion
            json.append("    \"login\": \"").append(echapperJson(user.getLogin())).append("\",\n");
            // Écrit le mot de passe
            json.append("    \"motDePasse\": \"").append(echapperJson(user.getMotDePasse())).append("\",\n");
            // Écrit le rôle sous forme de chaîne (ADMIN ou TECHNICIEN)
            json.append("    \"role\": \"").append(user.getRole().name()).append("\"\n");
            // Ferme l'objet JSON et ajoute une virgule sauf pour le dernier utilisateur
            json.append(i < utilisateurs.size() - 1 ? "  },\n" : "  }\n");
        // Fin de la boucle sur les utilisateurs
        }

        // Ferme le tableau JSON principal
        json.append("]\n");

        // Tente d'écrire le fichier data/users.json
        try {
            // Écrit le contenu JSON en UTF-8 sur le disque
            Files.writeString(Paths.get(cheminUsers), json.toString(), StandardCharsets.UTF_8);
            // Retourne true pour confirmer la sauvegarde
            return true;
        // Capture les éventuelles erreurs d'écriture
        } catch (IOException e) {
            // Affiche l'erreur sans interrompre le programme
            System.err.println("Erreur lors de la sauvegarde des utilisateurs : " + e.getMessage());
            // Retourne false en cas d'échec
            return false;
        // Fin du bloc try-catch
        }
    // Fin de la méthode sauvegarderUtilisateurs
    }

    // Charge dynamiquement tous les utilisateurs depuis data/users.json
    public List<User> chargerUtilisateurs() {
        // Initialise la liste qui recevra les utilisateurs lus
        List<User> liste = new ArrayList<>();
        // Récupère le chemin vers data/users.json
        Path path = Paths.get(cheminUsers);

        // Vérifie si le fichier data/users.json n'existe pas encore
        if (!Files.exists(path)) {
            // Retourne une liste vide (AuthService proposera alors de créer le 1er admin)
            return liste;
        // Fin de la vérification d'existence
        }

        // Tente de lire et analyser le fichier JSON des utilisateurs
        try {
            // Lit l'intégralité du fichier en UTF-8
            String contenu = Files.readString(path, StandardCharsets.UTF_8);
            // Isole chaque bloc d'objet JSON { ... }
            Matcher blocMatcher = Pattern.compile("\\{([^{}]*)\\}").matcher(contenu);

            // Parcourt chaque objet utilisateur trouvé
            while (blocMatcher.find()) {
                // Récupère le contenu textuel du bloc courant
                String bloc = blocMatcher.group(1);
                // Extrait l'identifiant
                String id = extraireChampJson(bloc, "identifiant");
                // Extrait le nom complet
                String nom = extraireChampJson(bloc, "nom");
                // Extrait le login
                String login = extraireChampJson(bloc, "login");
                // Extrait le mot de passe
                String mdp = extraireChampJson(bloc, "motDePasse");
                // Extrait le rôle sous forme de texte
                String roleTexte = extraireChampJson(bloc, "role");

                // Convertit le rôle texte en constante de l'énumération UserRole
                UserRole role = UserRole.valueOf(roleTexte);
                // Instancie l'utilisateur et l'ajoute à la liste
                liste.add(new User(id, nom, login, mdp, role));
            // Fin de la boucle sur les blocs utilisateurs
            }
        // Capture les erreurs de lecture ou de format
        } catch (IOException | IllegalArgumentException e) {
            // Affiche un message d'erreur explicite
            System.err.println("Erreur lors du chargement des utilisateurs : " + e.getMessage());
        // Fin du bloc try-catch
        }

        // Retourne la liste des utilisateurs chargés
        return liste;
    // Fin de la méthode chargerUtilisateurs
    }
// Fin de la classe StorageService
}