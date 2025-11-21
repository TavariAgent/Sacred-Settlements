# Sacred Settlements

A NeoForge mod that adds protected, purchasable villages with an emerald-based economy and taxation system.

## Features

### 🛡️ Village Protection
- Configurable protection radius around village bells (16-500 blocks)
- Prevents block breaking and placement by non-owners
- Protects villagers and iron golems from damage
- Admin override permissions for server management

### 💎 Ownership System
- Purchase villages with emeralds (configurable price, default: 64)
- Owner-only access to village treasury
- Clean GUI showing ownership stats and emerald balance
- Real-time ownership timer

### 💰 Tax System
- Configurable hourly tax rate (default: 0.5 emeralds/hour)
- Automatic tax collection at configurable intervals (default: every 12 hours)
- Built-in grace period - villages abandon when treasury runs dry
- Visual tax rate display in GUI

### 🎮 Commands
- `/sacred unstuck` - Teleport to the nearest village bell when stuck

## Configuration

All settings are in `config/sacredsettlements-protection.toml`:
```toml
[Village Protection Settings]
    # Radius in blocks around village bells that will be protected
    # Range: 16 ~ 500
    protectionRadius = 64
    
    # Emeralds deducted per hour (displayed rate, actual collection happens at interval)
    # Range: 0.0 ~ 10.0
    taxRatePerHour = 0.5
    
    # Hours between tax collections (default: 12 hours)
    # Range: 1 ~ 168
    taxCollectionHours = 12
    
    # Number of emeralds required to purchase a village
    # Range: 1 ~ 2304
    villagePurchasePrice = 64
```

## Usage

### Purchasing a Village
1. Find a village with a bell
2. Right-click the bell to open the GUI
3. Ensure you have enough emeralds (default: 64)
4. Click the "Purchase" button
5. The village is now yours!

### Managing Your Village
- Right-click the bell to access the treasury
- Add emeralds to pay taxes
- Only you can modify blocks within the protected radius
- Monitor your ownership time and tax rate in the GUI

### Tax System
- Default: 6 emeralds every 12 hours (0.5/hr × 12hr)
- If treasury runs empty, you have one grace period (12 hours default) to refill
- Village abandons if taxes remain unpaid

## For Server Admins

### Admin Permissions
- Level 2+ (ops): Can bypass village block protection
- Level 4+: Can break/move village bells

### Data Storage
- Village data saves to `world/data/sacredsettlements_villages.dat`
- Auto-saves every 5 minutes
- Saves on server shutdown

### Recommended Settings
- **Casual servers**: `taxRatePerHour = 0.1`, `taxCollectionHours = 24`
- **Balanced servers**: Default settings
- **Hardcore servers**: `taxRatePerHour = 1.0`, `taxCollectionHours = 6`

## Compatibility

- **Minecraft Version**: 1.21.1
- **Mod Loader**: NeoForge 21.1.215+
- **Side**: Server-side (client optional for GUI)

Works with village-enhancing mods! Protection is bell-centric, so modified villages are automatically protected.

## Planned Features (v1.1.0+)

- Village transfer/selling between players
- Tax payment reminders
- Villager employment tracking

## Support

Found a bug or have a suggestion? Open an issue on [GitHub](#) or comment on the Modrinth page!

## License
MIT

---

**Made with claude.ai by Bleepz**