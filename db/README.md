# BlueMoon database

This directory contains the MySQL schema for the v1.0 fee-management scope:

- user accounts for the management board and accounting;
- households, residents, and residency changes;
- fee types and time-effective fee rates;
- billing periods and household charges;
- charge details and payments.

## Run order

Run the scripts with MySQL 8.0 or later:

```text
01_schema.sql
02_seed.sql
03_test.sql
```

For example:

```bash
mysql -u <user> -p < db/01_schema.sql
mysql -u <user> -p < db/02_seed.sql
mysql -u <user> -p < db/03_test.sql
```

The seed passwords are placeholders. The application must replace them with
password hashes before using the demo accounts.

## Important data rules

- Monetary values use `DECIMAL(12,2)`.
- A charge can have multiple detail lines and multiple payments.
- Charge details store the applied unit price so historical charges do not
  change when a new rate is configured.
- Voluntary contributions use `fee_category = 'VOLUNTARY'` and are kept
  separate from mandatory household debt.
- Residents who move out are retained with `residency_status = 'MOVED_OUT'`.
