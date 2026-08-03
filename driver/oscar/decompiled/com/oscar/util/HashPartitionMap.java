/*
 * Decompiled with CFR 0.152.
 */
package com.oscar.util;

import com.oscar.jdbc.MetaData;
import java.io.Serializable;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;

public class HashPartitionMap
implements Serializable {
    private static final long serialVersionUID = -5952477578322320409L;
    public static final int XDB_DEFAULT_BUCKETSIZE = 8;
    private int HASH_SIZE = -1;
    private transient HashSet partitions;
    private transient ArrayList[] mappingTable;
    private transient int[] idxMap;
    private transient long[] weightMap = null;
    private transient long[][] stat;

    public HashPartitionMap() {
    }

    public HashPartitionMap(int bucket_size) {
        this.HASH_SIZE = bucket_size;
    }

    public void initBucketCount(MetaData metadata, int tableId) throws SQLException {
        String query = "SELECT COUNT(HASHVALUE) FROM CSYS_TABPARTHASH WHERE tableid = " + tableId;
        int count = 0;
        ResultSet rs = metadata.executeQuery(query);
        if (rs.next()) {
            count = rs.getInt(1);
        }
        this.HASH_SIZE = count;
    }

    public Collection findPartitions(String key) {
        int bucketId = this.getBucketId(key);
        long[] lArray = this.stat[bucketId];
        lArray[0] = lArray[0] + 1L;
        return this.mappingTable[bucketId];
    }

    public void generateDistribution(Collection partitionIdList) {
        this.partitions = new HashSet(partitionIdList);
        Iterator it = partitionIdList.iterator();
        boolean valLoopFinished = false;
        boolean partLoopFinished = false;
        ArrayList parts = null;
        int bucketId = 0;
        if (-1 == this.HASH_SIZE) {
            this.HASH_SIZE = 8;
        }
        this.mappingTable = new ArrayList[this.HASH_SIZE];
        while (!partLoopFinished && it.hasNext()) {
            bucketId = 0;
            for (int i = 0; i < this.HASH_SIZE; ++i) {
                if (!it.hasNext()) {
                    partLoopFinished = true;
                    if (valLoopFinished) break;
                    it = partitionIdList.iterator();
                }
                if (this.mappingTable[bucketId] != null && this.mappingTable[bucketId].size() != 0) {
                    this.mappingTable[bucketId].add(it.next());
                } else {
                    parts = new ArrayList();
                    parts.add(it.next());
                    this.mappingTable[bucketId] = parts;
                }
                ++bucketId;
            }
            valLoopFinished = true;
        }
    }

    public Collection getPartitions(String key) {
        int bucketId = this.getBucketId(key);
        int currentIdx = this.idxMap[bucketId];
        ArrayList parts = this.mappingTable[bucketId];
        if (this.weightMap != null && this.weightMap[bucketId] > 0L) {
            currentIdx = 0;
            this.weightMap[bucketId] = this.weightMap[bucketId] > 1L ? this.weightMap[bucketId] - 1L : -1L;
        } else {
            if (++currentIdx == parts.size()) {
                currentIdx = 0;
            }
            if (currentIdx != this.idxMap[bucketId]) {
                this.idxMap[bucketId] = currentIdx;
            }
        }
        long[] lArray = this.stat[bucketId];
        lArray[1] = lArray[1] + 1L;
        return Collections.singleton((Integer)parts.get(currentIdx));
    }

    public Collection joinPartitions() {
        return this.allPartitions();
    }

    public Collection allPartitions() {
        if (this.partitions == null) {
            this.partitions = new HashSet();
            int length = this.mappingTable.length;
            for (int i = 0; i < length; ++i) {
                this.partitions.addAll(this.mappingTable[i]);
            }
        }
        return this.partitions;
    }

    public int getRedundancyLevel() {
        return 1;
    }

    public Object clone() {
        try {
            return super.clone();
        }
        catch (CloneNotSupportedException e) {
            return null;
        }
    }

    public boolean equals(Object other) {
        if (other instanceof HashPartitionMap && this.allPartitions().size() <= this.HASH_SIZE) {
            int length = this.mappingTable.length;
            for (int i = 0; i < length; ++i) {
                if (this.mappingTable[i].size() <= 1) continue;
                return false;
            }
            return this.mappingTable.equals(((HashPartitionMap)other).mappingTable);
        }
        return false;
    }

    private int getBucketId(String sValue) {
        return this.getBucketFromHash(this.hash(sValue));
    }

    private int getBucketFromHash(int iHash) {
        if (null == this.mappingTable[iHash] || 0 == this.mappingTable[iHash].size()) {
            String errorMessage = "The Node Information Is Lost : (Node ID) ( " + iHash + " )";
            throw new RuntimeException(errorMessage);
        }
        return iHash;
    }

    public final int hash(String str) {
        return this.hash(str == null ? null : str.getBytes());
    }

    private final int hash(byte[] key) {
        if (key == null) {
            return 0;
        }
        int value = 596579247 * key.length;
        for (int i = 0; i < key.length; ++i) {
            value = value + (key[i] << i * 5 % 24) & Integer.MAX_VALUE;
        }
        return (1103515243 * value + 12345 & Integer.MAX_VALUE) % this.HASH_SIZE;
    }

    public int getColumnCount(MetaData metadata, int tableId) throws SQLException {
        String query = "SELECT COUNT(colname) FROM CSYS_ATTRIBUTE WHERE tableid = " + tableId;
        int count = 0;
        ResultSet rs = metadata.executeQuery(query);
        if (rs.next()) {
            count = rs.getInt(1);
        }
        return count;
    }

    public int getColumnPosition(MetaData metadata, int tableId) throws SQLException {
        String query = "select COLSEQ from CSYS_ATTRIBUTE c inner join CSYS_CLASS t on c.colname = t.partcol and c.tableid = " + tableId + " and t.tableid = " + tableId;
        int position = 0;
        ResultSet rs = metadata.executeQuery(query);
        if (rs.next()) {
            position = rs.getInt(1);
        }
        return position;
    }

    public int getTableId(MetaData metadata, String tableName) throws SQLException {
        String sql = "";
        int index = tableName.indexOf(".");
        sql = index > 0 ? "select tableid from CSYS_CLASS t, CSYS_DATABASE d where t.tablename = '" + tableName.substring(index + 1, tableName.length()) + "' and d.DBNAME = '" + tableName.substring(0, index) + "' and d.DBID = t.DBID" : "select tableid from CSYS_CLASS where tablename = '" + tableName + "'";
        ResultSet rs = metadata.executeQuery(sql);
        if (rs.next()) {
            return rs.getInt(1);
        }
        throw new SQLException("can not find " + tableName);
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public void readMapFromMetadataDB(MetaData metadata, int tableId) throws SQLException {
        String query = "SELECT COUNT(DISTINCT hashValue) FROM CSYS_TABPARTHASH WHERE tableid = " + tableId;
        ResultSet rs = metadata.executeQuery(query);
        rs.next();
        this.HASH_SIZE = rs.getInt(1);
        int bucketId = 0;
        this.mappingTable = new ArrayList[this.HASH_SIZE];
        this.weightMap = new long[this.HASH_SIZE];
        query = "SELECT hashValue, nodeid, weight FROM CSYS_TABPARTHASH WHERE tableid = " + tableId;
        rs = metadata.executeQuery(query);
        try {
            ArrayList<Integer> parts = null;
            while (rs.next()) {
                if (this.mappingTable[rs.getInt(1)] != null && this.mappingTable[rs.getInt(1)].size() != 0) {
                    if (this.weightMap[rs.getInt(1)] < rs.getLong(3)) {
                        this.weightMap[rs.getInt((int)1)] = rs.getLong(3);
                        this.mappingTable[rs.getInt(1)].add(0, new Integer(rs.getInt(2)));
                    } else {
                        this.mappingTable[rs.getInt(1)].add(new Integer(rs.getInt(2)));
                    }
                } else {
                    parts = new ArrayList<Integer>();
                    parts.add(new Integer(rs.getInt(2)));
                    this.mappingTable[rs.getInt((int)1)] = parts;
                    this.weightMap[rs.getInt((int)1)] = rs.getLong(3);
                }
                ++bucketId;
            }
            Object var8_7 = null;
        }
        catch (Throwable throwable) {
            Object var8_8 = null;
            rs.close();
            throw throwable;
        }
        rs.close();
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public void storeMapToMetadataDB(MetaData metadata, int databaseId, int tableID) throws SQLException {
        int parthashid = 0;
        String query = "SELECT max(parthashid) FROM CSYS_TABPARTHASH";
        ResultSet rs = metadata.executeQuery(query);
        try {
            rs.next();
            parthashid = rs.getInt(1) + 1;
            Object var8_7 = null;
        }
        catch (Throwable throwable) {
            Object var8_8 = null;
            rs.close();
            throw throwable;
        }
        rs.close();
        for (int i = 0; i < this.mappingTable.length; ++i) {
            int size = this.mappingTable[i].size();
            for (int j = 0; j < size; ++j) {
                String insert = "INSERT INTO CSYS_TABPARTHASH (parthashid, tableid, dbid, hashValue, nodeid, weight) VALUES (" + parthashid++ + ", " + tableID + ", " + databaseId + ", " + i + ", " + this.mappingTable[i].get(j) + ", 0)";
                metadata.executeUpdate(insert);
            }
        }
    }

    public void removeMapFromMetadataDB(MetaData metadata, int tableId) throws SQLException {
        String delete = "DELETE FROM CSYS_TABPARTHASH WHERE tableid = " + tableId;
        metadata.executeUpdate(delete);
        delete = "DELETE FROM CSYS_TABPART WHERE tableid = " + tableId;
        metadata.executeUpdate(delete);
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public void addNodeToMap(MetaData metadata, int tableId, int databaseId, int bucket, Integer node, long weight) throws SQLException {
        int parthashid = 0;
        String query = "SELECT max(parthashid) FROM CSYS_TABPARTHASH";
        ResultSet rs = metadata.executeQuery(query);
        try {
            rs.next();
            parthashid = rs.getInt(1) + 1;
            Object var12_10 = null;
        }
        catch (Throwable throwable) {
            Object var12_11 = null;
            rs.close();
            throw throwable;
        }
        rs.close();
        String insert = "INSERT INTO CSYS_TABPARTHASH (parthashid, tableid, dbid, hashValue, nodeid, weight) VALUES (" + parthashid++ + ", " + tableId + ", " + databaseId + ", " + bucket + ", " + node + ", " + weight + ")";
        metadata.executeUpdate(insert);
    }

    public void updateWeight(MetaData metadata, int tableId) throws SQLException {
        for (int i = 0; i < this.weightMap.length; ++i) {
            if (-1L != this.weightMap[i] && this.weightMap[i] <= 0L) continue;
            long weight = -1L == this.weightMap[i] ? 0L : this.weightMap[i];
            String update = "Update CSYS_TABPARTHASH set weight = " + weight + " where tableid = " + tableId + " and hashValue = " + i + " and nodeid = " + this.mappingTable[i].get(0);
            metadata.executeUpdate(update);
        }
    }

    public int getBucketSize() {
        return this.HASH_SIZE;
    }

    public long[][] getBucketStat() {
        return this.stat;
    }

    public ArrayList[] getMappingTable() {
        return this.mappingTable;
    }
}

