AbletonInstrumentHost {
    *start { |config| ^AIHSession.start(config) }
    *stop { ^AIHSession.stop }
    *status { ^AIHSession.status }
    *capabilities { ^AIHSession.capabilities }
    *loadPlugin { |trackId, pluginSpec| ^AIHSession.loadPlugin(trackId, pluginSpec) }
    *unloadPlugin { |trackId| ^AIHSession.unloadPlugin(trackId) }
    *loadPreset { |trackId, presetSpec| ^AIHSession.loadPreset(trackId, presetSpec) }
    *saveSnapshot { |name| ^AIHSession.saveSnapshot(name) }
    *restoreSnapshot { |name| ^AIHSession.restoreSnapshot(name) }
    *noteOn { |trackId, note, vel, chan = 0| ^AIHSession.noteOn(trackId, note, vel, chan) }
    *noteOff { |trackId, note, chan = 0| ^AIHSession.noteOff(trackId, note, chan) }
    *cc { |trackId, cc, value, chan = 0| ^AIHSession.cc(trackId, cc, value, chan) }
    *setParam { |trackId, param, value| ^AIHSession.setParam(trackId, param, value) }
    *automate { |trackId, automationSpec| ^AIHSession.automate(trackId, automationSpec) }
    *setTempo { |bpm| ^AIHSession.setTempo(bpm) }
    *linkStart { ^AIHSession.linkStart }
    *linkStop { ^AIHSession.linkStop }
    *exportStemPlan { |spec| ^AIHSession.exportStemPlan(spec) }
    *exportScorePlan { |spec| ^AIHSession.exportScorePlan(spec) }
    *report { |item| ^AIHResult.report(item) }
}

