"""Generate explicit development-only configuration; never overwrite user values."""
import os
import secrets
from pathlib import Path

root = Path(__file__).resolve().parents[2]
local = root / ".local"
local.mkdir(exist_ok=True)
configuration = local / "dev.env"
if not configuration.exists():
    values = {
        "DATABASE_URL": "jdbc:postgresql://localhost:5432/safe",
        "DATABASE_USERNAME": "safe",
        "DATABASE_PASSWORD": secrets.token_urlsafe(24),
        "ADMIN_USERNAME": "local-admin",
        "ADMIN_PASSWORD": secrets.token_urlsafe(24),
        "SAFE_PRODUCTION": "false",
        "COOKIE_SECURE": "false",
        "STORAGE_ROOT": str(local / "files"),
    }
    configuration.write_text("\n".join(f"{k}={v}" for k, v in values.items()) + "\n")
    os.chmod(configuration, 0o600)
print("Local configuration available in .local/dev.env. Values are not logged.")
