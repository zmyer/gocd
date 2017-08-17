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

package com.thoughtworks.go.server.messaging.activemq;

import com.thoughtworks.go.server.messaging.GoMessage;
import com.thoughtworks.go.server.messaging.GoMessageListener;
import com.thoughtworks.go.server.service.support.DaemonThreadStatsCollector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageConsumer;
import javax.jms.ObjectMessage;

public class JMSMessageListenerAdapter implements Runnable {
    private static final Logger LOG = LoggerFactory.getLogger(JMSMessageListenerAdapter.class);

    private final MessageConsumer consumer;
    private final GoMessageListener listener;
    private final DaemonThreadStatsCollector daemonThreadStatsCollector;
    public Thread thread;

    private JMSMessageListenerAdapter(MessageConsumer consumer, GoMessageListener listener, DaemonThreadStatsCollector daemonThreadStatsCollector) throws JMSException {
        this.consumer = consumer;
        this.listener = listener;
        this.daemonThreadStatsCollector = daemonThreadStatsCollector;


        thread = new Thread(this);
        String threadNameSuffix = "MessageListener for " + listener.getClass().getSimpleName();
        thread.setName(thread.getId() + "@" + threadNameSuffix);
        thread.setDaemon(true);
        thread.start();
    }

    public void run() {
        while (true) {
            if (runImpl()) {
                return;
            }
        }
    }

    public void stop() throws JMSException {
        consumer.close();
    }

    protected boolean runImpl() {
        try {
            Message message = consumer.receive();
            if (message == null) {
                LOG.debug("Message consumer was closed.");
                return true;
            }

            ObjectMessage omessage = (ObjectMessage) message;
            daemonThreadStatsCollector.captureStats(thread.getId());
            listener.onMessage((GoMessage) omessage.getObject());
        } catch (JMSException e) {
            LOG.warn("Error receiving message. Message receiving will continue despite this error.", e);
        } catch (Exception e) {
            LOG.error("Exception thrown in message handling by listener {}", listener, e);
        } finally {
            daemonThreadStatsCollector.clearStats(thread.getId());
        }
        return false;
    }

    public static JMSMessageListenerAdapter startListening(MessageConsumer consumer, GoMessageListener listener, DaemonThreadStatsCollector daemonThreadStatsCollector)
            throws JMSException {
        return new JMSMessageListenerAdapter(consumer, listener, daemonThreadStatsCollector);
    }

}
