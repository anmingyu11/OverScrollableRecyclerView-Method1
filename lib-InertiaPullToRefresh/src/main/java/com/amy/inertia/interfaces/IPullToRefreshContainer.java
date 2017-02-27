package com.amy.inertia.interfaces;

public interface IPullToRefreshContainer {

    void attachToAView(IAView iaView);

    void animScrollBack(float start);

    void animScrollTo();

    void animOverFling();
}
