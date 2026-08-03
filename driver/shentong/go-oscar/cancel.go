package oscar

import (
	"encoding/binary"
	"errors"
	"net"
	"strconv"
	"time"
)

// cancelRequestCode is the PostgreSQL-compatible cancel request code; OSCAR
// mirrors the standard protocol (80877102 = 0x04D2162E).
const cancelRequestCode = 80877102

// cancelRequest asks the server to abort the query currently running on the
// session identified by (pid, ckey). It opens a fresh TCP connection and
// sends a 16-byte cancel packet, mirroring the JDBC driver's
// OSCARProtocol.cancelRequest / CancelRequestPacket. It is best-effort and
// fire-and-forget: the caller decides whether to surface the error.
func (c *conn) cancelRequest(pid, ckey int) error {
	if c.cfg.Host == "" || c.cfg.Port == 0 {
		return errors.New("oscar: no server address for cancel request")
	}
	nc, err := net.DialTimeout("tcp",
		net.JoinHostPort(c.cfg.Host, strconv.Itoa(c.cfg.Port)), 5*time.Second)
	if err != nil {
		return err
	}
	defer nc.Close()
	if err := nc.SetWriteDeadline(time.Now().Add(5 * time.Second)); err != nil {
		return err
	}
	var buf [16]byte
	binary.BigEndian.PutUint32(buf[0:4], 16) // total packet length
	binary.BigEndian.PutUint32(buf[4:8], uint32(cancelRequestCode))
	binary.BigEndian.PutUint32(buf[8:12], uint32(pid))
	binary.BigEndian.PutUint32(buf[12:16], uint32(ckey))
	_, err = nc.Write(buf[:])
	return err
}
