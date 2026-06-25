# Particle Scale

Eine Fabric-Mod für **Minecraft 1.21.11**, mit der du über ein GUI die Größe
**jedes Partikel-Typs einzeln** einstellen kannst (Flamme, Rauch, Funken, ...).

## Features

- GUI mit je einem Schieberegler pro Partikel-Typ
- Suchfeld zum Filtern der Partikel
- Faktor von **0.0x bis 5.0x** (0 = unsichtbar, 1 = Original)
- Änderungen wirken sofort und werden automatisch gespeichert
  (`config/particlescale.json`)
- "Alle zurücksetzen"-Knopf

## Steuerung

- Standard-Taste zum Öffnen des GUIs: **P**
  (im Spiel unter *Optionen → Steuerung → Particle Scale* änderbar)

## Bauen

Du brauchst **JDK 21**. Dann im Projektordner:

```bash
./gradlew build
```

Die fertige Mod liegt danach unter:

```
build/libs/particlescale-1.0.0.jar
```

## Installieren

1. [Fabric Loader](https://fabricmc.net/use/) für 1.21.11 installieren
2. [Fabric API](https://modrinth.com/mod/fabric-api) (Version `0.141.4+1.21.11`
   oder neuer) in den `mods`-Ordner legen
3. `particlescale-1.0.0.jar` ebenfalls in den `mods`-Ordner legen
4. Minecraft mit dem Fabric-Profil starten

## Im Entwicklungsmodus testen

```bash
./gradlew runClient
```

## Technischer Hinweis

Die Skalierung greift an `BillboardParticle#getSize` an – das ist die
Basisklasse fast aller sichtbaren Partikel. Jeder Partikel wird beim Erzeugen
(`ParticleManager#createParticle`) mit seinem Typ markiert, damit der richtige
Faktor angewendet werden kann. Einzelne Spezial-Partikel, die nicht von
`BillboardParticle` erben, werden nicht skaliert.
