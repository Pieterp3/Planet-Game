# Planet Game - GitHub Pages Deployment Setup Guide

## Overview
This guide walks you through deploying the Planet Game web version to GitHub Pages with persistent storage capabilities. The game uses IndexedDB for local storage that survives browser clearing and can be extended for server-side functionality.

## Prerequisites
- GitHub account
- Git installed on your computer
- Basic understanding of GitHub repositories

## Step-by-Step Deployment Process

### Step 1: Prepare Your Repository
1. **Create a new GitHub repository**:
   - Go to GitHub.com and create a new repository
   - Repository name: `planet-game` (or your preferred name)
   - Set to Public (required for GitHub Pages)
   - Initialize with README if desired

2. **Clone the repository locally**:
   ```bash
   git clone https://github.com/YOUR_USERNAME/planet-game.git
   cd planet-game
   ```

### Step 2: Upload Web Files
1. **Copy web directory contents**:
   - Copy ALL files from the `web/` directory to your repository root
   - Your repository structure should look like:
   ```
   planet-game/
   ├── index.html (copy from mainmenu.html)
   ├── mainmenu.html
   ├── gamemenu.html
   ├── shopmenu.html
   ├── achievements.html
   ├── helpmenu.html
   ├── settings.html
   ├── css/
   ├── js/
   └── components/
   ```

2. **Create index.html**:
   - Copy `mainmenu.html` and rename it to `index.html`
   - This ensures GitHub Pages loads your game by default

### Step 3: Configure GitHub Pages
1. **Enable GitHub Pages**:
   - Go to your repository on GitHub
   - Click Settings → Pages (in the left sidebar)
   - Under "Source", select "Deploy from a branch"
   - Select "main" branch and "/ (root)" folder
   - Click Save

2. **Wait for deployment**:
   - GitHub will provide a URL like: `https://YOUR_USERNAME.github.io/planet-game`
   - Initial deployment may take 5-10 minutes

### Step 4: Verify Persistent Storage
The game includes a comprehensive storage system that works automatically:

1. **IndexedDB Primary Storage**:
   - Automatically initializes when game loads
   - Stores: Player data, achievements, settings, challenge progress
   - Survives browser clearing, private browsing, cookie deletion

2. **localStorage Fallback**:
   - Automatic fallback if IndexedDB fails
   - Includes migration system from old localStorage saves

3. **Storage Features**:
   - **Player Data**: Coins, upgrades, abilities, best times
   - **Achievement System**: 46+ achievements with progress tracking
   - **Settings**: Display preferences, planet colors, audio settings
   - **Challenge Progress**: Career stats, completion tracking

### Step 5: Test Your Deployment
1. **Access your game**:
   - Visit `https://YOUR_USERNAME.github.io/planet-game`
   - Game should load with animated background

2. **Test persistence**:
   - Play a game, earn coins, unlock achievements
   - Close browser completely
   - Reopen and verify data is saved
   - Try clearing cookies - data should persist

3. **Test all features**:
   - ✅ Main menu navigation
   - ✅ Game difficulty selection
   - ✅ Shop (upgrades and abilities tabs)
   - ✅ Achievement system with real progress
   - ✅ Help page with all abilities/upgrades
   - ✅ Settings with planet colors
   - ✅ Responsive mobile design

## Advanced Setup Options

### Custom Domain (Optional)
1. **Purchase a domain** and configure DNS
2. **Add CNAME file** to repository root:
   ```
   yourgame.com
   ```
3. **Configure in GitHub Pages settings**

### Analytics Integration (Optional)
Add Google Analytics to track usage:
```html
<!-- Add to all HTML files before </head> -->
<script async src="https://www.googletagmanager.com/gtag/js?id=GA_MEASUREMENT_ID"></script>
<script>
  window.dataLayer = window.dataLayer || [];
  function gtag(){dataLayer.push(arguments);}
  gtag('js', new Date());
  gtag('config', 'GA_MEASUREMENT_ID');
</script>
```

## Storage System Architecture

### Current Implementation
```javascript
// Persistent Storage Structure
{
  playerData: {
    coins: Number,
    upgrades: Object,
    abilities: Object,
    bestTimes: Object
  },
  challengeData: {
    totalPlanetsCaptured: Number,
    totalAbilitiesUsed: Number,
    completedChallenges: Array,
    specificAbilityUsage: Map
  },
  visualSettings: {
    planetColor: String,
    displayOptions: Object
  }
}
```

### Future Server Integration
The storage system is designed for easy server integration:

1. **API Endpoints** (when you add a backend):
   ```javascript
   // Easy conversion to server calls
   await fetch('/api/save-player-data', {
     method: 'POST',
     body: JSON.stringify(playerData)
   });
   ```

2. **User Authentication**:
   - Add login system to sync across devices
   - Current IndexedDB becomes local cache
   - Server becomes source of truth

3. **Multiplayer Features**:
   - Leaderboards using achievement data
   - Challenge competitions
   - Social features

## Troubleshooting

### Common Issues

1. **Game doesn't load**:
   - Check browser console for errors
   - Verify all files are uploaded correctly
   - Ensure index.html exists

2. **Storage not working**:
   - Check if IndexedDB is supported (all modern browsers)
   - Verify console for storage errors
   - Test in incognito mode

3. **GitHub Pages not updating**:
   - Wait 5-10 minutes after pushing changes
   - Check GitHub Actions tab for deployment status
   - Clear browser cache

### Performance Optimization
1. **Enable compression** (GitHub Pages does this automatically)
2. **Optimize images** if you add any assets
3. **Minify JavaScript** for production (optional)

## Maintenance

### Updating Your Game
1. **Local changes**:
   ```bash
   git add .
   git commit -m "Update game features"
   git push origin main
   ```

2. **Automatic deployment**:
   - GitHub Pages automatically rebuilds
   - Changes live in 5-10 minutes

### Monitoring
- **GitHub Pages status**: Repository Settings → Pages
- **Usage analytics**: GitHub repository Insights → Traffic
- **Error monitoring**: Browser developer tools

## Security Considerations

### Client-Side Security
- All data stored client-side (secure by default)
- No sensitive information in code
- Cross-origin requests properly configured

### Future Server Security
When adding backend:
- HTTPS only (GitHub Pages provides this)
- Input validation on all API endpoints
- Authentication tokens with expiration
- Rate limiting for API calls

## Support and Updates

### Getting Help
- Check browser console for error messages
- Test in different browsers (Chrome, Firefox, Safari)
- Verify internet connection for GitHub Pages access

### Future Enhancements
The current architecture supports:
- **Multiplayer features** via WebSocket integration
- **Real-time leaderboards** with server backend
- **Cross-device sync** with user accounts
- **Progressive Web App** (PWA) features for mobile installation

## Success Metrics
After deployment, you should have:
- ✅ Fully functional web game accessible worldwide
- ✅ Persistent progression that survives browser clearing
- ✅ Complete achievement system with 46+ achievements
- ✅ Professional UI with responsive design
- ✅ All Java features ported and functional
- ✅ Mobile-friendly interface
- ✅ Zero-setup gameplay for users

Your Planet Game is now ready for global deployment! 🚀