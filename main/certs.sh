#!/usr/bin/env bash
mkdir -p ../certs && cd ../certs
openssl req -x509 -subj "/CN=bootiful-1" -keyout bootiful.key -out bootiful.crt -sha256 -days 365 -nodes -newkey ed25519