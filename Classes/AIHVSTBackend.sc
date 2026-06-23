AIHVSTBackend {
    *available {
        ^(\VSTPlugin.asClass.notNil and: { \VSTPluginController.asClass.notNil })
    }

    *load { |trackId, pluginSpec, syncState|
        var warnings;
        var spec;
        var result;
        var controllerClass;

        warnings = List.new;
        spec = pluginSpec ? IdentityDictionary.new;
        controllerClass = \VSTPluginController.asClass;

        if(this.available.not) {
            warnings.add("VSTPlugin or VSTPluginController is unavailable; stored plugin intent only.");
            ^AIHResult.success((
                trackId: trackId,
                loaded: false,
                degraded: true,
                controller: nil,
                warnings: warnings.asArray
            ))
        };

        result = try {
            var controller;

            controller = spec[\controller];
            if(controller.isNil and: { controllerClass.notNil and: { spec[\synth].notNil } }) {
                controller = controllerClass.tryPerform(\new, spec[\synth]);
            };
            if(controller.notNil and: { spec[\path].notNil }) {
                controller.tryPerform(\open, spec[\path], spec[\editor] ? false);
            };
            if(controller.isNil) {
                warnings.add("No VSTPluginController or synth was supplied; stored plugin intent only.");
            };
            AIHResult.success((
                trackId: trackId,
                loaded: controller.notNil,
                degraded: controller.isNil,
                controller: controller,
                warnings: warnings.asArray
            ))
        } { |error|
            warnings.add(error.asString);
            AIHResult.success((
                trackId: trackId,
                loaded: false,
                degraded: true,
                controller: nil,
                warnings: warnings.asArray
            ))
        };

        ^result
    }

    *unload { |track|
        var controller;

        controller = track[\controller];
        if(controller.notNil) {
            try {
                controller.tryPerform(\close);
            } { |error| };
        };
        ^AIHResult.success((trackId: track[\id]))
    }

    *loadPreset { |track, preset|
        var controller;
        var path;

        controller = track[\controller];
        path = preset[\path];
        if(controller.notNil and: { path.notNil }) {
            try {
                controller.tryPerform(\readProgram, path);
            } { |error| };
        };
        ^AIHResult.success((trackId: track[\id], preset: preset))
    }

    *sendEvent { |track, event|
        var controller;
        var midi;

        controller = track[\controller];
        midi = if(controller.notNil) { controller.tryPerform(\midi) } { nil };

        if(midi.notNil) {
            try {
                switch(event[\type],
                    \noteOn, { midi.tryPerform(\noteOn, event[\chan], event[\note], event[\vel]) },
                    \noteOff, { midi.tryPerform(\noteOff, event[\chan], event[\note]) },
                    \cc, { midi.tryPerform(\control, event[\chan], event[\cc], event[\value]) }
                );
            } { |error| };
        };
        ^event
    }

    *setParam { |track, param, value|
        var controller;

        controller = track[\controller];
        if(controller.notNil) {
            try {
                controller.tryPerform(\set, param, value);
            } { |error| };
        };
        ^(type: \setParam, param: param, value: value)
    }
}
