/**
 * 
 */
package test.com.juxtapose.example.own;

import org.springframework.batch.core.JobParametersBuilder;
import test.com.juxtapose.example.JobLaunchBase;

import java.util.Date;

/**
 * 
 * @author bruce.liu(mailto:jxta.liu@gmail.com)
 * 2013-11-16下午10:59:46
 */
public class JobLaunchPartitionDB2 {
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		JobLaunchBase.executeJob("own/job/job-partition-db2.xml", "myPartitionJob",
				new JobParametersBuilder().addDate("date", new Date()));
	}
}
