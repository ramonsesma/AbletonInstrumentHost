AIHExportPlanner {
    *stemPlan { |spec, sessionState|
        var source;
        var sync;
        var plan;

        source = spec ? IdentityDictionary.new;
        sync = AIHSync.snapshot(sessionState[\sync]);
        plan = (
            kind: \stems,
            createdAt: Date.getDate.stamp,
            bpm: sync[\bpm],
            tracks: (source[\tracks] ? sessionState[\tracks].keys.asArray).copy,
            bars: source[\bars],
            format: source[\format] ? \wav,
            destination: source[\destination],
            includeAutomation: source[\includeAutomation] ? true,
            warnings: Array.new
        );
        sessionState[\exports][\lastStemPlan] = plan.deepCopy;
        ^AIHResult.success((plan: plan))
    }

    *scorePlan { |spec, sessionState|
        var source;
        var sync;
        var plan;

        source = spec ? IdentityDictionary.new;
        sync = AIHSync.snapshot(sessionState[\sync]);
        plan = (
            kind: \score,
            createdAt: Date.getDate.stamp,
            bpm: sync[\bpm],
            tracks: (source[\tracks] ? sessionState[\tracks].keys.asArray).copy,
            bars: source[\bars],
            format: source[\format] ? \lilypond,
            notation: source[\notation] ? IdentityDictionary.new,
            includeAutomation: source[\includeAutomation] ? true,
            warnings: Array.new
        );
        sessionState[\exports][\lastScorePlan] = plan.deepCopy;
        ^AIHResult.success((plan: plan))
    }
}

