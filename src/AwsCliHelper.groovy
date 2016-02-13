class AwsCliHelper
{
    static boolean isAwsCLiInstalled()
    {
        boolean awsInstalled = false;
        if (new File('/usr/local/bin/aws').exists())
            awsInstalled = true
        if (new File('/usr/bin/aws').exists())
            awsInstalled = true
        return awsInstalled
    }

    static boolean isAwsCliConfigured()
    {
        if ("aws s3 ls".execute().waitFor() == 255)
            return false
        return true
    }
}
