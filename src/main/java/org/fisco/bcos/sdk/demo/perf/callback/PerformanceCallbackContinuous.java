/**
 * Copyright 2014-2020 [fisco-dev]
 *
 * <p>Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file
 * except in compliance with the License. You may obtain a copy of the License at
 *
 * <p>http://www.apache.org/licenses/LICENSE-2.0
 *
 * <p>Unless required by applicable law or agreed to in writing, software distributed under the
 * License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 * express or implied. See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.fisco.bcos.sdk.demo.perf.callback;

import org.fisco.bcos.sdk.demo.perf.collector.PerformanceCollectorContinuous;
import org.fisco.bcos.sdk.model.TransactionReceipt;
import org.fisco.bcos.sdk.model.callback.TransactionCallback;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PerformanceCallbackContinuous extends TransactionCallback {

    private static Logger logger = LoggerFactory.getLogger(PerformanceCallbackContinuous.class);
    private Long startTime = System.currentTimeMillis();

    private PerformanceCollectorContinuous collector;

    public PerformanceCollectorContinuous getCollector() {
        return collector;
    }

    public void setCollector(PerformanceCollectorContinuous collector) {
        this.collector = collector;
    }

    public PerformanceCallbackContinuous() {}

    @Override
    public void onResponse(TransactionReceipt receipt) {
        Long cost = System.currentTimeMillis() - startTime;

        try {
            collector.onMessage(receipt, cost);
        } catch (Exception e) {
            logger.error("onMessage error: ", e);
        }
    }
}
