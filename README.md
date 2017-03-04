# Help commands

## ?help

Display help commands


## ?ping

Responds with a basic ping request

----

### Music

----

## ?add \<query/url\>

Searches query or adds url to the end of the queue

## ?add playlist \<query/url\>

Searches query or adds url to the end of the queue.
Url must be a playlist url

## ?insert \<query/url\>

Searches query or adds url to the front of the queue

## ?insert playlist \<query/url\>

Searches query or adds url to the front of the queue.
Url must be a playlist url

## ?volume \<amount\>

Sets the music volume. (max 100)

## ?pause

Pauses the music player

## ?resume

Unpauses the music player

## ?skip

Skips the current playing song

## ?leave

Clear the queue, stop playing and leave the current voice channel.

----

### Playlist and Queue

----

## ?queue

Display's the current playing songs and a few other songs in queue

## ?remove \<start\> - \<end\>

Remove tracks **start** to **end** in queue.

## ?shuffle

Shuffles the queue

## ?clear

Deletes and clears all songs in queue. (Does not affect current playing)

----

### Seeking

----

```markdown
Position = = Youtube Time format (ex: **2m3s** **120**(defaults to seconds))
```

## ?seek \<position\>

Seeks to that position in the current playing track.

Ex: `?seek 1m5s`

## ?rewind \<position\>

Rewind current playing track by position amount

Ex: `?rewind 5s`

## ?forward \<position\>

Same as rewind, except fast forwards by position amount

Ex: `?forward 5s`
