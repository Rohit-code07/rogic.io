# Disaster Recovery (DR) & Database Migration Drill Guide

This guide details the procedures for database migration (to isolated EBS volume) and executing database restoration drills (using GitHub Actions and S3 backups) for both **Staging** and **Production** environments.

---

## 1. Hybrid Disaster Recovery Strategy

Our DR architecture utilizes a hybrid approach:
* **1st Defense (Infrastructure Level - RTO ≈ 1~2 min)**: EBS volume is isolated from the EC2 lifecycle (`delete_on_termination = false` & independent resource definition). If the instance crashes, is rebooted, or terminated, the storage persists and can be re-attached instantly.
* **2nd Defense (Data Level - RTO ≈ 10~20 min)**: Regular 6-hour interval backups (`pg_dump` compressed as `.sql.gz`) are pushed to S3. Restorations can be triggered via GitHub Actions (`db-restore.yml`) without ssh/login, measuring actual realized RTO.

---

## 2. One-Time Database Migration (to Isolated EBS Volume)

When applying the new Terraform configuration, a new 10GB EBS volume is provisioned. To prevent data loss, the existing PostgreSQL data must be migrated from the legacy Docker volume to the new EBS volume.

### Migration Step-by-Step

#### Step 2.1: Suspend Backend Applications
Stop active backend containers to prevent write operations and lock database transactions:
```bash
docker compose -f /opt/nemologic/docker-compose.prod.yml stop backend-blue backend-green backend-stage || true
```

#### Step 2.2: Identify the Legacy and New Volume Paths
* **Legacy Docker Volume Path**: `/var/lib/docker/volumes/nemologic_postgres_data/_data`
* **New EBS Mount Path**: `/opt/nemologic/db_data` (Mounted automatically by Ansible)

#### Step 2.3: Data Synchronization
Copy existing Postgres physical data files to the newly mounted EBS volume with metadata and permissions preserved:
```bash
sudo rsync -aHAXxv /var/lib/docker/volumes/nemologic_postgres_data/_data/ /opt/nemologic/db_data/
```
Verify the contents in `/opt/nemologic/db_data/` (should contain `PG_VERSION`, `base`, `global`, etc.).

#### Step 2.4: Switch Docker Compose Configurations & Restart
Run Ansible deployment to deploy the updated `docker-compose.prod.yml` (which uses bind mount `/opt/nemologic/db_data`) and launch the PostgreSQL container:
```bash
docker compose -f /opt/nemologic/docker-compose.prod.yml up -d db
```
Wait for DB healthcheck status to become `healthy`:
```bash
docker ps --filter "name=nemologic-db"
```

#### Step 2.5: Restart Backends & Verify
Startup backend services and verify through logs:
```bash
docker compose -f /opt/nemologic/docker-compose.prod.yml start backend-blue backend-green backend-stage || true
```
Verify page retrieval and logs to ensure the database operates correctly.

---

## 3. Disaster Recovery Restoration Procedure (S3 Dump Backup)

Use this when data is corrupted (e.g. database dropped, malicious payload injection) or when the EBS volume is completely corrupted/lost.

### Scenario A: Target EC2 is Online, SSM Agent is Healthy
1. Go to the GitHub repository and select the **Actions** tab.
2. Select the **Database Disaster Recovery (Restore)** workflow.
3. Click **Run workflow**.
4. Configure parameters:
   * **environment**: Select `staging` or `production`.
   * **backup_file** (Optional): Provide specific `.sql.gz` file name. Leave blank to restore the **latest** backup.
5. Click **Run workflow** and monitor the execution logs. The workflow automatically measures and logs the realized RTO (Recovery Time Objective).

### Scenario B: Target EC2 is Terminated/Missing (Complete Instance Rebuild)
If the EC2 instance is terminated or deleted:
1. **Rebuild Infrastructure**: Run Terraform to recreate the security groups, VPC routing, and the EC2 instance with the EBS attachment.
   * `staging`: Push changes to trigger GitHub actions or manually execute `terraform apply`.
2. **Setup Stacks**: Run CI/CD deployment or manually run the Ansible playbook to configure Docker, SSL certificates, Nginx, and launch the empty `nemologic-db` container.
3. **Execute Restore**: Once the SSM Agent on the newly provisioned instance is online (System Status: healthy), execute **Scenario A** to restore the data from the S3 backup bucket.

---

## 4. Manual Verification Drill (DR Drill Guide)

To verify the restoration pipeline is valid, SREs should perform periodic drills on the **Staging** environment.

### 1. Perform Backup
On the Staging host, run the daily backup script to upload a mock drill backup:
```bash
sudo /opt/nemologic/backups/backup_db.sh
```

### 2. Simulate Disaster (Data Truncation)
Truncate the main database tables to simulate complete data loss:
```bash
docker exec -i nemologic-db psql -U postgres -d nemologic -c "TRUNCATE TABLE stages CASCADE;"
```
Access the staging website (`https://stage.rogic.io/`) and verify that `/api/stages` returns `[]` (no puzzles displayed).

### 3. Run Restoration Pipeline
1. Trigger the **Database Disaster Recovery (Restore)** workflow via GitHub Actions, selecting `staging` environment.
2. Watch the logs. Verify that:
   * Staging backend stops automatically.
   * Empty database is recreated.
   * Streaming restore succeeds.
   * Backend restarts and healthy checks pass.
   * Realized RTO is printed.

### 4. Verify Recovery
Access the staging website and verify that all puzzle grids and user profiles are completely restored and operational.
