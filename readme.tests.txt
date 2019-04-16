---------------------------------
| Java REST Testcases for Atmos |
---------------------------------

Configuration
-------------
Before running the testcases, a file named test.properties must be either put on the
classpath (e.g. src/test/resources) or present in your home directory.  The file should
contain the following configuration keys:

Atmos:
atmos.uid - Atmos full token uid (e.g. 123daba33425413251/user1)
atmos.secret - Shared secret key for the uid
atmos.endpoints - Comma separated list of endpoint URIs (more than one is optional)

ACDP:
acdp.admin.endpoint - ACDP admin endpoint, usually http://admin_node:8080
acdp.admin.username - ACDP administrator username
acdp.admin.password - ACDP administrator password
acdp.mgmt.endpoint - ACDP management endpoint, usually http://portal_node:8080
acdp.mgmt.username - ACDP management user (account user)
acdp.mgmt.password - ACDP management password

Atmos Management API:
atmos.mgmt.endpoints - Management REST API endpoints, usually https://<primary-or-secondary-node>:443 (multiple optional)
atmos.mgmt.sysadmin.user - System admin user
atmos.mgmt.sysadmin.password - System admin password
atmos.mgmt.tenant - Existing tenant for testing
atmos.mgmt.tenantadmin.user - Tenant admin user
atmos.mgmt.tenantadmin.password - Tenant admin password

If a particular configuration key is missing, that test group will be skipped.

 
Running the Testcases
---------------------
The tests are run through Maven.  You can run them with:

mvn test

