# OSCAR（神通数据库）Go 驱动

本目录为 OSCAR 数据库的 Go 语言驱动实现（`database/sql/driver`），参考
反编译的 JDBC 驱动源码逐步移植。

## 目录结构

| 文件 | 说明 |
| --- | --- |
| `driver.go` | 驱动入口：`Driver`、`Connector`、`OpenConnector` |
| `dsn.go` | DSN 解析（host/port/user/password/database 等配置） |
| `conn.go` | 连接管理与协议层：握手、认证（MD5/信任）、查询包收发、行描述/数据行读取 |
| `stmt.go` | 预处理语句（oscarStmt）：Prepare、ExecContext、QueryContext |
| `bind.go` | 参数类型推断与绑定编码（ParamInfo 类型 OID 选择） |
| `error.go` | 驱动错误类型 `OscarError`（SQLState 分类）与 `Is*` 分类辅助函数 |
| `cancel.go` | Context 取消：独立连接发送 PostgreSQL 兼容 CancelRequest 包 |
| `lob.go` | LOB 内容获取：Fastpath（0x02）协议、locator 解码、READ 分块 |
| `numeric.go` | OSCAR 内部 numeric（base-100）编码数字解码（math/big 保精度） |
| `datetime.go` | date/time/timestamp/timestamptz 二进制解码为 `time.Time` |
| `*_test.go` | 测试用例（见“测试”一节） |

## 使用指南（如何在项目中引入）

### 1. 引入依赖

驱动是标准 `database/sql/driver` 实现，注册名为 `oscar`。在 `go.mod` 中
引入即可（仓库路径以实际为准）：

```go
import (
    "database/sql"

    _ "github.com/nbird6266/go-driver/driver/shentong/go-oscar" // 注册 oscar 驱动
)
```

> 用空白导入 `_` 触发 `init()` 中的 `sql.Register("oscar", ...)`。
> 注意：该纯 Go 驱动只在 **nbird6266 的 fork** 上（官方 team-ide/go-driver
> v1.3.8 的 `driver/shentong/` 下只有 cgo 版 go-aci），需拉取 fork 模块：
>
> ```
> go get github.com/nbird6266/go-driver@<版本或分支>
> ```
>
> 若以本地目录形式维护，可配合 `go.mod` 的 `replace` 指令使用。

### 2. DSN 格式

```
user/password@host:port/database
```

- 默认端口 `2003`，连接超时默认 5s（暂不支持 DSN 参数扩展）。
- 密码中的特殊字符（如 `@`、`/`）需做 URL 编码（`url.PathUnescape` 解码）。
- 示例：

```go
db, err := sql.Open("oscar", "SYSDBA/szoscar55@127.0.0.1:2003/OSRDB")
```

### 3. 基础用法

```go
db.SetMaxOpenConns(10)
db.SetMaxIdleConns(5)
db.SetConnMaxLifetime(time.Hour)

// 查询
rows, err := db.Query("select id, name from t_user where dept = ?", "dev")

// 参数化执行
res, err := db.Exec("insert into t_user(id, name) values (?, ?)", 1, "张三")

// 事务（支持 Isolation / ReadOnly，见 txIsolationSQL）
tx, err := db.Begin()
tx.Exec("insert into t_user(id, name) values (?, ?)", 2, "李四")
tx.Commit()

// Savepoint（注意：RELEASE SAVEPOINT 不受支持，Oracle 语义无需显式释放）
tx.Exec("savepoint sp1")
tx.Exec("rollback to sp1")

// Context 超时/取消（取消会向服务器发 CancelRequest 中断查询，约 5s 生效）
ctx, cancel := context.WithTimeout(context.Background(), 10*time.Second)
db.ExecContext(ctx, "select ...")
```

### 4. 类型映射

| Go 值 | 绑定方式 | 说明 |
| --- | --- | --- |
| int/int64 等 | base-100 数字 | 超出 int64 的返回值以十进制字符串给出 |
| float32/float64 | ASCII 文本（保精度） | |
| bool | `"1"`/`"0"` 文本 | |
| string | 原始字节（>240 字节自动 0xFE 分块） | CLOB 列传 string |
| `[]byte` | 见下 | |
| `time.Time` | timestamp 二进制 | date/time/timestamp 列通用 |
| nil | NULL | |

`[]byte` 的绑定按服务器推断的参数类型分流（首次执行自动 `PREPARE` +
`GET PARAMINFO` 获取类型）：

- **bytea/binary/varbinary 列**：`\ooo` 转义绑定，任意字节（含 NUL、反斜杠）
  无损往返（见“参数化查询”一节）。
- **blob 列**：`0x` + 小写 hex 文本绑定。

读取时数字、bool、日期时间自动解码为 Go 类型；其余列返回 `[]byte`，
`ColumnTypeDatabaseTypeName` 可查询数据库类型名。

### 5. 错误处理

驱动错误统一为 `*oscar.OscarError`，可 `errors.As` 提取并分类：

```go
_, err := db.Exec("insert into t_user(id) values (1)")
var oe *oscar.OscarError
if errors.As(err, &oe) {
    fmt.Println(oe.Code, oe.SQLState, oe.Message) // e.g. [23000] 属性ID不能为空
}
if oscar.IsUniqueViolation(err) { /* 唯一约束冲突 */ }
if oscar.IsNotNullViolation(err) { /* NOT NULL 冲突 */ }
if oscar.IsUndefinedTable(err) { /* 表不存在 */ }
if oscar.IsSyntaxError(err) { /* 语法错误 */ }
```

注意：本服务器 NOT NULL 与唯一约束的 SQLState 均为 `23000`，`Is*` 函数
会同时按消息文本（`不能为空`/`重复键值`）辅助判别。

### 6. 已知限制（供使用时参考）

- 服务器 bytea 按文本存储：含 NUL 或反斜杠的原始字节必须经驱动转义
  （已自动处理）；`RELEASE SAVEPOINT` 不被支持。
- Context 取消依赖服务器轮询，中断延迟约 5s；纯本地的 `db.Ping` 等
  不受影响。
- 未加引号的标识符会被服务器转成大写（`as i` → 列名 `I`）。
- 大结果集会一次性缓冲到内存（流式读取/COPY/批量插入见路线图 P4）。

## 协议要点

- 协议标签：`1`=查询（QueryPacket）、`0x02`=Fastpath 函数调用、
  `0xA4`=PlanID、`p`=ParamInfo、`T`=RowDescription、`D`=DataRow、
  `C`=CommandComplete、`E`=Error、`N`=Notice、`Z`=ReadyForQuery。
- 大端字节序（`SendInteger`/`SendLong`）。
- 连接握手完成后服务器可能启用 **网络帧压缩**（`0xA2`=snappy、
  `0xA3`=不压缩），由 `conn.compress` 控制读取层解包；
  注意这与 LOB 的 READCOMPRESS（zlib）是两回事，不要混用。
- RowDescription 中 `TypeOID` 是**外部 OID**（如 clob=3001），
  而数据行值需按**内部 OscarType** 读取，二者通过 `oscarTypeForOID` 映射
  （镜像 JDBC `oscarTypeCache`）。

### 线上值读取分派（readValueV2）

| 内部 OscarType | 线上格式 |
| --- | --- |
| 23（int 族） | base-100 二进制数字（`decodeOscarNumber`，math/big 解码，超 int64 返回精确十进制字符串） |
| 33（boolean） | 单字节 0/1（解码为 Go bool） |
| 25/26/28/29（date/time/timestamp/timestamptz） | 固定二进制布局（`decodeOscarDateTime`，解码为 `time.Time`） |
| 27（timetz） | 线上布局未确认，暂返回原始 []byte |
| 34（numeric/float） | ASCII 字符串 |
| 50/51/52（blob/clob/bfile） | 4 字节长度前缀（值长 = l-4），值为 ASCII hex locator，读取后替换为真实内容 |
| 其他 | 1 字节长度前缀（253 特殊值、>240 分块），原样 []byte 返回 |

### 日期时间二进制格式（datetime.go，镜像 JDBC TimestampConverter/DateConverter/TimeConverter）

- **timestamp（28，11 字节）**：`[y/100+100, y%100+100, mon, day, h+1, mi+1, s+1, 微秒(4 大端)]`，
  年 = `((b0&0xFF)-100)*100 + (b1&0xFF)-100`；月为 **1-based**（编码时 `Calendar.get(MONTH)+1`）。
- **timestamptz（29，13 字节）**：11 字节 + zone(2)：`hour=val[off]-20, min=val[off+1]-60`，
  偏移秒 = hour*3600 + min*60（`getZone`）。
- **date（25）**：完整 7 字节形式或紧凑 4 字节 `[y/100+100, y%100+100, mon, day]`。
- **time（26）**：3 字节 `[h+1, mi+1, s+1]`；可选 7 字节变体第 `[3..6]` 为纳秒。
- **无穷值**：2 字节哨兵 `[0xFD, 0x02]`=+∞、`[0xFD, 0x03]`=-∞（返回原始字节，不强行解码）。
- **纳秒启发式**（JDBC `getNanos`）：4 字节大端值若 `< 1 秒`（`i/100000000 <= 0`）则 `i *= 1000`
  （微秒→纳秒歧义处理）。
- 服务器 timestamp 精度为**微秒**（999999999ns 插入后被截断为 .999999）。
- BC 年份无法用 `time.Time` 表示，返回原始字节。

## LOB 读取（lob.go）

CLOB/BLOB/BFILE 列在数据行中返回 **ASCII 十六进制 locator 字符串**（v4，
72 字符 ≈ 36 字节二进制）。真实内容通过 Fastpath 函数调用读取：

1. `hexStringToBytes` 把 locator 解码为二进制（镜像 JDBC
   `Hex.parserStringToByte`）。
2. `GETPRECISELENGTH(locator)` → 8 字节大端长度。
3. `GET_CHUNKSIZE(locator)` → 4 字节大端块大小（本服务器实测 600000）。
4. 循环 `READ(locator, len[4], pos[8])`，pos 从 1 开始，累计内容。

### Fastpath 调用包（FunctionCallPacketV2）

```
0x02 | queryNum(1)=0 | funcOID(4) | paraCount(4) | 每参: paraLen(4)+值
响应: 'V'(isNull(1) [='G' 时 resultSize(4)+result+unused(1)]) / 'E' / 'N'，直至 'Z'
```

参数编码：int→4 字节大端、long→8 字节大端、byte[]→原始字节
（镜像 `FastpathArg`）。

### v4 LOB 函数 OID 表（Fastpath.java）

| 函数 | blob(1) | clob(2) | bfile(3) |
| --- | --- | --- | --- |
| GETPRECISELENGTH | 2970 | 2971 | 2972 |
| GET_CHUNKSIZE | 2968 | 2969 | — |
| READ | 2976 | 2977 | 2978 |
| READCOMPRESS | 3018（仅 blob，zlib） | — | — |

> 注意：GETPRECISELENGTH 的 blob=2970 / bfile=2972，**不要与 bfile 弄反**。
> READCOMPRESS 仅当 JDBC `isCompressTransfer()` 为 true 时使用，而该标志
> 在 JDBC 中默认 **false**，故 Go 驱动默认总是用 READ。

### 调试经验（坑）

- 错误的函数 OID 会得到 `invalid lob locator`（如把 blob 的
  GETPRECISELENGTH 当成 2972 bfile 时）。
- 用网络帧压缩标志 `c.compress` 判断是否走 READCOMPRESS 是**错的**，
  会报 `函数参数个数不一致`（READCOMPRESS 与本服务器签名不匹配）。
- locator 前 4 字节 `0x20/0x21` 及第 9 字节 `03/02` 为内部元数据，直接原样
  传回 fastpath 即可，无需拆分/截断。
- 空 LOB：行值长度为 0 → 返回空内容，不要调 fastpath。
- BLOB 参数按 `0x`+hex 串绑定，>240 字节时若不经过 0xFE 分块编码，
  服务端报 `Bad hex code: 'x'`（表现为 4 字节小 blob 正常、大 blob 报错）。

## 参数化查询（stmt.go / bind.go）

- `Prepare` 发送 0xA4（PlanID 包）+ `p`（ParamInfo）+ Execute 流程；
  服务器缓存 plan，重复执行时不再回发 RowDescription（用缓存的 knownFields）。
- 参数类型推断见 `bind.go`，按 Go 值类型映射到服务器内部类型
  （int→23 族、float→34、string→24、[]byte→按参数类型、time→28、nil→24）。
- **bytea/binary/varbinary 参数**（`GET PARAMINFO` 推断出类型 OID 17/1365/3100）：
  服务器把 bytea 当作文本存储（转义校验严格：裸反斜杠/非法转义报
  `Bad input string for type bytea`，NUL 会截断值），因此驱动把所有字节
  编码为 `\ooo` 八进制转义文本后绑定（`escapeBytea`）；读取时反向解码
  （`decodeByteaEscape`，服务器输出 `\`→`\\`、非 ASCII→`\ooo`）。
  实测任意字节（含 NUL、反斜杠、0x80-0xFF、>240 字节大值）均可无损往返。
- **BLOB 参数**仍按 `0x`+小写 hex 串绑定（服务器只认 hex 文本，不接受原始字节）。
- 首次执行带 []byte 参数时，先发 `PREPARE name AS sql` + `GET PARAMINFO FOR name`
  获取服务器推断的参数类型 OID，缓存在 stmt 上供后续复用。
- `ExecContext`/`QueryContext` 支持 Context 超时，且取消时向服务器发送
  CancelRequest（见下）。
- **所有 >240 字节的参数值必须先经过 `convertByteArr` 的 0xFE 长串分块编码**
  （`0xFE` 头 + `0xF0`+240 字节块 + 尾块真实长度 + `0x00`）。`[]byte` 转成的
  hex/转义串也不例外——否则服务器解析会报 `Bad hex code: 'x'`。
- 大 LOB 参数（>240 字节的 string/[]byte）均可用分块编码直通绑定，
  CLOB 列传 string、BLOB 列传 []byte（1.5MB 实测通过），无需临时 LOB。

## Context 取消（cancel.go）

`queryPacket` 在查询期间启动 watcher goroutine：Context 取消时通过
**独立 TCP 连接**发送 PostgreSQL 兼容的 16 字节 CancelRequest
`[len=16][code=80877102][pid][ckey]`（镜像 JDBC `CancelRequestPacket`），
服务器按自身轮询间隔中断查询（实测约 5s，错误 `[40000] 线程被用户终止`）。
pid/ckey 来自 `'K'` BackendKey 包，由 `backendKey`/`setBackendKey` 加锁读写。
阻塞读锁无法在同一连接上取消，因此必须新开连接。

## 结果集元数据（ColumnType）

`oscarRows` 实现了 `driver.RowsColumnType*` 系列接口：

- `ColumnTypeDatabaseTypeName`：OID→类型名（镜像 JDBC `DBTypeCache`，见
  `dbTypeName`）。
- `ColumnTypeScanType`：按 OscarType 返回扫描 Go 类型
  （23→int64、33→bool、34→float64、25/26/27/28/29→time.Time、其余→[]byte）。
- `ColumnTypeLength` / `ColumnTypeNullable`：协议不携带，返回“未知”。

> 注意：未加引号的标识符会被服务器转成大写（`as i` → 列名 `I`）。

## 测试

连接使用 DSN 环境变量，见 `openTestDB`（host/port/user/password/database）。

```powershell
go test -count=1 -v .
```

覆盖：登录、参数化增删查（TestPreparedXxx）、CLOB/BLOB 读取（TestLobRead）、
1.5MB 大 LOB 分块读写（TestLobLarge）、bytea 二进制往返（TestBytea*，含 NUL/
反斜杠/大值）、NULL 绑定、列类型元数据（TestColumnTypes）、日期时间 time.Time
往返（TestPreparedTimestamp/TestTimestampDecode）、大数精度（numeric_test）、
NamedValueChecker（TestNamedValue）、错误分类（TestErrorClassification）、
Savepoint/事务内失败（TestSavepoint/TestTxStatementFailure）、Context 取消
（TestContextCancelQuery）、事务/连接池（TestTx*/TestPool*）。

## 参考（JDBC 反编译源码）

| 主题 | 文件 |
| --- | --- |
| oscarTypeCache / getOscarType | `driver/oscar/decompiled/com/oscar/jdbc/OscarJdbc2Connection.java`（L2425/L723） |
| OID 常量 | 同上（L2212-2318） |
| LOB 读取流程 | `driver/oscar/decompiled/com/oscar/jdbc/OscarLob.java` |
| locator 解析 | `OscarLob.analyzeLocator/setLocatorStr` + `com/oscar/util/Hex.java` |
| 函数 OID 表 | `driver/oscar/decompiled/com/oscar/fastpath/Fastpath.java` |
| Fastpath 包 | `com/oscar/protocol/packets/FunctionCallPacketV2.java` |
| 版本配置 | `driver/oscar/com/oscar/util/versionConfig.properties`（MainVersion=4） |
| 参数绑定 | `OscarStatement.java` / `OscarStatementV2.java`（setObject 按 SQL 类型分发） |

## 待办 / 路线图

### 已完成（P0 基础能力）

- [x] DSN 解析、连接握手、认证（含监听重定向）、协议版本协商
- [x] 会话初始化（autocommit、二进制传输、浮点精度、LOBLOCATOR）
- [x] 简单查询/执行（QueryContext/ExecContext）与 RowsAffected
- [x] 参数化查询（0xA4 PlanID + `p` ParamInfo + Execute 0x0B/0x0D 复用）
- [x] OID→OscarType 映射与全类型行值读取（含 boolean 解码）
- [x] CLOB/BLOB/BFILE 内容读取（Fastpath GETPRECISELENGTH/GET_CHUNKSIZE/READ 分块）
- [x] 大 LOB 参数直通（0xFE 分块编码，1.5MB 实测）
- [x] 结果集列类型元数据（ColumnType* 系列接口）
- [x] 基础事务（Begin/Commit/Rollback）、Pinger

### 进行中：P1 连接健壮性（池与生命周期）

- [x] `SessionResetter`（ResetSession）：连接回池前回滚未完成事务；回滚后连接可复用
- [x] `Validator`（IsValid）：连接健康检查
- [x] `PrepareContext` / `Stmt.ExecContext` / `Stmt.QueryContext`
- [x] `BeginTx` 隔离级别/只读选项映射
- [x] Context 取消时中断服务器端查询（CancelRequest 独立连接 + watcher goroutine）
  （见上方“Context 取消”一节；服务器轮询间隔约 5s，测试 TestContextCancelQuery 通过）

### 已完成（P1）

- 实现了全部 `database/sql` 可选接口（编译期断言见 `driver.go`），
  `keepConnOnRollback` 生效：事务回滚后连接可留在池中复用
  （数据库/sql 要求驱动同时实现 SessionResetter + Validator）。
- `BeginTx` 支持 `sql.TxOptions.Isolation`（serializable 等）与
  `ReadOnly`（映射为 `SET TRANSACTION ISOLATION LEVEL X` /
  `SET TRANSACTION READ ONLY`）。
- `ResetSession` 仅在驱动跟踪到未完成事务时才发 `rollback`，常规路径
  零网络往返。
- Context 取消：`queryPacket` 启动 watcher，取消时经独立 TCP 连接发送
  CancelRequest（`[16][80877102][pid][ckey]`），阻塞查询被中断
  （错误 `[40000] 线程被用户终止`）。

### P2 类型系统完善

- [x] 日期时间解码为 `time.Time`（镜像 JDBC TimestampConverter，7/11 字节二进制格式；timetz 待确认）
- [x] 大数精度：int 族超 int64 时返回精确十进制字符串（math/big 解码，`decodeOscarNumber`）
- [x] `NamedValueChecker`：sql.NullString 等 Valuer 类型原生支持（`CheckNamedValue`）
- [x] bytea/binary/varbinary 二进制参数直通（`GET PARAMINFO` 类型推断 +
  `\ooo` 转义绑定 `escapeBytea` + 读取解码 `decodeByteaEscape`，任意字节无损往返）

### P3 事务与错误处理

- [x] 错误分类：`OscarError`（Code/SQLState/Message）+ `errors.As` +
  `Is*` 分类函数（`IsIntegrityViolation`/`IsNotNullViolation`/`IsUniqueViolation`/
  `IsForeignKeyViolation`/`IsUndefinedTable`/`IsSyntaxError`）
  （实测本服务器 SQLState：NOT NULL 与唯一约束均 `23000`，按消息文本区分；
  未定义表 `42S02`；语法错误类 42）
- [x] Savepoint：`SAVEPOINT`/`ROLLBACK TO` 可用；**`RELEASE SAVEPOINT` 不受支持**
  （服务器报 parser 语法错误，遵循 Oracle 语义无需显式释放）
- [x] 事务内语句失败后的连接状态处理（`STMT_ROLLBACK=1` 生效，事务保持可用，
  TestTxStatementFailure 通过）

### P4 性能与批量（暂不实施）

- [ ] 大结果集流式读取（fetchSize/游标，避免全量缓冲）
- [ ] COPY 快速导入（若服务器支持）
- [ ] 批量插入辅助封装

### P5 高级 / 可选（暂不实施）

- [ ] 网络帧压缩开关（snappy 0xA2/A3 可选关闭）
- [ ] LOB 压缩传输（READCOMPRESS+zlib）DSN 开关（默认 false，与 JDBC 一致）
- [ ] 多结果集（RowsNextResultSet）
- [ ] DSN 扩展：connect_timeout、charset、应用名等
- [ ] TLS/证书支持（若服务器支持）
