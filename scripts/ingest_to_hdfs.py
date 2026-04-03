from hdfs import InsecureClient
import glob
import os 
def ingest_to_hdfs(source_path,hdfs_path):
    try:
        client = InsecureClient(url='http://namenode:9870', user='root')
        client.makedirs(hdfs_path)
        csv_files = glob.glob(os.path.join(source_path,'*.csv'))
        if not csv_files:
            raise ValueError(f"no csv files found in {source_path}")
        print(f"csv files found: {len(csv_files)}")
        success_count = 0
        failed_files = []
        for csv_file in csv_files:
            basefilename = os.path.basename(csv_file)
            hdfs_path_put = f"{hdfs_path}/{basefilename}"
            try:
                client.upload(hdfs_path_put,csv_file,overwrite = True)
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
