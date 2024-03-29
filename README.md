# data-generator
Simple data generator

Runs a sql query against a database and saves the results in an azure storage container.

It can export the results as one of:
* csv
* json
* jsonlines

It obtains its configuration from the following environment variables:
* ETL_DB_URL: jdbc connection url  (e.g. "jdbc:postgresql://localhost:5432")
* ETL_DB_USER_FILE: file containing the db username relative to "/mnt/secrets" (e.g. data-generator/aat-ccd-user)
* ETL_DB_PASSWORD_FILE: file containing the db password relative to "/mnt/secrets" (e.g. data-generator/aat-ccd-pwd)
* ETL_SQL: sql statement to execute (e.g. "SELECT ID FROM parent WHERE ID = 1")


The 2 values: ETL_DB_USER_FILE and ETL_DB_PASSWORD_FILE are useful if the username and password are retrieved 
from Azure keyvault and exposed as flexvolumes. The same username and password can alternatively be passed as environment 
variables (ETL_DB_USER and ETL_DB_PASSWORD).


## Helm chart
The easiest way to run a job is by using the included helm chart which is 
based on [chart-job](https://github.com/hmcts/chart-job). This can be done running the following command:
`helm install hmcts/data-generator-job  --name data-generator-job-001 --namespace mi -f job-values.yaml --wait`
where `job-values.yaml` is:
```yaml
job:
  image: hmcts.azurecr.io/hmcts/data-generator-job:prod-f888e665
  aadIdentityName: mi
  keyVaults:
    "data-generator":
      resourceGroup: data-generator
      secrets:
        - aat-ccdro-user
        - aat-ccdro-password
  labels:
    app.kubernetes.io/instance : data-generator-job-001
    app.kubernetes.io/name: data-generator-job
  environment:
    ETL_DB_URL: jdbc:postgresql://ccd-data-store-api-postgres-db-aat.postgres.database.azure.com:5432/ccd_data_store
    ETL_DB_USER_FILE: data-generator/aat-ccdro-user
    ETL_DB_PASSWORD_FILE: data-generator/aat-ccdro-password
    ETL_SQL: >
      SELECT id, created_date, event_id, summary, description, user_id, case_data_id,
      case_type_id, case_type_version, state_id, user_first_name, user_last_name,
      event_name, state_name, security_classification
      FROM case_event
      WHERE created_date >= (current_date-1 + time '00:00')
      AND created_date < (current_date + time '00:00')
      ORDER BY created_date ASC;
global:
  job:
    kind: Job
  subscriptionId: "1c4f0704-a29e-403d-b719-b90c34ef14c9"
  tenantId: "531ff96d-0ae9-462a-8d2d-bec7c0b42082"
  environment: aat
``` 
