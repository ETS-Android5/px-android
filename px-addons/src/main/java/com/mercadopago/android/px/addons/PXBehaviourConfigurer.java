package com.mercadopago.android.px.addons;

import androidx.annotation.NonNull;

public final class PXBehaviourConfigurer {

    private SecurityBehaviour securityBehaviour;
    private ESCManagerBehaviour escManagerBehaviour;
    private TrackingBehaviour trackingBehaviour;
    private LocaleBehaviour localeBehaviour;
    private FlowBehaviour flowBehaviour;
    private ThreeDSBehaviour threeDSBehaviour;
    private TokenDeviceBehaviour tokenDeviceBehaviour;
    private AuthenticationBehaviour authenticationBehaviour;
    private DeviceInfoBehaviour deviceInfoBehaviour;

    public PXBehaviourConfigurer with(@NonNull final ESCManagerBehaviour escManagerBehaviour) {
        this.escManagerBehaviour = escManagerBehaviour;
        return this;
    }

    public PXBehaviourConfigurer with(@NonNull final SecurityBehaviour securityBehaviour) {
        this.securityBehaviour = securityBehaviour;
        return this;
    }

    public PXBehaviourConfigurer with(@NonNull final TrackingBehaviour trackingBehaviour) {
        this.trackingBehaviour = trackingBehaviour;
        return this;
    }

    public PXBehaviourConfigurer with(@NonNull final LocaleBehaviour localeBehaviour) {
        this.localeBehaviour = localeBehaviour;
        return this;
    }

    public PXBehaviourConfigurer with(@NonNull final FlowBehaviour flowBehaviour) {
        this.flowBehaviour = flowBehaviour;
        return this;
    }

    public PXBehaviourConfigurer with(@NonNull final ThreeDSBehaviour threeDSBehaviour) {
        this.threeDSBehaviour = threeDSBehaviour;
        return this;
    }

    public PXBehaviourConfigurer with(@NonNull final TokenDeviceBehaviour tokenDeviceBehaviour) {
        this.tokenDeviceBehaviour = tokenDeviceBehaviour;
        return this;
    }
  
    public PXBehaviourConfigurer with(@NonNull final AuthenticationBehaviour authenticationBehaviour) {
        this.authenticationBehaviour = authenticationBehaviour;
        return this;
    }

    public PXBehaviourConfigurer with(@NonNull final DeviceInfoBehaviour deviceInfoBehaviour) {
        this.deviceInfoBehaviour = deviceInfoBehaviour;
        return this;
    }

    /**
     * Apply only an nonnull configuration to override previous configuration
     * with nonnull values.
     * This configs may be null at start flow but BehaviourProvider retrieves a new one
     * with basic behaviour.
     */
    public void configure() {
        if (securityBehaviour != null) {
            BehaviourProvider.set(securityBehaviour);
        }
        if (escManagerBehaviour != null) {
            BehaviourProvider.set(escManagerBehaviour);
        }
        if (trackingBehaviour != null) {
            BehaviourProvider.set(trackingBehaviour);
        }
        if (localeBehaviour != null) {
            BehaviourProvider.set(localeBehaviour);
        }
        if (flowBehaviour != null) {
            BehaviourProvider.set(flowBehaviour);
        }
        if (threeDSBehaviour != null) {
            BehaviourProvider.set(threeDSBehaviour);
        }
        if (tokenDeviceBehaviour != null) {
            BehaviourProvider.set(tokenDeviceBehaviour);
        }
        if (authenticationBehaviour != null) {
            BehaviourProvider.set(authenticationBehaviour);
        }
        if (deviceInfoBehaviour != null) {
            BehaviourProvider.set(deviceInfoBehaviour);
        }
    }
}