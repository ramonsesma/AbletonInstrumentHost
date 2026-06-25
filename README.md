# AbletonInstrumentHost

![SuperCollider Quarks cover](assets/supercollider-quarks-cover.png)

[![Release](https://img.shields.io/github/v/release/ramonsesma/AbletonInstrumentHost)](https://github.com/ramonsesma/AbletonInstrumentHost/releases)
[![Validate](https://img.shields.io/github/actions/workflow/status/ramonsesma/AbletonInstrumentHost/validate.yml?branch=main&label=validate)](https://github.com/ramonsesma/AbletonInstrumentHost/actions/workflows/validate.yml)
[![License](https://img.shields.io/github/license/ramonsesma/AbletonInstrumentHost)](https://github.com/ramonsesma/AbletonInstrumentHost/blob/main/LICENSE)
[![Quark](https://img.shields.io/badge/quark-0.1.2-blue)](https://github.com/ramonsesma/AbletonInstrumentHost/releases/tag/0.1.2)

`AbletonInstrumentHost` is a public, self-contained SuperCollider Quark for organizing instrument hosting workflows around `VSTPlugin`, tempo sync, Ableton Link, presets, snapshots, automation, and export planning.

It does not replace Ableton Live. It gives SuperCollider a small, performable, programmable layer for instrument/session state so live coding and structured composition can share the same track registry and planning vocabulary.

## Quick Start

```supercollider
AbletonInstrumentHost.start((bpm: 124, beatsPerBar: 4));

AbletonInstrumentHost.loadPlugin(\keys, (
    path: "Piano.vst3",
    outputs: 2,
    editor: false
));

AbletonInstrumentHost.loadPreset(\keys, (name: "soft-keys"));
AbletonInstrumentHost.noteOn(\keys, 60, 96);
AbletonInstrumentHost.setParam(\keys, \cutoff, 0.42);
AbletonInstrumentHost.automate(\keys, (
    param: \cutoff,
    points: [[0, 0.2], [8, 0.8]]
));

AbletonInstrumentHost.saveSnapshot(\verse);
AbletonInstrumentHost.setTempo(128);
AbletonInstrumentHost.restoreSnapshot(\verse);

AbletonInstrumentHost.exportStemPlan((tracks: [\keys], bars: 16));
AbletonInstrumentHost.exportScorePlan((tracks: [\keys], format: \lilypond));

AbletonInstrumentHost.stop;
```

## Install

```supercollider
Quarks.install("https://github.com/ramonsesma/AbletonInstrumentHost");
```

## Public API

- `AbletonInstrumentHost.start(config)`
- `AbletonInstrumentHost.stop`
- `AbletonInstrumentHost.status`
- `AbletonInstrumentHost.capabilities`
- `AbletonInstrumentHost.loadPlugin(trackId, pluginSpec)`
- `AbletonInstrumentHost.unloadPlugin(trackId)`
- `AbletonInstrumentHost.loadPreset(trackId, presetSpec)`
- `AbletonInstrumentHost.saveSnapshot(name)`
- `AbletonInstrumentHost.restoreSnapshot(name)`
- `AbletonInstrumentHost.noteOn(trackId, note, vel, chan = 0)`
- `AbletonInstrumentHost.noteOff(trackId, note, chan = 0)`
- `AbletonInstrumentHost.cc(trackId, cc, value, chan = 0)`
- `AbletonInstrumentHost.setParam(trackId, param, value)`
- `AbletonInstrumentHost.automate(trackId, automationSpec)`
- `AbletonInstrumentHost.setTempo(bpm)`
- `AbletonInstrumentHost.linkStart`
- `AbletonInstrumentHost.linkStop`
- `AbletonInstrumentHost.exportStemPlan(spec)`
- `AbletonInstrumentHost.exportScorePlan(spec)`

## Architecture

The public facade delegates to focused internal classes:

- `AIHSession`: lifecycle and host/session state.
- `AIHCapabilities`: dynamic capability detection.
- `AIHPluginRegistry`: plugin track registry and per-track state.
- `AIHSync`: tempo, clock, and optional Ableton Link state.
- `AIHPresets`: preset normalization and snapshots.
- `AIHAutomation`: MIDI, param, and automation event shapes.
- `AIHExportPlanner`: stem and score export plan dictionaries.
- `AIHVSTBackend`: optional VSTPlugin adapter using dynamic class lookup.
- `AIHResult`: stable result dictionaries and reporting.

## Degradation Model

`AbletonInstrumentHost` compiles without `VSTPlugin` installed. Capability detection uses symbol lookup such as `\VSTPlugin.asClass`, so optional dependencies are not hard references in class files.

When `VSTPlugin` or `VSTPluginController` is absent, `loadPlugin` stores the plugin intent in the track registry and returns `ok: true` with `degraded: true` plus warnings. MIDI events, params, presets, snapshots, automation plans, tempo state, and export plans still work as structured data.

When `VSTPlugin` is available, v1 expects either a ready `controller` in `pluginSpec` or a `synth` from which a `VSTPluginController` can be created. If neither is supplied, `loadPlugin` still stores the track intent and reports degraded state instead of creating hidden audio infrastructure.

`LinkClock` is part of modern SuperCollider builds, but `linkStart` is still best effort. If Link is absent or cannot be started, tempo state remains local and the result reports degraded state.

## Export Planning

Export methods do not render audio or write notation files in v1. They return deterministic plans that downstream renderers can consume:

```supercollider
AbletonInstrumentHost.exportStemPlan((
    tracks: [\drums, \bass, \keys],
    bars: 32,
    format: \wav,
    destination: "exports/stems"
));

AbletonInstrumentHost.exportScorePlan((
    tracks: [\keys],
    bars: 32,
    format: \lilypond,
    notation: (quant: 0.25)
));
```

## Tests

Run from the repository root:

```powershell
& 'C:\Program Files\SuperCollider-3.14.1\sclang.exe' -D -r -s --include-path 'Classes' --include-path 'tests' 'tests\RunAbletonInstrumentHost.scd'
```

Or inside sclang after loading the Quark classes:

```supercollider
TestAbletonInstrumentHost.run;
```

License: MIT.

