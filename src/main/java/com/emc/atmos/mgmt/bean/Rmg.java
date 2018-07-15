/*
 * BSD 3-Clause License
 *
 * Copyright (c) 2013-2018, Dell EMC
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 *  Redistributions of source code must retain the above copyright notice, this
 *   list of conditions and the following disclaimer.
 *
 *  Redistributions in binary form must reproduce the above copyright notice,
 *   this list of conditions and the following disclaimer in the documentation
 *   and/or other materials provided with the distribution.
 *
 *  Neither the name of the copyright holder nor the names of its
 *   contributors may be used to endorse or promote products derived from
 *   this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE
 * FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
 * DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER
 * CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 */
package com.emc.atmos.mgmt.bean;

import javax.xml.bind.annotation.XmlElement;

public class Rmg {
    private String name;
    private String localTime;
    private int nodesUp;
    private int nodesDown;
    private float avgLoad15;
    private float avgLoad5;
    private float avgLoad1;

    @XmlElement
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @XmlElement
    public String getLocalTime() {
        return localTime;
    }

    public void setLocalTime(String localTime) {
        this.localTime = localTime;
    }

    @XmlElement
    public int getNodesUp() {
        return nodesUp;
    }

    public void setNodesUp(int nodesUp) {
        this.nodesUp = nodesUp;
    }

    @XmlElement
    public int getNodesDown() {
        return nodesDown;
    }

    public void setNodesDown(int nodesDown) {
        this.nodesDown = nodesDown;
    }

    @XmlElement
    public float getAvgLoad15() {
        return avgLoad15;
    }

    public void setAvgLoad15(float avgLoad15) {
        this.avgLoad15 = avgLoad15;
    }

    @XmlElement
    public float getAvgLoad5() {
        return avgLoad5;
    }

    public void setAvgLoad5(float avgLoad5) {
        this.avgLoad5 = avgLoad5;
    }

    @XmlElement
    public float getAvgLoad1() {
        return avgLoad1;
    }

    public void setAvgLoad1(float avgLoad1) {
        this.avgLoad1 = avgLoad1;
    }
}
