# AltarePartners

A standalone plugin that advertises **Altare.gg** hosting to your players: it
broadcasts a rotating sponsor announcement every 10 minutes and ships a `/sponser`
menu with the free tier, Altare Plus and a link to the website.

Two jars are built from this repository, because Minecraft 1.8.8 and modern Paper
share almost no UI API.

## Which jar do I need?

| Server | Jar | Java |
| --- | --- | --- |
| Spigot / Paper 1.8.8 – 1.15.x | `AltarePartners-Legacy-1.1.0.jar` | 8+ |
| Paper 1.16.5 – 26.x | `AltarePartners-1.1.0.jar` | 11+ |

Install exactly one of them. Both use the same plugin name, commands, permissions
and `plugins/AltarePartners/config.yml`, so you can swap jars without losing your
settings.

- **Legacy jar** — legacy colour codes, Bungee chat click events, pre-1.13 material
  names with automatic fallbacks, Java 8 bytecode. Compiles against the Spigot 1.8.8
  API and the exact chat library that 1.8.8 bundles.
- **Modern jar** — Adventure components and MiniMessage with a legacy fallback,
  Java 11 bytecode. Compiles against Paper 1.21.11 and only uses API that also
  exists on Paper 1.16.5, so one jar spans 1.16.5 to the year-based releases.

## Installation

1. Drop the jar for your server version into `plugins/`.
2. Restart the server.
3. Edit `plugins/AltarePartners/config.yml` if you want to change the branding.
4. Run `/altare reload`.

## Commands

| Command | Permission | Description |
| --- | --- | --- |
| `/sponser` | `altarepartners.sponsor` (default: everyone) | Opens the Altare sponsor menu |
| `/altare reload` | `altarepartners.admin` (default: op) | Reloads `config.yml` and restarts the rotation |
| `/altare list` | `altarepartners.admin` | Lists the built-in announcements |
| `/altare preview [id\|number]` | `altarepartners.admin` | Shows one announcement to yourself |
| `/altare test announce` | `altarepartners.admin` | Sends a random announcement right now |

`/altare test announce` sends the announcement to you when run by a player, and
broadcasts it to everyone when run from the console. `/altare` also answers to
`/altarepartners`, `/partners` and `/ap`.


## Project layout

```text
src/main/java/com/altare/partners/          modern build (Adventure / MiniMessage)
├── AltarePartners.java         plugin entry point
├── AnnouncementManager.java    built-in announcements, 10-minute rotation
├── AltarePartnersCommand.java  /altare admin command
├── SponsorCommand.java         /sponser command
├── SponsorGui.java             27-slot sponsor menu
└── Text.java                   MiniMessage + legacy fallback parser

legacy/src/main/java/com/altare/partners/legacy/   legacy build (1.8.8)
├── LegacyPlugin.java           plugin entry point
├── LegacyAnnouncements.java    built-in announcements, 10-minute rotation
├── LegacyAdminCommand.java     /altare admin command
├── LegacySponsorCommand.java   /sponser command
├── LegacySponsorGui.java       27-slot sponsor menu
└── LegacyText.java             MiniMessage tags to legacy colour codes
```

## Building

Requires a **full JDK 21** (a JRE is not enough) and network access to
`repo.papermc.io` and `hub.spigotmc.org`.

```bash
./gradlew build
```

```text
build/libs/AltarePartners-1.1.0.jar                 modern build (Paper 1.16.5+)
legacy/build/libs/AltarePartners-Legacy-1.1.0.jar   legacy build (Spigot 1.8.8+)
```

## Licence

All rights reserved by Serene (And Altare).
