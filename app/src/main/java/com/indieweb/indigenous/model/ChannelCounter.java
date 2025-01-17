// SPDX-FileCopyrightText: 2018 Kristof De Jaeger <swentel@realize.be>
// SPDX-License-Identifier: GPL-3.0-only

package com.indieweb.indigenous.model;

public class ChannelCounter {

    Integer counter = -1;
    boolean isSource = false;

    public Integer getCounter() {
        return counter;
    }

    public void setCounter(Integer counter) {
        this.counter = counter;
    }

    public boolean isSource() {
        return isSource;
    }

    public void setSource(boolean source) {
        isSource = source;
    }
}
