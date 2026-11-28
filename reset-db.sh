#!/bin/bash

# Script para resetar o container do PostgreSQL
# Uso: ./reset-db.sh

set -e

CONTAINER_NAME="postgres-cli"
NETWORK_NAME="cli-network"
ENV_FILE="$(dirname "$0")/.env"

# Reads a single KEY from .env without sourcing it (values may contain shell metacharacters).
read_env() {
    local value
    value="$(grep -E "^${1}=" "${ENV_FILE}" 2>/dev/null | tail -n 1 | cut -d= -f2-)" || true
    printf '%s' "${value}"
}

POSTGRES_USER="${CLI_DB_USERNAME:-$(read_env CLI_DB_USERNAME)}"
POSTGRES_PASSWORD="${CLI_DB_PASSWORD:-$(read_env CLI_DB_PASSWORD)}"
if [ -z "${POSTGRES_USER}" ] || [ -z "${POSTGRES_PASSWORD}" ]; then
    echo "❌ CLI_DB_USERNAME and CLI_DB_PASSWORD must be set in the environment or in .env (see .env.example)."
    exit 1
fi
POSTGRES_DB="cli"
POSTGRES_PORT="5432"
POSTGRES_VERSION="17"

echo "=========================================="
echo "🔄 Reset PostgreSQL Container"
echo "=========================================="
echo ""

# Verificar se o container existe e está rodando
if docker ps -a --format '{{.Names}}' | grep -q "^${CONTAINER_NAME}$"; then
    echo "📦 Container '${CONTAINER_NAME}' encontrado."

    # Parar o container se estiver rodando
    if docker ps --format '{{.Names}}' | grep -q "^${CONTAINER_NAME}$"; then
        echo "⏹️  Parando container..."
        docker stop "${CONTAINER_NAME}"
    fi

    # Remover o container
    echo "🗑️  Removendo container..."
    docker rm "${CONTAINER_NAME}"
    echo "✅ Container removido com sucesso!"
else
    echo "ℹ️  Container '${CONTAINER_NAME}' não existe."
fi

echo ""
echo "🌐 Verificando Docker network..."
if docker network ls --format '{{.Name}}' | grep -q "^${NETWORK_NAME}$"; then
    echo "✅ Network '${NETWORK_NAME}' já existe."
else
    echo "📡 Criando network '${NETWORK_NAME}'..."
    docker network create "${NETWORK_NAME}"
    echo "✅ Network criada com sucesso!"
fi

echo ""
echo "🚀 Criando novo container..."
docker run --name "${CONTAINER_NAME}" -d \
    --network "${NETWORK_NAME}" \
    -p "${POSTGRES_PORT}:5432" \
    -e POSTGRES_USER="${POSTGRES_USER}" \
    -e POSTGRES_PASSWORD="${POSTGRES_PASSWORD}" \
    -e POSTGRES_DB="${POSTGRES_DB}" \
    "postgres:${POSTGRES_VERSION}"

echo ""
echo "⏳ Aguardando PostgreSQL inicializar..."
sleep 3

# Verificar se o container está rodando
if docker ps --format '{{.Names}}' | grep -q "^${CONTAINER_NAME}$"; then
    echo ""
    echo "=========================================="
    echo "✅ PostgreSQL resetado com sucesso!"
    echo "=========================================="
    echo ""
    echo "📋 Informações de conexão:"
    echo "   Host: localhost (ou ${CONTAINER_NAME} via Docker network)"
    echo "   Port: ${POSTGRES_PORT}"
    echo "   Database: ${POSTGRES_DB}"
    echo "   User: ${POSTGRES_USER}"
    echo "   Network: ${NETWORK_NAME}"
    echo ""
else
    echo ""
    echo "❌ Erro ao criar o container!"
    exit 1
fi
