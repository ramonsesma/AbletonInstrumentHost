# Changelog

All notable changes to `AbletonInstrumentHost` will be documented in this file.

The format follows Keep a Changelog, and versions use Semantic Versioning.

## [0.1.1] - 2026-06-24

Documentation and distribution update.

Changed:
- updated the Quark metadata URL to the standalone GitHub repository
- added standalone installation instructions and corrected standalone test paths
- added a Windows GitHub Actions workflow to run the UnitTests on push and pull request

## [0.1.0] - 2026-06-24

Initial public release.

Added:
- `AbletonInstrumentHost` facade for lifecycle, tracks, presets, snapshots, MIDI/param events, automation, tempo, Link, and export planning
- graceful capability detection for optional `VSTPlugin` and `VSTPluginController`
- standalone README, schelp help, `.quark` metadata, and UnitTests
- stem and score export planning models for downstream render workflows
