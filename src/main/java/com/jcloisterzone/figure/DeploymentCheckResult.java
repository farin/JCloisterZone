package com.jcloisterzone.figure;

public class DeploymentCheckResult {
    public final boolean result;
    public final String error;

    private DeploymentCheckResult() {
        this.result = true;
        this.error = null;
    }

    public DeploymentCheckResult(String error) {
        this.result = false;
        this.error = error;
    }

    public static final DeploymentCheckResult OK = new DeploymentCheckResult();
}