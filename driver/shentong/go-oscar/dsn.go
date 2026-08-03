package oscar

import (
	"fmt"
	"net"
	"net/url"
	"strconv"
	"strings"
	"time"
)

type config struct {
	User        string
	Password    string
	Host        string
	Port        int
	Database    string
	DialTimeout time.Duration
}

func parseDSN(dsn string) (cfg config, err error) {
	cfg.DialTimeout = 5 * time.Second
	cfg.Port = 2003

	parts := strings.SplitN(dsn, "@", 2)
	if len(parts) != 2 {
		return cfg, fmt.Errorf("oscar: invalid dsn %q", dsn)
	}

	cred := parts[0]
	target := parts[1]

	credParts := strings.SplitN(cred, "/", 2)
	cfg.User = credParts[0]
	if cfg.User == "" {
		return cfg, fmt.Errorf("oscar: empty user in dsn")
	}
	if len(credParts) == 2 {
		cfg.Password, err = url.PathUnescape(credParts[1])
		if err != nil {
			return cfg, fmt.Errorf("oscar: decode password: %w", err)
		}
	}

	slash := strings.LastIndex(target, "/")
	if slash <= 0 || slash == len(target)-1 {
		return cfg, fmt.Errorf("oscar: invalid target in dsn %q", dsn)
	}

	hostPort := target[:slash]
	cfg.Database = target[slash+1:]
	if cfg.Database == "" {
		return cfg, fmt.Errorf("oscar: empty database in dsn")
	}

	host, port, err := net.SplitHostPort(hostPort)
	if err != nil {
		return cfg, fmt.Errorf("oscar: parse host/port: %w", err)
	}
	cfg.Host = host
	cfg.Port, err = strconv.Atoi(port)
	if err != nil {
		return cfg, fmt.Errorf("oscar: parse port: %w", err)
	}
	return cfg, nil
}

func normalizeIdentifier(v string) string {
	if len(v) >= 2 && v[0] == '"' && v[len(v)-1] == '"' {
		return v[1 : len(v)-1]
	}
	return strings.ToUpper(v)
}
