# TabInputPlugin

Ein leichtgewichtiges und flexibles Minecraft-Spigot-Plugin, mit dem Spieler benutzerdefinierte **Tags** (Namenszusätze) im Tab-Menü erstellen, verwalten und anzeigen lassen können. Ideal für kleine Server, Communitys oder RPG-Umgebungen mit individuellen Spielerkennzeichnungen.

---

## Features

- Eigene Tags mit Farbe über Chatbefehl hinzufügen
- Temporäre Tags mit Ablaufzeit
- Tags wieder entfernen
- Automatische Gruppentags basierend auf Permissions (`group.<name>`)
- Farbige Tags mit Unterstützung für Minecraft `ChatColor`
- Easter Egg für den Tag `GHG` – Belohnung alle 5 Nutzungen

---

## Installation

1. Lade die neueste Version der `.jar`-Datei von [Releases](https://github.com/dein-benutzername/TabInputPlugin/releases) herunter.
2. Lege die Datei in den `plugins/`-Ordner deines Minecraft-Spigot-Servers.
3. Starte den Server neu oder führe `/reload` aus.
4. Optional: Passe die `config.yml` im Pluginordner an.

---

## Befehle

| Befehl | Beschreibung |
|--------|--------------|
| `/tag add <color> <tag>` | Fügt einen Tag mit Farbe hinzu |
| `/tag remove <tag>` | Entfernt einen vorhandenen Tag |
| `/tag temp <color> <tag> <dauer>` | Fügt einen temporären Tag hinzu (in Sekunden) |

**Beispiel:**
```bash
/tag add green Admin
/tag temp red Test 60
/tag remove Admin
