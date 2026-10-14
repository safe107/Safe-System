#!/usr/bin/env python3
"""Configure a same-origin Vercel API proxy using a real Railway HTTPS URL."""
import argparse
import json
from pathlib import Path
from urllib.parse import urlsplit

parser = argparse.ArgumentParser()
parser.add_argument("--api-url", required=True)
args = parser.parse_args()
url = args.api_url.rstrip("/")
parsed = urlsplit(url)
if (parsed.scheme != "https" or not parsed.hostname or parsed.username
        or parsed.password or parsed.query or parsed.fragment or parsed.path):
    parser.error("Informe apenas a origem HTTPS real, sem credenciais, caminho ou parâmetros.")
root = Path(__file__).resolve().parents[2]
config_path = root / "apps/web/vercel.json"
config = json.loads(config_path.read_text())
config["rewrites"] = [
    {"source": "/api/:path*", "destination": url + "/api/:path*"},
    *[r for r in config["rewrites"] if r["source"] != "/api/:path*"],
]
config_path.write_text(json.dumps(config, indent=2) + "\n")
print("Proxy configurado em apps/web/vercel.json. Revise e versione a alteração.")
