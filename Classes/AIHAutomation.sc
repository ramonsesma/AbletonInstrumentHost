AIHAutomation {
    *noteOn { |trackId, note, vel, chan = 0|
        ^(type: \noteOn, trackId: trackId, note: note.asInteger, vel: vel, chan: chan.asInteger)
    }

    *noteOff { |trackId, note, chan = 0|
        ^(type: \noteOff, trackId: trackId, note: note.asInteger, chan: chan.asInteger)
    }

    *cc { |trackId, cc, value, chan = 0|
        ^(type: \cc, trackId: trackId, cc: cc.asInteger, value: value, chan: chan.asInteger)
    }

    *setParam { |trackId, param, value|
        ^(type: \setParam, trackId: trackId, param: param, value: value)
    }

    *normalizePlan { |trackId, automationSpec|
        var spec;
        var points;

        spec = automationSpec ? IdentityDictionary.new;
        points = spec[\points] ? Array.new;
        ^(
            kind: \automation,
            trackId: trackId,
            param: spec[\param],
            cc: spec[\cc],
            curve: spec[\curve] ? \linear,
            points: points.deepCopy,
            clock: spec[\clock] ? \beats
        )
    }
}

