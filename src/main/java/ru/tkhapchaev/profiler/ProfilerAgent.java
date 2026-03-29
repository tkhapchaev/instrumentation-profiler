package ru.tkhapchaev.profiler;

import java.lang.instrument.Instrumentation;

public class ProfilerAgent {
    private static final AgentConfigParser CONFIG_PARSER = new AgentConfigParser();

    public static void premain(String agentArgs, Instrumentation instrumentation) {
        AgentConfig config = CONFIG_PARSER.parse(agentArgs);
        instrumentation.addTransformer(new ProfilerClassFileTransformer(), false);

        Runtime.getRuntime().addShutdownHook(new Thread(
            () -> ProfilerRuntime.printReport(config.topN(), config.sortMode()),
            "simple-profiler-report"
        ));

        System.out.printf(
            "Custom profiler agent enabled (top=%d, sort=%s)%n",
            config.topN(),
            config.sortMode().name().toLowerCase()
        );
    }
}