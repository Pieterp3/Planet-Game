# Planet Game 🌌

A real-time strategy game built with Java Swing where you command planets to produce ships and battle against AI opponents across the galaxy.

## 🎮 Game Overview

Planet Game is a fast-paced RTS featuring:
- **Real-time planetary combat** with ship production and deployment
- **Strategic resource management** across multiple planets
- **Progressive upgrade system** with coins and abilities
- **Challenge system** with achievements and unlockables
- **Multiple difficulty levels** from Easy to Extreme
- **Special abilities** including Black Holes, Shields, and Planetary Infection

## 🚀 Quick Start

### Running the Game
```powershell
# Option 1: Use the batch file
./Run.bat

# Option 2: Run directly with Java
java -jar "Planet Game.jar"

# Option 3: Development mode (VS Code)
# Press F5 or use Run and Debug
```

### System Requirements
- **Java 17+** (Eclipse Adoptium recommended)
- **Windows** (developed for Windows, may work on other platforms)
- **Mouse and keyboard** for controls

## 🎯 How to Play

### Basic Controls
- **Left Click**: Select your planets
- **Right Click**: Send ships to target planets
- **Number Keys (1-9)**: Activate abilities
- **ESC**: Pause menu
- **Space**: Toggle slow-motion targeting mode

### Core Gameplay Loop
1. **Capture Planets**: Send ships from your planets to neutral or enemy planets
2. **Manage Production**: Planets automatically produce ships over time
3. **Strategic Combat**: Ships engage enemies en route to their targets
4. **Use Abilities**: Deploy special powers with cooldowns
5. **Earn Coins**: Win matches to unlock upgrades and new abilities

### Difficulty Levels
- **Easy**: Slower bots, fewer enemies, 0.5x coin rewards
- **Medium**: Balanced gameplay, 1.0x coin rewards
- **Hard**: Aggressive bots with abilities, 1.5x coin rewards  
- **Extreme**: Maximum challenge with smart AI, 2.0x coin rewards

## 🛠️ Development

### Project Structure
```
src/                    # Source code
├── Main.java          # Entry point with stdout redirection
├── home/
│   ├── frame/         # Swing UI components
│   │   ├── GameFrame.java     # Main window coordinator
│   │   ├── gamemenu/          # In-game UI and rendering
│   │   └── mainmenu/          # Menus and shop system
│   └── game/          # Core game logic
│       ├── Engine.java        # 60 TPS game loop
│       ├── Game.java          # Central game coordinator
│       ├── abilities/         # Special powers system
│       ├── combat/            # Ship-to-ship combat
│       ├── operators/         # Player and bot AI
│       └── planets/           # Planet entities and types
bin/                   # Compiled Java classes
lib/                   # External JAR dependencies
logs.txt              # Runtime output and debug info
```

### Architecture Highlights
- **Tick-based Engine**: 60 TPS game loop on separate thread
- **Entity-Component System**: Ships, Planets, Projectiles with tick() methods
- **Singleton Pattern**: PlayerData, ChallengeManager for persistence
- **Thread Safety**: Swing UI thread separate from game logic thread
- **Event-driven Challenges**: Achievement system tracks gameplay events

### Build Process
The project uses VS Code's built-in Java compilation:
1. Source files in `src/` auto-compile to `bin/`
2. JAR creation is manual process for distribution
3. No Maven/Gradle - simple classpath-based project

### Key Development Commands
```powershell
# Run in development mode with enhanced exceptions
java -XX:+ShowCodeDetailsInExceptionMessages -cp bin Main

# Check runtime logs
Get-Content logs.txt -Wait

# Clean build (delete bin folder to force recompilation)
Remove-Item -Recurse bin/
```

## 🏆 Features

### Upgrade System
- **8 Upgrade Types**: Ship damage, health, speed, spawn rate, and more
- **Exponential Costs**: Level 1 costs ~10 coins, Level 30 costs ~400 coins  
- **Multiplier Effects**: Upgrades apply as multipliers when entities are created

### Special Abilities
- **❄️ Freeze**: Stop enemy ship production
- **🚀 Missile Barrage**: Target random enemy planets  
- **🛡️ Shield**: Temporary invulnerability
- **⚡ Factory Hype**: 3x ship production speed
- **🔧 Improved Factories**: Double ship stats (speed, damage, health)
- **💚 Answered Prayers**: Heal all damaged planets
- **💜 Curse**: Reduce enemy ship stats by 20%
- **⚫ Black Hole**: Orbital damage to enemy planets
- **🔥 Planetary Flame**: Fire damage from player planets
- **🦠 Planetary Infection**: Convert enemy planets temporarily
- **⚔️ Unstoppable Ships**: Invulnerable ships for limited time

### Planet Types
- **🌍 Standard**: Balanced stats across all attributes
- **⚔️ Attack**: 50% more ship damage, extra targeting capability  
- **🛡️ Defense**: 50% more health and regeneration
- **💨 Speed**: 50% faster ships and ship production

### Challenge System
- **Progressive Unlocks**: Challenges unlock new abilities and upgrades
- **Achievement Tracking**: Persistent progress across game sessions
- **Reward System**: Coins and score for completing challenges
- **Notification System**: In-game popups for achievements and progress
- **Challenge Categories**: Time-based, ability usage, planet capture, progression streaks
- **Unlock Requirements**: Complete specific challenges to access new content

### Progression & Persistence  
- **Player Data**: Automatic save/load of upgrades, coins, and unlocked content
- **Visual Settings**: Customizable planet colors and UI preferences
- **Challenge Progress**: Achievement data persisted across sessions
- **Automatic Saving**: Progress saved on game exit and periodically during play

### Combat Mechanics
- **Engagement Distance**: Ships fight within 80 units of each other
- **Projectile System**: Realistic ballistics with range limits
- **Defensive Behavior**: Ships prioritize defending their origin planet
- **Real-time Combat**: No turns - continuous action at 60 TPS

## 🐛 Debugging

### Debug Features
- **Invincibility Mode**: Set `INVINCIBLE_FOR_DEBUGGING = true` in GameConstants
- **Coin Multiplier**: Increase `DEBUG_COINS_MULTIPLIER` for faster testing
- **Verbose Logging**: All output redirected to `logs.txt`
- **Engine State Logging**: Track engine start/stop/pause states

### Common Issues
- **Exit Code 1**: Check `logs.txt` for exception details
- **Performance Issues**: Monitor tick interval and entity counts
- **Save/Load Problems**: PlayerData and ChallengeManager persistence

## 📁 File Organization

### Critical Files
- **GameConstants.java**: All gameplay balance values
- **PlayerData.java**: Persistent progression and upgrades  
- **ChallengeManager.java**: Achievement system with delayed saves
- **Engine.java**: Core game loop at 60 TPS
- **Game.java**: Central coordinator for all entities and systems

### UI Components
- **Artist Classes**: Handle rendering (PlanetArtist, ShipArtist, etc.)
- **Menu Classes**: Extend JPanel with mouse/key listeners  
- **Manager Classes**: Cross-cutting concerns (CombatManager, AbilityManager)

## 📊 Game Statistics

### Performance
- **60 TPS Engine**: Consistent 60 ticks per second gameplay
- **Real-time Combat**: No turn-based delays, continuous action
- **Scalable Entities**: Handles hundreds of ships and projectiles
- **Efficient Rendering**: Optimized Swing graphics with entity pooling

### Technical Specs
- **Language**: Java 17+ with Swing UI framework
- **Architecture**: Multi-threaded with separated UI and game logic
- **Save Format**: Binary serialization for player data and settings
- **Logging**: Comprehensive debug output to `logs.txt`

## 🎨 Customization

### Visual Settings
- **Planet Colors**: Customize appearance of different planet types
- **UI Themes**: Adjustable color schemes for menus and HUD
- **Effects**: Toggle visual effects for performance optimization
- **Settings Persistence**: Visual preferences saved automatically

### Gameplay Modifications
- **Debug Mode**: Enable invincibility and coin multipliers for testing
- **Balance Tweaks**: All gameplay values centralized in `GameConstants.java`
- **Difficulty Scaling**: Adjustable AI behavior and reward multipliers

## 🚀 Installation & Distribution

### For Players
1. Download the latest release
2. Ensure Java 17+ is installed
3. Run `Run.bat` or execute: `java -jar "Planet Game.jar"`
4. No additional setup required - game creates save files automatically

### For Developers  
1. Clone the repository
2. Open in VS Code with Java Extension Pack
3. Project auto-configures via `.vscode/settings.json`
4. Press F5 to build and run in development mode

### Build Distribution
```powershell
# Create JAR from compiled classes (manual process)
# 1. Compile source files to bin/ (automatic in VS Code)
# 2. Package bin/ contents into Planet Game.jar
# 3. Test with Run.bat before distribution
```

## 🤝 Contributing

### Code Style
- Follow existing patterns in Artist, Menu, and Manager classes
- Use singleton pattern for persistent data (PlayerData, ChallengeManager)
- All game balance values go in `GameConstants.java`
- Thread safety: never modify game state from Swing thread

### Adding Features
1. **New Entities**: Implement `tick()` method and add to Game collections
2. **New Abilities**: Add to `AbilityType` enum and implement in `AbilityManager`  
3. **New Challenges**: Define in `ChallengeType` and add tracking to `ChallengeManager`
4. **UI Changes**: Follow JPanel extension pattern with proper event handling

## 🤖 AI Agent Instructions

For AI coding assistants working on this project, see [`.github/copilot-instructions.md`](.github/copilot-instructions.md) for detailed architectural guidance, conventions, and development workflows specific to this codebase.

## 📄 License

This project is distributed under the terms specified in the project files. See the project repository for license details.

---

**Planet Game** - Command the galaxy, one planet at a time! 🌍⚔️🚀
