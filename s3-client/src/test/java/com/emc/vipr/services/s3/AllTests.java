package com.emc.vipr.services.s3;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

@RunWith(Suite.class)
@SuiteClasses({ AppendTest.class, BasicS3Test.class,
        S3EncryptionClientTest.class, UpdateTest.class,
        ViPRResponsesSaxParserTest.class, ViPRS3FileAccessTest.class,
        NamespaceTest.class })
public class AllTests {

}
