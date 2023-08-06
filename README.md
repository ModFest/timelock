# Timelock

Create a **zone** with `timelock zone <zone_id> set <ticks> <offset>`, where:

- `zone_id` is the ID of the zone,
- `ticks` is a number in the range 0-24000 for the amount of ticks, and
- `offset` is whether the zone should use the world time and offset it or lock it to the ticks specified

> **Example**: `timelock zone timelock:my_zone 12000 true`

You can change a zone's parameters at any time by running the `set` subcommand again. Delete a zone using the `delete` subcommand and use the `info` subcommand to inspect a zone.

To edit the chunks in a zone, run `timelock start <zone_id>`. Modify the selection by right-clicking any block in a chunk. When you are ready to commit your changes, run `timelockc commit`. Run `timelockc abort` to discard your changes.

## License

MIT © ModFest 2023