// AbletonInstrumentHost quickstart
// Organize VST/link/preset/export intent from sclang.
(
AbletonInstrumentHost.start((bpm: 124, beatsPerBar: 4));

AbletonInstrumentHost.loadPlugin(\keys, (
    path: "Piano.vst3",
    outputs: 2,
    editor: false
)).postln;

AbletonInstrumentHost.loadPreset(\keys, (name: "soft-keys"));
AbletonInstrumentHost.noteOn(\keys, 60, 96);
AbletonInstrumentHost.setParam(\keys, \cutoff, 0.42);
AbletonInstrumentHost.saveSnapshot(\verse);
AbletonInstrumentHost.exportStemPlan((tracks: [\keys], bars: 16)).postln;
AbletonInstrumentHost.exportScorePlan((tracks: [\keys], format: \lilypond)).postln;
)

// AbletonInstrumentHost.stop;
