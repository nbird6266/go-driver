/*
 * Decompiled with CFR 0.152.
 */
package com.oscar.cluster;

import com.oscar.cluster.Node;
import com.oscar.cluster.core.DataImportStream;
import com.oscar.cluster.core.ImportStrategy;
import java.util.List;

/*
 * This class specifies class file version 49.0 but uses Java 6 signatures.  Assumed Java 6.
 */
public class RobinImportStrategy
implements ImportStrategy {
    private Node[] importNodes;
    private int currentImportIndex = 0;
    private Node currentNode;

    public RobinImportStrategy(List<Node> nodes) {
        this.importNodes = new Node[nodes.size()];
        nodes.toArray(this.importNodes);
    }

    @Override
    public DataImportStream nextStream() {
        Node node = this.importNodes[this.currentImportIndex % this.importNodes.length];
        ++this.currentImportIndex;
        this.currentNode = node;
        return node;
    }

    @Override
    public DataImportStream currentStream() {
        if (this.currentNode == null) {
            this.nextStream();
        }
        return this.currentNode;
    }
}

