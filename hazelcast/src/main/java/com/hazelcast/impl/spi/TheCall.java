/*
 * Copyright (c) 2008-2012, Hazel Bilisim Ltd. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.hazelcast.impl.spi;

import com.hazelcast.impl.base.Call;
import com.hazelcast.nio.Address;
import com.hazelcast.nio.Packet;
import com.hazelcast.util.ResponseQueueFactory;

import java.io.IOException;
import java.util.concurrent.BlockingQueue;

import static com.hazelcast.nio.IOUtil.toObject;

public class TheCall implements Call {
    long id;
    private final Address target;
    private final Operation op;
    private final Callback callback;

    public TheCall(Address target, Operation op) {
        this.target = target;
        this.op = op;
        final BlockingQueue responseQ = ResponseQueueFactory.newResponseQueue();
        this.callback = new Callback() {
            public void notify(Operation op, final Object result) {
                responseQ.offer(result);
            }
        };
    }

    public TheCall(Address target, Operation op, Callback callback) {
        this.target = target;
        this.op = op;
        this.callback = callback;
    }

    public long getCallId() {
        return id;
    }

    public void setCallId(long id) {
        this.id = id;
    }

    public void onEnqueue() {
    }

    public int getEnqueueCount() {
        return 0;
    }

    public void handleResponse(Packet packet) {
        offerResponse((Response) toObject(packet.getValueData()));
    }

    public void offerResponse(Response response) {
        callback.notify(op, response);
    }

    public void process() {
    }

    public void onDisconnect(Address dead) {
        if (dead.equals(target)) {
            callback.notify(op, new IOException());
        }
    }
}