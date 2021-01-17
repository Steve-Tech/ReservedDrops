# Reserved Drops
Only the player who died, or the player that killed the mob can pickup their/its items.
## Config
```yaml
# Config File for ReservedDrops
timeout: 60 # Timeout in seconds before items can be picked up by anyone
reserve-mob-drops: true
allow-entity-pickup: false # Mobs such as Zombies can pick up items
```
## Commands
- ReservedDrops:
  - Description: ReservedDrops reload command
  - Usage: /\<command\> \[reload\]
  - Permission: ReservedDrops.reload
## Permissions
- ReservedDrops.bypass:
  - Description: Bypass Reserved Drops
  - Default: false
- ReservedDrops.reload:
  - Description: ReservedDrops reload command
  - Default: op