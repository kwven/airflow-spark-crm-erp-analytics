import glob
import os
from pathlib import Path

from hdfs import InsecureClient


def cleanup_stale_upload_artifacts(client, hdfs_path):
    for entry_name, status in client.list(hdfs_path, status=True):
        if status["type"] == "FILE" and ".temp-" in entry_name:
            stale_path = f"{hdfs_path.rstrip('/')}/{entry_name}"
            client.delete(stale_path)
            print(f"deleted stale HDFS temp file: {stale_path}")


def ingest_to_hdfs(source_path, hdfs_path):
    try:
        client = InsecureClient(url="http://namenode:9870", user="root")
        client.makedirs(hdfs_path)
        cleanup_stale_upload_artifacts(client, hdfs_path)
        csv_files = sorted(glob.glob(os.path.join(source_path, "*.csv")))
        if not csv_files:
            raise ValueError(f"no csv files found in {source_path}")
        print(f"csv files found: {len(csv_files)}")
        success_count = 0
        failed_files = []

        for csv_file in csv_files:
            basefilename = os.path.basename(csv_file)
            hdfs_path_put = f"{hdfs_path.rstrip('/')}/{basefilename}"
            try:
                # Stream directly to the final HDFS path. This avoids the library's temp-file rename flow
                with Path(csv_file).open("rb") as reader:
                    client.write(hdfs_path_put, data=reader, overwrite=True)
                success_count += 1
                print(f"uploaded {basefilename} to {hdfs_path_put}")
            except Exception as e:
                failed_files.append((basefilename, str(e)))

        if failed_files:
            raise ValueError(f"ingestion to HDFS failed for files: {failed_files}")
        return success_count
    except Exception as e:
        print(f"ingestion to HDFS failed: {e}")
        raise e
