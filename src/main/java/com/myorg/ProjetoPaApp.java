package com.myorg;

import software.amazon.awscdk.App;
import software.amazon.awscdk.StackProps;

public class ProjetoPaApp {
    public static void main(final String[] args) {
        App app = new App();

        new ProjetoPaStack(app, "ProjetoPaStack", StackProps.builder()
                .build());

        app.synth();
    }
}

