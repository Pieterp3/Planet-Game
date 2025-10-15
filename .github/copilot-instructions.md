# Planet Game - AI Coding Assistant Instructions

## Architecture Overview

This is a sophisticated real-time strategy game built with Java Swing featuring a **multi-threaded, entity-based architecture**:

### Core Threading Model
- **Main Thread**: Swing EDT handles all UI rendering and input events (`GameMenu`, `PauseMenu`, etc.)
- **Game Thread**: Engine runs at exactly 60 TPS (`Engine.java` - `game.tick()` every ~16.67ms)
- **Critical Rule**: **NEVER modify game state from Swing thread** - all game logic runs on Engine thread

### Central Coordination Layer
- **Main.java**: Entry point with stdout redirection to `logs.txt` and shutdown hooks for save operations
- **GameFrame.java**: Swing window coordinator managing menu state transitions with proper cleanup
- **Game.java**: Central game coordinator owning ALL entity collections and system managers
- **Engine.java**: Precise 60 TPS loop with pause/resume capability and slow-motion targeting mode

### Entity Management Architecture
```java
// Game.java manages ALL entity collections with thread-safe patterns
private List<Planet> planets;      // Orbital mechanics, ownership, health
private List<Ship> ships;          // Movement, combat targeting, interception
private List<Projectile> projectiles; // Ballistic physics, collision detection
private List<Bot> bots;            // AI decision making, strategic behavior
```

### System Manager Pattern
- **CombatManager**: Handles ship-to-ship combat with complex engagement rules and projectile physics
- **AbilityManager**: Multi-operator ability effects with cooldowns, duration tracking, and cross-operator state
- **ChallengeManager**: Event-driven achievement system with delayed persistence and notification queuing
- **PlayerData**: Singleton persistent progression with exponential upgrade costs and save/load serialization

### Key Data Flow Architecture
```
Input Layer:     GameMenu (Mouse/Keys) → Event Queue → Game State Validation
Game Logic:      Engine.tick() → Entity.tick() → Manager.update() → State Changes
Persistence:     Game Events → ChallengeManager → Delayed Save (2s buffer)
Rendering:       Entity Collections → Artist Classes → Swing Graphics2D
Notifications:   Achievement Events → Notification Queue → GameMenu Overlay
```

## Critical Architectural Patterns

### Singleton Pattern Usage (Persistent State)
```java
// These singletons manage cross-session persistence
PlayerData.getInstance()      // Coins, upgrades, abilities, best times → player_data.dat
ChallengeManager.getInstance() // Achievement progress, career stats → challenge_data.dat  
VisualSettings.getInstance()   // Display preferences, planet colors → visual_settings.dat
```

### Entity Lifecycle Management
```java
// CRITICAL: Always use ArrayList copy in tick loops to prevent ConcurrentModificationException
for (Ship ship : new ArrayList<>(ships)) {
    ship.tick(); // May call operator.removeShip(ship) during iteration
}

// Entity addition/removal ONLY through Game methods:
game.addShip(ship);           // Sets game reference, adds to collection
game.removeShip(ship);        // Removes from game AND combat manager
operator.removeShip(ship);    // Calls game.removeShip() + operator cleanup
```

### Coordinate System & Movement Physics
- **Origin**: (0,0) at top-left, positive Y goes down (standard Swing coordinates)
- **Distance Calculations**: Always use `Math.hypot(dx, dy)` for precise 2D distance
- **Angular Movement**: Direction angles in radians, movement via `Math.cos(angle)` and `Math.sin(angle)`
- **Collision Detection**: Entity size constants in `GameConstants` (SHIP_SIZE=8, PLANET_SIZE=35)

### Thread Safety Boundaries
```java
// SWING THREAD (EDT) - UI only, no game state modification
public void paintComponent(Graphics g) { /* Safe to read game state */ }
public void mouseClicked(MouseEvent e) { /* Queue actions, don't modify directly */ }

// GAME THREAD (Engine) - All game logic modifications
public void tick() { /* Safe to modify any game state */ }
```

## Development Workflows

### Build & Run
```bash
# Compile (VS Code handles automatically via .vscode/settings.json)
# Run: Use F5 or Run.bat which executes: java -jar "Planet Game.jar"
```

### Project Structure Rules
- **src/**: All source code with package structure matching directories
- **bin/**: Compiled output (auto-generated, don't edit)
- **lib/**: JAR dependencies referenced in .vscode/settings.json
- **logs.txt**: Runtime output (stdout redirected in Main.java)

### Key File Patterns
- **Artist classes**: Handle rendering specific entity types (`PlanetArtist`, `ShipArtist`, `BackgroundArtist`)
- **Menu classes**: Extend JPanel with mouse/key listeners (`GameMenu`, `PauseMenu`, `ShopMenu`)
- **Manager classes**: Handle cross-cutting concerns (`CombatManager`, `AbilityManager`)

## Entity System Deep Dive

### Ship Behavior Architecture
```java
// Ship.tick() handles ONLY movement - combat managed separately by CombatManager
public void tick() {
    if (!isStationary()) {  // CombatManager controls stationary state
        move();             // Interception targeting + planetary prediction
        checkDestination(); // Planet collision and damage dealing
    }
}

// Combat targeting priority: Interception > Planetary prediction
private Ship findInterceptionTarget() {
    // Ships target enemies attacking their origin planet (defensive behavior)
    // NOT random enemies - this is key to strategic gameplay
}
```

### Combat System State Machine
```java
// CombatManager maintains CombatState per ship with complex validation
class CombatState {
    boolean inCombat;           // Currently engaged in combat
    Ship combatTarget;          // Current enemy being fought  
    long lastShotTime;          // Rate limiting for projectile firing
}

// Combat engagement rules:
// 1. Ships only fight enemies attacking their origin planet
// 2. Combat distance: 80 units engagement, 120 units disengagement (hysteresis)
// 3. Combat validation: target must be valid threat, within range, approaching
```

### Planet Production & Orbital Mechanics
```java
// Planet.tick() manages:
private double shipsPerSecond;     // Production rate (affected by abilities/upgrades)
private double shipProgress;       // Accumulator for fractional ship production
private OrbitParameters orbit;     // Elliptical orbital motion parameters

// Orbital system: Planets orbit center star with unique elliptical paths
// Z-index system prevents collision detection - visual layering only
```

### Projectile Ballistics System
```java
// Projectile.tick() implements realistic ballistics
private void updatePosition() {
    x += velocity.x; // Linear trajectory, no gravity
    y += velocity.y;
    
    // Range limiting prevents infinite projectiles
    if (distanceTraveled > GameConstants.getProjectileMaxRange()) {
        game.removeProjectile(this);
    }
}
```

### Ability System State Tracking
```java
// AbilityManager maintains complex multi-operator state
private Map<Operator, Boolean> operatorFreezeActive;     // Per-operator ability effects
private Map<Operator, Map<Planet, Long>> operatorCursedPlanets; // Nested state tracking
private Map<Operator, List<BlackHole>> operatorBlackHoles;      // Entity collections per operator

// Backward compatibility: Player abilities also stored in legacy boolean fields
// Bot abilities use ONLY the Map<Operator, ...> system
```

## Common Modification Patterns

### Adding New Entity Types
1. Create entity class with `tick()` method
2. Add collection to `Game.java`
3. Add update loop in `Game.tick()`
4. Create corresponding Artist class for rendering
5. Add to GameMenu paint method

### Game Balance Changes
- Modify values in **GameConstants.java** (never hardcode numbers)
- Player progression in **PlayerData.java** upgrade cost calculations
- Bot difficulty scaling in **Difficulty.java** enum

### UI Extensions
- Extend JPanel for new menus
- Implement mouse/key listeners for interaction
- Add menu switching logic to **GameFrame.java**
- Follow existing pattern: remove all components, add new, revalidate/repaint

## Combat System Mechanics

### Ship-to-Ship Combat
- **CombatManager** handles all combat logic separate from Ship movement
- Combat triggers when ships are within `COMBAT_ENGAGEMENT_DISTANCE` (80 units)
- Ships target enemies attacking their origin planet, not random enemies
- Combat states track: `combatTarget`, `lastShotTime`, `inCombat` boolean

### Projectile System
```java
// Combat flow: Ship fires → Projectile created → Collision detection → Damage application
// Key constants: PROJECTILE_SPEED (5.0), PROJECTILE_MAX_RANGE (150), SHIP_FIRE_RATE (500ms)
```

### Combat Validation
- Ships only fight enemies targeting their home planet (defensive behavior)
- Combat exits when target moves away or is destroyed
- Distance checks use `Math.hypot()` for precise 2D calculation

## Progression & Upgrade System

### PlayerData Persistence
- **Singleton pattern**: `PlayerData.getInstance()` loads from `player_data.dat`
- **Exponential upgrade costs**: Level 0→1 costs ~10 coins, Level 29→30 costs ~400 coins
- **Difficulty-based rewards**: EASY (0.5x), MEDIUM (1.0x), HARD (1.5x), EXTREME (2.0x)

### Upgrade Application
```java
// Upgrades applied via multipliers when entities are created, not as permanent stat changes
double shipSpeedMultiplier = playerData.getUpgradeMultiplier(UpgradeType.SHIP_SPEED);
double newSpeed = GameConstants.getDefaultShipSpeed() * shipSpeedMultiplier;
```

### Ability System
- **AbilityManager** tracks cooldowns, active effects, and multi-operator abilities
- Abilities have unlock requirements and upgrade levels (like freeze, shield, factory hype)
- Complex abilities: BlackHoles (positional with event horizon), Planetary Flame (area effect)

## Challenge/Achievement System

### ChallengeManager Architecture
- **Singleton pattern**: `ChallengeManager.getInstance()` tracks player achievements across game sessions
- **Event-driven updates**: Game events trigger challenge progress via methods like `onGameWon()`, `onPlanetCaptured()`, `onAbilityUsed()`
- **Delayed persistence**: Uses 2-second delay before saving to avoid excessive file I/O during active gameplay
- **Shutdown hook**: `Main.java` ensures challenge progress is saved when program exits

### Achievement Notifications
```java
// Two notification types displayed in GameMenu overlay
AchievementNotification: Challenge completed (5-second display)
ProgressNotification: Challenge progress update (3-second display)
```

### Challenge Integration Points
- **Game.java**: Reports game outcomes and major events to ChallengeManager
- **ShopMenu.java**: Gold donation challenges tracked via `onGoldDonated()`
- **AchievementMenu.java**: Displays all challenges and completion status
- **GameMenu.java**: Renders notification overlays during gameplay

## Game Generation & Difficulty Scaling

### Procedural Generation Pipeline
```java
// GameGenerator.generate() creates balanced scenarios
1. Difficulty analysis → Bot count, planet count, ownership ratios
2. Operator creation → Player + N bots with difficulty-scaled behavior
3. Planet generation → Orbital mechanics + strategic placement + type distribution
4. Ownership assignment → Player guarantee + enemy ratio + neutral remainder
```

### Difficulty Scaling System
```java
// Difficulty enum contains comprehensive scaling parameters
EASY(3000ms, 0.5, 0.8, 1-2 bots, 5-8 planets, 30% enemies, no bot abilities)
MEDIUM(2000ms, 0.7, 1.0, 2-3 bots, 8-12 planets, 50% enemies, no bot abilities)  
HARD(1200ms, 1.1, 1.3, 3-5 bots, 12-16 planets, 70% enemies, bot abilities enabled)
EXTREME(800ms, 1.8, 1.8, 4-6 bots, 16-20 planets, 80% enemies, bot abilities enabled)

// Parameters affect: decision speed, aggression, targeting accuracy, planet distribution
```

### Visual Settings & Customization
```java
// VisualSettings.getInstance() manages display preferences
private boolean displayConnectionLines = true;    // Ship targeting arrows
private boolean displayEffects = true;           // Particle effects
private boolean displayProjectiles = true;       // Combat projectiles  
private Color playerPlanetColor = Color.BLUE;    // 16 predefined color options
private float connectionLineOpacity = 0.6f;      // Transparency control
```

## Persistence & Save System Architecture

### Multi-File Persistence Strategy
```java
// Three separate save files for different data lifecycles:
player_data.dat      // PlayerData: Coins, upgrades, best times, ability unlocks
challenge_data.dat   // ChallengeManager: Achievement progress, career stats
visual_settings.dat  // VisualSettings: Color preferences, display toggles
```

### Challenge System Event Integration
```java
// ChallengeManager integrates throughout game systems:
Game.tick() → checkWinCondition() → challengeManager.onGameWon()
Planet.takeDamage() → onPlanetCaptured() → challenge progress tracking
AbilityManager.activateAbility() → onAbilityUsed() → usage counting
ShopMenu.donate() → onGoldDonated() → donation challenge progress

// Delayed save mechanism (2s buffer) prevents excessive I/O during gameplay
```

### Save State Management
```java
// Critical save points with proper cleanup:
1. Main.java shutdown hook → ChallengeManager.forceSave()
2. GameFrame.windowClosing() → ChallengeManager.forceSave()
3. Game.stop() → challengeManager.forceSave() before engine stop
4. Periodic delayed saves during gameplay via updateSaveState()
```

## UI Architecture & Event Handling

### Menu State Machine
```java
// GameFrame manages menu transitions with proper cleanup
openGameMenu(difficulty) → Clean previous + Create Game(difficulty) + GameMenu
openMainMenu() → Clean previous + Create MainMenu + BackgroundArtist  
openShop() → Clean previous + Create ShopMenu + Upgrade/Ability purchasing
openAchievements() → Clean previous + Create AchievementMenu + Challenge display
```

### Artist Rendering Pipeline
```java
// Specialized Artist classes handle entity rendering:
PlanetArtist → Orbital animation, health bars, visual effects, planet features
ShipArtist → Movement trails, combat states, operator colors, selection indicators  
BackgroundArtist → Starfield, central star, orbital paths, UI backgrounds
EffectsArtist → Ability effects, notifications, particle systems, black holes
```

### Input Event Processing
```java
// GameMenu input handling with game state validation:
mouseClicked() → Planet selection → Ship deployment validation → Game state update
keyPressed() → Ability activation → Cooldown/unlock checking → AbilityManager.activate()
mouseMoved() → Hover detection → Tooltip display → UI feedback updates
```

## Debugging Workflows

### Output Redirection System
- **All console output** redirected to `logs.txt` in Main.java
- Engine state changes logged: "Engine started", "Engine paused", "Engine stopped"
- Error logging in Artist classes for rendering issues

### Debug Constants
```java
// In GameConstants.java - toggle for testing
public static final boolean INVINCIBLE_FOR_DEBUGGING = true; // Currently enabled
public static final int DEBUG_COINS_MULTIPLIER = 1; // Increase for testing progression
```

### Development Debugging
- VS Code F5 runs compiled JAR (not direct source)
- Check `logs.txt` for runtime output and engine state
- Ship targeting logic has verbose debug prints (may need enabling)
- **stdout redirection commented out in Main.java** - uncomment to redirect console output to logs.txt
- Exit code 1 typically indicates unhandled exceptions - check logs.txt for details

## Build & Deployment Processes

### VS Code Java Setup
```json
// .vscode/settings.json configures build
{
    "java.project.sourcePaths": ["src"],
    "java.project.outputPath": "bin", 
    "java.project.referencedLibraries": ["lib/**/*.jar"]
}
```

### Build Process
1. **Automatic compilation**: VS Code compiles to `bin/` on save
2. **JAR creation**: Manual process creates `Planet Game.jar` from bin contents
3. **Distribution**: `Run.bat` executes the JAR for end users

### Runtime Execution
```powershell
# Development: F5 in VS Code (runs compiled classes from bin/)
# Production: Run.bat or direct: java -jar "Planet Game.jar"  
# Manual run: java -XX:+ShowCodeDetailsInExceptionMessages -cp bin Main
# All output redirected to logs.txt automatically
```

### Dependency Management
- External JARs placed in `lib/` directory
- Referenced automatically via VS Code settings
- No Maven/Gradle - simple classpath-based project

## Advanced Combat Mechanics Deep Dive

### Combat Engagement State Machine
```java
// CombatManager implements sophisticated engagement logic:
1. Target Discovery → findNearbyEnemyShip() → Only enemies attacking origin planet
2. Engagement Validation → isValidCombatTarget() → Range, operator, destination checks
3. Combat State Tracking → CombatState per ship → inCombat, target, lastShot timing
4. Projectile Management → Fire rate limiting → Ballistic trajectory calculation
5. Disengagement Logic → Target distance/approach validation → Hysteresis prevention
```

### Ship Movement & Targeting Intelligence
```java
// Ship.move() priority system:
Priority 1: findInterceptionTarget() → Defend origin planet from attackers
Priority 2: predictPlanetPosition() → Account for orbital mechanics in targeting
Priority 3: Direct movement → Linear path when no interception needed

// Combat vs Movement separation:
CombatManager.updateCombat() → Sets stationary flag → Ship.tick() respects flag
Ship.tick() → Pure movement logic → No combat decision making
```

### Projectile Physics & Collision
```java
// Projectile system implements realistic ballistics:
Projectile(origin, target, speed) → Calculate trajectory vector
tick() → Linear movement + range tracking + collision detection
Collision → Damage application + Ship.takeDamage() + removal from game
Range limit → Prevents infinite projectiles + performance optimization
```

## Ability System Architecture Deep Dive

### Multi-Operator Ability State Management
```java
// AbilityManager handles both Player and Bot abilities:
// Legacy Player fields (backward compatibility):
private boolean freezeActive;  // Direct boolean for player
// Modern Multi-operator Maps:
private Map<Operator, Boolean> operatorFreezeActive; // Per-operator tracking

// State lookup priority: Check operator maps first, fallback to player booleans
// This allows Bot abilities while maintaining save compatibility
```

### Complex Ability Effect Implementation
```java
// BlackHole ability demonstrates advanced state tracking:
private Map<Operator, List<BlackHole>> operatorBlackHoles; // Multiple per operator
BlackHole.tick() → Orbital damage calculation → Planet proximity detection
BlackHole rendering → Particle effects + event horizon visualization

// Planetary Infection ability:
private Map<Operator, Map<Planet, Long>> operatorInfectedPlanets; // Nested state
Infected planet → Temporary operator change → Ship production for infector
Infection expiry → Revert to original owner → Clean state tracking
```

### Ability Cooldown & Duration System
```java
// Dual timing system for abilities:
Map<AbilityType, Long> cooldowns;     // When ability can next be used
Map<AbilityType, Long> activeEffects; // When current effect expires

// Player upgrade system affects cooldowns:
double cooldownReduction = playerData.getUpgradeMultiplier(UpgradeType.ABILITY_COOLDOWN);
long adjustedCooldown = (long)(baseCooldown * (1.0 - cooldownReduction));
```

## Challenge System Event-Driven Architecture

### Challenge Progress Tracking Integration
```java
// Challenge events integrated throughout game systems:
Ship.takeDamage() → Planet capture → ChallengeManager.onPlanetCaptured()
Game.checkWinCondition() → Victory conditions → onGameWon() + time tracking
AbilityManager.activateAbility() → Usage counting → onAbilityUsed(type, operator)
PlayerData.purchaseUpgrade() → Progression → onUpgradePurchased() + onGoldSpent()
```

### Achievement Notification System
```java
// Dual notification queues in ChallengeManager:
Queue<AchievementNotification> pendingNotifications;         // 5-second display
Queue<ProgressNotification> pendingProgressNotifications;    // 3-second display

// GameMenu rendering integration:
paintComponent() → Check notification queues → Render overlays + timers
Notification expiry → Remove from queue → Clean UI state
```

### Save State Buffering System
```java
// Delayed save mechanism prevents excessive I/O:
private boolean pendingSave = false;           // Save requested flag
private long lastSaveRequest = 0;              // Timestamp of request  
private static final long SAVE_DELAY_MS = 2000; // 2-second buffer

updateSaveState() → Check timer → Execute delayed save → Reset flags
Critical moments → forceSave() → Immediate save + skip buffer
```

### Dependency Management
- External JARs placed in `lib/` directory
- Referenced automatically via VS Code settings
- No Maven/Gradle - simple classpath-based project