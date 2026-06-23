AIHCapabilities {
    *inspect { |request|
        var requested;
        var classNames;
        var classes;
        var warnings;
        var vstAvailable;
        var controllerAvailable;
        var linkAvailable;
        var report;

        requested = request ? IdentityDictionary.new;
        classNames = requested[\classes] ? [\VSTPlugin, \VSTPluginController, \LinkClock, \MIDIOut, \TempoClock];
        classes = IdentityDictionary.new;
        warnings = List.new;

        classNames.do { |name|
            var sym;
            var cls;

            sym = name.asSymbol;
            cls = sym.asClass;
            classes[sym] = (
                requested: sym,
                available: cls.notNil,
                className: if(cls.notNil) { cls.name } { nil }
            );
        };

        vstAvailable = (classes[\VSTPlugin].notNil and: { classes[\VSTPlugin][\available] == true });
        controllerAvailable = (classes[\VSTPluginController].notNil and: { classes[\VSTPluginController][\available] == true });
        linkAvailable = (classes[\LinkClock].notNil and: { classes[\LinkClock][\available] == true });

        if(vstAvailable.not or: { controllerAvailable.not }) {
            warnings.add("VSTPlugin is unavailable; plugin load calls will store intent and run degraded.");
        };
        if(linkAvailable.not) {
            warnings.add("LinkClock is unavailable; tempo sync will use TempoClock state only.");
        };

        report = IdentityDictionary.new;
        report[\environment] = (
            sclangVersion: Main.version.asString,
            platform: Platform.name.asString
        );
        report[\classes] = classes;
        report[\features] = (
            vstPlugin: (vstAvailable and: { controllerAvailable }),
            abletonLink: linkAvailable,
            midiEvents: true,
            exportPlanning: true,
            snapshots: true
        );
        report[\server] = (
            name: Server.default.name,
            booted: Server.default.serverRunning
        );
        report[\warnings] = warnings.asArray;
        report[\errors] = Array.new;

        ^report
    }
}

