# Contributing

Thanks for helping improve `AbletonInstrumentHost`.

## Scope

This repository is a standalone public SuperCollider Quark. Changes should stay focused on:

- the public `AbletonInstrumentHost` API
- capability detection and graceful degradation
- track, sync, preset, snapshot, automation, and export planning layers
- tests, schelp, README, and release metadata

## Local Development

Run the UnitTests from the repository root:

```powershell
& 'C:\Program Files\SuperCollider-3.14.1\sclang.exe' -D -r -s --include-path 'Classes' --include-path 'tests' 'tests\RunAbletonInstrumentHost.scd'
```

## Guidelines

- Keep the public API small and strong.
- Preserve graceful degradation when optional dependencies like `VSTPlugin` are missing.
- Avoid hard references to optional classes that would break compilation.
- Update tests and docs together with behavior changes.
- Keep edits scoped; do not mix unrelated refactors into feature or fix branches.

## Pull Requests

Please include:

- a short summary of what changed
- why it changed
- how you verified it
- any compatibility or dependency notes

If a change affects the public API or packaging, update `README.md`, `HelpSource`, and `CHANGELOG.md` in the same pull request.

