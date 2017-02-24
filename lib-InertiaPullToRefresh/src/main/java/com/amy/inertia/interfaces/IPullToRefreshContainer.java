package com.amy.inertia.interfaces;

public interface IPullToRefreshContainer {

    void attachToAView(IAView iaView);

    void setInTouching(boolean inTouching);

}
