/*
 * Copyright 2017 ThoughtWorks, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.thoughtworks.go.agent.bootstrapper.osx;

import com.thoughtworks.go.agent.bootstrapper.AgentBootstrapper;
import com.thoughtworks.go.agent.common.AgentBootstrapperArgs;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * worker thread for Mac only
 */
public class MacBootstrapperThread extends Thread {
    private static final Logger LOG = LoggerFactory.getLogger(MacBootstrapperThread.class);
    private final AgentBootstrapperArgs bootstrapperArgs;
    private AgentBootstrapper bootstrapper;

    public MacBootstrapperThread(AgentBootstrapperArgs bootstrapperArgs) {
        this.bootstrapperArgs = bootstrapperArgs;
        setName("MacBootstrapper" + getName() + " " + this.bootstrapperArgs);
    }

    public void run() {
        LOG.info("Launching Agent Bootstrapper for server {}", bootstrapperArgs);
        bootstrapper = new AgentBootstrapper();
        bootstrapper.go(true, bootstrapperArgs);
    }

    public void stopLooping() {
        bootstrapper.stopLooping();
    }
}
