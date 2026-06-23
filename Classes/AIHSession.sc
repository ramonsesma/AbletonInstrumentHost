AIHSession {
    classvar <state;

    *initClass {
        this.resetState;
    }

    *resetState {
        state = IdentityDictionary.new;
        state[\running] = false;
        state[\config] = IdentityDictionary.new;
        state[\capabilities] = AIHCapabilities.inspect;
        state[\tracks] = AIHPluginRegistry.newState;
        state[\snapshots] = IdentityDictionary.new;
        state[\sync] = AIHSync.defaults;
        state[\exports] = IdentityDictionary.new;
        state[\startedAt] = nil;
        ^state
    }

    *start { |config|
        var normalized;

        normalized = config ? IdentityDictionary.new;
        if(state.isNil) {
            this.resetState;
        };
        if(state[\running] == true) {
            this.stop;
        };

        state[\config] = normalized.deepCopy;
        state[\capabilities] = AIHCapabilities.inspect;
        state[\tracks] = AIHPluginRegistry.newState;
        state[\snapshots] = IdentityDictionary.new;
        state[\exports] = IdentityDictionary.new;
        state[\sync] = AIHSync.start(normalized, state[\capabilities]);
        state[\running] = true;
        state[\startedAt] = Date.getDate.stamp;
        ^AIHResult.success((
            running: true,
            startedAt: state[\startedAt],
            sync: AIHSync.snapshot(state[\sync]),
            capabilities: state[\capabilities]
        ))
    }

    *stop {
        if(state.isNil) {
            this.resetState;
        };
        state[\sync] = AIHSync.stop(state[\sync]);
        state[\running] = false;
        state[\startedAt] = nil;
        ^AIHResult.success((running: false))
    }

    *status {
        if(state.isNil) {
            this.resetState;
        };
        ^(
            running: state[\running],
            startedAt: state[\startedAt],
            config: state[\config].deepCopy,
            capabilities: state[\capabilities].deepCopy,
            sync: AIHSync.snapshot(state[\sync]),
            tracks: AIHPluginRegistry.snapshot(state[\tracks]),
            snapshots: state[\snapshots].keys.asArray.sort,
            exports: state[\exports].deepCopy
        )
    }

    *capabilities {
        if(state.isNil) {
            this.resetState;
        };
        state[\capabilities] = AIHCapabilities.inspect;
        ^state[\capabilities].deepCopy
    }

    *loadPlugin { |trackId, pluginSpec|
        ^AIHPluginRegistry.loadPlugin(state[\tracks], trackId, pluginSpec, state[\sync])
    }

    *unloadPlugin { |trackId|
        ^AIHPluginRegistry.unloadPlugin(state[\tracks], trackId)
    }

    *loadPreset { |trackId, presetSpec|
        var preset;

        preset = AIHPresets.normalize(presetSpec);
        ^AIHPluginRegistry.loadPreset(state[\tracks], trackId, preset)
    }

    *saveSnapshot { |name|
        var snapshot;

        snapshot = AIHPresets.snapshot(name, state);
        state[\snapshots][name] = snapshot;
        ^AIHResult.success((name: name, snapshot: snapshot.deepCopy))
    }

    *restoreSnapshot { |name|
        ^AIHPresets.restore(state, state[\snapshots][name])
    }

    *noteOn { |trackId, note, vel, chan = 0|
        ^AIHPluginRegistry.recordEvent(state[\tracks], trackId, AIHAutomation.noteOn(trackId, note, vel, chan))
    }

    *noteOff { |trackId, note, chan = 0|
        ^AIHPluginRegistry.recordEvent(state[\tracks], trackId, AIHAutomation.noteOff(trackId, note, chan))
    }

    *cc { |trackId, cc, value, chan = 0|
        ^AIHPluginRegistry.recordEvent(state[\tracks], trackId, AIHAutomation.cc(trackId, cc, value, chan))
    }

    *setParam { |trackId, param, value|
        ^AIHPluginRegistry.setParam(state[\tracks], trackId, param, value)
    }

    *automate { |trackId, automationSpec|
        ^AIHPluginRegistry.addAutomation(state[\tracks], trackId, AIHAutomation.normalizePlan(trackId, automationSpec))
    }

    *setTempo { |bpm|
        ^AIHSync.setTempo(state[\sync], bpm)
    }

    *linkStart {
        ^AIHSync.linkStart(state[\sync])
    }

    *linkStop {
        ^AIHSync.linkStop(state[\sync])
    }

    *exportStemPlan { |spec|
        ^AIHExportPlanner.stemPlan(spec, state)
    }

    *exportScorePlan { |spec|
        ^AIHExportPlanner.scorePlan(spec, state)
    }
}

