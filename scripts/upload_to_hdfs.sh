#!/usr/bin/env bash
set -euo pipefail

# =========================
# Config
# =========================
ERP_LOCAL_DIR="./data/landing/erp"
CRM_LOCAL_DIR="./data/landing/crm"

HDFS_ERP_DIR="/data/bronze/erp"
HDFS_CRM_DIR="/data/bronze/crm"
# =========================
# Checks
# =========================
if ! command -v hdfs >/dev/null 2>&1; then
  echo "[ERROR] hdfs command not found"
  exit 1
fi

if [ ! -d "${LOCAL_DATA_DIR}" ]; then
  echo "[ERROR] Local data directory not found: ${LOCAL_DATA_DIR}"
  exit 1
fi
echo "Creating HDFS directories"
hdfs dfs -mkdir -p "${HDFS_ERP_DIR}"
hdfs dfs -mkdir -p "${HDFS_CRM_DIR}"

# =========================
# Upload ERP files
# =========================
if [ -d "${ERP_LOCAL_DIR}" ]; then
  echo "Uploading ERP CSV files"
  find "${ERP_LOCAL_DIR}" -maxdepth 1 -type f -name "*.csv" | while read -r file; do
    filename="$(basename "${file}")"
    echo "Uploading ${filename}"
    hdfs dfs -put -f "${file}" "${HDFS_ERP_DIR}/"
  done
else
  echo "[WARN] ERP directory not found: ${ERP_LOCAL_DIR}"
fi

# =========================
# Upload CRM files
# =========================
if [ -d "${CRM_LOCAL_DIR}" ]; then
  echo "Uploading CRM CSV files"
  find "${CRM_LOCAL_DIR}" -maxdepth 1 -type f -name "*.csv" | while read -r file; do
    filename="$(basename "${file}")"
    echo "Uploading ${filename}"
    hdfs dfs -put -f "${file}" "${HDFS_CRM_DIR}/"
  done
else
  echo "[WARN] CRM directory not found: ${CRM_LOCAL_DIR}"
fi

echo "Upload finished"

echo "HDFS ERP files:"
hdfs dfs -ls "${HDFS_ERP_DIR}" || true

echo "[INFO] HDFS CRM files:"
hdfs dfs -ls "${HDFS_CRM_DIR}" || true