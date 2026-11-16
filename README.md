# AltarePartners

A standalone Paper plugin that advertises **Altare.gg** hosting to your players: it
broadcasts a rotating sponsor announcement every 10 minutes and ships a `/sponser`
menu with the free tier, Altare Plus and a link to the website.

## Requirements

| Item | Requirement |
| --- | --- |
| Server | Paper 1.16.5 or newer (including the year-based `26.x` releases) |
| Java | 11 or newer |
| Compiled against | Paper API 1.21.11, Java 11 bytecode |

The plugin is compiled to Java 11 bytecode and only uses API that exists on Paper
1.16.5, so the same jar runs on every version from 1.16.5 up to the current
year-based releases. Tested by compiling against both Paper 1.16.5 and Paper
1.21.11.

## Installation

1. Drop `AltarePartners-1.0.0.jar` into your server's `plugins/` folder.
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

## Configuration

```yaml
# Name inserted wherever <ServerName> appears in the announcements.
server-name: "Your Server"

# Announcements are always enabled and rotate every 10 minutes.
announcements:
  prefix: "<#2fff6b><bold>ꜱᴘᴏɴꜱᴏʀ</bold></#2fff6b> <dark_gray>»</dark_gray>"
  sound: ENTITY_EXPERIENCE_ORB_PICKUP
```

- `server-name` replaces the `<ServerName>` placeholder (`%server_name%` works too).
- `announcements.prefix` is parsed as MiniMessage. On Paper versions without
  MiniMessage, `&` codes and `<#rrggbb>` hex colours still work.
- `announcements.sound` accepts either a Bukkit enum name
  (`ENTITY_EXPERIENCE_ORB_PICKUP`) or a vanilla sound key
  (`entity.experience_orb.pickup`). Use `none` to disable the sound.

## Building

Requires a **full JDK 21** (a JRE is not enough) and network access to
`repo.papermc.io`.

```bash
./gradlew build
```

The jar is written to `build/libs/AltarePartners-1.0.0.jar`.

## Project layout

```
src/main/java/com/altare/partners/
├── AltarePartners.java         plugin entry point
├── AnnouncementManager.java    built-in announcements, 10-minute rotation
├── AltarePartnersCommand.java  /altare admin command
├── SponsorCommand.java         /sponser command
├── SponsorGui.java             27-slot sponsor menu
└── Text.java                   MiniMessage + legacy fallback parser
src/main/resources/
├── config.yml
└── plugin.yml
```

## Licence

All rights reserved by the Altare network.
