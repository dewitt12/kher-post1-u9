#!/usr/bin/env bash
# Reproduce las pruebas de la Parte 2 (JWT). Con la app corriendo: ./scripts/probar-api.sh
# Cada bloque corresponde a una captura de /capturas.
BASE="${BASE:-http://localhost:8080}"
USUARIO="${API_USER:-juan}"
CLAVE="${API_PASS:-User#2026}"   # coincide con app.seed.user-password; cambiar si se sobreescribio

echo "=== 06) POST /api/auth/login (token recibido) ==="
RESP=$(curl -s -X POST "$BASE/api/auth/login" -H "Content-Type: application/json" \
        -d "{\"username\":\"$USUARIO\",\"password\":\"$CLAVE\"}")
echo "$RESP"
TOKEN=$(echo "$RESP" | sed -E 's/.*"token":"([^"]+)".*/\1/')
[ -z "$TOKEN" ] || [ "$TOKEN" = "$RESP" ] && { echo "No se obtuvo token: revisar usuario/clave."; exit 1; }

echo; echo "=== 07) GET /api/tareas con token (200) ==="
curl -i -s "$BASE/api/tareas" -H "Authorization: Bearer $TOKEN"

echo; echo; echo "=== 08) GET /api/tareas SIN token (401) ==="
curl -i -s "$BASE/api/tareas"

echo; echo; echo "=== Extra) token alterado (401) ==="
POS=$(( ${#TOKEN} - 10 ))
CAR="${TOKEN:$POS:1}"; NUEVO=A; [ "$CAR" = "A" ] && NUEVO=B
ALTERADO="${TOKEN:0:$POS}${NUEVO}${TOKEN:$((POS+1))}"   # cambia un caracter de la firma
curl -i -s "$BASE/api/tareas" -H "Authorization: Bearer $ALTERADO"

echo; echo; echo "=== Extra) token de USER en ruta ADMIN (403) ==="
curl -i -s "$BASE/api/tareas/todas" -H "Authorization: Bearer $TOKEN"
echo
