/*
 * Decompiled with CFR 0.152.
 */
package com.oscar.util;

import com.oscar.util.ByteData;
import com.oscar.util.CharacterData;
import com.oscar.util.ColumnData;
import com.oscar.util.StreamData;
import java.util.ArrayList;
import java.util.List;

public class ColumnDataFactory {
    private List byteDatas = new ArrayList(0);
    private List streamDatas = new ArrayList(0);
    private List characterDatas = new ArrayList(0);
    private static final int increment = 3;
    private int bytePosition = 0;
    private int streamPosition = 0;
    private int charcterPosition = 0;

    public ColumnData getByteData() {
        if (this.byteDatas.size() <= this.bytePosition) {
            for (int i = 0; i < 3; ++i) {
                this.byteDatas.add(new ByteData());
            }
        }
        return (ByteData)this.byteDatas.get(this.bytePosition++);
    }

    public ColumnData getStreamData() {
        if (this.streamDatas.size() <= this.streamPosition) {
            for (int i = 0; i < 3; ++i) {
                this.streamDatas.add(new StreamData());
            }
        }
        return (StreamData)this.streamDatas.get(this.streamPosition++);
    }

    public CharacterData getCharacterData() {
        if (this.characterDatas.size() <= this.charcterPosition) {
            for (int i = 0; i < 3; ++i) {
                this.characterDatas.add(new CharacterData());
            }
        }
        return (CharacterData)this.characterDatas.get(this.charcterPosition++);
    }

    public void reset() {
        this.bytePosition = 0;
        this.streamPosition = 0;
        this.charcterPosition = 0;
    }
}

