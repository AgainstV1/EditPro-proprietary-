# Agent access (MCP)

Edit Pro Studio can expose the open project to an AI agent over the
[Model Context Protocol](https://modelcontextprotocol.io), so the agent can read the timeline and
make real edits instead of guessing at them.

It is **off until you start it**, and it is **editing-only**: there is no export trigger and no
timeline-wide clear, so an agent cannot destroy a project or start a long render.

## Turning it on

**Tools → Agent access (MCP) → Start.**

The panel shows:

- the **endpoint** — `http://127.0.0.1:8765/mcp`
- the **pairing token** — eight characters, generated once and stored on the device
- a **recent activity** log of what the agent has done

`New token` rotates the token and restarts the server; any client holding the old one is cut off.

## Connecting a client

The server binds to **127.0.0.1 only**, so the MCP client must run on the same tablet. Point it at
the endpoint with the token:

```json
{
  "mcpServers": {
    "editpro": {
      "type": "http",
      "url": "http://127.0.0.1:8765/mcp",
      "headers": { "Authorization": "Bearer YOUR-TOKEN" }
    }
  }
}
```

Clients that cannot send headers can pass the token in the query string instead:
`http://127.0.0.1:8765/mcp?token=YOUR-TOKEN`.

The transport is MCP **Streamable HTTP**, POST-only: a request gets a plain `application/json`
response rather than an SSE stream. Servers and clients that require SSE streaming are not
supported, which is the one compatibility limit to be aware of.

There is also `GET /health`, which returns `{"status":"ok","server":"editpro-studio","tools":19}`
and needs no token.

## Tools

| Tool | What it does |
| --- | --- |
| `get_summary` | Project name, duration, playhead, selection, per-track state, counts. Start here. |
| `get_project` | The whole project as `.filmcraft` JSON. |
| `list_media` | Media assets, with the ids `add_clip` needs. |
| `list_clips` | Every clip: track, start/end, trim, transform, keyframe count. |
| `list_look_presets` | Colour looks, for `apply_look`. |
| `add_clip` | Place an asset on a track at a time. Returns the clip id. |
| `update_clip` | Change any property of one clip — position, trim, track, transform, volume, text, fades, full colour grade. Only the keys you pass change. |
| `delete_clip` | Remove one clip. |
| `split_clip` | Cut a clip in two at an absolute time. |
| `set_track_state` | Mute, hide, solo or lock a track. |
| `add_text` | Create a title clip and set its words. |
| `add_marker` | Marker at the playhead. |
| `seek` | Move the playhead. |
| `set_playback` | Play or pause the preview. |
| `undo` / `redo` | Step through history, including the agent's own edits. |
| `set_project_name` | Rename the project. |
| `apply_look` | Apply a colour look preset to one clip. |
| `add_keyframe` | Keyframe a clip at a time, capturing its current transform. |

### Deliberately absent

- **Export.** Starting a render is expensive and long-running; an agent should not do it silently.
- **Timeline-wide clears.** Destructive and easy to trigger by accident.
- **`blur`, `hue`, `grain`.** These are stored by the model but not applied to the render (they are
  labelled "not rendered yet" in the inspector). Exposing them would let an agent set a value and
  see nothing happen, which is worse than not offering them.

## Guarantees

- **Undoable.** Every mutating tool goes through the same view-model operations as the UI, so agent
  edits appear in the normal undo history and can be reverted by hand.
- **Single project.** The agent edits whatever project is open in the app.
- **No background server.** The socket lives in the app process; closing the app stops it.

## Security notes

The token is the only thing standing between an agent and your project, so treat it like a
password: it is stored in the app's private preferences and shown only in the panel. Because the
server is bound to loopback, another device on your network cannot reach it even if it has the
token.
