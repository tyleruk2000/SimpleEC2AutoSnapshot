import groovy.json.JsonSlurper
import java.text.DateFormat
import java.text.SimpleDateFormat
import org.codehaus.groovy.runtime.InvokerHelper
class Main extends Script
{
    int totalSnapshots = 5
    Ec2Helper ec2Details
    def jsonSlurper = new JsonSlurper()

    def checkIfAwsCliIsInstalled()
    {
        if (!AwsCliHelper.isAwsCLiInstalled())
        {
            println("Aws CLI not Installed!")
            System.exit(1)
        }
    }

    def checkIfAwsIsConfigured()
    {
        if (!AwsCliHelper.isAwsCliConfigured())
        {
            println("Aws not configured please run aws configure!")
            System.exit(1)
        }

    }

    def getRootVolume()
    {
        def volumes = ec2Details.getVolumes()
        def parsedVolumes = jsonSlurper.parseText(volumes)
        assert parsedVolumes instanceof Map
        assert parsedVolumes.Volumes instanceof List
        return parsedVolumes.Volumes.VolumeId[0]
    }

    def cleanAndBackup()
    {
        boolean cleaning = true
        while(cleaning)
        {
            def snapshots = ec2Details.getSnapshots(rootVolume)
            def parsedSnapshots = jsonSlurper.parseText(snapshots)
            assert parsedSnapshots instanceof Map
            assert parsedSnapshots.Snapshots instanceof List
            println("Snapshot Count:" + parsedSnapshots.Snapshots.size())

            if (parsedSnapshots.Snapshots.size() < totalSnapshots)
            {
                def test  = ec2Details.createSnapshot(rootVolume)
                println("Creating Snapshot:\n" + test)
                cleaning = false;
            }
            else
            {
                DateFormat format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
                Date oldestDate = new Date();
                def oldestId = null;
                for ( snapshot in parsedSnapshots.Snapshots)
                {
                    //noinspection GroovyAssignabilityCheck
                    Date date = format.parse(snapshot.StartTime)
                    println(snapshot)
                    if (date.before(oldestDate))
                    {
                        oldestDate = date
                        oldestId = snapshot.SnapshotId
                    }
                }
                println("Deleting Snapshot: " + oldestId)
                ec2Details.deleteSnapshot(oldestId)
                sleep(1000)
            }
        }
    }

    def handleArgs()
    {
        def cli = new CliBuilder(usage: "EC2 Backup")
        cli.h('display this')
        cli.i(args: 1, argName:'instance id', 'Instance ID')
        cli.s(args: 1, argName:'totalSnapshots', 'total snapshots, default = 5')
        def options = cli.parse(args)

        if (options.h)
        {
            cli.usage()
            System.exit(0)
        }


        if (options.i)
        {
            assert options.i
            ec2Details = new Ec2Helper(options.i)
        }
        else
        {
            ec2Details = new Ec2Helper()
        }

        if (options.s)
        {
            assert options.s
            totalSnapshots = Integer.parseInt(options.s)
        }

    }

    def run()
    {
        //Exit if AWS is not installed or configured
        checkIfAwsCliIsInstalled()
        checkIfAwsIsConfigured()

        handleArgs()

        def rootVolume = getRootVolume()
        println("Root Volume: ${rootVolume}")

        cleanAndBackup()
    }
    static void main(String[] args)
    {
        InvokerHelper.runScript(Main, args)
    }
}