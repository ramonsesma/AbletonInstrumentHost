AIHPresets {
    *normalize { |presetSpec|
        var spec;

        spec = presetSpec ? IdentityDictionary.new;
        ^(
            name: spec[\name],
            path: spec[\path],
            bank: spec[\bank],
            program: spec[\program],
            meta: spec[\meta] ? IdentityDictionary.new
        )
    }

    *snapshot { |name, sessionState|
        ^(
            name: name,
            savedAt: Date.getDate.stamp,
            config: (sessionState[\config] ? IdentityDictionary.new).deepCopy,
            tracks: AIHPluginRegistry.snapshot(sessionState[\tracks]),
            sync: AIHSync.snapshot(sessionState[\sync]),
            exports: (sessionState[\exports] ? IdentityDictionary.new).deepCopy
        )
    }

    *restore { |sessionState, snapshot|
        if(snapshot.isNil) {
            ^AIHResult.failure(\snapshot_missing, "Snapshot not found", ())
        };

        AIHPluginRegistry.restore(sessionState[\tracks], snapshot[\tracks]);
        sessionState[\config] = (snapshot[\config] ? IdentityDictionary.new).deepCopy;
        sessionState[\exports] = (snapshot[\exports] ? IdentityDictionary.new).deepCopy;
        if(snapshot[\sync].notNil) {
            sessionState[\sync][\bpm] = snapshot[\sync][\bpm];
            sessionState[\sync][\beatsPerBar] = snapshot[\sync][\beatsPerBar];
            AIHSync.setTempo(sessionState[\sync], snapshot[\sync][\bpm]);
        };
        ^AIHResult.success((name: snapshot[\name], snapshot: snapshot.deepCopy))
    }
}

