AIHPluginRegistry {
    *newState {
        ^IdentityDictionary.new
    }

    *trackTemplate { |trackId, pluginSpec, backendResult|
        var track;
        var spec;

        spec = pluginSpec ? IdentityDictionary.new;
        track = IdentityDictionary.new;
        track[\id] = trackId;
        track[\pluginSpec] = spec.deepCopy;
        track[\controller] = backendResult[\controller];
        track[\loaded] = backendResult[\loaded] ? false;
        track[\degraded] = backendResult[\degraded] ? false;
        track[\warnings] = backendResult[\warnings] ? Array.new;
        track[\preset] = nil;
        track[\params] = IdentityDictionary.new;
        track[\events] = List.new;
        track[\automation] = List.new;
        ^track
    }

    *loadPlugin { |tracks, trackId, pluginSpec, syncState|
        var backendResult;
        var track;

        backendResult = AIHVSTBackend.load(trackId, pluginSpec, syncState);
        track = this.trackTemplate(trackId, pluginSpec, backendResult);
        tracks[trackId] = track;
        ^AIHResult.success((
            trackId: trackId,
            track: this.snapshotTrack(track),
            loaded: track[\loaded],
            degraded: track[\degraded],
            warnings: track[\warnings]
        ))
    }

    *unloadPlugin { |tracks, trackId|
        var track;

        track = tracks[trackId];
        if(track.isNil) {
            ^AIHResult.failure(\track_missing, "Unknown track: " ++ trackId, (trackId: trackId))
        };
        AIHVSTBackend.unload(track);
        tracks.removeAt(trackId);
        ^AIHResult.success((trackId: trackId))
    }

    *getTrack { |tracks, trackId|
        var track;

        track = tracks[trackId];
        if(track.isNil) {
            track = this.trackTemplate(trackId, (), (loaded: false, degraded: true, warnings: ["track created without plugin"]));
            tracks[trackId] = track;
        };
        ^track
    }

    *recordEvent { |tracks, trackId, event|
        var track;

        track = this.getTrack(tracks, trackId);
        track[\events].add(event);
        AIHVSTBackend.sendEvent(track, event);
        ^AIHResult.success((trackId: trackId, event: event, degraded: track[\degraded], warnings: track[\warnings]))
    }

    *setParam { |tracks, trackId, param, value|
        var track;
        var event;

        track = this.getTrack(tracks, trackId);
        track[\params][param] = value;
        event = AIHVSTBackend.setParam(track, param, value);
        track[\events].add(event);
        ^AIHResult.success((trackId: trackId, event: event, degraded: track[\degraded], warnings: track[\warnings]))
    }

    *loadPreset { |tracks, trackId, preset|
        var track;

        track = this.getTrack(tracks, trackId);
        track[\preset] = preset.deepCopy;
        AIHVSTBackend.loadPreset(track, preset);
        ^AIHResult.success((trackId: trackId, preset: preset, degraded: track[\degraded], warnings: track[\warnings]))
    }

    *addAutomation { |tracks, trackId, plan|
        var track;

        track = this.getTrack(tracks, trackId);
        track[\automation].add(plan.deepCopy);
        ^AIHResult.success((trackId: trackId, plan: plan, degraded: track[\degraded], warnings: track[\warnings]))
    }

    *snapshotTrack { |track|
        ^(
            id: track[\id],
            pluginSpec: track[\pluginSpec].deepCopy,
            loaded: track[\loaded],
            degraded: track[\degraded],
            warnings: (track[\warnings] ? Array.new).copy,
            preset: if(track[\preset].notNil) { track[\preset].deepCopy } { nil },
            params: track[\params].deepCopy,
            events: track[\events].asArray.deepCopy,
            automation: track[\automation].asArray.deepCopy
        )
    }

    *snapshot { |tracks|
        var result;

        result = IdentityDictionary.new;
        tracks.keysValuesDo { |trackId, track|
            result[trackId] = this.snapshotTrack(track);
        };
        ^result
    }

    *restore { |tracks, snapshot|
        tracks.clear;
        (snapshot ? IdentityDictionary.new).keysValuesDo { |trackId, data|
            var track;

            track = IdentityDictionary.new;
            track[\id] = data[\id] ? trackId;
            track[\pluginSpec] = (data[\pluginSpec] ? IdentityDictionary.new).deepCopy;
            track[\controller] = nil;
            track[\loaded] = data[\loaded] ? false;
            track[\degraded] = data[\degraded] ? true;
            track[\warnings] = (data[\warnings] ? Array.new).copy;
            track[\preset] = if(data[\preset].notNil) { data[\preset].deepCopy } { nil };
            track[\params] = (data[\params] ? IdentityDictionary.new).deepCopy;
            track[\events] = List.newFrom((data[\events] ? Array.new).deepCopy);
            track[\automation] = List.newFrom((data[\automation] ? Array.new).deepCopy);
            tracks[trackId] = track;
        };
        ^tracks
    }
}

