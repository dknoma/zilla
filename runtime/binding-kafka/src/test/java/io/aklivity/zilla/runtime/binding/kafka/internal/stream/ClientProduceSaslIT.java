/*
 * Copyright 2021-2023 Aklivity Inc.
 *
 * Aklivity licenses this file to you under the Apache License,
 * version 2.0 (the "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at:
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations
 * under the License.
 */
package io.aklivity.zilla.runtime.binding.kafka.internal.stream;

import static io.aklivity.zilla.runtime.binding.kafka.internal.KafkaConfigurationTest.KAFKA_CLIENT_SASL_SCRAM_NONCE_NAME;
import static io.aklivity.zilla.runtime.engine.EngineConfiguration.ENGINE_BUFFER_SLOT_CAPACITY;
import static io.aklivity.zilla.runtime.engine.EngineConfiguration.ENGINE_DRAIN_ON_CLOSE;
import static java.util.concurrent.TimeUnit.SECONDS;
import static org.junit.rules.RuleChain.outerRule;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.DisableOnDebug;
import org.junit.rules.TestRule;
import org.junit.rules.Timeout;
import org.kaazing.k3po.junit.annotation.Specification;
import org.kaazing.k3po.junit.rules.K3poRule;

import io.aklivity.zilla.runtime.engine.test.EngineRule;
import io.aklivity.zilla.runtime.engine.test.annotation.Configuration;
import io.aklivity.zilla.runtime.engine.test.annotation.Configure;

public class ClientProduceSaslIT
{
    private final K3poRule k3po = new K3poRule()
        .addScriptRoot("net", "io/aklivity/zilla/specs/binding/kafka/streams/network/produce.v3.sasl.handshake.v1")
        .addScriptRoot("app", "io/aklivity/zilla/specs/binding/kafka/streams/application/produce");

    private final TestRule timeout = new DisableOnDebug(new Timeout(10, SECONDS));

    private final EngineRule engine = new EngineRule()
        .directory("target/zilla-itests")
        .commandBufferCapacity(1024)
        .responseBufferCapacity(1024)
        .counterValuesBufferCapacity(8192)
        .configure(ENGINE_DRAIN_ON_CLOSE, false)
        .configure(ENGINE_BUFFER_SLOT_CAPACITY, 8192)
        .configurationRoot("io/aklivity/zilla/specs/binding/kafka/config")
        .external("net0")
        .clean();

    @Rule
    public final TestRule chain = outerRule(engine).around(k3po).around(timeout);

    @Test
    @Configuration("client.options.sasl.plain.yaml")
    @Specification({
        "${app}/message.value/client",
        "${net}/message.value.sasl.plain/server"})
    public void shouldSendMessageValueWithSaslPlain() throws Exception
    {
        k3po.finish();
    }

    @Test
    @Configuration("client.options.sasl.scram.yaml")
    @Specification({
        "${app}/message.value/client",
        "${net}/message.value.sasl.scram/server"})
    @Configure(name = KAFKA_CLIENT_SASL_SCRAM_NONCE_NAME,
            value = "io.aklivity.zilla.runtime.binding.kafka.internal.stream.ClientDescribeSaslIT::supplyNonce")
    public void shouldSendMessageValueWithSaslScram() throws Exception
    {
        k3po.finish();
    }

    public static String supplyNonce()
    {
        return "fyko+d2lbbFgONRv9qkxdawL";
    }
}
