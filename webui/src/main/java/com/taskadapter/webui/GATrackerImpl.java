package com.taskadapter.webui;

import org.vaadin.googleanalytics.tracking.GoogleAnalyticsTracker;

/** Google analytics tracker implementation. */
public class GATrackerImpl implements Tracker {

    /** Used tracker. */
    final GoogleAnalyticsTracker tracker;

    public GATrackerImpl(GoogleAnalyticsTracker tracker) {
        this.tracker = tracker;
    }

    @Override
    public void trackPage(String name) {
        tracker.trackPageview("/" + name);
    }

    @Override
    public void trackEvent(String category, String action, String label) {
        tracker.trackEvent(category, action, label);
    }

}
