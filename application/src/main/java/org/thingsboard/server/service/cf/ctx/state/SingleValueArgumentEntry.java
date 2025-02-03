/**
 * Copyright © 2016-2024 The Thingsboard Authors
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
package org.thingsboard.server.service.cf.ctx.state;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.thingsboard.server.common.data.kv.AttributeKvEntry;
import org.thingsboard.server.common.data.kv.BasicKvEntry;
import org.thingsboard.server.common.data.kv.KvEntry;
import org.thingsboard.server.common.data.kv.TsKvEntry;
import org.thingsboard.server.common.util.ProtoUtils;
import org.thingsboard.server.gen.transport.TransportProtos.AttributeValueProto;
import org.thingsboard.server.gen.transport.TransportProtos.TsKvProto;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SingleValueArgumentEntry implements ArgumentEntry {

    public static final ArgumentEntry EMPTY = new SingleValueArgumentEntry(0);

    private long ts;
    private BasicKvEntry kvEntryValue;
    private Long version;

    public SingleValueArgumentEntry(TsKvProto entry) {
        this.ts = entry.getTs();
        this.version = entry.getVersion();
        this.kvEntryValue = ProtoUtils.fromProto(entry.getKv());
    }

    public SingleValueArgumentEntry(AttributeValueProto entry) {
        this.ts = entry.getLastUpdateTs();
        this.version = entry.getVersion();
        this.kvEntryValue = ProtoUtils.basicKvEntryFromProto(entry);
    }

    public SingleValueArgumentEntry(KvEntry entry) {
        if (entry instanceof TsKvEntry tsKvEntry) {
            this.ts = tsKvEntry.getTs();
            this.version = tsKvEntry.getVersion();
        } else if (entry instanceof AttributeKvEntry attributeKvEntry) {
            this.ts = attributeKvEntry.getLastUpdateTs();
            this.version = attributeKvEntry.getVersion();
        }
        this.kvEntryValue = ProtoUtils.basicKvEntryFromKvEntry(entry);
    }

    /**
     * Internal constructor to create immutable SingleValueArgumentEntry.EMPTY
     * */
    private SingleValueArgumentEntry(int ignored) {
        this.ts = System.currentTimeMillis();
        this.kvEntryValue = null;
    }

    @Override
    public ArgumentEntryType getType() {
        return ArgumentEntryType.SINGLE_VALUE;
    }

    @JsonIgnore
    public Object getValue() {
        return kvEntryValue.getValue();
    }

    @Override
    public ArgumentEntry copy() {
        return new SingleValueArgumentEntry(this.ts, this.kvEntryValue, this.version);
    }

    @Override
    public boolean updateEntry(ArgumentEntry entry) {
        if (entry instanceof SingleValueArgumentEntry singleValueEntry) {
            if (singleValueEntry.getTs() == this.ts) {
                return false;
            }

            Long newVersion = singleValueEntry.getVersion();
            if (newVersion == null || this.version == null || newVersion > this.version) {
                this.ts = singleValueEntry.getTs();
                this.kvEntryValue = singleValueEntry.getKvEntryValue();
                this.version = newVersion;
                return true;
            }
        } else {
            throw new IllegalArgumentException("Unsupported argument entry type for single value argument entry: " + entry.getType());
        }
        return false;
    }
}
