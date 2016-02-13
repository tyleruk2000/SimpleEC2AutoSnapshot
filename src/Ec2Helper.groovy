class Ec2Helper
{
    private def instanceId;

    Ec2Helper()
    {
        instanceId = getInstanceID()
    }

    Ec2Helper(instanceId)
    {
        this.instanceId = instanceId
    }

    def getInstanceId()
    {
        return instanceId
    }

    public String getVolumes()
    {
        return "aws ec2 describe-volumes --filters Name=attachment.instance-id,Values=${instanceId}".execute().getText()
    }

    public static String getSnapshots(String rootVolume)
    {
        return "aws ec2 describe-snapshots --filters Name=volume-id,Values=${rootVolume} Name=description,Values=autoBackup".execute().getText()
    }

    public static String createSnapshot(String rootVolume)
    {
        return "aws ec2 create-snapshot --volume-id ${rootVolume} --description autoBackup".execute().getText()
    }

    public static void deleteSnapshot(String snapshotId)
    {
        "aws ec2 delete-snapshot --snapshot-id ${snapshotId}".execute()
    }

    static String getInstanceID(int connectTimeout = 100, int readTimeout = 100)
    {
        //noinspection GroovyUnusedCatchParameter
        try
        {
            return new URL("http://169.254.169.254/latest/meta-data/instance-id").getText([connectTimeout: connectTimeout, readTimeout: readTimeout])
        }
        catch (SocketTimeoutException)
        {
            println("Cannot get instance-id from meta-data service")
            System.exit(1)
        }
        return ""
    }
}
