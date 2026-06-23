AIHSync {
    *defaults {
        ^(
            bpm: 120,
            beatsPerBar: 4,
            clock: nil,
            linkClock: nil,
            linkRunning: false,
            linkAvailable: \LinkClock.asClass.notNil,
            degraded: false,
            warnings: Array.new
        )
    }

    *normalizeConfig { |config|
        var source;
        var bpm;
        var beatsPerBar;

        source = config ? IdentityDictionary.new;
        bpm = source[\bpm] ? 120;
        beatsPerBar = source[\beatsPerBar] ? 4;
        ^(
            bpm: bpm.asFloat,
            beatsPerBar: beatsPerBar.asInteger,
            clock: source[\clock],
            linkClock: nil,
            linkRunning: false,
            linkAvailable: \LinkClock.asClass.notNil,
            degraded: false,
            warnings: Array.new
        )
    }

    *start { |config, capabilities|
        var state;
        var clock;

        state = this.normalizeConfig(config);
        clock = state[\clock] ? TempoClock.new(state[\bpm] / 60);
        clock.tempo = state[\bpm] / 60;
        state[\clock] = clock;
        state[\linkAvailable] = \LinkClock.asClass.notNil;
        ^state
    }

    *stop { |syncState|
        var state;
        var clock;

        state = syncState ? this.defaults;
        clock = state[\clock];
        if(clock.notNil and: { clock !== TempoClock.default }) {
            clock.stop;
        };
        state[\clock] = nil;
        state[\linkClock] = nil;
        state[\linkRunning] = false;
        ^state
    }

    *setTempo { |syncState, bpm|
        var state;
        var clock;
        var value;

        state = syncState ? this.defaults;
        value = bpm.asFloat;
        state[\bpm] = value;
        clock = state[\linkClock] ? state[\clock];
        if(clock.notNil) {
            clock.tempo = value / 60;
        };
        ^AIHResult.success((bpm: value, sync: this.snapshot(state)))
    }

    *linkStart { |syncState|
        var state;
        var linkClass;
        var baseClock;
        var linkClock;
        var warnings;

        state = syncState ? this.defaults;
        warnings = List.new;
        linkClass = \LinkClock.asClass;

        if(linkClass.isNil) {
            state[\linkAvailable] = false;
            state[\linkRunning] = false;
            state[\degraded] = true;
            warnings.add("LinkClock is unavailable; linkStart kept local tempo only.");
            state[\warnings] = warnings.asArray;
            ^AIHResult.success((link: state[\linkRunning], degraded: true, warnings: state[\warnings], sync: this.snapshot(state)))
        };

        baseClock = state[\clock] ? TempoClock.new(state[\bpm] / 60);
        linkClock = try {
            linkClass.tryPerform(\newFromTempoClock, baseClock)
        } { |error|
            warnings.add(error.asString);
            nil
        };

        if(linkClock.isNil) {
            state[\linkRunning] = false;
            state[\degraded] = true;
            warnings.add("LinkClock failed to start; using local TempoClock state.");
        } {
            state[\linkClock] = linkClock;
            state[\clock] = linkClock;
            state[\linkRunning] = true;
            state[\degraded] = false;
        };

        state[\warnings] = warnings.asArray;
        ^AIHResult.success((link: state[\linkRunning], degraded: state[\degraded], warnings: state[\warnings], sync: this.snapshot(state)))
    }

    *linkStop { |syncState|
        var state;
        var linkClock;

        state = syncState ? this.defaults;
        linkClock = state[\linkClock];
        if(linkClock.notNil) {
            linkClock.stop;
        };
        state[\linkClock] = nil;
        state[\linkRunning] = false;
        ^AIHResult.success((link: false, sync: this.snapshot(state)))
    }

    *snapshot { |syncState|
        var state;

        state = syncState ? this.defaults;
        ^(
            bpm: state[\bpm],
            beatsPerBar: state[\beatsPerBar],
            linkAvailable: state[\linkAvailable],
            linkRunning: state[\linkRunning],
            degraded: state[\degraded],
            warnings: state[\warnings] ? Array.new
        )
    }
}
