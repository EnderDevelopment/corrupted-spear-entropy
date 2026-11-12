# Corrupted Spear of Entropy Minecraft Plugin

Corrupted Spear of Entropy is a Paper Minecraft plugin that adds a green custom spear with passive corruption effects, an offhand hook attack, a corruption domain, cooldown handling, potion effects, freezing, green particles, sounds, and a command for obtaining the spear.

## Install

1. Install Java 21.
2. Install a Paper 1.21.11 server.
3. Build the plugin with Maven:

```bash
mvn package
```

4. Copy the generated `target/plugin.jar` file into the server's `plugins` folder.
5. Start or restart the Paper server.
6. Make sure the server has the required Paper API version available through the server installation.

## Use

1. Give yourself permission to use the command:

```text
greenspear.give
```

2. Run:

```text
/spear
```

The command gives the player the Green Spear, which is implemented as a tagged trident.

### Passive corruption

Hold the Green Spear in your main hand and hit a living entity. Each hit has a 20% chance to activate the passive effect, provided the 30-second passive cooldown is ready.

The passive effect:

- Applies poison and weakness for 6 seconds.
- Applies strong slowness for 3 seconds.
- Creates green visual particles.
- Applies the passive cooldown to the player.

### Hook attack

Place the Green Spear in your offhand and right-click while not sneaking.

The plugin targets the living entity directly in front of the player within 20 blocks. The attack:

- Deals 8 points of damage, equal to 4 hearts.
- Applies poison and weakness for 10 seconds.
- Applies strong slowness for 3 seconds.
- Creates green visual particles.
- Plays a fishing hook retrieval sound.
- Uses a 25-second cooldown.

### Corruption domain

Place the Green Spear in your offhand and right-click while sneaking.

The domain:

- Lasts for 30 seconds.
- Applies poison and weakness to nearby living entities every second.
- Affects entities within approximately 6 blocks horizontally and 3 blocks vertically.
- Displays green dust particles and plays a beacon activation sound.
- Uses a 4 minute 30 second cooldown.

While the player is inside an active domain, the plugin currently multiplies registered ability cooldown durations by two. The domain does not currently prevent entities from leaving its area.

The plugin uses Minecraft potion effects and particles for corruption, freezing, and visual feedback. The supplied model and texture files are included as project assets, but the plugin does not automatically install or apply them through a resource pack.

---

More Minecraft plugin projects by EnderDevelopment: https://github.com/EnderDevelopment

Project page: https://dash.enderdevelopment.com/community/a118dac0-d9ab-4c47-8899-c8979a111b96?utm_source=github&utm_medium=readme&utm_campaign=auto_publish&utm_content=a118dac0-d9ab-4c47-8899-c8979a111b96
