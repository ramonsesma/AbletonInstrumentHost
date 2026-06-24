// Run from PowerShell with:
//   & 'C:\Program Files\SuperCollider-3.14.1\sclang.exe' -D -r -s --include-path 'Classes' --include-path 'tests' 'tests\RunAbletonInstrumentHost.scd'

TestAbletonInstrumentHost : UnitTest {

    test_facade_methods_exist {
        this.assert(AbletonInstrumentHost.notNil);
        this.assert(AbletonInstrumentHost.respondsTo(\start));
        this.assert(AbletonInstrumentHost.respondsTo(\stop));
        this.assert(AbletonInstrumentHost.respondsTo(\status));
        this.assert(AbletonInstrumentHost.respondsTo(\capabilities));
        this.assert(AbletonInstrumentHost.respondsTo(\loadPlugin));
        this.assert(AbletonInstrumentHost.respondsTo(\unloadPlugin));
        this.assert(AbletonInstrumentHost.respondsTo(\loadPreset));
        this.assert(AbletonInstrumentHost.respondsTo(\saveSnapshot));
        this.assert(AbletonInstrumentHost.respondsTo(\restoreSnapshot));
        this.assert(AbletonInstrumentHost.respondsTo(\noteOn));
        this.assert(AbletonInstrumentHost.respondsTo(\noteOff));
        this.assert(AbletonInstrumentHost.respondsTo(\cc));
        this.assert(AbletonInstrumentHost.respondsTo(\setParam));
        this.assert(AbletonInstrumentHost.respondsTo(\automate));
        this.assert(AbletonInstrumentHost.respondsTo(\setTempo));
        this.assert(AbletonInstrumentHost.respondsTo(\linkStart));
        this.assert(AbletonInstrumentHost.respondsTo(\linkStop));
        this.assert(AbletonInstrumentHost.respondsTo(\exportStemPlan));
        this.assert(AbletonInstrumentHost.respondsTo(\exportScorePlan));
    }

    test_start_status_stop_lifecycle {
        var startResult;
        var statusResult;
        var stopResult;

        AbletonInstrumentHost.stop;
        startResult = AbletonInstrumentHost.start((bpm: 124, beatsPerBar: 4));
        statusResult = AbletonInstrumentHost.status;
        stopResult = AbletonInstrumentHost.stop;

        this.assertEquals(startResult[\ok], true);
        this.assertEquals(startResult[\running], true);
        this.assertEquals(statusResult[\running], true);
        this.assertEquals(statusResult[\sync][\bpm], 124);
        this.assertEquals(statusResult[\sync][\beatsPerBar], 4);
        this.assert(statusResult.includesKey(\capabilities));
        this.assertEquals(stopResult[\ok], true);
        this.assertEquals(stopResult[\running], false);
        this.assertEquals(AbletonInstrumentHost.status[\running], false);
    }

    test_capabilities_report_optional_dependencies {
        var report;

        report = AbletonInstrumentHost.capabilities;

        this.assert(report.includesKey(\environment));
        this.assert(report.includesKey(\classes));
        this.assert(report[\classes].includesKey(\VSTPlugin));
        this.assert(report[\classes].includesKey(\VSTPluginController));
        this.assert(report[\classes].includesKey(\LinkClock));
        this.assert(report.includesKey(\warnings));
        this.assert(report.includesKey(\errors));
    }

    test_track_registry_degrades_without_vstplugin_and_records_events {
        var loadResult;
        var noteResult;
        var ccResult;
        var paramResult;
        var unloadResult;
        var track;

        AbletonInstrumentHost.stop;
        AbletonInstrumentHost.start((bpm: 120));

        loadResult = AbletonInstrumentHost.loadPlugin(\keys, (
            path: "Piano.vst3",
            outputs: 2,
            editor: false
        ));
        noteResult = AbletonInstrumentHost.noteOn(\keys, 60, 100);
        ccResult = AbletonInstrumentHost.cc(\keys, 74, 0.5);
        paramResult = AbletonInstrumentHost.setParam(\keys, \cutoff, 0.25);
        track = AbletonInstrumentHost.status[\tracks][\keys];
        unloadResult = AbletonInstrumentHost.unloadPlugin(\keys);

        this.assertEquals(loadResult[\ok], true);
        this.assertEquals(loadResult[\trackId], \keys);
        this.assertEquals(track[\pluginSpec][\path], "Piano.vst3");
        this.assert(track.includesKey(\degraded));
        this.assertEquals(noteResult[\event][\type], \noteOn);
        this.assertEquals(noteResult[\event][\note], 60);
        this.assertEquals(noteResult[\event][\vel], 100);
        this.assertEquals(noteResult[\event][\chan], 0);
        this.assertEquals(ccResult[\event][\type], \cc);
        this.assertEquals(ccResult[\event][\cc], 74);
        this.assertEquals(ccResult[\event][\value], 0.5);
        this.assertEquals(paramResult[\event][\type], \setParam);
        this.assertEquals(paramResult[\event][\param], \cutoff);
        this.assertEquals(track[\params][\cutoff], 0.25);
        this.assertEquals(track[\events].size, 3);
        this.assertEquals(unloadResult[\ok], true);
        this.assert(AbletonInstrumentHost.status[\tracks][\keys].isNil);

        AbletonInstrumentHost.stop;
    }

    test_note_off_uses_default_channel_and_keeps_track_state {
        var eventResult;
        var track;

        AbletonInstrumentHost.stop;
        AbletonInstrumentHost.start(());
        AbletonInstrumentHost.loadPlugin(\lead, (path: "Lead.vst3"));
        eventResult = AbletonInstrumentHost.noteOff(\lead, 64);
        track = AbletonInstrumentHost.status[\tracks][\lead];

        this.assertEquals(eventResult[\event][\type], \noteOff);
        this.assertEquals(eventResult[\event][\note], 64);
        this.assertEquals(eventResult[\event][\chan], 0);
        this.assertEquals(track[\events].last[\type], \noteOff);

        AbletonInstrumentHost.stop;
    }

    test_load_preset_snapshot_restore_and_automation {
        var presetResult;
        var snapshotResult;
        var automationResult;
        var restoreResult;
        var track;

        AbletonInstrumentHost.stop;
        AbletonInstrumentHost.start((bpm: 118));
        AbletonInstrumentHost.loadPlugin(\bass, (path: "Bass.vst3"));
        presetResult = AbletonInstrumentHost.loadPreset(\bass, (
            name: "round",
            bank: "factory"
        ));
        AbletonInstrumentHost.setParam(\bass, \drive, 0.7);
        automationResult = AbletonInstrumentHost.automate(\bass, (
            param: \drive,
            curve: \linear,
            points: [[0, 0.1], [4, 0.8]]
        ));
        snapshotResult = AbletonInstrumentHost.saveSnapshot(\verse);
        AbletonInstrumentHost.setParam(\bass, \drive, 0.1);
        restoreResult = AbletonInstrumentHost.restoreSnapshot(\verse);
        track = AbletonInstrumentHost.status[\tracks][\bass];

        this.assertEquals(presetResult[\ok], true);
        this.assertEquals(presetResult[\preset][\name], "round");
        this.assertEquals(automationResult[\ok], true);
        this.assertEquals(automationResult[\plan][\points].size, 2);
        this.assertEquals(snapshotResult[\ok], true);
        this.assertEquals(snapshotResult[\name], \verse);
        this.assertEquals(restoreResult[\ok], true);
        this.assertEquals(track[\params][\drive], 0.7);
        this.assertEquals(track[\preset][\bank], "factory");
        this.assertEquals(track[\automation].size, 1);

        AbletonInstrumentHost.stop;
    }

    test_set_tempo_and_link_lifecycle_degrade_cleanly {
        var tempoResult;
        var linkStartResult;
        var linkStopResult;
        var status;

        AbletonInstrumentHost.stop;
        AbletonInstrumentHost.start((bpm: 100));
        tempoResult = AbletonInstrumentHost.setTempo(132);
        linkStartResult = AbletonInstrumentHost.linkStart;
        status = AbletonInstrumentHost.status;
        linkStopResult = AbletonInstrumentHost.linkStop;

        this.assertEquals(tempoResult[\ok], true);
        this.assertEquals(tempoResult[\bpm], 132);
        this.assertEquals(status[\sync][\bpm], 132);
        this.assertEquals(linkStartResult[\ok], true);
        this.assert(linkStartResult.includesKey(\link));
        this.assertEquals(linkStopResult[\ok], true);
        this.assert(linkStopResult.includesKey(\link));

        AbletonInstrumentHost.stop;
    }

    test_export_stem_and_score_plans_are_structured {
        var stemPlan;
        var scorePlan;

        AbletonInstrumentHost.stop;
        AbletonInstrumentHost.start((bpm: 126));
        AbletonInstrumentHost.loadPlugin(\drums, (path: "Drums.vst3"));
        AbletonInstrumentHost.loadPlugin(\pad, (path: "Pad.vst3"));

        stemPlan = AbletonInstrumentHost.exportStemPlan((
            tracks: [\drums, \pad],
            bars: 16,
            format: \wav,
            destination: "exports/stems"
        ));
        scorePlan = AbletonInstrumentHost.exportScorePlan((
            tracks: [\pad],
            bars: 16,
            format: \lilypond,
            notation: (quant: 0.25)
        ));

        this.assertEquals(stemPlan[\ok], true);
        this.assertEquals(stemPlan[\plan][\kind], \stems);
        this.assertEquals(stemPlan[\plan][\bpm], 126);
        this.assertEquals(stemPlan[\plan][\tracks], [\drums, \pad]);
        this.assertEquals(scorePlan[\ok], true);
        this.assertEquals(scorePlan[\plan][\kind], \score);
        this.assertEquals(scorePlan[\plan][\format], \lilypond);
        this.assertEquals(scorePlan[\plan][\tracks], [\pad]);

        AbletonInstrumentHost.stop;
    }

    test_documentation_smoke_check {
        var readme;
        var help;
        var quark;

        this.assert(File.exists("README.md"));
        this.assert(File.exists("HelpSource/Classes/AbletonInstrumentHost.schelp"));
        this.assert(File.exists("AbletonInstrumentHost.quark"));

        readme = File.readAllString("README.md");
        help = File.readAllString("HelpSource/Classes/AbletonInstrumentHost.schelp");
        quark = File.readAllString("AbletonInstrumentHost.quark");

        this.assert(readme.find("# AbletonInstrumentHost").notNil);
        this.assert(readme.find("## Quick Start").notNil);
        this.assert(readme.find("## Install").notNil);
        this.assert(readme.find("## Degradation Model").notNil);
        this.assert(readme.find("## Export Planning").notNil);
        this.assert(readme.find("AbletonInstrumentHost.start").notNil);
        this.assert(readme.find("ramonsesma/AbletonInstrumentHost").notNil);
        this.assert(help.find("CLASS:: AbletonInstrumentHost").notNil);
        this.assert(help.find("METHOD:: start").notNil);
        this.assert(help.find("METHOD:: exportScorePlan").notNil);
        this.assert(quark.find("AbletonInstrumentHost").notNil);
        this.assert(quark.find("ramonsesma/AbletonInstrumentHost").notNil);
    }
}

