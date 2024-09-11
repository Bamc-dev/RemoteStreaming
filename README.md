# StreamsFilesBack

Projet back permettant de streamer en simultané un fichier vidéo. Le front fonctionnant avec une implémentation de VLC, il y a juste besoin d'envoyer le lien ou se trouve le fichier afin de pouvoir voir la vidéo.

## Default Idea

Projet back permettant de streamer en simultané un fichier vidéo.

- Le back permet de recevoir les fichiers sour format de chunk.
- Utilise un serveur websocket afin d'avoir une connexion direct avec les controles vidéos, et les fichiers en simultanés
- Il s'agissait d'un projet permettant de regarder du contenu sans perte de qualité avec différentes personnes à distance.

## How to Setup

### Prérequis

- [Java]([https://dotnet.microsoft.com/download](https://www.oracle.com/fr/java/technologies/downloads/#java17)) version 17 ou supérieure.
- [RemoteStreamingFront](https://github.com/Bamc-dev/StreamsFiles)
- [Maven](https://maven.apache.org/download.cgi) A ajouté à votre PATH si vous avez plusieurs projets Maven

### Installation

```bash
# Clonez le dépôt
git clone https://github.com/Bamc-dev/StreamsFiles.git

# Allez dans le répertoire du projet
cd RemoteStreaming

# Construisez le projet
mvn spring-boot:run
```

Ensuite vous aurez comme les URL suivantes de disponible :

- URL Websocket : ws://{votre-ip-de-serveur}:4532/socket
- URL API : http://{votre-ip-de-serveur}:4532

## Améliorations
Ajouter de la documentation, et réfléchir a peut être un autre système afin de partagé les fichiers vidéos à l'aide de FFMPEG afin de convertir les MKV vers des MP4, et ensuite de créer un flux vidéo séparé en chunk.
Cela permettrait d'étendre le front vers une application WEB, plutôt qu'une application bureau
